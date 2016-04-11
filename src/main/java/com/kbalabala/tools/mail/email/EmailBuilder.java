package com.kbalabala.tools.mail.email;

public interface EmailBuilder {

	EmailBuilder from(String address);

    EmailBuilder fromName(String fromName);

	EmailBuilder to(String... addresses);

	EmailBuilder cc(String... addresses);

	EmailBuilder bcc(String... addresses);

	EmailBuilder withSubject(String subject);

	EmailBuilder withBody(String body);
	
	EmailBuilder withAttachment(String... attachments);

	void send();

    EmailBuilder attachmentFileName(String fileName);

    EmailBuilder attachmentFileBytes(byte[] bytes);
}