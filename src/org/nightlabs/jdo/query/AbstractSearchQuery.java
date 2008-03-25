package org.nightlabs.jdo.query;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * The abstract base class of all {@link SearchQuery}s.
 * 
 * @param <R> the result type of me.
 * @author Marius Heinzmann - marius[at]nightlabs[dot]com
 */
public abstract class AbstractSearchQuery<R>
	implements Serializable, SearchQuery<R>
{
	private Class<? extends R> resultType;
	private transient Collection<?> candidates;
	protected long fromInclude = 0;
	protected long toExclude = Long.MAX_VALUE;
	
	// Property IDs used for the PropertyChangeListeners
	private static final String PROPERTY_PREFIX = "AbstractSearchQuery.";
	public static final String PROPERTY_FROM_INCLUDE = PROPERTY_PREFIX + "fromInclude";
	public static final String PROPERTY_TO_EXCLUDE = PROPERTY_PREFIX + "toExclude";
	public static final String PROPERTY_WHOLE_QUERY = PROPERTY_PREFIX + "wholeQuery";

	public AbstractSearchQuery()
	{
		resultType = init();
		assert resultType != null;
		pcs = new PropertyChangeSupport(this);
	}

	/**
	 * This method has to return the runtime class of the declared result type and may do additional
	 * initialisation.
	 */
	protected abstract Class<? extends R> init();

	/* (non-Javadoc)
	 * @see org.nightlabs.jdo.query.SearchQuery#executeQuery()
	 */
	public abstract Collection<R> getResult();

	/**
	 * Returns the candidates from which this query will filter the possible result.
	 * @return the candidates from which this query will filter the possible result.
	 */
	@Override
	public Collection<?> getCandidates()
	{
		return candidates;
	}

	/**
	 * Sets the candidates from which to filter the possible result.
	 * @param candidates the candidates from which to filter the possible result.
	 */
	@Override
	public void setCandidates(Collection<?> candidates)
	{
		this.candidates = candidates;
	}

	/**
	 * Returns the class of the result type.
	 * @return the class of the result type.
	 */
	protected Class<? extends R> getResultType()
	{
		return resultType;
	}

	/**
	 * returns the range beginning including the given value.
	 * @return the 0-based inclusive start index.
	 */
	protected long getFromInclude()
	{
		return fromInclude;
	}

	/**
	 * sets the the range beginning including the given value.
	 * @param fromInclude 0-based inclusive start index.
	 */
	protected void setFromInclude(long fromInclude)
	{
		final long oldInclude = this.fromInclude;
		this.fromInclude = fromInclude;
		notifyListeners(PROPERTY_FROM_INCLUDE, oldInclude, fromInclude);
	}

	/**
	 * returns the range end excluding the given value.
	 * @return the 0-based exclusive end index, or {@link Long#MAX_VALUE} for no limit.
	 */
	protected long getToExclude()
	{
		return toExclude;
	}
	
	/**
	 * Sets the range end excluding the given value.
	 * @param toExclude 0-based exclusive end index, or {@link Long#MAX_VALUE} for no limit.
	 */
	protected void setToExclude(long toExclude)
	{
		final long oldToExclude = this.toExclude;
		this.toExclude = toExclude;
		notifyListeners(PROPERTY_TO_EXCLUDE, oldToExclude, toExclude);
	}
	
	/**
	 * Support object managing all property change listeners.
	 */
	private transient PropertyChangeSupport pcs; 
	
	/**
	 * Internal method that shall be used to notify all pcs that I have changed. 
	 */
	protected void notifyListeners(String propertyName, Object oldValue, Object newValue)
	{
		if (pcs == null)
			return;
		
		final QueryEvent event = new QueryEvent(this, propertyName, oldValue, newValue);
		pcs.firePropertyChange(event);			
	}
	
	/**
	 * Adds the given listener to my list of listeners, if not already in the list.
	 * @param listener the listener to add.
	 */
	@Override
	public void addQueryChangeListener(PropertyChangeListener listener)
	{
		if (pcs != null && ! Arrays.asList(pcs.getPropertyChangeListeners()).contains(listener))
		{
			pcs.addPropertyChangeListener(listener);
		}
	}
	
	/**
	 * Removes the given listener from my list of listeners.
	 * @param listener the listener to remove.
	 */
	@Override
	public void removeQueryChangeListener(PropertyChangeListener listener)
	{
		if (pcs == null)
			return;
		
		pcs.removePropertyChangeListener(listener);
	}

	@Override
	public List<FieldChangeCarrier> getChangedFields(String propertyName)
	{
		assert propertyName != null;
		List<FieldChangeCarrier> changedFields = new LinkedList<FieldChangeCarrier>();
		return changedFields;
	}
	
	/**
	 * Carrier that contains the changed field property name and its new value;
	 * 
	 * @author Marius Heinzmann - marius[at]nightlabs[dot]com
	 */
	public static class FieldChangeCarrier
	{
		private String propertyName;
		private Object newValue;
		
		/**
		 * @param propertyName
		 * @param oldValue
		 * @param newValue
		 */
		public FieldChangeCarrier(String propertyName, Object newValue)
		{
			assert propertyName != null;
			this.propertyName = propertyName;
			this.newValue = newValue;
		}

		/**
		 * @return the propertyName
		 */
		public String getPropertyName()
		{
			return propertyName;
		}

		/**
		 * @return the newValue
		 */
		public Object getNewValue()
		{
			return newValue;
		}
	}
}
