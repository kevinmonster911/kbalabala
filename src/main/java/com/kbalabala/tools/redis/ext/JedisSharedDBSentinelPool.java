package com.kbalabala.tools.redis.ext;

import com.kbalabala.tools.redis.common.JedisDbNameAndIndex;
import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;
import redis.clients.jedis.Protocol;
import redis.clients.jedis.exceptions.JedisConnectionException;
import redis.clients.util.Pool;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

/**
 * <p>
 *      Jedis哨兵监听多库连接池
 * </p>
 *
 * @author kevin
 * @since 15-5-13
 */
public class JedisSharedDBSentinelPool extends Pool<Jedis> {

    protected GenericObjectPoolConfig poolConfig;

    protected int timeout = Protocol.DEFAULT_TIMEOUT;

    protected String password;

    private List<JedisDbNameAndIndex> dbs = new ArrayList<>();

    protected Set<MasterListener> masterListeners = new HashSet<MasterListener>();

    protected Logger log = Logger.getLogger(getClass().getName());

    private Map<String, InnerSharedPool> poolMap = new HashMap<>();

    private static final JedisDbNameAndIndex DEFAULT_DB = new JedisDbNameAndIndex("DEFAULT", 0);

    private JedisDbNameAndIndex defaultChoiceOnNoneSpecify = null;

    public JedisDbNameAndIndex getDefaultChoiceOnNoneSpecify() {
        return defaultChoiceOnNoneSpecify;
    }

    public JedisSharedDBSentinelPool(String masterName,
                                     Set<String> sentinels,
                                     GenericObjectPoolConfig poolConfig,
                                     int timeout,
                                     String password,
                                     List<JedisDbNameAndIndex> dbs) {

        this.poolConfig = poolConfig;
        this.timeout = timeout;
        this.password = password;
        this.dbs = dbs == null || dbs.size() <= 0 ? Arrays.asList(DEFAULT_DB) : dbs;

        setDefaultChoiceDb(dbs); // 设定默认DB在没有制定的情况下
        HostAndPort master = initSentinels(sentinels, masterName);
        initPool(master);
    }

    private void setDefaultChoiceDb(List<JedisDbNameAndIndex> dbs){
        Collections.sort(dbs, new Comparator<JedisDbNameAndIndex>() {
            @Override
            public int compare(JedisDbNameAndIndex db1, JedisDbNameAndIndex db2) {
                return db1.getIndex() - db2.getIndex();
            }
        });
        defaultChoiceOnNoneSpecify = dbs.get(0);
    }

    /**
     * 初始化制定的DB ON Redis
     * @param master
     */
    private void initPool(HostAndPort master) {
        if (!master.equals(currentHostMaster)) {
            currentHostMaster = master;
            log.info("Created JedisPool to master at " + master);

            for(JedisDbNameAndIndex db : dbs) {
                poolMap.put(db.getName(),
                        new InnerSharedPool(poolConfig,
                                new JedisFactory(master.getHost(), master.getPort(), timeout, password, db.getIndex())));

            }
        }
    }

    public Jedis getResource(String dbName) {
        return getDbPool(dbName).getResource();
    }

    public void returnBrokenResource(String dbName, final Jedis resource) {
        getDbPool(dbName).returnBrokenResource(resource);
    }

    public void returnResource(String dbName, final Jedis resource) {
        getDbPool(dbName).returnResource(resource);
    }

    private InnerSharedPool getDbPool(String dbName){
        if(poolMap.get(dbName) == null) throw new RuntimeException(String.format("you specified db[%1s] on redis is not existed", dbName));
        return poolMap.get(dbName);
    }

    /**
     * 关闭哨兵监听器
     */
    public void destroy() {
        for (MasterListener m : masterListeners) {
            m.shutdown();
        }

        for(Map.Entry<String, InnerSharedPool> entry : poolMap.entrySet()){
            entry.getValue().destroy();
        }
    }


    /**
     * 重写Pool的数据源相关接口
     */
    private static class InnerSharedPool extends Pool<Jedis> {

        public InnerSharedPool(GenericObjectPoolConfig poolConfig, PooledObjectFactory<Jedis> factory) {
            super(poolConfig, factory);
        }

        public Jedis getResource() {
            Jedis jedis = super.getResource();
            jedis.setDataSource(this);
            return jedis;
        }

        public void returnBrokenResource(final Jedis resource) {
            if (resource != null) {
                returnBrokenResourceObject(resource);
            }
        }

        public void returnResource(final Jedis resource) {
            if (resource != null) {
                resource.resetState();
                returnResourceObject(resource);
            }
        }

    }

    private volatile HostAndPort currentHostMaster;

    public HostAndPort getCurrentHostMaster() {
        return currentHostMaster;
    }

    private HostAndPort toHostAndPort(List<String> getMasterAddrByNameResult) {
        String host = getMasterAddrByNameResult.get(0);
        int port = Integer.parseInt(getMasterAddrByNameResult.get(1));
        return new HostAndPort(host, port);
    }

