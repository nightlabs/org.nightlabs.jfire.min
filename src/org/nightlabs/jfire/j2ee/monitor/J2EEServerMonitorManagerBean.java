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

package org.nightlabs.jfire.j2ee.monitor;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Queue;
import javax.naming.NamingException;

import org.nightlabs.jfire.base.BaseSessionBeanImpl;
import org.nightlabs.jfire.base.j2ee.JMSMessageDescriptor;
import org.nightlabs.jfire.base.j2ee.JMSMessageDescriptorImpl;
import org.nightlabs.jfire.base.j2ee.JMSMessageRenderer;
import org.nightlabs.jfire.servermanager.j2ee.J2EEAdapter;


/**
 * @ejb.bean name="jfire/ejb/JFireBaseBean/J2EEServerMonitorManager"
 *           jndi-name="jfire/ejb/JFireBaseBean/J2EEServerMonitorManager"
 *           type="Stateless"
 *           transaction-type="Container"
 *
 * @ejb.util generate = "physical"
 */
public abstract class J2EEServerMonitorManagerBean
extends BaseSessionBeanImpl
implements SessionBean
{
//	private static final Logger logger = Logger.getLogger(J2EEServerMonitorManagerBean.class);

	/**
	 * @ejb.create-method  
	 * @ejb.permission role-name="_Guest_"
	 */
	public void ejbCreate()
	throws CreateException
	{
	}
	
	/**
	 * @see javax.ejb.SessionBean#ejbRemove()
	 * 
	 * @ejb.permission unchecked="true"
	 */
	public void ejbRemove() throws EJBException, RemoteException
	{
	}

	/**
	 * Looks up the {@link J2EEAdapter} from JNDI.
	 * 
	 * @return The {@link J2EEAdapter} for the JFire server.
	 * @throws NamingException If lookup fails.
	 */
	protected J2EEAdapter getJ2EEAdapter() throws NamingException {
		return (J2EEAdapter) getInitialContext(getOrganisationID()).lookup(J2EEAdapter.JNDI_NAME);		
	}
	
	/**
	 * Lists the name of all queues in this server.
	 *  
	 * @return The name of all queues in this server.
	 * 
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type="Supports"
	 */
	public Collection<String> listQueues() throws NamingException, JMSException {
		J2EEAdapter adapter = getJ2EEAdapter();
		Collection<Queue> queues = adapter.getServerMonitor().listQueues();
		Collection<String> result = new ArrayList<String>(queues.size());
		for (Queue queue : queues) {
			result.add(queue.getQueueName());
		}
		return result; 
	}

	/**
	 * Lists all Messages in the queue with the given queueName.
	 * 
	 * @param queueName The name of the queue to list messages for.
	 * @return all messages in the queue with the given <code>queueName</code> or <code>null</code> if no queue with this name can be found.
	 * 
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type="Supports"
	 */
	public Collection<JMSMessageDescriptor> listQueueMessages(String queueName) throws NamingException, JMSException {
		J2EEAdapter adapter = getJ2EEAdapter();
		Collection<Queue> queues = adapter.getServerMonitor().listQueues();
		for (Queue queue : queues) {
			if (queue.getQueueName().equals(queueName)) {
				Collection<Message> msgs = adapter.getServerMonitor().listQueueMessages(queue);
				return JMSMessageDescriptorImpl.getMessageDescriptors(msgs, new ArrayList<JMSMessageRenderer>(0));
			}
		}
		return null;
	}

	/**
	 * Returns the count of messages in the queue with the given queueName.
	 *
	 * @param queueName The name of the queue to list Messages for.
	 * @return the count of messages in the queue with the given <code>queueName</code> or -1 if no queue exists with this name.
	 * 
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type="Supports"
	 */
	public int getQueueDepth(String queueName) throws NamingException, JMSException {
		J2EEAdapter adapter = getJ2EEAdapter();
		Collection<Queue> queues = adapter.getServerMonitor().listQueues();
		for (Queue queue : queues) {
			if (queue.getQueueName().equals(queueName)) {
//				Collection<Message> msgs = adapter.getServerMonitor().listQueueMessages(queue);
				return adapter.getServerMonitor().getQueueDepth(queue);
			}
		}
		return -1;
	}
}
