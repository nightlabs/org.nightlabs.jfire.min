package org.nightlabs.jfire.organisationinit;

import java.io.Serializable;

import javax.jdo.PersistenceManager;

import org.nightlabs.jfire.asyncinvoke.Invocation;
import org.nightlabs.jfire.crossorganisationregistrationinit.Context;
import org.nightlabs.jfire.crossorganisationregistrationinit.CrossOrganisationRegistrationInitManager;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.servermanager.JFireServerManager;

public class CrossOrganisationRegistrationInitInvocation
extends Invocation
{
	private static final long serialVersionUID = 1L;

	private Context context;

	public CrossOrganisationRegistrationInitInvocation(Context context)
	{
		this.context = context;
	}

	@Override
	public Serializable invoke() throws Exception
	{
		JFireServerManager jfsm = null;
		PersistenceManager pm = null;
		try {
			jfsm = getJFireServerManager();
			pm = getPersistenceManager();
			CrossOrganisationRegistrationInitManager manager = new CrossOrganisationRegistrationInitManager(jfsm);
			String localOrganisationID = getOrganisationID();

			manager.initialiseOrganisation(
					getJFireServerManagerFactory(),
					jfsm.getJFireServerConfigModule().getLocalServer(),
					localOrganisationID,
					jfsm.jfireSecurity_createTempUserPassword(localOrganisationID, User.USER_ID_SYSTEM),
					context
			);

			return null;
		} finally {
			if (jfsm != null)
				jfsm.close();

			if (pm != null)
				pm.close();
		}
	}
}
