package org.nightlabs.jfire.installer;

import org.nightlabs.installer.base.InstallationException;
import org.nightlabs.installer.base.defaults.DefaultVisibilityDecider;

/**
 * @version $Revision$ - $Date$
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public class InstallationTypeUserDefinedVisibilityDecider extends DefaultVisibilityDecider
{
	/* (non-Javadoc)
	 * @see org.nightlabs.installer.base.DefaultVisibilityDecider#isVisible()
	 */
	@Override
	public boolean isVisible() throws InstallationException
	{
		return "userDefined".equals(getInstaller().getResult("05_welcome.20_installtype.result")); //$NON-NLS-1$ //$NON-NLS-2$
	}
}
