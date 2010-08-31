/* *****************************************************************************
 * JFire - it's hot - Free ERP System - http://jfire.org                       *
 * Copyright (C) 2004-2005 NightLabs - http://NightLabs.org                    *
 *                                                                             *
 * This library is free software; you can redistribute it and/or               *
 * modify it under the terms of the GNU Lesser General Public                  *
 * License as published by the Free Software Foundation; either                *
 * version 2.1 of the License, or (at your option) any later version.          *
 *                                                                             *
 * This library is distributed in the hope that it will be useful,             *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of              *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU           *
 * Lesser General Public License for more details.                             *
 *                                                                             *
 * You should have received a copy of the GNU Lesser General Public            *
 * License along with this library; if not, write to the                       *
 *     Free Software Foundation, Inc.,                                         *
 *     51 Franklin St, Fifth Floor,                                            *
 *     Boston, MA  02110-1301  USA                                             *
 *                                                                             *
 * Or get it online :                                                          *
 *     http://opensource.org/licenses/lgpl-license.php                         *
 *                                                                             *
 *                                                                             *
 ******************************************************************************/

package org.nightlabs.jfire.person;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;

import org.apache.log4j.Logger;
import org.nightlabs.jfire.base.DuplicateKeyException;
import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.jfire.prop.DisplayNamePart;
import org.nightlabs.jfire.prop.IStruct;
import org.nightlabs.jfire.prop.Struct;
import org.nightlabs.jfire.prop.StructBlock;
import org.nightlabs.jfire.prop.StructField;
import org.nightlabs.jfire.prop.StructLocal;
import org.nightlabs.jfire.prop.exception.StructBlockNotFoundException;
import org.nightlabs.jfire.prop.exception.StructFieldNotFoundException;
import org.nightlabs.jfire.prop.id.StructBlockID;
import org.nightlabs.jfire.prop.id.StructFieldID;
import org.nightlabs.jfire.prop.structfield.DateStructField;
import org.nightlabs.jfire.prop.structfield.ImageStructField;
import org.nightlabs.jfire.prop.structfield.MultiSelectionStructField;
import org.nightlabs.jfire.prop.structfield.MultiSelectionStructFieldValue;
import org.nightlabs.jfire.prop.structfield.NumberStructField;
import org.nightlabs.jfire.prop.structfield.PhoneNumberStructField;
import org.nightlabs.jfire.prop.structfield.RegexStructField;
import org.nightlabs.jfire.prop.structfield.SelectionStructField;
import org.nightlabs.jfire.prop.structfield.StructFieldValue;
import org.nightlabs.jfire.prop.structfield.TextStructField;
import org.nightlabs.l10n.DateFormatter;
import org.nightlabs.util.CollectionUtil;

/**
 * PersonStruct knows/defines the common structure of {@link Person}s
 * within the JFire datastore.
 *
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 */
public class PersonStruct
{
	private static final Logger logger = Logger.getLogger(PersonStruct.class);

