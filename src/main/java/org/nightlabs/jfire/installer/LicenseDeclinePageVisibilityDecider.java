package org.nightlabs.jfire.installer;

import org.nightlabs.installer.base.InstallationException;
import org.nightlabs.installer.base.defaults.DefaultVisibilityDecider;

/**
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public class LicenseDeclinePageVisibilityDecider extends DefaultVisibilityDecider
{

	/* (non-Javadoc)
	 * @see org.nightlabs.installer.base.DefaultVisibilityDecider#isVisible()
	 */
	@Override
	public boolean isVisible() throws InstallationException
	{
		return !"yes".equals(getInstallationEntity().getParent().getResult("10_license.20_confirm.result")); //$NON-NLS-1$ //$NON-NLS-2$
	}
}
