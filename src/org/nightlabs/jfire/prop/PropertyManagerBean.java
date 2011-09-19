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

package org.nightlabs.jfire.prop;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.jdo.JDOHelper;
import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import org.apache.log4j.Logger;
import org.nightlabs.i18n.MultiLanguagePropertiesBundle;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jdo.moduleregistry.UpdateHistoryItem;
import org.nightlabs.jdo.moduleregistry.UpdateNeededHandle;
import org.nightlabs.jfire.base.BaseSessionBeanImpl;
import org.nightlabs.jfire.base.JFireBaseEAR;
import org.nightlabs.jfire.base.expression.IExpression;
import org.nightlabs.jfire.config.ConfigSetup;
import org.nightlabs.jfire.config.UserConfigSetup;
import org.nightlabs.jfire.config.id.ConfigModuleInitialiserID;
import org.nightlabs.jfire.layout.LegalEntitySearchEditLayoutIntialiser;
import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.jfire.person.Person;
import org.nightlabs.jfire.person.PersonSearchConfigModule;
import org.nightlabs.jfire.person.PersonStruct;
import org.nightlabs.jfire.prop.cache.DetachedPropertySetCache;
import org.nightlabs.jfire.prop.config.PropertySetEditLayoutConfigModule;
import org.nightlabs.jfire.prop.config.PropertySetFieldBasedEditConstants;
import org.nightlabs.jfire.prop.config.PropertySetFieldBasedEditLayoutConfigModule;
import org.nightlabs.jfire.prop.config.PropertySetFieldBasedEditLayoutUseCase;
import org.nightlabs.jfire.prop.config.id.PropertySetFieldBasedEditLayoutUseCaseID;
import org.nightlabs.jfire.prop.datafield.DateDataField;
import org.nightlabs.jfire.prop.datafield.I18nTextDataField;
import org.nightlabs.jfire.prop.datafield.ImageDataField;
import org.nightlabs.jfire.prop.datafield.MultiSelectionDataField;
import org.nightlabs.jfire.prop.datafield.NumberDataField;
import org.nightlabs.jfire.prop.datafield.PhoneNumberDataField;
import org.nightlabs.jfire.prop.datafield.RegexDataField;
import org.nightlabs.jfire.prop.datafield.SelectionDataField;
import org.nightlabs.jfire.prop.datafield.TextDataField;
import org.nightlabs.jfire.prop.datafield.TimePatternSetDataField;
import org.nightlabs.jfire.prop.id.PropertySetID;
import org.nightlabs.jfire.prop.id.StructFieldID;
import org.nightlabs.jfire.prop.id.StructID;
import org.nightlabs.jfire.prop.id.StructLocalID;
import org.nightlabs.jfire.prop.search.PropSearchFilter;
import org.nightlabs.jfire.prop.search.config.PropertySetSearchEditLayoutConfigModule;
import org.nightlabs.jfire.prop.structfield.DateStructField;
import org.nightlabs.jfire.prop.structfield.I18nTextStructField;
import org.nightlabs.jfire.prop.structfield.ImageStructField;
import org.nightlabs.jfire.prop.structfield.MultiSelectionStructField;
import org.nightlabs.jfire.prop.structfield.NumberStructField;
import org.nightlabs.jfire.prop.structfield.PhoneNumberStructField;
import org.nightlabs.jfire.prop.structfield.RegexStructField;
import org.nightlabs.jfire.prop.structfield.SelectionStructField;
import org.nightlabs.jfire.prop.structfield.TextStructField;
import org.nightlabs.jfire.prop.structfield.TimePatternSetStructField;
import org.nightlabs.jfire.prop.view.PropertySetTableViewerConfiguration;
import org.nightlabs.jfire.prop.view.PropertySetViewerConfiguration;
import org.nightlabs.jfire.security.Authority;
import org.nightlabs.jfire.security.ResolveSecuringAuthorityStrategy;
import org.nightlabs.jfire.security.id.RoleID;
import org.nightlabs.util.CollectionUtil;
import org.nightlabs.util.Stopwatch;
import org.nightlabs.util.Util;

