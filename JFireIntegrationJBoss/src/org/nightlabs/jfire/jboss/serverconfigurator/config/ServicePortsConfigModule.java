package org.nightlabs.jfire.jboss.serverconfigurator.config;

import org.nightlabs.config.ConfigModule;
import org.nightlabs.config.InitException;

/**
 * ConfigModule for configuring the binding address (ports) and hosts for the JBoss application server services.
 *
 * The default values are taken from the file ../docs/examples/binding-manager/sample-bindings.xml inside the JBoss 4.2.2 GA
 * Further details see http://www.jboss.org/community/docs/DOC-9376.
 *
 * @author Daniel Mazurek - Daniel.Mazurek [dot] nightlabs [dot] de
 *
 * FIXME: this class has several problems.
 * 1st: It must distinguish between bind addresses and connection addresses and provide a default for both separately.
 * bind address can be something like 0.0.0.0 which can never be a connection address.
 * 2nd: The default ${jboss.bin.address} does not work in all affected configuration files. Thus, I changed the default
 * to 127.0.0.1 (I would have used 0.0.0.0 but this does not work because of the problem above.
 * Marc
 */
public class ServicePortsConfigModule extends ConfigModule
{
	/**
	 * The serial version UID.
	 */
	private static final long serialVersionUID = 1L;

//	public static String getDefaultHostName() {
//		return "${jboss.bind.address}";
//	}

	private int serviceNamingBindingPort = 0;
	private int serviceNamingRMIPort = 0;
	private int serviceWebServicePort = 0;
	private int serviceTomcatPort = 0;
	private int serviceJBossMessagingPort = 0;
	private int serviceJrmpPort = 0;
	private int servicePooledPort = 0;
	private int serviceClusterHAJNDIBindingPort = 0;
	private int serviceClusterHAJNDIRMIPort = 0;
	private int serviceClusterJrmphaPort = 0;
	private int serviceClusterPooledhaPort = 0;
	private int serviceCorbaORBPort = 0;
	private int serviceJMXConnectorRMIPort = 0;
	private int serviceSnmpAgentTrapdPort = 0;
	private int serviceSnmpAgentSnmpPort = 0;
	private int serviceJMSPort = 0;
	private int serviceJMSHttpPort = 0;
	private int serviceJSMHajndiPort = 0;
	private int serviceEJB3InvokerHttpPort = 0;
	private int serviceEJB3RemoteConnectorPort = 0;
	private int serviceInvokerJMXHttpPort = 0;
	private int serviceInvokerJMXHttpReadOnlyPort = 0;
	private int serviceEJBInvokerHAPort = 0;
	private int serviceJMXInvokerHAPort = 0;
	private int serviceAxisServicePort = 0;
	private int serviceRemotingConnectorPort = 0;

	private String serviceNamingBindingHost;
	private String serviceNamingRMIHost;
	private String serviceWebServiceHost;
	private String serviceTomcatHost;
	private String serviceJBossMessagingHost;
	private String serviceJrmpHost;
	private String servicePooledHost;
	private String serviceClusterHAJNDIBindingHost;
	private String serviceClusterHAJNDIRMIHost;
	private String serviceClusterJrmphaHost;
	private String serviceClusterPooledhaHost;
	private String serviceCorbaORBHost;
	private String serviceJMXConnectorRMIHost;
	private String serviceSnmpAgentTrapdHost;
	private String serviceSnmpAgentSnmpHost;
	private String serviceJMSHost;
	private String serviceJMSHttpHost;
	private String serviceJSMHajndiHost;
	private String serviceEJB3InvokerHttpHost;
	private String serviceEJB3RemoteConnectorHost;
	private String serviceInvokerJMXHttpHost;
	private String serviceInvokerJMXHttpReadOnlyHost;
	private String serviceEJBInvokerHAHost;
	private String serviceJMXInvokerHAHost;
	private String serviceAxisServiceHost;
	private String serviceRemotingConnectorHost;

//	private String globalServiceHost;

	/**
	 * To be used when a specific host is set to <code>null</code>.
	 */
	private String defaultServiceHost;

