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

package org.nightlabs.jfire.security;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ejb.EJBContext;
import javax.jdo.JDODetachedFieldAccessException;
import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.jdo.annotations.Column;
import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.FetchGroups;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.Key;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.PersistenceModifier;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;
import javax.jdo.annotations.Value;
import javax.jdo.listener.StoreCallback;

import org.nightlabs.i18n.I18nText;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jdo.ObjectIDUtil;
import org.nightlabs.jfire.base.JFireBasePrincipal;
import org.nightlabs.jfire.organisation.LocalOrganisation;
import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.jfire.security.id.AuthorityID;
import org.nightlabs.jfire.security.id.AuthorityTypeID;
import org.nightlabs.jfire.security.id.AuthorizedObjectID;
import org.nightlabs.jfire.security.id.RoleGroupID;
import org.nightlabs.jfire.security.id.RoleID;
import org.nightlabs.jfire.security.id.UserID;
import org.nightlabs.jfire.security.id.UserLocalID;
import org.nightlabs.jfire.security.listener.SecurityChangeController;
import org.nightlabs.util.CollectionUtil;
import org.nightlabs.util.Util;

/**
 * <p>
 * Instances of this class describe a security configuration, i.e. a scope within which users can have
 * access rights granted.
 * </p>
 * <p>
 * To define only links between {@link User}s (or {@link UserGroup})s and {@link Role}s (or {@link RoleGroup}s),
 * would reduce the grain of ACLs (access control lists) to the {@link Organisation} level. This is not sufficient,
 * if it should be possible to define different rights for different target objects with the same type within
 * the same <code>Organisation</code>. Thus, the actual links are managed within the context of an
 * <code>Authority</code>. Each Authority can contain 0 or more <code>User[Group]</code>s and 0 or more
 * <code>RoleGroup</code>s. Note, that it is not possible to manage {@link Role}s directly in an <code>Authority</code>!
 * Only <code>RoleGroup</code>s can be assigned to <code>User</code>s or <code>UserGroup</code>s.
 * </p>
 * <p>
 * Every target object (usually implementing {@link SecuredObject}), onto which an action is executed,
 * lies within the responsibility of 0 or 1 authorities. If there is no responsible <code>Authority</code>
 * defined, only the per <code>Organisation</code> access is controlled. If there is an <code>Authority</code> assigned
 * to the target, the access rights for the <code>User</code> within this <code>Authority</code> will be checked after
 * the general per <code>Organisation</code> check was successful. This means, it's a two-door-principle: Once, you
 * entered the outer door, the inner one either has no lock or the lock is managed by the responsible
 * <code>Authority</code>. Thus, you can only restrict rights on the <code>Authority</code> level that have
 * been granted on the <code>Organisation</code> level.
 * </p>
 * <p>
 * Note, that there is an exception to this 2-door-principle: The access rights management itself (i.e. the modification
 * of an <code>Authority</code> is solely controlled by the "inner door", because it must be possible for everyone
 * to execute the EJB methods, since it would otherwise not be possible to allow this person to edit access rights
 * anywhere in the system. But a user should be able to edit access rights for certain domains - e.g. decide about
 * who can read vouchers without having system wide security-administration privileges. Therefore, access to an
 * <code>Authority</code> is only controlled by that single <code>Authority</code> which is returned by
 * {@link #resolveSecuringAuthority()}.
 * </p>
 * <p>
 * To define the ACLs for the <code>Organisation</code> level, a special Authority named "_Organisation_"
 * ({@link #AUTHORITY_ID_ORGANISATION}) is existent within each <code>Organisation</code>.
 * </p>
 * <p>
 * To avoid registration of every User within every Authority, the special user "_Other_" ({@link User#USER_ID_OTHER})
 * exists. The access rights defined for this special {@link User} will be applied, if the acting User is neither
 * registered directly within an Authority nor indirectly by a {@link UserGroup} in which he is a member.
 * </p>
 *
 * @author Marco Schulze - marco at nightlabs dot de
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.security.id.AuthorityID"
 *		detachable="true"
 *		table="JFireBase_Authority"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.create-objectid-class field-order="organisationID, authorityID"
 *
 * @jdo.fetch-group name="Authority.name" fields="name"
 * @jdo.fetch-group name="Authority.description" fields="description"
 * @jdo.fetch-group name="Authority.authorityType" fields="authorityType"
 * @jdo.fetch-group name="Authority.authorizedObjectRefs" fields="authorizedObjectRefs"
 * @jdo.fetch-group name="Authority.roleGroupRefs" fields="roleGroupRefs"
 */
