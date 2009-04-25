package org.nightlabs.jfire.prop;

import java.util.Collection;
import java.util.Set;

import javax.ejb.Remote;
import javax.jdo.spi.PersistenceCapable;

import org.nightlabs.jfire.prop.id.PropertySetID;
import org.nightlabs.jfire.prop.id.StructFieldID;
import org.nightlabs.jfire.prop.id.StructID;
import org.nightlabs.jfire.prop.id.StructLocalID;
import org.nightlabs.jfire.prop.search.PropSearchFilter;

@Remote
public interface PropertyManagerRemote 
{
	String ping(String message);

	/**
	 * Detaches and returns the complete Struct.
	 *
	 * @param organisationID
	 *          The organisation id of the {@link Struct} to be retrieved.
	 * @param linkClass
	 *          The linkClass of the {@link Struct} to be retrieved.
	 * @param structScope
	 * 			The scope of the {@link Struct} to be retrieved.
	 * @return The complete {@link Struct}.
	 */
	Struct getFullStruct(String organisationID, String linkClass,
			String structScope, String[] fetchGroups, int maxFetchDepth);

	/**
	 * Convenience method
	 */
	Struct getFullStruct(StructID structID, String[] fetchGroups,
			int maxFetchDepth);

	/**
	 * Detaches and returns the complete {@link StructLocal}.
	 *
	 * @param organisationID
	 *          The organisation id of the {@link StructLocal} to be retrieved.
	 * @param linkClass
	 *          The linkClass of the {@link StructLocal} to be retrieved.
	 * @return The complete {@link StructLocal}.
	 */
	StructLocal getFullStructLocal(String organisationID, String linkClass,
			String structScope, String structLocalScope, String[] fetchGroups,
			int maxFetchDepth);

	/**
	 * Convenience method
	 */
	StructLocal getFullStructLocal(StructLocalID structLocalID,
			String[] fetchGroups, int maxFetchDepth);

	/**
	 * Retrieve the person with the given ID
	 */
	PropertySet getPropertySet(PropertySetID propID, String[] fetchGroups,
			int maxFetchDepth);

	/**
	 * Returns those objects found by the given search filter.
	 * The results therefore might not be an instance
	 * of {@link PropertySet}, this depends on the result columns
	 * set in the search filter.
	 * <p>
	 * All found objects are detached with the given fetch-groups
	 * if they are {@link PersistenceCapable}.
	 * </p>
	 */
	Set<?> searchPropertySets(PropSearchFilter propSearchFilter,
			String[] fetchGroups, int maxFetchDepth);

	/**
	 * Executes the given search filter and assumes it will return instances
	 * of {@link PropertySet}. It will return the {@link PropertySetID}s of the
	 * found {@link PropertySet}s then.
	 * <p>
	 * Note, that if the given search filter does not return instances
	 * of {@link PropertySet} (its result columns might be set to something different)
	 * this method will fail with a {@link ClassCastException}.
	 * </p>
	 */
	Set<PropertySetID> searchPropertySetIDs(PropSearchFilter propSearchFilter);

	/**
	 * Store a {@link PropertySet} either detached or not made persistent yet.
	 */
	PropertySet storePropertySet(PropertySet propertySet, boolean get,
			String[] fetchGroups, int maxFetchDepth);

	/**
	 * Store a struct either detached or not made persistent yet.
	 */
	IStruct storeStruct(IStruct struct, boolean get, String[] fetchGroups,
			int maxFetchDepth);

	Collection<StructID> getAvailableStructIDs();

	Collection<StructLocalID> getAvailableStructLocalIDs();

	/**
	 * Returns all {@link PropertySet}s for the given propIDs, detached with the given
	 * fetchGroups
	 */
	Set<PropertySet> getPropertySets(Set<PropertySetID> propIDs,
			String[] fetchGroups, int maxFetchDepth);

	/**
	 * Returns the number of data field instances that have been created for the
	 * given {@link StructField}.
	 *
	 * @param field
	 *          The struct field
	 * @return
	 */
	long getDataFieldInstanceCount(StructFieldID fieldID);

	void initialise();
}