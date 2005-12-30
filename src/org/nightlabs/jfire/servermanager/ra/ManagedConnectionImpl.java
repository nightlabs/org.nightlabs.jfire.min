/* ************************************************************************** *
 * Copyright (C) 2004 NightLabs GmbH, Marco Schulze                           *
 * All rights reserved.                                                       *
 * http://www.NightLabs.de                                                    *
 *                                                                            *
 * This program and the accompanying materials are free software; you can re- *
 * distribute it and/or modify it under the terms of the GNU General Public   *
 * License as published by the Free Software Foundation; either ver 2 of the  *
 * License, or any later version.                                             *
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
 * In case, you want to use this module or parts of it in a proprietary pro-  *
 * ject, you can purchase it under the NightLabs Commercial License. Please   *
 * contact NightLabs GmbH under info AT nightlabs DOT com for more infos or   *
 * visit http://www.NightLabs.com                                             *
 * ************************************************************************** */

/*
 * Created on 07.06.2004
 */
package org.nightlabs.jfire.servermanager.ra;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.resource.ResourceException;
import javax.resource.spi.ConnectionEvent;
import javax.resource.spi.ConnectionEventListener;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.LocalTransaction;
import javax.resource.spi.ManagedConnection;
import javax.resource.spi.ManagedConnectionMetaData;
import javax.resource.spi.security.PasswordCredential;
import javax.security.auth.Subject;
import javax.transaction.xa.XAResource;

import org.apache.log4j.Logger;

/**
 * @author marco
 */
