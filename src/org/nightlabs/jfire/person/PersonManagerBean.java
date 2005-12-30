/*
 * Created on 02.03.2004
 */
package org.nightlabs.jfire.person;

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.jdo.FetchPlan;
import javax.jdo.JDOHelper;
import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import org.apache.log4j.Logger;
import org.nightlabs.jfire.base.BaseSessionBeanImpl;
import org.nightlabs.jfire.person.Person;
import org.nightlabs.jfire.person.PersonRegistry;
import org.nightlabs.jfire.person.PersonStruct;
import org.nightlabs.jfire.person.id.PersonID;
import org.nightlabs.jfire.person.util.PersonSearchFilter;

import org.nightlabs.ModuleException;
import org.nightlabs.jdo.NLJDOHelper;

/**
 * @author alex
 * @author nick
 * @author marco
 */

/**
 * @ejb.bean
 *		name="jfire/ejb/JFireBaseBean/PersonManager"
 *		jndi-name="jfire/ejb/JFireBaseBean/PersonManager"
 *		type="Stateless"
 * 
 * @ejb.util generate = "physical"
 */
public abstract class PersonManagerBean extends BaseSessionBeanImpl implements
		SessionBean {
	private static final Logger LOGGER = Logger.getLogger(PersonManagerBean.class);

	/**
	 * @see org.nightlabs.jfire.base.BaseSessionBeanImpl#setSessionContext(javax.ejb.SessionContext)
	 */
	public void setSessionContext(SessionContext sessionContext)
			throws EJBException, RemoteException {
		super.setSessionContext(sessionContext);
	}

	/**
	 * @ejb.create-method
	 * @ejb.permission role-name="PersonManager-read"
	 */
	public void ejbCreate() throws CreateException {
//		try {
//			System.out.println("PersonManagerBean created by "
//					+ this.getPrincipalString());
//		} catch (ModuleException e) {
//			throw new CreateException(e.getMessage());
//		}
	}

	/**
	 * @see javax.ejb.SessionBean#ejbRemove()
	 * 
	 * @ejb.permission unchecked="true"
	 */
	public void ejbRemove() throws EJBException, RemoteException {
	}

	/**
	 * Detaches and returns the complete PersonStruct.
	 * 
	 * @ejb.interface-method
	 * @ejb.permission role-name="PersonManager-read"
	 * @ejb.transaction type = "Required"
	 */
	public PersonStruct getFullPersonStructure() throws ModuleException {
		PersistenceManager pm = getPersistenceManager();
		try {
			pm.getFetchPlan().addGroup(FetchPlan.ALL);
			PersonStruct ps = PersonStruct.getPersonStruct(getOrganisationID(), pm);
			PersonStruct result = (PersonStruct) pm.detachCopy(ps);
			return result;

		} finally {
			pm.close();
		}
	}
	
	/**
	 * @throws ModuleException
	 * 
	 * @ejb.interface-method
	 * @ejb.permission role-name="PersonManager-write"
	 * @ejb.transaction type = "Required"
	 */
	public long createPersonID() throws ModuleException {
		PersistenceManager pm = this.getPersistenceManager();
		try {
			long nextPersonID = PersonRegistry.getRegistry(pm).createPersonID();
			LOGGER.info("createPersonID() returning "+nextPersonID+" as next PersonID");
			return nextPersonID;
		} 
		finally {
			pm.close();
		}
	}

	/**
	 * Retrieve the person with the given ID
	 * 
	 * @throws ModuleException
	 * @ejb.interface-method
	 * @ejb.permission role-name="PersonManager-read"
	 * @ejb.transaction type = "Required"
	 */
	public Person getPerson(PersonID personID, String[] fetchGroups)
			throws ModuleException, JDOObjectNotFoundException {
		PersistenceManager pm = this.getPersistenceManager();
		pm.getExtent(Person.class, true);
		Person person = (Person) pm.getObjectById(personID, true);

		if (fetchGroups != null) {
			for (int i = 0; i < fetchGroups.length; i++) {
				pm.getFetchPlan().addGroup(fetchGroups[i]);
			}
		}

		Person result = (Person) pm.detachCopy(person);
		pm.close();

		return result;
	}

	/**
	 * Retrieve the person with the given ID
	 * 
	 * @throws ModuleException
	 * @ejb.interface-method
	 * @ejb.permission role-name="PersonManager-read"
	 * @ejb.transaction type = "Required"
	 */
	public Person getPerson(PersonID personID) throws ModuleException,
			JDOObjectNotFoundException {
		PersistenceManager pm = this.getPersistenceManager();
		try {
			Object o = pm.getObjectById(personID, true);
			long startTime = System.currentTimeMillis();
//			pm.getFetchPlan().resetGroups();
			pm.getFetchPlan().addGroup(FetchPlan.ALL);
			Person ret = (Person) pm.detachCopy(o);
			return ret;
		} finally {
			pm.close();
		}
		
	}

	/**
	 * 
	 * Retrieve a list Objects searchable by PersonSearchFilter.
	 * 
	 * @throws ModuleException
	 * @ejb.interface-method
	 * @ejb.permission role-name="PersonManager-read"
	 * @ejb.transaction type = "Required"
	 */
	public Collection searchPerson(PersonSearchFilter personSearchFilter, String[] fetchGroups)
			throws ModuleException, JDOObjectNotFoundException {
		
		PersistenceManager pm = this.getPersistenceManager();
		try {
			Collection persons = personSearchFilter.executeQuery(pm);
			
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);
			
			Collection result = pm.detachCopyAll(persons);
			
			return result;
		} finally {
			pm.close();
		}
	}
	
	/**
	 * 
	 * Retrieve a list of of ObjectIDs of Objects searchable by a
	 * PersonSearchFilter
	 * 
	 * @throws ModuleException
	 * @ejb.interface-method
	 * @ejb.permission role-name="PersonManager-read"
	 * @ejb.transaction type = "Required"
	 */
	public Collection searchPerson(PersonSearchFilter personSearchFilter)
			throws ModuleException, JDOObjectNotFoundException {
		PersistenceManager pm = this.getPersistenceManager();
		try {
			Collection persons = personSearchFilter.executeQuery(pm);
			Collection result = new LinkedList();
			for (Iterator iter = persons.iterator(); iter.hasNext();) {
				result.add(JDOHelper.getObjectId(iter.next()));
			}
			return result;
		} finally {
			pm.close();
		}
	}
	
	/**
	 * Store a person either detached or not 
	 * made persistent yet.
	 * 
	 * @throws ModuleException
	 * 
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type = "Required"
	 */
	public Person storePerson(Person person, boolean get, String[] fetchGroups)
	throws ModuleException
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			return (Person) NLJDOHelper.storeJDO(pm, person, get, fetchGroups);
		} finally {
			pm.close();
		}
	}
	
	/**
	 * Get a Collection all Persons
	 * 
	 * @throws ModuleException
	 * 
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type = "Required"
	 */
	public Collection getPersons(String[] fetchGroups) throws ModuleException {
//		MultiPageSearchResult multiPageSearchResult = new MultiPageSearchResult();
		PersistenceManager pm = getPersistenceManager();
		try {
			
			Query query = pm.newQuery(Person.class);
			
			Collection elements = (Collection)query.execute();
			for (int i=0; i<fetchGroups.length; i++)
				pm.getFetchPlan().addGroup(fetchGroups[i]);
			
			long time = System.currentTimeMillis();
			Collection result = pm.detachCopyAll(elements);
			time = System.currentTimeMillis() - time;
			LOGGER.info("Detach of "+result.size()+" Persons took "+((double)time / (double)1000));
			return result;
		}
		finally {
			pm.close();
		}
	}	
	
	
	/**
	 * Returns all persons for the given personIDs, detached with the given fetchGroups
	 * 
	 * 
	 * @throws ModuleException
	 * 
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type = "Required"
	 */
	public Collection getPersons(Object[] personIDs, String[] fetchGroups) throws ModuleException {
//		MultiPageSearchResult multiPageSearchResult = new MultiPageSearchResult();
		PersistenceManager pm = getPersistenceManager();
		try {
			Collection persons = new LinkedList();
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);
			
			for (int i = 0; i < personIDs.length; i++) {
				if (!(personIDs[i] instanceof PersonID))
					throw new IllegalArgumentException("personIDs["+i+" is not of type PersonID");
				persons.add(pm.getObjectById(personIDs[i]));
			}
			
			long time = System.currentTimeMillis();
			Collection result = pm.detachCopyAll(persons);
			time = System.currentTimeMillis() - time;
			LOGGER.debug("Detach of "+result.size()+" Persons took "+((double)time / (double)1000));
			return result;
		}
		finally {
			pm.close();
		}
	}	
	
}