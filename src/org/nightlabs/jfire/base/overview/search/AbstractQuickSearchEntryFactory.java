/**
 * 
 */
package org.nightlabs.jfire.base.overview.search;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.nightlabs.base.extensionpoint.AbstractEPProcessor;

/**
 * Abstract base class for {@link QuickSearchEntryFactory}s
 * 
 * @author Daniel Mazurek - daniel [at] nightlabs [dot] de
 */
public abstract class AbstractQuickSearchEntryFactory 
implements QuickSearchEntryFactory 
{
//	private Image composedDecoratorImage = null;
	private Image image = null;
	private Image decoratorImage = null;
	private String name = null;
	private String id = null;
	
	public Image getDecoratorImage() {
		return decoratorImage;
	}
	public void setDecoratorImage(Image decoratorImage) {
		this.decoratorImage = decoratorImage;
	}
	
	public Image getImage() {
		return image;
	}
	public void setImage(Image image) {
		this.image = image;
	}
	
//	public Image getComposedDecoratorImage() 
//	{
//		if (composedDecoratorImage == null && getDecoratorImage() != null) {
//			composedDecoratorImage = new SearchCompositeImage(getDecoratorImage()).createImage();
//		}
//		return composedDecoratorImage;
//	}
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	public void setInitializationData(IConfigurationElement config, String propertyName, Object data) 
	throws CoreException 
	{
		if (config.getName().equals(QuickSearchEntryRegistry.ELEMENT_QUICK_SEARCH_ENTRY_FACTORY)) {
			String decoratorString = config.getAttribute(QuickSearchEntryRegistry.ATTRIBUTE_DECORATOR_IMAGE);
			String iconString = config.getAttribute(QuickSearchEntryRegistry.ATTRIBUTE_IMAGE);
			String name = config.getAttribute(QuickSearchEntryRegistry.ATTRIBUTE_NAME);
			String idString = config.getAttribute(QuickSearchEntryRegistry.ATTRIBUTE_ID);
			if (AbstractEPProcessor.checkString(name)) {
				this.name = name;
			}
			if (AbstractEPProcessor.checkString(iconString)) {
				ImageDescriptor imageDescriptor = AbstractUIPlugin.imageDescriptorFromPlugin(
						config.getNamespaceIdentifier(), iconString);
				if (imageDescriptor != null)
					image = imageDescriptor.createImage();										
			}
			if (AbstractEPProcessor.checkString(decoratorString)) {
				ImageDescriptor imageDescriptor = AbstractUIPlugin.imageDescriptorFromPlugin(
						config.getNamespaceIdentifier(), decoratorString);
				if (imageDescriptor != null)
					decoratorImage = imageDescriptor.createImage();										
			}
			if (AbstractEPProcessor.checkString(idString)) {
				id = idString;				
			}
		}		
	}
	
}
