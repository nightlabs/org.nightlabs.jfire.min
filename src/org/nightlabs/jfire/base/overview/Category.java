package org.nightlabs.jfire.base.overview;

import java.util.List;

import org.eclipse.swt.widgets.Composite;

/**
 * The object created by a {@link CategoryFactory}. The {@link Category} holds its
 * {@link Entry}s and is responsible of creating its own {@link Composite} that represents it.
 * 
 * @author Daniel.Mazurek [at] NightLabs [dot] de
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 */
public interface Category {

	/**
	 * Create the {@link Composite} of this Category that is responsible for displaying its entries.
	 * 
	 * @param composite The parent Composite
	 * @return The category {@link Composite}.
	 */
	Composite createCategoryComposite(Composite composite);

	/**
	 * Adds a new {@link Entry} for this {@link Category}.
	 * 
	 * @param entry The entry to add.
	 */
	void addEntry(Entry entry);
	
	/**
	 * Removes the given {@link Entry} from this {@link Category}.
	 * 
	 * @param entry The entry to remove.
	 */
	void removeEntry(Entry entry);
	
	/**
	 * Returns an unmodifiable list of the {@link Entry}s of this {@link Category}. 
	 * @return An unmodifiable list of {@link Entry}s of this {@link Category}.
	 */
	List<Entry> getEntries();
	
	/**
	 * Returns the {@link CategoryFactory} that created this {@link Category}.
	 * @return The {@link CategoryFactory} that created this {@link Category}.
	 */
	CategoryFactory getCategoryFactory();
	
}
