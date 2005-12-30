/* ************************************************************************** *
 * Copyright (C) 2004 NightLabs GmbH, Marco Schulze                           *
 * All rights reserved.                                                       *
 * http://www.NightLabs.de                                                    *
 *                                                                            *
 * This program and the accompanying materials are free software; you can re- *
 * distribute it and/or modify it under the terms of the GNU Lesser General   *
 * Public License as published by the Free Software Foundation; either ver 2  *
 * of the License, or any later version.                                      *
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
 * In case, you want to use this module or parts of it in a commercial way    * 
 * that is not allowed by the GPL, pleas contact us and we will provide a     *
 * commercial licence.                                                        *
 * ************************************************************************** */

/*
 * Created on 04.10.2004
 */
package org.nightlabs.ipanema.classloader;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.nightlabs.config.ConfigModule;
import org.nightlabs.config.InitException;
import org.nightlabs.util.Utils;

/**
 * @author marco
 */
public class CLRegistryCfMod extends ConfigModule
{
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

	private List resourceRepositories = null;

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
	public void setResourceRepositories(List resourceRepositories)
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
	public void init() throws InitException
	{
		if (resourceRepositories == null) {
			resourceRepositories = new ArrayList();
			resourceRepositories.add(
					new ResourceRepository("server.lib", "../server/default/lib", true));
			resourceRepositories.add(
					new ResourceRepository("server.deploy", "../server/default/deploy", false));
			resourceRepositories.add(
					new ResourceRepository("server.deploy.ipanema", "../server/default/deploy/JFire.last", true));
		}

		if (tempRepository == null) {
			tempRepository = new ResourceRepository("temp", Utils.addFinalSlash(System.getProperty("java.io.tmpdir")) + "ipanema" + File.separatorChar + "classloader", true);
		}
	}

}
