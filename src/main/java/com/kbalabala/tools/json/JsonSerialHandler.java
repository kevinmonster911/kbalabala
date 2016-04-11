package com.kbalabala.tools.json;

import com.google.gson.JsonDeserializer;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;

/**
 * Created by kevin on 15-3-6.
 */
public interface JsonSerialHandler<T> extends JsonSerializer<T>, JsonDeserializer<T> {

    Type myType();
}
