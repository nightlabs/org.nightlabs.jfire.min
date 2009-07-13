package org.nightlabs.jfire.prop.validation;

import org.nightlabs.jfire.base.expression.IExpression;
import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.prop.DataBlock;
import org.nightlabs.jfire.prop.StructBlock;
import org.nightlabs.util.NLLocale;

import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.FetchGroups;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceModifier;

/**
 * Implementation of {@link IDataBlockValidator} that is able to validate a {@link DataBlock}
 * based on an {@link IExpression}.
 *
 * @jdo.persistence-capable
 * 		identity-type="application"
 * 		persistence-capable-superclass="org.nightlabs.jfire.prop.validation.DataBlockValidator"
 * 		table="JFireBase_Prop_ExpressionDataBlockValidator"
 *
 * @jdo.inheritance strategy="new-table"
 * 
 * @jdo.fetch-group name "IStruct.fullData" fetch-groups="default" fields="expression, validationResult"
 *
 * @author Tobias Langner <!-- tobias[dot]langner[at]nightlabs[dot]de -->
 * @author Daniel Mazurek
 */
@PersistenceCapable(
	identityType=IdentityType.APPLICATION,
	table="JFireBase_Prop_ExpressionDataBlockValidator")
@FetchGroups(
	@FetchGroup(
		fetchGroups={"default"},
		name="IStruct.fullData",
		members={@Persistent(name="expression"), @Persistent(name="validationResult")})
)
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class ExpressionDataBlockValidator
extends DataBlockValidator
implements IExpressionValidator
{
	private static final long serialVersionUID = 1L;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 * @jdo.field dependent="true"
	 */
	@Persistent(
		dependent="true",
		persistenceModifier=PersistenceModifier.PERSISTENT)
	private IExpression expression;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 * @jdo.field dependent="true"
	 */
	@Persistent(
		dependent="true",
		persistenceModifier=PersistenceModifier.PERSISTENT)
	private I18nValidationResult validationResult;
	
	/**
	 * @deprecated Only for JDO.
	 */
	@Deprecated
	protected ExpressionDataBlockValidator() {
	}

//	/**
//	 * Creates a new instance with the given expression, message and validation result type used when the expression evaluates
//	 * to <code>false</code>; the primary key is auto-generated using the {@link IDGenerator}.
//	 *
//	 * @param expression The expression to be used for the validation.
//	 * @param message The message that should be displayed to be used when expression evaluates to <code>false.
//	 * @param validationResultType The type of the validation result to be used when expression evaluates to <code>false</code>.
//	 */
//	public ExpressionDataBlockValidator(IExpression expression, String message, ValidationResultType validationResultType) {
//		this(IDGenerator.getOrganisationID(), IDGenerator.nextID(DataBlockValidator.class), expression, message, validationResultType);
//	}

	/**
	 * Creates a new instance with the given expression, message and validation result type used when the expression evaluates
	 * to <code>false</code>; the primary key is auto-generated using the {@link IDGenerator}.
	 *
	 * @param expression The expression to be used for the validation.
	 * @param message The message that should be displayed to be used when expression evaluates to <code>false.
	 * @param validationResultType The type of the validation result to be used when expression evaluates to <code>false</code>.
	 */
	public ExpressionDataBlockValidator(IExpression expression, String message, ValidationResultType validationResultType, StructBlock structBlock) {
		this(IDGenerator.getOrganisationID(), IDGenerator.nextID(DataBlockValidator.class), expression, message, validationResultType, structBlock);
	}

	/**
	 * Creates a new instance with the given expression, message and validation result type used when the expression evaluates
	 * to <code>false</code> and the given primary key.
	 *
	 * @param organisationID The organisation ID of the primary key.
	 * @param validatorID The validator ID of the primary key.
	 * @param expression The expression to be used for the validation.
	 * @param message The message that should be displayed to be used when expression evaluates to <code>false.
	 * @param validationResultType The type of the validation result to be used when expression evaluates to <code>false</code>.
	 */
	public ExpressionDataBlockValidator(String organisationID, long validatorID, IExpression expression, String message, 
			ValidationResultType validationResultType, StructBlock structBlock) 
	{
		super(organisationID, validatorID, structBlock);
		this.expression = expression;
		this.validationResult = new I18nValidationResult(organisationID, IDGenerator.nextID(I18nValidationResult.class), 
				validationResultType);
		this.validationResult.getI18nValidationResultMessage().setText(NLLocale.getDefault().getLanguage(), message);
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
//	public ExpressionDataBlockValidator(String organisationID, long validatorID, IExpression expression, String message, ValidationResultType validationResultType) {
//		super(organisationID, validatorID);
//		this.expression = expression;
//		this.validationResult = new I18nValidationResult(organisationID, IDGenerator.nextID(I18nValidationResult.class), 
//				validationResultType);
//		this.validationResult.getI18nValidationResultMessage().setText(NLLocale.getDefault().getLanguage(), message);
//	}

	@Override
	public ValidationResult validate(DataBlock dataBlock, StructBlock structBlock) {
		if (!expression.evaluate(new DataBlockEvaluationContext(dataBlock, structBlock)))
			return new ValidationResult(validationResult.getResultType(), validationResult.getI18nValidationResultMessage().getText());
		else
			return null;
	}
	
	public IExpression getExpression() {
		return expression;
	}
	
	public void setExpression(IExpression expression) {
		this.expression = expression;
	}

	public I18nValidationResult getValidationResult() {
		return validationResult;
	}
}
