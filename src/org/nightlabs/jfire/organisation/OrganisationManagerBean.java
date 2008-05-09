/* *****************************************************************************
 * JFire - it's hot - Free ERP System - http://jfire.org                       *
 * Copyright (C) 2004-2005 NightLabs - http://NightLabs.org                    *
 *                                                                             *
 * This library is free software; you can redistribute it and/or               *
 * modify it under the terms of the GNU Lesser General Public                  *
 * License as published by the Free Software Foundation; either                *
 * version 2.1 of the License, or (at your option) any later version.          *
 *                                                                             *
 * This library is distributed in the hope that it will be useful,             *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of              *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU           *
 * Lesser General Public License for more details.                             *
 *                                                                             *
 * You should have received a copy of the GNU Lesser General Public            *
 * License along with this library; if not, write to the                       *
 *     Free Software Foundation, Inc.,                                         *
 *     51 Franklin St, Fifth Floor,                                            *
 *     Boston, MA  02110-1301  USA                                             *
 *                                                                             *
 * Or get it online :                                                          *
 *     http://opensource.org/licenses/lgpl-license.php                         *
 *                                                                             *
 *                                                                             *
 ******************************************************************************/

package org.nightlabs.jfire.organisation;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.jdo.FetchPlan;
import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.nightlabs.ModuleException;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.base.BaseSessionBeanImpl;
import org.nightlabs.jfire.base.JFireException;
import org.nightlabs.jfire.base.JFirePrincipal;
import org.nightlabs.jfire.base.JFireRemoteException;
import org.nightlabs.jfire.base.Lookup;
import org.nightlabs.jfire.organisation.id.OrganisationID;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.security.UserLocal;
import org.nightlabs.jfire.security.id.UserID;
import org.nightlabs.jfire.servermanager.JFireServerManager;
import org.nightlabs.jfire.servermanager.OrganisationNotFoundException;
import org.nightlabs.jfire.servermanager.config.JFireServerConfigModule;
import org.nightlabs.jfire.servermanager.config.OrganisationCf;
import org.nightlabs.jfire.servermanager.config.RootOrganisationCf;
import org.nightlabs.jfire.servermanager.config.ServerCf;
import org.nightlabs.jfire.servermanager.createorganisation.BusyCreatingOrganisationException;
import org.nightlabs.jfire.servermanager.createorganisation.CreateOrganisationProgress;
import org.nightlabs.jfire.servermanager.createorganisation.CreateOrganisationProgressID;
import org.nightlabs.jfire.servermanager.createorganisation.CreateOrganisationStatus;
import org.nightlabs.jfire.servermanager.createorganisation.CreateOrganisationStep;
import org.nightlabs.jfire.test.cascadedauthentication.TestRequestResultTreeNode;
import org.nightlabs.math.Base62Coder;
import org.nightlabs.util.Util;

/**
 * @author Niklas Schiffler - nick at nightlabs dot de
 * @author Marco Schulze - marco at nightlabs dot de
 *
 * @ejb.bean
 *		name="jfire/ejb/JFireBaseBean/OrganisationManager"
 *		jndi-name="jfire/ejb/JFireBaseBean/OrganisationManager"
 *		type="Stateless"
 *		view-type="remote"
 *
 * @ejb.util generate="physical"
 * @ejb.transaction type="Required"
 **/
