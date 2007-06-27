package org.nightlabs.jfire.testsuite.login;

import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;

import org.apache.log4j.Logger;
import org.nightlabs.ModuleException;
import org.nightlabs.jfire.base.login.JFireLogin;
import org.nightlabs.jfire.security.Authority;
import org.nightlabs.jfire.security.RoleGroup;
import org.nightlabs.jfire.security.RoleGroupRef;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.security.UserLocal;
import org.nightlabs.jfire.security.UserRef;
import org.nightlabs.jfire.security.id.AuthorityID;
import org.nightlabs.jfire.security.id.UserID;
import org.nightlabs.jfire.testsuite.JFireTestSuiteEAR;

/**
 * 
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 *
 */
public class JFireTestLogin {


	/**
	 * Log4J Logger for this class
	 */
	private static final Logger logger = Logger.getLogger(JFireTestLogin.class);
	
	public static final String PROP_TEST_USER_PREFIX = "test.user";
	public static final String USER_QUALIFIER_SERVER_ADMIN = "serverAdmin";
	public static final String PROP_TEST_ADMIN_ = PROP_TEST_USER_PREFIX + "." + USER_QUALIFIER_SERVER_ADMIN;
	
	/**
	 * Returns a {@link JFireLogin} configured to the properties given in the
	 * JFireTestSuite properties file for the given userQualifier.
	 * This means the login properties will be those defined with the prefix of "test.user.userQualifier".
	 * The organisationID in the properties though, will be set to the current organisationID.
	 * 
	 * @param userQualifier The userQualifier.
	 * @return A configured {@link JFireLogin}.
	 */
	public static JFireLogin getUserLogin(String userQualifier) throws ModuleException, IOException {
		Properties properties = JFireTestSuiteEAR.getProperties(JFireTestSuiteEAR.getJFireTestSuiteProperties(), PROP_TEST_USER_PREFIX + "." + userQualifier + ".");
		properties.setProperty(JFireLogin.PROP_ORGANISATION_ID, SecurityReflector.getUserDescriptor().getOrganisationID());
		return new JFireLogin(properties);
	}

	/**
	 * Checks if all Users referenced in the main properties file are extstent 
	 * and creates them if not.
	 * 
	 * @param pm The PersitenceManager to use.
	 * @return Whether it succeeded.
	 */
	public static boolean checkCreateLoginUsers(PersistenceManager pm) throws ModuleException, IOException {
		Properties properties = JFireTestSuiteEAR.getProperties(JFireTestSuiteEAR.getJFireTestSuiteProperties(), PROP_TEST_USER_PREFIX + ".");
		Pattern findUserPropName = Pattern.compile(Pattern.quote(PROP_TEST_USER_PREFIX) + "\\.([^.]*).*");
		Set<String> userPropNames = new HashSet<String>();
		for (Object _key : properties.keySet()) {
			String key = (String) _key;
			Matcher m = findUserPropName.matcher(key);
			if (m.matches()) {
				String userPropName = m.group(1);
				userPropNames.add(userPropName);
			}
		}
		for (String userPropName : userPropNames) {
			Properties userProps = JFireTestSuiteEAR.getProperties(properties, PROP_TEST_USER_PREFIX + "." + userPropName + ".");
			String _userID = userProps.getProperty(JFireLogin.PROP_USER_ID);
			String password = userProps.getProperty(JFireLogin.PROP_PASSWORD);
			String organisationID = SecurityReflector.getUserDescriptor().getOrganisationID();
			UserID userID = UserID.create(organisationID, _userID);
			User user = null;
			try {
				user = (User) pm.getObjectById(userID);
				user.getUserID(); // WORKAROUND
				continue;
			} catch (JDOObjectNotFoundException e) {
			}
			if(logger.isDebugEnabled())
				logger.debug("Creating User " + userID + " for testing.");
			// user not created by now; continue was not invoked.
			user = new User(userID.organisationID, userID.userID);
			UserLocal userLocal = new UserLocal(user);
			userLocal.setPasswordPlain(password);
			pm.makePersistent(user);
			if (USER_QUALIFIER_SERVER_ADMIN.equals(userPropName)) {
				Authority authority = (Authority) pm.getObjectById(AuthorityID.create(
						organisationID, Authority.AUTHORITY_ID_ORGANISATION));
				UserRef userRef = authority.createUserRef(user);
				if(logger.isDebugEnabled())
					logger.debug("Creating instances of UserRef for both Users within the default authority done.");

				// Give the user all RoleGroups.
				if(logger.isDebugEnabled())
					logger.debug("Assign all RoleGroups to the user \""+userID+"\"...");
				for (Iterator it = pm.getExtent(RoleGroup.class).iterator(); it.hasNext(); ) {
					RoleGroup roleGroup = (RoleGroup)it.next();
					RoleGroupRef roleGroupRef = authority.createRoleGroupRef(roleGroup);
					userRef.addRoleGroupRef(roleGroupRef);
				}
				if(logger.isDebugEnabled())
					logger.debug("Assigning all RoleGroups to user \""+userID+"\" done.");
			}
		}
		try {
			pm.getObjectById(UserID.create("chezfrancois.jfire.org", "francois"));
		} catch (JDOObjectNotFoundException e) {
			return false;
		}
		return true;
	}
	
}
