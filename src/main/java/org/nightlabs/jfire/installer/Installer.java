package org.nightlabs.jfire.installer;

import java.awt.Rectangle;
import java.net.URL;

import org.nightlabs.installer.base.defaults.DefaultInstaller;

/**
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public class Installer extends DefaultInstaller
{
	/* (non-Javadoc)
	 * @see org.nightlabs.installer.base.defaults.DefaultInstaller#getWizardIconURL()
	 */
	@Override
	protected URL getWizardIconURL()
	{
		return getClass().getResource("installer-wizard-icon.png");
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.installer.base.defaults.DefaultInstaller#getInstallerFrameBounds()
	 */
	@Override
	protected Rectangle getInstallerFrameBounds()
	{
		int x = Integer.parseInt(Messages.getString("Installer.SwingUIFrameBounds.x"));
		int y = Integer.parseInt(Messages.getString("Installer.SwingUIFrameBounds.y"));
		int width = Integer.parseInt(Messages.getString("Installer.SwingUIFrameBounds.width"));
		int height = Integer.parseInt(Messages.getString("Installer.SwingUIFrameBounds.height"));
		return new Rectangle(x, y, width, height);
	}
}
