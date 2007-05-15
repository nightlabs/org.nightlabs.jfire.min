package org.nightlabs.jfire.base.overview;

import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.swt.graphics.Image;

/**
 * a categoryFactory is a root entry displayed in the overview perspective
 * categoryfactories can be registered via the extension-point org.nightlabs.jfire.base.overview
 *  
 * @author Daniel.Mazurek [at] NightLabs [dot] de
 *
 */
public interface CategoryFactory
extends IExecutableExtension
{
	/**
	 * returns the name of the category
	 * @return the name of the category
	 */
	String getName();
	
	/**
	 * returns the image of the category 
	 * @return the image of the category
	 */
	Image getImage();
	
	/**
	 * returns the id of the category 
	 * @return the id of the category
	 */
	public String getCategoryID();

	/**
	 * returns the index of the category
	 * @return the index of the category
	 */
	public int getIndex();
	
	/**
	 * returns the created Category
	 * @return the created Category
	 */
	public Category createCategory();
	
//	/**
//	 * sets the categoryID
//	 * @param categoryID the categoryID to set
//	 */
//	public void setCategoryID(String categoryID);
//	
//	/**
//	 * sets the name of the category
//	 * @param name the name to set
//	 */
//	public void setName(String name);
//	
//	/**
//	 * sets the image of the category
//	 * @param image the image to set
//	 */
//	public void setImage(Image image);
}
