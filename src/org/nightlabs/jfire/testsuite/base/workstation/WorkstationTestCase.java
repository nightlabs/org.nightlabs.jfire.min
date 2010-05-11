package org.nightlabs.jfire.testsuite.base.workstation;

import java.util.Collection;
import java.util.Set;

import javax.jdo.FetchPlan;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jdo.query.QueryCollection;
import org.nightlabs.jfire.base.JFireEjb3Factory;
import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.jfire.testsuite.JFireTestSuite;
import org.nightlabs.jfire.workstation.Workstation;
import org.nightlabs.jfire.workstation.WorkstationManagerRemote;
import org.nightlabs.jfire.workstation.id.WorkstationID;
import org.nightlabs.jfire.workstation.search.WorkstationQuery;


/**
 *
 * @author Fitas Amine - fitas [at] nightlabs [dot] de
 *
 */
@JFireTestSuite(JFireBaseWorkstationTestSuite.class)
public class WorkstationTestCase extends TestCase{

	Logger logger = Logger.getLogger(WorkstationTestCase.class);	
	
	//	WorkstationManagerRemote wm = JFireEjb3Factory.getRemoteBean(WorkstationManagerRemote.class, SecurityReflector.getInitialContextProperties());
	//	Workstation res = wm.storeWorkstation(workstation, get, fetchGroups, maxFetchDepth);
	@Test
	public void testCreateWorkstation() throws Exception{	
		
		WorkstationManagerRemote wm = JFireEjb3Factory.getRemoteBean(WorkstationManagerRemote.class, SecurityReflector.getInitialContextProperties());
		logger.info("CreateWorkstation: begin");	
		long ID = IDGenerator.nextID(Workstation.class);
		String workID = "Workstation" + String.valueOf(ID);
		WorkstationID workstationID = WorkstationID.create(SecurityReflector.getUserDescriptor().getOrganisationID(), workID);
		Workstation workstation = new Workstation(workstationID.organisationID, workstationID.workstationID);
		workstation.setDescription("Description"+ String.valueOf(ID));
//		WorkstationManagerRemote workstationManager = JFireEjb3Factory.getRemoteBean(WorkstationManagerRemote.class, Login.getLogin().getInitialContextProperties());		
		Workstation res = wm.storeWorkstation(workstation,false, null, -1);
		logger.info("CreateWorkstation: begin");		
	}

	@Test
	public void testListWorkstations() throws Exception{	

		logger.info("ListWorkstations: begin");
		WorkstationManagerRemote wm = JFireEjb3Factory.getRemoteBean(WorkstationManagerRemote.class, SecurityReflector.getInitialContextProperties());
	
		final QueryCollection<WorkstationQuery> queries =new QueryCollection<WorkstationQuery>(Workstation.class);
		WorkstationQuery WorkstationQuery = new WorkstationQuery();
		queries.add(WorkstationQuery);
		Set<WorkstationID> workstationIDs = wm.getWorkstationIDs(queries);

		if (workstationIDs != null && !workstationIDs.isEmpty()) {
			Collection<Workstation> workstations = wm.getWorkstations(workstationIDs, new String[] {FetchPlan.DEFAULT}, NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT);
			if (workstations.isEmpty())
				fail("No Workstations was found!!!");
			logger.info("the following Workstations found with");
			for (Workstation workstation : workstations) {
				logger.info("Workstations = "+ workstation.getWorkstationID());
			}
		}
		logger.info("ListWorkstations: end");
	}	
}
