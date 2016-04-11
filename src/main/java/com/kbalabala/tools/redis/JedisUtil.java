package com.kbalabala.tools.redis;

import com.kbalabala.tools.JmbStringUtils;
import com.kbalabala.tools.redis.common.JedisDbNameAndIndex;
import com.kbalabala.tools.redis.ext.JedisSharedDBSentinelPool;
import com.kbalabala.tools.redis.ext.JedisSharedDBSinglePool;
import org.apache.log4j.Logger;
import redis.clients.jedis.*;
import redis.clients.util.Pool;
import redis.clients.util.SafeEncoder;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JedisUtil {

    public static class RedisResourceUnAvailableException extends RuntimeException{
        public RedisResourceUnAvailableException(String message) {
            super(message);
        }
    }

    private static final Logger logger = Logger.getLogger(JedisUtil.class);

    private static Mode mode = Mode.SINGLE;

    /** 操作Key的方法 */
    private Keys keys = new Keys();

    /** 对存储结构为String类型的操作 */
    private Strings STRINGS;

    /** 对存储结构为List类型的操作 */
    private Lists LISTS = new Lists();

    /** 对存储结构为Set类型的操作 */
    private Sets SETS;

    /** 对存储结构为HashMap类型的操作 */
    private Hash HASH = new Hash();

    /** 对存储结构为Set(排序的)类型的操作 */
    private SortSet SORTSET;

    //pool
    private static Pool<Jedis> jedisPool = null;

    private RedisConfiguration configuration;

    private volatile boolean initial = false;

    private JedisUtil(RedisConfiguration configuration) {
        if(!initial) {
            init(configuration);
        }
    }

    /**
     * 初始化连接配置信息并建立连接
     */
    private synchronized void init(RedisConfiguration configuration){
        if(!initial){

            RedisConfiguration rconfig =
                    configuration == null ? RedisConfig.getRedisConfiguration() : configuration;
            if(rconfig == null || !rconfig.isConfig()) throw new RuntimeException("无效的Redis配置信息");
            this.configuration = rconfig;

            JedisPoolConfig config = new JedisPoolConfig();
            config.setMaxTotal(rconfig.getRedisMaxActive());
            config.setMaxIdle(rconfig.getRedisMaxIdle());
            config.setMaxWaitMillis(rconfig.getRedisMaxWaitMillSeconds());
            config.setTestOnBorrow(rconfig.isRedisTestOnBorrow());
            config.setTestOnReturn(rconfig.isRedisTestOnReturn());
            String databases = rconfig.getRedisDatabases();

            //set sentinels
            String redisSentinelsAddr = rconfig.getRedisSentinelsAddr();

            mode = determineMode(redisSentinelsAddr);

            switch (mode) {
                case SINGLE:
                    connectOnSingle(rconfig, databases, config);
                    break;
                case MULTI:
                    connectOnMulti(rconfig, databases, config);
                    break;
                default:
                    throw new RuntimeException("invalid mode on config");
            }

            if (jedisPool == null) {
                throw new RuntimeException("Redis Pool无法生成，请检查你的Redis配置。");
            }

            initial = true;
        }
    }

    public String defaultDB(){
        switch (mode) {
            case SINGLE:
                throw new RuntimeException("很抱歉单实例连接方式暂时不支持多库");
            case MULTI:
                return ((JedisSharedDBSentinelPool)jedisPool).getDefaultChoiceOnNoneSpecify().getName();
            default:
                throw new RuntimeException("invalid mode on config");
        }
    }

    public static String getCurrentMaster(){
        if(jedisPool != null && mode == Mode.MULTI){
            HostAndPort hostAndPort = ((JedisSharedDBSentinelPool)jedisPool).getCurrentHostMaster();
            if(hostAndPort != null){
                return hostAndPort.toString();
            }
        }
        return null;
    }

    /**
     * 通过哨兵连接实例
     * @param configuration
     * @param dbs
     * @param config
     */
    private static void connectOnMulti(RedisConfiguration configuration,
                                       String dbs,
                                       JedisPoolConfig config){

        String[] sentinelsArray = configuration.getRedisSentinelsAddr().split(";");
        if(sentinelsArray == null || sentinelsArray.length == 0){
            throw new RuntimeException("Redis 哨兵地址配置错误，合法格式是：xx1:xx1:xx1:xx1:port1;xx2:xx2:xx2:xx2:port2;xx3:xx3:xx3:xx3:port3...，请检查你的Redis配置。");
        }
        Set<String> sentinels = new HashSet<>(Arrays.asList(sentinelsArray));

        //get master names
        if(JmbStringUtils.isBlank(configuration.getRedisMasterName())){
            throw new RuntimeException("Redis Master名字配置错误，请检查你的Redis配置。");
        }

        jedisPool = new JedisSharedDBSentinelPool(configuration.getRedisMasterName(),
                sentinels,
                config,
                configuration.getRedisPoolTimeoutMs(),
                JmbStringUtils.isBlank(configuration.getRedisConnectPassword())?null:configuration.getRedisConnectPassword(),
                parseDbs(dbs));
    }

    /**
     * 直连单redis实例
     * @param configuration
     * @param config
     */
    private static void connectOnSingle(RedisConfiguration configuration,
                                        String dbs,
                                       JedisPoolConfig config){

        String redisServerAddr = configuration.getRedisServerAddr();
        Integer redisServerPort = configuration.getRedisServerPort();
        Integer timeout = configuration.getRedisPoolTimeoutMs();

        if(JmbStringUtils.isBlank(redisServerAddr) ||
                redisServerPort == null) {
            throw new IllegalArgumentException("无效的Redis实例地址/端口，请正确配置你的Redis实例地址和端口");
        }

        jedisPool = new JedisSharedDBSinglePool(
                config,
                redisServerAddr,
                redisServerPort,
                timeout,
                JmbStringUtils.isBlank(configuration.getRedisConnectPassword())?null:configuration.getRedisConnectPassword(),
                parseDbs(dbs));
    }

    /**
     * 客户端的连接方式 1）直连单redis实例 2）通过哨兵连接实例
     */
    private enum Mode {
        SINGLE,
        MULTI;
    }

    /**
     * 判断redis客户端的连接方式 1）直连单redis实例 2）通过哨兵连接实例
     * @param redisSentinelAddr
     * @return
     */
    private static Mode determineMode(String redisSentinelAddr){
        if(!JmbStringUtils.isBlank(redisSentinelAddr)){
            return Mode.MULTI;
        }
        return Mode.SINGLE;

    }

    /**
     * REDIS_DATABASES格式:xxxx_数字(0-15),如: seesion_0, cache_1
     */
    private static Pattern REDIS_DATABASES_PATTERN = Pattern.compile("(^[a-zA-Z]+)_([0-9]|1[0-5])$");

    /**
     * 解析配置配置文件中Redis数据库配置
     * @param dbs
     * @return
     */
    private static List<JedisDbNameAndIndex> parseDbs(String dbs){
        if(JmbStringUtils.isBlank(dbs)) return null;
        String[] dbPairs = JmbStringUtils.split(dbs, ",");
        List<JedisDbNameAndIndex> dbSet = new ArrayList<>();
        for(String dbPair : dbPairs) {
            Matcher matcher = REDIS_DATABASES_PATTERN.matcher(dbPair);
            if(matcher.find()){
                dbSet.add(new JedisDbNameAndIndex(matcher.group(1), Integer.parseInt(matcher.group(2))));
            } else {
                throw new RuntimeException(String.format("配置信息［%1s］存在无效的配置项[%2s]", "REDIS_DATABASES", dbPair));
            }
        }
        return dbSet;
    }

    public <T extends Pool<Jedis>> T getPool() {
        return (T)jedisPool;
    }

    public void shutdownRedisPool(){
        if(jedisPool != null){
            jedisPool.destroy();
        }
    }


    public void returnJedis(Jedis jedis) {
        returnJedis(defaultDB(), jedis);
    }

    public void returnJedis(String dbName, Jedis jedis) {
        dbName = JmbStringUtils.isBlank(dbName) ? defaultDB() : dbName;
        switch (mode) {
            case SINGLE:
                returnJedisOnSingle(dbName, jedis);
                break;
            case MULTI:
                returnJedisOnMulti(dbName, jedis);
                break;
            default:
                return;
        }

    }

    public void returnJedisOnSingle(String dbName, Jedis jedis) {
        JedisSharedDBSinglePool pool = (JedisSharedDBSinglePool)jedisPool;
        if(pool != null && jedis != null){
            pool.returnResource(dbName, jedis);
        }
    }

    public void returnJedisOnMulti(String dbName, Jedis jedis) {
        JedisSharedDBSentinelPool pool = (JedisSharedDBSentinelPool)jedisPool;
        if(pool != null && jedis != null){
            pool.returnResource(dbName, jedis);
        }
    }

    public Jedis getJedis() {
        return getJedis(defaultDB());
    }

    public Jedis getJedis(String dbName) {
        dbName = JmbStringUtils.isBlank(dbName) ? defaultDB() : dbName;
        switch (mode) {
            case SINGLE:
                return getJedisOnSingle(dbName);
            case MULTI:
                return getJedisOnMulti(dbName);
            default:
                return null;
        }
    }

    public Jedis getJedisOnSingle(String dbName) {
        Jedis jedis  = null;
        int count =0;
        JedisSharedDBSinglePool pool = (JedisSharedDBSinglePool)jedisPool;
        do {
            try {
                jedis = pool.getResource(dbName);
            } catch (Exception e) { //error occurred, sleep and retry max 10 times: 5*10=50 seconds
                pool.returnBrokenResource(dbName, jedis);
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e1) {
                }
            }
            count++;
        } while(jedis == null && count < configuration.getRedisConnectRetryNumber());

        //check is null
        if(jedis == null){
            RedisResourceUnAvailableException redisResourceUnAvailableException = new RedisResourceUnAvailableException("Redis resource is not available");
            logger.error("Redis resource is not available", redisResourceUnAvailableException);
            throw redisResourceUnAvailableException;
        }

        return jedis;
    }

    public Jedis getJedisOnMulti(String dbName) {
        Jedis jedis  = null;
        int count =0;
        JedisSharedDBSentinelPool pool = (JedisSharedDBSentinelPool)jedisPool;
        do {
            try {
                jedis = pool.getResource(dbName);
            } catch (Exception e) {
                pool.returnBrokenResource(dbName, jedis);
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e1) {
                }
            }
            count++;
        } while(jedis == null && count < configuration.getRedisConnectRetryNumber());

        //check is null
        if(jedis == null){
            RedisResourceUnAvailableException redisResourceUnAvailableException = new RedisResourceUnAvailableException("Redis resource is not available");
            logger.error("Redis resource is not available", redisResourceUnAvailableException);
            throw redisResourceUnAvailableException;
        }

        return jedis;
    }

    private static volatile JedisUtil jedisUtil = null;
    private static Object lock = new Object();

    /**
     * 获取JedisUtil实例
     * @return
     */
    public static JedisUtil getInstance(RedisConfiguration configuration) {
        if(jedisUtil == null){
            synchronized (lock) {
                if (jedisUtil == null) {
                    jedisUtil = new JedisUtil(configuration);
                }
            }
        }
        return jedisUtil;
    }



    //*******************************************Keys*******************************************//
    public class Keys {

        private String dbName = null;
        public Keys(){}
        public Keys(String dbName){
            this.dbName = dbName;
        }

        public String flushAll(){
            return flushAll(this.dbName);
        }

        /**
         * 清空所有key
         */
        private String flushAll(String dbName) {
            Jedis jedis = getJedis(dbName);
            String stata = jedis.flushAll();
            returnJedis(dbName, jedis);
            return stata;
        }

        public String rename(String oldkey, String newkey) {
            return rename(this.dbName, oldkey, newkey);
        }

        /**
         * 更改key
         * @param oldkey
         * @param  newkey
         * @return 状态码
         * */
        private String rename(String dbName, String oldkey, String newkey) {
            return rename(dbName, SafeEncoder.encode(oldkey),
                    SafeEncoder.encode(newkey));
        }

        /**
         * 更改key
         * @param oldkey
         * @param newkey
         * @return 状态码
         * */
        public String rename(String dbName, byte[] oldkey, byte[] newkey) {
            Jedis jedis = getJedis(dbName);
            String status = jedis.rename(oldkey, newkey);
            returnJedis(dbName, jedis);
            return status;
        }

        /**
         * 更改key,仅当新key不存在时才执行
         * @param oldkey
         * @param newkey
         * @return 状态码
         * */
        public long renamenx(String oldkey, String newkey) {
            return renamenx(this.dbName, oldkey, newkey);
        }

        private long renamenx(String dbName, String oldkey, String newkey) {
            Jedis jedis = getJedis(dbName);
            long status = jedis.renamenx(oldkey, newkey);
            returnJedis(dbName, jedis);
            return status;
        }

        /**
         * 设置key的过期时间，以秒为单位
         * @param key
         * @param已秒为单位
         * @return 影响的记录数
         * */
        public long expired(String key, int seconds) {
            return expired(this.dbName, key, seconds);
        }

        private long expired(String dbName, String key, int seconds) {
            Jedis jedis = getJedis(dbName);
            long count = jedis.expire(key, seconds);
            returnJedis(dbName, jedis);
            return count;
        }

        /**
         * 设置key的过期时间,它是距历元（即格林威治标准时间 1970 年 1 月 1 日的 00:00:00，格里高利历）的偏移量。
         * @param key
         * @param timestamp
         * @return 影响的记录数
         * */
        public long expireAt(String key, long timestamp) {
            return expireAt(dbName, key, timestamp);
        }

        private long expireAt(String dbName, String key, long timestamp) {
            Jedis jedis = getJedis(dbName);
            long count = jedis.expireAt(key, timestamp);
            returnJedis(dbName, jedis);
            return count;
        }

        /**
         * determine if key exist
         * @param key
         * @return
         */
        public boolean exists(String key) {
            return exists(dbName, key);
        }

        private boolean exists(String dbName, String key) {
            Jedis jedis = getJedis(dbName);
            boolean exis = jedis.exists(key);
            returnJedis(dbName, jedis);
            return exis;
        }

        /**
         * 查询key的过期时间
         * @param key
         * @return 以秒为单位的时间表示
         * */
        public long ttl(String key) {
            return ttl(dbName, key);
        }

        private long ttl(String dbName, String key) {
            Jedis jedis = getJedis(dbName);
            long len = jedis.ttl(key);
            returnJedis(dbName, jedis);
            return len;
        }

        /**
         * 取消对key过期时间的设置
         * @param key
         * @return 影响的记录数
         * */
        public long persist(String key) {
            return persist(dbName, key);
        }

        private long persist(String dbName, String key) {
            Jedis jedis = getJedis(dbName);
            long count = jedis.persist(key);
            returnJedis(dbName, jedis);
            return count;
        }

        /**
         * 删除keys对应的记录,可以是多个key
         * @param  keys
         * @return 删除的记录数
         * */
        public long del(String... keys) {
            return del(dbName, keys);
        }

        private long del(String dbName, String... keys) {
            Jedis jedis = getJedis(dbName);
            long count = jedis.del(keys);
            returnJedis(dbName, jedis);
            return count;
        }

        /**
         * 删除keys对应的记录,可以是多个key
         * @param  keys
         * @return 删除的记录数
         */
        public long del(byte[]... keys) {
            return del(dbName, keys);
        }

        private long del(String dbName, byte[]... keys) {
            Jedis jedis = getJedis(dbName);
            long count = jedis.del(keys);
            returnJedis(dbName, jedis);
            return count;
        }

        /**
         * 对List,Set,SortSet进行排序,如果集合数据较大应避免使用这个方法
         * @param key
         * @return List<String> 集合的全部记录
         */
        public List<String> sort(String key) {
            return sort(dbName, key);
        }

        private List<String> sort(String dbName, String key) {
            Jedis jedis=getJedis(dbName);
            List<String> list = jedis.sort(key);
            returnJedis(dbName, jedis);
            return list;
        }

        /**
         * 对List,Set,SortSet进行排序或limit
         * @param key
         * @param params 定义排序类型或limit的起止位置.
         * @return List<String> 全部或部分记录
         * **/
        public List<String> sort(String key, SortingParams params) {
            return sort(dbName, key, params);
        }

        private List<String> sort(String dbName, String key, SortingParams params) {
            Jedis jedis = getJedis(dbName);
            List<String> list = jedis.sort(key, params);
            returnJedis(dbName, jedis);
            return list;
        }

        /**
         * 返回指定key存储的类型
         * @param key
         * @return String string|list|set|zset|hash
         * **/
        public String type(String key) {
            return type(dbName, key);
        }

        private String type(String dbName, String key) {
            Jedis jedis = getJedis(dbName);
            String type = jedis.type(key);
            returnJedis(dbName, jedis);
            return type;
        }

        /**
         * 查找所有匹配给定的模式的键
         * @param  pattern key的表达式,*表示多个，？表示一个
         * */
        public Set<String> keys(String pattern) {
            return keys(dbName, pattern);
        }

        private Set<String> keys(String dbName, String pattern) {
            Jedis jedis = getJedis(dbName);
            Set<String> set = jedis.keys(pattern);
            returnJedis(dbName, jedis);
            return set;
        }
    }

    //*******************************************Sets*******************************************//
    public class Sets {

        private String dbName = null;
        public Sets(){}
        public Sets(String dbName){
            this.dbName = dbName;
        }

        /**
         * 向Set添加一条记录，如果member已存在返回0,否则返回1
         * @param  key
         * @param member
         * @return 操作码,0或1
         * */
        public long sadd(String key, String member) {
            return sadd(dbName, key, member);
        }

        private long sadd(String dbName, String key, String member) {
            Jedis jedis = getJedis(dbName);
            long s = jedis.sadd(key, member);
            returnJedis(dbName, jedis);
            return s;
        }

        public long sadd(byte[] key, byte[] member) {
            return sadd(dbName, key, member);
        }

        private long sadd(String dbName, byte[] key, byte[] member) {
            Jedis jedis = getJedis(dbName);
            long s = jedis.sadd(key, member);
            returnJedis(dbName, jedis);
            return s;
        }

        /**
         * 获取给定key中元素个数
         * @param key
         * @return 元素个数
         * */
        public long scard(String key) {
            return scard(dbName, key);
        }

        public long scard(String dbName, String key) {
            Jedis jedis = getJedis(dbName);
            long len = jedis.scard(key);
            returnJedis(dbName, jedis);
            return len;
        }

        /**
         * 返回从第一组和所有的给定集合之间的差异的成员
         * @param keys
         * @return 差异的成员集合
         */
        public Set<String> sdiff(String... keys) {
            return sdiff(dbName, keys);
        }

        private Set<String> sdiff(String dbName, String... keys) {
            Jedis jedis = getJedis(dbName);
            Set<String> set = jedis.sdiff(keys);
            returnJedis(dbName, jedis);
            return set;
        }

        /**
         * 这个命令等于sdiff,但返回的不是结果集,而是将结果集存储在新的集合中，如果目标已存在，则覆盖。
         * @param newkey 新结果集的key
         * @param keys 比较的集合
         * @return 新集合中的记录数
         * **/
        public long sdiffstore(String newkey, String... keys) {
            return sdiffstore(dbName, newkey, keys);
        }

        private long sdiffstore(String dbName, String newkey, String... keys) {
            Jedis jedis = getJedis(dbName);
            long s = jedis.sdiffstore(newkey, keys);
            returnJedis(dbName, jedis);
            return s;
        }

        /**
         * 返回给定集合交集的成员,如果其中一个集合为不存在或为空，则返回空Set
         * @param  keys
         * @return 交集成员的集合
         * **/
        public Set<String> sinter(String... keys) {
            return sinter(dbName, keys);
        }

        private Set<String> sinter(String dbName, String... keys) {
            Jedis jedis = getJedis(dbName);
            Set<String> set = jedis.sinter(keys);
            returnJedis(dbName, jedis);
            return set;
        }

        /**
         * 这个命令等于sinter,但返回的不是结果集,而是将结果集存储在新的集合中，如果目标已存在，则覆盖。
         * @param  newkey 新结果集的key
         * @param  keys 比较的集合
         * @return 新集合中的记录数
         * **/
        public long sinterstore(String newkey, String... keys) {
            return sinterstore(dbName, newkey, keys);
        }

        private long sinterstore(String dbName, String newkey, String... keys) {
            Jedis jedis = getJedis(dbName);
            long s = jedis.sinterstore(newkey, keys);
            returnJedis(dbName, jedis);
            return s;
        }

        /**
         * 确定一个给定的值是否存在
         * @param  key
         * @param member 要判断的值
         * @return 存在返回1，不存在返回0
         * **/
        public boolean sismember(String key, String member) {
            return sismember(dbName, key, member);
        }

        private boolean sismember(String dbName, String key, String member) {
            Jedis jedis = getJedis(dbName);
            boolean s = jedis.sismember(key, member);
            returnJedis(dbName, jedis);
            return s;
        }

        /**
         * 返回集合中的所有成员
         * @param  key
         * @return 成员集合
         * */
        public Set<String> smembers(String key) {
            return smembers(dbName, key);
        }

        private Set<String> smembers(String dbName, String key) {
            Jedis jedis = getJedis(dbName);
            Set<String> set = jedis.smembers(key);
            returnJedis(dbName, jedis);
            return set;
        }

        public Set<byte[]> smembers(byte[] key) {
            return smembers(dbName, key);
        }

        private Set<byte[]> smembers(String dbName, byte[] key) {
            Jedis jedis = getJedis(dbName);
            Set<byte[]> set = jedis.smembers(key);
            returnJedis(dbName, jedis);
            return set;
        }

        /**
         * 将成员从源集合移出放入目标集合 <br/>
         * 如果源集合不存在或不包哈指定成员，不进行任何操作，返回0<br/>
         * 否则该成员从源集合上删除，并添加到目标集合，如果目标集合中成员已存在，则只在源集合进行删除
         * @param  srckey 源集合
         * @param dstkey 目标集合
         * @param member 源集合中的成员
         * @return 状态码，1成功，0失败
         * */
        public long smove(String srckey, String dstkey, String member) {
            return smove(dbName, srckey, dstkey, member);
        }

        private long smove(String dbName, String srckey, String dstkey, String member) {
            Jedis jedis = getJedis(dbName);
            long s = jedis.smove(srckey, dstkey, member);
            returnJedis(dbName, jedis);
            return s;
        }

        /**
         * 从集合中删除成员
         * @param  key
         * @return 被删除的成员
         * */
        public String spop(String key) {
            return spop(dbName, key);
        }

        private String spop(String dbName, String key) {
            Jedis jedis = getJedis(dbName);
            String s = jedis.spop(key);
            returnJedis(dbName, jedis);
            return s;
        }

        /**
         * 从集合中删除指定成员
         * @param key
         * @param  member 要删除的成员
         * @return 状态码，成功返回1，成员不存在返回0
         * */
        public long srem(String key, String member) {
            return srem(dbName, key, member);
        }

        private long srem(String dbName, String key, String member) {
            Jedis jedis = getJedis(dbName);
            long s = jedis.srem(key, member);
            returnJedis(dbName, jedis);
            return s;
        }

        /**
         * 合并多个集合并返回合并后的结果，合并后的结果集合并不保存<br/>
         * @param keys
         * @return 合并后的结果集合
         * */
        public Set<String> sunion(String... keys) {
            return sunion(dbName, keys);
        }

        private Set<String> sunion(String dbName, String... keys) {
            Jedis jedis = getJedis(dbName);
            Set<String> set = jedis.sunion(keys);
            returnJedis(dbName, jedis);
            return set;
        }

        /**
         * 合并多个集合并将合并后的结果集保存在指定的新集合中，如果新集合已经存在则覆盖
         * @param  newkey 新集合的key
         * @param keys 要合并的集合
         * **/
        public long sunionstore(String newkey, String... keys) {
            return sunionstore(dbName, newkey, keys);
        }

        private long sunionstore(String dbName, String newkey, String... keys) {
            Jedis jedis = getJedis(dbName);
            long s = jedis.sunionstore(newkey, keys);
            returnJedis(dbName, jedis);
            return s;
        }
    }

    //*******************************************SortSet*******************************************//
    public class SortSet {

        private String dbName = null;
        public SortSet(){}
        public SortSet(String dbName){
            this.dbName = dbName;
        }

        /**
         * 向集合中增加一条记录,如果这个值已存在，这个值对应的权重将被置为新的权重
         * @param  key
         * @param score 权重
         * @param  member 要加入的值，
         * @return 状态码 1成功，0已存在member的值
         * */
        public long zadd(String key, double score, String member) {
            return zadd(dbName, key, score, member);
        }

        private long zadd(String dbName, String key, double score, String member) {
            Jedis jedis = getJedis(dbName);
            long s = jedis.zadd(key, score, member);
            returnJedis(dbName, jedis);
            return s;
        }

        /**
         * 获取集合中元素的数量
         * @param  key
         * @return 如果返回0则集合不存在
         * */
        public long zcard(String key) {
            return zcard(dbName, key);
        }

        private long zcard(String dbName, String key) {
            Jedis jedis = getJedis(dbName);
            long len = jedis.zcard(key);
            returnJedis(dbName, jedis);
            return len;
        }

        /**
         * 获取指定权重区间内集合的数量
         * @param key
         * @param min 最小排序位置
         * @param max 最大排序位置
         * */
        public long zcount(String key, double min, double max) {
            return zcount(dbName, key, min, max);
        }

        private long zcount(String dbName, String key, double min, double max) {
            Jedis jedis = getJedis(dbName);
            long len = jedis.zcount(key, min, max);
            returnJedis(dbName, jedis);
            return len;
        }

        /**
         * 获得set的长度
         *
         * @param key
         * @return
         */
        public long zlength(String key) {
            return zlength(dbName, key);
        }

        private long zlength(String dbName, String key) {
            long len = 0;
            Set<String> set = zrange(dbName, key, 0, -1);
            len = set.size();
            return len;
        }

        /**
         * 权重增加给定值，如果给定的member已存在
         * @param  key
         * @param score 要增的权重
         * @param  member 要插入的值
         * @return 增后的权重
         * */
        public double zincrby(String key, double score, String member) {
            return zincrby(dbName, key, score, member);
        }

        private double zincrby(String dbName, String key, double score, String member) {
            Jedis jedis = getJedis(dbName);
            double s = jedis.zincrby(key, score, member);
            returnJedis(dbName, jedis);
            return s;
        }

        /**
         * 返回指定位置的集合元素,0为第一个元素，-1为最后一个元素
         * @param key
         * @param start 开始位置(包含)
         * @param end 结束位置(包含)
         * @return Set<String>
         * */
        public Set<String> zrange(String key, int start, int end) {
            return zrange(dbName, key, start, end);
        }

        private Set<String> zrange(String dbName, String key, int start, int end) {
            Jedis jedis = getJedis(dbName);
            Set<String> set = jedis.zrange(key, start, end);
            returnJedis(dbName, jedis);
            return set;
        }

        /**
         * 返回指定位置的集合元素,0为第一个元素，-1为最后一个元素,并且返回对应的分数
         * @param key
         * @param start 开始位置(包含)
         * @param end 结束位置(包含)
         * @return Set<Tuple>
         * */
        public Set<Tuple> zrangeWithScores(String key, int start, int end) {
            return zrangeWithScores(dbName, key, start, end);
        }

        private Set<Tuple> zrangeWithScores(String dbName, String key, int start, int end) {
            Jedis jedis = getJedis(dbName);
            Set<Tuple> set = jedis.zrangeWithScores(key, start, end);
            returnJedis(dbName, jedis);
            return set;
        }

        /**
         * 返回指定权重区间的元素集合
         * @param key
         * @param min 上限权重
         * @param max 下限权重
         * @return Set<String>
         * */
        public Set<String> zrangeByScore(String key, double min, double max) {
            return zrangeByScore(dbName, key, min, max);
        }

        private Set<String> zrangeByScore(String dbName, String key, double min, double max) {
            Jedis jedis = getJedis(dbName);
            Set<String> set = jedis.zrangeByScore(key, min, max);
            returnJedis(dbName, jedis);
            return set;
        }

        /**
         * 获取指定值在集合中的位置，集合排序从低到高
         * @param key
         * @param member
         * @return long 位置
         * */
        public long zrank(String key, String member) {
            return zrank(dbName, key, member);
        }

        private long zrank(String dbName, String key, String member) {
            Jedis jedis = getJedis(dbName);
            long index = jedis.zrank(key, member);
            returnJedis(dbName, jedis);
            return index;
        }

        /**
         * 获取指定值在集合中的位置，集合排序从高到低
         * 
         * @param key
         * @param member
         * @return long 位置
         * */
        public long zrevrank(String key, String member) {
            return zrevrank(dbName, key, member);
        }

        private long zrevrank(String dbName, String key, String member) {
            Jedis jedis = getJedis(dbName);
            long index = jedis.zrevrank(key, member);
            returnJedis(dbName, jedis);
            return index;
        }

        /**
         * 从集合中删除成员
         * @param key
         * @param member
         * @return 返回1成功
         * */
        public long zrem(String key, String member) {
            return zrem(dbName, key, member) ;
        }

        private long zrem(String dbName, String key, String member) {
            Jedis jedis = getJedis(dbName);
            long s = jedis.zrem(key, member);
            returnJedis(dbName, jedis);
            return s;
        }

        /**
         * 删除集合
         * @param key
         * @return
         */
        public long zremset(String key) {
            return zremset(dbName, key);
        }

        private long zremset(String dbName, String key) {
            Jedis jedis = getJedis(dbName);
            long s = jedis.del(key);
            returnJedis(dbName, jedis);
            return s;
        }

        /**
         * 删除给定位置区间的元素
         * @param  key
         * @param start 开始区间，从0开始(包含)
         * @param end 结束区间,-1为最后一个元素(包含)
         * @return 删除的数量
         * */
        public long zremrangeByRank(String key, int start, int end) {
            return zremrangeByRank(dbName, key, start, end);
        }

        private long zremrangeByRank(String dbName, String key, int start, int end) {
            Jedis jedis = getJedis(dbName);
            long s = jedis.zremrangeByRank(key, start, end);
            returnJedis(dbName, jedis);
            return s;
        }

        /**
         * 删除给定权重区间的元素
         * @param key
         * @param min 下限权重(包含)
         * @param max 上限权重(包含)
         * @return 删除的数量
         * */
        public long zremrangeByScore(String key, double min, double max) {
            return zremrangeByScore(dbName, key, min, max);
        }

        private long zremrangeByScore(String dbName, String key, double min, double max) {
            Jedis jedis = getJedis(dbName);
            long s = jedis.zremrangeByScore(key, min, max);
            returnJedis(dbName, jedis);
            return s;
        }

        /**
         * 获取给定区间的元素，原始按照权重由高到低排序
         * @param  key
         * @param start
         * @param end
         * @return Set<String>
         * */
        public Set<String> zrevrange(String key, int start, int end) {
            return zrevrange(dbName, key, start, end) ;
        }

        private Set<String> zrevrange(String dbName, String key, int start, int end) {
            Jedis jedis = getJedis(dbName);
            Set<String> set = jedis.zrevrange(key, start, end);
            returnJedis(dbName, jedis);
            return set;
        }

        /**
         * 获取给定值在集合中的权重
         * @param  key
         * @param memebr
         * @return double 权重
         * */
        public double zscore(String key, String memebr) {
            return zscore(dbName, key, memebr);
        }

        private double zscore(String dbName, String key, String memebr) {
            Jedis jedis = getJedis(dbName);
            Double score = jedis.zscore(key, memebr);
            returnJedis(dbName, jedis);
            if (score != null)
                return score;
            return 0;
        }
    }

    //*******************************************Hash*******************************************//
    public class Hash {

        private String dbName = null;
        public Hash(){}
        public Hash(String dbName){
            this.dbName = dbName;
        }

        /**
         * 从hash中删除指定的存储
         * @param key
         * @param  fieid 存储的名字
         * @return 状态码，1成功，0失败
         * */
        public long hdel(String key, String fieid) {
            return hdel(dbName, key, fieid);
        }

        private long hdel(String dbName, String key, String fieid) {
            Jedis jedis = getJedis(dbName);
            long s = jedis.hdel(key, fieid);
            returnJedis(dbName, jedis);
            return s;
        }

        /**
         * 删除指定的hash
         * @param key
         * @return 状态码，1成功，0失败
         * */
        public long hdelhash(String key) {
            return hdelhash(dbName, key);
        }

        private long hdelhash(String dbName, String key) {
            Jedis jedis = getJedis(dbName);
            long s = jedis.del(key);
            returnJedis(dbName, jedis);
            return s;
        }

        /**
         * 测试hash中指定的存储是否存在
         * @param key
         * @param  fieId 存储的名字
         * @return 1存在，0不存在
         * */
        public boolean hexists(String key, String fieId) {
            return hexists(dbName, key, fieId);
        }

        private boolean hexists(String dbName, String key, String fieId) {
            Jedis jedis = getJedis(dbName);
            boolean s = jedis.hexists(key, fieId);
            returnJedis(dbName, jedis);
            return s;
        }

        /**
         * 返回hash中指定存储位置的值
         *
         * @param key
         * @param fieId 存储的名字
         * @return 存储对应的值
         * */
        public String hget(String key, String fieId) {
            return hget(dbName, key, fieId);
        }

        private String hget(String dbName, String key, String fieId) {
            Jedis jedis = getJedis(dbName);
            String s = jedis.hget(key, fieId);
            returnJedis(dbName, jedis);
            return s;
        }

        public byte[] hget(byte[] key, byte[] fieId) {
            return hget(dbName, key, fieId);
        }

        private byte[] hget(String dbName, byte[] key, byte[] fieId) {
            Jedis jedis = getJedis(dbName);
            byte[] s = jedis.hget(key, fieId);
            returnJedis(dbName, jedis);
            return s;
        }

        /**
         * 以Map的形式返回hash中的存储和值
         * @param    key
         * @return Map<Strinig,String>
         * */
        public Map<String, String> hgetAll(String key) {
            return hgetAll(dbName, key);
        }

        private Map<String, String> hgetAll(String dbName, String key) {
            Jedis jedis = getJedis(dbName);
            Map<String, String> map = jedis.hgetAll(key);
            returnJedis(dbName, jedis);
            return map;
        }

        /**
         * 添加一个对应关系
         * @param  key
         * @param fieId
         * @param value
         * @return 状态码 1成功，0失败，fieid已存在将更新，也返回0
         * **/
        public long hset(String key, String fieId, String value) {
            return hset(dbName, key, fieId, value);
        }

        private long hset(String dbName, String key, String fieId, String value) {
            Jedis jedis = getJedis(dbName);
            long s = jedis.hset(key, fieId, value);
            returnJedis(dbName, jedis);
            return s;
        }

        public long hset(String key, String fieId, byte[] value) {
            return hset(dbName, key, fieId, value);
        }

        private long hset(String dbName, String key, String fieId, byte[] value) {
            Jedis jedis = getJedis(dbName);
            long s = jedis.hset(key.getBytes(), fieId.getBytes(), value);
            returnJedis(dbName, jedis);
            return s;
        }

        /**
         * 添加对应关系，只有在fieid不存在时才执行
         * @param key
         * @param fieId
         * @param value
         * @return 状态码 1成功，0失败fieid已存
         * **/
        public long hsetnx(String key, String fieId, String value) {
            return hsetnx(dbName, key, fieId, value);
        }

        private long hsetnx(String dbName, String key, String fieId, String value) {
            Jedis jedis = getJedis(dbName);
            long s = jedis.hsetnx(key, fieId, value);
            returnJedis(dbName, jedis);
            return s;
        }

        /**
         * 获取hash中value的集合
         *
         * @param
         *            key
         * @return List<String>
         * */
        public List<String> hvals(String key) {
            return hvals(dbName, key);
        }

        private List<String> hvals(String dbName, String key) {
            Jedis jedis = getJedis(dbName);
            List<String> list = jedis.hvals(key);
            returnJedis(dbName, jedis);
            return list;
        }

        /**
         * 在指定的存储位置加上指定的数字，存储位置的值必须可转为数字类型
         * @param  key
         * @param  fieId 存储位置
         * @paramvalue 要增加的值,可以是负数
         * @return 增加指定数字后，存储位置的值
         * */
        public long hincrby(String key, String fieId, long value) {
            return hincrby(dbName, key, fieId, value);
        }

        private long hincrby(String dbName, String key, String fieId, long value) {
            Jedis jedis = getJedis(dbName);
            long s = jedis.hincrBy(key, fieId, value);
            returnJedis(dbName, jedis);
            return s;
        }

        /**
         * 返回指定hash中的所有存储名字,类似Map中的keySet方法
         * @param key
         * @return Set<String> 存储名称的集合
         * */
        public Set<String> hkeys(String key) {
            return hkeys(dbName, key);
        }

        private Set<String> hkeys(String dbName, String key) {
            Jedis jedis = getJedis(dbName);
            Set<String> set = jedis.hkeys(key);
            returnJedis(dbName, jedis);
            return set;
        }

        /**
         * 获取hash中存储的个数，类似Map中size方法
         * @param  key
         * @return long 存储的个数
         * */
        public long hlen(String key) {
            return hlen(dbName, key);
        }

        private long hlen(String dbName, String key) {
            Jedis jedis = getJedis(dbName);
            long len = jedis.hlen(key);
            returnJedis(dbName, jedis);
            return len;
        }

        /**
         * 根据多个key，获取对应的value，返回List,如果指定的key不存在,List对应位置为null
         * @param  key
         * @param fieIds 存储位置
         * @return List<String>
         * */
        public List<String> hmget(String key, String... fieIds) {
            return hmget(dbName, key, fieIds);
        }

        private List<String> hmget(String dbName, String key, String... fieIds) {
            Jedis jedis = getJedis(dbName);
            List<String> list = jedis.hmget(key, fieIds);
            returnJedis(dbName, jedis);
            return list;
        }

        public List<byte[]> hmget(byte[] key, byte[]... fieIds) {
            return hmget(dbName, key, fieIds);
        }

        private List<byte[]> hmget(String dbName, byte[] key, byte[]... fieIds) {
            Jedis jedis = getJedis(dbName);
            List<byte[]> list = jedis.hmget(key, fieIds);
            returnJedis(dbName, jedis);
            return list;
        }

        /**
         * 添加对应关系，如果对应关系已存在，则覆盖
         * @param   key
         * @param map 对应关系
         * @return 状态，成功返回OK
         * */
        public String hmset(String key, Map<String, String> map) {
            return hmset(dbName, key, map);
        }

        private String hmset(String dbName, String key, Map<String, String> map) {
            Jedis jedis = getJedis(dbName);
            String s = jedis.hmset(key, map);
            returnJedis(dbName, jedis);
            return s;
        }

        /**
         * 添加对应关系，如果对应关系已存在，则覆盖
         * @param key
         * @param map 对应关系
         * @return 状态，成功返回OK
         * */
        public String hmset(byte[] key, Map<byte[], byte[]> map) {
            return hmset(dbName, key, map);
        }

        private String hmset(String dbName, byte[] key, Map<byte[], byte[]> map) {
            Jedis jedis = getJedis(dbName);
            String s = jedis.hmset(key, map);
            returnJedis(dbName, jedis);
            return s;
        }

    }


    //*******************************************Strings*******************************************//
    public class Strings {

        private String dbName = null;
        public Strings(){}
        public Strings(String dbName){
            this.dbName = dbName;
        }
        /**
         * 根据key获取记录
         * @param  key
         * @return 值
         * */
        public String get(String key) {
            return get(dbName, key);
        }

        private String get(String dbName, String key) {
            Jedis jedis = getJedis(dbName);
            String value = jedis.get(key);
            returnJedis(dbName, jedis);
            return value;
        }

        /**
         * 根据key获取记录
         * @param key
         * @return 值
         * */
        public byte[] get(byte[] key) {
            return get(dbName, key);
        }

        private byte[] get(String dbName, byte[] key) {
            Jedis jedis = getJedis(dbName);
            byte[] value = jedis.get(key);
            returnJedis(dbName, jedis);
            return value;
        }

        /**
         * 添加有过期时间的记录
         *
         * @param  key
         * @param seconds 过期时间，以秒为单位
         * @param value
         * @return String 操作状态
         * */
        public String setEx(String key, int seconds, String value) {
            return setEx(dbName, key, seconds, value);
        }

        private String setEx(String dbName, String key, int seconds, String value) {
            Jedis jedis = getJedis(dbName);
            String str = jedis.setex(key, seconds, value);
            returnJedis(dbName, jedis);
            return str;
        }

        /**
         * 添加有过期时间的记录
         *
         * @param key
         * @param seconds 过期时间，以秒为单位
         * @param  value
         * @return String 操作状态
         * */
        public String setEx(byte[] key, int seconds, byte[] value) {
            return setEx(dbName, key, seconds, value);
        }

        private String setEx(String dbName, byte[] key, int seconds, byte[] value) {
            Jedis jedis = getJedis(dbName);
            String str = jedis.setex(key, seconds, value);
            returnJedis(dbName, jedis);
            return str;
        }

        /**
         * 添加一条记录，仅当给定的key不存在时才插入
         * @param key
         * @param value
         * @return long 状态码，1插入成功且key不存在，0未插入，key存在
         * */
        public long setnx(String key, String value) {
            return setnx(dbName, key, value);
        }

        private long setnx(String dbName, String key, String value) {
            Jedis jedis = getJedis(dbName);
            long str = jedis.setnx(key, value);
            returnJedis(dbName, jedis);
            return str;
        }

        /**
         * 添加记录,如果记录已存在将覆盖原有的value
         * @param key
         * @param value
         * @return 状态码
         * */
        public String set(String key, String value) {
            return set(dbName, SafeEncoder.encode(key), SafeEncoder.encode(value));
        }

        private String set(String dbName, String key, String value) {
            return set(dbName, SafeEncoder.encode(key), SafeEncoder.encode(value));
        }

        /**
         * 添加记录,如果记录已存在将覆盖原有的value
         * @param  key
         * @param value
         * @return 状态码
         * */
        public String set(String key, byte[] value) {
            return set(dbName, SafeEncoder.encode(key), value);
        }

        private String set(String dbName, String key, byte[] value) {
            return set(dbName, SafeEncoder.encode(key), value);
        }

        /**
         * 添加记录,如果记录已存在将覆盖原有的value
         * @param key
         * @param value
         * @return 状态码
         * */
        private String set(String dbName, byte[] key, byte[] value) {
            Jedis jedis = getJedis(dbName);
            String status = jedis.set(key, value);
            returnJedis(dbName, jedis);
            return status;
        }

        /**
         * 从指定位置开始插入数据，插入的数据会覆盖指定位置以后的数据<br/>
         * 例:String str1="123456789";<br/>
         * 对str1操作后setRange(key,4,0000)，str1="123400009";
         * @param  key
         * @paramoffset
         * @param  value
         * @return long value的长度
         * */
        public long setRange(String key, long offset, String value) {
            return setRange(dbName, key, offset, value);
        }

        private long setRange(String dbName, String key, long offset, String value) {
            Jedis jedis = getJedis(dbName);
            long len = jedis.setrange(key, offset, value);
            returnJedis(dbName, jedis);
            return len;
        }

        /**
         * 在指定的key中追加value
         * @param  key
         * @param value
         * @return long 追加后value的长度
         * **/
        public long append(String key, String value) {
            return append(dbName, key, value);
        }

        private long append(String dbName, String key, String value) {
            Jedis jedis = getJedis(dbName);
            long len = jedis.append(key, value);
            returnJedis(dbName, jedis);
            return len;
        }

        /**
         * 将key对应的value减去指定的值，只有value可以转为数字时该方法才可用
         * @param key
         * @paramnumber 要减去的值
         * @return long 减指定值后的值
         * */
        public long decrBy(String key, long number) {
            return decrBy(dbName, key, number);
        }

        private long decrBy(String dbName, String key, long number) {
            Jedis jedis = getJedis(dbName);
            long len = jedis.decrBy(key, number);
            returnJedis(dbName, jedis);
            return len;
        }

        /**
         * <b>可以作为获取唯一id的方法</b><br/>
         * 将key对应的value加上指定的值，只有value可以转为数字时该方法才可用
         * @param  key
         * @paramnumber 要减去的值
         * @return long 相加后的值
         * */
        public long incrBy(String key, long number) {
            return incrBy(dbName, key, number);
        }

        public long incrBy(String dbName, String key, long number) {
            Jedis jedis = getJedis(dbName);
            long len = jedis.incrBy(key, number);
            returnJedis(dbName, jedis);
            return len;
        }

        /**
         * 对指定key对应的value进行截取
         * @param   key
         * @paramstartOffset 开始位置(包含)
         * @paramendOffset 结束位置(包含)
         * @return String 截取的值
         * */
        public String getrange(String key, long startOffset, long endOffset) {
            return getrange(dbName, key, startOffset, endOffset);
        }

        public String getrange(String dbName, String key, long startOffset, long endOffset) {
            Jedis jedis = getJedis(dbName);
            String value = jedis.getrange(key, startOffset, endOffset);
            returnJedis(dbName, jedis);
            return value;
        }

        /**
         * 获取并设置指定key对应的value<br/>
         * 如果key存在返回之前的value,否则返回null
         * @param  key
         * @param value
         * @return String 原始value或null
         * */
        public String getSet(String key, String value) {
            return getSet(dbName, key, value);
        }

        private String getSet(String dbName, String key, String value) {
            Jedis jedis = getJedis(dbName);
            String str = jedis.getSet(key, value);
            returnJedis(dbName, jedis);
            return str;
        }

        /**
         * 批量获取记录,如果指定的key不存在返回List的对应位置将是null
         * @param keys
         * @return List<String> 值得集合
         * */
        public List<String> mget(String... keys) {
            return mget(dbName, keys);
        }

        private List<String> mget(String dbName, String... keys) {
            Jedis jedis = getJedis(dbName);
            List<String> str = jedis.mget(keys);
            returnJedis(dbName, jedis);
            return str;
        }

        /**
         * 批量存储记录
         * @param keysvalues 例:keysvalues="key1","value1","key2","value2";
         * @return String 状态码
         * */
        public String mset(String... keysvalues) {
            return mset(dbName, keysvalues);
        }

        private String mset(String dbName, String... keysvalues) {
            Jedis jedis = getJedis(dbName);
            String str = jedis.mset(keysvalues);
            returnJedis(dbName, jedis);
            return str;
        }

        /**
         * 获取key对应的值的长度
         * @param key
         * @return value值得长度
         * */
        public long strlen(String key) {
            return strlen(dbName, key);
        }

        private long strlen(String dbName, String key) {
            Jedis jedis = getJedis(dbName);
            long len = jedis.strlen(key);
            returnJedis(dbName, jedis);
            return len;
        }
    }


    //*******************************************Lists*******************************************//
    public class Lists {

        private String dbName = null;
        public Lists(){}
        public Lists(String dbName){
            this.dbName = dbName;
        }

        /**
         * List长度
         * @param key
         * @return 长度
         * */
        public long llen(String key) {
            return llen(dbName, SafeEncoder.encode(key));
        }

        private long llen(String dbName, String key) {
            return llen(dbName, SafeEncoder.encode(key));
        }

        private long llen(String dbName, byte[] key) {
            Jedis jedis = getJedis(dbName);
            long count = jedis.llen(key);
            returnJedis(dbName, jedis);
            return count;
        }

        /**
         * 覆盖操作,将覆盖List中指定位置的值
         * @param key
         * @param index 位置
         * @param  value 值
         * @return 状态码
         * */
        public String lset(String key, int index, String value) {
            return lset(dbName, key, index, value);
        }

        private String lset(String dbName, String key, int index, String value) {
            Jedis jedis = getJedis(dbName);
            String status = jedis.lset(key, index, value);
            returnJedis(dbName, jedis);
            return status;
        }

        /**
         * 在value的相对位置插入记录
         * @param key
         * @param where   前面插入或后面插入
         * @param pivot 相对位置的内容
         * @param value 插入的内容
         * @return 记录总数
         * */
        public long linsert(String key, BinaryClient.LIST_POSITION where, String pivot,
                            String value) {
            return linsert(dbName, key, where, pivot, value);
        }
        private long linsert(String dbName, String key, BinaryClient.LIST_POSITION where, String pivot,
                            String value) {
            Jedis jedis = getJedis(dbName);
            long count = jedis.linsert(key, where, pivot, value);
            returnJedis(dbName, jedis);
            return count;
        }

        /**
         * 获取List中指定位置的值
         * @param  key
         * @param index 位置
         * @return 值
         * **/
        public String lindex(String key, int index) {
            return lindex(dbName, key, index);
        }

        private String lindex(String dbName, String key, int index) {
            Jedis jedis = getJedis(dbName);
            String value = jedis.lindex(key, index);
            returnJedis(dbName, jedis);
            return value;
        }

        /**
         * 将List中的第一条记录移出List
         * @param key
         * @return 移出的记录
         * */
        public String lpop(String key) {
            return lpop(dbName, key);
        }

        private String lpop(String dbName, String key) {
            Jedis jedis = getJedis(dbName);
            String value = jedis.lpop(key);
            returnJedis(dbName, jedis);
            return value;
        }

        /**
         * 将List中最后第一条记录移出List
         *
         * @param key
         * @return 移出的记录
         * */
        public String rpop(String key) {
            return rpop(dbName, key);
        }

        private String rpop(String dbName, String key) {
            Jedis jedis = getJedis(dbName);
            String value = jedis.rpop(key);
            returnJedis(dbName, jedis);
            return value;
        }

        /**
         * 向List尾部追加记录
         * @param key
         * @param value
         * @return 记录总数
         * */
        public long lpush(String key, String value) {
            return lpush(dbName, key, value);
        }

        private long lpush(String dbName, String key, String value) {
            Jedis jedis = getJedis(dbName);
            long count = jedis.lpush(key, value);
            returnJedis(dbName, jedis);
            return count;
        }

        /**
         * 向List头部追加记录
         * @param  key
         * @param  value
         * @return 记录总数
         * */
        public long rpush(String key, String value) {
            return rpush(dbName, key, value);
        }

        private long rpush(String dbName, String key, String value) {
            Jedis jedis = getJedis(dbName);
            long count = jedis.rpush(key, value);
            returnJedis(dbName, jedis);
            return count;
        }

        /**
         * 获取指定范围的记录，可以做为分页使用
         * @param key
         * @paramstart
         * @paramend
         * @return List
         * */
        public List<String> lrange(String key, long start, long end) {
            return lrange(dbName, key, start, end);
        }

        private List<String> lrange(String dbName, String key, long start, long end) {
            Jedis jedis = getJedis(dbName);
            List<String> list = jedis.lrange(key, start, end);
            returnJedis(dbName, jedis);
            return list;
        }

        /**
         * 删除List中c条记录，被删除的记录值为value
         * @param key
         * @param c 要删除的数量，如果为负数则从List的尾部检查并删除符合的记录
         * @param value 要匹配的值
         * @return 删除后的List中的记录数
         * */
        public long lrem(String key, int c, String value) {
            return lrem(dbName, key, c, value);
        }

        private long lrem(String dbName, String key, int c, String value) {
            Jedis jedis = getJedis(dbName);
            long count = jedis.lrem(key, c, value);
            returnJedis(dbName, jedis);
            return count;

        }

        /**
         * 算是删除吧，只保留start与end之间的记录
         * @param key
         * @param start 记录的开始位置(0表示第一条记录)
         * @param end 记录的结束位置（如果为-1则表示最后一个，-2，-3以此类推）
         * @return 执行状态码
         * */
        public String ltrim(String key, int start, int end) {
            return ltrim(dbName, key, start, end);
        }

        public String ltrim(String dbName, String key, int start, int end) {
            Jedis jedis = getJedis(dbName);
            String str = jedis.ltrim(key, start, end);
            returnJedis(dbName, jedis);
            return str;
        }

    }

    public RedisConfiguration getConfiguration() {
        return configuration;
    }
}