	@Override
	public void init() throws InitException
	{
		if (serviceNamingBindingPort == 0)
			serviceNamingBindingPort = 1099;

		if (serviceNamingRMIPort == 0)
			serviceNamingRMIPort = 1098;

		if (serviceWebServicePort == 0)
			serviceWebServicePort = 8083;

		if (serviceTomcatPort == 0)
			serviceTomcatPort = 8080;

		if (serviceJBossMessagingPort == 0)
			serviceJBossMessagingPort = 4457;

		if (serviceJrmpPort == 0)
			serviceJrmpPort = 4444;

		if (servicePooledPort == 0)
			servicePooledPort = 4445;

		if (serviceClusterHAJNDIBindingPort == 0)
			serviceClusterHAJNDIBindingPort = 1100;

		if (serviceClusterHAJNDIRMIPort == 0)
			serviceClusterHAJNDIRMIPort = 1101;

		if (serviceClusterJrmphaPort == 0)
			serviceClusterJrmphaPort = 4444;

		if (serviceClusterPooledhaPort == 0)
			serviceClusterPooledhaPort = 4448;

		if (serviceCorbaORBPort == 0)
			serviceCorbaORBPort = 3528;

		if (serviceJMXConnectorRMIPort == 0)
			serviceJMXConnectorRMIPort = 19001;

		if (serviceSnmpAgentTrapdPort == 0)
			serviceSnmpAgentTrapdPort = 1162;

		if (serviceSnmpAgentSnmpPort == 0)
			serviceSnmpAgentSnmpPort = 1161;

		if (serviceJMSPort == 0)
			serviceJMSPort = 8093;

		if (serviceJMSHttpPort == 0)
			serviceJMSHttpPort = 8080;

		if (serviceJSMHajndiPort == 0)
			serviceJSMHajndiPort = 1100;

		if (serviceEJB3InvokerHttpPort == 0)
			serviceEJB3InvokerHttpPort = 8080;

		if (serviceEJB3RemoteConnectorPort == 0)
			serviceEJB3RemoteConnectorPort = 3873;

		if (serviceInvokerJMXHttpPort == 0)
			serviceInvokerJMXHttpPort = 8080;

		if (serviceInvokerJMXHttpReadOnlyPort == 0)
			serviceInvokerJMXHttpReadOnlyPort = 8080;

		if (serviceEJBInvokerHAPort == 0)
			serviceEJBInvokerHAPort = 8080;

		if (serviceJMXInvokerHAPort == 0)
			serviceJMXInvokerHAPort = 8080;

		if (serviceAxisServicePort == 0)
			serviceAxisServicePort = 8080;

		if (serviceRemotingConnectorPort == 0)
			serviceRemotingConnectorPort = 4446;

		if(defaultServiceHost == null)
			defaultServiceHost = "0.0.0.0";
		// see class-comment
		//	defaultServiceHost = "${jboss.bind.address}";

		setChanged();
	}

	private void setTomcatDependentPorts(int port) {
		serviceEJB3InvokerHttpPort = port;
		serviceInvokerJMXHttpPort = port;
		serviceInvokerJMXHttpReadOnlyPort = port;
		setChanged();
	}

	/**
	 * Returns the serviceNamingBindingPort.
	 * @return the serviceNamingBindingPort or <code>null</code> if it is not set. In this case,
	 * 		use the value given by {@link #getDefaultServiceHost()}.
	 * @see #getDefaultServiceHost()
	 */
	public int getServiceNamingBindingPort() {
		return serviceNamingBindingPort;
	}

	/**
	 * Sets the serviceNamingBindingPort.
	 * @param serviceNamingBindingPort the serviceNamingBindingPort to set
	 */
	public void setServiceNamingBindingPort(int serviceNamingBindingPort) {
		this.serviceNamingBindingPort = serviceNamingBindingPort;
		setChanged();
	}

	/**
	 * Returns the serviceNamingRMIPort.
	 * @return the serviceNamingRMIPort
	 * @see #getDefaultServiceHost()
	 */
	public int getServiceNamingRMIPort() {
		return serviceNamingRMIPort;
	}

	/**
	 * Sets the serviceNamingRMIPort.
	 * @param serviceNamingRMIPort the serviceNamingRMIPort to set
	 */
	public void setServiceNamingRMIPort(int serviceNamingRMIPort) {
		this.serviceNamingRMIPort = serviceNamingRMIPort;
		setChanged();
	}

	/**
	 * Returns the serviceWebServicePort.
	 * @return the serviceWebServicePort
	 * @see #getDefaultServiceHost()
	 */
	public int getServiceWebServicePort() {
		return serviceWebServicePort;
	}

	/**
	 * Sets the serviceWebServicePort.
	 * @param serviceWebServicePort the serviceWebServicePort to set
	 */
	public void setServiceWebServicePort(int serviceWebServicePort) {
		this.serviceWebServicePort = serviceWebServicePort;
		setChanged();
	}

	/**
	 * Returns the serviceTomcatPort.
	 * @return the serviceTomcatPort
	 * @see #getDefaultServiceHost()
	 */
	public int getServiceTomcatPort() {
		return serviceTomcatPort;
	}

	/**
	 * Sets the serviceTomcatPort.
	 * @param serviceTomcatPort the serviceTomcatPort to set
	 */
	public void setServiceTomcatPort(int serviceTomcatPort) {
		this.serviceTomcatPort = serviceTomcatPort;
		setTomcatDependentPorts(serviceTomcatPort);
		setChanged();
	}

	/**
	 * Returns the serviceJBossMessagingPort.
	 * @return the serviceJBossMessagingPort
	 * @see #getDefaultServiceHost()
	 */
	public int getServiceJBossMessagingPort() {
		return serviceJBossMessagingPort;
	}

	/**
	 * Sets the serviceJBossMessagingPort.
	 * @param serviceJBossMessagingPort the serviceJBossMessagingPort to set
	 */
	public void setServiceJBossMessagingPort(int serviceJBossMessagingPort) {
		this.serviceJBossMessagingPort = serviceJBossMessagingPort;
		setChanged();
	}

	/**
	 * Returns the serviceJrmpPort.
	 * @return the serviceJrmpPort
	 * @see #getDefaultServiceHost()
	 */
	public int getServiceJrmpPort() {
		return serviceJrmpPort;
	}

	/**
	 * Sets the serviceJrmpPort.
	 * @param serviceJrmpPort the serviceJrmpPort to set
	 */
	public void setServiceJrmpPort(int serviceJrmpPort) {
		this.serviceJrmpPort = serviceJrmpPort;
		setChanged();
	}

	/**
	 * Returns the servicePooledPort.
	 * @return the servicePooledPort
	 * @see #getDefaultServiceHost()
	 */
	public int getServicePooledPort() {
		return servicePooledPort;
	}

