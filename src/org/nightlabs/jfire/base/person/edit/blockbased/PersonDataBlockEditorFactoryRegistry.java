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

package org.nightlabs.jfire.base.person.edit.blockbased;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.swt.widgets.Composite;

import org.nightlabs.base.extensionpoint.AbstractEPProcessor;
import org.nightlabs.base.extensionpoint.EPProcessorException;
import org.nightlabs.jfire.person.PersonDataBlock;
import org.nightlabs.jfire.person.id.PersonStructBlockID;

/**
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 *
 */
public class PersonDataBlockEditorFactoryRegistry extends AbstractEPProcessor {
	
	public static final String EXTENSION_POINT_ID = "org.nightlabs.jfire.base.person_edit_specializedDataBlockEditor";
	public static final String EXTENSION_POINT_ELEMENT_NAME = "specializeddatablockeditor"; // lower case for error tolerance
	public static final String EXTENSION_POINT_CLASS_ATTRIBUTE_NAME = "class"; // lower case for error tolerance
	
	/**
	 * key: PersonStructBlockID: providerID<br/>
	 * value: PersonDataBlockEditorFactory provider
	 */
	private Map providerRegistry = new HashMap();

	public void addPersonDataBlockEditorProvider(PersonDataBlockEditorFactory provider) {
		PersonStructBlockID blockID = provider.getProviderStructBlockID();
		providerRegistry.put(blockID,provider);
	}
	
	public PersonDataBlockEditor getPersonDataBlockEditor(
		PersonDataBlock dataBlock,
		Composite parent, 
		int style,
		int columnHint
	) 
	throws EPProcessorException 
	{
		if (!isProcessed())
			process();
		PersonStructBlockID blockID = PersonStructBlockID.create(dataBlock.getPersonStructBlockOrganisationID(),dataBlock.getPersonStructBlockID());
		PersonDataBlockEditorFactory provider = (PersonDataBlockEditorFactory)providerRegistry.get(blockID);
		if (provider != null)
			return provider.createPersonDataBlockEditor(dataBlock,parent,style);
		else
			return new GenericPersonDataBlockEditor(dataBlock,parent,style,columnHint);
	}
	
	private static PersonDataBlockEditorFactoryRegistry sharedInstance;
	
	public static PersonDataBlockEditorFactoryRegistry getSharedInstace() {
		if (sharedInstance == null)
			sharedInstance = new PersonDataBlockEditorFactoryRegistry();
		return sharedInstance;
	}

	/**
	 * @see org.nightlabs.base.extensionpoint.AbstractEPProcessor#getExtensionPointID()
	 */
	public String getExtensionPointID() {
		return EXTENSION_POINT_ID;
	}

	/**
	 * @see org.nightlabs.base.extensionpoint.AbstractEPProcessor#processElement(IExtension, org.eclipse.core.runtime.IConfigurationElement)
	 */
	public void processElement(IExtension extension, IConfigurationElement element) throws EPProcessorException {
		try{
			if (element.getName().toLowerCase().equals(EXTENSION_POINT_ELEMENT_NAME)){
				PersonDataBlockEditorFactory provider = (PersonDataBlockEditorFactory) element.createExecutableExtension(EXTENSION_POINT_CLASS_ATTRIBUTE_NAME);
				addPersonDataBlockEditorProvider(provider);
			}
			else {
				throw new IllegalArgumentException("Element "+element.getName()+" is not supported by extension-point "+EXTENSION_POINT_ID);
			}
		}catch(Throwable e){
			throw new EPProcessorException(e);
		}
	}

}
