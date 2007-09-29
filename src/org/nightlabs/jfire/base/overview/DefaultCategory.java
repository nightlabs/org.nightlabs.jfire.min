/**
 * 
 */
package org.nightlabs.jfire.base.overview;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.nightlabs.base.ui.table.AbstractTableComposite;

/**
 * The default {@link Category} displays its {@link Entry}s in
 * a {@link DefaultCategoryComposite}.
 * 
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 *
 */
public class DefaultCategory extends AbstractCategory {

	private DefaultCategoryComposite categoryComposite = null;
	

	/**
	 * Create a new {@link DefaultCategoryComposite}.
	 * <p>
	 * Note that the {@link DefaultCategory} will 
	 * create its entries in the constructor.
	 * </p>
	 * 
	 * @param categoryFactory The factory creating this category.
	 */
	public DefaultCategory(CategoryFactory categoryFactory) {
		super(categoryFactory);
		createEntries();
	}

	/** 
	 * {@inheritDoc}
	 * <p>
	 * This method is intended to be overridden in order to use
	 * other GUI to display the categorys entries.
	 * </p>
	 * @see org.nightlabs.jfire.base.overview.Category#createComposite(org.eclipse.swt.widgets.Composite)
	 */
	public Composite createComposite(Composite composite) {
		categoryComposite = new DefaultCategoryComposite(composite, SWT.NONE, this, 
				AbstractTableComposite.DEFAULT_STYLE_SINGLE);
		return categoryComposite;
	}

	/**
	 * {@inheritDoc}
	 * @see org.nightlabs.jfire.base.overview.Category#getComposite()
	 */
	public Composite getComposite() {
		return categoryComposite;
	}
	
	/**
	 * {@inheritDoc}
	 * @see org.nightlabs.jfire.base.overview.AbstractCategory#updateCategoryComposite()
	 */
	@Override
	protected void updateCategoryComposite() {
		if (categoryComposite == null)
			return;
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				if (categoryComposite != null && !categoryComposite.isDisposed()) 
					categoryComposite.setInput(getEntries());
			}
		});
	}

}