	@SuppressWarnings("cast") //$NON-NLS-1$
	public static IStruct getPersonStructLocal(PersistenceManager pm)
	{
		String devOrganisationID = Organisation.DEV_ORGANISATION_ID;
		Struct personStruct = null;
		StructLocal personStructLocal = null;
		try {
			personStruct = Struct.getStruct(devOrganisationID, Person.class, Person.STRUCT_SCOPE, pm);
			// ensure each time that the standard structure is maintained
//			PersonStruct.createStandardStructure(personStruct, pm);
		} catch (JDOObjectNotFoundException e) {
			// person struct not persisted yet.
			personStruct = new Struct(devOrganisationID, Person.class.getName(), Person.STRUCT_LOCAL_SCOPE);
			// standard structure is now checked every time and not only if not existing. Daniel 2010-08-30
			PersonStruct.createStandardStructure(personStruct, pm);
			try {
				PersonStructValidationInitialiser.initialiseStructureValidators(personStruct);
			} catch (Exception x) {
				// should never happen as the structure is just created before
				throw new RuntimeException(e);
			}
			personStruct.getName().setText(Locale.ENGLISH.getLanguage(), "Persons"); //$NON-NLS-1$
			personStruct.getName().setText(Locale.GERMAN.getLanguage(), "Personen"); //$NON-NLS-1$
			personStruct = pm.makePersistent(personStruct);

			// WORKAROUND: Workaround for JPOX error 'cannot delete/update child row', foreign key problem, maybe this is also wrong tagging problem
			try {
				personStruct.addDisplayNamePart(new DisplayNamePart(devOrganisationID, "company", personStruct.getStructField(PERSONALDATA_COMPANY), ": ")); //$NON-NLS-1$
				personStruct.addDisplayNamePart(new DisplayNamePart(devOrganisationID, "name", personStruct.getStructField(PERSONALDATA_NAME), ", ")); //$NON-NLS-1$
				personStruct.addDisplayNamePart(new DisplayNamePart(devOrganisationID, "firstName", personStruct.getStructField(PERSONALDATA_FIRSTNAME), "")); //$NON-NLS-1$
			} catch (Exception e1) {
				logger.error("Error creating PersonStruct DisplayNameParts: ", e); //$NON-NLS-1$
			}
		}

//		// ensure each time that the standard structure is maintained
//		PersonStruct.createStandardStructure(personStruct, pm);
		
		try {
			personStructLocal = StructLocal.getStructLocal(pm, devOrganisationID, Person.class.getName(), personStruct.getStructScope(), Person.STRUCT_LOCAL_SCOPE);
		} catch (JDOObjectNotFoundException e) {
			personStructLocal = new StructLocal(personStruct, Person.STRUCT_LOCAL_SCOPE);
			personStructLocal.getName().setText(Locale.ENGLISH.getLanguage(), "Default Person Structure"); //$NON-NLS-1$
			personStructLocal.getName().setText(Locale.GERMAN.getLanguage(), "Standardstruktur für Personen"); //$NON-NLS-1$
			personStructLocal = pm.makePersistent(personStructLocal);
		}

		return personStructLocal;
	}

//	public static void createStandardStructure(IStruct ps, PersistenceManager pm) {
//		try {
//			StructBlock psb = PropHelper.createStructBlock(ps, PERSONALDATA, "Personal Data", "Persönliche Daten"); //$NON-NLS-1$ //$NON-NLS-2$
//			psb.setUnique(true);
//			TextStructField companyField = PropHelper.createTextDataField(psb, PERSONALDATA_COMPANY, "Company", "Firma"); //$NON-NLS-1$ //$NON-NLS-2$
//			TextStructField nameField = PropHelper.createTextDataField(psb, PERSONALDATA_NAME, "Name", "Name"); //$NON-NLS-1$ //$NON-NLS-2$
//			TextStructField firstNameField = PropHelper.createTextDataField(psb, PERSONALDATA_FIRSTNAME, "First Name", "Vorname"); //$NON-NLS-1$ //$NON-NLS-2$
//
//			psb.addStructField(companyField);
//			psb.addStructField(nameField);
//			psb.addStructField(firstNameField);
//
//			// is now done be PersonStructValidationInitialiser
////			IExpression nameCondition = new OrCondition(
////					new AndCondition(
////							new TextDataFieldNotEmptyExpression(PERSONALDATA_FIRSTNAME),
////							new TextDataFieldNotEmptyExpression(PERSONALDATA_NAME)
////					),
////					new TextDataFieldNotEmptyExpression(PERSONALDATA_COMPANY)
////			);
////			psb.addDataBlockValidator(new ExpressionDataBlockValidator(nameCondition, Messages.getString("org.nightlabs.jfire.person.PersonStruct.NameValidationError"), ValidationResultType.ERROR)); //$NON-NLS-1$
//
//			SelectionStructField selField = new SelectionStructField(psb, PERSONALDATA_SALUTATION);
//			selField.getName().setText(Locale.ENGLISH.getLanguage(), "Salutation"); //$NON-NLS-1$
//			selField.getName().setText(Locale.GERMAN.getLanguage(), "Anrede"); //$NON-NLS-1$
//			selField.setAllowsEmptySelection(true); // If it's a company, we don't need and don't want a salutation! Marco.
//
//			// is now done be PersonStructValidationInitialiser
////			IExpression salutationNeededCondition =
////					new OrCondition(
////							new AndCondition(
////									new Negation(new TextDataFieldNotEmptyExpression(PERSONALDATA_FIRSTNAME)),
////									new Negation(new TextDataFieldNotEmptyExpression(PERSONALDATA_NAME))
////							),
////							new SelectionDataFieldAssignedExpression(PERSONALDATA_SALUTATION)
////					);
////			psb.addDataBlockValidator(new ExpressionDataBlockValidator(salutationNeededCondition, Messages.getString("org.nightlabs.jfire.person.PersonStruct.SalutationNeededValidationError"), ValidationResultType.ERROR)); //$NON-NLS-1$
////
////			IExpression salutationNotNeededCondition =
////					new Negation(
////							new AndCondition(
////								new SelectionDataFieldAssignedExpression(PERSONALDATA_SALUTATION),
////								new Negation(new TextDataFieldNotEmptyExpression(PERSONALDATA_FIRSTNAME)),
////								new Negation(new TextDataFieldNotEmptyExpression(PERSONALDATA_NAME))
////							)
////					);
////			psb.addDataBlockValidator(new ExpressionDataBlockValidator(salutationNotNeededCondition, Messages.getString("org.nightlabs.jfire.person.PersonStruct.SalutationNotNeededValidationError"), ValidationResultType.WARNING)); //$NON-NLS-1$
//
//			StructFieldValue sfv = selField.newStructFieldValue(PERSONALDATA_SALUTATION_MR);
//			sfv.getValueName().setText(Locale.ENGLISH.getLanguage(), "Mr."); //$NON-NLS-1$
//			sfv.getValueName().setText(Locale.GERMAN.getLanguage(), "Herr"); //$NON-NLS-1$
//			sfv = selField.newStructFieldValue(PERSONALDATA_SALUTATION_MRS);
//			sfv.getValueName().setText(Locale.ENGLISH.getLanguage(), "Ms."); //$NON-NLS-1$
//			sfv.getValueName().setText(Locale.GERMAN.getLanguage(), "Frau"); //$NON-NLS-1$
//			psb.addStructField(selField);
//
//			psb.addStructField(PropHelper.createTextDataField(psb,PERSONALDATA_TITLE, "Title", "Titel")); //$NON-NLS-1$ //$NON-NLS-2$
//
//			DateStructField dateStructField = PropHelper.createDateDataField(psb, PERSONALDATA_DATEOFBIRTH, "Date of Birth", "Geburtsdatum"); //$NON-NLS-1$ //$NON-NLS-2$
//			dateStructField.setDateTimeEditFlags(DateFormatter.FLAGS_DATE_SHORT);
//			psb.addStructField(dateStructField);
//
//			ImageStructField imageStructField = PropHelper.createImageDataField(psb, PERSONALDATA_PHOTO, "Photo", "Bild"); //$NON-NLS-1$ //$NON-NLS-2$
//			imageStructField.addImageFormat("gif"); //$NON-NLS-1$
//			imageStructField.addImageFormat("jpg"); //$NON-NLS-1$
//			imageStructField.addImageFormat("jpeg"); //$NON-NLS-1$
//			imageStructField.addImageFormat("png"); //$NON-NLS-1$
//			imageStructField.setMaxSizeKB(100);
//			psb.addStructField(imageStructField);
//			ps.addStructBlock(psb);
//
//
//			psb = PropHelper.createStructBlock(ps,POSTADDRESS, "Post address", "Anschrift"); //$NON-NLS-1$ //$NON-NLS-2$
//			psb.addStructField(PropHelper.createTextDataField(psb,POSTADDRESS_ADDRESS, "Address", "Adresse")); //$NON-NLS-1$ //$NON-NLS-2$
//			// There are countries which use alphanumeric postcodes (e.g. Canada) => we cannot use NumberStructField. Marco.
////			psb.addStructField(new NumberStructField(psb, POSTADDRESS_POSTCODE));
//			psb.addStructField(PropHelper.createTextDataField(psb,POSTADDRESS_POSTCODE, "Postcode", "Postleitzahl")); //$NON-NLS-1$ //$NON-NLS-2$
//			psb.addStructField(PropHelper.createTextDataField(psb,POSTADDRESS_CITY, "City", "Stadt")); //$NON-NLS-1$ //$NON-NLS-2$
//			psb.addStructField(PropHelper.createTextDataField(psb,POSTADDRESS_REGION, "Region", "Region")); //$NON-NLS-1$ //$NON-NLS-2$
//			psb.addStructField(PropHelper.createTextDataField(psb,POSTADDRESS_COUNTRY, "Country", "Land")); //$NON-NLS-1$ //$NON-NLS-2$
//			ps.addStructBlock(psb);
//
//
//			psb = PropHelper.createStructBlock(ps, PHONE, "Phone", "Telefon"); //$NON-NLS-1$ //$NON-NLS-2$
//			psb.addStructField(PropHelper.createPhoneNumberDataField(psb, PHONE_PRIMARY, "Phone", "Telefon")); //$NON-NLS-1$ //$NON-NLS-2$
//			psb.addStructField(PropHelper.createPhoneNumberDataField(psb, FAX, "Fax", "Fax")); //$NON-NLS-1$ //$NON-NLS-2$
////			psb.addStructField(new TextStructField(psb,PHONE_COUNTRYCODE
//			ps.addStructBlock(psb);
//
////			psb = new StructBlock(ps,FAX);
////			psb.addStructField(new TextStructField(psb,FAX_COUNTRYCODE));
////			psb.addStructField(new TextStructField(psb,FAX_AREACODE));
////			psb.addStructField(new TextStructField(psb,FAX_LOCALNUMBER));
//////			psb.addStructField(new NumberStructField(psb,FAX_COUNTRYCODE));
//////			psb.addStructField(new NumberStructField(psb,FAX_AREACODE));
//////			psb.addStructField(new NumberStructField(psb,FAX_LOCALNUMBER));
////			ps.addStructBlock(psb);
//
//
//			psb = PropHelper.createStructBlock(ps,INTERNET, "Internet", "Internet"); //$NON-NLS-1$ //$NON-NLS-2$
//			// psb.addStructField(new TextStructField(psb,INTERNET_EMAIL));
//			RegexStructField regexStructField = PropHelper.createRegexDataField(psb, INTERNET_EMAIL, "Email", "Email"); //$NON-NLS-1$ //$NON-NLS-2$
//			// old regex, changed by marc 2008-04-30. Source: http://www.regular-expressions.info/email.html
//			//regexStructField.setRegex("^([a-zA-Z0-9_\\-\\.])+@((([a-zA-Z0-9\\-])+\\.)+([a-zA-Z]){2,4})$");
//			
//			// TODO: Somehow this regular expression seems not to work properly. Tobias
//			regexStructField.setRegex("^[a-zA-Z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-zA-Z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-zA-Z0-9](?:[a-zA-Z0-9-]*[a-zA-Z0-9])?\\.)+[a-zA-Z0-9](?:[a-zA-Z0-9-]*[a-zA-Z0-9])?$"); //$NON-NLS-1$
//			psb.addStructField(regexStructField);
//			psb.addStructField(PropHelper.createTextDataField(psb,INTERNET_HOMEPAGE, "Homepage", "Homepage")); //$NON-NLS-1$ //$NON-NLS-2$
//			ps.addStructBlock(psb);
//
//
//			psb = PropHelper.createStructBlock(ps,BANKDATA, "Bankdata", "Bankdaten"); //$NON-NLS-1$ //$NON-NLS-2$
//			psb.addStructField(PropHelper.createTextDataField(psb,BANKDATA_ACCOUNTHOLDER, "Account Holder", "Kontoinhaber")); //$NON-NLS-1$ //$NON-NLS-2$
////			psb.addStructField(new TextStructField(psb,BANKDATA_ACCOUNTNUMBER));
////			psb.addStructField(PropHelper.createNumberField(psb,BANKDATA_ACCOUNTNUMBER, "Account Number", "Kontonummer")); //$NON-NLS-1$ //$NON-NLS-2$
//			psb.addStructField(PropHelper.createTextDataField(psb,BANKDATA_ACCOUNTNUMBER, "Account Number", "Kontonummer")); //$NON-NLS-1$ //$NON-NLS-2$
////			psb.addStructField(new TextStructField(psb,BANKDATA_BANKCODE));
//			psb.addStructField(PropHelper.createTextDataField(psb,BANKDATA_BANKCODE, "Bank Code", "Bankleitzahl")); //$NON-NLS-1$ //$NON-NLS-2$
//			psb.addStructField(PropHelper.createTextDataField(psb,BANKDATA_BANKNAME, "Bank Name", "Bank")); //$NON-NLS-1$ //$NON-NLS-2$
//			psb.addStructField(PropHelper.createTextDataField(psb,BANKDATA_BIC, "Bank Identifier Code BIC", "Internationale Bankleitzahl BIC")); //$NON-NLS-1$ //$NON-NLS-2$
//			psb.addStructField(PropHelper.createTextDataField(psb,BANKDATA_IBAN, "International Bank Account Number IBAN", "Internationale Kontonummer IBAN")); //$NON-NLS-1$ //$NON-NLS-2$
//			ps.addStructBlock(psb);
//
//
//			psb = PropHelper.createStructBlock(ps,GOVERNMENTALDATA, "Governmental Data", "Staatliche Daten"); //$NON-NLS-1$ //$NON-NLS-2$
//			psb.addStructField(PropHelper.createTextDataField(psb,GOVERNMENTALDATA_VATIN, "VAT Number", "USt-IdNr.")); //$NON-NLS-1$ //$NON-NLS-2$
//			psb.addStructField(PropHelper.createTextDataField(psb,GOVERNMENTALDATA_NATIONALTAXNUMBER, "National tax number", "Nationale Steuernummer")); //$NON-NLS-1$ //$NON-NLS-2$
//			psb.addStructField(PropHelper.createTextDataField(psb,GOVERNMENTALDATA_TRADEREGISTERNAME, "Trade register name", "Handelsregister-Name")); //$NON-NLS-1$ //$NON-NLS-2$
//			psb.addStructField(PropHelper.createTextDataField(psb,GOVERNMENTALDATA_TRADEREGISTERNUMBER, "Trade register number", "Handelsregister-Nummer")); //$NON-NLS-1$ //$NON-NLS-2$
//			ps.addStructBlock(psb);
//
//
//			psb = PropHelper.createStructBlock(ps,CREDITCARD, "Credit Card", "Kreditkarte"); //$NON-NLS-1$ //$NON-NLS-2$
//			psb.addStructField(PropHelper.createTextDataField(psb,CREDITCARD_CREDITCARDHOLDER, "Credit Card Holder", "Karteninhaber")); //$NON-NLS-1$ //$NON-NLS-2$
////			psb.addStructField(new TextStructField(psb,CREDITCARD_NUMBER));
////			psb.addStructField(new TextStructField(psb,CREDITCARD_EXPIRYYEAR));
////			psb.addStructField(new TextStructField(psb,CREDITCARD_EXPIRYMONTH));
//			psb.addStructField(PropHelper.createTextDataField(psb,CREDITCARD_NUMBER, "Credit Card Number", "Kreditkartennummer")); //$NON-NLS-1$ //$NON-NLS-2$
//			psb.addStructField(PropHelper.createNumberDataField(psb,CREDITCARD_EXPIRYYEAR, "Expiry Year", "Gültigkeit Jahr")); //$NON-NLS-1$ //$NON-NLS-2$
////			psb.addStructField(PropHelper.createNumberField(psb,CREDITCARD_EXPIRYMONTH, "Expiry Month", "Gültigkeit Monat")); //$NON-NLS-1$ //$NON-NLS-2$
//
//			selField = new SelectionStructField(psb, CREDITCARD_EXPIRYMONTH);
//			selField.getName().setText(Locale.ENGLISH.getLanguage(), "Expiry Month"); //$NON-NLS-1$
//			selField.getName().setText(Locale.GERMAN.getLanguage(), "Gültigkeit Monat"); //$NON-NLS-1$
//			selField.setAllowsEmptySelection(true);
//
//			sfv = selField.newStructFieldValue(CREDITCARD_EXPIRYMONTH_JANUARY);
//			sfv.getValueName().setText(Locale.ENGLISH.getLanguage(), "01 - January"); //$NON-NLS-1$
//			sfv.getValueName().setText(Locale.GERMAN.getLanguage(), "01 - Januar"); //$NON-NLS-1$
//
//			sfv = selField.newStructFieldValue(CREDITCARD_EXPIRYMONTH_FEBRUARY);
//			sfv.getValueName().setText(Locale.ENGLISH.getLanguage(), "02 - February"); //$NON-NLS-1$
//			sfv.getValueName().setText(Locale.GERMAN.getLanguage(), "02 - Februar"); //$NON-NLS-1$
//
//			sfv = selField.newStructFieldValue(CREDITCARD_EXPIRYMONTH_MARCH);
//			sfv.getValueName().setText(Locale.ENGLISH.getLanguage(), "03 - March"); //$NON-NLS-1$
//			sfv.getValueName().setText(Locale.GERMAN.getLanguage(), "03 - März"); //$NON-NLS-1$
//
//			sfv = selField.newStructFieldValue(CREDITCARD_EXPIRYMONTH_APRIL);
//			sfv.getValueName().setText(Locale.ENGLISH.getLanguage(), "04 - April"); //$NON-NLS-1$
////			sfv.getValueName().setText(Locale.GERMAN.getLanguage(), "04 - April"); //$NON-NLS-1$
//
//			sfv = selField.newStructFieldValue(CREDITCARD_EXPIRYMONTH_MAY);
//			sfv.getValueName().setText(Locale.ENGLISH.getLanguage(), "05 - May"); //$NON-NLS-1$
//			sfv.getValueName().setText(Locale.GERMAN.getLanguage(), "05 - Mai"); //$NON-NLS-1$
//
//			sfv = selField.newStructFieldValue(CREDITCARD_EXPIRYMONTH_JUNE);
//			sfv.getValueName().setText(Locale.ENGLISH.getLanguage(), "06 - June"); //$NON-NLS-1$
//			sfv.getValueName().setText(Locale.GERMAN.getLanguage(), "06 - Juni"); //$NON-NLS-1$
//
//			sfv = selField.newStructFieldValue(CREDITCARD_EXPIRYMONTH_JULY);
//			sfv.getValueName().setText(Locale.ENGLISH.getLanguage(), "07 - July"); //$NON-NLS-1$
//			sfv.getValueName().setText(Locale.GERMAN.getLanguage(), "07 - Juli"); //$NON-NLS-1$
//
//			sfv = selField.newStructFieldValue(CREDITCARD_EXPIRYMONTH_AUGUST);
//			sfv.getValueName().setText(Locale.ENGLISH.getLanguage(), "08 - August"); //$NON-NLS-1$
//			sfv.getValueName().setText(Locale.GERMAN.getLanguage(), "08 - August"); //$NON-NLS-1$
//
//			sfv = selField.newStructFieldValue(CREDITCARD_EXPIRYMONTH_SEPTEMBER);
//			sfv.getValueName().setText(Locale.ENGLISH.getLanguage(), "09 - September"); //$NON-NLS-1$
//			sfv.getValueName().setText(Locale.GERMAN.getLanguage(), "09 - September"); //$NON-NLS-1$
//
//			sfv = selField.newStructFieldValue(CREDITCARD_EXPIRYMONTH_OCTOBER);
//			sfv.getValueName().setText(Locale.ENGLISH.getLanguage(), "10 - October"); //$NON-NLS-1$
//			sfv.getValueName().setText(Locale.GERMAN.getLanguage(), "10 - Oktober"); //$NON-NLS-1$
//
//			sfv = selField.newStructFieldValue(CREDITCARD_EXPIRYMONTH_NOVEMBER);
//			sfv.getValueName().setText(Locale.ENGLISH.getLanguage(), "11 - November"); //$NON-NLS-1$
////			sfv.getValueName().setText(Locale.GERMAN.getLanguage(), "11 - November"); //$NON-NLS-1$
//
//			sfv = selField.newStructFieldValue(CREDITCARD_EXPIRYMONTH_DECEMBER);
//			sfv.getValueName().setText(Locale.ENGLISH.getLanguage(), "12 - December"); //$NON-NLS-1$
//			sfv.getValueName().setText(Locale.GERMAN.getLanguage(), "12 - Dezember"); //$NON-NLS-1$
//			psb.addStructField(selField);
//
//			ps.addStructBlock(psb);
//
//
//			psb = PropHelper.createStructBlock(ps,COMMENT, "Comment", "Kommentar"); //$NON-NLS-1$ //$NON-NLS-2$
//			TextStructField commentField = PropHelper.createTextDataField(psb,COMMENT_COMMENT, "Comment", "Kommentar");
//			commentField.setLineCount(10);
//			psb.addStructField(commentField); //$NON-NLS-1$ //$NON-NLS-2$
//			ps.addStructBlock(psb);
//
////			ps.addDisplayNamePart(new DisplayNamePart(companyField, ": "));
////			ps.addDisplayNamePart(new DisplayNamePart(nameField, ", "));
////			ps.addDisplayNamePart(new DisplayNamePart(firstNameField, ""));
//
//			// add standard validators for person structure
//			PersonStructValidationInitialiser.initialiseStructureValidators(ps);
//
//		} catch (Throwable t) {
//			throw new RuntimeException(t);
//		}
//	}

