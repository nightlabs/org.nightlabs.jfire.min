/**
 * 
 */
package org.nightlabs.jfire.testsuite.prop;

import java.rmi.RemoteException;
import java.util.Collections;
import java.util.List;

import javax.ejb.CreateException;
import javax.jdo.FetchPlan;
import javax.jdo.JDOHelper;
import javax.naming.NamingException;

import junit.framework.TestCase;

import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.base.jdo.cache.Cache;
import org.nightlabs.jfire.base.login.JFireLogin;
import org.nightlabs.jfire.base.login.JFireSecurityConfiguration;
import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.person.Person;
import org.nightlabs.jfire.person.PersonStruct;
import org.nightlabs.jfire.prop.PropertyManager;
import org.nightlabs.jfire.prop.PropertyManagerUtil;
import org.nightlabs.jfire.prop.PropertySet;
import org.nightlabs.jfire.prop.StructLocal;
import org.nightlabs.jfire.prop.dao.StructLocalDAO;
import org.nightlabs.jfire.prop.datafield.NumberDataField;
import org.nightlabs.jfire.prop.datafield.PhoneNumberDataField;
import org.nightlabs.jfire.prop.datafield.RegexDataField;
import org.nightlabs.jfire.prop.datafield.SelectionDataField;
import org.nightlabs.jfire.prop.datafield.TextDataField;
import org.nightlabs.jfire.prop.id.PropertyID;
import org.nightlabs.jfire.prop.structfield.SelectionStructField;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.jfire.testsuite.JFireTestManager;
import org.nightlabs.jfire.testsuite.JFireTestManagerUtil;
import org.nightlabs.jfire.testsuite.JFireTestSuite;
import org.nightlabs.jfire.testsuite.login.JFireTestLogin;
import org.nightlabs.progress.NullProgressMonitor;

/**
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 *
 */
@JFireTestSuite(JFirePropertySetTestSuite.class)
public class JFirePropertySetTestCase extends TestCase {

	/**
	 * 
	 */
	public JFirePropertySetTestCase() {
	}

	/**
	 * @param name
	 */
	public JFirePropertySetTestCase(String name) {
		super(name);
	}

	private static final String[] FETCH_GROUPS = new String[] {FetchPlan.DEFAULT, PropertySet.FETCH_GROUP_FULL_DATA};
	private static final int FETCH_DEPTH = NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT;
	
	private PropertyID propertySetID;
	private JFireLogin login;
	private boolean isSetup = false;
	

	protected PropertyManager getPropertyManager() throws RemoteException, CreateException, NamingException {
		return PropertyManagerUtil.getHome(login.getInitialContextProperties()).create();
	}
	
	@Override
	protected void setUp() throws Exception {
		if (isSetup)
			return;
		login = JFireTestLogin.getUserLogin(JFireTestLogin.USER_QUALIFIER_SERVER_ADMIN);
		login.login();
		PropertySet propertySet = new PropertySet(login.getOrganisationID(), IDGenerator.nextID(PropertySet.class), Person.class.getName(), StructLocal.DEFAULT_SCOPE);
		propertySet = getPropertyManager().storePropertySet(propertySet, true, FETCH_GROUPS, FETCH_DEPTH);
		propertySetID = (PropertyID) JDOHelper.getObjectId(propertySet);
		Cache.sharedInstance().open(SecurityReflector.getUserDescriptor().getSessionID());
		super.setUp();
		isSetup = true;
	}

	/**
	 * Fetch and explode the {@link PropertySet} used for this test.
	 * @return The propertySet used for this test.
	 */
	private PropertySet fetchPropertySet() {
		PropertySet propertySet;
		try {
			propertySet = getPropertyManager().getPropertySet(propertySetID, FETCH_GROUPS, FETCH_DEPTH);
		} catch (Exception e) {
			throw new RuntimeException("Fetching propertySet failed.", e);
		}
		try {
			StructLocal structLocal = StructLocalDAO.sharedInstance().getStructLocal(
					propertySet.getStructLocalLinkClass(), 
					propertySet.getStructLocalScope(), 
					new NullProgressMonitor()
			);
			structLocal.explodePropertySet(propertySet);
		} catch (Exception e) {
			throw new RuntimeException("Exploding propertySet failed", e);
		}
		return propertySet;
	}
	
