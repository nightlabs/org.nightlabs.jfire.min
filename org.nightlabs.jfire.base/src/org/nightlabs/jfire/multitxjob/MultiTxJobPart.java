package org.nightlabs.jfire.multitxjob;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import org.nightlabs.util.CollectionUtil;

import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.Queries;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.PrimaryKey;
import javax.jdo.annotations.PersistenceCapable;
import org.nightlabs.jfire.multitxjob.id.MultiTxJobPartID;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceModifier;

/**
 * Persistent data wrapper for {@link MultiTxJob}s. Each instance keeps a single
 * entity (quite unlikely) or a {@link Collection} of entities (more common) or
 * a {@link Map} of entities (more common).
 *
 * @author marco schulze - marco at nightlabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.multitxjob.id.MultiTxJobPartID"
 *		detachable="true"
 *		table="JFireBase_MultiTxJobPart"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.create-objectid-class field-order="multiTxJobID, multiTxJobPartID"
 *
 * @jdo.query name="getMultiTxJobPartsOfMultiTxJob" query="SELECT WHERE this.multiTxJobID == :multiTxJobID ORDER BY this.multiTxJobPartID ASC"
 *
 * @jdo.query name="getFirstMultiTxJobPartOfMultiTxJob" query="SELECT WHERE this.multiTxJobID == :multiTxJobID ORDER BY this.multiTxJobPartID ASC RANGE 0, 1"
 */
@PersistenceCapable(
	objectIdClass=MultiTxJobPartID.class,
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireBase_MultiTxJobPart")
@Queries({
	@javax.jdo.annotations.Query(
		name="getMultiTxJobPartsOfMultiTxJob",
		value="SELECT WHERE this.multiTxJobID == :multiTxJobID ORDER BY this.multiTxJobPartID ASC"),
	@javax.jdo.annotations.Query(
		name="getFirstMultiTxJobPartOfMultiTxJob",
		value="SELECT WHERE this.multiTxJobID == :multiTxJobID ORDER BY this.multiTxJobPartID ASC RANGE 0, 1")
})
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class MultiTxJobPart
{
	protected static Collection<? extends MultiTxJobPart> getMultiTxJobParts(PersistenceManager pm, String multiTxJobID)
	{
		Query q = pm.newNamedQuery(MultiTxJobPart.class, "getMultiTxJobPartsOfMultiTxJob");
		Collection<? extends MultiTxJobPart> c = CollectionUtil.castCollection((Collection<?>) q.execute(multiTxJobID));
		return c;
	}

	protected static MultiTxJobPart getFirstMultiTxJobPart(PersistenceManager pm, String multiTxJobID)
	{
		Query q = pm.newNamedQuery(MultiTxJobPart.class, "getFirstMultiTxJobPartOfMultiTxJob");
		Collection<? extends MultiTxJobPart> c = CollectionUtil.castCollection((Collection<?>) q.execute(multiTxJobID));
		Iterator<? extends MultiTxJobPart> it = c.iterator();
		if (it.hasNext())
			return c.iterator().next();
		else
			return null;
	}

	/**
	 * @jdo.field primary-key="true"
	 */
	@PrimaryKey
	private String multiTxJobID;

	/**
	 * @jdo.field primary-key="true"
	 */
	@PrimaryKey
	private long multiTxJobPartID;

	/**
	 * @jdo.field persistence-modifier="persistent" serialized="true"
	 */
	@Persistent(
		serialized="true",
		persistenceModifier=PersistenceModifier.PERSISTENT)
	private Object data;

	protected MultiTxJobPart() { }

	public MultiTxJobPart(String multiTxJobID, long multiTxJobPartID) {
		this.multiTxJobID = multiTxJobID;
		this.multiTxJobPartID = multiTxJobPartID;
	}

	public String getMultiTxJobID() {
		return multiTxJobID;
	}

	public long getMultiTxJobPartID() {
		return multiTxJobPartID;
	}

	public Object getData() {
		return data;
	}

	public void setData(Object data) {
		this.data = data;
	}
}
