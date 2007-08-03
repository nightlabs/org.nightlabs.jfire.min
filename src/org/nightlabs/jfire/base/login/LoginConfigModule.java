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

package org.nightlabs.jfire.base.login;

import java.util.LinkedList;

import org.nightlabs.config.ConfigModule;
import org.nightlabs.config.InitException;

/**
 * This class holds all user specific data relevant for login in into JFIre. It holds a list of
 * {@link LoginConfiguration}s that may be presented to the user upon login to reuse. 
 * 
 * @author Tobias Langner <!-- tobias[dot]langner[at]nightlabs[dot]de -->
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 */
public class LoginConfigModule extends ConfigModule 
{
	private static final long serialVersionUID = 3L;

	/**
	 * Holds the login configurations that have been saved upon request by the user.
	 */
	private LinkedList<LoginConfiguration> loginConfigurations;
	
	/**
	 * Holds the login configuration that is currently used.
	 */
	private LoginConfiguration latestLoginConfiguration;
	
	private int maxLoginConfigurations = 5;
	
	public void init() throws InitException {
		super.init();

		if (loginConfigurations == null)
			setLoginConfigurations(new LinkedList<LoginConfiguration>());

		for (LoginConfiguration loginConfiguration : loginConfigurations)
			loginConfiguration.init();
	}

	public void setLatestLoginConfiguration(String userID, String workstationID, String organisationID, String serverURL, String initialContextFactory,
			String securityProtocol, String configurationName) {
		acquireReadLock();
		
		LoginConfiguration loginConfiguration = new LoginConfiguration(userID, workstationID, organisationID, serverURL, initialContextFactory, securityProtocol, configurationName);
		loginConfiguration.init();		
		setLatestLoginConfiguration(loginConfiguration);
		
		releaseLock();
	}

	public void saveLatestConfiguration() {
		acquireReadLock();
		
		try {
			LoginConfiguration copy = (LoginConfiguration) latestLoginConfiguration.clone();
			loginConfigurations.remove(copy);
			loginConfigurations.addFirst(copy);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}		
		setChanged();
		releaseLock();
		ensureCapacity();
	}

	public LinkedList<LoginConfiguration> getLoginConfigurations() {
//		return new LinkedList<LoginConfiguration>(loginConfigurations);
		return loginConfigurations;
	}

	public void setLoginConfigurations(LinkedList<LoginConfiguration> loginConfigurations) {
		this.loginConfigurations = loginConfigurations;
		setChanged();
	}
	
	public LoginConfiguration getLatestLoginConfiguration() {
		return latestLoginConfiguration;
	}
	
	public void setLatestLoginConfiguration(LoginConfiguration currentLoginConfiguration) {
		this.latestLoginConfiguration = currentLoginConfiguration;
		setChanged();
	}
	
	public void setMaxLoginConfigurations(int maxLoginConfigurations) {
		this.maxLoginConfigurations = maxLoginConfigurations;
		setChanged();
		ensureCapacity();
	}
	
	public int getMaxLoginConfigurations() {
		return maxLoginConfigurations;
	}
	
	private void ensureCapacity() {
		acquireReadLock();
		
		while (loginConfigurations.size() > maxLoginConfigurations && !loginConfigurations.isEmpty())
			loginConfigurations.removeLast();
		
		releaseLock();
	}
	
	public LoginConfiguration getLastSavedLoginConfiguration() {
		if (loginConfigurations.isEmpty())
			return null;
		else
			return loginConfigurations.getFirst();
	}
	

	public void makeLatestFirst() {
		for (LoginConfiguration cfg : loginConfigurations) {
			if (cfg.equals(latestLoginConfiguration)) {
				loginConfigurations.remove(cfg);
				loginConfigurations.addFirst(cfg);
				return;
			}
		}
	}
}
