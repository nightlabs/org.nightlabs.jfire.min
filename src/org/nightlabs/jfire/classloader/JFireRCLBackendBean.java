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
import java.rmi.RemoteException;
import java.util.List;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;

import org.apache.log4j.Logger;
import org.nightlabs.jfire.classloader.ClassLoaderException;
import org.nightlabs.jfire.classloader.ResourceMetaData;

import org.nightlabs.jfire.base.BaseSessionBeanImpl;
import org.nightlabs.jfire.servermanager.JFireServerManager;

/**
 * @ejb.bean name="jfire/ejb/JFireRCLBackendBean/JFireRCLBackend"
 *	jndi-name="jfire/ejb/JFireRCLBackendBean/JFireRCLBackend"
 *	type="Stateless"
 *
 * @ejb.util generate = "physical"
 */
public abstract class JFireRCLBackendBean
	extends BaseSessionBeanImpl
	implements SessionBean
{
	/**
	 * LOG4J logger used by this class
	 */
	private static final Logger logger = Logger.getLogger(JFireRCLBackendBean.class);

	/**
	 * @see org.nightlabs.jfire.base.BaseSessionBeanImpl#setSessionContext(javax.ejb.SessionContext)
	 */
	public void setSessionContext(SessionContext sessionContext)
			throws EJBException, RemoteException
	{
//		LOGGER.debug(this.getClass().getName() + ".setSessionContext("+sessionContext+")");
		super.setSessionContext(sessionContext);
	}
	/**
	 * @see org.nightlabs.jfire.base.BaseSessionBeanImpl#unsetSessionContext()
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
		logger.debug(this.getClass().getName() + ".ejbCreate()");
	}
	/**
	 * @see javax.ejb.SessionBean#ejbRemove()
	 * 
	 * @ejb.permission unchecked="true"
	 */
	public void ejbRemove() throws EJBException, RemoteException
	{
		logger.debug(this.getClass().getName() + ".ejbRemove()");
	}

	/**
	 * @see javax.ejb.SessionBean#ejbActivate()
	 */
	public void ejbActivate() throws EJBException, RemoteException
	{
		logger.debug(this.getClass().getName() + ".ejbActivate()");
	}
	/**
	 * @see javax.ejb.SessionBean#ejbPassivate()
	 */
	public void ejbPassivate() throws EJBException, RemoteException
	{
		logger.debug(this.getClass().getName() + ".ejbPassivate()");
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
			JFireServerManager jfireServerManager = getJFireServerManager();
			try {
				CLRegistrar clRegistrar = jfireServerManager.getCLRegistrar();
				return clRegistrar.getResourcesMetaData(name);
				// eigentlich sollte der CLRegistrar ein echter ResourceAdapter sein und
				// nicht �ber den JFireServerManager verwaltet werden.
			} finally {
				jfireServerManager.close();
			}
		} catch (Exception x) {
			logger.error("getResourcesMetaData(\""+name+"\") failed!", x);
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
			JFireServerManager jfireServerManager = getJFireServerManager();
			try {
				CLRegistrar clRegistrar = jfireServerManager.getCLRegistrar();
				return clRegistrar.getResourceBytes(rmd);
				// eigentlich sollte der CLRegistrar ein echter ResourceAdapter sein und
				// nicht �ber den JFireServerManager verwaltet werden.
			} finally {
				jfireServerManager.close();
			}
		} catch (Exception x) {
			logger.error("getResourceBytes(\""+rmd.getJar()+'!'+rmd.getPath()+"\") failed!", x);
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
			JFireServerManager jfireServerManager = getJFireServerManager();
			try {
				CLRegistrar clRegistrar = jfireServerManager.getCLRegistrar();
				return clRegistrar.getResourcesMetaDataMapBytes();
			} finally {
				jfireServerManager.close();
			}
		} catch (Exception x) {
			logger.error("getResourcesMetaDataMapBytes() failed!", x);
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
			JFireServerManager jfireServerManager = getJFireServerManager();
			try {
				CLRegistrar clRegistrar = jfireServerManager.getCLRegistrar();
				return clRegistrar.getResourcesMetaDataMapBytesTimestamp();
			} finally {
				jfireServerManager.close();
			}
		} catch (Exception x) {
			logger.error("getResourcesMetaDataMapBytesTimestamp() failed!", x);
			// Because of classloading problems, we must NOT rethrow/encause x - x won't be found in the client!
			// Thus, we throw a special classloader exception.
			throw new ClassLoaderException("JFireRCLBackendBean.getResourcesMetaDataMapBytesTimestamp() failed!" + x.getMessage());
		}
	}

}
