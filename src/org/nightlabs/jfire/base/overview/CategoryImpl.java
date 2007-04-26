package org.nightlabs.jfire.base.overview;

import java.util.Locale;

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
public class CategoryImpl 
implements Category 
{
	public static final String ELEMENT_CATEGORY = "category";
	public static final String ATTRIBUTE_NAME = "name";
	public static final String ATTRIBUTE_CATEGORY_ID = "categoryID";
	public static final String ATTRIBUTE_ICON = "icon";
	
	private String name;
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

	private String categoryID;
	public String getCategoryID() {
		return categoryID;
	}
	public void setCategoryID(String categoryID) {
		this.categoryID = categoryID;
	}
	
	private Image image;
	public Image getImage() {
		return image;
	}
	public void setImage(Image image) {
		this.image = image;
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
			CategoryImpl category = new CategoryImpl();
			category.setName(name);
			category.setCategoryID(categoryID);			
			if (AbstractEPProcessor.checkString(iconString)) {
				ImageDescriptor imageDescriptor = AbstractUIPlugin.imageDescriptorFromPlugin(
						element.getNamespaceIdentifier(), iconString);
				if (imageDescriptor != null)
					category.setImage(imageDescriptor.createImage());										
			}										
		}
	}
			
}
