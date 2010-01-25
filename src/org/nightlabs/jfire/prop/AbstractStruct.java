package org.nightlabs.jfire.prop;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.nightlabs.jfire.base.DuplicateKeyException;
import org.nightlabs.jfire.prop.exception.PropertyException;
import org.nightlabs.jfire.prop.exception.StructBlockNotFoundException;
import org.nightlabs.jfire.prop.exception.StructFieldNotFoundException;
import org.nightlabs.jfire.prop.id.StructBlockID;
import org.nightlabs.jfire.prop.id.StructFieldID;
import org.nightlabs.jfire.prop.validation.IPropertySetValidator;

/**
 * @author Tobias Langner <!-- tobias[dot]langner[at]nightlabs[dot]de -->
 *
 * Abstract implementation of a property structure.
 */
public abstract class AbstractStruct implements IStruct, Serializable
{
	private static final long serialVersionUID = 1L;

	private transient Map<String, StructBlock> structBlockMap;

	@Override
	public void addStructBlock(StructBlock psb) throws DuplicateKeyException
	{
		if (!getStructBlockList().contains(psb)) {
			getStructBlockList().add(psb);
			psb.setStruct(this);
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
		structBlockMap = new HashMap<String, StructBlock>(getStructBlocks().size());
		for (StructBlock block : getStructBlocks())
			structBlockMap.put(block.getPrimaryKey(), block);
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
		getDisplayNameParts().add(part);
	}

	/*
	 * (non-Javadoc)
	 * @see org.nightlabs.jfire.prop.IStruct#removeDisplayNamePart(org.nightlabs.jfire.prop.DisplayNamePart)
	 */
	@Override
	public void removeDisplayNamePart(DisplayNamePart part)
	{
		getDisplayNameParts().remove(part);
	}

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
