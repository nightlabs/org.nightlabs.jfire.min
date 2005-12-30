/*
 * Created on Sep 15, 2005
 */
package org.nightlabs.jfire.jdo.organisationsync;

import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.Timer;

import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.naming.InitialContext;
import javax.naming.NameAlreadyBoundException;
import javax.naming.NamingException;
import javax.transaction.TransactionManager;

import org.apache.log4j.Logger;
import org.nightlabs.jfire.jdo.cache.CacheManagerFactory;
import org.nightlabs.jfire.jdo.cache.LocalDirtyListener;
import org.nightlabs.jfire.jdo.organisationsync.IncomingChangeListenerDescriptor;
import org.nightlabs.jfire.jdo.organisationsync.OutgoingChangeListenerDescriptor;
import org.nightlabs.jfire.jdo.organisationsync.SyncContext;
import org.nightlabs.jfire.jdo.organisationsync.id.SyncContextID;
import org.nightlabs.jfire.servermanager.ra.JFireServerManagerFactoryImpl;

import org.nightlabs.ModuleException;
import org.nightlabs.jdo.ObjectID;
import org.nightlabs.jdo.ObjectIDUtil;

public class OrganisationSyncManagerFactory implements Serializable
{
	public static final Logger LOGGER = Logger
			.getLogger(OrganisationSyncManagerFactory.class);

	public static final String JNDI_PREFIX = "java:/jfire/organisationSyncManagerFactory/";

	public static String getJNDIName(String organisationID)
	{
		return JNDI_PREFIX + organisationID;
	}

	public static final OrganisationSyncManagerFactory getOrganisationSyncManagerFactory(
			InitialContext ctx, String organisationID) throws NamingException
	{
		return (OrganisationSyncManagerFactory) ctx
				.lookup(getJNDIName(organisationID));
	}

	private String organisationID;
	private transient TransactionManager transactionManager;
	private PersistenceManagerFactory persistenceManagerFactory;

	private transient DirtyObjectIDBuffer dirtyObjectIDBuffer;
	private transient Timer timerOutgoing = new Timer();
	private transient Timer timerIncoming = new Timer();

	public OrganisationSyncManagerFactory(InitialContext ctx,
			String organisationID, TransactionManager transactionManager, PersistenceManagerFactory pmf)
			throws NamingException, DirtyObjectIDBufferException
	{
		this.organisationID = organisationID;
		this.transactionManager = transactionManager;
		this.persistenceManagerFactory = pmf;

		String dirtyObjectIDBufferClassName = DirtyObjectIDBufferFileSystem.class.getName(); // TODO aus configmodule holen

		try {
			this.dirtyObjectIDBuffer = (DirtyObjectIDBuffer) Class.forName(dirtyObjectIDBufferClassName).newInstance();
		} catch (Exception x) {
			throw new DirtyObjectIDBufferException(x);
		}

		this.dirtyObjectIDBuffer.init(this);

		try {
			ctx.createSubcontext("java:/jfire");
		} catch (NameAlreadyBoundException e) {
			// ignore
		}

		try {
			ctx.createSubcontext("java:/jfire/organisationSyncManagerFactory");
		} catch (NameAlreadyBoundException e) {
			// ignore
		}

		ctx.bind(getJNDIName(organisationID), this);

		CacheManagerFactory.getCacheManagerFactory(ctx, organisationID)
				.addLocalDirtyListener(new LocalDirtyListener() {
					public void notifyDirtyObjectIDs(Collection objectIDs)
					{
						makeDirty(objectIDs);
					}
				});

//		timerOutgoing.schedule(new TimerTask() {
//			public void run()
//			{
//				processDirtyObjectsOutgoing();
//			}
//		}, 0, 15000); // TODO the 15000 should come from a config module
//
//		timerIncoming.schedule(new TimerTask() {
//			public void run()
//			{
//				processDirtyObjectsIncoming();
//			}
//		}, 0, 20000); // TODO the 20000 should come from a config module
	}

	private void writeObject(java.io.ObjectOutputStream out)
	throws IOException
	{
		out.defaultWriteObject();
	}

	private void readObject(java.io.ObjectInputStream in)
	throws IOException, ClassNotFoundException
	{
		throw new UnsupportedOperationException("I don't have any idea what I should do if this happens? Restart new threads? When does this happen? In a cluster maybe? Marco.");
	}

