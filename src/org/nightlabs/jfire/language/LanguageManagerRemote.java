package org.nightlabs.jfire.language;

import java.util.Collection;

import javax.ejb.Remote;

import org.nightlabs.jfire.language.id.LanguageID;
import org.nightlabs.language.LanguageCf;

@Remote
public interface LanguageManagerRemote
{
	String ping(String message);

	/**
	 * @return A Collection containing all {@link Language}s that the current organisationID knows.
	 * @throws LanguageException if sth. unexpected happens - e.g. if the PersistenceManager is not accessible.
	 */
	Collection<Language> getLanguages() throws LanguageException;

	/**
	 * Creates a new language if it does not yet exist.
	 * @param langCf {@link LanguageCf} instance for the considered language.
	 * @param autoSync If true, current language sync mode will be examined, otherwise not.
	 * @param get If true, a detached copy of the considered language will be returned by this method, otherwise null
	 * will be returned.
	 * @return A detached copy of the considered language or null (depending on get parameter).
	 * @throws LanguageException if sth. unexpected happens - e.g. if the PersistenceManager is not accessible.
	 */
	Language createLanguage(LanguageCf langCf, boolean autoSync, boolean get) throws LanguageException;

	/**
	 * Delete a {@link Language} from the datastore.
	 * @param languageID The identifier of the {@link Language} to be deleted. Must not be <code>null</code>!
	 * @return <code>true</code>, if the language did exist and was deleted (i.e. the datastore was modified);
	 * <code>false</code>, if the language did not exist.
	 */
	boolean deleteLanguage(LanguageID languageID);

	/**
	 * Retrieves the current language configuration.
	 * @param fetchGroups
	 * @param maxFetchDepth
	 * @return The current language configuration.
	 */
	LanguageConfig getLanguageConfig(String[] fetchGroups, int maxFetchDepth);

	/**
	 * Stores/updates the current language configuration.
	 * @param languageConfig The language configuration to be used from now on.
	 * @param get If true, a detached copy of the new language configuration will be returned by this method, otherwise null.
	 * @param fetchGroups
	 * @param maxFetchDepth
	 * @return A detached copy of the new language configuration or null (depending on get parameter).
	 */
	LanguageConfig storeLanguageConfig(LanguageConfig languageConfig, boolean get, String[] fetchGroups, int maxFetchDepth);

//	public boolean deleteLanguageConfig();
}