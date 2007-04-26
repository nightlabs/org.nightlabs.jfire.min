package org.nightlabs.jfire.base.overview;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.lf5.viewer.categoryexplorer.CategoryImmediateEditor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.nightlabs.base.extensionpoint.AbstractEPProcessor;
import org.nightlabs.base.extensionpoint.EPProcessorException;

/**
 * @author Daniel.Mazurek [at] NightLabs [dot] de
 *
 */
public class OverviewRegistry 
extends AbstractEPProcessor 
{
	public static final String EXTENSION_POINT_ID = "org.nightlabs.jfire.base.overview";
	public static final String ELEMENT_CATEGORY = "category";
	public static final String ELEMENT_CATEGORY_ENTRY = "categoryEntry";
	public static final String ATTRIBUTE_NAME = "name";
	public static final String ATTRIBUTE_CATEGORY_ID = "categoryID";
	public static final String ATTRIBUTE_CATEGORY_ENTRY_ID = "categoryEntryID";
	public static final String ATTRIBUTE_ENTRY = "entry";
	public static final String ATTRIBUTE_ICON = "icon";
	
	private static OverviewRegistry sharedInstance;

	public static OverviewRegistry sharedInstance() {
		if (sharedInstance == null) {
			synchronized (OverviewRegistry.class) {
				if (sharedInstance == null)
					sharedInstance = new OverviewRegistry();
			}
		}
		return sharedInstance;
	}
	
	protected OverviewRegistry() {
		super();
	}

	@Override
	public String getExtensionPointID() {
		return EXTENSION_POINT_ID;
	}

	@Override
	public void processElement(IExtension extension, IConfigurationElement element)
	throws EPProcessorException 
	{
		if (element.getName().equals(ELEMENT_CATEGORY)) {
			String categoryID = element.getAttribute(ATTRIBUTE_CATEGORY_ID);
			String name = element.getAttribute(ATTRIBUTE_NAME);
			String iconString = element.getAttribute(ATTRIBUTE_ICON);			
			Category category = new CategoryImpl();
			category.setName(name);
			category.setCategoryID(categoryID);			
			if (checkString(iconString)) {
				ImageDescriptor imageDescriptor = AbstractUIPlugin.imageDescriptorFromPlugin(
						extension.getNamespaceIdentifier(), iconString);
				if (imageDescriptor != null)
					category.setImage(imageDescriptor.createImage());										
			}					
			categoryID2Category.put(categoryID, category);
		}		
		if (element.getName().equals(ELEMENT_CATEGORY_ENTRY)) {
			String categoryEntryID = element.getAttribute(ATTRIBUTE_CATEGORY_ENTRY_ID);
			String categoryID = element.getAttribute(ATTRIBUTE_CATEGORY_ID);
			String name = element.getAttribute(ATTRIBUTE_NAME);
			String iconString = element.getAttribute(ATTRIBUTE_ICON);
			try {
				Entry entry = (Entry) element.createExecutableExtension(ATTRIBUTE_ENTRY);
//				entry.setName(name);
//				if (checkString(iconString)) {
//					ImageDescriptor imageDescriptor = AbstractUIPlugin.imageDescriptorFromPlugin(
//							extension.getNamespaceIdentifier(), iconString);
//					if (imageDescriptor != null)
//						entry.setImage(imageDescriptor.createImage());										
//				}				
				List<Entry> factories = categoryID2Entries.get(categoryID);
				if (factories == null)
					factories = new ArrayList<Entry>();
				factories.add(entry);			
				categoryID2Entries.put(categoryID, factories);
			} catch (CoreException e) {
				throw new RuntimeException(e);
			}
		}
	}

	private Map<String, Category> categoryID2Category = new HashMap<String, Category>();	
	private Map<String, List<Entry>> categoryID2Entries = new HashMap<String, List<Entry>>();
	
	private Category fallBackCategory = null;
	public Category getFallbackCategory() {
		if (fallBackCategory == null) {
			fallBackCategory = new CategoryImpl();
			fallBackCategory.setName("Other");
		}
		return fallBackCategory;
	}
	
	protected void check() 
	{
		if (category2Entries == null) 
		{
			checkProcessing();
			category2Entries = new HashMap<Category, List<Entry>>();
			for (Map.Entry<String, List<Entry>> mapEntry : categoryID2Entries.entrySet()) {
				Category category = categoryID2Category.get(mapEntry.getKey());
				List<Entry> entries = mapEntry.getValue();
				if (category == null)
					category = getFallbackCategory();
				
				category2Entries.put(category, entries);				
//				for (Entry entry : entries) 
//				{
//					List<Entry> entries = category2Entries.get(category);
//					if (entries == null)
//						entries = new ArrayList<Entry>();
//					
//					Entry entry = entryFactory.createEntry();
//					entry.setName(entryFactory.getName());
//					entry.setImage(entryFactory.getImage());
//					entries.add(entry);
//					
//					category2Entries.put(category, entries);
//				}
			}			
		}
	}
	
	private Map<Category, List<Entry>> category2Entries = null;
	public List<Entry> getEntries(Category category) {
		check();
		return category2Entries.get(category);
	}
	
	public Collection<Category> getCategories() {
		check();
		return category2Entries.keySet();
	}
	
}
