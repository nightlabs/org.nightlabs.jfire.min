/**
 * 
 */
package org.nightlabs.jfire.base.overview.search;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.nightlabs.base.ui.extensionpoint.AbstractEPProcessor;

/**
 * registry for the extension-point org.nightlabs.jfire.base.quickSearchEntry 
 * 
 * @author Daniel Mazurek - daniel [at] nightlabs [dot] de
 */
public class QuickSearchEntryRegistry 
extends AbstractEPProcessor 
{
	private static final Logger logger = Logger.getLogger(QuickSearchEntryRegistry.class);
	public static final String EXTENSION_POINT_ID = "org.nightlabs.jfire.base.quickSearchEntry";
	
	public static String ELEMENT_QUICK_SEARCH_ENTRY_FACTORY = "quickSearchEntryFactory";
	public static String ATTRIBUTE_CLASS = "class";
	public static String ATTRIBUTE_ID = "id";
	public static String ATTRIBUTE_NAME = "name";
	public static String ATTRIBUTE_IMAGE = "image";
	public static String ATTRIBUTE_DECORATOR_IMAGE = "decoratorImage";
	
	private static QuickSearchEntryRegistry sharedInstance;
	public static QuickSearchEntryRegistry sharedInstance() {
		if (sharedInstance == null) {
			synchronized (QuickSearchEntryRegistry.class) {
				if (sharedInstance == null) {
					sharedInstance = new QuickSearchEntryRegistry();
				}
			}
		}
		return sharedInstance;
	}
	
	private Map<String, Set<QuickSearchEntryFactory>> id2Factories = new HashMap<String, Set<QuickSearchEntryFactory>>();
			
	@Override
	public String getExtensionPointID() {
		return EXTENSION_POINT_ID;
	}

	public void processElement(IExtension extension, IConfigurationElement element)
	throws Exception 
	{
		if (element.getName().equals(ELEMENT_QUICK_SEARCH_ENTRY_FACTORY)) {
			if (checkString(element.getAttribute(ATTRIBUTE_CLASS))) {
				try {
					QuickSearchEntryFactory factory = (QuickSearchEntryFactory) element.createExecutableExtension(ATTRIBUTE_CLASS);
					if (factory != null && factory.getId() != null) {
						String id = factory.getId();
						Set<QuickSearchEntryFactory> factories = id2Factories.get(id);
						if (factories == null)
							factories = new HashSet<QuickSearchEntryFactory>();
						factories.add(factory);
						id2Factories.put(id, factories);						
					}
				} catch (Exception e) {
					logger.error("There occured an error during initalizing the class "+element.getAttribute(ATTRIBUTE_CLASS), e); //$NON-NLS-1$
				}				
			}
		}
	}

	public Set<QuickSearchEntryFactory> getFactories(String id) {
		checkProcessing();
		return id2Factories.get(id);
	}
}
