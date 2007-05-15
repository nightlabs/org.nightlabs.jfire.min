package org.nightlabs.jfire.base.overview;

import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.swt.graphics.Image;
import org.nightlabs.i18n.I18nText;

/**
 * Represents an factory which creates entries for a category in the overview perspective
 * 
 * @author Daniel.Mazurek [at] NightLabs [dot] de
 *
 */
public interface EntryFactory 
extends IExecutableExtension
{	
	/**
	 * returns the {@link Entry} for the entry
	 * 
	 * @return the {@link Entry} for the entry
	 */
	Entry createEntry();
	
	/**
	 * returns the multilanguage capable name as {@link I18nText}
	 * @return the multilanguage capable name
	 */
	String getName();
		
	/**
	 * returns the Image for the entry
	 * @return the Image for the entry
	 */
	Image getImage();
		
	/**
	 * returns the index of the entry
	 * @return the index of the entry
	 */
	int getIndex();
}
