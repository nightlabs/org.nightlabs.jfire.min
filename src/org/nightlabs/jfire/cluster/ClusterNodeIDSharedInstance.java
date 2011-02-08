package org.nightlabs.jfire.cluster;

import java.util.UUID;

public class ClusterNodeIDSharedInstance
{
	private static final UUID clusterNodeID = UUID.randomUUID();

	public static UUID getClusterNodeID() {
		return clusterNodeID;
	}
}
