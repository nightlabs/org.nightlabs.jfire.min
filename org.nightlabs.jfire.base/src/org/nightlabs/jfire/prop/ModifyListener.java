package org.nightlabs.jfire.prop;

/**
 * Listener that can be used to track changes in {@link PropertySet} structure and data.
 * 
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 */
public interface ModifyListener {
	/**
	 * Called to notify the listener of a change.
	 */
	void modifyData();
}
