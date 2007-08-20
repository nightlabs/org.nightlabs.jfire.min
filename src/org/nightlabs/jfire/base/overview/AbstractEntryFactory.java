package org.nightlabs.jfire.base.overview;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * Base class for {@link EntryFactory}s that manages
 * the properties defined in the extension (id, name, icon).
 * 
 * @author Daniel.Mazurek [at] NightLabs [dot] de
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 */
public abstract class AbstractEntryFactory 
implements EntryFactory 
{
	public static final String ELEMENT_CATEGORY_ENTRY = "categoryEntry"; //$NON-NLS-1$
	public static final String ATTRIBUTE_NAME = "name"; //$NON-NLS-1$
	public static final String ATTRIBUTE_ICON = "icon"; //$NON-NLS-1$
	public static final String ATTRIBUTE_INDEX = "index"; //$NON-NLS-1$
	public static final String ATTRIBUTE_ID = "categoryEntryID"; //$NON-NLS-1$
	
	public AbstractEntryFactory() {		
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
	
	private String name = null;
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

	private int index = -1;
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
	
	private String id = null;
	/**
	 * returns the id.
	 * @return the id
	 */
	public String getID() {
		return id;
	}
	/**
	 * sets the id
	 * @param name the id to set
	 */
	public void setID(String id) {
		this.id = id;
	}  
	
	
	public void setInitializationData(IConfigurationElement element,
			String propertyName, Object data) 
	throws CoreException 
	{
		String name = element.getAttribute(ATTRIBUTE_NAME);
		String iconString = element.getAttribute(ATTRIBUTE_ICON);
		String indexString = element.getAttribute(ATTRIBUTE_INDEX);
		String id = element.getAttribute(ATTRIBUTE_ID);
		if (checkString(name))
			setName(name);
		if (checkString(iconString)) {
			ImageDescriptor imageDescriptor = AbstractUIPlugin.imageDescriptorFromPlugin(
					element.getNamespaceIdentifier(), iconString);
			if (imageDescriptor != null)
				setImage(imageDescriptor.createImage());										
		}
		if (checkString(indexString)) {
			try {
				int index = Integer.valueOf(indexString);
				setIndex(index);
			} catch (NumberFormatException e) {
				
			}			
		}
		if (checkString(id))
			setID(id);
		else
			throw new IllegalArgumentException("An EntryFactory has to have an id set. Extension came from " + element.getNamespaceIdentifier()); //$NON-NLS-1$
		
	}
	
	protected boolean checkString(String s) 
	{
		if (s == null || s.trim().equals("") ) //$NON-NLS-1$
			return false;
		
		return true;
	}

}
