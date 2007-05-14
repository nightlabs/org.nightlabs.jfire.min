package org.nightlabs.jfire.base.overview;

import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.swt.graphics.Image;
import org.nightlabs.i18n.I18nText;

/**
 * Represents an entry of an category in the overview perspective
 * 
 * @author Daniel.Mazurek [at] NightLabs [dot] de
 *
 */
public interface Entry 
extends IExecutableExtension
{
	/**
	 * opens the entry
	 * 
	 * Implementations should handle here what action should be performed
	 * when the entry is opened
	 */
	void openEntry();
	
	/**
	 * returns the {@link EntryViewController} for the entry
	 * 
	 * @return the {@link EntryViewController} for the entry
	 */
	EntryViewController createEntryViewController();
	
	/**
	 * returns the multilanguage capable name as {@link I18nText}
	 * @return the multilanguage capable name
	 */
	String getName();
	
//	/**
//	 * sets the the multilanguage capable name as {@link I18nText}
//	 * @param name the {@link I18nText} to set
//	 */
//	void setName(I18nText name);
	
	/**
	 * returns the Image for the entry
	 * @return the Image for the entry
	 */
	Image getImage();
	
//	/**
//	 * sets the image
//	 * @param image the image to set
//	 */
//	void setImage(Image image);
	
	/**
	 * returns the index of the entry
	 * @return the index of the entry
	 */
	int getIndex();
}
