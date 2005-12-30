/*
 * Created on Jul 24, 2005
 */
package org.nightlabs.jfire.base.jdo.cache;

import java.util.Set;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 */
public class Key
{
	private String scope;
	private Object objectID;
	private Set fetchGroups;

	/**
	 * @param scope Can be <tt>null</tt> (the default) or a <tt>String</tt> specifying a different
	 *		namespace (e.g. if the method with which the data has been fetched does some special
	 *		manipulation with the object and it therefore differs even though the fetchGroups
	 *		are the same as with the normal fetch-method). 
	 * @param objectID A JDO object ID - must not be <tt>null</tt>. Note, that you MUST NOT change
	 *		the objectID after you called this constructor!
	 * @param fetchGroups Can be <tt>null</tt> or must be a <tt>Set</tt> of <tt>String</tt>.
	 *		Note, that you MUST NOT change the set after you called this constructor!
	 */
	public Key(String scope, Object objectID, Set fetchGroups)
	{
		this.scope = scope;

		if (objectID == null)
			throw new NullPointerException("objectID");

		this.objectID = objectID;
//		if (!(objectID instanceof ObjectID)) {
//			Logger.getLogger(Key.class).warn(
//					"objectID (class " + objectID.getClass().getName() + ") does not implement " + ObjectID.class.getName() + "!");
//		}

		this.fetchGroups = fetchGroups;
	}

	private int _hashCode = 0;

	/**
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode()
	{
		if (_hashCode == 0) {
			_hashCode =
				(scope == null ? 0 : scope.hashCode()) ^
				(objectID == null ? 0 : objectID.hashCode()) ^
				(fetchGroups ==  null ? 0 : fetchGroups.hashCode());
		}

		return _hashCode;
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj)
	{
		if (obj == this)
			return true;

		if (obj.hashCode() != this.hashCode())
			return false;

		if (!(obj instanceof Key))
			return false;

		Key other = (Key)obj;

		return
				(this.scope == null ? other.scope == null : this.scope.equals(other.scope))
				&&
				(this.objectID == null ? other.objectID == null : this.objectID.equals(other.objectID))
				&&
				(this.fetchGroups == null ? other.fetchGroups == null : this.fetchGroups.equals(other.fetchGroups));
	}

	private transient String thisString = null;

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString()
	{
		if (thisString == null) {
			StringBuffer sb = new StringBuffer();
			sb.append(this.getClass().getName());
			sb.append('{');
			sb.append("scope=");
			sb.append(scope);
			sb.append(';');
			sb.append("objectID=");
			sb.append(objectID);
			sb.append(';');
			sb.append("fetchGroups=");
			sb.append(fetchGroups);
			sb.append('}');
			thisString = sb.toString();
		}
		return thisString;
	}

	/**
	 * Note, that you MUST NOT change the returned <tt>Set</tt>! 
	 *
	 * @return Returns the fetchGroups.
	 */
	public Set getFetchGroups()
	{
		return fetchGroups;
	}
	/**
	 * @return Returns the objectID.
	 */
	public Object getObjectID()
	{
		return objectID;
	}
	/**
	 * @return Returns the scope.
	 */
	public String getScope()
	{
		return scope;
	}
}
