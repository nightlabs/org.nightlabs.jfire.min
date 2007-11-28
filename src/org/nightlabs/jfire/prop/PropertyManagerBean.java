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
import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.jfire.person.PersonStruct;
import org.nightlabs.jfire.prop.id.PropertyID;
import org.nightlabs.jfire.prop.id.StructFieldID;
import org.nightlabs.jfire.prop.id.StructID;
import org.nightlabs.jfire.prop.id.StructLocalID;
import org.nightlabs.jfire.prop.search.PropSearchFilter;
import org.nightlabs.util.Util;

/**
 * @author Tobias Langner <!-- tobias[dot]langner[at]nightlabs[dot]de -->
 */
/**
 * TODO: Manage access rights for Properties on object-link-type basis
 * 
 * @ejb.bean name="jfire/ejb/JFireBaseBean/PropertyManager"
 *           jndi-name="jfire/ejb/JFireBaseBean/PropertyManager" 
 *           type="Stateless"
 *           transaction-type="Container"
 * 
 * @ejb.util generate="physical"
 */
public abstract class PropertyManagerBean extends BaseSessionBeanImpl implements SessionBean {
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
	 * Detaches and returns the complete Struct.
	 * 
	 * @param organisationID
	 *          The organisation id of the {@link Struct} to be retrieved.
	 * @param linkClass
	 *          The linkClass of the {@link Struct} to be retrieved.
	 * @return The complete {@link Struct}.
	 * 
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type = "Required"
	 */
	public Struct getFullStruct(String organisationID, String linkClass, String[] fetchGroups, int maxFetchDepth) throws ModuleException {
		PersistenceManager pm = getPersistenceManager();
		try {
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);
			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			Struct ps = Struct.getStruct(organisationID, linkClass, pm);
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
	 * @ejb.transaction type = "Required"
	 */
	public Struct getFullStruct(StructID structID, String[] fetchGroups, int maxFetchDepth) throws ModuleException {
		return getFullStruct(structID.organisationID, structID.linkClass, fetchGroups, maxFetchDepth);
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
	 * @ejb.transaction type = "Required"
	 */
	public StructLocal getFullStructLocal(String organisationID, String linkClass, String scope, String[] fetchGroups, int maxFetchDepth) throws ModuleException {
		PersistenceManager pm = getPersistenceManager();
		try {
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);
			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			StructLocal ps = StructLocal.getStructLocal(organisationID, linkClass, scope, pm);
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
	 * @ejb.transaction type = "Required"
	 */
	public StructLocal getFullStructLocal(StructLocalID structLocalID, String[] fetchGroups, int maxFetchDepth) throws ModuleException
	{
		return getFullStructLocal(structLocalID.organisationID, structLocalID.linkClass, structLocalID.scope, fetchGroups, maxFetchDepth);
	}

	/**
	 * Retrieve the person with the given ID
	 * 
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type = "Required"
	 */
	public PropertySet getPropertySet(PropertyID propID, String[] fetchGroups, int maxFetchDepth)
	{
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
	 * @ejb.permission role-name="PropManager-read"
	 * @ejb.transaction type = "Required"
	 */
	public Set<?> searchPropertySets(PropSearchFilter propSearchFilter, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = this.getPersistenceManager();
		try {
			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);
			
			Collection<PropertySet> props = propSearchFilter.executeQuery(pm);
			return NLJDOHelper.getDetachedQueryResultAsSet(pm, props);
		} finally {
			pm.close();
		}
	}
	
	/**
	 * Executes the given search filter and assumes it will return instances
	 * of {@link PropertySet}. It will return the {@link PropertyID}s of the
	 * found {@link PropertySet}s then.
	 * <p>
	 * Note, that if the given search filter does not return instances 
	 * of {@link PropertySet} (its result columns might be set to something different)
	 * this method will fail with a {@link ClassCastException}. 
	 * </p>
	 * @ejb.interface-method
	 * @ejb.permission role-name="PropManager-read"
	 * @ejb.transaction type = "Required"
	 */
	public Set<PropertyID> searchPropertySetIDs(PropSearchFilter propSearchFilter)
	{
		PersistenceManager pm = this.getPersistenceManager();
		try {
			Collection<PropertySet> props = propSearchFilter.executeQuery(pm);
			Set<PropertyID> result = new HashSet<PropertyID>();
			for (PropertySet propertySet : props) {
				result.add((PropertyID) JDOHelper.getObjectId(propertySet));
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
	 * @ejb.transaction type = "Required"
	 */
	public PropertySet storePropertySet(PropertySet propertySet, boolean get, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			return NLJDOHelper.storeJDO(pm, propertySet, get, fetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}
	}

	/**
	 * Store a propStruct either detached or not made persistent yes.
	 * 
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type="Required"
	 */
	public void storeStruct(IStruct istruct)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			if (istruct instanceof Struct) {
				Struct modifiedStruct = (Struct) istruct;
				Struct currentStruct = (Struct) pm.getObjectById(modifiedStruct.getID());
				modifiedStruct = applyStructuralChanges(modifiedStruct, currentStruct);
				NLJDOHelper.storeJDO(pm, modifiedStruct, false, null, -1);
			} else if (istruct instanceof StructLocal) {
				StructLocal structLocal = (StructLocal) istruct;
				structLocal.restoreAdoptedBlocks();
				pm.makePersistent(structLocal);
//				NLJDOHelper.storeJDO(pm, structLocal, false, null, -1);
			}
		} finally {
			pm.close();
		}
	}

	/**
	 * Checks if the changes reflected by <code>modifiedStruct</code> are
	 * adequate, i.e. that they did not touch blocks that are owned by the devil
	 * organisation and thus aren't modifyable. Changes to such blocks are
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
	public Struct applyStructuralChanges(Struct modifiedStruct, Struct currentStruct) {
		if (!modifiedStruct.getID().equals(currentStruct.getID()))
			throw new IllegalArgumentException("Cannot apply changes because structure IDs are not the same.");

		List<StructBlock> modifiedBlocks, currentBlocks;
		modifiedBlocks = modifiedStruct.getStructBlocks();
		currentBlocks = currentStruct.getStructBlocks();

		for (StructBlock block : currentBlocks) {
			if (block.getStructBlockOrganisationID().equals(Organisation.DEVIL_ORGANISATION_ID)) {
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
	 * @ejb.transaction type = "Required"
	 */
	public Set<PropertySet> getPropertySets(Set<PropertyID> propIDs, String[] fetchGroups, int maxFetchDepth) 
	{
		// MultiPageSearchResult multiPageSearchResult = new
		// MultiPageSearchResult();
		PersistenceManager pm = getPersistenceManager();
		try {
			long time = System.currentTimeMillis();
			Set<PropertySet> result = NLJDOHelper.getDetachedObjectSet(pm, propIDs, null, fetchGroups, maxFetchDepth);
			logger.debug("Detach of " + result.size() + " Props took " + Util.getTimeDiffString(time));
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
	 * @ejb.transaction type="Supports"
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
	public void initialise()
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			PersonStruct.getPersonStruct(getOrganisationID(), pm);
		} finally {
			pm.close();
		}
	}

//	/**
//	 * Creates some test structures and persists them.
//	 * 
//	 * @throws ModuleException
//	 * 
//	 * @ejb.interface-method
//	 * @ejb.permission role-name="_Guest_"
//	 * @ejb.transaction type="Required"
//	 */
//	public void createTestStructs() throws ModuleException {
//		PersistenceManager pm = getPersistenceManager();
//		try {
//			Struct struct = null;
//			StructLocal structLocal = null;
//			
//			struct = createPersonStruct();
//			pm.makePersistent(struct);
//			pm.makePersistent(new StructLocal(struct));
//
////			for (StructID structID : getAvailableStructIDs(pm)) {
////				deleteStruct(structID, pm);
////				logger.debug("Successfully deleted struct with ID: " + structID);
////			}
////			
////			for (StructLocalID structLocalID : getAvailableStructLocalIDs(pm)) {
////				deleteStructLocal(structLocalID, pm);
////				logger.debug("Successfully deleted struct with ID: " + structLocalID);
////			}
//			
////			struct = createPersonStruct();
////			pm.makePersistent(struct);
//
////			struct = createRandomStructure(StructID.create("chezfrancois.jfire.org", Object.class.getName()));
////			structLocal = new StructLocal(struct);
////			pm.makePersistent(struct);
////			pm.makePersistent(structLocal);
////
////			struct = createRandomStructure(StructID.create("chezfrancois.jfire.org", String.class.getName()));
////			structLocal = new StructLocal(struct);
////			pm.makePersistent(struct);
////			pm.makePersistent(structLocal);
////
////			struct = createRandomStructure(StructID.create("chezfrancois.jfire.org", Integer.class.getName()));
////			structLocal = new StructLocal(struct);
////			pm.makePersistent(struct);
////			pm.makePersistent(structLocal);
////
////			struct = createRandomStructure(StructID.create("chezfrancois.jfire.org", Double.class.getName()));
////			structLocal = new StructLocal(struct);
////			pm.makePersistent(struct);
////			pm.makePersistent(structLocal);
////
////			struct = createRandomStructure(StructID.create("chezfrancois.jfire.org", Enum.class.getName()));
////			structLocal = new StructLocal(struct);
////			pm.makePersistent(struct);
////			pm.makePersistent(structLocal);
//
//		} finally {
//			pm.close();
//		}
//	}
//	
//	private Struct createPersonStruct() {
//		Struct struct;
//		StructID structID = StructID.create("chezfrancois.jfire.org", Person.class.getName());
//		struct = new Struct(structID.organisationID, structID.linkClass);
//		return struct;
//	}

//	private Struct createStructManual(StructID structID) {
//		Struct struct;
//		try {
//			struct = new Struct(structID.organisationID, structID.linkClass);
//			StructField field;
//			StructBlock block;
//			block = new StructBlock(struct, structID.organisationID, "block1");
//			struct.addStructBlock(block);
//			field = new TextStructField(block, structID.organisationID, "field1");
//			field.getName().setText(Locale.getDefault().getLanguage(), "Feld 1");
//			block.addStructField(field);
//			field = new TextStructField(block, structID.organisationID, "field2");
//			field.getName().setText(Locale.getDefault().getLanguage(), "Feld 2");
//			block.addStructField(field);
//			field = new TextStructField(block, structID.organisationID, "field3");
//			field.getName().setText(Locale.getDefault().getLanguage(), "Feld 3");
//			block.addStructField(field);
//			field = new TextStructField(block, structID.organisationID, "field4");
//			block.addStructField(field);
//			block.getName().setText(Locale.getDefault().getLanguage(), "Block 1");
//			block = new StructBlock(struct, structID.organisationID, "block2");
//			struct.addStructBlock(block);
//			field = new TextStructField(block, structID.organisationID, "field1");
//			field.getName().setText(Locale.getDefault().getLanguage(), "Feld 1");
//			block.addStructField(field);
//			field = new TextStructField(block, structID.organisationID, "field2");
//			field.getName().setText(Locale.getDefault().getLanguage(), "Feld 2");
//			block.addStructField(field);
//			field = new TextStructField(block, structID.organisationID, "field3");
//			block.addStructField(field);
//			block.getName().setText(Locale.getDefault().getLanguage(), "Block 2");
//			block = new StructBlock(struct, structID.organisationID, "block3");
//			struct.addStructBlock(block);
//			field = new TextStructField(block, structID.organisationID, "field1");
//			field.getName().setText(Locale.getDefault().getLanguage(), "Feld 1");
//			block.addStructField(field);
//			// ---------------
//			SelectionStructField selField = new SelectionStructField(block, structID.organisationID, "field2");
//			selField.getName().setText(Locale.getDefault().getLanguage(), "Feld 2 (sel)");
//			StructFieldValue value = selField.newStructFieldValue("1");
//			value.getValueName().setText(Locale.getDefault().getLanguage(), "Auswahl 1");
//			value = selField.newStructFieldValue("2");
//			value.getValueName().setText(Locale.getDefault().getLanguage(), "Auswahl 2");
//			value = selField.newStructFieldValue("3");
//			value.getValueName().setText(Locale.getDefault().getLanguage(), "Auswahl 3");
//			block.addStructField(selField);
//			block.getName().setText(Locale.getDefault().getLanguage(), "Block 3");
//			// ---------------
//
//			return struct;
//		} catch (Exception e) {
//			throw new RuntimeException(e);
//		}
//
//	}
//
//	private Struct createRandomStructure(StructID structID) {
//		String langID = Locale.getDefault().getLanguage();
//		Struct struct = new Struct(structID.organisationID, structID.linkClass);
//		int structBlockCount = (int) (2 + Math.random() * 9);
//		for (int b = 0; b < structBlockCount; b++) {
//			StructBlock block = new StructBlock(struct, struct.getOrganisationID(), structID.linkClass + "_" + b);
//			block.getName().setText(langID, "Block " + b);
//			try {
//				struct.addStructBlock(block);
//			} catch (DuplicateKeyException e) {
//				logger.error("Duplicate StructBlock detected.");
//				throw new RuntimeException(e);
//			}
//			int structFieldCount = (int) (2 + Math.random() * 9);
//			for (int f = 0; f < structFieldCount; f++) {
//				boolean isTextField = Math.random() < 0.75;
//				if (isTextField) {
//					TextStructField field = new TextStructField(block, struct.getOrganisationID(), structID.linkClass + "_" + f);
//					field.getName().setText(langID, "Feld " + f);
//					try {
//						block.addStructField(field);
//					} catch (DuplicateKeyException e) {
//						logger.error("Duplicate StructField detected.");
//						throw new RuntimeException(e);
//					}
//				} else {
//					SelectionStructField field = new SelectionStructField(block, struct.getOrganisationID(), structID.linkClass + "_"
//							+ f);
//					field.getName().setText(langID, "Feld " + f);
//					try {
//						block.addStructField(field);
//					} catch (DuplicateKeyException e) {
//						logger.error("Duplicate StructField detected.");
//						throw new RuntimeException(e);
//					}
//					int structFieldValueCount = (int) (2 + Math.random() * 9);
//					;
//					for (int v = 0; v < structFieldValueCount; v++) {
//						field.newStructFieldValue(Integer.toString(v)).getValueName().setText(langID, "Auswahl " + v);
//					}
//				}
//			}
//		}
//		return struct;
//	}
//
//	private void deleteStruct(StructID structID, PersistenceManager pm) {
//		try {
//			Struct struct = (Struct) pm.getObjectById(structID, true);
//			pm.deletePersistent(struct);
//		} catch (JDOObjectNotFoundException jdoonfe) {
//			logger.debug("Object could not be deleted since it didn't exist.");
//		} catch (JDODataStoreException jdodse) {
//			logger.error("Deleting of "+ structID +" failed.", jdodse);
//			throw new RuntimeException(jdodse);
//		}
//	}
//	
//	private void deleteStructLocal(StructLocalID structLocalID, PersistenceManager pm) {
//		try {
//			StructLocal structLocal = (StructLocal) pm.getObjectById(structLocalID, true);
//			pm.deletePersistent(structLocal);
//		} catch (JDOObjectNotFoundException jdoonfe) {
//			logger.debug("Object could not be deleted since it didn't exist.");
//		} catch (JDODataStoreException jdodse) {
//			logger.error("Deleting of Struct "+ structLocalID +" failed.", jdodse);
//			throw new RuntimeException(jdodse);
//		}
//	}
}
