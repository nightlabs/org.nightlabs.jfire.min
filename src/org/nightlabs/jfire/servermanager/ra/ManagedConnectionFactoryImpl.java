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

package org.nightlabs.jfire.servermanager.ra;

import java.io.File;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Set;

import javax.resource.ResourceException;
import javax.resource.spi.ConnectionManager;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.ManagedConnection;
import javax.resource.spi.ManagedConnectionFactory;
import javax.resource.spi.security.PasswordCredential;
import javax.security.auth.Subject;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.nightlabs.jfire.servermanager.config.JFireServerConfigModule;
import org.nightlabs.jfire.servermanager.dbcreate.DatabaseCreator;

import org.nightlabs.config.Config;
import org.nightlabs.config.ConfigException;

/**
 * @author marco
 */
public class ManagedConnectionFactoryImpl
	implements ManagedConnectionFactory
{
	public static Logger LOGGER = Logger.getLogger(ManagedConnectionFactoryImpl.class);

	public ManagedConnectionFactoryImpl()
	{
		LOGGER.debug(this.getClass().getName() + ": CONSTRUCTOR");
	}
	
	// *********************************************
	// *** Config variables                      ***
	// *********************************************
	private String sysConfigDirectory = null;

  // *********************************************
	// *** Config methods                        ***
	// *********************************************
	/**
	 * @return Returns the sysConfigDirectory.
	 */
	public String getSysConfigDirectory() {
		return sysConfigDirectory;
	}
	/**
	 * @param sysConfigDirectory The sysConfigDirectory to set.
	 */
	public void setSysConfigDirectory(String _sysConfigDirectory) {
		assertConfigurable();
		this.sysConfigDirectory = _sysConfigDirectory;
	}
	
	// *********************************************
	// *** Methods for j2ee requirements         ***
	// *********************************************
	/**
	 * @return Returns the databaseUserName.
	 */
	public String getDatabaseUserName() {
		return getConfigModule().getDatabase().getDatabaseUserName();
	}
	/**
	 * @return Returns the databasePassword.
	 */
	public String getDatabasePassword() {
		return getConfigModule().getDatabase().getDatabasePassword();
	}


	// *********************************************
	// *** Runtime variables                     ***
	// *********************************************
	private Config config = null;
	
	// *********************************************
	// *** Runtime methods                       ***
	// *********************************************
	public Config getConfig()
	{
		if (config ==  null)
			throw new IllegalStateException("freezeConfiguration() has not been called! Config not existent!");
		
		return config;
	}
	
	public JFireServerConfigModule getConfigModule()
	{
		return (JFireServerConfigModule)getConfig()
				.getConfigModule(JFireServerConfigModule.class, true);
	}

	
	// *********************************************
	// *** Methods from ManagedConnectionFactory ***
	// *********************************************

	/**
	 * @see javax.resource.spi.ManagedConnectionFactory#createConnectionFactory()
	 */
	public Object createConnectionFactory() throws ResourceException {
		throw new ResourceException("NYI");
	}

	/**
	 * @see javax.resource.spi.ManagedConnectionFactory#createConnectionFactory(javax.resource.spi.ConnectionManager)
	 */
	public Object createConnectionFactory(ConnectionManager cm) throws ResourceException {
		LOGGER.info(this.getClass().getName()+": createConnectionFactory(ConnectionManager)");
		try {
			freezeConfiguration();
		} catch (ConfigException e) {
			LOGGER.log(Level.FATAL, "Error in configuration!", e);
			throw new ResourceException(e.getMessage());
		}
		return new JFireServerManagerFactoryImpl(this, cm);
	}

	/**
	 * @see javax.resource.spi.ManagedConnectionFactory#createManagedConnection(javax.security.auth.Subject, javax.resource.spi.ConnectionRequestInfo)
	 */
	public ManagedConnection createManagedConnection(Subject subject, ConnectionRequestInfo requestInfo) throws ResourceException {
		LOGGER.debug("***********************************************************");
		LOGGER.debug(this.getClass().getName()+": createManagedConnection(...)");
		LOGGER.debug("subject: "+subject);
		LOGGER.debug("***********************************************************");
		PasswordCredential pc = getPasswordCredential(subject);
		return new ManagedConnectionImpl(pc);
	}

	/**
	 * @see javax.resource.spi.ManagedConnectionFactory#getLogWriter()
	 */
	public PrintWriter getLogWriter() throws ResourceException {
		LOGGER.debug(this.getClass().getName()+": getLogWriter()");
		return null;
	}

	/**
	 * @see javax.resource.spi.ManagedConnectionFactory#matchManagedConnections(java.util.Set, javax.security.auth.Subject, javax.resource.spi.ConnectionRequestInfo)
	 */
	public ManagedConnection matchManagedConnections(Set mcs, Subject subject, ConnectionRequestInfo cri) throws ResourceException {
		LOGGER.debug("***********************************************************");
		LOGGER.debug(this.getClass().getName()+": matchManagedConnections(...)");
		LOGGER.debug("subject: "+subject);
		LOGGER.debug("***********************************************************");
		
		PasswordCredential pc = getPasswordCredential(subject);
		for (Iterator i = mcs.iterator(); i.hasNext();)
		{
			Object o = i.next();
			if (!(o instanceof ManagedConnectionImpl))
			{
				continue;
			}

			ManagedConnectionImpl mc = (ManagedConnectionImpl)o;
			if (!(mc.getManagedConnectionFactory().equals(this)))
			{
				continue;
			}
			if (pc.equals(mc.getPasswordCredential())) 
			{
				return mc;
			}
		}
		return null;
	}

	/**
	 * @see javax.resource.spi.ManagedConnectionFactory#setLogWriter(java.io.PrintWriter)
	 */
	public void setLogWriter(PrintWriter pw) throws ResourceException {
		LOGGER.debug(this.getClass().getName()+": setLogWriter(pw): pw="+pw);
	}
	
	// *** helpers
	protected PasswordCredential getPasswordCredential(Subject subject) 
	throws ResourceException
	{
		System.out.println(this.getClass().getName()+": getPasswordCredential(subject=\""+subject+"\")");
		
		if (subject == null) 
		{
			PasswordCredential pc=new PasswordCredential(getDatabaseUserName(), getDatabasePassword().toCharArray());
			pc.setManagedConnectionFactory(this);
			return pc;
		}

		for (Iterator i=subject.getPrivateCredentials().iterator();i.hasNext();)
		{
			Object o = i.next();
			if (o instanceof PasswordCredential) 
			{
				PasswordCredential pc = (PasswordCredential)o;
				if (this.equals(pc.getManagedConnectionFactory())) 
				{
					return pc;
				}
			}
		}
		throw new ResourceException("No credentials found for ManagedConnectionFactory: " + this);
	}
	
	// *********************************************
	// *** protected internal methods            ***
	// *********************************************
	
	private boolean configurable = true;
	public boolean isConfigurable()
	{
		return configurable;
	}
	
	protected void assertConfigurable()
	{
		if (!configurable)
			throw new IllegalStateException("This instance of ManagedConnectionFactoryImpl cannot be configured anymore! Configuration already frozen.");
	}

	protected void freezeConfiguration()
		throws ConfigException
	{
		if (!configurable) {
			LOGGER.log(Level.WARN, "freezeConfiguration is called twice!");
			LOGGER.log(Level.WARN, "STACKTRACE:", new Exception());
			return;
		}

		if (sysConfigDirectory == null)
			throw new ConfigException("SysConfigDirectory is not configured! Check your JFireServerManager-ds.xml!");
		
		File fileSysConfDir = new File(sysConfigDirectory);
		
		String sysConfigDirectoryAbsolute;
		try {
			sysConfigDirectoryAbsolute = fileSysConfDir.getCanonicalPath();
		} catch (Exception x) {
			throw new ConfigException("fileSysConfDir.getCanonicalPath() for \""+sysConfigDirectory+"\" failed!", x);
		}

		if (!fileSysConfDir.exists())
			throw new ConfigException("SysConfigDirectory \""+sysConfigDirectoryAbsolute+"\" defined in JFireServerManager-ds.xml does not exist!");
		
		if (!fileSysConfDir.canRead())
			throw new ConfigException("SysConfigDirectory \""+sysConfigDirectoryAbsolute+"\" defined in JFireServerManager-ds.xml is not readable!");
		
		if (!fileSysConfDir.canWrite())
			throw new ConfigException("SysConfigDirectory \""+sysConfigDirectoryAbsolute+"\" defined in JFireServerManager-ds.xml is not writeable!");

		config = new Config("Config.xml", true, sysConfigDirectory);

		configurable = false;

//		Runtime.getRuntime().addShutdownHook(
//			new Thread() {
//				public void run()
//				{
//					try {
//						config.saveConfFile();
//					} catch (Throwable e) {
//						LOGGER.log(Level.FATAL, "Saving config failed!", e);
//					}
//				}
//			}
//		);
		
		JFireServerConfigModule cfMod = (JFireServerConfigModule)
			config.createConfigModule(JFireServerConfigModule.class);

		config.saveConfFile();

		try {
			testConfiguration(cfMod);
		} catch (ConfigException x) {
			LOGGER.fatal("Configuration invalid!", x);
		}
	}

	public void testConfiguration(JFireServerConfigModule cfMod)
		throws ConfigException
	{
		// Test j2ee settings.
		JFireServerConfigModule.J2ee j2eeCf = cfMod.getJ2ee();

		File j2eeDeployBaseDir = new File(j2eeCf.getJ2eeDeployBaseDirectory());
		if (!j2eeDeployBaseDir.exists())
			throw new ConfigException("j2ee deploy base directory \""+j2eeCf.getJ2eeDeployBaseDirectory()+"\" does not exist!");
		
		if (!j2eeDeployBaseDir.isDirectory())
			throw new ConfigException("j2ee deploy base directory \""+j2eeCf.getJ2eeDeployBaseDirectory()+"\" is not a directory!");

		if (!j2eeDeployBaseDir.canRead())
			throw new ConfigException("j2ee deploy base directory \""+j2eeCf.getJ2eeDeployBaseDirectory()+"\" is not readable!");

		if (!j2eeDeployBaseDir.canWrite())
			throw new ConfigException("j2ee deploy base directory \""+j2eeCf.getJ2eeDeployBaseDirectory()+"\" is not writeable!");


//		String j2eeVendorAdapterClassName = j2eeCf.getJ2eeVendorAdapterClassName();
//		try {
//			Class j2eeVendorAdapterClass = Class.forName(j2eeVendorAdapterClassName);
//			if (!VendorAdapter.class.isAssignableFrom(j2eeVendorAdapterClass))
//				throw new ClassCastException("J2eeVendorAdapterClass does not implement interface \""+VendorAdapter.class.getName()+"\"!");
//		} catch (Exception x) {
//			throw new ConfigException("J2eeVendorAdapter \""+j2eeVendorAdapterClassName+"\" is not correct!", x);
//		}


		// Test connection to sql server.
		JFireServerConfigModule.Database dbCf = cfMod.getDatabase();
		try {
			Class.forName(dbCf.getDatabaseDriverName());
		} catch (ClassNotFoundException e) {
			throw new ConfigException("Database driver class \""+dbCf.getDatabaseDriverName()+"\" could not be found!", e);
		}

		try {
			Connection sqlConn = DriverManager.getConnection(
			    "jdbc:"+dbCf.getDatabaseProtocol()+"://"+dbCf.getDatabaseHost()+"/",
			    dbCf.getDatabaseUserName(),
			    dbCf.getDatabasePassword()
			  );			
			sqlConn.close();
		} catch (SQLException e) {
			throw new ConfigException("Connecting to database server failed!", e);
		}

		String dbCreatorClassName = dbCf.getDatabaseCreator();
		try {
			Class dbCreatorClass = Class.forName(dbCreatorClassName);
			if (!DatabaseCreator.class.isAssignableFrom(dbCreatorClass))
				throw new ClassCastException("DatabaseCreatorClass does not implement interface \""+DatabaseCreator.class.getName()+"\"!");
		} catch (Exception x) {
			throw new ConfigException("DatabaseCreator \""+dbCreatorClassName+"\" is not correct!", x);
		}

		File jdoConfigDir = new File(cfMod.getJdo().getJdoConfigDirectory());
		if (!jdoConfigDir.exists())
			throw new ConfigException("JDO config directory \""+cfMod.getJdo().getJdoConfigDirectory()+"\" does not exist!");
		
		if (!jdoConfigDir.isDirectory())
			throw new ConfigException("JDO config directory \""+cfMod.getJdo().getJdoConfigDirectory()+"\" is not a directory!");

		if (!jdoConfigDir.canRead())
			throw new ConfigException("JDO config directory \""+cfMod.getJdo().getJdoConfigDirectory()+"\" is not readable!");

		if (!jdoConfigDir.canWrite())
			throw new ConfigException("JDO config directory \""+cfMod.getJdo().getJdoConfigDirectory()+"\" is not writeable!");
		
		File jdoDSXMLTemplate = new File(cfMod.getJdo().getJdoTemplateDSXMLFile());
		if (!jdoDSXMLTemplate.exists())
			throw new ConfigException("JDO datasource xml template file \""+cfMod.getJdo().getJdoTemplateDSXMLFile()+"\" does not exist!");

		if (!jdoDSXMLTemplate.isFile())
			throw new ConfigException("JDO datasource xml template file \""+cfMod.getJdo().getJdoTemplateDSXMLFile()+"\" is not a file!");
		
		if (!jdoDSXMLTemplate.canRead())
			throw new ConfigException("JDO datasource xml template file \""+cfMod.getJdo().getJdoTemplateDSXMLFile()+"\" is not readable!");
	}

}
