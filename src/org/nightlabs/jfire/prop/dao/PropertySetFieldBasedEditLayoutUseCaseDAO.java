/**
 *
 */
package org.nightlabs.jfire.prop.dao;

import java.util.Collection;
import java.util.Set;

import org.nightlabs.jfire.base.JFireEjb3Factory;
import org.nightlabs.jfire.base.jdo.BaseJDOObjectDAO;
import org.nightlabs.jfire.prop.PropertyManagerRemote;
import org.nightlabs.jfire.prop.config.PropertySetFieldBasedEditLayoutUseCase;
import org.nightlabs.jfire.prop.config.id.PropertySetFieldBasedEditLayoutUseCaseID;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.progress.ProgressMonitor;

/**
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] -->
 */
public class PropertySetFieldBasedEditLayoutUseCaseDAO
extends BaseJDOObjectDAO<PropertySetFieldBasedEditLayoutUseCaseID, PropertySetFieldBasedEditLayoutUseCase>
{
	@Override
	protected Collection<PropertySetFieldBasedEditLayoutUseCase> retrieveJDOObjects(
			Set<PropertySetFieldBasedEditLayoutUseCaseID> objectIDs,
			String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
			throws Exception {
		PropertyManagerRemote pm = JFireEjb3Factory.getRemoteBean(PropertyManagerRemote.class, SecurityReflector.getInitialContextProperties());
		return pm.getPropertySetFieldBasedEditLayoutUseCases(objectIDs, fetchGroups, maxFetchDepth);
	}

	/** The shared instance */
	private static PropertySetFieldBasedEditLayoutUseCaseDAO sharedInstance = null;

	/**
	 * Returns (and lazily creates) the static shared instance of {@link PropertySetFieldBasedEditLayoutUseCaseDAO}.
	 * @return The static shared instance of {@link PropertySetFieldBasedEditLayoutUseCaseDAO}.
	 */
	public static PropertySetFieldBasedEditLayoutUseCaseDAO sharedInstance() {
		if (sharedInstance == null) {
			synchronized (PropertySetFieldBasedEditLayoutUseCaseDAO.class) {
				if (sharedInstance == null)
					sharedInstance = new PropertySetFieldBasedEditLayoutUseCaseDAO();
			}
		}
		return sharedInstance;
	}

	public Collection<PropertySetFieldBasedEditLayoutUseCase> getAllUseCases(String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor) {
		PropertyManagerRemote pm = JFireEjb3Factory.getRemoteBean(PropertyManagerRemote.class, SecurityReflector.getInitialContextProperties());
		Set<PropertySetFieldBasedEditLayoutUseCaseID> useCaseIDs = pm.getAllPropertySetFieldBasedEditLayoutUseCaseIDs();
		return getJDOObjects(null, useCaseIDs, fetchGroups, maxFetchDepth, monitor);
	}
}
