package org.nightlabs.jfire.testsuite.editlock;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import javax.jdo.FetchPlan;
import javax.jdo.JDOHelper;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jdo.query.QueryCollection;
import org.nightlabs.jfire.base.JFireBaseEAR;
import org.nightlabs.jfire.base.JFireEjb3Factory;
import org.nightlabs.jfire.editlock.AcquireEditLockResult;
import org.nightlabs.jfire.editlock.EditLock;
import org.nightlabs.jfire.editlock.EditLockManagerRemote;
import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.person.Person;
import org.nightlabs.jfire.person.PersonStruct;
import org.nightlabs.jfire.prop.IStruct;
import org.nightlabs.jfire.prop.PropertyManagerRemote;
import org.nightlabs.jfire.prop.PropertySet;
import org.nightlabs.jfire.prop.exception.DataBlockGroupNotFoundException;
import org.nightlabs.jfire.prop.exception.DataBlockNotFoundException;
import org.nightlabs.jfire.prop.exception.DataFieldNotFoundException;
import org.nightlabs.jfire.prop.id.PropertySetID;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.jfire.testsuite.JFireTestSuite;
import org.nightlabs.jfire.workstation.Workstation;
import org.nightlabs.jfire.workstation.WorkstationManagerRemote;
import org.nightlabs.jfire.workstation.id.WorkstationID;
import org.nightlabs.jfire.workstation.search.WorkstationQuery;



@JFireTestSuite(JFireBaseEditLockTestSuite.class)
public class EditLockTestCase extends TestCase{

	Logger logger = Logger.getLogger(EditLockTestCase.class);	

	@Test
	public void testEditLock() throws Exception{	


		// create a dummy person and link it to a new Issue
		PropertyManagerRemote pm = JFireEjb3Factory.getRemoteBean(PropertyManagerRemote.class, SecurityReflector.getInitialContextProperties());

		PropertySetID ObjectEditLock = creatDemoObjectforLock();	
		EditLockManagerRemote wm = JFireEjb3Factory.getRemoteBean(EditLockManagerRemote.class, SecurityReflector.getInitialContextProperties());
		WorkstationManagerRemote ws = JFireEjb3Factory.getRemoteBean(WorkstationManagerRemote.class, SecurityReflector.getInitialContextProperties());


		AcquireEditLockResult acquireEditLockResult = wm.acquireEditLock(JFireBaseEAR.EDIT_LOCK_TYPE_ID_CONFIG,
				ObjectEditLock, 
				"testLock", 
				new String[] {FetchPlan.DEFAULT, 
				EditLock.FETCH_GROUP_LOCK_OWNER_USER, 
				EditLock.FETCH_GROUP_EDIT_LOCK_TYPE}, 
				NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT);


		WorkstationID randomWsID = getRandomWorkstation();

		acquireEditLockResult = wm.acquireEditLock(JFireBaseEAR.EDIT_LOCK_TYPE_ID_CONFIG,
				randomWsID, 
				"testLock", 
				new String[] {FetchPlan.DEFAULT, 
				EditLock.FETCH_GROUP_LOCK_OWNER_USER, 
				EditLock.FETCH_GROUP_EDIT_LOCK_TYPE}, 
				NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT);


		Workstation wsObject = ws.getWorkstations(Collections.singleton(randomWsID), 
				new String[] {FetchPlan.DEFAULT}, NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT).iterator().next();

		wsObject.setDescription("Edit the workstation");

		ws.storeWorkstation(wsObject,false, null, -1);

		Person person = (Person) pm.getPropertySet(ObjectEditLock, 
				new String[] {FetchPlan.DEFAULT, Person.FETCH_GROUP_FULL_DATA, Person.FETCH_GROUP_DATA_FIELDS}, 
				NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT);

		IStruct personStruct  = pm.getFullStructLocal(person.getStructLocalObjectID(), 
				new String[] {FetchPlan.DEFAULT, IStruct.FETCH_GROUP_ISTRUCT_FULL_DATA}, 
				NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT);
		person.inflate(personStruct);		
		person.getDataField(PersonStruct.PERSONALDATA_NAME).setData("Modified Edit Lock");
		person.deflate();

		person = (Person) pm.storePropertySet(person, true, null, NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT);

	}


	private WorkstationID getRandomWorkstation(){	
		WorkstationManagerRemote wm = JFireEjb3Factory.getRemoteBean(WorkstationManagerRemote.class, SecurityReflector.getInitialContextProperties());

		final QueryCollection<WorkstationQuery> queries =new QueryCollection<WorkstationQuery>(Workstation.class);
		WorkstationQuery WorkstationQuery = new WorkstationQuery();
		queries.add(WorkstationQuery);
		List<WorkstationID> workstationIDs = new ArrayList<WorkstationID>( wm.getWorkstationIDs(queries));
		Random rndGen = new Random(System.currentTimeMillis());		

		if (workstationIDs != null && !workstationIDs.isEmpty()) {
			return workstationIDs.get(rndGen.nextInt(workstationIDs.size()));	
		}

		return null;
	}	

	private PropertySetID creatDemoObjectforLock() throws DataBlockNotFoundException, DataBlockGroupNotFoundException, DataFieldNotFoundException{	
		logger.info("test Create IssueLink: begin");

		// create a dummy person and link it to a new Issue
		PropertyManagerRemote pm = JFireEjb3Factory.getRemoteBean(PropertyManagerRemote.class, SecurityReflector.getInitialContextProperties());

		Person person = new Person(IDGenerator.getOrganisationID(), IDGenerator.nextID(PropertySet.class));		
		IStruct personStruct  = pm.getFullStructLocal(person.getStructLocalObjectID(), 
				new String[] {FetchPlan.DEFAULT, IStruct.FETCH_GROUP_ISTRUCT_FULL_DATA}, 
				NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT);
		person.inflate(personStruct);
		person.getDataField(PersonStruct.PERSONALDATA_NAME).setData("Demo Edit Lock");
		person.getDataField(PersonStruct.PERSONALDATA_FIRSTNAME).setData("Demo Edit Lock");
		person.setAutoGenerateDisplayName(true);
		person.setDisplayName(null, personStruct);
		person.deflate();

		person = (Person) pm.storePropertySet(person, true, null, NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT);
		return (PropertySetID)JDOHelper.getObjectId(person);


	}
}



