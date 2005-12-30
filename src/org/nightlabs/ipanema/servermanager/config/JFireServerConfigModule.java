/* ************************************************************************** *
 * Copyright (C) 2004 NightLabs GmbH, Marco Schulze                           *
 * All rights reserved.                                                       *
 * http://www.NightLabs.de                                                    *
 *                                                                            *
 * This program and the accompanying materials are free software; you can re- *
 * distribute it and/or modify it under the terms of the GNU General Public   *
 * License as published by the Free Software Foundation; either ver 2 of the  *
 * License, or any later version.                                             *
 *                                                                            *
 * This module is distributed in the hope that it will be useful, but WITHOUT *
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FIT- *
 * NESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more *
 * details.                                                                   *
 *                                                                            *
 * You should have received a copy of the GNU General Public License along    *
 * with this module; if not, write to the Free Software Foundation, Inc.:     *
 *    59 Temple Place, Suite 330                                              *
 *    Boston MA 02111-1307                                                    *
 *    USA                                                                     *
 *                                                                            *
 * Or get it online:                                                          *
 *    http://www.opensource.org/licenses/gpl-license.php                      *
 *                                                                            *
 * In case, you want to use this module or parts of it in a proprietary pro-  *
 * ject, you can purchase it under the NightLabs Commercial License. Please   *
 * contact NightLabs GmbH under info AT nightlabs DOT com for more infos or   *
 * visit http://www.NightLabs.com                                             *
 * ************************************************************************** */

/*
 * Created on 07.06.2004
 */
package org.nightlabs.ipanema.servermanager.config;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.log4j.Logger;
import org.nightlabs.ipanema.server.LocalServer;
import org.nightlabs.ipanema.server.Server;
import org.nightlabs.ipanema.servermanager.dbcreate.DatabaseCreatorMySQL;

import org.nightlabs.ModuleException;
import org.nightlabs.config.ConfigModule;
import org.nightlabs.config.InitException;

/**
 * @author marco
 */
public class JFireServerConfigModule extends ConfigModule
{
	public static Logger LOGGER = Logger.getLogger(JFireServerConfigModule.class);
	
	public static class J2ee implements Serializable
	{
		private String j2eeDeployBaseDirectory;
//		private String j2eeVendorAdapterClassName;
		
		/**
		 * @return Returns the j2eeDeployBaseDirectory.
		 */
		public String getJ2eeDeployBaseDirectory() {
			return j2eeDeployBaseDirectory;
		}
		/**
		 * @param deployBaseDirectory The j2eeDeployBaseDirectory to set.
		 */
		public void setJ2eeDeployBaseDirectory(String deployBaseDirectory) {
			j2eeDeployBaseDirectory = deployBaseDirectory;
		}
//		/**
//		 * @return Returns the j2eeVendorAdapterClassName.
//		 */
//		public String getJ2eeVendorAdapterClassName() {
//			return j2eeVendorAdapterClassName;
//		}
//		/**
//		 * @param vendorAdapterClassName The j2eeVendorAdapterClassName to set.
//		 */
//		public void setJ2eeVendorAdapterClassName(String vendorAdapterClassName) {
//			j2eeVendorAdapterClassName = vendorAdapterClassName;
//		}
		
		public void init()
		{
//			if (j2eeVendorAdapterClassName == null)
//				j2eeVendorAdapterClassName = VendorAdapterJBoss.class.getName();

			if (j2eeDeployBaseDirectory == null)
				j2eeDeployBaseDirectory = "../server/default/deploy/JFire.last/";
		}
	}

	public static class Database implements Serializable
	{
		private String databaseDriverName;
		private String databaseProtocol;
		private String databaseHost;
		private String databasePrefix;
		private String databaseSuffix;
		private String databaseUserName;
		private String databasePassword;
		
		private String databaseCreator;
		
