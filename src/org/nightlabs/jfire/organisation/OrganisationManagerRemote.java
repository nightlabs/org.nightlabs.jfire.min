package org.nightlabs.jfire.organisation;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.ejb.CreateException;
import javax.ejb.Remote;
import javax.naming.NamingException;

import org.nightlabs.jfire.base.JFireException;
import org.nightlabs.jfire.base.JFireRemoteException;
import org.nightlabs.jfire.organisation.id.OrganisationID;
import org.nightlabs.jfire.organisationinit.OrganisationInitException;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.security.UserLocal;
import org.nightlabs.jfire.servermanager.OrganisationNotFoundException;
import org.nightlabs.jfire.servermanager.config.OrganisationCf;
import org.nightlabs.jfire.servermanager.config.ServerCf;
import org.nightlabs.jfire.servermanager.createorganisation.BusyCreatingOrganisationException;
import org.nightlabs.jfire.servermanager.createorganisation.CreateOrganisationException;
import org.nightlabs.jfire.servermanager.createorganisation.CreateOrganisationProgress;
import org.nightlabs.jfire.servermanager.createorganisation.CreateOrganisationProgressID;
import org.nightlabs.jfire.test.cascadedauthentication.TestRequestResultTreeNode;

@Remote
public interface OrganisationManagerRemote
{
	void createOrganisationAfterReboot(String organisationID,
			String organisationDisplayName, String userID, String password,
			boolean isServerAdmin) throws IOException;

	/**
	 * This method creates an organisation on this server.
	 *
	 * @param organisationID The ID of the new organisation. It must be unique in the whole world.
	 * @param organisationDisplayName A nice name that will be used to display the new representative organisation.
	 * @param userID The userID of the first user to create within the new organisation. It will have all necessary permissions to manage users and roles within the new organisation.
	 * @param password The password of the new user.
	 * @param isServerAdmin Whether this user should have global server administration permissions.
	 * @throws CreateOrganisationException If creating the organisation failed
	 * @throws OrganisationInitException If initializing the organisation failed
	 */
	void createOrganisation(String organisationID,
			String organisationDisplayName, String userID, String password,
			boolean isServerAdmin) throws OrganisationInitException,
			CreateOrganisationException;

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
	 */
	CreateOrganisationProgressID createOrganisationAsync(String organisationID,
			String organisationDisplayName, String userID, String password,
			boolean isServerAdmin) throws BusyCreatingOrganisationException;

	CreateOrganisationProgress getCreateOrganisationProgress(
			CreateOrganisationProgressID createOrganisationProgressID);

	List<OrganisationCf> getOrganisationCfs(boolean sorted);

	/**
	 * @throws OrganisationNotFoundException If the specified organisation does not exist.
	 */
	OrganisationCf getOrganisationConfig(String organisationID)
			throws OrganisationNotFoundException;

	/**
	 * This method finds out whether there exists at least one arganisation.
	 * @return <code>true</code> if no organisation exists on this server; <code>false</code> if there is at least one organisation.
	 *
	 * @see org.nightlabs.jfire.servermanager.JFireServerManager#isOrganisationCfsEmpty()
	 */
	boolean isOrganisationCfsEmpty();

	Collection<RegistrationStatus> getPendingRegistrations(
			String[] fetchGroups, int maxFetchDepth);

	/**
	 * This method is called by the other organisation to inform this
	 * one that the registration has been accepted.
	 * <br/><br/>
	 * Though, there
	 * is no j2ee security, it is extremely hard to guess the correct
	 * registrationID (a registrationID is actually far more secure than an average password).
	 * Additionally, the organisation is read out
	 * of the JFirePrincipal and must match the other organisationID.
	 */
	void notifyAcceptRegistration(String registrationID,
			Organisation grantOrganisation, String userPassword);

	void notifyRejectRegistration(String registrationID);

	/**
	 * This method is called by the client (a user who has organisation administrative
	 * rights) to accept a registration. The registration must have been previously
	 * applied for by the other organisation.
	 */
	void acceptRegistration(String applicantOrganisationID)
			throws JFireRemoteException;

	void rejectRegistration(String applicantOrganisationID)
			throws JFireRemoteException;

	void cancelRegistration(String grantOrganisationID)
			throws JFireRemoteException;

	/**
	 * This method is called by a client. It is the first step to make two
	 * organisations know each other. It creates a user for the new organisation
	 * and requests to be registered at the other organisation.
	 * @throws OrganisationAlreadyRegisteredException
	 */
	void beginRegistration(String initialContextFactory,
			String initialContextURL, String organisationID)
			throws OrganisationAlreadyRegisteredException, JFireRemoteException;

	/**
	 * This method is called by a client. After we began a registration and
	 * the partner has either rejected or accepted, we still have the
	 * RegistrationStatus in the list of pending registrations. This
	 * is for the user to recognize that the other organisation has reacted
	 * and what this reaction was (accept or reject).
	 **/
	void ackRegistration(String grantOrganisationID);

	TestRequestResultTreeNode testCascadedAuthentication(
			TestRequestResultTreeNode node) throws Exception;

	TestRequestResultTreeNode internal_testCascadedAuthentication(
			TestRequestResultTreeNode node, int level) throws Exception;

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
	 * @throws OrganisationAlreadyRegisteredException
	 */
	void registerInRootOrganisation()
			throws OrganisationAlreadyRegisteredException, JFireRemoteException;

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
	 */
	Collection<Organisation> getOrganisationsFromRootOrganisation(
			boolean filterPartnerOrganisations, String[] fetchGroups,
			int maxFetchDepth) throws JFireException;

	/**
	 * This method registers this organisation in the
	 * network's root-organisation, if it is not yet registered (or rejected). If the registration
	 * has been rejected, it does not try again if you do not pass <code>force = true</code>.
	 * <p>
	 * Note, that this method does nothing, if the local organisation IS the root-organisation.
	 * </p>
	 * @throws OrganisationAlreadyRegisteredException
	 */
	void registerInRootOrganisation(boolean force)
			throws OrganisationAlreadyRegisteredException, JFireRemoteException;

	/**
	 * This method is used internally to fill a newly created organisation with initial
	 * objects like {@link org.nightlabs.jfire.server.Server}, {@link Organisation}, {@link User}, {@link UserLocal} and
	 * many more.
	 */
	void internalInitializeEmptyOrganisation(
			CreateOrganisationProgressID createOrganisationProgressID,
			ServerCf localServerCf, OrganisationCf organisationCf,
			String userID, String password) throws CreateException,
			NamingException;

	Set<OrganisationID> getOrganisationIDs();

	List<Organisation> getOrganisations(
			Collection<OrganisationID> organisationIDs, String[] fetchGroups,
			int maxFetchDepth);

	/**
	 * Stores the given {@link Organisation} object if it is the organisation of the calling
	 * user (throws an {@link IllegalArgumentException} otherwise).
	 * Optionally returns a detached copy of the new version.
	 *
	 * @param organisation The {@link Organisation} to store. Has to be the organisation of the calling user.
	 * @param get Whether to return a detached copy of the new verison of the given {@link Organisation}.
	 * @param fetchGroups The fetch-groups to detach the {@link Organisation} with.
	 * @param maxFetchDepth The maximum fetch-depth to use when detaching.
	 * @return A detached copy of the new version of the given {@link Organisation}, or <code>null</code> if get is <code>false</code>.
	 */
	Organisation storeLocalOrganisation(Organisation organisation, boolean get,
			String[] fetchGroups, int maxFetchDepth);

	String ping(String message);
}