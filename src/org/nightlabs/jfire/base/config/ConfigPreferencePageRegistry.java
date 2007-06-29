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

package org.nightlabs.jfire.base.config;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.nightlabs.base.extensionpoint.AbstractEPProcessor;
import org.nightlabs.base.extensionpoint.EPProcessorException;

/**
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public class ConfigPreferencePageRegistry extends AbstractEPProcessor 
{
	public static final String CLASS_ELEMENT = "class"; //$NON-NLS-1$
	private static final String NAME_ELEMENT = "name"; //$NON-NLS-1$
	private static final String CATEGORY_ELEMENT = "category"; //$NON-NLS-1$
	private static final String ID_ELEMENT = "id"; //$NON-NLS-1$
	private static final String PAGE_ELEMENT = "page"; //$NON-NLS-1$

	/**
	 * LOG4J logger used by this class
	 */
	private static final Logger logger = Logger.getLogger(ConfigPreferencePageRegistry.class); 
	
	private ConfigPreferenceNode preferencesRootNode; 
	
	/**
	 * key: String id<br/>
	 * value: ConfigPreferenceNode preferenceNode
	 */
	private Map<String, ConfigPreferenceNode> preferenceNodesByIDs;
	
	/* (non-Javadoc)
	 * @see org.nightlabs.base.extensionpoint.AbstractEPProcessor#getExtensionPointID()
	 */
	@Override
	public String getExtensionPointID() 
	{
		return "org.eclipse.ui.preferencePages"; //$NON-NLS-1$
	}
	
	/**
	 * Returns a new ConfigPreferenceNode to wich all registered
	 * {@link AbstractConfigModulePreferencePage}s
	 * are children. This will contain new
	 * instances of PreferencePages.
	 * 
	 * @return A new ConfigPreferenceNode
	 */
	public ConfigPreferenceNode getPreferencesRootNode() {
		checkProcessing();
		return preferencesRootNode;
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.base.extensionpoint.AbstractEPProcessor#processElement(org.eclipse.core.runtime.IExtension, org.eclipse.core.runtime.IConfigurationElement)
	 */
	@Override
	public void processElement(IExtension extension, IConfigurationElement element) throws Exception 
	{
		if (element.getName().equals(PAGE_ELEMENT)) {
			String id = element.getAttribute(ID_ELEMENT);
			String category = element.getAttribute(CATEGORY_ELEMENT);
			if (id == null || "".equals(id)) //$NON-NLS-1$
				throw new EPProcessorException("Element page has to define an attribute id."); //$NON-NLS-1$
			String name = element.getAttribute(NAME_ELEMENT);
			IWorkbenchPreferencePage page = null;			
			try {
				page = (IWorkbenchPreferencePage)element.createExecutableExtension(CLASS_ELEMENT);
			} catch (Throwable e) {
				logger.error("Could not instantiate preference-page extension of type "+element.getAttribute(CLASS_ELEMENT)+"! Check, whether it has a default constructor!", e); //$NON-NLS-1$ //$NON-NLS-2$
				page = null;
			}
			if (page == null)
				return;
			if (!(page instanceof AbstractConfigModulePreferencePage))
				return;
			AbstractConfigModulePreferencePage configPage = (AbstractConfigModulePreferencePage) page;
			ConfigPreferenceNode preferenceNode = new ConfigPreferenceNode(
					id, name, category, 
					null, element,
					configPage,
					null // cfModIDs are set later when the merging takes place
			);
			preferenceNodesByIDs.put(id, preferenceNode);
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.base.extensionpoint.AbstractEPProcessor#process()
	 */
	@Override
	public synchronized void process() {
		preferencesRootNode = new ConfigPreferenceNode(
				"", //$NON-NLS-1$
				"", //$NON-NLS-1$
				"", //$NON-NLS-1$
				null,
				null,
				null,
				null
		);		
		
		preferenceNodesByIDs = new HashMap<String, ConfigPreferenceNode>();
		super.process();
		
		for (Iterator iter = preferenceNodesByIDs.values().iterator(); iter.hasNext();) {
			ConfigPreferenceNode node = (ConfigPreferenceNode) iter.next();
			ConfigPreferenceNode parentNode = (ConfigPreferenceNode)preferenceNodesByIDs.get(node.getCategoryID());			
			if (parentNode != null)
				parentNode.addChild(node);
			else
				preferencesRootNode.addChild(node);
		}
	}
	
	private static ConfigPreferencePageRegistry sharedInstance;
	
	public static ConfigPreferencePageRegistry sharedInstance() {
		if (sharedInstance == null) 
			sharedInstance = new ConfigPreferencePageRegistry();
		return sharedInstance;
	}
}
