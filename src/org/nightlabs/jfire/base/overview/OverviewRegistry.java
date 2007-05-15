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
			CategoryFactoryImpl category = new CategoryFactoryImpl();
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
				EntryFactory entryFactory = (EntryFactory) element.createExecutableExtension(ATTRIBUTE_ENTRY);
//				entry.setName(name);
//				if (checkString(iconString)) {
//					ImageDescriptor imageDescriptor = AbstractUIPlugin.imageDescriptorFromPlugin(
//							extension.getNamespaceIdentifier(), iconString);
//					if (imageDescriptor != null)
//						entry.setImage(imageDescriptor.createImage());										
//				}				
				List<EntryFactory> entryFactories = categoryID2Entries.get(categoryID);
				if (entryFactories == null)
					entryFactories = new ArrayList<EntryFactory>();
								
				try {
					if (entryFactory.getIndex() == -1 || entryFactories.get(entryFactory.getIndex()) != null) {
						if (entryFactory instanceof AbstractEntryFactory) {
							int index = entryFactories.size();
							AbstractEntryFactory abstractEntryFactory = (AbstractEntryFactory) entryFactory;
							abstractEntryFactory.setIndex(index);
						}											
					}					
				} catch (IndexOutOfBoundsException e) {
					if (entryFactory instanceof AbstractEntryFactory) {
						int index = entryFactories.size();
						AbstractEntryFactory abstractEntryFactory = (AbstractEntryFactory) entryFactory;
						abstractEntryFactory.setIndex(index);
					}										
				}
				
				entryFactories.add(entryFactory.getIndex(), entryFactory);				
				categoryID2Entries.put(categoryID, entryFactories);
			} catch (CoreException e) {
				throw new EPProcessorException(e);
			}
		}
	}

	private Map<String, CategoryFactory> categoryID2Category = new HashMap<String, CategoryFactory>();	
	private Map<String, List<EntryFactory>> categoryID2Entries = new HashMap<String, List<EntryFactory>>();
	private SortedMap<CategoryFactory, List<EntryFactory>> category2Entries = null;
	private CategoryFactory fallBackCategory = null;
	
	public CategoryFactory getFallbackCategory() {
		if (fallBackCategory == null) {
			CategoryFactoryImpl fallBackCategory = new CategoryFactoryImpl();
			fallBackCategory.setName("Other");
			this.fallBackCategory = fallBackCategory;
		}
		return fallBackCategory;
	}
	
	protected void check() {
		if (category2Entries == null) {
			checkProcessing();
			category2Entries = new TreeMap<CategoryFactory, List<EntryFactory>>(categoryComparator);
			for (Map.Entry<String, List<EntryFactory>> mapEntry : categoryID2Entries.entrySet()) {
				CategoryFactory categoryFactory = categoryID2Category.get(mapEntry.getKey());
				List<EntryFactory> entryFactories = mapEntry.getValue();
				if (categoryFactory == null)
					categoryFactory = getFallbackCategory();
				
				category2Entries.put(categoryFactory, entryFactories);				
			}			
		}
	}
	
	public List<EntryFactory> getEntries(CategoryFactory categoryFactory) {
		check();
		return category2Entries.get(categoryFactory);
	}
	
	public Collection<CategoryFactory> getCategories() {
		check();
		return category2Entries.keySet();
	}
	
	private Comparator<CategoryFactory> categoryComparator = new Comparator<CategoryFactory>(){	
		public int compare(CategoryFactory c1, CategoryFactory c2) {
			return c1.getIndex() - c2.getIndex();
		}	
	};
}