	public static void createStandardStructure(IStruct ps, PersistenceManager pm) {
		try {
			// *** Block Personal Data ***
			StructBlock psb = createStructBlock(ps, PERSONALDATA);
			psb.setUnique(true);

			createTextStructField(psb, PERSONALDATA_COMPANY);
			createTextStructField(psb, PERSONALDATA_NAME);
			createTextStructField(psb, PERSONALDATA_FIRSTNAME);
			SelectionStructField salutationField = createSelectionStructField(psb, PERSONALDATA_SALUTATION,
					PERSONALDATA_SALUTATION_MR, PERSONALDATA_SALUTATION_MRS);
			salutationField.setAllowsEmptySelection(true); // If it's a company, we don't need and don't want a salutation! Marco.
			createTextStructField(psb, PERSONALDATA_TITLE);
			DateStructField dateStructField = createDateStructField(psb, PERSONALDATA_DATEOFBIRTH);
			dateStructField.setDateTimeEditFlags(DateFormatter.FLAGS_DATE_SHORT);
			createImageStructField(psb, PERSONALDATA_PHOTO, 100, "gif", "jpg", "jpeg", "png");

			// *** Block Post address ***
			psb = createStructBlock(ps, POSTADDRESS);
			createTextStructField(psb, POSTADDRESS_ADDRESS);
			// There are countries which use alphanumeric postcodes (e.g. Canada) => we cannot use NumberStructField. Marco.
			createTextStructField(psb, POSTADDRESS_POSTCODE);
			createTextStructField(psb, POSTADDRESS_CITY);
			createTextStructField(psb, POSTADDRESS_REGION);
			createTextStructField(psb, POSTADDRESS_COUNTRY);

			// *** Block Phone ***
			psb = createStructBlock(ps, PHONE);
			createPhoneNumberStructField(psb, PHONE_PRIMARY);
			createPhoneNumberStructField(psb, FAX);

			// *** Block Internet ***
			psb = createStructBlock(ps, INTERNET);
			RegexStructField regexStructField = createRegexStructField(psb, INTERNET_EMAIL);
			// old regex, changed by marc 2008-04-30. Source: http://www.regular-expressions.info/email.html
			//regexStructField.setRegex("^([a-zA-Z0-9_\\-\\.])+@((([a-zA-Z0-9\\-])+\\.)+([a-zA-Z]){2,4})$");
			regexStructField.setRegex("^[a-zA-Z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-zA-Z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-zA-Z0-9](?:[a-zA-Z0-9-]*[a-zA-Z0-9])?\\.)+[a-zA-Z0-9](?:[a-zA-Z0-9-]*[a-zA-Z0-9])?$"); //$NON-NLS-1$
			createTextStructField(psb, INTERNET_HOMEPAGE);

			// *** Block Bankdata ***
			psb = createStructBlock(ps, BANKDATA);
			createTextStructField(psb, BANKDATA_ACCOUNTHOLDER);
			createTextStructField(psb, BANKDATA_ACCOUNTNUMBER);
			createTextStructField(psb, BANKDATA_BANKCODE);
			createTextStructField(psb, BANKDATA_BANKNAME);
			createTextStructField(psb, BANKDATA_BIC);
			createTextStructField(psb, BANKDATA_IBAN);

			// *** Block Governmental Data ***
			psb = createStructBlock(ps, GOVERNMENTALDATA);
			createTextStructField(psb, GOVERNMENTALDATA_VATIN);
			createTextStructField(psb, GOVERNMENTALDATA_NATIONALTAXNUMBER);
			createTextStructField(psb, GOVERNMENTALDATA_TRADEREGISTERNAME);
			createTextStructField(psb, GOVERNMENTALDATA_TRADEREGISTERNUMBER);

			// *** Block Credit Card ***
			psb = createStructBlock(ps, CREDITCARD);
			createTextStructField(psb, CREDITCARD_CREDITCARDHOLDER);
			createTextStructField(psb, CREDITCARD_NUMBER);
			createNumberStructField(psb, CREDITCARD_EXPIRYYEAR);
			SelectionStructField selField = createSelectionStructField(psb, CREDITCARD_EXPIRYMONTH,
					CREDITCARD_EXPIRYMONTH_JANUARY,
					CREDITCARD_EXPIRYMONTH_FEBRUARY,
					CREDITCARD_EXPIRYMONTH_MARCH,
					CREDITCARD_EXPIRYMONTH_APRIL,
					CREDITCARD_EXPIRYMONTH_MAY,
					CREDITCARD_EXPIRYMONTH_JUNE,
					CREDITCARD_EXPIRYMONTH_JULY,
					CREDITCARD_EXPIRYMONTH_AUGUST,
					CREDITCARD_EXPIRYMONTH_SEPTEMBER,
					CREDITCARD_EXPIRYMONTH_OCTOBER,
					CREDITCARD_EXPIRYMONTH_NOVEMBER,
					CREDITCARD_EXPIRYMONTH_DECEMBER
					);
			selField.setAllowsEmptySelection(true);

			// *** Block Comment ***
			psb = createStructBlock(ps, COMMENT);
			createTextStructField(psb, COMMENT_COMMENT, 10);

//			ps.addDisplayNamePart(new DisplayNamePart(companyField, ": "));
//			ps.addDisplayNamePart(new DisplayNamePart(nameField, ", "));
//			ps.addDisplayNamePart(new DisplayNamePart(firstNameField, ""));
			
			// add standard validators for person structure
			// validation is now only added once at the beginning
//			PersonStructValidationInitialiser.initialiseStructureValidators(ps);
		} catch (Throwable t) {
			throw new RuntimeException(t);
		}
	}
	
