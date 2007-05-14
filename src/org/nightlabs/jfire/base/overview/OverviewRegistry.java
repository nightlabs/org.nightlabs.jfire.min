package org.nightlabs.jfire.base.overview;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

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
	public static final String ATTRIBUTE_INDEX = "index";
	
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
			String indexString = element.getAttribute(ATTRIBUTE_INDEX);
			CategoryImpl category = new CategoryImpl();
			category.setName(name);
			category.setCategoryID(categoryID);
			if (checkString(iconString)) {
				ImageDescriptor imageDescriptor = AbstractUIPlugin.imageDescriptorFromPlugin(
						extension.getNamespaceIdentifier(), iconString);
				if (imageDescriptor != null)
					category.setImage(imageDescriptor.createImage());										
			}
			if (checkString(indexString)) {
				int index = Integer.valueOf(indexString);
				category.setIndex(index);
			}
			categoryID2Category.put(categoryID, category);
		}		
		if (element.getName().equals(ELEMENT_CATEGORY_ENTRY)) {
			String categoryID = element.getAttribute(ATTRIBUTE_CATEGORY_ID);
//			String categoryEntryID = element.getAttribute(ATTRIBUTE_CATEGORY_ENTRY_ID);
//			String name = element.getAttribute(ATTRIBUTE_NAME);
//			String iconString = element.getAttribute(ATTRIBUTE_ICON);
			try {
				Entry entry = (Entry) element.createExecutableExtension(ATTRIBUTE_ENTRY);
//				entry.setName(name);
//				if (checkString(iconString)) {
//					ImageDescriptor imageDescriptor = AbstractUIPlugin.imageDescriptorFromPlugin(
//							extension.getNamespaceIdentifier(), iconString);
//					if (imageDescriptor != null)
//						entry.setImage(imageDescriptor.createImage());										
//				}				
				List<Entry> entries = categoryID2Entries.get(categoryID);
				if (entries == null)
					entries = new ArrayList<Entry>();
								
				try {
					if (entry.getIndex() == -1 || entries.get(entry.getIndex()) != null) {
						if (entry instanceof AbstractEntry) {
							int index = entries.size();
							AbstractEntry abstractEntry = (AbstractEntry) entry;
							abstractEntry.setIndex(index);
						}											
					}					
				} catch (IndexOutOfBoundsException e) {
					if (entry instanceof AbstractEntry) {
						int index = entries.size();
						AbstractEntry abstractEntry = (AbstractEntry) entry;
						abstractEntry.setIndex(index);
					}										
				}
				
				entries.add(entry.getIndex(), entry);				
				categoryID2Entries.put(categoryID, entries);
			} catch (CoreException e) {
				throw new EPProcessorException(e);
			}
		}
	}

	private Map<String, Category> categoryID2Category = new HashMap<String, Category>();	
	private Map<String, List<Entry>> categoryID2Entries = new HashMap<String, List<Entry>>();
	private SortedMap<Category, List<Entry>> category2Entries = null;
	private Category fallBackCategory = null;
	
	public Category getFallbackCategory() {
		if (fallBackCategory == null) {
			CategoryImpl fallBackCategory = new CategoryImpl();
			fallBackCategory.setName("Other");
			this.fallBackCategory = fallBackCategory;
		}
		return fallBackCategory;
	}
	
	protected void check() {
		if (category2Entries == null) {
			checkProcessing();
			category2Entries = new TreeMap<Category, List<Entry>>(categoryComparator);
			for (Map.Entry<String, List<Entry>> mapEntry : categoryID2Entries.entrySet()) {
				Category category = categoryID2Category.get(mapEntry.getKey());
				List<Entry> entries = mapEntry.getValue();
				if (category == null)
					category = getFallbackCategory();
				
				category2Entries.put(category, entries);				
			}			
		}
	}
	
	public List<Entry> getEntries(Category category) {
		check();
		return category2Entries.get(category);
	}
	
	public Collection<Category> getCategories() {
		check();
		return category2Entries.keySet();
	}
	
	private Comparator<Category> categoryComparator = new Comparator<Category>(){	
		public int compare(Category c1, Category c2) {
			return c1.getIndex() - c2.getIndex();
		}	
	};
}
