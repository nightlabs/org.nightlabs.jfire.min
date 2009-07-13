/**
 * 
 */
package org.nightlabs.jfire.person;

import org.nightlabs.jfire.base.expression.AndCondition;
import org.nightlabs.jfire.base.expression.IExpression;
import org.nightlabs.jfire.base.expression.Negation;
import org.nightlabs.jfire.base.expression.OrCondition;
import org.nightlabs.jfire.person.resource.Messages;
import org.nightlabs.jfire.prop.IStruct;
import org.nightlabs.jfire.prop.StructBlock;
import org.nightlabs.jfire.prop.exception.StructBlockNotFoundException;
import org.nightlabs.jfire.prop.validation.ExpressionDataBlockValidator;
import org.nightlabs.jfire.prop.validation.GenericDataFieldNotEmptyExpression;
import org.nightlabs.jfire.prop.validation.ValidationResultType;

/**
 * @author Daniel Mazurek - Daniel.Mazurek [dot] nightlabs [dot] de
 *
 */
public class PersonStructValidationInitialiser 
{
	public static void initialiseStructureValidators(IStruct struct)
	throws StructBlockNotFoundException
	{ 
		StructBlock personalDataStructBlock;
		personalDataStructBlock = struct.getStructBlock(PersonStruct.PERSONALDATA);
		String baseName = "org.nightlabs.jfire.person.resource.messages";
		ClassLoader loader = PersonStructValidationInitialiser.class.getClassLoader();
		
		// add name validator
		IExpression nameCondition = new OrCondition(
			new AndCondition(
				new GenericDataFieldNotEmptyExpression(PersonStruct.PERSONALDATA_FIRSTNAME),
				new GenericDataFieldNotEmptyExpression(PersonStruct.PERSONALDATA_NAME)
			),
			new GenericDataFieldNotEmptyExpression(PersonStruct.PERSONALDATA_COMPANY)
		);
		ExpressionDataBlockValidator nameValidator = new ExpressionDataBlockValidator(nameCondition, 
			Messages.getString("org.nightlabs.jfire.person.PersonStruct.NameValidationError"), ValidationResultType.ERROR, personalDataStructBlock); //$NON-NLS-1$
		nameValidator.getValidationResult().getI18nValidationResultMessage().readFromProperties(baseName, loader, 
			"org.nightlabs.jfire.person.PersonStruct.NameValidationError"); //$NON-NLS-1$
		personalDataStructBlock.addDataBlockValidator(nameValidator);

		// add salutation validator
		IExpression salutationNeededCondition =
			new OrCondition(
				new AndCondition(
					new Negation(new GenericDataFieldNotEmptyExpression(PersonStruct.PERSONALDATA_FIRSTNAME)),
					new Negation(new GenericDataFieldNotEmptyExpression(PersonStruct.PERSONALDATA_NAME))							
				),
				new GenericDataFieldNotEmptyExpression(PersonStruct.PERSONALDATA_SALUTATION)
			);
		ExpressionDataBlockValidator salutationValidator = new ExpressionDataBlockValidator(salutationNeededCondition, 
			Messages.getString("org.nightlabs.jfire.person.PersonStruct.SalutationNeededValidationError"), ValidationResultType.ERROR, personalDataStructBlock); //$NON-NLS-1$
		salutationValidator.getValidationResult().getI18nValidationResultMessage().readFromProperties(baseName, loader, 
			"org.nightlabs.jfire.person.PersonStruct.SalutationNeededValidationError"); //$NON-NLS-1$
		personalDataStructBlock.addDataBlockValidator(salutationValidator);

		// add salutation not needed validator
		IExpression salutationNotNeededCondition =
			new Negation(
					new AndCondition(
							new GenericDataFieldNotEmptyExpression(PersonStruct.PERSONALDATA_SALUTATION),
							new Negation(new GenericDataFieldNotEmptyExpression(PersonStruct.PERSONALDATA_FIRSTNAME)),
							new Negation(new GenericDataFieldNotEmptyExpression(PersonStruct.PERSONALDATA_NAME))							
					)
			);
		ExpressionDataBlockValidator salutationNotNeededValidator = new ExpressionDataBlockValidator(salutationNotNeededCondition, 
			Messages.getString("org.nightlabs.jfire.person.PersonStruct.SalutationNotNeededValidationError"), ValidationResultType.WARNING, personalDataStructBlock); //$NON-NLS-1$
		salutationNotNeededValidator.getValidationResult().getI18nValidationResultMessage().readFromProperties(baseName, loader, 
			"org.nightlabs.jfire.person.PersonStruct.SalutationNotNeededValidationError"); //$NON-NLS-1$
		personalDataStructBlock.addDataBlockValidator(salutationNotNeededValidator);
    }
		
//	public static void initialiseStructureValidators(IStruct struct)
//	throws StructBlockNotFoundException, StructFieldNotFoundException
//	{
//		// add name validator
//		ScriptDataBlockValidator nameValidator = new ScriptDataBlockValidator(
//				ScriptDataBlockValidator.SCRIPT_ENGINE_NAME, getNameScript());		
//		I18nValidationResult validationResult = new I18nValidationResult(IDGenerator.getOrganisationID(), 
//				IDGenerator.nextID(I18nValidationResult.class), ValidationResultType.ERROR);
//		validationResult.getI18nValidationResultMessage().setText(Locale.ENGLISH.getLanguage(), 
//				"Name and firstname or company may not be empty");
//		validationResult.getI18nValidationResultMessage().setText(Locale.GERMAN.getLanguage(), 
//				"Name und Vorname oder Firma darf nicht leer sein");		
//		nameValidator.addValidationResult(KEY_NAME_NOT_SET, validationResult);
//		StructBlock personalDataBlock = struct.getStructBlock(PersonStruct.PERSONALDATA);
//		personalDataBlock.addDataBlockValidator(nameValidator);
//		
//		// add salutation validator
//		ScriptDataBlockValidator salutationValidator = new ScriptDataBlockValidator(
//				ScriptDataBlockValidator.SCRIPT_ENGINE_NAME, getSalutationScript());		
//		validationResult = new I18nValidationResult(IDGenerator.getOrganisationID(), IDGenerator.nextID(I18nValidationResult.class),
//				ValidationResultType.WARNING);
//		validationResult.getI18nValidationResultMessage().setText(Locale.ENGLISH.getLanguage(), "Salutation should be set");
//		validationResult.getI18nValidationResultMessage().setText(Locale.GERMAN.getLanguage(), "Anrede sollte gesetzt sein");		
//		nameValidator.addValidationResult(KEY_SALUTATION_NOT_SET, validationResult);
//		personalDataBlock = struct.getStructBlock(PersonStruct.PERSONALDATA);
//		personalDataBlock.addDataBlockValidator(salutationValidator);
//    }	
//	
//	public static final String KEY_NAME_NOT_SET = "nameNotSet";
//	public static final String KEY_SALUTATION_NOT_SET = "salutationNotSet";
//	private static final String LINE_BREAK = "\n";
//	
//	private static String getNameScript() 
//	{
//		return
//			"importPackage(Packages.org.nightlabs.jfire.prop);" + LINE_BREAK +
//			"importPackage(Packages.org.nightlabs.jfire.person);" + LINE_BREAK +
//			"firstName = dataBlock.getDataField(PersonStruct.PERSONALDATA_FIRSTNAME);" + LINE_BREAK +
//			"lastName = dataBlock.getDataField(PersonStruct.PERSONALDATA_NAME);" + LINE_BREAK +
//			"company = dataBlock.getDataField(PersonStruct.PERSONALDATA_COMPANY);" + LINE_BREAK +
//			"if ((firstName.isEmpty() && lastName.isEmpty()) || company.isEmpty()) {" + LINE_BREAK +
//			"\""+ KEY_NAME_NOT_SET +"\";" + LINE_BREAK +
//			"}" + LINE_BREAK +
//			"else {" + LINE_BREAK +
//			"	undefined" + ";" + LINE_BREAK +
//			"}";
//	}
//	
//	private static String getSalutationScript() 
//	{
//		return
//			"importPackage(Packages.org.nightlabs.jfire.prop);" + LINE_BREAK +
//			"importPackage(Packages.org.nightlabs.jfire.person);" + LINE_BREAK +
//			"firstName = dataBlock.getDataField(PersonStruct.PERSONALDATA_FIRSTNAME);" + LINE_BREAK +
//			"lastName = dataBlock.getDataField(PersonStruct.PERSONALDATA_NAME);" + LINE_BREAK +
//			"salutation = dataBlock.getDataField(PersonStruct.PERSONALDATA_SALUTATION);" + LINE_BREAK +
//			"if ((!firstName.isEmpty() && !lastName.isEmpty()) && salutation.getStructFieldValueID() == null) {" + LINE_BREAK +
//			"\""+	KEY_SALUTATION_NOT_SET +"\";" + LINE_BREAK +
//			"}" + LINE_BREAK +
//			"else {" + LINE_BREAK +
//			"	undefined" + ";" + LINE_BREAK +
//			"}";
//	}
}
