package com.kbalabala.tools.redis;

import com.kbalabala.tools.JmbStringUtils;

/**
 * <p>
 *    Redis配置
 * </p>
 *
 * @author kevin
 * @since 2015-07-09 17:46
 */
public class RedisConfiguration {

    private boolean enableRedisCache;
    private String redisServerAddr;
    private String redisSentinelsAddr;
    private int redisServerPort;
    private String redisMasterName;
    private String redisConnectPassword;
    private String redisDatabases;
    private int redisMaxActive;
    private int redisMaxIdle;
    private int redisMaxWaitMillSeconds;
    private int redisPoolTimeoutMs;
    private int redisConnectRetryNumber;
    private boolean redisTestOnBorrow;
    private boolean redisTestOnReturn;

    public boolean isConfig(){
        if(JmbStringUtils.isBlank(redisDatabases) ||
                (JmbStringUtils.isBlank(redisSentinelsAddr) &&
                        (JmbStringUtils.isBlank(redisServerAddr) || redisServerPort == 0 || redisServerPort == -1))){

            return false;
        }
        return true;
    }


    public boolean isEnableRedisCache() {
        return enableRedisCache;
    }

    public void setEnableRedisCache(boolean enableRedisCache) {
        this.enableRedisCache = enableRedisCache;
    }

    public String getRedisServerAddr() {
        return redisServerAddr;
    }

    public void setRedisServerAddr(String redisServerAddr) {
        this.redisServerAddr = redisServerAddr;
    }

    public String getRedisSentinelsAddr() {
        return redisSentinelsAddr;
    }

    public void setRedisSentinelsAddr(String redisSentinelsAddr) {
        this.redisSentinelsAddr = redisSentinelsAddr;
    }

    public int getRedisServerPort() {
        return redisServerPort;
    }

    public void setRedisServerPort(int redisServerPort) {
        this.redisServerPort = redisServerPort;
    }

    public String getRedisMasterName() {
        return redisMasterName;
    }

    public void setRedisMasterName(String redisMasterName) {
        this.redisMasterName = redisMasterName;
    }

    public String getRedisConnectPassword() {
        return redisConnectPassword;
    }

    public void setRedisConnectPassword(String redisConnectPassword) {
        this.redisConnectPassword = redisConnectPassword;
    }

    public String getRedisDatabases() {
        return redisDatabases;
    }

    public void setRedisDatabases(String redisDatabases) {
        this.redisDatabases = redisDatabases;
    }

    public int getRedisMaxActive() {
        return redisMaxActive;
    }

    public void setRedisMaxActive(int redisMaxActive) {
        this.redisMaxActive = redisMaxActive;
    }

    public int getRedisMaxIdle() {
        return redisMaxIdle;
    }

    public void setRedisMaxIdle(int redisMaxIdle) {
        this.redisMaxIdle = redisMaxIdle;
    }

    public int getRedisMaxWaitMillSeconds() {
        return redisMaxWaitMillSeconds;
    }

    public void setRedisMaxWaitMillSeconds(int redisMaxWaitMillSeconds) {
        this.redisMaxWaitMillSeconds = redisMaxWaitMillSeconds;
    }

    public int getRedisPoolTimeoutMs() {
        return redisPoolTimeoutMs;
    }

    public void setRedisPoolTimeoutMs(int redisPoolTimeoutMs) {
        this.redisPoolTimeoutMs = redisPoolTimeoutMs;
    }

    public int getRedisConnectRetryNumber() {
        return redisConnectRetryNumber;
    }

    public void setRedisConnectRetryNumber(int redisConnectRetryNumber) {
        this.redisConnectRetryNumber = redisConnectRetryNumber;
    }

    public boolean isRedisTestOnBorrow() {
        return redisTestOnBorrow;
    }

    public void setRedisTestOnBorrow(boolean redisTestOnBorrow) {
        this.redisTestOnBorrow = redisTestOnBorrow;
    }

    public boolean isRedisTestOnReturn() {
        return redisTestOnReturn;
    }

    public void setRedisTestOnReturn(boolean redisTestOnReturn) {
        this.redisTestOnReturn = redisTestOnReturn;
    }
}
