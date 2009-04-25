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

import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.naming.InitialContext;

import org.apache.log4j.Logger;
import org.nightlabs.jfire.base.BaseSessionBeanImplEJB3;
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
@TransactionAttribute(TransactionAttributeType.REQUIRED)
@TransactionManagement(TransactionManagementType.CONTAINER)
@Stateless
public class OrganisationInitDelegateBean
extends BaseSessionBeanImplEJB3
implements OrganisationInitDelegateRemote // , OrganisationInitDelegateLocal
{
	private static final long serialVersionUID = 1L;

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.organisationinit.OrganisationInitDelegateRemote#invokeOrganisationInitInNestedTransaction(org.nightlabs.jfire.organisationinit.OrganisationInit)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	@RolesAllowed("_System_")
	@Override
	public void invokeOrganisationInitInNestedTransaction(OrganisationInit init)
	throws Exception
	{
		Logger logger = Logger.getLogger(OrganisationInitDelegateBean.class);
		InitialContext initCtx = new InitialContext();
		try {
			if (logger.isDebugEnabled())
				logger.debug("Executing OrganisationInit as user " + getPrincipal() +": " + init);
//			Object bean = InvokeUtil.createBean(initCtx, init.getBean());
			Object bean = initCtx.lookup(InvokeUtil.JNDI_PREFIX_EJB_BY_REMOTE_INTERFACE + init.getBean());
			Method beanMethod = bean.getClass().getMethod(init.getMethod(), (Class[]) null);
			beanMethod.invoke(bean, (Object[]) null);
//			InvokeUtil.removeBean(bean);
		} finally {
			initCtx.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.organisationinit.OrganisationInitDelegateRemote#invokeCrossOrganisationRegistrationInitInNestedTransaction(org.nightlabs.jfire.crossorganisationregistrationinit.CrossOrganisationRegistrationInit, org.nightlabs.jfire.crossorganisationregistrationinit.Context)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	@RolesAllowed("_System_")
	@Override
	public void invokeCrossOrganisationRegistrationInitInNestedTransaction(CrossOrganisationRegistrationInit init, Context context)
	throws Exception
	{
		Logger logger = Logger.getLogger(OrganisationInitDelegateBean.class);
		InitialContext initCtx = new InitialContext();
		try {
			if (logger.isDebugEnabled())
				logger.debug("Executing CrossOrganisationRegistrationInit as user " + getPrincipal() +": " + init);
//			Object bean = InvokeUtil.createBean(initCtx, init.getBean());
			Object bean = initCtx.lookup(InvokeUtil.JNDI_PREFIX_EJB_BY_REMOTE_INTERFACE + init.getBean());
			Method beanMethod = bean.getClass().getMethod(init.getMethod(), new Class[] { Context.class });
			beanMethod.invoke(bean, new Object[] { context });
//			InvokeUtil.removeBean(bean);
		} finally {
			initCtx.close();
		}
	}
}
