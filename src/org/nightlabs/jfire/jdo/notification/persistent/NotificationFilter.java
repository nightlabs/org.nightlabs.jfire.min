package org.nightlabs.jfire.jdo.notification.persistent;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jdo.JDODetachedFieldAccessException;
import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.jdo.annotations.Column;
import javax.jdo.annotations.Discriminator;
import javax.jdo.annotations.DiscriminatorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.Join;
import javax.jdo.annotations.NullValue;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.PersistenceModifier;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;
import javax.jdo.annotations.Queries;
import javax.jdo.listener.StoreCallback;

import org.nightlabs.jfire.jdo.notification.DirtyObjectID;
import org.nightlabs.jfire.jdo.notification.JDOLifecycleState;
import org.nightlabs.jfire.jdo.notification.persistent.id.NotificationFilterID;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.security.SecurityReflector.UserDescriptor;
import org.nightlabs.util.Util;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.jdo.notification.persistent.id.NotificationFilterID"
 *		detachable="true"
 *		table="JFireBase_NotificationFilter"
 *
 * @jdo.inheritance strategy="new-table"
 * @jdo.inheritance-discriminator strategy="class-name"
 *
 * @jdo.create-objectid-class field-order="organisationID, subscriberType, subscriberID, subscriptionID"
 *
 *  @jdo.query
 *		name="getNotificationFiltersByCandidateClassAndLifecycleState"
 *		query="SELECT WHERE
 *			this.candidateClasses.contains(:candidateClass) &&
 *			this.lifecycleStates.contains(:lifecycleState)"
 *
 * @jdo.query
 *		name="getNotificationFiltersByCandidateClassAndLifecycleStateAndIncludeSubclasses"
 *		query="SELECT WHERE
 *			this.candidateClasses.contains(:candidateClass) &&
 *			this.lifecycleStates.contains(:lifecycleState) &&
 *			this.includeSubclasses == :includeSubclasses"
 */
