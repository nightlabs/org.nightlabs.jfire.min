package org.nightlabs.jfire.installer;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.nightlabs.installer.UIType;
import org.nightlabs.installer.base.InstallationException;
import org.nightlabs.installer.base.defaults.DefaultInitializer;
import org.nightlabs.installer.base.ui.SwingUI;
import org.nightlabs.installer.pages.ui.swing.InstallerFrame;
import org.nightlabs.installer.util.Programs;

/**
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 * @version $Revision$ - $Date$
 */
public class InstallerInitializer extends DefaultInitializer
{
	private static final String JAVA_VERSION_PREFIX = "java version \""; //$NON-NLS-1$

	private void checkEnvironment() throws InstallationException
	{
		String java = Programs.findJava();

//		System.out.println("Checking java with command '"+java+"'");

		try {
			Process process = Runtime.getRuntime().exec(new String[] {java, "-version"}); //$NON-NLS-1$
			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
			boolean foundVersion = false;
			String line;
			while((line = reader.readLine()) != null) {
//				System.out.println(line);
				if(line.startsWith(JAVA_VERSION_PREFIX)) {
					foundVersion = true;
					try {
						String version = line.substring(JAVA_VERSION_PREFIX.length());
						String[] parts = version.split("\\."); //$NON-NLS-1$
						int major = Integer.parseInt(parts[0]);
						int minor = Integer.parseInt(parts[1]);
//						System.out.println(major+"."+minor);
						if(major < 1 || (major == 1 && minor < 6))
							throw new InstallationException(Messages.getString("InstallerInitializer.wrongVersion")); //$NON-NLS-1$
					} catch (InstallationException e) {
						throw e;
					} catch(Exception e) {
						throw new InstallationException(Messages.getString("InstallerInitializer.getVersionError"), e); //$NON-NLS-1$
					}
					break;
				}
			}
			if(!foundVersion)
				throw new InstallationException(Messages.getString("InstallerInitializer.getVersionError")); //$NON-NLS-1$
		} catch (InstallationException e) {
			throw e;
		} catch(Exception e) {
			throw new InstallationException(Messages.getString("InstallerInitializer.noJava"), e); //$NON-NLS-1$
		}
	}

	@Override
	public void initialize() throws InstallationException
	{
		super.initialize();

		boolean nextEnabled = true;

		UIType uiType = InstallationManager.getInstallationManager().getUiType();
		if(uiType == UIType.swing) {
			// TODO: actually, this should be done on the ui thread...
			InstallerFrame installerFrame = SwingUI.getInstallerFrame();
			nextEnabled = installerFrame.getNextButton().isEnabled();
			installerFrame.getNextButton().setEnabled(false);
			installerFrame.setVisible(true);
			installerFrame.setHeaderText(Messages.getString("InstallerInitializer.headerText"), Messages.getString("InstallerInitializer.headerAnnotation")); //$NON-NLS-1$ //$NON-NLS-2$
		}
		// TODO: what about other ui types?

		try {

			checkEnvironment();

		} finally {
			if(uiType == UIType.swing) {
				// TODO: actually, this should be done on the ui thread...
				InstallerFrame installerFrame = SwingUI.getInstallerFrame();
				installerFrame.getNextButton().setEnabled(nextEnabled);
			}
		}
	}
}
