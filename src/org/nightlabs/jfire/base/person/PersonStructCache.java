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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import org.nightlabs.jfire.base.app.JFireApplication;
import org.nightlabs.jfire.base.login.Login;
import org.nightlabs.jfire.base.person.preferences.PersonStructOrderConfigModule;
import org.nightlabs.jfire.person.PersonManager;
import org.nightlabs.jfire.person.PersonManagerHome;
import org.nightlabs.jfire.person.PersonManagerUtil;
import org.nightlabs.jfire.person.PersonStruct;
import org.nightlabs.jfire.person.PersonStructBlock;
import org.nightlabs.jfire.person.id.PersonStructBlockID;

/**
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 * @deprecated use {@link org.nightlabs.jfire.base.person.PersonStructProvider} instead
 */
public class PersonStructCache {
	private static final Logger LOGGER = Logger.getLogger(PersonStructCache.class);
	
	public PersonStructCache() {
	}
	
	private PersonManager personManager;
	
	public PersonManager getPersonManager() {
		if (personManager == null) {
			try {
				Login login = Login.getLogin();
		    PersonManagerHome home = null;
		    try {
		      home = PersonManagerUtil.getHome(login.getInitialContextProperties());
		    } catch (Exception e) {
		    	LOGGER.error("Error getting PersonManagerHome.",e);
		    }
		    try {
		      personManager = home.create();
		    } catch (Exception e) {
		    	LOGGER.error("Error creating PersonManager.",e);
		    }	
			} catch (Throwable e) {
				throw new RuntimeException(e);
			}
		}
		return personManager;
	}
	
	
	public void setPersonManager(PersonManager personManager) {
		this.personManager = personManager;
	}
	
	private PersonStruct personStructure;
	
	/**
	 * Returns and caches a {@link PersonStruct} per session;
	 * @return
	 */
	public PersonStruct getCachedPersonStructure() {
		if (personStructure == null) {
			File cache = new File(getCacheDir()+File.separatorChar+"PersonStruct.res");
			
			if (cache.exists()) {
				// have locally stored version
				// TODO: Check validity of PersonStructCache 
				try {
					ObjectInputStream ois = new ObjectInputStream(new FileInputStream(cache));
					personStructure = (PersonStruct)ois.readObject();
				} catch (Throwable t) {
					// something, whatever went wrong ..
					LOGGER.error("Error reading person structure cache.",t);
					personStructure = null;
				}
			}
			
			if (personStructure == null) {
				// not found locally 
				try {
					personStructure = getPersonManager().getFullPersonStructure();
				} catch (Throwable t) {
					LOGGER.error("Error fetching person structure",t);
					personStructure = null;
				}
				
				if (personStructure != null) {
					// store person structure for later use
					try {
//						cache.mkdirs();
						File _cacheDir = new File(getCacheDir());
						_cacheDir.mkdirs();
						cache.createNewFile();
						ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(cache));
						oos.writeObject(personStructure);
					} catch (Throwable t) {
						// something, whatever went wrong ..
						LOGGER.error("Error writing person structure cache.",t);
						cache.delete();
					}
				}
			}
		}
		if (personStructure == null)
			throw new IllegalStateException("personStructure should not be null at this point!");
		return personStructure;
	}
	
	private static PersonStructCache sharedInstance;
	
	protected static PersonStructCache sharedInstance() {
		if (sharedInstance == null)
			sharedInstance = new PersonStructCache();
		return sharedInstance;
	}
	
	private static boolean initialized = false;
	
	/**
	 * @see org.nightlabs.jfire.base.person.PersonStructFactory#getPersonStruct()
	 */
	public PersonStruct getPersonStruct() {
		return getCachedPersonStructure();
	}
	
	/**
	 * @deprecated Use {@link PersonStructProvider#getPersonStrucure()} instead.
	 */
	public static PersonStruct getPersonStructure() {
		return sharedInstance().getPersonStruct();
	}
	
	private String cacheDir;
	
	public String getCacheDir() {
		return JFireApplication.getRootDir()+File.separatorChar+"personstruct.cache";
	}
	
	
	private static List orderedPersonStructBlocks;
	/**
	 * Returns a ordered list of the StructBlocks for the
	 * current person structure.
	 * If refresh is true the list will be resorted according to 
	 * the current order in {@link PersonStructOrderConfigModule}.
	 * 
	 * @param refresh
	 * @deprecated Use {@link PersonStructProvider#getOrderedPersonStructBlocks()}
	 */
	public static List getOrderedPersonStructBlocks(boolean refresh) {
		if ((orderedPersonStructBlocks != null) && (!refresh))
			return orderedPersonStructBlocks;
		
		orderedPersonStructBlocks = new ArrayList();
		int allStructBlockCount = PersonStructCache.getPersonStructure().getPersonStructBlocks().size();
		int unmentionedCount = 0;
		
		Map structBlockOrder = PersonStructOrderConfigModule.sharedInstance().structBlockDisplayOrder();
		
		for (Iterator it = PersonStructCache.getPersonStructure().getPersonStructBlocks().iterator(); it.hasNext(); ) {
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
		
		return orderedPersonStructBlocks;
	}
	
	public List getOrderedPersonStructFields(PersonStructBlockID structBlockID) {
		// TODO: Implement getOrderedPersonStructFields
		return null;
	}
	
}
