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
import org.nightlabs.config.Config;
import org.nightlabs.config.ConfigException;
import org.nightlabs.jfire.server.data.dir.JFireServerDataDirectory;
import org.nightlabs.jfire.servermanager.config.DatabaseCf;
import org.nightlabs.jfire.servermanager.config.J2eeCf;
import org.nightlabs.jfire.servermanager.config.JFireServerConfigModule;
import org.nightlabs.jfire.servermanager.db.DatabaseAdapter;
import org.nightlabs.jfire.servermanager.db.DatabaseException;

/**
 * @author marco
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public class ManagedConnectionFactoryImpl
	implements ManagedConnectionFactory
{
	/**
	 * The serial version of this class.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * LOG4J logger used by this class
	 */
	public static final Logger logger = Logger.getLogger(ManagedConnectionFactoryImpl.class);

	public ManagedConnectionFactoryImpl()
	{
		if(logger.isDebugEnabled())
			logger.debug(this.getClass().getName() + ": CONSTRUCTOR");
	}

	// *********************************************
	// *** Config variables                      ***
	// *********************************************

//	/**
//	 * Is calculated from the {@link #dataDirectory} by applying {@link IOUtil#replaceTemplateVariables(String, java.util.Map)}
//	 * with the system properties (=&gt; {@link System#getProperties()}).
//	 */
//	private File resolvedDataDirectory = null;
//
//	public File getResolvedDataDirectory() {
//		if (resolvedDataDirectory == null) {
//			if (dataDirectory == null)
//				return null;
//
//			Map<String, String> variables = CollectionUtil.castMap(System.getProperties());
//			String dir = IOUtil.replaceTemplateVariables(dataDirectory, variables);
//			int firstVarIdx = dir.indexOf("${");
//			if (firstVarIdx >= 0) {
//
//
//				throw new IllegalStateException("dataDirectory \"" + dataDirectory + "\" contains at least one unknown variable: " + );
//			}
//
//			resolvedDataDirectory = new File(dir);
//		}
//
//		return resolvedDataDirectory;
//	}
//
//	/**
//	 * Get the raw (including variables!) data directory of JFire.
//	 *
//	 * @return the raw data directory (as configured). This is exactly the value which was passed to
//	 * {@link #setDataDirectory(String)} or <code>null</code>, if that method was not called yet.
//	 */
//	public String getDataDirectory() {
//
//		return dataDirectory;
//	}

	/**
	 * Is calculated from the {@link #resolvedDataDirectory}.
	 */
	private File sysConfigDirectory = null;

  // *********************************************
	// *** Config methods                        ***
	// *********************************************
	/**
	 * @return Returns the sysConfigDirectory.
	 */
	public File getSysConfigDirectory() {
		if (sysConfigDirectory == null) {
			sysConfigDirectory = new File(JFireServerDataDirectory.getJFireServerDataDirFile(), "config");
		}

		return sysConfigDirectory;
	}
