package org.nightlabs.jfire.testsuite.base.prop;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import javax.ejb.CreateException;
import javax.jdo.FetchPlan;
import javax.jdo.JDOHelper;
import javax.naming.NamingException;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.nightlabs.i18n.I18nText;
import org.nightlabs.inheritance.InheritanceManager;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jdo.timepattern.TimePatternSetJDOImpl;
import org.nightlabs.jfire.base.JFireEjb3Factory;
import org.nightlabs.jfire.base.jdo.cache.Cache;
import org.nightlabs.jfire.base.jdo.notification.JDOLifecycleManager;
import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.jfire.prop.DataField;
import org.nightlabs.jfire.prop.PropertyManagerRemote;
import org.nightlabs.jfire.prop.PropertySet;
import org.nightlabs.jfire.prop.Struct;
import org.nightlabs.jfire.prop.StructLocal;
import org.nightlabs.jfire.prop.dao.StructLocalDAO;
import org.nightlabs.jfire.prop.datafield.DateDataField;
import org.nightlabs.jfire.prop.datafield.I18nTextDataField;
import org.nightlabs.jfire.prop.datafield.ImageDataField;
import org.nightlabs.jfire.prop.datafield.ImageDataFieldContent;
import org.nightlabs.jfire.prop.datafield.MultiSelectionDataField;
import org.nightlabs.jfire.prop.datafield.NumberDataField;
import org.nightlabs.jfire.prop.datafield.PhoneNumber;
import org.nightlabs.jfire.prop.datafield.PhoneNumberDataField;
import org.nightlabs.jfire.prop.datafield.RegexDataField;
import org.nightlabs.jfire.prop.datafield.SelectionDataField;
import org.nightlabs.jfire.prop.datafield.TextDataField;
import org.nightlabs.jfire.prop.datafield.TimePatternSetDataField;
import org.nightlabs.jfire.prop.id.PropertySetID;
import org.nightlabs.jfire.prop.id.StructLocalID;
import org.nightlabs.jfire.prop.structfield.MultiSelectionStructFieldValue;
import org.nightlabs.jfire.prop.structfield.SelectionStructField;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.jfire.testsuite.JFireTestSuite;
import org.nightlabs.jfire.testsuite.TestCase;
import org.nightlabs.timepattern.TimePattern;
import org.nightlabs.timepattern.TimePatternSet;

/**
 * Extension of {@link TestCase} for testing proper persistence and inheritance of different {@link DataField} kinds.
 * The following types of {@link DataField}s are tested:
 * <li> {@link DateDataField}
 * <li> {@link I18nTextDataField}
 * <li> {@link ImageDataField}
 * <li> {@link MultiSelectionDataField}
 * <li> {@link NumberDataField}
 * <li> {@link PhoneNumberDataField}
 * <li> {@link RegexDataField}
 * <li> {@link SelectionDataField}
 * <li> {@link TextDataField}
 * <li> {@link TimePatternSetDataField}
 * @author Frederik Loeser <!-- frederik [AT] nightlabs [DOT] de -->
 * @author Fitas Amine - fitas [at] nightlabs [dot] de
 */

public class JFirePropertySetInheritanceTestCase extends TestCase {

	private static final Logger logger = Logger.getLogger(JFirePropertySetInheritanceTestCase.class);
	private static final String[] FETCH_GROUPS = new String[] {FetchPlan.DEFAULT, 
		PropertySet.FETCH_GROUP_FULL_DATA,
		MultiSelectionStructFieldValue.FETCH_GROUP_VALUE_NAME,
		PropertySet.FETCH_GROUP_DATA_FIELDS,
		TimePatternSetJDOImpl.FETCH_GROUP_TIME_PATTERNS};
	private static final int FETCH_DEPTH = NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT;
	
	private static final String PROPERTY_SET_ID_MOTHER = "propertySetIDMother";
	private static final String PROPERTY_SET_ID_CHILD = "propertySetIDChild";

	
	private PropertyManagerRemote getPropertyManager() throws RemoteException, CreateException, NamingException {
		return JFireEjb3Factory.getRemoteBean(PropertyManagerRemote.class, SecurityReflector.getInitialContextProperties());
	}

