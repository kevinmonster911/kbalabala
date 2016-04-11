package com.kbalabala.tools;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by kevin on 15-4-20.
 */
public class JmbHtmlUtilsTest {

    public static void main(String[] args){
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("user", "caizheng");
        params.put("age", 30);
        params.put("date", JmbDateUtils.now());

        System.out.println(JmbHtmlUtils.toFormalUrl("/showinfo", params, JmbDateUtils.FORMAT_YYYY_MM_DD));

    }

}