@PersistenceCapable(
	objectIdClass=NotificationFilterID.class,
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireBase_NotificationFilter")
@Discriminator(strategy=DiscriminatorStrategy.CLASS_NAME)
@Queries({
	@javax.jdo.annotations.Query(
		name="getNotificationFiltersByCandidateClassAndLifecycleState",
		value="SELECT WHERE this.candidateClasses.contains(:candidateClass) && this.lifecycleStates.contains(:lifecycleState)"),
	@javax.jdo.annotations.Query(
		name="getNotificationFiltersByCandidateClassAndLifecycleStateAndIncludeSubclasses",
		value="SELECT WHERE this.candidateClasses.contains(:candidateClass) && this.lifecycleStates.contains(:lifecycleState) && this.includeSubclasses == :includeSubclasses")
})
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public abstract class NotificationFilter
implements StoreCallback, Serializable
{
	private static final long serialVersionUID = 1L;

	@SuppressWarnings("unchecked")
	public static Set<NotificationFilter> getCandidates(PersistenceManager pm, Class dirtyObjectClass, JDOLifecycleState lifecycleState)
	throws ClassNotFoundException
	{
		Set res = new HashSet();
		Class clazz = dirtyObjectClass;

		// first search for exact matches of the DirtyObjectID's JDO-object's class
		Query q = pm.newNamedQuery(NotificationFilter.class, "getNotificationFiltersByCandidateClassAndLifecycleState");

		Map params = new HashMap(3);
		params.put("candidateClass", clazz.getName());
		params.put("lifecycleState", lifecycleState.name());

		res.addAll((Collection)q.executeWithMap(params));

		// step up the class hierarchy and search for all matching Subscriptions (which include subclasses)
		clazz = clazz.getSuperclass();

		q = pm.newNamedQuery(NotificationFilter.class, "getNotificationFiltersByCandidateClassAndLifecycleStateAndIncludeSubclasses");
		params.put("includeSubclasses", Boolean.TRUE);

		while (clazz != null) {
			params.put("candidateClass", clazz.getName());
			res.addAll((Collection)q.executeWithMap(params));
			clazz = clazz.getSuperclass();
		}

		return res;
	}

//	/**
//	 * This is the only type known by the core, but you can use whatever type you want for
//	 * other purposes (e.g. clients or satellite systems).
//	 * <p>
//	 * Depending on the subscriber type, a PushNotifier will be chosen. If none exists, the
//	 * notifications cannot be actively forwarded, but will instead wait for polling.
//	 * </p>
//	 */
//	public static final String SUBSCRIBER_TYPE_ORGANISATION = "organisation";
	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
@PrimaryKey
@Column(length=100)
	private String organisationID;
	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	@PrimaryKey
	@Column(length=100)
	private String subscriberType;
	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	@PrimaryKey
	@Column(length=100)
	private String subscriberID;
	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	@PrimaryKey
	@Column(length=100)
	private String subscriptionID;

	/**
	 * The user who owns this NotificationFilter. The {@link #filter(List)} method is executed as this user.
	 *
	 * @jdo.field persistence-modifier="persistent" null-value="exception"
	 */
	@Persistent(
		nullValue=NullValue.EXCEPTION,
		persistenceModifier=PersistenceModifier.PERSISTENT)
	private User user;

	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected NotificationFilter() { }

	/**
	 * @param organisationID This is the organisation which emits notifications - i.e. where the JDO objects
	 *		focused by this <code>NotificationFilter</code> are added/modified/deleted.
	 * @param subscriberType This describes the type of the subscriber. If it is another organisation, then
	 *		this must be {@link #SUBSCRIBER_TYPE_ORGANISATION} - otherwise it is any other identifier-string
	 *		describing the type of the subscriber.
	 * @param subscriberID The identifier of the subscriber within the scope of the type. If the type is
	 *		{@link #SUBSCRIBER_TYPE_ORGANISATION}, then this subscriberID is the other organisationID (the one
	 *		that will be notified).
	 * @param subscriptionID An identifier chosen by the subscriber to reference this subscription.
	 */
	public NotificationFilter(String organisationID, String subscriberType, String subscriberID, String subscriptionID)
	{
		this.organisationID = organisationID;
		this.subscriberType = subscriberType;
		this.subscriberID = subscriberID;
		this.subscriptionID = subscriptionID;

		this.candidateClasses = new HashSet<String>();
		this.lifecycleStates = new HashSet<String>();
//		this.lifecycleStates = new HashSet<JDOLifecycleState>();
	}

	public String getOrganisationID()
	{
		return organisationID;
	}
	public String getSubscriberType()
	{
		return subscriberType;
	}
	public String getSubscriberID()
	{
		return subscriberID;
	}
	public String getSubscriptionID()
	{
		return subscriptionID;
	}

	public User getUser()
	{
		return user;
	}

	protected PersistenceManager getPersistenceManager()
	{
		PersistenceManager pm = JDOHelper.getPersistenceManager(this);
		if (pm == null)
			throw new IllegalStateException("Cannot obtain PersistenceManager! This instance seems to be detached (or otherwise not persistent)!");

		return pm;
	}

//	public String getPrimaryKey()
//	{
//		return getPrimaryKey(organisationID, subscriberType, subscriberID, subscriptionID);
//	}
//
//	public static String getPrimaryKey(String organisationID, String subscriberType, String subscriberID, String subscriptionID)
//	{
//		return organisationID + '/' + subscriberType + '/' + subscriberID + '/' + subscriptionID;
//	}

	// *** begin: filtering NotificationFilter (maybe we'll extend the NotificationFilter to support registration per object-id later)
	/**
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="collection"
	 *		element-type="String"
	 *		table="JFireBase_NotificationFilter_candidateClasses"
	 *		null-value="exception"
	 *
	 * @jdo.join
	 */
@Join
@Persistent(
	nullValue=NullValue.EXCEPTION,
	table="JFireBase_NotificationFilter_candidateClasses",
	persistenceModifier=PersistenceModifier.PERSISTENT)
	private Set<String> candidateClasses;

	/**
	 * @return The fully qualified class names of JDO objects about which subscriber wants to get notified.
	 *		This must not be empty! It is urgently recommended, not
	 *		to use <code>Object</code> here, but be as specific as possible.
	 *		Currently, only classes are supported (no interfaces), but this
	 *		might be changed in future versions of JFire.
	 *
	 * @see #includeSubclasses()
	 */
	public Set<String> getCandidateClasses()
	{
		return candidateClasses;
	}

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private boolean includeSubclasses;

	/**
	 * If this method returns <code>false</code>, this listener will only be triggered
	 * when the jdo object's class exactly matches one of the given candidate classes.
	 * If this method returns <code>true</code>, this listener will be triggered for
	 * subclasses of the given candidate classes, as well.
	 *
	 * @return Whether or not to include subclasses of the given candidate classes.
	 */
	public boolean isIncludeSubclasses()
	{
		return includeSubclasses;
	}
	public void setIncludeSubclasses(boolean includeSubclasses)
	{
		this.includeSubclasses = includeSubclasses;
	}

	/**
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="collection"
	 *		element-type="String"
	 *		Xelement-type="JDOLifecycleState"
	 *		table="JFireBase_NotificationFilter_lifecycleStates"
	 *		null-value="exception"
	 *
	 * @jdo.join
	 */
	@Join
	@Persistent(
		nullValue=NullValue.EXCEPTION,
		table="JFireBase_NotificationFilter_lifecycleStates",
		persistenceModifier=PersistenceModifier.PERSISTENT)
	private Set<String> lifecycleStates;
//	private Set<JDOLifecycleState> lifecycleStates;

	private static class WrapperIterator
	implements Iterator<JDOLifecycleState>
	{
		private Iterator<String> real;

		public WrapperIterator(Iterator<String> real)
		{
			this.real = real;
		}

		public boolean hasNext()
		{
			return real.hasNext();
		}

		public JDOLifecycleState next()
		{
			return JDOLifecycleState.valueOf(real.next());
		}

		public void remove()
		{
			real.remove();
		}
	}

	private static class WrapperSet
	implements Set<JDOLifecycleState>
	{
		private Set<String> real;

		public WrapperSet(Set<String> real)
		{
			this.real = real;
		}

		public boolean add(JDOLifecycleState o)
		{
			return real.add(o.name());
		}

		public boolean addAll(Collection<? extends JDOLifecycleState> c)
		{
			boolean res = false;
			for (JDOLifecycleState state : c) {
				if (real.add(state.name()))
					res = true;
			}
			return res;
		}

		public void clear()
		{
			real.clear();
		}

		public boolean contains(Object o)
		{
			if (o instanceof JDOLifecycleState)
				return real.contains(((JDOLifecycleState)o).name());
			else
				return false;
		}

		public boolean containsAll(Collection<?> c)
		{
			for (Object object : c) {
				if (!contains(object))
					return false;
			}
			return true;
		}

		public boolean isEmpty()
		{
			return real.isEmpty();
		}

		public Iterator<JDOLifecycleState> iterator()
		{
			return new WrapperIterator(real.iterator());
		}

		public boolean remove(Object o)
		{
			if (o instanceof JDOLifecycleState)
				return real.contains(((JDOLifecycleState)o).name());
			else
				return false;
		}

		public boolean removeAll(Collection<?> c)
		{
			boolean res = false;
			for (Object object : c) {
				if (remove(object))
					res = true;
			}
			return res;
		}

		@SuppressWarnings("unchecked")
		public boolean retainAll(Collection<?> c)
		{
			HashSet cs = new HashSet(c.size());
			for (Object object : c) {
				if (object instanceof JDOLifecycleState)
					cs.add(((JDOLifecycleState)object).name());
			}
			return real.retainAll(cs);
		}

		public int size()
		{
			return real.size();
		}

		public Object[] toArray()
		{
			return toArray(new JDOLifecycleState[size()]);
		}

		@SuppressWarnings("unchecked")
		public <T> T[] toArray(T[] a)
		{
			int size = size();
			if (a.length < size)
				a = (T[])java.lang.reflect.Array.newInstance(a.getClass().getComponentType(), size);

			int idx = 0;
			for (String s : real)
				a[idx++] = (T)JDOLifecycleState.valueOf(s);

			if (a.length > size)
				a[size] = null;

			return a;
		}
	}

	/**
	 * @jdo.field persistence-modifier="none"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.NONE)
	private transient WrapperSet wrapperSet;

	/**
	 * @return The lifecycle stages in which the listener is interested. This must not be empty.
	 */
	public Set<JDOLifecycleState> getLifecycleStates()
	{
		if (wrapperSet == null)
			wrapperSet = new WrapperSet(lifecycleStates);

		return wrapperSet;
	}

	/**
	 * This method checks which ones of the given {@link DirtyObjectID}s need
	 * to be forwarded to listener (or so-called subscriber).
	 * <p>
	 * Note that the passed <code>dirtyObjectIDs</code> argument contains
	 * only those instances that match already the other criteria of this filter
	 * (e.g. the candidate classes and the lifecycle types).
	 * </p>
	 * <p>
	 * Since implementors are allowed to modify the <code>dirtyObjectIDs</code> parameter,
	 * it is common practice to iterate this list, remove the elements that are not interesting
	 * and return the same list. If this practice does not fit your needs, you can of course
	 * create a new <code>Collection</code> and return it instead.
	 * </p>
	 *
	 * @param dirtyObjectIDs the objects to be filtered. Implementors are allowed to modify this <code>List</code> (i.e. to remove elements from it).
	 * @return Those {@link DirtyObjectID}s that shall be forwarded to the client.
	 *		This can be empty or even <code>null</code> (they're understood as equivalent).
	 */
	public abstract Collection<DirtyObjectID> filter(List<DirtyObjectID> dirtyObjectIDs);

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (obj == null) return false;
		if (obj.getClass() != this.getClass()) return false;
		NotificationFilter o = (NotificationFilter) obj;
		return (
				Util.equals(this.organisationID, o.organisationID) &&
				Util.equals(this.subscriberType, o.subscriberType) &&
				Util.equals(this.subscriberID, o.subscriberID) &&
				Util.equals(this.subscriptionID, o.subscriptionID)
		);
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + Util.hashCode(organisationID);
		result = prime * result + Util.hashCode(subscriberType);
		result = prime * result + Util.hashCode(subscriberID);
		result = prime * result + Util.hashCode(subscriptionID);
		return result;
	}

	@Override
	public String toString() {
		return this.getClass().getName() + '@' + Integer.toHexString(System.identityHashCode(this)) + '[' + organisationID + ',' + subscriberType + ',' + subscriberID + ',' + subscriptionID + ']';
	}

	@Override
	public void jdoPreStore()
	{
		try {
			if (this.user == null) {
				UserDescriptor userDescriptor = SecurityReflector.getUserDescriptor();
				this.user = userDescriptor.getUser(getPersistenceManager());
			}
		} catch (JDODetachedFieldAccessException x) {
			// ignore if user is not detached
		}
	}
}
