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

package org.nightlabs.jfire.servermanager.config;

import java.lang.reflect.InvocationTargetException;

import org.apache.commons.beanutils.BeanUtils;
import org.nightlabs.ModuleException;
import org.nightlabs.config.ConfigModule;
import org.nightlabs.config.InitException;
import org.nightlabs.jfire.server.LocalServer;
import org.nightlabs.jfire.server.Server;

/**
 * The server core configuration module.
 * @author marco
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public class JFireServerConfigModule extends ConfigModule
{
	/**
	 * The serial version of this class.
	 */
	private static final long serialVersionUID = 1L;
	
	public static final String ORGANISATION_ID_VAR = "${organisationID}";

	private J2eeCf j2ee = null;
	private SmtpMailServiceCf smtp = null;
	private JDOCf jdo = null;
	private DatabaseCf database = null;
	private RootOrganisationCf rootOrganisation = null;
	private ServerCf localServer = null;
	
	/* (non-Javadoc)
	 * @see org.nightlabs.config.ConfigModule#init()
	 */
	@Override
	public void init() throws InitException 
	{
		if (rootOrganisation == null) {
			ServerCf server = new ServerCf("jfire.nightlabs.org");
			server.init();
			server.setServerID("jfire.nightlabs.org");
			server.setServerName("JFire Devil Server");
			rootOrganisation = new RootOrganisationCf("", "Leave root-organisation-id empty for stand-alone mode or ask your administrator for the correct values.", server);
		} // if (rootOrganisation == null) {
		if (j2ee == null)
			setJ2ee(new J2eeCf());
		if (smtp == null)
			setSmtp(new SmtpMailServiceCf());
		if (database == null)
			setDatabase(new DatabaseCf());
		if (jdo == null)
			setJdo(new JDOCf());

		if (localServer != null)
			localServer.init();
	}
	
	public RootOrganisationCf getRootOrganisation()
	{
		return rootOrganisation;
	}
	
	public void setRootOrganisation(RootOrganisationCf rootOrganisation)
	{
		this.rootOrganisation = rootOrganisation;
		setChanged();
	}

	/**
	 * @return Returns the localServer.
	 */
	public ServerCf getLocalServer() 
	{
		return localServer;
	}
	
	/**
	 * @param localServer The localServer to set.
	 */
	public void setLocalServer(ServerCf _localServer) 
	{
		if (_localServer == null)
			throw new NullPointerException("localServer must not be null!");
		this.localServer = _localServer;
		setChanged();
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
	 * Get the database.
	 * @return the database
	 */
	public DatabaseCf getDatabase()
	{
		return database;
	}

	/**
	 * Set the database.
	 * @param database the database to set
	 */
	public void setDatabase(DatabaseCf database)
	{
		this.database = database;
		if(database != null) {
			this.database.setParentConfigModule(this);
			this.database.init();
		}
		setChanged();
	}

	/**
	 * Get the j2ee.
	 * @return the j2ee
	 */
	public J2eeCf getJ2ee()
	{
		return j2ee;
	}

	/**
	 * Set the j2ee.
	 * @param j2ee the j2ee to set
	 */
	public void setJ2ee(J2eeCf j2ee)
	{
		this.j2ee = j2ee;
		if(j2ee != null) {
			this.j2ee.setParentConfigModule(this);
			this.j2ee.init();
		}
		setChanged();
	}
	
	/**
	 * Set the smtp.
	 * @param smtp The Smtp to set
	 */
	public void setSmtp(SmtpMailServiceCf smtp)
	{
		this.smtp = smtp;
		if (smtp != null) {
			this.smtp.setParentConfigModule(this);
			this.smtp.init();
		}
		setChanged();
	}
	
	/**
	 * Get the smtp.
	 * @return the smtp
	 */
	public SmtpMailServiceCf getSmtp() {
		return smtp;
	}

	/**
	 * Get the jdo.
	 * @return the jdo
	 */
	public JDOCf getJdo()
	{
		return jdo;
	}

	/**
	 * Set the jdo.
	 * @param jdo the jdo to set
	 */
	public void setJdo(JDOCf jdo)
	{
		this.jdo = jdo;
		if(jdo != null) {
			this.jdo.setParentConfigModule(this);
			this.jdo.init();
		}
		setChanged();
	}
}
