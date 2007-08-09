/**
 * 
 */
package org.nightlabs.jfire.base.overview;

/**
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 *
 */
public abstract class AbstractEntryViewer implements EntryViewer {

	private Entry entry;
	
	/**
	 * 
	 */
	public AbstractEntryViewer(Entry entry) {
		this.entry = entry;
	}
	
	/**
	 * {@inheritDoc}
	 * @see org.nightlabs.jfire.base.overview.EntryViewer#getEntry()
	 */
	public Entry getEntry() {
		return entry;
	}

}
