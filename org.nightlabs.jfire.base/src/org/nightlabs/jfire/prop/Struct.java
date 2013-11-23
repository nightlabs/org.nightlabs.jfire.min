/* ********************************************************************
 * JFireBase                                                          *
 * Copyright (C) 2004-2007 NightLabs - http://NightLabs.org           *
 *                                                                    *
 * This library is free software; you can redistribute it and/or      *
 * modify it under the terms of the GNU Lesser General Public         *
 * License as published by the Free Software Foundation; either       *
 * version 2.1 of the License, or (at your option) any later version. *
 *                                                                    *
 * This library is distributed in the hope that it will be useful,    *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of     *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU  *
 * Lesser General Public License for more details.                    *
 *                                                                    *
 * You should have received a copy of the GNU Lesser General Public   *
 * License along with this library; if not, write to the              *
 *     Free Software Foundation, Inc.,                                *
 *     51 Franklin St, Fifth Floor,                                   *
 *     Boston, MA  02110-1301  USA                                    *
 *                                                                    *
 * Or get it online:                                                  *
 *     http://www.gnu.org/copyleft/lesser.html                        *
 **********************************************************************/
package org.nightlabs.jfire.prop;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.jdo.annotations.Column;
import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.FetchGroups;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Join;
import javax.jdo.annotations.NullValue;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.PersistenceModifier;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;
import javax.jdo.annotations.Queries;

import org.nightlabs.jfire.prop.i18n.StructName;
import org.nightlabs.jfire.prop.id.StructID;
import org.nightlabs.jfire.prop.validation.IPropertySetValidator;
import org.nightlabs.jfire.security.SecurityReflector;

/**
 * Global structure definition of the {@link PropertySet} linked to instances of the class {@link #getLinkClass()}.
 * <p>
 * Although {@link Struct} is an implementation of {@link IStruct}, it is not intended to be used as the structure
 * definition of a {@link PropertySet} directly. It is rather the base definition a {@link StructLocal}
 * is build upon. Each {@link PropertySet} references a {@link StructLocal} as structure, not a {@link Struct}.
 * </p>
 *
 * @author Tobias Langner <!-- tobias[dot]langner[at]nightlabs[dot]de -->
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 *
 * @jdo.persistence-capable identity-type="application"
 *                          objectid-class="org.nightlabs.jfire.prop.id.StructID"
 *                          detachable="true" table="JFireBase_Prop_Struct"
 *
 * @jdo.implements name="org.nightlabs.jfire.prop.IStruct"
 *
 * @jdo.create-objectid-class
 *		field-order="organisationID, linkClass, structScope"
 *		add-interfaces="org.nightlabs.jfire.prop.id.IStructID"
 *		include-body="id/StructID.body.inc"
 *
 * @jdo.query name="getAllStructIDs"
 * 						query="SELECT JDOHelper.getObjectId(this) import javax.jdo.JDOHelper"
 *
 * @jdo.fetch-group
 * 		name="IStruct.fullData"
 * 		fetch-groups="default"
 * 		fields="structBlockList, displayNameParts, name, propertySetValidators"
 *
 * @jdo.fetch-group name="Struct.name" fetch-groups="default" fields="name"
 */
