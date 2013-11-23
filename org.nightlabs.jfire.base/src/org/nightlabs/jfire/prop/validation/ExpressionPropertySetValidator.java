package org.nightlabs.jfire.prop.validation;

import org.nightlabs.jfire.base.expression.IExpression;
import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.prop.IStruct;
import org.nightlabs.jfire.prop.PropertySet;
import org.nightlabs.util.NLLocale;

import org.nightlabs.jfire.prop.validation.id.ExpressionPropertySetValidatorID;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.FetchGroups;
import javax.jdo.annotations.PrimaryKey;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.Column;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceModifier;

/**
 * Implementation of {@link IPropertySetValidator} that is able to validate a {@link PropertySet}
 * based on a {@link IExpression}.
 *
 * @jdo.persistence-capable
 * 		identity-type="application"
 * 		objectid-class="org.nightlabs.jfire.prop.validation.id.ExpressionPropertySetValidatorID"
 *    detachable="true"
 *    table="JFireBase_Prop_ExpressionPropertySetValidator"
 *
 * @jdo.create-objectid-class
 *
 * @jdo.implements name="org.nightlabs.jfire.prop.validation.IPropertySetValidator"
 *
 * @jdo.fetch-group name "IStruct.fullData" fetch-groups="default" fields="expression, validationResult"
 * 
 * @author Tobias Langner <!-- tobias[dot]langner[at]nightlabs[dot]de -->
 */
@PersistenceCapable(
	objectIdClass=ExpressionPropertySetValidatorID.class,
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireBase_Prop_ExpressionPropertySetValidator")
@FetchGroups(
	@FetchGroup(
		fetchGroups={"default"},
		name="IStruct.fullData",
		members={@Persistent(name="expression"), @Persistent(name="validationResult")})
)
public class ExpressionPropertySetValidator implements IPropertySetValidator, IExpressionValidator
{
	private static final long serialVersionUID = 1L;

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	@PrimaryKey
	@Column(length=100)
	private String organisationID;

	/**
	 * @jdo.field primary-key="true"
	 */
	@PrimaryKey
	private long validatorID;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private IExpression expression;
	
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
	protected ExpressionPropertySetValidator() {
	}

	/**
	 * Creates a new instance with the given expression, message and validation result type used when the expression evaluates
	 * to <code>false</code>; the primary key is auto-generated using the {@link IDGenerator}.
	 *
	 * @param expression The expression to be used for the validation.
	 * @param message The message that should be displayed to be used when expression evaluates to <code>false.
	 * @param validationResultType The type of the validation result to be used when expression evaluates to <code>false</code>.
	 */
	public ExpressionPropertySetValidator(IExpression expression, String message, ValidationResultType validationResultType) {
		this(IDGenerator.getOrganisationID(), IDGenerator.nextID(ExpressionPropertySetValidator.class), expression, message, validationResultType);
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
	public ExpressionPropertySetValidator(String organisationID, long validatorID, IExpression condition, String message, ValidationResultType validationResultType) {
		this.organisationID = organisationID;
		this.validatorID = validatorID;
		this.expression = condition;
//		this.message = message;
//		this.resultType = validationResultType;
		this.validationResult = new I18nValidationResult(organisationID, IDGenerator.nextID(I18nValidationResult.class), 
				validationResultType);
		this.validationResult.getI18nValidationResultMessage().setText(NLLocale.getDefault().getLanguage(), message);		
	}

	@Override
	public ValidationResult validate(PropertySet propertySet, IStruct struct) {
		if (!expression.evaluate(new PropertySetEvaluationContext(propertySet, struct)))
//			return new ValidationResult(resultType, message);
			return new ValidationResult(validationResult.getResultType(), validationResult.getI18nValidationResultMessage().getText());
		else
			return null;
	}

	public String getOrganisationID() {
		return organisationID;
	}

	public long getValidatorID() {
		return validatorID;
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
