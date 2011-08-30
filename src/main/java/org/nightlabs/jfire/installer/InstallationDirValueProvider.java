package org.nightlabs.jfire.installer;

import java.io.File;
import java.util.Properties;

import org.nightlabs.installer.base.InstallationException;
import org.nightlabs.installer.base.defaults.DefaultValueProvider;

/**
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public class InstallationDirValueProvider extends DefaultValueProvider
{
	/* (non-Javadoc)
	 * @see org.nightlabs.installer.base.DefaultValueProvider#getValues()
	 */
	@Override
	public Properties getValues() throws InstallationException
	{
//		File base;
//		if(EnvironmentHelper.isWindows())
//			base = new File(System.getenv("ProgramFiles")); //$NON-NLS-1$
//		else {
//			String nonWindowsBaseDir = getConfig().getProperty("nonWindowsBaseDir"); //$NON-NLS-1$
//			if(nonWindowsBaseDir != null)
//				base = new File(nonWindowsBaseDir);
//			else
//				base = new File("/opt"); //$NON-NLS-1$
//		}

		// Since we have no trouble with access rights and installed all productive servers into the home directory
		// of a special user, we select the home-directory by default now.
		String userHome = System.getProperty("user.home"); //$NON-NLS-1$
		if (userHome == null)
			throw new IllegalStateException("System property user.home is not set! This should never happen!"); //$NON-NLS-1$

		File base = new File(userHome);

		String subDir = getConfig().getProperty("subDir"); //$NON-NLS-1$
		if(subDir == null)
			subDir = "jfire-server"; //$NON-NLS-1$

		File installDir = new File(base, subDir);

		Properties defaultValues = new Properties();
		defaultValues.setProperty("result", installDir.getAbsolutePath()); //$NON-NLS-1$
		return defaultValues;
	}
}
