package org.nightlabs.jfire.prop.dao;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.nightlabs.jdo.ObjectIDUtil;
import org.nightlabs.jfire.base.JFireEjb3Factory;
import org.nightlabs.jfire.base.jdo.BaseJDOObjectDAO;
import org.nightlabs.jfire.prop.DataField;
import org.nightlabs.jfire.prop.PropertyManagerRemote;
import org.nightlabs.jfire.prop.PropertySet;
import org.nightlabs.jfire.prop.Struct;
import org.nightlabs.jfire.prop.StructField;
import org.nightlabs.jfire.prop.id.PropertySetID;
import org.nightlabs.jfire.prop.id.StructFieldID;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.progress.ProgressMonitor;
import org.nightlabs.util.CollectionUtil;

/**
 * Data access objects for {@link PropertySet}s whose {@link DataField}s are trimmed to a subsect of
 * the {@link StructField}s of the underlying {@link Struct}.
 * <p>
 * This DAO uses a special scope in the Cache-system in order to not interfere with the normally
 * detached objects.
 * </p>
 * <p>
 * Note that objects obtained using this DAO can't be re-attached to the datastore after change.
 * </p>
 * 
 * @author Tobias Langner <!-- tobias[dot]langner[at]nightlabs[dot]de -->
 */
public class TrimmedPropertySetDAO
extends BaseJDOObjectDAO<PropertySetID, PropertySet>
{

	private static final String TRIMMED_PROPERTY_SETS_SCOPE = "org.nightlabs.jfire.prop.PropertyManagerRemote#getDetachedTrimmedPropertySets";

	protected TrimmedPropertySetDAO() {}

	/** The shared instance */
	private static TrimmedPropertySetDAO sharedInstance = null;

	/**
	 * Returns (and lazily creates) the static shared instance of {@link TrimmedPropertySetDAO}.
	 * @return The static shared instance of {@link TrimmedPropertySetDAO}.
	 */
	public static TrimmedPropertySetDAO sharedInstance() {
		if (sharedInstance == null) {
			synchronized (PropertySetDAO.class) {
				if (sharedInstance == null)
					sharedInstance = new TrimmedPropertySetDAO();
			}
		}
		return sharedInstance;
	}
	
	@Override
	protected Collection<? extends PropertySet> retrieveJDOObjects(Set<PropertySetID> objectIDs, String[] fetchGroupArray, int maxFetchDepth,
			ProgressMonitor monitor) throws Exception
	{
		List<String> fetchGroupList = CollectionUtil.array2ArrayList(fetchGroupArray);
		PropertyManagerRemote pm = JFireEjb3Factory.getRemoteBean(PropertyManagerRemote.class, SecurityReflector.getInitialContextProperties());
		
		// Extract StructFieldIDs encoded within the fetchGroups and remove them from the fetch groups afterwards
		Set<StructFieldID> structFieldIDs = new HashSet<StructFieldID>();
		
		final String JDO_PREFIX = ObjectIDUtil.JDO_PREFIX + ObjectIDUtil.JDO_PREFIX_SEPARATOR;
		
		for (Iterator<String> it = fetchGroupList.iterator(); it.hasNext(); ) {
			String fetchGroup = it.next();
			
			if (fetchGroup.startsWith(JDO_PREFIX)) {
				structFieldIDs.add((StructFieldID) ObjectIDUtil.createObjectID(fetchGroup));
				it.remove();
			}
		}
		
		String[] fetchGroups = CollectionUtil.collection2TypedArray(fetchGroupList, String.class);
			
		return pm.getDetachedTrimmedPropertySets(objectIDs, structFieldIDs, fetchGroups, maxFetchDepth);
	}
	
	protected static String[] getMergedFetchGroups(String[] fetchGroups, Collection<StructFieldID> structFieldIDs) {
		String[] result = null; 
		int i = 0;
		if (fetchGroups != null) {
			result =  Arrays.copyOf(fetchGroups, fetchGroups.length + structFieldIDs.size());
			i = fetchGroups.length;
		} else {
			result = new String[structFieldIDs.size()];
		}
		
		for (StructFieldID id : structFieldIDs)
			result[i++] = id.toString();
		
		return result;
	}
	
	/**
	 * Returns the {@link PropertySet} with the given ID trimmed to the {@link DataField}s of the given {@link StructFieldID}s.
	 * 
	 * @param propertySetId The {@link PropertySetID} of the {@link PropertySet} to be returned.
	 * @param structFieldIDs The IDs of the {@link StructField}s to which the returned {@link PropertySet} shall be trimmed.
	 * @param fetchGroups The fetch groups to be used.
	 * @param maxFetchDepth The max fetch depth to be used.
	 * @param monitor A progress monitor.
	 * @return The {@link PropertySet} with the given ID trimmed to the {@link DataField}s of the given {@link StructFieldID}s.
	 */
	public PropertySet getTrimmedPropertySet(PropertySetID propertySetId, Collection<StructFieldID> structFieldIDs, String[] fetchGroups,
			int maxFetchDepth, ProgressMonitor monitor)
	{
		String[] mergedFetchGroups = getMergedFetchGroups(fetchGroups, structFieldIDs);
		
		return getJDOObject(TRIMMED_PROPERTY_SETS_SCOPE, propertySetId, mergedFetchGroups, maxFetchDepth, monitor);
	}
	
	/**
	 * Returns the {@link PropertySet}s with the given IDs trimmed to the {@link DataField}s of the given {@link StructFieldID}s.
	 * 
	 * @param propertySetIDs The {@link PropertySetID}s of the {@link PropertySet}s to be returned.
	 * @param structFieldIDs The IDs of the {@link StructField}s to which the returned {@link PropertySet}s shall be trimmed.
	 * @param fetchGroups The fetch groups to be used.
	 * @param maxFetchDepth The max fetch depth to be used.
	 * @param monitor A progress monitor.
	 * @return The {@link PropertySet}s with the given IDs trimmed to the {@link DataField}s of the given {@link StructFieldID}s.
	 */
	public Collection<? extends PropertySet> getTrimmedPropertySets(Set<PropertySetID> propertySetIds, Collection<StructFieldID> structFieldIDs,
			String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
	{
		String[] mergedFetchGroups = getMergedFetchGroups(fetchGroups, structFieldIDs);
		
		return getJDOObjects(TRIMMED_PROPERTY_SETS_SCOPE, propertySetIds, mergedFetchGroups, maxFetchDepth, monitor);
	}

}
