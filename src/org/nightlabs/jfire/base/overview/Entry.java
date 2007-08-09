package org.nightlabs.jfire.base.overview;

import org.eclipse.swt.widgets.Composite;

/**
 * Object created by {@link EntryFactory}. {@link Entry}s are hold by {@link Category}s
 * which might choose to either display their entries by using
 * their descriptive information like {@link EntryFactory#getName()}, {@link EntryFactory#getImage()}
 * or ask the Entry itself to create the UI for its display ({@link #createComposite(Composite)}). 
 * <p>
 * Additionally an {@link Entry} might create an {@link EntryViewer}
 * that can be used to show detailed information about the entry.
 * </p>
 *  
 * @author Daniel.Mazurek [at] NightLabs [dot] de
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 */
public interface Entry 
{
	/**
	 * Performs what should be done when this {@link Entry} is activated. 
	 * This might be called when an item representing the Entry is double-clicked etc.
	 */
	void handleActivation();
	
	/**
	 * Creates the Composite representing this {@link Entry}.
	 * <p>
	 * This method should only be called once per Entry.
	 * Note, that implementations might ensure this and
	 * throw an exception if it is called more than once.
	 * </p>
	 * @return The newly created Composite of this {@link Entry}.
	 */
	Composite createComposite(Composite parent);

	/**
	 * Returns the {@link Composite} of this Entry that
	 * was created by {@link #createComposite(Composite)}.
	 * <p>
	 * This method should only be called after {@link #createComposite(Composite)} was called.
	 * Note, that implementations might ensure this and
	 * throw an exception if it is called before {@link #createComposite(Composite)}.
	 * </p>
	 * @return The {@link Composite} created by {@link #createComposite(Composite)}.
	 */
	Composite getComposite();	
	
	/**
	 * Create a new {@link EntryViewer} for this Entry.
	 * For one Entry any number of {@link EntryViewer}s might be created.
	 *  
	 * @return A new {@link EntryViewer} linked to this Entry.
	 */
	EntryViewer createEntryViewer();
	
	/**
	 * Returns the {@link EntryFactory} which created the Entry
	 */
	EntryFactory getEntryFactory();
}
