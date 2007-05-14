package org.nightlabs.jfire.base.prop;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import javax.jdo.FetchPlan;
import javax.security.auth.login.LoginException;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.base.jdo.JDOObjectDAO;
import org.nightlabs.jfire.base.login.Login;
import org.nightlabs.jfire.prop.PropertyManager;
import org.nightlabs.jfire.prop.PropertyManagerUtil;
import org.nightlabs.jfire.prop.Struct;
import org.nightlabs.jfire.prop.id.StructID;

public class StructDAO extends JDOObjectDAO<StructID, Struct> {

	PropertyManager pm;

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
	protected Collection<Struct> retrieveJDOObjects(Set<StructID> objectIDs, String[] fetchGroups, int maxFetchDepth, IProgressMonitor monitor) throws Exception {
		Assert.isNotNull(pm);
		ArrayList<Struct> structs = new ArrayList<Struct>(objectIDs.size());
		for (StructID structID : objectIDs)
			structs.add(retrieveJDOObject(structID, fetchGroups, maxFetchDepth, monitor));
		return structs;
	}

	@Override
	protected Struct retrieveJDOObject(StructID objectID, String[] fetchGroups, int maxFetchDepth, IProgressMonitor monitor) throws Exception {
		assert pm != null : "pm == null";
		Struct struct = pm.getFullStruct(objectID, fetchGroups, maxFetchDepth);
		if (monitor != null)
			monitor.worked(1);
		return struct;
	}

	private synchronized Struct getStruct(StructID structID, String[] fetchGroups, int maxFetchDepth, IProgressMonitor monitor)
	{
		try {
			pm = PropertyManagerUtil.getHome(Login.getLogin().getInitialContextProperties()).create();
			Struct struct = getJDOObject(null, structID, fetchGroups, maxFetchDepth, monitor);
			return struct;
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Struct download failed.", e);
		} finally {
			pm = null;
		}
	}

	public Struct getStruct(Class linkClass) {
		return getStruct(linkClass.getName());
	}
	
	public Struct getStruct(String linkClass)	{
		StructID structID;
		try {
			structID = StructID.create(Login.getLogin().getOrganisationID(), linkClass);
		} catch (LoginException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		return getStruct(structID, new String[] {FetchPlan.ALL}, NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT, new NullProgressMonitor());
	}
	
	public Struct getStruct(StructID structID) {
		return getStruct(structID, new String[] {FetchPlan.ALL}, NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT, new NullProgressMonitor());
	}
}
