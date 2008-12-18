package org.nightlabs.jfire.config.xml;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.nightlabs.config.Config;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.jfire.security.SecurityReflector.UserDescriptor;
import org.nightlabs.jfire.security.id.UserID;
import org.nightlabs.l10n.DateFormatter;
import org.nightlabs.util.NLLocale;

public class JFireDateFormatterFactory
extends org.nightlabs.l10n.DateFormatterFactory
{
	private Map<UserID, DateFormatter> userID2dateFormatter = new HashMap<UserID, DateFormatter>();

	@Override
	public DateFormatter sharedInstance() {
		UserDescriptor userDescriptor = SecurityReflector.getUserDescriptor();
		Locale locale = NLLocale.getDefault();
		UserID userID = UserID.create(userDescriptor.getOrganisationID(), userDescriptor.getUserID());
		synchronized (userID2dateFormatter) {
			DateFormatter dateFormatter = userID2dateFormatter.get(userID);

			// In case, the user switched the language in the mean time, we recreate the user's shared instance.
			if (dateFormatter != null && !locale.equals(dateFormatter.getLocale()))
				dateFormatter = null;

			if (dateFormatter == null) {
				dateFormatter = new DateFormatter(Config.sharedInstance(), locale);
				userID2dateFormatter.put(userID, dateFormatter);
			}
			return dateFormatter;
		}
	}

}
