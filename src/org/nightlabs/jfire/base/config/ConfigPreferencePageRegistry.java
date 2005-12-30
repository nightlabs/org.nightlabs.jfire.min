/*
 * Created 	on May 31, 2005
 * 					by alex
 *
 */
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
 * 
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 *
 */
public class ConfigPreferencePageRegistry extends AbstractEPProcessor {

	protected Logger LOGGER = Logger.getLogger(ConfigPreferencePageRegistry.class); 
	
	private ConfigPreferenceNode preferencesRootNode; 
	
	/**
	 * key: String id<br/>
	 * value: ConfigPreferenceNode preferenceNode
	 */
	private Map preferencePagesByIDs;
	
	/**
	 * 
	 */
	public ConfigPreferencePageRegistry() {
		super();
	}

	/**
	 * @see org.nightlabs.base.extensionpoint.AbstractEPProcessor#getExtensionPointID()
	 */
	public String getExtensionPointID() {
		// TODO: find static final
		return "org.eclipse.ui.preferencePages";
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
		try {
			process();
		} catch (EPProcessorException e) {
			throw new RuntimeException(e);
		}
		return preferencesRootNode;
	}

	/**
	 * @see org.nightlabs.base.extensionpoint.AbstractEPProcessor#processElement(IExtension, org.eclipse.core.runtime.IConfigurationElement)
	 */
	public void processElement(IExtension extension, IConfigurationElement element)
			throws EPProcessorException {
		if (element.getName().equals("page")) {
			String id = element.getAttribute("id");
			String category = element.getAttribute("category");
			if (id == null || "".equals(id))
				throw new EPProcessorException("Element page has to define an attribute id.");
			String name = element.getAttribute("name");
			IWorkbenchPreferencePage page = null;			
			try {
					page = (IWorkbenchPreferencePage)element.createExecutableExtension("class");
			} catch (Throwable e) {				
				LOGGER.warn("Could not instantiate preference-page extension of type "+element.getAttribute("class")+". Reason "+e.getClass().getName()+": "+e.getMessage());
				page = null;
			}
			if (page == null)
				return;
			if (!(page instanceof AbstractConfigModulePreferencePage))
				return;
			
			ConfigPreferenceNode preferenceNode = new ConfigPreferenceNode(id, name, category, null, (AbstractConfigModulePreferencePage)page);
			preferencePagesByIDs.put(id, preferenceNode);
		}
	}

	public synchronized void process() throws EPProcessorException {
		preferencesRootNode = new ConfigPreferenceNode(
				"",
				"",
				"",
				null,
				null
		);		
		
		preferencePagesByIDs = new HashMap();
		super.process();
		
		for (Iterator iter = preferencePagesByIDs.values().iterator(); iter.hasNext();) {
			ConfigPreferenceNode node = (ConfigPreferenceNode) iter.next();
			ConfigPreferenceNode parentNode = (ConfigPreferenceNode)preferencePagesByIDs.get(node.getCategoryID());			
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