    /**
     * 初始化哨兵监听器
     * @param sentinels
     * @param masterName
     * @return
     */
    private HostAndPort initSentinels(Set<String> sentinels,
                                      final String masterName) {

        HostAndPort master = null;
        boolean running = true;

        outer: while (running) {

            log.info("Trying to find master from available Sentinels...");

            for (String sentinel : sentinels) {

                final HostAndPort hap = toHostAndPort(Arrays.asList(sentinel
                        .split(":")));

                log.fine("Connecting to Sentinel " + hap);

                try {
                    Jedis jedis = new Jedis(hap.getHost(), hap.getPort());

                    if (master == null) {
                        master = toHostAndPort(jedis
                                .sentinelGetMasterAddrByName(masterName));
                        log.fine("Found Redis master at " + master);
                        jedis.disconnect();
                        break outer;
                    }
                } catch (JedisConnectionException e) {
                    log.warning("Cannot connect to sentinel running @ " + hap
                            + ". Trying next one.");
                }
            }

            try {
                log.severe("All sentinels down, cannot determine where is "
                        + masterName + " master is running... sleeping 1000ms.");
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        log.info("Redis master running at " + master
                + ", starting Sentinel listeners...");

        for (String sentinel : sentinels) {
            final HostAndPort hap = toHostAndPort(Arrays.asList(sentinel
                    .split(":")));
            MasterListener masterListener = new MasterListener(masterName,
                    hap.getHost(), hap.getPort());
            masterListeners.add(masterListener);
            masterListener.start();
        }

        return master;
    }

    /**
     * 默认无任何实现的发布/订阅
     */
    protected class JedisPubSubAdapter extends JedisPubSub {
        @Override
        public void onMessage(String channel, String message) {
        }

        @Override
        public void onPMessage(String pattern, String channel, String message) {
        }

        @Override
        public void onPSubscribe(String pattern, int subscribedChannels) {
        }

        @Override
        public void onPUnsubscribe(String pattern, int subscribedChannels) {
        }

        @Override
        public void onSubscribe(String channel, int subscribedChannels) {
        }

        @Override
        public void onUnsubscribe(String channel, int subscribedChannels) {
        }
    }

    /**
     * 哨兵监听器－额外的线程用于监听哨兵
     */
    protected class MasterListener extends Thread {

        protected String masterName;
        protected String host;
        protected int port;
        protected long subscribeRetryWaitTimeMillis = 5000;
        protected Jedis j;
        protected AtomicBoolean running = new AtomicBoolean(false);

        protected MasterListener() {
        }

        public MasterListener(String masterName, String host, int port) {
            this.masterName = masterName;
            this.host = host;
            this.port = port;
        }

        public MasterListener(String masterName, String host, int port,
                              long subscribeRetryWaitTimeMillis) {
            this(masterName, host, port);
            this.subscribeRetryWaitTimeMillis = subscribeRetryWaitTimeMillis;
        }

        public void run() {

            running.set(true);

            while (running.get()) {

                j = new Jedis(host, port);

                try {
                    j.subscribe(new JedisPubSubAdapter() {
                        @Override
                        public void onMessage(String channel, String message) {
                            log.fine("Sentinel " + host + ":" + port
                                    + " published: " + message + ".");

                            String[] switchMasterMsg = message.split(" ");

                            if (switchMasterMsg.length > 3) {

                                if (masterName.equals(switchMasterMsg[0])) {
                                    initPool(toHostAndPort(Arrays.asList(
                                            switchMasterMsg[3],
                                            switchMasterMsg[4])));
                                } else {
                                    log.fine("Ignoring message on +switch-master for master name "
                                            + switchMasterMsg[0]
                                            + ", our master name is "
                                            + masterName);
                                }

                            } else {
                                log.severe("Invalid message received on Sentinel "
                                        + host
                                        + ":"
                                        + port
                                        + " on channel +switch-master: "
                                        + message);
                            }
                        }
                    }, "+switch-master");

                } catch (JedisConnectionException e) {

                    if (running.get()) {
                        log.severe("Lost connection to Sentinel at " + host
                                + ":" + port
                                + ". Sleeping 5000ms and retrying.");
                        try {
                            Thread.sleep(subscribeRetryWaitTimeMillis);
                        } catch (InterruptedException e1) {
                            e1.printStackTrace();
                        }
                    } else {
                        log.fine("Unsubscribing from Sentinel at " + host + ":"
                                + port);
                    }
                }
            }
        }

        public void shutdown() {
            try {
                log.fine("Shutting down listener on " + host + ":" + port);
                running.set(false);
                // This isn't good, the Jedis object is not thread safe
                j.disconnect();
            } catch (Exception e) {
                log.severe("Caught exception while shutting down: "
                        + e.getMessage());
            }
        }
    }
}
