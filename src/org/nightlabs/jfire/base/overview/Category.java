package org.nightlabs.jfire.base.overview;

import org.eclipse.swt.graphics.Image;

/**
 * a category is a root entry displayed in the overview perspective
 * categories can be registered via the extension-point org.nightlabs.jfire.base.overview
 *  
 * @author Daniel.Mazurek [at] NightLabs [dot] de
 *
 */
public interface Category
//extends IExecutableExtension
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
	 * sets the categoryID
	 * @param categoryID the categoryID to set
	 */
	public void setCategoryID(String categoryID);
	
	/**
	 * sets the name of the category
	 * @param name the name to set
	 */
	public void setName(String name);
	
	/**
	 * sets the image of the category
	 * @param image the image to set
	 */
	public void setImage(Image image);
}
