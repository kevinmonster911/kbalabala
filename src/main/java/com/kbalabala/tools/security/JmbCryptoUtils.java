package com.kbalabala.tools.security;

import java.security.GeneralSecurityException;
import java.security.SecureRandom;

/**
 * Created by kevin on 15-4-22.
 */
public class JmbCryptoUtils {

    private static final String RANDOM_ALGORITHM = "SHA1PRNG";

    /**
     * 生成加密矢量
     * @param length
     * @return
     * @throws GeneralSecurityException
     */
    public static byte[] randomBytes(int length) throws GeneralSecurityException {
        SecureRandom random = SecureRandom.getInstance(RANDOM_ALGORITHM);
        byte[] b = new byte[length];
        random.nextBytes(b);
        return b;
    }
}