	// *************** STANDARD StructBlocks StructField IDs ***************************

	public static final String DEV_ORGANISATION_ID = Organisation.DEV_ORGANISATION_ID;

	public static final StructBlockID INTERNALBLOCK = StructBlockID.create(DEV_ORGANISATION_ID,"InternalBlock"); //$NON-NLS-1$
	public static final StructFieldID INTERNALBLOCK_DISPLAYNAME = StructFieldID.create(INTERNALBLOCK,"DisplayName"); //$NON-NLS-1$

	public static final StructBlockID PERSONALDATA = StructBlockID.create(DEV_ORGANISATION_ID,"PersonalData"); //$NON-NLS-1$
	public static final StructFieldID PERSONALDATA_COMPANY = StructFieldID.create(PERSONALDATA,"Company"); //$NON-NLS-1$
	public static final StructFieldID PERSONALDATA_NAME = StructFieldID.create(PERSONALDATA,"Name"); //$NON-NLS-1$
	public static final StructFieldID PERSONALDATA_FIRSTNAME = StructFieldID.create(PERSONALDATA,"FirstName"); //$NON-NLS-1$
	public static final StructFieldID PERSONALDATA_SALUTATION = StructFieldID.create(PERSONALDATA,"Salutation"); //$NON-NLS-1$
	public static final String PERSONALDATA_SALUTATION_MR = "Mister"; //$NON-NLS-1$
	public static final String PERSONALDATA_SALUTATION_MRS = "Mistress"; //$NON-NLS-1$
	public static final StructFieldID PERSONALDATA_TITLE = StructFieldID.create(PERSONALDATA,"Title"); //$NON-NLS-1$
	public static final StructFieldID PERSONALDATA_DATEOFBIRTH = StructFieldID.create(PERSONALDATA,"DateOfBirth"); //$NON-NLS-1$
	public static final StructFieldID PERSONALDATA_PHOTO = StructFieldID.create(PERSONALDATA, "Photo"); //$NON-NLS-1$

