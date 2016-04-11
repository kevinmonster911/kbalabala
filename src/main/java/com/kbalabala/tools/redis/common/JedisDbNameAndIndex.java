package com.kbalabala.tools.redis.common;

/**
 * <p>
 *     redis数据库名称和索引封装类
 * </p>
 *
 * @author kevin
 * @since 15-5-13
 */
public class JedisDbNameAndIndex  {

    private String name;
    private int index;

    public JedisDbNameAndIndex() {
    }

    public JedisDbNameAndIndex(String name, int index) {
        this.name = name;
        this.index = index;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }
}
