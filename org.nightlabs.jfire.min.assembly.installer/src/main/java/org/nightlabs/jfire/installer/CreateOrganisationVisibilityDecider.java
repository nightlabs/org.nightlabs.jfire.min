package org.nightlabs.jfire.installer;

import org.nightlabs.installer.base.InstallationException;
import org.nightlabs.installer.base.defaults.DefaultVisibilityDecider;

/**
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public class CreateOrganisationVisibilityDecider extends
		DefaultVisibilityDecider
{
	/* (non-Javadoc)
	 * @see org.nightlabs.installer.base.defaults.DefaultVisibilityDecider#isVisible()
	 */
	@Override
	public boolean isVisible() throws InstallationException
	{
		return "true".equals(getInstaller().getResult("65_organisations.20_createOrganisation.result")); //$NON-NLS-1$ //$NON-NLS-2$
	}
}
