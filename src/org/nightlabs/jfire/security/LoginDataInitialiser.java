package org.nightlabs.jfire.security;

import javax.naming.InitialContext;

import org.nightlabs.j2ee.LoginData;
import org.nightlabs.jfire.init.InitException;
import org.nightlabs.jfire.server.Server;
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

					// TODO do we really need these default values?
					// Is there any way we can get rid of them?
					// I think we should think about a better solution. Marco.
					String initialContextFactory = jfsmf.getInitialContextFactory(server.getJ2eeServerType(), true);
					String initialContextURL = server.getInitialContextURL(Server.PROTOCOL_JNP, true);

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
