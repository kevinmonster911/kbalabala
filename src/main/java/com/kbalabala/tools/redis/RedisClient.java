package com.kbalabala.tools.redis;

import com.google.gson.Gson;
import com.kbalabala.tools.JmbStringUtils;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;

import java.lang.reflect.Type;
import java.util.*;

public class RedisClient {

    private String specificDB = null;
    private RedisConfiguration configuration = null;
    private JedisUtil jedisUtil = null;
    private JedisUtil.Keys keys = null;
    private JedisUtil.Sets sets = null;
    private JedisUtil.SortSet sortSet = null;
    private JedisUtil.Hash hash = null;
    private JedisUtil.Strings strings = null;
    private JedisUtil.Lists lists = null;

    public JedisUtil.Keys keys() {
        return keys;
    }

    public JedisUtil.Sets sets() {
        return sets;
    }

    public JedisUtil.SortSet sortSet() {
        return sortSet;
    }

    public JedisUtil.Hash hashs() {
        return hash;
    }

    public JedisUtil.Strings strings() {
        return strings;
    }

    public JedisUtil.Lists lists() {
        return lists;
    }

    public <T> T execute(RedisLogicCommand command){
        RedisContext context =
                RedisContext.getContext().setJedis(jedisUtil.getJedis(determineDB()));

        T result = command.doLogic(context);
        jedisUtil.returnJedis(determineDB(), context.getJedis());

        return result;
    }

    public RedisClient() {
        this(null, null);
    }

    public RedisClient(String specificDB) {
        this(specificDB, null);
    }

    public RedisClient(String specificDB, RedisConfiguration configuration) {
        this.specificDB = specificDB;
        this.configuration = configuration;
        this.jedisUtil = JedisUtil.getInstance(this.configuration);
        this.keys = jedisUtil.new Keys(specificDB);
        this.sets = jedisUtil.new Sets(specificDB);
        this.sortSet = jedisUtil.new SortSet(specificDB);
        this.hash = jedisUtil.new Hash(specificDB);
        this.strings = jedisUtil.new Strings(specificDB);
        this.lists = jedisUtil.new Lists(specificDB);
    }

    //set json value
    public <T> boolean setJson(String key, T value) {
        if(value != null) {
            String jsonValue = new Gson().toJson(value);
            return setValue(key, jsonValue);
        }
        return true;
    }

    public <T> boolean setJson(String key, String field, T value) {
        if(value != null) {
            String jsonValue = new Gson().toJson(value);
            return setValue(key, field, jsonValue);
        }
        return true;
    }

    public <T> boolean setJson(String key, T value, int expireSeconds) {
        if(value != null) {
            String jsonValue = new Gson().toJson(value);
            return setValue(key, jsonValue, expireSeconds);
        }
        return true;
    }

    public <T> T getFromJson(String key, Type type){
        //check is open
        if(!jedisUtil.getConfiguration().isEnableRedisCache()){
            return null;
        }

        Jedis jedis = jedisUtil.getJedis(determineDB());
        try {
            String cacheData = jedis.get(key);
            if(JmbStringUtils.isNotEmpty(cacheData)){
                T obj = new Gson().fromJson(cacheData, type);
                return obj;
            }
        } catch (Exception e) {

        } finally {
            jedisUtil.returnJedis(determineDB(), jedis);
        }

        return null;
    }

    public <T> T getFromJson(String dbName, String key, String field, Type type){
        //check is open
        if(!jedisUtil.getConfiguration().isEnableRedisCache()){
            return null;
        }
        Jedis jedis = jedisUtil.getJedis(dbName);
        try {
            String cacheData = jedis.hget(key, field);
            if(JmbStringUtils.isNotEmpty(cacheData)){
                T obj = new Gson().fromJson(cacheData, type);
                return obj;
            }
        } catch (Exception e) {

        } finally {
            jedisUtil.returnJedis(dbName, jedis);
        }
        return null;
    }

    public <T> T getFromJson(String key, String field, Type type){
        return getFromJson(determineDB(), key, field, type);
    }

    //set value
    public boolean setValue(String key, String value) {
        return setValue(determineDB(), key, value);
    }

    public boolean setValue(String dbName, String key, String value) {
        Jedis jedis = jedisUtil.getJedis(dbName);
        try {
            jedis.set(key, value);
        } catch (Exception e) {
            return false;
        } finally {
            jedisUtil.returnJedis(dbName, jedis);
        }
        return true;
    }


    public boolean setHValue(String key, String field, String value) {
        return setHValue(determineDB(), key, field, value);
    }

    public boolean setHValue(String dbName, String key, String field, String value) {
        Jedis jedis = jedisUtil.getJedis(dbName);
        try {
            jedis.hset(key, field, value);
        } catch (Exception e) {
            return false;
        } finally {
            jedisUtil.returnJedis(dbName, jedis);
        }
        return true;
    }

