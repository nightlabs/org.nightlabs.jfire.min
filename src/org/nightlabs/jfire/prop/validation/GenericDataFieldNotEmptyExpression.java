/**
 * 
 */
package org.nightlabs.jfire.prop.validation;

import org.nightlabs.jfire.base.expression.IEvaluationContext;
import org.nightlabs.jfire.prop.DataField;
import org.nightlabs.jfire.prop.id.StructFieldID;

import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.Inheritance;

/**
 * {@link DataFieldExpression} that evaluates to true if the underlying
 * {@link DataField} to the given {@link StructFieldID} is not empty.
 *
 * @jdo.persistence-capable
 * 		persistence-capable-superclass="org.nightlabs.jfire.prop.validation.DataFieldExpression"
 * 		detachable="true"
 *
 * @jdo.inheritance
 * 		strategy="superclass-table"
 *
 * @author Daniel Mazurek - Daniel.Mazurek [dot] nightlabs [dot] de
 *
 */@PersistenceCapable(detachable="true")
@Inheritance(strategy=InheritanceStrategy.SUPERCLASS_TABLE)

public class GenericDataFieldNotEmptyExpression 
extends DataFieldExpression<DataField>
{
	private static final long serialVersionUID = 1L;

	/**
	 * @deprecated Only for JDO.
	 */
	@Deprecated
	protected GenericDataFieldNotEmptyExpression() {
	}

	public GenericDataFieldNotEmptyExpression(StructFieldID structFieldID) {
		super(structFieldID);
	}

	public GenericDataFieldNotEmptyExpression(String organisationID, long expressionID, StructFieldID structFieldID) {
		super(organisationID, expressionID, structFieldID);
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.prop.validation.IDataFieldExpression#evaluate(org.nightlabs.jfire.prop.validation.IPropertySetEvaluationContext)
	 */
	@Override
	public boolean evaluate(IPropertySetEvaluationContext<DataField> context) {
		for (DataField dataField : context.getDataFields(getStructFieldID())) {
			if (dataField.isEmpty())
				return false;
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.base.expression.IExpression#evaluate(org.nightlabs.jfire.base.expression.IEvaluationContext)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public boolean evaluate(IEvaluationContext context) {
		return evaluate((IPropertySetEvaluationContext<DataField>) context);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return toString("", "  ");
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.base.expression.IExpression#toString(java.lang.String, java.lang.String)
	 */
	@Override
	public String toString(String indent, String indentChar) {
		return indent + "( DataField not empty: " + getStructFieldID().toString() + " )";
	}
}
