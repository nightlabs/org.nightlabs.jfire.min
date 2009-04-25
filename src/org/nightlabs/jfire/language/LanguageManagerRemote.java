package org.nightlabs.jfire.language;

import java.util.Collection;

import javax.ejb.Remote;

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
	 */
	void createLanguage(LanguageCf langCf) throws LanguageException;

	/**
	 * @return A Collection containing all languages that the current organisationID knows.
	 * @throws LanguageException if sth. unexpected happens - e.g. if the PersistenceManager
	 *   is not accessible.
	 */
	Collection<Language> getLanguages() throws LanguageException;
}