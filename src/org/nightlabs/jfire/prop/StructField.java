package org.nightlabs.jfire.prop;

import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.Discriminator;
import javax.jdo.annotations.DiscriminatorStrategy;
import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.FetchGroups;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.PersistenceModifier;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import org.nightlabs.jdo.ObjectIDUtil;
import org.nightlabs.jfire.base.DuplicateKeyException;
import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.prop.i18n.StructFieldName;
import org.nightlabs.jfire.prop.id.StructFieldID;
import org.nightlabs.jfire.prop.validation.DataFieldValidator;
import org.nightlabs.jfire.prop.validation.IDataFieldValidator;
import org.nightlabs.util.CollectionUtil;
import org.nightlabs.util.Util;

/**
 * Basic class for all members of {@link org.nightlabs.jfire.prop.StructBlock} .<br/>
 * It holds the primary key and names for a StructField. All other field data is
 * handled by subclasses of this one.<br/> Subclasses should always be added with
 * {@link org.nightlabs.jfire.prop.StructBlock#addStructField(StructField)}
 *
 * @jdo.persistence-capable identity-type="application"
 *                          objectid-class="org.nightlabs.jfire.prop.id.StructFieldID"
 *                          detachable="true"
 *                          table="JFireBase_Prop_StructField"
 *
 * @jdo.inheritance strategy="new-table"
 * @jdo.inheritance-discriminator strategy="class-name"
 *
 * @jdo.implements name="org.nightlabs.jfire.prop.StructField"
 *
 * @jdo.create-objectid-class field-order="structBlockOrganisationID,
 *                            structBlockID, structFieldOrganisationID,
 *                            structFieldID"
 *                            include-body="id/StructFieldID.body.inc"
 *
 * @jdo.fetch-group
 * 		name="IStruct.fullData"
 * 		fetch-groups="default"
 * 		fields="name, dataFieldValidators, structBlock"
 *
 * @jdo.fetch-group name="StructField.name" fields="name"
 * @jdo.fetch-group name="StructField.structBlock" fields="structBlock"
 *
 * @author Tobias Langner <!-- tobias[dot]langner[at]nightlabs[dot]de -->
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 *
 * @param <DataFieldType> The type of the {@link DataField} that corresponds to this {@link StructField}.
 */
