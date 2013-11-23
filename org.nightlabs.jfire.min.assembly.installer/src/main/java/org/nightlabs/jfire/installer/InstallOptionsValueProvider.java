package org.nightlabs.jfire.installer;

import java.util.Properties;

import org.nightlabs.installer.base.InstallationException;
import org.nightlabs.installer.base.defaults.DefaultValueProvider;

import com.izforge.izpack.util.OsVersion;

/**
 * Disable all shortcut stuff by default if not on a Windows OS.
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public class InstallOptionsValueProvider extends DefaultValueProvider 
{
	/* (non-Javadoc)
	 * @see org.nightlabs.installer.base.defaults.DefaultValueProvider#getValues()
	 */
	@Override
	public Properties getValues() throws InstallationException 
	{
		Properties values = new Properties();
		if(OsVersion.IS_WINDOWS) {
			values.setProperty("10_createDesktopEntry.result", "true");
			values.setProperty("20_createStartMenuEntry.result", "true");
		} else {
			values.setProperty("10_createDesktopEntry.result", "false");
			values.setProperty("20_createStartMenuEntry.result", "false");
		}
		return values;
	}
}
