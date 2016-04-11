package com.kbalabala.tools.statemachine.configuration;


import com.kbalabala.tools.statemachine.base.Action;
import com.kbalabala.tools.statemachine.base.State;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 *    detail State transfer condition
 * </p>
 *
 * @author kevin
 * @since 2015-08-07 09:19
 */
public class StateTransferCondition{
    private State status;
    private Action action;
    private Map<String, State> result;

    public State getStatus() {
        return status;
    }

    public void setStatus(State status) {
        this.status = status;
    }

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }

    public Map<String, State> getResult() {
        return result;
    }

    public void setResult(Map<String, State> result) {
        this.result = result;
    }

    public void addResult(String result, State toState){
        if(this.result == null){
            this.result = new HashMap<>();
        }
        this.result.put(result, toState);
    }
}
