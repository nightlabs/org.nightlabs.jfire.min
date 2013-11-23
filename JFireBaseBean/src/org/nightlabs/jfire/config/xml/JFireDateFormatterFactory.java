package org.nightlabs.jfire.config.xml;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.nightlabs.config.Config;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.jfire.security.UserDescriptor;
import org.nightlabs.jfire.security.id.UserID;
import org.nightlabs.l10n.ConfigurationDateFormatter;
import org.nightlabs.l10n.IDateFormatter;
import org.nightlabs.util.NLLocale;

public class JFireDateFormatterFactory
extends org.nightlabs.l10n.DateFormatterFactory
{
	private final Map<UserID, ConfigurationDateFormatter> userID2dateFormatter = new HashMap<UserID, ConfigurationDateFormatter>();

	@Override
	public IDateFormatter sharedInstance() {
		final UserDescriptor userDescriptor = SecurityReflector.getUserDescriptor();
		final Locale locale = NLLocale.getDefault();
		final UserID userID = UserID.create(userDescriptor.getOrganisationID(), userDescriptor.getUserID());
		synchronized (userID2dateFormatter) {
			ConfigurationDateFormatter dateFormatter = userID2dateFormatter.get(userID);

			// In case, the user switched the language in the mean time, we recreate the user's shared instance.
			if (dateFormatter != null && !locale.equals(dateFormatter.getLocale()))
				dateFormatter = null;

			if (dateFormatter == null) {
				dateFormatter = new ConfigurationDateFormatter(Config.sharedInstance(), locale);
				userID2dateFormatter.put(userID, dateFormatter);
			}
			return dateFormatter;
		}
	}

}
