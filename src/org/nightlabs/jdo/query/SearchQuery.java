package org.nightlabs.jdo.query;

import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.List;

import org.nightlabs.jdo.query.AbstractSearchQuery.FieldChangeCarrier;

/**
 * Defines all methods necessary to search for the given result type.
 * 
 * @param <R> the result type of me.
 * @author Marius Heinzmann - marius[at]nightlabs[dot]com
 */
public interface SearchQuery<R>
{
	/**
	 * The actual search is done in this method and the result is returned.
	 * 
	 * @return Returns the result of the query. Never returns <code>null</code>!
	 */
	Collection<? extends R> getResult();

	/**
	 * Sets the candidates from which to filter the possible result.
	 * @param candidates the candidates from which to filter the possible result.
	 */
	void setCandidates(Collection<?> candidates);

	/**
	 * Returns the candidates from which this query will filter the possible result.
	 * @return the candidates from which this query will filter the possible result.
	 */
	Collection<?> getCandidates();
	
	/**
	 * Returns a list of {@link FieldChangeCarrier}s that are combined by the given
	 * <code>propertyName</code>.
	 * 
	 * @param propertyName the name of the property for which to return all changed fields.
	 * @return a list of {@link FieldChangeCarrier}s that are combined by the given
	 * <code>propertyName</code>.
	 */
	List<FieldChangeCarrier> getChangedFields(String propertyName);
	
	/**
	 * Adds the given listener to my list of listeners, if not already in the list.
	 * @param listener the listener to add.
	 */
	void addQueryChangeListener(PropertyChangeListener listener);
	
	/**
	 * Removes the given listener from my list of listeners.
	 * @param listener the listener to remove.
	 */
	void removeQueryChangeListener(PropertyChangeListener listener);
}