	/**
	 * Test the fetching and exploding of a {@link PropertySet} 
	 */
	public void testFetchPropertySet() throws Exception {
		fetchPropertySet();
	}
	
	
	public void testSetTextDataField() {
		PropertySet propertySet = fetchPropertySet();
		try {
			((TextDataField) propertySet.getDataField(PersonStruct.PERSONALDATA_COMPANY)).setText("Test Company");
		} catch (Exception e) {
			throw new RuntimeException("Setting text of TextDataField failed", e);
		}
		try {
			propertySet.getStructure().implodePropertySet(propertySet);
			getPropertyManager().storePropertySet(propertySet, true, FETCH_GROUPS, FETCH_DEPTH);
		} catch (Exception e) {
			throw new RuntimeException("Storing PropertySet with TextDataField failed", e);
		}
	}
	
	public void testSetRegexDataField() {
		PropertySet propertySet = fetchPropertySet();
		try {
			((RegexDataField) propertySet.getDataField(PersonStruct.INTERNET_EMAIL)).setText("nobody@nosuchdomain.org");
		} catch (Exception e) {
			throw new RuntimeException("Setting text of RegexDataField failed", e);
		}
		try {
			propertySet.getStructure().implodePropertySet(propertySet);
			getPropertyManager().storePropertySet(propertySet, true, FETCH_GROUPS, FETCH_DEPTH);
		} catch (Exception e) {
			throw new RuntimeException("Storing PropertySet with RegexDataField failed", e);
		}
	}
	
	public void testSetNumberDataField() {
		if (true)
			return; // TODO: No NumberDataField used by now
		PropertySet propertySet = fetchPropertySet();
		try {
			((NumberDataField) propertySet.getDataField(PersonStruct.INTERNET_EMAIL)).setValue(123456);
		} catch (Exception e) {
			throw new RuntimeException("Setting text of NumberDataField failed", e);
		}
		try {
			propertySet.getStructure().implodePropertySet(propertySet);
			getPropertyManager().storePropertySet(propertySet, true, FETCH_GROUPS, FETCH_DEPTH);
		} catch (Exception e) {
			throw new RuntimeException("Storing PropertySet with NumberDataField failed", e);
		}
	}
	
	public void testSetPhoneNumberDataField() {
		PropertySet propertySet = fetchPropertySet();
		try {
			PhoneNumberDataField dataField = ((PhoneNumberDataField) propertySet.getDataField(PersonStruct.PHONE_PRIMARY));
			dataField.setCountryCode("+49");
			dataField.setAreaCode("(0)761");
			dataField.setLocalNumber("123456789");
		} catch (Exception e) {
			throw new RuntimeException("Setting text of PhoneNumberDataField failed", e);
		}
		try {
			propertySet.getStructure().implodePropertySet(propertySet);
			getPropertyManager().storePropertySet(propertySet, true, FETCH_GROUPS, FETCH_DEPTH);
		} catch (Exception e) {
			throw new RuntimeException("Storing PropertySet with PhoneNumberDataField failed", e);
		}
	}
	
	public void testSelectionDataField() {
		PropertySet propertySet = fetchPropertySet();
		try {
			SelectionDataField dataField = ((SelectionDataField) propertySet.getDataField(PersonStruct.PERSONALDATA_SALUTATION));
			SelectionStructField structField = (SelectionStructField) propertySet.getStructure().getStructField(PersonStruct.PERSONALDATA_SALUTATION);
			dataField.setSelection(structField.getStructFieldValue(PersonStruct.PERSONALDATA_SALUTATION_MR));
		} catch (Exception e) {
			throw new RuntimeException("Setting text of SelectionDataField failed", e);
		}
		try {
			propertySet.getStructure().implodePropertySet(propertySet);
			getPropertyManager().storePropertySet(propertySet, true, FETCH_GROUPS, FETCH_DEPTH);
		} catch (Exception e) {
			throw new RuntimeException("Storing PropertySet with SelectionDataField failed", e);
		}
	}
	
	public static void main(String[] args) throws Exception {
		JFireSecurityConfiguration.declareConfiguration();
		JFireLogin login = new JFireLogin("chezfrancois.jfire.org", "francois", "test");
		login.login();
		JFireTestManager testManager = JFireTestManagerUtil.getHome(login.getInitialContextProperties()).create();
		testManager.runTestSuites(Collections.singletonList(JFirePropertySetTestSuite.class));
	}
}
