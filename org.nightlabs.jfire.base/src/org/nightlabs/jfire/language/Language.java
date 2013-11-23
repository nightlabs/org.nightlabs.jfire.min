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

package org.nightlabs.jfire.language;

import java.io.Serializable;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.FetchGroups;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import org.nightlabs.jdo.ObjectIDUtil;
import org.nightlabs.jfire.language.id.LanguageID;
import org.nightlabs.language.LanguageCf;

/**
 * @author nick
 * @author Marco Schulze - Marco at NightLabs dot de
 */
@PersistenceCapable(
	objectIdClass=LanguageID.class,
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireBase_Language")
@FetchGroups(
	@FetchGroup(
		name="Language.name",
		members=@Persistent(name="name"))
)
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class Language implements Serializable
{
	private static final long serialVersionUID = 1L;

	@PrimaryKey
	@Column(length=25)
	private String languageID = null;

	@Column(length=100)
	private String nativeName = null;

	@Persistent(
			dependent="true",
			mappedBy="language"
	)
	private LanguageName name;

	protected Language() {}

	public Language(LanguageCf langCf)
	{
		if (!ObjectIDUtil.isValidIDString(langCf.getLanguageID()))
			throw new IllegalArgumentException("languageID \""+langCf.getLanguageID()+"\" is not a valid id!");

		this.languageID = langCf.getLanguageID();
		this.nativeName = langCf.getNativeName();
		this.name = new LanguageName(this);
		this.name.copyFrom(langCf.getName());
	}

	public LanguageCf createLanguageCf()
	{
		LanguageCf languageCf = new LanguageCf(languageID);
		languageCf.init(null);
		languageCf.setNativeName(nativeName);
		languageCf.getName().copyFrom(name);
		return languageCf;
	}

	/**
	 * @return Returns the nativeName.
	 */
	public String getNativeName()
	{
		return nativeName;
	}

	/**
	 * @param nativeName The nativeName to set.
	 */
	public void setNativeName(String _nativeName)
	{
		this.nativeName = _nativeName;
	}

	/**
	 * @return Returns the languageID.
	 */
	public String getLanguageID()
	{
		return languageID;
	}

	/**
	 * @param languageID The languageID to set.
	 */
	public void setLanguageID(String languageID)
	{
		this.languageID = languageID;
	}

	public LanguageName getName()
	{
		return name;
	}
}
