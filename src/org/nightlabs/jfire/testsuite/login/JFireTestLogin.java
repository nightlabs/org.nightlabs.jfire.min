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
import javax.naming.NamingException;

import org.apache.log4j.Logger;
import org.apache.log4j.Priority;
import org.nightlabs.ModuleException;
import org.nightlabs.j2ee.LoginData;
import org.nightlabs.jfire.base.InitException;
import org.nightlabs.jfire.base.login.JFireLogin;
import org.nightlabs.jfire.config.ConfigSetup;
import org.nightlabs.jfire.security.Authority;
import org.nightlabs.jfire.security.AuthorizedObjectRef;
import org.nightlabs.jfire.security.RoleGroup;
import org.nightlabs.jfire.security.RoleGroupRef;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.security.UserLocal;
import org.nightlabs.jfire.security.id.AuthorityID;
import org.nightlabs.jfire.security.id.UserID;
import org.nightlabs.jfire.security.listener.SecurityChangeController;
import org.nightlabs.jfire.servermanager.JFireServerManager;
import org.nightlabs.jfire.servermanager.JFireServerManagerUtil;
import org.nightlabs.jfire.testsuite.JFireTestSuiteEAR;
import org.nightlabs.jfire.workstation.Workstation;
import org.nightlabs.jfire.workstation.id.WorkstationID;

/**
 * This class is used to initialse users for the JFireTestSuite.
 *
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 */
public class JFireTestLogin
{
	private static final Logger logger = Logger.getLogger(JFireTestLogin.class);

	public static final String USER_QUALIFIER_SERVER_ADMIN = "serverAdmin";
	public static final String USER_QUALIFIER_ORGANISATION_ADMIN = "organisationAdmin";
	public static final String USER_QUALIFIER_ACCOUNTANT = "accountant";
	public static final String USER_QUALIFIER_SALESMAN = "salesman";

	public static final String PROP_TEST_USER_PREFIX = "test.user";
	public static final String PROP_TEST_ADMIN_SERVER = PROP_TEST_USER_PREFIX + "." + USER_QUALIFIER_SERVER_ADMIN;
	public static final String PROP_TEST_ORGANISATION_ADMIN = PROP_TEST_USER_PREFIX+"."+ USER_QUALIFIER_ORGANISATION_ADMIN;
	public static final String PROP_TEST_ACCOUNTANT = PROP_TEST_USER_PREFIX+"."+ USER_QUALIFIER_ACCOUNTANT;
	public static final String PROP_TEST_SALESMAN = PROP_TEST_USER_PREFIX+"."+ USER_QUALIFIER_SALESMAN;

//	/**
//	 * Returns a {@link JFireLogin} configured to the properties given in the
//	 * JFireTestSuite properties file for the given userQualifier.
//	 * This means the login properties will be those defined with the prefix of "test.user.userQualifier".
//	 * The organisationID in the properties though, will be set to the current organisationID.
//	 *
//	 * @param userQualifier The userQualifier.
//	 * @return A configured {@link JFireLogin}.
//	 */
//	public static JFireLogin getUserLogin(String userQualifier) throws ModuleException, IOException {
//		Properties properties = JFireTestSuiteEAR.getProperties(JFireTestSuiteEAR.getJFireTestSuiteProperties(), PROP_TEST_USER_PREFIX + "." + userQualifier + ".");
//		properties.setProperty(JFireLogin.PROP_ORGANISATION_ID, SecurityReflector.getUserDescriptor().getOrganisationID());
//		return new JFireLogin(properties);
//	}

	/**
	 * Returns a {@link LoginData} instance configured to the properties given in the
	 * JFireTestSuite properties file for the given userQualifier.
	 * This means the login properties will be those defined with the prefix of "test.user.userQualifier".
	 * The organisationID in the properties though, will be set to the current organisationID.
	 *
	 * @param userQualifier The userQualifier.
	 * @return A configured {@link JFireLogin}.
	 */
	public static LoginData getUserLoginData(String userQualifier) throws ModuleException, IOException {
		Properties properties = JFireTestSuiteEAR.getProperties(JFireTestSuiteEAR.getJFireTestSuiteProperties(), PROP_TEST_USER_PREFIX + "." + userQualifier + ".");
		properties.setProperty(JFireLogin.PROP_ORGANISATION_ID, SecurityReflector.getUserDescriptor().getOrganisationID());
		return new JFireLogin(properties).getLoginData();
	}