	@Override
	 protected void setUpBeforeClass() throws Exception{
		PropertySet propertySet_Mother = new PropertySet(IDGenerator.getOrganisationID(), IDGenerator.nextID(PropertySet.class), Organisation.DEV_ORGANISATION_ID,
			PropertySetInheritanceTestStruct.class.getName(), Struct.DEFAULT_SCOPE,	StructLocal.DEFAULT_SCOPE);
		propertySet_Mother = getPropertyManager().storePropertySet(propertySet_Mother, true, FETCH_GROUPS, FETCH_DEPTH);
		PropertySetID propertySetID_Mother = (PropertySetID) JDOHelper.getObjectId(propertySet_Mother);

		PropertySet propertySet_Child = new PropertySet(IDGenerator.getOrganisationID(), IDGenerator.nextID(PropertySet.class), Organisation.DEV_ORGANISATION_ID,
			PropertySetInheritanceTestStruct.class.getName(), Struct.DEFAULT_SCOPE, StructLocal.DEFAULT_SCOPE);
		propertySet_Child = getPropertyManager().storePropertySet(propertySet_Child, true, FETCH_GROUPS, FETCH_DEPTH);
		PropertySetID propertySetID_Child = (PropertySetID) JDOHelper.getObjectId(propertySet_Child);
		// set the context IDs
		setTestCaseContextObject(PROPERTY_SET_ID_MOTHER, propertySetID_Mother);
		setTestCaseContextObject(PROPERTY_SET_ID_CHILD, propertySetID_Child);
		// FIXME what is the intended behaviour here? Marc
		//Cache.setServerMode(true);
		String className = System.getProperty(JDOLifecycleManager.PROPERTY_KEY_JDO_LIFECYCLE_MANAGER);
		if (className == null) {
			className = JDOLifecycleManager.class.getName();
			System.setProperty(JDOLifecycleManager.PROPERTY_KEY_JDO_LIFECYCLE_MANAGER, className);
		}
	}


	/**
	 * Fetch and inflate a {@link PropertySet} used for this test.
	 * @return A propertySet used for this test.
	 * @throws NamingException 
	 * @throws CreateException 
	 * @throws RemoteException 
	 */
	private PropertySet fetchPropertySet(final PropertySetID propertySetID) throws RemoteException, CreateException, NamingException {
		PropertySet propertySet;
		assertThat("PropertySetID is Null",propertySetID, notNullValue());
		propertySet = getPropertyManager().getPropertySet(propertySetID, FETCH_GROUPS, FETCH_DEPTH);
		// Fetching the stuff directly from the beans in order to avoid using the Cache. Marco.
		final StructLocalID structLocalID = StructLocalID.create(
				SecurityReflector.getUserDescriptor().getOrganisationID(), propertySet.getStructLinkClass(), propertySet.getStructScope(), propertySet.getStructLocalScope()
		);
		final StructLocal structLocal = getPropertyManager().getFullStructLocal(
				structLocalID, StructLocalDAO.DEFAULT_FETCH_GROUPS, NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT
		);
		propertySet.inflate(structLocal);
		return propertySet;
	}

	
	private PropertySet storePropertySet(final PropertySet propertySet, final boolean get) {
		try {
			propertySet.deflate();
			return getPropertyManager().storePropertySet(propertySet, get, FETCH_GROUPS, FETCH_DEPTH);
		} catch (final Exception exception) {
			throw new RuntimeException(exception);
		}
	}

	private PropertySet inheritPropertySet(final PropertySet mother, final PropertySet child) {
		final InheritanceManager im = new InheritanceManager();
		im.inheritAllFields(mother, child);
		try {
			return storePropertySet(child, true);
		} catch (final Exception exception) {
			throw new RuntimeException(exception);
		}
	}

	private void loadImage(final ImageDataField dataField) {
		try {
			final URL url = getClass().getResource("JFire_Test.gif");
			if (url == null)
				return;
			final InputStream in = url.openStream();
			try {
				dataField.loadStream(in, "JFire_Test.gif", "image/gif");
			} finally {
				in.close();
			}
		} catch (final IOException exception) {
			throw new RuntimeException(exception);
		}
	}

	/**
	 * Test fetching and exploding of mother's and child's {@link PropertySet}s.
	 */
    @Test
	public void testFetchPropertySet() throws Exception {
    	PropertySetID propertySetID_Mother = (PropertySetID)getTestCaseContextObject(PROPERTY_SET_ID_MOTHER);
    	PropertySetID propertySetID_Child = (PropertySetID)getTestCaseContextObject(PROPERTY_SET_ID_CHILD);
		fetchPropertySet(propertySetID_Mother);
		fetchPropertySet(propertySetID_Child);
	}

