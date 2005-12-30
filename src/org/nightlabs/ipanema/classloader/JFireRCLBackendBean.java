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
import java.rmi.RemoteException;
import java.util.List;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;

import org.apache.log4j.Logger;
import org.nightlabs.ipanema.classloader.ClassLoaderException;
import org.nightlabs.ipanema.classloader.ResourceMetaData;

import org.nightlabs.ipanema.base.BaseSessionBeanImpl;
import org.nightlabs.ipanema.servermanager.JFireServerManager;

/**
 * @ejb.bean name="ipanema/ejb/JFireRCLBackendBean/JFireRCLBackend"
 *	jndi-name="ipanema/ejb/JFireRCLBackendBean/JFireRCLBackend"
 *	type="Stateless"
 *
 * @ejb.util generate = "physical"
 */
public abstract class JFireRCLBackendBean
	extends BaseSessionBeanImpl
	implements SessionBean
{
	public static final Logger LOGGER = Logger.getLogger(JFireRCLBackendBean.class);

	/**
	 * @see org.nightlabs.ipanema.base.BaseSessionBeanImpl#setSessionContext(javax.ejb.SessionContext)
	 */
	public void setSessionContext(SessionContext sessionContext)
			throws EJBException, RemoteException
	{
//		LOGGER.debug(this.getClass().getName() + ".setSessionContext("+sessionContext+")");
		super.setSessionContext(sessionContext);
	}
	/**
	 * @see org.nightlabs.ipanema.base.BaseSessionBeanImpl#unsetSessionContext()
	 */
	public void unsetSessionContext() {
		super.unsetSessionContext();
	}
	/**
	 * @ejb.create-method  
	 * @ejb.permission role-name="_Guest_"
	 */
	public void ejbCreate()
	throws CreateException
	{
		LOGGER.debug(this.getClass().getName() + ".ejbCreate()");
	}
	/**
	 * @see javax.ejb.SessionBean#ejbRemove()
	 * 
	 * @ejb.permission unchecked="true"
	 */
	public void ejbRemove() throws EJBException, RemoteException
	{
		LOGGER.debug(this.getClass().getName() + ".ejbRemove()");
	}

	/**
	 * @see javax.ejb.SessionBean#ejbActivate()
	 */
	public void ejbActivate() throws EJBException, RemoteException
	{
		LOGGER.debug(this.getClass().getName() + ".ejbActivate()");
	}
	/**
	 * @see javax.ejb.SessionBean#ejbPassivate()
	 */
	public void ejbPassivate() throws EJBException, RemoteException
	{
		LOGGER.debug(this.getClass().getName() + ".ejbPassivate()");
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
	 *
	 * @ejb.interface-method
	 * @ejb.transaction type = "Required"
	 * @ejb.permission role-name="_Guest_"
	 */
	public List getResourcesMetaData(String name)
		throws ClassLoaderException
	{
		try {
			JFireServerManager ipanemaServerManager = getJFireServerManager();
			try {
				CLRegistrar clRegistrar = ipanemaServerManager.getCLRegistrar();
				return clRegistrar.getResourcesMetaData(name);
				// eigentlich sollte der CLRegistrar ein echter ResourceAdapter sein und
				// nicht �ber den JFireServerManager verwaltet werden.
			} finally {
				ipanemaServerManager.close();
			}
		} catch (Exception x) {
			LOGGER.error("getResourcesMetaData(\""+name+"\") failed!", x);
			// Because of classloading problems, we must NOT rethrow/encause x - x won't be found in the client!
			// Thus, we throw a special classloader exception.
			throw new ClassLoaderException("JFireRCLBackendBean.getResourcesMetaData(\""+name+"\") failed!" + x.getMessage());
		}
	}

	/**
	 * @param rmd
	 * @return
	 * @throws ModuleException
	 *
	 * @ejb.interface-method
	 * @ejb.transaction type = "Required"
	 * @ejb.permission role-name="_Guest_"
	 */
	public byte[] getResourceBytes(ResourceMetaData rmd)
		throws ClassLoaderException
	{
		try {
			JFireServerManager ipanemaServerManager = getJFireServerManager();
			try {
				CLRegistrar clRegistrar = ipanemaServerManager.getCLRegistrar();
				return clRegistrar.getResourceBytes(rmd);
				// eigentlich sollte der CLRegistrar ein echter ResourceAdapter sein und
				// nicht �ber den JFireServerManager verwaltet werden.
			} finally {
				ipanemaServerManager.close();
			}
		} catch (Exception x) {
			LOGGER.error("getResourceBytes(\""+rmd.getJar()+'!'+rmd.getPath()+"\") failed!", x);
			// Because of classloading problems, we must NOT rethrow/encause x - x won't be found in the client!
			// Thus, we throw a special classloader exception.
			throw new ClassLoaderException("JFireRCLBackendBean.getResourceBytes(\""+rmd.getJar()+'!'+rmd.getPath()+"\") failed!" + x.getMessage());
		}
	}

	/**
	 * @return
	 * @throws ModuleException
	 * 
	 * @ejb.interface-method
	 * @ejb.transaction type = "Required"
	 * @ejb.permission role-name="_Guest_"
	 */
	public byte[] getResourcesMetaDataMapBytes()
		throws ClassLoaderException
	{
		try {
			JFireServerManager ipanemaServerManager = getJFireServerManager();
			try {
				CLRegistrar clRegistrar = ipanemaServerManager.getCLRegistrar();
				return clRegistrar.getResourcesMetaDataMapBytes();
			} finally {
				ipanemaServerManager.close();
			}
		} catch (Exception x) {
			LOGGER.error("getResourcesMetaDataMapBytes() failed!", x);
			// Because of classloading problems, we must NOT rethrow/encause x - x won't be found in the client!
			// Thus, we throw a special classloader exception.
			throw new ClassLoaderException("JFireRCLBackendBean.getResourcesMetaDataMapBytes() failed!" + x.getMessage());
		}
	}

	/**
	 * @return
	 * @throws ModuleException
	 * 
	 * @ejb.interface-method
	 * @ejb.transaction type = "Required"
	 * @ejb.permission role-name="_Guest_"
	 */
	public long getResourcesMetaDataMapBytesTimestamp()
	throws ClassLoaderException
	{
		try {
			JFireServerManager ipanemaServerManager = getJFireServerManager();
			try {
				CLRegistrar clRegistrar = ipanemaServerManager.getCLRegistrar();
				return clRegistrar.getResourcesMetaDataMapBytesTimestamp();
			} finally {
				ipanemaServerManager.close();
			}
		} catch (Exception x) {
			LOGGER.error("getResourcesMetaDataMapBytesTimestamp() failed!", x);
			// Because of classloading problems, we must NOT rethrow/encause x - x won't be found in the client!
			// Thus, we throw a special classloader exception.
			throw new ClassLoaderException("JFireRCLBackendBean.getResourcesMetaDataMapBytesTimestamp() failed!" + x.getMessage());
		}
	}

}
