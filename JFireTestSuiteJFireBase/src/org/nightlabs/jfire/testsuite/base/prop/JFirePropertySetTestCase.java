/**
 *
 */
package org.nightlabs.jfire.testsuite.base.prop;

import java.io.InputStream;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.Date;

import javax.ejb.CreateException;
import javax.jdo.FetchPlan;
import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.naming.NamingException;

import org.junit.Test;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.base.JFireEjb3Factory;
import org.nightlabs.jfire.base.jdo.notification.JDOLifecycleManager;
import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.jfire.prop.PropertyManagerRemote;
import org.nightlabs.jfire.prop.PropertySet;
import org.nightlabs.jfire.prop.Struct;
import org.nightlabs.jfire.prop.StructLocal;
import org.nightlabs.jfire.prop.dao.StructLocalDAO;
import org.nightlabs.jfire.prop.datafield.DateDataField;
import org.nightlabs.jfire.prop.datafield.ImageDataField;
import org.nightlabs.jfire.prop.datafield.NumberDataField;
import org.nightlabs.jfire.prop.datafield.PhoneNumberDataField;
import org.nightlabs.jfire.prop.datafield.RegexDataField;
import org.nightlabs.jfire.prop.datafield.SelectionDataField;
import org.nightlabs.jfire.prop.datafield.TextDataField;
import org.nightlabs.jfire.prop.id.PropertySetID;
import org.nightlabs.jfire.prop.id.StructLocalID;
import org.nightlabs.jfire.prop.structfield.SelectionStructField;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.jfire.testsuite.PropertySetTestStruct;
import org.nightlabs.jfire.testsuite.TestCase;


/**
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 * @author Fitas Amine - fitas [at] nightlabs [dot] de
 */

public class JFirePropertySetTestCase extends TestCase {

	private static final String[] FETCH_GROUPS = new String[] {FetchPlan.DEFAULT, PropertySet.FETCH_GROUP_FULL_DATA};
	private static final int FETCH_DEPTH = NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT;

	private static String NEW_PROPERTY_ID = "NEWPROPERTY";
	
//	private JFireLogin login;


	protected PropertyManagerRemote getPropertyManager() throws RemoteException, CreateException, NamingException {
//		return JFireEjb3Factory.getBean(PropertyManager.class, login.getInitialContextProperties());
//		return PropertyManagerUtil.getHome().create();
		return JFireEjb3Factory.getRemoteBean(PropertyManagerRemote.class, null);
	}

	@Override
	public String canRunTest(PersistenceManager pm) throws Exception {
		PropertySetTestStruct.getTestStruct(SecurityReflector.getUserDescriptor().getOrganisationID(), pm);
		PropertySetInheritanceTestStruct.getInheritanceTestStructure(SecurityReflector.getUserDescriptor().getOrganisationID(), pm);	
		pm.flush();
		return null;
	}
	
	@Override
	protected void setUpBeforeClass() throws Exception {		
//		login = JFireTestLogin.getUserLogin(JFireTestLogin.USER_QUALIFIER_SERVER_ADMIN);
//		login.login(); // TO DO shouldn't we logout??!!! Marco. I think we don't need to do anything! And we better should not!

		PropertySet propertySet = new PropertySet(
				IDGenerator.getOrganisationID(), IDGenerator.nextID(PropertySet.class),
				Organisation.DEV_ORGANISATION_ID,
				PropertySetTestStruct.class.getName(), Struct.DEFAULT_SCOPE, StructLocal.DEFAULT_SCOPE);
		propertySet = getPropertyManager().storePropertySet(propertySet, true, FETCH_GROUPS, FETCH_DEPTH);		
		setTestCaseContextObject(NEW_PROPERTY_ID, (PropertySetID) JDOHelper.getObjectId(propertySet));
		// FIXME what is the intended behaviour here? Marc
		//Cache.setServerMode(true);
		String className = System.getProperty(JDOLifecycleManager.PROPERTY_KEY_JDO_LIFECYCLE_MANAGER);
		if (className == null) {
			className = JDOLifecycleManager.class.getName();
			System.setProperty(JDOLifecycleManager.PROPERTY_KEY_JDO_LIFECYCLE_MANAGER, className);
		}
//		Cache.sharedInstance().open(SecurityReflector.getUserDescriptor().getSessionID());
//		Cache.sharedInstance().open(
//				this.getClass().getName()
//				+ '-' +
//				Long.toString(System.currentTimeMillis(), 36)
//				+ '-' +
//				Integer.toString((int) (Math.random() * Integer.parseInt("zzzz", 36)), 36)
//		);
	}

	@Override
	protected void tearDown() throws Exception {
//		Cache.sharedInstance().close();
		super.tearDown();
	}

