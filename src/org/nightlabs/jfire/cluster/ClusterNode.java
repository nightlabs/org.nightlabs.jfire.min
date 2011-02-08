package org.nightlabs.jfire.cluster;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Index;
import javax.jdo.annotations.NullValue;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;
import javax.jdo.annotations.Queries;
import javax.jdo.annotations.Query;
import javax.jdo.annotations.Version;
import javax.jdo.annotations.VersionStrategy;

import org.nightlabs.jfire.cluster.id.ClusterNodeID;

@javax.jdo.annotations.PersistenceCapable(
		objectIdClass=ClusterNodeID.class,
		identityType=IdentityType.APPLICATION,
		detachable="true",
		table="JFireBase_ClusterNode"
)
@Queries({
	@Query(name="getClusterNodesWithLastHeartbeatDateBefore", value="SELECT WHERE this.lastHeartbeatDate < :lastHeartbeatDate")
})
@Version(strategy=VersionStrategy.VERSION_NUMBER)
public class ClusterNode
implements Serializable
{
	private static final long serialVersionUID = 1L;

	public static List<? extends ClusterNode> getClusterNodesWithLastHeartbeatDateBefore(PersistenceManager pm, Date lastHeartbeatDate)
	{
		javax.jdo.Query q = pm.newNamedQuery(ClusterNode.class, "getClusterNodesWithLastHeartbeatDateBefore");
		@SuppressWarnings("unchecked")
		List<? extends ClusterNode> result = (List<? extends ClusterNode>) q.execute(lastHeartbeatDate);
		result = new ArrayList<ClusterNode>(result);
		q.closeAll();
		return result;
	}

	public static ClusterNode getClusterNode(PersistenceManager pm, UUID clusterNodeID)
	{
		pm.getExtent(ClusterNode.class);
		try {
			return (ClusterNode) pm.getObjectById(ClusterNodeID.create(clusterNodeID));
		} catch (JDOObjectNotFoundException x) {
			return null;
		}
	}

	public static ClusterNode createClusterNode(PersistenceManager pm, UUID clusterNodeID)
	{
		pm.getExtent(ClusterNode.class);
		try {
			return (ClusterNode) pm.getObjectById(ClusterNodeID.create(clusterNodeID));
		} catch (JDOObjectNotFoundException x) {
			return pm.makePersistent(new ClusterNode(clusterNodeID));
		}
	}

	@PrimaryKey
	private UUID clusterNodeID;

	@Persistent(nullValue=NullValue.EXCEPTION)
	@Index(name="idxLastHeartbeatDate", unique="false")
	private Date lastHeartbeatDate;

	protected ClusterNode() { }

	public ClusterNode(UUID clusterNodeID)
	{
		if (clusterNodeID == null)
			throw new IllegalArgumentException("clusterNodeID == null");

		this.clusterNodeID = clusterNodeID;
		this.lastHeartbeatDate = new Date();
	}

	public UUID getClusterNodeID() {
		return clusterNodeID;
	}

	public Date getLastHeartbeatDate() {
		return lastHeartbeatDate;
	}

	public void setLastHeartbeatDate(Date lastHeartbeatDate) {
		this.lastHeartbeatDate = lastHeartbeatDate;
	}
}
