package com.kbalabala.tools;

import com.kbalabala.tools.redis.RedisClient;

/**
 * <p>
 *   redis工具测试
 * </p>
 *
 * @author kevin
 * @since 2015-07-10 11:06
 */
public class JmbRedisUtilsTest {

    private static final RedisClient redisClient = new RedisClient("CACHE");
    private static final RedisClient passportRedisClient = new RedisClient("CACHE", PassportConfig.getRedisConfiguration());

    public static void main(String[] args){

        redisClient.setValue("redist_test_2015-07-10", "12345qwert", 600);
        System.out.println(redisClient.getValue("redist_test_2015-07-10"));


        passportRedisClient.setValue("redist_test_2015-07-10_2", "ok my god!!!you win!", 50);
        System.out.println(passportRedisClient.getValue("redist_test_2015-07-10_2"));
    }
}
