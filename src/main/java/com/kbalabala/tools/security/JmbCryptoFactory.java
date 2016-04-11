package com.kbalabala.tools.security;

import java.util.HashMap;
import java.util.Map;

/**
 * 简单加密服务工厂用于根据加密类型选择加密服务
 *
 * @author kevin
 * @since 2015-4-22.
 */
public abstract class JmbCryptoFactory {

    private static Map<CryptoServiceType, JmbCryptoService> cryptoServiceMap = new HashMap<CryptoServiceType, JmbCryptoService>();

    static {
        cryptoServiceMap.put(CryptoServiceType.AES_CBC_HMAC, new AesCbcHMacService());
        cryptoServiceMap.put(CryptoServiceType.AES_JEC_256, new AesJCE256Service());
    }

    public static JmbCryptoService getCryptoService(CryptoServiceType cst) {
        return cryptoServiceMap.get(cst == null ? CryptoServiceType.AES_CBC_HMAC : cst);
    }
}