/**
 * @author Tobias Langner <!-- tobias[dot]langner[at]nightlabs[dot]de -->
 *
 * TODO: Manage access rights for Properties on object-link-type basis or maybe on StructLocal basis? Not all PropertySets are linked to an object!
 * See https://www.jfire.org/modules/bugs/view.php?id=896
 *
 * @ejb.bean name="jfire/ejb/JFireBaseBean/PropertyManager"
 *           jndi-name="jfire/ejb/JFireBaseBean/PropertyManager"
 *           type="Stateless"
 *           transaction-type="Container"
 *
 * @ejb.util generate="physical"
 * @ejb.transaction type="Required"
 */
@TransactionAttribute(TransactionAttributeType.REQUIRED)
@TransactionManagement(TransactionManagementType.CONTAINER)
@Stateless
public class PropertyManagerBean extends BaseSessionBeanImpl implements PropertyManagerRemote
{
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(PropertyManagerBean.class);

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.prop.PropertyManagerRemote#getFullStruct(java.lang.String, java.lang.String, java.lang.String, java.lang.String[], int)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_Guest_")
	@Override
	public Struct getFullStruct(
			String organisationID, String linkClass, String structScope, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = createPersistenceManager();
		try {
			if (fetchGroups != null) {
				pm.getFetchPlan().setGroups(fetchGroups);
				HashSet<String> fetchGroupSet = CollectionUtil.array2HashSet(fetchGroups);
				if (fetchGroupSet.contains(IStruct.FETCH_GROUP_ISTRUCT_FULL_DATA))
					fetchGroupSet.add(IExpression.FETCH_GROUP_IEXPRESSION_FULL_DATA);
				pm.getFetchPlan().setGroups(fetchGroupSet.toArray(new String[fetchGroupSet.size()]));
			}
			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			Struct ps = Struct.getStruct(organisationID, linkClass, structScope, pm);
			Struct result = pm.detachCopy(ps);
			return result;
		} finally {
			pm.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.prop.PropertyManagerRemote#getFullStruct(org.nightlabs.jfire.prop.id.StructID, java.lang.String[], int)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_Guest_")
	@Override
	public Struct getFullStruct(StructID structID, String[] fetchGroups, int maxFetchDepth)
	{
		return getFullStruct(structID.organisationID, structID.linkClass, structID.structScope, fetchGroups, maxFetchDepth);
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.prop.PropertyManagerRemote#getFullStructLocal(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String[], int)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_Guest_")
	@Override
	public StructLocal getFullStructLocal(
			String organisationID, String linkClass, String structScope, String structLocalScope, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = createPersistenceManager();
		try {
			if (fetchGroups != null) {
				HashSet<String> fetchGroupSet = CollectionUtil.array2HashSet(fetchGroups);
				if (fetchGroupSet.contains(IStruct.FETCH_GROUP_ISTRUCT_FULL_DATA)) {
					fetchGroupSet.add(IExpression.FETCH_GROUP_IEXPRESSION_FULL_DATA);
				}
				pm.getFetchPlan().setGroups(fetchGroupSet);
			}
			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			StructLocal ps = StructLocal.getStructLocal(pm, organisationID, linkClass, structScope, structLocalScope);
			StructLocal result = pm.detachCopy(ps);
			return result;
		} finally {
			pm.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.prop.PropertyManagerRemote#getFullStructLocal(org.nightlabs.jfire.prop.id.StructLocalID, java.lang.String[], int)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_Guest_")
	@Override
	public StructLocal getFullStructLocal(StructLocalID structLocalID, String[] fetchGroups, int maxFetchDepth)
	{
		return getFullStructLocal(
				structLocalID.organisationID, structLocalID.linkClass,
				structLocalID.structScope, structLocalID.structLocalScope,
				fetchGroups, maxFetchDepth);
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.prop.PropertyManagerRemote#getPropertySet(org.nightlabs.jfire.prop.id.PropertySetID, java.lang.String[], int)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
//	@RolesAllowed("org.nightlabs.jfire.prop.seePropertySet")
	@RolesAllowed("_Guest_")
	@Override
	public PropertySet getPropertySet(PropertySetID propID, String[] fetchGroups, int maxFetchDepth) {
		PersistenceManager pm = this.createPersistenceManager();
		try {
			pm.getExtent(PropertySet.class, true);
			PropertySet prop = (PropertySet) pm.getObjectById(propID, true);

			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);

			PropertySet result = pm.detachCopy(prop);

			/* filter secured objects (begin) */
			final List<PropertySet> resultSet = new ArrayList<PropertySet>();
			resultSet.add(result);
			final List<PropertySet> result_ = Authority.filterSecuredObjects(pm, resultSet, getPrincipal(), RoleConstants.seePropertySet, ResolveSecuringAuthorityStrategy.allow);
			if (result_.size() == 1)
				return result_.get(0);
			return null;
			/* filter secured objects (end) */
		} finally {
			pm.close();
		}
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_Guest_")
	@Override
	public Set<PropertySet> getDetachedTrimmedPropertySets(Set<PropertySetID> propIDs, Set<StructFieldID> structFieldIDs, String[] fetchGroups, int maxFetchDepth) {
		PersistenceManager pm = this.createPersistenceManager();
		try {
			return getDetachedTrimmedPropertySets(pm, propIDs, structFieldIDs, fetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}
	}

	private Set<PropertySet> getDetachedTrimmedPropertySets(PersistenceManager pm, Set<PropertySetID> propIDs, Set<StructFieldID> structFieldIDs, String[] fetchGroups, int maxFetchDepth) {
		
		Set<String> detachFetchGroups = new HashSet<String>();
		if (fetchGroups != null) {
			detachFetchGroups.addAll(Arrays.asList(fetchGroups));
		}
		if (!detachFetchGroups.contains(PropertySet.FETCH_GROUP_FULL_DATA) && !detachFetchGroups.contains(PropertySet.FETCH_GROUP_DATA_FIELDS)) {
			// We don't need FULL_DATA to trim, but FULL_DATA includes dataFields, so if FULL_DATA is set that is more than sufficient.
			detachFetchGroups.add(PropertySet.FETCH_GROUP_DATA_FIELDS);
		}
		
		Set<PropertySet> detachedPropertySets = getCachedAndAuthorityFilteredDetachedPropertySets(
				pm, propIDs, detachFetchGroups.toArray(new String[detachFetchGroups.size()]), maxFetchDepth, RoleConstants.seePropertySet);
		for (PropertySet propertySet : detachedPropertySets) {
			PropertySet.trimDetachedPropertySet(propertySet, structFieldIDs);
		}
		return detachedPropertySets;
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.prop.PropertyManagerRemote#searchPropertySets(org.nightlabs.jfire.prop.search.PropSearchFilter, java.lang.String[], int)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_Guest_")
	@Override
	public Set<?> searchPropertySets(PropSearchFilter propSearchFilter, String[] fetchGroups, int maxFetchDepth) {
		PersistenceManager pm = this.createPersistenceManager();
		try {
			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);

			propSearchFilter.setPersistenceManager(pm);

			long time = System.currentTimeMillis();
			Collection<?> props = propSearchFilter.getResult();
			if (logger.isDebugEnabled())
				logger.debug("searchPropertySets: Query returned " + props.size() + " PropertySets and took " + Util.getTimeDiffString(time));

//			Set result = NLJDOHelper.getDetachedQueryResultAsSet(pm, props);
//			return (Set<?>) Authority.filterSecuredObjects(pm, result, getPrincipal(), RoleConstants.seePropertySet, ResolveSecuringAuthorityStrategy.allow);

//			Set detachedProps = NLJDOHelper.getDetachedQueryResultAsSet(pm, props);
//			Set result = new HashSet();
//			Set filteredProps = new HashSet();
//			// because this method can also return other objects then PropertySet we only filter PropertySets and in case it is no PropertySet we dont filter at all.
//			for (Object detachedProp : detachedProps) {
//				if (detachedProp instanceof PropertySet) {
//					filteredProps.add(detachedProp);
//				}
//				else {
//					result.add(detachedProp);
//				}
//			}
//			result.addAll(Authority.filterSecuredObjects(pm, filteredProps, getPrincipal(), RoleConstants.seePropertySet, ResolveSecuringAuthorityStrategy.allow));
//			return result;

			DetachedPropertySetCache detachedPropertySetCache = DetachedPropertySetCache.getInstance(pm);

			time = System.currentTimeMillis();
			Set<Object> result = new HashSet<Object>();
			Set<PropertySet> detachedProps = new HashSet<PropertySet>();
			// because this method can also return other objects then PropertySet we only filter PropertySets and in case it is no PropertySet we dont filter at all.
			for (Object prop : props) {
				if (prop instanceof PropertySet) {
					PropertySetID propertySetID = (PropertySetID) JDOHelper.getObjectId(prop);
					PropertySet detachedProp = detachedPropertySetCache.get(propertySetID, fetchGroups, maxFetchDepth);
					detachedProps.add(detachedProp);
				}
				else {
					result.add(prop);
				}
			}
			if (logger.isDebugEnabled())
				logger.debug("searchPropertySets: Iterating " + props.size() + " PropertySets for 1st stage of postprocessing took " + Util.getTimeDiffString(time));

			// result is not yet detached => detach now
			if (!result.isEmpty()) {
				time = System.currentTimeMillis();
				result = new HashSet<Object>(pm.detachCopyAll(result));
				if (logger.isDebugEnabled())
					logger.debug("searchPropertySets: Detaching " + result.size() + " objects (not instances of PropertySet) took " + Util.getTimeDiffString(time));
			}

			// Filter the detached PropertySet instances.
			time = System.currentTimeMillis();
			Set<PropertySet> filteredDetachedProps = Authority.filterSecuredObjects(
					pm, detachedProps, getPrincipal(),
					RoleConstants.seePropertySet, ResolveSecuringAuthorityStrategy.allow
			);
			if (logger.isDebugEnabled())
				logger.debug("searchPropertySets: Filtering " + detachedProps.size() + " PropertySets returned " + filteredDetachedProps.size() + " filtered elements and took " + Util.getTimeDiffString(time));

			// Add the filtered detached PropertySets.
			result.addAll(filteredDetachedProps);

			// And return the result.
			return result;
		} finally {
			pm.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.prop.PropertyManagerRemote#searchPropertySetIDs(org.nightlabs.jfire.prop.search.PropSearchFilter)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_Guest_")
	@Override
	public Set<PropertySetID> searchPropertySetIDs(PropSearchFilter propSearchFilter) {
		PersistenceManager pm = this.createPersistenceManager();
		try {
			propSearchFilter.setPersistenceManager(pm);
			long start = System.currentTimeMillis();
			Collection<?> props = propSearchFilter.getResult();
			if (logger.isDebugEnabled()) {
				long duration = System.currentTimeMillis() - start;
				logger.debug("propSearchFilter.getResult() took "+duration+" ms!");
			}

			Set<PropertySetID> result = new HashSet<PropertySetID>();
			for (Object element : props) {
				if (element instanceof PropertySet) {
					PropertySet propertySet = (PropertySet) element;
					result.add((PropertySetID) JDOHelper.getObjectId(propertySet));
				}
			}
//			return result;
			return Authority.filterSecuredObjectIDs(pm, result, getPrincipal(), RoleConstants.seePropertySet, ResolveSecuringAuthorityStrategy.allow);
		} finally {
			pm.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.prop.PropertyManagerRemote#storePropertySet(org.nightlabs.jfire.prop.PropertySet, boolean, java.lang.String[], int)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_Guest_")
	@Override
	public PropertySet storePropertySet(PropertySet propertySet, boolean get, String[] fetchGroups, int maxFetchDepth) {
		PersistenceManager pm = createPersistenceManager();
		try {
			/* test if user is allowed to store the property set (begin) */
			// TODO is this sufficient?
			if (JDOHelper.isDetached(propertySet)) {
				PropertySet propertySet_ = (PropertySet) pm.getObjectById(JDOHelper.getObjectId(propertySet));
				Authority.resolveSecuringAuthority(pm, propertySet_, ResolveSecuringAuthorityStrategy.allow)
					.assertContainsRoleRef(getPrincipal(), RoleConstants.editPropertySet);
			}
			/* test if user is allowed to store the property set (end) */
			return NLJDOHelper.storeJDO(pm, propertySet, get, fetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.prop.PropertyManagerRemote#storeStruct(org.nightlabs.jfire.prop.IStruct, boolean, java.lang.String[], int)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_Guest_")
	@Override
	public IStruct storeStruct(IStruct struct, boolean get, String[] fetchGroups, int maxFetchDepth) {
		PersistenceManager pm = createPersistenceManager();
		try {
			IStruct persistenStruct = null;
			if (struct instanceof Struct) {
				if (hasRootOrganisation() && !getOrganisationID().equals(getRootOrganisationID()))
					throw new IllegalStateException("Structs can only be stored by the root organisation.");

				Struct modifiedStruct = (Struct) struct;
				Struct currentStruct = (Struct) pm.getObjectById(modifiedStruct.getID());
				modifiedStruct = applyStructuralChanges(modifiedStruct, currentStruct);
				modifiedStruct = pm.makePersistent(modifiedStruct);
				persistenStruct = modifiedStruct;
			} else if (struct instanceof StructLocal) {
				StructLocal modifiedStructLocal = (StructLocal) struct;
				pm.getFetchPlan().setGroup(IStruct.FETCH_GROUP_ISTRUCT_FULL_DATA);
				pm.getFetchPlan().setMaxFetchDepth(NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT);
				StructLocal unmodifiedStructLocal = pm.detachCopy((StructLocal) pm.getObjectById(modifiedStructLocal.getID()));

				// Replace the struct with all its struct blocks with the unmodified version from the datastore to prevent
				// accidental or malicious modifications through the StructLocal
				modifiedStructLocal.setStruct(unmodifiedStructLocal.getStruct());

				// Somehow, after this call, the modified structure comes back, this may be a JPOX bug or a general
				// misconception by myself.
				// I stated this problem in http://www.jpox.org/servlet/forum/viewthread?thread=4967&lastpage=yes
				modifiedStructLocal = pm.makePersistent(modifiedStructLocal);

				persistenStruct = modifiedStructLocal;
			} else {
				throw new IllegalArgumentException("Given struct must be of type Struct or StructLocal.");
			}
			if (get) {
				pm.getFetchPlan().setGroups(fetchGroups);
				pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
				return pm.detachCopy(persistenStruct);
			} else {
				return null;
			}
		} finally {
			pm.close();
		}
	}

	/**
	 * Checks if the changes reflected by <code>modifiedStruct</code> are
	 * adequate, i.e. that they did not touch blocks that are owned by the dev
	 * organisation and thus aren't modifiable. Changes to such blocks are
	 * discarded. The "corrected" structure containing only legal changes is
	 * returned.
	 *
	 * @param modifiedStruct
	 *          The structure how it should look like after the modification.
	 * @param currentStruct
	 *          The structure at its current state.
	 * @return The modified structure with all all illegal modifications been
	 *         revoked.
	 */
	protected Struct applyStructuralChanges(Struct modifiedStruct, Struct currentStruct) {
		if (!modifiedStruct.getID().equals(currentStruct.getID()))
			throw new IllegalArgumentException("Cannot apply changes because structure IDs are not the same.");

		List<StructBlock> modifiedBlocks, currentBlocks;
		modifiedBlocks = modifiedStruct.getStructBlocks();
		currentBlocks = currentStruct.getStructBlocks();

		for (StructBlock block : currentBlocks) {
			if (block.getStructBlockOrganisationID().equals(Organisation.DEV_ORGANISATION_ID)) {
				int index = modifiedBlocks.indexOf(block);
				if (index != -1)
					modifiedBlocks.set(index, block);
				else
					modifiedBlocks.add(block);
			}
		}

		return modifiedStruct;
	}

	/**
	 * Retrieves the {@link StructID}s of all {@link java.sql.Struct}s in the
	 * current datastore.
	 *
	 * @return Collection of the {@link StructID}s of all available
	 *         {@link Struct}s.
	 */
	private Set<StructID> getAvailableStructIDs(PersistenceManager pm) {
		Set<StructID> structIDs;
		structIDs = NLJDOHelper.getDetachedQueryResultAsSet(pm, Struct.getAllStructIDs(pm));
		return structIDs;
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.prop.PropertyManagerRemote#getAvailableStructIDs()
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_Guest_")
	@Override
	public Collection<StructID> getAvailableStructIDs() {
		PersistenceManager pm = createPersistenceManager();
		try {
			return getAvailableStructIDs(pm);
		} finally {
			pm.close();
		}
	}

	/**
	 * Retrieves the {@link StructLocalID}s of all {@link StructLocal}s in the
	 * current datastore.
	 *
	 * @return Collection of the {@link StructLocalID}s of all available
	 *         {@link StructLocal}s.
	 */
	private Set<StructLocalID> getAvailableStructLocalIDs(PersistenceManager pm) {
		Set<StructLocalID> structLocalIDs;
		structLocalIDs = NLJDOHelper.getDetachedQueryResultAsSet(pm, StructLocal.getAllStructLocalIDs(pm));
		return structLocalIDs;
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.prop.PropertyManagerRemote#getAvailableStructLocalIDs()
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_Guest_")
	@Override
	public Collection<StructLocalID> getAvailableStructLocalIDs() {
		PersistenceManager pm = createPersistenceManager();
		try {
			return getAvailableStructLocalIDs(pm);
		} finally {
			pm.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.prop.PropertyManagerRemote#getPropertySets(java.util.Set, java.lang.String[], int)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_Guest_")
	@Override
	public Set<PropertySet> getPropertySets(Set<PropertySetID> propIDs, String[] fetchGroups, int maxFetchDepth) {
		PersistenceManager pm = createPersistenceManager();
		try {
			return getCachedAndAuthorityFilteredDetachedPropertySets(pm, propIDs, fetchGroups, maxFetchDepth, RoleConstants.seePropertySet);
		} finally {
			pm.close();
		}
	}

	private Set<PropertySet> getCachedAndAuthorityFilteredDetachedPropertySets(
			PersistenceManager pm, Set<PropertySetID> propIDs, String[] fetchGroups, int maxFetchDepth, RoleID roleID) {
		
		long time = System.currentTimeMillis();
//			Set<PropertySet> result = NLJDOHelper.getDetachedObjectSet(pm, propIDs, null, fetchGroups, maxFetchDepth);
		List<PropertySet> l = DetachedPropertySetCache.getInstance(pm).get(propIDs, fetchGroups, maxFetchDepth);

		if (logger.isDebugEnabled())
		logger.debug("getPropertySets: Detaching " + l.size() + " PropertySets took " + Util.getTimeDiffString(time));

		time = System.currentTimeMillis();
		Set<PropertySet> result = new HashSet<PropertySet>(l);
		if (logger.isDebugEnabled())
			logger.debug("getPropertySets: Creating Set from List with " + result.size() + " PropertySets took " + Util.getTimeDiffString(time));

		/* filter secured objects (begin) */
		time = System.currentTimeMillis();
		result = Authority.filterSecuredObjects(pm, result, getPrincipal(), roleID, ResolveSecuringAuthorityStrategy.allow);
		if (logger.isDebugEnabled())
			logger.debug("getPropertySets: filterSecuredObjects with  " + result.size() + " PropertySets took " + Util.getTimeDiffString(time));
		/* filter secured objects (end) */
		return result;
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.prop.PropertyManagerRemote#getDataFieldInstanceCount(org.nightlabs.jfire.prop.id.StructFieldID)
	 */
	@RolesAllowed("_Guest_")
	@Override
	public long getDataFieldInstanceCount(StructFieldID fieldID) {
		PersistenceManager pm = createPersistenceManager();
		try {
			return DataField.getDataFieldInstanceCountByStructFieldType(pm, fieldID);
		} finally {
			pm.close();
		}
	}

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type="Required"
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_Guest_")
	@Override
	public Set<PropertySetFieldBasedEditLayoutUseCaseID> getAllPropertySetFieldBasedEditLayoutUseCaseIDs() {
		PersistenceManager pm = createPersistenceManager();
		try {
			return NLJDOHelper.getDetachedQueryResultAsSet(pm, PropertySetFieldBasedEditLayoutUseCase.getAllUseCaseIDs(pm));
		} finally {
			pm.close();
		}
	}

	/**
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type="Required"
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_Guest_")
	@Override
	public Set<PropertySetFieldBasedEditLayoutUseCase> getPropertySetFieldBasedEditLayoutUseCases(Set<PropertySetFieldBasedEditLayoutUseCaseID> ids, String[] fetchGroups, int maxFetchDepth) {
		PersistenceManager pm = createPersistenceManager();
		try {
			Set<PropertySetFieldBasedEditLayoutUseCase> result = NLJDOHelper.getDetachedObjectSet(pm, ids, null, fetchGroups, maxFetchDepth);
			return result;
		} finally {
			pm.close();
		}
	}


	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.prop.PropertyManagerRemote#initialise()
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_System_")
	@Override
	public void initialise() {
		PersistenceManager pm = createPersistenceManager();
		try {
			// We initialise all meta data, because there is a DataNucleus bug with lazy initialisation of
			// some classes (=> inheritance) related to the SelectionStructField. This bug does not happen after DataNucleus was
			// restarted and thus this code makes sure that the 2nd time JFire is started, the bug does not occur.
			// Maybe it even solves (workaround) the bug by specifying a reliable order of initialisation.
			// Marco.
			try {
				pm.getExtent(DateStructField.class);
				pm.getExtent(I18nTextStructField.class);
				pm.getExtent(ImageStructField.class);
				pm.getExtent(MultiSelectionStructField.class);
				pm.getExtent(NumberStructField.class);
				pm.getExtent(PhoneNumberStructField.class);
				pm.getExtent(RegexStructField.class);
				pm.getExtent(SelectionStructField.class);
				pm.getExtent(TextStructField.class);
				pm.getExtent(TimePatternSetStructField.class);

				pm.getExtent(DateDataField.class);
				pm.getExtent(I18nTextDataField.class);
				pm.getExtent(ImageDataField.class);
				pm.getExtent(MultiSelectionDataField.class);
				pm.getExtent(NumberDataField.class);
				pm.getExtent(PhoneNumberDataField.class);
				pm.getExtent(RegexDataField.class);
				pm.getExtent(SelectionDataField.class);
				pm.getExtent(TextDataField.class);
				pm.getExtent(TimePatternSetDataField.class);
			} catch (Exception x) {
				logger.warn("initialise: Initialising of PropertySet-class-meta-data failed!", x);
			}

			try {
				logger.info("Initialising meta data of PropertySetViewerConfigurations.");
				pm.getExtent(PropertySetViewerConfiguration.class);
				pm.getExtent(PropertySetTableViewerConfiguration.class);
				pm.getExtent(PropertySetSearchEditLayoutConfigModule.class);
			} catch (Exception x) {
				logger.warn("initialise: Initialising of PropertySetSearch class-meta-data failed!", x);
			}

			PersonStruct.getPersonStructLocal(pm);

			// register ConfigModule type
			UserConfigSetup userConfigSetup = (UserConfigSetup)	ConfigSetup.getConfigSetup(
					pm, getOrganisationID(), UserConfigSetup.CONFIG_SETUP_TYPE_USER);
			final String cfModClassName = PropertySetEditLayoutConfigModule.class.getName();
			if (!userConfigSetup.getConfigModuleClasses().contains(cfModClassName))
				userConfigSetup.getConfigModuleClasses().add(cfModClassName);

			if (!userConfigSetup.getConfigModuleClasses().contains(PersonSearchConfigModule.class.getName()))
				userConfigSetup.getConfigModuleClasses().add(PersonSearchConfigModule.class.getName());

			// register the corresponding ConfigModuleIntialiser
			ConfigModuleInitialiserID legalEntitiySearchInitialiserID = ConfigModuleInitialiserID.create(getOrganisationID(),
					cfModClassName, Person.class.getSimpleName());
			LegalEntitySearchEditLayoutIntialiser legalEntityInitialiser = null;
			try {
				legalEntityInitialiser = (LegalEntitySearchEditLayoutIntialiser) pm.getObjectById(legalEntitiySearchInitialiserID);
			} catch (JDOObjectNotFoundException e) {
				legalEntityInitialiser = new LegalEntitySearchEditLayoutIntialiser(legalEntitiySearchInitialiserID.organisationID,
						legalEntitiySearchInitialiserID.configModuleInitialiserID);
				legalEntityInitialiser = pm.makePersistent(legalEntityInitialiser);
			}

			PropertySetFieldBasedEditLayoutUseCaseID useCaseID = PropertySetFieldBasedEditLayoutUseCaseID.create(
					getOrganisationID(), PropertySetFieldBasedEditConstants.USE_CASE_ID_EDIT_PERSON);
			PropertySetFieldBasedEditLayoutUseCase useCaseEditPerson = null;
			try {
				useCaseEditPerson = (PropertySetFieldBasedEditLayoutUseCase) pm.getObjectById(useCaseID);
			} catch (JDOObjectNotFoundException e) {
				StructLocal structLocal = (StructLocal) PersonStruct.getPersonStructLocal(pm);
				useCaseEditPerson = new PropertySetFieldBasedEditLayoutUseCase(useCaseID.organisationID, useCaseID.useCaseID, structLocal);
				useCaseEditPerson = pm.makePersistent(useCaseEditPerson);
				MultiLanguagePropertiesBundle propertiesBundle = new MultiLanguagePropertiesBundle(this.getClass().getPackage().getName() + ".resource.messages", this.getClass().getClassLoader());
				useCaseEditPerson.getName().readFromMultiLanguagePropertiesBundle(propertiesBundle, "org.nightlabs.jfire.prop.config.PropertySetFieldBasedEditLayoutUseCase.EditPerson.name");
				useCaseEditPerson.getDescription().readFromMultiLanguagePropertiesBundle(propertiesBundle, "org.nightlabs.jfire.prop.config.PropertySetFieldBasedEditLayoutUseCase.EditPerson.description");
			}

			ConfigModuleInitialiserID initialiserID = ConfigModuleInitialiserID.create(
					getOrganisationID(), PropertySetFieldBasedEditLayoutConfigModule.class.getName(), Person.class.getSimpleName());
			PropertySetFieldBasedEditLayoutCfModIntialiser initialiser = null;
			try {
				initialiser = (PropertySetFieldBasedEditLayoutCfModIntialiser) pm.getObjectById(initialiserID);
			} catch (JDOObjectNotFoundException e) {
				initialiser = new PropertySetFieldBasedEditLayoutCfModIntialiser(initialiserID.organisationID, initialiserID.configModuleClassName, initialiserID.configModuleInitialiserID);
				initialiser = pm.makePersistent(initialiser);
			}

			// BEGIN conversion of wrong data
			UpdateNeededHandle handle = UpdateHistoryItem.updateNeeded(pm, JFireBaseEAR.MODULE_NAME, MultiSelectionDataField.class.getName() + "#convertBinarySerialisedStructFieldValueIDs");
			if (handle != null) {
				UpdateHistoryItem.updateDone(handle);

				Stopwatch sw = new Stopwatch();
				sw.start("convertMultiSelectionDataField.structFieldValueIDs.query.create");
				Query q = pm.newQuery(MultiSelectionDataField.class);
//				q.setFilter("this.structFieldValueIDs != null"); // This causes an exception: org.datanucleus.exceptions.NucleusUserException: Impossible to query a collection/map field ("org.nightlabs.jfire.prop.datafield.MultiSelectionDataField.structFieldValueIDs") when it is serialised. Either change your query, or change the field to not be serialised.
				sw.stop("convertMultiSelectionDataField.structFieldValueIDs.query.create");

				sw.start("convertMultiSelectionDataField.structFieldValueIDs.query.execute");
				@SuppressWarnings("unchecked")
				Collection<MultiSelectionDataField> c = (Collection<MultiSelectionDataField>) q.execute();
				sw.stop("convertMultiSelectionDataField.structFieldValueIDs.query.execute");

				for (MultiSelectionDataField msdf : c) {
					sw.start("convertMultiSelectionDataField.structFieldValueIDs.convertInstance");
					msdf.setSelection(msdf.getStructFieldValues());
					sw.stop("convertMultiSelectionDataField.structFieldValueIDs.convertInstance");
				}

				logger.warn("initialise: Conversion of MultiSelectionDataField: " + sw.createHumanReport(true));
			}
			// END conversion of wrong data
			
			// BEGIN set default displayFieldColumnCount for StructBlocks
			handle = UpdateHistoryItem.updateNeeded(pm, JFireBaseEAR.MODULE_NAME, MultiSelectionDataField.class.getName() + "#setDefaultDisplayFieldColumnCount");
			if (handle != null) {
				UpdateHistoryItem.updateDone(handle);
				// iterate all StructBlocks and set the default displayFieldColumnCount of 1 if necessary
				Iterator<StructBlock> structBlocks = pm.getExtent(StructBlock.class).iterator();
				while (structBlocks.hasNext()) {
					StructBlock structBlock = structBlocks.next();
					if (structBlock.getDisplayFieldColumnCount() <= 0) {
						structBlock.setDisplayFieldColumnCount(1);
					}
				}
			}

		} finally {
			pm.close();
		}
	}
}
