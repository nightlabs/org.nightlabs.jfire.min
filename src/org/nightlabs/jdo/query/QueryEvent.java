package org.nightlabs.jdo.query;

import java.beans.PropertyChangeEvent;
import java.util.List;

import org.nightlabs.jdo.query.AbstractSearchQuery.FieldChangeCarrier;

/**
 * 
 * @author Marius Heinzmann - marius[at]nightlabs[dot]com
 */
public class QueryEvent
	extends PropertyChangeEvent
{
	/**
	 * The serial version id.
	 */
	private static final long serialVersionUID = 1L;
	
	public QueryEvent(AbstractSearchQuery<?> changedQuery, String propertyName, Object oldValue, Object newValue)
	{
		super(changedQuery, propertyName, oldValue, newValue);
	}

	/**
	 * @return the changedQuery
	 */
	public AbstractSearchQuery<?> getChangedQuery()
	{
		return (AbstractSearchQuery<?>) getSource();
	}
	
	private List<FieldChangeCarrier> changedFields;
	
	/**
	 * 
	 * @return
	 */
	public List<FieldChangeCarrier> getChangedFields()
	{
		if (getChangedQuery() == null)
			return null;
		
		if (changedFields == null)
		{
			changedFields = getChangedQuery().getChangedFields(getPropertyName());
		}

		return changedFields;
	}
}
