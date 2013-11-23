package org.nightlabs.jfire.installer;

import java.util.Properties;

import org.nightlabs.installer.base.InstallationException;
import org.nightlabs.installer.base.defaults.DefaultValueProvider;

/**
 * Take the installation presets into account.
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public class ServerConfiguratorValueProvider extends DefaultValueProvider
{
	@Override
	public Properties getValues() throws InstallationException
	{
		String installationPresets = getInstaller().getResult("20_localServer.70_installationPresets.result"); //$NON-NLS-1$
		Properties defaultValues = new Properties();
		if("JBossMySQL".equals(installationPresets)) {
			defaultValues.setProperty("result", "org.nightlabs.jfire.jboss.serverconfigurator.ServerConfiguratorJBossMySQL"); //$NON-NLS-1$
		}
		else if("JBossDerby".equals(installationPresets)) {
			defaultValues.setProperty("result", "org.nightlabs.jfire.jboss.serverconfigurator.ServerConfiguratorJBossDerby"); //$NON-NLS-1$
		}
		else {
			// this should not happen...
			defaultValues.setProperty("result", "org.nightlabs.jfire.jboss.serverconfigurator.ServerConfiguratorJBoss"); //$NON-NLS-1$
		}
		return defaultValues;
	}
}
