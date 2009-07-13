package org.nightlabs.jfire.base.expression;

import java.io.Serializable;

/**
 * Base interface for all classes in the expression framework. Implementations of this
 * interface can be evaluated with a given {@link IEvaluationContext} resulting in a
 * boolean value of either <code>true</code> or <code>false</code>.
 *
 * @author Tobias Langner <!-- tobias[dot]langner[at]nightlabs[dot]de -->
 */
public interface IExpression extends Serializable{

	/**
	 * A constant representing the JDO fetch group that returns <b>all</b> data of the expression.
	 * All implementations should declare this fetch group with all of its fields included.
	 */
	String FETCH_GROUP_IEXPRESSION_FULL_DATA = "IExpression.fullData";

	/**
	 * Evaluates the expession under the given {@link IEvaluationContext} and returns a boolean
	 * value indicating the result of the evaluation.
	 *
	 * @param context The context in which the expression is to be evalutated.
	 * @return a boolean indicating the result of the evaluation.
	 */
	boolean evaluate(IEvaluationContext context);

	/**
	 * Returns a string representation of the expression.
	 *
	 * @param indent The indent string that should prefix every line returned by this method.
	 * @param indentChar The string representing a single indent level.
	 * @return a string representation of the expression.
	 */
	String toString(String indent, String indentChar);
}