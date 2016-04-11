package com.kbalabala.tools.security;

import com.kbalabala.tools.Constants;
import com.kbalabala.tools.JmbStringUtils;
import jodd.util.Base64;
import jodd.util.StringUtil;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.*;

import static java.util.Arrays.copyOfRange;

/**
 * <p>
 *  利用AES(with jce)加密，块大小256要安装jce7在java security
 * </p>
 * @author kevin
 * @since  2015－4-22
 */
public class AesJCE256Service implements JmbCryptoService {


    @Override
    public String encrypt(String text, String pwd) {
        try {
            return encrypt(text, pwd, null);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public String decrypt(String text, String pwd) {
        try {
            return decrypt(text, pwd, null);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public String encrypt(String text, String pwd, String iv) {
        if(StringUtil.isBlank(iv)) throw new IllegalArgumentException("iv is required for AesJCE256 encrypting");
        return doEncrypt(text, pwd, iv);
    }

    @Override
    public String decrypt(String text, String pwd, String iv) {
        if(StringUtil.isBlank(iv)) throw new IllegalArgumentException("iv is required for AesJCE256 decrypting");
        return doDecrypt(text, pwd, iv);
    }

    /**
     * pwd and salt将不会参与密钥的生成
     * @param pwd
     * @param salt
     * @return
     * @throws GeneralSecurityException
     */
    @Override
    public String generateKey(String pwd, String salt){
        try {
            return generateKey() + Constants.LINE_FEED + generateIv();
        } catch (Exception e) {
            return null;
        }
    }

    // ############################ detail ##########################
    private static final String CIPHER_TRANSFORMATION = "AES/CBC/PKCS5Padding";
    private static final String ALGORITHM = "AES";


    private String generateKey() {
        try {
            KeyGenerator kg = KeyGenerator.getInstance(ALGORITHM);
            kg.init(256);
            SecretKey secretKey=kg.generateKey();
            return Base64.encodeToString(secretKey.getEncoded());
        } catch (Exception e) {
            return null;
        }
    }

    private String generateIv() {
        try {
            return Base64.encodeToString(JmbCryptoUtils.randomBytes(16));
        } catch (Exception e) {
            return null;
        }
    }


    private String doDecrypt(String cipherText, String key, String iv) {

        try {
            Cipher aes256CipherForEncryption = Cipher.getInstance(CIPHER_TRANSFORMATION);
            aes256CipherForEncryption.init(Cipher.DECRYPT_MODE,
                                            new SecretKeySpec(Base64.decode(key), ALGORITHM),
                                            new IvParameterSpec(Base64.decode(iv)));

            byte[] decryptedData = aes256CipherForEncryption.doFinal(Base64.decode(cipherText));
            return new String(decryptedData);

        } catch (Exception e) {
            return null;
        }
    }

    private String doEncrypt(String text, String key, String iv) {

        try {
            Cipher aes256CipherForEncryption = Cipher.getInstance(CIPHER_TRANSFORMATION);
            aes256CipherForEncryption.init(Cipher.ENCRYPT_MODE,
                                            new SecretKeySpec(Base64.decode(key), ALGORITHM),
                                            new IvParameterSpec(Base64.decode(iv)));

            byte[] encryptedData = aes256CipherForEncryption.doFinal(JmbStringUtils.getBytesUtf8(text));
            return Base64.encodeToString(encryptedData);

        }  catch (Exception e) {
            return null;
        }
    }

}
