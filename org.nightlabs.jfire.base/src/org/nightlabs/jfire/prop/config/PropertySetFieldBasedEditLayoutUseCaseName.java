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

import java.util.HashMap;
import java.util.Map;

import org.nightlabs.i18n.I18nText;

import javax.jdo.annotations.Join;
import javax.jdo.annotations.Persistent;
import org.nightlabs.jfire.prop.config.id.PropertySetFieldBasedEditLayoutUseCaseNameID;
import javax.jdo.annotations.FetchGroups;
import javax.jdo.annotations.NullValue;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.PrimaryKey;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.Column;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceModifier;

/**
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 * @author marco schulze - marco at nightlabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.prop.config.id.PropertySetFieldBasedEditLayoutUseCaseNameID"
 *		detachable="true"
 *		table="JFireBase_Prop_PropertySetFieldBasedEditLayoutUseCaseName"
 *
 * @jdo.inheritance strategy="new-table"
 * 
 * @jdo.create-objectid-class field-order="organisationID, useCaseID"
 *
 * @jdo.fetch-group name="PropertySetFieldBasedEditLayoutUseCaseName.name" fetch-groups="default" fields="useCase, names"
 */
@PersistenceCapable(
	objectIdClass=PropertySetFieldBasedEditLayoutUseCaseNameID.class,
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireBase_Prop_PropertySetFieldBasedEditLayoutUseCaseName")
@FetchGroups(
	@FetchGroup(
		fetchGroups={"default"},
		name="PropertySetFieldBasedEditLayoutUseCaseName.name",
		members={@Persistent(name="useCase"), @Persistent(name="names")})
)
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class PropertySetFieldBasedEditLayoutUseCaseName extends I18nText
{
	private static final long serialVersionUID = 20090311L;
	
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
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private PropertySetFieldBasedEditLayoutUseCase useCase;

	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected PropertySetFieldBasedEditLayoutUseCaseName() {
	}

	public PropertySetFieldBasedEditLayoutUseCaseName(PropertySetFieldBasedEditLayoutUseCase useCase) {
		this.useCase = useCase;
		this.organisationID = useCase.getOrganisationID();
		this.useCaseID = useCase.getUseCaseID();
		this.names = new HashMap<String, String>();
	}

	/**
	 * key: String languageID<br/>
	 * value: String name
	 * 
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="map"
	 *		key-type="java.lang.String"
	 *		value-type="java.lang.String"
	 *		default-fetch-group="true"
	 *		table="JFireBase_Prop_PropertySetFieldBasedEditLayoutUseCaseName_names"
	 *		null-value="exception"
	 *
	 * @jdo.join
	 */
	@Join
	@Persistent(
		nullValue=NullValue.EXCEPTION,
		table="JFireBase_Prop_PropertySetFieldBasedEditLayoutUseCaseName_names",
		defaultFetchGroup="true",
		persistenceModifier=PersistenceModifier.PERSISTENT)
	protected Map<String, String> names;
	
	/**
	 * @see org.nightlabs.i18n.I18nText#getI18nMap()
	 */
	@Override
	protected Map<String, String> getI18nMap() {
		return names;
	}

	/**
	 * @see org.nightlabs.i18n.I18nText#getFallBackValue(java.lang.String)
	 */
	@Override
	protected String getFallBackValue(String languageID) {
		return useCaseID;
	}

	public String getOrganisationID() {
		return organisationID;
	}
	
	public String getUseCaseID() {
		return useCaseID;
	}
	
	public PropertySetFieldBasedEditLayoutUseCase getUseCase() {
		return useCase;
	}
}
