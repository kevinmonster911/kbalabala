package com.kbalabala.tools.statemachine.utils;

import com.kbalabala.tools.JmbStringUtils;
import com.kbalabala.tools.statemachine.base.State;

/**
 * <p>
 * pls input by self
 * </p>
 *
 * @author kevin
 * @since 2015-08-07 14:55
 */
public class StateAndActionUtils {

    public static <T> T analyzeType(Class<T> clazz, String stateOrAction) {
        Class<?> stateOrActionClazz =
                clazz.isAssignableFrom(State.class) ? ConfigurationContextUtils.getCurrentState() : ConfigurationContextUtils.getCurrentAction();
        if(stateOrActionClazz.isEnum()){
            T[] states = (T[])stateOrActionClazz.getEnumConstants();
            return determineState(states, stateOrAction);
        }
        return null;
    }


    private static <T> T determineState(T[] states, String name){
        for(T item : states){
            if(JmbStringUtils.equals(name, item.toString())){
                return item;
            }
        }
        return null;
    }
}
