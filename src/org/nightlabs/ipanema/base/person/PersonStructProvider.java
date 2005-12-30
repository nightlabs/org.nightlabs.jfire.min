/*
 * Created 	on Sep 9, 2005
 * 					by alex
 *
 */
package org.nightlabs.ipanema.base.person;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.security.auth.login.LoginException;

import org.nightlabs.ipanema.base.jdo.JDOObjectProvider;
import org.nightlabs.ipanema.base.login.Login;
import org.nightlabs.ipanema.base.person.preferences.PersonStructOrderConfigModule;
import org.nightlabs.ipanema.person.PersonManager;
import org.nightlabs.ipanema.person.PersonManagerUtil;
import org.nightlabs.ipanema.person.PersonStruct;
import org.nightlabs.ipanema.person.PersonStructBlock;
import org.nightlabs.ipanema.person.id.PersonStructBlockID;
import org.nightlabs.ipanema.person.id.PersonStructID;

/**
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 *
 */
public class PersonStructProvider extends JDOObjectProvider {

	protected static final String[] dummyFetchGroups = new String[] {};
	
	private List orderedPersonStructBlocks;
	
	/**
	 * 
	 */
	public PersonStructProvider() {
		super();
	}

	/**
	 * Retrieves the PersonStruct and creates the orderedPersonStructBlocks
	 * list.
	 * 
	 * @see org.nightlabs.ipanema.base.jdo.JDOObjectProvider#retrieveJDOObject(java.lang.String, java.lang.Object, java.lang.String[])
	 */
	protected synchronized Object retrieveJDOObject(String scope, Object objectID, String[] fetchGroups)  throws Exception {
		PersonManager personManager = PersonManagerUtil.getHome(Login.getLogin().getInitialContextProperties()).create();
		PersonStruct personStruct = personManager.getFullPersonStructure();
		
		orderedPersonStructBlocks = new ArrayList();
		int allStructBlockCount = personStruct.getPersonStructBlocks().size();
		int unmentionedCount = 0;
		
		Map structBlockOrder = PersonStructOrderConfigModule.sharedInstance().structBlockDisplayOrder();
		
		for (Iterator it = personStruct.getPersonStructBlocks().iterator(); it.hasNext(); ) {
			// all blocks
			PersonStructBlock structBlock = (PersonStructBlock)it.next();
			
			if (structBlockOrder.containsKey(structBlock.getPrimaryKey())) {
				// block mentioned in structBlockOrder
				Integer index = (Integer)structBlockOrder.get(structBlock.getPrimaryKey());
				structBlock.setPriority(index.intValue());
			}
			else {
				structBlock.setPriority(allStructBlockCount + (unmentionedCount++));
			}
			
			orderedPersonStructBlocks.add(structBlock);
		}
		Collections.sort(orderedPersonStructBlocks);
		
		return personStruct;
	}
	
	/**
	 * Get the full Person structure out of the Cache.
	 * 
	 * @return The full PersonStruct.
	 */
	public PersonStruct getPersonStruct() {
		try {
			return (PersonStruct)getJDOObject(null, PersonStructID.create(Login.getLogin().getOrganisationID()), dummyFetchGroups);
		} catch (LoginException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Returns a ordered list of the StructBlocks for the
	 * current person structure.
	 */
	public List getOrderedPersonStructBlocks() {
		getPersonStruct();
		return orderedPersonStructBlocks;
	}
	
	public List getOrderedPersonStructFields(PersonStructBlockID structBlockID) {
		// TODO: Implement getOrderedPersonStructFields
		return null;
	}
	
	
	private static PersonStructProvider sharedInstance;
	 
	public static PersonStructProvider sharedInstance() {
		if (sharedInstance == null)
			sharedInstance = new PersonStructProvider();
		return sharedInstance;
	}
	
	public static PersonStruct getPersonStructure() {
		return sharedInstance().getPersonStruct();
	}

}
