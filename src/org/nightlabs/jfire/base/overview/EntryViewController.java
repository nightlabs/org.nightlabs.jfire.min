package org.nightlabs.jfire.base.overview;

import org.eclipse.swt.widgets.Composite;

/**
 * The Controller Object which is responsible for the handling
 * of an {@link Entry}
 *  
 * @author Daniel.Mazurek [at] NightLabs [dot] de
 *
 */
public interface EntryViewController 
{
	/**
	 * returns the Composite created by the controller
	 * @return the Composite created by the controller
	 */
	Composite createComposite(Composite parent);
	
	/**
	 * returns the id of the {@link EntryViewController}
	 * @return the id of the {@link EntryViewController}
	 */
	String getID();
}
