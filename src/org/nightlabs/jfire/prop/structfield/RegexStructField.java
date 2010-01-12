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

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.FetchGroups;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.PersistenceModifier;
import javax.jdo.annotations.Persistent;

import org.nightlabs.jfire.prop.DataBlock;
import org.nightlabs.jfire.prop.DataField;
import org.nightlabs.jfire.prop.StructBlock;
import org.nightlabs.jfire.prop.StructField;
import org.nightlabs.jfire.prop.datafield.RegexDataField;
import org.nightlabs.jfire.prop.id.StructFieldID;

/**
 * {@link StructField} that represents a {@link DataField} that stores text
 * but whose value should be validated against a regular expression.
 * The regular expression is {@link #getRegex()}.
 * 
 * @jdo.persistence-capable
 * 		identity-type="application"
 *    persistence-capable-superclass="org.nightlabs.jfire.prop.StructField"
 *   	detachable="true"
 *   	table="JFireBase_Prop_RegexStructField"
 *
 * @jdo.implements name="org.nightlabs.jfire.prop.StructField"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.fetch-group name="IStruct.fullData" fetch-groups="default"
 *
 * @author Tobias Langner <!-- tobias[dot]langner[at]nightlabs[dot]de -->
 */
@PersistenceCapable(
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireBase_Prop_RegexStructField")
@FetchGroups(
	@FetchGroup(
		fetchGroups={"default"},
		name="IStruct.fullData",
		members={})
)
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class RegexStructField extends StructField<RegexDataField> {

	private static final long serialVersionUID = 1L;

	/** @jdo.field persistence-modifier="persistent" */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	protected String regex = "";

	/** @jdo.field persistence-modifier="none" */
	@Persistent(persistenceModifier=PersistenceModifier.NONE)
	protected transient Pattern pattern;

	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected RegexStructField() { }

	/**
	 * @param _structBlock
	 * @param _structFieldID
	 */
	public RegexStructField(StructBlock _structBlock, StructFieldID _structFieldID) {
		super(_structBlock, _structFieldID);
	}

	/**
	 * @param _structBlock
	 */
	public RegexStructField(StructBlock _structBlock) {
		super(_structBlock);
	}

	public void setRegex(String regex) {
		if (!validateRegex(regex))
			throw new IllegalArgumentException("'simplePattern' must be a valid simple pattern containing no multipliers.");

		notifyModifyListeners();
		this.regex = regex;
	}

	public String getRegex() {
		return regex;
	}

	@Override
	protected RegexDataField createDataFieldInstanceInternal(DataBlock dataBlock) {
		return new RegexDataField(dataBlock, this);
	}

	public boolean validateRegex(String regex) {
		try {
			Pattern.compile(regex);
		} catch (PatternSyntaxException e) {
			return false;
		}
		return true;
	}

	public boolean validateData(String regex) {
		resetValidationError();

		boolean toReturn = true;
		if (!validateRegex(regex)) {
			setValidationError("The given pattern is no valid regular expression");
			toReturn &= false;
		}
		return toReturn;
	}

	public boolean validateValue(String value) {
		if (pattern == null)
			pattern = Pattern.compile(regex);

		boolean matches = pattern.matcher(value).matches();
		return matches;
	}

	@Override
	public Class<RegexDataField> getDataFieldClass() {
		return RegexDataField.class;
	}
}