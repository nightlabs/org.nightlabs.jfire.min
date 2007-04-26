package org.nightlabs.jfire.base.overview;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * @author Daniel.Mazurek [at] NightLabs [dot] de
 *
 */
public abstract class AbstractEntry 
implements Entry 
{
	public static final String ELEMENT_CATEGORY_ENTRY = "categoryEntry";
	public static final String ATTRIBUTE_NAME = "name";
	public static final String ATTRIBUTE_ICON = "icon";
	
	public AbstractEntry() {		
	}

	private Image image;
	public Image getImage() {
		return image;
	}
	public void setImage(Image image) {
		this.image = image;
	}
	
	private String name = null;
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

	public void setInitializationData(IConfigurationElement element,
			String propertyName, Object data) 
	throws CoreException 
	{
		String name = element.getAttribute(ATTRIBUTE_NAME);
		String iconString = element.getAttribute(ATTRIBUTE_ICON);
		if (checkString(name))
			setName(name);
		if (checkString(iconString)) {
			ImageDescriptor imageDescriptor = AbstractUIPlugin.imageDescriptorFromPlugin(
					element.getNamespaceIdentifier(), iconString);
			if (imageDescriptor != null)
				setImage(imageDescriptor.createImage());										
		}				
	}
	
	protected boolean checkString(String s) 
	{
		if (s == null || s.trim().equals("") )
			return false;
		
		return true;
	}  	
}
