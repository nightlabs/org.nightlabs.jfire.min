package org.nightlabs.jfire.prop;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.nightlabs.i18n.I18nText;
import org.nightlabs.jfire.base.DuplicateKeyException;
import org.nightlabs.jfire.prop.exception.IllegalStructureModificationException;
import org.nightlabs.jfire.prop.exception.StructBlockNotFoundException;
import org.nightlabs.jfire.prop.exception.StructFieldNotFoundException;
import org.nightlabs.jfire.prop.id.StructBlockID;
import org.nightlabs.jfire.prop.id.StructFieldID;
import org.nightlabs.jfire.prop.validation.IPropertySetValidator;

/**
 * An IStruct reflects the structure of the {@link PropertySet}
 * linked to instances of the class {@link #getLinkClass()}.
 * It manages references to {@link StructBlock}s and {@link StructField}s
 * that form the two-level structure of a {@link PropertySet}.
 * <p>
 * There are two implementations of this interface, one {@link Struct}
 * is global and managed by the root organisation.
 * {@link StructLocal} are local per-organisation modifications of
 * a {@link Struct} and might modifiy the order of the blocks and fields
 * of the referenced {@link Struct} and add custom blocks and fields.
 * </p>
 *
 * @author Tobias Langner <tobias.langner[AT]nightlabs[DOT]de>
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 */
public interface IStruct
{
	/**
	 * Enum for the directions items can be moved within a structure.
	 */
	public enum OrderMoveDirection {
		/**
		 * Direction up means moving the item further up (closer to beginning) in the order.
		 */
		up,
		/**
		 * Direction down means moving the item further down (closer to beginning) in the order.
		 */
		down
	}

	/**
	 * Fetch group to use when retrieving full property structure definitions.
	 */
	public static final String FETCH_GROUP_ISTRUCT_FULL_DATA = "IStruct.fullData";

	/**
	 * Adds a new {@link StructBlock}.
	 *
	 * @param psb The {@link StructBlock} to add
	 */
	public void addStructBlock(StructBlock psb) throws DuplicateKeyException;

	/**
	 * Removes the given {@link StructBlock}.
	 *
	 * @param psb The structBlock to remove.
	 * @throws IllegalStructureModificationException If the block was already persisted with this structure
	 * 		it is not possible to remove it any more.
	 */
	public void removeStructBlock(StructBlock psb) throws IllegalStructureModificationException;

	/**
	 * Returns <b>all</b> {@link StructBlock}s for this Struct.
	 *
	 * @return All {@link StructBlock}s for this Struct.
	 */
	public List<StructBlock> getStructBlocks();
	
	/**
	 * Returns <b>all</b> {@link StructField}s of this Struct.
	 * 
	 * @return all {@link StructField}s of this Struct.
	 */
	public List<StructField<?>> getStructFields();

	/**
	 * A shortcut to {@link #getStructBlock(String, String)}.
	 *
	 * @param structBlockID
	 * @return The {@link StructBlock} identified by
	 *         <code>structBlockID</code>
	 * @throws StructBlockNotFoundException
	 */
	public StructBlock getStructBlock(StructBlockID structBlockID) throws StructBlockNotFoundException;

	/**
	 * Returns the {@link StructBlock} for the given key.<br/> If no entry
	 * could be found for this key a {@link StructBlockNotFoundException} is
	 * thrown.
	 *
	 * @param structBlockOrganisationID
	 * @param structBlockID
	 * @return The {@link StructBlock} for the given key
	 * @throws StructBlockNotFoundException
	 */
	public StructBlock getStructBlock(String structBlockOrganisationID, String structBlockID)
			throws StructBlockNotFoundException;

	/**
	 * Returns the {@link StructBlock} corresponding to the given {@link DataBlockGroup}.
	 *
	 * @param pdbg The {@link DataBlockGroup} to search the {@link StructBlock} for.
	 * @return The {@link StructBlock} corresponding to the given {@link DataBlockGroup}.
	 */
	public StructBlock getStructBlock(DataBlockGroup pdbg);

	/**
	 * Returns the {@link StructField} corresponding to the given {@link DataField}.
	 * @param dataField The {@link DataBlock} for which the {@link StructField} should be returned.
	 * @return the {@link StructField} corresponding to the given {@link DataField}.
	 */
	public StructField<? extends DataField> getStructField(DataField dataField) throws StructBlockNotFoundException, StructFieldNotFoundException;

