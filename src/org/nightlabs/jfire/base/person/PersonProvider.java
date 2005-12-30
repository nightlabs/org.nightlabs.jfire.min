/*
 * Created 	on Sep 8, 2005
 * 					by alex
 *
 */
package org.nightlabs.jfire.base.person;

import java.util.Collection;
import java.util.Iterator;

import org.nightlabs.jfire.base.jdo.JDOObjectProvider;
import org.nightlabs.jfire.base.login.Login;
import org.nightlabs.jfire.person.Person;
import org.nightlabs.jfire.person.PersonManager;
import org.nightlabs.jfire.person.PersonManagerUtil;
import org.nightlabs.jfire.person.PersonStruct;
import org.nightlabs.jfire.person.id.PersonID;

/**
 * 
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 */
public class PersonProvider extends JDOObjectProvider {

	public PersonProvider() {
		super();
	}

	/**
	 * Retrieves and explodes a Person before putting it into the Cache.
	 * 
	 * @see org.nightlabs.jfire.base.jdo.JDOObjectProvider#retrieveJDOObject(java.lang.String, java.lang.Object, java.lang.String[])
	 */
	protected Object retrieveJDOObject(String scope, Object objectID, String[] fetchGroups) throws Exception {
		PersonManager personManager = PersonManagerUtil.getHome(Login.getLogin().getInitialContextProperties()).create();
		Person person = personManager.getPerson((PersonID)objectID, fetchGroups);
		PersonStructProvider.sharedInstance().getPersonStruct().explodePerson(person);
		return person;
	}

	/**
	 * Retrieves and explodes the given 
	 * @see org.nightlabs.jfire.base.jdo.JDOObjectProvider#retrieveJDOObjects(java.lang.String, java.lang.Object[], java.lang.String[])
	 */
	protected Collection retrieveJDOObjects(String scope, Object[] objectIDs, String[] fetchGroups) throws Exception {
		PersonManager personManager = PersonManagerUtil.getHome(Login.getLogin().getInitialContextProperties()).create();
		Collection persons = personManager.getPersons(objectIDs, fetchGroups);
		PersonStruct personStruct = PersonStructProvider.sharedInstance().getPersonStruct();
		for (Iterator iter = persons.iterator(); iter.hasNext();) {
			personStruct.explodePerson((Person)iter.next());
		}
		return persons; 
	}
	
	/**
	 * Returns an expoloded Person from Cache.
	 * 
	 * @param personID The PersonID of the desired Person
	 * @param fetchGroups The FetchGroups the Person should be detached with
	 * @return An exploded Person.
	 */
	public Person getPerson(PersonID personID, String[] fetchGroups) {
		return (Person)getJDOObject(null, personID, fetchGroups);
	}
	
	/**
	 * Returns 
	 * @param personIDs
	 * @param fetchGroups
	 * @return
	 */
	public Collection getPersons(Object[] personIDs, String[] fetchGroups) {
		return getJDOObjects(null, personIDs, fetchGroups);
	}

	
	private static PersonProvider sharedInstance;
	
	public static PersonProvider sharedInstane() {
		if (sharedInstance == null)
			sharedInstance = new PersonProvider();
		return sharedInstance;
	}

}
