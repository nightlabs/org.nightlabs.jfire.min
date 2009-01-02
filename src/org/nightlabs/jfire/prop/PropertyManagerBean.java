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

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.spi.PersistenceCapable;

import org.apache.log4j.Logger;
import org.nightlabs.ModuleException;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.base.BaseSessionBeanImpl;
import org.nightlabs.jfire.base.expression.IExpression;
import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.jfire.person.PersonStruct;
import org.nightlabs.jfire.prop.id.PropertySetID;
import org.nightlabs.jfire.prop.id.StructFieldID;
import org.nightlabs.jfire.prop.id.StructID;
import org.nightlabs.jfire.prop.id.StructLocalID;
import org.nightlabs.jfire.prop.search.PropSearchFilter;
import org.nightlabs.util.CollectionUtil;
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
public abstract class PropertyManagerBean extends BaseSessionBeanImpl implements SessionBean
{
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(PropertyManagerBean.class);

	/**
	 * @see org.nightlabs.jfire.base.BaseSessionBeanImpl#setSessionContext(javax.ejb.SessionContext)
	 */
	@Override
	public void setSessionContext(SessionContext sessionContext) throws EJBException, RemoteException {
		super.setSessionContext(sessionContext);
	}

	/**
	 * @see org.nightlabs.jfire.base.BaseSessionBeanImpl#unsetSessionContext()
	 */
	@Override
	public void unsetSessionContext() {
		super.unsetSessionContext();
	}

	/**
	 * @ejb.create-method
	 * @ejb.permission role-name="_Guest_"
	 */
	public void ejbCreate() throws CreateException {
	}

	/**
	 * @see javax.ejb.SessionBean#ejbRemove()
	 *
	 * @ejb.permission unchecked="true"
	 */
	public void ejbRemove() throws EJBException, RemoteException {
	}

	/**
	 * @ejb.interface-method
	 * @ejb.transaction type="Supports"
	 * @ejb.permission role-name="_Guest_"
	 */
	@Override
	public String ping(String message) {
		return super.ping(message);
	}

	/**
	 * Detaches and returns the complete Struct.
	 *
	 * @param organisationID
	 *          The organisation id of the {@link Struct} to be retrieved.
	 * @param linkClass
	 *          The linkClass of the {@link Struct} to be retrieved.
	 * @param structScope
	 * 			The scope of the {@link Struct} to be retrieved.
	 * @return The complete {@link Struct}.
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type="Required"
	 */
	public Struct getFullStruct(
			String organisationID, String linkClass, String structScope, String[] fetchGroups, int maxFetchDepth) throws ModuleException {
		PersistenceManager pm = getPersistenceManager();
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

	/**
	 * Convenience method
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type="Required"
	 */
	public Struct getFullStruct(StructID structID, String[] fetchGroups, int maxFetchDepth) throws ModuleException {
		return getFullStruct(structID.organisationID, structID.linkClass, structID.structScope, fetchGroups, maxFetchDepth);
	}

	/**
	 * Detaches and returns the complete {@link StructLocal}.
	 *
	 * @param organisationID
	 *          The organisation id of the {@link StructLocal} to be retrieved.
	 * @param linkClass
	 *          The linkClass of the {@link StructLocal} to be retrieved.
	 * @return The complete {@link StructLocal}.
	 * @throws ModuleException
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type="Required"
	 */
	public StructLocal getFullStructLocal(
			String organisationID, String linkClass, String structScope, String structLocalScope, String[] fetchGroups, int maxFetchDepth)
			throws ModuleException {
		PersistenceManager pm = getPersistenceManager();
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

	/**
	 * Convenience method
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type="Required"
	 */
	public StructLocal getFullStructLocal(StructLocalID structLocalID, String[] fetchGroups, int maxFetchDepth) throws ModuleException {
		return getFullStructLocal(
				structLocalID.organisationID, structLocalID.linkClass,
				structLocalID.structScope, structLocalID.structLocalScope,
				fetchGroups, maxFetchDepth);
	}

	/**
	 * Retrieve the person with the given ID
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type="Required"
	 */
	public PropertySet getPropertySet(PropertySetID propID, String[] fetchGroups, int maxFetchDepth) {
		PersistenceManager pm = this.getPersistenceManager();
		try {
			pm.getExtent(PropertySet.class, true);
			PropertySet prop = (PropertySet) pm.getObjectById(propID, true);

			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);

			PropertySet result = pm.detachCopy(prop);
			return result;
		} finally {
			pm.close();
		}
	}

	/**
	 * Returns those objects found by the given search filter.
	 * The results therefore might not be an instance
	 * of {@link PropertySet}, this depends on the result columns
	 * set in the search filter.
	 * <p>
	 * All found objects are detached with the given fetch-groups
	 * if they are {@link PersistenceCapable}.
	 * </p>
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type="Required"
	 */
	public Set<?> searchPropertySets(PropSearchFilter propSearchFilter, String[] fetchGroups, int maxFetchDepth) {
		PersistenceManager pm = this.getPersistenceManager();
		try {
			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);

			propSearchFilter.setPersistenceManager(pm);
			Collection<?> props = propSearchFilter.getResult();
			return NLJDOHelper.getDetachedQueryResultAsSet(pm, props);
		} finally {
			pm.close();
		}
	}

