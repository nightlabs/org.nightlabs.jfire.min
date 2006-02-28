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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.security.auth.login.LoginException;

import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.base.jdo.JDOObjectProvider;
import org.nightlabs.jfire.base.login.Login;
import org.nightlabs.jfire.base.person.preferences.PersonStructOrderConfigModule;
import org.nightlabs.jfire.person.PersonManager;
import org.nightlabs.jfire.person.PersonManagerUtil;
import org.nightlabs.jfire.person.PersonStruct;
import org.nightlabs.jfire.person.PersonStructBlock;
import org.nightlabs.jfire.person.id.PersonStructBlockID;
import org.nightlabs.jfire.person.id.PersonStructID;

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
	 * @see org.nightlabs.jfire.base.jdo.JDOObjectProvider#retrieveJDOObject(java.lang.String, java.lang.Object, java.lang.String[])
	 */
	protected synchronized Object retrieveJDOObject(String scope, Object objectID, String[] fetchGroups, int maxFetchDepth)  throws Exception {
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
			return (PersonStruct)getJDOObject(null, PersonStructID.create(Login.getLogin().getOrganisationID()), dummyFetchGroups, NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT);
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
