package com.kbalabala.tools.json;

/**
 * Created by kevin on 15-3-6.
 */

import com.google.gson.*;
import com.kbalabala.tools.JmbDateUtils;

import java.lang.reflect.Type;
import java.util.Date;

/**
 * Created by kevin on 15-3-6.
 */
public class DateYYYYMMDDhhmmssHandler implements JsonSerialHandler<Date> {


    private static DateYYYYMMDDhhmmssHandler dateYYYYMMDDhhmmssHandler = new DateYYYYMMDDhhmmssHandler();

    @Override
    public Type myType() {
        return Date.class;
    }

    @Override
    public JsonElement serialize(Date src, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(JmbDateUtils.toYYYYMMDDhhmmss(src));
    }

    @Override
    public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        return JmbDateUtils.fromYYYYMMDDhhmmss(json.getAsJsonPrimitive().getAsString());
    }

    public static DateYYYYMMDDhhmmssHandler instance() {
        return dateYYYYMMDDhhmmssHandler;
    }
}