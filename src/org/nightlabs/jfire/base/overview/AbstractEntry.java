/**
 * 
 */
package org.nightlabs.jfire.base.overview;

import org.nightlabs.annotation.Implement;

/**
 * The abstract base for {@link Entry}s holding their creating {@link EntryFactory}.
 * 
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 *
 */
public abstract class AbstractEntry implements Entry {

	private EntryFactory entryFactory;
	
	/**
	 * 
	 */
	public AbstractEntry(EntryFactory entryFactory) {
		this.entryFactory = entryFactory;
	}

	@Implement
	public EntryFactory getEntryFactory() {
		return entryFactory;
	}
}
