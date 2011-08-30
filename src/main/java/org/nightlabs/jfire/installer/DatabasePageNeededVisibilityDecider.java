package org.nightlabs.jfire.installer;

import org.nightlabs.installer.base.InstallationException;
import org.nightlabs.installer.base.defaults.DefaultVisibilityDecider;

/**
 * Show the database page only if one of the follwing:
 * - We are in user-defined mode
 * - We are not using the Derby presets
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public class DatabasePageNeededVisibilityDecider extends DefaultVisibilityDecider
{
	@Override
	public boolean isVisible() throws InstallationException
	{
		boolean userDefined = "userDefined".equals(getInstaller().getResult("05_welcome.20_installtype.result")); //$NON-NLS-1$ //$NON-NLS-2$
		boolean notDerbyPresets = !"JBossDerby".equals(getInstaller().getResult("20_localServer.70_installationPresets.result")); //$NON-NLS-1$ //$NON-NLS-2$
		return userDefined || notDerbyPresets;
	}
}
