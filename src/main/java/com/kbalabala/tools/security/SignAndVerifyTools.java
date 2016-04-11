package com.kbalabala.tools.security;

import com.kbalabala.tools.JmbStringUtils;
import com.kbalabala.tools.json.JmbJsonUtils;

import java.util.Map;
import java.util.TreeMap;

/**
 * <p>
 *    签名验签工具
 * </p>
 *
 * @author kevin
 * @since 2015-07-02 09:34
 */
public class SignAndVerifyTools {

    /**
     * 拼接加密字段
     * @param map
     * @return
     */
    private static String combineTextForCrypto(Map<String, String> map, SkipStrategy skipStrategy) {

        StringBuilder sb = new StringBuilder();

        for(Map.Entry<String, String> entry : map.entrySet()){

            if(entry.getValue() == null ||
                    (skipStrategy!=null && skipStrategy.skip(entry.getKey()))) continue;
            sb.append(entry.getValue());
        }

        return sb.toString();
    }

    private static String combineTextForCrypto(Map<String, String> map) {
        return combineTextForCrypto(map, null);
    }


    public static String sign(Map params, String password){

        String needCryptoText = combineTextForCrypto(params);

        // 签名
        JmbCryptoService jmbCryptoService =
                JmbCryptoFactory.getCryptoService(CryptoServiceType.AES_CBC_HMAC);
        String signedText = jmbCryptoService.encrypt(needCryptoText, password);

        return signedText;

    }

    //################### 验证签名 ##########################
    public static boolean verifySign(String signText,
                                      String password,
                                      final String signField){

        Map<String, String> params = JmbJsonUtils.fromJson(signText, TreeMap.class);
        String sign = params.get(signField);
        if(sign == null || JmbStringUtils.isBlank(sign)) return false;

        String text = combineTextForCrypto(params, new SkipStrategy() {
            public boolean skip(String field) {
                return signField != null && signField.equals(field);
            }
        });

        // 签名
        JmbCryptoService jmbCryptoService =
                JmbCryptoFactory.getCryptoService(CryptoServiceType.AES_CBC_HMAC);
        String decryptText = jmbCryptoService.decrypt(sign, password);

        return JmbStringUtils.equals(text, decryptText);

    }

    /**
     * 过滤不参与签名字段的策略
     */
    public static interface SkipStrategy {
        boolean skip(String field);
    }
}