	public static final StructBlockID POSTADDRESS = StructBlockID.create(DEV_ORGANISATION_ID,"PostAddress"); //$NON-NLS-1$
	public static final StructFieldID POSTADDRESS_ADDRESS = StructFieldID.create(POSTADDRESS,"Address"); //$NON-NLS-1$
	public static final StructFieldID POSTADDRESS_POSTCODE = StructFieldID.create(POSTADDRESS,"PostCode"); //$NON-NLS-1$
	public static final StructFieldID POSTADDRESS_CITY = StructFieldID.create(POSTADDRESS,"City"); //$NON-NLS-1$
	public static final StructFieldID POSTADDRESS_REGION = StructFieldID.create(POSTADDRESS,"Region"); //$NON-NLS-1$
	public static final StructFieldID POSTADDRESS_COUNTRY = StructFieldID.create(POSTADDRESS,"Country"); //$NON-NLS-1$

	public static final StructBlockID INTERNET = StructBlockID.create(DEV_ORGANISATION_ID,"Internet"); //$NON-NLS-1$
	public static final StructFieldID INTERNET_EMAIL = StructFieldID.create(INTERNET,"EMail"); //$NON-NLS-1$
	public static final StructFieldID INTERNET_HOMEPAGE = StructFieldID.create(INTERNET,"Homepage"); //$NON-NLS-1$

