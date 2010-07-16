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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jdo.FetchPlan;
import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.annotations.Column;
import javax.jdo.annotations.Discriminator;
import javax.jdo.annotations.DiscriminatorStrategy;
import javax.jdo.annotations.Element;
import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.FetchGroups;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.Join;
import javax.jdo.annotations.NullValue;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.PersistenceModifier;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;
import javax.jdo.listener.AttachCallback;
import javax.jdo.listener.DetachCallback;
import javax.jdo.listener.StoreCallback;

import org.nightlabs.inheritance.FieldInheriter;
import org.nightlabs.inheritance.FieldMetaData;
import org.nightlabs.inheritance.Inheritable;
import org.nightlabs.inheritance.StaticFieldMetaData;
import org.nightlabs.jdo.ObjectIDUtil;
import org.nightlabs.jdo.inheritance.JDOSimpleFieldInheriter;
import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.person.Person;
import org.nightlabs.jfire.prop.datafield.II18nTextDataField;
import org.nightlabs.jfire.prop.exception.DataBlockGroupNotFoundException;
import org.nightlabs.jfire.prop.exception.DataBlockNotFoundException;
import org.nightlabs.jfire.prop.exception.DataFieldNotFoundException;
import org.nightlabs.jfire.prop.exception.DataNotFoundException;
import org.nightlabs.jfire.prop.exception.StructureViolationException;
import org.nightlabs.jfire.prop.id.PropertySetID;
import org.nightlabs.jfire.prop.id.StructBlockID;
import org.nightlabs.jfire.prop.id.StructFieldID;
import org.nightlabs.jfire.prop.id.StructID;
import org.nightlabs.jfire.prop.id.StructLocalID;
import org.nightlabs.jfire.prop.validation.IPropertySetValidator;
import org.nightlabs.jfire.prop.validation.ValidationResult;
import org.nightlabs.jfire.prop.validation.ValidationResultType;
import org.nightlabs.jfire.security.SecuredObject;
import org.nightlabs.jfire.security.id.AuthorityID;
import org.nightlabs.jfire.security.id.AuthorityTypeID;
import org.nightlabs.util.Util;

/**
 * Instances of this class represent the actual set of properties
 * of an entity within JFire. Each instance stores all non-empty
 * properties of the linked entity according to its structure definition.
 * The fields actually stored are subclasses of {@link DataField}.
 * <p>
 * Although the property structure is organised in two levels (blocks and fields)
 * the data is stored in a flat {@link Set} within this class ({@link #dataFields})
 * </p>
 * <p>
 * PropertySets were created to be able to add a set of arbitrary properties
 * to all kinds of entities within JFire. The class might be extended
 * (as it was done for {@link Person}), but its recommended usage is to
 * be added as instance-member of the object you want to enrich with a
 * PropertySet.
 * </p>
 * <p>
 * To work with a PropertySet you will need to have it inflated with the
 * corresponding structure. See {@link PropertySet#inflate(IStruct)}.
 * Do not inflate persistent/attached {@link PropertySet}s as the changes
 * will be immediately reflected in the datastore and even empty data fields
 * will get stored. Also do not store/attach an inflated PropertySet, this
 * will result in an exception.
 * </p>
 * <p>
 * If you want to work with a PropertySet within the server an do not have
 * the need to inflate it, but have to access its data fields directly,
 * you should access them via the {@link #getPersistentDataField(StructFieldID, int)} method.
 * </p>
 *
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 * @author nick
 * @author Tobias Langner <!-- tobias[dot]langner[at]nightlabs[dot]de -->
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.prop.id.PropertySetID"
 *		detachable="true"
 *		table="JFireBase_Prop_PropertySet"
 *
 * @jdo.inheritance strategy="new-table"
 * @jdo.inheritance-discriminator strategy="class-name"
 *
 * @jdo.create-objectid-class field-order="organisationID, propertySetID"
 *
 * @jdo.fetch-group name="PropertySet.dataFields" fetch-groups="default" fields="dataFields"
 * @!jdo.fetch-group name="PropertySet.propTypes" fetch-groups="default" fields="propTypes"
 * @jdo.fetch-group name="PropertySet.this" fetch-groups="default" fields="dataFields"
 * @jdo.fetch-group name="FetchGroupsProp.fullData" fetch-groups="default" fields="dataFields"
 */
