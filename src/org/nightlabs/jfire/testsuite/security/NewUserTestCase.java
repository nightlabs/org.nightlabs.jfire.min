package org.nightlabs.jfire.testsuite.security;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;

import javax.jdo.FetchPlan;
import javax.jdo.JDOHelper;
import javax.security.auth.login.LoginException;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jdo.query.QueryCollection;
import org.nightlabs.jfire.base.JFireEjb3Factory;
import org.nightlabs.jfire.base.expression.IExpression;
import org.nightlabs.jfire.base.login.JFireLogin;
import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.person.Person;
import org.nightlabs.jfire.person.PersonStruct;
import org.nightlabs.jfire.prop.IStruct;
import org.nightlabs.jfire.prop.PropertyManagerRemote;
import org.nightlabs.jfire.prop.PropertySet;
import org.nightlabs.jfire.prop.datafield.PhoneNumberDataField;
import org.nightlabs.jfire.prop.datafield.SelectionDataField;
import org.nightlabs.jfire.prop.structfield.SelectionStructField;
import org.nightlabs.jfire.prop.structfield.StructFieldValue;
import org.nightlabs.jfire.security.Authority;
import org.nightlabs.jfire.security.JFireSecurityManagerRemote;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.security.UserLocal;
import org.nightlabs.jfire.security.id.AuthorityID;
import org.nightlabs.jfire.security.id.AuthorizedObjectID;
import org.nightlabs.jfire.security.id.RoleGroupID;
import org.nightlabs.jfire.security.id.UserID;
import org.nightlabs.jfire.security.search.UserQuery;
import org.nightlabs.jfire.testsuite.JFireTestSuite;


/**
 *
 * @author Fitas Amine - fitas [at] nightlabs [dot] de
 *
 * A simple TestCase for demonstration.
 * With the annotation it is linked to
 * the JFireBaseTestSuite, that will check
 * if JFireBase is deployedsss.
 */
@JFireTestSuite(JFireBaseSecurityTestSuite.class)
public class NewUserTestCase extends TestCase
{
	Logger logger = Logger.getLogger(NewUserTestCase.class);

	private static ThreadLocal<UserID> newUserID = new ThreadLocal<UserID>();


	private static String[] FETCH_GROUP_USER =new String[]{FetchPlan.DEFAULT,
		User.FETCH_GROUP_NAME,
		User.FETCH_GROUP_USER_LOCAL,
		User.FETCH_GROUP_PERSON,
		PropertySet.FETCH_GROUP_FULL_DATA,
		IExpression.FETCH_GROUP_IEXPRESSION_FULL_DATA};



	//Commented out this nonsense method. It should use API methods rather than its own EJB and
	//especially it is wrong to work with a LegalEntity here. I'll remove the dependency on JFireTrade, too. Marco.