	/**
	 * Test proper persistence and inheritance of {@link ImageDataField} contents.
	 */
    @Test
	public void testInheritImageDataField() throws Exception {

    	PropertySet propertySet_Mother = fetchPropertySet((PropertySetID)getTestCaseContextObject(PROPERTY_SET_ID_MOTHER));
		PropertySet propertySet_Child = fetchPropertySet((PropertySetID)getTestCaseContextObject(PROPERTY_SET_ID_CHILD));

		try {
			// Test whether data have been stored in a correct way considering this DataField.
			final ImageDataField dataField_Mother = propertySet_Mother.getDataField(PropertySetInheritanceTestStruct.STRUCTFIELD_ID_IMAGE_DATAFIELD, ImageDataField.class);
			loadImage(dataField_Mother);
			final ImageDataFieldContent imageContent_Mother = (ImageDataFieldContent) dataField_Mother.getData();
			{
				final PropertySet detPropertySet_Mother = storePropertySet(propertySet_Mother, true);
				final ImageDataField detDataField_Mother = detPropertySet_Mother.getDataField(PropertySetInheritanceTestStruct.STRUCTFIELD_ID_IMAGE_DATAFIELD, ImageDataField.class);
				final ImageDataFieldContent detImageContent_Mother = (ImageDataFieldContent) detDataField_Mother.getData();
				assertEquals("ImageDataField - content encodings differ after storing", imageContent_Mother.getContentEncoding(), detImageContent_Mother.getContentEncoding());
				assertEquals("ImageDataField - content types differ after storing", imageContent_Mother.getContentType(), detImageContent_Mother.getContentType());
				assertEquals("ImageDataField - descriptions differ after storing", imageContent_Mother.getDescription(), detImageContent_Mother.getDescription());
				assertEquals("ImageDataField - file names differ after storing", imageContent_Mother.getFileName(), detImageContent_Mother.getFileName());
				assertEquals("ImageDataField - contents differ after storing", new String(imageContent_Mother.getContent()), new String(detImageContent_Mother.getContent()));
				assertEquals("ImageDataField - file timestamps differ after storing", imageContent_Mother.getFileTimestamp(), detImageContent_Mother.getFileTimestamp());
				propertySet_Mother = detPropertySet_Mother;
			}

			// Test whether the content of the considered DataField is inherited in a correct way between mother and child.
			propertySet_Child = inheritPropertySet(propertySet_Mother, propertySet_Child);
			final ImageDataField dataField_Child = propertySet_Child.getDataField(PropertySetInheritanceTestStruct.STRUCTFIELD_ID_IMAGE_DATAFIELD, ImageDataField.class);
			final ImageDataFieldContent imageContent_Child = (ImageDataFieldContent) dataField_Child.getData();
			assertEquals("ImageDataField - content encodings of mother and child differ after applying inheritance", imageContent_Mother.getContentEncoding(), imageContent_Child.getContentEncoding());
			assertEquals("ImageDataField - content types of mother and child differ after applying inheritance", imageContent_Mother.getContentType(), imageContent_Child.getContentType());
			assertEquals("ImageDataField - descriptions of mother and child differ after applying inheritance", imageContent_Mother.getDescription(), imageContent_Child.getDescription());
			assertEquals("ImageDataField - file names of mother and child differ after applying inheritance", imageContent_Mother.getFileName(), imageContent_Child.getFileName());
			assertEquals("ImageDataField - contents of mother and child differ after applying inheritance", new String(imageContent_Mother.getContent()), new String(imageContent_Child.getContent()));
			assertEquals("ImageDataField - file timestamps of mother and child differ after applying inheritance", imageContent_Mother.getFileTimestamp(), imageContent_Child.getFileTimestamp());

		} catch (final Exception exception) {
			throw new RuntimeException(exception);
		}
	}

