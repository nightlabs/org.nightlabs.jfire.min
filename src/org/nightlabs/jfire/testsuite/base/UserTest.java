/**
 * 
 */
package org.nightlabs.jfire.testsuite.base;

import java.rmi.RemoteException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import javax.ejb.CreateException;
import javax.naming.NamingException;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.nightlabs.jfire.base.JFireEjb3Factory;
import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.person.Person;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.testsuite.JFireTestSuite;
import org.nightlabs.jfire.trade.LegalEntity;
import org.nightlabs.jfire.trade.Order;

/**
 * A simple TestCase for demonstration.
 * With the annotation it is linked to
 * the JFireBaseTestSuite, that will check
 * if JFireBase is deployed.
 */
@JFireTestSuite(JFireBaseTestSuite.class)
public class UserTest extends TestCase
{
	Logger logger = Logger.getLogger(UserTest.class);

	@Override
	protected void setUp() throws Exception
	{
		logger.info("setUp: invoked");
	}

	@Override
	protected void tearDown()
			throws Exception
	{
		logger.info("tearDown: invoked");
	}

	/**
	 * This method is invoked by the JUnit run,
	 * as its name starts with test!
	 */
	public void testCreateUser() throws Exception{		
		
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
		
		// Create the Person (a person in a JFire datastore).
		Person person = getJFireTestSuiteBaseManager().createPerson(
				           company, name, firstName, eMail, dob, salutation, title, postAdress, postCode,
				           postCity, postRegion, postCountry, phoneCountryCode, phoneAreaCode, phoneNumber, faxCountryCode,
				           faxAreaCode, faxNumber, bankAccountHolder, bankAccountNumber, bankCode, bankName, creditCardHolder, creditCardNumber,
				           creditCardExpiryMonth, creditCardExpiryYear, comment);
		
		
		logger.info("test Create Person: end");

		logger.info("test Create LegalEntity: begin");
		LegalEntity legalEntity = getJFireTestSuiteBaseManager().createLegalEntity(person);
		
		logger.info("test Create LegalEntity: end");
		
		logger.info("testCreateUser: begin");
				
		String userID = "User"+String.valueOf(ID);
		String password = "test";
		User user = getJFireTestSuiteBaseManager().createUser(userID, password, person);			
		
		// TODO implement!
		// It does not do very much, though
		logger.info("testCreateUser: end");
	}
	
	
	private JFireTestSuiteBaseManagerRemote getJFireTestSuiteBaseManager() throws RemoteException, CreateException, NamingException {
		return JFireEjb3Factory.getRemoteBean(JFireTestSuiteBaseManagerRemote.class, SecurityReflector.getInitialContextProperties());
	}
	
	
	
	
	
	/**
	 * This method is invoked by the JUnit run,
	 * as it is annotated with the Test annotation.
	 */
	@Test
	public void listUsers() {
		// if fails, however ;-)
		// fail("Well, somewhere is an error.");

		logger.info("listUsers: begin");
		// TODO implement!
		logger.info("listUsers: end");
	}
}
