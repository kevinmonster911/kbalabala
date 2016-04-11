package com.kbalabala.tools.statemachine.utils;

import com.kbalabala.tools.statemachine.StateMachineExecutor;
import com.kbalabala.tools.statemachine.base.Action;
import com.kbalabala.tools.statemachine.base.State;

/**
 * <p>
 *   state machine utils
 * </p>
 *
 * @author kevin
 * @since 2015-08-07 17:19
 */
public class StateMachineUtils {

    private static StateMachineExecutor executor = new StateMachineExecutor().init();

    public static State fire(State state, Action action, String result){
        return executor.execute(state, action, result);
    }
}
