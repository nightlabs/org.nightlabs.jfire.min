package org.nightlabs.jfire.testsuite.cascadedauthentication;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.jdo.PersistenceManager;
import javax.naming.InitialContext;

import junit.framework.TestCase;

import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.jfire.servermanager.JFireServerManager;
import org.nightlabs.jfire.servermanager.JFireServerManagerFactory;
import org.nightlabs.jfire.servermanager.config.OrganisationCf;
import org.nightlabs.jfire.testsuite.TestSuite;
import org.nightlabs.jfire.testsuite.login.JFireTestLogin;

public class CascadedAuthenticationTestSuite
extends TestSuite
{
	private boolean hasRootOrganisation;

	/**
	 * @param classes
	 */
	public CascadedAuthenticationTestSuite(Class<? extends TestCase>... classes) {
		super(classes);
		setName("Test JFire Cascaded Authentication (cross organisation communication)");

		try {
			InitialContext initialContext = new InitialContext();

			hasRootOrganisation = Organisation.hasRootOrganisation(initialContext);
			if (hasRootOrganisation)
				requiredOrganisationIDs.add(Organisation.getRootOrganisationID(initialContext));

			initialContext.close();
		} catch (Exception x) {
			throw new RuntimeException(x);
		}

		requiredOrganisationIDs.add("chezfrancois.jfire.org");
		requiredOrganisationIDs.add("reseller.jfire.org");
	}

	private Set<String> requiredOrganisationIDs = new HashSet<String>();

	@Override
	public String canRunTests(PersistenceManager pm)
	throws Exception
	{
//		// as a first quick-check, we search for the module JFireDemoSetupMultiOrganisation
//		String className = "org.nightlabs.demo.multiorg.MultiOrganisationServerInitialiser";
//		try {
//			Class.forName(className);
//		} catch (ClassNotFoundException x) {
//			return "The module JFireDemoSetupMultiOrganisation seems not to be installed (Class \"" + className + "\" could not be found)!";
//		}

// TODO if I activate the following code, the tests fail - otherwise they succeed. I assume that's due to a mistake in the TestSuite framework: It should execute every test somehow in its own scope (maybe use a helper-bean?). Marco.
// The above mentioned problem should be solved by now (I'm using sub-transactions). Is it really?
		if (!hasRootOrganisation)
			return "This server is in standalone mode (has no root organisation)";

		// then we check whether the required organisations all exist and know each other.
		JFireServerManager jfsm = null;
		InitialContext initialContext = new InitialContext();
		try {
			JFireServerManagerFactory jfsmf = (JFireServerManagerFactory) initialContext.lookup(JFireServerManagerFactory.JNDI_NAME);
			jfsm = jfsmf.getJFireServerManager();

			// which organisations exist on the local server?
			List<OrganisationCf> organisationCfs = jfsm.getOrganisationCfs(false);
			Set<String> organisationIDs = new HashSet<String>(organisationCfs.size());
			for (OrganisationCf organisationCf : organisationCfs)
				organisationIDs.add(organisationCf.getOrganisationID());

			// are all required organisations here?
			for (String requiredOrganisationID : requiredOrganisationIDs) {
				if (!organisationIDs.contains(requiredOrganisationID))
					return "This server does not host the demo-organisation \"" + requiredOrganisationID + "\" which should have been created by the module JFireDemoSetupMultiOrganisation.";
			}

			// TODO are the cross-organisation-registrations complete?

		} finally {
			initialContext.close();

			if (jfsm != null)
				jfsm.close();
		}

		if (JFireTestLogin.checkCreateLoginsAndRegisterInAuthorities(pm))
			return null;
		else
			return "Creating users was not successful!";
	}
}
