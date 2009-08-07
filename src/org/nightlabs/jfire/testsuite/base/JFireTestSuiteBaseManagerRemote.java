package org.nightlabs.jfire.testsuite.base;

import java.util.Date;

import javax.annotation.security.RolesAllowed;
import javax.ejb.Remote;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import org.nightlabs.jfire.person.Person;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.trade.LegalEntity;

@Remote
public interface JFireTestSuiteBaseManagerRemote {

	
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_System_")
	void initialise() throws Exception;
	
	
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_Guest_")		
	public User createUser(String userID, String password, Person person) throws Exception;


	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_Guest_")
	public Person createPerson(String company, String name, String firstName, String eMail,
			Date dateOfBirth, String salutation, String title, String postAdress, String postCode,
			String postCity, String postRegion, String postCountry, String phoneCountryCode,
			String phoneAreaCode, String phoneNumber, String faxCountryCode,
			String faxAreaCode, String faxNumber, String bankAccountHolder, String bankAccountNumber,
			String bankCode, String bankName, String creditCardHolder, String creditCardNumber,
			int creditCardExpiryMonth, int creditCardExpiryYear, String comment)
	throws Exception;
	

	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_Guest_")
	public LegalEntity createLegalEntity(Person person)
	throws Exception;

}
