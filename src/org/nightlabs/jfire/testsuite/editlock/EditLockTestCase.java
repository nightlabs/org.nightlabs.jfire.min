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
 * this Test Case tests all the APIs used in the EditLockManager Bean. 
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
	public static final String USER_ID = "francois";
	public static final String USER_PASSWORD = "test";
	public static final String ORGANISATION = "chezfrancois.jfire.org";
	private static String[] FETCH_GROUP_EDITLOCK = new String[]{
		FetchPlan.DEFAULT,
		EditLock.FETCH_GROUP_LOCK_OWNER_USER, 
		EditLock.FETCH_GROUP_EDIT_LOCK_TYPE	
	};	

	@Override
	protected void setUp()throws Exception
	{
		super.setUp();
		// create a dummy Object to Test EditLocking on it
		PropertySetID ObjectEditLock = createDemoObjectForEditLocking();	
		newObjectforEditLock.set(ObjectEditLock);
	}

	@Test
	public void testUserEditLockingAndReleasing() throws Exception{	
		if (newObjectforEditLock.get() == null) {
			fail("Seems that creating the dummy EditLock Object failed, no PropertySetID in the ThreadLocal");
		}
		EditLockManagerRemote elm = JFireEjb3Factory.getRemoteBean(EditLockManagerRemote.class, SecurityReflector.getInitialContextProperties());
		// lock and release the Object Multiple times
		for (int i = 0; i < 20; ++i) {
			elm.acquireEditLock(JFireBaseEAR.EDIT_LOCK_TYPE_ID_CONFIG,
					newObjectforEditLock.get(), 
					"testEditLock-1", 
					FETCH_GROUP_EDITLOCK, 
					NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT);	
			elm.releaseEditLock(newObjectforEditLock.get(), ReleaseReason.normal);	
		}
		// login with another user and check if a lock is still placed on the Object 
		JFireLogin login = new JFireLogin(ORGANISATION, USER_ID, USER_PASSWORD);
		try {
			login.login();
			Set<EditLockID> editLockIDs = elm.getEditLockIDs(newObjectforEditLock.get());
			if(editLockIDs.size() > 0)
				fail("no Locks should exist on the the Object!!!!");				
			elm.releaseEditLock(newObjectforEditLock.get(), ReleaseReason.normal);			
		}catch (LoginException e) {
			fail("couldnt login with another User Account to test the EditLock!!!!");		
		}
		login.logout();
	}
	

	@Test
	public void testDetectEditLockCollision() throws Exception{	
			if (newObjectforEditLock.get() == null) {
				fail("Seems that creating the dummy EditLock Object failed, no PropertySetID in the ThreadLocal");
			}
			EditLockManagerRemote elm = JFireEjb3Factory.getRemoteBean(EditLockManagerRemote.class, SecurityReflector.getInitialContextProperties());
			elm.acquireEditLock(JFireBaseEAR.EDIT_LOCK_TYPE_ID_CONFIG,
					newObjectforEditLock.get(), 
					"testEditLock-1", 
					FETCH_GROUP_EDITLOCK, 
					NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT);	
			// login with another User to detect the editLock Collision.
			JFireLogin login = new JFireLogin(ORGANISATION, USER_ID, USER_PASSWORD);
			try {
				login.login();
				AcquireEditLockResult acquireEditLockResult = elm.acquireEditLock(JFireBaseEAR.EDIT_LOCK_TYPE_ID_CONFIG,
						newObjectforEditLock.get(), 
						"testEditLock-francois", 
						new String[] {FetchPlan.DEFAULT, 
					EditLock.FETCH_GROUP_LOCK_OWNER_USER, 
					EditLock.FETCH_GROUP_EDIT_LOCK_TYPE}, 
					NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT);	
				if(acquireEditLockResult.getEditLockCount()!= 2)
					fail("the Object should have been locked already !!!!");				
				Set<EditLockID> editLockIDs = elm.getEditLockIDs(newObjectforEditLock.get());
				logger.info("our new Object Got EditLocks:" + editLockIDs.size());
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
				elm.releaseEditLock(newObjectforEditLock.get(), ReleaseReason.normal);
			}catch (LoginException e) {
				fail("couldnt login with another User Account to test the EditLock!!!!");		
			}
			login.logout();
		}
	
	private PropertySetID createDemoObjectForEditLocking() throws Exception{	
		// create a dummy Object for Locking
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
	}
}