public abstract class OrganisationManagerBean
	extends BaseSessionBeanImpl
	implements SessionBean
{
	private static final long serialVersionUID = 1L;
	/**
	 * LOG4J logger used by this class
	 */
	private static final Logger logger = Logger.getLogger(OrganisationManagerBean.class);

	/**
	 * @see org.nightlabs.jfire.base.BaseSessionBeanImpl#setSessionContext(javax.ejb.SessionContext)
	 */
	@Override
	public void setSessionContext(SessionContext sessionContext)
			throws EJBException, RemoteException
	{
		if(logger.isDebugEnabled())
			logger.debug("setSessionContext("+sessionContext+")");
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
		if(logger.isDebugEnabled())
			logger.debug("ejbCreate()");
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
//	 * @ejb.transaction type="Never"
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
	 * @ejb.interface-method
	 * @ejb.transaction type="Required"
	 * @ejb.permission role-name="_ServerAdmin_"
	 */
	public void createOrganisationAfterReboot(String organisationID, String organisationDisplayName, String userID, String password, boolean isServerAdmin)
	throws ModuleException
	{
		try {
			JFireServerManager ism = getJFireServerManager();
			try {
				CreateOrganisationAfterRebootData coar = new CreateOrganisationAfterRebootData(ism);
				coar.addOrganisation(organisationID, organisationDisplayName, userID, password, isServerAdmin);
			} finally {
				ism.close();
			}
		} catch (Exception x) {
			if (x instanceof ModuleException)
				throw (ModuleException)x;
			else
				throw new ModuleException(x);
		}
	}

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
	 * @ejb.transaction type="Never"
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
	 * This method creates an organisation on this server. In contrast to {@link OrganisationManager#createOrganisation(String, String, String, String, boolean)},
	 * this method works asynchronously and you can track the progress using {@link OrganisationManager#getCreateOrganisationProgress(CreateOrganisationProgressID)}.
	 *
	 * @param organisationID The ID of the new organisation. It must be unique in the whole world.
	 * @param organisationDisplayName A nice name that will be used to display the new representative organisation.
	 * @param userID The userID of the first user to create within the new organisation. It will have all necessary permissions to manage users and roles within the new organisation.
	 * @param password The password of the new user.
	 * @param isServerAdmin Whether this user should have global server administration permissions.
	 * @throws BusyCreatingOrganisationException
	 *
	 * @ejb.interface-method
	 * @ejb.transaction type="Never"
	 * @ejb.permission role-name="_ServerAdmin_"
	 */
	public CreateOrganisationProgressID createOrganisationAsync(String organisationID, String organisationDisplayName, String userID, String password, boolean isServerAdmin)
	throws BusyCreatingOrganisationException
	{
		JFireServerManager ism = getJFireServerManager();
		try {
			return ism.createOrganisationAsync(organisationID, organisationDisplayName, userID, password, isServerAdmin);
		} finally {
			ism.close();
		}
	}

	/**
	 * @ejb.interface-method
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 * @ejb.permission role-name="_ServerAdmin_"
	 */
	public CreateOrganisationProgress getCreateOrganisationProgress(CreateOrganisationProgressID createOrganisationProgressID)
	{
		JFireServerManager ism = getJFireServerManager();
		try {
			return ism.getCreateOrganisationProgress(createOrganisationProgressID);
		} finally {
			ism.close();
		}
	}

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_ServerAdmin_"
	 **/
	public List<OrganisationCf> getOrganisationCfs(boolean sorted)
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
	 * @throws OrganisationNotFoundException If the specified organisation does not exist.
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="_ServerAdmin_"
	 **/
	public OrganisationCf getOrganisationConfig(String organisationID)
	throws OrganisationNotFoundException
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
	public Collection<RegistrationStatus> getPendingRegistrations(String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);

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
	 * @ejb.transaction type="Required"
	 * @ejb.permission role-name="_Guest_"
	 **/
	public void notifyAcceptRegistration(String registrationID, Organisation grantOrganisation, String userPassword)
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
			pm.makePersistent(grantOrganisation);
		} finally {
			pm.close();
		}
	}

	/**
	 * @ejb.interface-method
	 * @ejb.transaction type="Required"
	 * @ejb.permission role-name="_Guest_"
	 **/
	public void notifyRejectRegistration(String registrationID)
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
	 * @ejb.transaction type="Required"
	 * @ejb.permission role-name="OrganisationManager-write"
	 * @!ejb.permission role-name="_Guest_"
	 **/
	public void acceptRegistration(String applicantOrganisationID)
	throws JFireRemoteException
	{
		logger.info("acceptRegistration: entered (called by "+getPrincipal().getName()+")");
		try {
			PersistenceManager pm = getPersistenceManager();
			try {
				LocalOrganisation localOrganisation = LocalOrganisation.getLocalOrganisation(pm);

				RegistrationStatus registrationStatus = localOrganisation
				.getPendingRegistration(applicantOrganisationID);

				if (registrationStatus == null)
					throw new IllegalArgumentException("There is no pending registration for applicantOrganisation \""+applicantOrganisationID+"\" at grantOrganisation \""+getOrganisationID()+"\"!");

				// We have to create a user for the new organisation. Therefore,
				// generate a password with a random length between 15 and 20 characters.
				String usrPassword = UserLocal.createMachinePassword(15, 20);

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
				pm.getFetchPlan().setMaxFetchDepth(NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT);
				Organisation grantOrganisation = pm.detachCopy(
						localOrganisation.getOrganisation());
//				Organisation grantOrganisation = localOrganisation.getOrganisation();
//				grantOrganisation.getPerson();
//				grantOrganisation.getServer();
//				pm.makeTransient(grantOrganisation, true);

//				// WORKAROUND Because of a JPOX bug, we need to do this here (it's not nice, though it should be ok, because the transaction should be rolled back if an error occurs)
//				// We close the RegistrationStatus by accepting
//				// and remove it from the pending ones.
//				registrationStatus.accept(User.getUser(pm, getPrincipal()));
//				localOrganisation.removePendingRegistration(applicantOrganisationID);

				// Now, we notify the other organisation that its request has been
				// accepted.
				try {
					OrganisationManager organisationManager = OrganisationManagerUtil.getHome(getInitialContextProperties(applicantOrganisationID)).create();
					organisationManager.notifyAcceptRegistration(
							registrationStatus.getRegistrationID(), grantOrganisation, usrPassword);
				} catch (Exception e) {
					throw new JFireRemoteException(e);
				}

				// WORKAROUND Because of a JPOX bug, we need to do this above.
				// We close the RegistrationStatus by accepting
				// and remove it from the pending ones.
				registrationStatus.accept(User.getUser(pm, getPrincipal()));
				localOrganisation.removePendingRegistration(applicantOrganisationID);

			} finally {
				pm.close();
			}
		} catch (Exception x) {
			logger.error("Accepting registration of applicant '"+applicantOrganisationID+"' at this organisation '" + getOrganisationID() + "' failed!", x);
			if (x instanceof JFireRemoteException)
				throw (JFireRemoteException)x;
			else
				throw new JFireRemoteException(x);
		}
	}

	/**
	 * @ejb.interface-method
	 * @ejb.transaction type="Required"
	 * @ejb.permission role-name="OrganisationManager-write"
	 **/
	public void rejectRegistration(String applicantOrganisationID)
	throws JFireRemoteException
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			LocalOrganisation localOrganisation = LocalOrganisation.getLocalOrganisation(pm);

			RegistrationStatus registrationStatus = localOrganisation
					.getPendingRegistration(applicantOrganisationID);

			if (registrationStatus == null)
				throw new IllegalArgumentException("There is no pending registration for applicantOrganisation \""+applicantOrganisationID+"\" at grantOrganisation \""+getOrganisationID()+"\"!");

			// Now, we notify the other organisation that its request has been
			// rejected.
			try {
				OrganisationManager organisationManager = OrganisationManagerUtil.getHome(getInitialContextProperties(applicantOrganisationID)).create();
				organisationManager.notifyRejectRegistration(registrationStatus.getRegistrationID());
			} catch (Exception e) {
				throw new JFireRemoteException(e);
			}
			// Because notifyRejectRegistration drops our user remotely, we cannot execute
			// any command there anymore - not even remove the bean.
			// organisationManager.remove();

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
	 * @ejb.transaction type="Required"
	 * @ejb.permission role-name="OrganisationManager-write"
	 **/
	public void cancelRegistration(String grantOrganisationID)
	throws JFireRemoteException
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			LocalOrganisation localOrganisation = LocalOrganisation.getLocalOrganisation(pm);
			RegistrationStatus registrationStatus = localOrganisation.getPendingRegistration(grantOrganisationID);
			if (registrationStatus == null)
				throw new NoPendingRegistrationExistingException("There is no pending registration for grantOrganisation \""+grantOrganisationID+"\" at applicantOrganisation \""+getOrganisationID()+"\"!");

			// Create the initial context properties to connect to the remote server.
			Properties props = new Properties();
			props.put(Context.INITIAL_CONTEXT_FACTORY, registrationStatus.getInitialContextFactory());
			props.put(Context.PROVIDER_URL, registrationStatus.getInitialContextURL());

			// Obtain the OrganisationLinker EJB and request registration
			try {
				OrganisationLinker organisationLinker = OrganisationLinkerUtil.getHome(props).create();
				organisationLinker.cancelRegistration(
						registrationStatus.getRegistrationID(),
						getOrganisationID(), grantOrganisationID);
			} catch (Exception e) {
				if (ExceptionUtils.indexOfThrowable(e, NoPendingRegistrationExistingException.class) >= 0)
					logger.warn("There is no pending organisation-registration on the remote side. Will ignore this problem and remove the pending registration locally.", e);
				else
					throw new JFireRemoteException(e);
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
	 * @throws OrganisationAlreadyRegisteredException
	 *
	 * @ejb.interface-method
	 * @ejb.transaction type="Required"
	 * @ejb.permission role-name="OrganisationManager-write"
	 **/
	public void beginRegistration(
			String initialContextFactory, String initialContextURL, String organisationID)
	throws OrganisationAlreadyRegisteredException, JFireRemoteException
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			beginRegistration(pm, getPrincipal(), initialContextFactory, initialContextURL, organisationID);
		} finally {
			pm.close();
		}
	}

	protected static void beginRegistration(
			PersistenceManager pm, JFirePrincipal principal,
			String initialContextFactory, String initialContextURL, String organisationID)
	throws OrganisationAlreadyRegisteredException, JFireRemoteException
	{
		String registrationID = principal.getOrganisationID()
				+ '-' + Base62Coder.sharedInstance().encode(System.currentTimeMillis(), 1)
				+ '-' + Base62Coder.sharedInstance().encode((int)Math.round(Integer.MAX_VALUE * Math.random()), 1);

		// fetch and detach our local organisation (containing the local server)
		Organisation myOrganisation;

		LocalOrganisation localOrganisation = LocalOrganisation.getLocalOrganisation(pm);

		// We need to find out, whether the applicant organisation has already
		// been successfully registered (status accepted).
		// If there is currently a pending registration, it will be cancelled.
		RegistrationStatus.ensureRegisterability(
				pm, localOrganisation, organisationID);

		pm.getFetchPlan().addGroup(FetchPlan.ALL); // TODO fetch-groups?!
		pm.getFetchPlan().setMaxFetchDepth(NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT);
		myOrganisation = pm.detachCopy(localOrganisation.getOrganisation());

		RegistrationStatus registrationStatus = new RegistrationStatus(
				registrationID,
				organisationID, User.getUser(pm, principal),
				initialContextFactory, initialContextURL);

		localOrganisation.addPendingRegistration(registrationStatus);

		// We have to create a user for the new organisation. Therefore,
		// generate a password with a random length between 8 and 16 characters.
		String usrPassword = UserLocal.createMachinePassword(8, 16);

		// Create the user if it doesn't yet exist
		String userID = User.USERID_PREFIX_TYPE_ORGANISATION + organisationID;
		try {
			User user = User.getUser(pm, principal.getOrganisationID(), userID);
			user.getUserLocal().setPasswordPlain(usrPassword); // set the new password, if the user already exists
		} catch (JDOObjectNotFoundException x) {
			// Create the user
			User user = new User(principal.getOrganisationID(), userID);
			UserLocal userLocal = new UserLocal(user);
			userLocal.setPasswordPlain(usrPassword);
			pm.makePersistent(user);
		}

		// Create the initial context properties to connect to the remote server.
		Properties props = new Properties();
		props.put(Context.INITIAL_CONTEXT_FACTORY, initialContextFactory);
		props.put(Context.PROVIDER_URL, initialContextURL);

		// Obtain the OrganisationLinker EJB and request registration
		try {
			OrganisationLinker organisationLinker = OrganisationLinkerUtil.getHome(props).create();
			organisationLinker.requestRegistration(registrationID, myOrganisation, organisationID, usrPassword);
		} catch (OrganisationAlreadyRegisteredException x) {
			throw x;
		} catch (Exception x) {
			throw new JFireRemoteException(x);
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
	 * @ejb.transaction type="Required"
	 * @ejb.permission role-name="OrganisationManager-write"
	 **/
	public void ackRegistration(String grantOrganisationID)
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

//	/**
//	 * @ejb.interface-method
//	 * @ejb.transaction type="Required"
//	 * @ejb.permission role-name="_Guest_"
//	 **/
//	public void testBackhand(String[] organisationIDs)
//	throws ModuleException
//	{
//		logger.info("testBackhand ("+organisationIDs.length+"): begin: principal="+getPrincipalString());
//		if (organisationIDs != null && organisationIDs.length > 0) {
//			String organisationID = organisationIDs[0];
//			String[] bhOrgaIDs = new String[organisationIDs.length - 1];
//			for (int i = 1; i < organisationIDs.length; ++i) {
//				bhOrgaIDs[i-1] = organisationIDs[i];
//			}
//
//			logger.info("testBackhand ("+organisationIDs.length+"): backhanding to organisation \""+organisationID+"\"");
//			try {
//				logger.info("testBackhand ("+organisationIDs.length+"): OrganisationManagerUtil.getHome(...)");
//				OrganisationManager organisationManager = OrganisationManagerUtil.getHome(getInitialContextProperties(organisationID)).create();
//
//				logger.info("testBackhand ("+organisationIDs.length+"): UserManagerUtil.getHome()");
//				UserManagerHome userManagerHome = UserManagerUtil.getHome();
//
//				logger.info("testBackhand ("+organisationIDs.length+"): userManagerHome.create()");
//				UserManager userManager = userManagerHome.create();
//
//				logger.info("testBackhand ("+organisationIDs.length+"): organisationManager.testBackhand(...)");
//				organisationManager.testBackhand(bhOrgaIDs);
//
//				logger.info("testBackhand ("+organisationIDs.length+"): userManager.whoami()");
//				userManager.whoami();
//
//				organisationManager.remove();
//				userManager.remove();
//			} catch (RuntimeException e) {
//				throw e;
//			} catch (ModuleException e) {
//				throw e;
//			} catch (Exception e) {
//				throw new ModuleException(e);
//			}
//		}
//		logger.info("testBackhand ("+organisationIDs.length+"): end: principal="+getPrincipalString());
//	}

	/**
	 * @ejb.interface-method
	 * @ejb.transaction type="Required"
	 * @ejb.permission role-name="_Guest_"
	 **/
	@SuppressWarnings("unchecked")
	public TestRequestResultTreeNode testCascadedAuthentication(TestRequestResultTreeNode node)
	throws Exception
	{
		return internal_testCascadedAuthentication(node, -1);
	}

	/**
	 * @ejb.interface-method
	 * @ejb.transaction type="Required"
	 * @ejb.permission role-name="_Guest_"
	 **/
	@SuppressWarnings("unchecked")
	public TestRequestResultTreeNode internal_testCascadedAuthentication(TestRequestResultTreeNode node, int level)
	throws Exception
	{
		logger.info("testCascadedAuthentication (level="+level+") (principal="+getPrincipalString() +"): begin");
		if (node == null)
			throw new IllegalArgumentException("node must not be null!");

		if (level < -1)
			throw new IllegalArgumentException("level < -1 : level=" + level);

		node = Util.cloneSerializable(node); // we clone, because it may not be serialized due to optimizations of JBoss

		String organisationIDBeforeRecursion = getOrganisationID();

		List<TestRequestResultTreeNode> children = node.getChildren();
		if (level < 0) {
			children = new ArrayList<TestRequestResultTreeNode>();
			children.add(node);
			node = null;
		}

		if (!children.isEmpty()) {
			PersistenceManager pm = getPersistenceManager();
			try {
				Map<String, OrganisationManager> organisationManagers = new HashMap<String, OrganisationManager>();

				for (TestRequestResultTreeNode childNode : children) {
					String organisationID = childNode.getRequest_organisationID();
					logger.info("testCascadedAuthentication (level="+level+") (principal="+getPrincipalString()+"): creating organisationManager for organisationID=" + organisationID);
					OrganisationManagerHome home = OrganisationManagerUtil.getHome(Lookup.getInitialContextProperties(pm, organisationID));
					OrganisationManager organisationManager = home.create();
					organisationManagers.put(organisationID, organisationManager);
					logger.info("testCascadedAuthentication (level="+level+") (principal="+getPrincipalString()+"): created organisationManager for organisationID=" + organisationID);
				}

				for (TestRequestResultTreeNode childNode : children) {
					logger.info("testCascadedAuthentication (level="+level+") (principal="+getPrincipalString()+"): calling organisationManager.testCascadedAuthentication() for organisationID=" + childNode.getRequest_organisationID());
					OrganisationManager organisationManager = organisationManagers.get(childNode.getRequest_organisationID());
					TestRequestResultTreeNode resultChildNode = organisationManager.internal_testCascadedAuthentication(childNode, level + 1);

					if (childNode.getParent() == null) {
						if (children.size() != 1)
							throw new IllegalStateException("children.size() != 1");

						children.clear();
						children.add(resultChildNode);
					}
					else {
						if (node != childNode.getParent())
							throw new IllegalStateException("node != childNode.getParent()");

						node.replaceChild(childNode, resultChildNode);
					}

					logger.info("testCascadedAuthentication (level="+level+") (principal="+getPrincipalString()+"): called organisationManager.testCascadedAuthentication() for organisationID=" + childNode.getRequest_organisationID());
				}
			} finally {
				pm.close();
			}
		}

		logger.info("testCascadedAuthentication (level="+level+") (principal="+getPrincipalString() +"): end");

		if (node != null) {
			node.setResult_organisationID_beforeRecursion(organisationIDBeforeRecursion);
			node.setResult_organisationID_afterRecursion(getOrganisationID());
		}
		else {
			if (children.size() != 1)
				throw new IllegalStateException("children.size() != 1");

			node = children.get(0);
			if (node.getParent() != null)
				throw new IllegalStateException("node is not root: node.getParent() != null");
		}

		return node;
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
	 * @throws NamingException
	 * @throws CreateException
	 * @throws RemoteException
	 * @throws OrganisationAlreadyRegisteredException
	 *
	 * @ejb.interface-method
	 * @ejb.transaction type="Required"
	 * @ejb.permission role-name="_System_,OrganisationManager-write"
	 **/
	public void registerInRootOrganisation()
	throws OrganisationAlreadyRegisteredException, JFireRemoteException
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
	 * @ejb.transaction type="Required"
	 * @ejb.permission role-name="_Guest_"
	 **/
	public Collection<Organisation> getOrganisationsFromRootOrganisation(boolean filterPartnerOrganisations, String[] fetchGroups, int maxFetchDepth)
	throws JFireException
	{
		try {
			InitialContext ctx = new InitialContext();
			try {
				String rootOrganisationID = Organisation.getRootOrganisationID(ctx);
				String localOrganisationID = getOrganisationID();
				if (!rootOrganisationID.equals(localOrganisationID)) {
					OrganisationManager organisationManager = OrganisationManagerUtil.getHome(getInitialContextProperties(rootOrganisationID)).create();
					Collection<Organisation> res = organisationManager.getOrganisationsFromRootOrganisation(filterPartnerOrganisations, fetchGroups, maxFetchDepth);

					// TODO DEBUG begin
					logger.info("Root Organisation returned the following organisations:");
					if (res.isEmpty())
						logger.info("  {NONE}");
					else {
						for (Iterator<Organisation> iter = res.iterator(); iter.hasNext();) {
							Organisation organisation = iter.next();
							logger.info("  " + organisation.getOrganisationID());
						}
					}
					// TODO DEBUG end

					if (!filterPartnerOrganisations)
						return res;

					Collection<Organisation> newRes = new LinkedList<Organisation>();

					PersistenceManager pm = getPersistenceManager();
					try {
						pm.getExtent(User.class);

						for (Iterator<Organisation> it = res.iterator(); it.hasNext(); ) {
							Organisation orga = it.next();
							if (getOrganisationID().equals(orga.getOrganisationID())) {
								logger.info("Organisation is myself and will be filtered: " + orga.getOrganisationID());
								continue;
							}

							String userID = User.USERID_PREFIX_TYPE_ORGANISATION + orga.getOrganisationID();
							try {
								pm.getObjectById(UserID.create(localOrganisationID, userID));
								logger.info("Organisation is already a partner and will be filtered: " + orga.getOrganisationID());
							} catch (JDOObjectNotFoundException x) {
								logger.info("Organisation will not be filtered and added to result: " + orga.getOrganisationID());
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
				pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
				if (fetchGroups != null)
					pm.getFetchPlan().setGroups(fetchGroups);

				Collection<Organisation> res = (Collection<Organisation>)pm.newQuery(Organisation.class).execute();

				// TODO DEBUG begin
				logger.info("I am the Root Organisation and I will return the following organisations: ");
				for (Iterator<Organisation> iter = res.iterator(); iter.hasNext();) {
					Organisation organisation = iter.next();
					logger.info("  " + organisation.getOrganisationID());
				}
				// TODO DEBUG end

				return pm.detachCopyAll(res);
			} finally {
				pm.close();
			}

		} catch (JFireException x) {
			logger.error("Obtaining organisations failed!", x);
			throw x;
		} catch (Throwable x) {
			logger.error("Obtaining organisations failed!", x);
			throw new JFireException(x);
		}
	}

	/**
	 * This method registers this organisation in the
	 * network's root-organisation, if it is not yet registered (or rejected). If the registration
	 * has been rejected, it does not try again if you do not pass <code>force = true</code>.
	 * <p>
	 * Note, that this method does nothing, if the local organisation IS the root-organisation.
	 * </p>
	 * @throws OrganisationAlreadyRegisteredException
	 *
	 * @ejb.interface-method
	 * @ejb.transaction type="Required"
	 * @ejb.permission role-name="_System_,OrganisationManager-write"
	 **/
	public void registerInRootOrganisation(boolean force)
	throws OrganisationAlreadyRegisteredException, JFireRemoteException
	{
		try {
			InitialContext initialContext = new InitialContext();
			try {
				if (!Organisation.hasRootOrganisation(initialContext)) {
					logger.info("registerInRootOrganisation: This server is in stand-alone mode (no root-organisation); we do not register.");
					return;
				}
			} finally {
				initialContext.close();
			}
		} catch (NamingException e) {
			throw new RuntimeException("Fucking shit! Our own JNDI isn't available!");
		}

		JFireServerManager ism = getJFireServerManager();
		try {
			JFireServerConfigModule jfireServerConfigModule = ism.getJFireServerConfigModule();
			RootOrganisationCf rootOrganisationCf = jfireServerConfigModule.getRootOrganisation();
			ServerCf rootServer = rootOrganisationCf.getServer();
			String localOrganisationID = getOrganisationID();
			if (localOrganisationID.equals(rootOrganisationCf.getOrganisationID())) {
				// This IS the root-organisation. Only check the correctness of the server.
				if (logger.isDebugEnabled())
					logger.debug("Organisation \"" + localOrganisationID + "\" is the root-organisation. Will not perform registration.");

				ServerCf localServer = jfireServerConfigModule.getLocalServer();

				if (!localServer.getServerID().equals(rootServer.getServerID()))
					logger.error("localOrganisation.server.serverID == \"" + localServer.getServerID() + "\" != rootOrganisation.server.serverID == \"" + rootServer.getServerID() + "\"");

				if (!localServer.getServerName().equals(rootServer.getServerID()))
					logger.error("localOrganisation.server.serverName == \"" + localServer.getServerName() + "\" != rootOrganisation.server.serverName == \"" + rootServer.getServerName() + "\"");

				if (!localServer.getJ2eeServerType().equals(rootServer.getJ2eeServerType()))
					logger.error("localOrganisation.server.j2eeServerType == \"" + localServer.getJ2eeServerType() + "\" != rootOrganisation.server.j2eeServerType == \"" + rootServer.getJ2eeServerType() + "\"");

				if (!localServer.getInitialContextURL().equals(rootServer.getInitialContextURL()))
					logger.error("localOrganisation.server.initialContextURL == \"" + localServer.getInitialContextURL() + "\" != rootOrganisation.server.initialContextURL == \"" + rootServer.getInitialContextURL() + "\"");

				return;
			}

			String rootOrganisationID = rootOrganisationCf.getOrganisationID();

			PersistenceManager pm = getPersistenceManager();
			try {
				if (RegistrationStatus.getRegistrationStatusCount(pm, rootOrganisationID) > 0) {
					if (!force) {
						if (logger.isDebugEnabled())
							logger.debug("There has already been a registration of organisation \""+localOrganisationID+"\" in the root organisation \""+rootOrganisationID+"\" (though it might have been rejected). Will NOT register again. Do it manually if you want to!");
						return;
					}
				} // if (RegistrationStatus.getRegistrationStatusCount(pm, rootOrganisationID) > 0) {

				logger.info("Registering organisation \""+localOrganisationID+"\" in the root organisation \""+rootOrganisationID+"\".");
				beginRegistration(
						pm,
						getPrincipal(),
						getJFireServerManagerFactory().getJ2eeRemoteServer(rootOrganisationCf.getServer().getJ2eeServerType()).getAnonymousInitialContextFactory(),
						rootServer.getInitialContextURL(),
						rootOrganisationID);

			} finally {
				pm.close();
			}
		} finally {
			ism.close();
		}
	}

	/**
	 * This method is used internally to fill a newly created organisation with initial
	 * objects like {@link Server}, {@link Organisation}, {@link User}, {@link UserLocal} and
	 * many more.
	 *
	 * @ejb.interface-method
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 * @ejb.permission role-name="_System_"
	 **/
	public void internalInitializeEmptyOrganisation(
			CreateOrganisationProgressID createOrganisationProgressID,
			ServerCf localServerCf,
			OrganisationCf organisationCf,
			String userID, String password
			)
	throws CreateException, NamingException
	{
		OrganisationManagerHelperLocal m = OrganisationManagerHelperUtil.getLocalHome().create();
		JFireServerManager jfsm = getJFireServerManager();
		try {
			jfsm.createOrganisationProgress_addCreateOrganisationStatus(createOrganisationProgressID,
					new CreateOrganisationStatus(CreateOrganisationStep.OrganisationManagerHelper_initializeEmptyOrganisation_step1_begin));

			m.internalInitializeEmptyOrganisation_step1(localServerCf, organisationCf, userID, password);

			jfsm.createOrganisationProgress_addCreateOrganisationStatus(createOrganisationProgressID,
					new CreateOrganisationStatus(CreateOrganisationStep.OrganisationManagerHelper_initializeEmptyOrganisation_step1_end));


			jfsm.createOrganisationProgress_addCreateOrganisationStatus(createOrganisationProgressID,
					new CreateOrganisationStatus(CreateOrganisationStep.OrganisationManagerHelper_initializeEmptyOrganisation_step2_begin));

			m.internalInitializeEmptyOrganisation_step2();

			jfsm.createOrganisationProgress_addCreateOrganisationStatus(createOrganisationProgressID,
					new CreateOrganisationStatus(CreateOrganisationStep.OrganisationManagerHelper_initializeEmptyOrganisation_step2_end));


			jfsm.createOrganisationProgress_addCreateOrganisationStatus(createOrganisationProgressID,
					new CreateOrganisationStatus(CreateOrganisationStep.OrganisationManagerHelper_initializeEmptyOrganisation_step3_begin));

			m.internalInitializeEmptyOrganisation_step3(localServerCf, organisationCf, userID);

			jfsm.createOrganisationProgress_addCreateOrganisationStatus(createOrganisationProgressID,
					new CreateOrganisationStatus(CreateOrganisationStep.OrganisationManagerHelper_initializeEmptyOrganisation_step3_end));
		} finally {
			jfsm.close();
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

	/**
	 * @ejb.interface-method
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 * @ejb.permission role-name="_Guest_"
	 */
	public Set<OrganisationID> getOrganisationIDs()
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			Query q = pm.newQuery(Organisation.class);
			q.setResult("JDOHelper.getObjectId(this)");
			return new HashSet<OrganisationID>((Collection<? extends OrganisationID>) q.execute());
		} finally {
			pm.close();
		}
	}

	/**
	 * @ejb.interface-method
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 * @ejb.permission role-name="_Guest_"
	 */
	@SuppressWarnings("unchecked")
	public List<Organisation> getOrganisations(Collection<OrganisationID> organisationIDs, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			return NLJDOHelper.getDetachedObjectList(pm, organisationIDs, Organisation.class, fetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}
	}
}