@PersistenceCapable(
	objectIdClass=PropertySetID.class,
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireBase_Prop_PropertySet")
@FetchGroups({
	@FetchGroup(
		fetchGroups={"default"},
		name=PropertySet.FETCH_GROUP_DATA_FIELDS,
		members=@Persistent(name="dataFields")),
	@FetchGroup(
		fetchGroups={"default"},
		name=PropertySet.FETCH_GROUP_THIS,
		members=@Persistent(name="dataFields")),
	@FetchGroup(
		fetchGroups={"default"},
		name=PropertySet.FETCH_GROUP_FULL_DATA,
		members=@Persistent(name="dataFields"))
})
@Discriminator(strategy=DiscriminatorStrategy.CLASS_NAME)
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class PropertySet implements Serializable, StoreCallback, AttachCallback, DetachCallback, Inheritable, SecuredObject
{
	/**
	 * The Log4j Logger used by this class.
	 */
	private static final org.apache.log4j.Logger LOGGER = org.apache.log4j.Logger.getLogger(PropertySet.class);

	/**
	 * The serial version of this class.
	 */
	private static final long serialVersionUID = 20090304L;

	private static Set<String> nonInheritableFields = new HashSet<String>();

	public static final long TEMPORARY_PROP_ID = -1;

	/**
	 * Fetch-group for detaching the full data of all {@link DataField}s.
	 * Each {@link DataField} implementation must add its custom fields
	 * to this fetch-group.
	 */
	public static final String FETCH_GROUP_FULL_DATA = "FetchGroupsProp.fullData";
	/**
	 * Fetch-group to detach all stored data fields of this {@link PropertySet}.
	 */
	public static final String FETCH_GROUP_DATA_FIELDS = "PropertySet.dataFields";

	/**
	 * @deprecated The *.this-FetchGroups lead to bad programming style and are therefore deprecated, now. They should be removed soon!
	 */
	@Deprecated
	public static final String FETCH_GROUP_THIS = "PropertySet.this";

	/**
	 * Virtual fetch group for data field counts.
	 * @see #getDataFieldCount()
	 */
	public static final String FETCH_GROUP_DATA_FIELD_COUNT = "PropertySet.dataFieldCount";

	/**
	 * Property constant for a {@link PropertySet}s display name.
	 */
	public static final String PROP_DISPLAY_NAME = "displayName";

	/**
	 * The {@link Struct} that this instance corresponds to.
	 * This is set after a {@link PropertySet} was inflated and is set to <code>null</code> while deflating. Thus it is ensured,
	 * that this instance is never submitted to the server.
	 *
	 * @jdo.field persistence-modifier="none"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.NONE)
	protected IStruct refStruct = null;

	/**
	 * @jdo.field persistence-modifier="none"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.NONE)
	protected Map<StructFieldID, Integer> dataFieldCount;

	/**
	 * Get the data field count as a map StuctFieldID -&gt; count.
	 * @return the dataFieldCount map
	 */
	public Map<StructFieldID, Integer> getDataFieldCount()
	{
		if(dataFieldCount == null) {
			dataFieldCount = new HashMap<StructFieldID, Integer>();
			for (DataField df : dataFields) {
				StructFieldID structFieldID = df.getStructFieldIDObj();
				Integer oldCount = dataFieldCount.get(structFieldID);
				if(oldCount == null)
					oldCount = 0;
				dataFieldCount.put(structFieldID, oldCount + 1);
			}

		}
		return dataFieldCount;
	}

	/**
	 * Never use this constructor! For JDO only!
	 */
	protected PropertySet() {
	}

	/**
	 * Create a new PropertySet.
	 * @param organisationID The organisation id
	 * @param propertySetID The property set id
	 * @param structLocal The structural definition
	 */
	public PropertySet(String organisationID, long propertySetID, StructLocal structLocal)
	{
		this(
				organisationID, propertySetID,
				structLocal.getOrganisationID(), structLocal.getLinkClassInternal(),
				structLocal.getStructScope(), structLocal.getStructLocalScope()
		);
	}

	/**
	 * Create a new PropertySet.
	 * @param organisationID The organisation id
	 * @param propertySetID The property set id
	 * @param structOrganisationID TODO
	 * @param structlLinkClass The structural definition link class
	 * @param structScope The {@link Struct} scope
	 * @param structLocalScope The {@link StructLocal} scope
	 */
	public PropertySet(
			String organisationID, long propertySetID,
			String structOrganisationID, String structlLinkClass, String structScope, String structLocalScope
	)
	{
		if ((propertySetID < 0) && (propertySetID != TEMPORARY_PROP_ID))
			throw new IllegalArgumentException("Parameter _propID has to be either >= 0 or TEMPORARY_PROP_ID");
		if (organisationID.equals(""))
			throw new IllegalArgumentException("Parameter _organisationID must not be empty");
		this.organisationID = organisationID;
		this.propertySetID = propertySetID;
		this.structOrganisationID = structOrganisationID;
		this.structLinkClass = structlLinkClass;
		this.structScope = structScope;
		this.structLocalScope = structLocalScope;
	}

	/**
	 * @jdo.field persistence-modifier="persistent" primary-key="true"
	 * @jdo.column length="100"
	 */
	@PrimaryKey
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	@Column(length=100)
	private String organisationID;

	/**
	 * @return the organisationID.
	 */
	public String getOrganisationID() {
		return organisationID;
	}

	/**
	 * @jdo.field persistence-modifier="persistent" primary-key="true"
	 */
	@PrimaryKey
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private long propertySetID;

	/**
	 * @return the propertySetID.
	 */
	public long getPropertySetID() {
		return propertySetID;
	}

	/**
	 * @jdo.field persistence-modifier="persistent" indexed="true"
	 */
	@Element(indexed="true")
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private String structOrganisationID;

	/**
	 * @jdo.field persistence-modifier="persistent" indexed="true"
	 */
	@Element(indexed="true")
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private String structLinkClass;


	/**
	 * @jdo.field persistence-modifier="persistent" indexed="true"
	 * @jdo.column length="100"
	 */
	@Element(indexed="true")
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	@Column(length=100)
	private String structScope;

	/**
	 * @jdo.field persistence-modifier="persistent" indexed="true"
	 * @jdo.column length="100"
	 */
	@Element(indexed="true")
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	@Column(length=100)
	private String structLocalScope;

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

	public void setSecuringAuthorityTypeID(AuthorityTypeID securingAuthorityTypeID) {
		if (this.securingAuthorityTypeID != null && !this.getSecuringAuthorityTypeID().equals(securingAuthorityTypeID))
			throw new IllegalStateException("A different AuthorityType has already been assigned! Cannot change this value afterwards! Currently assigned: " + this.securingAuthorityTypeID + " New value: " + securingAuthorityTypeID);

		this.securingAuthorityTypeID = securingAuthorityTypeID == null ? null : securingAuthorityTypeID.toString();
	}
	
	/**
	 * @return The scope of the StructLocal this PropertySet
	 * is build of.
	 */
	public String getStructLocalScope() {
		return structLocalScope;
	}

	/**
	 * @return The scope of the {@link Struct} this PropertySet
	 * is build of.
	 */
	public String getStructScope() {
		return structScope;
	}

	protected void setStructOrganisationID(String structOrganisationID) {
		this.structOrganisationID = structOrganisationID;
	}

	/**
	 * Set the scope of the {@link StructLocal} this PropertySet is build of.
	 * Is used by {@link #setStructLocalAttributes(StructLocal)}.
	 *
	 * @param structLocalScope the scope of the {@link StructLocal} this PropertySet is build of.
	 */
	protected void setStructLocalScope(String structLocalScope) {
		this.structLocalScope = structLocalScope;
	}

	/**
	 * Set the scope of the {@link Struct} this PropertySet is build of.
	 * Is used by {@link #setStructLocalAttributes(StructLocal)}.
	 *
	 * @param structScope The the scope of the {@link Struct} this PropertySet is build of.
	 */
	protected void setStructScope(String structScope) {
		this.structScope = structScope;
	}

	/**
	 * Set the link class of the structure this PropertySet is build of.
	 * Is used by {@link #setStructLocalAttributes(StructLocal)}.
	 *
	 * @param structLocalLinkClass The link class of the structure this PropertySet is build of.
	 */
	protected void setStructLinkClass(String structLocalLinkClass) {
		this.structLinkClass = structLocalLinkClass;
	}

	public String getStructOrganisationID() {
		return structOrganisationID;
	}

	/**
	 * @return The link class of the structure this PropertySet is build of.
	 */
	public String getStructLinkClass() {
		return structLinkClass;
	}

	/**
	 * Set the {@link #structLinkClass} and {@link #structLocalScope} attributes
	 * of this PropertySet and thus associates this set with the given structure.
	 * <p>
	 * Note that this attributes are also set when a PropertySet is
	 * inflated or deflated. See {@link PropertySet#inflate(IStruct)}, {@link PropertySet#deflate()}}
	 * </p>
	 * @param structLocal The structure to adopt.
	 */
	public void setStructLocalAttributes(StructLocal structLocal) {
		setStructOrganisationID(structLocal.getOrganisationID());
		setStructLinkClass(structLocal.getLinkClass().getName());
		setStructScope(structLocal.getStructScope());
		setStructLocalScope(structLocal.getStructLocalScope());
	}

	public StructID getStructObjectID() {
		return StructID.create(structOrganisationID, structLinkClass, structScope);
	}

	public StructLocalID getStructLocalObjectID() {
		return StructLocalID.create(structOrganisationID, structLinkClass, structScope, structLocalScope);
	}

	/**
	 * Returns a unique ID for a PropertySet.
	 * @return A unique ID for a property
	 */
	public static long createPropertySetID() {
		return IDGenerator.nextID(PropertySet.class);
	}

	/**
	 * key: String StructBlockID.getPrimaryKey(structBlockOrganisationID, structBlockID)<br/>
	 * value: DataBlockGroup propBlockGroup
	 *
	 * @jdo.field persistence-modifier="none"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.NONE)
	protected transient Map<String, DataBlockGroup> dataBlockGroups;

	/**
	 * Used internally to speed up {@link #getPersistentDataField(StructFieldID, int)}.
	 * It maps each struct field into a list of the corresponding {@link DataField}s ordered by their index.
	 *
	 * @jdo.field persistence-modifier="none"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.NONE)
	private transient Map<StructFieldID, List<DataField>> dataFieldsMap;

	/**
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="collection"
	 *		element-type="org.nightlabs.jfire.prop.DataField"
	 *		table="JFireBase_Prop_PropertySet_dataFields"
	 *		dependent-element="true"
	 *		null-value="exception"
	 *
	 * @jdo.join
	 */
	@Join
	@Persistent(
		dependentElement="true",
		nullValue=NullValue.EXCEPTION,
		table="JFireBase_Prop_PropertySet_dataFields",
		persistenceModifier=PersistenceModifier.PERSISTENT)
	private Set<DataField> dataFields = new HashSet<DataField>();

	private static Comparator<DataField> dataFieldIndexComparator = new Comparator<DataField>() {
		public int compare(DataField o1, DataField o2) {
			return Integer.valueOf(o1.getDataBlockIndex()).compareTo(o2.getDataBlockIndex());
		}
	};

	/**
	 * This class defines constants for the field names of implementation of {@link Inheritable},
	 * to avoid the use of "hard-coded" Strings for retrieving {@link FieldMetaData} or {@link FieldInheriter}.
	 * In the future the JFire project will probably auto-generate this class, but until then you should
	 * implement it manually.
	 */
	public static final class FieldName {
		public static final String autoGenerateDisplayName = "autoGenerateDisplayName";
		public static final String dataBlockGroups = "dataBlockGroups";
		public static final String dataFieldCount = "dataFieldCount";
		public static final String dataFields = "dataFields";
		public static final String dataFieldsMap = "dataFieldsMap";
		public static final String displayName = "displayName";
		public static final String nonPersistentUserObjectMap = "nonPersistentUserObjectMap";
		public static final String organisationID = "organisationID";
		public static final String propertySetID = "propertySetID";
		public static final String refStruct = "refStruct";
		public static final String structLinkClass = "structLinkClass";
		public static final String structLocalScope = "structLocalScope";
		public static final String structOrganisationID = "structOrganisationID";
		public static final String structScope = "structScope";
		public static final String trimmedDetached = "trimmedDetached";
		public static final String securingAuthorityID = "securingAuthorityID";
		public static final String securingAuthorityTypeID = "securingAuthorityTypeID";
	}

	/**
	 * Adds a {@link DataField} to the list of persistent fields.
	 * <p>
	 * Important: You should
	 * not directly access this method! It is used internally and only public for very special use-cases!
	 * </p>
	 *
	 * @param field the field to be added.
	 */
	public void internalAddDataFieldToPersistentCollection(DataField field) {
		//		for (int i = 0; i < dataFields.size(); i++)
		//		{
		//			if (field.getPropRelativePK().equals(dataFields.get(i).getPropRelativePK()))
		//			{
		//				dataFields.set(i, field);
		//				// break; // Tobias: IMHO this should be return instead of break!
		//				return;
		//			}
		//		}
		dataFields.add(field);
		dataFieldsMap = null;
	}

	/**
	 * Removes the given {@link DataField} from the persistent list. This method is only used during the deflation of a {@link PropertySet}.
	 * <p>
	 * Important: You should not directly access this method! It is used internally and only public for very special use-cases!
	 * </p>
	 *
	 * @param field the {@link DataField} to be removed.
	 *
	 * @see #deflate()
	 */
	public void internalRemoveDataFieldFromPersistentCollection(DataField field) {
		dataFields.remove(field);
		dataFieldsMap = null;
	}

	/**
	 * @return The map of {@link DataBlockGroup}s with at least the structure
	 *         built to represent the persistent data, Therefore inflation is
	 *         not necessary to call this method, however after inflation
	 *         the result will contain all {@link DataBlockGroup}s.
	 */
	protected Map<String, DataBlockGroup> getDataBlockGroupMap() {
		populateStructure();
		return dataBlockGroups;
	}

	/**
	 * @return The {@link DataBlockGroup}s of this {@link PropertySet} in random order.
	 *         Note, that only after inflation this will contain all {@link DataBlockGroup}s
	 *         according to the {@link IStruct} of this {@link PropertySet}. Before
	 *         inflation there will be only those {@link DataBlockGroup} necessary to
	 *         reflect the persistent {@link DataField}s.
	 */
	public Collection<DataBlockGroup> getDataBlockGroups() {
		return getDataBlockGroupMap().values();
	}

	/**
	 * Returns the {@link DataBlockGroup} for the {@link StructBlock} referenced with the given {@link StructBlockID}.
	 * @param structBlockID The {@link StructBlockID} of the {@link StructBlock} to reference.
	 * @return The {@link DataBlockGroup} for the {@link StructBlock} referenced with the given {@link StructBlockID}.
	 * @throws DataBlockGroupNotFoundException If no such {@link DataBlockGroup} exists yet.
	 */
	public DataBlockGroup getDataBlockGroup(StructBlockID structBlockID) throws DataBlockGroupNotFoundException {
		return getDataBlockGroup(structBlockID.structBlockOrganisationID, structBlockID.structBlockID);
	}

	/**
	 * Returns the {@link DataBlockGroup} for the {@link StructBlock} identified by the parameters.
	 *
	 * @param structBlockOrganisationID The organisationID of the {@link StructBlock} to reference.
	 * @param structBlockID The structBlockID of the {@link StructBlock} to reference.
	 * @return The {@link DataBlockGroup} for the {@link StructBlock} identified by the parameters.
	 * @throws DataBlockGroupNotFoundException If no such {@link DataBlockGroup} exists yet.
	 */
	public DataBlockGroup getDataBlockGroup(String structBlockOrganisationID, String structBlockID) throws DataBlockGroupNotFoundException {
		String structBlockPrimaryKey = StructBlock.getPrimaryKey(structBlockOrganisationID, structBlockID);
		DataBlockGroup pdbg = getDataBlockGroupMap().get(structBlockPrimaryKey);
		if (pdbg == null) {
			throw new DataBlockGroupNotFoundException("Could not find DataBlockGroup for (structBlockOrganisationID/structBlockID)="
					+ structBlockPrimaryKey);
		}
		return pdbg;
	}

	/**
	 * Returns the {@link DataBlockGroup} for the references {@link StructBlock}
	 * out of {@link #dataBlockGroups}, or <code>null</code> if none present.
	 * This must only be called after {@link #dataBlockGroups} has been created.
	 *
	 * @param structBlockOrganisationID The organisationID of the {@link StructBlock} to reference.
	 * @param structBlockID The structBlockID of the {@link StructBlock} to reference.
	 * @return The {@link DataBlockGroup} for the references {@link StructBlock}
	 *         out of {@link #dataBlockGroups}, or <code>null</code> if none present.
	 */
	private DataBlockGroup getStructBlockRepresentation(String structBlockOrganisationID, String structBlockID) {
		String structBlockPrimaryKey = StructBlock.getPrimaryKey(structBlockOrganisationID, structBlockID);
		return dataBlockGroups.get(structBlockPrimaryKey);
		//		return getDataBlockGroupMap().get(structBlockPrimaryKey);
	}

	/**
	 * Shortcut method to access a PropDataField directly. Takes all parts of the primary key of a
	 * DataField and the ID of the DataBlock to get the field from.
	 *
	 * @param dataBlockOrganisationID The organisationID of the {@link StructBlock} the {@link DataField} references.
	 * @param dataBlockID The dataBlockID of the {@link StructBlock} the {@link DataField} references.
	 * @param dataFieldOrganisationID The organisationID of the {@link StructField} the {@link DataField} references.
	 * @param dataFieldID The dataFieldID of the {@link StructField} the {@link DataField} references.
	 * @param dataBlockIdx The index of the {@link DataBlock} the {@link DataField} should reference.
	 *                     Note, that this does not correspond to the {@link DataBlock#getDataBlockID()},
	 *                     but is the index of the {@link DataBlock} within the found {@link DataBlockGroup}.
	 *
	 * @return The {@link DataField} identified by the given parameters.
	 * @throws DataBlockGroupNotFoundException If the referenced {@link DataBlockGroup} could not be found.
	 * @throws DataBlockNotFoundException If the referenced {@link DataBlock} could not be found.
	 * @throws DataFieldNotFoundException If the referenced {@link DataField} could not be found.
	 */
	public DataField getDataField(String dataBlockOrganisationID, String dataBlockID, String dataFieldOrganisationID, String dataFieldID,
			int dataBlockIdx) throws DataBlockGroupNotFoundException, DataBlockNotFoundException, DataFieldNotFoundException {
		DataBlockGroup blockGroup = getDataBlockGroup(dataBlockOrganisationID, dataBlockID);
		DataBlock block = blockGroup.getDataBlockByIndex(dataBlockIdx);
		DataField dataField = block.getDataField(dataFieldOrganisationID, dataFieldID);
		return dataField;
	}

	/**
	 * Calls {@link #getDataField(String, String, String, String, int)} with propBlockIdx = 0
	 * and thus finds the first DataBlock in the referenced {@link DataBlockGroup}.
	 *
	 * @param dataBlockOrganisationID The organisationID of the {@link StructBlock} the {@link DataField} references.
	 * @param dataBlockID The dataBlockID of the {@link StructBlock} the {@link DataField} references.
	 * @param dataFieldOrganisationID The organisationID of the {@link StructField} the {@link DataField} references.
	 * @param dataFieldID The dataFieldID of the {@link StructField} the {@link DataField} references.
	 *
	 * @return The first {@link DataField} identified by the given parameters.
	 * @throws DataBlockGroupNotFoundException If the referenced {@link DataBlockGroup} could not be found.
	 * @throws DataBlockNotFoundException If the referenced {@link DataBlock} could not be found.
	 * @throws DataFieldNotFoundException If the referenced {@link DataField} could not be found.
	 */
	public DataField getDataField(String dataBlockOrganisationID, String dataBlockID, String dataFieldOrganisationID, String dataFieldID)
	throws DataBlockNotFoundException, DataBlockGroupNotFoundException, DataFieldNotFoundException {
		return getDataField(dataBlockOrganisationID, dataBlockID, dataFieldOrganisationID, dataFieldID, 0);
	}

	/**
	 * Calls {@link #getDataField(String, String, String, String)} and
	 * thus finds the first DataBlock in the referenced {@link DataBlockGroup}.
	 *
	 * @param structFieldID The {@link StructFieldID} of the {@link StructField} the {@link DataField} references.
	 *
	 * @return The first {@link DataField} identified by the given {@link StructFieldID}.
	 * @throws DataBlockGroupNotFoundException If the referenced {@link DataBlockGroup} could not be found.
	 * @throws DataBlockNotFoundException If the referenced {@link DataBlock} could not be found.
	 * @throws DataFieldNotFoundException If the referenced {@link DataField} could not be found.
	 */
	public DataField getDataField(StructFieldID structFieldID) throws DataBlockNotFoundException, DataBlockGroupNotFoundException,
	DataFieldNotFoundException {
		return getDataField(structFieldID.structBlockOrganisationID, structFieldID.structBlockID, structFieldID.structFieldOrganisationID,
				structFieldID.structFieldID);
	}

	/**
	 * Gets the {@link DataField} for the given {@link StructFieldID} and tries to cast it as the given type.
	 *
	 * @param <T> The type to cast the returned DataField to.
	 * @param structFieldID The {@link StructFieldID} of the {@link StructField} the {@link DataField} to return references.
	 * @param dataFieldType The type the found {@link DataField} should be casted to.
	 * @return The first {@link DataField} identified by the given {@link StructFieldID} casted to the given type.
	 * @throws DataBlockNotFoundException
	 * @throws DataBlockGroupNotFoundException
	 * @throws DataFieldNotFoundException
	 */
	public <T> T getDataField(StructFieldID structFieldID, Class<T> dataFieldType)
	throws DataBlockNotFoundException, DataBlockGroupNotFoundException, DataFieldNotFoundException {
		return (T) getDataField(structFieldID);
	}

	/**
	 * Get all {@link DataField}s in this PropertySet that are of the given type
	 * and that are in a {@link DataBlock} with the given index in each corresponding {@link DataBlockGroup}.
	 * <p>
	 * This method might be called on inflated and deflated instances of a {@link PropertySet}.
	 * </p>
	 * <p>
	 * Note, that this method silently ignores if it does not find a {@link DataBlock} for
	 * the given index in a {@link DataBlockGroup}.
	 * </p>
	 *
	 * @param <T> The type of DataFields to get.
	 * @param dataFieldType The type of DataFields to get.
	 * @param index The index of the DataBlock to get
	 * @return All {@link DataField}s in {@link DataBlock}s with the given index and of the given type.
	 */
	public <T> Collection<T> getDataFields(Class<T> dataFieldType, int index) {
		Collection<T> result = new ArrayList<T>();
		for (DataBlockGroup dbg : getDataBlockGroups()) {
			DataBlock db;
			try {
				db = dbg.getDataBlockByIndex(index);
			} catch (DataBlockNotFoundException e) {
				// we silently ignore, if this datablock is not existent
				continue;
			}
			for (DataField dataField : db.getDataFields()) {
				if (dataFieldType.isAssignableFrom(dataFieldType)) {
					result.add((T) dataField);
				}
			}
		}
		return result;
	}

	/**
	 * Get all {@link DataField}s in this that are in a {@link DataBlock} with the given index.
	 * <p>
	 * This method might be called on inflated and deflated instances of a {@link PropertySet}.
	 * </p>
	 * <p>
	 * Note, that this method silently ignores if it does not find a {@link DataBlock} for
	 * the given index in a {@link DataBlockGroup}.
	 * </p>
	 *
	 * @param index The index of the DataBlock to get
	 * @return All {@link DataField}s in {@link DataBlock}s with the given index.
	 */
	public Collection<DataField> getDataFields(int index) {
		return getDataFields(DataField.class, index);
	}

	public Collection<DataField> getDataFields() {
		return Collections.unmodifiableSet(dataFields);
	}

	/**
	 * Returns the DataFields of all DataBlockGroups that represent the given {@link StructFieldID}.
	 * This means if the {@link DataBlockGroup} for the StructBlock the given StructField is in
	 * has - let's say - 3 DataBlock entries, the resulting List will have 3 entries as well.
	 *
	 * @param structFieldID The {@link StructFieldID} to get all {@link DataField}s for.
	 *
	 * @return All {@link DataField}s for the given {@link StructFieldID}.
	 * @throws DataBlockGroupNotFoundException If the referenced {@link DataBlockGroup} could not be found.
	 * @throws DataFieldNotFoundException If the referenced {@link DataField} could not be found.
	 */
	public List<DataField> getDataFields(StructFieldID structFieldID) throws DataBlockGroupNotFoundException, DataFieldNotFoundException {
		List<DataField> result = new LinkedList<DataField>();
		DataBlockGroup blockGroup = getDataBlockGroup(structFieldID.structBlockOrganisationID, structFieldID.structBlockID);
		for (DataBlock dataBlock : blockGroup.getDataBlocks()) {
			DataField dataField = dataBlock.getDataField(structFieldID.structFieldOrganisationID, structFieldID.structFieldID);
			result.add(dataField);
		}
		return result;
	}

//	/**
//	 * @jdo.field persistence-modifier="persistent"
//	 */
//	private String languageID = LanguageID.SYSTEM;
//
//	/**
//	 * Get this props languageID.
//	 *
//	 * @return the language id of this {@link PropertySet}
//	 */
//	public String getLanguageID() {
//		return languageID;
//	}
//
//	/**
//	 * Set this props languageID.
//	 *
//	 * @param _propLanguageID
//	 */
//	public void setLanguageID(String _propLanguageID) {
//		this.languageID = _propLanguageID;
//	}

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private String displayName;

	/**
	 * @return Returns the displayName.
	 */
	public String getDisplayName() {
		return displayName;
	}

	/**
	 * Sets the displayName either to the value passed or when
	 * autoGenerateDisplayName is true it will be generated according to the
	 * displayNameParts in the structure this {@link PropertySet} was inflated with.
	 * <p>
	 * Note, that this method should only be called on inflated {@link PropertySet}s
	 * when {@link #isAutoGenerateDisplayName()} is <code>true</code> as
	 * it has to access the structure then.
	 * </p>
	 *
	 * @param displayName The displayName to set.
	 */
	public void setDisplayName(String displayName) {
		setDisplayName(displayName, null);
	}

	/**
	 * Sets the displayName either to the value passed or when
	 * autoGenerateDisplayName is true it will be generated according to the
	 * displayNameParts in the given structure.
	 *
	 * @param displayName The displayName to set.
	 */
	public void setDisplayName(String displayName, IStruct structure) {
		String lastSuffix = "";

		if (structure == null) {
			structure = refStruct;
		}

		if (structure == null)
			setAutoGenerateDisplayName(false);

		if (!isAutoGenerateDisplayName())
			this.displayName = displayName;
		else {
			this.displayName = "";
			displayName = "";
			for (DisplayNamePart displayNamePart : structure.getDisplayNameParts()) {
				DataField field = null;
				try {
					field = getDataField(displayNamePart.getStructField().getStructBlockOrganisationID(), displayNamePart.getStructField().getStructBlockID(),
							displayNamePart.getStructField().getStructFieldOrganisationID(), displayNamePart.getStructField().getStructFieldID());
				} catch (DataNotFoundException e) {
					// prop does not have this field
					continue;
				}
				if (!field.isEmpty()) {
					if (field instanceof II18nTextDataField) {
//						displayName += ((II18nTextDataField) field).getText(new Locale(languageID)) + displayNamePart.getStructFieldSuffix();
						displayName += ((II18nTextDataField) field).getI18nText().getText() + displayNamePart.getStructFieldSuffix();
						lastSuffix = displayNamePart.getStructFieldSuffix();
					}
				}
			} // for ...
			// at least one was added
			this.displayName = displayName.substring(0, displayName.length() - lastSuffix.length());
		}
	}

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private boolean autoGenerateDisplayName = true;

	/**
	 * Returns whether the displayName of this {@link PropertySet}
	 * should be created automatically out of the {@link DisplayNamePart}s
	 * in the structure for this {@link PropertySet}.
	 *
	 * @return Returns the autoGenerateDisplayName.
	 */
	public boolean isAutoGenerateDisplayName() {
		return this.autoGenerateDisplayName;
	}

	/**
	 * Define whether the displayName of this {@link PropertySet}
	 * should be created automatically out of the {@link DisplayNamePart}s
	 * in the structure for this {@link PropertySet}.
	 * <p>
	 * This value will be taken into account by {@link #setDisplayName(String)}
	 * and {@link #setDisplayName(String, IStruct)}.
	 * </p>
	 * <p>
	 * Note that if the value is set to <code>true</code> on an inflated
	 * {@link PropertySet} the display name is updated by this method.
	 * Otherwise (and in case the underlying data fields have changed)
	 * the method {@link #setDisplayName(String)} should be called again
	 * as there is no listener mechanism that would automatically update
	 * the display name.
	 * However, before a {@link PropertySet} get deflated it will also
	 * update is display name if the value is <code>true</code>.
	 * </p>
	 * @param _autoGenerateDisplayName The autoGenerateDisplayName to set.
	 */
	public void setAutoGenerateDisplayName(boolean _autoGenerateDisplayName) {
		this.autoGenerateDisplayName = _autoGenerateDisplayName;
		updateAutoGenDisplayName();
	}

	/**
	 * Checks whether the {@link #autoGenerateDisplayName} flag
	 * is set and updates the display name according to the given
	 * rules. Note that the update will only be done if the
	 * {@link PropertySet} is inflated.
	 */
	protected void updateAutoGenDisplayName() {
		if (isInflated() && autoGenerateDisplayName) {
			setDisplayName(null);
		}
	}

	/**
	 * Returns a '/' separated String of the primary key fields given.
	 * @param organisationID The organisationID to use.
	 * @param propertySetID The propertySetID to use.
	 * @return A '/' separated String of the primary key fields given.
	 */
	public static String getPrimaryKey(String organisationID, long propertySetID) {
		return organisationID + "/" + ObjectIDUtil.longObjectIDFieldToString(propertySetID);
	}

	/**
	 * If this PropertySet was instantiated with {@link #TEMPORARY_PROP_ID} as propertySetID
	 * this method will iterate through all DataBlocks and DataFields and set the
	 * propertySetID member of them to the given _propID. The prop must not have been
	 * made persistent before calling this method this will cause an
	 * {@link IllegalStateException}.
	 *
	 * @param _propertySetID The propertySetID to assign.
	 */
	public void assignID(long _propertySetID) {
		if (JDOHelper.isPersistent(this))
			throw new IllegalStateException("initPropID(long) must not be called for already persistent Props!");
		if (propertySetID != TEMPORARY_PROP_ID)
			throw new IllegalStateException("initPropID(long) must not be called for Props with a propertySetID other than TEMPORARY_PROP_ID!");

		// set the id in the virtual structure only when inflated.
		if (isInflated()) {
			for (DataBlockGroup blockGroup : dataBlockGroups.values()) // all block groups
			{
				blockGroup.setPropertySetID(_propertySetID);
				for (DataBlock block : blockGroup.dataBlockMap.values()) // all blocks within
				{
					block.setPropertySetID(_propertySetID);
					for (DataField field : block.dataFields.values())
						// all fields within
						field.setPropertySetID(_propertySetID);
				}
			}
		}
		// set id in the persistent structure
		for (DataField field : dataFields) {
			field.setPropertySetID(_propertySetID);
		}
		this.propertySetID = _propertySetID;
	}

	/**
	 * Validates if all {@link DataField}s of this property have a corresponding
	 * {@link StructField} in the passed <code>struct</code>.
	 *
	 * @param refStruct The struct against which the validation shall be performed.
	 * @throws StructureViolationException if at least one {@link DataField} does not have its
	 * corresponding {@link StructField}.
	 */
	public void validateStructure(IStruct refStruct) throws StructureViolationException {
		for (DataField field : dataFields) {
			if (!refStruct.containsDataField(field))
				throw new StructureViolationException("The structure of this property does not match " + refStruct);
		}
	}

	/**
	 * Adds the given {@link DataBlockGroup} to this property set. This method should only be used during the inflation process,
	 * hence it is package-private.
	 * @param dataBlockGroup The {@link DataBlockGroup} to be added
	 * @param structBlockOrganisationID Part of the primary key of the corresponding {@link StructBlock}
	 * @param structBlockID Part of the primary key of the corresponding {@link StructBlock}
	 */
	void addDataBlockGroup(DataBlockGroup dataBlockGroup, String structBlockOrganisationID, String structBlockID) {
		String structBlockPrimaryKey = StructBlock.getPrimaryKey(structBlockOrganisationID, structBlockID);
		if (dataBlockGroups == null)
			dataBlockGroups = new HashMap<String, DataBlockGroup>();
		dataBlockGroups.put(structBlockPrimaryKey, dataBlockGroup);
	}

	/**
	 * This method creates {@link DataBlockGroup}s and {@link DataBlock}s for all non-empty {@link DataField}s
	 * in this property set. If this method has already been called before, it does nothing.
	 */
	private void populateStructure() {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("populating structure...");
		}
		if (dataBlockGroups != null)
			return;

		dataBlockGroups = new HashMap<String, DataBlockGroup>();

		for (DataField dataField : dataFields) {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Index of DataBlock of currently considered DataField: " + dataField.getDataBlockIndex());
			}

			String structBlockID = dataField.getStructBlockID();
			String structBlockOrganisationID = dataField.getStructBlockOrganisationID();

			// Make sure that we have a DataBlockGroup
			DataBlockGroup dataBlockGroup = getStructBlockRepresentation(structBlockOrganisationID, structBlockID);
			if (dataBlockGroup == null) {
				dataBlockGroup = new DataBlockGroup(this, structBlockOrganisationID, structBlockID);
				addDataBlockGroup(dataBlockGroup, structBlockOrganisationID, structBlockID);
			}

			// Make sure that we have a DataBlock
			int dataBlockID = dataField.getDataBlockID();
			DataBlock dataBlock;
			try {
				dataBlock = dataBlockGroup.getDataBlock(dataBlockID);
			} catch (DataBlockNotFoundException e) {
				dataBlock = new DataBlock(dataBlockGroup, dataField.getDataBlockID());
				dataBlock.setIndex(dataField.getDataBlockIndex(), false);	// Set DataBlock index according to the value stored in the currently considered DataField that is part of this DataBlock. Otherwise -1 would be initially set as index for this DataBlock and sorting in DataBlockGroup#inflate would lead to a false random result.
				dataBlockGroup.addDataBlock(dataBlock);
			}
			dataBlock.addDataFieldToStructure(dataField);
		}
	}

	/**
	 * This method performs the following steps:
	 * <ol>
	 * <li>Validates if all {@link DataField}s of this property set have a corresponding
	 * {@link StructField} in the passed <code>struct</code>.</li>
	 * <li>Add all {@link DataField}s of this {@link PropertySet} to the structure.</li>
	 * <li>Ensure that for each {@link StructField} of the given {@link Struct}, a {@link DataField} in a {@link DataBlock} in a {@link DataBlockGroup}
	 * exists.</li>
	 * </ol>
	 *
	 * @param structure The structure to be used for inflating.
	 */
	public void inflate(IStruct structure) {
		try {
			validateStructure(structure);
		} catch (StructureViolationException e) {
			throw new IllegalArgumentException("Given structure is not valid for this property set.", e);
		}

		setRefStruct(structure);

		// Create all DataBlockGroups and DataBlocks for existing fields if that has not already happened
		populateStructure();

		// Create DataBlockGroups and DataBlocks with empty fields for non-existing fields
		for (StructBlock structBlock : structure.getStructBlocks()) {
			DataBlockGroup dataBlockGroup = getStructBlockRepresentation(structBlock.getStructBlockOrganisationID(), structBlock.getStructBlockID());
			if (dataBlockGroup == null) {
				dataBlockGroup = new DataBlockGroup(this, structBlock.getStructBlockOrganisationID(), structBlock.getStructBlockID());
				addDataBlockGroup(dataBlockGroup, structBlock.getStructBlockOrganisationID(), structBlock.getStructBlockID());
			}

			dataBlockGroup.inflate(structBlock);
		}
	}

	/**
	 * Scans all DataBlockGroups, its containing DataBlocks and DataFields and
	 * removes all entries where isEmpty() returns true.
	 * @param propertySet The propertySet to deflate.
	 */
	public void deflate() {
		if (!isInflated())
			return;

		updateAutoGenDisplayName();

		for (Iterator<Map.Entry<String, DataBlockGroup>> it = getDataBlockGroupMap().entrySet().iterator(); it.hasNext();) {
			DataBlockGroup blockGroup = it.next().getValue();
			blockGroup.deflate();
			if (blockGroup.isEmpty())
				it.remove();
		}
		if (refStruct instanceof StructLocal) {
			setStructLocalAttributes((StructLocal) refStruct);
		}
		setRefStruct(null);
	}

	/**
	 * Sets the {@link Struct} this instance corresponds to.
	 * @param refStruct
	 */
	private void setRefStruct(IStruct refStruct) {
		this.refStruct = refStruct;
	}

	/**
	 * Returns the {@link IStruct} this {@link PropertySet}
	 * is build of.
	 * <p>
	 * Call this method only on inflated instances of
	 * {@link PropertySet}. If called on deflated instance
	 * a {@link IllegalStateException} will be thrown.
	 * </p>
	 * @return The {@link IStruct} this {@link PropertySet}
	 *         is build of.
	 */
	public IStruct getStructure() {
		if (!isInflated())
			throw new IllegalStateException("Cannot retrieve structure of imploded property.");
		else
			return refStruct;
	}

	/**
	 * Returns whether this instance of {@link PropertySet}
	 * was inflated according to its structure.
	 *
	 * See {@link #inflate(IStruct)}.
	 *
	 * @return Whether this instance of {@link PropertySet} was inflated.
	 */
	public boolean isInflated() {
		return refStruct != null;
	}

	/**
	 * Creates a clone of this {@link PropertySet} and
	 * assigns it a clone of each {@link DataField} within this {@link PropertySet}.
	 *
	 * @return The newly created clone.
	 */
	public PropertySet clonePropertySet() {
		PropertySet newProperty = createPropertySetClone();
		for (DataField dataField : dataFields) {
			DataField newField = dataField.cloneDataField(newProperty, -1);
			newProperty.dataFields.add(newField);
		}
		return newProperty;
	}

	/**
	 * This method creates a clone instance of this {@link PropertySet}
	 * without any data assigned.
	 * <p>
	 * This method should be overridden by subclasses and an instance
	 * of the correct type has to be created there.
	 * </p>
	 * @return The newly created clone of this {@link PropertySet} without any data.
	 */
	protected PropertySet createPropertySetClone() {
		return new PropertySet(
				IDGenerator.getOrganisationID(), IDGenerator.nextID(PropertySet.class),
				getStructOrganisationID(),
				getStructLinkClass(), getStructLocalScope(), getStructLocalScope()
		);
	}

	/**
	 * Ensures no inflated instances of {@link PropertySet} can be attached.
	 */
	@Override
	public void jdoPreStore() {
		// TODO reactivate this - currently a JPOX bug causes a JDOObjectNotFoundException when copying a Person from one datastore to another. Marco. 2007-02-15
		//		if (isInflated())
		//			throw new IllegalStateException("You just attempted to store an exploded property");
	}

	/**
	 * @return The map of all persistent dataFields accessible by their id.
	 */
	Map<StructFieldID, List<DataField>> getDataFieldsMap() {
		if (dataFieldsMap == null) {
			dataFieldsMap = new HashMap<StructFieldID, List<DataField>>();
			for (DataField dataField : dataFields) {
				StructFieldID structFieldID = StructFieldID.create(dataField.getStructBlockOrganisationID(), dataField.getStructBlockID(), dataField.getStructFieldOrganisationID(), dataField.getStructFieldID());
				if (!dataFieldsMap.containsKey(structFieldID))
					dataFieldsMap.put(structFieldID, new ArrayList<DataField>());

				dataFieldsMap.get(structFieldID).add(dataField);
			}

			for (List<DataField> dataFields : dataFieldsMap.values()) {
				Collections.sort(dataFields, dataFieldIndexComparator);
			}
		}
		return dataFieldsMap;
	}

	/**
	 * This method serves to load one {@link DataField} without the need to detach and explode
	 * a <code>PropertySet</code>. If the DataField does not exist, it won't be created, but this method returns
	 * <code>null</code>. The result of this method should be seen as read-only (even though it is possible to modify
	 * an existing DataField). If you intend to edit a PropertySet, you should always detach, inflate, modify, deflate, attach.
	 *
	 * @param structFieldID The {@link StructFieldID} of the data field to search.
	 * @param dataBlockID The id/number of the {@link DataBlock} the field should be taken from.
	 * @return The {@link DataField} for the given key, or <code>null</code> it this can't be found.
	 */
	public DataField getPersistentDataField(StructFieldID structFieldID, int dataBlockID) {
		Collection<DataField> dataFields = getDataFieldsMap().get(structFieldID);
		if (dataFields == null)
			return null;

		for (DataField dataField : dataFields) {
			if (dataField.getDataBlockID() == dataBlockID)
				return dataField;
		}
		return null;
	}

	/**
	 * This method serves to load one {@link DataField} without the need to detach and explode
	 * a <code>PropertySet</code>. If the DataField does not exist, it won't be created, but this method returns
	 * <code>null</code>. The result of this method should be seen as read-only (even though it is possible to modify
	 * an existing DataField). If you intend to edit a PropertySet, you should always detach, inflate, modify, deflate, attach.
	 *
	 * @param structFieldID The {@link StructFieldID} of the data field to search.
	 * @param dataBlockIndex The index of the {@link DataBlock} the field should be taken from.
	 * @return The {@link DataField} for the given key, or <code>null</code> it this can't be found.
	 */
	public DataField getPersistentDataFieldByIndex(StructFieldID structFieldID, int dataBlockIndex) {
		List<DataField> dataFields = getDataFieldsMap().get(structFieldID);
		if (dataFields == null)
			return null;

		if (dataFields.size() <= dataBlockIndex)
			return null;

		return dataFields.get(dataBlockIndex);
	}

	/**
	 * Checks whether this {@link PropertySet} has one {@link DataField} for the given {@link StructFieldID}.
	 * <p>
	 * This method might be called on inflated and deflated instances of a {@link PropertySet}.
	 * </p>
	 * @param structFieldID The id of the {@link StructField} to check for a {@link DataField} representation.
	 * @return Whether this {@link PropertySet} has one {@link DataField} for the given {@link StructFieldID}.
	 */
	public boolean containsDataField(StructFieldID structFieldID) {
		return getDataFieldsMap().containsKey(structFieldID);
	}

	/**
	 * This field is used when a {@link PropertySet} is trimmed after detach.
	 * A trimmed detached PropertySet can not be attached any more. This field will be checked and
	 * an exception will be thrown.
	 *
	 * @jdo.field persistence-modifier="none"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.NONE)
	private boolean trimmedDetached = false;

	/**
	 * @return Whether this instance of PropertySet was detached with a trimmed list of
	 *         StructFields. Note, that trimmed-detached PropertySets can't be re-attached.
	 */
	public boolean isTrimmedDetached() {
		return trimmedDetached;
	}
	
	/**
	 * Does nothing.
	 */
	@Override
	public void jdoPostAttach(Object arg0) {
		// do nothing
	}

	/**
	 * Ensures not trimmed detached instances of {@link PropertySet} can be re-attached.
	 */
	public void jdoPreAttach() {
		if (trimmedDetached)
			throw new UnsupportedOperationException("Trimmed detached PropertySets are not allowed to be re-attached");
		
		// commented check because DataNucleus does not find Authority with given ID although it exists in the datastore
//		PersistenceManager pm = NLJDOHelper.getThreadPersistenceManager();
//		if (pm == null)
//			throw new IllegalStateException("This instance of " + this.getClass().getName() + " is not yet persistent or currently not attached to a datastore! Cannot obtain PersistenceManager!");
//		if (this.securingAuthorityID != null) {
//			// WORKAROUND because otherwise DataNucleus throws JDOObjectNotFound although object exists
//			pm.getExtent(Authority.class);
//			// Check if the AuthorityType is correct. This is already done by JFireSecurityManager.assignAuthority(...), but just to be absolutely sure since this method might be called by someone else.
//			final Authority authority = (Authority) pm.getObjectById(securingAuthorityID);
//			final AuthorityType securingAuthorityType = (AuthorityType) pm.getObjectById(getSecuringAuthorityTypeID());
//			if (!authority.getAuthorityType().equals(securingAuthorityType))
//				throw new IllegalArgumentException("securingAuthority.authorityType does not match this.securingAuthorityTypeID! securingAuthority: " + securingAuthorityID + " this: " + organisationID + "/" + propertySetID);
//		}		
	}

	/**
	 * Returns a detached copy of the given {@link PropertySet} whose list of {@link DataField}s will be trimmed
	 * so it will only contain these fields referenced in the given structFieldIDs.
	 * <p>
	 * Note that {@link PropertySet}s detached this way can not be re-attached.
	 * </p>
	 *
	 * @param pm The {@link PersistenceManager} to use.
	 * @param propertySet The property to detach and trim.
	 * @param structFieldIDs The Set of {@link StructFieldID}s that should be included in the trimmed copy.
	 * @param fetchGroups The fetchGroup to detach the {@link PropertySet} with.
	 * @param maxFetchDepth The maxFetchDepth for the detach fetch plan
	 * @return A detached PropertySet trimmed to include only the given structFieldIDs.
	 */
	public static PropertySet detachPropertySetWithTrimmedFieldList(PersistenceManager pm, PropertySet propertySet, Set<StructFieldID> structFieldIDs,
			String[] fetchGroups, int maxFetchDepth) {
		Set<String> _fetchGroups = new HashSet<String>();
		if (fetchGroups != null)
			_fetchGroups.addAll(Arrays.asList(fetchGroups));
		_fetchGroups.add(PropertySet.FETCH_GROUP_DATA_FIELDS);
		
		// FIXME: DataNucleus WORKAROUND BEGIN: This is a workaround for: 
		/*		
		[Persistence] DETACH ERROR : Error thrown while detaching org.nightlabs.jfire.person.Person@d0a532 (id=jdo/org.nightlabs.jfire.prop.id.PropertySetID?organisationID=chezfrancois.jfire.org&propertySetID=1h)
		java.lang.NullPointerException                                                                                                                                                                                 
		        at org.datanucleus.store.rdbms.query.PersistentIDROF.getObjectForApplicationId(PersistentIDROF.java:377)                                                                                               
		        at org.datanucleus.store.rdbms.query.PersistentIDROF.getObject(PersistentIDROF.java:276)                                                                                                               
		        at org.datanucleus.store.mapped.scostore.SetStoreIterator.<init>(SetStoreIterator.java:104)                                                                                                            
		        at org.datanucleus.store.rdbms.scostore.RDBMSSetStoreIterator.<init>(RDBMSSetStoreIterator.java:40)                                                                                                    
		        at org.datanucleus.store.rdbms.scostore.RDBMSJoinSetStore.iterator(RDBMSJoinSetStore.java:666)                                                                                                         
		        at org.datanucleus.sco.backed.Set.loadFromStore(Set.java:286)                                                                                                                                          
		        at org.datanucleus.sco.backed.Set.initialise(Set.java:235)                                                                                                                                             
		        at org.datanucleus.sco.SCOUtils.newSCOInstance(SCOUtils.java:183)                                                                                                                                      
		        at org.datanucleus.store.mapped.mapping.AbstractContainerMapping.replaceFieldWithWrapper(AbstractContainerMapping.java:426)                                                                            
		        at org.datanucleus.store.mapped.mapping.AbstractContainerMapping.postFetch(AbstractContainerMapping.java:444)                                                                                          
		        at org.datanucleus.store.rdbms.request.FetchRequest2.execute(FetchRequest2.java:391)                                                                                                                   
		        at org.datanucleus.store.rdbms.RDBMSPersistenceHandler.fetchObject(RDBMSPersistenceHandler.java:271)                                                                                                   
		        at org.datanucleus.state.JDOStateManagerImpl.loadUnloadedFieldsInFetchPlan(JDOStateManagerImpl.java:1627)                                                                                              
		        at org.datanucleus.state.JDOStateManagerImpl.detachCopy(JDOStateManagerImpl.java:3623)                                                                                                                 
		        at org.datanucleus.ObjectManagerImpl.detachObjectCopy(ObjectManagerImpl.java:1880)                                                                                                                     
		        at org.datanucleus.jdo.JDOPersistenceManager.jdoDetachCopy(JDOPersistenceManager.java:1105)                                                                                                            
		        at org.datanucleus.jdo.JDOPersistenceManager.detachCopy(JDOPersistenceManager.java:1134)                                                                                                               
		        at org.datanucleus.jdo.connector.PersistenceManagerImpl.detachCopy(PersistenceManagerImpl.java:883)                                                                                                    
		        at org.nightlabs.jfire.prop.PropertySet.detachPropertySetWithTrimmedFieldList(PropertySet.java:1259)                                                                                                   
		        at org.nightlabs.jfire.prop.PropertyManagerBean.getDetachedTrimmedPropertySets(PropertyManagerBean.java:233)
		*/
		// Without adding the default fetch-group the above exception happens
		_fetchGroups.add(FetchPlan.DEFAULT);
				// WORKAROUND END
		
		pm.getFetchPlan().setGroups(_fetchGroups);
		pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
		
		
		PropertySet detached = pm.detachCopy(propertySet);
		for (Iterator<DataField> iter = detached.dataFields.iterator(); iter.hasNext();) {
			DataField field = iter.next();
			StructFieldID dataStructFieldID = field.getStructFieldIDObj();

			if (!structFieldIDs.contains(dataStructFieldID))
				iter.remove();
		}
		detached.trimmedDetached = true;
		return detached;
	}

	@Override
	public FieldInheriter getFieldInheriter(String fieldName) {
		if (fieldName.equals(FieldName.autoGenerateDisplayName)
				|| fieldName.equals(FieldName.displayName))
			return new JDOSimpleFieldInheriter();

		if (fieldName.equals(FieldName.dataFields))
			return new PropertySetFieldInheritor();

		// Other fields will not be inherited.
		return null;
	}

	@Override
	public FieldMetaData getFieldMetaData(String fieldName) {
		if (fieldName.startsWith("jdo"))
			return null;

		synchronized (nonInheritableFields)
		{
			if (nonInheritableFields.isEmpty())
			{
				nonInheritableFields.add(FieldName.organisationID);
				nonInheritableFields.add(FieldName.propertySetID);
				nonInheritableFields.add(FieldName.dataBlockGroups);
				nonInheritableFields.add(FieldName.dataFieldCount);
				nonInheritableFields.add(FieldName.dataFieldsMap);
				nonInheritableFields.add(FieldName.nonPersistentUserObjectMap);
				nonInheritableFields.add(FieldName.refStruct);
				nonInheritableFields.add(FieldName.trimmedDetached);

				// If the following fields are different between source-PropertySet and destination-PropertySet, we do NOT inherit at all.
				nonInheritableFields.add(FieldName.structOrganisationID);
				nonInheritableFields.add(FieldName.structLinkClass);
				nonInheritableFields.add(FieldName.structScope);
				nonInheritableFields.add(FieldName.structLocalScope);
				
				// for the beginning we don't inherit the securingAuthorityID and securingAuthorityTypeID
				nonInheritableFields.add(FieldName.securingAuthorityID);
				nonInheritableFields.add(FieldName.securingAuthorityTypeID);
			}
			if (nonInheritableFields.contains(fieldName))
				return null;
		}

		// TODO for now, we inherit everything, but later, we should be able to enable/disable inheritance
		// - per *struct*block (i.e. all datablocks)
		// - per data block
		// - per data field
		// This all should be stored in a very efficient way - maybe use a simple implementation of MapFieldMetaData and encode the scope into the key-string.
		return new StaticFieldMetaData(fieldName);
	}

	/**
	 * Validates this instance against the given {@link IStruct} and returns a list of all {@link ValidationFailureResult}
	 * occurred during the validation or <code>null</code> if the validation succeeded.
	 * <p>
	 * This method can be called on inflated and deflated instances of {@link PropertySet}.
	 * </p>
	 *
	 * @param struct The {@link IStruct} against which to validated.
	 * @return A list of all {@link ValidationFailureResult} occurred during the validation or <code>null</code> if the validation succeeded..
	 */
	public List<ValidationResult> validate(IStruct struct) {
		List<ValidationResult> results = new LinkedList<ValidationResult>();
		for (IPropertySetValidator validator : struct.getPropertySetValidators()) {
			ValidationResult validationFailureResult = validator.validate(this, struct);
			if (validationFailureResult != null) {
				results.add(validationFailureResult);
			}
		}

		for (DataBlockGroup group : getDataBlockGroups()) {
			List<ValidationResult> groupValidationResults = group.validate(struct);
			if (groupValidationResults != null)
				results.addAll(groupValidationResults);
		}

		if (results.isEmpty())
			return null;
		else
			return results;
	}


	/**
	 * Validates this {@link PropertySet} with priority (in order) given to the referenced StructFields and StructBlocks.
	 * First the validators for the complete PropertySet will be executed with no way to skip that.
	 * Then the given fieldsOfInterest (if defined) will be validated before the given blocksOfInterest (if defined)
	 * are validated. Last all remaining StructBlocks in this PropertySet are validated.
	 * <p>
	 * This method returns either all {@link ValidationResult}s found, or if breakOnFirstError all results till the first
	 * error is found. If the validation did not result in any {@link ValidationResult}s, <code>null</code> is returned.
	 * </p>
	 * <p>
	 * This method can be called on inflated and deflated instances of {@link PropertySet}.
	 * </p>
	 *
	 * @param struct The {@link IStruct} against which to validate.
	 * @param fieldsOfInterest The list of {@link StructField}s that should have priority over the (other) blocks. Might be <code>null</code>.
	 * @param blocksOfInterest The list of {@link StructBlock}s that should have priority over the other blocks. Might be <code>null</code>.
	 * @param breakOnFirstError Whether to abort validation when the first error was found.
	 * @return A list of {@link ValidationResult}s or <code>null</code>.
	 */
	public List<ValidationResult> validate(IStruct struct, List<StructField<?>> fieldsOfInterest, List<StructBlock> blocksOfInterest, boolean breakOnFirstError) {
		List<Object> validationOrder = new LinkedList<Object>();
		List<ValidationResult> results = new LinkedList<ValidationResult>();
		List<StructBlock> blockOrder = new LinkedList<StructBlock>();
		if (blocksOfInterest != null) {
			blockOrder.addAll(blocksOfInterest);
		}
		// put the fields first
		if (fieldsOfInterest != null) {
			for (StructField<?> structField : fieldsOfInterest) {
				validationOrder.add(structField);
				if (!blockOrder.contains(structField.getStructBlock())) {
					// if the block of the found field is not in the interest-list
					// we add it as prioritised entry
					blockOrder.add(structField.getStructBlock());
				}
			}
		}
		// then the blocks to validate
		for (StructBlock structBlock : blockOrder) {
			validationOrder.add(structBlock);
		}
		// now add all remaining blocks
		for (StructBlock structBlock : struct.getStructBlocks()) {
			if (!validationOrder.contains(structBlock)) {
				validationOrder.add(structBlock);
			}
		}

		// now validate in the computed order
		validationOrderLoop: for (Object orderItem : validationOrder) {
			if (orderItem instanceof StructField<?>) {
				try {
					List<DataField> dataFields = getDataFields(((StructField<?>) orderItem).getStructFieldIDObj());
					for (DataField dataField : dataFields) {
						List<ValidationResult> valResults = dataField.validate(struct);
						if (valResults != null) {
							results.addAll(valResults);
						}
						if (breakOnFirstError && checkForValidationError(results)) {
							break validationOrderLoop;
						}
					}
				} catch (Exception e) {
					// We ignore this exception !
				}
			} else if (orderItem instanceof StructBlock) {
				try {
					DataBlockGroup dataBlockGroup = getDataBlockGroup(((StructBlock) orderItem).getStructBlockIDObj());
					List<ValidationResult> valResults = dataBlockGroup.validate(struct);
					if (valResults != null) {
						results.addAll(valResults);
					}
					if (breakOnFirstError && checkForValidationError(results)) {
						break validationOrderLoop;
					}
				} catch (DataBlockGroupNotFoundException e) {
					// We ignore this exception !
					e.printStackTrace();
				}
			}
		}

		if (results.isEmpty())
			return null;
		else
			return results;
	}

	/**
	 * Checks the validation results of this {@link PropertySet}
	 * for {@link ValidationResultType#ERROR}s. Returns <code>true</code>
	 * if an error was found and <code>false</code> otherwise.
	 *
	 * @param struct The structure to validate against.
	 * @return Whether this {@link PropertySet} has validation errors.
	 */
	public boolean hasValidationError(IStruct struct) {
		List<ValidationResult> validationResults = validate(struct, null, null, true);
		if (validationResults == null)
			return false;
		for (ValidationResult validationResult : validationResults) {
			if (validationResult.getType() == ValidationResultType.ERROR)
				return true;
		}
		return false;
	}

	/**
	 * Used by {@link #validate(IStruct, List, List, boolean)}
	 */
	private boolean checkForValidationError(List<ValidationResult> results) {
		for (ValidationResult validationResult : results) {
			if (validationResult.getType() == ValidationResultType.ERROR)
				return true;
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((organisationID == null) ? 0 : organisationID.hashCode());
		result = prime * result + (int) (propertySetID ^ (propertySetID >>> 32));
		return result;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		final PropertySet other = (PropertySet) obj;
		return (
				Util.equals(this.organisationID, other.organisationID) &&
				Util.equals(this.propertySetID, other.propertySetID)
		);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return this.getClass().getName() + '@' + Integer.toHexString(System.identityHashCode(this)) + '[' + organisationID + ',' + ObjectIDUtil.longObjectIDFieldToString(propertySetID) + ']';
	}

	/* (non-Javadoc)
	 * @see javax.jdo.listener.DetachCallback#jdoPostDetach(java.lang.Object)
	 */
	@Override
	public void jdoPostDetach(Object attachedObject)
	{
		// get data field count:
		PersistenceManager pm = JDOHelper.getPersistenceManager(attachedObject);
		if(pm == null)
			throw new IllegalStateException("No pm for attached object");

		FetchPlan fetchPlan = pm.getFetchPlan();
		if(fetchPlan.getGroups().contains(FETCH_GROUP_DATA_FIELD_COUNT)) {
			if (LOGGER.isDebugEnabled())
				LOGGER.debug("jdoPostDetach:\n\tthis="+this+"\n\tattached="+attachedObject);

			try {
				this.dataFieldCount = ((PropertySet)attachedObject).getDataFieldCount();
			} catch(Throwable e) {
				LOGGER.error("Getting data field count failed", e);
				throw new RuntimeException(e);
			}
			if (LOGGER.isDebugEnabled())
				LOGGER.debug("dataFieldCount="+dataFieldCount);
		}
	}

	@Override
	public void jdoPreDetach()
	{
		// do nothing
	}

	/**
	 * @jdo.field persistence-modifier="none"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.NONE)
	private Map<String, Object> nonPersistentUserObjectMap;

	public Map<String, Object> getNonPersistentUserObjectMap() {
		if (nonPersistentUserObjectMap == null)
			nonPersistentUserObjectMap = new HashMap<String, Object>();

		return nonPersistentUserObjectMap;
	}
	
	@Override
	public AuthorityID getSecuringAuthorityID() {
		if (securingAuthorityID == null)
			return null;
		try {
			return new AuthorityID(securingAuthorityID);
		} catch (final Exception e) {
			throw new RuntimeException(e); // should never happen.
		}
	}

	@Override
	public AuthorityTypeID getSecuringAuthorityTypeID() {
		return (AuthorityTypeID) ObjectIDUtil.createObjectID(securingAuthorityTypeID);
	}

//	@Override
//	public void setSecuringAuthorityID(AuthorityID authorityID) {
//		// Already obtain the persistence manager directly at the beginning of the method so that it always fails outside of the server (independent from the parameter).
//		final PersistenceManager pm = JDOHelper.getPersistenceManager(this);
//		if (pm == null)
//			throw new IllegalStateException("This instance of " + this.getClass().getName() + " is not yet persistent or currently not attached to a datastore! Cannot obtain PersistenceManager!");
//		final AuthorityID oldSecuringAuthorityID = this.getSecuringAuthorityID();
//		if (Util.equals(authorityID, oldSecuringAuthorityID))
//			return; // nothing to do
//		if (authorityID != null) {
//			// Check if the AuthorityType is correct. This is already done by JFireSecurityManager.assignAuthority(...), but just to be absolutely sure since this method might be called by someone else.
//			final Authority authority = (Authority) pm.getObjectById(authorityID);
//			final AuthorityType securingAuthorityType = (AuthorityType) pm.getObjectById(getSecuringAuthorityTypeID());
//			if (!authority.getAuthorityType().equals(securingAuthorityType))
//				throw new IllegalArgumentException("securingAuthority.authorityType does not match this.securingAuthorityTypeID! securingAuthority: " + authorityID + " this: " + JDOHelper.getObjectId(this));
//		}
//		this.securingAuthorityID = authorityID == null ? null : authorityID.toString();
//	}

	@Override
	public void setSecuringAuthorityID(AuthorityID authorityID)
	{
		// checking if authority type is right is done in preAttach()
		final AuthorityID oldSecuringAuthorityID = this.getSecuringAuthorityID();
		if (Util.equals(authorityID, oldSecuringAuthorityID))
			return; // nothing to do
		this.securingAuthorityID = authorityID == null ? null : authorityID.toString();
	}	
}
