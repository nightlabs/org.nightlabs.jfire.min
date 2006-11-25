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

/**
 * @author Tobias Langner <tobias[DOT]langner[AT]nightlabs[DOT]de>
 */
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.jdo.FetchPlan;
import javax.jdo.JDODataStoreException;
import javax.jdo.JDOHelper;
import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import org.apache.log4j.Logger;
import org.nightlabs.ModuleException;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.base.BaseSessionBeanImpl;
import org.nightlabs.jfire.base.DuplicateKeyException;
import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.jfire.person.Person;
import org.nightlabs.jfire.prop.id.PropertyID;
import org.nightlabs.jfire.prop.id.StructFieldID;
import org.nightlabs.jfire.prop.id.StructID;
import org.nightlabs.jfire.prop.id.StructLocalID;
import org.nightlabs.jfire.prop.search.PropSearchFilter;

/**
 * TODO: Manage access rights for Properties on object-link-type basis
 * 
 * @ejb.bean name="jfire/ejb/JFireBaseBean/PropertyManager"
 *           jndi-name="jfire/ejb/JFireBaseBean/PropertyManager" 
 *           type="Stateless"
 *           transaction-type="Container"
 *           display-name="jfire/ejb/JBaseBean/PropertyManager"
 * 
 * @ejb.util generate="physical"
 */
public abstract class PropertyManagerBean extends BaseSessionBeanImpl implements SessionBean {
	private static final Logger logger = Logger.getLogger(PropertyManagerBean.class);

	/**
	 * @see org.nightlabs.jfire.base.BaseSessionBeanImpl#setSessionContext(javax.ejb.SessionContext)
	 */
	public void setSessionContext(SessionContext sessionContext) throws EJBException, RemoteException {
		super.setSessionContext(sessionContext);
	}

