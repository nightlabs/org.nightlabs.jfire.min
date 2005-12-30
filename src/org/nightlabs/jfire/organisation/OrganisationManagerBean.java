/*
 * Created on 23.02.2004
 */
package org.nightlabs.jfire.organisation;

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.jdo.FetchPlan;
import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.naming.InitialContext;

import org.apache.log4j.Logger;
import org.nightlabs.jfire.base.BaseSessionBeanImpl;
import org.nightlabs.jfire.organisation.LocalOrganisation;
import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.jfire.organisation.RegistrationStatus;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.security.UserLocal;
import org.nightlabs.jfire.security.id.UserID;
import org.nightlabs.jfire.server.id.ServerID;
import org.nightlabs.jfire.servermanager.JFireServerManager;
import org.nightlabs.jfire.servermanager.config.OrganisationCf;
import org.nightlabs.jfire.servermanager.config.RootOrganisationCf;
import org.nightlabs.jfire.servermanager.config.ServerCf;

import org.nightlabs.ModuleException;
import org.nightlabs.jfire.organisation.OrganisationManager;
import org.nightlabs.jfire.organisation.OrganisationManagerUtil;
import org.nightlabs.jfire.security.UserManager;
import org.nightlabs.jfire.security.UserManagerHome;
import org.nightlabs.jfire.security.UserManagerUtil;

/**
 * @author Niklas Schiffler - nick at nightlabs dot de
 * @author Marco Schulze - marco at nightlabs dot de
 *
 * @ejb.bean
 *		name="jfire/ejb/JFireBaseBean/OrganisationManager"
 *		jndi-name="jfire/ejb/JFireBaseBean/OrganisationManager"
 *		type="Stateless"
 *
 * @ejb.util generate="physical"
 **/
