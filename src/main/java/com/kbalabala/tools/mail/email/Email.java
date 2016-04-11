package com.kbalabala.tools.mail.email;

import java.util.Set;

public interface Email {

	String getFromAddress();

    String getFromName();
	
	Set<String> getToAddresses();
	
	Set<String> getCcAddresses();
	
	Set<String> getBccAddresses();
	
	Set<String> getAttachments();
	
	String getSubject();
	
	String getBody();

    String getAttachmentFileName();

    byte[] getAttachmentFileBytes();
}