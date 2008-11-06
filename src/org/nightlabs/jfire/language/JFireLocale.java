/**
 *
 */
package org.nightlabs.jfire.language;

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;

import org.apache.log4j.Logger;
import org.nightlabs.jfire.base.Lookup;
import org.nightlabs.jfire.person.Person;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.security.SecurityReflector.UserDescriptor;
import org.nightlabs.util.NLLocale;

/**
 * @author Daniel Mazurek - daniel [at] nightlabs [dot] de
 * @author marco schulze - marco at nightlabs dot de
 */
public class JFireLocale
extends NLLocale
{
	private static final Logger logger = Logger.getLogger(JFireLocale.class);

	private Map<String, UserLocaleWrapper> completeUserID2UserLocaleWrapper = Collections.synchronizedMap(
			new HashMap<String, UserLocaleWrapper>()
	);

	private static final long userLocaleCacheLifetimeMSec = 10 * 60 * 1000; // 10 minutes

	@Override
	protected Locale _getDefault()
	{
		// We do not synchronize this method but instead only synchronize the Map completeUserID2UserLocaleWrapper,
		// because this minimizes the risk of a dead lock. Even though, it instead gives us the risk that
		// getUserLocale(...) is called multiple times in parallel for the same user, we choose this strategy, because
		// it is very unlikely. Even if this method is called in parallel for the same user,
		// it doesn't really matter (a little bit of unnecessary work, but no real problem). In this rare case, the
		// JDO 2nd-level cache will reduce the unnecessary work to a minimum, anyway.

		UserDescriptor userDescriptor = SecurityReflector.getUserDescriptor();
		String completeUserID = userDescriptor.getCompleteUserID();
		UserLocaleWrapper userLocaleWrapper = completeUserID2UserLocaleWrapper.get(completeUserID);
		long currentTime = System.currentTimeMillis();

		if (userLocaleWrapper != null && (currentTime - userLocaleWrapper.getTimeStamp() > userLocaleCacheLifetimeMSec))
			userLocaleWrapper = null; // expired => create a new one

		if (userLocaleWrapper == null) {
			Locale userLocale = getUserLocale(userDescriptor);
			userLocaleWrapper = new UserLocaleWrapper(completeUserID, userLocale, currentTime);
			completeUserID2UserLocaleWrapper.put(completeUserID, userLocaleWrapper);
		}

		return userLocaleWrapper.getLocale();
	}

	protected Locale getUserLocale(UserDescriptor userDescriptor)
	{
		Lookup lookup = new Lookup(userDescriptor.getOrganisationID());
		PersistenceManager pm = lookup.getPersistenceManager();
		try {
			Person person;
			try {
				User user;
				user = User.getUser(pm, userDescriptor.getOrganisationID(), userDescriptor.getUserID());
				person = user.getPerson();
			} catch (JDOObjectNotFoundException x) {
				// During organisation-setup, the user might not exist. Hence, we ignore it (with a debug message).
				person = null;
				if (logger.isDebugEnabled())
					logger.debug("getUserLocale: The user does not exist! organisationID=" + userDescriptor.getOrganisationID() + " userID=" + userDescriptor.getUserID());
			}
			if (person != null)
				return person.getLocale();
			else {
				if (logger.isDebugEnabled())
					logger.debug("getUserLocale: The user does not have a person assigned! organisationID=" + userDescriptor.getOrganisationID() + " userID=" + userDescriptor.getUserID());

				return Locale.getDefault();
			}
		} finally {
			pm.close();
		}
	}
}
