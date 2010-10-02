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
import javax.jdo.Query;

import org.apache.log4j.Logger;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.base.Lookup;
import org.nightlabs.jfire.organisation.LocalOrganisation;
import org.nightlabs.jfire.person.Person;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.security.UserDescriptor;
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

	private static final long userLocaleCacheLifetimeMSec = 5 * 60 * 1000; // 5 minutes

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
//		Lookup lookup = new Lookup(userDescriptor.getOrganisationID());
//		PersistenceManager pm = lookup.createPersistenceManager();
		// Creating (and closing) this new PM causes the "real" PM assigned to the current TX to be cleared (e.g. the fetch-plan is reset).
		// Therefore, we first try to get a current one assigned to the current TX. Marco.
		boolean closePM = false;
		PersistenceManager pm = NLJDOHelper.getThreadPersistenceManager(false);
		if (pm != null) {
			// We check, if the pm is (still) the right one, because maybe we changed the user (without yet acquiring a new PM for the new current org).
			Query q = pm.newQuery(LocalOrganisation.class);
			q.setUnique(true);
			LocalOrganisation localOrganisation = (LocalOrganisation) q.execute();
			q.closeAll();
			if (localOrganisation != null) {
//			Iterator<LocalOrganisation> iteratorLocalOrganisation = pm.getExtent(LocalOrganisation.class).iterator();
//			if (iteratorLocalOrganisation.hasNext()) { // during organisation setup, there is no LocalOrganisation, yet, thus we have to check (and cannot use LocalOrganisation.getLocalOrganisation(...))
//				LocalOrganisation localOrganisation = iteratorLocalOrganisation.next();
//
//				if (iteratorLocalOrganisation.hasNext())
//					throw new IllegalStateException("There are multiple instances of LocalOrganisation in the datastore!!!");

				if (!localOrganisation.getOrganisationID().equals(userDescriptor.getOrganisationID()))
					pm = null;
			}
			else
				logger.info("getUserLocale: There is no LocalOrganisation existing!");
		}
		if (pm == null) {
			Lookup lookup = new Lookup(userDescriptor.getOrganisationID());
			closePM = true;
			pm = lookup.createPersistenceManager();
		}
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

			if (logger.isDebugEnabled())
				logger.debug("getUserLocale: The user does not have a person assigned! organisationID=" + userDescriptor.getOrganisationID() + " userID=" + userDescriptor.getUserID());

			// The user has no person assigned => fall back to organisation's locale.
			LocalOrganisation localOrganisation = null;
			try {
				localOrganisation = LocalOrganisation.getLocalOrganisation(pm);
			} catch (JDOObjectNotFoundException e) {
				// The local organisation could not be found, this is only legal when we are currently creating
				// the organisation for this datastore
				// TO DO: Is there a way to find out whether we currently create the local organisation? No, there is no way, and I think it's not important here. I make a debug message out of it. Marco.
				if (logger.isDebugEnabled()) {
					if (logger.isTraceEnabled())
						logger.trace("getUserLocale: Could not get the local organisation from the datastore, will return System Locale.", e);
					else
						logger.debug("getUserLocale: Could not get the local organisation from the datastore, will return System Locale.");
				}

				return Locale.getDefault();
			}
			if (!userDescriptor.getOrganisationID().equals(localOrganisation.getOrganisationID()))
				throw new IllegalStateException("currentUser.organisationID != localOrganisation.organisationID :: " + userDescriptor.getOrganisationID() + " != " + localOrganisation.getOrganisationID());

			person = localOrganisation.getOrganisation().getPerson();
			if (person != null)
				return person.getLocale();

			if (logger.isDebugEnabled())
				logger.debug("getUserLocale: The LocalOrganisation does not have a person assigned! organisationID=" + userDescriptor.getOrganisationID());

			return Locale.getDefault();
		} finally {
			if (closePM)
				pm.close();
		}
	}
}
