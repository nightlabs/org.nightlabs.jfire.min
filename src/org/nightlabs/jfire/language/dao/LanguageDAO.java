package org.nightlabs.jfire.language.dao;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.nightlabs.jfire.base.JFireEjb3Factory;
import org.nightlabs.jfire.base.jdo.BaseJDOObjectDAO;
import org.nightlabs.jfire.language.Language;
import org.nightlabs.jfire.language.LanguageManagerRemote;
import org.nightlabs.jfire.language.id.LanguageID;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.progress.ProgressMonitor;

public class LanguageDAO extends BaseJDOObjectDAO<LanguageID, Language> {

	private static LanguageDAO sharedInstance = null;
	private LanguageManagerRemote languageManager;

	protected LanguageDAO() {
	}

	public static synchronized LanguageDAO sharedInstance() {
		if (sharedInstance == null) {
			sharedInstance = new LanguageDAO();
		}
		return sharedInstance;
	}

	@Override
	protected Collection<Language> retrieveJDOObjects(Set<LanguageID> languageIDs, String[] fetchGroups, int maxFetchDepth,
		ProgressMonitor monitor) throws Exception {

		LanguageManagerRemote lm = languageManager;
		if (lm == null) {
			lm = JFireEjb3Factory.getRemoteBean(LanguageManagerRemote.class, SecurityReflector.getInitialContextProperties());
		}
		return lm.getLanguages();
	}

	public List<Language> getLanguages(Set<LanguageID> languagenIDs, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor) {
		return getJDOObjects(null, languagenIDs, fetchGroups, maxFetchDepth, monitor);
	}
}