		/**
		 * @return Returns the databaseDriverName.
		 */
		public String getDatabaseDriverName() {
			return databaseDriverName;
		}
		/**
		 * @param databaseDriverName The databaseDriverName to set.
		 */
		public void setDatabaseDriverName(String _databaseDriverName) {
			this.databaseDriverName = _databaseDriverName;
		}
		/**
		 * @return Returns the databaseProtocol.
		 */
		public String getDatabaseProtocol() {
			return databaseProtocol;
		}
		/**
		 * @param databaseProtocol The databaseProtocol to set.
		 */
		public void setDatabaseProtocol(String databaseProtocol) {
			this.databaseProtocol = databaseProtocol;
		}
		/**
		 * @return Returns the databaseHost.
		 */
		public String getDatabaseHost() {
			return databaseHost;
		}
		/**
		 * @param databaseHost The databaseHost to set.
		 */
		public void setDatabaseHost(String databaseHost) {
			this.databaseHost = databaseHost;
		}
		/**
		 * @return Returns the databaseURLPrefix.
		 */
		public String getDatabasePrefix() {
			return databasePrefix;
		}
		/**
		 * @param databaseURLPrefix The databaseURLPrefix to set.
		 */
		public void setDatabasePrefix(String _databasePrefix) {
			this.databasePrefix = _databasePrefix;
		}
		/**
		 * @return Returns the databaseURLSuffix.
		 */
		public String getDatabaseSuffix() {
			return databaseSuffix;
		}
		/**
		 * @param databaseURLSuffix The databaseURLSuffix to set.
		 */
		public void setDatabaseSuffix(String _databaseSuffix) {
			this.databaseSuffix = _databaseSuffix;
		}
		/**
		 * @return Returns the databaseUserName.
		 */
		public String getDatabaseUserName() {
			return databaseUserName;
		}
		/**
		 * @param databaseUserName The databaseUserName to set.
		 */
		public void setDatabaseUserName(String _databaseUserName) {
			this.databaseUserName = _databaseUserName;
		}
		/**
		 * @return Returns the databasePassword.
		 */
		public String getDatabasePassword() {
			return databasePassword;
		}
		/**
		 * @param databasePassword The databasePassword to set.
		 */
		public void setDatabasePassword(String _databasePassword) {
			this.databasePassword = _databasePassword;
		}

		/**
		 * @return Returns the databaseCreator.
		 */
		public String getDatabaseCreator() {
			return databaseCreator;
		}
		/**
		 * @param databaseCreator The databaseCreator to set.
		 */
		public void setDatabaseCreator(String databaseCreator) {
			this.databaseCreator = databaseCreator;
		}

		public void init()
		{
			if (databaseDriverName == null)
				databaseDriverName = "com.mysql.jdbc.Driver";

			if (databaseProtocol == null)
				databaseProtocol = "mysql";

			if (databaseHost == null)
				databaseHost = "localhost";

			if (databasePrefix == null)	
				databasePrefix = "JFire_";

			if (databaseSuffix == null)	
				databaseSuffix = "";

			if (databaseUserName == null)
				databaseUserName = "ipanema";

			if (databasePassword == null)
				databasePassword = "{MyPassword}";
			
			if (databaseCreator == null)
				databaseCreator = DatabaseCreatorMySQL.class.getName();

			LOGGER.info("databaseDriverName = "+databaseDriverName);
			LOGGER.info("databaseProtocol = "+databaseProtocol);
			LOGGER.info("databaseHost = "+databaseHost);
			LOGGER.info("databasePrefix = "+databasePrefix);
			LOGGER.info("databaseSuffix = "+databaseSuffix);
			LOGGER.info("databaseUserName = "+databaseUserName);
			LOGGER.info("databasePassword = "+databasePassword);
			LOGGER.info("databaseCreator = "+databaseCreator);
		}
	}

	public static class JDO implements Serializable
	{
		private String jdoConfigDirectory;
		private String jdoConfigFilePrefix;
		private String jdoConfigFileSuffix;
//		private String jdoPersistenceManagerFactoryJNDIPrefix;
//		private String jdoPersistenceManagerFactoryJNDISuffix;
		private String jdoTemplateDSXMLFile;
		
