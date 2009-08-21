package org.nightlabs.jfire.testsuite.base;


import java.util.Date;

import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;

import org.apache.log4j.Logger;
import org.nightlabs.jfire.base.BaseSessionBeanImpl;
import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.person.Person;
import org.nightlabs.jfire.person.PersonStruct;
import org.nightlabs.jfire.prop.IStruct;
import org.nightlabs.jfire.prop.PropertySet;
import org.nightlabs.jfire.prop.datafield.PhoneNumberDataField;
import org.nightlabs.jfire.prop.datafield.SelectionDataField;
import org.nightlabs.jfire.prop.structfield.SelectionStructField;
import org.nightlabs.jfire.prop.structfield.StructFieldValue;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.security.UserLocal;
import org.nightlabs.jfire.security.UserSecurityGroup;
import org.nightlabs.jfire.security.id.UserID;
import org.nightlabs.jfire.trade.LegalEntity;
import org.nightlabs.jfire.trade.Trader;

/**
 * @author Fitas Amine <!-- fitas[at]nightlabs[dot]de -->
 * 
 */
@TransactionAttribute(TransactionAttributeType.REQUIRED)
@TransactionManagement(TransactionManagementType.CONTAINER)
@Stateless
public class JFireTestSuiteBaseManagerBean extends BaseSessionBeanImpl
implements JFireTestSuiteBaseManagerRemote{

	private static final Logger logger = Logger.getLogger(JFireTestSuiteBaseManagerBean.class);

	private static final long serialVersionUID = 1L;


	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_System_")
	public void initialise() throws Exception {
		// TODO Auto-generated method stub

	}

	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_Guest_")
	public User createUser(String userID, String password, Person person)throws Exception
	{

		PersistenceManager pm = createPersistenceManager();
		try{
			pm.getExtent(User.class);
			try {
				User user = (User) pm.getObjectById(UserID.create(getOrganisationID(), userID));
				// it already exists => return
				return user;
			} catch (JDOObjectNotFoundException x) {
				// fine, it doesn't exist yet
			}

			User user = new User(getOrganisationID(), userID);
			UserLocal userLocal = new UserLocal(user);
			userLocal.setPasswordPlain(password);
			user.setPerson(person);
			user = pm.makePersistent(user);
			return user;
		}
		finally{
			pm.close();
		}
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_Guest_")
	public Person createPerson(String company, String name, String firstName, String eMail,
			Date dateOfBirth, String salutation, String title, String postAdress, String postCode,
			String postCity, String postRegion, String postCountry, String phoneCountryCode,
			String phoneAreaCode, String phoneNumber, String faxCountryCode,
			String faxAreaCode, String faxNumber, String bankAccountHolder, String bankAccountNumber,
			String bankCode, String bankName, String creditCardHolder, String creditCardNumber,
			int creditCardExpiryMonth, int creditCardExpiryYear, String comment)
	throws Exception
	{

		PersistenceManager pm = createPersistenceManager();
		try{
			IStruct personStruct = PersonStruct.getPersonStructLocal(pm);		
			Person person = new Person(IDGenerator.getOrganisationID(), IDGenerator.nextID(PropertySet.class));
			person.inflate(personStruct);
			person.getDataField(PersonStruct.PERSONALDATA_COMPANY).setData(company);
			person.getDataField(PersonStruct.PERSONALDATA_NAME).setData(name);
			person.getDataField(PersonStruct.PERSONALDATA_FIRSTNAME).setData(firstName);
			person.getDataField(PersonStruct.INTERNET_EMAIL).setData(eMail);
			person.getDataField(PersonStruct.PERSONALDATA_DATEOFBIRTH).setData(dateOfBirth);

			SelectionStructField salutationSelectionStructField = (SelectionStructField) personStruct.getStructField(
					PersonStruct.PERSONALDATA, PersonStruct.PERSONALDATA_SALUTATION);
			StructFieldValue sfv = salutationSelectionStructField.getStructFieldValue(PersonStruct.PERSONALDATA_SALUTATION_MR);
			person.getDataField(PersonStruct.PERSONALDATA_SALUTATION, SelectionDataField.class).setSelection(sfv);

			person.getDataField(PersonStruct.PERSONALDATA_TITLE).setData(title);
			person.getDataField(PersonStruct.POSTADDRESS_ADDRESS).setData(postAdress);
			person.getDataField(PersonStruct.POSTADDRESS_POSTCODE).setData(postCode);
			person.getDataField(PersonStruct.POSTADDRESS_CITY).setData(postCity);
			person.getDataField(PersonStruct.POSTADDRESS_REGION).setData(postRegion);
			person.getDataField(PersonStruct.POSTADDRESS_COUNTRY).setData(postCountry);

			PhoneNumberDataField phoneNumberDF = person.getDataField(PersonStruct.PHONE_PRIMARY, PhoneNumberDataField.class);
			phoneNumberDF.setCountryCode(phoneCountryCode);
			phoneNumberDF.setAreaCode(phoneAreaCode);
			phoneNumberDF.setLocalNumber(phoneNumber);

			PhoneNumberDataField faxDF = person.getDataField(PersonStruct.FAX, PhoneNumberDataField.class);
			faxDF.setCountryCode(faxCountryCode);
			faxDF.setAreaCode(faxAreaCode);
			faxDF.setLocalNumber(faxNumber);

			person.getDataField(PersonStruct.BANKDATA_ACCOUNTHOLDER).setData(bankAccountHolder);
			person.getDataField(PersonStruct.BANKDATA_ACCOUNTNUMBER).setData(bankAccountNumber);
			person.getDataField(PersonStruct.BANKDATA_BANKCODE).setData(bankCode);
			person.getDataField(PersonStruct.BANKDATA_BANKNAME).setData(bankName);

			person.getDataField(PersonStruct.CREDITCARD_CREDITCARDHOLDER).setData(creditCardHolder);
			person.getDataField(PersonStruct.CREDITCARD_NUMBER).setData(creditCardNumber);

			//		((NumberDataField)person.getDataField(PersonStruct.CREDITCARD_EXPIRYMONTH)).setValue(creditCardExpiryMonth);

			SelectionStructField expiryMonthStructField = (SelectionStructField) personStruct.getStructField(
					PersonStruct.CREDITCARD, PersonStruct.CREDITCARD_EXPIRYMONTH);
			if (creditCardExpiryMonth < 1 || creditCardExpiryMonth > 12)
				sfv = null;
			else
				sfv = expiryMonthStructField.getStructFieldValue(PersonStruct.CREDITCARD_EXPIRYMONTHS[creditCardExpiryMonth - 1]);
			person.getDataField(PersonStruct.CREDITCARD_EXPIRYMONTH, SelectionDataField.class).setSelection(sfv);

			person.getDataField(PersonStruct.CREDITCARD_EXPIRYYEAR).setData(creditCardExpiryYear);

			person.getDataField(PersonStruct.COMMENT_COMMENT).setData(comment);

			person.setAutoGenerateDisplayName(true);
			person.setDisplayName(null, personStruct);
			person.deflate();
			pm.makePersistent(person);

			return person;
		}
		finally{
			pm.close();
		}
	}	

	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_Guest_")
	public UserSecurityGroup createUserGroup(String userGroupID) throws Exception
	{
		PersistenceManager pm = createPersistenceManager();
		try{
			UserSecurityGroup userSecurityGroup = new UserSecurityGroup(getOrganisationID(), userGroupID);
			return pm.makePersistent(userSecurityGroup);	
		}
		finally{
			pm.close();
		}

	}

	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_Guest_")
	public LegalEntity createLegalEntity(Person person) throws Exception
	{
		if (person == null)
			throw new IllegalArgumentException("person must not be null!");

		PersistenceManager pm = createPersistenceManager();
		try{		
			Trader trader = Trader.getTrader(pm);
			return trader.setPersonToLegalEntity(person, true);
		}
		finally{
			pm.close();
		}
	}	

}
