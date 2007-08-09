package org.nightlabs.jfire.base.overview;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.nightlabs.base.extensionpoint.AbstractEPProcessor;

/**
 * @author Daniel.Mazurek [at] NightLabs [dot] de
 *
 */
public class DefaultCategoryFactory 
implements CategoryFactory 
{
	public static final String ELEMENT_CATEGORY = "categoryFactory";
	public static final String ATTRIBUTE_NAME = "name";
	public static final String ATTRIBUTE_CATEGORY_ID = "categoryID";
	public static final String ATTRIBUTE_ICON = "icon";
	public static final String ATTRIBUTE_INDEX = "index";
	
	private String name;
	/**
	 * returns the name.
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	/**
	 * sets the name
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	private String categoryID;
	/**
	 * returns the categoryID.
	 * @return the categoryID
	 */
	public String getCategoryID() {
		return categoryID;
	}
	/**
	 * sets the categoryID
	 * @param categoryID the categoryID to set
	 */
	public void setCategoryID(String categoryID) {
		this.categoryID = categoryID;
	}
	
	private Image image;
	/**
	 * returns the image.
	 * @return the image
	 */
	public Image getImage() {
		return image;
	}
	/**
	 * sets the image
	 * @param image the image to set
	 */
	public void setImage(Image image) {
		this.image = image;
	}
	
	private int index;	
	/**
	 * returns the index.
	 * @return the index
	 */
	public int getIndex() {
		return index;
	}	
	/**
	 * sets the index
	 * @param index the index to set
	 */
	public void setIndex(int index) {
		this.index = index;
	}
	
	public void setInitializationData(IConfigurationElement element,
			String propertyName, Object data) 
	throws CoreException 
	{
		if (element.getName().equals(ELEMENT_CATEGORY)) 
		{
			String categoryID = element.getAttribute(ATTRIBUTE_CATEGORY_ID);
			String name = element.getAttribute(ATTRIBUTE_NAME);
			String iconString = element.getAttribute(ATTRIBUTE_ICON);			
			String indexString = element.getAttribute(ATTRIBUTE_INDEX);
			int index = -1;
			try {
				index = Integer.valueOf(indexString);
			} catch (NumberFormatException e) {
				// Do nothing if index not valid
			}
			setName(name);
			setCategoryID(categoryID);
			setIndex(index);
			if (AbstractEPProcessor.checkString(iconString)) {
				ImageDescriptor imageDescriptor = AbstractUIPlugin.imageDescriptorFromPlugin(
						element.getNamespaceIdentifier(), iconString);
				if (imageDescriptor != null)
					setImage(imageDescriptor.createImage());										
			}										
		}
	}
	
	private List<EntryFactory> entryFactories = new ArrayList<EntryFactory>();
	
	public List<EntryFactory> getEntryFactories() {
		return entryFactories;
	}
	
	public Category createCategory() {
		return new DefaultCategory(this);
	}
}

