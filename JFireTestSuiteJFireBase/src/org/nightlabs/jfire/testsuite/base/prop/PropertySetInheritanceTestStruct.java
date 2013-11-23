package org.nightlabs.jfire.testsuite.base.prop;

import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;

import org.apache.log4j.Logger;
import org.nightlabs.jdo.ObjectIDUtil;
import org.nightlabs.jfire.base.DuplicateKeyException;
import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.jfire.prop.AbstractStruct;
import org.nightlabs.jfire.prop.DisplayNamePart;
import org.nightlabs.jfire.prop.IStruct;
import org.nightlabs.jfire.prop.PropHelper;
import org.nightlabs.jfire.prop.Struct;
import org.nightlabs.jfire.prop.StructBlock;
import org.nightlabs.jfire.prop.StructLocal;
import org.nightlabs.jfire.prop.id.StructBlockID;
import org.nightlabs.jfire.prop.id.StructFieldID;
import org.nightlabs.jfire.prop.structfield.MultiSelectionStructField;
import org.nightlabs.jfire.prop.structfield.MultiSelectionStructFieldValue;
import org.nightlabs.jfire.prop.structfield.SelectionStructField;
import org.nightlabs.jfire.prop.structfield.StructFieldValue;

/**
 *
 * @author Frederik Loeser <!-- frederik [AT] nightlabs [DOT] de -->
 */
public class PropertySetInheritanceTestStruct {

	/** Logger used in this class. */
	private static final Logger LOGGER = Logger.getLogger(PropertySetInheritanceTestStruct.class);

	static final String DEV_ORGANISATION_ID = Organisation.DEV_ORGANISATION_ID;
	static final StructBlockID STRUCTBLOCK_ID = StructBlockID.create(DEV_ORGANISATION_ID, "StructBlockID");
	static final StructFieldID STRUCTFIELD_ID_DATE_DATAFIELD = StructFieldID.create(STRUCTBLOCK_ID, "DateDataField");
	static final StructFieldID STRUCTFIELD_ID_I18NTEXT_DATAFIELD = StructFieldID.create(STRUCTBLOCK_ID, "I18nTextDataField");
	static final StructFieldID STRUCTFIELD_ID_IMAGE_DATAFIELD = StructFieldID.create(STRUCTBLOCK_ID, "ImageDataField");
	static final StructFieldID STRUCTFIELD_ID_MULTISELECTION_DATAFIELD = StructFieldID.create(STRUCTBLOCK_ID, "MultiSelectionDataField");
	static final StructFieldID STRUCTFIELD_ID_NUMBER_DATAFIELD = StructFieldID.create(STRUCTBLOCK_ID, "NumberDataField");
	static final StructFieldID STRUCTFIELD_ID_PHONENUMBER_DATAFIELD = StructFieldID.create(STRUCTBLOCK_ID, "PhoneNumberDataField");
	static final StructFieldID STRUCTFIELD_ID_REGEX_DATAFIELD = StructFieldID.create(STRUCTBLOCK_ID, "RegexDataField");
	static final StructFieldID STRUCTFIELD_ID_SELECTION_DATAFIELD = StructFieldID.create(STRUCTBLOCK_ID, "SelectionDataField");
	static final StructFieldID STRUCTFIELD_ID_TEXT_DATAFIELD = StructFieldID.create(STRUCTBLOCK_ID, "TextDataField");
	static final StructFieldID STRUCTFIELD_ID_TIMEPATTERNSET_DATAFIELD = StructFieldID.create(STRUCTBLOCK_ID, "TimePatternSetDataField");
	static final String TESTBLOCK_SELECTION_1 = "Selection1";
	static final String TESTBLOCK_SELECTION_2 = "Selection2";
	static final String TESTBLOCK_MULTISELECTION_1 = "MultiSelection1";
	static final String TESTBLOCK_MULTISELECTION_2 = "MultiSelection2";
	static final String TESTBLOCK_MULTISELECTION_3 = "MultiSelection3";

	
	public static IStruct getInheritanceTestStructure(final String organisationID, final PersistenceManager pm) {
		Struct struct = null;
		StructLocal structLocal = null;
		try {
			struct = Struct.getStruct(organisationID, PropertySetInheritanceTestStruct.class, Struct.DEFAULT_SCOPE, pm);
		} catch (final JDOObjectNotFoundException e) {
			struct = new Struct(organisationID, PropertySetInheritanceTestStruct.class.getName(), Struct.DEFAULT_SCOPE);
			PropertySetInheritanceTestStruct.createInheritanceTestStructure(struct);
			struct = pm.makePersistent(struct);
			// Workaround for JPOX error 'cannot delete/update child row', foreign key problem, maybe this is also a wrong tagging problem.
			if (struct instanceof AbstractStruct) {
				try {
					struct.addDisplayNamePart(
						new DisplayNamePart(
							organisationID, ObjectIDUtil.longObjectIDFieldToString(IDGenerator.nextID(DisplayNamePart.class)),
							struct.getStructField(STRUCTFIELD_ID_TEXT_DATAFIELD), ": "));
				} catch (final Exception exception) {
					LOGGER.error("Error creating PropertySetInheritanceTestStruct DisplayNameParts: ", exception);
				}
			}
			structLocal = new StructLocal(struct, StructLocal.DEFAULT_SCOPE);
			pm.makePersistent(structLocal);
		}

		return struct;
	}

