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
import java.rmi.RemoteException;
import java.util.Collection;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
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
public abstract class LanguageManagerBean extends BaseSessionBeanImpl implements SessionBean
{
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(LanguageManagerBean.class);

	@Override
	public void setSessionContext(SessionContext sessionContext)
			throws EJBException, RemoteException
	{
		super.setSessionContext(sessionContext);
	}
	/**
	 * @ejb.create-method
	 * @ejb.permission role-name="_Guest_"
	 */
	public void ejbCreate() throws CreateException
	{
	}
	/**
	 * @ejb.permission unchecked="true"
	 */
	public void ejbRemove() throws EJBException, RemoteException { }

	/**
	 * @ejb.interface-method
	 * @ejb.transaction type="Supports"
	 * @ejb.permission role-name="_Guest_"
	 */
	@Override
	public String ping(String message) {
		return super.ping(message);
	}

	/**
	 * Creates a language if it does not exist. If it exists already, nothing is done.
	 *
	 * @param languageID ISO639-2 language code
	 * @param nativeName should be the language name in itself (e.g. French = Francais)
	 *
	 * @ejb.interface-method
	 *
	 * @ejb.permission role-name="org.nightlabs.jfire.language.createLanguage"
	 */
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


	/**
	 * @return A Collection containing all languages that the current organisationID knows.
	 * @throws LanguageException if sth. unexpected happens - e.g. if the PersistenceManager
	 *   is not accessible.
	 *
	 * @ejb.interface-method
	 *
	 * @ejb.permission role-name="_Guest_"
	 */
	@SuppressWarnings("unchecked")
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

//	/**
//	 * @param languageID ISO639-2 language code
//	 * @param throwExceptionIfNotExistent whether to return null or to throw a
//	 *   LanguageNotFoundException if desired language does not exist.
//	 * @return An instance of the desired Language.
//	 * @throws LanguageNotFoundException If the desired Language does not exist and
//	 *   throwExceptionIfNotExistent is true.
//	 *   This exception is an inheritor of LanguageException
//	 * @throws LanguageException If it's not a LanguageNotFoundException, sth. unexpected
//	 *   happened - maybe the PersistenceManager is not accessible.
//	 *
//	 * @see getLanguage(String languageID)
//	 */
//	public Language getLanguage(String languageID, boolean throwExceptionIfNotExistent)
//	throws LanguageException
//	{
//		try {
//			PersistenceManager pm = getPersistenceManager();
//			try {
//				Language lang = (Language)pm.getObjectById(LanguageID.create(languageID), true);
//				if (lang==null && throwExceptionIfNotExistent)
//					throw new LanguageNotFoundException("No language registered with languageID=\""+languageID+"\"!");
//				return lang;
//			} finally {
//				pm.close();
//			}
//		} catch (Exception x) {
//			throw new LanguageException(x);
//		}
//	}
//
//	/**
//	 * @param languageID ISO639-2 language code
//	 * @return An instance of the desired Language. Never null!
//	 * @throws LanguageNotFoundException If the desired Language does not exist.
//	 *   This exception is an inheritor of LanguageException
//	 * @throws LanguageException If it's not a LanguageNotFoundException, sth. unexpected
//	 *   happened - maybe the PersistenceManager is not accessible.
//	 *
//	 * @see getLanguage(String languageID, boolean throwExceptionIfNotExistent)
//	 */
//	public Language getLanguage(String languageID)
//	throws LanguageException
//	{
//		return getLanguage(languageID, true);
//	}

}
