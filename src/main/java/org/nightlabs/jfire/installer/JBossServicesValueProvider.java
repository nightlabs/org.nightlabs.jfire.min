package org.nightlabs.jfire.installer;

import java.util.Properties;

import org.nightlabs.installer.base.InstallationException;
import org.nightlabs.installer.base.defaults.DefaultValueProvider;
import org.nightlabs.jfire.jboss.serverconfigurator.config.ServicePortsConfigModule;

/**
 * @author Daniel Mazurek - Daniel.Mazurek [dot] nightlabs [dot] de
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public class JBossServicesValueProvider extends DefaultValueProvider {

	@Override
	public Properties getValues() throws InstallationException
	{
		ServicePortsConfigModule servicesCf = new ServicePortsConfigModule();
		servicesCf.init();
		Properties defaultValues = new Properties();

		defaultValues.setProperty("15_naming-port.result", String.valueOf(servicesCf.getServiceNamingBindingPort())); //$NON-NLS-1$
		defaultValues.setProperty("17_naming-host.result", nonNull(servicesCf.getServiceNamingBindingHost())); //$NON-NLS-1$

		defaultValues.setProperty("20_rmi-port.result", String.valueOf(servicesCf.getServiceNamingRMIPort())); //$NON-NLS-1$
		defaultValues.setProperty("21_rmi-host.result", nonNull(servicesCf.getServiceNamingRMIHost())); //$NON-NLS-1$

		defaultValues.setProperty("30_webservice-port.result", String.valueOf(servicesCf.getServiceWebServicePort())); //$NON-NLS-1$
		defaultValues.setProperty("35_webservice-host.result", nonNull(servicesCf.getServiceWebServiceHost())); //$NON-NLS-1$

		defaultValues.setProperty("40_tomcat-port.result", String.valueOf(servicesCf.getServiceTomcatPort())); //$NON-NLS-1$
		defaultValues.setProperty("45_tomcat-host.result", nonNull(servicesCf.getServiceTomcatHost())); //$NON-NLS-1$

		defaultValues.setProperty("50_jbossmessaging-port.result", String.valueOf(servicesCf.getServiceJBossMessagingPort())); //$NON-NLS-1$
		defaultValues.setProperty("55_jbossmessaging-host.result", nonNull(servicesCf.getServiceJBossMessagingHost())); //$NON-NLS-1$

		defaultValues.setProperty("60_jrmp-port.result", String.valueOf(servicesCf.getServiceJrmpPort())); //$NON-NLS-1$
		defaultValues.setProperty("65_jrmp-host.result", nonNull(servicesCf.getServiceJrmpHost())); //$NON-NLS-1$

		defaultValues.setProperty("70_pooled-port.result", String.valueOf(servicesCf.getServicePooledPort())); //$NON-NLS-1$
		defaultValues.setProperty("75_pooled-host.result", nonNull(servicesCf.getServicePooledHost())); //$NON-NLS-1$

		return defaultValues;
	}

	private static String nonNull(String s)
	{
		return s == null ? "" : s;
	}
}
