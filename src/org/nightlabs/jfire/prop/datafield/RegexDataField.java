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
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.PersistenceModifier;
import javax.jdo.annotations.Persistent;

import org.nightlabs.i18n.I18nText;
import org.nightlabs.i18n.StaticI18nText;
import org.nightlabs.jfire.prop.DataBlock;
import org.nightlabs.jfire.prop.DataField;
import org.nightlabs.jfire.prop.PropertySet;
import org.nightlabs.jfire.prop.StructField;


/**
 * {@link DataField} that stores a string whose value was validated using a regular expression.
 *
 * @author Tobias Langner <!-- tobias[dot]langner[at]nightlabs[dot]de -->
 *
 * @jdo.persistence-capable identity-type="application"
 *                          persistence-capable-superclass="org.nightlabs.jfire.prop.DataField"
 *                          detachable="true" table="JFireBase_Prop_RegexDataField"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.fetch-group name="FetchGroupsProp.fullData" fetch-groups="default" fields="text"
 */
@PersistenceCapable(
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireBase_Prop_RegexDataField")
@FetchGroups(
	@FetchGroup(
		fetchGroups={"default"},
		name="FetchGroupsProp.fullData",
		members=@Persistent(name="text"))
)
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class RegexDataField
extends DataField
implements II18nTextDataField
{
	/**
	 * The serial version of this class.
	 */
	private static final long serialVersionUID = 20090116L;

	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	protected String text;

	@Persistent(persistenceModifier=PersistenceModifier.NONE)
	private transient StaticI18nText textBuffer = null;

	/**
	 * For JDO only.
	 */
	protected RegexDataField() {
		super();
	}

	/**
	 * Create a new {@link RegexDataField} for the given {@link DataBlock}
	 * that represents the given {@link StructField}.
	 *
	 * @param dataBlock The {@link DataBlock} the new {@link PhoneNumberDataField} will be part of.
	 * @param structField The {@link StructField} the new {@link PhoneNumberDataField} represents in the data structure.
	 */
	public RegexDataField(DataBlock dataBlock, StructField<RegexDataField> structField) {
		super(dataBlock, structField);
	}

	/**
	 * Used for cloning.
	 */
	protected RegexDataField(String organisationID, long propertySetID, int dataBlockID, DataField cloneField) {
		super(organisationID, propertySetID, dataBlockID, cloneField);
	}

	/**
	 * Returns the value of this {@link RegexDataField}.
	 *
	 * @return The text-value fo this {@link RegexDataField}.
	 */
	public String getText() {
		return text;
	}

	/**
	 * Sets the value of this {@link RegexDataField}.
	 *
	 * @param text The value to set.
	 */
	public void setText(String text) {
		this.text = text;
		getI18nText().setStaticText(getStaticTextValue());
	}

	@Override
	public boolean isEmpty() {
		return text == null || text.isEmpty();
	}

	@Override
	public DataField cloneDataField(PropertySet propertySet, int dataBlockID) {
		RegexDataField newField = new RegexDataField(propertySet.getOrganisationID(), propertySet.getPropertySetID(), dataBlockID, this);
		newField.setText(getText());
		return newField;
	}

	private String getStaticTextValue() {
		if (isEmpty())
			return "";
		return getText();
	}

	@Override
	public StaticI18nText getI18nText() {
		if (textBuffer == null) {
			textBuffer = new StaticI18nText(getStaticTextValue());
		}
		return textBuffer;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Calling this method is equal to {@link #getText()}.
	 * </p>
	 */
	@Override
	public Object getData() {
		return getText();
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Takes the content from all supported input types (see {@link #supportsInputType(Class)}).
	 * </p>
	 */
	@Override
	public void setData(Object data) {
		if (data == null) {
			setText(null);
		} else if (data instanceof String) {
			setText((String) data);
		} else if (data instanceof RegexDataField) {
			setText(((RegexDataField) data).getText());
		} else if (data instanceof II18nTextDataField) {
			setText(((II18nTextDataField) data).getI18nText().getText());
		} else if (data instanceof I18nText) {
			setText(((I18nText) data).getText());
		} else {
			throw new IllegalArgumentException(this.getClass().getName() + " does not support input data of type " + data.getClass());
		}
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Supports String, {@link RegexDataField}, {@link II18nTextDataField}, {@link I18nText}.
	 * </p>
	 */
	@Override
	public boolean supportsInputType(Class<?> inputType) {
		return
			String.class.isAssignableFrom(inputType) ||
			RegexDataField.class.isAssignableFrom(inputType) ||
			I18nTextDataField.class.isAssignableFrom(inputType) ||
			I18nText.class.isAssignableFrom(inputType);
	}
}