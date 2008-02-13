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
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public class ManagedConnectionImpl
	implements LocalTransaction, ManagedConnection
{
	/**
	 * LOG4J logger used by this class
	 */
	private static final Logger logger = Logger.getLogger(ManagedConnectionImpl.class);
	
	private final PasswordCredential credential;
	
	private final List<JFireServerManagerImpl> handles = new ArrayList<JFireServerManagerImpl>();

	private final Collection<ConnectionEventListener> cels = new ArrayList<ConnectionEventListener>();
	
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
		if(logger.isDebugEnabled())
			logger.debug(this.getClass().getName()+": begin()");
	}

	/**
	 * @see javax.resource.spi.LocalTransaction#commit()
	 */
	public void commit() throws ResourceException {
		if(logger.isDebugEnabled())
			logger.debug(this.getClass().getName()+": commit()");
	}

	/**
	 * @see javax.resource.spi.LocalTransaction#rollback()
	 */
	public void rollback() throws ResourceException {
		if(logger.isDebugEnabled())
			logger.debug(this.getClass().getName()+": rollback()");
	}

	/**
	 * Mutator to add a connection listener
	 * @param cel <description>
	 */
	public void addConnectionEventListener(ConnectionEventListener cel)
	{
		if(logger.isDebugEnabled())
			logger.debug(this.getClass().getName()+": addConnectionEventListener(...)");
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
		if(logger.isDebugEnabled())
			logger.debug(this.getClass().getName()+": removeConnectionEventListener(...)");
		synchronized (cels) {
			cels.remove(cel);
		}
	}

	/**
	 * @see javax.resource.spi.ManagedConnection#associateConnection(java.lang.Object)
	 */
	public void associateConnection(Object c) throws ResourceException {
		if(logger.isDebugEnabled())
			logger.debug(this.getClass().getName()+": associateConnection(...)");
		if (!(c instanceof JFireServerManagerImpl))
			throw new ResourceException("Wrong Connection type! Expected \""+JFireServerManagerImpl.class.getName()+"\" Found \""+(c==null?"null":c.getClass().getName())+"\"");
		((JFireServerManagerImpl)c).setManagedConnection(this);
		handles.add(0, (JFireServerManagerImpl)c);
	}

	/**
	 * Cleanup method
	 * @exception javax.resource.ResourceException <description>
	 */
	public synchronized void cleanup()
	throws ResourceException
	{
		if(logger.isDebugEnabled())
			logger.debug(this.getClass().getName()+": cleanup()");
		for (Iterator<JFireServerManagerImpl> i = handles.iterator(); i.hasNext();)
		{
			i.next().setManagedConnection(null);
		}
		handles.clear();
	}

	/**
	 * @see javax.resource.spi.ManagedConnection#destroy()
	 */
	public void destroy() throws ResourceException {
		if(logger.isDebugEnabled())
			logger.debug(this.getClass().getName()+": destroy()");
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
		if(logger.isDebugEnabled()) {
			logger.debug("***********************************************************");
			logger.debug(this.getClass().getName()+": getConnection(...)");
			logger.debug("subject: " + subject);
			logger.debug("***********************************************************");
		}

//		PasswordCredential pc = getManagedConnectionFactory().getPasswordCredential(subject);
//		if (!credential.equals(pc))
//		{
//			throw new ResourceException("wrong subject");
//		}

		JFireServerManagerImpl o = new JFireServerManagerImpl(this);
		handles.add(0, o);
		return o;
	}


	/**
	 * @see javax.resource.spi.ManagedConnection#getLocalTransaction()
	 */
	public LocalTransaction getLocalTransaction() throws ResourceException {
		if(logger.isDebugEnabled())
			logger.debug(this.getClass().getName()+": getLocalTransaction()");
		return this;
	}

	/**
	 * @see javax.resource.spi.ManagedConnection#getLogWriter()
	 */
	public PrintWriter getLogWriter() throws ResourceException {
		if(logger.isDebugEnabled())
			logger.debug(this.getClass().getName()+": getLogWriter()");
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
		if(logger.isDebugEnabled())
			logger.debug(this.getClass().getName()+": setLogWriter(...)");
	}

	
	protected void notifyClosed(JFireServerManagerImpl handle)
	{
		ConnectionEvent ce = new ConnectionEvent(this, ConnectionEvent.CONNECTION_CLOSED, null);
		ce.setConnectionHandle(handle);
		Collection<ConnectionEventListener> localCels = null;
		synchronized (cels)
		{
			localCels = new ArrayList<ConnectionEventListener>(cels);
		}

		for (Iterator<ConnectionEventListener> i = localCels.iterator(); i.hasNext(); )
		{
			i.next().connectionClosed(ce);

		}
		handles.remove(handle);
	}


	protected void notifyTxBegin(JFireServerManagerImpl handle)
	{
		ConnectionEvent ce = new ConnectionEvent(this, ConnectionEvent.LOCAL_TRANSACTION_STARTED, null);
		ce.setConnectionHandle(handle);
		Collection<ConnectionEventListener> localCels = null;
		synchronized (cels)
		{
			localCels = new ArrayList<ConnectionEventListener>(cels);
		}

		for (Iterator<ConnectionEventListener> i = localCels.iterator(); i.hasNext(); )
		{
			(i.next()).localTransactionStarted(ce);

		}
	}

	void notifyTxCommit(JFireServerManagerImpl handle)
	{
		ConnectionEvent ce = new ConnectionEvent(this, ConnectionEvent.LOCAL_TRANSACTION_COMMITTED, null);
		ce.setConnectionHandle(handle);
		Collection<ConnectionEventListener> localCels = null;
		synchronized (cels)
		{
			localCels = new ArrayList<ConnectionEventListener>(cels);
		}
		for (Iterator<ConnectionEventListener> i = localCels.iterator(); i.hasNext(); )
		{
			(i.next()).localTransactionCommitted(ce);

		}
	}

	void notifyTxRollback(JFireServerManagerImpl handle)
	{
		ConnectionEvent ce = new ConnectionEvent(this, ConnectionEvent.LOCAL_TRANSACTION_ROLLEDBACK, null);
		ce.setConnectionHandle(handle);
		Collection<ConnectionEventListener> localCels = null;
		synchronized (cels)
		{
			localCels = new ArrayList<ConnectionEventListener>(cels);
		}

		for (Iterator<ConnectionEventListener> i = localCels.iterator(); i.hasNext(); )
		{
			i.next().localTransactionRolledback(ce);
		}
	}

}
