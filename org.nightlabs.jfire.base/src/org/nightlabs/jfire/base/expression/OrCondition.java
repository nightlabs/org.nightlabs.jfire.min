package org.nightlabs.jfire.base.expression;

import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.PersistenceCapable;

/**
 * Instances of this class represent a logical disjunction of multiple {@link IExpression}s.
 * A disjunction evaluates to <code>false</code> if <b>all</b> of its {@link IExpression}s
 * evaluate to <code>false</code> and to <code>true</code> otherwise.
 *
 * @author Tobias Langner <!-- tobias[dot]langner[at]nightlabs[dot]de -->
 *
 *
 *
 * @jdo.persistence-capable
 * 		persistence-capable-superclass="org.nightlabs.jfire.base.expression.Composition"
 *    detachable="true"
 *
 * @jdo.inheritance
 * 		strategy="superclass-table"
 *
 * @author Tobias Langner <!-- tobias[dot]langner[at]nightlabs[dot]de -->
 */
@PersistenceCapable(detachable="true")
@Inheritance(strategy=InheritanceStrategy.SUPERCLASS_TABLE)
public class OrCondition extends Composition
{
	private static final long serialVersionUID = 1L;

	public static final String OPERATOR_TEXT = "OR";

	/**
	 * @deprecated Only for JDO.
	 */
	@Deprecated
	protected OrCondition() {
	}

	public OrCondition(IExpression... expressions) {
		super(expressions);
	}

	@Override
	public boolean evaluate(IEvaluationContext context) {
		for (IExpression expression : getExpressions())
			if (expression != null && expression.evaluate(context))
				return true;

		return false;
	}

	@Override
	public String getOperatorText() {
		return OPERATOR_TEXT;
	}
}