	/**
	 * Sets the servicePooledPort.
	 * @param servicePooledPort the servicePooledPort to set
	 */
	public void setServicePooledPort(int servicePooledPort) {
		this.servicePooledPort = servicePooledPort;
		setChanged();
	}

	/**
	 * Returns the serviceClusterHAJNDIBindingPort.
	 * @return the serviceClusterHAJNDIBindingPort
	 * @see #getDefaultServiceHost()
	 */
	public int getServiceClusterHAJNDIBindingPort() {
		return serviceClusterHAJNDIBindingPort;
	}

	/**
	 * Sets the serviceClusterHAJNDIBindingPort.
	 * @param serviceClusterHAJNDIBindingPort the serviceClusterHAJNDIBindingPort to set
	 */
	public void setServiceClusterHAJNDIBindingPort(int serviceClusterHAJNDIBindingPort) {
		this.serviceClusterHAJNDIBindingPort = serviceClusterHAJNDIBindingPort;
		setChanged();
	}

	/**
	 * Returns the serviceClusterHAJNDIRMIPort.
	 * @return the serviceClusterHAJNDIRMIPort
	 * @see #getDefaultServiceHost()
	 */
	public int getServiceClusterHAJNDIRMIPort() {
		return serviceClusterHAJNDIRMIPort;
	}

	/**
	 * Sets the serviceClusterHAJNDIRMIPort.
	 * @param serviceClusterHAJNDIRMIPort the serviceClusterHAJNDIRMIPort to set
	 */
	public void setServiceClusterHAJNDIRMIPort(int serviceClusterHAJNDIRMIPort) {
		this.serviceClusterHAJNDIRMIPort = serviceClusterHAJNDIRMIPort;
		setChanged();
	}

	/**
	 * Returns the serviceClusterJrmphaPort.
	 * @return the serviceClusterJrmphaPort
	 * @see #getDefaultServiceHost()
	 */
	public int getServiceClusterJrmphaPort() {
		return serviceClusterJrmphaPort;
	}

	/**
	 * Sets the serviceClusterJrmphaPort.
	 * @param serviceClusterJrmphaPort the serviceClusterJrmphaPort to set
	 */
	public void setServiceClusterJrmphaPort(int serviceClusterJrmphaPort) {
		this.serviceClusterJrmphaPort = serviceClusterJrmphaPort;
		setChanged();
	}

	/**
	 * Returns the serviceClusterPooledhaPort.
	 * @return the serviceClusterPooledhaPort
	 * @see #getDefaultServiceHost()
	 */
	public int getServiceClusterPooledhaPort() {
		return serviceClusterPooledhaPort;
	}

	/**
	 * Sets the serviceClusterPooledhaPort.
	 * @param serviceClusterPooledhaPort the serviceClusterPooledhaPort to set
	 */
	public void setServiceClusterPooledhaPort(int serviceClusterPooledhaPort) {
		this.serviceClusterPooledhaPort = serviceClusterPooledhaPort;
		setChanged();
	}

	/**
	 * Returns the serviceCorbaORBPort.
	 * @return the serviceCorbaORBPort
	 * @see #getDefaultServiceHost()
	 */
	public int getServiceCorbaORBPort() {
		return serviceCorbaORBPort;
	}

	/**
	 * Sets the serviceCorbaORBPort.
	 * @param serviceCorbaORBPort the serviceCorbaORBPort to set
	 */
	public void setServiceCorbaORBPort(int serviceCorbaORBPort) {
		this.serviceCorbaORBPort = serviceCorbaORBPort;
		setChanged();
	}

	/**
	 * Returns the serviceJMXConnectorRMIPort.
	 * @return the serviceJMXConnectorRMIPort
	 * @see #getDefaultServiceHost()
	 */
	public int getServiceJMXConnectorRMIPort() {
		return serviceJMXConnectorRMIPort;
	}

	/**
	 * Sets the serviceJMXConnectorRMIPort.
	 * @param serviceJMXConnectorRMIPort the serviceJMXConnectorRMIPort to set
	 */
	public void setServiceJMXConnectorRMIPort(int serviceJMXConnectorRMIPort) {
		this.serviceJMXConnectorRMIPort = serviceJMXConnectorRMIPort;
		setChanged();
	}

	/**
	 * Returns the serviceSnmpAgentTrapdPort.
	 * @return the serviceSnmpAgentTrapdPort
	 * @see #getDefaultServiceHost()
	 */
	public int getServiceSnmpAgentTrapdPort() {
		return serviceSnmpAgentTrapdPort;
	}

	/**
	 * Sets the serviceSnmpAgentTrapdPort.
	 * @param serviceSnmpAgentTrapdPort the serviceSnmpAgentTrapdPort to set
	 */
	public void setServiceSnmpAgentTrapdPort(int serviceSnmpAgentTrapdPort) {
		this.serviceSnmpAgentTrapdPort = serviceSnmpAgentTrapdPort;
		setChanged();
	}

	/**
	 * Returns the serviceSnmpAgentSnmpPort.
	 * @return the serviceSnmpAgentSnmpPort
	 * @see #getDefaultServiceHost()
	 */
	public int getServiceSnmpAgentSnmpPort() {
		return serviceSnmpAgentSnmpPort;
	}

	/**
	 * Sets the serviceSnmpAgentSnmpPort.
	 * @param serviceSnmpAgentSnmpPort the serviceSnmpAgentSnmpPort to set
	 */
	public void setServiceSnmpAgentSnmpPort(int serviceSnmpAgentSnmpPort) {
		this.serviceSnmpAgentSnmpPort = serviceSnmpAgentSnmpPort;
		setChanged();
	}

