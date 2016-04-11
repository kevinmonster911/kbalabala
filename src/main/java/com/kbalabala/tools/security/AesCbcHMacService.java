package com.kbalabala.tools.security;

import com.kbalabala.tools.JmbStringUtils;
import jodd.util.Base64;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Arrays;

import static java.util.Arrays.copyOfRange;

/**
 * <p>
 *  利用AES加密，HmacSHA256消息摘要组合成对明文加密，防篡改的Security类。
 *  并提供密钥，盐生成和管理的方法
 *  <ol>
 *   <li>生成盐 saltString(generateSalt())</li>
 *   <li>生成AES，HMAC密钥对generateKeyFromPassword(password, salt)</li>
 *   <li>转换AES，HMAC密钥对为Base64字符串（用于配置文件存储）keyString(key)</li>
 *   <li>加密明文encrypt(text, key)</li>
 *   <li>解密decryptString(civ, key)</li>
 *  </ol>
 * </p>
 * @author kevin
 * @since  2015－4-22
 */
public class AesCbcHMacService implements JmbCryptoService {


    @Override
    public String encrypt(String text, String pwd) {
        return encrypt(text, pwd, null);
    }

    @Override
    public String decrypt(String text, String pwd) {
        return decrypt(text, pwd, null);
    }

    @Override
    public String encrypt(String text, String pwd, String iv) {
        try {
            return encrypt(text, keys(pwd)).toString();
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public String decrypt(String text, String pwd, String iv) {
        try {
            return decryptString(new CipherTextIvMac(text), keys(pwd));
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public String generateKey(String pwd, String salt) {
        try {
            return generateKeyFromPassword(pwd, JmbStringUtils.isBlank(salt) ? saltString(generateSalt()) : salt).toString();
        } catch (GeneralSecurityException e) {
            return null;
        }
    }

    // ###########################  detail ################################
    private static final String CIPHER_TRANSFORMATION = "AES/CBC/PKCS5Padding";
    private static final String CIPHER = "AES";
    private static final String RANDOM_ALGORITHM = "SHA1PRNG";
    private static final int AES_KEY_LENGTH_BITS = 128;
    private static final int IV_LENGTH_BYTES = 16;
    private static final int PBE_ITERATION_COUNT = 10000;
    private static final int PBE_SALT_LENGTH_BITS = AES_KEY_LENGTH_BITS;
    private static final String PBE_ALGORITHM = "PBKDF2WithHmacSHA1";
    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private static final int HMAC_KEY_LENGTH_BITS = 256;

    /**
     * SecretKeys ＝》base64(aesKey) : base64(hmacKey)
     *
     * @param keys
     */
    public String keyString(SecretKeys keys) {
        return keys.toString();
    }

    /**
     * 根据aesKey:hmacKey生成密钥对。
     * @param keysStr base64的AES key和hmac key ＝》aesKey : hmacKey.
     */
    public SecretKeys keys(String keysStr) throws InvalidKeyException {
        String[] keysArr = keysStr.split(":");

        if (keysArr.length != 2) {
            throw new IllegalArgumentException("Cannot parse aesKey:hmacKey");

        } else {
            byte[] confidentialityKey = Base64.decode(keysArr[0]);
            if (confidentialityKey.length != AES_KEY_LENGTH_BITS /8) {
                throw new InvalidKeyException("Base64 decoded key is not " + AES_KEY_LENGTH_BITS + " bytes");
            }
            byte[] integrityKey = Base64.decode(keysArr[1]);
            if (integrityKey.length != HMAC_KEY_LENGTH_BITS /8) {
                throw new InvalidKeyException("Base64 decoded key is not " + HMAC_KEY_LENGTH_BITS + " bytes");
            }

            return new SecretKeys(
                    new SecretKeySpec(confidentialityKey, 0, confidentialityKey.length, CIPHER),
                    new SecretKeySpec(integrityKey, HMAC_ALGORITHM));
        }
    }

    /**
     * 生成AES和HMAC keys
     */
    public SecretKeys generateKey() throws GeneralSecurityException {
        KeyGenerator keyGen = KeyGenerator.getInstance(CIPHER);
        keyGen.init(AES_KEY_LENGTH_BITS);
        SecretKey confidentialityKey = keyGen.generateKey();

        byte[] integrityKeyBytes = randomBytes(HMAC_KEY_LENGTH_BITS / 8);//to get bytes
        SecretKey integrityKey = new SecretKeySpec(integrityKeyBytes, HMAC_ALGORITHM);

        return new SecretKeys(confidentialityKey, integrityKey);
    }

    /**
     * 根据密码和盐生成AES和HMAC keys
     *
     * @param password 密码
     * @param salt 盐
     */
    public SecretKeys generateKeyFromPassword(String password, byte[] salt) throws GeneralSecurityException {
        KeySpec keySpec = new PBEKeySpec(password.toCharArray(), salt,
                PBE_ITERATION_COUNT, AES_KEY_LENGTH_BITS + HMAC_KEY_LENGTH_BITS);
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(PBE_ALGORITHM);
        byte[] keyBytes = keyFactory.generateSecret(keySpec).getEncoded();

        byte[] confidentialityKeyBytes = copyOfRange(keyBytes, 0, AES_KEY_LENGTH_BITS /8);
        byte[] integrityKeyBytes = copyOfRange(keyBytes, AES_KEY_LENGTH_BITS /8, AES_KEY_LENGTH_BITS /8 + HMAC_KEY_LENGTH_BITS /8);

        SecretKey confidentialityKey = new SecretKeySpec(confidentialityKeyBytes, CIPHER);
        SecretKey integrityKey = new SecretKeySpec(integrityKeyBytes, HMAC_ALGORITHM);

        return new SecretKeys(confidentialityKey, integrityKey);
    }

    /**
     * 根据密码和盐生成AES和HMAC keys
     *
     * @param password 密码
     * @param salt base64盐
     */
    public SecretKeys generateKeyFromPassword(String password, String salt) throws GeneralSecurityException {
        return generateKeyFromPassword(password, Base64.decode(salt));
    }

    /**
     * 随机生成128位salt.
     */
    public byte[] generateSalt() throws GeneralSecurityException {
        return randomBytes(PBE_SALT_LENGTH_BITS);
    }

    /**
     * base64（盐）
     *
     * @param salt
     * @return salt
     */
    public String saltString(byte[] salt) {
        return Base64.encodeToString(salt);
    }


    /**
     * 生成加密矢量
     * @return IV
     */
    public byte[] generateIv() throws GeneralSecurityException {
        return randomBytes(IV_LENGTH_BYTES);
    }

    private byte[] randomBytes(int length) throws GeneralSecurityException {
        SecureRandom random = SecureRandom.getInstance(RANDOM_ALGORITHM);
        byte[] b = new byte[length];
        random.nextBytes(b);
        return b;
    }

    /*
     * -----------------------------------------------------------------
     * 加密
     * -----------------------------------------------------------------
     */

    /**
     * 加密
     *
     * @param plaintext 待加密文本
     * @param secretKeys AES和HMAC密钥
     * @return IV, ciphertext, mac组合信息
     */
    public CipherTextIvMac encrypt(String plaintext, SecretKeys secretKeys)
            throws UnsupportedEncodingException, GeneralSecurityException {
        return encrypt(plaintext, secretKeys, "UTF-8");
    }

    /**
     * 加密
     * @param plaintext 待加密文本
     * @param secretKeys AES和HMAC密钥
     * @return IV, ciphertext, mac组合信息
     */
    public CipherTextIvMac encrypt(String plaintext, SecretKeys secretKeys, String encoding)
            throws UnsupportedEncodingException, GeneralSecurityException {
        return encrypt(plaintext.getBytes(encoding), secretKeys);
    }

    /**
     * 加密
     * @param plaintext 待加密文本
     * @param secretKeys AES和HMAC密钥
     * @return IV, ciphertext, mac组合信息
     */
    public CipherTextIvMac encrypt(byte[] plaintext, SecretKeys secretKeys)
            throws GeneralSecurityException {
        byte[] iv = generateIv();
        Cipher aesCipherForEncryption = Cipher.getInstance(CIPHER_TRANSFORMATION);
        aesCipherForEncryption.init(Cipher.ENCRYPT_MODE, secretKeys.getConfidentialityKey(), new IvParameterSpec(iv));

        iv = aesCipherForEncryption.getIV();
        byte[] byteCipherText = aesCipherForEncryption.doFinal(plaintext);
        byte[] ivCipherConcat = CipherTextIvMac.ivCipherConcat(iv, byteCipherText);

        byte[] integrityMac = generateMac(ivCipherConcat, secretKeys.getIntegrityKey());
        return new CipherTextIvMac(byteCipherText, iv, integrityMac);
    }


    /*
     * -----------------------------------------------------------------
     * 解密
     * -----------------------------------------------------------------
     */

    /**
     * AES/CBC解密.
     *
     * @param civ The cipher text, IV, and mac
     * @param secretKeys The AES & HMAC keys
     */
    public String decryptString(CipherTextIvMac civ, SecretKeys secretKeys, String encoding)
            throws UnsupportedEncodingException, GeneralSecurityException {
        return new String(decrypt(civ, secretKeys), encoding);
    }

    /**
     * AES/CBC解密.
     *
     * @param civ The cipher text, IV, and mac
     * @param secretKeys The AES & HMAC keys
     */
    public String decryptString(CipherTextIvMac civ, SecretKeys secretKeys)
            throws UnsupportedEncodingException, GeneralSecurityException {
        return decryptString(civ, secretKeys, "UTF-8");
    }

    /**
     * AES/CBC解密.
     *
     * @param civ the cipher text, iv, and mac
     * @param secretKeys the AES & HMAC keys
     */
    public byte[] decrypt(CipherTextIvMac civ, SecretKeys secretKeys)
            throws GeneralSecurityException {

        byte[] ivCipherConcat = CipherTextIvMac.ivCipherConcat(civ.getIv(), civ.getCipherText());
        byte[] computedMac = generateMac(ivCipherConcat, secretKeys.getIntegrityKey());
        if (constantTimeEq(computedMac, civ.getMac())) {
            Cipher aesCipherForDecryption = Cipher.getInstance(CIPHER_TRANSFORMATION);
            aesCipherForDecryption.init(Cipher.DECRYPT_MODE, secretKeys.getConfidentialityKey(),
                    new IvParameterSpec(civ.getIv()));
            return aesCipherForDecryption.doFinal(civ.getCipherText());
        } else {
            throw new GeneralSecurityException("MAC stored in civ does not match computed MAC.");
        }
    }

    /**
     * 根据HMAC_ALGORITHM算法生成密码文本摘要
     * @param integrityKey
     * @param byteCipherText
     */
    public byte[] generateMac(byte[] byteCipherText, SecretKey integrityKey) throws NoSuchAlgorithmException, InvalidKeyException {
        Mac sha256_HMAC = Mac.getInstance(HMAC_ALGORITHM);
        sha256_HMAC.init(integrityKey);
        return sha256_HMAC.doFinal(byteCipherText);
    }
    /**
     * 封装confidentialityKey AES KEY和integrityKey HMAC KEY
     */
    public static class SecretKeys {
        private SecretKey confidentialityKey;
        private SecretKey integrityKey;

        public SecretKeys(SecretKey confidentialityKeyIn, SecretKey integrityKeyIn) {
            setConfidentialityKey(confidentialityKeyIn);
            setIntegrityKey(integrityKeyIn);
        }

        public SecretKey getConfidentialityKey() {
            return confidentialityKey;
        }

        public void setConfidentialityKey(SecretKey confidentialityKey) {
            this.confidentialityKey = confidentialityKey;
        }

        public SecretKey getIntegrityKey() {
            return integrityKey;
        }

        public void setIntegrityKey(SecretKey integrityKey) {
            this.integrityKey = integrityKey;
        }

        @Override
        public String toString () {
            return Base64.encodeToString(getConfidentialityKey().getEncoded())
                    + ":" + Base64.encodeToString(getIntegrityKey().getEncoded());
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + confidentialityKey.hashCode();
            result = prime * result + integrityKey.hashCode();
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            SecretKeys other = (SecretKeys) obj;
            if (!integrityKey.equals(other.integrityKey))
                return false;
            if (!confidentialityKey.equals(other.confidentialityKey))
                return false;
            return true;
        }
    }

    public boolean constantTimeEq(byte[] a, byte[] b) {
        if (a.length != b.length) {
            return false;
        }
        int result = 0;
        for (int i = 0; i < a.length; i++) {
            result |= a[i] ^ b[i];
        }
        return result == 0;
    }

    /**
     * 包装加密矢量，待加密文本，摘要三种密码信息
     * 提供组合和拆分三种信息的功能。
     */
    public static class CipherTextIvMac {
        private final byte[] cipherText;
        private final byte[] iv;
        private final byte[] mac;

        public byte[] getCipherText() {
            return cipherText;
        }

        public byte[] getIv() {
            return iv;
        }

        public byte[] getMac() {
            return mac;
        }

        /**
         * 初始化iv,ciphertext,mac
         * @param c The ciphertext
         * @param i The IV
         * @param h The mac
         */
        public CipherTextIvMac(byte[] c, byte[] i, byte[] h) {
            cipherText = Arrays.copyOf(c, c.length);
            iv = Arrays.copyOf(i, i.length);
            mac = Arrays.copyOf(h, h.length);
        }

        /**
         * 解析［iv:ciphertext:mac］形式的Base64编码的字符串
         * @param base64IvAndCiphertext
         */
        public CipherTextIvMac(String base64IvAndCiphertext) {
            String[] civArray = base64IvAndCiphertext.split(":");
            if (civArray.length != 3) {
                throw new IllegalArgumentException("Cannot parse iv:ciphertext:mac");
            } else {
                iv = Base64.decode(civArray[0]);
                mac = Base64.decode(civArray[1]);
                cipherText = Base64.decode(civArray[2]);
            }
        }

        /**
         * 连接矢量和密码文本
         * @param iv
         * @param cipherText
         */
        public static byte[] ivCipherConcat(byte[] iv, byte[] cipherText) {
            byte[] combined = new byte[iv.length + cipherText.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(cipherText, 0, combined, iv.length, cipherText.length);
            return combined;
        }

        /**
         * base64编码加密矢量，mac摘要和待加密文本
         */
        @Override
        public String toString() {
            String ivString = Base64.encodeToString(iv);
            String cipherTextString = Base64.encodeToString(cipherText);
            String macString = Base64.encodeToString(mac);
            return String.format(ivString + ":" + macString + ":" + cipherTextString);
        }

        /**
         * 简单hash方式（jdk常用）
         * @return
         */
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + Arrays.hashCode(cipherText);
            result = prime * result + Arrays.hashCode(iv);
            result = prime * result + Arrays.hashCode(mac);
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            CipherTextIvMac other = (CipherTextIvMac) obj;
            if (!Arrays.equals(cipherText, other.cipherText))
                return false;
            if (!Arrays.equals(iv, other.iv))
                return false;
            if (!Arrays.equals(mac, other.mac))
                return false;
            return true;
        }
    }
}
