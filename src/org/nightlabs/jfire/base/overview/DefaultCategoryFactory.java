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
 * The default {@link CategoryFactory} is used for &lt;categoryFactory&gt; registrations
 * that do not specify a different class. It will then create a {@link DefaultCategory}
 * in {@link #createCategory()}. 
 * <p>
 * This class is also intended to be subclassed when developing custom {@link Category}s
 * as it manages the registration attributes (name, icon, index). Override {@link #createCategory()}
 * to change the {@link Category} that should be created.
 * </p>
 * 
 * @author Daniel.Mazurek [at] NightLabs [dot] de
 *
 */
public class DefaultCategoryFactory 
implements CategoryFactory 
{
	public static final String ELEMENT_CATEGORY = "categoryFactory"; //$NON-NLS-1$
	public static final String ATTRIBUTE_NAME = "name"; //$NON-NLS-1$
	public static final String ATTRIBUTE_CATEGORY_ID = "categoryID"; //$NON-NLS-1$
	public static final String ATTRIBUTE_ICON = "icon"; //$NON-NLS-1$
	public static final String ATTRIBUTE_INDEX = "index"; //$NON-NLS-1$
	
	private String name;
	/**
	 * {@inheritDoc}
	 * @see org.nightlabs.jfire.base.overview.CategoryFactory#getName()
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
	 * {@inheritDoc}
	 * @see org.nightlabs.jfire.base.overview.CategoryFactory#getCategoryID()
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
	 * {@inheritDoc}
	 * @see org.nightlabs.jfire.base.overview.CategoryFactory#getImage()
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
	 * {@inheritDoc}
	 * @see org.nightlabs.jfire.base.overview.CategoryFactory#getIndex()
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

	/**
	 * {@inheritDoc}
	 * <p>
	 * Initializes the registration attributes categoryID, name, icon and index.
	 * </p>
	 * @see org.eclipse.core.runtime.IExecutableExtension#setInitializationData(org.eclipse.core.runtime.IConfigurationElement, java.lang.String, java.lang.Object)
	 */
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
	
	/**
	 * {@inheritDoc}
	 * @see org.nightlabs.jfire.base.overview.CategoryFactory#getEntryFactories()
	 */
	public List<EntryFactory> getEntryFactories() {
		return entryFactories;
	}
	
	/**
	 * {@inheritDoc}
	 * @see org.nightlabs.jfire.base.overview.CategoryFactory#createCategory()
	 */
	public Category createCategory() {
		return new DefaultCategory(this);
	}
}