	/**
	 * Returns the serviceJMSPort.
	 * @return the serviceJMSPort
	 * @see #getDefaultServiceHost()
	 */
	public int getServiceJMSPort() {
		return serviceJMSPort;
	}

	/**
	 * Sets the serviceJMSPort.
	 * @param serviceJMSPort the serviceJMSPort to set
	 */
	public void setServiceJMSPort(int serviceJMSPort) {
		this.serviceJMSPort = serviceJMSPort;
		setChanged();
	}

	/**
	 * Returns the serviceJMSHttpPort.
	 * @return the serviceJMSHttpPort
	 * @see #getDefaultServiceHost()
	 */
	public int getServiceJMSHttpPort() {
		return serviceJMSHttpPort;
	}

	/**
	 * Sets the serviceJMSHttpPort.
	 * @param serviceJMSHttpPort the serviceJMSHttpPort to set
	 */
	public void setServiceJMSHttpPort(int serviceJMSHttpPort) {
		this.serviceJMSHttpPort = serviceJMSHttpPort;
		setChanged();
	}

	/**
	 * Returns the serviceJSMHajndiPort.
	 * @return the serviceJSMHajndiPort
	 * @see #getDefaultServiceHost()
	 */
	public int getServiceJSMHajndiPort() {
		return serviceJSMHajndiPort;
	}

	/**
	 * Sets the serviceJSMHajndiPort.
	 * @param serviceJSMHajndiPort the serviceJSMHajndiPort to set
	 */
	public void setServiceJSMHajndiPort(int serviceJSMHajndiPort) {
		this.serviceJSMHajndiPort = serviceJSMHajndiPort;
		setChanged();
	}

	/**
	 * Returns the serviceEJB3InvokerHttpPort.
	 * @return the serviceEJB3InvokerHttpPort
	 * @see #getDefaultServiceHost()
	 */
	public int getServiceEJB3InvokerHttpPort() {
		return serviceEJB3InvokerHttpPort;
	}

	/**
	 * Sets the serviceEJB3InvokerHttpPort.
	 * @param serviceEJB3InvokerHttpPort the serviceEJB3InvokerHttpPort to set
	 */
	public void setServiceEJB3InvokerHttpPort(int serviceEJB3InvokerHttpPort)
	{
		if (serviceEJB3InvokerHttpPort == serviceTomcatPort) {
			this.serviceEJB3InvokerHttpPort = serviceEJB3InvokerHttpPort;
			setChanged();
		}
	}

	/**
	 * Returns the serviceEJB3RemoteConnectorPort.
	 * @return the serviceEJB3RemoteConnectorPort
	 * @see #getDefaultServiceHost()
	 */
	public int getServiceEJB3RemoteConnectorPort() {
		return serviceEJB3RemoteConnectorPort;
	}

	/**
	 * Sets the serviceEJB3RemoteConnectorPort.
	 * @param serviceEJB3RemoteConnectorPort the serviceEJB3RemoteConnectorPort to set
	 */
	public void setServiceEJB3RemoteConnectorPort(int serviceEJB3RemoteConnectorPort) {
		this.serviceEJB3RemoteConnectorPort = serviceEJB3RemoteConnectorPort;
		setChanged();
	}

	/**
	 * Returns the serviceInvokerJMXHttpPort.
	 * @return the serviceInvokerJMXHttpPort
	 * @see #getDefaultServiceHost()
	 */
	public int getServiceInvokerJMXHttpPort() {
		return serviceInvokerJMXHttpPort;
	}

	/**
	 * Sets the serviceInvokerJMXHttpPort.
	 * @param serviceInvokerJMXHttpPort the serviceInvokerJMXHttpPort to set
	 */
	public void setServiceInvokerJMXHttpPort(int serviceInvokerJMXHttpPort) {
		if (serviceInvokerJMXHttpPort == serviceTomcatPort) {
			this.serviceInvokerJMXHttpPort = serviceInvokerJMXHttpPort;
			setChanged();
		}
	}

	/**
	 * Returns the serviceInvokerJMXHttpReadOnlyPort.
	 * @return the serviceInvokerJMXHttpReadOnlyPort
	 * @see #getDefaultServiceHost()
	 */
	public int getServiceInvokerJMXHttpReadOnlyPort() {
		return serviceInvokerJMXHttpReadOnlyPort;
	}

	/**
	 * Sets the serviceInvokerJMXHttpReadOnlyPort.
	 * @param serviceInvokerJMXHttpReadOnlyPort the serviceInvokerJMXHttpReadOnlyPort to set
	 */
	public void setServiceInvokerJMXHttpReadOnlyPort(int serviceInvokerJMXHttpReadOnlyPort) {
		if (serviceInvokerJMXHttpReadOnlyPort == serviceTomcatPort) {
			this.serviceInvokerJMXHttpReadOnlyPort = serviceInvokerJMXHttpReadOnlyPort;
			setChanged();
		}
	}

	/**
	 * Returns the serviceEJBInvokerHAPort.
	 * @return the serviceEJBInvokerHAPort
	 * @see #getDefaultServiceHost()
	 */
	public int getServiceEJBInvokerHAPort() {
		return serviceEJBInvokerHAPort;
	}

