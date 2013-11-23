package org.nightlabs.jfire.installer;

import org.nightlabs.installer.base.InstallationException;
import org.nightlabs.installer.base.defaults.DefaultVisibilityDecider;

/**
 * Credentials not needed for Derby.
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public class DatabaseCredentialsVisibilityDecider extends DefaultVisibilityDecider
{
	@Override
	public boolean isVisible() throws InstallationException
	{
		return !"JBossDerby".equals(getInstaller().getResult("20_localServer.70_installationPresets.result")); //$NON-NLS-1$ //$NON-NLS-2$
	}
}
