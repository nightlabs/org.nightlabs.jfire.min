package org.nightlabs.jfire.prop.dao;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import javax.jdo.FetchPlan;
import javax.security.auth.login.LoginException;

import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.base.jdo.BaseJDOObjectDAO;
import org.nightlabs.jfire.base.jdo.login.JFireLoginProvider;
import org.nightlabs.jfire.prop.IStruct;
import org.nightlabs.jfire.prop.PropertyManager;
import org.nightlabs.jfire.prop.PropertyManagerUtil;
import org.nightlabs.jfire.prop.StructLocal;
import org.nightlabs.jfire.prop.id.StructLocalID;
import org.nightlabs.progress.ProgressMonitor;

public class StructLocalDAO extends BaseJDOObjectDAO<StructLocalID, StructLocal> {

	PropertyManager pm;

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
		if (pm == null)
			pm = PropertyManagerUtil.getHome(JFireLoginProvider.sharedInstance().getInitialContextProperties()).create();
		try {
			ArrayList<StructLocal> structLocals = new ArrayList<StructLocal>(objectIDs.size());
			for (StructLocalID structLocalID : objectIDs)
				structLocals.add(retrieveJDOObject(structLocalID, fetchGroups, maxFetchDepth, monitor));
			return structLocals;
		} finally {
			pm = null;
		}
	}

	@Override
	protected StructLocal retrieveJDOObject(StructLocalID objectID, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor) throws Exception {
		PropertyManager pm2 = pm;
		if (pm2 == null)
			pm2 = PropertyManagerUtil.getHome(JFireLoginProvider.sharedInstance().getInitialContextProperties()).create();
		StructLocal structLocal = pm2.getFullStructLocal(objectID, fetchGroups, maxFetchDepth);
		if (monitor != null)
			monitor.worked(1);
		return structLocal;
	}

	private synchronized StructLocal getStructLocal(StructLocalID structLocalID, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
	{
		StructLocal structLocal = getJDOObject(null, structLocalID, fetchGroups, maxFetchDepth, monitor);
		structLocal.restoreAdoptedBlocks();
		return structLocal;
	}

	public StructLocal getStructLocal(Class linkClass, String scope, ProgressMonitor monitor) {
		return getStructLocal(linkClass.getName(), scope, monitor);
	}
	
	public StructLocal getStructLocal(String linkClass, String scope, ProgressMonitor monitor) {
		StructLocalID structLocalID;
		try {
			structLocalID = StructLocalID.create(JFireLoginProvider.sharedInstance().getOrganisationID(), linkClass, scope);
		} catch (LoginException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		return getStructLocal(structLocalID, new String[] {FetchPlan.DEFAULT, IStruct.FETCH_GROUP_ISTRUCT_FULL_DATA}, NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT, monitor);		
	}
	
	public StructLocal getStructLocal(StructLocalID structLocalID, ProgressMonitor monitor) {
		return getStructLocal(structLocalID, new String[] {FetchPlan.DEFAULT, IStruct.FETCH_GROUP_ISTRUCT_FULL_DATA}, NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT, monitor);
	}
}
