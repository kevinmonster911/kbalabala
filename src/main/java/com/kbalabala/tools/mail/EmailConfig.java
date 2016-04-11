package com.kbalabala.tools.mail;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;

import java.io.File;
import java.math.BigDecimal;

/**
 * <p/>
 *
 * @author kevin
 * @since 15-4-27
 */
public class EmailConfig {

    //check flag
    private static final boolean IS_CHECK_NOT_CONFIG_KEY = false;

    //environments
    private static PropertiesConfiguration settingsConfiguration = null;

    //settings
    private static PropertiesConfiguration environmentsConfiguration = null;

    public static String SETTINGS_FILE_PATH = null;
    static {
        //settings
        SETTINGS_FILE_PATH = System.getenv("JIMUBOX_SETTINGS_FILE") == null ? System.getProperty("JIMUBOX_SETTINGS_FILE") : System.getenv("JIMUBOX_SETTINGS_FILE");
        if (SETTINGS_FILE_PATH == null) {
            throw new IllegalArgumentException("没有设置环境变量：JIMUBOX_SETTINGS_FILE");
        }
        File file = new File(SETTINGS_FILE_PATH);
        if (!file.exists()) {
            throw new IllegalArgumentException("环境变量：JIMUBOX_SETTINGS_FILE 设置的配置文件找不到");
        }
        try {
            //init with FileChangedReloadingStrategy
            settingsConfiguration = new PropertiesConfiguration(SETTINGS_FILE_PATH);
            settingsConfiguration.setReloadingStrategy(new FileChangedReloadingStrategy());
        } catch (Exception e) {
            throw new RuntimeException("配置文件加载失败，请检查配置文件！");
        }
    }

    // ############### properties ##################
    public static String getSmtpHost() {
        return getString("SMTP_HOST", "smtp.exmail.qq.com");
    }

    public static int getSmtpPort() {
        return getInt("SMTP_PORT", 25);
    }

    public static String getSendMailer() {
        return getString("SEND_MAILER", "service@jimubox.com");
    }

    public static String getSendMailPass() {
        return getString("SEND_MAIL_PASS", "9ol.8ik,");
    }

    public static String getSendDisplayName() {
        return getString("SEND_DISPLAY_NAME", "积木盒子-客服");
    }
    // #############################################

    private static void checkNullValue(String key, String value, boolean env) {
        if (IS_CHECK_NOT_CONFIG_KEY && value == null) {
            throw new RuntimeException((env ? "环境" : "业务") + "配置文件中找不到Key：'" + key + "'，请检查相应的配置文件！");
        }
    }

    protected static String getString(String key, String defaultValue) {
        return getString(settingsConfiguration, key, defaultValue);
    }

    protected static Boolean getBool(String key, Boolean defaultValue) {
        return getBool(settingsConfiguration, key, defaultValue);
    }

    protected static int getInt(String key, int defaultValue) {
        return getInt(settingsConfiguration, key, defaultValue);
    }

    protected static BigDecimal getDecimal(String key, BigDecimal defaultValue) {
        return getDecimal(settingsConfiguration, key, defaultValue);
    }

    //get environment value
    protected static String getEnvString(String key, String defaultValue) {
        return getString(environmentsConfiguration, key, defaultValue);
    }

    protected static String getString(PropertiesConfiguration configuration, String key, String defaultValue) {
        String value = configuration.getString(key);
        checkNullValue(key , value, false);
        //no value
        if (value == null) {
            return defaultValue;
        }
        try {
            value = new String(value.getBytes("ISO8859-1"), "UTF-8");
        } catch (Exception e) {
            return defaultValue;
        }
        return value == null ? defaultValue : value;
    }

    protected static Boolean getBool(PropertiesConfiguration configuration, String key, Boolean defaultValue) {
        String value = configuration.getString(key);
        checkNullValue(key , value, false);
        return value == null ? defaultValue : Boolean.parseBoolean(value);
    }

    protected static int getInt(PropertiesConfiguration configuration, String key, int defaultValue) {
        String value = configuration.getString(key);
        checkNullValue(key , value, false);
        return value == null ? defaultValue : Integer.parseInt(value);
    }

    protected static BigDecimal getDecimal(PropertiesConfiguration configuration, String key, BigDecimal defaultValue) {
        String value = settingsConfiguration.getString(key);
        checkNullValue(key , value, false);
        return value == null ? defaultValue : new BigDecimal(value);
    }
}
