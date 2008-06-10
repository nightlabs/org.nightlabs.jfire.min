package org.nightlabs.jfire.init;

public enum Resolution {
	Optional,
	Required;
	
	public static Resolution getEnumConstant(String message) {
		if (message.equals("Optional") || message.equals("optional"))
			return Optional;
		else if (message.equals("Required") || message.equals("required"))
			return Required;
		else
			throw new IllegalArgumentException("No such constant found.");
	}
}
