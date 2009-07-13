package org.nightlabs.jfire.prop.validation;

import java.util.Collection;

import org.nightlabs.jfire.base.expression.IEvaluationContext;
import org.nightlabs.jfire.prop.DataField;
import org.nightlabs.jfire.prop.id.StructFieldID;

/**
 * Interface for {@link IEvaluationContext} in {@link org.nightlabs.jfire.prop.PropertySet}s.
 *
 * @author Tobias Langner <!-- tobias[dot]langner[at]nightlabs[dot]de -->
 * @param <DataFieldType>
 */
public interface IPropertySetEvaluationContext<DataFieldType extends DataField> extends IEvaluationContext
{
	/**
	 * Returns the {@link DataField}s that correspond to the ID of the given {@link org.nightlabs.jfire.prop.StructField}.
	 * @param structFieldID The ID of the {@link StructFieldID} whose corresponding {@link DataField}s are to be returned.
	 * @return the {@link DataField}s that correspond to the ID of the given {@link org.nightlabs.jfire.prop.StructField}.
	 */
	public Collection<DataFieldType> getDataFields(StructFieldID structFieldID);
}
