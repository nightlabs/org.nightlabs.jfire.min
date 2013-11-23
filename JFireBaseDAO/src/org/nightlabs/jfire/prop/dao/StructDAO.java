package org.nightlabs.jfire.prop.dao;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import javax.jdo.FetchPlan;

import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.base.jdo.BaseJDOObjectDAO;
import org.nightlabs.jfire.prop.PropertyManagerRemote;
import org.nightlabs.jfire.prop.Struct;
import org.nightlabs.jfire.prop.id.StructID;
import org.nightlabs.progress.ProgressMonitor;

public class StructDAO extends BaseJDOObjectDAO<StructID, Struct> {

	public static final String[] DEFAULT_FETCH_GROUPS = new String[] {FetchPlan.ALL};

	PropertyManagerRemote pm;

	/**
	 * The shared instance
	 */
	private static StructDAO sharedInstance;

	/**
	 * Returns the lazily created shared instance.
	 * @return the shared instance.
	 */
	public static StructDAO sharedInstance()
	{
		if (sharedInstance == null)
			sharedInstance = new StructDAO();
		return sharedInstance;
	}

	@Override
	protected Collection<Struct> retrieveJDOObjects(Set<StructID> objectIDs, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor) throws Exception {
		if (pm == null)
			pm = getEjbProvider().getRemoteBean(PropertyManagerRemote.class);
		try {
			ArrayList<Struct> structs = new ArrayList<Struct>(objectIDs.size());
			for (StructID structID : objectIDs)
				structs.add(retrieveJDOObject(structID, fetchGroups, maxFetchDepth, monitor));
			return structs;
		} finally {
			pm = null;
		}
	}

	@Override
	protected Struct retrieveJDOObject(StructID objectID, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor) throws Exception {
		PropertyManagerRemote pm2 = pm;
		if (pm2 == null)
			pm2 = getEjbProvider().getRemoteBean(PropertyManagerRemote.class);
		Struct struct = pm2.getFullStruct(objectID, fetchGroups, maxFetchDepth);
		if (monitor != null)
			monitor.worked(1);
		return struct;
	}

	private synchronized Struct getStruct(StructID structID, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
	{
		try {
			Struct struct = getJDOObject(null, structID, fetchGroups, maxFetchDepth, monitor);
			return struct;
		} catch (Exception e) {
			throw new RuntimeException("Struct download failed.", e);
		}
	}

	public Struct getStruct(StructID structID, ProgressMonitor monitor) {
		return getStruct(structID, DEFAULT_FETCH_GROUPS, NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT, monitor);
	}

	public Struct storeStruct(Struct struct, boolean get, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor) {
		try {
			PropertyManagerRemote propManager = getEjbProvider().getRemoteBean(PropertyManagerRemote.class);
			return (Struct) propManager.storeStruct(struct, get, fetchGroups, maxFetchDepth);
		} catch (Exception e) {
			throw new RuntimeException("Storing StructLocal failed", e);
		}
	}

}
