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

package org.nightlabs.jfire.classloader;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.nightlabs.config.ConfigModule;
import org.nightlabs.config.InitException;
import org.nightlabs.util.Util;

/**
 * @author marco
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public class CLRegistryCfMod extends ConfigModule
{
	/**
	 * The serial version of this class.
	 */
	private static final long serialVersionUID = 1L;
	
	public static class ResourceRepository
	{
		public ResourceRepository() { }
		public ResourceRepository(String _name, String _path, boolean _recursiveSubDirs)
		{
			this.name = _name;
			this.path = _path;
			this.recursiveSubDirs = _recursiveSubDirs;
		}

		private String name;
		private String path;
		private boolean recursiveSubDirs;
		/**
		 * The name specifies a short [variable] name which is used by the client to reference
		 * a serverside resource without the need to know the server's absolute paths.
		 *
		 * @return Returns the name.
		 */
		public String getName()
		{
			return name;
		}
		/**
		 * @param name The name to set.
		 */
		public void setName(String name)
		{
			this.name = name;
		}
		/**
		 * The path specifies an absolute or relative [to the server's working directory - usually "{jboss}/bin"]
		 * path to the published repository.
		 *
		 * @return Returns the path.
		 */
		public String getPath()
		{
			return path;
		}
		/**
		 * @param path The path to set.
		 */
		public void setPath(String path)
		{
			this.path = path;
		}
		/**
		 * @return Returns the recursiveSubDirs.
		 */
		public boolean isRecursiveSubDirs()
		{
			return recursiveSubDirs;
		}
		/**
		 * @param recursiveSubDirs The recursiveSubDirs to set.
		 */
		public void setRecursiveSubDirs(boolean recursiveSubDirs)
		{
			this.recursiveSubDirs = recursiveSubDirs;
		}
	}

	private List<ResourceRepository> resourceRepositories = null;

	private ResourceRepository tempRepository = null;

	/**
	 * @return Returns the resourceRepositories.
	 */
	public List getResourceRepositories()
	{
		return resourceRepositories;
	}
	/**
	 * @param resourceRepositories The resourceRepositories to set.
	 */
	public void setResourceRepositories(List<ResourceRepository> resourceRepositories)
	{
		this.resourceRepositories = resourceRepositories;
	}

	/**
	 * The tempRepository is used for wrapped jars (jars within jars). They are extracted
	 * into this directory and indexed as well.
	 * Important: This directory may be cleaned from time to time. Never store important
	 * data there!
	 * 
	 * @return Returns the tempRepository.
	 */
	public ResourceRepository getTempRepository()
	{
		return tempRepository;
	}
	/**
	 * @param tempRepository The tempRepository to set.
	 */
	public void setTempRepository(ResourceRepository tempRepository)
	{
		this.tempRepository = tempRepository;
	}

	/**
	 * @see org.nightlabs.config.Initializable#init()
	 */
	@Override
	public void init() throws InitException
	{
		if (resourceRepositories == null) {
			resourceRepositories = new ArrayList<ResourceRepository>();
			resourceRepositories.add(
					new ResourceRepository("server.lib", "../server/default/lib", true));
			resourceRepositories.add(
					new ResourceRepository("server.deploy", "../server/default/deploy", false));
			resourceRepositories.add(
					new ResourceRepository("server.deploy.jfire", "../server/default/deploy/JFire.last", true));
		}

		if (tempRepository == null) {
			tempRepository = new ResourceRepository("temp", Util.addFinalSlash(System.getProperty("java.io.tmpdir")) + "jfire" + File.separatorChar + "classloader", true);
		}
	}

}
