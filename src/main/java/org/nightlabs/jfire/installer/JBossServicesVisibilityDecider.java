package org.nightlabs.jfire.installer;

import org.nightlabs.installer.base.InstallationException;
import org.nightlabs.installer.base.defaults.DefaultVisibilityDecider;

/**
 * @author Daniel Mazurek - Daniel.Mazurek [dot] nightlabs [dot] de
 *
 */
public class JBossServicesVisibilityDecider
//extends InstallationTypeUserDefinedVisibilityDecider
extends DefaultVisibilityDecider
{
	/* (non-Javadoc)
	 * @see org.nightlabs.installer.base.defaults.DefaultVisibilityDecider#isVisible()
	 */
	@Override
	public boolean isVisible() throws InstallationException
	{
		boolean configureServcies = "true".equals(getInstaller().getResult("20_localServer.80_configureServices.result")); //$NON-NLS-1$ //$NON-NLS-2$
		return configureServcies ? super.isVisible() : configureServcies;
	}
}
