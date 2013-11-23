package org.nightlabs.jfire.installer;

import org.nightlabs.installer.base.InstallationException;

/**
 * @version $Revision: 11209 $ - $Date: 2008-07-02 16:47:57 +0200 (Mi, 02 Jul 2008) $
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public class InstallationTypeNotUserDefinedVisibilityDecider extends InstallationTypeUserDefinedVisibilityDecider
{
	/* (non-Javadoc)
	 * @see org.nightlabs.installer.base.DefaultVisibilityDecider#isVisible()
	 */
	@Override
	public boolean isVisible() throws InstallationException
	{
		return !super.isVisible();
	}
}
