/**
 * 
 */
package org.nightlabs.jfire.prop.validation;

import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.PersistenceCapable;

import org.nightlabs.jfire.base.expression.IEvaluationContext;
import org.nightlabs.jfire.prop.DataField;
import org.nightlabs.jfire.prop.id.StructFieldID;

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

	@Override
	public boolean evaluate(IPropertySetEvaluationContext<DataField> context) {
		for (DataField dataField : context.getDataFields(getStructFieldID())) {
			if (dataField.isEmpty())
				return false;
		}
		return true;
	}

	@Override
	public boolean evaluate(IEvaluationContext context) {
		return evaluate((IPropertySetEvaluationContext<DataField>) context);
	}

	@Override
	public String toString() {
		return toString("", "  ");
	}

	@Override
	public String toString(String indent, String indentChar) {
		return indent + "( DataField not empty: " + getStructFieldID().toString() + " )";
	}
}
