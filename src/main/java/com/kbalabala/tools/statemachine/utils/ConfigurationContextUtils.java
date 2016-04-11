package com.kbalabala.tools.statemachine.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 *  tools
 * </p>
 *
 * @author kevin
 * @since 2015-08-07 10:53
 */
public class ConfigurationContextUtils {

    private static final String CONFIGURATION_CURRENT_STATE = "CURRENT_STATE";
    private static final String CONFIGURATION_CURRENT_ACTION = "CURRENT_ACTION";

    private static final ThreadLocal<Map<String, Object>> configurationContext = new ThreadLocal<>();

    public static Class getCurrentState() {
        return (Class)getContext().get(CONFIGURATION_CURRENT_STATE);
    }

    public static void setCurrentState(Class clazz) {
        getContext().put(CONFIGURATION_CURRENT_STATE, clazz);
    }

    public static Class getCurrentAction() {
        return (Class)getContext().get(CONFIGURATION_CURRENT_ACTION);
    }

    public static void setCurrentAction(Class clazz) {
        getContext().put(CONFIGURATION_CURRENT_ACTION, clazz);
    }

    private static Map<String, Object> getContext(){
        Map<String, Object> context = configurationContext.get();
        if(context == null){
            configurationContext.set(new HashMap<String, Object>());
        }
        return configurationContext.get();
    }
}
