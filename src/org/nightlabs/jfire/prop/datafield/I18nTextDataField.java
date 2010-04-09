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

package org.nightlabs.jfire.prop.datafield;

import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.FetchGroups;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.PersistenceModifier;
import javax.jdo.annotations.Persistent;

import org.nightlabs.i18n.I18nText;
import org.nightlabs.jfire.prop.DataBlock;
import org.nightlabs.jfire.prop.DataField;
import org.nightlabs.jfire.prop.PropertySet;
import org.nightlabs.jfire.prop.StructField;
import org.nightlabs.jfire.prop.i18n.I18nDataFieldText;
import org.nightlabs.util.NLLocale;

/**
 * {@link DataField} that stores a internationalised text value using a {@link I18nText}.
 *
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 * @author Tobias Langner <!-- tobias[dot]langner[at]nightlabs[dot]de -->
 *
 * @jdo.persistence-capable
 * 		persistence-capable-superclass="org.nightlabs.jfire.prop.DataField"
 *		detachable="true"
 *		table="JFireBase_Prop_I18nTextDataField"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.fetch-group name="FetchGroupsProp.fullData" fetch-groups="default" fields="fieldText"
 * @jdo.fetch-group name="I18nTextDataField.fieldText" fetch-groups="default" fields="fieldText"
 */
@PersistenceCapable(
	detachable="true",
	table="JFireBase_Prop_I18nTextDataField")
@FetchGroups({
	@FetchGroup(
		fetchGroups={"default"},
		name="FetchGroupsProp.fullData",
		members=@Persistent(name="fieldText")),
	@FetchGroup(
		fetchGroups={"default"},
		name=I18nTextDataField.FETCH_GROUP_FIELD_TEXT,
		members=@Persistent(name="fieldText"))
})
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class I18nTextDataField
extends DataField
implements II18nTextDataField
{

	public static final String FETCH_GROUP_FIELD_TEXT = "I18nTextDataField.fieldText";
	private static final long serialVersionUID = 20090116L;

	/**
	 * @deprecated Only for JDO
	 */
	@Deprecated
	protected I18nTextDataField() {
		super();
	}

	/**
	 * Create a new {@link I18nTextDataField} for the given {@link DataBlock} that
	 * represents the given {@link StructField}.
	 *
	 * @param dataBlock The {@link DataBlock} the new {@link I18nDataField} will be part of.
	 * @param structField The {@link StructField} the new {@link I18nDataField} represents in the data structure.
	 */
	public I18nTextDataField(DataBlock propBlock, StructField<I18nTextDataField> structField) {
		super(propBlock, structField);
		fieldText = new I18nDataFieldText(this);
	}

	/**
	 * Used for cloning.
	 */
	protected I18nTextDataField(String organisationID, long propertySetID, I18nTextDataField cloneField) {
		super(organisationID, propertySetID, cloneField);
		fieldText = new I18nDataFieldText(this);
	}

	/**
	 * Used for cloning.
	 */
	protected I18nTextDataField(String organisationID, long propertySetID, int dataBlockID, I18nTextDataField cloneField) {
		super(organisationID, propertySetID, dataBlockID, cloneField);
		fieldText = new I18nDataFieldText(this);
	}

	/**
	 * The {@link I18nText} storing the values of this data field.
	 *
	 * @jdo.field
	 * 		persistence-modifier="persistent"
	 * 		mapped-by="dataField"
	 */
	@Persistent(
		mappedBy="dataField",
		dependent="true",
		persistenceModifier=PersistenceModifier.PERSISTENT
	)
	protected I18nDataFieldText fieldText;

	/**
	 * Returns the {@link I18nText} with the internationalised text values of this {@link DataField}.
	 * @return The {@link I18nText} of this {@link DataField}.
	 */
	public I18nText getI18nText() {
		return fieldText;
	}

	/*
	 * (non-Javadoc)
	 * @see org.nightlabs.jfire.prop.DataField#isEmpty()
	 */
	@Override
	public boolean isEmpty() {
		return getI18nText().isEmpty();
	}

	/*
	 * (non-Javadoc)
	 * @see org.nightlabs.jfire.prop.DataField#cloneDataField(org.nightlabs.jfire.prop.PropertySet)
	 */
	@Override
	public DataField cloneDataField(PropertySet propertySet) {
		return cloneDataField(propertySet, 0);
	}

	/*
	 * (non-Javadoc)
	 * @see org.nightlabs.jfire.prop.DataField#cloneDataField(org.nightlabs.jfire.prop.PropertySet, int)
	 */
	@Override
	public DataField cloneDataField(PropertySet propertySet, int dataBlockID) {
		I18nTextDataField newField = new I18nTextDataField(getOrganisationID(), propertySet.getPropertySetID(), dataBlockID, this);
		if (getI18nText() != null) {
			newField.getI18nText().copyFrom(getI18nText());
		}
		return newField;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Calling this method is equal to {@link #getI18nText()}.
	 * </p>
	 */
	@Override
	public Object getData() {
		return getI18nText();
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Copies data from an {@link II18nTextDataField} or an {@link I18nText}.
	 * If a String is passed, the field is cleared first and then the given
	 * String will be as only entry for the locale {@link NLLocale#getDefault()}.
	 * </p>
	 */
	@Override
	public void setData(Object data) {
		if (data == null) {
			getI18nText().clear();
		} else if (data instanceof II18nTextDataField) {
			this.getI18nText().copyFrom(((II18nTextDataField) data).getI18nText());
		} else if (data instanceof I18nText) {
			this.getI18nText().copyFrom((I18nText) data);
		} else if (data instanceof String){
			getI18nText().clear();
			getI18nText().setText(NLLocale.getDefault().getLanguage(), (String) data);
		} else {
			throw new IllegalArgumentException(this.getClass().getName() + " does not support input data of type " + data.getClass());
		}
	}

	@Override
	public boolean supportsInputType(Class<?> inputType) {
		return
			II18nTextDataField.class.isAssignableFrom(inputType) ||
			I18nText.class.isAssignableFrom(inputType) ||
			String.class.isAssignableFrom(inputType);
	}

}