		/**
		 * @return Returns the jdoConfigDirectory.
		 */
		public String getJdoConfigDirectory() {
			return jdoConfigDirectory;
		}
		/**
		 * @param jdoConfigDirectory The jdoConfigDirectory to set.
		 */
		public void setJdoConfigDirectory(String _jdoConfigDirectory) {
			this.jdoConfigDirectory = _jdoConfigDirectory;
		}
		/**
		 * @return Returns the jdoConfigFilePrefix.
		 */
		public String getJdoConfigFilePrefix() {
			return jdoConfigFilePrefix;
		}
		/**
		 * @param jdoConfigFilePrefix The jdoConfigFilePrefix to set.
		 */
		public void setJdoConfigFilePrefix(String _jdoConfigFilePrefix) {
			this.jdoConfigFilePrefix = _jdoConfigFilePrefix;
		}
		/**
		 * @return Returns the jdoConfigFileSuffix.
		 */
		public String getJdoConfigFileSuffix() {
			return jdoConfigFileSuffix;
		}
		/**
		 * @param jdoConfigFileSuffix The jdoConfigFileSuffix to set.
		 */
		public void setJdoConfigFileSuffix(String _jdoConfigFileSuffix) {
			this.jdoConfigFileSuffix = _jdoConfigFileSuffix;
		}
//		/**
//		 * @return Returns the jdoPersistenceManagerFactoryJNDIPrefix.
//		 */
//		public String getJdoPersistenceManagerFactoryJNDIPrefix() {
//			return jdoPersistenceManagerFactoryJNDIPrefix;
//		}
//		/**
//		 * @param jdoPersistenceManagerFactoryJNDIPrefix The jdoPersistenceManagerFactoryJNDIPrefix to set.
//		 */
//		public void setJdoPersistenceManagerFactoryJNDIPrefix(String _jdoPersistenceManagerJNDIPrefix) {
//			this.jdoPersistenceManagerFactoryJNDIPrefix = _jdoPersistenceManagerJNDIPrefix;
//		}
//		/**
//		 * @return Returns the jdoPersistenceManagerFactoryJNDISuffix.
//		 */
//		public String getJdoPersistenceManagerFactoryJNDISuffix() {
//			return jdoPersistenceManagerFactoryJNDISuffix;
//		}
//		/**
//		 * @param jdoPersistenceManagerFactoryJNDISuffix The jdoPersistenceManagerFactoryJNDISuffix to set.
//		 */
//		public void setJdoPersistenceManagerFactoryJNDISuffix(String _jdoPersistenceManagerJNDISuffix) {
//			this.jdoPersistenceManagerFactoryJNDISuffix = _jdoPersistenceManagerJNDISuffix;
//		}	
		/**
		 * @return Returns the jdoTemplateDSXMLFile.
		 */
		public String getJdoTemplateDSXMLFile() {
			return jdoTemplateDSXMLFile;
		}
		/**
		 * @param jdoTemplateDSXMLFile The jdoTemplateDSXMLFile to set.
		 */
		public void setJdoTemplateDSXMLFile(String jdoTemplateDSXMLFile) {
			this.jdoTemplateDSXMLFile = jdoTemplateDSXMLFile;
		}
		
