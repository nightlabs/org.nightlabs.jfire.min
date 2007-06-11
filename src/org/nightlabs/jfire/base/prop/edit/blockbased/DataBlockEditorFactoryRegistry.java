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

package org.nightlabs.jfire.base.prop.edit.blockbased;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.swt.widgets.Composite;
import org.nightlabs.base.extensionpoint.AbstractEPProcessor;
import org.nightlabs.base.extensionpoint.EPProcessorException;
import org.nightlabs.jfire.prop.DataBlock;
import org.nightlabs.jfire.prop.IStruct;
import org.nightlabs.jfire.prop.id.StructBlockID;

/**
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 * @author Tobias Langner <tobias[DOT]langner[AT]nightlabs[DOT]de>
 */
public class DataBlockEditorFactoryRegistry extends AbstractEPProcessor {
	
	public static final String EXTENSION_POINT_ID = "org.nightlabs.jfire.base.specialisedDataBlockEditor";
	public static final String EXTENSION_POINT_ELEMENT_NAME = "specialisedDataBlockEditor"; // lower case for error tolerance
	public static final String EXTENSION_POINT_CLASS_ATTRIBUTE_NAME = "class"; // lower case for error tolerance
	
	/**
	 * key: StructBlockID: providerID<br/>
	 * value: DataBlockEditorFactory provider
	 */
	private Map providerRegistry = new HashMap();

	public void addPropDataBlockEditorProvider(DataBlockEditorFactory provider)
	{
		providerRegistry.put(provider.getProviderStructBlockID(), provider);
	}
	
	public DataBlockEditor getPropDataBlockEditor(
		IStruct struct,
		DataBlock dataBlock,
		Composite parent, 
		int style,
		int columnHint
	) 
	throws EPProcessorException 
	{
		if (!isProcessed())
			process();
		StructBlockID blockID = StructBlockID.create(dataBlock.getStructBlockOrganisationID(),dataBlock.getStructBlockID());
		DataBlockEditorFactory provider = (DataBlockEditorFactory)providerRegistry.get(blockID);
		if (provider != null)
			return provider.createPropDataBlockEditor(struct, dataBlock, parent, style);
		else
			return new GenericDataBlockEditor(struct, dataBlock, parent, style, columnHint);
	}
	
	private static DataBlockEditorFactoryRegistry sharedInstance;
	
	public static DataBlockEditorFactoryRegistry sharedInstance() {
		if (sharedInstance == null)
			sharedInstance = new DataBlockEditorFactoryRegistry();
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
			if (element.getName().equalsIgnoreCase(EXTENSION_POINT_ELEMENT_NAME))
			{
				DataBlockEditorFactory provider = (DataBlockEditorFactory) element.createExecutableExtension(EXTENSION_POINT_CLASS_ATTRIBUTE_NAME);
				addPropDataBlockEditorProvider(provider);
			}
			else
			{
				throw new IllegalArgumentException("Element "+element.getName()+" is not supported by extension-point "+EXTENSION_POINT_ID);
			}
		}
		catch(Throwable e)
		{
			throw new EPProcessorException(e);
		}
	}

}
