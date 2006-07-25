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

package org.nightlabs.jfire.base.person.edit;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;

import org.nightlabs.base.extensionpoint.AbstractEPProcessor;
import org.nightlabs.base.extensionpoint.EPProcessorException;
import org.nightlabs.jfire.person.AbstractPersonDataField;

/**
 * A Registry holding Associations from subclasses of {@link org.nightlabs.jfire.base.person.AbstractPersonDataField}
 * to {@link org.nightlabs.jfire.base.person.edit.PersonDataFieldEditor}s grouped by editorTypes.<br/>
 * As EPProcessor it processes extensions to org.nightlabs.jfire.base.person.edit.personDataFieldEditor. 
 * 
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 */
public class PersonDataFieldEditorFactoryRegistry extends AbstractEPProcessor {
	/**
	 * LOG4J logger used by this class
	 */
	private static final Logger logger = Logger.getLogger(PersonDataFieldEditorFactoryRegistry.class);
	
	public static final String EXTENSION_POINT_ID = "org.nightlabs.jfire.base.person_edit_personDataFieldEditorFactory";
	public static final String EXTENSION_POINT_ELEMENT_NAME = "persondatafieldeditorfactory"; // lower case for error tolerance
	
	/**
	 * A Map holding the registries for all editorTypes.<br/>
	 * 
	 * key: String editorType<br/>
	 * value: Map registry<br/>
	 * 		key: Class targetType<br/>
	 * 		value: PersonDataFieldEditor editor<br/>
	 */
	private static Map registriesMap = new HashMap();
	
	private Map getTypedRegistry(String type) {
		Map registry = (Map)registriesMap.get(type);
		if (registry == null) {
			registry = new HashMap();
			registriesMap.put(type,registry);
		}
		return registry;
	}
	
	/**
	 * Add a new {@link PersonDataFieldEditor} to the registry.
	 * Checks if {@link PersonDataFieldEditor#getTargetPersonStructType()} returns
	 * a subclass of {@link org.nightlabs.jfire.base.person.AbstractPersonDataField}
	 * and throws a {@link IllegalArgumentException}
	 * @param editor
	 */
	public synchronized void addDataFieldEditorFactory(PersonDataFieldEditorFactory editorFactory) {
		if (editorFactory == null)
			throw new IllegalArgumentException("Parameter editor must not be null!");
		Class targetType = editorFactory.getTargetPersonDataFieldType();
		if (!(AbstractPersonDataField.class.isAssignableFrom(targetType)))
			throw new IllegalArgumentException("TargetType must be subclass of AbstractPersonDataField but is "+targetType.getName());
		logger.debug("Adding registration for "+targetType.getName()+" on editor "+editorFactory+" editorType is "+editorFactory.getEditorType());
		
		getTypedRegistry(editorFactory.getEditorType()).put(targetType,editorFactory);
	}
	
	
	private boolean extensionPointProcessed = false;
	/**
	 * Find the editor for a specific PersonDataField-type and editor type.
	 *  
	 * @param targetType
	 * @param editorType
	 * @return The registered PersonDataFieldEditor for the given targetType
	 * @throws PersonDataFieldEditorNotFoundException
	 */
	public synchronized PersonDataFieldEditorFactory getEditorFactory(Class targetType, String editorType)
	throws PersonDataFieldEditorNotFoundException
	{
		if (!extensionPointProcessed) {
			// process the extensionpoint to make the registrations
			try {
				process();
			} catch (EPProcessorException e) {
				throw new PersonDataFieldEditorNotFoundException(e);
			}
			extensionPointProcessed = true;
		}
		
		if (targetType == null)
			throw new IllegalArgumentException("Parameter targetType must not be null");
		Map registry = getTypedRegistry(editorType);
		
		if (!registry.containsKey(targetType))
			throw new PersonDataFieldEditorNotFoundException("No editor found for class "+targetType.getName());
		
		return (PersonDataFieldEditorFactory)registry.get(targetType);
	}
	
	/**
	 * Find the PersonDataFieldEditorFactory for the Class of the given dataField 
	 * and editorType and invokes createPersonDataFieldEditor(dataField, setData)
	 * 
	 * @param dataField
	 * @param editorType
	 * @param setData
	 * @return A new instance of the appropriate PersonDataFieldEditor
	 * @throws PersonDataFieldEditorNotFoundException
	 */
	public PersonDataFieldEditor getNewEditorInstance(AbstractPersonDataField dataField, String editorType, boolean setData) 
	throws PersonDataFieldEditorNotFoundException 
	{
		PersonDataFieldEditorFactory fieldEditorFactry = getEditorFactory(dataField.getClass(), editorType);
		return fieldEditorFactry.createPersonDataFieldEditor(dataField, setData);
	}
	
	/**
	 * Find the PersonDataFieldEditorFactory for the Class of the given dataField 
	 * and editorType and invokes createPersonDataFieldEditor(dataField, true)
	 * 
	 * @param dataField
	 * @param editorType
	 * @return A new instance of the appropriate PersonDataFieldEditor
	 * @throws PersonDataFieldEditorNotFoundException
	 */
	public PersonDataFieldEditor getNewEditorInstance(AbstractPersonDataField dataField, String editorType) 
	throws PersonDataFieldEditorNotFoundException 
	{
		PersonDataFieldEditorFactory fieldEditorFactry = getEditorFactory(dataField.getClass(), editorType);
		return fieldEditorFactry.createPersonDataFieldEditor(dataField, true);
	}
	
	/**
	 * 
	 * @param targetType
	 * @param editorType
	 * @return Weather the registry has a registration for the given targetType and editorType
	 */
	public synchronized boolean hasRegistration(Class targetType, String editorType) {
		return getTypedRegistry(editorType).containsKey(targetType);
	}
	
	private static PersonDataFieldEditorFactoryRegistry sharedInstance;
	
	/**
	 * Returns the static shared instance of a PersonDataFieldEditorFactoryRegistry. 
	 * @return The static shared instance of a PersonDataFieldEditorFactoryRegistry.
	 */
	public static PersonDataFieldEditorFactoryRegistry sharedInstance() {
		if (sharedInstance == null) {
			sharedInstance = new PersonDataFieldEditorFactoryRegistry();
		}
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
				PersonDataFieldEditorFactory fieldEditorFactory = (PersonDataFieldEditorFactory) element.createExecutableExtension("class");
				sharedInstance().addDataFieldEditorFactory(fieldEditorFactory);
			}
			else {
				throw new IllegalArgumentException("Element "+element.getName()+" is not supported by extension-point "+EXTENSION_POINT_ID);
			}
		}catch(Throwable e){
			throw new EPProcessorException(e);
		}
	}
}