@PersistenceCapable(
	objectIdClass=StructFieldID.class,
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireBase_Prop_StructField")
@FetchGroups({
	@FetchGroup(
		fetchGroups={"default"},
		name="IStruct.fullData",
		members={@Persistent(name="name"), @Persistent(name="dataFieldValidators"), @Persistent(name="structBlock")}),
	@FetchGroup(
		name=StructField.FETCH_GROUP_NAME,
		members=@Persistent(name="name")),
	@FetchGroup(
		name=StructField.FETCH_GROUP_STRUCT_BLOCK,
		members=@Persistent(name="structBlock"))
})
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
@Discriminator(strategy=DiscriminatorStrategy.CLASS_NAME)
public abstract class StructField<DataFieldType extends DataField>
implements Serializable
{
	private static final long serialVersionUID = 1L;

	public static final String FETCH_GROUP_NAME = "StructField.name";
	public static final String FETCH_GROUP_STRUCT_BLOCK = "StructField.structBlock";

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private StructBlock structBlock;

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	@PrimaryKey
	@Column(length=100)
	private String structBlockOrganisationID;

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	@PrimaryKey
	@Column(length=100)
	private String structBlockID;

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	@PrimaryKey
	@Column(length=100)
	private String structFieldOrganisationID;

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	@PrimaryKey
	@Column(length=100)
	private String structFieldID;

	/**
	 * @jdo.field persistence-modifier="persistent" dependent="true" mapped-by="field"
	 */
	@Persistent(
		dependent="true",
		mappedBy="field",
		persistenceModifier=PersistenceModifier.PERSISTENT)
	private StructFieldName name;

	/**
	 * @jdo.field persistence-modifier="none"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.NONE)
	private String validationError = "";

	/**
	 * This constructor is for JDO only
	 */
	protected StructField() {
	}

	/**
	 * Create a new {@link StructField} for the given block, with its organisationID
	 * and the given structFieldID.
	 *
	 * @param _structBlock The {@link StructBlock} the new {@link StructField} should be part of.
	 * @param structFieldID The structFieldID for the new {@link StructField}.
	 */
	protected StructField(StructBlock _structBlock, StructFieldID structFieldID) {
		this(_structBlock, structFieldID.structFieldOrganisationID, structFieldID.structFieldID);
	}

	/**
	 * Create a new {@link StructField} for the given {@link StructBlock}.
	 * The new field will have the organisationID of the block and a structFieldID
	 * obtained using the {@link IDGenerator}.
	 *
	 * @param _structBlock The {@link StructBlock} the new {@link StructField} should be part of.
	 */
	public StructField(StructBlock _structBlock) {
		this(
				_structBlock,
				IDGenerator.getOrganisationID(),
				ObjectIDUtil.longObjectIDFieldToString(IDGenerator.nextID(StructField.class, _structBlock.getPrimaryKey())));
	}

	/**
	 * Create a new {@link StructField} for the given {@link StructBlock} and given rest of the primary key fields.
	 * @param _structBlock The {@link StructBlock} the new {@link StructField} should be part of.
	 * @param _structFieldOrganisationID The structFieldOrganisationID of the new {@link StructField}.
	 * @param _structFieldID The structFieldID for the new {@link StructField}.
	 */
	protected StructField(StructBlock _structBlock, String _structFieldOrganisationID, String _structFieldID) {
		this.structBlockOrganisationID = _structBlock.getStructBlockOrganisationID();
		this.structBlockID = _structBlock.getStructBlockID();
		this.structFieldOrganisationID = _structFieldOrganisationID;
		this.structFieldID = _structFieldID;
		this.structBlock = _structBlock;
		this.name = new StructFieldName(this);
		this.dataFieldValidators = new LinkedList<DataFieldValidator<DataFieldType,StructField<DataFieldType>>>();
//	COMMENTED: seems not to be in use
//		this.structFieldKey = structFieldOrganisationID + "/" + structFieldID;
	}

	/**
	 * @return Returns the structBlockOrganisationID (primary-key field) of this {@link StructField}.
	 */
	public String getStructBlockOrganisationID() {
		return structBlockOrganisationID;
	}

	/**
	 * @return Returns the structBlockID (primary-key field) of this {@link StructField}.
	 */
	public String getStructBlockID() {
		return structBlockID;
	}

	/**
	 * Returns the {@link StructFieldID} (id-object) of this {@link StructField}.
	 * @return The {@link StructFieldID} (id-object) of this {@link StructField}.
	 */
	public StructFieldID getStructFieldIDObj() {
		return StructFieldID.create(structBlockOrganisationID, structBlockID, structFieldOrganisationID, structFieldID);
	}

	/**
	 * Returns a '/'-separated list of the given primary key fields.
	 * @param structBlockOrganisationID The organisationID of the {@link StructBlock} the field is in.
	 * @param structBlockID The structBlockID of the {@link StructBlock} the field is in.
	 * @param structFieldOrganisationID The organisationID of the {@link StructField}.
	 * @param structFieldID The structFieldID of the {@link StructField}.
	 * @return A '/'-separated list of the given primary key fields.
	 */
	public static String getPrimaryKey(String structBlockOrganisationID, String structBlockID,
			String structFieldOrganisationID, String structFieldID) {
		return structBlockOrganisationID + "/" + structBlockID + "/" + structFieldOrganisationID + "/" + structFieldID;
	}

	/**
	 * Returns a '/'-separated list of the primary key fields of this {@link StructField}.
	 * @return A '/'-separated list of the primary key fields of this {@link StructField}.
	 */
	public String getPrimaryKey() {
		return getPrimaryKey(structBlockOrganisationID, structBlockID, structFieldOrganisationID, structFieldID);
	}

	/**
	 * Returns a key for this {@link StructField} relative to its containing {@link StructBlock}.
	 * @param structFieldOrganisationID The organisationID of the {@link StructField}.
	 * @param structFieldID The structFieldID of the {@link StructField}.
	 * @return A key for this {@link StructField} relative to its containing {@link StructBlock}.
	 */
	public static String getStructFieldKey(String structFieldOrganisationID, String structFieldID) {
		return structFieldOrganisationID + "/" + structFieldID;
	}

	/**
	 * Returns a key for this {@link StructField} relative to the {@link StructBlock} it's contained in.
	 * @return A key for this {@link StructField} relative to the {@link StructBlock} it's contained in.
	 */
	public String getStructFieldKey() {
		return getStructFieldKey(structFieldOrganisationID, structFieldID);
	}

	/**
	 * Returns the key of the {@link StructBlock} this field is contained in.
	 * @return The key of the {@link StructBlock} this field is contained in.
	 */
	public String getStructBlockKey() {
		return getStructBlockOrganisationID() + "/" + getStructBlockID();
	}

	/**
	 * Returns the {@link StructBlock} this {@link StructField} is contained in.
	 * @return The {@link StructBlock} this {@link StructField} is contained in.
	 */
	public StructBlock getStructBlock() {
		return structBlock;
	}

	/**
	 * Returns the {@link StructFieldName} of this {@link StructField}.
	 * @return The {@link StructFieldName} of this {@link StructField}.
	 */
	public StructFieldName getName() {
		return name;
	}

	/**
	 * Creates a new {@link DataField} for the given {@link DataBlock} and adds it to the block.
	 *
	 * @param dataBlock The block for which a new {@link DataField} should be created.
	 * @return The {@link DataField} that has just been created.
	 */
	public DataFieldType addNewDataFieldInstance(DataBlock dataBlock) {
		DataFieldType dataField = createDataFieldInstanceInternal(dataBlock);
		try {
			dataBlock.addDataField(dataField);
		} catch (DuplicateKeyException e) {
			throw new RuntimeException(e);
		}
		return dataField;
	}

	/**
	 * Here extendors should return the type of {@link DataField}
	 * the {@link StructField} represents.
	 *
	 * @return The type of {@link DataField} the {@link StructField} represents.
	 */
	public abstract Class<DataFieldType> getDataFieldClass();

	/**
	 * Here extendors should create a new data field instance according to their type and return it.
	 *
	 * @param dataBlock The block for which the new {@link DataField} should be created.
	 * @return the just created data field.
	 */
	protected abstract DataFieldType createDataFieldInstanceInternal(DataBlock dataBlock);

	@Override
	public String toString() {
//		return this.name.getText(NLLocale.getDefault().getLanguage());
		return this.getClass().getName() + Integer.toHexString(System.identityHashCode(this)) + '[' + structBlockOrganisationID + ',' + structBlockID + ',' + structFieldOrganisationID + ',' + structFieldID + ']';
	}

	/**
	 * @return The structFieldOrganisationID (primary-key field) of this {@link StructField}.
	 */
	public String getStructFieldOrganisationID() {
		return structFieldOrganisationID;
	}

	/**
	 * @return The structFieldID (primary-key field) of this {@link StructField}.
	 */
	public String getStructFieldID() {
		return structFieldID;
	}

	/**
	 * Resets the validationError key of this {@link StructField}.
	 */
	public void resetValidationError() {
		this.validationError = "";
	}

	/**
	 * Set the validationError String of this {@link StructField}.
	 * @param validationError The String describing the validation error.
	 */
	public void setValidationError(String validationError) {
		this.validationError = validationError;
	}

	/**
	 * Returns the validation error String.
	 * @return The validation error String.
	 */
	public String getValidationError() {
		return validationError.trim();
	}

	/**
	 * Append the given validationError String as new line to the existing one.
	 * @param validationError The String to append.
	 */
	public void appendValidationError(String validationError) {
		this.validationError += validationError + "\n";
	}

	/** @jdo.field persistence-modifier="none" */
	@Persistent(persistenceModifier=PersistenceModifier.NONE)
	private transient List<ModifyListener> modifyListeners;

	/**
	 * Returns the list of {@link ModifyListener}s of this {@link StructField}.
	 * Note that this list is transient and listeners will not be serialized.
	 * @return The list of {@link ModifyListener}s of this {@link StructField}.
	 */
	private List<ModifyListener> getModifyListeners() {
		if (modifyListeners == null)
			modifyListeners = new LinkedList<ModifyListener>();
		return modifyListeners;
	}

	/**
	 * Adds the given {@link ModifyListener} to the list of listeners of this {@link StructField}.
	 * Note that the list is transient and listeners will not be serialized.
	 * @param listener The listener to add.
	 */
	public void addModifyListener(ModifyListener listener) {
		getModifyListeners().add(listener);
	}

	/**
	 * Removes the given {@link ModifyListener} from the list of listeners of this {@link StructField}.
	 * @param listener The listener to remove.
	 */
	public void removeModifyListener(ModifyListener listener) {
		getModifyListeners().remove(listener);
	}

	/**
	 * Extendors should call this method whenever the data of the struct field gets modified.
	 */
	protected void notifyModifyListeners() {
		for(ModifyListener listener : getModifyListeners())
			listener.modifyData();
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (! (obj instanceof StructField<?>) )	return false;
		final StructField<?> other = (StructField<?>) obj;

		return Util.equals(structBlockOrganisationID, other.structBlockOrganisationID) &&
						Util.equals(structBlockID, other.structBlockID) &&
						Util.equals(structFieldOrganisationID, other.structFieldOrganisationID) &&
						Util.equals(structFieldID, other.structFieldID);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int PRIME = 31;
		int result = 1;
		result = PRIME * result + ((structBlockOrganisationID == null) ? 0 : structBlockOrganisationID.hashCode());
		result = PRIME * result + ((structBlockID == null) ? 0 : structBlockID.hashCode());
		result = PRIME * result + ((structFieldOrganisationID == null) ? 0 : structFieldOrganisationID.hashCode());
		result = PRIME * result + ((structFieldID == null) ? 0 : structFieldID.hashCode());
		return result;
	}

	/**
	 * @jdo.field
	 * 		persistence-modifier="persistent"
	 * 		collection-type="collection"
	 * 		dependent-element="true"
	 *		mapped-by="structField"
	 *		element-type="org.nightlabs.jfire.prop.validation.DataFieldValidator"
	 */
	@Persistent(
		dependentElement="true",
		mappedBy="structField",
		persistenceModifier=PersistenceModifier.PERSISTENT)
	private List<DataFieldValidator<DataFieldType, StructField<DataFieldType>>> dataFieldValidators;

	/**
	 * Adds an implementation of {@link IDataFieldValidator} to the StructField, for validating the data of the associated DataField.
	 * @param validator the IDataFieldValidator to add.
	 */
	public void addDataFieldValidator(IDataFieldValidator<DataFieldType,StructField<DataFieldType>> validator) {
		dataFieldValidators.add((DataFieldValidator<DataFieldType, StructField<DataFieldType>>) validator);
	}

	/**
	 * Removes a previously added IDataFieldValidator {@link #addDataFieldValidator(IDataFieldValidator)} from the StructField.
	 * @param validator the IDataFieldValidator to remove.
	 */
	public void removeDataFieldValidator(IDataFieldValidator<DataFieldType,StructField<DataFieldType>> validator) {
		dataFieldValidators.remove(validator);
	}

	/**
	 * Returns all registered IDataFieldValidator which have been previously added by {@link #addDataFieldValidator(IDataFieldValidator)}
	 * as unmodifiable list.
	 * @return the list of {@link IDataFieldValidator}.
	 */
	public List<IDataFieldValidator<DataFieldType, StructField<DataFieldType>>> getDataFieldValidators() {
		return CollectionUtil.castList(Collections.unmodifiableList(dataFieldValidators));
	}
}