	/**
	 * Test proper persistence and inheritance of {@link I18nTextDataField} contents.
	 */
    @Test
	public void testInheritI18nTextDataField() throws Exception{
		    	
    	PropertySet propertySet_Mother = fetchPropertySet((PropertySetID)getTestCaseContextObject(PROPERTY_SET_ID_MOTHER));
		PropertySet propertySet_Child = fetchPropertySet((PropertySetID)getTestCaseContextObject(PROPERTY_SET_ID_CHILD));

		try {
			// Test whether data have been stored in a correct way considering this DataField.
			final I18nTextDataField dataField_Mother = propertySet_Mother.getDataField(PropertySetInheritanceTestStruct.STRUCTFIELD_ID_I18NTEXT_DATAFIELD, I18nTextDataField.class);
			dataField_Mother.setData("Test inheritance functionality of I18nTextDataField");
			{
				final PropertySet detPropertySet_Mother = storePropertySet(propertySet_Mother, true);
				final I18nTextDataField detDataField_Mother = detPropertySet_Mother.getDataField(PropertySetInheritanceTestStruct.STRUCTFIELD_ID_I18NTEXT_DATAFIELD, I18nTextDataField.class);
				assertEquals("I18nTextDataField - texts differ after storing", ((I18nText) dataField_Mother.getData()).getText(), ((I18nText) detDataField_Mother.getData()).getText());
				propertySet_Mother = detPropertySet_Mother;
			}
			// Test whether the content of the considered DataField is inherited in a correct way between mother and child.
			propertySet_Child = inheritPropertySet(propertySet_Mother, propertySet_Child);
			final I18nTextDataField dataField_Child = propertySet_Child.getDataField(PropertySetInheritanceTestStruct.STRUCTFIELD_ID_I18NTEXT_DATAFIELD, I18nTextDataField.class);
			assertEquals("I18nTextDataField - texts of mother and child differ after applying inheritance", ((I18nText) dataField_Mother.getData()).getText(), ((I18nText) dataField_Child.getData()).getText());

		} catch (final Exception exception) {
			throw new RuntimeException(exception);
		}
	}

	/**
	 * Test proper persistence and inheritance of {@link DateDataField} contents.
	 */
    @Test
	public void testInheritDateDataField() throws Exception {

    	PropertySet propertySet_Mother = fetchPropertySet((PropertySetID)getTestCaseContextObject(PROPERTY_SET_ID_MOTHER));
		PropertySet propertySet_Child = fetchPropertySet((PropertySetID)getTestCaseContextObject(PROPERTY_SET_ID_CHILD));

		try {
			// Test whether data have been stored in a correct way considering this DataField.
			final DateDataField dataField_Mother = propertySet_Mother.getDataField(PropertySetInheritanceTestStruct.STRUCTFIELD_ID_DATE_DATAFIELD, DateDataField.class);
			dataField_Mother.setData(new Date());
			{
				final PropertySet detPropertySet_Mother = storePropertySet(propertySet_Mother, true);
				final DateDataField detDataField_Mother = detPropertySet_Mother.getDataField(PropertySetInheritanceTestStruct.STRUCTFIELD_ID_DATE_DATAFIELD, DateDataField.class);
				assertEquals("DateDataField - time differs after storing", ((Date) dataField_Mother.getData()).getTime(), ((Date) detDataField_Mother.getData()).getTime());
				propertySet_Mother = detPropertySet_Mother;
			}
			// Test whether the content of the considered DataField is inherited in a correct way between mother and child.
			propertySet_Child = inheritPropertySet(propertySet_Mother, propertySet_Child);
			final DateDataField dataField_Child = propertySet_Child.getDataField(PropertySetInheritanceTestStruct.STRUCTFIELD_ID_DATE_DATAFIELD, DateDataField.class);
			assertEquals("DateDataField - times of mother and child differ after applying inheritance.", ((Date) dataField_Mother.getData()).getTime(), ((Date) dataField_Child.getData()).getTime());

		} catch (final Exception exception) {
			throw new RuntimeException(exception);
		}
	}
    