	/**
	 * Sets the serviceEJBInvokerHAPort.
	 * @param serviceEJBInvokerHAPort the serviceEJBInvokerHAPort to set
	 */
	public void setServiceEJBInvokerHAPort(int serviceEJBInvokerHAPort) {
		this.serviceEJBInvokerHAPort = serviceEJBInvokerHAPort;
		setChanged();
	}

	/**
	 * Returns the serviceJMXInvokerHAPort.
	 * @return the serviceJMXInvokerHAPort
	 * @see #getDefaultServiceHost()
	 */
	public int getServiceJMXInvokerHAPort() {
		return serviceJMXInvokerHAPort;
	}

	/**
	 * Sets the serviceJMXInvokerHAPort.
	 * @param serviceJMXInvokerHAPort the serviceJMXInvokerHAPort to set
	 */
	public void setServiceJMXInvokerHAPort(int serviceJMXInvokerHAPort) {
		this.serviceJMXInvokerHAPort = serviceJMXInvokerHAPort;
		setChanged();
	}

	/**
	 * Returns the serviceAxisServicePort.
	 * @return the serviceAxisServicePort
	 * @see #getDefaultServiceHost()
	 */
	public int getServiceAxisServicePort() {
		return serviceAxisServicePort;
	}

	/**
	 * Sets the serviceAxisServicePort.
	 * @param serviceAxisServicePort the serviceAxisServicePort to set
	 */
	public void setServiceAxisServicePort(int serviceAxisServicePort) {
		this.serviceAxisServicePort = serviceAxisServicePort;
		setChanged();
	}

	/**
	 * Returns the serviceRemotingConnectorPort.
	 * @return the serviceRemotingConnectorPort
	 * @see #getDefaultServiceHost()
	 */
	public int getServiceRemotingConnectorPort() {
		return serviceRemotingConnectorPort;
	}

	/**
	 * Sets the serviceRemotingConnectorPort.
	 * @param serviceRemotingConnectorPort the serviceRemotingConnectorPort to set
	 */
	public void setServiceRemotingConnectorPort(int serviceRemotingConnectorPort) {
		this.serviceRemotingConnectorPort = serviceRemotingConnectorPort;
		setChanged();
	}

	/**
	 * Returns the serviceNamingBindingHost.
	 * @return the serviceNamingBindingHost or <code>null</code> if it is not set. In this case,
	 * 		use the value given by {@link #getDefaultServiceHost()}.
	 * @see #getDefaultServiceHost()
	 */
	public String getServiceNamingBindingHost() {
		return serviceNamingBindingHost;
	}

	/**
	 * Sets the serviceNamingBindingHost.
	 * @param serviceNamingBindingHost the serviceNamingBindingHost to set
	 */
	public void setServiceNamingBindingHost(String serviceNamingBindingHost) {
		this.serviceNamingBindingHost = serviceNamingBindingHost;
		setChanged();
	}

	/**
	 * Returns the serviceWebServiceHost.
	 * @return the serviceWebServiceHost or <code>null</code> if it is not set. In this case,
	 * 		use the value given by {@link #getDefaultServiceHost()}.
	 * @see #getDefaultServiceHost()
	 */
	public String getServiceWebServiceHost() {
		return serviceWebServiceHost;
	}

	/**
	 * Sets the serviceWebServiceHost.
	 * @param serviceWebServiceHost the serviceWebServiceHost to set
	 */
	public void setServiceWebServiceHost(String serviceWebServiceHost) {
		this.serviceWebServiceHost = serviceWebServiceHost;
		setChanged();
	}

	/**
	 * Returns the serviceTomcatHost.
	 * @return the serviceTomcatHost or <code>null</code> if it is not set. In this case,
	 * 		use the value given by {@link #getDefaultServiceHost()}.
	 * @see #getDefaultServiceHost()
	 */
	public String getServiceTomcatHost() {
		return serviceTomcatHost;
	}

	/**
	 * Sets the serviceTomcatHost.
	 * @param serviceTomcatHost the serviceTomcatHost to set
	 */
	public void setServiceTomcatHost(String serviceTomcatHost) {
		this.serviceTomcatHost = serviceTomcatHost;
		setChanged();
	}

	/**
	 * Returns the serviceJBossMessagingHost.
	 * @return the serviceJBossMessagingHost or <code>null</code> if it is not set. In this case,
	 * 		use the value given by {@link #getDefaultServiceHost()}.
	 * @see #getDefaultServiceHost()
	 */
	public String getServiceJBossMessagingHost() {
		return serviceJBossMessagingHost;
	}

	/**
	 * Sets the serviceJBossMessagingHost.
	 * @param serviceJBossMessagingHost the serviceJBossMessagingHost to set
	 */
	public void setServiceJBossMessagingHost(String serviceJBossMessagingHost) {
		this.serviceJBossMessagingHost = serviceJBossMessagingHost;
		setChanged();
	}

	/**
	 * Returns the serviceJrmpHost.
	 * @return the serviceJrmpHost or <code>null</code> if it is not set. In this case,
	 * 		use the value given by {@link #getDefaultServiceHost()}.
	 * @see #getDefaultServiceHost()
	 */
	public String getServiceJrmpHost() {
		return serviceJrmpHost;
	}

	/**
	 * Sets the serviceJrmpHost.
	 * @param serviceJrmpHost the serviceJrmpHost to set
	 */
	public void setServiceJrmpHost(String serviceJrmpHost) {
		this.serviceJrmpHost = serviceJrmpHost;
		setChanged();
	}

