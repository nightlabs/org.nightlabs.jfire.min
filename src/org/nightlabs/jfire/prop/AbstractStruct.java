package org.nightlabs.jfire.prop;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jdo.JDOHelper;
import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.jdo.annotations.PersistenceModifier;
import javax.jdo.annotations.Persistent;
import javax.jdo.listener.AttachCallback;

import org.nightlabs.jfire.base.DuplicateKeyException;
import org.nightlabs.jfire.prop.exception.PropertyException;
import org.nightlabs.jfire.prop.exception.StructBlockNotFoundException;
import org.nightlabs.jfire.prop.exception.StructFieldNotFoundException;
import org.nightlabs.jfire.prop.id.DisplayNamePartID;
import org.nightlabs.jfire.prop.id.StructBlockID;
import org.nightlabs.jfire.prop.id.StructFieldID;
import org.nightlabs.jfire.prop.validation.IPropertySetValidator;

/**
 * @author Tobias Langner <!-- tobias[dot]langner[at]nightlabs[dot]de -->
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 *
 * Abstract implementation of a property structure.
 */
public abstract class AbstractStruct implements IStruct, Serializable, AttachCallback
{
	private static final long serialVersionUID = 1L;

	private transient Map<String, StructBlock> structBlockMap;

	@Override
	public void addStructBlock(StructBlock psb) throws DuplicateKeyException
	{
		if (!getStructBlockList().contains(psb)) {
			getStructBlockList().add(psb);
			psb.setStruct(this);
			structBlockMap = null; // clear cache, because it does not yet contain our new block.
		} else
			throw new DuplicateKeyException("The StructBlock " + psb.getPrimaryKey() + " does already exist.");
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This implementation uses {@link #getStructBlockList()} and
	 * returns an unmodifiable view of that.
	 * </p>
	 * <p>
	 * Note that extendors (like {@link StructLocal}) might
	 * override and return other blocks here.
	 * </p>
	 */
	@Override
	public List<StructBlock> getStructBlocks()
	{
		return Collections.unmodifiableList(getStructBlockList());
	}

	@Override
	public List<StructField<?>> getStructFields() {
		List<StructBlock> structBlocks = getStructBlocks();
		List<StructField<?>> structFieldList = new LinkedList<StructField<?>>();

		for (StructBlock block : structBlocks) {
			structFieldList.addAll(block.getStructFields());
		}

		return Collections.unmodifiableList(structFieldList);
	}

	/*
	 * (non-Javadoc)
	 * @see org.nightlabs.jfire.prop.IStruct#getStructBlock(org.nightlabs.jfire.prop.id.StructBlockID)
	 */
	@Override
	public StructBlock getStructBlock(StructBlockID structBlockID)
			throws StructBlockNotFoundException
	{
		return getStructBlock(structBlockID.structBlockOrganisationID, structBlockID.structBlockID);
	}

	/*
	 * (non-Javadoc)
	 * @see org.nightlabs.jfire.prop.IStruct#getStructBlock(java.lang.String, java.lang.String)
	 */
	@Override
	public StructBlock getStructBlock(String structBlockOrganisationID, String structBlockID)
			throws StructBlockNotFoundException
	{
		if (structBlockMap == null)
			initialiseStructBlockMap();

		String pKey = StructBlock.getPrimaryKey(structBlockOrganisationID, structBlockID);
		StructBlock psb = structBlockMap.get(pKey);
		if (psb == null)
			throw new StructBlockNotFoundException("No StructBlock found with key " + pKey);

		return psb;
	}

	private void initialiseStructBlockMap()
	{
		// The structBlockMap is initialized with ALL blocks of
		// the Struct and possibly others from the StructLocal
		// getStructBlockList()
		// Bugfix: the following old code is not thread-safe, but called from multiple threads in the client!
//		structBlockMap = new HashMap<String, StructBlock>(getStructBlocks().size());
//		for (StructBlock block : getStructBlocks())
//			structBlockMap.put(block.getPrimaryKey(), block);

		Map<String, StructBlock> m = new HashMap<String, StructBlock>(getStructBlocks().size());
		for (StructBlock block : getStructBlocks())
			m.put(block.getPrimaryKey(), block);

		structBlockMap = m;
	}

	/*
	 * (non-Javadoc)
	 * @see org.nightlabs.jfire.prop.IStruct#getStructBlock(org.nightlabs.jfire.prop.DataBlockGroup)
	 */
	@Override
	public StructBlock getStructBlock(DataBlockGroup pdbg)
	{
		try
		{
			return getStructBlock(pdbg.getStructBlockOrganisationID(), pdbg.getStructBlockID());
		}
		catch (StructBlockNotFoundException e)
		{
			throw new RuntimeException(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.nightlabs.jfire.prop.IStruct#getStructField(org.nightlabs.jfire.prop.DataField)
	 */
	@Override
	public StructField<? extends DataField> getStructField(DataField df) throws StructBlockNotFoundException, StructFieldNotFoundException {
		return getStructField(df.getStructBlockOrganisationID(), df.getStructBlockID(), df.getStructFieldOrganisationID(), df.getStructFieldID());
	}

	/*
	 * (non-Javadoc)
	 * @see org.nightlabs.jfire.prop.IStruct#getStructField(org.nightlabs.jfire.prop.StructBlock, java.lang.String, java.lang.String)
	 */
	@Override
	public StructField<? extends DataField> getStructField(StructBlock structBlock,
			String structFieldOrganisationID, String structFieldID) throws StructFieldNotFoundException
	{
		return structBlock.getStructField(structFieldOrganisationID, structFieldID);
	}

	/*
	 * (non-Javadoc)
	 * @see org.nightlabs.jfire.prop.IStruct#getStructField(org.nightlabs.jfire.prop.id.StructBlockID, org.nightlabs.jfire.prop.id.StructFieldID)
	 */
	@Override
	public StructField<? extends DataField> getStructField(StructBlockID psbID, StructFieldID psfID)
			throws StructFieldNotFoundException, StructBlockNotFoundException
	{
		return getStructField(psbID.structBlockOrganisationID, psbID.structBlockID, psfID.structFieldOrganisationID,
				psfID.structFieldID);
	}

	/*
	 * (non-Javadoc)
	 * @see org.nightlabs.jfire.prop.IStruct#getStructField(org.nightlabs.jfire.prop.id.StructFieldID)
	 */
	@Override
	public StructField<? extends DataField> getStructField(StructFieldID psfID)
			throws StructFieldNotFoundException, StructBlockNotFoundException
	{
		return getStructField(psfID.structBlockOrganisationID, psfID.structBlockID, psfID.structFieldOrganisationID,
				psfID.structFieldID);
	}

	/*
	 * (non-Javadoc)
	 * @see org.nightlabs.jfire.prop.IStruct#getStructField(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public StructField<? extends DataField> getStructField(String structBlockOrganisationID, String structBlockID,
			String structFieldOrganisationID, String structFieldID)
			throws StructFieldNotFoundException, StructBlockNotFoundException
	{
		StructBlock structBlock = getStructBlock(structBlockOrganisationID, structBlockID);
		return structBlock.getStructField(structFieldOrganisationID, structFieldID);
	}

	/*
	 * (non-Javadoc)
	 * @see org.nightlabs.jfire.prop.IStruct#addDisplayNamePart(org.nightlabs.jfire.prop.DisplayNamePart)
	 */
	@Override
	public void addDisplayNamePart(DisplayNamePart part)
	{
		_getDisplayNameParts().add(part);
	}

	/*
	 * (non-Javadoc)
	 * @see org.nightlabs.jfire.prop.IStruct#removeDisplayNamePart(org.nightlabs.jfire.prop.DisplayNamePart)
	 */
	@Override
	public void removeDisplayNamePart(DisplayNamePart part)
	{
		if (!_getDisplayNameParts().remove(part))
			return;

		PersistenceManager pm = JDOHelper.getPersistenceManager(this);
		if (pm == null) {
			DisplayNamePartID id = (DisplayNamePartID) JDOHelper.getObjectId(part);
			if (id != null) { // was it ever persisted? (otherwise this id is null)
				if (removedDisplayNamePartIDs == null)
					removedDisplayNamePartIDs = new HashSet<DisplayNamePartID>();

				removedDisplayNamePartIDs.add(id);
			}
		}
		else
			deleteOrphanedDisplayNamePart(pm, part);
	}

	@Override
	public void jdoPreAttach() { }

	@Override
	public void jdoPostAttach(Object o) {
		PersistenceManager pm = JDOHelper.getPersistenceManager(this);
		if (pm == null)
			throw new IllegalStateException("JDOHelper.getPersistenceManager(this) returned null!");

		if (removedDisplayNamePartIDs != null) {
			for (DisplayNamePartID displayNamePartID : removedDisplayNamePartIDs) {
				DisplayNamePart displayNamePart;
				try {
					displayNamePart = (DisplayNamePart) pm.getObjectById(displayNamePartID);
				} catch (JDOObjectNotFoundException x) {
					displayNamePart = null;
				}

				if (displayNamePart != null)
					deleteOrphanedDisplayNamePart(pm, displayNamePart);
			}
		}
	}

	private static Collection<IStruct> getOwnerStructs(PersistenceManager pm, Class<? extends IStruct> candidateClass, DisplayNamePart displayNamePart) {
		Query query = pm.newQuery(candidateClass);
		try {
			query.setFilter("this.displayNameParts.contains(:displayNamePart)");
			@SuppressWarnings("unchecked")
			Collection<IStruct> ownerStructs = new ArrayList<IStruct>((Collection<IStruct>) query.execute(displayNamePart));
			return ownerStructs;
		} finally {
			query.closeAll();
		}
	}

	private static void deleteOrphanedDisplayNamePart(PersistenceManager pm, DisplayNamePart displayNamePart) {
		// In order to make sure, the data is written to the DB when we execute our queries, we flush first.
		pm.flush();

		// Check, if the displayNamePart is orphaned.
		boolean isOrphan = getOwnerStructs(pm, Struct.class, displayNamePart).isEmpty();
		if (!isOrphan)
			return;

		isOrphan = getOwnerStructs(pm, StructLocal.class, displayNamePart).isEmpty();
		if (!isOrphan)
			return;

		// Delete the instance from the datastore, since it obviously is an orphan.
		pm.deletePersistent(displayNamePart);
		pm.flush();
	}

	@Persistent(persistenceModifier=PersistenceModifier.NONE)
	private Set<DisplayNamePartID> removedDisplayNamePartIDs = null;

	/**
	 * Get a writable list of the {@link DisplayNamePart}s since {@link IStruct#getDisplayNameParts()} is read-only.
	 * @return a writable list of {@link DisplayNamePart}s.
	 */
	protected abstract List<DisplayNamePart> _getDisplayNameParts();


	/*
	 * (non-Javadoc)
	 * @see org.nightlabs.jfire.prop.IStruct#containsDataField(org.nightlabs.jfire.prop.DataField)
	 */
	@Override
	public boolean containsDataField(DataField field)
	{
		try
		{
			this.getStructField(field.getStructBlockOrganisationID(), field.getStructBlockID(),
					field.getStructFieldOrganisationID(), field.getStructFieldID());
		}
		catch (PropertyException e)
		{
			return false;
		}
		return true;
	}

	/**
	 * If this object is an instance of StructLocal, it should return the corresponding global struct
	 * object, if it is an instance of Struct, it should return null.
	 * @return the Struct
	 */
	protected abstract IStruct getStruct();

	/**
	 * Extendors should return the structBlockList here.
	 * <p>
	 * This method should return the {@link StructBlock}s
	 * coming from the actual instance of an extendor,
	 * not including the ones possibly inherited from
	 * other instances.
	 * </p>
	 * @return a list of the struct blocks
	 */
	protected abstract List<StructBlock> getStructBlockList();

	/**
	 * Extendors should return the name of the link class here.
	 * @return the name of the link class
	 */
	protected abstract String getLinkClassInternal();

	/*
	 * (non-Javadoc)
	 * @see org.nightlabs.jfire.prop.IStruct#getLinkClass()
	 */
	@Override
	public Class<?> getLinkClass()
	{
		try
		{
			return Class.forName(getLinkClassInternal());
		}
		catch (ClassNotFoundException e)
		{
			e.printStackTrace();
			throw new IllegalStateException("Cannot instantiate class " + getLinkClassInternal()+".", e);
		}
	}

	/**
	 * @return the set of {@link IPropertySetValidator}s associated with this
	 * structure.
	 */
	protected abstract Set<IPropertySetValidator> getPropertySetValidatorSet();

	/*
	 * (non-Javadoc)
	 * @see org.nightlabs.jfire.prop.IStruct#addPropertySetValidator(org.nightlabs.jfire.prop.validation.IPropertySetValidator)
	 */
	@Override
	public void addPropertySetValidator(IPropertySetValidator validator) {
		getPropertySetValidatorSet().add(validator);
	}

	/*
	 * (non-Javadoc)
	 * @see org.nightlabs.jfire.prop.IStruct#removePropertySetValidator(org.nightlabs.jfire.prop.validation.IPropertySetValidator)
	 */
	@Override
	public void removePropertySetValidator(IPropertySetValidator validator) {
		getPropertySetValidatorSet().remove(validator);
	}

	/*
	 * (non-Javadoc)
	 * @see org.nightlabs.jfire.prop.IStruct#getPropertySetValidators()
	 */
	@Override
	public Set<IPropertySetValidator> getPropertySetValidators() {
		return Collections.unmodifiableSet(getPropertySetValidatorSet());
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		if (getStructBlockList().isEmpty())
			return "{ }";

		String toReturn = "";
		for (StructBlock block : getStructBlockList())
			toReturn += block.toString() + ", ";

		return "{ " + toReturn.substring(0, toReturn.length() - 2) + " }";
	}

}
