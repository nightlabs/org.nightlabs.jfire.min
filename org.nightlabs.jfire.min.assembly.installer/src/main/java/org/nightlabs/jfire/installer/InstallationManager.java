package org.nightlabs.jfire.installer;

/**
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public class InstallationManager extends
		org.nightlabs.installer.InstallationManager
{
	protected InstallationManager(String[] args)
	{
		super(args);
		setConfigInputStream(InstallationManager.class.getResourceAsStream("installation-config.properties")); //$NON-NLS-1$
	}

	public static void main(String[] args)
	{
		new InstallationManager(args).run();
	}
}
