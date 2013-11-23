package org.nightlabs.jfire.installer;

import java.io.File;
import java.util.Properties;

import org.nightlabs.installer.base.InstallationEntity;
import org.nightlabs.installer.base.InstallationException;
import org.nightlabs.installer.base.defaults.DefaultValueProvider;

/**
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public class InstallationSubDirValueProvider extends DefaultValueProvider
{
	/* (non-Javadoc)
	 * @see org.nightlabs.installer.base.DefaultValueProvider#getValues()
	 */
	@Override
	public Properties getValues() throws InstallationException
	{
		String baseDirSource = getConfig().getProperty("baseDirSource"); //$NON-NLS-1$
		if(baseDirSource == null)
			throw new InstallationException(String.format(Messages.getString("InstallationSubDirValueProvider.configEntryNotFound"), "baseDirSource"));  //$NON-NLS-1$ //$NON-NLS-2$ $NON-NLS-2$
		String baseDir = getBaseDir(getInstallationEntity(), baseDirSource);
		String subDir = getConfig().getProperty("subDir"); //$NON-NLS-1$
		if(subDir == null)
			throw new InstallationException(String.format(Messages.getString("InstallationSubDirValueProvider.configEntryNotFound"), "subDir"));  //$NON-NLS-1$ //$NON-NLS-2$ $NON-NLS-2$
		File dir = new File(baseDir, subDir);

		Properties defaultValues = new Properties();
		defaultValues.setProperty("result", dir.getAbsolutePath()); //$NON-NLS-1$
		return defaultValues;
	}
	
	private static String getBaseDir(InstallationEntity entity, String baseDirSource) throws InstallationException
	{
		if(entity.haveResult(baseDirSource))
			return entity.getResult(baseDirSource);
		else {
			InstallationEntity parent = entity.getParent();
			if(parent == null)
				throw new InstallationException(Messages.getString("InstallationSubDirValueProvider.baseDirSourceError")+baseDirSource); //$NON-NLS-1$
			return getBaseDir(parent, baseDirSource);
		}
	}
}
