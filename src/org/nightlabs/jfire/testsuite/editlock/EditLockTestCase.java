package org.nightlabs.jfire.testsuite.editlock;

import java.util.Collection;
import java.util.Set;

import javax.jdo.FetchPlan;
import javax.jdo.JDOHelper;
import javax.security.auth.login.LoginException;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.base.JFireBaseEAR;
import org.nightlabs.jfire.base.JFireEjb3Factory;
import org.nightlabs.jfire.base.login.JFireLogin;
import org.nightlabs.jfire.editlock.AcquireEditLockResult;
import org.nightlabs.jfire.editlock.EditLock;
import org.nightlabs.jfire.editlock.EditLockManagerRemote;
import org.nightlabs.jfire.editlock.ReleaseReason;
import org.nightlabs.jfire.editlock.id.EditLockID;
import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.person.Person;
import org.nightlabs.jfire.person.PersonStruct;
import org.nightlabs.jfire.prop.IStruct;
import org.nightlabs.jfire.prop.PropertyManagerRemote;
import org.nightlabs.jfire.prop.PropertySet;
import org.nightlabs.jfire.prop.id.PropertySetID;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.jfire.testsuite.JFireTestSuite;


/**
 * 
 * this Test Case tests all the possible API used in the EditLockManager. 
 * 
 * the following EJB methods has been tested and Called
 * 
 * 	AcquireEditLockResult acquireEditLock(EditLockTypeID editLockTypeID,
 *			ObjectID objectID, String description, String[] fetchGroups,
 *			int maxFetchDepth);
 *
 *	void releaseEditLock(ObjectID objectID, ReleaseReason releaseReason);
 *
 *	Set<EditLockID> getEditLockIDs(ObjectID objectID);
 *
 * 	List<EditLock> getEditLocks(Collection<EditLockID> editLockIDs,
 *			String[] fetchGroups, int maxFetchDepth);
 * 
 * 
 *  @author Fitas Amine - fitas [at] nightlabs [dot] de
 */
@JFireTestSuite(JFireBaseEditLockTestSuite.class)
public class EditLockTestCase extends TestCase{
	Logger logger = Logger.getLogger(EditLockTestCase.class);	
	private static ThreadLocal<PropertySetID> newObjectforEditLock = new ThreadLocal<PropertySetID>();
	private static String[] FETCH_GROUP_EDITLOCK = new String[]{
		FetchPlan.DEFAULT,
		EditLock.FETCH_GROUP_LOCK_OWNER_USER, 
		EditLock.FETCH_GROUP_EDIT_LOCK_TYPE	
	};	

	@Override
	protected void setUp()throws Exception
	{
		super.setUp();
		// create a dummy Object to Test Edit Locking
		PropertySetID ObjectEditLock = createDemoObjectForEditLocking();	
		EditLockManagerRemote elm = JFireEjb3Factory.getRemoteBean(EditLockManagerRemote.class, SecurityReflector.getInitialContextProperties());
		newObjectforEditLock.set(ObjectEditLock);
		elm.acquireEditLock(JFireBaseEAR.EDIT_LOCK_TYPE_ID_CONFIG,
				ObjectEditLock, 
				"testEditLock-1", 
				FETCH_GROUP_EDITLOCK, 
				NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT);	
	}
	@Test
	public void testDetectEditLockCollision() throws Exception{	
		{
			if (newObjectforEditLock.get() == null) {
				fail("Seems that creating the dummy EditLock Object failed, no PropertySetID in the ThreadLocal");
			}
			// login with another User to detect the Collision.
			JFireLogin login = new JFireLogin("chezfrancois.jfire.org", "francois", "test");
			try {
				login.login();
				EditLockManagerRemote elm = JFireEjb3Factory.getRemoteBean(EditLockManagerRemote.class, SecurityReflector.getInitialContextProperties());
				AcquireEditLockResult acquireEditLockResult = elm.acquireEditLock(JFireBaseEAR.EDIT_LOCK_TYPE_ID_CONFIG,
						newObjectforEditLock.get(), 
						"testEditLock-francois", 
						new String[] {FetchPlan.DEFAULT, 
					EditLock.FETCH_GROUP_LOCK_OWNER_USER, 
					EditLock.FETCH_GROUP_EDIT_LOCK_TYPE}, 
					NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT);	
				if(acquireEditLockResult.getEditLockCount()!= 2)
					fail("the Object should have been locked already !!!!");				
				Set<EditLockID> editLockIDs =  elm.getEditLockIDs(newObjectforEditLock.get());
				logger.info("our Object Got EditLocks" + editLockIDs.size());
				Collection<EditLock> editLocks = elm.getEditLocks(editLockIDs, 
						FETCH_GROUP_EDITLOCK, 
						NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT);
				// list all EditLocks by other Users
				logger.info("the following locks has been found !!!");
				for (EditLock editLock : editLocks) {
					logger.info("Description :" + editLock.getDescription());
					logger.info("Time Stamp Creation :" + editLock.getCreateDT().getTime());
					logger.info("User :" + editLock.getLockOwnerUser().getName());
					logger.info("Locked-Object-ID :" + editLock.getLockedObjectIDStr());
					logger.info("-------------------------------------------");
				}
			}catch (LoginException e) {
				fail("couldnt log with another User Account to test the EditLock!!!!");		
			}
			login.logout();
		}
	}
	private PropertySetID createDemoObjectForEditLocking() throws Exception{	
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
	@Override
	protected void tearDown()throws Exception
	{
		super.tearDown();
		EditLockManagerRemote elm = JFireEjb3Factory.getRemoteBean(EditLockManagerRemote.class, SecurityReflector.getInitialContextProperties());
		elm.releaseEditLock(newObjectforEditLock.get(), ReleaseReason.normal);
	}
}



