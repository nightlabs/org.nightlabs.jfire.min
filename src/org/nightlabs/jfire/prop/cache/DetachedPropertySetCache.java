package org.nightlabs.jfire.prop.cache;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.jdo.FetchPlan;
import javax.jdo.JDOHelper;
import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;

import org.apache.log4j.Logger;
import org.nightlabs.jfire.prop.PropertySet;
import org.nightlabs.jfire.prop.cache.id.DetachedPropertySetID;
import org.nightlabs.jfire.prop.id.PropertySetID;

@SuppressWarnings("deprecation")
public class DetachedPropertySetCache
{
	private static final Logger logger = Logger.getLogger(DetachedPropertySetCache.class);

	private PersistenceManager pm;

	public static DetachedPropertySetCache getInstance(PersistenceManager pm)
	{
		if (pm == null)
			throw new IllegalArgumentException("pm == null");

		String pmUserObjectKey = DetachedPropertySetCache.class.getName();
		DetachedPropertySetCache cache = (DetachedPropertySetCache) pm.getUserObject(pmUserObjectKey);
		if (cache == null) {
			cache = new DetachedPropertySetCache(pm);
			pm.putUserObject(pmUserObjectKey, cache);
		}
		return cache;
	}

	protected DetachedPropertySetCache(PersistenceManager pm) {
		this.pm = pm;
	}

	/**
	 * Get the {@link PropertySet} identified by <code>propertySetID</code> in a detached form. The detachment
	 * is done according to the specified <code>fetchGroups</code> and <code>maxFetchDepth</code>. If
	 * there is no <code>PropertySet</code> for the specified ID in the datastore, this method returns
	 * <code>null</code>.
	 *
	 * @param propertySetID the ID of the {@link PropertySet} to be looked up. This must not be <code>null</code>.
	 * @param fetchGroups the fetch-groups for detachment. This may be <code>null</code> (to use the default fg).
	 * @param maxFetchDepth the maximum fetch-depth for detachment.
	 * @return the desired {@link PropertySet} or <code>null</code>, if it does not exist.
	 */
	public PropertySet get(PropertySetID propertySetID, String[] fetchGroups, int maxFetchDepth)
	{
		FetchGroupsPartSet fetchGroupsPartSet = new FetchGroupsPartSet(fetchGroups);
		return get(propertySetID, fetchGroupsPartSet, maxFetchDepth);
	}

	protected PropertySet get(PropertySetID propertySetID, FetchGroupsPartSet fetchGroupsPartSet, int maxFetchDepth)
	{
		if (propertySetID == null)
			throw new IllegalArgumentException("propertySetID == null");

		if (fetchGroupsPartSet == null)
			throw new IllegalArgumentException("fetchGroupsPartSet == null");

		DetachedPropertySetID detachedPropertySetID = DetachedPropertySetID.create(
				propertySetID, fetchGroupsPartSet, maxFetchDepth
		);

		PropertySet propertySet = null;
		DetachedPropertySet detachedPropertySet;
		try {
			detachedPropertySet = (DetachedPropertySet) pm.getObjectById(detachedPropertySetID);

			if (logger.isDebugEnabled())
				logger.debug(String.format("get: Found detached property set for ID \"%s\".", propertySetID.organisationID + '/' + propertySetID.propertySetID));
		} catch (JDOObjectNotFoundException x) {
			if (logger.isDebugEnabled())
				logger.debug(String.format("get: Did not find detached property set for ID \"%s\".", propertySetID.organisationID + '/' + propertySetID.propertySetID));

			detachedPropertySet = null;
		}

		if (detachedPropertySet != null) {
			try {
				propertySet = detachedPropertySet.getPropertySet();
				checkPropertySet(propertySet);
			} catch (Throwable x) { // We better catch Throwable as deserializing might throw NoClassDefFoundError and other errors besides exceptions.
				propertySet = null;
				logger.warn("get: Failed loading serialized object: " + x, x);

				// Delete the non-readable carrier.
				pm.deletePersistent(detachedPropertySet);
				detachedPropertySet = null;
				pm.flush();
			}
		}

		if (propertySet == null) {
			if (fetchGroupsPartSet.isFetchGroupsNull())
				pm.getFetchPlan().setGroup(FetchPlan.DEFAULT);
			else
				pm.getFetchPlan().setGroups(fetchGroupsPartSet.getFetchGroups());

			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);

			PropertySet ps;
			try {
				ps = (PropertySet) pm.getObjectById(propertySetID);
			} catch (JDOObjectNotFoundException x) {
				ps = null;
			}

			if (ps != null) {
				propertySet = pm.detachCopy(ps);
				pm.makePersistent(new DetachedPropertySet(detachedPropertySetID, propertySet));

				if (logger.isDebugEnabled())
					logger.debug(String.format("get: Created and persisted detached property set for ID \"%s\".", propertySetID.organisationID + '/' + propertySetID.propertySetID));
			}
		}

		checkPropertySet(propertySet);
		return propertySet;
	}

	public List<PropertySet> get(Collection<PropertySetID> propertySetIDs, String[] fetchGroups, int maxFetchDepth)
	{
		if (propertySetIDs == null)
			throw new IllegalArgumentException("propertySetIDs == null");

		FetchGroupsPartSet fetchGroupsPartSet = new FetchGroupsPartSet(fetchGroups);

		List<PropertySet> propertySets = new ArrayList<PropertySet>();
		for (PropertySetID propertySetID : propertySetIDs) {
			PropertySet propertySet = get(propertySetID, fetchGroupsPartSet, maxFetchDepth);
			if (propertySet != null)
				propertySets.add(propertySet);
		}
		return propertySets;
	}

	private void checkPropertySet(PropertySet propertySet)
	{
		if (propertySet == null)
			return;

		if (JDOHelper.isDetached(propertySet) == false)
			throw new IllegalStateException("JDOHelper.isDetached(propertySet) == false :: " + propertySet);

		if (JDOHelper.getObjectId(propertySet) == null)
			throw new IllegalStateException("JDOHelper.getObjectId(propertySet) == null :: " + propertySet);
	}

	/**
	 * Remove all detached {@link PropertySet}s for the given one.
	 * @param propertySet the {@link PropertySet} for which to remove all cached entries.
	 */
	public void remove(PropertySet propertySet)
	{
		PropertySetID propertySetID = PropertySetID.create(propertySet.getOrganisationID(), propertySet.getPropertySetID());
		remove(propertySetID);
	}

	/**
	 * Remove all detached {@link PropertySet}s for the given {@link PropertySetID}.
	 * @param propertySetID the {@link PropertySetID} for which to remove all cached entries.
	 */
	public void remove(PropertySetID propertySetID)
	{
		Collection<DetachedPropertySet> detachedPropertySets = DetachedPropertySet.getDetachedPropertySets(pm, propertySetID);
		pm.deletePersistentAll(detachedPropertySets);
		pm.flush();

		if (logger.isDebugEnabled())
			logger.debug(String.format("remove: Removed %s detached property sets for ID \"%s\".", detachedPropertySets.size(), propertySetID.organisationID + '/' + propertySetID.propertySetID));
	}
}
