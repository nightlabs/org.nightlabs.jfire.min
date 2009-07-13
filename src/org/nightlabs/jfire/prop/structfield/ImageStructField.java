package org.nightlabs.jfire.prop.structfield;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import org.nightlabs.jfire.prop.DataBlock;
import org.nightlabs.jfire.prop.DataField;
import org.nightlabs.jfire.prop.StructBlock;
import org.nightlabs.jfire.prop.StructField;
import org.nightlabs.jfire.prop.datafield.ImageDataField;
import org.nightlabs.jfire.prop.id.StructFieldID;

import javax.jdo.annotations.Join;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.FetchGroups;
import javax.jdo.annotations.NullValue;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceModifier;

/**
 * {@link StructField} that represents a {@link DataField} holding the binary data of an image.
 * The {@link ImageStructField} can configure valid file extensions and maximum size of the images.
 * 
 * @jdo.persistence-capable
 * 		identity-type="application"
 *    persistence-capable-superclass="org.nightlabs.jfire.prop.StructField"
 *   	detachable="true"
 *   	table="JFireBase_Prop_ImageStructField"
 *
 * @jdo.implements name="org.nightlabs.jfire.prop.StructField"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.fetch-group name="IStruct.fullData" fetch-groups="default" fields="formats"
 *
 * @author Tobias Langner <!-- tobias[dot]langner[at]nightlabs[dot]de -->
 */
@PersistenceCapable(
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireBase_Prop_ImageStructField")
@FetchGroups(
	@FetchGroup(
		fetchGroups={"default"},
		name="IStruct.fullData",
		members=@Persistent(name="formats"))
)
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class ImageStructField extends StructField<ImageDataField> {

	private static final long serialVersionUID = 0;

	/**
	 * @jdo.field
	 * 		persistence-modifier="persistent"
	 * 		collection-type="collection"
	 * 		element-type="java.lang.String"
	 * 		dependent-element="true"
	 * 		table="JFireBase_Prop_ImageStructField_formats"
	 *		null-value="exception"
	 *
	 * @jdo.join
	 */
	@Join
	@Persistent(
		dependentElement="true",
		nullValue=NullValue.EXCEPTION,
		table="JFireBase_Prop_ImageStructField_formats",
		persistenceModifier=PersistenceModifier.PERSISTENT)
	private List<String> formats = new LinkedList<String>();

	/** @jdo.field persistence-modifier="persistent" */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private long maxSizeKB;

	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected ImageStructField() { }

	/**
	 * Create a new {@link ImageStructField} for the given {@link StructBlock} 
	 * and with primary-key values from the given {@link StructFieldID}.
	 * 
	 * @param structBlock The {@link StructBlock} the new {@link ImageStructField} will be part of.
	 * @param structFieldID The {@link StructFieldID} the new {@link ImageStructField} should take primary-key values from.
	 */
	public ImageStructField(StructBlock block, StructFieldID structFieldID) {
		super(block, structFieldID);
	}

	/**
	 * Create a new {@link ImageStructField} for the given {@link StructBlock}.
	 * 
	 * @see StructField#StructField(StructBlock)
	 * @param structBlock The {@link StructBlock} the new {@link ImageStructField} will be part of.
	 */
	public ImageStructField(StructBlock block) {
		super(block);
	}

	/*
	 * (non-Javadoc)
	 * @see org.nightlabs.jfire.prop.StructField#createDataFieldInstanceInternal(org.nightlabs.jfire.prop.DataBlock)
	 */
	@Override
	protected ImageDataField createDataFieldInstanceInternal(DataBlock dataBlock) {
		return new ImageDataField(dataBlock, this);
	}

	/**
	 * Add a new image format (file extension) to the list of valid formats of data base on this {@link ImageStructField}.
	 * @param extension The extension to add.
	 */
	public void addImageFormat(String extension) {
		if (!Pattern.matches("(\\w+|\\*)", extension))
			throw new IllegalArgumentException("Invalid extension specified.");

		if (!formats.contains(extension)) {
			formats.add(extension);

			notifyModifyListeners();
		}
	}

	public void removeImageFormat(String extension) {
		if (formats.contains(extension)) {
			formats.remove(extension);

			notifyModifyListeners();
		}
	}

	public void clearImageFormats() {
		formats.clear();

		notifyModifyListeners();
	}

	public List<String> getImageFormats() {
		return Collections.unmodifiableList(formats);
	}

	public void setMaxSizeKB(int maxKBytes) {
		this.maxSizeKB = maxKBytes;
		notifyModifyListeners();
	}

	public long getMaxSizeKB() {
		return maxSizeKB;
	}

	public boolean validateData() {
		resetValidationError();
		if (formats.isEmpty()) {
			setValidationError("You have to specify at least one extension.");
			return false;
		}
		return true;
	}

	public boolean validateSize(long sizeKB) {
		return (sizeKB <= maxSizeKB);
	}

	@Override
	public Class<ImageDataField> getDataFieldClass() {
		return ImageDataField.class;
	}

}