    public boolean setValue(String dbName, String key, String value, int expireSeconds) {
        Jedis jedis = jedisUtil.getJedis(dbName);
        try {
            jedis.setex(key, expireSeconds, value);
        } catch (Exception e) {

            return false;
        } finally {
            jedisUtil.returnJedis(dbName, jedis);
        }
        return true;
    }

    public boolean setValue(String key, String value, int expireSeconds) {
        return setValue(determineDB(), key, value, expireSeconds);
    }


    //get values
    public String getValue(String dbName, String key) {
        Jedis jedis = jedisUtil.getJedis(dbName);
        try {
            return jedis.get(key);
        } catch (Exception e) {

            return null;
        } finally {
            jedisUtil.returnJedis(dbName, jedis);
        }
    }

    public String getValue(String key) {
        return getValue(determineDB(), key);
    }

    public String getHValue(String dbName, String key , String field) {
        try {
            return hash.hget(key, field);
        } catch (Exception e) {
            return null;
        }
    }

    public String getHValue(String key , String field) {
        return getHValue(determineDB(), key, field);
    }

    public boolean setMapValue(String dbName, String key, Map<String, String> values, int expireSeconds) {
        Jedis jedis = jedisUtil.getJedis(dbName);
        try {
            jedis.hmset(key, values);
            if(expireSeconds > 0)
            {
                jedis.expire(key, expireSeconds);
            }
        } catch (Exception e) {
            return false;
        } finally {
            jedisUtil.returnJedis(dbName, jedis);
        }
        return true;
    }


    public boolean setMapValue(String key, Map<String, String> values, int expireSeconds) {
        return setMapValue(determineDB(), key, values, expireSeconds);
    }

    public Map<String, String> getMap(String key) {
        return getMap(determineDB(), key);
    }

    public Map<String, String> getMap(String dbName, String key) {
        try {
            return hash.hgetAll(key);
        } catch (Exception e) {
            return null;
        }
    }

    public List<String> getMapValue(String dbName, String key, String... fields) {
        try {
            return hash.hmget(key, fields);
        } catch (Exception e) {
            return null;
        }
    }

    public List<String> getMapValue(String key, String... fields) {
        return getMapValue(determineDB(), key, fields);
    }

    public boolean delValue(String dbName, String key) {
        Jedis jedis = jedisUtil.getJedis(dbName);
        try {
            Long ret = jedis.del(key);
            return ret > 0;
        } catch (Exception e) {

        } finally {
            jedisUtil.returnJedis(dbName, jedis);
        }
        return false;
    }

    public boolean delValue(String key) {
        return delValue(determineDB(), key);
    }

    public void delAllValue(String dbName) {
        Jedis jedis = jedisUtil.getJedis(dbName);
        try {
            jedis.flushDB();
        } catch (Exception e) {

        } finally {
            jedisUtil.returnJedis(dbName, jedis);
        }
    }

    public int incrValue(String dbName, String key, int step, int expireSeconds) {
        Jedis jedis = jedisUtil.getJedis(dbName);
        try {
            if(expireSeconds > 0)
            {
                jedis.expire(key, expireSeconds);
            }
            return jedis.incrBy(key, step).intValue();
        } catch (Exception e) {
            return -1;
        } finally {
            jedisUtil.returnJedis(dbName, jedis);
        }
    }

    public int incrValue(String key, int step, int expireSeconds) {
        return incrValue(determineDB(), key, step, expireSeconds);
    }

    public LinkedHashMap<String, String> getAllValues(){
        return getAllValues(determineDB());
    }

    public LinkedHashMap<String, String> getAllValues(String dbName){
        Jedis jedis = jedisUtil.getJedis(dbName);
        LinkedHashMap<String, String> mapValues = new LinkedHashMap<>();
        try {
            Set s = jedis.keys("*");
            Iterator it = s.iterator();
            while (it.hasNext()) {
                String key = (String) it.next();
                try{
                    String value = jedis.get(key);
                    if(key != null && value != null){
                        mapValues.put(key, value);
                    }
                }
                catch (Exception ex){
                }
            }
        } catch (Exception e) {

        } finally {
            jedisUtil.returnJedis(dbName, jedis);
        }
        return mapValues;
    }


    public void destroyRedis(){
        jedisUtil.shutdownRedisPool();
    }

    /**
     * return the length of list
     * @param key
     * @return
     */
    public long listLength(String key) {
        return listLength(determineDB(), key);
    }

    public long listLength(String dbName, String key) {
        return lists.llen(key);
    }

    public boolean addListValues(String dbName, String key, List<String> values, int expireSeconds){
        Jedis jedis = jedisUtil.getJedis(dbName);
        try {
            for (String value:values) {
                lists.lpush(key, value);
            }

            if(expireSeconds > 0) {
                jedis.expire(key, expireSeconds);
            }
        } catch (Exception e) {
            return false;
        } finally {
            jedisUtil.returnJedis(dbName, jedis);
        }
        return true;
    }


    public boolean addListValues(String key, List<String> values, int expireSeconds){
        return addListValues(determineDB(), key, values, expireSeconds);
    }

