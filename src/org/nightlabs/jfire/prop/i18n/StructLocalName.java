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
package org.nightlabs.jfire.prop.i18n;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.nightlabs.i18n.I18nText;
import org.nightlabs.jfire.prop.Struct;
import org.nightlabs.jfire.prop.StructLocal;

import javax.jdo.annotations.Join;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.FetchGroups;
import javax.jdo.annotations.NullValue;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.Inheritance;
import org.nightlabs.jfire.prop.i18n.id.StructLocalNameID;
import javax.jdo.annotations.PrimaryKey;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.Column;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceModifier;

/**
 * {@link I18nText} used as name for {@link StructLocal}s.
 * 
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.prop.i18n.id.StructLocalNameID"
 *		detachable="true"
 *		table="JFireBase_Prop_StructLocalName"
 *
 * @jdo.create-objectid-class field-order="organisationID, linkClass, structScope, structLocalScope"
 * 
 * @jdo.inheritance strategy="new-table"
 * 
 * @jdo.fetch-group name="IStruct.fullData" fetch-groups="default" fields="names"
 * @jdo.fetch-group name="StructLocal.name" fields="structLocal, names"
 */
@PersistenceCapable(
	objectIdClass=StructLocalNameID.class,
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireBase_Prop_StructLocalName")
@FetchGroups({
	@FetchGroup(
		fetchGroups={"default"},
		name="IStruct.fullData",
		members=@Persistent(name="names")),
	@FetchGroup(
		name="StructLocal.name",
		members={@Persistent(name="structLocal"), @Persistent(name="names")})
})
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class StructLocalName extends I18nText implements Serializable {
	
	private static final long serialVersionUID = 1L;

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	@PrimaryKey
	@Column(length=100)
	private String organisationID;

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	@PrimaryKey
	@Column(length=100)
	private String linkClass;
	
	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	@PrimaryKey
	@Column(length=100)
	private String structScope;
	
	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	@PrimaryKey
	@Column(length=100)
	private String structLocalScope;
	
	
	/**
	 * @jdo.field
	 * 	persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private StructLocal structLocal;

	/**
	 * key: String languageID<br/>
	 * value: String text
	 * 
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="map"
	 *		key-type="java.lang.String"
	 *		value-type="java.lang.String"
	 *		table="JFireBase_Prop_StructLocalName_names"
	 *		null-value="exception"
	 *
	 * @jdo.join
	 */
	@Join
	@Persistent(
		nullValue=NullValue.EXCEPTION,
		table="JFireBase_Prop_StructLocalName_names",
		persistenceModifier=PersistenceModifier.PERSISTENT)
	protected Map<String, String> names = new HashMap<String, String>();
	
	/**
	 * @deprecated Only for JDO
	 */
	@Deprecated
	protected StructLocalName() { }
	
	/**
	 * Create a new {@link StructLocalName} with the given {@link StructLocal}.
	 * @param structLocal The {@link StructLocal} to link to.
	 */
	public StructLocalName(StructLocal structLocal) {
		this.organisationID = structLocal.getOrganisationID();
		this.linkClass = structLocal.getLinkClass().getName();
		this.structScope = structLocal.getStructScope();
		this.structLocalScope = structLocal.getStructLocalScope();
		this.structLocal = structLocal;
	}

	@Override
	protected String getFallBackValue(String languageID) {
		return linkClass.substring(linkClass.lastIndexOf(".")+1);
	}

	@Override
	protected Map<String, String> getI18nMap() {
		return names;
	}
	
	/**
	 * @return The organisationID of the {@link StructLocal} this is linked to.
	 */
	public String getOrganisationID() {
		return organisationID;
	}

	/**
	 * @return The linkClass of the {@link StructLocal} this is linked to.
	 */
	public String getLinkClass() {
		return linkClass;
	}
	
	/**
	 * @return The {@link Struct}-scope of the {@link StructLocal} this is linked to.
	 */
	public String getStructScope() {
		return structScope;
	}
	
	/**
	 * @return The {@link StructLocal}-scope of the {@link StructLocal} this is linked to.
	 */
	public String getStructLocalScope() {
		return structLocalScope;
	}
	
	/**
	 * @return The {@link StructLocal} this is linked to.
	 */
	public StructLocal getStructLocal() {
		return structLocal;
	}
}