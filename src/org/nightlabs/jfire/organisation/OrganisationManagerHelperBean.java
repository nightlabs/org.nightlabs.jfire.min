package org.nightlabs.jfire.organisation;

import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.Locale;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.jdo.PersistenceManager;

import org.apache.log4j.Logger;
import org.nightlabs.jfire.asyncinvoke.AsyncInvokeProblem;
import org.nightlabs.jfire.base.BaseSessionBeanImpl;
import org.nightlabs.jfire.prop.Struct;
import org.nightlabs.jfire.prop.StructBlockOrderItem;
import org.nightlabs.jfire.prop.StructLocal;
import org.nightlabs.jfire.security.Authority;
import org.nightlabs.jfire.security.AuthorityType;
import org.nightlabs.jfire.security.AuthorizedObjectRef;
import org.nightlabs.jfire.security.RoleGroup;
import org.nightlabs.jfire.security.RoleGroupRef;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.security.UserLocal;
import org.nightlabs.jfire.security.id.AuthorityID;
import org.nightlabs.jfire.security.id.UserLocalID;
import org.nightlabs.jfire.server.LocalServer;
import org.nightlabs.jfire.server.Server;
import org.nightlabs.jfire.servermanager.JFireServerManager;
import org.nightlabs.jfire.servermanager.RoleImportSet;
import org.nightlabs.jfire.servermanager.config.OrganisationCf;
import org.nightlabs.jfire.servermanager.config.ServerCf;
import org.nightlabs.jfire.workstation.Workstation;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 *
 * @ejb.bean
 *		name="jfire/ejb/JFireBaseBean/OrganisationManagerHelper"
 *		jndi-name="jfire/ejb/JFireBaseBean/OrganisationManagerHelper"
 *		type="Stateless"
 *		view-type="local"
 *
 * @ejb.util generate="physical"
 * @ejb.transaction type="Required"
 **/