	/**
	 * Returns the servicePooledHost.
	 * @return the servicePooledHost or <code>null</code> if it is not set. In this case,
	 * 		use the value given by {@link #getDefaultServiceHost()}.
	 * @see #getDefaultServiceHost()
	 */
	public String getServicePooledHost() {
		return servicePooledHost;
	}

	/**
	 * Sets the servicePooledHost.
	 * @param servicePooledHost the servicePooledHost to set
	 */
	public void setServicePooledHost(String servicePooledHost) {
		this.servicePooledHost = servicePooledHost;
		setChanged();
	}

	/**
	 * Returns the serviceClusterHAJNDIBindingHost.
	 * @return the serviceClusterHAJNDIBindingHost or <code>null</code> if it is not set. In this case,
	 * 		use the value given by {@link #getDefaultServiceHost()}.
	 * @see #getDefaultServiceHost()
	 */
	public String getServiceClusterHAJNDIBindingHost() {
		return serviceClusterHAJNDIBindingHost;
	}

	/**
	 * Sets the serviceClusterHAJNDIBindingHost.
	 * @param serviceClusterHAJNDIBindingHost the serviceClusterHAJNDIBindingHost to set
	 */
	public void setServiceClusterHAJNDIBindingHost(String serviceClusterHAJNDIBindingHost) {
		this.serviceClusterHAJNDIBindingHost = serviceClusterHAJNDIBindingHost;
		setChanged();
	}

	/**
	 * Returns the serviceClusterHAJNDIRMIHost.
	 * @return the serviceClusterHAJNDIRMIHost or <code>null</code> if it is not set. In this case,
	 * 		use the value given by {@link #getDefaultServiceHost()}.
	 * @see #getDefaultServiceHost()
	 */
	public String getServiceClusterHAJNDIRMIHost() {
		return serviceClusterHAJNDIRMIHost;
	}

	/**
	 * Sets the serviceClusterHAJNDIRMIHost.
	 * @param serviceClusterHAJNDIRMIHost the serviceClusterHAJNDIRMIHost to set
	 */
	public void setServiceClusterHAJNDIRMIHost(String serviceClusterHAJNDIRMIHost) {
		this.serviceClusterHAJNDIRMIHost = serviceClusterHAJNDIRMIHost;
		setChanged();
	}

	/**
	 * Returns the serviceClusterJrmphaHost.
	 * @return the serviceClusterJrmphaHost or <code>null</code> if it is not set. In this case,
	 * 		use the value given by {@link #getDefaultServiceHost()}.
	 * @see #getDefaultServiceHost()
	 */
	public String getServiceClusterJrmphaHost() {
		return serviceClusterJrmphaHost;
	}

	/**
	 * Sets the serviceClusterJrmphaHost.
	 * @param serviceClusterJrmphaHost the serviceClusterJrmphaHost to set
	 */
	public void setServiceClusterJrmphaHost(String serviceClusterJrmphaHost) {
		this.serviceClusterJrmphaHost = serviceClusterJrmphaHost;
		setChanged();
	}

	/**
	 * Returns the serviceClusterPooledhaHost.
	 * @return the serviceClusterPooledhaHost or <code>null</code> if it is not set. In this case,
	 * 		use the value given by {@link #getDefaultServiceHost()}.
	 * @see #getDefaultServiceHost()
	 */
	public String getServiceClusterPooledhaHost() {
		return serviceClusterPooledhaHost;
	}

	/**
	 * Sets the serviceClusterPooledhaHost.
	 * @param serviceClusterPooledhaHost the serviceClusterPooledhaHost to set
	 */
	public void setServiceClusterPooledhaHost(String serviceClusterPooledhaHost) {
		this.serviceClusterPooledhaHost = serviceClusterPooledhaHost;
		setChanged();
	}

	/**
	 * Returns the serviceCorbaORBHost.
	 * @return the serviceCorbaORBHost or <code>null</code> if it is not set. In this case,
	 * 		use the value given by {@link #getDefaultServiceHost()}.
	 * @see #getDefaultServiceHost()
	 */
	public String getServiceCorbaORBHost() {
		return serviceCorbaORBHost;
	}

	/**
	 * Sets the serviceCorbaORBHost.
	 * @param serviceCorbaORBHost the serviceCorbaORBHost to set
	 */
	public void setServiceCorbaORBHost(String serviceCorbaORBHost) {
		this.serviceCorbaORBHost = serviceCorbaORBHost;
		setChanged();
	}

	/**
	 * Returns the serviceJMXConnectorRMIHost.
	 * @return the serviceJMXConnectorRMIHost or <code>null</code> if it is not set. In this case,
	 * 		use the value given by {@link #getDefaultServiceHost()}.
	 * @see #getDefaultServiceHost()
	 */
	public String getServiceJMXConnectorRMIHost() {
		return serviceJMXConnectorRMIHost;
	}

	/**
	 * Sets the serviceJMXConnectorRMIHost.
	 * @param serviceJMXConnectorRMIHost the serviceJMXConnectorRMIHost to set
	 */
	public void setServiceJMXConnectorRMIHost(String serviceJMXConnectorRMIHost) {
		this.serviceJMXConnectorRMIHost = serviceJMXConnectorRMIHost;
		setChanged();
	}

	/**
	 * Returns the serviceSnmpAgentTrapdHost.
	 * @return the serviceSnmpAgentTrapdHost or <code>null</code> if it is not set. In this case,
	 * 		use the value given by {@link #getDefaultServiceHost()}.
	 * @see #getDefaultServiceHost()
	 */
	public String getServiceSnmpAgentTrapdHost() {
		return serviceSnmpAgentTrapdHost;
	}

