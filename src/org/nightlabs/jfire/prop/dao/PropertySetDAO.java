/**
 * 
 */
package org.nightlabs.jfire.prop.dao;

import java.util.Collection;
import java.util.Set;

import org.nightlabs.jfire.base.JFireEjbUtil;
import org.nightlabs.jfire.base.jdo.BaseJDOObjectDAO;
import org.nightlabs.jfire.base.jdo.IJDOObjectDAO;
import org.nightlabs.jfire.prop.PropertyManager;
import org.nightlabs.jfire.prop.PropertySet;
import org.nightlabs.jfire.prop.id.PropertySetID;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.progress.ProgressMonitor;

/**
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 *
 */
public class PropertySetDAO
extends BaseJDOObjectDAO<PropertySetID, PropertySet>
implements IJDOObjectDAO<PropertySet>
{
	protected PropertySetDAO() {}

	/** The shared instance */
	private static PropertySetDAO sharedInstance = null;

	/**
	 * Returns (and lazily creates) the static shared instance of {@link PropertySetDAO}.
	 * @return The static shared instance of {@link PropertySetDAO}.
	 */
	public static PropertySetDAO sharedInstance() {
		if (sharedInstance == null) {
			synchronized (PropertySetDAO.class) {
				if (sharedInstance == null)
					sharedInstance = new PropertySetDAO();
			}
		}
		return sharedInstance;
	}	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Collection<PropertySet> retrieveJDOObjects(
			Set<PropertySetID> objectIDs, String[] fetchGroups, int maxFetchDepth,
			ProgressMonitor monitor) throws Exception 
	{
		PropertyManager pm = JFireEjbUtil.getBean(PropertyManager.class, SecurityReflector.getInitialContextProperties());
		return pm.getPropertySets(objectIDs, fetchGroups, maxFetchDepth);
	}
	
	public Collection<PropertySet> getPropertySets(Collection<PropertySetID> propertySetIDs, String[] fetchGroups,
			int maxFetchDepth, ProgressMonitor monitor) {
		return getJDOObjects(null, propertySetIDs, fetchGroups, maxFetchDepth, monitor);
	}
	
	public PropertySet getPropertySet(PropertySetID propertySetID, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor) {
		return super.getJDOObject(null, propertySetID, fetchGroups, maxFetchDepth, monitor);
	}

	/**
	 * {@inheritDoc}
	 */
	public PropertySet storeJDOObject(PropertySet propertySet, boolean get, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
	{
		try {
			PropertyManager pm = JFireEjbUtil.getBean(PropertyManager.class, SecurityReflector.getInitialContextProperties());
			return pm.storePropertySet(propertySet, get, fetchGroups, maxFetchDepth);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public PropertySet storePropertySet(PropertySet propertySet, boolean get, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor) {
		return storeJDOObject(propertySet, get, fetchGroups, maxFetchDepth, monitor);
	}
}