    /**
     * Test proper persistence and inheritance of {@link MultiSelectionDataField} contents.
     */
	@Test
    public void testInheritMultiSelectionDataField() throws Exception{
    	PropertySet propertySet_Mother = fetchPropertySet((PropertySetID)getTestCaseContextObject(PROPERTY_SET_ID_MOTHER));
    	PropertySet propertySet_Child = fetchPropertySet((PropertySetID)getTestCaseContextObject(PROPERTY_SET_ID_CHILD));
    	// Test whether data have been stored in a correct way considering this DataField.
    	final MultiSelectionDataField dataField_Mother = propertySet_Mother.getDataField(PropertySetInheritanceTestStruct.STRUCTFIELD_ID_MULTISELECTION_DATAFIELD, MultiSelectionDataField.class);
    	List<MultiSelectionStructFieldValue> predefinedValues = dataField_Mother.getStructField().getStructFieldValues();	
    	assertThat(predefinedValues.size(), equalTo(3));
    	// select three selections
    	HashSet<MultiSelectionStructFieldValue> structFieldValues = new HashSet<MultiSelectionStructFieldValue>();
    	for(MultiSelectionStructFieldValue value:predefinedValues)
    	{
    		String valueID = value.getStructFieldValueID();    		
    		if(valueID.equals(PropertySetInheritanceTestStruct.TESTBLOCK_MULTISELECTION_3) || valueID.equals(PropertySetInheritanceTestStruct.TESTBLOCK_MULTISELECTION_2))
    			structFieldValues.add(value);
    	}
    	dataField_Mother.setData(structFieldValues);
    	// store the PropertySet
    	final PropertySet detpropertySet_Mother = storePropertySet(propertySet_Mother, true);
    	final MultiSelectionDataField defdataField_Mother = detpropertySet_Mother.getDataField(PropertySetInheritanceTestStruct.STRUCTFIELD_ID_MULTISELECTION_DATAFIELD, MultiSelectionDataField.class);
    	Collection<MultiSelectionStructFieldValue> values = defdataField_Mother.getStructFieldValues();
    	assertThat("the selection size doesnt match our selection",values.size(), equalTo(2));
    	assertThat(values, equalTo(dataField_Mother.getStructFieldValues()));
    	propertySet_Mother = detpropertySet_Mother;
    	// Test whether the content of the considered DataField is inherited in a correct way between mother and child.
    	propertySet_Child = inheritPropertySet(propertySet_Mother, propertySet_Child);
    	final MultiSelectionDataField dataField_child = propertySet_Child.getDataField(PropertySetInheritanceTestStruct.STRUCTFIELD_ID_MULTISELECTION_DATAFIELD, MultiSelectionDataField.class);
    	assertThat("the selection size doesnt match our selection", dataField_child.getStructFieldValues().size(), equalTo(2));
    	assertThat(dataField_child.getStructFieldValues(), equalTo(dataField_Mother.getStructFieldValues()));
    }

	
	/**
	 * Test proper persistence and inheritance of {@link NumberDataField} contents.
	 */
    @Test
	public void testInheritNumberDataField() throws Exception{
    	PropertySet propertySet_Mother = fetchPropertySet((PropertySetID)getTestCaseContextObject(PROPERTY_SET_ID_MOTHER));
		PropertySet propertySet_Child = fetchPropertySet((PropertySetID)getTestCaseContextObject(PROPERTY_SET_ID_CHILD));

		try {
			// Test whether data have been stored in a correct way considering this DataField.
			final NumberDataField dataField_Mother = propertySet_Mother.getDataField(PropertySetInheritanceTestStruct.STRUCTFIELD_ID_NUMBER_DATAFIELD, NumberDataField.class);
			dataField_Mother.setData(new BigDecimal(500));
			{
				final PropertySet detPropertySet_Mother = storePropertySet(propertySet_Mother, true);
				final NumberDataField detDataField_Mother = detPropertySet_Mother.getDataField(PropertySetInheritanceTestStruct.STRUCTFIELD_ID_NUMBER_DATAFIELD, NumberDataField.class);
				assertEquals("NumberDataField - numbers differ after storing", (dataField_Mother.getData()).intValue(), (detDataField_Mother.getData()).intValue());
				propertySet_Mother = detPropertySet_Mother;
			}
			// Test whether the content of the considered DataField is inherited in a correct way between mother and child.
			propertySet_Child = inheritPropertySet(propertySet_Mother, propertySet_Child);
			final NumberDataField dataField_Child = propertySet_Child.getDataField(PropertySetInheritanceTestStruct.STRUCTFIELD_ID_NUMBER_DATAFIELD, NumberDataField.class);
			assertEquals("NumberDataField - numbers of mother and child differ after applying inheritance", (dataField_Mother.getData()).intValue(), (dataField_Child.getData()).intValue());

		} catch (final Exception exception) {
			throw new RuntimeException(exception);
		}
	}

