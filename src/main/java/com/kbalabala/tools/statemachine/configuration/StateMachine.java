package com.kbalabala.tools.statemachine.configuration;

import java.util.List;

/**
 * <p>
 *    the State machine for special State.
 * </p>
 *
 * @author kevin
 * @since 2015-08-07 09:17
 */
public class StateMachine {

    private Class status;

    private List<StateTransferCondition> stateTransferConditions;

    public Class getStatus() {
        return status;
    }

    public void setStatus(Class status) {
        this.status = status;
    }

    public List<StateTransferCondition> getStateTransferConditions() {
        return stateTransferConditions;
    }

    public void setStateTransferConditions(List<StateTransferCondition> stateTransferConditions) {
        this.stateTransferConditions = stateTransferConditions;
    }
}
