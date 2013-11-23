package org.nightlabs.jfire.installer;

import java.util.Properties;

import org.nightlabs.installer.base.InstallationException;
import org.nightlabs.installer.base.defaults.DefaultValueProvider;
import org.nightlabs.jfire.jboss.serverconfigurator.config.ServicePortsConfigModule;

/**
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public class JBossDefaultServiceHostValueProvider extends DefaultValueProvider
{
	@Override
	public Properties getValues() throws InstallationException
	{
		ServicePortsConfigModule servicesCf = new ServicePortsConfigModule();
		servicesCf.init();
		Properties defaultValues = new Properties();

		defaultValues.setProperty("result", servicesCf.getDefaultServiceHost()); //$NON-NLS-1$

		return defaultValues;
	}
}
