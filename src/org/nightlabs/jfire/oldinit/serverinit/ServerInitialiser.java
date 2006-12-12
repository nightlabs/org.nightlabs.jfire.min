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
package org.nightlabs.jfire.oldinit.serverinit;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import javax.naming.InitialContext;

import org.apache.log4j.Logger;
import org.nightlabs.jfire.serverinit.ServerInitialiserDelegate;
import org.nightlabs.jfire.servermanager.j2ee.J2EEAdapter;
import org.nightlabs.jfire.servermanager.ra.JFireServerManagerFactoryImpl;
import org.nightlabs.jfire.servermanager.ra.ManagedConnectionFactoryImpl;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 */
public class ServerInitialiser
{
	/**
	 * LOG4J logger used by this class
	 */
	private static final Logger logger = Logger.getLogger(ServerInitialiser.class);
	
	private final J2EEAdapter j2eeAdapter;
	private final JFireServerManagerFactoryImpl jFireServerManagerFactory;
	private final ManagedConnectionFactoryImpl managedConnectionFactory;

	public ServerInitialiser(JFireServerManagerFactoryImpl jfsmf, ManagedConnectionFactoryImpl mcf, J2EEAdapter j2eeAdapter)
	{
		this.jFireServerManagerFactory = jfsmf;
		this.managedConnectionFactory = mcf;
		this.j2eeAdapter = j2eeAdapter;
	}

	public void initializeServer(InitialContext ctx)
	throws IOException
	{
		String deployBaseDir = managedConnectionFactory.getConfigModule().getJ2ee().getJ2eeDeployBaseDirectory();
		File deployBaseDirFile = new File(deployBaseDir);

		long startDT = System.currentTimeMillis();
		String[] ears = deployBaseDirFile.list(new JFireServerManagerFactoryImpl.FileFilterEARs());
		List<String> serverInitEARs = new LinkedList<String>();
		for (int i = 0; i < ears.length; i++) {
			String ear = ears[i];
			File serverInitEARDir = new File(deployBaseDirFile, ear);
			File serverInitEARPropertiesFile = new File(serverInitEARDir, "serverinit.properties");
			if (serverInitEARPropertiesFile.exists())
				serverInitEARs.add(ear);
		}
		Collections.sort(serverInitEARs);
		long stopDT = System.currentTimeMillis();
		logger.debug("Searching server init EARs took " + (stopDT - startDT) + " msec. Found: " + serverInitEARs.size());

		loopServerInitEARs: for (String serverInitEAR : serverInitEARs) {
			File serverInitEARDir = new File(deployBaseDirFile, serverInitEAR);
			File serverInitEARPropertiesFile = new File(serverInitEARDir, "serverinit.properties");

			logger.debug("Reading \"serverinit.properties\" file of server init EAR \"" + serverInitEAR + "\"...");
			Properties serverInitEARProperties = new Properties();
			InputStream in = new BufferedInputStream(new FileInputStream(serverInitEARPropertiesFile));
			try {
				serverInitEARProperties.load(in);
			} finally {
				in.close();
			}

			String serverInitializerClassName = (String) serverInitEARProperties.get("serverInitialiser.class");
			if (serverInitializerClassName == null || "".equals(serverInitializerClassName)) {
				logger.error("Server init EAR \"" + serverInitEAR + "\" contains a \"serverinit.properties\" file, but this file misses the property \"serverInitialiser.class\"!");
				continue loopServerInitEARs;
			}

			try {
				Class serverInitializerClass = Class.forName(serverInitializerClassName);
				if (!ServerInitialiserDelegate.class.isAssignableFrom(serverInitializerClass))
					throw new ClassCastException("Class " + serverInitializerClassName + " does not extend " + ServerInitialiserDelegate.class);

				ServerInitialiserDelegate serverInitializer = (ServerInitialiserDelegate) serverInitializerClass.newInstance();
				serverInitializer.setInitialContext(ctx);
				serverInitializer.setJFireServerManagerFactory(jFireServerManagerFactory);
				serverInitializer.setJ2EEVendorAdapter(j2eeAdapter);
				serverInitializer.initialise();
			} catch (Exception x) {
				logger.error("Executing server init EAR \"" + serverInitEAR + "\" failed!", x);
				continue loopServerInitEARs;
			}

		} // loopServerInitEARs: for (String serverInitEAR : serverInitEARs) {
	}

}
