package com.kbalabala.tools.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.kbalabala.tools.statemachine.base.ClazzTypeAdaptor;
import com.kbalabala.tools.statemachine.configuration.StateTransferCondition;
import com.kbalabala.tools.statemachine.configuration.StateTransferConditionTypeAdaptor;

/**
 * Created by kevin on 15-3-9.
 */
public class JmbJsonUtils {

    private static Gson gson;

    static {
        gson = new GsonBuilder().registerTypeAdapter(StateTransferCondition.class, new StateTransferConditionTypeAdaptor())
                .registerTypeAdapter(Class.class, new ClazzTypeAdaptor()).registerTypeAdapter(DateYYYYMMDDhhmmssHandler.instance().myType(), DateYYYYMMDDhhmmssHandler.instance())
                                .registerTypeAdapter(BigDecimalHandler.instance().myType(), BigDecimalHandler.instance()).create();
    }

    public static <T> T fromJson(String input, Class<T> clazz) {
        return  gson.fromJson(input, clazz);
    }

    public static String toJson(Object object) {
        return gson.toJson(object);
    }
}
