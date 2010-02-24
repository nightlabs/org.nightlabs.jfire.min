package org.nightlabs.jfire.testsuite;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.PersistenceModifier;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import org.nightlabs.jdo.ObjectID;
import org.nightlabs.jfire.testsuite.id.TestCaseObjectsMapID;


/**
 * @author Fitas Amine - fitas at nightlabs dot de
 *
 * a Utility class used to store ObjectIDs of some Objects created during the running of Testcases.
 *
 */
@PersistenceCapable(
		objectIdClass=TestCaseObjectsMapID.class,
		identityType=IdentityType.APPLICATION,
		detachable="true",
		table="JFireTestSuite_ObjectsMap")

@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class TestCaseObjectsMap implements Serializable{

	/**
	 * The serial version of this class.
	 */
	private static final long serialVersionUID = 1L;
	
	@PrimaryKey
	@Column(length=100)
	private String organisationID;

	
	@PrimaryKey
	@Column(length=100)
	private String testCaseObjectsMapID;
	


	@Persistent(serialized="true",
	defaultFetchGroup="true",
	persistenceModifier=PersistenceModifier.PERSISTENT)
	private  Map<String, ObjectID>  objectIDsMap;
	
	
	public TestCaseObjectsMap(String organisationID, String objectsMapID)
	{
		if (organisationID == null)
			throw new NullPointerException("organisationID");

		if (objectsMapID == null)
			throw new NullPointerException("locationID");

		this.organisationID = organisationID;
		this.testCaseObjectsMapID = objectsMapID;
		this.objectIDsMap = new HashMap<String, ObjectID>();
	}

	
	public String getOrganisationID()
	{
		return organisationID;
	}
	public String getObjectsMapID()
	{
		return testCaseObjectsMapID;
	}
	
	
	public ObjectID getObjectID(String key)
	{	
		return objectIDsMap.get(key);
	}
	
	public void addObjectID(String key, ObjectID objectID)
	{
		objectIDsMap.put(key, objectID);
	}

	
	public Boolean isEmpty()
	{
		return objectIDsMap.isEmpty();
	}
}