@PersistenceCapable(
	objectIdClass=AuthorityID.class,
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireBase_Authority")
@FetchGroups({
	@FetchGroup(
		name=Authority.FETCH_GROUP_NAME,
		members=@Persistent(name="name")),
	@FetchGroup(
		name=Authority.FETCH_GROUP_DESCRIPTION,
		members=@Persistent(name="description")),
	@FetchGroup(
		name=Authority.FETCH_GROUP_AUTHORITY_TYPE,
		members=@Persistent(name="authorityType")),
	@FetchGroup(
		name=Authority.FETCH_GROUP_AUTHORIZED_OBJECT_REFS,
		members=@Persistent(name="authorizedObjectRefs")),
	@FetchGroup(
		name=Authority.FETCH_GROUP_ROLE_GROUP_REFS,
		members=@Persistent(name="roleGroupRefs"))
})
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class Authority
implements Serializable, SecuredObject, StoreCallback
{
	private static final long serialVersionUID = 1L;

//	private static final Logger logger = Logger.getLogger(Authority.class);

	public static final String FETCH_GROUP_NAME = "Authority.name";
	public static final String FETCH_GROUP_DESCRIPTION = "Authority.description";
	public static final String FETCH_GROUP_AUTHORITY_TYPE = "Authority.authorityType";
	public static final String FETCH_GROUP_AUTHORIZED_OBJECT_REFS = "Authority.authorizedObjectRefs";
	public static final String FETCH_GROUP_ROLE_GROUP_REFS = "Authority.roleGroupRefs";

	/**
	 * This constant defines the authorityID that is used for representing
	 * the organisation itself.
	 * @see #getOrganisationAuthority(PersistenceManager)
	 */
	public static String AUTHORITY_ID_ORGANISATION = "_Organisation_";

	/**
	 * Get the <code>Authority</code> that is used for the global per-organisation access right configuration.
	 * This <code>Authority</code> controls the authorization performed by the JavaEE container (on EJB method
	 * level).
	 *
	 * @param pm the door to the datastore.
	 * @return the organisation-wide access right configuration.
	 * @see #AUTHORITY_ID_ORGANISATION
	 */
	public static Authority getOrganisationAuthority(PersistenceManager pm)
	{
		LocalOrganisation localOrganisation = LocalOrganisation.getLocalOrganisation(pm);
		AuthorityID authorityID = AuthorityID.create(localOrganisation.getOrganisationID(), AUTHORITY_ID_ORGANISATION);
		return (Authority) pm.getObjectById(authorityID);
	}

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	@PrimaryKey
	@Column(length=100)
	private String organisationID;

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="50"
	 */
	@PrimaryKey
	@Column(length=50)
	private String authorityID;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private AuthorityType authorityType;

	/**
	 * @jdo.field persistence-modifier="persistent" dependent="true" mapped-by="authority"
	 */
	@Persistent(
		dependent="true",
		mappedBy="authority",
		persistenceModifier=PersistenceModifier.PERSISTENT)
	private AuthorityName name;

	/**
	 * @jdo.field persistence-modifier="persistent" dependent="true" mapped-by="authority"
	 */
	@Persistent(
		dependent="true",
		mappedBy="authority",
		persistenceModifier=PersistenceModifier.PERSISTENT)
	private AuthorityDescription description;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private String securingAuthorityID;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private String securingAuthorityTypeID;

	/**
	 * key: String authorizedObjectID (toString-representation of AuthorizedObjectID)<br/>
	 * value: AuthorizedObjectRef authorizedObjectRef
	 * <br/><br/>
	 * Authority (1) - (n) AuthorizedObjectRef
	 *
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="map"
	 *		key-type="java.lang.String"
	 *		value-type="AuthorizedObjectRef"
	 *		mapped-by="authority"
	 *		dependent-value="true"
	 *
	 * @jdo.key mapped-by="authorizedObjectID"
	 */
	@Persistent(
		mappedBy="authority",
		persistenceModifier=PersistenceModifier.PERSISTENT)
	@Key(mappedBy="authorizedObjectID")
	@Value(dependent="true")
	private Map<String, AuthorizedObjectRef> authorizedObjectRefs = new HashMap<String, AuthorizedObjectRef>();

	/**
	 * key: String roleGroupID<br/>
	 * value: RoleGroupRef roleGroupRef
	 * <br/><br/>
	 * Authority (1) - (n) RoleGroupRef
	 *
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="map"
	 *		key-type="java.lang.String"
	 *		value-type="RoleGroupRef"
	 *		mapped-by="authority"
	 *		dependent-value="true"
	 *
	 * @jdo.key mapped-by="roleGroupID"
	 */
	@Persistent(
		mappedBy="authority",
		persistenceModifier=PersistenceModifier.PERSISTENT)
	@Key(mappedBy="roleGroupID")
	@Value(dependent="true")
	private Map<String, RoleGroupRef> roleGroupRefs = new HashMap<String, RoleGroupRef>();

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private String managedBy;

	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected Authority() { }

	public Authority(String organisationID, String _authorityID, AuthorityType authorityType)
	{
		if (!ObjectIDUtil.isValidIDString(_authorityID))
			throw new IllegalArgumentException("authorityID \""+_authorityID+"\" is not a valid id!");

		if (authorityType == null)
			throw new IllegalArgumentException("authorityType must not be null!");

		this.organisationID = organisationID;
		this.authorityID = _authorityID;
		this.authorityType = authorityType;
		this.name = new AuthorityName(this);
		this.description = new AuthorityDescription(this);
		this.securingAuthorityTypeID = authorityType.getSecuringAuthorityTypeID().toString(); // should always be the same - and never null
		this.managedBy = null;
	}

	/**
	 * @return the organisationID.
	 */
	public String getOrganisationID()
	{
		return organisationID;
	}

	/**
	 * @return the authorityID.
	 */
	public String getAuthorityID() {
		return authorityID;
	}

	public AuthorityType getAuthorityType()
	{
		return authorityType;
	}

	/**
	 * @return Returns the name.
	 */
	public I18nText getName()
	{
		return this.name;
	}

	/**
	 * @return Returns the description.
	 */
	public I18nText getDescription()
	{
		return this.description;
	}

	public AuthorizedObjectRef createAuthorizedObjectRef(AuthorizedObject authorizedObject)
	{
		if (!this.getOrganisationID().equals(authorizedObject.getOrganisationID()))
			throw new IllegalArgumentException("this.organisationID != authorizedObject.organisationID! " + this + " " + authorizedObject);

		AuthorizedObjectRef authorizedObjectRef = _createAuthorizedObjectRef(authorizedObject, true);
		authorizedObjectRef.setVisible(true);
		return authorizedObjectRef;
	}

	public Collection<AuthorizedObjectRef> getAuthorizedObjectRefs() {
		return Collections.unmodifiableCollection(authorizedObjectRefs.values());
	}

	/**
	 * This method does nothing if the AuthorizedObjectRef already exists.
	 *
	 * @param authorizedObject
	 * @param visible If the AuthorizedObjectRef does not yet exist, whether it should be created visible.
	 * @return Returns the generated/found AuthorizedObjectRef.
	 */
	protected AuthorizedObjectRef _createAuthorizedObjectRef(AuthorizedObject authorizedObject, boolean visible)
	{
		Object authorizedObjectID = JDOHelper.getObjectId(authorizedObject);
		if (authorizedObjectID == null)
			throw new IllegalArgumentException("JDOHelper.getObjectId(authorizedObject) returned null! Is it persistent? " + authorizedObject);
		AuthorizedObjectRef authorizedObjectRef = authorizedObjectRefs.get(authorizedObjectID.toString());
		if (authorizedObjectRef == null) {
			SecurityChangeController.getInstance().fireSecurityChangeEvent_pre_Authority_createAuthorizedObjectRef(this, authorizedObject);

			if (authorizedObject instanceof UserSecurityGroup)
				authorizedObjectRef = new UserSecurityGroupRef(this, authorizedObject, visible);
			else
				authorizedObjectRef = new AuthorizedObjectRef(this, authorizedObject, visible);

			authorizedObjectRefs.put(authorizedObjectRef.getAuthorizedObjectID(), authorizedObjectRef);
			authorizedObject._addAuthorizedObjectRef(authorizedObjectRef);

			SecurityChangeController.getInstance().fireSecurityChangeEvent_post_Authority_createAuthorizedObjectRef(this, authorizedObject, authorizedObjectRef);
		}
		return authorizedObjectRef;
	}

	public AuthorizedObjectRef getAuthorizedObjectRef(AuthorizedObjectID authorizedObjectID)
	{
		if (authorizedObjectID == null)
			throw new IllegalArgumentException("authorizedObjectID must not be null");

		return authorizedObjectRefs.get(authorizedObjectID.toString());
	}

	public AuthorizedObjectRef getAuthorizedObjectRef(AuthorizedObject authorizedObject)
	{
		AuthorizedObjectID authorizedObjectID = (AuthorizedObjectID) JDOHelper.getObjectId(authorizedObject);
		if (authorizedObjectID == null)
			throw new IllegalArgumentException("JDOHelper.getObjectId(authorizedObject) returned null! Is it persistent? " + authorizedObject);
		return authorizedObjectRefs.get(authorizedObjectID.toString());
	}

	/**
	 * This property is <code>null</code> for all {@link Authority}s that were not created by
	 * an automated import from another system or similar automated processes.
	 * <p>
	 * A non-<code>null</code> value indicates that the {@link Authority} is managed by some automated system
	 * and should not be changed by the users of the organisation.
	 * </p>
	 *
	 * @return The managed-by tag of this {@link Authority}, might be <code>null</code>.
	 */
	public String getManagedBy() {
		return managedBy;
	}

	/**
	 * Sets the managed-by flag for this {@link Authority} (see {@link #getManagedBy()}).
	 * <p>
	 * Note, that this property can only be set on attached instances of {@link Authority},
	 * attempts of setting this to detached instances will result in an {@link IllegalStateException}.
	 * </p>
	 * @param managedBy The managed-by flag to set.
	 */
	public void setManagedBy(String managedBy) {
		if (JDOHelper.isDetached(this))
			throw new IllegalStateException("setManagedBy can only be set for attached instances of " + this.getClass().getSimpleName());
		this.managedBy = managedBy;
	}

	/**
	 * Checks if the given {@link Authority} is tagged with a non-<code>null</code> managed-by property.
	 * This method will throw an {@link ManagedAuthorityModficationException}
	 * if the given {@link Authority} is found to be tagged with a manged-by flag.
	 *
	 * @param pm The {@link PersistenceManager} to use.
	 * @param authorityID The id of the {@link Authority} to check, this might also be <code>null</code> (the result of JDOHelper.getObjectId() of a new object).
	 */
	public static void assertAuthorityNotManaged(PersistenceManager pm, AuthorityID authorityID) {
		Authority authority = (Authority) pm.getObjectById(authorityID);
		if (authority.getManagedBy() != null)
			throw new ManagedAuthorityModficationException(authorityID, authority.getManagedBy());
	}

	/**
	 * Checks if the {@link Authority} is tagged with a non-<code>null</code> managed-by property.
	 * <p>
	 * If the {@link Authority} can't be found in the datastore <code>false</code> will be returned.
	 * This might occur if the given {@link AuthorityID} is of a {@link Authority} not yet in the given datastore,
	 * or <code>null</code>.
	 * </p>
	 *
	 * @param pm The {@link PersistenceManager} to use.
	 * @param authorityID The id of the {@link Authority} to check, this might also be <code>null</code> (the result of JDOHelper.getObjectId() of a new object).
	 * @return <code>true</code> if the given {@link Authority} is found to be tagged with the managed-by flag, <code>false</code> otherwise.
	 */
	public static boolean isAuthorityManaged(PersistenceManager pm, AuthorityID authorityID) {
		Authority authority = (Authority) pm.getObjectById(authorityID);
		return authority.getManagedBy() != null;
	}

	public void assertAuthorityNotManaged()
	{
		if (getManagedBy() != null)
			throw new ManagedAuthorityModficationException((AuthorityID) JDOHelper.getObjectId(this), getManagedBy());
	}

	public static <T extends Collection<?>> T filterSecuredObjectIDs(
			PersistenceManager pm,
			T objectIDs,
			JFireBasePrincipal principal,
			RoleID roleID,
			ResolveSecuringAuthorityStrategy strategy
	)
	{
		return filterSecuredObjectIDs(pm, objectIDs, UserID.create(principal), roleID, strategy);
	}

	public static <T extends Collection<?>> T filterIndirectlySecuredObjectIDs(
			PersistenceManager pm,
			T objectIDs,
			JFireBasePrincipal principal,
			RoleID roleID,
			ResolveSecuringAuthorityStrategy strategy
	)
	{
		return filterIndirectlySecuredObjectIDs(pm, objectIDs, UserID.create(principal), roleID, strategy);
	}

	/**
	 * This method filters those SecuredObjects out of the given collection where the user referenced by the
	 * principal parameter either doesn't have the given right in the {@link Authority} directly assigned
	 * to the object or, when the strategy is {@link ResolveSecuringAuthorityStrategy#organisation},
	 * where the current user doesn't have the given role in the organisation/global Authority.
	 *
	 * @param <T>
	 * 		The type of the typed collection of SecuredObjects to filter.
	 * @param pm
	 * 		The {@link PersistenceManager} to use in order to access the datastore.
	 * @param securedObjects
	 * 		The collection of {@link SecuredObject}s to filter.
	 * @param principal
	 * 		The principal pointing to the user to check the access rights for.
	 * @param roleID
	 * 		The id of the role to check for. If the Authority odes not contain a reference
	 * 		to this role the corresponding object will be filtered out.
	 * @param strategy
	 * 		The {@link ResolveSecuringAuthorityStrategy} to apply if no securing
	 * 		{@link Authority} is assigned to an object in the list.
	 * @return
	 * 		A new {@link Collection} of the same type as the input but with only those
	 * 		objects from the input collection where the user has the given role assigned
	 * 		in the corresponding {@link Authority}.
	 */
	public static <T extends Collection<? extends SecuredObject>> T filterSecuredObjects(
			PersistenceManager pm,
			T securedObjects,
			JFireBasePrincipal principal,
			RoleID roleID,
			ResolveSecuringAuthorityStrategy strategy
	)
	{
		return filterSecuredObjects(pm, securedObjects, UserID.create(principal), roleID, strategy);
	}

	public static <T extends Collection<? extends IndirectlySecuredObject>> T filterIndirectlySecuredObjects(
			PersistenceManager pm,
			T indirectlySecuredObjects,
			JFireBasePrincipal principal,
			RoleID roleID,
			ResolveSecuringAuthorityStrategy strategy
	)
	{
		return filterIndirectlySecuredObjects(pm, indirectlySecuredObjects, UserID.create(principal), roleID, strategy);
	}

	/**
	 * This method filters the ids of those SecuredObjects out of the given collection where the given
	 * user either doesn't have the given right in the {@link Authority} directly assigned
	 * to the object or, when the strategy is {@link ResolveSecuringAuthorityStrategy#organisation},
	 * where the user doesn't have the given role in the organisation/global Authority.
	 * <p>
	 * Note, that this method has to load all {@link SecuredObject} referenced with the given ids
	 * in order to be able to apply the filter.
	 * </p>
	 *
	 * @param <T>
	 * 		The type of the typed collection of SecuredObjectID to filter.
	 * @param pm
	 * 		The {@link PersistenceManager} to use in order to access the datastore.
	 * @param securedObjects
	 * 		The collection of {@link SecuredObject}s to filter.
	 * @param userID
	 * 		The userID to check access rihts for.
	 * @param roleID
	 * 		The id of the role to check for. If the Authority odes not contain a reference
	 * 		to this role the corresponding object will be filtered out.
	 * @param strategy
	 * 		The {@link ResolveSecuringAuthorityStrategy} to apply if no securing
	 * 		{@link Authority} is assigned to an object in the list.
	 * @return
	 * 		A new {@link Collection} of the same type as the input but with only those
	 * 		objects from the input collection where the user has the given role assigned
	 * 		in the corresponding {@link Authority}.
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Collection<?>> T filterSecuredObjectIDs(
			PersistenceManager pm,
			T objectIDs,
			UserID userID,
			RoleID roleID,
			ResolveSecuringAuthorityStrategy strategy
	)
	{
		List<SecuredObject> securedObjects = NLJDOHelper.getObjectList(pm, objectIDs, null);
		securedObjects = filterSecuredObjects(pm, securedObjects, userID, roleID, strategy);

		if (objectIDs instanceof Set)
			return (T) NLJDOHelper.getObjectIDSet(securedObjects);
		else
			return (T) NLJDOHelper.getObjectIDList(securedObjects);
	}

	@SuppressWarnings("unchecked")
	public static <T extends Collection<?>> T filterIndirectlySecuredObjectIDs(
			PersistenceManager pm,
			T objectIDs,
			UserID userID,
			RoleID roleID,
			ResolveSecuringAuthorityStrategy strategy
	)
	{
		List<IndirectlySecuredObject> indirectlySecuredObjects = NLJDOHelper.getObjectList(pm, objectIDs, null);
		indirectlySecuredObjects = filterIndirectlySecuredObjects(pm, indirectlySecuredObjects, userID, roleID, strategy);

		if (objectIDs instanceof Set)
			return (T) NLJDOHelper.getObjectIDSet(indirectlySecuredObjects);
		else
			return (T) NLJDOHelper.getObjectIDList(indirectlySecuredObjects);
	}

	/**
	 * This method filters those SecuredObjects out of the given collection where the given
	 * user either doesn't have the given right in the {@link Authority} directly assigned
	 * to the object or, when the strategy is {@link ResolveSecuringAuthorityStrategy#organisation},
	 * where the user doesn't have the given role in the organisation/global Authority.
	 *
	 * @param <T>
	 * 		The type of the typed collection of SecuredObjects to filter.
	 * @param pm
	 * 		The {@link PersistenceManager} to use in order to access the datastore.
	 * @param securedObjects
	 * 		The collection of {@link SecuredObject}s to filter.
	 * @param userID
	 * 		The userID to check access rihts for.
	 * @param roleID
	 * 		The id of the role to check for. If the Authority odes not contain a reference
	 * 		to this role the corresponding object will be filtered out.
	 * @param strategy
	 * 		The {@link ResolveSecuringAuthorityStrategy} to apply if no securing
	 * 		{@link Authority} is assigned to an object in the list.
	 * @return
	 * 		A new {@link Collection} of the same type as the input but with only those
	 * 		objects from the input collection where the user has the given role assigned
	 * 		in the corresponding {@link Authority}.
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Collection<? extends SecuredObject>> T filterSecuredObjects(
			PersistenceManager pm,
			T securedObjects,
			UserID userID,
			RoleID roleID,
			ResolveSecuringAuthorityStrategy strategy
	)
	{
		Collection<SecuredObject> result;
		if (securedObjects instanceof Set)
			result = new HashSet<SecuredObject>(securedObjects.size());
		else
			result = new ArrayList<SecuredObject>(securedObjects.size());

		Map<AuthorityID, Boolean> authorityID2allowed = new HashMap<AuthorityID, Boolean>();
		for (SecuredObject securedObject : securedObjects) {
			AuthorityID authorityID = securedObject.getSecuringAuthorityID();
			Boolean allowed = authorityID2allowed.get(authorityID);
			if (allowed == null) {
				allowed = resolveSecuringAuthority(pm, securedObject, strategy).containsRoleRef(userID, roleID) ? Boolean.TRUE : Boolean.FALSE;
				authorityID2allowed.put(authorityID, allowed);
			}
			if (allowed.booleanValue())
				result.add(securedObject);
		}

		return (T) result;
	}

	/**
	 * Just like {@link #filterSecuredObjects(PersistenceManager, Collection, UserID, RoleID, ResolveSecuringAuthorityStrategy)}
	 * this method filters those SecuredObjects out of the given collection where the given
	 * user either doesn't have the given right in the {@link Authority} directly assigned
	 * to the object or, when the strategy is {@link ResolveSecuringAuthorityStrategy#organisation},
	 * where the user doesn't have the given role in the organisation/global Authority.
	 * <p>
	 * This method however operatates on instances of {@link IndirectlySecuredObject} that have to point to
	 * the {@link SecuredObject} that contains the access right definition.
	 * </p>
	 *
	 * @param <T>
	 * 		The type of the typed collection of {@link IndirectlySecuredObject} to filter.
	 * @param pm
	 * 		The {@link PersistenceManager} to use in order to access the datastore.
	 * @param indirectlySecuredObjects
	 * 		The collection of {@link IndirectlySecuredObject}s to filter.
	 * @param userID
	 * 		The userID to check access rihts for.
	 * @param roleID
	 * 		The id of the role to check for. If the Authority odes not contain a reference
	 * 		to this role the corresponding object will be filtered out.
	 * @param strategy
	 * 		The {@link ResolveSecuringAuthorityStrategy} to apply if no securing
	 * 		{@link Authority} is assigned to an object in the list.
	 * @return
	 * 		A new {@link Collection} of the same type as the input but with only those
	 * 		objects from the input collection where the user has the given role assigned
	 * 		in the corresponding {@link Authority}.
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Collection<? extends IndirectlySecuredObject>> T filterIndirectlySecuredObjects(
			PersistenceManager pm,
			T indirectlySecuredObjects,
			UserID userID,
			RoleID roleID,
			ResolveSecuringAuthorityStrategy strategy
	)
	{
		Collection<IndirectlySecuredObject> result;
		if (indirectlySecuredObjects instanceof Set)
			result = new HashSet<IndirectlySecuredObject>(indirectlySecuredObjects.size());
		else
			result = new ArrayList<IndirectlySecuredObject>(indirectlySecuredObjects.size());

		Map<AuthorityID, Boolean> authorityID2allowed = new HashMap<AuthorityID, Boolean>();
		for (IndirectlySecuredObject indirectlySecuredObject : indirectlySecuredObjects) {
			SecuredObject securedObject = indirectlySecuredObject.getSecuredObject();
			AuthorityID authorityID = securedObject.getSecuringAuthorityID();
			Boolean allowed = authorityID2allowed.get(authorityID);
			if (allowed == null) {
				allowed = resolveSecuringAuthority(pm, securedObject, strategy).containsRoleRef(userID, roleID) ? Boolean.TRUE : Boolean.FALSE;
				authorityID2allowed.put(authorityID, allowed);
			}
			if (allowed.booleanValue())
				result.add(indirectlySecuredObject);
		}

		return (T) result;
	}

	/**
	 * Convenience method delegating to {@link #assertContainsRoleRef(UserID, RoleID)}.
	 *
	 * @param principal the current user as it is accessible within EJBs (see {@link EJBContext#getCallerPrincipal()}).
	 * @param roleID the permission to check.
	 * @throws MissingRoleException if the specified permission is <b>not</b> granted to the specified user.
	 */
	public void assertContainsRoleRef(JFireBasePrincipal principal, RoleID roleID)
	throws MissingRoleException
	{
		assertContainsRoleRef(UserID.create(principal), roleID);
	}

	/**
	 * Check whether a certain permission (referenced by <code>roleID</code>) is granted to
	 * a certain {@link User} (referenced by <code>userID</code>). This method calls
	 * {@link #containsRoleRef(UserID, RoleID)} and, if the result is <code>false</code>,
	 * throws a {@link MissingRoleException}.
	 *
	 * @param userID the JDO object identifier of a certain {@link User}.
	 * @param roleID the JDO object identifier of a certain {@link Role}.
	 * @throws MissingRoleException if the specified permission is <b>not</b> granted to the specified user.
	 */
	public void assertContainsRoleRef(UserID userID, RoleID roleID)
	throws MissingRoleException
	{
		PersistenceManager pm = JDOHelper.getPersistenceManager(this);
		if (pm == null)
			pm = NLJDOHelper.getThreadPersistenceManager(); // this is necessary for the dummy-allow-all-Authority

		if (pm == null)
			throw new IllegalStateException("This instance of Authority is currently not persistent! Cannot obtain PersistenceManager!");

		if (!containsRoleRef(userID, roleID))
			throw new MissingRoleException(pm, userID, (AuthorityID) JDOHelper.getObjectId(this), roleID);
	}

	/**
	 * Convenience method delegating to {@link #containsRoleRef(UserID, RoleID)}.
	 *
	 * @param principal the current user as it is accessible within EJBs (see {@link EJBContext#getCallerPrincipal()}).
	 * @param roleID the permission to check.
	 * @return <code>true</code> if the user has the right within the scope of this <code>Authority</code> (either directly or indirectly), <code>false</code> if the right is not available to the specified user.
	 */
	public boolean containsRoleRef(JFireBasePrincipal principal, RoleID roleID)
	{
		return containsRoleRef(UserID.create(principal), roleID);
	}

	/**
	 * Find out whether the specified {@link User} (defined by the given <code>userID</code>) has the access right specified
	 * by the given <code>roleID</code> within the scope of this <code>Authority</code>. This means the user either has it directly,
	 * or by a group in which the user is a member or by the special user {@link User#USER_ID_OTHER}.
	 *
	 * @param userID the user-id for which to check the presence of the access right.
	 * @param roleID the id of the access right to check.
	 * @return <code>true</code> if the user has the right within the scope of this <code>Authority</code> (either directly or indirectly), <code>false</code> if the right is not available to the specified user.
	 */
	public boolean containsRoleRef(UserID userID, RoleID roleID)
	{
		// the system user has all access rights - hence we always return true for him
		if (this.organisationID.equals(userID.organisationID) && User.USER_ID_SYSTEM.equals(userID.userID))
			return true;

		AuthorizedObjectRef userRef = getAuthorizedObjectRef(UserLocalID.create(userID, userID.organisationID));

		// If the User has no AuthorizedObjectRef in this Authority, we check for the "_Other_" user.
		// Note, that a AuthorizedObjectRef exists, even if only a UserGroup containing the User has been added (and not the User directly). In this case, AuthorizedObjectRef.isVisible() will be false.
		if (userRef == null)
			userRef = getAuthorizedObjectRef(UserLocalID.create(this.organisationID, User.USER_ID_OTHER, this.organisationID));

		// There is no AuthorizedObjectRef at all, so the role is definitely not present.
		if (userRef == null)
			return false;

		return userRef.containsRoleRef(roleID);
	}

	public void destroyAuthorizedObjectRef(AuthorizedObject authorizedObject)
	{
		_destroyAuthorizedObjectRef(authorizedObject, true);
	}

	protected void _destroyAuthorizedObjectRef(AuthorizedObject authorizedObject, boolean force)
	{
		Object authorizedObjectID = JDOHelper.getObjectId(authorizedObject);
		if (authorizedObjectID == null)
			throw new IllegalArgumentException("JDOHelper.getObjectId(authorizedObject) returned null! Is it persistent? " + authorizedObject);
		AuthorizedObjectRef authorizedObjectRef = authorizedObjectRefs.get(authorizedObjectID.toString());

		if (authorizedObjectRef != null) {

			if (!force && authorizedObjectRef.isVisible())
				return;

			if (authorizedObjectRef.getUserSecurityGroupReferenceCount() > 0) {
				authorizedObjectRef.setVisible(false);
				return; // TODO if force is true, shouldn't we continue instead of return? Marco.
			}

			SecurityChangeController.getInstance().fireSecurityChangeEvent_pre_Authority_destroyAuthorizedObjectRef(this, authorizedObject, authorizedObjectRef);

			// It is much cleaner to manually remove all the links. Even though, some of
			// the links are automatically removed, some are not and maybe jpox will run
			// into trouble if sth. is deleted in the database, but not in its cache.
			for (Iterator<RoleGroupRef> it = new ArrayList<RoleGroupRef>(authorizedObjectRef.getRoleGroupRefs()).iterator(); it.hasNext(); ) {
				RoleGroupRef roleGroupRef = it.next();
				authorizedObjectRef.removeRoleGroupRef(roleGroupRef);
			}
			authorizedObjectRef.getAuthorizedObject()._removeAuthorizedObjectRef(authorizedObjectRef);

			// TODO WORKAROUND DATANUCLEUS - need to force loading - otherwise changes are lost
			authorizedObjectRefs.entrySet().iterator();
			// WORKAROUND END

			authorizedObjectRefs.remove(authorizedObjectID.toString()); // jpox will automatically delete the authorizedObjectRef by this call.

			SecurityChangeController.getInstance().fireSecurityChangeEvent_post_Authority_destroyAuthorizedObjectRef(this, authorizedObject);
		}
	}

	public void destroyRoleGroupRef(RoleGroup roleGroup)
	{
		destroyRoleGroupRef(roleGroup.getRoleGroupID());
	}
	public void destroyRoleGroupRef(String roleGroupID)
	{
		RoleGroupRef roleGroupRef = roleGroupRefs.get(roleGroupID);
		if (roleGroupRef != null) {
			// It is much cleaner to manually remove all the links. Even though, some of
			// the links are automatically removed, some are not and maybe jpox will run
			// into trouble if sth. is deleted in the database, but not in its cache.
			for (Iterator<AuthorizedObjectRef> it = new HashSet<AuthorizedObjectRef>(roleGroupRef.getAuthorizedObjectRefs()).iterator(); it.hasNext(); ) {
				AuthorizedObjectRef userRef = it.next();
				userRef.removeRoleGroupRef(roleGroupRef);
			}
			roleGroupRef.getRoleGroup()._removeRoleGroupRef(authorityID);
			roleGroupRefs.remove(roleGroupID); // jpox will automatically delete the roleGroupRef by this call.
		}
	}

	public RoleGroupRef createRoleGroupRef(RoleGroup roleGroup)
	{
		RoleGroupRef roleGroupRef = roleGroupRefs.get(roleGroup.getRoleGroupID());
		if (roleGroupRef == null) {
			roleGroupRef = new RoleGroupRef(this, roleGroup);
			roleGroup._addRoleGroupRef(roleGroupRef);
			roleGroupRefs.put(roleGroupRef.getRoleGroupID(), roleGroupRef);
		}
		return roleGroupRef;
	}

	public Collection<RoleGroupRef> getRoleGroupRefs()
	{
		return Collections.unmodifiableCollection(roleGroupRefs.values());
	}

	public RoleGroupRef getRoleGroupRef(RoleGroup roleGroup)
	{
		return getRoleGroupRef(roleGroup.getRoleGroupID());
	}

	public RoleGroupRef getRoleGroupRef(RoleGroupID roleGroupID)
	{
		return getRoleGroupRef(roleGroupID.roleGroupID);
	}

	public RoleGroupRef getRoleGroupRef(String roleGroupID)
	{
		return roleGroupRefs.get(roleGroupID);
	}

	private static Authority dummyAuthorityAllow = null;

	private static Authority getDummyAuthorityAllow(PersistenceManager pm)
	{
		// The following registration of the current thread's PM should already
		// be done by the JFire framework (Lookup.getPersistenceManager). However, if
		// you obtain a PM directly from a PersistenceManagerFactory, this does not happen.
		// Therefore, we ensure here it is set.
		// The Thread-PM is necessary to execute assertContainsRoleRef.
		NLJDOHelper.setThreadPersistenceManager(pm);

		if (dummyAuthorityAllow == null) { // no need to synchronize since it doesn't hurt if multiple threads create it
			AuthorityType dummyAuthorityTypeAllow = new AuthorityType(ResolveSecuringAuthorityStrategy.class.getName() + '.' + ResolveSecuringAuthorityStrategy.allow.name())
			{
				private static final long serialVersionUID = 1L;

				@Override
				public void jdoPreStore() {
					throw new UnsupportedOperationException("This instance must not be persisted to the datastore! " + this);
				}
			};

			dummyAuthorityAllow = new Authority(
					Organisation.DEV_ORGANISATION_ID,
					ResolveSecuringAuthorityStrategy.class.getName() + '.' + ResolveSecuringAuthorityStrategy.allow.name(),
					dummyAuthorityTypeAllow
			)
			{
				private static final long serialVersionUID = 1L;

				@Override
				public boolean containsRoleRef(JFireBasePrincipal principal, RoleID roleID) {
					return true;
				}
				@Override
				public boolean containsRoleRef(UserID userID, RoleID roleID) {
					return true;
				}
				@Override
				public void jdoPreStore() {
					throw new UnsupportedOperationException("This instance must not be persisted to the datastore! " + this);
				}
			};
		}

		return dummyAuthorityAllow;
	}

	/**
	 * Get the <code>Authority</code> (i.e. the access right configuration) that has been assigned to
	 * a certain {@link SecuredObject}. This method never returns <code>null</code>.
	 * <p>
	 * If there is no <code>Authority</code> assigned
	 * (i.e. {@link SecuredObject#getSecuringAuthorityID()} returns <code>null</code>), the
	 * {@link ResolveSecuringAuthorityStrategy} decides about what <code>Authority</code>
	 * will be returned.
	 * </p>
	 * <p>
	 * If a certain permission (a {@link RoleID}) was already checked on the EJB method level
	 * (i.e. had the JavaEE server check the authorization), it is a good idea to use
	 * {@link ResolveSecuringAuthorityStrategy#allow}, because it's not necessary to perform the same
	 * check twice. However, if the permission was not yet checked on the EJB method level, you should pass
	 * {@link ResolveSecuringAuthorityStrategy#organisation}.
	 * </p>
	 *
	 * @param pm the door to the datastore.
	 * @param securedObject the object that might have an individual access right configuration assigned.
	 * @param strategy the strategy to follow when there is no individual access right configuration assigned to <code>securedObject</code>.
	 * @return the access right configuration for the specified <code>securedObject</code> - never <code>null</code>.
	 */
	public static Authority resolveSecuringAuthority(PersistenceManager pm, SecuredObject securedObject, ResolveSecuringAuthorityStrategy strategy)
	{
		if (pm == null)
			throw new IllegalArgumentException("PersistenceManager must not be null!");

		Authority securingAuthorityForSecuredObject = null;

		if (securedObject.getSecuringAuthorityID() != null)
			securingAuthorityForSecuredObject = (Authority) pm.getObjectById(securedObject.getSecuringAuthorityID());

		if (securingAuthorityForSecuredObject != null)
			return securingAuthorityForSecuredObject;

		if (securedObject instanceof Authority) {
			// Only if the SecuredObject is an authority, it is possible to assign the SecuringAuthority to the AuthorityType
			// as a fallback, in case Authority.securingAuthority is not assigned. This does not apply to any other
			// implementation of SecuredObject - all other SecuredObjects directly fall back to the OrganisationAuthority.
			AuthorityID id = ((Authority)securedObject).getAuthorityType().getSecuringAuthorityID();
			if (id != null)
				securingAuthorityForSecuredObject = (Authority) pm.getObjectById(id);;

			if (securingAuthorityForSecuredObject != null)
				return securingAuthorityForSecuredObject;
		}

		if (ResolveSecuringAuthorityStrategy.allow == strategy)
			return getDummyAuthorityAllow(pm);

		securingAuthorityForSecuredObject = Authority.getOrganisationAuthority(pm);
		return securingAuthorityForSecuredObject;
	}

	@Override
	public AuthorityID getSecuringAuthorityID() {
		return (AuthorityID) ObjectIDUtil.createObjectID(securingAuthorityID);
	}
	@Override
	public void setSecuringAuthorityID(AuthorityID securingAuthorityID) {
		// TODO this needs to be checked in a jdo-pre-store and pre-attach method
//		if (securingAuthority != null) {
//			// check if the AuthorityType is correct.
//			if (!securingAuthority.getAuthorityType().equals(securingAuthorityType))
//				throw new IllegalArgumentException("securingAuthority.authorityType does not match this.authorityType! securingAuthority: " + JDOHelper.getObjectId(securingAuthority) + " this: " + JDOHelper.getObjectId(this));
//		}

		this.securingAuthorityID = securingAuthorityID == null ? null : securingAuthorityID.toString();
	}
	@Override
	public AuthorityTypeID getSecuringAuthorityTypeID() {
		// Return that AuthorityType that controls the Authority in the field 'securingAuthority'.
		// This is very likely different from 'this.authorityType'. There is the rule that
		// this.securingAuthority.authorityType == this.securingAuthorityType. Therefore,
		// this method must return the AuthorityType containing the role grous for
		// "access rights management in general".

		return (AuthorityTypeID) ObjectIDUtil.createObjectID(securingAuthorityTypeID);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) return true;
		if (obj == null) return false;
		if (obj.getClass() != this.getClass()) return false;
		Authority o = (Authority) obj;
		return Util.equals(this.organisationID, o.organisationID) && Util.equals(this.authorityID, o.authorityID);
	}

	@Override
	public int hashCode() {
		return Util.hashCode(organisationID) * 31 + Util.hashCode(authorityID);
	}

	@Override
	public String toString() {
		return this.getClass().getName() + '@' + Integer.toHexString(System.identityHashCode(this)) + '[' + organisationID + ',' + authorityID + ']';
	}

	@Override
	public void jdoPreStore() {
		// nothing to do - subclasses override this method - see getDummyAuthorityAllow()
	}

	/**
	 * Removes all information of this <b>detached</b> <code>Authority</code> that is not
	 * directly bound to the given user.
	 *
	 * @param userLocal the user for which to keep data - all other data will be removed.
	 */
	public void removeSecretDataAfterDetachmentForSingleUser(UserLocal userLocal)
	{
		if (!JDOHelper.isDetached(this))
			throw new IllegalStateException("This instance is not detached!");

		try {
			roleGroupRefs.clear();
			throw new IllegalStateException("this.roleGroupRefs has been detached! It should not!");
		} catch (JDODetachedFieldAccessException x) {
			// fine - that's what we expect
		}

		for (Iterator<Map.Entry<String, AuthorizedObjectRef>> it = authorizedObjectRefs.entrySet().iterator(); it.hasNext(); ) {
			Map.Entry<String, AuthorizedObjectRef> me = it.next();
			AuthorizedObjectRef authorizedObjectRef = me.getValue();

			if (userLocal.equals(authorizedObjectRef.getAuthorizedObject())) {
				// password is nulled in detach-callback of UserLocal.
//				((UserLocal)authorizedObjectRef.getAuthorizedObject()).setPassword(null);
//				if (logger.isDebugEnabled()){
//					logger.debug("Setting password to null for UserLocal "+userLocal+" which is the authorizedObject of AuthorizedObjectRef "+authorizedObjectRef);
//				}
			}
			else
				it.remove();
		}
	}

	public Collection<UserLocal> getUserLocalsManagedViaOther()
	{
		PersistenceManager pm = JDOHelper.getPersistenceManager(this);
		if (pm == null)
			throw new IllegalStateException("This method can only be called while the Authority is attached to the datastore!");

		UserLocalID otherUserLocalID = UserLocalID.create(organisationID, User.USER_ID_OTHER, organisationID);
		String otherUserLocalIDString = otherUserLocalID.toString();

		if (!authorizedObjectRefs.containsKey(otherUserLocalIDString)) // the other user is not in the authority, thus it cannot affect any user in the context of this authority.
			return Collections.emptySet();

//		Query q = pm.newQuery(UserLocal.class);
//		q.declareVariables(AuthorizedObjectRef.class.getName() + " notContainedAuthorizedObjectRef");
//		q.setFilter(
////				"JDOHelper.getObjectId(this) != :otherUserLocalID && " + // this line causes the opposite: only the user other is returned! // TODO DataNucleus WORKAROUND - re-enable this line!
//				"notContainedAuthorizedObjectRef.userLocal == this && " +
//				"!:authority.authorizedObjectRefs.containsValue(notContainedAuthorizedObjectRef)"
//		);
//		Map<String, Object> params = new HashMap<String, Object>(2);
//		params.put("authority", this);
//		params.put("otherUserLocalID", otherUserLocalID);
//		Collection<?> c = (Collection<?>) q.executeWithMap(params);
//		Collection<UserLocal> res = CollectionUtil.castCollection(c);

		// TODO DataNucleus WORKAROUND begin - the other user is part of the result set - even though the query doesn't include it!
//		res = new HashSet<UserLocal>(res);
//		UserLocal otherUserLocal = (UserLocal) pm.getObjectById(otherUserLocalID);
//		res.remove(otherUserLocal);

		// There seems to be an issue with NOT contains(...) - as used in !authority.authorized... above
		Query q = pm.newQuery(UserLocal.class);
		Collection<?> c = (Collection<?>) q.execute();
		Collection<UserLocal> res = CollectionUtil.castCollection(c);
		res = new HashSet<UserLocal>(res);
		for (AuthorizedObjectRef authorizedObjectRef : authorizedObjectRefs.values()) {
			res.remove(authorizedObjectRef.getAuthorizedObject());
		}
		// TODO DataNucleus WORKAROUND end

		return res;
	}
}
