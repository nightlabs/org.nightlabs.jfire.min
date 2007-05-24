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
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 *
 */
public class DefaultCategory implements Category {

	private List<Entry> entries = new ArrayList<Entry>();
	private CategoryFactory categoryFactory;
	private Composite categoryComposite = null;
	
	/**
	 * 
	 */
	public DefaultCategory(CategoryFactory categoryFactory) {
		this.categoryFactory = categoryFactory;
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.base.overview.Category#createCategoryComposite(org.eclipse.swt.widgets.Composite)
	 */
	public Composite createCategoryComposite(Composite composite) {
		categoryComposite = new DefaultCategoryComposite(composite, SWT.NONE, this);
		return categoryComposite;
	}

	public void addEntry(Entry entry) {
		if (categoryComposite != null)
			throw new UnsupportedOperationException("This category (DefaultCategory) does not support adding/removing of entries when its Composite was already created");
		synchronized (entries) {
			entries.add(entry);
		}
	}

	public List<Entry> getEntries() {
		return Collections.unmodifiableList(entries);
	}

	public void removeEntry(Entry entry) {
		if (categoryComposite != null)
			throw new UnsupportedOperationException("This category (DefaultCategory) does not support adding/removing of entries when its Composite was already created");
		synchronized (entries) {
			entries.remove(entry);
		}
	}

	public CategoryFactory getCategoryFactory() {
		return categoryFactory;
	}

}
