package com.kbalabala.tools.excel;

/**
 * Created by kevin on 14-5-19.
 */
public interface TypeResolver<T, V> {
    public V resolve(T object);
}
