package com.kbalabala.tools.mail.transport;

import com.kbalabala.tools.JmbStringUtils;
import com.kbalabala.tools.mail.email.Email;
import com.sun.mail.smtp.SMTPTransport;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.Properties;


public class PostalService {

    private static EmailTransportConfiguration emailTransportConfig = new EmailTransportConfiguration();
    private static Session session;

    public void send(Email email) throws AddressException, MessagingException {
        MimeMessage message = createMessage(email);
        send(message);
    }

    protected Session getSession() {
        if (session == null) {
            Properties properties = System.getProperties();
            properties.put("mail.smtp.host", emailTransportConfig
                    .getSmtpServer());
            properties.put("mail.smtp.auth", emailTransportConfig
                    .isAuthenticationRequired());

            session = Session.getInstance(properties);
        }

        return session;
    }

    protected MimeMessage createMessage(Email email) throws MessagingException {
        Multipart multipart = new MimeMultipart();

        MimeBodyPart mimeText = new MimeBodyPart();
        mimeText.setText(email.getBody(),"gb2312","html");
        multipart.addBodyPart(mimeText);

        MimeMessage message = new MimeMessage(getSession());

        if(JmbStringUtils.isNotBlank(email.getFromName())){
            try {
                message.setFrom(new InternetAddress(email.getFromAddress(),email.getFromName(),"gb2312"));
            }catch (Exception ex){
                message.setFrom(new InternetAddress(email.getFromAddress()));
            }
        } else {
            message.setFrom(new InternetAddress(email.getFromAddress()));
        }

        StringBuilder toAddresses = new StringBuilder();
        int i=0;
        for (String to : email.getToAddresses()) {
            if(i==0){
                toAddresses.append(to);
            }else{
                toAddresses.append(",").append(to);
            }
            i++;
        }
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toAddresses.toString()));

        StringBuilder ccAddresses = new StringBuilder();
        int j=0;
        for (String cc : email.getCcAddresses()) {
            if(j==0){
                ccAddresses.append(cc);
            }else{
                ccAddresses.append(",").append(cc);
            }
            j++;
        }
        message.setRecipients(Message.RecipientType.CC, InternetAddress.parse(ccAddresses.toString()));

        StringBuilder bccAddresses = new StringBuilder();
        int k=0;
        for (String bcc : email.getBccAddresses()) {
            if(k==0){
                bccAddresses.append(bcc);
            }else{
                bccAddresses.append(",").append(bcc);
            }
            k++;
        }
        message.setRecipients(Message.RecipientType.BCC, InternetAddress.parse(bccAddresses.toString()));

        for (String attachment : email.getAttachments()) {
            MimeBodyPart mimeAttachment = new MimeBodyPart();
            FileDataSource fds = new FileDataSource(attachment);
            mimeAttachment.setDataHandler(new DataHandler(fds));
            mimeAttachment.setFileName(fds.getName());
            multipart.addBodyPart(mimeAttachment);
        }

        if(JmbStringUtils.isNotBlank(email.getAttachmentFileName())
                && null != email.getAttachmentFileBytes()
                && email.getAttachmentFileBytes().length > 0){
            MimeBodyPart mimeAttachment = new MimeBodyPart();
            mimeAttachment.setFileName(email.getAttachmentFileName());
            DataSource ds = createDataSource(email.getAttachmentFileBytes());
            mimeAttachment.setDataHandler(new DataHandler(ds));
            //mimeAttachment.setDataHandler(new DataHandler(new ByteArrayDataSource(email.getAttachmentFileBytes(),null)));
            multipart.addBodyPart(mimeAttachment);
        }

        message.setContent(multipart);
        message.setSubject(email.getSubject(),"gb2312");
        message.setHeader("X-Mailer", "jimubox mail");
        message.setSentDate(Calendar.getInstance().getTime());

        return message;
    }

    private DataSource createDataSource(final byte[] bytes){
        DataSource ds = new DataSource() {
            @Override
            public InputStream getInputStream() throws IOException {
                return new ByteArrayInputStream(bytes);
            }

            @Override
            public OutputStream getOutputStream() throws IOException {
                throw new UnsupportedOperationException("Read-only javax.activation.DataSource");
            }

            @Override
            public String getContentType() {
                return "application/octet-stream";
            }

            @Override
            public String getName() {
                return null;
            }
        };
        return  ds;
    }

    protected void send(MimeMessage message) throws NoSuchProviderException,
            MessagingException {
        SMTPTransport smtpTransport = (SMTPTransport) getSession()
                .getTransport(getProtocol());
        if (emailTransportConfig.isAuthenticationRequired()) {
            smtpTransport.connect(emailTransportConfig.getSmtpServer(),
                    emailTransportConfig.getUsername(), emailTransportConfig
                            .getPassword());
        } else {
            smtpTransport.connect();
        }
        smtpTransport.sendMessage(message, message.getAllRecipients());
        smtpTransport.close();
    }

    protected String getProtocol() {
        String protocol = "smtp";
        if (emailTransportConfig.useSecureSmtp()) {
            protocol = "smtps";
        }
        return protocol;
    }
}
