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

import java.io.IOException;
import java.util.List;

import org.nightlabs.jfire.base.JFireBasePrincipal;
import org.nightlabs.jfire.classloader.remote.backend.ResourceMetaData;
import org.nightlabs.xml.XMLReadException;

/**
 * @author marco schulze - marco at nightlabs dot de
 */
public class CLRegistrar
{
	protected CLRegistrarFactory clRegistrarFactory;
	protected JFireBasePrincipal principal;
	
	protected CLRegistrar(CLRegistrarFactory _clRegistrarFactory, JFireBasePrincipal _principal)
	{
		this.clRegistrarFactory = _clRegistrarFactory;
		this.principal = _principal;
	}

	/**
	 * This method returns a List of ResourceMetaData for all resources that match
	 * the given name. There may be many, because the j2ee server has multiple
	 * repositories (at least lib and deploy) and even within one repository, there may
	 * be multiple jars providing the same resource.
	 *
	 * @param name
	 * @return
	 * @throws IOException 
	 * @throws XMLReadException 
	 * @throws IOException
	 */
	public List<ResourceMetaData> getResourcesMetaData(String name) throws XMLReadException, IOException
	{
		return clRegistrarFactory.getResourcesMetaData(name);
	}

	public byte[] getResourceBytes(ResourceMetaData rmd) throws IOException, XMLReadException
	{
		return clRegistrarFactory.getResourceBytes(rmd);
	}

	public synchronized byte[] getResourcesMetaDataMapBytes() throws XMLReadException, IOException
	{
		return clRegistrarFactory.getResourcesMetaDataMapBytes();
	}

	public synchronized long getResourcesMetaDataMapBytesTimestamp() throws XMLReadException, IOException
	{
		return clRegistrarFactory.getResourcesMetaDataMapBytesTimestamp();
	}
}
