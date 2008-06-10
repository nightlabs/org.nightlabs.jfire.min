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

package org.nightlabs.jfire.organisationinit;

import java.lang.reflect.Method;
import java.rmi.RemoteException;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.naming.InitialContext;

import org.apache.log4j.Logger;
import org.nightlabs.jfire.base.BaseSessionBeanImpl;
import org.nightlabs.jfire.base.InvokeUtil;
import org.nightlabs.jfire.crossorganisationregistrationinit.Context;
import org.nightlabs.jfire.crossorganisationregistrationinit.CrossOrganisationRegistrationInit;

/**
 * @ejb.bean name="jfire/ejb/JFireBaseBean/OrganisationInitDelegate"
 *           jndi-name="jfire/ejb/JFireBaseBean/OrganisationInitDelegate"
 *           type="Stateless"
 *           transaction-type="Container"
 *
 * @ejb.util generate="physical"
 * @ejb.transaction type="Required"
 */
public abstract class OrganisationInitDelegateBean
extends BaseSessionBeanImpl
implements SessionBean
{
	private static final long serialVersionUID = 1L;

	/**
	 * @ejb.create-method
	 * @ejb.permission role-name="_Guest_"
	 */
	public void ejbCreate()
	throws CreateException
	{
	}
	/**
	 * @ejb.permission unchecked="true"
	 */
	public void ejbRemove() throws EJBException, RemoteException
	{
	}

	/**
	 * @ejb.interface-method
	 * @ejb.transaction type="RequiresNew"
	 * @ejb.permission role-name="_System_"
	 */
	public void invokeOrganisationInitInNestedTransaction(OrganisationInit init)
	throws Exception
	{
		Logger logger = Logger.getLogger(OrganisationInitDelegateBean.class);
		InitialContext initCtx = new InitialContext();
		try {
			logger.info("Executing OrganisationInit as user " + getPrincipalString() +": " + init);
			Object bean = InvokeUtil.createBean(initCtx, init.getBean());
			Method beanMethod = bean.getClass().getMethod(init.getMethod(), (Class[]) null);
			beanMethod.invoke(bean, (Object[]) null);
			InvokeUtil.removeBean(bean);
		} finally {
			initCtx.close();
		}
	}

	/**
	 * @ejb.interface-method
	 * @ejb.transaction type="RequiresNew"
	 * @ejb.permission role-name="_System_"
	 */
	public void invokeCrossOrganisationRegistrationInitInNestedTransaction(CrossOrganisationRegistrationInit init, Context context)
	throws Exception
	{
		Logger logger = Logger.getLogger(OrganisationInitDelegateBean.class);
		InitialContext initCtx = new InitialContext();
		try {
			logger.info("Executing CrossOrganisationRegistrationInit as user " + getPrincipalString() +": " + init);
			Object bean = InvokeUtil.createBean(initCtx, init.getBean());
			Method beanMethod = bean.getClass().getMethod(init.getMethod(), new Class[] { Context.class });
			beanMethod.invoke(bean, new Object[] { context });
			InvokeUtil.removeBean(bean);
		} finally {
			initCtx.close();
		}
	}
}
