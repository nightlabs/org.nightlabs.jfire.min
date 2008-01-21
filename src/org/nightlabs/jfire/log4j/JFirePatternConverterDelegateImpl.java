package org.nightlabs.jfire.log4j;

import org.apache.log4j.spi.LoggingEvent;
import org.nightlabs.jfire.security.NoUserException;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.jfire.security.SecurityReflector.UserDescriptor;

public class JFirePatternConverterDelegateImpl
implements JFirePatternConverterDelegate
{
	public static enum Type {
		organisationID,
		userID,
		completeUserID
	}

	@Override
	public String getDelegateID()
	{
		return type.name();
	}

	private Type type;

	public JFirePatternConverterDelegateImpl(Type type)
	{
		this.type = type;
	}

	@Override
	public String convert(LoggingEvent event)
	{
		UserDescriptor userDescriptor;
		try {
			userDescriptor = SecurityReflector.getUserDescriptor();
		} catch (NoUserException x) {
			return "{no user}";
		}

		switch (type) {
			case organisationID:
				return userDescriptor.getOrganisationID();

			case userID:
				return userDescriptor.getUserID();

			case completeUserID:
				return userDescriptor.getCompleteUserID();

			default:
				return "{unknown type}";
		}
	}

}