	/**
	 * Sets the serviceSnmpAgentTrapdHost.
	 * @param serviceSnmpAgentTrapdHost the serviceSnmpAgentTrapdHost to set
	 */
	public void setServiceSnmpAgentTrapdHost(String serviceSnmpAgentTrapdHost) {
		this.serviceSnmpAgentTrapdHost = serviceSnmpAgentTrapdHost;
		setChanged();
	}

	/**
	 * Returns the serviceSnmpAgentSnmpHost.
	 * @return the serviceSnmpAgentSnmpHost or <code>null</code> if it is not set. In this case,
	 * 		use the value given by {@link #getDefaultServiceHost()}.
	 * @see #getDefaultServiceHost()
	 */
	public String getServiceSnmpAgentSnmpHost() {
		return serviceSnmpAgentSnmpHost;
	}

	/**
	 * Sets the serviceSnmpAgentSnmpHost.
	 * @param serviceSnmpAgentSnmpHost the serviceSnmpAgentSnmpHost to set
	 */
	public void setServiceSnmpAgentSnmpHost(String serviceSnmpAgentSnmpHost) {
		this.serviceSnmpAgentSnmpHost = serviceSnmpAgentSnmpHost;
		setChanged();
	}

	/**
	 * Returns the serviceJMSHost.
	 * @return the serviceJMSHost or <code>null</code> if it is not set. In this case,
	 * 		use the value given by {@link #getDefaultServiceHost()}.
	 * @see #getDefaultServiceHost()
	 */
	public String getServiceJMSHost() {
		return serviceJMSHost;
	}

	/**
	 * Sets the serviceJMSHost.
	 * @param serviceJMSHost the serviceJMSHost to set
	 */
	public void setServiceJMSHost(String serviceJMSHost) {
		this.serviceJMSHost = serviceJMSHost;
		setChanged();
	}

	/**
	 * Returns the serviceJMSHttpHost.
	 * @return the serviceJMSHttpHost or <code>null</code> if it is not set. In this case,
	 * 		use the value given by {@link #getDefaultServiceHost()}.
	 * @see #getDefaultServiceHost()
	 */
	public String getServiceJMSHttpHost() {
		return serviceJMSHttpHost;
	}

	/**
	 * Sets the serviceJMSHttpHost.
	 * @param serviceJMSHttpHost the serviceJMSHttpHost to set
	 */
	public void setServiceJMSHttpHost(String serviceJMSHttpHost) {
		this.serviceJMSHttpHost = serviceJMSHttpHost;
		setChanged();
	}

	/**
	 * Returns the serviceJSMHajndiHost.
	 * @return the serviceJSMHajndiHost or <code>null</code> if it is not set. In this case,
	 * 		use the value given by {@link #getDefaultServiceHost()}.
	 * @see #getDefaultServiceHost()
	 */
	public String getServiceJSMHajndiHost() {
		return serviceJSMHajndiHost;
	}

	/**
	 * Sets the serviceJSMHajndiHost.
	 * @param serviceJSMHajndiHost the serviceJSMHajndiHost to set
	 */
	public void setServiceJSMHajndiHost(String serviceJSMHajndiHost) {
		this.serviceJSMHajndiHost = serviceJSMHajndiHost;
		setChanged();
	}

	/**
	 * Returns the serviceEJB3InvokerHttpHost.
	 * @return the serviceEJB3InvokerHttpHost or <code>null</code> if it is not set. In this case,
	 * 		use the value given by {@link #getDefaultServiceHost()}.
	 * @see #getDefaultServiceHost()
	 */
	public String getServiceEJB3InvokerHttpHost() {
		return serviceEJB3InvokerHttpHost;
	}

	/**
	 * Sets the serviceEJB3InvokerHttpHost.
	 * @param serviceEJB3InvokerHttpHost the serviceEJB3InvokerHttpHost to set
	 */
	public void setServiceEJB3InvokerHttpHost(String serviceEJB3InvokerHttpHost) {
		this.serviceEJB3InvokerHttpHost = serviceEJB3InvokerHttpHost;
		setChanged();
	}

	/**
	 * Returns the serviceEJB3RemoteConnectorHost.
	 * @return the serviceEJB3RemoteConnectorHost or <code>null</code> if it is not set. In this case,
	 * 		use the value given by {@link #getDefaultServiceHost()}.
	 * @see #getDefaultServiceHost()
	 */
	public String getServiceEJB3RemoteConnectorHost() {
		return serviceEJB3RemoteConnectorHost;
	}

	/**
	 * Sets the serviceEJB3RemoteConnectorHost.
	 * @param serviceEJB3RemoteConnectorHost the serviceEJB3RemoteConnectorHost to set
	 */
	public void setServiceEJB3RemoteConnectorHost(String serviceEJB3RemoteConnectorHost) {
		this.serviceEJB3RemoteConnectorHost = serviceEJB3RemoteConnectorHost;
		setChanged();
	}

	/**
	 * Returns the serviceInvokerJMXHttpHost.
	 * @return the serviceInvokerJMXHttpHost or <code>null</code> if it is not set. In this case,
	 * 		use the value given by {@link #getDefaultServiceHost()}.
	 * @see #getDefaultServiceHost()
	 */
	public String getServiceInvokerJMXHttpHost() {
		return serviceInvokerJMXHttpHost;
	}

