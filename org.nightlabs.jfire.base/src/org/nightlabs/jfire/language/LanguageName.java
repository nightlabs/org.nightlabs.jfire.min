package org.nightlabs.jfire.language;

import java.util.HashMap;
import java.util.Map;

import org.nightlabs.i18n.I18nText;

import javax.jdo.annotations.Join;
import javax.jdo.annotations.Persistent;
import org.nightlabs.jfire.language.id.LanguageNameID;
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
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.language.id.LanguageNameID"
 *		detachable="true"
 *		table="JFireBase_LanguageName"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.create-objectid-class
 *
 * @jdo.fetch-group name="Language.name" fields="language, names"
 *
 * @author Marco Schulze - Marco at NightLabs dot de
 */@PersistenceCapable(
	objectIdClass=LanguageNameID.class,
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireBase_LanguageName")
@FetchGroups(
	@FetchGroup(
		name="Language.name",
		members={@Persistent(name="language"), @Persistent(name="names")})
)
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)

public class LanguageName
extends I18nText
{
	private static final long serialVersionUID = 1L;

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="25"
	 */	@PrimaryKey
	@Column(length=25)

	private String languageID;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)

	private Language language;

	/**
	 * key: String languageID<br/>
	 * value: String name
	 *
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="map"
	 *		key-type="java.lang.String"
	 *		value-type="java.lang.String"
	 *		table="JFireBase_LanguageName_names"
	 *		null-value="exception"
	 *
	 * @jdo.join
	 */	@Join
	@Persistent(
		nullValue=NullValue.EXCEPTION,
		table="JFireBase_LanguageName_names",
		persistenceModifier=PersistenceModifier.PERSISTENT)

	private Map<String, String> names = new HashMap<String, String>();

	protected LanguageName()
	{
	}

	public LanguageName(Language language)
	{
		this.language = language;
		this.languageID = language.getLanguageID();
	}

	@Override
	@SuppressWarnings("unchecked")
	protected Map getI18nMap()
	{
		return names;
	}

	/**
	 * @see org.nightlabs.i18n.I18nText#getFallBackValue(java.lang.String)
	 */
	@Override
	protected String getFallBackValue(String languageID)
	{
		return languageID;
	}

	public String getLanguageID()
	{
		return languageID;
	}

	public Language getLanguage()
	{
		return language;
	}
}
