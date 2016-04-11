package com.kbalabala.tools;

import com.kbalabala.tools.redis.RedisBaseConfig;
import com.kbalabala.tools.redis.RedisConfiguration;

/**
 * <p>
 *     redis配置
 * </p>
 * @author kevin
 * @since 15-5-14
 */
public class PassportConfig extends RedisBaseConfig {

    public static RedisConfiguration getRedisConfiguration(){
        try {
            RedisConfiguration configuration = new RedisConfiguration();
            configuration.setEnableRedisCache(isEnableRedisCache());
            configuration.setRedisServerAddr(getRedisServerAddr());
            configuration.setRedisSentinelsAddr(getRedisSentinelsAddr());
            configuration.setRedisMasterName(getRedisMasterName());
            configuration.setRedisServerPort(getRedisServerPort());
            configuration.setRedisConnectPassword(getRedisConnectPassword());
            configuration.setRedisDatabases(getRedisDatabases());
            configuration.setRedisMaxActive(getRedisMaxActive());
            configuration.setRedisMaxIdle(getRedisMaxIdle());
            configuration.setRedisMaxWaitMillSeconds(getRedisMaxWaitMillSeconds());
            configuration.setRedisPoolTimeoutMs(getRedisPoolTimeoutMs());
            configuration.setRedisConnectRetryNumber(getRedisConnectRetryNumber());
            configuration.setRedisTestOnBorrow(isRedisTestOnBorrow());
            configuration.setRedisTestOnReturn(isRedisTestOnReturn());
            return configuration;
        } catch (Exception error) {
            return null;
        }
    }

    public static boolean isEnableRedisSession() {return getBool("REDIS_SESSION_ENABLE", true);}
    public static boolean isEnableRedisCache() {return getBool("REDIS_CACHE_ENABLE", true);}
    public static String getRedisServerAddr(){return getString("REDIS_SERVER_ADDR","");}
    public static String getRedisSentinelsAddr(){return getString("REDIS_SENTINEL_ADDR", "");}
    public static String getRedisMasterName(){return getString("REDIS_MASTER_NAME","");}
    public static int getRedisServerPort(){return getInt("REDIS_SERVER_PORT", 6397);}
    public static String getRedisConnectPassword(){return getString("REDIS_CONNECT_PASSWORD", "");}
    public static String getRedisDatabases(){return getString("REDIS_DATABASES", "DEFAULT");}

    public static int getRedisMaxActive(){return getInt("REDIS_MAX_ACTIVE", 100);}
    public static int getRedisMaxIdle(){return getInt("REDIS_MAX_IDLE", 10);}
    public static int getRedisMaxWaitMillSeconds(){return getInt("REDIS_MAX_WAIT_MILLSECONDS", 5000);}
    public static int getRedisPoolTimeoutMs(){return getInt("REDIS_POOL_TIMEOUT_MS", 2000);}
    public static int getRedisConnectRetryNumber(){return getInt("REDIS_CONNECT_RETRY_NUMBER", 3);}
    public static boolean isRedisTestOnBorrow(){return getBool("REDIS_TEST_ON_BORROW", true);}
    public static boolean isRedisTestOnReturn(){return getBool("REDIS_TEST_ON_RETURN", true);}

}
