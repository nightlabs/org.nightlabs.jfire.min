/**
 * 
 */
package org.nightlabs.jfire.base.overview;

import org.eclipse.swt.widgets.Composite;

/**
 * Base {@link Entry} intended to be used with the {@link DefaultCategory}.
 * It implements {@link #createComposite(Composite)} but not create a Composite.
 * 
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 *
 */
public abstract class DefaultEntry extends AbstractEntry {

	/**
	 * 
	 */
	public DefaultEntry(EntryFactory entryFactory) {
		super(entryFactory);
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Returns <code>null</code>.
	 * </p>
	 * @see org.nightlabs.jfire.base.overview.Entry#createComposite(org.eclipse.swt.widgets.Composite)
	 */
	public Composite createComposite(Composite parent) {
		return null;
	}
	
	/**
	 * {@inheritDoc}
	 * <p>
	 * Returns <code>null</code>.
	 * </p>
	 * @see org.nightlabs.jfire.base.overview.Entry#createComposite(org.eclipse.swt.widgets.Composite)
	 */
	public Composite getComposite() {
		// TODO Auto-generated method stub
		return null;
	}
}
