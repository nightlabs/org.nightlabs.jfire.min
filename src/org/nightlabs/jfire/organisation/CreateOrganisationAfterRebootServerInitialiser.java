package org.nightlabs.jfire.organisation;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.nightlabs.ModuleException;
import org.nightlabs.annotation.Implement;
import org.nightlabs.jfire.init.InitException;
import org.nightlabs.jfire.serverinit.ServerInitialiserDelegate;
import org.nightlabs.jfire.servermanager.JFireServerManager;
import org.nightlabs.jfire.servermanager.OrganisationNotFoundException;

public class CreateOrganisationAfterRebootServerInitialiser
		extends ServerInitialiserDelegate
{
	@Implement
	public void initialise() throws InitException
	{
		try {
			JFireServerManager jfsm = getJFireServerManagerFactory().getJFireServerManager();
			try {
				CreateOrganisationAfterRebootData coard = new CreateOrganisationAfterRebootData(jfsm);
				if (coard.isEmpty())
					return;

				if (jfsm.isNewServerNeedingSetup()) {
					Logger logger = Logger.getLogger(CreateOrganisationAfterRebootServerInitialiser.class);
					logger.error("Creation of organisations is not possible, because the basic server configuration is not complete yet! Configure and reboot the server!");
					return;
				}

				CreateOrganisationAfterRebootData.Descriptor descriptor = coard.fetchOrganisationCreationDescriptor();
				while (descriptor != null) {
					try {
						jfsm.getOrganisationConfig(descriptor.organisationID);
					} catch (OrganisationNotFoundException x) {
						// do initialization!
						try {
							jfsm.createOrganisation(descriptor.organisationID, descriptor.organisationDisplayName, descriptor.userID, descriptor.password, descriptor.isServerAdmin);
						} catch (ModuleException e) {
//							try { // TODO should we restore???
//								coard.addOrganisation(descriptor);
//							} catch (Throwable t) {
//								Logger logger = Logger.getLogger(CreateOrganisationAfterRebootServerInitialiser.class);
//								logger.error("Could not restore the organisation-create-request after organisation-creation failed!", t);
//							}
							throw new InitException(e.getMessage(), e);
						}
					}

					descriptor = coard.fetchOrganisationCreationDescriptor();
				}
			} finally {
				jfsm.close();
			}
		} catch (IOException x) {
			throw new InitException(x);
		}
	}

}
