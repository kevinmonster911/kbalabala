package com.kbalabala.tools.statemachine.configuration;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.kbalabala.tools.JmbStringUtils;
import com.kbalabala.tools.statemachine.base.Action;
import com.kbalabala.tools.statemachine.base.State;
import com.kbalabala.tools.statemachine.utils.StateAndActionUtils;

import java.io.IOException;

/**
 * <p>
 *   base mark interface adaptor
 * </p>
 *
 * @author kevin
 * @since 2015-07-14 14:55
 */
public class StateTransferConditionTypeAdaptor extends TypeAdapter<StateTransferCondition> {

    @Override
    public void write(JsonWriter jsonWriter, StateTransferCondition stateTransferCondition) throws IOException {

    }

    @Override
    public StateTransferCondition read(JsonReader jsonReader) throws IOException {

        jsonReader.beginObject();
        StateTransferCondition stateTransferCondition = new StateTransferCondition();
        while(jsonReader.hasNext()){
            String name = jsonReader.nextName();
            if(JmbStringUtils.equals("status", name)){
                stateTransferCondition.setStatus(StateAndActionUtils.analyzeType(State.class, jsonReader.nextString()));
            }
            if(JmbStringUtils.equals("action", name)){
                stateTransferCondition.setAction(StateAndActionUtils.analyzeType(Action.class, jsonReader.nextString()));
            }
            if(JmbStringUtils.startsWithIgnoreCase(name, "action_")){
                stateTransferCondition.addResult(
                        JmbStringUtils.substring(name, 7, name.length()),
                        StateAndActionUtils.analyzeType(State.class, jsonReader.nextString()));
            }
        }

        jsonReader.endObject();
        return stateTransferCondition;
    }

}
