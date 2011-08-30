package org.nightlabs.jfire.installer;

import java.net.URI;
import java.net.URISyntaxException;

import org.nightlabs.installer.base.VerificationException;
import org.nightlabs.installer.base.defaults.DefaultResultVerifier;

/**
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public class InitialContextURLVerifier extends DefaultResultVerifier
{
	@Override
	public void verify() throws VerificationException
	{
		String initialContextURL = getInstaller().getResult("20_localServer.40_initialContextURL.result"); //$NON-NLS-1$
		if(initialContextURL == null || initialContextURL.trim().isEmpty())
			throw new VerificationException("Initial context URL is not set");
		URI initialContextURI;
		try {
			initialContextURI = new URI(initialContextURL);
		} catch (URISyntaxException e) {
			throw new VerificationException(String.format("Invalid initial context URL set: %s", initialContextURL), e);
		}

		if("jnp".equals(initialContextURI.getScheme())) {
			String defaultBindAddress = getInstaller().getResult("20_localServer.55_bindAdress.result"); //$NON-NLS-1$
			String jndiBindAddress = getInstaller().getResult("25_serverServices.17_naming-host.result"); //$NON-NLS-1$
			if(jndiBindAddress == null || jndiBindAddress.trim().isEmpty())
				jndiBindAddress = defaultBindAddress;

			String jndiPortStr = getInstaller().getResult("25_serverServices.15_naming-port.result");
			if(jndiPortStr == null || jndiPortStr.trim().isEmpty())
				throw new VerificationException("Naming (JNDI) bind port not set");
			int jndiBindPort;
			try {
				jndiBindPort = Integer.parseInt(jndiPortStr);
			} catch(NumberFormatException e) {
				throw new VerificationException(String.format("Invalid Naming (JNDI) bind port set: %s", jndiPortStr));
			}

			URI boundInitialContextURI;
			try {
				boundInitialContextURI = new URI("jnp://"+jndiBindAddress+":"+jndiBindPort);
			} catch (URISyntaxException e) {
				throw new VerificationException("Invalid naming (JNDI) bind address or port", e);
			}

			if(!boundInitialContextURI.equals(initialContextURI)) {
				throw new VerificationException(String.format("Warning: The given Naming (JNDI) bind address does not match the given initial context URL. This may work depending on the network configuration but is not recommended.\nNaming (JNDI) bind address: %s\nInitial context URL: %s", boundInitialContextURI.toString(), initialContextURI.toString()));
			}
		}
	}
}
