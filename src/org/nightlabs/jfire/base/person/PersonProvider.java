/* *****************************************************************************
 * JFire - it's hot - Free ERP System - http://jfire.org                       *
 * Copyright (C) 2004-2005 NightLabs - http://NightLabs.org                    *
 *                                                                             *
 * This library is free software; you can redistribute it and/or               *
 * modify it under the terms of the GNU Lesser General Public                  *
 * License as published by the Free Software Foundation; either                *
 * version 2.1 of the License, or (at your option) any later version.          *
 *                                                                             *
 * This library is distributed in the hope that it will be useful,             *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of              *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU           *
 * Lesser General Public License for more details.                             *
 *                                                                             *
 * You should have received a copy of the GNU Lesser General Public            *
 * License along with this library; if not, write to the                       *
 *     Free Software Foundation, Inc.,                                         *
 *     51 Franklin St, Fifth Floor,                                            *
 *     Boston, MA  02110-1301  USA                                             *
 *                                                                             *
 * Or get it online :                                                          *
 *     http://opensource.org/licenses/lgpl-license.php                         *
 *                                                                             *
 *                                                                             *
 ******************************************************************************/

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
