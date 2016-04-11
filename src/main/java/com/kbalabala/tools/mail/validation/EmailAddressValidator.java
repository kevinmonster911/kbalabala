package com.kbalabala.tools.mail.validation;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

public class EmailAddressValidator {

	public boolean validate(String emailAddress) {
		if (emailAddress == null) {
			return false;
		}

		try {
			// Validate using Java Mail API
			// Don't know if this is the best way, but it's good enough for now
			new InternetAddress(emailAddress, true);
		} catch (AddressException e) {
			return false;
		}

		return true;
	}
}
