package org.nightlabs.jfire.language;

public class LanguageSyncDeactivatedException extends LanguageException {
	private static final long serialVersionUID = 1L;

	public LanguageSyncDeactivatedException() {
	}

	public LanguageSyncDeactivatedException(String message) {
		super(message);
	}

	public LanguageSyncDeactivatedException(String message, Throwable cause) {
		super(message, cause);
	}

	public LanguageSyncDeactivatedException(Throwable cause) {
		super(cause);
	}

}
