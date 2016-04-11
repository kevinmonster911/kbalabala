package com.kbalabala.tools.statemachine.configuration;

import java.util.List;

/**
 * <p>
 *    State machine configuration as root configuration for wrapping the
 *    StateMachineCollection.
 * </p>
 *
 * @author kevin
 * @since 2015-08-07 09:12
 */
public class StateMachineConfiguration {

    private List<StateMachine> stateMachines;

    public List<StateMachine> getStateMachines() {
        return stateMachines;
    }

    public void setStateMachines(List<StateMachine> stateMachines) {
        this.stateMachines = stateMachines;
    }
}
