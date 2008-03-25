package org.nightlabs.jdo.query;

import java.beans.PropertyChangeListener;

/**
 * 
 * @author Marius Heinzmann - marius[at]nightlabs[dot]com
 * @deprecated use {@link PropertyChangeListener} instead!
 */
//public interface QueryChangeListener<T extends AbstractSearchQuery<?, ? super T>>
@Deprecated
public interface QueryChangeListener extends PropertyChangeListener
{
	/**
	 * Is called when a query of type <code>T</code> is changed. 
	 * @param e the event encapsulating all information about the changed query.
	 */
	void queryChanged(QueryEvent e);
}