	/**
	 * @see org.nightlabs.jfire.base.BaseSessionBeanImpl#unsetSessionContext()
	 */
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
	public Struct getFullStruct(String organisationID, String linkClass) throws ModuleException {
		PersistenceManager pm = getPersistenceManager();
		try {
			pm.getFetchPlan().addGroup(FetchPlan.ALL);
			pm.getFetchPlan().setMaxFetchDepth(NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT);
			Struct ps = Struct.getStruct(organisationID, linkClass, pm);
			Struct result = (Struct) pm.detachCopy(ps);
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
	public Struct getFullStruct(StructID structID) throws ModuleException {
		return getFullStruct(structID.organisationID, structID.linkClass);
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
	public StructLocal getFullStructLocal(String organisationID, String linkClass) throws ModuleException {
		PersistenceManager pm = getPersistenceManager();
		try {
			pm.getFetchPlan().addGroup(FetchPlan.ALL);
			pm.getFetchPlan().setMaxFetchDepth(NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT);
			StructLocal ps = StructLocal.getStructLocal(organisationID, linkClass, pm);
			StructLocal result = (StructLocal) pm.detachCopy(ps);
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
	public StructLocal getFullStructLocal(StructLocalID structLocalID) throws ModuleException {
		return getFullStructLocal(structLocalID.organisationID, structLocalID.linkClass);
	}

	/**
	 * Retrieve the person with the given ID
	 * 
	 * @throws ModuleException
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type = "Required"
	 */
	public Property getProperty(PropertyID propID, String[] fetchGroups, int maxFetchDepth) throws ModuleException,
			JDOObjectNotFoundException {
		PersistenceManager pm = this.getPersistenceManager();
		try {
			pm.getExtent(Property.class, true);
			Property prop = (Property) pm.getObjectById(propID, true);

			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);

			Property result = (Property) pm.detachCopy(prop);
			return result;
		} finally {
			pm.close();
		}
	}

	/**
	 * Retrieve the person with the given ID
	 * 
	 * @throws ModuleException
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type = "Required"
	 */
	public Property getProperty(PropertyID propID) throws ModuleException, JDOObjectNotFoundException {
		PersistenceManager pm = this.getPersistenceManager();
		try {
			Object o = pm.getObjectById(propID, true);
			long startTime = System.currentTimeMillis();
			// pm.getFetchPlan().resetGroups();
			pm.getFetchPlan().addGroup(FetchPlan.ALL);
			Property ret = (Property) pm.detachCopy(o);
			return ret;
		} finally {
			pm.close();
		}
	}

	/**
	 * 
	 * Retrieve a list Objects searchable by PropSearchFilter.
	 * 
	 * @throws ModuleException
	 * @ejb.interface-method
	 * @ejb.permission role-name="PropManager-read"
	 * @ejb.transaction type = "Required"
	 */
	public Collection searchProperty(PropSearchFilter propSearchFilter, String[] fetchGroups, int maxFetchDepth)
			throws ModuleException, JDOObjectNotFoundException {
		PersistenceManager pm = this.getPersistenceManager();
		try {
			Collection props = propSearchFilter.executeQuery(pm);

			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);

			Collection result = pm.detachCopyAll(props);
			return result;
		} finally {
			pm.close();
		}
	}

	/**
	 * 
	 * Retrieve a list of of ObjectIDs of Objects searchable by a PropSearchFilter
	 * 
	 * @throws ModuleException
	 * @ejb.interface-method
	 * @ejb.permission role-name="PropManager-read"
	 * @ejb.transaction type = "Required"
	 */
	public Collection searchProperty(PropSearchFilter propSearchFilter) throws ModuleException,
			JDOObjectNotFoundException {
		PersistenceManager pm = this.getPersistenceManager();
		try {
			Collection props = propSearchFilter.executeQuery(pm);
			Collection result = new LinkedList();
			for (Iterator iter = props.iterator(); iter.hasNext();) {
				result.add(JDOHelper.getObjectId(iter.next()));
			}
			return result;
		} finally {
			pm.close();
		}
	}

	/**
	 * Store a prop either detached or not made persistent yet.
	 * 
	 * @throws ModuleException
	 * 
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type = "Required"
	 */
	public Property storeProperty(Property prop, boolean get, String[] fetchGroups, int maxFetchDepth)
			throws ModuleException {
		PersistenceManager pm = getPersistenceManager();
		try {
			return (Property) NLJDOHelper.storeJDO(pm, prop, get, fetchGroups, maxFetchDepth);
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
	public void storeStruct(IStruct istruct) throws ModuleException {
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
				NLJDOHelper.storeJDO(pm, structLocal, false, null, -1);
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
	private Collection<StructID> getAvailableStructIDs(PersistenceManager pm) {
		Collection<StructID> structIDs;
		structIDs = NLJDOHelper.getDetachedQueryResult(pm, Struct.getAllStructIDs(pm));
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
	private Collection<StructLocalID> getAvailableStructLocalIDs(PersistenceManager pm) {
		Collection<StructLocalID> structLocalIDs;
		structLocalIDs = NLJDOHelper.getDetachedQueryResult(pm, StructLocal.getAllStructLocalIDs(pm));
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
	 * Get a Collection of all Props
	 * 
	 * @throws ModuleException
	 * 
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type = "Required"
	 */
	public Collection getProperties(String[] fetchGroups, int maxFetchDepth) throws ModuleException {
		// MultiPageSearchResult multiPageSearchResult = new
		// MultiPageSearchResult();
		PersistenceManager pm = getPersistenceManager();
		try {
			Query query = pm.newQuery(Property.class);

			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);

			Collection elements = (Collection) query.execute();

			long time = System.currentTimeMillis();
			Collection result = pm.detachCopyAll(elements);
			time = System.currentTimeMillis() - time;
			logger.info("Detach of " + result.size() + " Props took " + ((double) time / (double) 1000));
			return result;
		} finally {
			pm.close();
		}
	}

	/**
	 * Returns all props for the given propIDs, detached with the given
	 * fetchGroups
	 * 
	 * @throws ModuleException
	 * 
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type = "Required"
	 */
	public Collection getProperties(Object[] propIDs, String[] fetchGroups, int maxFetchDepth) throws ModuleException {
		// MultiPageSearchResult multiPageSearchResult = new
		// MultiPageSearchResult();
		PersistenceManager pm = getPersistenceManager();
		try {
			Collection props = new LinkedList();

			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);

			for (int i = 0; i < propIDs.length; i++) {
				if (!(propIDs[i] instanceof PropertyID))
					throw new IllegalArgumentException("propIDs[" + i + " is not of type PropertyID");
				props.add(pm.getObjectById(propIDs[i]));
			}

			long time = System.currentTimeMillis();
			Collection result = pm.detachCopyAll(props);
			time = System.currentTimeMillis() - time;
			logger.debug("Detach of " + result.size() + " Props took " + ((double) time / (double) 1000));
			return result;
		} finally {
			pm.close();
		}
	}

	/**
	 * Returns the number of data field instances that have been created for the
	 * given {@link AbstractStructField}.
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
			return AbstractDataField.getDataFieldInstanceCountByStructFieldType(pm, fieldID);
		} finally {
			pm.close();
		}
	}

	/**
	 * Creates some test structures and persists them.
	 * 
	 * @throws ModuleException
	 * 
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type="Required"
	 */
	public void createTestStructs() throws ModuleException {
		PersistenceManager pm = getPersistenceManager();
		try {
			Struct struct = null;
			StructLocal structLocal = null;
			
			struct = createPersonStruct();
			pm.makePersistent(struct);
			pm.makePersistent(new StructLocal(struct));

//			for (StructID structID : getAvailableStructIDs(pm)) {
//				deleteStruct(structID, pm);
//				logger.debug("Successfully deleted struct with ID: " + structID);
//			}
//			
//			for (StructLocalID structLocalID : getAvailableStructLocalIDs(pm)) {
//				deleteStructLocal(structLocalID, pm);
//				logger.debug("Successfully deleted struct with ID: " + structLocalID);
//			}
			
//			struct = createPersonStruct();
//			pm.makePersistent(struct);

//			struct = createRandomStructure(StructID.create("chezfrancois.jfire.org", Object.class.getName()));
//			structLocal = new StructLocal(struct);
//			pm.makePersistent(struct);
//			pm.makePersistent(structLocal);
//
//			struct = createRandomStructure(StructID.create("chezfrancois.jfire.org", String.class.getName()));
//			structLocal = new StructLocal(struct);
//			pm.makePersistent(struct);
//			pm.makePersistent(structLocal);
//
//			struct = createRandomStructure(StructID.create("chezfrancois.jfire.org", Integer.class.getName()));
//			structLocal = new StructLocal(struct);
//			pm.makePersistent(struct);
//			pm.makePersistent(structLocal);
//
//			struct = createRandomStructure(StructID.create("chezfrancois.jfire.org", Double.class.getName()));
//			structLocal = new StructLocal(struct);
//			pm.makePersistent(struct);
//			pm.makePersistent(structLocal);
//
//			struct = createRandomStructure(StructID.create("chezfrancois.jfire.org", Enum.class.getName()));
//			structLocal = new StructLocal(struct);
//			pm.makePersistent(struct);
//			pm.makePersistent(structLocal);

		} finally {
			pm.close();
		}
	}
	
	private Struct createPersonStruct() {
		Struct struct;
		StructID structID = StructID.create("chezfrancois.jfire.org", Person.class.getName());
		struct = new Struct(structID.organisationID, structID.linkClass);
		return struct;
	}

	private Struct createStructManual(StructID structID) {
		Struct struct;
		try {
			struct = new Struct(structID.organisationID, structID.linkClass);
			AbstractStructField field;
			StructBlock block;
			block = new StructBlock(struct, structID.organisationID, "block1");
			struct.addStructBlock(block);
			field = new TextStructField(block, structID.organisationID, "field1");
			field.getName().setText(Locale.getDefault().getLanguage(), "Feld 1");
			block.addStructField(field);
			field = new TextStructField(block, structID.organisationID, "field2");
			field.getName().setText(Locale.getDefault().getLanguage(), "Feld 2");
			block.addStructField(field);
			field = new TextStructField(block, structID.organisationID, "field3");
			field.getName().setText(Locale.getDefault().getLanguage(), "Feld 3");
			block.addStructField(field);
			field = new TextStructField(block, structID.organisationID, "field4");
			block.addStructField(field);
			block.getName().setText(Locale.getDefault().getLanguage(), "Block 1");
			block = new StructBlock(struct, structID.organisationID, "block2");
			struct.addStructBlock(block);
			field = new TextStructField(block, structID.organisationID, "field1");
			field.getName().setText(Locale.getDefault().getLanguage(), "Feld 1");
			block.addStructField(field);
			field = new TextStructField(block, structID.organisationID, "field2");
			field.getName().setText(Locale.getDefault().getLanguage(), "Feld 2");
			block.addStructField(field);
			field = new TextStructField(block, structID.organisationID, "field3");
			block.addStructField(field);
			block.getName().setText(Locale.getDefault().getLanguage(), "Block 2");
			block = new StructBlock(struct, structID.organisationID, "block3");
			struct.addStructBlock(block);
			field = new TextStructField(block, structID.organisationID, "field1");
			field.getName().setText(Locale.getDefault().getLanguage(), "Feld 1");
			block.addStructField(field);
			// ---------------
			SelectionStructField selField = new SelectionStructField(block, structID.organisationID, "field2");
			selField.getName().setText(Locale.getDefault().getLanguage(), "Feld 2 (sel)");
			StructFieldValue value = selField.newStructFieldValue("1");
			value.getValueName().setText(Locale.getDefault().getLanguage(), "Auswahl 1");
			value = selField.newStructFieldValue("2");
			value.getValueName().setText(Locale.getDefault().getLanguage(), "Auswahl 2");
			value = selField.newStructFieldValue("3");
			value.getValueName().setText(Locale.getDefault().getLanguage(), "Auswahl 3");
			block.addStructField(selField);
			block.getName().setText(Locale.getDefault().getLanguage(), "Block 3");
			// ---------------

			return struct;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

	}

	private Struct createRandomStructure(StructID structID) {
		String langID = Locale.getDefault().getLanguage();
		Struct struct = new Struct(structID.organisationID, structID.linkClass);
		int structBlockCount = (int) (2 + Math.random() * 9);
		for (int b = 0; b < structBlockCount; b++) {
			StructBlock block = new StructBlock(struct, struct.getOrganisationID(), structID.linkClass + "_" + b);
			block.getName().setText(langID, "Block " + b);
			try {
				struct.addStructBlock(block);
			} catch (DuplicateKeyException e) {
				logger.error("Duplicate StructBlock detected.");
				throw new RuntimeException(e);
			}
			int structFieldCount = (int) (2 + Math.random() * 9);
			for (int f = 0; f < structFieldCount; f++) {
				boolean isTextField = Math.random() < 0.75;
				if (isTextField) {
					TextStructField field = new TextStructField(block, struct.getOrganisationID(), structID.linkClass + "_" + f);
					field.getName().setText(langID, "Feld " + f);
					try {
						block.addStructField(field);
					} catch (DuplicateKeyException e) {
						logger.error("Duplicate StructField detected.");
						throw new RuntimeException(e);
					}
				} else {
					SelectionStructField field = new SelectionStructField(block, struct.getOrganisationID(), structID.linkClass + "_"
							+ f);
					field.getName().setText(langID, "Feld " + f);
					try {
						block.addStructField(field);
					} catch (DuplicateKeyException e) {
						logger.error("Duplicate StructField detected.");
						throw new RuntimeException(e);
					}
					int structFieldValueCount = (int) (2 + Math.random() * 9);
					;
					for (int v = 0; v < structFieldValueCount; v++) {
						field.newStructFieldValue(Integer.toString(v)).getValueName().setText(langID, "Auswahl " + v);
					}
				}
			}
		}
		return struct;
	}

	private void deleteStruct(StructID structID, PersistenceManager pm) {
		try {
			Struct struct = (Struct) pm.getObjectById(structID, true);
			pm.deletePersistent(struct);
		} catch (JDOObjectNotFoundException jdoonfe) {
			logger.debug("Object could not be deleted since it didn't exist.");
		} catch (JDODataStoreException jdodse) {
			logger.error("Deleting of "+ structID +" failed.", jdodse);
			throw new RuntimeException(jdodse);
		}
	}
	
	private void deleteStructLocal(StructLocalID structLocalID, PersistenceManager pm) {
		try {
			StructLocal structLocal = (StructLocal) pm.getObjectById(structLocalID, true);
			pm.deletePersistent(structLocal);
		} catch (JDOObjectNotFoundException jdoonfe) {
			logger.debug("Object could not be deleted since it didn't exist.");
		} catch (JDODataStoreException jdodse) {
			logger.error("Deleting of Struct "+ structLocalID +" failed.", jdodse);
			throw new RuntimeException(jdodse);
		}
	}
}
