package org.nightlabs.jfire.base.overview;

import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.swt.graphics.Image;

/**
 * Represents a factory which creates entries for a category.
 * <p>
 * {@link EntryFactory}s are registered as extensions an have 
 * the scope of a {@link CategoryFactory} (its id, in fact).
 * This way entries are linked to categories.
 * </p>
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
	 * Returns the Image for this factory.
	 * @return The Image for this factory.
	 */
	Image getImage();
		
	/**
	 * Returns the index of this factory.
	 * @return Te index of this factory.
	 */
	int getIndex();
	
	/**
	 * Returns the id of this factory.
	 * @return Tthe id of this factory.
	 */
	String getID();
}