	private static void createInheritanceTestStructure(final IStruct struct) {
		final StructBlock sb = PropHelper.createStructBlock(struct, STRUCTBLOCK_ID, "TestBlock", "TestBlock");

		try {
			sb.addStructField(PropHelper.createDateDataField(sb, STRUCTFIELD_ID_DATE_DATAFIELD, "Date", "Datum"));
			sb.addStructField(PropHelper.createI18nTextDataField(sb, STRUCTFIELD_ID_I18NTEXT_DATAFIELD, "I18n text", "I18n-Text"));
			sb.addStructField(PropHelper.createImageDataField(sb, STRUCTFIELD_ID_IMAGE_DATAFIELD, "Image", "Bild"));			
			addMultiSelectionStructField(sb);
			sb.addStructField(PropHelper.createNumberDataField(sb, STRUCTFIELD_ID_NUMBER_DATAFIELD, "Number", "Nummer"));
			sb.addStructField(PropHelper.createPhoneNumberDataField(sb, STRUCTFIELD_ID_PHONENUMBER_DATAFIELD, "Phone number", "Telefonnummer"));
			sb.addStructField(PropHelper.createRegexDataField(sb, STRUCTFIELD_ID_REGEX_DATAFIELD, "Regular expression", "Regulärer Ausdruck"));
			addSelectionStructField(sb);
			sb.addStructField(PropHelper.createTextDataField(sb, STRUCTFIELD_ID_TEXT_DATAFIELD, "Text", "Text"));
			sb.addStructField(PropHelper.createTimePatternSetDataField(sb, STRUCTFIELD_ID_TIMEPATTERNSET_DATAFIELD, "", ""));	// java.lang.IllegalArgumentException: org.nightlabs.jfire.prop.datafield.TimePatternSetDataField does not support input data of type class org.nightlabs.jfire.prop.structfield.StructFieldValue => FIXED
			struct.addStructBlock(sb);
		} catch (final DuplicateKeyException e) {
			throw new RuntimeException(e);
		}
	}
	
	private static void addMultiSelectionStructField(final StructBlock sb) {
		MultiSelectionStructField structField = PropHelper.createMultiSelectionDataField(sb, STRUCTFIELD_ID_MULTISELECTION_DATAFIELD, "Multi selection", "Mehrfachauswahl");
		// add the multi selection values
		MultiSelectionStructFieldValue fieldNameValue1 = structField.newStructFieldValue(TESTBLOCK_MULTISELECTION_1);
    	fieldNameValue1.getValueName().setText(Locale.ENGLISH.getLanguage(), "Multi Selection 1"); 
    	MultiSelectionStructFieldValue fieldNameValue2 = structField.newStructFieldValue(TESTBLOCK_MULTISELECTION_2);
    	fieldNameValue2.getValueName().setText(Locale.ENGLISH.getLanguage(),  "Multi Selection 2"); 
    	MultiSelectionStructFieldValue fieldNameValue3 = structField.newStructFieldValue(TESTBLOCK_MULTISELECTION_3);
    	fieldNameValue3.getValueName().setText(Locale.ENGLISH.getLanguage(),  "Multi Selection 3"); 
    	structField.setDefaultValues( new HashSet<MultiSelectionStructFieldValue>(Collections.singleton(fieldNameValue1)));
      	structField.setMinimumSelectionCount(1);		
		try {
			sb.addStructField(structField);
		} catch (final DuplicateKeyException e) {
			throw new RuntimeException(e);
		}
	}
	
	private static void addSelectionStructField(final StructBlock sb) {
		final SelectionStructField selField = new SelectionStructField(sb, STRUCTFIELD_ID_SELECTION_DATAFIELD);
		selField.getName().setText(Locale.ENGLISH.getLanguage(), "Selection Test");

		StructFieldValue sfv = selField.newStructFieldValue(TESTBLOCK_SELECTION_1);
		sfv.getValueName().setText(Locale.ENGLISH.getLanguage(), "Selection 1");
		sfv = selField.newStructFieldValue(TESTBLOCK_SELECTION_2);
		sfv.getValueName().setText(Locale.ENGLISH.getLanguage(), "Selection 2");
		try {
			sb.addStructField(selField);
		} catch (final DuplicateKeyException e) {
			throw new RuntimeException(e);
		}
	}
}
