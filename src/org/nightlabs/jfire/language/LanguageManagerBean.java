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

/**
 * @ejb.bean name="jfire/ejb/JFireBaseBean/LanguageManager"
 *	jndi-name="jfire/ejb/JFireBaseBean/LanguageManager"
 *	type="Stateless"
 *
 * @ejb.util generate="physical"
 * @ejb.transaction type="Required"
 */
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
		logger.debug("LanguageManagerBean.createLanguage");
		try {
			PersistenceManager pm = this.getPersistenceManager();
			try {
				pm.getExtent(Language.class, false);

				try {
					pm.getObjectById(LanguageID.create(langCf.getLanguageID()));
					return;
				} catch (JDOObjectNotFoundException e) {
					// ignore and create a new language afterwards
				}

				Language newLang = new Language(langCf);
				pm.makePersistent(newLang);
				logger.debug("new language created..");
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
		  PersistenceManager pm = getPersistenceManager();
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
}