		public void init()
		{
			if (jdoConfigDirectory == null)
				jdoConfigDirectory = "../server/default/deploy/JFire_JDO.last/";

			if (jdoConfigFilePrefix == null)
				jdoConfigFilePrefix = "ipanema_jpox-";

			if (jdoConfigFileSuffix == null)
				jdoConfigFileSuffix = "-ds.xml";

//			if (jdoPersistenceManagerFactoryJNDIPrefix == null)
//				jdoPersistenceManagerFactoryJNDIPrefix = "ipanema/persistenceManagerFactory/"; // "ipanema/persistenceManagers/";
//
//			if (jdoPersistenceManagerFactoryJNDISuffix == null)
//				jdoPersistenceManagerFactoryJNDISuffix = "";

			if (jdoTemplateDSXMLFile == null)
				jdoTemplateDSXMLFile = "../server/default/deploy/JFire.last/JFireBase.ear/jdo-jpox-ds.template.xml";

			LOGGER.info("jdoConfigDirectory = "+jdoConfigDirectory);
			LOGGER.info("jdoConfigFilePrefix = "+jdoConfigFilePrefix);
			LOGGER.info("jdoConfigFileSuffix = "+jdoConfigFileSuffix);
//			LOGGER.info("jdoPersistenceManagerFactoryJNDIPrefix = "+jdoPersistenceManagerFactoryJNDIPrefix);
//			LOGGER.info("jdoPersistenceManagerFactoryJNDISuffix = "+jdoPersistenceManagerFactoryJNDISuffix);
			LOGGER.info("jdoTemplateDSXMLFile = "+jdoTemplateDSXMLFile);
		}
	}

	private RootOrganisationCf rootOrganisation = null;
	private ServerCf localServer = null;
	private J2ee j2ee;
	private Database database;
	private JDO jdo;

	public RootOrganisationCf getRootOrganisation()
	{
		return rootOrganisation;
	}
	public void setRootOrganisation(RootOrganisationCf rootOrganisation)
	{
		this.rootOrganisation = rootOrganisation;
	}

	/**
	 * @return Returns the localServer.
	 */
	public ServerCf getLocalServer() {
		return localServer;
	}
	/**
	 * @param localServer The localServer to set.
	 */
	public void setLocalServer(ServerCf _localServer) {
		if (_localServer == null)
			throw new NullPointerException("localServer must not be null!");
		this.localServer = _localServer;
	}
	public LocalServer createJDOLocalServer()
	throws ModuleException
	{
		if (localServer == null)
			throw new NullPointerException("localServer is null! Check basic server configuration!");

		Server server = new Server(localServer.getServerID());
		try {
			BeanUtils.copyProperties(server, localServer);
		} catch (IllegalAccessException e) {
			throw new ModuleException(e);
		} catch (InvocationTargetException e) {
			throw new ModuleException(e);
		}
		LocalServer localServer = new LocalServer(server);

		return localServer;
	}

	/**
	 * @return Returns the j2ee.
	 */
	public J2ee getJ2ee() {
		return j2ee;
	}
	/**
	 * @param j2ee The j2ee to set.
	 */
	public void setJ2ee(J2ee j2ee) {
		this.j2ee = j2ee;
	}

	/**
	 * @return Returns the database.
	 */
	public Database getDatabase() {
		return database;
	}
	/**
	 * @param database The database to set.
	 */
	public void setDatabase(Database database) {
		this.database = database;
	}
	/**
	 * @return Returns the jdo.
	 */
	public JDO getJdo() {
		return jdo;
	}
	/**
	 * @param jdo The jdo to set.
	 */
	public void setJdo(JDO jdo) {
		this.jdo = jdo;
	}


	// *********************************************
	// *** Methods from ConfigModule             ***
	// *********************************************	
	/**
	 * @see org.nightlabs.config.Initializable#init()
	 */
	public void init() throws InitException {
		if (rootOrganisation == null) {
			ServerCf server = new ServerCf("ipanema.nightlabs.org");
			server.init();
			server.setServerID("ipanema.nightlabs.org");
			server.setServerName("JFire Devil Server");
			rootOrganisation = new RootOrganisationCf("ipanema.nightlabs.org", "JFire Devil Root Organisation", server);
		} // if (rootOrganisation == null) {

		if (j2ee == null)
			j2ee = new J2ee();

		if (database == null)
			database = new Database();

		if (jdo == null)
			jdo = new JDO();

		j2ee.init();
		database.init();
		jdo.init();

		if (localServer != null)
			localServer.init();
	}

}
