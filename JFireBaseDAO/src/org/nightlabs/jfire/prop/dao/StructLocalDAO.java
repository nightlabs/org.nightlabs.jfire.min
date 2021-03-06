package org.nightlabs.jfire.prop.dao;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import javax.jdo.FetchPlan;

import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.base.jdo.BaseJDOObjectDAO;
import org.nightlabs.jfire.prop.IStruct;
import org.nightlabs.jfire.prop.PropertyManagerRemote;
import org.nightlabs.jfire.prop.StructLocal;
import org.nightlabs.jfire.prop.id.StructLocalID;
import org.nightlabs.progress.ProgressMonitor;

public class StructLocalDAO extends BaseJDOObjectDAO<StructLocalID, StructLocal> {

	// FIXME: To retrieve the complete struct local, the fetchgroup IExpression.FETCH_GROUP_IEXPRESSION_FULL_DATA should also be included.
	// otherwise, validating the structure yields a JDODetachedFieldAccessException. I did not add it here because all usecases relying on
	// the fetch groups defined here seem to work. This could, however, also be due to the fact, that the respective struct local was detached
	// earlier with proper fetch groups and could thus have been fetched from the cache.
	public static final String[] DEFAULT_FETCH_GROUPS = new String[] {FetchPlan.DEFAULT, IStruct.FETCH_GROUP_ISTRUCT_FULL_DATA};

	/**
	 * The shared instance
	 */
	private static StructLocalDAO sharedInstance;

	/**
	 * Returns the lazily created shared instance.
	 * @return the shared instance.
	 */
	public static StructLocalDAO sharedInstance()
	{
		if (sharedInstance == null)
			sharedInstance = new StructLocalDAO();
		return sharedInstance;
	}

	@Override
	protected Collection<StructLocal> retrieveJDOObjects(Set<StructLocalID> objectIDs, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor) throws Exception {
		ArrayList<StructLocal> structLocals = new ArrayList<StructLocal>(objectIDs.size());
		for (StructLocalID structLocalID : objectIDs)
			structLocals.add(retrieveJDOObject(structLocalID, fetchGroups, maxFetchDepth, monitor));
		return structLocals;
	}

	@Override
	protected StructLocal retrieveJDOObject(StructLocalID objectID, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor) throws Exception {
		PropertyManagerRemote pm = getEjbProvider().getRemoteBean(PropertyManagerRemote.class);
		StructLocal structLocal = pm.getFullStructLocal(objectID, fetchGroups, maxFetchDepth);
		if (monitor != null)
			monitor.worked(1);
		return structLocal;
	}

	private synchronized StructLocal getStructLocal(StructLocalID structLocalID, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
	{
		StructLocal structLocal = getJDOObject(null, structLocalID, fetchGroups, maxFetchDepth, monitor);
		return structLocal;
	}

	public StructLocal getStructLocal(StructLocalID structLocalID, ProgressMonitor monitor) {
		return getStructLocal(structLocalID, DEFAULT_FETCH_GROUPS, NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT, monitor);
	}

	public StructLocal getStructLocal(StructLocalID structLocalID, String[] fetchGroups, ProgressMonitor monitor) {
		return getStructLocal(structLocalID, fetchGroups, NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT, monitor);
	}

	public StructLocal storeStructLocal(StructLocal structLocal, boolean get, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor) {
		try {
			PropertyManagerRemote propManager = getEjbProvider().getRemoteBean(PropertyManagerRemote.class);
			return (StructLocal) propManager.storeStruct(structLocal, get, fetchGroups, maxFetchDepth);
		} catch (Exception e) {
			throw new RuntimeException("Storing StructLocal failed", e);
		}
	}
}