	/**
	 * Test proper persistence and inheritance of {@link PhoneNumberDataField} contents.
	 */
    @Test
	public void testInheritPhoneNumberDataField() throws Exception{
    	PropertySet propertySet_Mother = fetchPropertySet((PropertySetID)getTestCaseContextObject(PROPERTY_SET_ID_MOTHER));
		PropertySet propertySet_Child = fetchPropertySet((PropertySetID)getTestCaseContextObject(PROPERTY_SET_ID_CHILD));

		try {
			// Test whether data have been stored in a correct way considering this DataField.
			final PhoneNumberDataField dataField_Mother = propertySet_Mother.getDataField(PropertySetInheritanceTestStruct.STRUCTFIELD_ID_PHONENUMBER_DATAFIELD, PhoneNumberDataField.class);
			dataField_Mother.setData(new PhoneNumber("0049", "761", "123456789"));
			{
				final PropertySet detPropertySet_Mother = storePropertySet(propertySet_Mother, true);
				final PhoneNumberDataField detDataField_Mother = detPropertySet_Mother.getDataField(PropertySetInheritanceTestStruct.STRUCTFIELD_ID_PHONENUMBER_DATAFIELD, PhoneNumberDataField.class);
				assertEquals("PhoneNumberDataField - country codes differ after storing", ((PhoneNumber) dataField_Mother.getData()).getCountryCode(), ((PhoneNumber) detDataField_Mother.getData()).getCountryCode());
				assertEquals("PhoneNumberDataField - area codes differ after storing", ((PhoneNumber) dataField_Mother.getData()).getAreaCode(), ((PhoneNumber) detDataField_Mother.getData()).getAreaCode());
				assertEquals("PhoneNumberDataField - local numbers differ after storing", ((PhoneNumber) dataField_Mother.getData()).getLocalNumber(), ((PhoneNumber) detDataField_Mother.getData()).getLocalNumber());
				propertySet_Mother = detPropertySet_Mother;
			}
			// Test whether the content of the considered DataField is inherited in a correct way between mother and child.
			propertySet_Child = inheritPropertySet(propertySet_Mother, propertySet_Child);
			final PhoneNumberDataField dataField_Child = propertySet_Child.getDataField(PropertySetInheritanceTestStruct.STRUCTFIELD_ID_PHONENUMBER_DATAFIELD, PhoneNumberDataField.class);
			assertEquals("PhoneNumberDataField - country codes of mother and child differ after applying inheritance", ((PhoneNumber) dataField_Mother.getData()).getCountryCode(), ((PhoneNumber) dataField_Child.getData()).getCountryCode());
			assertEquals("PhoneNumberDataField - area codes of mother and child differ after applying inheritance", ((PhoneNumber) dataField_Mother.getData()).getAreaCode(), ((PhoneNumber) dataField_Child.getData()).getAreaCode());
			assertEquals("PhoneNumberDataField - local numbers of mother and child differ after applying inheritance", ((PhoneNumber) dataField_Mother.getData()).getLocalNumber(), ((PhoneNumber) dataField_Child.getData()).getLocalNumber());

		} catch (final Exception exception) {
			throw new RuntimeException(exception);
		}
	}

	/**
	 * Test proper persistence and inheritance of {@link RegexDataField} contents.
	 */
    @Test
	public void testInheritRegexDataField() throws Exception{
    	PropertySet propertySet_Mother = fetchPropertySet((PropertySetID)getTestCaseContextObject(PROPERTY_SET_ID_MOTHER));
		PropertySet propertySet_Child = fetchPropertySet((PropertySetID)getTestCaseContextObject(PROPERTY_SET_ID_CHILD));

		try {
			// Test whether data have been stored in a correct way considering this DataField.
			final RegexDataField dataField_Mother = propertySet_Mother.getDataField(PropertySetInheritanceTestStruct.STRUCTFIELD_ID_REGEX_DATAFIELD, RegexDataField.class);
			dataField_Mother.setData("ab[c]+");
			{
				final PropertySet detPropertySet_Mother = storePropertySet(propertySet_Mother, true);
				final RegexDataField detDataField_Mother = detPropertySet_Mother.getDataField(PropertySetInheritanceTestStruct.STRUCTFIELD_ID_REGEX_DATAFIELD, RegexDataField.class);
				assertEquals("RegexDataField - regular expressions differ after storing", (String) dataField_Mother.getData(), (String) detDataField_Mother.getData());
				propertySet_Mother = detPropertySet_Mother;
			}
			// Test whether the content of the considered DataField is inherited in a correct way between mother and child.
			propertySet_Child = inheritPropertySet(propertySet_Mother, propertySet_Child);
			final RegexDataField dataField_Child = propertySet_Child.getDataField(PropertySetInheritanceTestStruct.STRUCTFIELD_ID_REGEX_DATAFIELD, RegexDataField.class);
			assertEquals("RegexDataField - regular expressions of mother and child differ after applying inheritance", (String) dataField_Mother.getData(), (String) dataField_Child.getData());

		} catch (final Exception exception) {
			throw new RuntimeException(exception);
		}
	}

