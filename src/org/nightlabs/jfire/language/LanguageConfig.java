package org.nightlabs.jfire.language;

import java.io.Serializable;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.annotations.Column;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.NullValue;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import org.nightlabs.jfire.language.id.LanguageConfigID;
import org.nightlabs.jfire.organisation.LocalOrganisation;
import org.nightlabs.jfire.organisation.Organisation;

@PersistenceCapable(
		identityType=IdentityType.APPLICATION,
		objectIdClass=LanguageConfigID.class
)
public class LanguageConfig
implements Serializable
{
	private static final long serialVersionUID = 1L;

	@PrimaryKey
	@Column(length=100)
	private String organisationID;

	@Persistent(nullValue=NullValue.EXCEPTION)
	private LanguageSyncMode languageSyncMode = LanguageSyncMode.oneOnly;

	private LanguageConfig() { }

	private LanguageConfig(String organisationID) {
		Organisation.assertValidOrganisationID(organisationID);
		this.organisationID = organisationID;
	}

	public static LanguageConfig getLanguageConfig(PersistenceManager pm)
	{
		String organisationID = LocalOrganisation.getLocalOrganisation(pm).getOrganisationID();
		LanguageConfigID languageConfigID = LanguageConfigID.create(organisationID);
		pm.getExtent(LanguageConfig.class);

		LanguageConfig languageConfig;
		try {
			languageConfig = (LanguageConfig) pm.getObjectById(languageConfigID);
		} catch (JDOObjectNotFoundException x) {
			languageConfig = new LanguageConfig();
			languageConfig = pm.makePersistent(languageConfig);
		}
		return languageConfig;
	}

	public String getOrganisationID() {
		return organisationID;
	}

	public LanguageSyncMode getLanguageSyncMode() {
		return languageSyncMode;
	}
	public void setLanguageSyncMode(LanguageSyncMode languageSyncMode) {
		this.languageSyncMode = languageSyncMode;
	}
}