	public static final StructBlockID PHONE = StructBlockID.create(DEV_ORGANISATION_ID,"Phone"); //$NON-NLS-1$
	public static final StructFieldID PHONE_PRIMARY = StructFieldID.create(PHONE,"PrimaryPhone"); //$NON-NLS-1$
	public static final StructFieldID FAX = StructFieldID.create(PHONE, "Fax"); //$NON-NLS-1$

//	public static final StructBlockID FAX = StructBlockID.create(DEV_ORGANISATION_ID,"Fax");
//	public static final StructFieldID FAX_COUNTRYCODE = StructFieldID.create(FAX,"CountryCode");
//	public static final StructFieldID FAX_AREACODE = StructFieldID.create(FAX,"AreaCode");
//	public static final StructFieldID FAX_LOCALNUMBER = StructFieldID.create(FAX,"LocalNumber");

	public static final StructBlockID BANKDATA = StructBlockID.create(DEV_ORGANISATION_ID,"BankData"); //$NON-NLS-1$
	public static final StructFieldID BANKDATA_ACCOUNTHOLDER = StructFieldID.create(BANKDATA,"AccountHolder"); //$NON-NLS-1$
	public static final StructFieldID BANKDATA_BANKCODE = StructFieldID.create(BANKDATA,"BankCode"); //$NON-NLS-1$
	public static final StructFieldID BANKDATA_BANKNAME = StructFieldID.create(BANKDATA,"BankName"); //$NON-NLS-1$
	public static final StructFieldID BANKDATA_ACCOUNTNUMBER = StructFieldID.create(BANKDATA,"AccountNumber"); //$NON-NLS-1$
	public static final StructFieldID BANKDATA_IBAN = StructFieldID.create(BANKDATA,"IBAN"); //$NON-NLS-1$
	public static final StructFieldID BANKDATA_BIC = StructFieldID.create(BANKDATA,"BIC"); //$NON-NLS-1$

	public static final StructBlockID CREDITCARD = StructBlockID.create(DEV_ORGANISATION_ID,"CreditCard"); //$NON-NLS-1$
	public static final StructFieldID CREDITCARD_CREDITCARDHOLDER = StructFieldID.create(CREDITCARD,"CreditCardHolder"); //$NON-NLS-1$
	public static final StructFieldID CREDITCARD_NUMBER = StructFieldID.create(CREDITCARD,"Number"); //$NON-NLS-1$
	public static final StructFieldID CREDITCARD_EXPIRYYEAR = StructFieldID.create(CREDITCARD,"ExpiryYear"); //$NON-NLS-1$
	public static final StructFieldID CREDITCARD_EXPIRYMONTH = StructFieldID.create(CREDITCARD,"ExpiryMonth"); //$NON-NLS-1$

	public static final String CREDITCARD_EXPIRYMONTH_JANUARY = "01";
	public static final String CREDITCARD_EXPIRYMONTH_FEBRUARY = "02";
	public static final String CREDITCARD_EXPIRYMONTH_MARCH = "03";
	public static final String CREDITCARD_EXPIRYMONTH_APRIL = "04";
	public static final String CREDITCARD_EXPIRYMONTH_MAY = "05";
	public static final String CREDITCARD_EXPIRYMONTH_JUNE = "06";
	public static final String CREDITCARD_EXPIRYMONTH_JULY = "07";
	public static final String CREDITCARD_EXPIRYMONTH_AUGUST = "08";
	public static final String CREDITCARD_EXPIRYMONTH_SEPTEMBER = "09";
	public static final String CREDITCARD_EXPIRYMONTH_OCTOBER = "10";
	public static final String CREDITCARD_EXPIRYMONTH_NOVEMBER = "11";
	public static final String CREDITCARD_EXPIRYMONTH_DECEMBER = "12";

