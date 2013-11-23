package org.nightlabs.jfire.installer;

import org.nightlabs.installer.base.InstallationException;
import org.nightlabs.installer.base.defaults.DefaultVisibilityDecider;

import com.izforge.izpack.util.OsVersion;

/**
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public class OSTypeWindowsVisibilityDecider extends DefaultVisibilityDecider 
{
	/* (non-Javadoc)
	 * @see org.nightlabs.installer.base.defaults.DefaultVisibilityDecider#isVisible()
	 */
	@Override
	public boolean isVisible() throws InstallationException {
		return OsVersion.IS_WINDOWS;
	}
}
