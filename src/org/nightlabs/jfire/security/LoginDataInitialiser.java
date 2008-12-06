package org.nightlabs.jfire.security;

import javax.naming.InitialContext;

import org.nightlabs.j2ee.LoginData;
import org.nightlabs.jfire.init.InitException;
import org.nightlabs.jfire.serverinit.ServerInitialiserDelegate;
import org.nightlabs.jfire.servermanager.JFireServerManager;
import org.nightlabs.jfire.servermanager.JFireServerManagerFactory;
import org.nightlabs.jfire.servermanager.config.JFireServerConfigModule;
import org.nightlabs.jfire.servermanager.config.ServerCf;

public class LoginDataInitialiser extends ServerInitialiserDelegate {

	@Override
	public void initialise() throws InitException {
		try {
			InitialContext ctx = new InitialContext();
			try {
				JFireServerManagerFactory jfsmf = (JFireServerManagerFactory)ctx.lookup(JFireServerManagerFactory.JNDI_NAME);

				JFireServerManager jfsm = jfsmf.getJFireServerManager();
				try {
					JFireServerConfigModule cfMod = jfsm.getJFireServerConfigModule();

					ServerCf server = cfMod.getLocalServer();
					if (server == null) // There is no configuration, yet. Silently exit.
						return;

					String initialContextFactory = jfsmf.getInitialContextFactory(server.getJ2eeServerType(), true);
					String initialContextURL = server.getInitialContextURL();

					LoginData.setDefaultInitialContextFactory(initialContextFactory);
					LoginData.setDefaultProviderURL(initialContextURL);
				} finally {
					jfsm.close();
				}

			} finally {
				ctx.close();
			}
		} catch (Exception x) {
			throw new InitException(x);
		}
	}

}
