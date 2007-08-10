/**
 * 
 */
package org.nightlabs.jfire.base.overview;

/**
 * Abstract base for {@link EntryViewer}s holding the entry that created it.
 * 
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
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
