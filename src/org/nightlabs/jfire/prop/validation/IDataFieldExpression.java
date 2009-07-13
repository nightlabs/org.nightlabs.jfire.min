package org.nightlabs.jfire.prop.validation;

import org.nightlabs.jfire.base.expression.IExpression;
import org.nightlabs.jfire.prop.DataField;

/**
 * Interface for {@link IExpression}s that evaluate properties of {@link DataField}s.
 *
 * @author Tobias Langner <!-- tobias[dot]langner[at]nightlabs[dot]de -->
 * @param <DataFieldType>
 */
public interface IDataFieldExpression<DataFieldType extends DataField> extends IExpression
{
	/**
	 * Evaluates the condition in the given {@link IPropertySetEvaluationContext} and
	 * returns a boolean indicating the result of the validation.
	 *
	 * @param context The context in which the expression is to be evaluated.
	 * @return a boolean indicating the result of the validation.
	 */
	public boolean evaluate(IPropertySetEvaluationContext<DataFieldType> context);
}
