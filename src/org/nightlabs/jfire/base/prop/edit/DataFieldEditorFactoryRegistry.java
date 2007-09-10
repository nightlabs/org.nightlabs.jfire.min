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

package org.nightlabs.jfire.base.prop.edit;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.nightlabs.base.extensionpoint.AbstractEPProcessor;
import org.nightlabs.base.extensionpoint.EPProcessorException;
import org.nightlabs.jfire.prop.AbstractDataField;
import org.nightlabs.jfire.prop.IStruct;

/**
 * A Registry holding associations from subclasses of {@link org.nightlabs.jfire.base.prop.AbstractDataField} to
 * {@link org.nightlabs.jfire.base.prop.edit.DataFieldEditor}s grouped by editorTypes.<br/>
 * 
 * As EPProcessor it processes extensions to org.nightlabs.jfire.base.prop.edit.propDataFieldEditor.
 *  
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 * @author Tobias Langner <!-- tobias[dot]langner[at]nightlabs[dot]de -->
 */
public class DataFieldEditorFactoryRegistry extends AbstractEPProcessor {
	private Logger LOGGER = Logger.getLogger(DataFieldEditorFactoryRegistry.class);
	
	public static final String EXTENSION_POINT_ID = "org.nightlabs.jfire.base.propDataFieldEditorFactory"; //$NON-NLS-1$
	public static final String EXTENSION_POINT_ELEMENT_NAME = "propdatafieldeditorfactory"; // lower case for error tolerance //$NON-NLS-1$
	
	/**
	 * A Map holding the registries for all editorTypes.<br/>
	 *   key: String editorType<br/>
	 *   value: Map registry<br />
	 *     key: Class targetType<br/>
	 *     value: DataFieldEditor editor<br/>
	 */
	private static Map<String, Map<Class, DataFieldEditorFactory>> registriesMap =
		new HashMap<String, Map<Class,DataFieldEditorFactory>>();
	
	private Map<Class, DataFieldEditorFactory> getTypedRegistry(String type)
	{
		Map registry = registriesMap.get(type);
		if (registry == null)
		{
			registry = new HashMap<String, Map<Class,DataFieldEditorFactory>>();
			registriesMap.put(type, registry);
		}
		
		return registry;
	}
	
	/**
	 * Add a new {@link DataFieldEditor} to the registry.
	 * Checks if {@link DataFieldEditor#getTargetPropStructType()} returns
	 * a subclass of {@link org.nightlabs.jfire.base.prop.AbstractDataField}
	 * and throws a {@link IllegalArgumentException}
	 * 
	 * @param context The context in which this binding should be used. <code>null</code> can be used to indicate
	 *        the default context.
	 * @param targetType
	 * @param editorFactory
	 */
	public synchronized void addDataFieldEditorFactory(DataFieldEditorFactory editorFactory)
	{
		if (editorFactory == null)
			throw new IllegalArgumentException("Parameter editor must not be null!"); //$NON-NLS-1$
		Class targetType = editorFactory.getPropDataFieldType();
		if (targetType == null)
			throw new IllegalArgumentException("Parameter targetType must not be null!"); //$NON-NLS-1$
		
		if (!(AbstractDataField.class.isAssignableFrom(targetType)))
			throw new IllegalArgumentException("TargetType must be subclass of AbstractDataField but is "+targetType.getName()); //$NON-NLS-1$
		
		for (String editorType : editorFactory.getEditorTypes()) {
			getTypedRegistry(editorType).put(targetType, editorFactory);
		}
	}
		
