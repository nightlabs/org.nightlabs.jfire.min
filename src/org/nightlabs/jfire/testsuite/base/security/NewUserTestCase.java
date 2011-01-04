package org.nightlabs.jfire.testsuite.base.security;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.both;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.nightlabs.jfire.testsuite.hamcrest.IsNotEmptyMatcher.isNotEmpty;
import static org.nightlabs.jfire.testsuite.hamcrest.IsNotNullMatcher.isNotNull;

import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Properties;
import java.util.Random;
import java.util.Set;

import javax.jdo.FetchPlan;
import javax.jdo.JDOHelper;
import javax.security.auth.login.LoginException;

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
import org.nightlabs.jfire.testsuite.TestCase;

/**
 *
 * @author Fitas Amine - fitas [at] nightlabs [dot] de
 *
 */

public class NewUserTestCase extends TestCase
{
	Logger logger = Logger.getLogger(NewUserTestCase.class);

	public static final String NEW_USER_PREFEXID = "UserTC";
	public static final String NEW_USER_PASSWORD = "test";
	private static String NEW_USER = "NEWUSER";

	private static String[] FETCH_GROUP_USER =new String[]{FetchPlan.DEFAULT,
		User.FETCH_GROUP_NAME,
		User.FETCH_GROUP_USER_LOCAL,
		User.FETCH_GROUP_PERSON,
		PropertySet.FETCH_GROUP_FULL_DATA,
		IExpression.FETCH_GROUP_IEXPRESSION_FULL_DATA};

	// a simple function to generate a random birthday date between 1955 until 1985
	private Date getRandomBirthDayDate()
	{
		Calendar cdr = Calendar.getInstance();
		Random rand = new Random();
		cdr.set(Calendar.DAY_OF_MONTH, 1 + rand.nextInt(25));
		cdr.set(Calendar.YEAR, rand.nextInt(30) + 1955);
		cdr.set(Calendar.MONTH, 1 + rand.nextInt(11));
		return cdr.getTime();
	}
	
	@Test
	public void testCreateUser() throws Exception{

		Properties initialContextProperties = SecurityReflector.getInitialContextProperties();
		JFireSecurityManagerRemote sm = JFireEjb3Factory.getRemoteBean(JFireSecurityManagerRemote.class, initialContextProperties);		
		long ID = IDGenerator.nextID(User.class);
		// Person 's information.
		String company = "Company";
		String name = "Name"+ID;
		String firstName = "Firstname";
		String eMail = "email";
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
		// pick up a random birthday
		Date birthDate = getRandomBirthDayDate();
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
		newPerson.getDataField(PersonStruct.PERSONALDATA_DATEOFBIRTH).setData(birthDate);

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
		String userID = NEW_USER_PREFEXID +String.valueOf(ID);

		logger.info("testCreateUser: begin");

		if(!sm.isUserIDAvailable(SecurityReflector.getUserDescriptor().getOrganisationID(), userID))
			fail("User ID is not Available.");

		User newUser = new User(SecurityReflector.getUserDescriptor().getOrganisationID(), userID);
		UserLocal userLocal = new UserLocal(newUser);
		userLocal.setPasswordPlain(NEW_USER_PASSWORD);
		newUser.setPerson(newPerson);	

		newUser = sm.storeUser(newUser, NEW_USER_PASSWORD, true, FETCH_GROUP_USER, NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT);
		assertThat(newUser,notNullValue());
		UserID newUserID = (UserID)JDOHelper.getObjectId(newUser);
		assertThat(sm.isUserIDAlreadyRegistered(newUserID), equalTo(true));
		setTestCaseContextObject(NEW_USER, newUserID);
		// TODO: currently we cant test grantAllRoleGroupsInAllAuthorities as it throws runtime exceptions.
		// take a look at https://www.jfire.org/modules/bugs/view.php?id=1337
		//	sm.grantAllRoleGroupsInAllAuthorities((UserID)JDOHelper.getObjectId(newUser));
		logger.info("the following User was created" + newUser.getName());
		logger.info("testCreateUser: end");
	}
	//  Assign the User the login right.
	@Test
	public void testAssignRoleGroupToNewUser() throws Exception{
		JFireSecurityManagerRemote sm = JFireEjb3Factory.getRemoteBean(JFireSecurityManagerRemote.class,SecurityReflector.getInitialContextProperties());
		UserID newUserID =  (UserID)getTestCaseContextObject(NEW_USER);
		final String LOGWITHOUT_WORKSTATION_ROLEGROUP_ID =  "org.nightlabs.jfire.workstation.loginWithoutWorkstation";
		User user = sm.getUsers(Collections.singleton(newUserID), FETCH_GROUP_USER
				, NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT).iterator().next();
		assertThat(user,notNullValue());
		// Assign login authority to the User
		AuthorityID authorityID = AuthorityID.create(SecurityReflector.getUserDescriptor().getOrganisationID(), 
				Authority.AUTHORITY_ID_ORGANISATION);
		RoleGroupID logUserRoleGroupID  = RoleGroupID.create(LOGWITHOUT_WORKSTATION_ROLEGROUP_ID);
		sm.setGrantedRoleGroups((AuthorizedObjectID) JDOHelper.getObjectId(user.getUserLocal()), authorityID, Collections.singleton(logUserRoleGroupID));		
		user = sm.storeUser(user, null, false, FETCH_GROUP_USER, NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT);	
		assertThat(user,nullValue());
	}

	// try to login the new user and violate a role group later on
	@Test
	public void testLoginNewUser() throws Exception{

		logger.info("LoginNewUser: begin");
		JFireSecurityManagerRemote sm = JFireEjb3Factory.getRemoteBean(JFireSecurityManagerRemote.class,SecurityReflector.getInitialContextProperties());
		UserID newUserID =  (UserID)getTestCaseContextObject(NEW_USER);
		User user = sm.getUsers(Collections.singleton(newUserID), FETCH_GROUP_USER,
				NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT).iterator().next();

		JFireLogin login = new JFireLogin(user.getUserLocal().getOrganisationID(), 
				user.getUserLocal().getUserID(), NEW_USER_PASSWORD);
		try {
			login.login();
		} catch (LoginException e) {
			fail("Could not login with the new users");
		}
		finally
		{
			login.logout();		
		}
		// try to login again and access unauthorized method
		try {
			login.login();
			// violate the role of querying users as this user doesnt have the right to do so.
			QueryNewUsers();
			fail("Could Access unauthirized EJB method");	
		} catch (Exception e) {
			// success because an exception should be thrown
			logger.info("ended with success");
		}
		finally
		{
			login.logout();
		}
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
		QueryNewUsers();
		logger.info("listUsers: end");
	}


	private void QueryNewUsers() throws Exception{
		JFireSecurityManagerRemote sm = JFireEjb3Factory.getRemoteBean(JFireSecurityManagerRemote.class,SecurityReflector.getInitialContextProperties());
		final QueryCollection<UserQuery> queries =new QueryCollection<UserQuery>(User.class);
		UserQuery userQuery = new UserQuery();
		userQuery.setUserID("UserTC");
		queries.add(userQuery);
		Set<UserID> userIDs = sm.getUserIDs(queries);
		assertThat("No UserIDs was found!!!",
				userIDs,both(isNotNull()).and(isNotEmpty())); 
		Collection<User> users = sm.getUsers(userIDs, new String[] {FetchPlan.DEFAULT}, NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT);
		assertThat("No Users was found!!!",
				users,both(isNotNull()).and(isNotEmpty())); 
		logger.info("the following Users found with");
		for (User user : users) {
			logger.info("name = "+user.getName());
		}
	}
}
