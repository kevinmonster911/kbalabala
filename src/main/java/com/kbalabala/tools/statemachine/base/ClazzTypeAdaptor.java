package com.kbalabala.tools.statemachine.base;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.kbalabala.tools.statemachine.utils.ConfigurationContextUtils;
import org.apache.commons.lang.ClassUtils;

import java.io.IOException;

/**
 * <p>
 *    clazz type adaptor
 * </p>
 *
 * @author kevin
 * @since 2015-08-07 14:19
 */
public class ClazzTypeAdaptor extends TypeAdapter<Class> {



    @Override
    public Class read(JsonReader jsonReader) throws IOException {
        String clazzName = jsonReader.nextString();
        Class stateClazz = null;
        Class actionClazz = null;
        try{
            stateClazz = ClassUtils.getClass(clazzName);
            actionClazz = ClassUtils.getClass(clazzName + "Action");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("请正确配置状态机，必须确保Status和StatusAction配对存在!");
        }
        ConfigurationContextUtils.setCurrentState(stateClazz);
        ConfigurationContextUtils.setCurrentAction(actionClazz);

        return stateClazz;
    }

    @Override
    public void write(JsonWriter jsonWriter, Class aClass) throws IOException {
    }
}
