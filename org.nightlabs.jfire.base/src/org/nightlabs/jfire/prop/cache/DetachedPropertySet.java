package org.nightlabs.jfire.prop.cache;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

import javax.jdo.PersistenceManager;
import javax.jdo.annotations.Column;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Index;
import javax.jdo.annotations.Indices;
import javax.jdo.annotations.NullValue;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;
import javax.jdo.annotations.Queries;
import javax.jdo.annotations.Query;

import org.nightlabs.jfire.prop.PropertySet;
import org.nightlabs.jfire.prop.cache.id.DetachedPropertySetID;
import org.nightlabs.jfire.prop.id.PropertySetID;

/**
 * Data carrier holding a detached instance of {@link PropertySet} in binary-serialized form.
 *
 * @author marco
 * @deprecated <b>Internal class!</b> Do not use this class directly! Use the API provided by {@link DetachedPropertySetCache} only!
 */
@Deprecated
@PersistenceCapable(
		objectIdClass=DetachedPropertySetID.class,
		identityType=IdentityType.APPLICATION,
		detachable="true",
		table="JFireBase_Prop_DetachedPropertySet"
)
@Queries(
		@Query(
				name="getDetachedPropertySetsByPropertySetID",
				value="SELECT WHERE this.organisationID == :organisationID && this.propertySetID == :propertySetID"
		)
)
// I tested the performance with and without the following index. Even though the fields are part of the primary key,
// there is a significant difference in query performance and thus the following index is essentially necessary. Marco.
@Indices(
		@Index(
				name="organisationID_propertySetID",
				members={"organisationID", "propertySetID"}
		)
)
public class DetachedPropertySet
implements Serializable
{
	private static final long serialVersionUID = 1L;

	public static Collection<DetachedPropertySet> getDetachedPropertySets(PersistenceManager pm, PropertySetID propertySetID)
	{
		javax.jdo.Query q = pm.newNamedQuery(DetachedPropertySet.class, "getDetachedPropertySetsByPropertySetID");
		@SuppressWarnings("unchecked")
		Collection<DetachedPropertySet> result = (Collection<DetachedPropertySet>) q.execute(propertySetID.organisationID, propertySetID.propertySetID);
		result = new ArrayList<DetachedPropertySet>(result);
		q.closeAll();
		return result;
	}

	@PrimaryKey
	@Column(length=100)
	private String organisationID;

	@PrimaryKey
	private long propertySetID;

	@PrimaryKey
	private String fetchGroups_part1;

	@PrimaryKey
	private String fetchGroups_part2;

	@PrimaryKey
	private String fetchGroups_part3;

	@PrimaryKey
	private int maxFetchDepth;

	@Persistent(nullValue=NullValue.EXCEPTION)
	private byte[] propertySet;

	protected DetachedPropertySet() { }

	public DetachedPropertySet(DetachedPropertySetID detachedPropertySetID, PropertySet propertySet)
	{
		this.organisationID = detachedPropertySetID.organisationID;
		this.propertySetID = detachedPropertySetID.propertySetID;
		this.fetchGroups_part1 = detachedPropertySetID.fetchGroups_part1;
		this.fetchGroups_part2 = detachedPropertySetID.fetchGroups_part2;
		this.fetchGroups_part3 = detachedPropertySetID.fetchGroups_part3;
		this.maxFetchDepth = detachedPropertySetID.maxFetchDepth;

		this.propertySet = serialize(propertySet);
	}

	private byte[] serialize(PropertySet propertySet) {
		try {
			ByteArrayOutputStream bout = new ByteArrayOutputStream();
			ObjectOutputStream out = new ObjectOutputStream(bout);
			out.writeObject(propertySet);
			out.close();
			return bout.toByteArray();
		} catch (IOException x) {
			throw new RuntimeException(x); // should never happen since IO is RAM-only.
		}
	}

	private PropertySet deserialize(byte[] propertySetByteArray) {
		try {
			ByteArrayInputStream bin = new ByteArrayInputStream(propertySetByteArray);
			ObjectInputStream in = new ObjectInputStream(bin);
			PropertySet propertySet = (PropertySet) in.readObject();
			in.close();
			return propertySet;
		} catch (IOException x) {
			throw new RuntimeException(x); // should never happen since IO is RAM-only.
		} catch (ClassNotFoundException x) {
			throw new RuntimeException(x); // though this might happen, it's IMHO still better to wrap this exception. Marco.
		}
	}

	public String getOrganisationID() {
		return organisationID;
	}

	public long getPropertySetID() {
		return propertySetID;
	}

	public String[] getFetchGroups() {
		return new FetchGroupsPartSet(fetchGroups_part1, fetchGroups_part2, fetchGroups_part3).getFetchGroups();
	}

	public int getMaxFetchDepth() {
		return maxFetchDepth;
	}

	public PropertySet getPropertySet() {
		return deserialize(propertySet);
	}

}
