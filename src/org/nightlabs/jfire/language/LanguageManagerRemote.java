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
	 * Creates a language if it does not exist. If it exists already, nothing is done.
	 *
	 * @param languageID ISO639-2 language code
	 * @param nativeName should be the language name in itself (e.g. French = Francais)
	 * @deprecated Use {@link #createLanguage(LanguageCf, boolean, boolean)} instead!
	 */
	@Deprecated
	void createLanguage(LanguageCf langCf) throws LanguageException;

	/**
	 * @return A Collection containing all languages that the current organisationID knows.
	 * @throws LanguageException if sth. unexpected happens - e.g. if the PersistenceManager
	 *   is not accessible.
	 */
	Collection<Language> getLanguages() throws LanguageException;

	Language createLanguage(LanguageCf langCf, boolean autoSync, boolean get)
			throws LanguageException;

	/**
	 * Delete a {@link Language} from the datastore.
	 *
	 * @param languageID the identifier of the {@link Language} to be deleted. Must not be <code>null</code>!
	 * @return <code>true</code>, if the language did exist and was deleted (i.e. the datastore was modified); <code>false</code>, if the language did not exist.
	 */
	boolean deleteLanguage(LanguageID languageID);

	LanguageConfig getLanguageConfig(String[] fetchGroups, int maxFetchDepth);

	LanguageConfig setLanguageConfig(LanguageConfig languageConfig,
			boolean get, String[] fetchGroups, int maxFetchDepth);
}