	/**
	 * This method is invoked by the JUnit run,
	 * as its name starts with test!
	 */
	@Test
	public void testCreateUser() throws Exception{

		Properties initialContextProperties = SecurityReflector.getInitialContextProperties();
		JFireSecurityManagerRemote sm = JFireEjb3Factory.getRemoteBean(JFireSecurityManagerRemote.class, initialContextProperties);		

		//
		long ID = IDGenerator.nextID(User.class);
		// Person 's information.
		String company = "Company";
		String name = "Name"+ID;
		String firstName = "Firstname";
		String eMail = "email";
		String dateOfBirth = "";
		String salutation = "Mr";
		String title = "Mr";
		String postAdress = "4B strasse";
		String postCode = "478541";
		String postCity = "Berlin";
		String postRegion = "";
		String postCountry = "Germany";
		String phoneCountryCode = "78";
		String phoneAreaCode = "41";
		String phoneNumber = "44444444";
		String faxCountryCode = "555555";
		String faxAreaCode = "55";
		String faxNumber = "8888888";
		String bankAccountHolder = "Name"+ID;
		String bankAccountNumber = "45115-54557";
		String bankCode = "code-111";
		String bankName = "Bank of Germany";
		String creditCardHolder = "Name"+ID;
		String creditCardNumber = "4551-4477-11447";
		int creditCardExpiryMonth = 11;
		int creditCardExpiryYear = 2020;
		String comment = "";

		DateFormat formatter = new SimpleDateFormat("dd.MM.yyyy", Locale.ENGLISH);
		Date dob;
		try { dob = formatter.parse( dateOfBirth ); }
		catch (ParseException e) { dob = new Date(); }

		logger.info("test Create Person: begin");

		Person newPerson = new Person(IDGenerator.getOrganisationID(), IDGenerator.nextID(PropertySet.class));


		PropertyManagerRemote pm2  = JFireEjb3Factory.getRemoteBean(PropertyManagerRemote.class, SecurityReflector.getInitialContextProperties());
		IStruct personStruct  = pm2.getFullStructLocal(newPerson.getStructLocalObjectID(), 
				new String[] {FetchPlan.DEFAULT, IStruct.FETCH_GROUP_ISTRUCT_FULL_DATA}, 
				NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT);


		newPerson.inflate(personStruct);
		newPerson.getDataField(PersonStruct.PERSONALDATA_COMPANY).setData(company);
		newPerson.getDataField(PersonStruct.PERSONALDATA_NAME).setData(name);
		newPerson.getDataField(PersonStruct.PERSONALDATA_FIRSTNAME).setData(firstName);
		newPerson.getDataField(PersonStruct.INTERNET_EMAIL).setData(eMail);
		newPerson.getDataField(PersonStruct.PERSONALDATA_DATEOFBIRTH).setData(dateOfBirth);

		SelectionStructField salutationSelectionStructField = (SelectionStructField) personStruct.getStructField(
				PersonStruct.PERSONALDATA, PersonStruct.PERSONALDATA_SALUTATION);
		StructFieldValue sfv = salutationSelectionStructField.getStructFieldValue(PersonStruct.PERSONALDATA_SALUTATION_MR);
		newPerson.getDataField(PersonStruct.PERSONALDATA_SALUTATION, SelectionDataField.class).setSelection(sfv);

		newPerson.getDataField(PersonStruct.PERSONALDATA_TITLE).setData(title);
		newPerson.getDataField(PersonStruct.POSTADDRESS_ADDRESS).setData(postAdress);
		newPerson.getDataField(PersonStruct.POSTADDRESS_POSTCODE).setData(postCode);
		newPerson.getDataField(PersonStruct.POSTADDRESS_CITY).setData(postCity);
		newPerson.getDataField(PersonStruct.POSTADDRESS_REGION).setData(postRegion);
		newPerson.getDataField(PersonStruct.POSTADDRESS_COUNTRY).setData(postCountry);

		PhoneNumberDataField  phoneNumberDF = newPerson.getDataField(PersonStruct.PHONE_PRIMARY, PhoneNumberDataField.class);
		phoneNumberDF.setCountryCode(phoneCountryCode);
		phoneNumberDF.setAreaCode(phoneAreaCode);
		phoneNumberDF.setLocalNumber(phoneNumber);

		PhoneNumberDataField faxDF = newPerson.getDataField(PersonStruct.FAX, PhoneNumberDataField.class);
		faxDF.setCountryCode(faxCountryCode);
		faxDF.setAreaCode(faxAreaCode);
		faxDF.setLocalNumber(faxNumber);

		newPerson.getDataField(PersonStruct.BANKDATA_ACCOUNTHOLDER).setData(bankAccountHolder);
		newPerson.getDataField(PersonStruct.BANKDATA_ACCOUNTNUMBER).setData(bankAccountNumber);
		newPerson.getDataField(PersonStruct.BANKDATA_BANKCODE).setData(bankCode);
		newPerson.getDataField(PersonStruct.BANKDATA_BANKNAME).setData(bankName);

		newPerson.getDataField(PersonStruct.CREDITCARD_CREDITCARDHOLDER).setData(creditCardHolder);
		newPerson.getDataField(PersonStruct.CREDITCARD_NUMBER).setData(creditCardNumber);

		SelectionStructField expiryMonthStructField = (SelectionStructField) personStruct.getStructField(
				PersonStruct.CREDITCARD, PersonStruct.CREDITCARD_EXPIRYMONTH);
		if (creditCardExpiryMonth < 1 || creditCardExpiryMonth > 12)
			sfv = null;
		else
			sfv = expiryMonthStructField.getStructFieldValue(PersonStruct.CREDITCARD_EXPIRYMONTHS[creditCardExpiryMonth - 1]);
		newPerson.getDataField(PersonStruct.CREDITCARD_EXPIRYMONTH, SelectionDataField.class).setSelection(sfv);

		newPerson.getDataField(PersonStruct.CREDITCARD_EXPIRYYEAR).setData(creditCardExpiryYear);

		newPerson.getDataField(PersonStruct.COMMENT_COMMENT).setData(comment);
		newPerson.setAutoGenerateDisplayName(true);
		newPerson.setDisplayName(null, personStruct);
		newPerson.deflate();

		logger.info("test Create Person: end");

		String userID = "UserTC"+String.valueOf(ID);
		String password = "test";

		logger.info("testCreateUser: begin");

		User newUser = new User(SecurityReflector.getUserDescriptor().getOrganisationID(), userID);
		UserLocal userLocal = new UserLocal(newUser);
		userLocal.setPasswordPlain(password);
		newUser.setPerson(newPerson);	

		newUser = sm.storeUser(newUser, password, true, FETCH_GROUP_USER, NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT);


		if(newUser!=null)
		{
			newUserID.set((UserID)JDOHelper.getObjectId(newUser));
			//	sm.grantAllRoleGroupsInAllAuthorities((UserID)JDOHelper.getObjectId(newUser));
			logger.info("the following User was created"+newUser.getName());
		}	
		logger.info("testCreateUser: end");
	}
	//  Assign the User the login right.
	@Test
	public void testAssignRoleGroupToNewUser() throws Exception{
		JFireSecurityManagerRemote sm = JFireEjb3Factory.getRemoteBean(JFireSecurityManagerRemote.class,SecurityReflector.getInitialContextProperties());

		final String LOGWITHOUT_WORKSTATION_ROLEGROUP_ID =  "org.nightlabs.jfire.workstation.loginWithoutWorkstation";

		User user = sm.getUsers(Collections.singleton(newUserID.get()), FETCH_GROUP_USER
				, NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT).iterator().next();

		AuthorityID authorityID = AuthorityID.create(SecurityReflector.getUserDescriptor().getOrganisationID(), 
				Authority.AUTHORITY_ID_ORGANISATION);
		RoleGroupID logUserRoleGroupID  = RoleGroupID.create(LOGWITHOUT_WORKSTATION_ROLEGROUP_ID);
		sm.setGrantedRoleGroups((AuthorizedObjectID) JDOHelper.getObjectId(user.getUserLocal()), authorityID, Collections.singleton(logUserRoleGroupID));		
		sm.storeUser(user, null, false, FETCH_GROUP_USER, NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT);	
	}