	/**
	 * 0-based month array (January = index 0).
	 */
	public static final String[] CREDITCARD_EXPIRYMONTHS = {
		CREDITCARD_EXPIRYMONTH_JANUARY,
		CREDITCARD_EXPIRYMONTH_FEBRUARY,
		CREDITCARD_EXPIRYMONTH_MARCH,
		CREDITCARD_EXPIRYMONTH_APRIL,
		CREDITCARD_EXPIRYMONTH_MAY,
		CREDITCARD_EXPIRYMONTH_JUNE,
		CREDITCARD_EXPIRYMONTH_JULY,
		CREDITCARD_EXPIRYMONTH_AUGUST,
		CREDITCARD_EXPIRYMONTH_SEPTEMBER,
		CREDITCARD_EXPIRYMONTH_OCTOBER,
		CREDITCARD_EXPIRYMONTH_NOVEMBER,
		CREDITCARD_EXPIRYMONTH_DECEMBER,
	};

	public static final StructBlockID GOVERNMENTALDATA = StructBlockID.create(DEV_ORGANISATION_ID,"GovernmentalData"); //$NON-NLS-1$
	/**
	 * Constant for the "Value added tax identification number" (VATIN).
	 */
	public static final StructFieldID GOVERNMENTALDATA_VATIN = StructFieldID.create(GOVERNMENTALDATA,"VATIN"); //$NON-NLS-1$
	/**
	 * http://de.wikipedia.org/wiki/Steuernummer
	 */
	public static final StructFieldID GOVERNMENTALDATA_NATIONALTAXNUMBER = StructFieldID.create(GOVERNMENTALDATA,"NationalTaxNumber"); //$NON-NLS-1$

// I didn't add the "TIN" field, because we should use the GOVERNMENTALDATA_NATIONALTAXNUMBER field for the TIN,
// since it replaces the old German national tax number, anyway. And because this number is a special german case
// and other countries call their national tax numbers differently, it makes no sense to add this special German number, only
// because Germany has temporarily 2 of these numbers. IMHO one field for the national tax number (however it's called in the local country)
// should be sufficient. Marco.
//	/**
//	 * Constant for the "Tax Identification Number" (TIN).
//	 * http://de.wikipedia.org/wiki/Steuer-Identifikationsnummer
//	 */
//	public static final StructFieldID GOVERNMENTALDATA_TIN = StructFieldID.create(GOVERNMENTALDATA,"TIN"); //$NON-NLS-1$

	public static final StructFieldID GOVERNMENTALDATA_TRADEREGISTERNAME = StructFieldID.create(GOVERNMENTALDATA,"TradeRegisterName"); //$NON-NLS-1$
	public static final StructFieldID GOVERNMENTALDATA_TRADEREGISTERNUMBER = StructFieldID.create(GOVERNMENTALDATA,"TradeRegisterNumber"); //$NON-NLS-1$

	public static final StructBlockID COMMENT = StructBlockID.create(DEV_ORGANISATION_ID,"Comment"); //$NON-NLS-1$
	public static final StructFieldID COMMENT_COMMENT = StructFieldID.create(COMMENT,"Comment"); //$NON-NLS-1$

	private static void readNameFromProperties(StructField<?> structField)
	{
		structField.getName().readFromProperties(
				org.nightlabs.jfire.person.resource.Messages.BUNDLE_NAME,
				PersonStruct.class.getClassLoader(),
				StructField.class.getName() + '[' + structField.getPrimaryKey() + "].name"
		);
	}

	private static void readNameFromProperties(StructBlock structBlock)
	{
		structBlock.getName().readFromProperties(
				org.nightlabs.jfire.person.resource.Messages.BUNDLE_NAME,
				PersonStruct.class.getClassLoader(),
				StructBlock.class.getName() + '[' + structBlock.getPrimaryKey() + "].name"
		);
	}

	private static void readNameFromProperties(MultiSelectionStructFieldValue multiSelectionStructFieldValue)
	{
		String pk =
			multiSelectionStructFieldValue.getStructBlockOrganisationID()
			+ '/'
			+ multiSelectionStructFieldValue.getStructBlockID()
			+ '/'
			+ multiSelectionStructFieldValue.getStructFieldOrganisationID()
			+ '/'
			+ multiSelectionStructFieldValue.getStructFieldID()
			+ '/'
			+ multiSelectionStructFieldValue.getStructFieldValueID();

		multiSelectionStructFieldValue.getValueName().readFromProperties(
				org.nightlabs.jfire.person.resource.Messages.BUNDLE_NAME,
				PersonStruct.class.getClassLoader(),
				MultiSelectionStructFieldValue.class.getName() + '[' + pk + "].name"
		);
	}

	private static void readNameFromProperties(StructFieldValue structFieldValue)
	{
		String pk =
			structFieldValue.getStructBlockOrganisationID()
			+ '/'
			+ structFieldValue.getStructBlockID()
			+ '/'
			+ structFieldValue.getStructFieldOrganisationID()
			+ '/'
			+ structFieldValue.getStructFieldID()
			+ '/'
			+ structFieldValue.getStructFieldValueID();

		structFieldValue.getValueName().readFromProperties(
				org.nightlabs.jfire.person.resource.Messages.BUNDLE_NAME,
				PersonStruct.class.getClassLoader(),
				StructFieldValue.class.getName() + '[' + pk + "].name"
		);
	}

	private static TextStructField createTextStructField(StructBlock structBlock, StructFieldID structFieldID)
	{
		TextStructField field;
		try {
			field = (TextStructField) structBlock.getStructField(structFieldID);
		} catch (StructFieldNotFoundException x) {
			field = new TextStructField(structBlock, structFieldID);
			try {
				structBlock.addStructField(field);
			} catch (DuplicateKeyException e) { // Should never happen, because we check for the existence of the field, first.
				throw new RuntimeException(e);
			}
		}
		readNameFromProperties(field);
		return field;
	}

	private static TextStructField createTextStructField(StructBlock structBlock, StructFieldID structFieldID, int lineCount)
	{
		TextStructField field;
		try {
			field = (TextStructField) structBlock.getStructField(structFieldID);
		} catch (StructFieldNotFoundException x) {
			field = new TextStructField(structBlock, structFieldID);
			try {
				structBlock.addStructField(field);
			} catch (DuplicateKeyException e) { // Should never happen, because we check for the existence of the field, first.
				throw new RuntimeException(e);
			}
		}
		field.setLineCount(lineCount);
		readNameFromProperties(field);
		return field;
	}

