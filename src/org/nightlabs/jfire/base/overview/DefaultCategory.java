/**
 * 
 */
package org.nightlabs.jfire.base.overview;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

/**
 * The default {@link Category} displays its {@link Entry}s in
 * an {@link DefaultCategoryComposite}.
 * <p>
 * This class is intended to be subclassed to inherit the 
 * handling of the entry list and overriding the {@link Composite}
 * used to display the entries. 
 * </p>
 * 
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 *
 */
public class DefaultCategory implements Category {

	private List<Entry> entries = new ArrayList<Entry>();
	private CategoryFactory categoryFactory;
	private Composite categoryComposite = null;
	

	/**
	 * Create a new {@link DefaultCategoryComposite}
	 * 
	 * @param categoryFactory The factory creating this category.
	 */
	public DefaultCategory(CategoryFactory categoryFactory) {
		this.categoryFactory = categoryFactory;
	}

	/** 
	 * {@inheritDoc}
	 * <p>
	 * This method is intended to be overridden in order to use
	 * other GUI to display the categorys entries.
	 * </p>
	 * @see org.nightlabs.jfire.base.overview.Category#createCategoryComposite(org.eclipse.swt.widgets.Composite)
	 */
	public Composite createCategoryComposite(Composite composite) {
		categoryComposite = new DefaultCategoryComposite(composite, SWT.NONE, this);
		return categoryComposite;
	}

	/**
	 * {@inheritDoc}
	 * @see org.nightlabs.jfire.base.overview.Category#addEntry(org.nightlabs.jfire.base.overview.Entry)
	 */
	public void addEntry(Entry entry) {
		if (categoryComposite != null)
			throw new UnsupportedOperationException("This category (DefaultCategory) does not support adding/removing of entries when its Composite was already created");
		synchronized (entries) {
			entries.add(entry);
		}
	}

	/**
	 * {@inheritDoc}
	 * @see org.nightlabs.jfire.base.overview.Category#getEntries()
	 */
	public List<Entry> getEntries() {
		return Collections.unmodifiableList(entries);
	}

	/**
	 * {@inheritDoc}
	 * @see org.nightlabs.jfire.base.overview.Category#removeEntry(org.nightlabs.jfire.base.overview.Entry)
	 */
	public void removeEntry(Entry entry) {
		if (categoryComposite != null)
			throw new UnsupportedOperationException("This category (DefaultCategory) does not support adding/removing of entries when its Composite was already created");
		synchronized (entries) {
			entries.remove(entry);
		}
	}

	/**
	 * {@inheritDoc}
	 * @see org.nightlabs.jfire.base.overview.Category#getCategoryFactory()
	 */
	public CategoryFactory getCategoryFactory() {
		return categoryFactory;
	}

}
