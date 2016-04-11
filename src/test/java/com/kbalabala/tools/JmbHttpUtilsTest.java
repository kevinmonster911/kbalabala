package com.kbalabala.tools;

import jodd.http.HttpUtil;

import java.util.HashMap;
import java.util.Map;

public class JmbHttpUtilsTest extends HttpUtil {

    public static void main(String[] args){


        Map<String, Object> params = new HashMap<>();
        params.put("type", "ranking");
        params.put("openid", 10230);
        params.put("zcm", 12312312);
        System.out.println(JmbHttpUtils.get("https://www.jimubox.com", params));

    }
}
