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

package org.nightlabs.jfire.prop.config;

import java.io.Serializable;
import java.util.Collection;

import javax.jdo.FetchPlan;
import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.jdo.annotations.Column;
import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.FetchGroups;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.PersistenceModifier;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;
import javax.jdo.annotations.Queries;
import javax.jdo.listener.DetachCallback;

import org.nightlabs.jdo.ObjectIDUtil;
import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.jfire.prop.StructLocal;
import org.nightlabs.jfire.prop.config.id.PropertySetFieldBasedEditLayoutUseCaseID;
import org.nightlabs.jfire.prop.id.StructLocalID;
import org.nightlabs.util.CollectionUtil;
import org.nightlabs.util.Util;

/**
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DT] de -->
 *
 * @jdo.persistence-capable
 *    identity-type="application"
 *    objectid-class="org.nightlabs.jfire.prop.config.id.PropertySetFieldBasedEditLayoutUseCaseID"
 *    detachable="true"
 *    table="JFireBase_Prop_PropertySetFieldBasedEditLayoutUseCase"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.create-objectid-class field-order="organisationID, useCaseID"
 *
 * @jdo.fetch-group name="PropertySetFieldBasedEditLayoutUseCase.name" fetch-groups="default" fields="name"
 * @jdo.fetch-group name="PropertySetFieldBasedEditLayoutUseCase.description" fetch-groups="default" fields="description"
 * @jdo.fetch-group name="PropertySetFieldBasedEditLayoutUseCase.structLocal" fetch-groups="default" fields="structLocal"
 *
 * @jdo.query name="getAllUseCaseIDs"
 * 						query="SELECT JDOHelper.getObjectId(this) import javax.jdo.JDOHelper"
 *
 */
@PersistenceCapable(
	objectIdClass=PropertySetFieldBasedEditLayoutUseCaseID.class,
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireBase_Prop_PropertySetFieldBasedEditLayoutUseCase"
)
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
@FetchGroups({
	@FetchGroup(
		name=PropertySetFieldBasedEditLayoutUseCase.FETCH_GROUP_NAME,
		fetchGroups={FetchPlan.DEFAULT},
		members=@Persistent(name=PropertySetFieldBasedEditLayoutUseCase.FieldName.name)
	),
	@FetchGroup(
			name=PropertySetFieldBasedEditLayoutUseCase.FETCH_GROUP_DESCRIPTION,
			members=@Persistent(name=PropertySetFieldBasedEditLayoutUseCase.FieldName.description)
	),
	@FetchGroup(
			name=PropertySetFieldBasedEditLayoutUseCase.FETCH_GROUP_STRUCT_LOCAL,
			members=@Persistent(name=PropertySetFieldBasedEditLayoutUseCase.FieldName.structLocal)
	)
})
@Queries(
		@javax.jdo.annotations.Query(name="getAllUseCaseIDs", value="SELECT JDOHelper.getObjectId(this)")
)
public class PropertySetFieldBasedEditLayoutUseCase implements Serializable, DetachCallback
{
	/**
	 * The serial version of this class.
	 */
	private static final long serialVersionUID = 20090311L;

	public static class FieldName {
		public static final String name = "name";
		public static final String description = "description";
		public static final String structLocal = "structLocal";
		public static final String structLocalID = "structLocalID";
	}

	public static final String FETCH_GROUP_NAME = "PropertySetFieldBasedEditLayoutUseCase.name";
	public static final String FETCH_GROUP_DESCRIPTION = "PropertySetFieldBasedEditLayoutUseCase.description";
	public static final String FETCH_GROUP_STRUCT_LOCAL = "PropertySetFieldBasedEditLayoutUseCase.structLocal";
	public static final String FETCH_GROUP_STRUCT_LOCAL_ID = "PropertySetFieldBasedEditLayoutUseCase.structLocalID";

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	@PrimaryKey
	@Column(length=100)
	private String organisationID;

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="50"
	 */
	@PrimaryKey
	@Column(length=50)
	private String useCaseID;

	/**
	 * @jdo.field persistence-modifier="persistent" mapped-by="useCase"
	 */
	@Persistent(
		mappedBy="useCase",
		persistenceModifier=PersistenceModifier.PERSISTENT)
	private PropertySetFieldBasedEditLayoutUseCaseName name;

