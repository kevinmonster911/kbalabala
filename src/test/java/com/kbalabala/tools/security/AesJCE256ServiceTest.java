package com.kbalabala.tools.security;

import com.kbalabala.tools.Constants;

/**
 * Created by kevin on 15-4-22.
 */
public class AesJCE256ServiceTest {

    public static void main(String[] args) {

        JmbCryptoService jmbCryptoService = new AesJCE256Service();
        String text = "我在一家叫积木盒子的公司工作！！！！";
        String[] pwdAndIv = jmbCryptoService.generateKey(null, null).split(Constants.LINE_FEED);

        System.out.println(pwdAndIv[0]);
        System.out.println(pwdAndIv[1]);
        System.out.println(jmbCryptoService.encrypt(text, pwdAndIv[0], pwdAndIv[1]));
        System.out.println(jmbCryptoService.decrypt(jmbCryptoService.encrypt(text, pwdAndIv[0], pwdAndIv[1]), pwdAndIv[0], pwdAndIv[1]));


    }
}
