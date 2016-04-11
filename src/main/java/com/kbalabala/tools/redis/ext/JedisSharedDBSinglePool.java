package com.kbalabala.tools.redis.ext;

import com.kbalabala.tools.redis.common.JedisDbNameAndIndex;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Protocol;
import redis.clients.util.Pool;

import java.util.*;
import java.util.logging.Logger;

/**
 * <p>
 *  支持Redis单实例多库
 * </p>
 *
 * @author kevin
 * @since 2015-08-04 09:35
 */
public class JedisSharedDBSinglePool extends Pool<Jedis> {

    protected Logger log = Logger.getLogger(getClass().getName());

    private List<JedisDbNameAndIndex> dbs = new ArrayList<>();

    private Map<String, JedisPool> poolMap = new HashMap<>();

    private String host = null;

    private int port = Protocol.DEFAULT_PORT;

    protected GenericObjectPoolConfig poolConfig;

    protected int timeout = Protocol.DEFAULT_TIMEOUT;

    protected String password;

    private static final JedisDbNameAndIndex DEFAULT_DB = new JedisDbNameAndIndex("DEFAULT", 0);

    private JedisDbNameAndIndex defaultChoiceOnNoneSpecify = null;

    public JedisDbNameAndIndex getDefaultChoiceOnNoneSpecify() {
        return defaultChoiceOnNoneSpecify;
    }

    public JedisSharedDBSinglePool(final GenericObjectPoolConfig poolConfig,
                                   final String host,
                                   int port,
                                   int timeout,
                                   final String password,
                                   List<JedisDbNameAndIndex> dbs) {

        this.poolConfig = poolConfig;
        this.host = host;
        this.port = port;
        this.timeout = timeout;
        this.password = password;
        this.dbs = dbs == null || dbs.size() <= 0 ? Arrays.asList(DEFAULT_DB) : dbs;
        setDefaultChoiceDb(this.dbs);
        initPool();
    }

    /**
     * 初始化制定的DB ON Redis
     */
    private void initPool() {
        for(JedisDbNameAndIndex db : dbs) {
            poolMap.put(db.getName(), new JedisPool(poolConfig, host, port, timeout, password, db.getIndex(), null));
        }
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


    public Jedis getResource(String dbName) {
        return getDbPool(dbName).getResource();
    }

    public void returnBrokenResource(String dbName, final Jedis resource) {
        getDbPool(dbName).returnBrokenResource(resource);
    }

    public void returnResource(String dbName, final Jedis resource) {
        getDbPool(dbName).returnResource(resource);
    }

    private JedisPool getDbPool(String dbName){
        if(poolMap.get(dbName) == null) throw new RuntimeException(String.format("you specified db[%1s] on redis is not existed", dbName));
        return poolMap.get(dbName);
    }

}