	public String getOrganisationID()
	{
		return organisationID;
	}
	public PersistenceManagerFactory getPersistenceManagerFactory()
	{
		return persistenceManagerFactory;
	}

	private static final String ORGANISATION_SYNC_DELEGATE_CLASS_NAME = OrganisationSyncDelegate.class.getName() + "Impl";

	private OrganisationSyncDelegate organisationSyncDelegate = null;

	/**
	 * This method is called by {@link #timerIncoming} and therefore does not run concurrently. 
	 */
	private void processDirtyObjectsIncoming()
	{
		try {
			ensureOpenPersistenceManagerFactory();

			boolean doCommit = false;
			transactionManager.begin();
			try {
				PersistenceManager pm = persistenceManagerFactory.getPersistenceManager();
				try {
					// key: String context
					// value: SyncContext syncContext
					Map syncContexts = new HashMap();

					Collection listenerDescriptors = IncomingChangeListenerDescriptor.findDirtyListenerDescriptors(pm);
					for (Iterator it = listenerDescriptors.iterator(); it.hasNext(); ) {
						IncomingChangeListenerDescriptor ld = (IncomingChangeListenerDescriptor) it.next();
						String context = ld.getContext();
						SyncContext syncContext = (SyncContext) syncContexts.get(context);
						if (syncContext == null) {
							syncContext = (SyncContext) pm.getObjectById(SyncContextID.create(context));
							syncContexts.put(context, syncContext);
						}
						syncContext.synchronize(ld);

						ld.setDirty(false);
					}
				} finally {
					pm.close();
				}

				doCommit = true;
			} finally {
				if (doCommit)
					transactionManager.commit();
				else
					transactionManager.rollback();
			}
		} catch (Throwable t) {
			LOGGER.error("Processing incoming dirty objects failed!", t);
		}
	}

	private synchronized void ensureOpenPersistenceManagerFactory()
	throws ModuleException
	{
		if (persistenceManagerFactory == null || persistenceManagerFactory.isClosed()) {
			PersistenceManagerFactory pmf = JFireServerManagerFactoryImpl.getPersistenceManagerFactory(organisationID);
			if (pmf == null)
				throw new ModuleException("PersistenceManagerFactory for organisationID=\""+organisationID+"\" could not be re-obtained from JNDI!");

			persistenceManagerFactory = pmf;
		}
	}