@PersistenceCapable(
	objectIdClass=StructID.class,
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireBase_Prop_Struct")
@FetchGroups({
	@FetchGroup(
		fetchGroups={Struct.DEFAULT_SCOPE},
		name="IStruct.fullData",
		members={@Persistent(name="structBlockList"), @Persistent(name="displayNameParts"), @Persistent(name="name"), @Persistent(name="propertySetValidators")}),
	@FetchGroup(
		fetchGroups={Struct.DEFAULT_SCOPE},
		name="Struct.name",
		members=@Persistent(name="name"))
})
@Queries(
	@javax.jdo.annotations.Query(
		name="getAllStructIDs",
		value="SELECT JDOHelper.getObjectId(this) import javax.jdo.JDOHelper")
)
public class Struct extends AbstractStruct {
	private static final long serialVersionUID = -20080610L;

	/**
	 * The default scope for new StructLocals of a Struct
	 */
	public static final String DEFAULT_SCOPE = "default";

	/**
	 * @jdo.field
	 * 		persistence-modifier="persistent"
	 * 		collection-type="collection"
	 * 		element-type="org.nightlabs.jfire.prop.StructBlock"
	 * 		table="JFireBase_Prop_Struct_structBlockList"
	 * 		dependent-element="true"
	 *		null-value="exception"
	 *		mapped-by="struct"
	 *
	 * @jdo.join
	 */
	@Join
	@Persistent(
		dependentElement="true",
		nullValue=NullValue.EXCEPTION,
		mappedBy="struct", // TODO why this mappedBy, if we have a @Join and a join-table?!??! Marco.
		table="JFireBase_Prop_Struct_structBlockList",
		persistenceModifier=PersistenceModifier.PERSISTENT)
	protected List<StructBlock> structBlockList;

	/**
	 * @jdo.field persistence-modifier="none"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.NONE)
	protected transient Map<String, StructBlock> structBlockMap;

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	@PrimaryKey
	@Column(length=100)
	protected String organisationID;

	/**
	 * The class whose properties are defined by this instance.
	 *
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	@PrimaryKey
	@Column(length=100)
	protected String linkClass;

	/**
	 * The class scope whose properties are defined by this instance.
	 *
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	@PrimaryKey
	@Column(length=100)
	protected String structScope;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	protected StructName name;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 *		collection-type="collection"
	 *		element-type="org.nightlabs.jfire.prop.DisplayNamePart"
	 *		table="JFireBase_Prop_Struct_displayNameParts"
	 *		dependent-element="true"
	 *
	 * @jdo.join
	 */
	@Join
	@Persistent(
//			dependentElement="true", // It is wrong to mark it dependent in *both* Struct and StructLocal. Instead, there should be code in the remove method that checks, if it is still used and delete it otherwise. Marco.
			table="JFireBase_Prop_Struct_displayNameParts",
			persistenceModifier=PersistenceModifier.PERSISTENT
	)
	protected List<DisplayNamePart> displayNameParts;

	/**
	 * Create a new Struct instance.
	 *
	 * @param organisationID The organisationID of the new Struct.
	 * @param linkClass The linkClass of the new Struct.
	 * @param structScope The scope of the new Struct.
	 */
	public Struct(String organisationID, String linkClass, String structScope) {
		this.organisationID = organisationID;
		this.linkClass = linkClass;
		this.structScope = structScope;
		this.name = new StructName(this);

		displayNameParts = new ArrayList<DisplayNamePart>();
		structBlockList = new ArrayList<StructBlock>();
		propertySetValidators = new HashSet<IPropertySetValidator>();
	}

	protected Struct() { }

	public static Struct getStruct(String organisationID, Class<?> linkClass, String structScope, PersistenceManager pm) {
		// initialise meta-data
		pm.getExtent(Struct.class);

		return (Struct) pm.getObjectById(StructID.create(organisationID, linkClass.getName(), structScope));
	}

	public static Struct getStruct(String organisationID, String linkClass, String structScope, PersistenceManager pm) {
		// initialise meta-data
		pm.getExtent(Struct.class);

		return (Struct) pm.getObjectById(StructID.create(organisationID, linkClass, structScope));
	}

	public static Struct getStruct(String linkClass, String structScope, PersistenceManager pm) {
		// initialise meta-data
		pm.getExtent(Struct.class);

		return getStruct(SecurityReflector.getUserDescriptor().getOrganisationID(), linkClass, structScope, pm);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.nightlabs.jfire.prop.IStruct#removeStructBlock(org.nightlabs.jfire.prop.StructBlock)
	 */
	@Override
	public void removeStructBlock(StructBlock psb) {
		// if (IDGenerator.getOrganisationID().equals()
		structBlockList.remove(psb);
//		psb.setStruct(null); // Must NOT do this, because the StructBlock is dependent and thus deleted and DN cannot write to a deleted object.
	}

	/**
	 * Returns the struct ID of the struct.
	 *
	 * @return the struct ID.
	 */
	public StructID getID() {
		return StructID.create(organisationID, linkClass, structScope);
	}

//	public void setStructLocal(StructLocal structLocal) {
//		this.structLocal = structLocal;
//	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.nightlabs.jfire.prop.AbstractStruct#getStruct()
	 */
	@Override
	protected Struct getStruct() {
		return null;
	}

	@Override
	protected List<DisplayNamePart> _getDisplayNameParts() {
		return displayNameParts;
	}

	@Override
	public List<DisplayNamePart> getDisplayNameParts() {
		return Collections.unmodifiableList(displayNameParts);
	}

	@Override
	protected String getLinkClassInternal() {
		return linkClass;
	}

	@Override
	public String getOrganisationID() {
		return organisationID;
	}

	/**
	 * Returns the scope of this Struct.
	 * Using a scope there can exists multiple
	 * structures that can be used to enhance the same
	 * class of objects with a PropertySet (linkClass)
	 * The default scope is {@link #DEFAULT_SCOPE}.
	 *
	 * @return The scope of this Struct.
	 */
	public String getStructScope() {
		return structScope;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Returns the {@link #structBlockList}.
	 * </p>
	 */
	@Override
	protected List<StructBlock> getStructBlockList() {
		return structBlockList;
	}

	/**
	 * @return The I18n name of this {@link Struct}.
	 */
	public StructName getName() {
		return name;
	}

	public static Collection<StructID> getAllStructIDs(PersistenceManager pm) {
		Query q = pm.newNamedQuery(Struct.class, "getAllStructIDs");
		return (Collection<StructID>) q.execute();
	}

	/**
	 * @jdo.field
	 * 		persistence-modifier="persistent"
	 * 		collection-type="collection"
	 * 		table="JFireBase_Prop_Struct_propertySetValidators"
	 * 		dependent-element="true"
	 *		null-value="exception"
	 *
	 * @jdo.join
	 */
	@Join
	@Persistent(
		dependentElement="true",
		nullValue=NullValue.EXCEPTION,
		table="JFireBase_Prop_Struct_propertySetValidators",
		persistenceModifier=PersistenceModifier.PERSISTENT)
	private Set<IPropertySetValidator> propertySetValidators;

	@Override
	protected Set<IPropertySetValidator> getPropertySetValidatorSet() {
		return propertySetValidators;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode()
	{
		final int PRIME = 31;
		int result = 1;
		result = PRIME * result + ((getLinkClass() == null) ? 0 : getLinkClass().hashCode());
		result = PRIME * result + ((getOrganisationID() == null) ? 0 : getOrganisationID().hashCode());
		return result;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final Struct other =  (Struct) obj;
		if (getLinkClass() == null)
		{
			if (other.getLinkClass() != null)
				return false;
		}
		else if (!getLinkClass().equals(other.getLinkClass()))
			return false;
		if (getOrganisationID() == null)
		{
			if (other.getOrganisationID() != null)
				return false;
		}
		else if (!getOrganisationID().equals(other.getOrganisationID()))
			return false;
		return true;
	}

}