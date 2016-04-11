package com.kbalabala.tools.json;
import com.google.gson.*;
import com.kbalabala.tools.JmbNumberUtils;

import java.lang.reflect.Type;
import java.math.BigDecimal;

/**
 * Created by kevin on 15-3-6.
 */
public class BigDecimalHandler implements JsonSerialHandler<BigDecimal> {


    private static BigDecimalHandler decimalHandler = new BigDecimalHandler();

    @Override
    public Type myType() {
        return BigDecimal.class;
    }

    @Override
    public JsonElement serialize(BigDecimal src, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(JmbNumberUtils.toDecimal4Pos(src));
    }

    @Override
    public BigDecimal deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        return new BigDecimal(json.getAsJsonPrimitive().getAsString());
    }

    public static BigDecimalHandler instance() {
        return decimalHandler;
    }
}