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

package org.nightlabs.jfire.config;

import java.io.Serializable;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.PersistenceModifier;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import org.apache.log4j.Logger;
import org.nightlabs.inheritance.FieldMetaData;
import org.nightlabs.inheritance.NotWritableException;
import org.nightlabs.jfire.config.id.ConfigModuleSimpleFieldMetaDataID;

/**
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.config.id.ConfigModuleSimpleFieldMetaDataID"
 *		detachable="true"
 *		table="JFireBase_ConfigModuleSimpleFieldMetaData"
 *
 * @jdo.create-objectid-class field-order="organisationID, configKey, configType, cfModKey, fieldName"
 */
@PersistenceCapable(
	objectIdClass=ConfigModuleSimpleFieldMetaDataID.class,
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireBase_ConfigModuleSimpleFieldMetaData")
public class ConfigModuleSimpleFieldMetaData
implements FieldMetaData, Serializable
{
	private static final long serialVersionUID = 1L;

	private static final Logger logger = Logger.getLogger(ConfigModuleSimpleFieldMetaData.class);

	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected ConfigModuleSimpleFieldMetaData() { }

	public ConfigModuleSimpleFieldMetaData(ConfigModule configModule, String fieldName) {
		this.configModule = configModule;
		this.organisationID = configModule.getOrganisationID();
		this.configKey = configModule.getConfigKey();
		this.configType = configModule.getConfigType();
		this.cfModKey = configModule.getCfModKey();
		this.fieldName = fieldName;
	}

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	@PrimaryKey
	@Column(length=100)
	private String organisationID;

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="200"
	 */
	@PrimaryKey
//	@Column(length=200)
	private String configKey;

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	@PrimaryKey
	@Column(length=100)
	protected String configType;

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="150"
	 */
	@PrimaryKey
	@Column(length=150)
	private String cfModKey;

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	@PrimaryKey
	@Column(length=100)
	private String fieldName;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private ConfigModule configModule;

	/**
	 * @see org.nightlabs.inheritance.FieldMetaData#getFieldName()
	 */
	public String getFieldName() {
		return fieldName;
	}

	/**
	 * @see org.nightlabs.inheritance.FieldMetaData#getWritableByChildren()
	 */
	public byte getWritableByChildren() {
//		if (configModule.isGroupMembersMayOverride()) {
//			if (writableByChildren != WRITABLEBYCHILDREN_YES)
//				writableByChildren = WRITABLEBYCHILDREN_YES;
//			return WRITABLEBYCHILDREN_YES;
//		}
//		else {
//			if (writableByChildren != WRITABLEBYCHILDREN_NO)
//				writableByChildren = WRITABLEBYCHILDREN_NO;
//			return WRITABLEBYCHILDREN_NO;
//		}
		return writableByChildren;
	}

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private byte writableByChildren = WRITABLEBYCHILDREN_YES;

	/**
	 * @see org.nightlabs.inheritance.FieldMetaData#setWritableByChildren(byte)
	 */
	public void setWritableByChildren(byte writableByChildren) {
		this.writableByChildren = writableByChildren;
	}

	/**
	 * @see org.nightlabs.inheritance.FieldMetaData#isWritable()
	 */
	public boolean isWritable() {
		return writable;
	}

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private boolean writable = true;

	/**
	 * @see org.nightlabs.inheritance.FieldMetaData#assertWritable()
	 */
	public void assertWritable() throws NotWritableException {
		if (!isWritable())
			throw new NotWritableException("Field "+fieldName+" of "+configModule.getClass().getName()+" is not writable.");
	}

	/**
	 * @see org.nightlabs.inheritance.FieldMetaData#setWritable(boolean)
	 */
	public void setWritable(boolean writable) {
		this.writable = writable;
	}

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private boolean valueInherited = false;

	/**
	 * @see org.nightlabs.inheritance.FieldMetaData#isValueInherited()
	 */
	public boolean isValueInherited() {
		return valueInherited;
	}

	/**
	 * @see org.nightlabs.inheritance.FieldMetaData#setValueInherited(boolean)
	 */
	public void setValueInherited(boolean valueInherited) {
		if (logger.isDebugEnabled())
			logger.debug("valueInherited = "+this.valueInherited+", is changed to: "+valueInherited, new Exception());
		this.valueInherited = valueInherited;
	}

	/**
	 * Get the cfModKey.
	 * @return the cfModKey
	 */
	public String getCfModKey()
	{
		return cfModKey;
	}

	/**
	 * Get the configKey.
	 * @return the configKey
	 */
	public String getConfigKey()
	{
		return configKey;
	}

	/**
	 * Get the organisationID.
	 * @return the organisationID
	 */
	public String getOrganisationID()
	{
		return organisationID;
	}

}
