package org.nightlabs.jfire.config.xml;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.nightlabs.config.Config;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.jfire.security.UserDescriptor;
import org.nightlabs.jfire.security.id.UserID;
import org.nightlabs.l10n.ConfigurationNumberFormatter;
import org.nightlabs.l10n.INumberFormatter;
import org.nightlabs.util.NLLocale;

public class JFireNumberFormatterFactory
extends org.nightlabs.l10n.NumberFormatterFactory
{
	private final Map<UserID, ConfigurationNumberFormatter> userID2numberFormatter = new HashMap<UserID, ConfigurationNumberFormatter>();

	@Override
	public INumberFormatter sharedInstance() {
		final UserDescriptor userDescriptor = SecurityReflector.getUserDescriptor();
		final Locale locale = NLLocale.getDefault();
		final UserID userID = UserID.create(userDescriptor.getOrganisationID(), userDescriptor.getUserID());
		synchronized (userID2numberFormatter) {
			ConfigurationNumberFormatter numberFormatter = userID2numberFormatter.get(userID);

			// In case, the user switched the language in the mean time, we recreate the user's shared instance.
			if (numberFormatter != null && !locale.equals(numberFormatter.getLocale()))
				numberFormatter = null;

			if (numberFormatter == null) {
				numberFormatter = new ConfigurationNumberFormatter(Config.sharedInstance(), NLLocale.getDefault());
				userID2numberFormatter.put(userID, numberFormatter);
			}
			return numberFormatter;
		}
	}

}
