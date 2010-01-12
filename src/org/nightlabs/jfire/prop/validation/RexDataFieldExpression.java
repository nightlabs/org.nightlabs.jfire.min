package org.nightlabs.jfire.prop.validation;

import java.util.regex.PatternSyntaxException;

import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.PersistenceCapable;

import org.nightlabs.jfire.base.expression.IEvaluationContext;
import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.prop.datafield.RegexDataField;
import org.nightlabs.jfire.prop.id.StructFieldID;
import org.nightlabs.jfire.prop.structfield.RegexStructField;

@PersistenceCapable(detachable="true")
@Inheritance(strategy=InheritanceStrategy.SUPERCLASS_TABLE)
public class RexDataFieldExpression
extends DataFieldExpression<RegexDataField>
{
	private static final long serialVersionUID = 1L;
	
//	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
//	private IStruct struct;
	
	public RexDataFieldExpression(/*IStruct struct, */StructFieldID structFieldID) {
		this(IDGenerator.getOrganisationID(), IDGenerator.nextID(DataFieldExpression.class), structFieldID);
//		this.struct = struct;
	}
	
	public RexDataFieldExpression(String organisationID, long expressionID, StructFieldID structFieldID) {
		super(organisationID, expressionID, structFieldID);
	}
	
	@Override
	public boolean evaluate(
			IPropertySetEvaluationContext<RegexDataField> context) {
		for (RegexDataField dataField : context.getDataFields(getStructFieldID())) {
			try {
				RegexStructField structField = (RegexStructField) dataField.getStructField();
				String value = dataField.getText();
				return structField.validateValue(value == null ? "" : value);
			} catch (PatternSyntaxException e) {
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean evaluate(IEvaluationContext context) {
		return evaluate(context);
	}

	@Override
	public String toString() {
		return toString("", "  ");
	}

	@Override
	public String toString(String indent, String indentChar) {
		return indent + "( DataField not correct: " + getStructFieldID().toString() + " )";
	}
}