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
import org.nightlabs.jfire.prop.datafield.TextDataField;
import org.nightlabs.jfire.prop.id.StructFieldID;

import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.FetchGroups;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceModifier;

/**
 * {@link StructField} that represents a {@link DataField} that stores text.
 *  
 * @jdo.persistence-capable identity-type="application"
 *                          persistence-capable-superclass="org.nightlabs.jfire.prop.StructField"
 *                          detachable="true" table="JFireBase_Prop_TextStructField"
 *
 * @jdo.implements name="org.nightlabs.jfire.prop.StructField"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.fetch-group name="IStruct.fullData" fetch-groups="default"
 *
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 */
@PersistenceCapable(
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireBase_Prop_TextStructField")
@FetchGroups(
	@FetchGroup(
		fetchGroups={"default"},
		name="IStruct.fullData",
		members={})
)
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class TextStructField extends StructField<TextDataField>
{
	/**
	 * The serial version of this class.
	 */
	private static final long serialVersionUID = 1L;

	/** @jdo.field persistence-modifier="persistent" */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private int lineCount = 1;

	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected TextStructField() { }

	public TextStructField(StructBlock _structBlock, StructFieldID _structFieldID) {
		super(_structBlock, _structFieldID);
	}

	public TextStructField(StructBlock _structBlock) {
		super(_structBlock);
	}

	@Override
	protected TextDataField createDataFieldInstanceInternal(DataBlock dataBlock) {
		return new TextDataField(dataBlock, this);
	}

	public int getLineCount() {
		return lineCount;
	}

	public void setLineCount(int lineCount) {
		if (!validateLineCount(lineCount))
			throw new IllegalArgumentException("Line count must be greater than zero.");

		this.lineCount = lineCount;

		notifyModifyListeners();
	}

//	public boolean validateData(int lineCount) {
//		if (!validateLineCount(lineCount)) {
//			setValidationError("Line count must be greater than zero.");
//			return false;
//		}
//		resetValidationError();
//		return true;
//	}
//
	public boolean validateLineCount(int lineCount) {
		return lineCount > 0;
	}

	@Override
	public Class<TextDataField> getDataFieldClass() {
		return TextDataField.class;
	}
}