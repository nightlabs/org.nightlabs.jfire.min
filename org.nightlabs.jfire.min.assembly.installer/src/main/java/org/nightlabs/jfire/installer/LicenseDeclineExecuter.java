package org.nightlabs.jfire.installer;

import org.nightlabs.installer.base.InstallationException;
import org.nightlabs.installer.base.defaults.DefaultExecuter;

/**
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public class LicenseDeclineExecuter extends DefaultExecuter
{
	/* (non-Javadoc)
	 * @see org.nightlabs.installer.base.DefaultExecuter#execute()
	 */
	@Override
	public void execute() throws InstallationException
	{
		if(!"yes".equals(getInstallationEntity().getParent().getResult("10_license.20_confirm.result"))) //$NON-NLS-1$ //$NON-NLS-2$
			System.exit(0);
	}
}