	@Test
	public void testLoginNewUser() throws Exception{

		logger.info("LoginNewUser: begin");
		JFireSecurityManagerRemote sm = JFireEjb3Factory.getRemoteBean(JFireSecurityManagerRemote.class,SecurityReflector.getInitialContextProperties());
		User user = sm.getUsers(Collections.singleton(newUserID.get()), FETCH_GROUP_USER,
				NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT).iterator().next();

		JFireLogin login = new JFireLogin(user.getUserLocal().getOrganisationID(), 
				user.getUserLocal().getUserID(), "test");
		try {
			login.login();
		} catch (LoginException e) {
			fail("Could not login with the new users");
			return;
		}

		login.logout();		
		logger.info("LoginNewUser: end");
	}

	/**
	 * This method is invoked by the JUnit run,
	 * as it is annotated with the Test annotation.
	 */
	@Test
	public void testQueryNewUsers() throws Exception{
		// if fails, however ;-)
		// fail("Well, somewhere is an error.");
		logger.info("listUsers: begin");
		// TODO implement!
		JFireSecurityManagerRemote sm = JFireEjb3Factory.getRemoteBean(JFireSecurityManagerRemote.class,SecurityReflector.getInitialContextProperties());
		final QueryCollection<UserQuery> queries =new QueryCollection<UserQuery>(User.class);
		UserQuery userQuery = new UserQuery();
		userQuery.setUserID("UserTC");
		queries.add(userQuery);
		Set<UserID> userIDs = sm.getUserIDs(queries);
		if (userIDs != null && !userIDs.isEmpty()) {
			Collection<User> users = sm.getUsers(userIDs, new String[] {FetchPlan.DEFAULT}, NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT);
			if (users.isEmpty())
				fail("No Users was found!!!");
			logger.info("the following Users found with");
			for (User user : users) {
				logger.info("name = "+user.getName());
			}
		}
		logger.info("listUsers: end");
	}
}
