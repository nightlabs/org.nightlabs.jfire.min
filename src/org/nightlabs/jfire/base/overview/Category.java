package org.nightlabs.jfire.base.overview;

import java.util.List;

import org.eclipse.swt.widgets.Composite;

/**
 * The object created by a {@link CategoryFactory}. The {@link Category} holds its
 * {@link Entry}s and is responsible of creating its own {@link Composite} that represents it.
 * <p>
 * The {@link DefaultCategory}, which is usually used 
 * (default registration with no differing class specified for the {@link CategoryFactory}),
 * creates a Composite that displays all entries in a Table. ({@link DefaultCategoryComposite}).
 * </p>
 * 
 * @author Daniel.Mazurek [at] NightLabs [dot] de
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 */
public interface Category {

	/**
	 * Create the {@link Composite} of this Category that is 
	 * responsible for displaying its entries.
	 * <p>
	 * This method should only be called once per Category.
	 * Note, that implementations might ensure this and
	 * throw an exception if it is called more than once.
	 * </p>
	 * 
	 * @param composite The parent Composite
	 * @return The newly created category {@link Composite}.
	 */
	Composite createComposite(Composite composite);
	
	/**
	 * Returns the {@link Composite} of this Category that
	 * was created by {@link #createComposite(Composite)}.
	 * <p>
	 * This method should only be called after {@link #createComposite(Composite)} was called.
	 * Note, that implementations might ensure this and
	 * throw an exception if it is called before {@link #createComposite(Composite)}.
	 * </p>
	 * @return
	 */
	Composite getComposite();	

	/**
	 * Create the entries according to the extension registrations.
	 * <p>
	 * This method should only be called once per Category. 
	 * Note, that implementations might ensure this an throw an
	 * expception if createEntries() is called more than once.
	 * </p>
	 */
	void createEntries();

	/**
	 * Adds a new {@link Entry} for this {@link Category}.
	 * <p>
	 * This is usually called when the Category is created by {@link #createEntries()}.
	 * </p>
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
	 * Returns an unmodifiable list of the current {@link Entry}s of this {@link Category}.
	 * <p>
	 * This should not be called before {@link #createEntries()} was called.
	 * Note that implementations might ensure this and
	 * throw an exception if it is called before.
	 * </p> 
	 * @return An unmodifiable list of {@link Entry}s of this {@link Category}.
	 */
	List<Entry> getEntries();
	
	/**
	 * Returns the {@link CategoryFactory} that created this {@link Category}.
	 * @return The {@link CategoryFactory} that created this {@link Category}.
	 */
	CategoryFactory getCategoryFactory();
	
}
