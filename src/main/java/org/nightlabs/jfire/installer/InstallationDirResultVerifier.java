package org.nightlabs.jfire.installer;

import java.io.File;

import org.nightlabs.installer.base.VerificationException;
import org.nightlabs.installer.base.defaults.DefaultResultVerifier;

/**
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public class InstallationDirResultVerifier extends DefaultResultVerifier
{
	/* (non-Javadoc)
	 * @see org.nightlabs.installer.base.defaults.DefaultResultVerifier#verify()
	 */
	@Override
	public void verify() throws VerificationException
	{
		String installationDir = getInstallationEntity().getResult("10_installDir.result"); //$NON-NLS-1$
		File dir = new File(installationDir);
		boolean createdDir = false;
		try {
			if(!dir.exists()) {
				try {
					dir.mkdirs();
					createdDir = true;
				} catch (SecurityException e) {
					throw new VerificationException(String.format(Messages.getString("InstallationDirResultVerifier.dirCreateExceptionError"), installationDir, e.getLocalizedMessage())); //$NON-NLS-1$
				}
			}

			if(!dir.exists())
				throw new VerificationException(String.format(Messages.getString("InstallationDirResultVerifier.dirCreateError"), installationDir)); //$NON-NLS-1$

			File file = null;
			do {
				file = new File(dir, "removeme-"+Math.random()); //$NON-NLS-1$
			} while(file.exists());

			try {
				file.createNewFile();
				file.delete();
			} catch(Throwable e) {
				throw new VerificationException(String.format(Messages.getString("InstallationDirResultVerifier.fileCreateError"), installationDir, e.getLocalizedMessage())); //$NON-NLS-1$
			}
		} finally {
			if(createdDir)
				dir.delete();
		}
	}
}