public class ManagedConnectionImpl
	implements LocalTransaction, ManagedConnection
{
	public static Logger LOGGER = Logger.getLogger(ManagedConnectionImpl.class);
	
	private final PasswordCredential credential;
	
	private final List handles = new ArrayList();

	private final Collection cels = new ArrayList();
	
	public ManagedConnectionImpl(PasswordCredential credential)
	throws ResourceException
	{
		this.credential = credential;
	}

	PasswordCredential getPasswordCredential()
	{
		return credential;
	}

	ManagedConnectionFactoryImpl getManagedConnectionFactory()
	{
		return (ManagedConnectionFactoryImpl)credential.getManagedConnectionFactory();
	}

	/**
	 * @see javax.resource.spi.LocalTransaction#begin()
	 */
	public void begin() throws ResourceException {
		LOGGER.debug(this.getClass().getName()+": begin()");		
	}

	/**
	 * @see javax.resource.spi.LocalTransaction#commit()
	 */
	public void commit() throws ResourceException {
		LOGGER.debug(this.getClass().getName()+": commit()");
	}

	/**
	 * @see javax.resource.spi.LocalTransaction#rollback()
	 */
	public void rollback() throws ResourceException {
		LOGGER.debug(this.getClass().getName()+": rollback()");
	}

	/**
	 * Mutator to add a connection listener
	 * @param cel <description>
	 */
	public void addConnectionEventListener(ConnectionEventListener cel)
	{
		LOGGER.debug(this.getClass().getName()+": addConnectionEventListener(...)");
		synchronized (cels) {
			cels.add(cel);
		}
	}
	
	/**
	 * Mutator to remove a connection listener
	 * @param cel <description>
	 */
	public void removeConnectionEventListener(ConnectionEventListener cel)
	{
		LOGGER.debug(this.getClass().getName()+": removeConnectionEventListener(...)");
		synchronized (cels) {
			cels.remove(cel);
		}
	}

	/**
	 * @see javax.resource.spi.ManagedConnection#associateConnection(java.lang.Object)
	 */
	public void associateConnection(Object c) throws ResourceException {
		LOGGER.debug(this.getClass().getName()+": associateConnection(...)");
		if (!(c instanceof JFireServerManagerImpl)) 
		{
			throw new ResourceException("Wrong Connection type! Expected \""+JFireServerManagerImpl.class.getName()+"\" Found \""+(c==null?"null":c.getClass().getName())+"\"");
		}
		((JFireServerManagerImpl)c).setManagedConnection(this);
		handles.add(0, c);
	}

	/**
	 * Cleanup method
	 * @exception javax.resource.ResourceException <description>
	 */
	public synchronized void cleanup()
	throws ResourceException
	{
		System.out.println(this.getClass().getName()+": cleanup()");
		for (Iterator i = handles.iterator(); i.hasNext();)
		{
			((JFireServerManagerImpl)i.next()).setManagedConnection(null);
		}
		handles.clear();
	}

	/**
	 * @see javax.resource.spi.ManagedConnection#destroy()
	 */
	public void destroy() throws ResourceException {
		System.out.println(this.getClass().getName()+": destroy()");		
	}

	/**
	 * @see javax.resource.spi.ManagedConnection#getConnection(javax.security.auth.Subject, javax.resource.spi.ConnectionRequestInfo)
	 */
	/**
	 * Accessor for the connection
	 * @param subject <description>
	 * @param cri <description>
	 * @return <description>
	 * @exception javax.resource.ResourceException <description>
	 */
	public Object getConnection(Subject subject, ConnectionRequestInfo cri)
	throws ResourceException
	{
		LOGGER.debug("***********************************************************");
		LOGGER.debug(this.getClass().getName()+": getConnection(...)");
		LOGGER.debug("subject: " + subject);
		LOGGER.debug("***********************************************************");

//		PasswordCredential pc = getManagedConnectionFactory().getPasswordCredential(subject);
//		if (!credential.equals(pc))
//		{
//			throw new ResourceException("wrong subject");
//		}

		Object o = new JFireServerManagerImpl(getManagedConnectionFactory(), this);
		handles.add(0, o);
		return o;
	}


	/**
	 * @see javax.resource.spi.ManagedConnection#getLocalTransaction()
	 */
	public LocalTransaction getLocalTransaction() throws ResourceException {
		LOGGER.debug(this.getClass().getName()+": getLocalTransaction()");
		return this;
	}

	/**
	 * @see javax.resource.spi.ManagedConnection#getLogWriter()
	 */
	public PrintWriter getLogWriter() throws ResourceException {
		LOGGER.debug(this.getClass().getName()+": getLogWriter()");
		return null;
	}

	/**
	 * @see javax.resource.spi.ManagedConnection#getMetaData()
	 */
	public ManagedConnectionMetaData getMetaData() throws ResourceException {
		throw new ResourceException("NYI");
	}

	/**
	 * @see javax.resource.spi.ManagedConnection#getXAResource()
	 */
	public XAResource getXAResource() throws ResourceException {
		throw new ResourceException("NYI");
	}

	/**
	 * @see javax.resource.spi.ManagedConnection#setLogWriter(java.io.PrintWriter)
	 */
	public void setLogWriter(PrintWriter lw) throws ResourceException {
		LOGGER.debug(this.getClass().getName()+": setLogWriter(...)");		
	}

	
	protected void notifyClosed(JFireServerManagerImpl handle)
	{
		ConnectionEvent ce = new ConnectionEvent(this, ConnectionEvent.CONNECTION_CLOSED, null);
		ce.setConnectionHandle(handle);
		Collection localCels = null;
		synchronized (cels)
		{
			localCels = new ArrayList(cels);
		}

		for (Iterator i = localCels.iterator(); i.hasNext(); )
		{
			((ConnectionEventListener)i.next()).connectionClosed(ce);

		}
		handles.remove(handle);
	}	


	protected void notifyTxBegin(JFireServerManagerImpl handle)
	{
		ConnectionEvent ce = new ConnectionEvent(this, ConnectionEvent.LOCAL_TRANSACTION_STARTED, null);
		ce.setConnectionHandle(handle);
		Collection localCels = null;
		synchronized (cels)
		{
			localCels = new ArrayList(cels);
		}

		for (Iterator i = localCels.iterator(); i.hasNext(); )
		{
			((ConnectionEventListener)i.next()).localTransactionStarted(ce);

		}
	}

	void notifyTxCommit(JFireServerManagerImpl handle)
	{
		ConnectionEvent ce = new ConnectionEvent(this, ConnectionEvent.LOCAL_TRANSACTION_COMMITTED, null);
		ce.setConnectionHandle(handle);
		Collection localCels = null;
		synchronized (cels)
		{
			localCels = new ArrayList(cels);
		}
		for (Iterator i = localCels.iterator(); i.hasNext(); )
		{
			((ConnectionEventListener)i.next()).localTransactionCommitted(ce);

		}
	}

	void notifyTxRollback(JFireServerManagerImpl handle)
	{
		ConnectionEvent ce = new ConnectionEvent(this, ConnectionEvent.LOCAL_TRANSACTION_ROLLEDBACK, null);
		ce.setConnectionHandle(handle);
		Collection localCels = null;
		synchronized (cels)
		{
			localCels = new ArrayList(cels);
		}

		for (Iterator i = localCels.iterator(); i.hasNext(); )
		{
			((ConnectionEventListener)i.next()).localTransactionRolledback(ce);
		}
	}

}
