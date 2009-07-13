/**
 * 
 */
package org.nightlabs.jfire.prop;

import java.util.Locale;

import org.nightlabs.jfire.prop.id.StructBlockID;
import org.nightlabs.jfire.prop.id.StructFieldID;
import org.nightlabs.jfire.prop.structfield.DateStructField;
import org.nightlabs.jfire.prop.structfield.I18nTextStructField;
import org.nightlabs.jfire.prop.structfield.ImageStructField;
import org.nightlabs.jfire.prop.structfield.NumberStructField;
import org.nightlabs.jfire.prop.structfield.PhoneNumberStructField;
import org.nightlabs.jfire.prop.structfield.RegexStructField;
import org.nightlabs.jfire.prop.structfield.TextStructField;

/**
 * Helper class to create {@link StructBlock}s and special StructFields.
 * 
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 *
 */
public class PropHelper {

	public static StructBlock createStructBlock(IStruct struct, StructBlockID structBlockID, String englishName, String germanName) {
		StructBlock block = new StructBlock(struct, structBlockID);
		block.getName().setText(Locale.GERMAN.getLanguage(), germanName);
		block.getName().setText(Locale.ENGLISH.getLanguage(), englishName);
		return block;
	}
	
	public static TextStructField createTextField(StructBlock psb, StructFieldID structFieldID, String englishName, String germanName) {
		TextStructField field = new TextStructField(psb,structFieldID);
		field.getName().setText(Locale.GERMAN.getLanguage(), germanName);
		field.getName().setText(Locale.ENGLISH.getLanguage(), englishName);
		return field;
	}
	
	public static DateStructField createDateField(StructBlock psb, StructFieldID structFieldID, String englishName, String germanName) {
		DateStructField field = new DateStructField(psb,structFieldID);
		field.getName().setText(Locale.GERMAN.getLanguage(), germanName);
		field.getName().setText(Locale.ENGLISH.getLanguage(), englishName);
		return field;
	}
	
	public static NumberStructField createNumberField(StructBlock psb, StructFieldID structFieldID, String englishName, String germanName) {
		NumberStructField field = new NumberStructField(psb,structFieldID);
		field.getName().setText(Locale.GERMAN.getLanguage(), germanName);
		field.getName().setText(Locale.ENGLISH.getLanguage(), englishName);
		return field;
	}
	
	public static RegexStructField createRegexField(StructBlock psb, StructFieldID structFieldID, String englishName, String germanName) {
		RegexStructField field = new RegexStructField(psb,structFieldID);
		field.getName().setText(Locale.GERMAN.getLanguage(), germanName);
		field.getName().setText(Locale.ENGLISH.getLanguage(), englishName);
		return field;
	}
	
	public static ImageStructField createImageField(StructBlock psb, StructFieldID structFieldID, String englishName, String germanName) {
		ImageStructField field = new ImageStructField(psb,structFieldID);
		field.getName().setText(Locale.GERMAN.getLanguage(), germanName);
		field.getName().setText(Locale.ENGLISH.getLanguage(), englishName);
		return field;
	}
	
	public static I18nTextStructField createI18nTextField(StructBlock psb, StructFieldID structFieldID, String englishName, String germanName) {
		I18nTextStructField field = new I18nTextStructField(psb,structFieldID);
		field.getName().setText(Locale.GERMAN.getLanguage(), germanName);
		field.getName().setText(Locale.ENGLISH.getLanguage(), englishName);
		return field;
	}
	
	public static PhoneNumberStructField createPhoneNumberField(StructBlock psb, StructFieldID structFieldID, String englishName, String germanName) {
		PhoneNumberStructField field = new PhoneNumberStructField(psb,structFieldID);
		field.getName().setText(Locale.GERMAN.getLanguage(), germanName);
		field.getName().setText(Locale.ENGLISH.getLanguage(), englishName);
		return field;
	}
}
