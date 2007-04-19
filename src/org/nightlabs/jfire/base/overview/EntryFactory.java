package org.nightlabs.jfire.base.overview;

import org.eclipse.swt.graphics.Image;
import org.nightlabs.i18n.I18nText;

/**
 * This factory creates {@link Entry}
 * 
 * @author Daniel.Mazurek [at] NightLabs [dot] de
 *
 */
public interface EntryFactory 
{
	/**
	 * returns the {@link Entry} created by this factory
	 * @return the Entry created by this factory
	 */
	Entry createEntry();
	
	/**
	 * returns the multilanguage capable name as {@link I18nText}
	 * @return the multilanguage capable name
	 */
	I18nText getName();
	/**
	 * sets the the multilanguage capable name as {@link I18nText}
	 * @param name the {@link I18nText} to set
	 */
	void setName(I18nText name);
	
	/**
	 * returns the Image for the entry
	 * @return the Image for the entry
	 */
	Image getImage();
	
	/**
	 * sets the image
	 * @param image the image to set
	 */
	void setImage(Image image);
}
