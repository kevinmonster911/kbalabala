package com.kbalabala.tools.statemachine;

import com.kbalabala.tools.JmbStringUtils;

import com.kbalabala.tools.json.JmbJsonUtils;
import com.kbalabala.tools.statemachine.base.Action;
import com.kbalabala.tools.statemachine.base.State;
import com.kbalabala.tools.statemachine.configuration.StateMachine;
import com.kbalabala.tools.statemachine.configuration.StateMachineConfiguration;
import com.kbalabala.tools.statemachine.configuration.StateTransferCondition;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.InitializingBean;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 *    state machine executor
 * </p>
 *
 * @author kevin
 * @since 2015-08-07 15:39
 */
public class StateMachineExecutor implements InitializingBean{

    private static final String DEFAULT_CONFIGURATION = "state_machine_configuration.json";

    private StateMachineConfiguration stateMachineConfiguration;
    private String configPath = DEFAULT_CONFIGURATION;
    private Map<Class, List<StateTransferCondition>> config = new HashMap<>();

    public StateMachineExecutor init() {
        this.afterPropertiesSet();
        return this;
    }

    @Override
    public void afterPropertiesSet() {
        InputStream configStream = getClass().getClassLoader().getResourceAsStream(configPath);
        String configJson = null;
        try {
            configJson = IOUtils.toString(configStream);
        } catch (IOException e) {
            configJson = null;
        }
        if(JmbStringUtils.isBlank(configJson)){
            throw new RuntimeException("无效状态机配置");
        }
        stateMachineConfiguration = JmbJsonUtils.fromJson(configJson, StateMachineConfiguration.class);
        if(stateMachineConfiguration == null) throw new RuntimeException("无效状态机配置");
        toTransform();
    }

    private void toTransform(){
        List<StateMachine> stateMachines = stateMachineConfiguration.getStateMachines();
        for(StateMachine stateMachine : stateMachines){
            config.put(stateMachine.getStatus(), stateMachine.getStateTransferConditions());
        }
    }

    public State execute(State currentState, Action action, String result){
        List<StateTransferCondition> conditions = config.get(currentState.getClass());
        for(StateTransferCondition condition : conditions){
            if(condition.getStatus() == currentState &&
                    action == condition.getAction()){
                return condition.getResult().get(result);
            }
        }
        return null;
    }
}
