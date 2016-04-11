package com.kbalabala.tools.mail.transport;

import com.kbalabala.tools.mail.EmailConfig;

import java.io.InputStream;
import java.util.Properties;

public class EmailTransportConfiguration {

    private static final String PROPERTIES_FILE = "mail.properties";
    private static final String KEY_SMTP_SERVER = "smtp.server";
    private static final String KEY_AUTH_REQUIRED = "auth.required";
    private static final String KEY_USE_SECURE_SMTP = "use.secure.smtp";
    private static final String KEY_USERNAME = "smtp.username";
    private static final String KEY_PASSWORD = "smtp.password";

    private static String smtpServer = "";
    private static boolean authenticationRequired = true;
    private static boolean useSecureSmtp = false;
    private static String username = null;
    private static String password = null;

    static {
        String smtpServer = EmailConfig.getSmtpHost();
        String username = EmailConfig.getSendMailer();
        String password = EmailConfig.getSendMailPass();
        configure(smtpServer, true, false, username, password);
    }

    private static Properties loadProperties() {
        Properties properties = new Properties();

        InputStream inputStream = EmailTransportConfiguration.class
                .getResourceAsStream(PROPERTIES_FILE);

        if (inputStream == null) {
            inputStream = EmailTransportConfiguration.class
                    .getResourceAsStream("/" + PROPERTIES_FILE);
        }

        try {
            properties.load(inputStream);
        } catch (Exception e) {
            // Properties file not found, no problem.
        }

        return properties;
    }

    /**
     * @param smtpServer
     * @param authenticationRequired
     * @param useSecureSmtp          Use secure SMTP to send messages.
     * @param username               The SMTP username.
     * @param password               The SMTP password.
     */
    public static void configure(String smtpServer,
                                 boolean authenticationRequired,
                                 boolean useSecureSmtp,
                                 String username,
                                 String password) {
        EmailTransportConfiguration.smtpServer = smtpServer;
        EmailTransportConfiguration.authenticationRequired = authenticationRequired;
        EmailTransportConfiguration.useSecureSmtp = useSecureSmtp;
        EmailTransportConfiguration.username = username;
        EmailTransportConfiguration.password = password;
    }

    public String getSmtpServer() {
        return smtpServer;
    }

    public boolean isAuthenticationRequired() {
        return authenticationRequired;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public boolean useSecureSmtp() {
        return useSecureSmtp;
    }

}