	/**
	 * Find the editor for a specific PropDataField-type and editor type und a specific context.
	 * context may be <code>null</code> to indicate the default context.
	 * @param editorType
	 * @param targetType
	 *  
	 * @return The registered DataFieldEditor for the given targetType
	 * @throws DataFieldEditorNotFoundException
	 */
	public synchronized DataFieldEditorFactory getEditorFactory(String editorType, Class targetType)
	throws DataFieldEditorNotFoundException
	{
		checkProcessing();
		
		if (targetType == null)
			throw new IllegalArgumentException("Parameter targetType must not be null"); //$NON-NLS-1$
		
		Map<Class, DataFieldEditorFactory> registry = getTypedRegistry(editorType);

		if (!registry.containsKey(targetType))
			throw new DataFieldEditorNotFoundException("No editor found for editorType=\""+editorType+"\" targetType=\""+targetType.getName()+"\""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		return registry.get(targetType);
	}
	
	/**
	 * Find the DataFieldEditorFactory for the Class of the given dataField, 
	 * editorType and context and invokes createPropDataFieldEditor(dataField, setData)
	 * 
	 * @param dataField
	 * @param editorType
	 * @param context May be null to indicate default context
	 * @param setData
	 * @return A new instance of the appropriate DataFieldEditor
	 * @throws DataFieldEditorNotFoundException
	 */
	public DataFieldEditor getNewEditorInstance(
			IStruct struct, String editorType, 
			String context, AbstractDataField dataField, boolean setData
		) 
	throws DataFieldEditorNotFoundException 
	{
		DataFieldEditorFactory fieldEditorFactry = getEditorFactory(editorType, dataField.getClass());
		return fieldEditorFactry.createPropDataFieldEditor(struct, dataField, setData);
	}
	
	/**
	 * Find the DataFieldEditorFactory for the Class of the given dataField 
	 * and editorType and invokes createPropDataFieldEditor(dataField, true)
	 * @param editorType
	 * @param context may be null to indicate default context.
	 * @param dataField
	 * 
	 * @return A new instance of the appropriate DataFieldEditor
	 * @throws DataFieldEditorNotFoundException
	 */
	public DataFieldEditor getNewEditorInstance(
			IStruct struct, String editorType, 
			String context, AbstractDataField dataField
		) 
	throws DataFieldEditorNotFoundException 
	{
		DataFieldEditorFactory fieldEditorFactory = getEditorFactory(editorType, dataField.getClass());
		return fieldEditorFactory.createPropDataFieldEditor(struct, dataField, true);
	}
	
	/**
	 * 
	 * @param editorType
	 * @param context may be null to indicate default context.
	 * @param targetType
	 * @return Wether the registry has a registration for the given targetType and editorType
	 */
	public synchronized boolean hasRegistration(String editorType, String context, Class targetType) {
		return getTypedRegistry(editorType).containsKey(targetType);
	}
	
	private static DataFieldEditorFactoryRegistry sharedInstance;
	
	/**
	 * Returns the static shared instance of a DataFieldEditorFactoryRegistry. 
	 * @return The static shared instance of a DataFieldEditorFactoryRegistry.
	 */
	public static DataFieldEditorFactoryRegistry sharedInstance() {
		if (sharedInstance == null) {
			sharedInstance = new DataFieldEditorFactoryRegistry();
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
	public void processElement(IExtension extension, IConfigurationElement element) throws Exception {
		try
		{
			if (element.getName().toLowerCase().equals(EXTENSION_POINT_ELEMENT_NAME))
			{
				DataFieldEditorFactory fieldEditorFactory = (DataFieldEditorFactory) element.createExecutableExtension("class"); //$NON-NLS-1$
//				Class targetType = Class.forName(element.getAttribute("targetType"));
//				if (targetType != fieldEditorFactory.getPropDataFieldType())
//					throw new IllegalStateException("Target type from extension point does not match editorFactory's target type.");
//				
//				String context = element.getAttribute("context");
				sharedInstance().addDataFieldEditorFactory(fieldEditorFactory);
			}
			else
			{
				throw new IllegalArgumentException("Element "+element.getName()+" is not supported by extension-point "+EXTENSION_POINT_ID); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
		catch(Throwable e)
		{
			throw new EPProcessorException(e);
		}
	}
}