	private static PhoneNumberStructField createPhoneNumberStructField(StructBlock structBlock, StructFieldID structFieldID)
	{
		PhoneNumberStructField field;
		try {
			field = (PhoneNumberStructField) structBlock.getStructField(structFieldID);
		} catch (StructFieldNotFoundException x) {
			field = new PhoneNumberStructField(structBlock, structFieldID);
			try {
				structBlock.addStructField(field);
			} catch (DuplicateKeyException e) { // Should never happen, because we check for the existence of the field, first.
				throw new RuntimeException(e);
			}
		}
		readNameFromProperties(field);
		return field;
	}

	private static SelectionStructField createSelectionStructField(
			StructBlock structBlock, StructFieldID structFieldID,
			String ... structFieldValueIDs
	)
	{
		SelectionStructField field;
		try {
			field = (SelectionStructField) structBlock.getStructField(structFieldID);
		} catch (StructFieldNotFoundException x) {
			field = new SelectionStructField(structBlock, structFieldID);
			try {
				structBlock.addStructField(field);
			} catch (DuplicateKeyException e) { // Should never happen, because we check for the existence of the field, first.
				throw new RuntimeException(e);
			}
		}
		readNameFromProperties(field);

		Map<String, StructFieldValue> structFieldValueID2value = new HashMap<String, StructFieldValue>();
		for (StructFieldValue value : field.getStructFieldValues())
			structFieldValueID2value.put(value.getStructFieldValueID(), value);

		for (String structFieldValueID : structFieldValueIDs) {
			StructFieldValue value = structFieldValueID2value.get(structFieldValueID);

			if (value == null)
				value = field.newStructFieldValue(structFieldValueID);

			readNameFromProperties(value);
		}

		return field;
	}


	private static MultiSelectionStructField createMultiSelectionStructField(
			StructBlock structBlock, StructFieldID structFieldID,
			String ... structFieldValueIDs
	)
	{
		MultiSelectionStructField field;
		try {
			field = (MultiSelectionStructField) structBlock.getStructField(structFieldID);
		} catch (StructFieldNotFoundException x) {
			field = new MultiSelectionStructField(structBlock, structFieldID);
			try {
				structBlock.addStructField(field);
			} catch (DuplicateKeyException e) { // Should never happen, because we check for the existence of the field, first.
				throw new RuntimeException(e);
			}
		}
		readNameFromProperties(field);


		Map<String, MultiSelectionStructFieldValue> structFieldValueID2value = new HashMap<String, MultiSelectionStructFieldValue>();
		for (MultiSelectionStructFieldValue value : field.getStructFieldValues())
			structFieldValueID2value.put(value.getStructFieldValueID(), value);

		for (String structFieldValueID : structFieldValueIDs) {
			MultiSelectionStructFieldValue value = structFieldValueID2value.get(structFieldValueID);

			if (value == null)
				value = field.newStructFieldValue(structFieldValueID);

			readNameFromProperties(value);
		}

		return field;
	}

	private static ImageStructField createImageStructField(
			StructBlock structBlock, StructFieldID structFieldID,
			int maxSizeKB, String ... imageFormats
	)
	{
		ImageStructField field;
		try {
			field = (ImageStructField) structBlock.getStructField(structFieldID);
		} catch (StructFieldNotFoundException x) {
			field = new ImageStructField(structBlock, structFieldID);
			try {
				structBlock.addStructField(field);
			} catch (DuplicateKeyException e) { // Should never happen, because we check for the existence of the field, first.
				throw new RuntimeException(e);
			}
		}

		if (field.getMaxSizeKB() != maxSizeKB)
			field.setMaxSizeKB(maxSizeKB);

		boolean imageFormatsDiffer = false;

		if (field.getImageFormats().size() != imageFormats.length)
			imageFormatsDiffer = true;
		else {
			Set<String> processed = new HashSet<String>();
			Set<String> imageFormatsSet = CollectionUtil.array2HashSet(imageFormats);
			for (String extension : field.getImageFormats()) {
				if (!processed.add(extension)) {
					imageFormatsDiffer = true;
					break;
				}

				if (!imageFormatsSet.contains(extension)) {
					imageFormatsDiffer = true;
					break;
				}
			}
		}

		if (imageFormatsDiffer) {
			field.clearImageFormats();

			for (String extension : imageFormats)
				field.addImageFormat(extension);
		}

		readNameFromProperties(field);
		return field;
	}

	private static DateStructField createDateStructField(StructBlock structBlock, StructFieldID structFieldID) {
		DateStructField field;
		try {
			field = (DateStructField) structBlock.getStructField(structFieldID);
		} catch (StructFieldNotFoundException x) {
			field = new DateStructField(structBlock, structFieldID);
			try {
				structBlock.addStructField(field);
			} catch (DuplicateKeyException e) { // Should never happen, because we check for the existence of the field, first.
				throw new RuntimeException(e);
			}
		}
		readNameFromProperties(field);
		return field;
	}

	private static StructBlock createStructBlock(IStruct struct, StructBlockID structBlockID) {
		StructBlock block;
		try {
			block = struct.getStructBlock(structBlockID);
		} catch (StructBlockNotFoundException x) {
			block = new StructBlock(struct, structBlockID);
			try {
				struct.addStructBlock(block);
			} catch (DuplicateKeyException e) {  // Should never happen, because we check for the existence of the field, first.
				throw new RuntimeException(e);
			}
		}
		readNameFromProperties(block);
		return block;
	}

	private static RegexStructField createRegexStructField(StructBlock structBlock, StructFieldID structFieldID) {
		RegexStructField field;
		try {
			field = (RegexStructField) structBlock.getStructField(structFieldID);
		} catch (StructFieldNotFoundException x) {
			field = new RegexStructField(structBlock, structFieldID);
			try {
				structBlock.addStructField(field);
			} catch (DuplicateKeyException e) { // Should never happen, because we check for the existence of the field, first.
				throw new RuntimeException(e);
			}
		}
		readNameFromProperties(field);
		return field;
	}

	private static NumberStructField createNumberStructField(StructBlock structBlock, StructFieldID structFieldID) {
		NumberStructField field;
		try {
			field = (NumberStructField) structBlock.getStructField(structFieldID);
		} catch (StructFieldNotFoundException x) {
			field = new NumberStructField(structBlock, structFieldID);
			try {
				structBlock.addStructField(field);
			} catch (DuplicateKeyException e) { // Should never happen, because we check for the existence of the field, first.
				throw new RuntimeException(e);
			}
		}
		readNameFromProperties(field);
		return field;
	}
}