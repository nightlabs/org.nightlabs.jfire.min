/*
 * Created on Mar 15, 2004
 *
 */
package org.nightlabs.ipanema.language;
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
import org.nightlabs.ipanema.base.BaseSessionBeanImpl;
import org.nightlabs.ipanema.language.Language;
import org.nightlabs.ipanema.language.LanguageException;
import org.nightlabs.ipanema.language.LanguageNotFoundException;
import org.nightlabs.ipanema.language.id.LanguageID;

import org.nightlabs.language.LanguageCf;

/**
 * @ejb.bean name="ipanema/ejb/JFireBaseBean/LanguageManager"
 *	jndi-name="ipanema/ejb/JFireBaseBean/LanguageManager"
 *	type="Stateless"
 *   
 * @ejb.util generate = "physical"
 */
public abstract class LanguageManagerBean extends BaseSessionBeanImpl implements SessionBean 
{
	public static final Logger LOGGER = Logger.getLogger(LanguageManagerBean.class);
	/**
	 * @see org.nightlabs.ipanema.base.BaseSessionBeanImpl#setSessionContext(javax.ejb.SessionContext)
	 */
	public void setSessionContext(SessionContext sessionContext)
			throws EJBException, RemoteException
	{
		super.setSessionContext(sessionContext);
	}
	/**
	 * @ejb.create-method  
	 * @ejb.permission role-name="LanguageManager-read"
	 */
	public void ejbCreate() throws CreateException
	{
//		try
//		{
//			LOGGER.debug("LanguageManagerBean created by " + this.getPrincipalString());
//		}
//		catch (ModuleException e)
//		{
//			throw new CreateException(e.getMessage());
//		}
	}
	/**
	 * @see javax.ejb.SessionBean#ejbRemove()
	 * 
	 * @ejb.permission unchecked="true"
	 */
	public void ejbRemove() throws EJBException, RemoteException { }

	/**
	 * Creates a language if it does not exist. If it exists already, nothing is done.
	 * 
	 * @param languageID ISO639-2 language code
	 * @param nativeName should be the language name in itself (e.g. French = Francais)
	 *
	 * @ejb.interface-method
	 *
	 * @ejb.permission role-name="LanguageManager-write"
	 **/
	public void createLanguage(LanguageCf langCf) 
	throws LanguageException
	{
		LOGGER.debug("LanguageManagerBean.createLanguage");
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
				LOGGER.debug("new language created..");
			} finally {
				pm.close();
			}
		} catch(Exception e) {
			LOGGER.debug("exception: " + e.getMessage());
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
	 * @ejb.permission role-name="LanguageManager-read"
	 */
	public Collection getLanguages()
	throws LanguageException
	{
		try {
		  PersistenceManager pm = getPersistenceManager();
		  try {
			  pm.getFetchPlan().addGroup(FetchPlan.ALL);
			  return pm.detachCopyAll((Collection)pm.newQuery(Language.class).execute());
		  } finally {
		  	pm.close();
		  }
		} catch (Exception x) {
			throw new LanguageException(x);
		}
	}
	
	/**
	 * @param languageID ISO639-2 language code
	 * @param throwExceptionIfNotExistent whether to return null or to throw a 
	 *   LanguageNotFoundException if desired language does not exist.
	 * @return An instance of the desired Language.
	 * @throws LanguageNotFoundException If the desired Language does not exist and
	 *   throwExceptionIfNotExistent is true.
	 *   This exception is an inheritor of LanguageException 
	 * @throws LanguageException If it's not a LanguageNotFoundException, sth. unexpected
	 *   happened - maybe the PersistenceManager is not accessible.
	 * 
	 * @see getLanguage(String languageID)
	 */
	public Language getLanguage(String languageID, boolean throwExceptionIfNotExistent)
	throws LanguageException
	{
		try {
			PersistenceManager pm = getPersistenceManager();
			try {
				Language lang = (Language)pm.getObjectById(LanguageID.create(languageID), true);
				if (lang==null && throwExceptionIfNotExistent) 
					throw new LanguageNotFoundException("No language registered with languageID=\""+languageID+"\"!");
				return lang;
			} finally {
				pm.close();
			}
		} catch (Exception x) {
			throw new LanguageException(x);
		}
	}

	/**
	 * @param languageID ISO639-2 language code
	 * @return An instance of the desired Language. Never null!
	 * @throws LanguageNotFoundException If the desired Language does not exist.
	 *   This exception is an inheritor of LanguageException 
	 * @throws LanguageException If it's not a LanguageNotFoundException, sth. unexpected
	 *   happened - maybe the PersistenceManager is not accessible.
	 * 
	 * @see getLanguage(String languageID, boolean throwExceptionIfNotExistent)
	 */
	public Language getLanguage(String languageID)
	throws LanguageException
	{
		return getLanguage(languageID, true);
	}

}