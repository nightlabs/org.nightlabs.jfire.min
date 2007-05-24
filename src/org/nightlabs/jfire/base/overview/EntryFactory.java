package org.nightlabs.jfire.base.overview;

import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.swt.graphics.Image;

/**
 * Represents an factory which creates entries for a category.
 * 
 * @author Daniel.Mazurek [at] NightLabs [dot] de
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 */
public interface EntryFactory 
extends IExecutableExtension
{	
	/**
	 * Creates a new {@link Entry}.
	 * 
	 * @return A new {@link Entry}.
	 */
	Entry createEntry();
	
	/**
	 * Returns the name of this factory.
	 * @return The name of this factory.
	 */
	String getName();
		
	/**
	 * returns the Image for this factory.
	 * @return the Image for this factory.
	 */
	Image getImage();
		
	/**
	 * returns the index of this factory.
	 * @return the index of this factory.
	 */
	int getIndex();
}