//	/**
//	 * @param sysConfigDirectory The sysConfigDirectory to set.
//	 */
//	public void setSysConfigDirectory(String _sysConfigDirectory) {
//		assertConfigurable();
//		this.sysConfigDirectory = _sysConfigDirectory;
//	}

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
		return getConfig()
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
		logger.info(this.getClass().getName()+": createConnectionFactory(ConnectionManager)");
		try {
			freezeConfiguration();
		} catch (ConfigException e) {
			logger.log(Level.FATAL, "Error in configuration!", e);
			throw new ResourceException(e.getMessage());
		}
		return new JFireServerManagerFactoryImpl(this, cm);
	}

	/**
	 * @see javax.resource.spi.ManagedConnectionFactory#createManagedConnection(javax.security.auth.Subject, javax.resource.spi.ConnectionRequestInfo)
	 */
	public ManagedConnection createManagedConnection(Subject subject, ConnectionRequestInfo requestInfo) throws ResourceException {
		if(logger.isDebugEnabled()) {
			logger.debug("***********************************************************");
			logger.debug(this.getClass().getName()+": createManagedConnection(...)");
			logger.debug("subject: "+subject);
			logger.debug("***********************************************************");
		}
		PasswordCredential pc = getPasswordCredential(subject);
		return new ManagedConnectionImpl(pc);
	}

	/**
	 * @see javax.resource.spi.ManagedConnectionFactory#getLogWriter()
	 */
	public PrintWriter getLogWriter() throws ResourceException {
		if(logger.isDebugEnabled())
			logger.debug(this.getClass().getName()+": getLogWriter()");
		return null;
	}

	/**
	 * @see javax.resource.spi.ManagedConnectionFactory#matchManagedConnections(java.util.Set, javax.security.auth.Subject, javax.resource.spi.ConnectionRequestInfo)
	 */
	@SuppressWarnings("unchecked")
	public ManagedConnection matchManagedConnections(Set mcs, Subject subject, ConnectionRequestInfo cri) throws ResourceException {
		if(logger.isDebugEnabled()) {
			logger.debug("***********************************************************");
			logger.debug(this.getClass().getName()+": matchManagedConnections(...)");
			logger.debug("subject: "+subject);
			logger.debug("***********************************************************");
		}

		PasswordCredential pc = getPasswordCredential(subject);
		for (Iterator<?> i = mcs.iterator(); i.hasNext();)
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
		if(logger.isDebugEnabled())
			logger.debug(this.getClass().getName()+": setLogWriter(pw): pw="+pw);
	}

	// *** helpers
	protected PasswordCredential getPasswordCredential(Subject subject)
	throws ResourceException
	{
		if(logger.isDebugEnabled())
			logger.debug(this.getClass().getName()+": getPasswordCredential(subject=\""+subject+"\")");

		if (subject == null)
		{
			PasswordCredential pc=new PasswordCredential(getDatabaseUserName(), getDatabasePassword().toCharArray());
			pc.setManagedConnectionFactory(this);
			return pc;
		}

		for (Iterator<?> i=subject.getPrivateCredentials().iterator();i.hasNext();)
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

	/**
	 * FIXME Remove the use of deprecated members.
	 */
	protected void freezeConfiguration()
		throws ConfigException
	{
		if (!configurable) {
			logger.warn("freezeConfiguration is called twice!");
			logger.warn("STACKTRACE:", new Exception());
			return;
		}

		File fileSysConfDir = getSysConfigDirectory();
		fileSysConfDir.mkdirs();

//		String sysConfigDirectoryAbsolute;
//		try {
//			sysConfigDirectoryAbsolute = fileSysConfDir.getCanonicalPath();
//		} catch (Exception x) {
//			throw new ConfigException("fileSysConfDir.getCanonicalPath() for \""+sysConfigDirectory+"\" failed!", x);
//		}

		if (!fileSysConfDir.exists())
			throw new ConfigException("SysConfigDirectory \""+sysConfigDirectory.getAbsolutePath()+"\" does not exist!");

		if (!fileSysConfDir.canRead())
			throw new ConfigException("SysConfigDirectory \""+sysConfigDirectory.getAbsolutePath()+"\" is not readable!");

		if (!fileSysConfDir.canWrite())
			throw new ConfigException("SysConfigDirectory \""+sysConfigDirectory.getAbsolutePath()+"\" is not writeable!");

		config = new Config(new File(sysConfigDirectory, "Config.xml"));

		configurable = false;

		Runtime.getRuntime().addShutdownHook(
			new Thread() {
				@Override
				public void run()
				{
					try {
						config.save();
					} catch (Throwable e) {
						logger.error("freezeConfiguration: ShutdownHook: Saving config failed!", e);
					}
				}
			}
		);

		JFireServerConfigModule cfMod = config.createConfigModule(JFireServerConfigModule.class);

		config.save();

		try {
			testConfiguration(cfMod);
		} catch (ConfigException x) {
			logger.fatal("Configuration invalid!", x);
		}
	}

	public void testConfiguration(JFireServerConfigModule cfMod)
		throws ConfigException
	{
		// Test j2ee settings.
		J2eeCf j2eeCf = cfMod.getJ2ee();

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
//			if (!J2EEAdapter.class.isAssignableFrom(j2eeVendorAdapterClass))
//				throw new ClassCastException("J2eeVendorAdapterClass does not implement interface \""+J2EEAdapter.class.getName()+"\"!");
//		} catch (Exception x) {
//			throw new ConfigException("J2eeVendorAdapter \""+j2eeVendorAdapterClassName+"\" is not correct!", x);
//		}


		// Test connection to sql server.
		DatabaseCf dbCf = cfMod.getDatabase();
		try {
			Class.forName(dbCf.getDatabaseDriverName_noTx());
		} catch (ClassNotFoundException e) {
			throw new ConfigException("Database driver class (no-tx) \""+dbCf.getDatabaseDriverName_noTx()+"\" could not be found!", e);
		}
		try {
			Class.forName(dbCf.getDatabaseDriverName_localTx());
		} catch (ClassNotFoundException e) {
			throw new ConfigException("Database driver class (local-tx) \""+dbCf.getDatabaseDriverName_localTx()+"\" could not be found!", e);
		}
		try {
			Class.forName(dbCf.getDatabaseDriverName_xa());
		} catch (ClassNotFoundException e) {
			throw new ConfigException("Database driver class (xa) \""+dbCf.getDatabaseDriverName_xa()+"\" could not be found!", e);
		}

		String dbAdapterClassName = dbCf.getDatabaseAdapter();
		DatabaseAdapter databaseAdapter;
		try {
			databaseAdapter = dbCf.instantiateDatabaseAdapter();
		} catch (Exception x) {
			throw new ConfigException("DatabaseAdapter \""+dbAdapterClassName+"\" is not correct!", x);
		}

		try {
			databaseAdapter.test(cfMod);
		} catch (DatabaseException e) {
			logger.error("Connecting to database server failed!", e);
			throw new ConfigException(e);
		}

		try {
			databaseAdapter.close();
		} catch (Exception e) {
			logger.warn("Closing DatabaseAdaptor \""+dbAdapterClassName+"\" failed.", e);
		}
		databaseAdapter = null;

//		File jdoConfigDir = new File(cfMod.getJdo().getJdoConfigDirectory());
//		if (!jdoConfigDir.exists())
//			throw new ConfigException("JDO config directory \""+cfMod.getJdo().getJdoConfigDirectory()+"\" does not exist!");
//
//		if (!jdoConfigDir.isDirectory())
//			throw new ConfigException("JDO config directory \""+cfMod.getJdo().getJdoConfigDirectory()+"\" is not a directory!");
//
//		if (!jdoConfigDir.canRead())
//			throw new ConfigException("JDO config directory \""+cfMod.getJdo().getJdoConfigDirectory()+"\" is not readable!");
//
//		if (!jdoConfigDir.canWrite())
//			throw new ConfigException("JDO config directory \""+cfMod.getJdo().getJdoConfigDirectory()+"\" is not writeable!");

		File jdoDSXMLTemplate = new File(cfMod.getJdo().getJdoDeploymentDescriptorTemplateFile());
		if (!jdoDSXMLTemplate.exists())
			throw new ConfigException("JDO datasource xml template file \""+cfMod.getJdo().getJdoDeploymentDescriptorTemplateFile()+"\" does not exist!");

		if (!jdoDSXMLTemplate.isFile())
			throw new ConfigException("JDO datasource xml template file \""+cfMod.getJdo().getJdoDeploymentDescriptorTemplateFile()+"\" is not a file!");

		if (!jdoDSXMLTemplate.canRead())
			throw new ConfigException("JDO datasource xml template file \""+cfMod.getJdo().getJdoDeploymentDescriptorTemplateFile()+"\" is not readable!");
	}

}
