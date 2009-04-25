package org.nightlabs.jfire.workstation;

import java.util.List;
import java.util.Set;

import org.nightlabs.jdo.query.QueryCollection;
import org.nightlabs.jfire.workstation.id.WorkstationID;
import org.nightlabs.jfire.workstation.search.WorkstationQuery;

public interface WorkstationManagerRemote 
{
	String ping(String message);

	Workstation storeWorkstation(Workstation ws, boolean get,
			String[] fetchGroups, int maxFetchDepth);

	Set<WorkstationID> getWorkstationIDs();

	List<Workstation> getWorkstations(Set<WorkstationID> workstationIDs,
			String[] fetchGroups, int maxFetchDepth);

	Set<WorkstationID> getWorkstationIDs(
			QueryCollection<? extends WorkstationQuery> workstationQueries);
}