	/**
	 * Sets the serviceInvokerJMXHttpHost.
	 * @param serviceInvokerJMXHttpHost the serviceInvokerJMXHttpHost to set
	 */
	public void setServiceInvokerJMXHttpHost(String serviceInvokerJMXHttpHost) {
		this.serviceInvokerJMXHttpHost = serviceInvokerJMXHttpHost;
		setChanged();
	}

	/**
	 * Returns the serviceInvokerJMXHttpReadOnlyHost.
	 * @return the serviceInvokerJMXHttpReadOnlyHost or <code>null</code> if it is not set. In this case,
	 * 		use the value given by {@link #getDefaultServiceHost()}.
	 * @see #getDefaultServiceHost()
	 */
	public String getServiceInvokerJMXHttpReadOnlyHost() {
		return serviceInvokerJMXHttpReadOnlyHost;
	}

	/**
	 * Sets the serviceInvokerJMXHttpReadOnlyHost.
	 * @param serviceInvokerJMXHttpReadOnlyHost the serviceInvokerJMXHttpReadOnlyHost to set
	 */
	public void setServiceInvokerJMXHttpReadOnlyHost(String serviceInvokerJMXHttpReadOnlyHost) {
		this.serviceInvokerJMXHttpReadOnlyHost = serviceInvokerJMXHttpReadOnlyHost;
		setChanged();
	}

	/**
	 * Returns the serviceEJBInvokerHAHost.
	 * @return the serviceEJBInvokerHAHost or <code>null</code> if it is not set. In this case,
	 * 		use the value given by {@link #getDefaultServiceHost()}.
	 * @see #getDefaultServiceHost()
	 */
	public String getServiceEJBInvokerHAHost() {
		return serviceEJBInvokerHAHost;
	}

	/**
	 * Sets the serviceEJBInvokerHAHost.
	 * @param serviceEJBInvokerHAHost the serviceEJBInvokerHAHost to set
	 */
	public void setServiceEJBInvokerHAHost(String serviceEJBInvokerHAHost) {
		this.serviceEJBInvokerHAHost = serviceEJBInvokerHAHost;
		setChanged();
	}

	/**
	 * Returns the serviceJMXInvokerHAHost.
	 * @return the serviceJMXInvokerHAHost or <code>null</code> if it is not set. In this case,
	 * 		use the value given by {@link #getDefaultServiceHost()}.
	 * @see #getDefaultServiceHost()
	 */
	public String getServiceJMXInvokerHAHost() {
		return serviceJMXInvokerHAHost;
	}

	/**
	 * Sets the serviceJMXInvokerHAHost.
	 * @param serviceJMXInvokerHAHost the serviceJMXInvokerHAHost to set
	 */
	public void setServiceJMXInvokerHAHost(String serviceJMXInvokerHAHost) {
		this.serviceJMXInvokerHAHost = serviceJMXInvokerHAHost;
		setChanged();
	}

	/**
	 * Returns the serviceAxisServiceHost.
	 * @return the serviceAxisServiceHost or <code>null</code> if it is not set. In this case,
	 * 		use the value given by {@link #getDefaultServiceHost()}.
	 * @see #getDefaultServiceHost()
	 */
	public String getServiceAxisServiceHost() {
		return serviceAxisServiceHost;
	}

	/**
	 * Sets the serviceAxisServiceHost.
	 * @param serviceAxisServiceHost the serviceAxisServiceHost to set
	 */
	public void setServiceAxisServiceHost(String serviceAxisServiceHost) {
		this.serviceAxisServiceHost = serviceAxisServiceHost;
		setChanged();
	}

	/**
	 * Returns the serviceRemotingConnectorHost.
	 * @return the serviceRemotingConnectorHost or <code>null</code> if it is not set. In this case,
	 * 		use the value given by {@link #getDefaultServiceHost()}.
	 * @see #getDefaultServiceHost()
	 */
	public String getServiceRemotingConnectorHost() {
		return serviceRemotingConnectorHost;
	}

	/**
	 * Sets the serviceRemotingConnectorHost.
	 * @param serviceRemotingConnectorHost the serviceRemotingConnectorHost to set
	 */
	public void setServiceRemotingConnectorHost(String serviceRemotingConnectorHost) {
		this.serviceRemotingConnectorHost = serviceRemotingConnectorHost;
		setChanged();
	}

	/**
	 * Get the serviceNamingRMIHost.
	 * @return the serviceNamingRMIHost or <code>null</code> if it is not set. In this case,
	 * 		use the value given by {@link #getDefaultServiceHost()}.
	 * @see #getDefaultServiceHost()
	 */
	public String getServiceNamingRMIHost()
	{
		return serviceNamingRMIHost;
	}

	public void setServiceNamingRMIHost(String serviceNamingRMIHost)
	{
		this.serviceNamingRMIHost = serviceNamingRMIHost;
		setChanged();
	}

	/**
	 * Set the default service host. This value is to be used when a specific
	 * host setting is <code>null</code>.
	 * @param defaultServiceHost The host to set
	 */
	public void setDefaultServiceHost(String defaultServiceHost)
	{
		if(defaultServiceHost == null)
			throw new NullPointerException("defaultServiceHost");
		this.defaultServiceHost = defaultServiceHost;
	}

	/**
	 * Get the default service host. This value is to be used when a specific
	 * host setting is <code>null</code>.
	 * @return The default service host
	 */
	public String getDefaultServiceHost()
	{
		return defaultServiceHost;
	}
}
