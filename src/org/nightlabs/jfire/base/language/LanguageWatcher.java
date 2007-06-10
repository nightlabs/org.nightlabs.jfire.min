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

package org.nightlabs.jfire.base.language;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.jface.action.IAction;
import org.nightlabs.base.language.LanguageManager;
import org.nightlabs.config.Config;
import org.nightlabs.config.ConfigException;
import org.nightlabs.jfire.base.login.Login;
import org.nightlabs.jfire.base.login.LoginStateListener;
import org.nightlabs.jfire.language.Language;
import org.nightlabs.jfire.language.LanguageManagerUtil;
import org.nightlabs.l10n.GlobalL10nSettings;
import org.nightlabs.language.LanguageCf;

/**
 * A shared instance of LanguageWatcher is used to create
 * the clients language on the server if not existent
 * and to ask the user to restart the client if it was
 * not started with his language. ...really? Marco :-)
 * 
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 */
public class LanguageWatcher implements LoginStateListener {
	
	/**
	 * LOG4J logger used by this class
	 */
	private static final Logger logger = Logger.getLogger(LanguageWatcher.class);
	
	private Map languageChecks = new HashMap();
	private static LanguageWatcher sharedInstance;
	
	private boolean isLanguageChecked(String userName) {
		if (!languageChecks.containsKey(userName))
			return false;
		Boolean checked = (Boolean) languageChecks.get(userName);
		return checked.booleanValue();
	}
	
	private void setLanguageChecked(String userName, boolean checked) {
		languageChecks.put(userName,new Boolean(checked));
	}

	/**
	 * @see org.nightlabs.jfire.base.login.LoginStateListener#loginStateChanged(int, org.eclipse.jface.action.IAction)
	 */
	public void loginStateChanged(int loginState, IAction action) {
		switch (loginState) {
			case Login.LOGINSTATE_LOGGED_IN:
				logger.debug("loginStateChanged(..): syncing languages"); //$NON-NLS-1$
				String userName = Login.sharedInstance().getLoginContext().getUsername();
				if (!isLanguageChecked(userName)) {
					syncLanguages();
					checkUserLanguage();
					setLanguageChecked(userName,true);
				}
				break;
			default:
				break;
		}
	}
	
	/**
	 * Download all languages from the server and upload all the client's
	 * languages that are missing on the server.
	 */
	protected void syncLanguages() {
		org.nightlabs.jfire.language.LanguageManager remoteLanguageManager = null;		
		try {
			LanguageManager localLanguageManager = LanguageManager.sharedInstance();
			
			remoteLanguageManager = LanguageManagerUtil.getHome(
					Login.getLogin().getInitialContextProperties()
			).create();

			// download all languages known to the organisation on the server
			Collection languages = remoteLanguageManager.getLanguages();
			Set<String> languageIDs = new HashSet<String>();
			Set remoteLanguageIDSet = new HashSet(languages.size());
			for (Iterator it = languages.iterator(); it.hasNext(); ) {
				Language language = (Language) it.next();
				String languageID = language.getLanguageID();
				remoteLanguageIDSet.add(languageID);
				LanguageCf langCf = localLanguageManager.getLanguage(language.getLanguageID(), false);
				if (langCf == null) {
					// language does not exist in the client => create it and copy all properties.
//					langCf = new LanguageCf(languageID);
//					langCf.setNativeName(language.getNativeName());
					langCf = language.createLanguageCf();
					localLanguageManager.addLanguage(langCf);
				}
				else {
					// copy properties
					if (!langCf.getNativeName().equals(language.getNativeName())) {
						langCf.setNativeName(language.getNativeName());
						localLanguageManager.makeDirty(langCf);
					}
					if (langCf.getName().copyFrom(language.getName()))
						localLanguageManager.makeDirty(langCf);
				}
			}

			for (Iterator it = localLanguageManager.getLanguages().iterator(); it.hasNext(); ) {
				LanguageCf langCf = (LanguageCf) it.next();
				languageIDs.add(langCf.getLanguageID());
			}

			// check client's languages and create the ones which are missing on the server
			for (Iterator it = localLanguageManager.getLanguages().iterator(); it.hasNext(); ) {
				LanguageCf langCf = (LanguageCf) it.next();

				{ // init langCf again
					int namesCount = langCf.getName().getTexts().size();
					langCf.init(languageIDs);
					if (namesCount != langCf.getName().getTexts().size())
						localLanguageManager.makeDirty(langCf);
				}

				String languageID = langCf.getLanguageID();
				if (!remoteLanguageIDSet.contains(languageID)) {
					// language does not exist in the organisation on the server => create it
					remoteLanguageManager.createLanguage(langCf); // TODO we should add additional translations of the language name to the server
				}
			}
		} catch (Exception e) {
			logger.error("Failed syncing languages: ",e); //$NON-NLS-1$
		}
	}
	
	/**
	 * Checks if Locale has the users prefered language and 
	 * asks to restart if not
	 */
	protected void checkUserLanguage() {
		// TODO: implement language check and restart dialog
	}
	
	
	public static void showRestartDialog() {
		
	}
	
	public static LanguageWatcher sharedInstance() {
		if (sharedInstance == null) {
			sharedInstance = new LanguageWatcher();
		}
		return sharedInstance;
	}
	
//	/**
//	 * Registeres the shared intance as LoginStateListener
//	 */
//	public static void registerAsLoginStateListener() {
//		try {
//			Login.getLogin(false).addLoginStateListener(getSharedInstance());
//		} catch (LoginException e) {
//			throw
//		}
//	}
	
	static {
		try {
			Config.sharedInstance().createConfigModule(GlobalL10nSettings.class);
		} catch (ConfigException e) {
			logger.error("Error creating GlobalL10nSettings Cf-Mod.",e); //$NON-NLS-1$
		}
	}

}