    public List<String> getListVaules(String dbName, String key){
        //start和end都是0-based。即0表示链表头部(leftmost)的第一个元素。其中start的值也可以为负值，-1将表示链表中的最后一个元素，即尾部元素，-2表示倒数第二个并以此类推。该命令在获取元素时，start和end位置上的元素也会被取出
        List<String> values = lists.lrange(key, 0,-1);
        return values;
    }

    public List<String> getListVaules(String key){
        //start和end都是0-based。即0表示链表头部(leftmost)的第一个元素。其中start的值也可以为负值，-1将表示链表中的最后一个元素，即尾部元素，-2表示倒数第二个并以此类推。该命令在获取元素时，start和end位置上的元素也会被取出
        List<String> values = lists.lrange(key, 0, -1);
        return values;
    }

    /**
     * 返回并弹出指定Key关联的链表中的第一个元素，即头部元素，。如果该Key不存在，返回null
     * @param key
     * @return
     */
    public String popFirstValueFromList(String key){
        return popFirstValueFromList(determineDB(), key);
    }

    public String popFirstValueFromList(String dbName, String key){
        String value = lists.lpop(key);
        return value;
    }


    /**
     * 返回并弹出指定Key关联的链表中右边的第一个元素，即尾部元素，。如果该Key不存在，返回null
     * @param key
     * @return
     */
    public String popFirstValueFromRightList(String key){
        return popFirstValueFromRightList(determineDB(), key);
    }

    public String popFirstValueFromRightList(String dbName, String key){
        String value = lists.rpop(key);
        return value;
    }

    /**
     * 从链表左侧加元素
     * @param dbName
     * @param key
     * @param value
     * @return
     */
    public boolean addListFromLeft(String dbName, String key,String value){
        try {
            lists.lpush(key,value);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public boolean addListFromLeft(String key,String value){
        return addListFromLeft(determineDB(), key, value);
    }

    /**
     * 从链表右侧侧加元素
     * @param key
     * @param value
     * @return
     */
    public boolean addListFromRight(String key,String value){
        return addListFromRight(determineDB(), key, value);
    }

    public boolean addListFromRight(String dbName, String key,String value){
        try {
            lists.rpush(key, value);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public String determineDB(){
        return JmbStringUtils.isBlank(specificDB) ? null : specificDB;
    }

    public void setSpecificDB(String specificDB) {
        this.specificDB = specificDB;
    }

    /**
     * determine if key exist
     * @param key
     * @return
     */
    public boolean exists(String key) {
        return exists(determineDB(), key);
    }

    /**
     * determine if key exist
     * @param dbName
     * @param key
     * @return
     */
    public boolean exists(String dbName, String key) {
        return keys.exists(key);
    }


    /**
     * 申请分布式锁，成功返回锁ID,失败返回null
     * @param lockName 锁名称
     * @param acquireTimeout 申请等待时间，在这段时间内，会一直尝试申请锁
     * @param lockTimeout 锁超时时间 ms
     * @return 锁ID
     */
    public String acquireLockWithTimeout(
            String lockName, long acquireTimeout, long lockTimeout)
    {
        Jedis jedis = jedisUtil.getJedis(determineDB());
        String identifier = UUID.randomUUID().toString();
        String lockKey = "lock:" + lockName;
        int lockExpire = (int)(lockTimeout / 1000);

        long end = System.currentTimeMillis() + acquireTimeout;
        while (System.currentTimeMillis() < end) {
            if (jedis.setnx(lockKey, identifier) == 1){
                jedis.expire(lockKey, lockExpire);
                return identifier;
            }
            if (jedis.ttl(lockKey) == -1) {
                jedis.expire(lockKey, lockExpire);
            }

            try {
                Thread.sleep(1);
            }catch(InterruptedException ie){
                Thread.currentThread().interrupt();
            }
        }
        // null indicates that the lock was not acquired
        return null;
    }

    /**
     * 设置锁超时时间
     * @param lockName 锁名称
     * @param identifier 锁ID
     * @param lockTimeout 超时时间 ms
     * @return
     */
    public boolean expireLock(String lockName, String identifier,long lockTimeout) {
        Jedis jedis = jedisUtil.getJedis(determineDB());
        String lockKey = "lock:" + lockName;
        int lockExpire = (int)(lockTimeout / 1000);
        if (identifier.equals(jedis.get(lockKey))){
            jedis.expire(lockKey, lockExpire);
            return true;
        }
        return false;
    }

    /**
     * 释放锁
     * @param lockName 锁名称
     * @param identifier 锁id
     * @return 成功返回true
     */
    public boolean releaseLock(String lockName, String identifier) {
        Jedis jedis = jedisUtil.getJedis(determineDB());
        String lockKey = "lock:" + lockName;

        while (true){
            jedis.watch(lockKey);
            if (identifier.equals(jedis.get(lockKey))){
                Transaction trans = jedis.multi();
                trans.del(lockKey);
                List<Object> results = trans.exec();
                if (results == null){
                    continue;
                }
                return true;
            }

            jedis.unwatch();
            break;
        }

        return false;
    }

}
