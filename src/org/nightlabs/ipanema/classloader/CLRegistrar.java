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
 * Created on 30.09.2004
 */
package org.nightlabs.ipanema.classloader;

import java.io.IOException;
import java.util.List;

import org.nightlabs.ipanema.base.JFireBasePrincipal;

import org.nightlabs.ModuleException;

/**
 * @author marco
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
	 */
	public List getResourcesMetaData(String name)
		throws ModuleException
	{
		return clRegistrarFactory.getResourcesMetaData(name);
	}

	public byte[] getResourceBytes(ResourceMetaData rmd)
		throws ModuleException
	{
		return clRegistrarFactory.getResourceBytes(rmd);
	}

	protected synchronized byte[] getResourcesMetaDataMapBytes()
		throws ModuleException
	{
		return clRegistrarFactory.getResourcesMetaDataMapBytes();
	}

	protected synchronized long getResourcesMetaDataMapBytesTimestamp()
		throws ModuleException
	{
		return clRegistrarFactory.getResourcesMetaDataMapBytesTimestamp();
	}
}