	/**
	 * Test proper persistence and inheritance of {@link SelectionDataField} contents.
	 */
    @Test
	public void testInheritSelectionDataField() throws Exception{
    	PropertySet propertySet_Mother = fetchPropertySet((PropertySetID)getTestCaseContextObject(PROPERTY_SET_ID_MOTHER));
		PropertySet propertySet_Child = fetchPropertySet((PropertySetID)getTestCaseContextObject(PROPERTY_SET_ID_CHILD));

		try {
			// Test whether data have been stored in a correct way considering this DataField.
			final SelectionDataField dataField_Mother = propertySet_Mother.getDataField(PropertySetInheritanceTestStruct.STRUCTFIELD_ID_SELECTION_DATAFIELD, SelectionDataField.class);
			SelectionStructField structField = (SelectionStructField) propertySet_Mother.getStructure().getStructField(PropertySetInheritanceTestStruct.STRUCTFIELD_ID_SELECTION_DATAFIELD);
			dataField_Mother.setData(structField.getStructFieldValue(PropertySetInheritanceTestStruct.TESTBLOCK_SELECTION_1));
			{
				final PropertySet detPropertySet_Mother = storePropertySet(propertySet_Mother, true);
				final SelectionDataField detDataField_Mother = detPropertySet_Mother.getDataField(PropertySetInheritanceTestStruct.STRUCTFIELD_ID_SELECTION_DATAFIELD, SelectionDataField.class);
				assertEquals("SelectionDataField - selections differ after storing", dataField_Mother.getStructFieldValueID(), detDataField_Mother.getStructFieldValueID());
				propertySet_Mother = detPropertySet_Mother;
			}
			// Test whether the content of the considered DataField is inherited in a correct way between mother and child.
			propertySet_Child = inheritPropertySet(propertySet_Mother, propertySet_Child);
			final SelectionDataField dataField_Child = propertySet_Child.getDataField(PropertySetInheritanceTestStruct.STRUCTFIELD_ID_SELECTION_DATAFIELD, SelectionDataField.class);
			assertEquals("SelectionDataField - selections of mother and child differ after applying inheritance", dataField_Mother.getStructFieldValueID(), dataField_Child.getStructFieldValueID());

		} catch (final Exception exception) {
			throw new RuntimeException(exception);
		}
	}

	/**
	 * Test proper persistence and inheritance of {@link TextDataField} contents.
	 */
    @Test
	public void testInheritTextDataField() throws Exception {
    	PropertySet propertySet_Mother = fetchPropertySet((PropertySetID)getTestCaseContextObject(PROPERTY_SET_ID_MOTHER));
		PropertySet propertySet_Child = fetchPropertySet((PropertySetID)getTestCaseContextObject(PROPERTY_SET_ID_CHILD));

		try {
			// Test whether data have been stored in a correct way considering this DataField.
			final TextDataField dataField_Mother = propertySet_Mother.getDataField(PropertySetInheritanceTestStruct.STRUCTFIELD_ID_TEXT_DATAFIELD, TextDataField.class);
			dataField_Mother.setData("Test inheritance functionality of TextDataField");
			{
				final PropertySet detPropertySet_Mother = storePropertySet(propertySet_Mother, true);
				final TextDataField detDataField_Mother = detPropertySet_Mother.getDataField(PropertySetInheritanceTestStruct.STRUCTFIELD_ID_TEXT_DATAFIELD, TextDataField.class);
				assertEquals("TextDataField - texts differ after storing", (String) dataField_Mother.getData(), (String) detDataField_Mother.getData());
				propertySet_Mother = detPropertySet_Mother;
			}
			// Test whether the content of the considered DataField is inherited in a correct way between mother and child.
			propertySet_Child = inheritPropertySet(propertySet_Mother, propertySet_Child);
			final TextDataField dataField_Child = propertySet_Child.getDataField(PropertySetInheritanceTestStruct.STRUCTFIELD_ID_TEXT_DATAFIELD, TextDataField.class);
			assertEquals("TextDataField - texts of mother and child differ after applying inheritance", (String) dataField_Mother.getData(), (String) dataField_Child.getData());

		} catch (final Exception exception) {
			throw new RuntimeException(exception);
		}
	}

