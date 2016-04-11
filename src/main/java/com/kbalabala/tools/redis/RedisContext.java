package com.kbalabala.tools.redis;

import redis.clients.jedis.Jedis;

import java.util.HashMap;

/**
 * <p>
 *    上下文获取
 * </p>
 *
 * @author kevin
 * @since 2015-08-19 17:19
 */
public class RedisContext extends HashMap<String, Jedis> {

    private static final String CONTEXT_JEDIS = "jedis";

    public static RedisContext getContext(){
        return new RedisContext();
    }

    public RedisContext setJedis(Jedis jedis) {
        put(CONTEXT_JEDIS, jedis);
        return this;
    }

    public Jedis getJedis(){
        return get(CONTEXT_JEDIS);
    }

}
