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
import java.util.Collection;

import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.jdo.FetchPlan;
import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;

import org.apache.log4j.Logger;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.base.BaseSessionBeanImpl;
import org.nightlabs.jfire.language.id.LanguageID;
import org.nightlabs.language.LanguageCf;

@TransactionAttribute(TransactionAttributeType.REQUIRED)
@Stateless
public class LanguageManagerBean extends BaseSessionBeanImpl implements LanguageManagerRemote
{
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(LanguageManagerBean.class);

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.language.LanguageManagerRemote#createLanguage(org.nightlabs.language.LanguageCf)
	 */
	@RolesAllowed("org.nightlabs.jfire.language.createLanguage")
	@Override
	public void createLanguage(LanguageCf langCf)
	throws LanguageException
	{
		createLanguage(langCf, true, false);
	}

	@RolesAllowed("org.nightlabs.jfire.language.createLanguage")
	@Override
	public Language createLanguage(LanguageCf langCf, boolean autoSync, boolean get)
	throws LanguageException
	{
		if (langCf == null)
			throw new IllegalArgumentException("langCf == null");

		logger.debug("LanguageManagerBean.createLanguage");
		try {
			PersistenceManager pm = this.createPersistenceManager();
			try {
				pm.getExtent(Language.class);

				Language language;
				try {
					language = (Language) pm.getObjectById(LanguageID.create(langCf.getLanguageID()));
				} catch (JDOObjectNotFoundException e) {
					if (autoSync) {
						switch (LanguageConfig.getLanguageConfig(pm).getLanguageSyncMode()) {
							case off:
								throw new LanguageSyncDeactivatedException("LanguageSyncMode is off!");
							case oneOnly:
								if (pm.getExtent(Language.class).iterator().hasNext())
									throw new LanguageSyncDeactivatedException("LanguageSyncMode is oneOnly and at least one language already exists!");
								else
									break;
							default:
								break;
						}
					}
					// ignore and create a new language afterwards
					language = new Language(langCf);
					language = pm.makePersistent(language);
					logger.debug("new language created..");
				}
				if (!get)
					return null;
				pm.getFetchPlan().setMaxFetchDepth(NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT);
				pm.getFetchPlan().addGroup(FetchPlan.ALL);
				return pm.detachCopy(language);
			} finally {
				pm.close();
			}
		} catch(Exception e) {
			logger.debug("exception: " + e.getMessage());
			throw new LanguageException(e);
		}
	}


	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.language.LanguageManagerRemote#getLanguages()
	 */
	@RolesAllowed("_Guest_")
	@SuppressWarnings("unchecked")
	@Override
	public Collection<Language> getLanguages()
	throws LanguageException
	{
		try {
			PersistenceManager pm = createPersistenceManager();
			try {
				pm.getFetchPlan().setMaxFetchDepth(NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT);
				pm.getFetchPlan().addGroup(FetchPlan.ALL);
				return pm.detachCopyAll((Collection<Language>)pm.newQuery(Language.class).execute());
			} finally {
				pm.close();
			}
		} catch (Exception x) {
			throw new LanguageException(x);
		}
	}

	@RolesAllowed("_Guest_")
	@Override
	public LanguageConfig getLanguageConfig(String[] fetchGroups, int maxFetchDepth) {
		PersistenceManager pm = createPersistenceManager();
		try {
			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);
			return pm.detachCopy(LanguageConfig.getLanguageConfig(pm));
		} finally {
			pm.close();
		}
	}

	@RolesAllowed("org.nightlabs.jfire.language.createLanguage") // TODO not nice, but for now it's OK.
	@Override
	public LanguageConfig setLanguageConfig(LanguageConfig languageConfig, boolean get, String[] fetchGroups, int maxFetchDepth) {
		if (languageConfig == null)
			throw new IllegalArgumentException("languageConfig == null");

		PersistenceManager pm = createPersistenceManager();
		try {
			return NLJDOHelper.storeJDO(pm, languageConfig, get, fetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}
	}

	@Override
	public boolean deleteLanguage(LanguageID languageID) {
		if (languageID == null)
			throw new IllegalArgumentException("languageID == null");

		PersistenceManager pm = createPersistenceManager();
		try {
			Object language = null;
			try {
				language = pm.getObjectById(languageID);
			} catch (JDOObjectNotFoundException x) { } // silently ignore non-existent language

			if (language != null) {
				pm.deletePersistent(language);
				return true;
			}
			return false;
		} finally {
			pm.close();
		}
	}
}
