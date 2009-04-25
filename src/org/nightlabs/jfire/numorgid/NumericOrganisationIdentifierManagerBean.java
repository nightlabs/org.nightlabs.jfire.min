package org.nightlabs.jfire.numorgid;

import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.jdo.FetchPlan;
import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;

import org.nightlabs.jdo.moduleregistry.ModuleMetaData;
import org.nightlabs.jfire.base.BaseSessionBeanImpl;
import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.idgenerator.IDNamespace;
import org.nightlabs.jfire.idgenerator.id.IDNamespaceID;
import org.nightlabs.jfire.numorgid.id.NumericOrganisationIdentifierID;
import org.nightlabs.jfire.organisation.id.OrganisationID;
import org.nightlabs.jfire.security.User;

/**
 * @ejb.bean
 * 		name="jfire/ejb/JFireNumericOrganisationID/NumericOrganisationIdentifierManager"
 *    jndi-name="jfire/ejb/JFireNumericOrganisationID/NumericOrganisationIdentifierManager"
 *    type="Stateless"
 *    transaction-type="Container"
 *
 * @ejb.util
 * 		generate="physical"
 */
@TransactionManagement(TransactionManagementType.CONTAINER)
@Stateless
public class NumericOrganisationIdentifierManagerBean
extends BaseSessionBeanImpl
implements NumericOrganisationIdentifierManagerRemote
{
	private static final long serialVersionUID = 1L;

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.numorgid.NumericOrganisationIdentifierManagerRemote#getNumericOrganisationIdentifier(java.lang.String, java.lang.Integer)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_Guest_")
	public NumericOrganisationIdentifier getNumericOrganisationIdentifier(String organisationID, Integer numericOrganisationID)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			String clientOrganisationID = getUserID();
			if (!clientOrganisationID.startsWith(User.USER_ID_PREFIX_TYPE_ORGANISATION))
				throw new IllegalStateException("Sorry, only organisations are allowed to call this method!");

			clientOrganisationID = clientOrganisationID.substring(User.USER_ID_PREFIX_TYPE_ORGANISATION.length());

			if (organisationID != null && numericOrganisationID != null)
				throw new IllegalArgumentException("At least one of the parameters must be null!");

			if (organisationID == null && numericOrganisationID == null)
				organisationID = clientOrganisationID;

			String localOrganisationID = getOrganisationID();
			String rootOrganisationID = getRootOrganisationID();

			if (! localOrganisationID.equals(rootOrganisationID))
				throw new IllegalStateException("You must not call this method for any other organisation than the root organisation. I am " + localOrganisationID + " - ask " + rootOrganisationID);

			pm.getFetchPlan().setGroup(FetchPlan.ALL);
			pm.getFetchPlan().setMaxFetchDepth(1);
			if (organisationID != null) {
				try {
					pm.getObjectById(OrganisationID.create(organisationID));
				} catch (JDOObjectNotFoundException x) {
					throw new UnknownOrganisationException(organisationID);
				}

				NumericOrganisationIdentifierID numericOrganisationIdentifierID = NumericOrganisationIdentifierID.create(organisationID);
				try {
					NumericOrganisationIdentifier numericOrganisationIdentifier = (NumericOrganisationIdentifier) pm.getObjectById(numericOrganisationIdentifierID);
					return pm.detachCopy(numericOrganisationIdentifier);
				} catch (JDOObjectNotFoundException x) {
					// fine - ask the ID-generator and create the record
				}

				long id = IDGenerator.nextID(NumericOrganisationIdentifier.class);
				if (id > NumericOrganisationIdentifier.MAX_NUMERIC_ORGANISATION_ID)
					throw new IllegalStateException("Out of range! The id generated by the IDGenerator exceeds NumericOrganisationIdentifier.MAX_NUMERIC_ORGANISATION_ID!");

				NumericOrganisationIdentifier numericOrganisationIdentifier = new NumericOrganisationIdentifier(organisationID, (int) id);
				numericOrganisationIdentifier = pm.makePersistent(numericOrganisationIdentifier);

				// We call the following method in order to ensure that we have no duplicate value (the UNIQUE query would throw an exception).
				NumericOrganisationIdentifier.getNumericOrganisationIdentifierByNumericID(pm, numericOrganisationIdentifier.getNumericOrganisationID());

				return pm.detachCopy(numericOrganisationIdentifier);
			}
			else {
				NumericOrganisationIdentifier numericOrganisationIdentifier = NumericOrganisationIdentifier.getNumericOrganisationIdentifierByNumericID(pm, numericOrganisationID.intValue());
				if (numericOrganisationIdentifier == null)
					throw new UnknownNumericOrganisationIdentifierException(numericOrganisationID);

				return pm.detachCopy(numericOrganisationIdentifier);
			}
		} finally {
			pm.close();
		}
	}

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type="Required"
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_Guest_")
	public NumericOrganisationIdentifier getNumericOrganisationIdentifier(NumericOrganisationIdentifierID numericOrganisationIdentifierID)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			return NumericOrganisationIdentifier.getNumericOrganisationIdentifier(pm, numericOrganisationIdentifierID.organisationID);
		} finally {
			pm.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.numorgid.NumericOrganisationIdentifierManagerRemote#getNumericOrganisationIdentifier(int)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_Guest_")
	public NumericOrganisationIdentifier getNumericOrganisationIdentifier(int numericOrganisationID)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			return NumericOrganisationIdentifier.getNumericOrganisationIdentifier(pm, numericOrganisationID);
		} finally {
			pm.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.numorgid.NumericOrganisationIdentifierManagerRemote#initialise()
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_System_")
	public void initialise()
	throws Exception
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			String localOrganisationID = getOrganisationID();
			String rootOrganisationID = getRootOrganisationID();

			if (!hasRootOrganisation()) {
				// For test systems it's good to have a valid value
				try {
					pm.getObjectById(NumericOrganisationIdentifierID.create(getOrganisationID()));
				} catch (JDOObjectNotFoundException x) {
					pm.makePersistent(new NumericOrganisationIdentifier(getOrganisationID(), NumericOrganisationIdentifier.MAX_NUMERIC_ORGANISATION_ID));
				}
			}

			if (ModuleMetaData.getModuleMetaData(pm, JFireNumericOrganisationIDEAR.MODULE_NAME) == null) {
				pm.makePersistent(new ModuleMetaData(
						JFireNumericOrganisationIDEAR.MODULE_NAME, "0.9.7.0.0.beta", "0.9.7.0.0.beta")
				);
			}

			if (!localOrganisationID.equals(rootOrganisationID))
				return; // We only want to assign a numeric organisation ID for the root organisation

			// Set conservative settings for the ID generator to not waste numeric organisation IDs due to the caching strategy
			IDNamespace idNamespace;
			try {
				idNamespace = (IDNamespace) pm.getObjectById(IDNamespaceID.create(rootOrganisationID, NumericOrganisationIdentifier.class.getName()));
			} catch (JDOObjectNotFoundException e) {
				idNamespace = new IDNamespace(rootOrganisationID, NumericOrganisationIdentifier.class.getName(), null);
				idNamespace.setCacheSizeClient(0);
				idNamespace.setCacheSizeServer(0);
				idNamespace.setNextID(1);

				idNamespace = pm.makePersistent(idNamespace);
			}

			try {
				NumericOrganisationIdentifierID numericOrganisationIdentifierID = NumericOrganisationIdentifierID.create(rootOrganisationID);
				pm.getObjectById(numericOrganisationIdentifierID);
				// if the id already exists, there's nothing to do
			} catch (JDOObjectNotFoundException x) {
				// otherwise we create it
				NumericOrganisationIdentifier numericOrganisationIdentifier = new NumericOrganisationIdentifier(rootOrganisationID, NumericOrganisationIdentifier.ROOT_ORGANISATION_NUMERIC_ORGANISATION_ID);
				numericOrganisationIdentifier = pm.makePersistent(numericOrganisationIdentifier);
			}
		} finally {
			pm.close();
		}
	}
}