	/**
	 * Fetch and inflate the {@link PropertySet} used for this test.
	 * @return The propertySet used for this test.
	 */
	private PropertySet fetchPropertySet() {
		PropertySet propertySet;
		PropertySetID propertySetID = (PropertySetID)getTestCaseContextObject(NEW_PROPERTY_ID);
		try {
			propertySet = getPropertyManager().getPropertySet(propertySetID, FETCH_GROUPS, FETCH_DEPTH);
		} catch (Exception e) {
			throw new RuntimeException("Fetching propertySet failed.", e);
		}
		try {
			// Fetching the stuff directly from the beans in order to avoid using the Cache. Marco.
			StructLocalID structLocalID = StructLocalID.create(
					SecurityReflector.getUserDescriptor().getOrganisationID(),
					propertySet.getStructLinkClass(),
					propertySet.getStructScope(),
					propertySet.getStructLocalScope()
			);

			StructLocal structLocal = getPropertyManager().getFullStructLocal(
					structLocalID,
					StructLocalDAO.DEFAULT_FETCH_GROUPS,
					NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT
			);

			propertySet.inflate(structLocal);
			
			// REV Alex: Here it should be checked, if the structures are equal after inflation
			
		} catch (Exception e) {
			throw new RuntimeException("Exploding propertySet failed", e);
		}
		return propertySet;
	}

	/**
	 * Test the fetching and exploding of a {@link PropertySet}
	 */
	@Test
	public void testFetchPropertySet() throws Exception {
		fetchPropertySet();
	}


	@Test
	public void testSetTextDataField() {
		PropertySet propertySet = fetchPropertySet();
		TextDataField dataField = null;
		PropertySet detachedPropertySet = null;
		TextDataField detachedDataField = null;
		try {
			dataField = (TextDataField) propertySet.getDataField(PropertySetTestStruct.TESTBLOCK_TEXT);
			dataField.setData("Test Text");
		} catch (Exception e) {
			throw new RuntimeException("Setting text of TextDataField failed", e);
		}
		try {
			propertySet.deflate();
			detachedPropertySet = getPropertyManager().storePropertySet(propertySet, true, FETCH_GROUPS, FETCH_DEPTH);
			detachedDataField = detachedPropertySet.getDataField(PropertySetTestStruct.TESTBLOCK_TEXT, TextDataField.class);
		} catch (Exception e) {
			throw new RuntimeException("Storing PropertySet with TextDataField failed", e);
		}
		assertEquals("Text field text differs", dataField.getText(), detachedDataField.getText());
	}

	@Test
	public void testSetRegexDataField() {
		PropertySet propertySet = fetchPropertySet();
		RegexDataField dataField = null;
		PropertySet detachedPropertySet = null;
		RegexDataField detachedDataField = null;
		try {
			dataField = propertySet.getDataField(PropertySetTestStruct.TESTBLOCK_REGEX, RegexDataField.class);
			dataField.setText("nobody@nosuchdomain.org");
		} catch (Exception e) {
			throw new RuntimeException("Setting text of RegexDataField failed", e);
		}
		try {
			propertySet.deflate();
			detachedPropertySet = getPropertyManager().storePropertySet(propertySet, true, FETCH_GROUPS, FETCH_DEPTH);
			detachedDataField = detachedPropertySet.getDataField(PropertySetTestStruct.TESTBLOCK_REGEX, RegexDataField.class);
		} catch (Exception e) {
			throw new RuntimeException("Storing PropertySet with RegexDataField failed", e);
		}
		assertEquals("Regex field text differs", dataField.getText(), detachedDataField.getText());
	}

	@Test
	public void testSetNumberDataField() {
		PropertySet propertySet = fetchPropertySet();
		NumberDataField dataField = null;
		PropertySet detachedPropertySet = null;
		NumberDataField detachedDataField = null;
		try {
			dataField = propertySet.getDataField(PropertySetTestStruct.TESTBLOCK_NUMBER, NumberDataField.class);
			dataField.setValue(123456);
		} catch (Exception e) {
			throw new RuntimeException("Setting number of NumberDataField failed", e);
		}
		try {
			propertySet.deflate();
			detachedPropertySet = getPropertyManager().storePropertySet(propertySet, true, FETCH_GROUPS, FETCH_DEPTH);
			detachedDataField = ((NumberDataField) detachedPropertySet.getDataField(PropertySetTestStruct.TESTBLOCK_NUMBER));
		} catch (Exception e) {
			throw new RuntimeException("Storing PropertySet with NumberDataField failed", e);
		}
		assertEquals("Number field numbers differ", dataField.getIntValue(), detachedDataField.getIntValue());
	}

