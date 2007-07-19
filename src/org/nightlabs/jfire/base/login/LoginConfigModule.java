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
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 */
public class LoginConfigModule extends ConfigModule 
{
	private static final long serialVersionUID = 3L;

	private LinkedList<LoginConfiguration> loginConfigurations;
	private transient LoginConfiguration currentLoginConfiguration;
	
	/**
	 * This method indicates whether a call to {@link #persistCurrentConfiguration()} is required
	 * to add the currentLoginConfiguration to the list of persistent LoginConfigurations
	 */
	private static final boolean MANUAL_PERSISTING = true;

//	private String organisationID = null;
//	private String serverURL = null;
//	private String initialContextFactory = null;
//	private String securityProtocol = null;
////	private String loginModuleName = null;
//	private String userID = null;
//
//	private String workstationID;
//	private boolean automaticUpdate;
//
//	private ArrayList securityConfigurations = null;

	private int maxLoginConfigurations = 5;
	
	public LoginConfigModule() {
//		currentLoginConfiguration = new LoginConfiguration();
//		loginConfigurations = new LinkedList<LoginConfiguration>();
		// this initialisation should not be done here, but in init! Marco.
	}

	public void init() throws InitException {
		super.init();

		if (loginConfigurations == null)
			setLoginConfigurations(new LinkedList<LoginConfiguration>());

		if (currentLoginConfiguration == null) {
			setCurrentLoginConfiguration(new LoginConfiguration());
//			loginConfigurations.addFirst(currentLoginConfiguration);
			currentLoginConfiguration.init();
		}

		for (LoginConfiguration loginConfiguration : loginConfigurations)
			loginConfiguration.init();

//		loginConfigurations = new LinkedList<LoginConfiguration>();
		
		

//		if(workstationID == null)
//			workstationID = ""; //$NON-NLS-1$
//
//		if (organisationID == null)
//			organisationID = ""; //$NON-NLS-1$
//
//		if (userID == null)
//			userID = ""; //$NON-NLS-1$
//
//		// default values
//		if(initialContextFactory == null)
//			initialContextFactory = "org.jboss.security.jndi.LoginInitialContextFactory"; //$NON-NLS-1$
//		if (securityProtocol == null || "".equals(securityProtocol)) //$NON-NLS-1$
//			securityProtocol = "jfire"; //$NON-NLS-1$
//		if(serverURL == null)
//			serverURL = "jnp://localhost:1099"; //$NON-NLS-1$
//
//		if (securityConfigurations == null) {
//			securityConfigurations = new ArrayList();
//			securityConfigurations.add(
//					new JFireSecurityConfigurationEntry(
//							"jfire", //$NON-NLS-1$
//							"org.jboss.security.ClientLoginModule"							 //$NON-NLS-1$
//					)
//			);
//		}
	}

	public void setCurrentLoginConfiguration(String userID, String workstationID, String organisationID, String serverURL, String initialContextFactory,
			String securityProtocol) {
		acquireReadLock();
		currentLoginConfiguration = new LoginConfiguration(userID, workstationID, organisationID, serverURL, initialContextFactory, securityProtocol);
		currentLoginConfiguration.init();
		if (!MANUAL_PERSISTING)
			persistCurrentConfiguration();
		releaseLock();
	}

	public void persistCurrentConfiguration() {
		loginConfigurations.remove(currentLoginConfiguration);
		loginConfigurations.addFirst(currentLoginConfiguration);
		setLoginConfigurations(getLoginConfigurations());
		ensureCapacity();
	}

	public LinkedList<LoginConfiguration> getLoginConfigurations() {
		return loginConfigurations;
	}

	public void setLoginConfigurations(LinkedList<LoginConfiguration> loginConfigurations) {
		this.loginConfigurations = loginConfigurations;
		setChanged();
	}
	
	public LoginConfiguration getCurrentLoginConfiguration() {
		return currentLoginConfiguration;
	}
	
	public void setMaxLoginConfigurations(int maxLoginConfigurations) {
		this.maxLoginConfigurations = maxLoginConfigurations;
		setChanged();
		ensureCapacity();
	}
	
	public void setCurrentLoginConfiguration(LoginConfiguration currentLoginConfiguration) {
		this.currentLoginConfiguration = currentLoginConfiguration;
		setChanged();
	}

	public int getMaxLoginConfigurations() {
		return maxLoginConfigurations;
	}
	
	private void ensureCapacity() {
		acquireReadLock();
		while (loginConfigurations.size() > maxLoginConfigurations)
			loginConfigurations.removeLast();
		releaseLock();
	}
	
	public LoginConfiguration getLastLoginConfiguration() {
		if (loginConfigurations.isEmpty())
			return null;
		else
			return loginConfigurations.getFirst();
	}
	
//	public void copyFrom(LoginConfigModule source) {
//		this.currentLoginConfiguration = source.currentLoginConfiguration;
//		this.loginConfigurations = new LinkedList<LoginConfiguration>(source.loginConfigurations);
//		this.maxLoginConfigurations = source.maxLoginConfigurations;
//		setChanged();
//	}

//	public String getOrganisationID() {
//		return organisationID;
//	}
//	public void setOrganisationID(String organisationID) {
//		this.organisationID = organisationID;
//		setChanged();
//	}
//	public String getInitialContextFactory() {
//		return initialContextFactory;
//	}
//	public void setInitialContextFactory(String initialContextFactory) {
//		this.initialContextFactory = initialContextFactory;
//		setChanged();
//	}
//	public String getServerURL() {
//		return serverURL;
//	}
//	public void setServerURL(String serverURL) {
//		this.serverURL = serverURL;
//		setChanged();
//	}		
//	public String getSecurityProtocol() {
//		return securityProtocol;
//	}
//	public void setSecurityProtocol(String securityProtocol) {
//		this.securityProtocol = securityProtocol;
//		setChanged();
//	}		
//	public ArrayList getSecurityConfigurations() {
//		return securityConfigurations;
//	}
//	public void setSecurityConfigurations(ArrayList securityConfigurations) {
//		this.securityConfigurations = securityConfigurations;
//		setChanged();
//	}
//
//	public String getUserID() {
//		return userID;
//	}
//	public void setUserID(String userID) {
//		this.userID = userID;
//		setChanged();
//	}
//
//
//	public String getWorkstationID()
//	{
//		return workstationID;
//	}
//
//
//	public void setWorkstationID(String workstationID)
//	{
//		this.workstationID = workstationID;
//		setChanged();
//	}
//
//
//	public boolean getAutomaticUpdate()
//	{
//		return automaticUpdate;
//	}
//
//
//	public void setAutomaticUpdate(boolean automaticUpdate)
//	{
//		this.automaticUpdate = automaticUpdate;
//		setChanged();
//	}
}