	/**
	 * Checks if all Users referenced in the main properties file are extstent
	 * and creates them if not.
	 *
	 * @param pm The PersitenceManager to use.
	 * @return Whether it succeeded.
	 * @throws NamingException
	 */
	public static boolean checkCreateLoginsAndRegisterInAuthorities(PersistenceManager pm) throws ModuleException, IOException, NamingException {
		Properties properties = JFireTestSuiteEAR.getProperties(JFireTestSuiteEAR.getJFireTestSuiteProperties(), PROP_TEST_USER_PREFIX + ".");
		Pattern findUserPropName = Pattern.compile("([^.]*).*");
		Set<String> userPropNames = new HashSet<String>();
		for (Object _key : properties.keySet()) {
			String key = (String) _key;
			Matcher m = findUserPropName.matcher(key);
			if (m.matches()) {
				String userPropName = m.group(1);
				userPropNames.add(userPropName);
			}
		}
		boolean result = true;
		result &= checkCreateLoginUsers(pm, properties, userPropNames);
		result &= checkSetRoleGroupRegistrationToAuthorities(pm, properties, userPropNames);
		return result;
	}

	private static boolean checkCreateLoginUsers(PersistenceManager pm, Properties userProperties,
			Set<String> userPropNames) throws InitException, ModuleException, NamingException
	{
		SecurityChangeController.beginChanging();
		boolean successful = false;
		try {
			for (String userPropName : userPropNames) {
				Properties userProps = JFireTestSuiteEAR.getProperties(userProperties, userPropName + ".");

				String organisationID = SecurityReflector.getUserDescriptor().getOrganisationID();

				String _userID = userProps.getProperty(JFireLogin.PROP_USER_ID);
				UserID userID = UserID.create(organisationID, _userID);

				String password = userProps.getProperty(JFireLogin.PROP_PASSWORD);

				String _workstationID = userProps.getProperty(JFireLogin.PROP_WORKSTATION_ID);
				if (_workstationID != null && _workstationID.isEmpty())
					_workstationID = null;
				WorkstationID workstationID = _workstationID == null ? null : WorkstationID.create(organisationID, _workstationID);


				User user;
				try {
					user = (User) pm.getObjectById(userID);
				} catch (JDOObjectNotFoundException e) {
					if(logger.isDebugEnabled())
						logger.debug("Creating User " + userID + " for testing.");
					// user not created by now; continue was not invoked.
					user = new User(userID.organisationID, userID.userID);
					UserLocal userLocal = new UserLocal(user);
					userLocal.setPasswordPlain(password);
					user = pm.makePersistent(user);
				}


				Workstation workstation;
				if (workstationID == null)
					workstation = null;
				else {
					try {
						workstation = (Workstation) pm.getObjectById(workstationID);
					} catch (JDOObjectNotFoundException e) {
						workstation = new Workstation(workstationID.organisationID, workstationID.workstationID);
						workstation.setDescription("Test workstation created by JFire's testsuite (workstationID='" + workstationID.workstationID + "')");
						workstation = pm.makePersistent(workstation);
					}
				}


				if (USER_QUALIFIER_SERVER_ADMIN.equals(userPropName)) {
					{
						JFireServerManager jFireServerManager = JFireServerManagerUtil.getJFireServerManager();
						try {
							jFireServerManager.addServerAdmin(userID.organisationID, userID.userID);
						} finally {
							jFireServerManager.close();
						}
					}

					Authority authority = (Authority) pm.getObjectById(
							AuthorityID.create(organisationID, Authority.AUTHORITY_ID_ORGANISATION)
					);
					AuthorizedObjectRef userRef = authority.createAuthorizedObjectRef(user.getUserLocal());
					if(logger.isDebugEnabled())
						logger.debug("Creating instances of AuthorizedObjectRef for both Users within the default authority done.");

					// Give the user all RoleGroups.
					if(logger.isDebugEnabled())
						logger.debug("Assign all RoleGroups to the user \""+userID+"\"...");
					for (Iterator<RoleGroup> it = pm.getExtent(RoleGroup.class).iterator(); it.hasNext(); ) {
						RoleGroup roleGroup = it.next();
						RoleGroupRef roleGroupRef = authority.createRoleGroupRef(roleGroup);
						userRef.addRoleGroupRef(roleGroupRef);
					}
					if(logger.isDebugEnabled())
						logger.debug("Assigning all RoleGroups to user \""+userID+"\" done.");
				}
			}
			ConfigSetup.ensureAllPrerequisites(pm);

			// check if they are correctly stored
			if (userPropNames.isEmpty()) {
				if (logger.isEnabledFor(Priority.WARN))
					logger.warn("No declared users found!");

				return true;
			}

			for (String userPropName : userPropNames) {
				Properties userProps = JFireTestSuiteEAR.getProperties(userProperties, userPropName + ".");
				String _userID = userProps.getProperty(JFireLogin.PROP_USER_ID);
				String organisationID = SecurityReflector.getUserDescriptor().getOrganisationID();
				UserID userID = UserID.create(organisationID, _userID);
				try {
					pm.getObjectById(userID);
				}	catch (JDOObjectNotFoundException e) {
					return false;
				}
			}

			successful = true;
		} finally {
			SecurityChangeController.endChanging(successful);
		}
		return true;
	}

	private static boolean checkSetRoleGroupRegistrationToAuthorities(PersistenceManager pm,
			Properties userProperties, Set<String> userPropNames)
	{
		return true; // TODO implement this method!
	}
}
