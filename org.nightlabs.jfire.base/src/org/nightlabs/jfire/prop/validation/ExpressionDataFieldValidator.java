package org.nightlabs.jfire.prop.validation;

import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.FetchGroups;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.PersistenceModifier;
import javax.jdo.annotations.Persistent;

import org.nightlabs.jfire.base.expression.IExpression;
import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.prop.DataField;
import org.nightlabs.jfire.prop.StructField;
import org.nightlabs.util.NLLocale;

/**
 * Implementation of {@link IPropertySetValidator} that is able to validate a {@link DataField}
 * based on an {@link IExpression}.
 *
 * @jdo.persistence-capable
 * 		identity-type="application"
 * 		persistence-capable-superclass="org.nightlabs.jfire.prop.validation.DataFieldValidator"
 * 		table="JFireBase_Prop_ExpressionDataFieldValidator"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.fetch-group name "IStruct.fullData" fetch-groups="default" fields="expression, validationResult"
 *
 * @author Tobias Langner <!-- tobias[dot]langner[at]nightlabs[dot]de -->
 */
@PersistenceCapable(
	identityType=IdentityType.APPLICATION,
	table="JFireBase_Prop_ExpressionDataFieldValidator")
@FetchGroups(
	@FetchGroup(
		fetchGroups={"default"},
		name="IStruct.fullData",
		members={@Persistent(name="expression"), @Persistent(name="validationResult")})
)
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class ExpressionDataFieldValidator<DataFieldType extends DataField, StructFieldType extends StructField<DataFieldType>>
extends DataFieldValidator<DataFieldType, StructFieldType>
implements IExpressionValidator
{
	private static final long serialVersionUID = 1L;
	
	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private IDataFieldExpression<DataFieldType> expression;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 * 			  dependent="true"
	 */
	@Persistent(
		dependent="true",
		persistenceModifier=PersistenceModifier.PERSISTENT)
	private I18nValidationResult validationResult;
	
	/**
	 * @deprecated Only for JDO.
	 */
	@Deprecated
	protected ExpressionDataFieldValidator() {
	}

//	/**
//	 * Creates a new instance with the given expression, message and validation result type used when the expression evaluates
//	 * to <code>false</code>; the primary key is auto-generated using the {@link IDGenerator}.
//	 *
//	 * @param expression The expression to be used for the validation.
//	 * @param message The message that should be displayed to be used when expression evaluates to <code>false.
//	 * @param validationResultType The type of the validation result to be used when expression evaluates to <code>false</code>.
//	 */
//	public ExpressionDataFieldValidator(IDataFieldExpression<DataFieldType> expression, String message, ValidationResultType validationResultType) {
//		this(IDGenerator.getOrganisationID(), IDGenerator.nextID(DataFieldValidator.class), expression, message, validationResultType);
//	}

	/**
	 * Creates a new instance with the given expression, message and validation result type used when the expression evaluates
	 * to <code>false</code>; the primary key is auto-generated using the {@link IDGenerator}.
	 *
	 * @param expression The expression to be used for the validation.
	 * @param message The message that should be displayed to be used when expression evaluates to <code>false.
	 * @param validationResultType The type of the validation result to be used when expression evaluates to <code>false</code>.
	 * @param structField
	 */
	public ExpressionDataFieldValidator(IDataFieldExpression<DataFieldType> expression, String message, ValidationResultType validationResultType, StructField<?> structField) {
		this(IDGenerator.getOrganisationID(), IDGenerator.nextID(DataFieldValidator.class), expression, message, validationResultType, structField);
	}
	
//	/**
//	 * Creates a new instance with the given expression, message and validation result type used when the expression evaluates
//	 * to <code>false</code> and the given primary key.
//	 *
//	 * @param organisationID The organisation ID of the primary key.
//	 * @param validatorID The validator ID of the primary key.
//	 * @param expression The expression to be used for the validation.
//	 * @param message The message that should be displayed to be used when expression evaluates to <code>false.
//	 * @param validationResultType The type of the validation result to be used when expression evaluates to <code>false</code>.
//	 */
//	public ExpressionDataFieldValidator(String organisationID, long validatorID, IDataFieldExpression<DataFieldType> expression,
//                                       String message, ValidationResultType validationResultType)
//	{
//		super(organisationID, validatorID);
//		this.expression = expression;
//		this.validationResult = new I18nValidationResult(organisationID, IDGenerator.nextID(I18nValidationResult.class),
//				validationResultType);
//		this.validationResult.getI18nValidationResultMessage().setText(NLLocale.getDefault().getLanguage(), message);
//	}

	/**
	 * Creates a new instance with the given expression, message and validation result type used when the expression evaluates
	 * to <code>false</code> and the given primary key.
	 *
	 * @param organisationID The organisation ID of the primary key.
	 * @param validatorID The validator ID of the primary key.
	 * @param expression The expression to be used for the validation.
	 * @param message The message that should be displayed to be used when expression evaluates to <code>false.
	 * @param validationResultType The type of the validation result to be used when expression evaluates to <code>false</code>.
	 * @param structField
	 */
	public ExpressionDataFieldValidator(String organisationID, long validatorID, IDataFieldExpression<DataFieldType> expression,
                                       String message, ValidationResultType validationResultType, StructField<?> structField)
	{
		super(organisationID, validatorID, structField);
		this.expression = expression;
		this.validationResult = new I18nValidationResult(organisationID, IDGenerator.nextID(I18nValidationResult.class),
				validationResultType);
		this.validationResult.getI18nValidationResultMessage().setText(NLLocale.getDefault().getLanguage(), message);
	}
	
	@Override
	public ValidationResult validate(DataFieldType dataElement, StructFieldType structElement) {
		if (!expression.evaluate(new DataFieldEvaluationContext<DataFieldType>(dataElement, structElement)))
			return new ValidationResult(validationResult.getResultType(), validationResult.getI18nValidationResultMessage().getText());
		else
			return null;
	}
	
	public IExpression getExpression() {
		return expression;
	}
	
	public void setExpression(IExpression expression) {
		if (expression instanceof IDataFieldExpression<?>) {
			this.expression = (IDataFieldExpression<DataFieldType>) expression;
		}
		else {
			throw new IllegalArgumentException("Param expression must be of type IDataFieldExpression but is of type "+expression.getClass());
		}
	}
	
	public I18nValidationResult getValidationResult() {
		return validationResult;
	}
}