public abstract class OrganisationManagerBean
	extends BaseSessionBeanImpl
	implements SessionBean
{
	public static final Logger LOGGER = Logger.getLogger(OrganisationManagerBean.class);
	
	/**
	 * @see org.nightlabs.jfire.base.BaseSessionBeanImpl#setSessionContext(javax.ejb.SessionContext)
	 */
	public void setSessionContext(SessionContext sessionContext)
			throws EJBException, RemoteException
	{
		System.out.println(this.getClass().getName() + ".setSessionContext("+sessionContext+")");
		super.setSessionContext(sessionContext);
	}
	/**
	 * _Guest_ needs to be able to instantiate this EJB, because of the orga registration.
	 *
	 * @ejb.create-method
	 * @ejb.permission role-name="_Guest_"
	 * @!!!ejb.permission role-name="_ServerAdmin_, OrganisationManager-read"
	 */
	public void ejbCreate()
	throws CreateException
	{
		System.out.println(this.getClass().getName() + ".ejbCreate()");
//		try
//		{
//			System.out.println("OrganisationManager created by " + this.getPrincipalString());
//		}
//		catch (ModuleException e)
//		{
//			throw new CreateException(e.getMessage());
//		}
	}
	/**
	 * @see javax.ejb.SessionBean#ejbRemove()
	 * 
	 * @ejb.permission unchecked="true"
	 */
	public void ejbRemove() throws EJBException, RemoteException { }

//	/**
//	 * This method creates a representative organisation on this server. Note,
//	 * that it cannot be converted into a real organisation afterwards. This
//	 * representative cannot start operation before its masterOrganisation has
//	 * accepted it.
//	 *
//	 * @param organisationID The organisationID of the representative. Must be new and unique in the whole network (means world!).
//	 * @param organisationDisplayName A nice name that will be used to display the new representative organisation. 
//	 * @param masterOrganisationID The organisationID of the master organisation, this new slave is representing.
//	 * @throws ModuleException
//	 *
//	 * @ejb.interface-method
//	 * @!ejb.transaction type = "Required"
//	 * @ejb.transaction type = "Never"
//	 * @ejb.permission role-name="_ServerAdmin_"
//	 */
//	public void createOrganisation(String organisationID, String organisationDisplayName, String masterOrganisationID)
//		throws ModuleException
//	{
//		JFireServerManager ism = getJFireServerManager();
//		try {
//			ism.createOrganisation(organisationID, organisationDisplayName);
//		} finally {
//			ism.close();
//		}
//	}

	/**
	 * This method creates an organisation on this server.
	 *
	 * @param organisationID The ID of the new organisation. It must be unique in the whole world.
	 * @param organisationDisplayName A nice name that will be used to display the new representative organisation.
	 * @param userID The userID of the first user to create within the new organisation. It will have all necessary permissions to manage users and roles within the new organisation. 
	 * @param password The password of the new user.
	 * @param isServerAdmin Whether this user should have global server administration permissions.
	 * @throws ModuleException
	 *
	 * @ejb.interface-method
	 * @!ejb.transaction type = "Required"
	 * @ejb.transaction type = "Never"
	 * @ejb.permission role-name="_ServerAdmin_"
	 */
	public void createOrganisation(String organisationID, String organisationDisplayName, String userID, String password, boolean isServerAdmin)
		throws ModuleException
	{
		JFireServerManager ism = getJFireServerManager();
		try {
			ism.createOrganisation(organisationID, organisationDisplayName, userID, password, isServerAdmin);
		} finally {
			ism.close();
		}
	}


	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_ServerAdmin_"
	 **/
	public List getOrganisationCfs(boolean sorted)
		throws ModuleException
	{
		JFireServerManager ism = getJFireServerManager();
		try {
			return ism.getOrganisationCfs(sorted);
		} finally {
			ism.close();
		}
	}

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_ServerAdmin_"
	 **/
	public OrganisationCf getOrganisationConfig(String organisationID)
		throws ModuleException
	{
		JFireServerManager ism = getJFireServerManager();
		try {
			return ism.getOrganisationConfig(organisationID);
		} finally {
			ism.close();
		}
	}

	/**
	 * This method finds out whether the current 
	 * 
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 *
	 * @see org.nightlabs.jfire.servermanager.JFireServerManagerFactory#isOrganisationCfsEmpty()
	 */
	public boolean isOrganisationCfsEmpty()
		throws ModuleException
	{
		JFireServerManager ism = getJFireServerManager();
		try {
			return ism.isOrganisationCfsEmpty();
		} finally {
			ism.close();
		}
	}

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="OrganisationManager-read"
	 **/
	public Collection getPendingRegistrations(String[] fetchGroups)
		throws ModuleException
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			if (fetchGroups != null) {
				FetchPlan fetchPlan = pm.getFetchPlan();
				for (int i = 0; i < fetchGroups.length; ++i)
					fetchPlan.addGroup(fetchGroups[i]);
			}

			return pm.detachCopyAll(
					LocalOrganisation.getLocalOrganisation(pm).getPendingRegistrations());
		} finally {
			pm.close();
		}
	}

	/**
	 * This method is called by the other organisation to inform this
	 * one that the registration has been accepted.
	 * <br/><br/>
	 * Though, there
	 * is no j2ee security, it is quite hard to guess the correct
	 * registrationID. Additionally, the organisation is read out
	 * of the JFirePrincipal and must match the other organisationID.
	 *
	 * @ejb.interface-method
	 * @ejb.transaction type = "Required"
	 * @ejb.permission role-name="_Guest_"
	 **/
	public void notifyAcceptRegistration(String registrationID, Organisation grantOrganisation, String userPassword)
		throws ModuleException
	{
		if (registrationID == null)
			throw new NullPointerException("registrationID");

		if (userPassword == null)
			throw new NullPointerException("userPassword");

		String userID = getPrincipal().getUserID();
		if (!userID.startsWith(User.USERID_PREFIX_TYPE_ORGANISATION))
			throw new IllegalStateException("This method can only be executed by an organisation!");

		String grantOrganisationID = userID.substring(User.USERID_PREFIX_TYPE_ORGANISATION.length());

		PersistenceManager pm = getPersistenceManager();
		try {
			LocalOrganisation localOrganisation = LocalOrganisation.getLocalOrganisation(pm);
			RegistrationStatus registrationStatus = localOrganisation.getPendingRegistration(grantOrganisationID);
			if (registrationStatus == null)
				throw new IllegalStateException("There is no pending registration for applicantOrganisationID=\""+getOrganisationID()+"\" and grantOrganisationID=\""+grantOrganisationID+"\"!");

			if (!registrationID.equals(registrationStatus.getRegistrationID()))
				throw new IllegalArgumentException("The given registrationID \""+registrationID+"\" does not match the pending registration for applicantOrganisationID=\""+getOrganisationID()+"\" and grantOrganisationID=\""+grantOrganisationID+"\"!");

			localOrganisation.setPassword(grantOrganisationID, userPassword);
			registrationStatus.accept(User.getUser(pm, getPrincipal()));

//			// It seems, there is a bug in JPOX (foreign key constraint error) and therefore,
//			// we need to persist the server manually...
//			try {
//				pm.getObjectById(ServerID.create(grantOrganisation.getServer().getServerID()));
//			} catch (JDOObjectNotFoundException x) {
//				pm.makePersistent(grantOrganisation.getServer());
//			}
//			pm.makePersistent(grantOrganisation);
		} finally {
			pm.close();
		}
	}
	
	/**
	 * @ejb.interface-method
	 * @ejb.transaction type = "Required"
	 * @ejb.permission role-name="_Guest_"
	 **/
	public void notifyRejectRegistration(String registrationID)
		throws ModuleException
	{
		if (registrationID == null)
			throw new NullPointerException("registrationID");

		String userID = getPrincipal().getUserID();
		if (!userID.startsWith(User.USERID_PREFIX_TYPE_ORGANISATION))
			throw new IllegalStateException("This method can only be executed by an organisation!");

		String grantOrganisationID = userID.substring(User.USERID_PREFIX_TYPE_ORGANISATION.length());

		PersistenceManager pm = getPersistenceManager();
		try {
			LocalOrganisation localOrganisation = LocalOrganisation.getLocalOrganisation(pm);
			RegistrationStatus registrationStatus = localOrganisation.getPendingRegistration(grantOrganisationID);
			if (registrationStatus == null)
				throw new IllegalStateException("There is no pending registration for applicantOrganisationID=\""+getOrganisationID()+"\" and grantOrganisationID=\""+grantOrganisationID+"\"!");

			if (!registrationID.equals(registrationStatus.getRegistrationID()))
				throw new IllegalArgumentException("The given registrationID \""+registrationID+"\" does not match the pending registration for applicantOrganisationID=\""+getOrganisationID()+"\" and grantOrganisationID=\""+grantOrganisationID+"\"!");

			User user = User.getUser(pm, getOrganisationID(), userID);
			// user.setPassword(null); // login is impossible with a null password
			pm.deletePersistent(user);

			registrationStatus.reject(null);
			// TODO we must remove the pending registration after the user has seen, that
			// it was rejected.
//			localOrganisation.removePendingRegistration(grantOrganisationID);
		} finally {
			pm.close();
		}		
	}

	/**
	 * This method is called by the client (a user who has organisation administrative
	 * rights) to accept a registration. The registration must have been previously
	 * applied for by the other organisation.
	 *
	 * @ejb.interface-method
	 * @ejb.transaction type = "Required"
	 * @ejb.permission role-name="OrganisationManager-write"
	 **/
	public void acceptRegistration(String applicantOrganisationID)
		throws ModuleException
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			LocalOrganisation localOrganisation = LocalOrganisation.getLocalOrganisation(pm);
			
			RegistrationStatus registrationStatus = localOrganisation
					.getPendingRegistration(applicantOrganisationID);

			if (registrationStatus == null)
				throw new IllegalArgumentException("There is no pending registration for applicantOrganisation \""+applicantOrganisationID+"\" at grantOrganisation \""+getOrganisationID()+"\"!");

			// We have to create a user for the new organisation. Therefore,
			// generate a password with a random length between 8 and 16 characters.
			String usrPassword = UserLocal.generatePassword(8, 16);

			try {
				// Create the user if it doesn't yet exist
				String userID = User.USERID_PREFIX_TYPE_ORGANISATION + applicantOrganisationID;				
				try {
					User user = User.getUser(pm, getOrganisationID(), userID);
					user.getUserLocal().setPasswordPlain(usrPassword); // set the new password, if the user already exists
				} catch (JDOObjectNotFoundException x) {
					// Create the user
					User user = new User(getOrganisationID(), userID);
					UserLocal userLocal = new UserLocal(user);
					userLocal.setPasswordPlain(usrPassword);
					pm.makePersistent(user);
				}

				pm.getFetchPlan().addGroup(FetchPlan.ALL); // TODO fetch-groups?!
				Organisation grantOrganisation = (Organisation) pm.detachCopy(
						localOrganisation.getOrganisation());

//				// WORKAROUND Because of a JPOX bug, we need to do this here (it's not nice, though it should be ok, because the transaction should be rolled back if an error occurs)
//				// We close the RegistrationStatus by accepting
//				// and remove it from the pending ones.
//				registrationStatus.accept(User.getUser(pm, getPrincipal()));
//				localOrganisation.removePendingRegistration(applicantOrganisationID);

				// Now, we notify the other organisation that its request has been
				// accepted.
				OrganisationManager organisationManager = OrganisationManagerUtil.getHome(getInitialContextProps(applicantOrganisationID)).create();
				organisationManager.notifyAcceptRegistration(
						registrationStatus.getRegistrationID(), grantOrganisation, usrPassword);
			} catch (RuntimeException x) {
				throw x;
			} catch (ModuleException x) {
				throw x;
			} catch (Exception x) {
				throw new ModuleException(x);
			}

			// WORKAROUND Because of a JPOX bug, we need to do this above.
			// We close the RegistrationStatus by accepting
			// and remove it from the pending ones.
			registrationStatus.accept(User.getUser(pm, getPrincipal()));
			localOrganisation.removePendingRegistration(applicantOrganisationID);

		} finally {
			pm.close();
		}
	}

	/**
	 * @ejb.interface-method
	 * @ejb.transaction type = "Required"
	 * @ejb.permission role-name="OrganisationManager-write"
	 **/
	public void rejectRegistration(String applicantOrganisationID)
		throws ModuleException
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			LocalOrganisation localOrganisation = LocalOrganisation.getLocalOrganisation(pm);
			
			RegistrationStatus registrationStatus = localOrganisation
					.getPendingRegistration(applicantOrganisationID);

			if (registrationStatus == null)
				throw new IllegalArgumentException("There is no pending registration for applicantOrganisation \""+applicantOrganisationID+"\" at grantOrganisation \""+getOrganisationID()+"\"!");

			try {
				// Now, we notify the other organisation that its request has been
				// rejected.
				OrganisationManager organisationManager = OrganisationManagerUtil.getHome(getInitialContextProps(applicantOrganisationID)).create();
				organisationManager.notifyRejectRegistration(registrationStatus.getRegistrationID());
				// Because notifyRejectRegistration drops our user remotely, we cannot execute
				// any command there anymore - not even remove the bean.
				// organisationManager.remove();
			} catch (RuntimeException x) {
				throw x;
			} catch (ModuleException x) {
				throw x;
			} catch (Exception x) {
				throw new ModuleException(x);
			}

			// We close the RegistrationStatus by rejecting
			// and remove it from the pending ones.
			registrationStatus.reject(User.getUser(pm, getPrincipal()));
			localOrganisation.removePendingRegistration(applicantOrganisationID);
		} finally {
			pm.close();
		}
	}

	/**
	 * @ejb.interface-method
	 * @ejb.transaction type = "Required"
	 * @ejb.permission role-name="OrganisationManager-write"
	 **/
	public void cancelRegistration(String grantOrganisationID)
		throws ModuleException
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			LocalOrganisation localOrganisation = LocalOrganisation.getLocalOrganisation(pm);
			RegistrationStatus registrationStatus = localOrganisation.getPendingRegistration(grantOrganisationID);
			if (registrationStatus == null)
				throw new IllegalArgumentException("There is no pending registration for grantOrganisation \""+grantOrganisationID+"\" at applicantOrganisation \""+getOrganisationID()+"\"!");

			try {
				// Create the initial context properties to connect to the remote server.
				Properties props = new Properties();
				props.put(InitialContext.INITIAL_CONTEXT_FACTORY, registrationStatus.getInitialContextFactory());
				props.put(InitialContext.PROVIDER_URL, registrationStatus.getInitialContextURL());

				// Obtain the OrganisationLinker EJB and request registration
				OrganisationLinker organisationLinker = OrganisationLinkerUtil.getHome(props).create();
				organisationLinker.cancelRegistration(
						registrationStatus.getRegistrationID(),
						getOrganisationID(), grantOrganisationID);
				organisationLinker.remove();
			} catch (ModuleException e) {
				throw e;
			} catch (Exception e) {
				throw new ModuleException(e);
			}

			// Delete the user we previously created.
			User user = User.getUser(
					pm, getOrganisationID(),
					User.USERID_PREFIX_TYPE_ORGANISATION + grantOrganisationID);
			pm.deletePersistent(user);

			// We close the RegistrationStatus by cancelling
			// and remove it from the pending ones.
			registrationStatus.cancel(User.getUser(pm, getPrincipal()));
			localOrganisation.removePendingRegistration(grantOrganisationID);
		} finally {
			pm.close();
		}
	}

	/**
	 * This method is called by a client. It is the first step to make two
	 * organisations know each other. It creates a user for the new organisation
	 * and requests to be registered at the other organisation.
	 *
	 * @ejb.interface-method
	 * @ejb.transaction type = "Required"
	 * @ejb.permission role-name="OrganisationManager-write"
	 **/
	public void beginRegistration(
			String initialContextFactory, String initialContextURL, String organisationID)
		throws ModuleException
	{
		String registrationID = getOrganisationID()
				+ '-' + Long.toHexString(System.currentTimeMillis())
				+ '-' + Integer.toHexString((int)Math.round(Integer.MAX_VALUE * Math.random()));

		// fetch and detach our local organisation (containing the local server)
		Organisation myOrganisation;
		PersistenceManager pm = getPersistenceManager();
		try {
			LocalOrganisation localOrganisation = LocalOrganisation.getLocalOrganisation(pm);

			// We need to find out, whether the applicant organisation has already
			// been successfully registered (status accepted).
			// If there is currently a pending registration, it will be cancelled.
			RegistrationStatus.ensureRegisterability(
					pm, localOrganisation, organisationID);

			pm.getFetchPlan().addGroup(FetchPlan.ALL); // TODO fetch-groups?!
			myOrganisation = (Organisation) pm.detachCopy(localOrganisation.getOrganisation());

			RegistrationStatus registrationStatus = new RegistrationStatus(
					registrationID,
					organisationID, User.getUser(pm, getPrincipal()),
					initialContextFactory, initialContextURL);

			localOrganisation.addPendingRegistration(registrationStatus);
			
			// We have to create a user for the new organisation. Therefore,
			// generate a password with a random length between 8 and 16 characters.
			String usrPassword = UserLocal.generatePassword(8, 16);

			try {
				// Create the user if it doesn't yet exist
				String userID = User.USERID_PREFIX_TYPE_ORGANISATION + organisationID;
				try {
					User user = User.getUser(pm, getOrganisationID(), userID);
					user.getUserLocal().setPasswordPlain(usrPassword); // set the new password, if the user already exists
				} catch (JDOObjectNotFoundException x) {
					// Create the user
					UserManager userManager = UserManagerUtil.getHome().create();
					User user = new User(getOrganisationID(), userID);
					UserLocal userLocal = new UserLocal(user);
					userLocal.setPasswordPlain(usrPassword);
					pm.makePersistent(user);
//					userManager.saveUser(user);
//					userManager.remove();
				}

				// Create the initial context properties to connect to the remote server.
				Properties props = new Properties();
				props.put(InitialContext.INITIAL_CONTEXT_FACTORY, initialContextFactory);
				props.put(InitialContext.PROVIDER_URL, initialContextURL);

				// Obtain the OrganisationLinker EJB and request registration
				OrganisationLinker organisationLinker = OrganisationLinkerUtil.getHome(props).create();
				organisationLinker.requestRegistration(registrationID, myOrganisation, organisationID, usrPassword);
				organisationLinker.remove();

			} catch (ModuleException e) {
				throw e;
			} catch (Exception e) {
				throw new ModuleException(e);
			}
		} finally {
			pm.close();
		}
	}
	
	/**
	 * This method is called by a client. After we began a registration and
	 * the partner has either rejected or accepted, we still have the
	 * RegistrationStatus in the list of pending registrations. This
	 * is for the user to recognize that the other organisation has reacted
	 * and what this reaction was (accept or reject).
	 *
	 * @ejb.interface-method
	 * @ejb.transaction type = "Required"
	 * @ejb.permission role-name="OrganisationManager-write"
	 **/
	public void ackRegistration(
			String grantOrganisationID)
		throws ModuleException
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			LocalOrganisation localOrganisation = LocalOrganisation.getLocalOrganisation(pm);
			RegistrationStatus registrationStatus = localOrganisation.getPendingRegistration(grantOrganisationID);
			if (registrationStatus == null)
				throw new IllegalArgumentException("There is no pending registration for grantOrganisation \""+grantOrganisationID+"\" at applicantOrganisation \""+getOrganisationID()+"\"!");

			if (registrationStatus.getCloseDT() == null)
				throw new IllegalArgumentException("The registration for grantOrganisation \""+grantOrganisationID+"\" at applicantOrganisation \""+getOrganisationID()+"\" is still open and has neither been rejected nor accepted!");

			localOrganisation.removePendingRegistration(grantOrganisationID);
		} finally {
			pm.close();
		}
	}
	
	/**
	 * @ejb.interface-method
	 * @ejb.transaction type = "Required"
	 * @ejb.permission role-name="_Guest_"
	 **/
	public void testBackhand(String[] organisationIDs)
		throws ModuleException
	{
		LOGGER.info("testBackhand ("+organisationIDs.length+"): begin: principal="+getPrincipalString());
		if (organisationIDs != null && organisationIDs.length > 0) {
			String organisationID = organisationIDs[0];
			String[] bhOrgaIDs = new String[organisationIDs.length - 1];
			for (int i = 1; i < organisationIDs.length; ++i) {
				bhOrgaIDs[i-1] = organisationIDs[i];
			}

//			String username;
//			String password;
//			PersistenceManager pm = getPersistenceManager();
//			try {
//				LocalOrganisation localOrganisation = LocalOrganisation.getLocalOrganisation(pm);
//	
//				password = localOrganisation.getPassword(organisationID);
//	
//				username = User.USERID_PREFIX_TYPE_ORGANISATION
//						+ localOrganisation.getOrganisationID()
//						+ '@'
//						+ organisationID;
//			} finally {
//				pm.close();
//			}

			LOGGER.info("testBackhand ("+organisationIDs.length+"): backhanding to organisation \""+organisationID+"\"");
			try {
				LOGGER.info("testBackhand ("+organisationIDs.length+"): OrganisationManagerUtil.getHome(...)");
				OrganisationManager organisationManager = OrganisationManagerUtil.getHome(getInitialContextProps(organisationID)).create();
				
				LOGGER.info("testBackhand ("+organisationIDs.length+"): UserManagerUtil.getHome()");
				UserManagerHome userManagerHome = UserManagerUtil.getHome();
				
				LOGGER.info("testBackhand ("+organisationIDs.length+"): userManagerHome.create()");
				UserManager userManager = userManagerHome.create();

				LOGGER.info("testBackhand ("+organisationIDs.length+"): organisationManager.testBackhand(...)");
				organisationManager.testBackhand(bhOrgaIDs);

				LOGGER.info("testBackhand ("+organisationIDs.length+"): userManager.whoami()");
				userManager.whoami();

				organisationManager.remove();
				userManager.remove();
			} catch (RuntimeException e) {
				throw e;
			} catch (ModuleException e) {
				throw e;
			} catch (Exception e) {
				throw new ModuleException(e);			
			}
		}
		LOGGER.info("testBackhand ("+organisationIDs.length+"): end: principal="+getPrincipalString());
	}

	/**
	 * This method is called via datastoreinit.xml and registers this organisation in the
	 * network's root-organisation, if it is not yet registered (or rejected). If the registration
	 * has been rejected, it does not try again.
	 * <p>
	 * This method calls {@link #registerInRootOrganisation(boolean)} with <code>force = false</code>.
	 * </p>
	 * <p>
	 * Note, that this method does nothing, if the local organisation IS the root-organisation.
	 * </p>
	 *
	 * @ejb.interface-method
	 * @ejb.transaction type = "Required"
	 * @ejb.permission role-name="OrganisationManager-write"
	 **/
	public void registerInRootOrganisation()
	throws ModuleException
	{
		registerInRootOrganisation(false);
	}

	/**
	 * If this organisation is not the root-organisation, it delegates this query
	 * to the root-organisation. If it is the root-organisation, it returns all
	 * {@link Organisation}s that are known to the root-organisation and that
	 * are allowed to be seen by the querying organisation. If it is not an
	 * organisation which queries, but a local user, this method returns all.
	 * <p>
	 * TODO Currently, all known organisations are returned. They must be filtered! An organisation should only see a configurable subset. Maybe this should be configured by groups (and not individually).
	 * </p>
	 *
	 * @param filterPartnerOrganisations If <tt>true</tt> and the local organisation is not the
	 *		root-organisation, the local organisation filters out all organisations that it already
	 *		has a cooperation with (means a user for the other organisation exists). If this is the
	 *		root-organisation, this param is ignored.
	 * @param fetchGroups Either <tt>null</tt> or the fetchGroups to use for detaching.
	 * @return Returns instances of {@link Organisation}.
	 *
	 * @ejb.interface-method
	 * @ejb.transaction type = "Required"
	 * @ejb.permission role-name="_Guest_"
	 **/
	public Collection getOrganisationsFromRootOrganisation(boolean filterPartnerOrganisations, String[] fetchGroups)
	throws ModuleException
	{
		try {
			InitialContext ctx = new InitialContext();
			try {
				String rootOrganisationID = Organisation.getRootOrganisationID(ctx);
				String localOrganisationID = getOrganisationID();
				if (!rootOrganisationID.equals(localOrganisationID)) {
					OrganisationManager organisationManager = OrganisationManagerUtil.getHome(getInitialContextProps(rootOrganisationID)).create();
					Collection res = organisationManager.getOrganisationsFromRootOrganisation(filterPartnerOrganisations, fetchGroups);

					if (!filterPartnerOrganisations)
						return res;

					Collection newRes = new LinkedList();

					PersistenceManager pm = getPersistenceManager();
					try {
						pm.getExtent(User.class);

						for (Iterator it = res.iterator(); it.hasNext(); ) {
							Organisation orga = (Organisation) it.next();
							String userID = User.USERID_PREFIX_TYPE_ORGANISATION + orga.getOrganisationID();
							try {
								pm.getObjectById(UserID.create(localOrganisationID, userID));
							} catch (JDOObjectNotFoundException x) {
								newRes.add(orga); // add only if no user existent yet
							}
						} // for (Iterator it = res.iterator(); it.hasNext(); ) {
					} finally {
						pm.close();
					}

					return newRes;
				}
			} finally {
				ctx.close();
			}

			PersistenceManager pm = getPersistenceManager();
			try {
				if (fetchGroups != null)
					pm.getFetchPlan().setGroups(fetchGroups);

				return pm.detachCopyAll((Collection)pm.newQuery(Organisation.class).execute());
			} finally {
				pm.close();
			}

		} catch (ModuleException x) {
			throw x;
		} catch (Exception x) {
			throw new ModuleException(x);
		}
	}

	/**
	 * This method registers this organisation in the
	 * network's root-organisation, if it is not yet registered (or rejected). If the registration
	 * has been rejected, it does not try again if you do not pass <code>force = true</code>.
	 * <p>
	 * Note, that this method does nothing, if the local organisation IS the root-organisation.
	 * </p> 
	 *
	 * @ejb.interface-method
	 * @ejb.transaction type = "Required"
	 * @ejb.permission role-name="OrganisationManager-write"
	 **/
	public void registerInRootOrganisation(boolean force)
	throws ModuleException
	{
		JFireServerManager ism = getJFireServerManager();
		try {
			RootOrganisationCf rootOrganisationCf = ism.getJFireServerConfigModule().getRootOrganisation();
			ServerCf rootServer = rootOrganisationCf.getServer();
			String localOrganisationID = getOrganisationID();
			if (localOrganisationID.equals(rootOrganisationCf.getOrganisationID())) {
				// This IS the root-organisation. Only check the correctness of the server.
				if (LOGGER.isDebugEnabled())
					LOGGER.debug("Organisation \"" + localOrganisationID + "\" is the root-organisation. Will not perform registration.");

				ServerCf localServer = ism.getJFireServerConfigModule().getLocalServer();

				if (!localServer.getServerID().equals(rootServer.getServerID()))
					LOGGER.error("localOrganisation.server.serverID == \"" + localServer.getServerID() + "\" != rootOrganisation.server.serverID == \"" + rootServer.getServerID() + "\"");

				if (!localServer.getServerName().equals(rootServer.getServerID()))
					LOGGER.error("localOrganisation.server.serverName == \"" + localServer.getServerName() + "\" != rootOrganisation.server.serverName == \"" + rootServer.getServerName() + "\"");

				if (!localServer.getJ2eeServerType().equals(rootServer.getJ2eeServerType()))
					LOGGER.error("localOrganisation.server.j2eeServerType == \"" + localServer.getJ2eeServerType() + "\" != rootOrganisation.server.j2eeServerType == \"" + rootServer.getJ2eeServerType() + "\"");

				if (!localServer.getInitialContextURL().equals(rootServer.getInitialContextURL()))
					LOGGER.error("localOrganisation.server.initialContextURL == \"" + localServer.getInitialContextURL() + "\" != rootOrganisation.server.initialContextURL == \"" + rootServer.getInitialContextURL() + "\"");

				return;
			}

			String rootOrganisationID = rootOrganisationCf.getOrganisationID();

			PersistenceManager pm = getPersistenceManager();
			try {
				if (RegistrationStatus.getRegistrationStatusCount(pm, rootOrganisationID) > 0) {
					if (!force) {
						if (LOGGER.isDebugEnabled())
							LOGGER.debug("There has already been a registration of organisation \""+localOrganisationID+"\" in the root organisation \""+rootOrganisationID+"\" (though it might have been rejected). Will NOT register again. Do it manually if you want to!");
						return;
					}
				} // if (RegistrationStatus.getRegistrationStatusCount(pm, rootOrganisationID) > 0) {

			} finally {
				pm.close();
			}

			LOGGER.info("Registering organisation \""+localOrganisationID+"\" in the root organisation \""+rootOrganisationID+"\".");
			beginRegistration(
					"org.jnp.interfaces.NamingContextFactory", // TODO this should come from a config - maybe an anonymousInitialContextFactory in org.nightlabs.jfire.servermanager.config.J2eeServerTypeRegistryConfigModule - see Lookup.getInitialContextProperties(...)
					rootServer.getInitialContextURL(),
					rootOrganisationID);
		} finally {
			ism.close();
		}
	}

//	/**
//	 * This method returns all organisations that the current organisation knows.
//	 * 
//	 * @return a Collection of instances of type Organisation
//	 * 
//	 * @ejb.interface-method 
//	 */
//	public Collection getOrganisations() 
//	{
//		PersistenceManager pm = persistenceManagerFactory.getPersistenceManager();
//		
//	  Collection organisations = new HashSet();
//	  for (Iterator it = pm.getExtent(Organisation.class, true).iterator(); it.hasNext(); ) {
//	    Organisation o = (Organisation) it.next();
//	    pm.retrieve(o.getServer());
//	    pm.makeTransient(o);
//	    pm.makeTransient(o.getServer());
//	    organisations.add(o);
//	  }
//	  
//	  pm.close();
//	  
//	  return organisations;
//	}

}