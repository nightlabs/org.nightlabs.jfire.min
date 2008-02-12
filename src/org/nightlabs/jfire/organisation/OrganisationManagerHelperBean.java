package org.nightlabs.jfire.organisation;

import java.rmi.RemoteException;
import java.util.Iterator;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.jdo.PersistenceManager;

import org.apache.log4j.Logger;
import org.nightlabs.jfire.base.BaseSessionBeanImpl;
import org.nightlabs.jfire.security.Authority;
import org.nightlabs.jfire.security.AuthorityType;
import org.nightlabs.jfire.security.RoleGroup;
import org.nightlabs.jfire.security.RoleGroupRef;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.security.UserLocal;
import org.nightlabs.jfire.security.UserRef;
import org.nightlabs.jfire.security.id.AuthorityID;
import org.nightlabs.jfire.security.id.UserID;
import org.nightlabs.jfire.server.LocalServer;
import org.nightlabs.jfire.server.Server;
import org.nightlabs.jfire.servermanager.JFireServerManager;
import org.nightlabs.jfire.servermanager.RoleImportSet;
import org.nightlabs.jfire.servermanager.config.OrganisationCf;
import org.nightlabs.jfire.servermanager.config.ServerCf;

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
	/**
	 * 
	 */
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
				logger.debug("Creating JDO object LocalServer...");
			Server server = localServerCf.createServer(pm);
			LocalServer localServer = new LocalServer(server);
			pm.makePersistent(localServer);
			if(logger.isDebugEnabled())
				logger.debug("pm.makePersistent(localServer) done.");

			if(logger.isDebugEnabled())
				logger.debug("Creating JDO object LocalOrganisation...");
			Organisation organisation = organisationCf.createOrganisation(pm, server);
			LocalOrganisation localOrganisation = new LocalOrganisation(organisation);
			pm.makePersistent(localOrganisation);
			if(logger.isDebugEnabled())
				logger.debug("pm.makePersistent(localOrganisation) done.");

			if(logger.isDebugEnabled())
				logger.debug("Creating JDO object User with ID \""+User.USERID_OTHER+"\"...");
			User otherUser = new User(organisationID, User.USERID_OTHER);
			new UserLocal(otherUser);
			pm.makePersistent(otherUser);
			if(logger.isDebugEnabled())
				logger.debug("pm.makePersistent(otherUser) done.");

			if(logger.isDebugEnabled())
				logger.debug("Creating JDO object User with ID \""+userID+"\"...");
			User user = new User(organisationID, userID);
			UserLocal userLocal = new UserLocal(user);
			userLocal.setPasswordPlain(password);
			pm.makePersistent(user);
			if(logger.isDebugEnabled())
				logger.debug("pm.makePersistent(user) done.");

			if(logger.isDebugEnabled())
				logger.debug("Creating JDO object AuthorityType with ID \""+AuthorityType.AUTHORITY_TYPE_ID_SYSTEM+"\"...");
			AuthorityType authorityType = new AuthorityType(organisationID, AuthorityType.AUTHORITY_TYPE_ID_SYSTEM);
			pm.makePersistent(authorityType);
			if(logger.isDebugEnabled())
				logger.debug("pm.makePersistent(authorityType) done.");

			if(logger.isDebugEnabled())
				logger.debug("Creating JDO object Authority with ID \""+Authority.AUTHORITY_ID_ORGANISATION+"\"...");
			Authority authority = new Authority(organisationID, Authority.AUTHORITY_ID_ORGANISATION, authorityType);
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
//			RoleImportSet roleImportSet = roleImport_prepare(organisationID);
			RoleImportSet roleImportSet = jfsm.roleImport_prepare(organisationID);
//			roleImport_commit(roleImportSet, pm);
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
			if(logger.isDebugEnabled())
				logger.debug("Loading previously created Authority from datastore...");

			Authority authority = (Authority) pm.getObjectById(AuthorityID.create(
					organisationID, Authority.AUTHORITY_ID_ORGANISATION));

			if(logger.isDebugEnabled())
				logger.debug("Loading previously created Authority from datastore...");



			if(logger.isDebugEnabled())
				logger.debug("Creating instances of UserRef for both Users within the default authority...");

			User otherUser = (User) pm.getObjectById(UserID.create(organisationID, User.USERID_OTHER));
			User user = (User) pm.getObjectById(UserID.create(organisationID, userID));
			authority.createUserRef(otherUser);
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

			// create system user
			if(logger.isDebugEnabled())
				logger.debug("Creating system user...");
			User systemUser = new User(organisationID, User.USERID_SYSTEM);
			new UserLocal(systemUser);
			pm.makePersistent(systemUser);
			if(logger.isDebugEnabled())
				logger.debug("System user created.");

		} finally {
			pm.close();
		}
	}
}
