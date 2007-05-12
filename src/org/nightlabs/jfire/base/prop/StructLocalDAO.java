package org.nightlabs.jfire.base.prop;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import javax.security.auth.login.LoginException;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.nightlabs.jfire.base.jdo.JDOObjectDAO;
import org.nightlabs.jfire.base.login.Login;
import org.nightlabs.jfire.prop.PropertyManager;
import org.nightlabs.jfire.prop.PropertyManagerUtil;
import org.nightlabs.jfire.prop.StructLocal;
import org.nightlabs.jfire.prop.id.StructLocalID;

public class StructLocalDAO extends JDOObjectDAO<StructLocalID, StructLocal> {

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
	protected Collection<StructLocal> retrieveJDOObjects(Set<StructLocalID> objectIDs, String[] fetchGroups, int maxFetchDepth, IProgressMonitor monitor) throws Exception {
		Assert.isNotNull(pm);
		ArrayList<StructLocal> structLocals = new ArrayList<StructLocal>(objectIDs.size());
		for (StructLocalID structLocalID : objectIDs)
			structLocals.add(retrieveJDOObject(structLocalID, fetchGroups, maxFetchDepth, monitor));
		return structLocals;
	}

	@Override
	protected StructLocal retrieveJDOObject(StructLocalID objectID, String[] fetchGroups, int maxFetchDepth, IProgressMonitor monitor) throws Exception {
		assert pm != null : "pm == null";
		StructLocal structLocal = pm.getFullStructLocal(objectID);
		if (monitor != null)
			monitor.worked(1);
		return structLocal;
	}

	private synchronized StructLocal getStructLocal(StructLocalID structLocalID, String[] fetchGroups, int maxFetchDepth, IProgressMonitor monitor)
	{
		try {
			pm = PropertyManagerUtil.getHome(Login.getLogin().getInitialContextProperties()).create();
			StructLocal structLocal = getJDOObject(null, structLocalID, fetchGroups, maxFetchDepth, monitor);
			structLocal.restoreAdoptedBlocks();
			return structLocal;
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("StructLocal download failed.", e);
		} finally {
			pm = null;
		}
	}

	public StructLocal getStructLocal(Class linkClass, String scope)	{
		return getStructLocal(linkClass.getName(), scope);
	}
	
	public StructLocal getStructLocal(String linkClass, String scope)	{
		StructLocalID structLocalID;
		try {
			structLocalID = StructLocalID.create(Login.getLogin().getOrganisationID(), linkClass, scope);
		} catch (LoginException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		return getStructLocal(structLocalID, null, -1, null);
	}
	
	public StructLocal getStructLocal(StructLocalID structLocalID) {
		return getStructLocal(structLocalID, null, -1, null);
	}
}