public abstract class OrganisationManagerHelperBean
	extends BaseSessionBeanImpl
	implements SessionBean
{
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(OrganisationManagerBean.class);

	@Override
	public void setSessionContext(SessionContext sessionContext)
			throws EJBException, RemoteException
	{
		super.setSessionContext(sessionContext);
	}

	/**
	 * @ejb.create-method
	 * @ejb.permission role-name="_Guest_"
	 */
	public void ejbCreate()
	throws CreateException
	{
	}

	/**
	 * @ejb.permission unchecked="true"
	 */
	public void ejbRemove() throws EJBException, RemoteException { }

	/**
	 * @ejb.interface-method view-type="local"
	 * @ejb.transaction type="RequiresNew"
	 * @ejb.permission role-name="_System_"
	 **/
	public void internalInitializeEmptyOrganisation_step1(
			ServerCf localServerCf,
			OrganisationCf organisationCf,
			String userID, String password
			)
	{
		String organisationID = getOrganisationID();
		PersistenceManager pm = getPersistenceManager();
		try {
			if(logger.isDebugEnabled())
				logger.debug("Initializing JDO meta-data...");

			pm.getExtent(TemporaryOrganisation.class);
			pm.getExtent(Organisation.class);
			pm.getExtent(Server.class);
			pm.getExtent(User.class);
			pm.getExtent(AsyncInvokeProblem.class);

			pm.getExtent(Struct.class);
			pm.getExtent(StructLocal.class);
			pm.getExtent(StructBlockOrderItem.class);

			if(logger.isDebugEnabled())
				logger.debug("Initializing JDO meta-data done.");

			if(logger.isDebugEnabled())
				logger.debug("Creating JDO object LocalServer...");
			Server server = localServerCf.createServer(pm);
			LocalServer localServer = new LocalServer(server);
			if (logger.isDebugEnabled()) {
				logger.debug("Persisting LocalServer: ");
				logger.debug("  serverID                 = " + localServer.getServerID());
				logger.debug("  server.serverName        = " + localServer.getServer().getServerName());
				logger.debug("  server.j2eeServerType    = " + localServer.getServer().getJ2eeServerType());
				logger.debug("  server.initialContextURL = " + localServer.getServer().getInitialContextURL());
			}
			localServer = pm.makePersistent(localServer);
			if(logger.isDebugEnabled())
				logger.debug("pm.makePersistent(localServer) done.");

			if(logger.isDebugEnabled())
				logger.debug("Creating JDO object LocalOrganisation...");
			Organisation organisation = organisationCf.createOrganisation(pm, server);
			LocalOrganisation localOrganisation = new LocalOrganisation(organisation);
			localOrganisation = pm.makePersistent(localOrganisation);
			if(logger.isDebugEnabled())
				logger.debug("pm.makePersistent(localOrganisation) done.");

			if(logger.isDebugEnabled())
				logger.debug("Creating JDO object User with ID \""+User.USER_ID_OTHER+"\"...");
			User otherUser = new User(organisationID, User.USER_ID_OTHER);
			new UserLocal(otherUser);
			otherUser = pm.makePersistent(otherUser);
			if(logger.isDebugEnabled())
				logger.debug("pm.makePersistent(otherUser) done.");

			if(logger.isDebugEnabled())
				logger.debug("Creating JDO object User with ID \""+userID+"\"...");
			User user = new User(organisationID, userID);
			UserLocal userLocal = new UserLocal(user);
			userLocal.setPasswordPlain(password);
			user = pm.makePersistent(user);
			if(logger.isDebugEnabled())
				logger.debug("pm.makePersistent(user) done.");

			if(logger.isDebugEnabled())
				logger.debug("Creating JDO object Workstation with ID \"" + Workstation.WORKSTATION_ID_FALLBACK + "\"...");
			Workstation workstation = new Workstation(organisationID, Workstation.WORKSTATION_ID_FALLBACK);
			workstation = pm.makePersistent(workstation);
			if(logger.isDebugEnabled())
				logger.debug("pm.makePersistent(workstation) done.");

			// AuthorityType.AUTHORITY_TYPE_ID_SELF
			if(logger.isDebugEnabled())
				logger.debug("Creating JDO object AuthorityType with ID \""+AuthorityType.AUTHORITY_TYPE_ID_SELF+"\"...");
			AuthorityType authorityType = new AuthorityType(AuthorityType.AUTHORITY_TYPE_ID_SELF);
			// All properties of this authority-type are now configured in the jfire-security.xml - we create it here, though, because
			// this way it's guaranteed to exist and consistent with how we handle the AuthorityType.AUTHORITY_TYPE_ID_ORGANISATION.
			authorityType = pm.makePersistent(authorityType);
			if(logger.isDebugEnabled())
				logger.debug("pm.makePersistent(authorityType) done.");


			// AuthorityType.AUTHORITY_TYPE_ID_ORGANISATION
			if(logger.isDebugEnabled())
				logger.debug("Creating JDO object AuthorityType with ID \""+AuthorityType.AUTHORITY_TYPE_ID_ORGANISATION+"\"...");
			authorityType = new AuthorityType(AuthorityType.AUTHORITY_TYPE_ID_ORGANISATION);
			// All properties of this authority-type are now configured in the jfire-security.xml - we create it here, though, because
			// this way it's guaranteed to exist and we need it to create the special Authority _Organisation_ below.
			authorityType = pm.makePersistent(authorityType);
			if(logger.isDebugEnabled())
				logger.debug("pm.makePersistent(authorityType) done.");


			// Authority.AUTHORITY_ID_ORGANISATION
			if(logger.isDebugEnabled())
				logger.debug("Creating JDO object Authority with ID \""+Authority.AUTHORITY_ID_ORGANISATION+"\"...");
			Authority authority = new Authority(organisationID, Authority.AUTHORITY_ID_ORGANISATION, authorityType);

			authority.getName().setText(Locale.ENGLISH.getLanguage(), "Organisation");
			authority.getDescription().setText(Locale.ENGLISH.getLanguage(), "This authority controls the access rights to the organisation as a whole.");

			authority.getName().setText(Locale.GERMAN.getLanguage(), "Organisation");
			authority.getDescription().setText(Locale.GERMAN.getLanguage(), "Diese Vollmacht kontrolliert den Zugriff auf die Organisation im ganzen.");

			authority = pm.makePersistent(authority);
			if(logger.isDebugEnabled())
				logger.debug("pm.makePersistent(authority) done.");

		} finally {
			pm.close();
		}
	}

	/**
	 * @ejb.interface-method view-type="local"
	 * @ejb.transaction type="RequiresNew"
	 * @ejb.permission role-name="_System_"
	 **/
	public void internalInitializeEmptyOrganisation_step2()
	{
		String organisationID = getOrganisationID();
		JFireServerManager jfsm = getJFireServerManager();
		try {
			// import all roles
			if(logger.isDebugEnabled())
				logger.debug("Importing all roles and role groups...");
			RoleImportSet roleImportSet = jfsm.roleImport_prepare(organisationID);
			jfsm.roleImport_commit(roleImportSet);
			if(logger.isDebugEnabled())
				logger.debug("Import of roles and role groups done.");
		} finally {
			jfsm.close();
		}
	}

	/**
	 * @ejb.interface-method view-type="local"
	 * @ejb.transaction type="RequiresNew"
	 * @ejb.permission role-name="_System_"
	 **/
	public void internalInitializeEmptyOrganisation_step3(
			ServerCf localServerCf,
			OrganisationCf organisationCf,
			String userID
			)
	{
		String organisationID = getOrganisationID();
		PersistenceManager pm = getPersistenceManager();
		try {
//			if(logger.isDebugEnabled())
//				logger.debug("Loading previously created AuthorityType (" + AuthorityType.AUTHORITY_TYPE_ID_SELF + ") from datastore and assigning role-groups...");
//
//			AuthorityType authorityType = (AuthorityType) pm.getObjectById(AuthorityType.AUTHORITY_TYPE_ID_SELF);
//			authorityType.addRoleGroup((RoleGroup) pm.getObjectById(RoleGroupConstants.securityManager_editAuthority));
//
//			if(logger.isDebugEnabled())
//				logger.debug("Loading previously created AuthorityType (" + AuthorityType.AUTHORITY_TYPE_ID_SELF + ") from datastore and assigning role-groups done.");


			if(logger.isDebugEnabled())
				logger.debug("Loading previously created Authority from datastore...");

			Authority authority = (Authority) pm.getObjectById(AuthorityID.create(
					organisationID, Authority.AUTHORITY_ID_ORGANISATION));

			if(logger.isDebugEnabled())
				logger.debug("Loading previously created Authority from datastore done.");


			if(logger.isDebugEnabled())
				logger.debug("Creating instances of AuthorizedObjectRef for both Users within the default authority...");

			UserLocal otherUserLocal = (UserLocal) pm.getObjectById(UserLocalID.create(organisationID, User.USER_ID_OTHER, organisationID));
			UserLocal userLocal = (UserLocal) pm.getObjectById(UserLocalID.create(organisationID, userID, organisationID));
			authority.createAuthorizedObjectRef(otherUserLocal);
			AuthorizedObjectRef userRef = authority.createAuthorizedObjectRef(userLocal);
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

			// create system user
			if(logger.isDebugEnabled())
				logger.debug("Creating system user...");
			User systemUser = new User(organisationID, User.USER_ID_SYSTEM);
			new UserLocal(systemUser);
			pm.makePersistent(systemUser);
			if(logger.isDebugEnabled())
				logger.debug("System user created.");

		} finally {
			pm.close();
		}
	}
}