	/**
	 * @jdo.field persistence-modifier="persistent" mapped-by="useCase"
	 */
	@Persistent(
		mappedBy="useCase",
		persistenceModifier=PersistenceModifier.PERSISTENT)
	private PropertySetFieldBasedEditLayoutUseCaseDescription description;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private StructLocal structLocal;

	/**
	 * Only used with virtual fetch-group.
	 *
	 * @jdo.field persistence-modifier="none"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.NONE)
	private boolean structLocalIDDetached;

	/**
	 * Only used with virtual fetch-group.
	 *
	 * @jdo.field persistence-modifier="none"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.NONE)
	private StructLocalID structLocalID;

	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	public PropertySetFieldBasedEditLayoutUseCase() {
	}

	public PropertySetFieldBasedEditLayoutUseCase(
			String organisationID, String useCaseID, StructLocal structLocal) {
		Organisation.assertValidOrganisationID(organisationID);
		this.organisationID = organisationID;
		ObjectIDUtil.assertValidIDString(useCaseID, "useCaseID");
		this.useCaseID = useCaseID;
		this.name = new PropertySetFieldBasedEditLayoutUseCaseName(this);
		this.description = new PropertySetFieldBasedEditLayoutUseCaseDescription(this);
		this.structLocal = structLocal;
	}

	/**
	 * Get the organisationID of this {@link PropertySetFieldBasedEditLayoutUseCase}.
	 * @return The organisationID of this {@link PropertySetFieldBasedEditLayoutUseCase}.
	 */
	public String getOrganisationID() {
		return organisationID;
	}

	/**
	 * Get the useCaseID of this {@link PropertySetFieldBasedEditLayoutUseCase}.
	 * @return The useCaseID of this {@link PropertySetFieldBasedEditLayoutUseCase}.
	 */
	public String getUseCaseID() {
		return useCaseID;
	}

	/**
	 * Get the name of this {@link PropertySetFieldBasedEditLayoutUseCase}.
	 * @return The name of this {@link PropertySetFieldBasedEditLayoutUseCase}.
	 */
	public PropertySetFieldBasedEditLayoutUseCaseName getName() {
		return name;
	}

	/**
	 * Get the description of this {@link PropertySetFieldBasedEditLayoutUseCase}.
	 * @return The description of this {@link PropertySetFieldBasedEditLayoutUseCase}.
	 */
	public PropertySetFieldBasedEditLayoutUseCaseDescription getDescription() {
		return description;
	}

	/**
	 * @return The {@link StructLocal} (i.e. property-structure) this use-case is for.
	 */
	public StructLocal getStructLocal() {
		return structLocal;
	}

	public StructLocalID getStructLocalID() {
		if (structLocalID == null && !structLocalIDDetached)
			structLocalID = (StructLocalID) JDOHelper.getObjectId(structLocal);

		return structLocalID;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((organisationID == null) ? 0 : organisationID.hashCode());
		result = prime * result + ((useCaseID == null) ? 0 : useCaseID.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		final PropertySetFieldBasedEditLayoutUseCase other = (PropertySetFieldBasedEditLayoutUseCase) obj;
		return (
				Util.equals(this.organisationID, other.organisationID) &&
				Util.equals(this.useCaseID, other.useCaseID)
		);
	}

	@Override
	public void jdoPostDetach(Object _attached)
	{
		PropertySetFieldBasedEditLayoutUseCase attached = (PropertySetFieldBasedEditLayoutUseCase)_attached;
		PropertySetFieldBasedEditLayoutUseCase detached = this;
		Collection<String> fetchGroups = CollectionUtil.castSet(JDOHelper.getPersistenceManager(attached).getFetchPlan().getGroups());

		if (fetchGroups.contains(FETCH_GROUP_STRUCT_LOCAL_ID)) {
			detached.structLocalID = attached.getStructLocalID();
			detached.structLocalIDDetached = true;
		}
	}

	@Override
	public void jdoPreDetach() {
	}


	@SuppressWarnings("unchecked")
	public static Collection<PropertySetFieldBasedEditLayoutUseCaseID> getAllUseCaseIDs(PersistenceManager pm) {
		Query q = pm.newNamedQuery(PropertySetFieldBasedEditLayoutUseCase.class, "getAllUseCaseIDs");
		return (Collection<PropertySetFieldBasedEditLayoutUseCaseID>) q.execute();
	}
}

