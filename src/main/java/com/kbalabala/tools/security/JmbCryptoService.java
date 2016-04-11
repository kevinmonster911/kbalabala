package com.kbalabala.tools.security;

/**
 * 基础加密服务接口，所有加密服务必须继承此接口实现服务
 *
 * @author kevin
 * @since 2015-4-22
 */
public interface JmbCryptoService {

    String encrypt(String text, String pwd);

    String encrypt(String text, String pwd, String iv);

    String decrypt(String text, String pwd);

    String decrypt(String text, String pwd, String iv);

    String generateKey(String pwd, String salt);
}
