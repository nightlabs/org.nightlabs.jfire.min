package org.nightlabs.jfire.base.expression;

import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.Inheritance;

/**
 * Instances of this class represent a logical conjunction of multiple {@link IExpression}s.
 * A conjunction evaluates to <code>true</code> if <b>all</b> of its {@link IExpression}s
 * evaluate to <code>true</code> and to <code>false</code> otherwise.
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
 */
@PersistenceCapable(detachable="true")
@Inheritance(strategy=InheritanceStrategy.SUPERCLASS_TABLE)
public class AndCondition extends Composition
{
	private static final long serialVersionUID = 1L;

	public static final String OPERATOR_TEXT = "AND"; 
	
	/**
	 * @deprecated Only for JDO.
	 */
	@Deprecated
	protected AndCondition() {
	}

	public AndCondition(IExpression... expressions) {
		super(expressions);
	}

	@Override
	public boolean evaluate(IEvaluationContext context) {
		for (IExpression expression : getExpressions())
			if (!expression.evaluate(context))
				return false;

		return true;
	}

	@Override
	public String getOperatorText() {
		return OPERATOR_TEXT;
	}
}