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

package org.nightlabs.jfire.testsuite.prop;

import java.util.Locale;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;

import org.apache.log4j.Logger;
import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.jfire.prop.AbstractStruct;
import org.nightlabs.jfire.prop.DisplayNamePart;
import org.nightlabs.jfire.prop.IStruct;
import org.nightlabs.jfire.prop.PropHelper;
import org.nightlabs.jfire.prop.PropertySet;
import org.nightlabs.jfire.prop.Struct;
import org.nightlabs.jfire.prop.StructBlock;
import org.nightlabs.jfire.prop.StructLocal;
import org.nightlabs.jfire.prop.id.StructBlockID;
import org.nightlabs.jfire.prop.id.StructFieldID;
import org.nightlabs.jfire.prop.id.StructLocalID;
import org.nightlabs.jfire.prop.structfield.ImageStructField;
import org.nightlabs.jfire.prop.structfield.SelectionStructField;
import org.nightlabs.jfire.prop.structfield.StructFieldValue;

/**
 * PropertySetTestStruct is a test {@link PropertySet} structure
 * 
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 */
public class PropertySetTestStruct
{

	private static final Logger logger = Logger.getLogger(PropertySetTestStruct.class);
	
	public static IStruct getTestStruct(String organisationID, PersistenceManager pm) {
		Struct struct = null;
		StructLocal structLocal = null;
		try {
			struct = Struct.getStruct(organisationID, PropertySetTestStruct.class, pm);
		} catch (JDOObjectNotFoundException e) {
			// person struct not persisted yet.
			struct = new Struct(organisationID, PropertySetTestStruct.class.getName());
			PropertySetTestStruct.createStandardStructure(struct);
			struct = pm.makePersistent(struct);
			
			// WORKAROUND: Workaround for JPOX error 'cannot delete/update child row', foreign key problem, maybe this is also wron tagging problem
			if (struct instanceof AbstractStruct) {
				try {
					struct.addDisplayNamePart(new DisplayNamePart(organisationID, IDGenerator.nextID(DisplayNamePart.class), struct, struct.getStructField(TESTBLOCK_TEXT), ": "));
				} catch (Exception e1) {
					logger.error("Error createing PropertySetTestStruct DisplayNameParts: ", e);
				}
			}
			structLocal = new StructLocal(struct, StructLocal.DEFAULT_SCOPE);
			pm.makePersistent(structLocal);
		}
		return struct;
	}
	

	public static void createStandardStructure(IStruct struct) {
		try {

			StructBlock structBlock = PropHelper.createStructBlock(struct, TESTBLOCK, "TestBlock", "TestBlock");
			structBlock.addStructField(PropHelper.createTextField(structBlock,TESTBLOCK_TEXT, "Text", "Text"));
			structBlock.addStructField(PropHelper.createRegexField(structBlock,TESTBLOCK_REGEX, "Regex", "Regex"));
			structBlock.addStructField(PropHelper.createNumberField(structBlock,TESTBLOCK_NUMBER, "Number", "Nummer"));
			structBlock.addStructField(PropHelper.createPhoneNumberField(structBlock,TESTBLOCK_PHONENUMBER, "PhoneNumber", "Telefonnummer"));
			structBlock.addStructField(PropHelper.createDateField(structBlock,TESTBLOCK_DATE, "Date", "Datum"));

			SelectionStructField selField = new SelectionStructField(structBlock, TESTBLOCK_SELECTION);
			selField.getName().setText(Locale.ENGLISH.getLanguage(), "Selection Test");

			StructFieldValue sfv = selField.newStructFieldValue(TESTBLOCK_SELECTION_1);
			sfv.getValueName().setText(Locale.ENGLISH.getLanguage(), "Selection 1");
			sfv = selField.newStructFieldValue(TESTBLOCK_SELECTION_2);
			sfv.getValueName().setText(Locale.ENGLISH.getLanguage(), "Selection 2");
			structBlock.addStructField(selField);
			
			ImageStructField imageStructField = PropHelper.createImageField(structBlock, TESTBLOCK_IMAGE, "Iamge", "Bild");
			imageStructField.addImageFormat("gif");
			imageStructField.addImageFormat("jpg");
			imageStructField.addImageFormat("png");
			imageStructField.setMaxSizeKB(100);
			structBlock.addStructField(imageStructField);
			struct.addStructBlock(structBlock);
		} catch (Throwable t) {
			throw new RuntimeException(t);
		}
	}


	public static final String DEV_ORGANISATION_ID = Organisation.DEV_ORGANISATION_ID;
	
	public static final StructBlockID INTERNALBLOCK = StructBlockID.create(DEV_ORGANISATION_ID,"InternalBlock"); 
	public static final StructFieldID INTERNALBLOCK_DISPLAYNAME = StructFieldID.create(INTERNALBLOCK,"DisplayName");

	public static final StructBlockID TESTBLOCK = StructBlockID.create(DEV_ORGANISATION_ID, "TestBlock");
	public static final StructFieldID TESTBLOCK_TEXT = StructFieldID.create(TESTBLOCK, "TestTextField");
	public static final StructFieldID TESTBLOCK_REGEX = StructFieldID.create(TESTBLOCK, "TestRegexField");
	public static final StructFieldID TESTBLOCK_NUMBER = StructFieldID.create(TESTBLOCK, "TestNumberField");
	public static final StructFieldID TESTBLOCK_PHONENUMBER = StructFieldID.create(TESTBLOCK, "TestPhoneNumberField");
	public static final StructFieldID TESTBLOCK_DATE = StructFieldID.create(TESTBLOCK, "TestDateField");
	public static final StructFieldID TESTBLOCK_SELECTION = StructFieldID.create(TESTBLOCK, "TestSelectionField");
	public static final StructFieldID TESTBLOCK_IMAGE = StructFieldID.create(TESTBLOCK, "TestImageField");
	
	public static final String TESTBLOCK_SELECTION_1 = "Selection1";
	public static final String TESTBLOCK_SELECTION_2 = "Selection2";

	public static StructLocalID getStructLocalID(String organisationID) {
		return StructLocalID.create(organisationID, PropertySetTestStruct.class.getName(), StructLocal.DEFAULT_SCOPE);		
	}
}