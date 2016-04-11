package com.kbalabala.tools.redis;

/**
 * <p>
 *    redis command
 * </p>
 *
 * @author kevin
 * @since 2015-08-19 17:15
 */
public interface RedisLogicCommand {

    <T> T doLogic(RedisContext context);
}