	/**
	 * Executes the given search filter and assumes it will return instances
	 * of {@link PropertySet}. It will return the {@link PropertySetID}s of the
	 * found {@link PropertySet}s then.
	 * <p>
	 * Note, that if the given search filter does not return instances
	 * of {@link PropertySet} (its result columns might be set to something different)
	 * this method will fail with a {@link ClassCastException}.
	 * </p>
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type="Required"
	 */
	public Set<PropertySetID> searchPropertySetIDs(PropSearchFilter propSearchFilter) {
		PersistenceManager pm = this.getPersistenceManager();
		try {
			propSearchFilter.setPersistenceManager(pm);
			Collection<?> props = propSearchFilter.getResult();
			Set<PropertySetID> result = new HashSet<PropertySetID>();
			for (Object element : props) {
				if (element instanceof PropertySet) {
					PropertySet propertySet = (PropertySet) element;
					result.add((PropertySetID) JDOHelper.getObjectId(propertySet));
				}
			}
			return result;
		} finally {
			pm.close();
		}
	}

	/**
	 * Store a {@link PropertySet} either detached or not made persistent yet.
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type="Required"
	 */
	public PropertySet storePropertySet(PropertySet propertySet, boolean get, String[] fetchGroups, int maxFetchDepth) {
		PersistenceManager pm = getPersistenceManager();
		try {
			return NLJDOHelper.storeJDO(pm, propertySet, get, fetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}
	}

	/**
	 * Store a struct either detached or not made persistent yet.
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type="Required"
	 */
	public IStruct storeStruct(IStruct struct, boolean get, String[] fetchGroups, int maxFetchDepth) {
		PersistenceManager pm = getPersistenceManager();
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

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type="Required"
	 */
	public Collection<StructID> getAvailableStructIDs() {
		PersistenceManager pm = getPersistenceManager();
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

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type="Required"
	 */
	public Collection<StructLocalID> getAvailableStructLocalIDs() {
		PersistenceManager pm = getPersistenceManager();
		try {
			return getAvailableStructLocalIDs(pm);
		} finally {
			pm.close();
		}
	}

	/**
	 * Returns all {@link PropertySet}s for the given propIDs, detached with the given
	 * fetchGroups
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type="Required"
	 */
	public Set<PropertySet> getPropertySets(Set<PropertySetID> propIDs, String[] fetchGroups, int maxFetchDepth) {
		// MultiPageSearchResult multiPageSearchResult = new
		// MultiPageSearchResult();
		PersistenceManager pm = getPersistenceManager();
		try {
			long time = System.currentTimeMillis();
			Set<PropertySet> result = NLJDOHelper.getDetachedObjectSet(pm, propIDs, null, fetchGroups, maxFetchDepth);
			logger.debug("Detaching " + result.size() + " PropertySets took " + Util.getTimeDiffString(time));
			return result;
		} finally {
			pm.close();
		}
	}

	/**
	 * Returns the number of data field instances that have been created for the
	 * given {@link StructField}.
	 *
	 * @param field
	 *          The struct field
	 * @return
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 */
	public long getDataFieldInstanceCount(StructFieldID fieldID) {
		PersistenceManager pm = getPersistenceManager();
		try {
			return DataField.getDataFieldInstanceCountByStructFieldType(pm, fieldID);
		} finally {
			pm.close();
		}
	}

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_System_"
	 * @ejb.transaction type="Required"
	 */
	public void initialise() {
		PersistenceManager pm = getPersistenceManager();
		try {
			PersonStruct.getPersonStruct(getOrganisationID(), pm);
		} finally {
			pm.close();
		}
	}

}
