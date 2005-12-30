/*
 * Created on Sep 15, 2005
 */
package org.nightlabs.ipanema.jdo.organisationsync;

import java.util.Collection;
import java.util.Set;

public interface DirtyObjectIDBuffer
{
	void init(OrganisationSyncManagerFactory organisationSyncManagerFactory) throws DirtyObjectIDBufferException;

	/**
	 * @param objectIDs JDO objectIDs which all implement {@link org.nightlabs.jdo.ObjectID}. They reference
	 *		all modified (or re-attached) JDO objects and will be fetched during outgoing-notification-processing
	 *		via {@link #fetchDirtyObjectIDs()}.
	 */
	void addDirtyObjectIDs(Collection objectIDs) throws DirtyObjectIDBufferException;

	/**
	 * @return Returns a Set with all dirty marked objectIDs. These records are marked
	 *		as being currently processed. After these objectIDs have been processed (means
	 *		distributed to the interested listeners), they are deleted by
	 *		{@link #clearFetchedDirtyObjectIDs()}.
	 */
	Set fetchDirtyObjectIDs() throws DirtyObjectIDBufferException;

	/**
	 * This method is called after {@link #fetchDirtyObjectIDs()} and must remove all
	 * objectIDs that have been returned (and marked) by {@link #fetchDirtyObjectIDs()}.
	 * If {@link #addDirtyObjectIDs(Collection)} adds new objectIDs in the meantime, they
	 * are not marked and won't be deleted.
	 */
	void clearFetchedDirtyObjectIDs() throws DirtyObjectIDBufferException;
}
