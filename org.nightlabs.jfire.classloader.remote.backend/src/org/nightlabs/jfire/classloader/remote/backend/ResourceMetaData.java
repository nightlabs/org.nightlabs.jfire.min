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

package org.nightlabs.jfire.classloader.remote.backend;

import java.io.Serializable;

/**
 * @author marco schulze - marco at nightlabs dot de
 */
public class ResourceMetaData implements Serializable
{
	private static final long serialVersionUID = 1L;

	public ResourceMetaData(String _repositoryName, String _jar, String _path, long _size, long _timestamp)
	{
		this.repositoryName = _repositoryName;
		this.jar = _jar;
		this.path = _path;
		this.size = _size;
		this.timestamp = _timestamp;
	}

	private String repositoryName;
	private String jar;
	private String path;
	private long size;
	private long timestamp;

	/**
	 * @return Returns the repositoryName.
	 */
	public String getRepositoryName()
	{
		return repositoryName;
	}
	/**
	 * @param repositoryName The repositoryName to set.
	 */
	public void setRepositoryName(String repositoryName)
	{
		this.repositoryName = repositoryName;
	}
	/**
	 * If the file is within a jar, this is the absolute path to the jar, relative to the j2ee deploy
	 * base directory. If the file is not within a jar, this returns null.
	 *
	 * @return Returns the jar.
	 */
	public String getJar()
	{
		return jar;
	}
	/**
	 * @param jar The jar to set.
	 */
	public void setJar(String jarFile)
	{
		this.jar = jarFile;
	}
	/**
	 * Path specifies the relative path of the resource either within the repository
	 * or within the jar.
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
	public void setPath(String fileName)
	{
		this.path = fileName;
	}
	/**
	 * @return Returns the size.
	 */
	public long getSize()
	{
		return size;
	}
	/**
	 * @param size The size to set.
	 */
	public void setSize(long size)
	{
		this.size = size;
	}
	/**
	 * @return Returns the timestamp.
	 */
	public long getTimestamp()
	{
		return timestamp;
	}
	/**
	 * @param timestamp The timestamp to set.
	 */
	public void setTimestamp(long timestamp)
	{
		this.timestamp = timestamp;
	}
}
