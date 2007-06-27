package org.nightlabs.jfire.testsuite.login;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;

import org.nightlabs.jfire.security.id.UserID;

/**
 * 
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 *
 */
public class JFireLoginTestHelper {

	public static boolean checkUser(PersistenceManager pm) {
		try {
			pm.getObjectById(UserID.create("chezfrancois.jfire.org", "francois"));
		} catch (JDOObjectNotFoundException e) {
			return false;
		}
		return true;
	}
}
