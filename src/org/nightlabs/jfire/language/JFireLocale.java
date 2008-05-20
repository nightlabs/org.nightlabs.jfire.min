/**
 * 
 */
package org.nightlabs.jfire.language;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.jdo.PersistenceManager;

import org.nightlabs.jfire.base.Lookup;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.security.SecurityReflector.UserDescriptor;
import org.nightlabs.util.NLLocale;

/**
 * @author Daniel Mazurek - daniel [at] nightlabs [dot] de
 *
 */
public class JFireLocale 
extends NLLocale 
{
	private Map<String, UserLocaleWrapper> completeUserID2UserLocaleWrapper = 
		new HashMap<String, UserLocaleWrapper>();
	
	private long maxDuration = 10000;
	
	@Override
	protected Locale _getDefault() 
	{
		UserDescriptor userDescriptor = SecurityReflector.getUserDescriptor();
		String completeUserID = userDescriptor.getCompleteUserID();
		UserLocaleWrapper userLocaleWrapper = completeUserID2UserLocaleWrapper.get(completeUserID);
		long currentTime = System.currentTimeMillis();
		if (userLocaleWrapper == null) 
		{
			Locale userLocale = getUserLocale(userDescriptor);
			userLocaleWrapper = new UserLocaleWrapper(completeUserID, userLocale, currentTime);
			completeUserID2UserLocaleWrapper.put(completeUserID, userLocaleWrapper);
		}
		else {
			long timeStamp = userLocaleWrapper.getTimeStamp();
			long diff = currentTime - timeStamp;
			if (diff > maxDuration) {
				Locale userLocale = getUserLocale(userDescriptor);
				userLocaleWrapper.setLocale(userLocale);
				userLocaleWrapper.setTimeStamp(currentTime);
			}
		}
		return userLocaleWrapper.getLocale();
	}

	protected Locale getUserLocale(UserDescriptor userDescriptor) 
	{
		Lookup lookup = new Lookup(userDescriptor.getOrganisationID());
		PersistenceManager pm = lookup.getPersistenceManager();
		try {
			User user = User.getUser(pm, userDescriptor.getOrganisationID(), userDescriptor.getUserID());
			return user.getLocale();
		} finally {
			pm.close();
		}		
	}
}