	/**
	 * Test proper persistence and inheritance of {@link TimePatternSetDataField} contents.
	 */
    @Test
    public void testInheritTimePatternSetDataField() throws Exception {
    	PropertySet propertySet_Mother = fetchPropertySet((PropertySetID)getTestCaseContextObject(PROPERTY_SET_ID_MOTHER));
    	PropertySet propertySet_Child = fetchPropertySet((PropertySetID)getTestCaseContextObject(PROPERTY_SET_ID_CHILD));
    	// Test whether data have been stored in a correct way considering this DataField.
    	final TimePatternSetDataField dataField_Mother = propertySet_Mother.getDataField(PropertySetInheritanceTestStruct.STRUCTFIELD_ID_TIMEPATTERNSET_DATAFIELD, TimePatternSetDataField.class);
    	TimePatternSetJDOImpl tps = new TimePatternSetJDOImpl(IDGenerator.nextIDString(TimePatternSetJDOImpl.class));
    	// pick up random TimePatternSet values
    	String year = String.valueOf((int)(2010 + Math.random() * 10));
    	String month = String.valueOf((int)(1 + Math.random() * 10));
    	String day = String.valueOf((int)(1 + Math.random() * 25));
    	String dayofweek = String.valueOf((int)(1 + Math.random() * 5));
    	String hour = String.valueOf((int)(1 + Math.random() * 22));
    	String minute = String.valueOf((int)(1 + Math.random() * 59));
    	tps.createTimePattern(
    			year, // year
    			month, // month
    			day, // day
    			dayofweek, // dayOfWeek
    			hour, //  hour
    			minute); // minute
    	dataField_Mother.setData(tps);
    	final TimePattern tp_Mother = ((TimePatternSet) dataField_Mother.getData()).getTimePatterns().iterator().next();
    	// check that the values has been set
    	assertEquals(tp_Mother.getDay(), day);
		assertEquals(tp_Mother.getDayOfWeek(), dayofweek);
		assertEquals(tp_Mother.getHour(), hour);
		assertEquals(tp_Mother.getMinute(), minute);
		assertEquals(tp_Mother.getMonth(), month);
		assertEquals(tp_Mother.getYear(), year);
    	{
    		final PropertySet detPropertySet_Mother = storePropertySet(propertySet_Mother, true);
    		final TimePatternSetDataField detDataField_Mother = detPropertySet_Mother.getDataField(PropertySetInheritanceTestStruct.STRUCTFIELD_ID_TIMEPATTERNSET_DATAFIELD, TimePatternSetDataField.class);
    		final TimePattern detTp_Mother = ((TimePatternSet) detDataField_Mother.getData()).getTimePatterns().iterator().next();
    		assertEquals("TimePatternSetDataField - days differ after storing", tp_Mother.getDay(), detTp_Mother.getDay());
    		assertEquals("TimePatternSetDataField - days of week differ after storing", tp_Mother.getDayOfWeek(), detTp_Mother.getDayOfWeek());
    		assertEquals("TimePatternSetDataField - hours differ after storing", tp_Mother.getHour(), detTp_Mother.getHour());
    		assertEquals("TimePatternSetDataField - minutes differ after storing", tp_Mother.getMinute(), detTp_Mother.getMinute());
    		assertEquals("TimePatternSetDataField - months differ after storing", tp_Mother.getMonth(), detTp_Mother.getMonth());
    		assertEquals("TimePatternSetDataField - years differ after storing", tp_Mother.getYear(), detTp_Mother.getYear());
    		propertySet_Mother = detPropertySet_Mother;
    	}
    	// Test whether the content of the considered DataField is inherited in a correct way between mother and child.
    	propertySet_Child = inheritPropertySet(propertySet_Mother, propertySet_Child);
    	final TimePatternSetDataField dataField_Child = propertySet_Child.getDataField(PropertySetInheritanceTestStruct.STRUCTFIELD_ID_TIMEPATTERNSET_DATAFIELD, TimePatternSetDataField.class);
    	final TimePattern tp_Child = ((TimePatternSet) dataField_Child.getData()).getTimePatterns().iterator().next();
    	assertEquals("TimePatternSetDataField - days of mother and child differ after applying inheritance", tp_Mother.getDay(), tp_Child.getDay());
    	assertEquals("TimePatternSetDataField - days of week of mother and child differ after applying inheritance", tp_Mother.getDayOfWeek(), tp_Child.getDayOfWeek());
    	assertEquals("TimePatternSetDataField - hours of mother and child differ after applying inheritance", tp_Mother.getHour(), tp_Child.getHour());
    	assertEquals("TimePatternSetDataField - minutes of mother and child differ after applying inheritance", tp_Mother.getMinute(), tp_Child.getMinute());
    	assertEquals("TimePatternSetDataField - months of mother and child differ after applying inheritance", tp_Mother.getMonth(), tp_Child.getMonth());
    	assertEquals("TimePatternSetDataField - years of mother and child differ after applying inheritance", tp_Mother.getYear(), tp_Child.getYear());
    }
}