	@Test
	public void testSetPhoneNumberDataField() {
		String cCode = "+49";
		String aCode = "(0)761";
		String lNumber = "123456789";
		PropertySet propertySet = fetchPropertySet();
		try {
			PhoneNumberDataField dataField = propertySet.getDataField(PropertySetTestStruct.TESTBLOCK_PHONENUMBER, PhoneNumberDataField.class);
			dataField.setCountryCode(cCode);
			dataField.setAreaCode(aCode);
			dataField.setLocalNumber(lNumber);
		} catch (Exception e) {
			throw new RuntimeException("Setting numbers of PhoneNumberDataField failed", e);
		}
		PropertySet detachedPropertySet = null;
		try {
			propertySet.deflate();
			detachedPropertySet = getPropertyManager().storePropertySet(propertySet, true, FETCH_GROUPS, FETCH_DEPTH);
		} catch (Exception e) {
			throw new RuntimeException("Storing PropertySet with PhoneNumberDataField failed", e);
		}
		PhoneNumberDataField detachedField = null;
		try {
			detachedField = detachedPropertySet.getDataField(PropertySetTestStruct.TESTBLOCK_PHONENUMBER, PhoneNumberDataField.class);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		assertEquals("Phone number field country code differs", cCode, detachedField.getCountryCode());
		assertEquals("Phone number field area code differs", aCode, detachedField.getAreaCode());
		assertEquals("Phone number field local number differs", lNumber, detachedField.getLocalNumber());
	}

	@Test
	public void testSelectionDataField() {

		PropertySet propertySet = fetchPropertySet();
		SelectionDataField dataField = null;
		PropertySet detachedPropertySet = null;
		SelectionDataField detachedDataField = null;
		try {
			dataField = propertySet.getDataField(PropertySetTestStruct.TESTBLOCK_SELECTION, SelectionDataField.class);
			SelectionStructField structField = (SelectionStructField) propertySet.getStructure().getStructField(PropertySetTestStruct.TESTBLOCK_SELECTION);
			dataField.setSelection(structField.getStructFieldValue(PropertySetTestStruct.TESTBLOCK_SELECTION_1));
		} catch (Exception e) {
			throw new RuntimeException("Setting selection of SelectionDataField failed", e);
		}
		try {
			propertySet.deflate();
			detachedPropertySet = getPropertyManager().storePropertySet(propertySet, true, FETCH_GROUPS, FETCH_DEPTH);
			detachedDataField = detachedPropertySet.getDataField(PropertySetTestStruct.TESTBLOCK_SELECTION, SelectionDataField.class);
		} catch (Exception e) {
			throw new RuntimeException("Storing PropertySet with SelectionDataField failed", e);
		}
		assertEquals("Selection field selection differs", dataField.getStructFieldValueID(), detachedDataField.getStructFieldValueID());
	}

	@Test
	public void testDateDataField() {
		Date date = new Date();
		PropertySet propertySet = fetchPropertySet();
		DateDataField dataField = null;
		PropertySet detachedPropertySet = null;
		DateDataField detachedDataField = null;
		try {
			dataField = propertySet.getDataField(PropertySetTestStruct.TESTBLOCK_DATE, DateDataField.class);
			dataField.setDate(date);
		} catch (Exception e) {
			throw new RuntimeException("Setting date of DateDataField failed", e);
		}
		try {
			propertySet.deflate();
			detachedPropertySet = getPropertyManager().storePropertySet(propertySet, true, FETCH_GROUPS, FETCH_DEPTH);
		} catch (Exception e) {
			throw new RuntimeException("Storing PropertySet with DateDataField failed", e);
		}
		try {
			detachedDataField = detachedPropertySet.getDataField(PropertySetTestStruct.TESTBLOCK_DATE, DateDataField.class);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		assertEquals("Date field date differs", dataField.getDate(), detachedDataField.getDate());
	}

	@Test
	public void testImageDataField() {
		PropertySet propertySet = fetchPropertySet();
		try {
			ImageDataField dataField = propertySet.getDataField(PropertySetTestStruct.TESTBLOCK_IMAGE, ImageDataField.class);
			URL url = getClass().getResource("JFire_Test.gif");
			if (url == null)
				return;
			InputStream in = url.openStream();
			try {
				dataField.loadStream(in, "JFire_Test.gif", "image/gif");
			} finally {
				in.close();
			}
		} catch (Exception e) {
			throw new RuntimeException("Setting image of ImageDataField failed", e);
		}
		try {
			propertySet.deflate();
			getPropertyManager().storePropertySet(propertySet, true, FETCH_GROUPS, FETCH_DEPTH);
		} catch (Exception e) {
			throw new RuntimeException("Storing PropertySet with ImageDataField failed", e);
		}
	}

//	public static void main(String[] args) throws Exception {
//		JFireSecurityConfiguration.declareConfiguration();
//		JFireLogin login = new JFireLogin("chezfrancois.jfire.org", "francois", "test");
//		login.login();
//		JFireTestManager testManager = JFireTestManagerUtil.getHome(login.getInitialContextProperties()).create();
//		testManager.runTestSuites(Collections.singletonList(JFirePropertySetTestSuite.class));
//	}
}
