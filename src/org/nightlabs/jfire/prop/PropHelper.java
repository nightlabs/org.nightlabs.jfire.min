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
import org.nightlabs.jfire.prop.structfield.MultiSelectionStructField;
import org.nightlabs.jfire.prop.structfield.NumberStructField;
import org.nightlabs.jfire.prop.structfield.PhoneNumberStructField;
import org.nightlabs.jfire.prop.structfield.RegexStructField;
import org.nightlabs.jfire.prop.structfield.SelectionStructField;
import org.nightlabs.jfire.prop.structfield.TextStructField;
import org.nightlabs.jfire.prop.structfield.TimePatternSetStructField;

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

	public static TextStructField createTextDataField(StructBlock psb, StructFieldID structFieldID, String englishName, String germanName) {
		TextStructField field = new TextStructField(psb,structFieldID);
		field.getName().setText(Locale.GERMAN.getLanguage(), germanName);
		field.getName().setText(Locale.ENGLISH.getLanguage(), englishName);
		return field;
	}

	public static DateStructField createDateDataField(StructBlock psb, StructFieldID structFieldID, String englishName, String germanName) {
		DateStructField field = new DateStructField(psb,structFieldID);
		field.getName().setText(Locale.GERMAN.getLanguage(), germanName);
		field.getName().setText(Locale.ENGLISH.getLanguage(), englishName);
		return field;
	}

	public static NumberStructField createNumberDataField(StructBlock psb, StructFieldID structFieldID, String englishName, String germanName) {
		NumberStructField field = new NumberStructField(psb,structFieldID);
		field.getName().setText(Locale.GERMAN.getLanguage(), germanName);
		field.getName().setText(Locale.ENGLISH.getLanguage(), englishName);
		return field;
	}

	public static RegexStructField createRegexDataField(StructBlock psb, StructFieldID structFieldID, String englishName, String germanName) {
		RegexStructField field = new RegexStructField(psb,structFieldID);
		field.getName().setText(Locale.GERMAN.getLanguage(), germanName);
		field.getName().setText(Locale.ENGLISH.getLanguage(), englishName);
		return field;
	}

	public static ImageStructField createImageDataField(StructBlock psb, StructFieldID structFieldID, String englishName, String germanName) {
		ImageStructField field = new ImageStructField(psb,structFieldID);
		field.getName().setText(Locale.GERMAN.getLanguage(), germanName);
		field.getName().setText(Locale.ENGLISH.getLanguage(), englishName);
		return field;
	}

	public static I18nTextStructField createI18nTextDataField(StructBlock psb, StructFieldID structFieldID, String englishName, String germanName) {
		I18nTextStructField field = new I18nTextStructField(psb,structFieldID);
		field.getName().setText(Locale.GERMAN.getLanguage(), germanName);
		field.getName().setText(Locale.ENGLISH.getLanguage(), englishName);
		return field;
	}

	public static PhoneNumberStructField createPhoneNumberDataField(StructBlock psb, StructFieldID structFieldID, String englishName, String germanName) {
		PhoneNumberStructField field = new PhoneNumberStructField(psb,structFieldID);
		field.getName().setText(Locale.GERMAN.getLanguage(), germanName);
		field.getName().setText(Locale.ENGLISH.getLanguage(), englishName);
		return field;
	}

//	public static HTMLStructField createHTMLField(StructBlock psb, StructFieldID structFieldID, String englishName, String germanName) {
//		HTMLStructField field = new HTMLStructField(psb, structFieldID);
//		field.getName().setText(Locale.GERMAN.getLanguage(), germanName);
//		field.getName().setText(Locale.ENGLISH.getLanguage(), englishName);
//		return field;
//	}

//	public static DepartmentStructField createDepartmentField(StructBlock psb, StructFieldID structFieldID, String englishName, String germanName) {
//		DepartmentStructField field = new DepartmentStructField(psb, structFieldID);
//		field.getName().setText(Locale.GERMAN.getLanguage(), germanName);
//		field.getName().setText(Locale.ENGLISH.getLanguage(), englishName);
//		return field;
//	}

//	public static FileStructField createFileField(StructBlock psb, StructFieldID structFieldID, String englishName, String germanName) {
//		FileStructField field = new FileStructField(psb, structFieldID);
//		field.getName().setText(Locale.GERMAN.getLanguage(), germanName);
//		field.getName().setText(Locale.ENGLISH.getLanguage(), englishName);
//		return field;
//	}

	public static MultiSelectionStructField createMultiSelectionDataField(StructBlock psb, StructFieldID structFieldID, String englishName, String germanName) {
		MultiSelectionStructField field = new MultiSelectionStructField(psb, structFieldID);
		field.getName().setText(Locale.GERMAN.getLanguage(), germanName);
		field.getName().setText(Locale.ENGLISH.getLanguage(), englishName);
		return field;
	}

	public static SelectionStructField createSelectionDataField(StructBlock psb, StructFieldID structFieldID, String englishName, String germanName) {
		SelectionStructField field = new SelectionStructField(psb, structFieldID);
		field.getName().setText(Locale.GERMAN.getLanguage(), germanName);
		field.getName().setText(Locale.ENGLISH.getLanguage(), englishName);
		return field;
	}

	public static TimePatternSetStructField createTimePatternSetDataField(StructBlock psb, StructFieldID structFieldID, String englishName, String germanName) {
		TimePatternSetStructField field = new TimePatternSetStructField(psb,structFieldID);
		field.getName().setText(Locale.GERMAN.getLanguage(), germanName);
		field.getName().setText(Locale.ENGLISH.getLanguage(), englishName);
		return field;
	}
}
