/*
 * Created on Nov 11, 2005
 */
package org.nightlabs.ipanema.jdo.cache;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class DirtyObjectIDContainer
implements Serializable
{
	private long createDT = System.currentTimeMillis();
	private boolean closed = false;

	protected void assertOpen()
	{
		if (closed)
			throw new IllegalStateException("This instance of DirtyObjectIDContainer (created " + createDT + ") is already closed!");
	}

	/**
	 * key: Object objectID<br/>
	 * value: DirtyObjectID dirtyObjectID
	 */
	private Map dirtyObjectIDs = null;
//	private static final Map EMPTY_MAP = Collections.unmodifiableMap(new HashMap());

	public DirtyObjectIDContainer()
	{
	}

	public long getCreateDT()
	{
		return createDT;
	}

	public synchronized void addDirtyObjectIDs(Collection newDirtyObjectIDs)
	{
		assertOpen();

		if (newDirtyObjectIDs.isEmpty())
			return;

		if (dirtyObjectIDs == null) {
			dirtyObjectIDs = new HashMap(newDirtyObjectIDs.size());
			for (Iterator it = newDirtyObjectIDs.iterator(); it.hasNext();) {
				DirtyObjectID newDirtyObjectID = (DirtyObjectID) it.next();
				dirtyObjectIDs.put(newDirtyObjectID.getObjectID(), newDirtyObjectID);
			}
			return;
		}

		for (Iterator it = newDirtyObjectIDs.iterator(); it.hasNext();) {
			DirtyObjectID newDirtyObjectID = (DirtyObjectID) it.next();
			Object objectID = newDirtyObjectID.getObjectID();
			DirtyObjectID dirtyObjectID = (DirtyObjectID) dirtyObjectIDs.get(objectID);
			if (dirtyObjectID == null)
				dirtyObjectIDs.put(objectID, newDirtyObjectID);
			else
				dirtyObjectID.addSourceSessionIDs(newDirtyObjectID.getSourceSessionIDs());
		}
	}

	/**
	 * @param objectID The JDO object-id pointing to an instance of {@link DirtyObjectID}. 
	 * @return Returns either <code>null</code> or a <code>DirtyObjectID</code>.
	 */
	public synchronized DirtyObjectID getDirtyObjectID(Object objectID)
	{
		assertOpen();

		if (dirtyObjectIDs == null)
			return null;

		return (DirtyObjectID) dirtyObjectIDs.get(objectID);
	}

	public void close()
	{
		closed = true;
	}
}