	/**
	 * Returns the {@link StructField} for the given key.
	 * <p>
	 * If the field can not be found an {@link StructFieldNotFoundException} will be thrown.
	 * </p>
	 * @param structBlock The StructBlock to find the field for.
	 * @param structFieldOrganisationID The organisationID of the field to find.
	 * @param structFieldID The structFieldID of the field to find.
	 * @return The {@link StructField} for the given key.
	 * @throws StructFieldNotFoundException
	 */
	public StructField<? extends DataField> getStructField(StructBlock structBlock, String structFieldOrganisationID,
			String structFieldID) throws StructFieldNotFoundException;

	/**
	 * Returns the {@link StructField} for the given key.
	 * <p>
	 * If the field can not be found an {@link StructFieldNotFoundException} will be thrown.
	 * </p>
	 * @param psbID The {@link StructBlockID} of the field to find.
	 * @param psfID The {@link StructFieldID} of the field to find.
	 * @return The {@link StructField} for the given key.
	 * @throws StructBlockNotFoundException If the block of the field was not found.
	 * @throws StructFieldNotFoundException If the field was not found.
	 */
	public StructField<? extends DataField> getStructField(StructBlockID psbID, StructFieldID psfID)
			throws StructFieldNotFoundException, StructBlockNotFoundException;

	/**
	 * Returns the {@link StructFieldNotFoundException} for the given key.
	 * <p>
	 * If the field can not be found an {@link StructFieldNotFoundException} will be thrown.
	 * </p>
	 * @param psfID The {@link StructFieldID} of the field to find.
	 * @return The {@link StructField} for the given key.
	 * @throws StructBlockNotFoundException If the block of the field was not found.
	 * @throws StructFieldNotFoundException If the field was not found.
	 */
	public StructField<? extends DataField> getStructField(StructFieldID psfID)
			throws StructFieldNotFoundException, StructBlockNotFoundException;
	/**
	 * Returns the {@link StructField} for the given key.
	 * <p>
	 * If the field can not be found an {@link StructFieldNotFoundException} will be thrown.
	 * </p>
	 * @param structBlockOrganisationID The structBlockOrganisationID of the field to find.
	 * @param structBlockID The structBlockID of the field to find.
	 * @param structFieldOrganisationID The structFieldOrganisationID of the field to find.
	 * @param structFieldID The structFieldID of the field to find.
	 * @return The {@link StructField} for the given key
	 * @throws StructBlockNotFoundException If the block of the field was not found.
	 * @throws StructFieldNotFoundException If the field was not found.
	 */
	public StructField<? extends DataField> getStructField(String structBlockOrganisationID, String structBlockID,
			String structFieldOrganisationID, String structFieldID)
			throws StructFieldNotFoundException, StructBlockNotFoundException;

	/**
	 * @return all {@link DisplayNamePart}s of this structure.
	 */
	public Collection<DisplayNamePart> getDisplayNameParts();

	/**
	 * Adds the given {@link DisplayNamePart} to the end of
	 * the list of {@link DisplayNamePart}s.
	 *
	 * @param part The part to add.
	 */
	public void addDisplayNamePart(DisplayNamePart part);

	/**
	 * Removes the given {@link DisplayNamePart} from the list of {@link DisplayNamePart}s.
	 * @param part The part to remove.
	 */
	public void removeDisplayNamePart(DisplayNamePart part);

	/**
	 * Checks whether this struct has a {@link StructField} corresponding
	 * to the given {@link DataField}.
	 *
	 * @param field The field to check.
	 * @return Whether a corresponding {@link StructField} can be found in this structure.
	 */
	public boolean containsDataField(DataField field);

	public String getOrganisationID();

	/**
	 * Returns the class of objects this Structure is linked to.
	 * @return The class of objects this Structure is linked to.
	 */
	public Class<?> getLinkClass();

	/**
	 * Returns the scope of this {@link IStruct}.
	 * Note that this is always the scope of the underlying {@link Struct}
	 * even if the implementation is {@link StructLocal}.
	 * @return Returns the scope of this {@link IStruct}.
	 */
	public String getStructScope();

	/**
	 * Returns the name of this {@link IStruct}.
	 * @return The name of this {@link IStruct}.
	 */
	public I18nText getName();

	/**
	 * Adds an {@link IPropertySetValidator} instance.
	 * @param validator The instance to be added.
	 */
	public void addPropertySetValidator(IPropertySetValidator validator);

	/**
	 * Removes an {@link IPropertySetValidator} instance.
	 * @param validator The instance to be removed.
	 */
	public void removePropertySetValidator(IPropertySetValidator validator);

	/**
	 * Returns a set of the {@link IPropertySetValidator} of this instance.
	 * @return a set of the {@link IPropertySetValidator} of this instance.
	 */
	public Set<IPropertySetValidator> getPropertySetValidators();
}