	/**
	 * This method is called by {@link #timerOutgoing} and therefore does not run concurrently. 
	 */
	private void processDirtyObjectsOutgoing()
	{
		try {
			ensureOpenPersistenceManagerFactory();

			// 1st step: fetch temporary dirty objectIDs and distribute the change-markers to all interested listeners

			// fetchDirtyObjectIDs() still keeps the objectIDs in case sth. goes wrong - but they're marked for removal
			Set dirtyObjectIDs = dirtyObjectIDBuffer.fetchDirtyObjectIDs();

			boolean doCommit = false;
			transactionManager.begin();
			try {
				PersistenceManager pm = persistenceManagerFactory.getPersistenceManager();
				try {

					for (Iterator itObjectIDs = dirtyObjectIDs.iterator(); itObjectIDs.hasNext(); ) {
						Object objectID = itObjectIDs.next();

						Collection listenerDescriptors = OutgoingChangeListenerDescriptor.findNonDirtyListenerDescriptorsForObjectID(pm, objectID);
						for (Iterator itLD = listenerDescriptors.iterator(); itLD.hasNext(); ) {
							OutgoingChangeListenerDescriptor ld = (OutgoingChangeListenerDescriptor) itLD.next();
							ld.setDirty(true);
						}
					}

				} finally {
					pm.close();
				}

				doCommit = true;
			} finally {
				if (doCommit)
					transactionManager.commit();
				else
					transactionManager.rollback();
			}

			// remove the objectids that have been marked for removal by fetchDirtyObjectIDs() before.
			dirtyObjectIDBuffer.clearFetchedDirtyObjectIDs();


			// create OrganisationSyncDelegateImpl if not yet existing
			if (organisationSyncDelegate == null) {
				organisationSyncDelegate = (OrganisationSyncDelegate) Class.forName(ORGANISATION_SYNC_DELEGATE_CLASS_NAME).newInstance();
				organisationSyncDelegate.init(this);
			}

			// 2nd step: notify all organisations that have dirty listeners
			doCommit = false;
			transactionManager.begin();
			try {
				PersistenceManager pm = persistenceManagerFactory.getPersistenceManager();
				try {

					// organise the listeners by organisation (in order to have only one RMI call per organisation)

					// key: String organisationID
					// value: Collection outgoingChangeListenerDescriptors
					Map listenerDescriptorsByOrganisationID = new HashMap();

					Collection listenerDescriptors = OutgoingChangeListenerDescriptor.findDirtyListenerDescriptors(pm);
					for (Iterator itLD = listenerDescriptors.iterator(); itLD.hasNext(); ) {
						OutgoingChangeListenerDescriptor ld = (OutgoingChangeListenerDescriptor) itLD.next();
						Collection c = (Collection) listenerDescriptorsByOrganisationID.get(ld.getOrganisationID());
						if (c == null) {
							c = new LinkedList();
							listenerDescriptorsByOrganisationID.put(ld.getOrganisationID(), c);
						}
						c.add(ld);
					}

					// iterate the organisations and pass the objectIDs of the modified objects.
					for (Iterator it = listenerDescriptorsByOrganisationID.entrySet().iterator(); it.hasNext(); ) {
						Map.Entry me = (Map.Entry)it.next();
						String organisationID = (String) me.getKey();
						try {
							Collection descriptors = (Collection) me.getValue();
							// organise the objectIDs by context
							Map dirtyObjectIDCarriersByContext = new HashMap();
							for (Iterator itLD = descriptors.iterator(); itLD.hasNext(); ) {
								OutgoingChangeListenerDescriptor ld = (OutgoingChangeListenerDescriptor) itLD.next();
								Object objectID = ObjectIDUtil.createObjectID(ld.getObjectIDClassName(), ld.getObjectIDFieldPart());
								String context = ld.getContext();
								DirtyObjectIDCarrier carrier = (DirtyObjectIDCarrier) dirtyObjectIDCarriersByContext.get(context);
								if (carrier == null) {
									carrier = new DirtyObjectIDCarrier(context);
									dirtyObjectIDCarriersByContext.put(context, carrier);
								}
								carrier.addObjectID(objectID);
							}

							// notify the other organisation (specified by organisationID)
							if (!dirtyObjectIDCarriersByContext.isEmpty()) {
								organisationSyncDelegate.notifyDirtyObjectIDs(
										pm, organisationID, dirtyObjectIDCarriersByContext.values());
							}

							// clear the dirty flag (if we come here, the other organisation was successfully notified)
							for (Iterator itLD = descriptors.iterator(); itLD.hasNext(); ) {
								OutgoingChangeListenerDescriptor ld = (OutgoingChangeListenerDescriptor) itLD.next();
								ld.setDirty(false);
							}
						} catch (Throwable t) {
							LOGGER.error("Notifying organisation \"" + organisationID + "\" failed!", t);
						}
					} // for (Iterator it = listenerDescriptorsByOrganisationID.entrySet().iterator(); it.hasNext(); ) {

				} finally {
					pm.close();
				}

				doCommit = true;
			} finally {
				if (doCommit)
					transactionManager.commit();
				else
					transactionManager.rollback();
			}
		} catch (Throwable t) {
			LOGGER.error("Processing outgoing dirty objects failed!", t);
		}
	}

	/**
	 * This method filters out all jdo object-ids which do not implement
	 * {@link ObjectID} and passes the filtered set to
	 * {@link DirtyObjectIDBuffer#addDirtyObjectIDs(Collection)}.
	 *
	 * @param objectIDs
	 */
	private void makeDirty(Collection objectIDs)
	{
		try {
			// TODO filter objectIDs of classes that have no listeners at all.
			// or maybe it's faster to dump simply all into the DirtyObjectIDBuffer?!

			Set filteredObjectIDs = new HashSet(objectIDs.size());
			for (Iterator it = objectIDs.iterator(); it.hasNext(); ) {
				Object objectID = it.next();
				if (!(objectID instanceof ObjectID))
					LOGGER.warn("Ignoring objectID \"" + objectID + "\" (" + objectID.getClass() + ") because it does not implement " + ObjectID.class +"!");
				else
					filteredObjectIDs.add(objectID);
			}
			dirtyObjectIDBuffer.addDirtyObjectIDs(filteredObjectIDs);
		} catch (DirtyObjectIDBufferException e) {
			throw new RuntimeException(e);
		}
	}
}
