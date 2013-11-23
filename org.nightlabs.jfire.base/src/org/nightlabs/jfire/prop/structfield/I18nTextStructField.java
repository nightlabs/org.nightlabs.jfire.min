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

package org.nightlabs.jfire.prop.structfield;

import org.nightlabs.jfire.prop.DataBlock;
import org.nightlabs.jfire.prop.DataField;
import org.nightlabs.jfire.prop.StructBlock;
import org.nightlabs.jfire.prop.StructField;
import org.nightlabs.jfire.prop.datafield.I18nTextDataField;
import org.nightlabs.jfire.prop.id.StructFieldID;

import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.FetchGroups;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.PersistenceModifier;

/**
 * {@link StructField} that represents a {@link DataField} holding an internationalised text.
 * The {@link I18nTextStructField} can configure the ui the data is shown/edited
 * by setting the visible line-count.
 * 
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 * 
 * @jdo.persistence-capable
 * 		persistence-capable-superclass="org.nightlabs.jfire.prop.StructField"
 *		detachable="true"
 *		table="JFireBase_Prop_I18nTextStructField"
 *
 * @jdo.implements name="org.nightlabs.jfire.prop.StructField"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.fetch-group name="IStruct.fullData" fetch-groups="default"
 *
 */
@PersistenceCapable(
	detachable="true",
	table="JFireBase_Prop_I18nTextStructField")
@FetchGroups(
	@FetchGroup(
		fetchGroups={"default"},
		name="IStruct.fullData",
		members={})
)
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class I18nTextStructField extends StructField<I18nTextDataField> {

	private static final long serialVersionUID = 1L;

	/** @jdo.field persistence-modifier="persistent" */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private int lineCount = 1;

	/**
	 * For JDO only.
	 */
	protected I18nTextStructField() {
		super();
	}

	/**
	 * Create a new {@link I18nTextStructField} for the given {@link StructBlock} 
	 * and with primary-key values from the given {@link StructFieldID}.
	 * 
	 * @param structBlock The {@link StructBlock} the new {@link I18nTextStructField} will be part of.
	 * @param structFieldID The {@link StructFieldID} the new {@link I18nTextStructField} should take primary-key values from.
	 */
	public I18nTextStructField(StructBlock structBlock, StructFieldID structFieldID) {
		super(structBlock, structFieldID);
	}
	/**
	 * Create a new {@link I18nTextStructField} for the given {@link StructBlock}.
	 * 
	 * @see StructField#StructField(StructBlock)
	 * @param structBlock The {@link StructBlock} the new {@link I18nTextStructField} will be part of.
	 */
	public I18nTextStructField(StructBlock structBlock) {
		super(structBlock);
	}
	
	/**
	 * This is a hint for the data editor. It defines how many lines the edit field should display.
	 */
	public int getLineCount() {
		return lineCount;
	}

	/**
	 * Sets the line count which must be greater than 0.
	 * @param lineCount The line count to set.
	 */
	public void setLineCount(int lineCount) {
		if (! (lineCount > 0))
			throw new IllegalArgumentException("lineCount must be greater than 0");

		this.lineCount = lineCount;
	}

	/*
	 * (non-Javadoc)
	 * @see org.nightlabs.jfire.prop.StructField#createDataFieldInstanceInternal(org.nightlabs.jfire.prop.DataBlock)
	 */
	@Override
	protected I18nTextDataField createDataFieldInstanceInternal(DataBlock dataBlock) {
		return new I18nTextDataField(dataBlock, this);
	}

	/*
	 * (non-Javadoc)
	 * @see org.nightlabs.jfire.prop.StructField#getDataFieldClass()
	 */
	@Override
	public Class<I18nTextDataField> getDataFieldClass() {
		return I18nTextDataField.class;
	}
}
