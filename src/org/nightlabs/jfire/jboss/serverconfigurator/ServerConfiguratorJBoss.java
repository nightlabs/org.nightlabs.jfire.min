package org.nightlabs.jfire.jboss.serverconfigurator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.URL;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.transform.TransformerException;

import org.apache.log4j.Logger;
import org.apache.xpath.CachedXPathAPI;
import org.nightlabs.config.ConfigModuleNotFoundException;
import org.nightlabs.jfire.jboss.authentication.JFireServerLocalLoginModule;
import org.nightlabs.jfire.jboss.authentication.JFireServerLoginModule;
import org.nightlabs.jfire.jboss.serverconfigurator.config.ServicePortsConfigModule;
import org.nightlabs.jfire.jboss.serverconfigurator.config.ServiceSettingsConfigModule;
import org.nightlabs.jfire.serverconfigurator.ServerConfigurationException;
import org.nightlabs.jfire.serverconfigurator.ServerConfigurator;
import org.nightlabs.jfire.servermanager.config.JFireServerConfigModule;
import org.nightlabs.jfire.servermanager.config.ServletSSLCf;
import org.nightlabs.jfire.servermanager.config.SmtpMailServiceCf;
import org.nightlabs.util.IOUtil;
import org.nightlabs.xml.DOMParser;
import org.nightlabs.xml.NLDOMUtil;
import org.w3c.dom.Attr;
import org.w3c.dom.Comment;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.traversal.NodeIterator;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * This implementation of {@link ServerConfigurator} performs the following tasks in order
 * to configure your JBoss server:
 * <ul>
 * <li>Add the security domains <code>jfireLocal</code> and <code>jfire</code> to the <code>login-config.xml</code></li>
 * <li>Add the <code>CascadedAuthenticationClientInterceptor</code> to the <code>standard-jboss.xml</code></li>
 * <li>Add the <code>CascadedAuthenticationClientInterceptor.properties</code> file, if it does not yet exist (in JBoss' bin directory)</li>
 * </ul>
 * Note, that it only has been tested with and requires JBoss version 4.2.3 due to bugs in JBossRemoting (until 4.2.2).
 *
 * @author Marco Schulze - marco at nightlabs dot de
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public class ServerConfiguratorJBoss
		extends ServerConfigurator
{
	private static final String JFIRE_SERVER_KEYSTORE_FILE_NAME = "jfire-server.keystore";
	/**
	 * The path to 'jfire-server.keystore' file relative from the server configuration root
	 * directory.
	 *
	 */
	private static final String JFIRE_SERVER_KEYSTORE_FILE = "conf/" + JFIRE_SERVER_KEYSTORE_FILE_NAME;
	private static final String HTTPS_CONNECTOR_MODIFIED_COMMENT = "Connector was enabled via ServerConfigurator!";
	private static final String HTTPS_CONNECTOR_KEY_ALIAS = "keyAlias";
	private static final String HTTPS_CONNECTOR_KEYSTORE_PASS = "keystorePass";
	private static final String HTTPS_CONNECTOR_KEYSTORE_FILE = "keystoreFile";
	private static final String HTTPS_ADDRESS = "address";
	private File jbossConfDir;

	private static final Logger logger = Logger.getLogger(ServerConfiguratorJBoss.class);
	protected static final boolean rebootOnDeployDirChanges = false;

	/**
	 * Used to mark the files modified by this server configurator.
	 */
	public static final String ModificationMarker = "!!!ModifiedByJFire!!!";

	private static final String HTTP_INVOKER_SERVICE_HTTP_CONNECTORS =
		"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n " +
		"<root>\n <!-- "+ModificationMarker+" -->\n " +
		"    <!-- STARTING with JNDI exposition! -->\n" +
		"    <!-- Expose the Naming service interface via the UnifiedInvoker using Servlet transport -->\n" +
		"    <mbean code=\"org.jboss.invocation.jrmp.server.JRMPProxyFactory\" name=\"jboss:service=proxyfactory,type=unified,transport=servlet,target=naming\">\n" +
		"        <attribute name=\"InvokerName\">jboss.remoting:service=invoker,type=unified,transport=servlet</attribute>\n" +
		"        <attribute name=\"TargetName\">jboss:service=Naming</attribute>\n" +
		"        <attribute name=\"JndiName\"/>\n" +
		"        <attribute name=\"ExportedInterface\">org.jnp.interfaces.Naming</attribute>\n" +
		"        <attribute name=\"ClientInterceptors\">\n" +
		"            <interceptors>\n" +
		"                <interceptor>org.jboss.proxy.ClientMethodInterceptor</interceptor>\n" +
		"                <interceptor>org.jboss.proxy.SecurityInterceptor</interceptor>\n" +
		"                <interceptor>org.jboss.naming.interceptors.ExceptionInterceptor</interceptor>\n" +
		"                <interceptor>org.jboss.invocation.InvokerInterceptor</interceptor>\n" +
		"            </interceptors>\n" +
		"        </attribute>\n" +
		"        <depends>jboss.remoting:service=invoker,type=unified,transport=servlet</depends>\n" +
		"    </mbean>\n" +
		"    <!-- Expose the Naming service interface via the UnifiedInvoker using SSL Servlet transport -->\n" +
		"    <mbean code=\"org.jboss.invocation.jrmp.server.JRMPProxyFactory\" name=\"jboss:service=proxyfactory,type=unified,transport=sslservlet,target=naming\">\n" +
		"        <attribute name=\"InvokerName\">jboss.remoting:service=invoker,type=unified,transport=sslservlet</attribute>\n" +
		"        <attribute name=\"TargetName\">jboss:service=Naming</attribute>\n" +
		"        <attribute name=\"JndiName\"/>\n" +
		"        <attribute name=\"ExportedInterface\">org.jnp.interfaces.Naming</attribute>\n" +
		"        <attribute name=\"ClientInterceptors\">\n" +
		"            <interceptors>\n" +
		"                <interceptor>org.jboss.proxy.ClientMethodInterceptor</interceptor>\n" +
		"                <interceptor>org.jboss.proxy.SecurityInterceptor</interceptor>\n" +
		"                <interceptor>org.jboss.naming.interceptors.ExceptionInterceptor</interceptor>\n" +
		"                <interceptor>org.jboss.invocation.InvokerInterceptor</interceptor>\n" +
		"            </interceptors>\n" +
		"        </attribute>\n" +
		"        <depends>jboss.remoting:service=invoker,type=unified,transport=sslservlet</depends>\n" +
		"    </mbean>\n" +
		"    <!-- Unified invoker (based on remoting) for invocations via HTTP with target EJB2 beans or JNDI -->\n" +
		"    <mbean code=\"org.jboss.invocation.unified.server.UnifiedInvoker\" name=\"jboss.remoting:service=invoker,type=unified,transport=servlet\">\n" +
		"        <depends>jboss:service=TransactionManager</depends>\n" +
		"        <depends>jboss.remoting:service=connector,transport=servlet</depends>\n" +
		"    </mbean>\n" +
		"    <!-- Remoting connector for standard EJB2 beans and JNDI. \n" +
		"\n" +
		"        To test JBREM-960, set an incorrect path like:\n" +
		"        <attribute name=\"path\">invoker/ServerInvokerServlet</attribute>  \n" +
		"   -->\n" +
		"    <mbean code=\"org.jboss.remoting.transport.Connector\"\n" +
		"        display-name=\"Servlet transport Connector\" name=\"jboss.remoting:service=connector,transport=servlet\">\n" +
		"        <!--      <attribute name=\"InvokerLocator\">\n" +
		"         servlet://${jboss.bind.address}:8080/invoker/ServerInvokerServlet\n" +
		"      </attribute>-->\n" +
		"        <attribute name=\"Configuration\">\n" +
		"            <config>\n" +
		"                <invoker transport=\"servlet\">\n" +
		"                    <attribute isParam=\"true\" name=\"dataType\">invocation</attribute>\n" +
		"                    <attribute isParam=\"true\" name=\"marshaller\">org.jboss.invocation.unified.marshall.InvocationMarshaller</attribute>\n" +
		"                    <attribute isParam=\"true\" name=\"unmarshaller\">org.jboss.invocation.unified.marshall.InvocationUnMarshaller</attribute>\n" +
		"                    <attribute isParam=\"true\" name=\"return-exception\">true</attribute>\n" +
		"                    <attribute name=\"serverBindAddress\">${jboss.bind.address}</attribute>\n" +
		"                    <attribute name=\"serverBindPort\">8080</attribute>\n" +
		"                    <attribute name=\"path\">invoker/ServerInvokerServlet</attribute>\n" +
		"                </invoker>\n" +
		"                <handlers>\n" +
		"                    <handler subsystem=\"invoker\">jboss.remoting:service=invoker,type=unified,transport=servlet</handler>\n" +
		"                </handlers>\n" +
		"            </config>\n" +
		"        </attribute>\n" +
		"    </mbean>\n" +
		"    <!-- Unified invoker (based on remoting) for invocations via HTTPs with target EJB2 beans or JNDI -->\n" +
		"    <mbean code=\"org.jboss.invocation.unified.server.UnifiedInvoker\" name=\"jboss.remoting:service=invoker,type=unified,transport=sslservlet\">\n" +
		"        <depends>jboss:service=TransactionManager</depends>\n" +
		"        <depends>jboss.remoting:service=connector,transport=sslservlet</depends>\n" +
		"    </mbean>\n" +
		"    <mbean code=\"org.jboss.remoting.transport.Connector\"\n" +
		"        display-name=\"SSL Servlet transport Connector\" name=\"jboss.remoting:service=connector,transport=sslservlet\">\n" +
		"        <!--      <attribute name=\"InvokerLocator\">\n" +
		"	sslservlet://${jboss.bind.address}:8443/invoker/SSLServerInvokerServlet\n" +
		"      </attribute>-->\n" +
		"        <attribute name=\"Configuration\">\n" +
		"            <config>\n" +
		"                <invoker transport=\"sslservlet\">\n" +
		"                    <attribute isParam=\"true\" name=\"dataType\">invocation</attribute>\n" +
		"                    <attribute isParam=\"true\" name=\"marshaller\">org.jboss.invocation.unified.marshall.InvocationMarshaller</attribute>\n" +
		"                    <attribute isParam=\"true\" name=\"unmarshaller\">org.jboss.invocation.unified.marshall.InvocationUnMarshaller</attribute>\n" +
		"                    <attribute isParam=\"true\" name=\"return-exception\">true</attribute>\n" +
		"                    <attribute name=\"serverBindAddress\">${jboss.bind.address}</attribute>\n" +
		"                    <attribute name=\"serverBindPort\">8443</attribute>\n" +
		"                    <attribute name=\"path\">invoker/SSLServerInvokerServlet</attribute>\n" +
		"                </invoker>\n" +
		"                <handlers>\n" +
		"                    <handler subsystem=\"invoker\">jboss.remoting:service=invoker,type=unified,transport=sslservlet</handler>\n" +
		"                </handlers>\n" +
		"            </config>\n" +
		"        </attribute>\n" +
		"    </mbean>\n" +
		"    <!-- FINISHED with JNDI exposition! -->\n" +
		"    <!-- Exposing EJB3 via servlet transport. -->\n" +
		"    <!-- Unified invoker (based on remoting) for invocations via HTTP with target EJB3 beans. -->\n" +
		"    <mbean code=\"org.jboss.remoting.transport.Connector\"\n" +
		"        display-name=\"EJB3 Servlet transport Connector\" name=\"jboss.remoting:service=connector,transport=servlet,target=ejb3\">\n" +
		"        <depends>jboss.aop:service=AspectDeployer</depends>\n" +
		"        <attribute name=\"InvokerLocator\">\n" +
		"        servlet://${jboss.bind.address}:8080/invoker/Ejb3ServerInvokerServlet\n" +
		"      </attribute>\n" +
		"        <attribute name=\"Configuration\">\n" +
		"            <handlers>\n" +
		"                <handler subsystem=\"AOP\">org.jboss.aspects.remoting.AOPRemotingInvocationHandler</handler>\n" +
		"            </handlers>\n" +
		"        </attribute>\n" +
		"    </mbean>\n" +
		"    <!-- Unified invoker (based on remoting) for invocations via HTTPs with target EJB3 beans -->\n" +
		"    <mbean code=\"org.jboss.remoting.transport.Connector\"\n" +
		"        display-name=\"EJB3 Servlet SSL transport Connector\" name=\"jboss.remoting:service=connector,transport=sslservlet,target=ejb3\">\n" +
		"        <depends>jboss.aop:service=AspectDeployer</depends>\n" +
		"        <attribute name=\"InvokerLocator\">\n" +
		"        sslservlet://${jboss.bind.address}:8443/invoker/SSLEjb3ServerInvokerServlet\n" +
		"      </attribute>\n" +
		"        <attribute name=\"Configuration\">\n" +
		"            <handlers>\n" +
		"                <handler subsystem=\"AOP\">org.jboss.aspects.remoting.AOPRemotingInvocationHandler</handler>\n" +
		"            </handlers>\n" +
		"        </attribute>\n" +
		"    </mbean>\n" +
		"</root>";
	private static final String HTTP_INVOKER_WEB_XML_SERVLETS =
		"<?xml version=\"1.0\" encoding=\"UTF-8\"?> \n"+
		"<root>\n <!-- "+ModificationMarker+" -->\n     " +
		"    <!-- START JNDI Exposition  -->\n" +
		"    <servlet>\n" +
		"        <servlet-name>JNDIFactory</servlet-name>\n" +
		"        <description>A servlet that exposes the JBoss JNDI Naming service stub\n" +
		"    through http. The return content is a serialized\n" +
		"    MarshalledValue containg the org.jnp.interfaces.Naming stub. This\n" +
		"    configuration handles requests for the standard JNDI naming service.\n" +
		"    </description>\n" +
		"        <servlet-class>org.jboss.invocation.http.servlet.NamingFactoryServlet</servlet-class>\n" +
		"        <init-param>\n" +
		"            <param-name>namingProxyMBean</param-name>\n" +
		"            <param-value>jboss:service=proxyfactory,type=unified,transport=servlet,target=naming</param-value>\n" +
		"        </init-param>\n" +
		"        <init-param>\n" +
		"            <param-name>proxyAttribute</param-name>\n" +
		"            <param-value>Proxy</param-value>\n" +
		"        </init-param>\n" +
		"        <load-on-startup>2</load-on-startup>\n" +
		"    </servlet>\n" +
		"    <servlet-mapping>\n" +
		"        <servlet-name>JNDIFactory</servlet-name>\n" +
		"        <url-pattern>/JNDIFactory/*</url-pattern>\n" +
		"    </servlet-mapping>\n" +
		"    <servlet>\n" +
		"        <servlet-name>SSLJNDIFactory</servlet-name>\n" +
		"        <description>A servlet that exposes the JBoss JNDI Naming service stub\n" +
		"    through http. The return content is a serialized\n" +
		"    MarshalledValue containg the org.jnp.interfaces.Naming stub. This\n" +
		"    configuration handles requests for the standard JNDI naming service.\n" +
		"    </description>\n" +
		"        <servlet-class>org.jboss.invocation.http.servlet.NamingFactoryServlet</servlet-class>\n" +
		"        <init-param>\n" +
		"            <param-name>namingProxyMBean</param-name>\n" +
		"            <param-value>jboss:service=proxyfactory,type=unified,transport=sslservlet,target=naming</param-value>\n" +
		"        </init-param>\n" +
		"        <init-param>\n" +
		"            <param-name>proxyAttribute</param-name>\n" +
		"            <param-value>Proxy</param-value>\n" +
		"        </init-param>\n" +
		"        <load-on-startup>2</load-on-startup>\n" +
		"    </servlet>\n" +
		"    <servlet-mapping>\n" +
		"        <servlet-name>SSLJNDIFactory</servlet-name>\n" +
		"        <url-pattern>/SSLJNDIFactory/*</url-pattern>\n" +
		"    </servlet-mapping>\n" +
		"    <!-- JNDI lookup calls are routed through these two servlets (no EJB3 authentication) is bound to the corresponding Connectors.  -->\n" +
		"    <servlet>\n" +
		"        <servlet-name>ServerInvokerServlet</servlet-name>\n" +
		"        <description>The ServerInvokerServlet receives requests via HTTP\n" +
		"           protocol from within a web container and passes it onto the\n" +
		"           ServletServerInvoker for processing.\n" +
		"        </description>\n" +
		"        <servlet-class>org.jboss.remoting.transport.servlet.web.ServerInvokerServlet</servlet-class>\n" +
		"        <init-param>\n" +
		"            <param-name>locatorUrl</param-name>\n" +
		"            <param-value><![CDATA[servlet://${jboss.bind.address}:8080/invoker/ServerInvokerServlet/?dataType=invocation&marshaller=org.jboss.invocation.unified.marshall.InvocationMarshaller&return-exception=true&unmarshaller=org.jboss.invocation.unified.marshall.InvocationUnMarshaller]]></param-value>\n" +
		"            <description>The servlet server invoker locator url</description>\n" +
		"        </init-param>\n" +
		"        <load-on-startup>1</load-on-startup>\n" +
		"    </servlet>\n" +
		"    <servlet-mapping>\n" +
		"        <servlet-name>ServerInvokerServlet</servlet-name>\n" +
		"        <url-pattern>/ServerInvokerServlet/*</url-pattern>\n" +
		"    </servlet-mapping>\n" +
		"    <servlet>\n" +
		"        <servlet-name>SSLServerInvokerServlet</servlet-name>\n" +
		"        <description>The ServerInvokerServlet receives requests via HTTPS\n" +
		"           protocol from within a web container and passes it onto the\n" +
		"           ServletServerInvoker for processing.\n" +
		"        </description>\n" +
		"        <servlet-class>org.jboss.remoting.transport.servlet.web.ServerInvokerServlet</servlet-class>\n" +
		"        <init-param>\n" +
		"            <param-name>locatorUrl</param-name>\n" +
		"            <param-value><![CDATA[sslservlet://${jboss.bind.address}:8443/invoker/SSLServerInvokerServlet/?dataType=invocation&marshaller=org.jboss.invocation.unified.marshall.InvocationMarshaller&return-exception=true&unmarshaller=org.jboss.invocation.unified.marshall.InvocationUnMarshaller]]></param-value>\n" +
		"            <description>The ssl servlet server invoker locator url</description>\n" +
		"        </init-param>\n" +
		"        <load-on-startup>1</load-on-startup>\n" +
		"    </servlet>\n" +
		"    <servlet-mapping>\n" +
		"        <servlet-name>SSLServerInvokerServlet</servlet-name>\n" +
		"        <url-pattern>/SSLServerInvokerServlet/*</url-pattern>\n" +
		"    </servlet-mapping>\n" +
		"    <!-- END JNDI Exposition  -->\n" +
		"    <servlet>\n" +
		"        <servlet-name>Ejb3ServerInvokerServlet</servlet-name>\n" +
		"        <description>The ServerInvokerServlet receives requests via HTTP\n" +
		"       protocol from within a web container and passes it onto the\n" +
		"       ServletServerInvoker for processing.\n" +
		"    </description>\n" +
		"        <servlet-class>org.jboss.remoting.transport.servlet.web.ServerInvokerServlet</servlet-class>\n" +
		"        <!-- Pass locatorUrl instead of invokerName because otherwise you end up\n" +
		"    sharing the same server invoker for org.jboss.invocation and org.jboss.aop \n" +
		"    type of invocations which you don't wanna do. Worth noting that invokerName \n" +
		"    is hardcoded and hence you cannot create a separate one that way, hence the \n" +
		"    use of locatorUrl. Was fixed in JBoss 4.2.3. -->\n" +
		"        <init-param>\n" +
		"            <param-name>locatorUrl</param-name>\n" +
		"            <param-value>servlet://${jboss.bind.address}:8080/invoker/Ejb3ServerInvokerServlet</param-value>\n" +
		"            <description>The servlet server invoker</description>\n" +
		"        </init-param>\n" +
		"        <load-on-startup>1</load-on-startup>\n" +
		"    </servlet>\n" +
		"    <servlet-mapping>\n" +
		"        <servlet-name>Ejb3ServerInvokerServlet</servlet-name>\n" +
		"        <url-pattern>/Ejb3ServerInvokerServlet/*</url-pattern>\n" +
		"    </servlet-mapping>\n" +
		"    <servlet>\n" +
		"        <servlet-name>SSLEjb3ServerInvokerServlet</servlet-name>\n" +
		"        <description>The ServerInvokerServlet receives requests via HTTPS\n" +
		"       protocol from within a web container and passes it onto the\n" +
		"       ServletServerInvoker for processing.\n" +
		"    </description>\n" +
		"        <servlet-class>org.jboss.remoting.transport.servlet.web.ServerInvokerServlet</servlet-class>\n" +
		"        <init-param>\n" +
		"            <param-name>locatorUrl</param-name>\n" +
		"            <param-value>sslservlet://${jboss.bind.address}:8443/invoker/SSLEjb3ServerInvokerServlet</param-value>\n" +
		"            <description>The ssl servlet server invoker locator url</description>\n" +
		"        </init-param>\n" +
		"        <load-on-startup>1</load-on-startup>\n" +
		"    </servlet>\n" +
		"    <servlet-mapping>\n" +
		"        <servlet-name>SSLEjb3ServerInvokerServlet</servlet-name>\n" +
		"        <url-pattern>/SSLEjb3ServerInvokerServlet/*</url-pattern>\n" +
		"    </servlet-mapping>\n" +
		"</root>";

	protected static void waitForServer()
	{
		if(System.getProperty("jboss.home.dir") != null) {
			// we are running in jboss!
			logger.debug("Waiting for server...");
			try { Thread.sleep(15000); } catch (InterruptedException ignore) { }
		}
	}

//	private static File getNonExistingFile(String pattern)
//	{
//		if(pattern == null)
//			throw new NullPointerException("pattern is null");
//		synchronized(pattern) {
//			int idx = 1;
//			File f;
//			do {
//				f = new File(String.format(pattern, idx));
//				idx++;
//			} while(f.exists());
//			return f;
//		}
//	}

//	protected static File backup(File f) throws IOException
//	{
//		if(!f.exists() || !f.canRead())
//			throw new FileNotFoundException("Invalid file to backup: "+f);
//		File backupFile = new File(f.getAbsolutePath()+".bak");
//		if(backupFile.exists())
//			backupFile = getNonExistingFile(f.getAbsolutePath()+".%d.bak");
//		IOUtil.copyFile(f, backupFile);
//		logger.info("Created backup of file "+f.getAbsolutePath()+": "+backupFile.getName());
//		return backupFile;
//	}
//
//	protected static File moveToBackup(File f) throws IOException
//	{
//		if(!f.exists())
//			throw new FileNotFoundException("Invalid file to backup: "+f);
//		File backupFile = new File(f.getAbsolutePath()+".bak");
//		if(backupFile.exists())
//			backupFile = getNonExistingFile(f.getAbsolutePath()+".%d.bak");
//		if(!f.renameTo(backupFile))
//			throw new IOException("Renaming file "+f.getAbsolutePath()+" to "+f.getName()+" failed");
//		return backupFile;
//	}

	// due to changes, server version is not needed anymore.
	// Please keep the commented enum in the file - it might be useful for later use.
//	public static enum ServerVersion {
//		JBOSS_4_0_x,
//		JBOSS_4_2_x
//	}
//	private ServerVersion serverVersion;

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.serverconfigurator.ServerConfigurator#doConfigureServer()
	 */
	@Override
	protected void doConfigureServer()
			throws ServerConfigurationException
	{
		try {
			// jbossDeployDir is ${jboss}/server/default/deploy - not ${jboss}/server/default/deploy/JFire.last
			// TODO: Why not using the system property set by JBoss to distinguish between different server configurations? (${jboss.server.home.dir}) and then append the deploy dir from the config module? (marius)
			File jbossDeployDir = new File(getJFireServerConfigModule().getJ2ee().getJ2eeDeployBaseDirectory()).getParentFile().getAbsoluteFile();

			jbossConfDir = new File(jbossDeployDir.getParentFile(), "conf");

			File jbossBaseDir = jbossDeployDir.getParentFile().getParentFile().getParentFile();
			File jbossBinDir = new File(jbossBaseDir, "bin");


			// due to changes, server version is not needed anymore.
			// Please keep the commented code in the file - it might be useful for later use.
//			discoverServerVersion(jbossBaseDir);
//			if (serverVersion == null) {
//				logger.error("Could not find out the server version!", new Exception("Could not find out the server version: jbossDeployDir=" + jbossDeployDir.getAbsolutePath()));
//				return;
//			}

			configureLoginConfigXml(jbossConfDir);
			configureAOP(jbossDeployDir);
			configureUnifiedEjbJndiJBoss(jbossConfDir);
//			configureStandardJBossXml(jbossConfDir); Not necessary anymore, since custom compression Sockets aren't used anymore.
			configureMailServiceXml(jbossDeployDir);
			configureJBossjtaPropertiesXml(jbossConfDir);
			configureJBossServiceXml(jbossConfDir);
			configureCascadedAuthenticationClientInterceptorProperties(jbossBinDir);
			configureTomcatServerXml(jbossDeployDir);
			configureHTTPInvoker(jbossDeployDir);
//			configureInvokerWebXml(jbossDeployDir);
			patchRunScripts(jbossBinDir);
			configureJavaOpts(jbossBinDir);
			removeUnneededFiles(jbossDeployDir);

		} catch(Exception e) {
			throw new ServerConfigurationException("Server configuration failed in server configurator "+getClass().getName(), e);
		}
	}

	private void configureAOP(File jbossDeployDir)
	throws SAXException, IOException {
		final File jbossAOPDir = new File(jbossDeployDir, "jboss-aop-jdk50.deployer").getAbsoluteFile();
		if (! jbossAOPDir.exists())
		{
			logger.error("Couldn't find the jboss aop folder! Assumed to be in" +
					jbossAOPDir.toString());
			return;
		}

		final File aopJbossServiceXml = new File(new File(jbossAOPDir, "META-INF"),
		"jboss-service.xml").getAbsoluteFile();
		configureLoadtimeWeaving(aopJbossServiceXml);
	}

	/**
	 *
	 * @param aopJbossServiceXml
	 * @throws FileNotFoundException
	 * @throws SAXException
	 */
	private void configureLoadtimeWeaving(File aopJbossServiceXml)
		throws SAXException, IOException
	{
		final DOMParser parser = new DOMParser();
		FileInputStream serverXmlStream = new FileInputStream(aopJbossServiceXml);
		try
		{
			parser.parse(new InputSource(serverXmlStream));
		}
		finally
		{
			serverXmlStream.close();
		}
		final Document document = parser.getDocument();

		String encoding = document.getXmlEncoding();
		if (encoding == null)
			encoding = "UTF-8";

		if (NLDOMUtil.getDocumentAsString(document, encoding).contains(ModificationMarker))
			return;

		Comment comment = document.createComment(ModificationMarker);
		Node mbeanNode = NLDOMUtil.findSingleNode(document, "server/mbean");
		mbeanNode.appendChild(comment);
		Node enableLoadtimeWeavingNode = NLDOMUtil.findNodeByAttribute(
				mbeanNode, "attribute", "name", "EnableLoadtimeWeaving"
		);
		enableLoadtimeWeavingNode.setTextContent("true");

		// write modified file
		backup(aopJbossServiceXml);

		setRebootRequired(true);
		FileOutputStream out = new FileOutputStream(aopJbossServiceXml);
		try {
			NLDOMUtil.writeDocument(document, out, encoding);
		} finally {
			out.close();
		}
	}

	/**
	 * Writes the default httpProxy.properties file to %server%/conf/props/ in which the http(s)invoker's addresses are
	 * written into.
	 *
	 * @param jbossConfDir The configuration directory of the current JBoss server.
	 * @throws IOException In case we couldn't write the httpProxy.properties file.
	 */
	private void configureUnifiedEjbJndiJBoss(File jbossConfDir)
		throws IOException
	{
		final File httpProxyProperties = new File(new File(jbossConfDir, "props"), "httpProxy.properties").getAbsoluteFile();
		if (httpProxyProperties.exists())
			return;

		final Properties httpProxyProps = new Properties();
		final ServletSSLCf servletConfig = getJFireServerConfigModule().getServletSSLCf();
		httpProxyProps.put("httpProxy.invoker.url", servletConfig.getServletBaseURL() + "/invoker/Ejb3ServerInvokerServlet");
		httpProxyProps.put("httpsProxy.invoker.url", servletConfig.getServletBaseURLHttps()+"/invoker/Ejb3ServerInvokerServlet");
		FileOutputStream propStream = new FileOutputStream(httpProxyProperties);
		httpProxyProps.store(propStream, ModificationMarker);
		setRebootRequired(true);
	}

	/**
	 * Adds the https read-only invoker that publishes the JDNI interface and enforces the other ones
	 * to point into the read-only domain.
	 *
	 * <p><b>Note</b>: This assumes that there are only Invoker defined to publish the JNDI service!
	 * </p>
	 *
	 * @param jbossDeployDir the deploy directory of the JBoss J2EE server.
	 * @throws SAXException
	 * @throws IOException
	 */
	private void configureHTTPInvoker(File jbossDeployDir) throws SAXException, IOException
	{
		final File invokerDeployerDir = new File(jbossDeployDir, "http-invoker.sar").getAbsoluteFile();
		if (! invokerDeployerDir.exists())
		{
			logger.error("Couldn't find the jboss http invoker deployer folder! Assumed to be in" +
					invokerDeployerDir.toString());
			return;
		}

		final File httpInvokerServerXml = new File(new File(invokerDeployerDir, "META-INF"),
				"jboss-service.xml").getAbsoluteFile();

		final File httpInvokerWebXml = new File(new File(new File(invokerDeployerDir,"invoker.war"), "WEB-INF"), "web.xml");
		if (! httpInvokerServerXml.exists())
		{
			logger.error("Couldn't find the http invoker jboss-server.xml! Assumed to be here: " +
					httpInvokerServerXml.toString());
			return;
		}
		if (! httpInvokerWebXml.exists())
		{
			logger.error("Couldn't find the http-invoker's web.xml! Assumed to be here: " +
					httpInvokerServerXml.toString());
			return;
		}

		configureHTTPInvokerServiceXML(httpInvokerServerXml);
		configureHTTPInvokerWebXML(httpInvokerWebXml);
	}

	/**
	 *
	 * @param httpInvokerServerXml
	 * @throws FileNotFoundException
	 * @throws SAXException
	 */
	private void configureHTTPInvokerServiceXML(File httpInvokerServerXml)
		throws SAXException, IOException
	{
		final DOMParser parser = new DOMParser();
		FileInputStream serverXmlStream = new FileInputStream(httpInvokerServerXml);;
		try
		{
			parser.parse(new InputSource(serverXmlStream));
		}
		finally
		{
			serverXmlStream.close();
		}
		final Document document = parser.getDocument();

		String encoding = document.getXmlEncoding();
		if (encoding == null)
			encoding = "UTF-8";

		if (NLDOMUtil.getDocumentAsString(document, encoding).contains(ModificationMarker))
			return;

		Node serverNode = NLDOMUtil.findSingleNode(document, "server");
		parser.reset();
		parser.parse(new InputSource(new StringReader(HTTP_INVOKER_SERVICE_HTTP_CONNECTORS)));
		Document additions = parser.getDocument();
		NodeList childNodes = additions.getChildNodes().item(0).getChildNodes();
		for (int i=0; i < childNodes.getLength(); i++)
		{
			Node additionsNode = document.importNode(childNodes.item(i), true);
			serverNode.appendChild(additionsNode);
		}

		// write modified file
		backup(httpInvokerServerXml);

		setRebootRequired(true);
		FileOutputStream out = new FileOutputStream(httpInvokerServerXml);
		try {
			NLDOMUtil.writeDocument(document, out, encoding);
		} finally {
			out.close();
		}
	}

	/**
	 *
	 * @param httpInvokerWebXml
	 * @throws IOException
	 * @throws SAXException
	 */
	private void configureHTTPInvokerWebXML(File httpInvokerWebXml)
		throws IOException, SAXException
	{
		final DOMParser parser = new DOMParser();
		FileInputStream serverXmlStream = new FileInputStream(httpInvokerWebXml);;
		try
		{
			parser.parse(new InputSource(serverXmlStream));
		}
		finally
		{
			serverXmlStream.close();
		}
		final Document document = parser.getDocument();

		String encoding = document.getXmlEncoding();
		if (encoding == null)
			encoding = "UTF-8";

		if (NLDOMUtil.getDocumentAsString(document, encoding).contains(ModificationMarker))
			return;

		// remove the defined JNDI servlet and add ours.
		Collection<Node> servlets = NLDOMUtil.findNodeList(document, "web-app/servlet");
		Collection<Node> servletMappings = NLDOMUtil.findNodeList(document, "web-app/servlet-mapping");

		Node servlet = null;
		Collection<Node> jndiFactoryMappings = new HashSet<Node>();
		for (Node tmp : servlets)
		{
			if (!"JNDIFactory".equalsIgnoreCase(NLDOMUtil.getTextContent(NLDOMUtil.findElementNode("servlet-name", tmp), false)))
				continue;

			servlet = tmp;
			break;
		}
		for (Node tmp : servletMappings)
		{
			if (!"JNDIFactory".equalsIgnoreCase(NLDOMUtil.getTextContent(NLDOMUtil.findElementNode("servlet-name", tmp), false)))
				continue;

			jndiFactoryMappings.add(tmp);
		}

		if (servlet != null)
			servlet.getParentNode().removeChild(servlet);
		if (! jndiFactoryMappings.isEmpty())
			for (Node node : jndiFactoryMappings)
				node.getParentNode().removeChild(node);


		Node serverNode = NLDOMUtil.findSingleNode(document, "web-app");
		parser.reset();

		parser.parse(new InputSource(new StringReader(HTTP_INVOKER_WEB_XML_SERVLETS)));
		Document additions = parser.getDocument();
		NodeList childNodes = additions.getChildNodes().item(0).getChildNodes();
		for (int i=0; i < childNodes.getLength(); i++)
		{
			Node additionsNode = document.importNode(childNodes.item(i), true);
			serverNode.appendChild(additionsNode);
		}

		// write modified file
		backup(httpInvokerWebXml);

		setRebootRequired(true);
		FileOutputStream out = new FileOutputStream(httpInvokerWebXml);
		try {
			NLDOMUtil.writeDocument(document, out, encoding);
		} finally {
			out.close();
		}
	}

//	if (configChanged)
//	{
//		setRebootRequired(true);
//
//		// write modified file
//		backup(httpInvokerServerXml);
//		String xmlEncoding = document.getXmlEncoding();
//		if(xmlEncoding == null)
//			xmlEncoding = "UTF-8";
//
//		FileOutputStream out = new FileOutputStream(httpInvokerServerXml);
//		try {
//			NLDOMUtil.writeDocument(document, out, xmlEncoding);
//		} finally {
//			out.close();
//		}
//	}
//
//
//	final DOMParser parser = new DOMParser();
//	FileInputStream serverXmlStream = new FileInputStream(httpInvokerServerXml);;
//	try
//	{
//		parser.parse(new InputSource(serverXmlStream));
//	}
//	finally
//	{
//		serverXmlStream.close();
//	}
//	final Document document = parser.getDocument();
//
//	Collection<Node> invokerMBeans = NLDOMUtil.findNodeList(document, "server/mbean");
//	final Node invokerMBeansParent = invokerMBeans.iterator().next().getParentNode();
//
//	// ensure all invoker point to read-only-filtered urls (URLs that are in the domain specified by
//	// the read-only filter in the web.xml of the invoker web deployment.
//	// See #configureInvokerWebXml()
//	Node httpsInvokerNode = null;
//	InvokerBeans: for (Node invokerMBean : invokerMBeans)
//	{
//		final Attr invokerClass = (Attr) invokerMBean.getAttributes().getNamedItem("code");
//		// skip the default HttpInvoker
//		if (! invokerClass.getValue().endsWith("HttpProxyFactory"))
//			continue;
//
//		// check if generated https invoker was found
//		final String invokerName = ((Attr) invokerMBean.getAttributes().getNamedItem("name")).getValue();
//		if ("jboss:service=invoker,type=https,target=Naming,readonly=true".equalsIgnoreCase(invokerName))
//			httpsInvokerNode = invokerMBean;
//
//		NodeList attributeChildNodes = invokerMBean.getChildNodes();
//		for (int i=0; i < attributeChildNodes.getLength(); i++)
//		{
//			final Node attrNode = attributeChildNodes.item(i);
//			if (attrNode.getNodeType() == Node.TEXT_NODE || attrNode.getNodeType() == Node.COMMENT_NODE)
//				continue;
//
//			NamedNodeMap attrNodeAttributes = attrNode.getAttributes();
//			final Attr nameAttribute = (Attr) attrNodeAttributes.getNamedItem("code");
//			if (nameAttribute == null || ! "InvokerURLSuffix".equals(nameAttribute.getValue()))
//				continue;
//
//			final String invokerURLSuffix = attrNode.getTextContent();
//			// check if invoker URL is in the read-only domain (the default == 'readonly')
//			if (invokerURLSuffix.contains("readonly"))
//				continue InvokerBeans;
//
//			int invokerIndex = invokerURLSuffix.lastIndexOf("invoker/");
//			if (invokerIndex == -1)
//			{
//				logger.warn("Cannot modify the URL suffix of the http invokers since the URL pattern " +
//				"is unkown! \n It is assumed to be ':%port/%path/invoker/%servletname");
//				return;
//			}
//
//			int endOfInvoker = invokerIndex + "invoker/".length();
//			String newInvokerURLSuffix = invokerURLSuffix.substring(0, endOfInvoker);
//			newInvokerURLSuffix += "readonly/" + invokerURLSuffix.substring(endOfInvoker);
//			attrNode.setTextContent(newInvokerURLSuffix);
//			configChanged = true;
//		}
//	}
//
//	final ServletSSLCf servletSSLCf = getJFireServerConfigModule().getServletSSLCf();
//
//	if (httpsInvokerNode == null)
//	{ // create default https invoker that has the following structure
//		httpsInvokerNode = document.createElement("mbean");
//
//		// add code & name attributes
//		NamedNodeMap attributes = httpsInvokerNode.getAttributes();
//		Attr codeAttr = document.createAttribute("code");
//		codeAttr.setValue("org.jboss.invocation.http.server.HttpProxyFactory");
//		attributes.setNamedItem(codeAttr);
//
//		Attr nameAttr = document.createAttribute("name");
//		nameAttr.setValue("jboss:service=invoker,type=https,target=Naming,readonly=true");
//		attributes.setNamedItem(nameAttr);
//
//		// add child nodes (all the attribute-nodes like URLPrefix, URLSuffix, ExportedInterface, etc.)
//		Node childNode = document.createElement("attribute");
//
//		childNode.setTextContent("jboss:service=Naming");
//		nameAttr = document.createAttribute("name");
//		nameAttr.setValue("InvokerName");
//		childNode.getAttributes().setNamedItem(nameAttr);
//		httpsInvokerNode.appendChild(childNode);
//
//		childNode = document.createElement("attribute");
//		childNode.setTextContent("https://");
//		nameAttr = document.createAttribute("name");
//		nameAttr.setValue("InvokerURLPrefix");
//		childNode.getAttributes().setNamedItem(nameAttr);
//		httpsInvokerNode.appendChild(childNode);
//
//		childNode = document.createElement("attribute");
//		childNode.setTextContent(":"+ servletSSLCf.getSSLPort()+"/invoker/readonly/JMXInvokerServlet");
//		nameAttr = document.createAttribute("name");
//		nameAttr.setValue("InvokerURLSuffix");
//		childNode.getAttributes().setNamedItem(nameAttr);
//		httpsInvokerNode.appendChild(childNode);
//
//		childNode = document.createElement("attribute");
//		childNode.setTextContent("true");
//		nameAttr = document.createAttribute("name");
//		nameAttr.setValue("UseHostName");
//		childNode.getAttributes().setNamedItem(nameAttr);
//		httpsInvokerNode.appendChild(childNode);
//
//		childNode = document.createElement("attribute");
//		childNode.setTextContent("org.jnp.interfaces.Naming");
//		nameAttr = document.createAttribute("name");
//		nameAttr.setValue("ExportedInterface");
//		childNode.getAttributes().setNamedItem(nameAttr);
//		httpsInvokerNode.appendChild(childNode);
//
//		childNode = document.createElement("attribute");
//		nameAttr = document.createAttribute("name");
//		nameAttr.setValue("JndiName");
//		childNode.getAttributes().setNamedItem(nameAttr);
//		httpsInvokerNode.appendChild(childNode);
//
//		childNode = document.createElement("attribute");
//		nameAttr = document.createAttribute("name");
//		nameAttr.setValue("ClientInterceptors");
//		childNode.getAttributes().setNamedItem(nameAttr);
//		httpsInvokerNode.appendChild(childNode);
//
//		Node clientInterceptors = document.createElement("interceptors");
//		childNode.appendChild(clientInterceptors);
//
//		Node interceptor = document.createElement("interceptor");
//		interceptor.setTextContent("org.jboss.proxy.ClientMethodInterceptor");
//		clientInterceptors.appendChild(interceptor);
//
//		interceptor = document.createElement("interceptor");
//		interceptor.setTextContent("org.jboss.proxy.SecurityInterceptor");
//		clientInterceptors.appendChild(interceptor);
//
//		interceptor = document.createElement("interceptor");
//		interceptor.setTextContent("org.jboss.naming.interceptors.ExceptionInterceptor");
//		clientInterceptors.appendChild(interceptor);
//
//		interceptor = document.createElement("interceptor");
//		interceptor.setTextContent("org.jboss.invocation.InvokerInterceptor");
//		clientInterceptors.appendChild(interceptor);
//
//		invokerMBeansParent.appendChild(httpsInvokerNode);
//		configChanged = true;
//	}
//	else
//	{ // update the invoker declaration to meet the https port specs from the server config.
//		NodeList childNodes = httpsInvokerNode.getChildNodes();
//		for (int i = 0; i < childNodes.getLength(); i++) {
//			final Node childNode = childNodes.item(i);
//
//			if (childNode.getNodeType() == Node.TEXT_NODE || childNode.getNodeType() == Node.COMMENT_NODE)
//				continue;
//
//			if (! "InvokerURLSuffix".equals(
//					((Attr)childNode.getAttributes().getNamedItem("name")).getValue()))
//				continue;
//
//			final String oldURLSuffix = childNode.getTextContent();
//			String newUrlSuffix = oldURLSuffix;
//			Pattern pattern = Pattern.compile(":(\\d*).*");
//			Matcher matcher = pattern.matcher(oldURLSuffix);
//
//			if (! matcher.matches())
//			{
//				logger.warn("In the URL suffix of the https Invoker no port definition was found! \n"+
//						"suffix: "+ oldURLSuffix);
//				break;
//			}
//
//			int oldPort = Integer.parseInt(matcher.group(1));
//			if (oldPort != servletSSLCf.getSSLPort())
//			{
//				newUrlSuffix = oldURLSuffix.replaceFirst(":(\\d*)", ":"+Integer.toString(servletSSLCf.getSSLPort()));
//				configChanged = true;
//				childNode.setTextContent(newUrlSuffix);
//			}
//			break;
//		}
//	}

//	/**
//	 * This method edits the ReadOnlyAccessFilter for servlets to allow read-only access to all
//	 * domains and modifies the JNDIFactory servlet to use the read-only invoker.
//	 *
//	 * @param jbossDeployDir the deploy directory of the JBoss J2EE server.
//	 * @throws SAXException
//	 * @throws IOException
//	 */
//	private void configureInvokerWebXml(File jbossDeployDir) throws SAXException, IOException
//	{
//		final File invokerDeployerDir = new File(jbossDeployDir, "http-invoker.sar").getAbsoluteFile();
//		if (! invokerDeployerDir.exists())
//		{
//			logger.error("Couldn't find the jboss http invoker deployer folder! Assumed to be in" +
//					invokerDeployerDir.toString());
//			return;
//		}
//
//		final File httpInvokerWebXml = new File(new File(new File(invokerDeployerDir, "invoker.war"), "WEB-INF"), "web.xml").getAbsoluteFile();
//		if (! httpInvokerWebXml.exists())
//		{
//			logger.error("Couldn't find the http invoker web.xml! Assumed to be " +
//					httpInvokerWebXml.toString());
//			return;
//		}
//
//		final DOMParser parser = new DOMParser();
//		FileInputStream serverXmlStream = new FileInputStream(httpInvokerWebXml);
//		try
//		{
//			parser.parse(new InputSource(serverXmlStream));
//		}
//		finally
//		{
//			serverXmlStream.close();
//		}
//		final Document document = parser.getDocument();
//
//		boolean configChanged = false;
//		Collection<Node> filterList = NLDOMUtil.findNodeList(document, "web-app/filter");
//		for (Node filter : filterList)
//		{
//			Node filterNameNode = NLDOMUtil.findElementNode("filter-name", filter);
//			if (! "ReadOnlyAccessFilter".equals(filterNameNode.getTextContent()))
//				continue;
//
//			Collection<Node> paramValues = NLDOMUtil.findNodeList(filter, "init-param/param-value");
//			for (Node paramValue : paramValues) {
//				if (! "readonly".equals(paramValue.getTextContent()))
//					continue;
//
//				// allow all domains to be read
//				paramValue.setTextContent("");
//				configChanged = true;
//				break;
//			}
//		}
//
//		Collection<Node> servletList = NLDOMUtil.findNodeList(document, "web-app/servlet");
//		Servlets: for (Node servlet : servletList)
//		{
//			Node servletNameNode = NLDOMUtil.findElementNode("servlet-name", servlet);
//			if (! "JNDIFactory".equals(servletNameNode.getTextContent()))
//				continue;
//
//			Collection<Node> paramValues = NLDOMUtil.findNodeList(servlet, "init-param/param-value",
//					false, false);
//			for (Node paramValue : paramValues)
//			{
//				if (! "jboss:service=invoker,type=http,target=Naming".equals(paramValue.getTextContent()))
//					continue;
//
//				paramValue.setTextContent("jboss:service=invoker,type=http,target=Naming,readonly=true");
//				configChanged = true;
//				break Servlets;
//			}
//		}
//
//		if (configChanged)
//		{
//			setRebootRequired(true);
//
//			// write modified file
//			backup(httpInvokerWebXml);
//			String xmlEncoding = document.getXmlEncoding();
//			if(xmlEncoding == null)
//				xmlEncoding = "UTF-8";
//
//			FileOutputStream out = new FileOutputStream(httpInvokerWebXml);
//			try {
//				NLDOMUtil.writeDocument(document, out, xmlEncoding);
//			} finally {
//				out.close();
//			}
//		}
//	}

	/**
	 * @return the config directory of the jboss.
	 */
	protected File getJBossConfigDir()
	{
		return jbossConfDir;
	}

	/**
	 * It is assumed that the jboss web deployer is in the
	 * <pre>${jboss}/server/default/deploy/jboss-web.deployer/</pre> folder.
	 *
	 * It modifies the <pre>server.xml</pre> so that the commented https connector is uncommented and
	 * adapted.
	 * <p><b>Important:</b>
	 * 		If there are multiple https connectors, the first one is adapted to our needs.
	 * </p>
	 *
	 * @param jbossDeployDir the deploy directory of the JBoss J2EE server.
	 * @throws SAXException
	 * @throws IOException
	 */
	private void configureTomcatServerXml(File jbossDeployDir) throws SAXException, IOException
	{
		final File jbossWebDeployerDir = new File(jbossDeployDir, "jboss-web.deployer").getAbsoluteFile();
		if (! jbossWebDeployerDir.exists())
		{
			logger.error("Couldn't find the jboss web deployer folder! Assumed to be in" +
					jbossWebDeployerDir.toString());
			return;
		}

		final File jbossWebDeployerServerXml = new File(jbossWebDeployerDir, "server.xml").getAbsoluteFile();
		if (! jbossWebDeployerServerXml.exists())
		{
			logger.error("Couldn't find the jboss web deployer server.xml! Assumed to be " +
					jbossWebDeployerServerXml.toString());

			return;
		}

		final DOMParser parser = new DOMParser();
		FileInputStream serverXmlStream = new FileInputStream(jbossWebDeployerServerXml);;
		try
		{
			parser.parse(new InputSource(serverXmlStream));
		}
		finally
		{
			serverXmlStream.close();
		}
		final Document document = parser.getDocument();

		Collection<Node> connectors = NLDOMUtil.findNodeList(document, "Server/Service/Connector"); // NodesByAttribute(document, "Server/Connector", "name", "Connector");
		Node httpsNode = null;
		for (Node node : connectors)
		{
			// If there is an https connector defined, then we assume that the connector is used for the
			// update servlets and is made public by the UpdateManagerBean.
			if ("https".equalsIgnoreCase(NLDOMUtil.getAttributeValue(node, "scheme")) &&
					"true".equalsIgnoreCase(NLDOMUtil.getAttributeValue(node, "SSLEnabled")))
			{
				httpsNode = node;
				break;
			}
		}

		boolean modified = false;
		if (httpsNode != null)
		{
			// there is a https connector -> modify it if necessary
			modified = adaptHttpsConnector(document, httpsNode, getJFireServerConfigModule(), getJBossConfigDir());
		}
		else
		{
			// So far no https connector was found -> uncomment the example included in default server.xml
			// and augment with keystore + password
			final Node serviceNode = NLDOMUtil.findSingleNode(document, "Server/Service");
			assert serviceNode != null;
			NodeList childNodes = serviceNode.getChildNodes();
			Node httpsConnectorComment = null;

			for (int i=0; i < childNodes.getLength(); i++)
			{
				Node childNode = childNodes.item(i);
				String nodeText = childNode.getTextContent();

				// \s*\<Connector[^>]*>\s* -- there has to be a Connector definition in the comments text.
				if (childNode.getNodeType() != Node.COMMENT_NODE || nodeText == null ||
						! nodeText.matches("\\s*\\<Connector[^>]*>\\s*"))
					continue;

				httpsConnectorComment = childNode;
				break;
			}

			if (httpsConnectorComment == null)
			{
				logger.error("Cannot find default https connector comment in " + jbossWebDeployerServerXml.toString());
				throw new IllegalStateException("Cannot find default https connector comment in " + jbossWebDeployerServerXml.toString());
			}

			DOMParser domParser = new DOMParser();
			domParser.parse(new InputSource(new StringReader(httpsConnectorComment.getTextContent())));
			Node httpsConnectorNode = domParser.getDocument().getFirstChild();
			Document conDoc = domParser.getDocument();

			NamedNodeMap attributes = httpsConnectorNode.getAttributes();
			if (attributes.getNamedItem(HTTPS_ADDRESS) == null ||
					!((Attr) attributes.getNamedItem(HTTPS_ADDRESS)).getValue().equals("${jboss.bind.address}"))
			{
				Attr addressAttr = (Attr) attributes.getNamedItem(HTTPS_ADDRESS);
				if (addressAttr == null) {
					addressAttr = domParser.getDocument().createAttribute(HTTPS_ADDRESS);
					attributes.setNamedItem(addressAttr);
				}

				addressAttr.setValue("${jboss.bind.address}");
			}

			modified = adaptHttpsConnector(conDoc, httpsConnectorNode, getJFireServerConfigModule(), getJBossConfigDir());

//			try
//			{
//				httpsNode = document.adoptNode(httpsConnectorNode);
//			}
//			catch (DOMException e)
//			{
//				httpsNode = document.importNode(httpsConnectorNode, true);
//			}
// This seems to not work reliably. We have sometimes garbled attributes. Try to do it always with importNode(...) - maybe that works better.
// Alternatively, we should try it with a newer Xerces.
			httpsNode = document.importNode(httpsConnectorNode, true);

			serviceNode.replaceChild(httpsNode, httpsConnectorComment);
		}

		if (modified)
		{
			// insert modified by server configurator as comment above https connector.
			Node nodeAboveConnector = httpsNode.getPreviousSibling().getPreviousSibling();
			if (nodeAboveConnector != null)
			{
				if (! (nodeAboveConnector instanceof Comment) )
				{
					Comment comment = document.createComment(HTTPS_CONNECTOR_MODIFIED_COMMENT);
					nodeAboveConnector.appendChild(comment);
				}
				else
				{
					final Comment comment = (Comment) nodeAboveConnector;
					final String commentText = comment.getTextContent();
					if (! commentText.contains(HTTPS_CONNECTOR_MODIFIED_COMMENT))
					{
						comment.setTextContent(commentText + "\n\t\t" + HTTPS_CONNECTOR_MODIFIED_COMMENT);
					}
				}
			}

			setRebootRequired(true);
			// write modified file
			backup(jbossWebDeployerServerXml);
			String xmlEncoding = document.getXmlEncoding();
			if(xmlEncoding == null)
				xmlEncoding = "UTF-8";

			FileOutputStream out = new FileOutputStream(jbossWebDeployerServerXml);
			try {
				NLDOMUtil.writeDocument(document, out, xmlEncoding);
			} finally {
				out.close();
			}
		}
	}

	/**
	 * Sets the reference to the keystore file, the keystore password as well as the chosen private
	 * certificate to the given httpsConnector node if not already set to the values from the given
	 * serverConfigModule.
	 *
	 * @param document The document with which new Nodes may be created
	 * @param httpsConnector The node corresponding to a https connector definition.
	 * @param serverConfigModule The config module containing the information about the keystore &
	 * 	the certificate.
	 * @param jbossConfigDir TODO
	 * @return whether the attributes of the given httpsConnector node have been modified.
	 */
	private boolean adaptHttpsConnector(Document document, Node httpsConnector,
			JFireServerConfigModule serverConfigModule, File jbossConfigDir)
	{
		assert document != null;
		assert httpsConnector != null;
		assert serverConfigModule != null;
		final ServletSSLCf servletSSLCf = serverConfigModule.getServletSSLCf();

		// Add keystore and password (currently it is assumed that there is only one certificate in
		// the keystore which will then be used for this connector.
		// keystoreFile="../../bin/jfire-server.keystore" (This is a convention -
		// see JFireServerManager/src/jfire-server.keystore.readme)
		NamedNodeMap attributes = httpsConnector.getAttributes();
		boolean attributesChanged = false;

		// ensures that the keystore is always in %JBoss%/server/%servername%/conf/jfire-server.keystore
//		new File("");
//		final File jbossBinDir = new File(jbossConfigDir.getParentFile().getParentFile().getParentFile(), "bin");
//		final File jbossBinDir = new File("");
//		String realtiveKeystoreFilePath = null;
//		try
//		{
//			realtiveKeystoreFilePath = IOUtil.getRelativePath(jbossBinDir, new File(jbossConfigDir, JFIRE_SERVER_KEYSTORE_FILE_NAME));
//		} catch (IOException e)
//		{
//			throw new IllegalStateException("Couldn't build a relative path to the keystore file!");
//		}

		if (attributes.getNamedItem(HTTPS_CONNECTOR_KEYSTORE_FILE) == null ||
				!((Attr) attributes.getNamedItem(HTTPS_CONNECTOR_KEYSTORE_FILE)).getValue().equals(JFIRE_SERVER_KEYSTORE_FILE))
		{
			Attr keystoreFileAttr = (Attr) attributes.getNamedItem(HTTPS_CONNECTOR_KEYSTORE_FILE);
			if (keystoreFileAttr == null) {
				keystoreFileAttr = document.createAttribute(HTTPS_CONNECTOR_KEYSTORE_FILE);
				attributes.setNamedItem(keystoreFileAttr);
			}

			keystoreFileAttr.setValue(JFIRE_SERVER_KEYSTORE_FILE);
			attributesChanged = true;
		}

		// keystorePass="nightlabs"
		if (attributes.getNamedItem(HTTPS_CONNECTOR_KEYSTORE_PASS) == null ||
				!((Attr) attributes.getNamedItem(HTTPS_CONNECTOR_KEYSTORE_PASS)).getValue().equals(servletSSLCf.getKeystorePassword()))
		{
			Attr keyPassAttr = (Attr) attributes.getNamedItem(HTTPS_CONNECTOR_KEYSTORE_PASS);
			if (keyPassAttr == null) {
				keyPassAttr = document.createAttribute(HTTPS_CONNECTOR_KEYSTORE_PASS);
				attributes.setNamedItem(keyPassAttr);
			}

			keyPassAttr.setValue(servletSSLCf.getKeystorePassword());
			attributesChanged = true;
		}

		// keyAlias= the chosen certificate
		if (attributes.getNamedItem(HTTPS_CONNECTOR_KEY_ALIAS) == null ||
				!((Attr) attributes.getNamedItem(HTTPS_CONNECTOR_KEY_ALIAS)).getValue().equals(servletSSLCf.getSslServerCertificateAlias()))
		{
			Attr keyAliasAttr = (Attr) attributes.getNamedItem(HTTPS_CONNECTOR_KEY_ALIAS);
			if (keyAliasAttr == null) {
				keyAliasAttr = document.createAttribute(HTTPS_CONNECTOR_KEY_ALIAS);
				attributes.setNamedItem(keyAliasAttr);
			}

			keyAliasAttr.setValue(servletSSLCf.getSslServerCertificateAlias());
			attributesChanged = true;
		}

		// TODO: As soon as tomcat supports a different password for the chosen certificate, set it here! (marius)
		return attributesChanged;
	}

	// due to changes, server version is not needed anymore.
	// Please keep the commented method in the file - it might be useful for later use.
//	private void discoverServerVersion(File jbossBaseDir) throws SAXException, FileNotFoundException, IOException
//	{
//		DOMParser parser = new DOMParser();
//		File jarVersionsFile = new File(jbossBaseDir, "jar-versions.xml");
//		parser.parse(new InputSource(new FileInputStream(jarVersionsFile)));
//		Document document = parser.getDocument();
//		Node node = NLDOMUtil.findNodeByAttribute(document, "jar-versions/jar", "name", "run.jar");
//		if (node == null) {
//			logger.error("File does not contain a node matching path=\"jar-versions/jar\" attributeName=\"name\" attributeValue=\"run.jar\": " + jarVersionsFile.getAbsolutePath());
//			return;
//		}
//
//		String specVersion = NLDOMUtil.getAttributeValue(node, "specVersion");
//		if (specVersion == null) {
//			logger.error("File does contain a node matching path=\"jar-versions/jar\" attributeName=\"name\" attributeValue=\"run.jar\", but it does not have the attribute \"specVersion\": " + jarVersionsFile.getAbsolutePath());
//			return;
//		}
//
//		if (specVersion.startsWith("4.0."))
//			serverVersion=ServerVersion.JBOSS_4_0_x;
//		else if (specVersion.startsWith("4.2."))
//			serverVersion=ServerVersion.JBOSS_4_2_x;
//		else
//			logger.error("File does contain a node matching path=\"jar-versions/jar\" attributeName=\"name\" attributeValue=\"run.jar\", but its attribute \"specVersion\" has the unknown value \"" + specVersion + "\": " + jarVersionsFile.getAbsolutePath());
//	}

	/**
	 * create ${jboss.bin}/CascadedAuthenticationClientInterceptor.properties if not yet existent
	 * jboss' bin is *NOT ALWAYS* our current working directory
	 *
	 * @param jbossBinDir The JBoss bin dir
	 * @throws FileNotFoundException If the file eas not found
	 * @throws IOException In case of an io error
	 */
	private void configureCascadedAuthenticationClientInterceptorProperties(File jbossBinDir) throws FileNotFoundException, IOException
	{
//		File destFile = new File(jbossBinDir, "CascadedAuthenticationClientInterceptor.properties");
//		if (!destFile.exists()) {
//			logger.info("File " + destFile.getAbsolutePath() + " does not exist. Will create it with enable=yes.");
//			Properties props = new Properties();
//			props.put("enable", "yes");
//			FileOutputStream out = new FileOutputStream(destFile);
//			try {
//				props.store(out, "Automatically created by " + this.getClass().getName());
//			} finally {
//				out.close();
//			}
//			CascadedAuthenticationClientInterceptor.reloadProperties(); // reboot should not be necessary anymore after this extension
//		}
	}

	private static boolean setMBeanAttribute(Document document, String mbeanCode, String attributeName, String comment, String content)
	{
		return setMBeanChild(document, mbeanCode, "attribute", attributeName, comment, content);
	}

	private static boolean setMBeanChild(Document document, String mbeanCode, String childTag, String childName, String comment, String content)
	{
		Node mbeanNode = NLDOMUtil.findNodeByAttribute(document, "server/mbean", "code", mbeanCode);
		if(mbeanNode == null) {
			logger.error("mbean node not found for code=\""+mbeanCode+"\"");
			return false;
		}
		Node attributeNode = NLDOMUtil.findNodeByAttribute(mbeanNode, childTag, "name", childName);
		if(attributeNode == null) {
			logger.error("attribute node not found for name=\""+childName+"\" - creating it...");
			Element newElement = document.createElement("attribute");
			newElement.setAttribute("name", childName);
			mbeanNode.appendChild(newElement);
			attributeNode = newElement;
		}
		NLDOMUtil.setTextContentWithComment(attributeNode, comment, content);
		return true;
	}

	private static Node getMBeanAttributeNode(Document document, String mbeanCode, String attributeName)
	{
		return getMBeanChildNode(document, mbeanCode, "attribute", attributeName);
	}

	private static Node getMBeanChildNode(Document document, String mbeanCode, String childTag, String childName)
	{
		Node mbeanNode = NLDOMUtil.findNodeByAttribute(document, "server/mbean", "code", mbeanCode);
		if(mbeanNode == null) {
			logger.error("mbean node not found for code=\""+mbeanCode+"\"");
			return null;
		}
		Node attributeNode = NLDOMUtil.findNodeByAttribute(mbeanNode, childTag, "name", childName);
		if(attributeNode == null) {
			logger.error("Tag "+childTag+" node not found for name=\""+childName+"\"");
			return null;
		}
		return attributeNode;
	}

	/**
	 * Replace an MBean attribute value.
	 * @return <code>true</code> if there are changes in the document - <code>false</code> otherwise.
	 */
	private static boolean replaceMBeanAttribute(Document document, String mbeanCode, String attributeName, String comment, String content)
	{
		Node node = getMBeanAttributeNode(document, mbeanCode, attributeName);
		String oldValue = null;
		if(node != null)
			oldValue = node.getTextContent().trim();
		if(oldValue == null || !oldValue.equals(content)) {
			logger.info("Updating mbean attribute: "+mbeanCode+" -> "+attributeName+": "+content);
			return setMBeanAttribute(document, mbeanCode, attributeName, comment, content);
		}
		return false;
	}

	/**
	 * Set the JAAS cache timeout and the transaction timout according to the settings in
	 * {@link ServiceSettingsConfigModule}.
	 * Set the listening ports as configured in {@link ServicePortsConfigModule}.
	 *
	 * @param jbossConfDir The JBoss config dir
	 * @throws FileNotFoundException If the file was not found
	 * @throws IOException In case of an io error
	 * @throws SAXException In case of a sax error
	 */
	private void configureJBossServiceXml(File jbossConfDir) throws FileNotFoundException, IOException, SAXException
	{
		File destFile = new File(jbossConfDir, "jboss-service.xml");
		String modificationMarker = "!!!ModifiedByJFire!!!";

		DOMParser parser = new DOMParser();
		parser.parse(new InputSource(new FileInputStream(destFile)));
		Document document = parser.getDocument();

		boolean needRestart = false;
		boolean haveChanges = false;
		boolean changed;

		// read ServicePortsConfigModule
		ServiceSettingsConfigModule serviceSettingsConfigModule = null;
		try {
			serviceSettingsConfigModule = getConfig().getConfigModule(ServiceSettingsConfigModule.class, true);
		} catch (ConfigModuleNotFoundException e) {
			serviceSettingsConfigModule = getConfig().createConfigModule(ServiceSettingsConfigModule.class);
		}

		// JAAS TIMEOUT
		changed = replaceMBeanAttribute(
				document,
				"org.jboss.security.plugins.JaasSecurityManagerService",
				"DefaultCacheTimeout",
				" " +
						modificationMarker + "\n " +
						ServerConfiguratorJBoss.class.getName() + " has set the JAAS cache timeout according to configuration.\n" +
						" JFire has its own cache, which is updated immediately. We cannot completely deactivate the JAAS cache, however,\n" +
						" because that causes JPOX bugs (why?!).\n Marco :-) ",
				String.valueOf(serviceSettingsConfigModule.getJaasCacheTimeout())) || needRestart;
		if(changed)
			logger.info("Have changes after DefaultCacheTimeout update");
		needRestart |= changed;
		haveChanges |= changed;

		// TRANSACTION TIMEOUT
		final String newTransactionManagerService = "com.arjuna.ats.jbossatx.jta.TransactionManagerService";
		final String oldTransactionManagerService = "org.jboss.tm.TransactionManagerService";
		String transactionManagerService;
		if(getMBeanAttributeNode(document, newTransactionManagerService, "TransactionTimeout") != null)
			transactionManagerService = newTransactionManagerService;
		else
			transactionManagerService = oldTransactionManagerService;
		changed = replaceMBeanAttribute(
				document,
				transactionManagerService,
				"TransactionTimeout",
				" " +
						modificationMarker + "\n " +
						ServerConfiguratorJBoss.class.getName() + " has set the transaction timeout according to configuration. ",
				String.valueOf(serviceSettingsConfigModule.getTransactionTimeout())) || needRestart;
		if(changed)
			logger.info("Have changes after TransactionTimeout update");
		needRestart |= changed;
		haveChanges |= changed;

		// IGNORE SUFFIX
		Node n = getMBeanAttributeNode(document, "org.jboss.deployment.scanner.URLDeploymentScanner", "FilterInstance");
		if(n == null) {
			logger.error("MBean attribute node org.jboss.deployment.scanner.URLDeploymentScanner -> FilterInstance not found");
		} else {
			Node p = NLDOMUtil.findNodeByAttribute(n, "property", "name", "suffixes");
			if(p == null) {
				Element newPropertyElement = document.createElement("property");
				newPropertyElement.setAttribute("name", "suffixes");
				newPropertyElement.setTextContent("#,$,%,~,\\,v,.BAK,.bak,.old,.orig,.tmp,.rej,.sh");
				n.appendChild(newPropertyElement);
				p = newPropertyElement;
			}
			String oldText = p.getTextContent();
			if(!oldText.contains(",-clrepository.xml")) {
				// this change is not critical - no restart required.
				haveChanges = true;
				String newText = oldText+",-clrepository.xml";
				NLDOMUtil.setTextContentWithComment(
						p,
						" " +
								modificationMarker + "\n         " +
								ServerConfiguratorJBoss.class.getName() + " has added -clrepository.xml ",
						newText);
			}
		}

		// PORT CONFIGURATION
		Node serviceBindingNode = NLDOMUtil.findNodeByAttribute(document, "server/mbean", "code",
			"org.jboss.services.binding.ServiceBindingManager");

		// find the Node ServiceBindingManager to add our custom port configuration
		if (serviceBindingNode == null) {
			changed = true;
			Element root = document.getDocumentElement();

			Element newnode = document.createElement("mbean");// Create Root Element
			newnode.setAttribute("code", "org.jboss.services.binding.ServiceBindingManager");
			newnode.setAttribute("name", "jboss.system:service=ServiceBindingManager");

			String LINEBREAK = "\n" + "\t" + "\t";
			String commentString = modificationMarker + LINEBREAK +
				"Binding service manager for port/host mapping. This config loading its bindings from an XML file using the ServicesStoreFactory implementation returned by the XMLServicesStoreFactory. " + LINEBREAK +
				"ServerName: The unique name assigned to a JBoss server instance for lookup purposes. This allows a single ServicesStore to handle mulitiple JBoss servers." + LINEBREAK +
				"StoreURL: The URL string passed to org.jboss.services.binding.ServicesStore during initialization that specifies how to connect to the bindings store. StoreFactory: The org.jboss.services.binding.ServicesStoreFactory interface implementation to create to obtain the ServicesStore instance.";
			Comment comment = document.createComment(commentString);
			newnode.appendChild(comment);

			Element item = document.createElement("attribute");
			item.setAttribute("name", "ServerName");
			item.setTextContent("ports-01");
			newnode.appendChild( item );

			item = document.createElement("attribute");
			item.setAttribute("name", "StoreURL");
			item.setTextContent("${jboss.home.url}/server/default/conf/service-bindings.xml");
			newnode.appendChild( item );

			item = document.createElement("attribute");
			item.setAttribute("name", "StoreFactoryClassName");
			item.setTextContent("org.jboss.services.binding.XMLServicesStoreFactory");
			newnode.appendChild( item );

			root.insertBefore(newnode, serviceBindingNode);

			logger.info("Added Custom MBean ServiceBindingManager for the Services Port Configuration");
		}

		// read ServicePortsConfigModule
		ServicePortsConfigModule servicePortsConfigModule = null;
		try {
			servicePortsConfigModule = getConfig().getConfigModule(ServicePortsConfigModule.class, true);
		} catch (ConfigModuleNotFoundException e) {
			servicePortsConfigModule = getConfig().createConfigModule(ServicePortsConfigModule.class);
		}

		String comment = null;

		// configure naming service
		changed = replaceMBeanAttribute(document, "org.jboss.naming.NamingService", "Port", comment, String.valueOf(servicePortsConfigModule.getServiceNamingBindingPort()));
		haveChanges |= changed;
		if (changed)
			logger.info("Have changes after configuring naming service binding port");

		changed = replaceMBeanAttribute(document, "org.jboss.naming.NamingService", "BindAddress", comment, servicePortsConfigModule.getServiceNamingBindingHost());
		haveChanges |= changed;
		if (changed)
			logger.info("Have changes after configuring naming service binding host");

		changed = replaceMBeanAttribute(document, "org.jboss.naming.NamingService", "RmiPort", comment, String.valueOf(servicePortsConfigModule.getServiceNamingRMIPort()));
		haveChanges |= changed;
		if (changed)
			logger.info("Have changes after configuring naming service binding rmi port");

		changed = replaceMBeanAttribute(document, "org.jboss.naming.NamingService", "RmiBindAddress", comment, servicePortsConfigModule.getServiceNamingRMIHost());
		haveChanges |= changed;
		if (changed)
			logger.info("Have changes after configuring naming service binding rmi host");

		// configure web-service
		changed = replaceMBeanAttribute(document, "org.jboss.web.WebService", "Port", comment, String.valueOf(servicePortsConfigModule.getServiceWebServicePort()));
		haveChanges |= changed;
		if (changed)
			logger.info("Have changes after configuring web service port");

		// Do not change host because not declared in service-bindings.xml and in jboss-service.xml declared as  ${java.rmi.server.hostname}
		// and not ${jboss.bind.address} which is the default value of servicePortsConfigModule.getServiceWebServiceHost()
//		changed = replaceMBeanAttribute(document, "org.jboss.web.WebService", "Host", null, String.valueOf(servicePortsConfigModule.getServiceWebServiceHost()));
//		haveChanges |= changed;
//		if (changed)
//			logger.info("Have changes after configuring web service port");

		// RMI/JRMP Invoker
		changed = replaceMBeanAttribute(document, "org.jboss.invocation.jrmp.server.JRMPInvoker", "RMIObjectPort", comment, String.valueOf(servicePortsConfigModule.getServiceJrmpPort()));
		haveChanges |= changed;
		if (changed)
			logger.info("Have changes after configuring JRMP Port");

		changed = replaceMBeanAttribute(document, "org.jboss.invocation.jrmp.server.JRMPInvoker", "ServerAddress", comment, String.valueOf(servicePortsConfigModule.getServiceJrmpHost()));
		haveChanges |= changed;
		if (changed)
			logger.info("Have changes after configuring JRMP Host");

		changed = replaceMBeanAttribute(document, "org.jboss.invocation.pooled.server.PooledInvoker", "ServerBindPort", comment, String.valueOf(servicePortsConfigModule.getServicePooledPort()));
		haveChanges |= changed;
		if (changed)
			logger.info("Have changes after configuring PooledInvoker Port");

		changed = replaceMBeanAttribute(document, "org.jboss.invocation.pooled.server.PooledInvoker", "ServerBindAddress", comment, String.valueOf(servicePortsConfigModule.getServicePooledHost()));
		haveChanges |= changed;
		if (changed)
			logger.info("Have changes after configuring PooledInvoker Host");

		// remoting service
//		changed = replaceMBeanAttribute(document, "org.jboss.remoting.transport.Connector", "serverBindPort", comment, String.valueOf(servicePortsConfigModule.getServiceRemotingConnectorPort()));
		changed = setMBeanChild(document, "org.jboss.remoting.transport.Connector", "attribute/config/invoker/attribute", "serverBindPort", comment, String.valueOf(servicePortsConfigModule.getServiceRemotingConnectorPort()));
		haveChanges |= changed;
		if (changed)
			logger.info("Have changes after configuring remoting service Port");

//		changed = replaceMBeanAttribute(document, "org.jboss.remoting.transport.Connector", "serverBindAddress", comment, String.valueOf(servicePortsConfigModule.getServiceRemotingConnectorHost()));
		changed = setMBeanChild(document, "org.jboss.remoting.transport.Connector", "attribute/config/invoker/attribute", "serverBindAddress", comment, String.valueOf(servicePortsConfigModule.getServiceRemotingConnectorHost()));
		haveChanges |= changed;
		if (changed)
			logger.info("Have changes after configuring remoting service Host");

		// copy service-bindings.xml
		File serviceBindingsDestFile = new File(jbossConfDir, "service-bindings.xml");
		IOUtil.copyResource(ServerConfiguratorJBoss.class,
				"service-bindings.xml.jfire", serviceBindingsDestFile);

		// configure service-bindings.xml based on ServicePortsConfigModule
		configureServiceBindingsXml(jbossConfDir, servicePortsConfigModule);

		// write changes
		if(haveChanges) {
			backup(destFile);
			String xmlEncoding = document.getXmlEncoding();
			if(xmlEncoding == null)
				xmlEncoding = "UTF-8";

			OutputStream out = new FileOutputStream(destFile);
			try {
				NLDOMUtil.writeDocument(document, out, xmlEncoding);
			} finally {
				out.close();
			}
		}

		if(needRestart)
			setRebootRequired(true);
	}


//	/**
//	 * *** work necessary for NightLabsCascadedAuthenticationJBoss ***
//	 * check/modify ${jboss.conf}/standardjboss.xml and REBOOT if changes occured
//	 *
//	 * @param jbossConfDir The JBoss config dir
//	 * @throws FileNotFoundException If the file eas not found
//	 * @throws IOException In case of an io error
//	 * @throws TransformerException
//	 */
//	private void configureStandardJBossXml(File jbossConfDir) throws FileNotFoundException, IOException, SAXException, TransformerException
//	{
//		boolean backupDone = false; // track whether the backup is already done - we don't need 2 backups for this one method.
//		File destFile = new File(jbossConfDir, "standardjboss.xml");
//		String text = IOUtil.readTextFile(destFile);
//
//		//check if we already had the stateless container for the SSL Compression Invoker
//		if (text.indexOf("<name>stateless-compression-invoker</name>") < 0)
//		{
//			logger.info("configureStandardJBossXml: Will add Compression SSL invoker to file: " + destFile.getAbsolutePath());
//
//			if (!backupDone) {
//				backup(destFile);
//				backupDone = true;
//			}
//
//			setRebootRequired(true); // this is a must, because the conf directory doesn't support redeployment
//
//			DOMParser parser = new DOMParser();
//			InputStream in = new FileInputStream(destFile);
//			try {
//				parser.parse(new InputSource(in));
//			} finally {
//				in.close();
//			}
//			Document document = parser.getDocument();
//
//			//configure the ssl compression invoker
//
//			Node root = document.getDocumentElement();
//
//			Node proxynode = NLDOMUtil.findSingleNode(root, "invoker-proxy-bindings");;
//
//			Element newnode = document.createElement("invoker-proxy-binding");// Create Root Element
//
//			Comment comment = document.createComment("Add the Custom Compression SSL Socket proxy Bindings");
//			newnode.appendChild(comment);
//
//			Element item = document.createElement("name");       // Create element
//			item.appendChild( document.createTextNode("stateless-compression-invoker") );
//			newnode.appendChild( item );
//
//			item = document.createElement("invoker-mbean");       // Create element
//			item.appendChild( document.createTextNode("jboss:service=invoker,type=jrmp,socketType=CompressionSocketFactory") );
//			newnode.appendChild( item );
//
//			item = document.createElement("proxy-factory");       // Create element
//			item.appendChild( document.createTextNode("org.jboss.proxy.ejb.ProxyFactory") );
//			newnode.appendChild( item );
//
//
//			item = document.createElement("proxy-factory-config");
//
//			Element item_sub = document.createElement("client-interceptors");
//
//			// add the home tag
//
//			Element item_sub1 = document.createElement("home");
//
//			Element item_value = document.createElement("interceptor");
//			item_value.appendChild( document.createTextNode("org.jboss.proxy.ejb.HomeInterceptor") );
//			item_sub1.appendChild(item_value);
//
//			item_value = document.createElement("interceptor");
//			item_value.appendChild( document.createTextNode("org.jboss.proxy.SecurityInterceptor") );
//			item_sub1.appendChild(item_value);
//
//			item_value = document.createElement("interceptor");
//			item_value.appendChild( document.createTextNode("org.jboss.proxy.TransactionInterceptor") );
//			item_sub1.appendChild(item_value);
//
//			item_value = document.createElement("interceptor");
//			item_value.appendChild( document.createTextNode("org.jboss.invocation.InvokerInterceptor") );
//			item_sub1.appendChild(item_value);
//
//
//			item_sub.appendChild(item_sub1);
//
//
//			// add the bean tag
//
//			item_sub1 = document.createElement("bean");
//
//			item_value = document.createElement("interceptor");
//			item_value.appendChild( document.createTextNode("org.jboss.proxy.ejb.StatelessSessionInterceptor") );
//			item_sub1.appendChild(item_value);
//
//			item_value = document.createElement("interceptor");
//			item_value.appendChild( document.createTextNode("org.jboss.proxy.SecurityInterceptor") );
//			item_sub1.appendChild(item_value);
//
//			item_value = document.createElement("interceptor");
//			item_value.appendChild( document.createTextNode("org.jboss.proxy.TransactionInterceptor") );
//			item_sub1.appendChild(item_value);
//
//			item_value = document.createElement("interceptor");
//			item_value.appendChild( document.createTextNode("org.jboss.invocation.InvokerInterceptor") );
//			item_sub1.appendChild(item_value);
//
//			item_sub.appendChild(item_sub1);
//
//
//			item.appendChild( item_sub );
//
//			newnode.appendChild( item );
//
//			proxynode.appendChild(newnode);
//
//
//			FileOutputStream out = new FileOutputStream(destFile);
//			try {
//				NLDOMUtil.writeDocument(
//						document,
//						out,
//						"UTF-8","-//JBoss//DTD JBOSS 4.0//EN",
//						"http://www.jboss.org/j2ee/dtd/jboss_4_0.dtd"
//				);
//			} finally {
//				out.close();
//			}
//		}


//		// reload the file - not necessary anymore since we now use a DOM parser which reloads (and the cascaded auth stuff isn't modified by the above code)
////		text = IOUtil.readTextFile(destFile);
//
//		if (text.indexOf(CascadedAuthenticationClientInterceptor.class.getName()) < 0) {
//			logger.info("File " + destFile.getAbsolutePath() + " does not contain an interceptor registration for "+CascadedAuthenticationClientInterceptor.class.getName()+". Will add it.");
//
//			if (!backupDone) {
//				backup(destFile);
//				backupDone = true;
//			}
//
//			setRebootRequired(true); // this is a must, because the conf directory doesn't support redeployment (even though some files like log4j.xml does, the standardjboss.xml does not)
//
//			DOMParser parser = new DOMParser();
//			InputStream in = new FileInputStream(destFile);
//			try {
//				parser.parse(new InputSource(in));
//			} finally {
//				in.close();
//			}
//			Document document = parser.getDocument();
//			CachedXPathAPI xpa = new CachedXPathAPI();
//
//			Node containerInterceptorsNode;
//			for (NodeIterator ni1 = xpa.selectNodeIterator(document, "/descendant::container-interceptors"); (containerInterceptorsNode = ni1.nextNode()) != null; ) {
//				Node interceptorNode;
//				for (NodeIterator ni2 = xpa.selectNodeIterator(containerInterceptorsNode, "interceptor"); (interceptorNode = ni2.nextNode()) != null; ) {
//					String textContent = NLDOMUtil.getTextContent(interceptorNode, false).replaceAll("[\n\r]", "").trim();
//					if (textContent.startsWith(TxInterceptorCMT.class.getName())) {
//						Node retryHandlersNode = xpa.selectNodeIterator(containerInterceptorsNode, "retry-handlers").nextNode();
//						if (retryHandlersNode == null) {
//							retryHandlersNode = document.createElement("retry-handlers");
//							interceptorNode.appendChild(retryHandlersNode);
//						}
//						Element retryHandlerElement = document.createElement("handler");
//						retryHandlerElement.appendChild(document.createTextNode(RetryHandler.class.getName()));
//						retryHandlersNode.appendChild(retryHandlerElement);
//					}
//				}
//
//				Element interceptorElement = document.createElement("interceptor");
//				interceptorElement.appendChild(document.createTextNode(ForceRollbackOnExceptionInterceptor.class.getName()));
//				containerInterceptorsNode.appendChild(interceptorElement);
//			}
//
//			Node clientInterceptorsNode;
//			for (NodeIterator ni1 = xpa.selectNodeIterator(document, "/descendant::client-interceptors"); (clientInterceptorsNode = ni1.nextNode()) != null; ) {
//				Node node;
//				for (NodeIterator ni2 = xpa.selectNodeIterator(clientInterceptorsNode, "home"); (node = ni2.nextNode()) != null; ) {
//					Element interceptorElement = document.createElement("interceptor");
//					interceptorElement.appendChild(document.createTextNode(CascadedAuthenticationClientInterceptor.class.getName()));
//					node.insertBefore(interceptorElement, node.getFirstChild());
//				}
//
//				for (NodeIterator ni2 = xpa.selectNodeIterator(clientInterceptorsNode, "bean"); (node = ni2.nextNode()) != null; ) {
//					Element interceptorElement = document.createElement("interceptor");
//					interceptorElement.appendChild(document.createTextNode(CascadedAuthenticationClientInterceptor.class.getName()));
//					node.insertBefore(interceptorElement, node.getFirstChild());
//				}
//			}
//
//
//			FileOutputStream out = new FileOutputStream(destFile);
//			try {
//				NLDOMUtil.writeDocument(
//						document,
//						out,
//						"UTF-8","-//JBoss//DTD JBOSS 4.0//EN",
//						"http://www.jboss.org/j2ee/dtd/jboss_4_0.dtd"
//				);
//			} finally {
//				out.close();
//			}
//		}
//	}

	private void configureMailServiceXml(File jbossDeployDir) throws FileNotFoundException, IOException, SAXException
	{
		File destFile = new File(jbossDeployDir, "mail-service.xml");

		/**
		 * Set this variable to true if at least one setting in the config module does not equal
		 * the respective setting in the configuration file. Doing so will cause the current
		 * version of the file to be backed up and the new one to be written at the end of this method.
		 */
		boolean changed = false;

		DOMParser parser = new DOMParser();
		parser.parse(new InputSource(new FileInputStream(destFile)));
		Document document = parser.getDocument();

		SmtpMailServiceCf smtp = getJFireServerConfigModule().getSmtp();

		if (logger.isDebugEnabled()) {
			logger.debug("configureMailServiceXml: Host: "+smtp.getHost());
			logger.debug("configureMailServiceXml: Port: "+String.valueOf(smtp.getPort()));
			logger.debug("configureMailServiceXml: From: "+smtp.getMailFrom());
			logger.debug("configureMailServiceXml: Username: "+ smtp.getUsername());
//			if (logger.isTraceEnabled()) {
//				logger.trace("configureMailServiceXml: Password: "+ smtp.getPassword());
//			}
			logger.debug("configureMailServiceXml: UseAuthentication: "+ smtp.getUseAuthentication());
			logger.debug("configureMailServiceXml: EncryptionMethod: "+ smtp.getEncryptionMethod());
			logger.debug("configureMailServiceXml: Debug: "+String.valueOf(smtp.getDebug()));
		}

		if (smtp.getUseAuthentication()) {
			changed |= setMBeanAttribute(
					document, "org.jboss.mail.MailService",
					"User", null, smtp.getUsername());

			changed |= setMBeanAttribute(
					document, "org.jboss.mail.MailService",
					"Password", null, smtp.getPassword());
		}

		Map<String, String> props = smtp.createProperties();
		for (Map.Entry<String, String> propEntry : props.entrySet()) {
			changed |= setMailConfigurationAttribute(document, propEntry.getKey(), propEntry.getValue());
		}

		if (changed) {
			backup(destFile);
			FileOutputStream out = new FileOutputStream(destFile);
			try {
				logger.info("File " + destFile.getAbsolutePath() + " was not yet updated. Will change SMTP settings.");
				NLDOMUtil.writeDocument(document, out, "UTF-8");
			} finally {
				out.close();
			}
		}
	}

	private boolean setMailConfigurationAttribute(Document document, String name, String value)
	{
		Collection<Node> nodes = NLDOMUtil.findNodeList(document, "server/mbean/attribute/configuration");
		if (nodes.size() <= 0) {
			logger.warn("Could not fine path server/mbean in mail-service.xml file", new Exception());
			return false;
		}
		Node configurationNode = nodes.iterator().next();
		boolean changed = false;
		Element propertyElement = (Element) NLDOMUtil.findNodeByAttribute(configurationNode, "property", "name", name);
		if (propertyElement == null) {
			if (logger.isInfoEnabled()) {
				logger.info("Configuration-property for mail-service.xml name=\"" + name + "\" could not be found, creating it");
			}
			propertyElement = document.createElement("property");
			propertyElement.setAttribute("name", name);
			propertyElement.setAttribute("value", value);
			changed = true;
			configurationNode.appendChild(propertyElement);
		}
		Node valueItem;
		if (propertyElement != null) {
			valueItem = propertyElement.getAttributes().getNamedItem("value");
			changed |= !value.equals(valueItem.getNodeValue());
			valueItem.setNodeValue(value);
		}

		return changed;
	}

	private void configureJBossjtaPropertiesXml(File jbossConfDir)
	throws FileNotFoundException, IOException, SAXException, DOMException, TransformerException
	{
		File destFile = new File(jbossConfDir, "jbossjta-properties.xml");
		if (!destFile.exists()) {
			logger.info("The JTA configuration file \""+ destFile.getAbsolutePath() +"\" does not exist. Assuming that this is JBoss is older than 4.2.0.GA and not updating the file!");
			return;
		}

		String text = IOUtil.readTextFile(destFile);
		if (text.indexOf("com.arjuna.ats.jta.allowMultipleLastResources") < 0) {
			logger.info("File " + destFile.getAbsolutePath() + " does not contain property \"com.arjuna.ats.jta.allowMultipleLastResources\". Will add it.");

			backup(destFile);
			setRebootRequired(true); // I'm not sure whether the arjuna JTA controller would be reinitialised... this is at least safe.

//			Pattern pattern = Pattern.compile("(<properties depends=\"arjuna\" name=\"jta\">)");
//			String replacementText = "$1\n        <property name=\"com.arjuna.ats.jta.allowMultipleLastResources\" value=\"true\"/>";
//			text = pattern.matcher(text).replaceAll(replacementText);
//
//			IOUtil.writeTextFile(destFile, text);
			DOMParser parser = new DOMParser();
			InputStream in = new FileInputStream(destFile);
			try {
				parser.parse(new InputSource(in));
			} finally {
				in.close();
			}
			Document document = parser.getDocument();
			CachedXPathAPI xpa = new CachedXPathAPI();

			Node n;
			for (NodeIterator ni1 = xpa.selectNodeIterator(document, "//transaction-service/properties[@name=\"jta\"]"); (n = ni1.nextNode()) != null; ) {
				Element propertyElement = document.createElement("property");
				propertyElement.setAttribute("name", "com.arjuna.ats.jta.allowMultipleLastResources");
				propertyElement.setAttribute("value", "true");
				n.appendChild(propertyElement);
			}

			FileOutputStream out = new FileOutputStream(destFile);
			try {
				NLDOMUtil.writeDocument(
						document,
						out,
						"UTF-8"
				);
			} finally {
				out.close();
			}

		}
	}

	/**
	 * *** work necessary for using JFire Authentication & Authorization ***
	 * add our JFire security domains to ${jboss.conf]/login-config.xml if necessary
	 *
	 * @param jbossConfDir The JBoss config dir
	 * @throws FileNotFoundException If the file was not found
	 * @throws IOException In case of an io error
	 */
	private void configureLoginConfigXml(File jbossConfDir) throws FileNotFoundException, IOException
	{
		File destFile = new File(jbossConfDir, "login-config.xml");
		String text;
		text = IOUtil.readTextFile(destFile);
		if (text.indexOf("jfireLocal") < 0)
		{
			backup(destFile);

			setRebootRequired(true);
			logger.info("File " + destFile.getAbsolutePath() + " does not contain the security domain \"jfireLocal\". Will add both, \"jfireLocal\" and \"jfire\".");
			String replacementText =
					"    <application-policy name = \"jfireLocal\">\n" +
					"        <authentication>\n" +
					"            <login-module code = \""+JFireServerLocalLoginModule.class.getName()+"\" flag = \"required\"/>\n" +
					"            <login-module code = \"org.jboss.security.ClientLoginModule\" flag = \"required\">\n" +
					"                <module-option name=\"restore-login-identity\">true</module-option>\n" +
					"                <module-option name=\"multi-threaded\">true</module-option>\n" +
					"            </login-module>\n" +
					"\n" +
					"<!--                    <login-module code = \"org.jboss.security.ClientLoginModule\" flag = \"required\"/>\n" +
					"-->\n" +
					"        </authentication>\n" +
					"    </application-policy>\n" +
					"\n" +
					"    <application-policy name = \"jfire\">\n" +
					"        <authentication>\n" +
					"            <login-module code = \""+JFireServerLoginModule.class.getName()+"\" flag = \"required\"/>\n" +
					"<!-- I deactivated JBoss' ClientLoginModule, because it has a bug and I therefore put its functionality\n" +
					"     (the part that is relevant to us) into our login-module. The bug is that it pops the current subject\n" +
					"     in the abort() method, even though it was never pushed (the push would have happened in commit(),\n" +
					"     if the login was successful).\n" +
					"            <login-module code = \"org.jboss.security.ClientLoginModule\" flag = \"required\">\n" +
					"                  <module-option name=\"restore-login-identity\">true</module-option>\n" +
					"            </login-module>\n" +
					"-->\n" +
					"\n" +
					"<!--                    <login-module code = \"org.jboss.security.ClientLoginModule\" flag = \"required\">\n" +
					"                <module-option name=\"multi-threaded\">true</module-option>\n" +
					"            </login-module>\n" +
					"-->\n" +
					"\n" +
					"<!--                    <login-module code = \"org.jboss.security.ClientLoginModule\" flag = \"required\">\n" +
					"                <module-option name=\"multi-threaded\">true</module-option>\n" +
					"                <module-option name=\"restore-login-identity\">true</module-option>\n" +
					"                <module-option name=\"password-stacking\">true</module-option>\n" +
					"            </login-module>\n" +
					"-->\n" +
					"        </authentication>\n" +
					"    </application-policy>\n" +
					"\n" +
					"</policy>\n";

			text = text.replaceAll("</policy>", replacementText);

			IOUtil.writeTextFile(destFile, text);
		}
	}

	/**
	 * Add custom values to the JAVA_OPTS (by property java.opts)
	 * and set PermSize.
	 * @param jbossBinDir The JBoss bin dir
	 * @throws FileNotFoundException If the file was not found
	 * @throws IOException In case of an io error
	 */
	private void configureJavaOpts(File jbossBinDir) throws FileNotFoundException, IOException
	{
		Properties serverConfiguratorSettings = getJFireServerConfigModule().getJ2ee().getServerConfiguratorSettings();
		String javaOpts = null;
		if(serverConfiguratorSettings != null)
			javaOpts = serverConfiguratorSettings.getProperty("java.opts");
		if(javaOpts == null)
			javaOpts = "";

		String rmiHost = serverConfiguratorSettings == null ? null : serverConfiguratorSettings.getProperty("java.rmi.server.hostname");
		if (rmiHost != null) {
			javaOpts += " -Djava.rmi.server.hostname="+rmiHost+" "+javaOpts;
			// issue #87:
			javaOpts += " -Djava.rmi.server.useLocalHostname=true";
		}

		// not working :-( - see #374
//		String bindAddress = serverConfiguratorSettings == null ? null : serverConfiguratorSettings.getProperty("j2ee.bind.address");
//		if(bindAddress != null) {
//			javaOpts += " -Djboss.bind.address="+bindAddress;
//		}

		// issue #58:
		javaOpts += " -XX:PermSize=64m -XX:MaxPermSize=128m -javaagent:../server/default/deploy/jboss-aop-jdk50.deployer/pluggable-instrumentor.jar";

		configureRunConf(jbossBinDir, javaOpts);
		configureRunBat(jbossBinDir, javaOpts);
	}

	private void configureRunConf(File jbossBinDir, String javaOpts) throws FileNotFoundException, IOException
	{
		String optsBegin = "# JAVA_OPTS by JFire server configurator\nJAVA_OPTS=\"$JAVA_OPTS";
		String optsEnd = "\"";
		Pattern oldOpts = Pattern.compile(
				"^"+Pattern.quote(optsBegin)+"([^\"]*)"+Pattern.quote(optsEnd)+"$",
				Pattern.MULTILINE);

		File destFile = new File(jbossBinDir, "run.conf");
		String text = IOUtil.readTextFile(destFile);

		String newSetting = optsBegin+javaOpts+optsEnd+"\n";
		Matcher m = oldOpts.matcher(text);
		boolean changed = false;
		boolean found = m.find();
//		if(found && !m.group(1).equals(javaOpts)) { I commented this out, because I think it should be possible to modify these options manually. Marco.
//			text = m.replaceFirst(Matcher.quoteReplacement(newSetting));
//			changed = true;
//		} else if(!found) {
		if(!found) {
			text += "\n"+newSetting;
			changed = true;
		}
		if(changed) {
			setRebootRequired(true);
			backup(destFile);
			IOUtil.writeTextFile(destFile, text);
		}
	}

	/**
	 * The test for using the -server option is wrong in the
	 * original file. It checks for the existence of "hotspot"
	 * in the -version output. Client-only VMs also have the
	 * "hotspot" output.
	 * We check for the existence of "-server" in the -help output.
	 */
	private void patchRunScripts(File jbossBinDir)
	{
		try {
			File destFile = new File(jbossBinDir, "run.bat");
			String text = IOUtil.readTextFile(destFile);
			boolean changed = false;
			String originalString = "\"%JAVA%\" -version 2>&1 | findstr /I hotspot > nul";
			String patchedString = "\"%JAVA%\" -version 2>&1 | findstr /I YOUWILLNEVERSEETHISSTRING > nul";
			if(text.indexOf(originalString) != -1) {
				changed = true;
				text = text.replace(originalString, patchedString);
			}
			if(changed) {
//				setRebootRequired(true);
				backup(destFile);
				IOUtil.writeTextFile(destFile, text);
			}
		} catch (IOException e) {
			logger.warn("Patching the run.bat file failed. Please check the -server opt settings");
		}

		// ----

		try {
			File destFile = new File(jbossBinDir, "run.sh");
			String text = IOUtil.readTextFile(destFile);
			boolean changed = false;
			String originalString = "HAS_HOTSPOT=`\"$JAVA\" -version 2>&1 | $GREP -i HotSpot`";
			String patchedString = "HAS_HOTSPOT=`\"$JAVA\" -version 2>&1 | $GREP -i YOUWILLNEVERSEETHISSTRING`";
			if(text.indexOf(originalString) != -1) {
				changed = true;
				text = text.replace(originalString, patchedString);
			}
			if(changed) {
//				setRebootRequired(true);
				backup(destFile);
				IOUtil.writeTextFile(destFile, text);
			}
		} catch (IOException e) {
			logger.warn("Patching the run.sh file failed. Please check the -server opt settings");
		}
	}

	private void configureRunBat(File jbossBinDir, String javaOpts)
	{
		String text;
		try {
			Pattern lastJavaOpts = Pattern.compile(".*set JAVA_OPTS.*?\n", Pattern.DOTALL);

			String optsBegin = "rem JAVA_OPTS by JFire server configurator\r\nset JAVA_OPTS=%JAVA_OPTS% ";
			String optsEnd = "";
			Pattern oldOpts = Pattern.compile(
					"^"+Pattern.quote(optsBegin)+"(.*?)"+Pattern.compile(optsEnd)+"$",
					Pattern.MULTILINE);

			File destFile = new File(jbossBinDir, "run.bat");
			text = IOUtil.readTextFile(destFile);

			String newSetting = optsBegin+javaOpts+optsEnd;
			Matcher m = oldOpts.matcher(text);
			boolean changed = false;
			boolean found = m.find();
			if(found && !m.group(1).equals(javaOpts)) {
				logger.debug("Have change in "+destFile.getAbsolutePath());
				text = m.replaceFirst(Matcher.quoteReplacement(newSetting));
				changed = true;
			} else if(!found) {
				logger.debug("Have new entry in "+destFile.getAbsolutePath());
				Matcher m2 = lastJavaOpts.matcher(text);
				text = m2.replaceFirst("$0"+Matcher.quoteReplacement("\r\n"+newSetting+"\r\n"));
				changed = true;
			}
			if(changed) {
				setRebootRequired(true);
				backup(destFile);
				IOUtil.writeTextFile(destFile, text);
			}
		} catch (IOException e) {
			logger.error("Changing the run.bat file failed. Please set the rmi host by changing the file manually or overwrite it with run.bat.jfire if it exists.");
		}
	}

	private void removeUnneededFiles(File jbossDeployDir) throws IOException
	{
		File[] filesToRemove = {
				new File(jbossDeployDir, "uuid-key-generator.sar"),
				new File(new File(jbossDeployDir, "jms"), "jbossmq-destinations-service.xml"),
		};

		for (File f : filesToRemove) {
			if(f.exists()) {
				File backup = moveToBackup(f);
				logger.info("Moved "+f.getAbsolutePath()+" to "+backup.getAbsolutePath()+" in order to deactivate it");
				setRebootRequired(true);
			}
		}
	}

	@Override
	protected void undoConfigureServer() throws ServerConfigurationException {
		setRebootRequired(true);
		File jbossDeployDir = new File(getJFireServerConfigModule().getJ2ee().getJ2eeDeployBaseDirectory()).getParentFile().getAbsoluteFile();
		File jbossConfDir = new File(jbossDeployDir.getParentFile(), "conf");
		File jbossBaseDir = jbossDeployDir.getParentFile().getParentFile().getParentFile();
		File jbossBinDir = new File(jbossBaseDir, "bin");

		File[] filesToRestore = {
				new File(jbossDeployDir, "uuid-key-generator.sar"),
				new File(new File(jbossDeployDir, "jms"), "jbossmq-destinations-service.xml"),
				new File(jbossBinDir, "run.conf"),
				new File(jbossBinDir, "run.bat"),
				new File(jbossConfDir, "login-config.xml"),
				new File(jbossConfDir, "jbossjta-properties.xml"),
				new File(jbossDeployDir, "mail-service.xml"),
				new File(jbossConfDir, "jboss-service.xml"),
				new File(jbossConfDir, "standardjboss.xml"),
				new File(new File(new File(jbossDeployDir, "jboss-aop-jdk50.deployer"), "META-INF"), "jboss-service.xml"),
				new File(new File(jbossDeployDir, "jboss-web.deployer"), "server.xml"),
				new File(new File(new File(jbossDeployDir, "http-invoker.sar"), "META-INF"), "jboss-service.xml"),
				new File(new File(new File(new File(jbossDeployDir, "http-invoker.sar"), "invoker.war"), "WEB-INF"), "web.xml"),
				new File(jbossConfDir, "jndi.properties")
		};

		try {
			for (File f : filesToRestore)
				restore(f);

			new File(jbossBinDir, "CascadedAuthenticationClientInterceptor.properties").delete();
			new File(jbossConfDir, "service-bindings.xml").delete();
		} catch (IOException e) {
			throw new ServerConfigurationException(e);
		}
	}

	private static Node setServicePortAndHost(Document document, Node nodeServerNode, String serviceConfigName, int port, String host)
	{
		Node serviceNode = NLDOMUtil.findNodeByAttribute(nodeServerNode, "service-config", "name",
				serviceConfigName);
		if (serviceNode != null) {
			Node oldBindingNode = NLDOMUtil.findElementNode("binding", serviceNode);
			if (oldBindingNode != null) {
				serviceNode.removeChild(oldBindingNode);
			}
			Element binding = document.createElement("binding");
			binding.setAttribute("port", String.valueOf(port));
			binding.setAttribute("host", host);
			serviceNode.appendChild(binding);
			return serviceNode;
		}
		return null;
	}

	private void configureServiceBindingsXml(File jbossConfDir, ServicePortsConfigModule servicePortsConfigModule) throws FileNotFoundException, IOException, SAXException
	{
		// configure jndi.properties
		File jndiProperties = new File(jbossConfDir, "jndi.properties");
		configureJndiProperties(jndiProperties, servicePortsConfigModule);

		File destFile = new File(jbossConfDir, "service-bindings.xml");
		DOMParser parser = new DOMParser();
		parser.parse(new InputSource(new FileInputStream(destFile)));
		Document document = parser.getDocument();
		Node nodeServerNode = NLDOMUtil.findNodeByAttribute(document, "service-bindings/server", "name",
			"ports-01");

		if (nodeServerNode != null)
		{
			// naming binding ports
			// Commented because already defined in jboss-service.xml and leads to problem when starting the server (comp not bound)
			Node serviceNamingNode = setServicePortAndHost(document, nodeServerNode, "jboss:service=Naming",
					servicePortsConfigModule.getServiceNamingBindingPort(), servicePortsConfigModule.getServiceNamingBindingHost());
			if (serviceNamingNode != null) {
				Node delegateNode = NLDOMUtil.findNodeByAttribute(serviceNamingNode, "delegate-config", "portName", "Port");
				Node oldAttributeNode = NLDOMUtil.findElementNode("attribute", delegateNode);
				if (oldAttributeNode != null) {
					delegateNode.removeChild(oldAttributeNode);
				}
				Element attribute = document.createElement("attribute");
				attribute.setAttribute("name", "RmiPort");
				attribute.appendChild( document.createTextNode(String.valueOf(servicePortsConfigModule.getServiceNamingRMIPort())) );
				delegateNode.appendChild( attribute );
			}

			// webservice ports
			setServicePortAndHost(document, nodeServerNode, "jboss:service=WebService",
					servicePortsConfigModule.getServiceWebServicePort(), servicePortsConfigModule.getServiceWebServiceHost());

			// jrmp ports
			setServicePortAndHost(document, nodeServerNode, "jboss:service=invoker,type=jrmp",
					servicePortsConfigModule.getServiceJrmpPort(), servicePortsConfigModule.getServiceJrmpHost());

			// pooled ports
			setServicePortAndHost(document, nodeServerNode, "jboss:service=invoker,type=pooled",
					servicePortsConfigModule.getServicePooledPort(), servicePortsConfigModule.getServicePooledHost());

			// HAJNDI (Cluster)
			Node hajndiNode = setServicePortAndHost(document, nodeServerNode, "jboss:service=HAJNDI",
					servicePortsConfigModule.getServiceClusterHAJNDIBindingPort(), servicePortsConfigModule.getServiceClusterHAJNDIBindingHost());
			if (hajndiNode != null) {
				Node delegateNode = NLDOMUtil.findNodeByAttribute(hajndiNode, "delegate-config", "portName", "Port");
				Node oldAttributeNode = NLDOMUtil.findElementNode("attribute", delegateNode);
				if (oldAttributeNode != null) {
					delegateNode.removeChild(oldAttributeNode);
				}
				Element attribute = document.createElement("attribute");
				attribute.setAttribute("name", "RmiPort");
				attribute.appendChild( document.createTextNode(String.valueOf(servicePortsConfigModule.getServiceClusterHAJNDIRMIPort())) );
				delegateNode.appendChild( attribute );
			}

			// jrmpha ports (Cluster)
			setServicePortAndHost(document, nodeServerNode, "jboss:service=invoker,type=jrmpha",
					servicePortsConfigModule.getServiceClusterJrmphaPort(), servicePortsConfigModule.getServiceClusterJrmphaHost());

			// pooledha ports (Cluster)
			setServicePortAndHost(document, nodeServerNode, "jboss:service=invoker,type=pooledha",
					servicePortsConfigModule.getServiceClusterPooledhaPort(), servicePortsConfigModule.getServiceClusterPooledhaHost());

			// corbaORB ports
			setServicePortAndHost(document, nodeServerNode, "jboss:service=CorbaORB",
					servicePortsConfigModule.getServiceCorbaORBPort(), servicePortsConfigModule.getServiceCorbaORBHost());

			// jmx RMI ports
			setServicePortAndHost(document, nodeServerNode, "jboss.jmx:type=Connector,name=RMI",
					servicePortsConfigModule.getServiceJMXConnectorRMIPort(), servicePortsConfigModule.getServiceJMXConnectorRMIHost());

			// SNMP trapd ports
			setServicePortAndHost(document, nodeServerNode, "jboss.jmx:name=SnmpAgent,service=trapd,type=logger",
					servicePortsConfigModule.getServiceSnmpAgentTrapdPort(), servicePortsConfigModule.getServiceSnmpAgentTrapdHost());

			// SNMP snmp ports
			setServicePortAndHost(document, nodeServerNode, "jboss.jmx:name=SnmpAgent,service=snmp,type=adaptor",
					servicePortsConfigModule.getServiceSnmpAgentSnmpPort(), servicePortsConfigModule.getServiceSnmpAgentSnmpHost());

			// JMS Ports (JBossMQ)
			setServicePortAndHost(document, nodeServerNode, "jboss.mq:service=InvocationLayer,type=UIL2",
					servicePortsConfigModule.getServiceJMSPort(), servicePortsConfigModule.getServiceJMSHost());

			// JMS Http Ports (JBossMQ)
			setServicePortAndHost(document, nodeServerNode, "jboss.mq:service=InvocationLayer,type=HTTP",
					servicePortsConfigModule.getServiceJMSHttpPort(), servicePortsConfigModule.getServiceJMSHttpHost());

			// JMS HAJNDI Ports
			// TODO: add port and host in java.naming.provider.url in CDATA
			setServicePortAndHost(document, nodeServerNode, "jboss.mq:service=JMSProviderLoader,name=HAJNDIJMSProvider",
					servicePortsConfigModule.getServiceJSMHajndiPort(), servicePortsConfigModule.getServiceJSMHajndiHost());

			// EJBInvoker ports
			setServicePortAndHost(document, nodeServerNode, "jboss:service=invoker,type=http",
					servicePortsConfigModule.getServiceEJB3InvokerHttpPort(), servicePortsConfigModule.getServiceEJB3InvokerHttpHost());

			// EJB3 Remoting Connector ports
			Node ejb3RemotingConnectorNode = setServicePortAndHost(document, nodeServerNode, "jboss.remoting:type=Connector,name=DefaultEjb3Connector,handler=ejb3",
					servicePortsConfigModule.getServiceEJB3RemoteConnectorPort(), servicePortsConfigModule.getServiceEJB3RemoteConnectorHost());
			if (ejb3RemotingConnectorNode != null) {
				Collection<Node> nodes = NLDOMUtil.findNodeList(ejb3RemotingConnectorNode, "delegate-config", true, false);
				Node delegateNode = nodes.iterator().next();
				Element attribute = document.createElement("attribute");
				attribute.setAttribute("name", "InvokerLocator");
				StringBuilder sb = new StringBuilder();
				sb.append("socket://");
				sb.append(servicePortsConfigModule.getServiceEJB3RemoteConnectorHost());
				sb.append(":");
				sb.append(servicePortsConfigModule.getServiceEJB3RemoteConnectorPort());
				attribute.appendChild( document.createTextNode(sb.toString()) );
				delegateNode.appendChild(attribute);
			}

			// JMX Http ports
			setServicePortAndHost(document, nodeServerNode, "jboss:service=invoker,type=http,target=Naming",
					servicePortsConfigModule.getServiceInvokerJMXHttpPort(), servicePortsConfigModule.getServiceInvokerJMXHttpHost());

			// JMX Http Readonly ports
			setServicePortAndHost(document, nodeServerNode, "jboss:service=invoker,type=http,target=Naming,readonly=true",
					servicePortsConfigModule.getServiceInvokerJMXHttpReadOnlyPort(), servicePortsConfigModule.getServiceInvokerJMXHttpReadOnlyHost());

			// EJBInvokerHA ports
			setServicePortAndHost(document, nodeServerNode, "jboss:service=invoker,type=httpHA",
					servicePortsConfigModule.getServiceInvokerJMXHttpReadOnlyPort(), servicePortsConfigModule.getServiceInvokerJMXHttpReadOnlyHost());

			// JMXInvokerHA ports
			setServicePortAndHost(document, nodeServerNode, "jboss:service=invoker,type=http,target=HAJNDI",
					servicePortsConfigModule.getServiceJMXInvokerHAPort(), servicePortsConfigModule.getServiceJMXInvokerHAHost());

			// Webservice ports
			setServicePortAndHost(document, nodeServerNode, "jboss.ws4ee:service=AxisService",
					servicePortsConfigModule.getServiceAxisServicePort(), servicePortsConfigModule.getServiceAxisServiceHost());

			// remoting connector ports
			setServicePortAndHost(document, nodeServerNode, "jboss.remoting:service=Connector,transport=socket",
					servicePortsConfigModule.getServiceRemotingConnectorPort(), servicePortsConfigModule.getServiceRemotingConnectorHost());

			// tomcat ports
			// TODO add values for portHttps and portAJP
			setServicePortAndHost(document, nodeServerNode, "jboss.web:service=WebServer",
					servicePortsConfigModule.getServiceTomcatPort(), servicePortsConfigModule.getServiceTomcatHost());

			// jboss messaging
			setServicePortAndHost(document, nodeServerNode, "jboss.messaging:service=Connector,transport=bisocket",
					servicePortsConfigModule.getServiceJBossMessagingPort(), servicePortsConfigModule.getServiceJBossMessagingHost());

			String encoding = document.getXmlEncoding();
			if(encoding == null)
				encoding = "UTF-8";

			OutputStream fos = new FileOutputStream(destFile);
			try {
				NLDOMUtil.writeDocument(document, fos, encoding);
			} finally {
				fos.close();
			}
		}
	}

	private void configureJndiProperties(File jndiProperties, ServicePortsConfigModule portsConfigModule) throws IOException
	{
		String jndiHost = portsConfigModule.getServiceNamingBindingHost();
		if (jndiHost != null && !ServicePortsConfigModule.getDefaultHostName().equals(jndiHost)) {
			backup(jndiProperties);
			int port = portsConfigModule.getServiceNamingBindingPort();
			StringBuilder nameBuilder = new StringBuilder();
			String content = IOUtil.readTextFile(jndiProperties);
			StringBuilder sb = new StringBuilder(content);
			sb.append("\n");
			sb.append("# Added by JFire ServerConfiguratorJBoss");
			sb.append("\n");
			nameBuilder.append("java.naming.provider.url=jnp://");
			nameBuilder.append(jndiHost);
			nameBuilder.append(":");
			nameBuilder.append(port);
			sb.append(nameBuilder);
			IOUtil.writeTextFile(jndiProperties, sb.toString());
		}
	}

	@Override
	protected void afterDoConfigureServer(Throwable x)
			throws ServerConfigurationException
	{
		super.afterDoConfigureServer(x);

		if (x != null)
			return;

		final JFireServerConfigModule jfireServerConfigModule = getJFireServerConfigModule();

		if (!jfireServerConfigModule.getServletSSLCf().isKeystoreURLImported())
		{
			final String keystoreURLToImport = jfireServerConfigModule.getServletSSLCf().getKeystoreURLToImport();
			FileOutputStream keystorePropFileStream = null;

			try
			{
				InputStream keystoreToImportStream;
				// distinguish between default and non-default keystore via this constant
				if ("".equals(keystoreURLToImport))
				{
					throw new IllegalStateException("No keystore can be found in " +
							"%jboss%/server/default/config/jfire-server.keystore and no keystoreToImport is set!");
				}
				else if (ServletSSLCf.DEFAULT_KEYSTORE.equals(keystoreURLToImport))
				{
					keystoreToImportStream = ServerConfigurator.class.getResourceAsStream("/jfire-server.keystore");
				}
				else
					keystoreToImportStream = new URL(keystoreURLToImport).openStream();

				boolean transferData = false;
				File jfireServerKeystoreFile = new File(getJBossConfigDir(), JFIRE_SERVER_KEYSTORE_FILE_NAME);

				if (! jfireServerKeystoreFile.exists() || ! jfireServerKeystoreFile.canRead())
				{
					transferData = true;
				}
				else
				{
					FileInputStream fileInputStream = new FileInputStream(jfireServerKeystoreFile);
					if (fileInputStream.available() != keystoreToImportStream.available() ||
							! IOUtil.compareInputStreams(
									keystoreToImportStream, fileInputStream, fileInputStream.available() ))
					{
						transferData = true;
					}
					fileInputStream.close();
				}

				final ServletSSLCf servletSSLCf = jfireServerConfigModule.getServletSSLCf();

				// if files are equal -> don't need to copy.
				if (! transferData)
				{
					// set the keystore file to the imported state (== set the keystoreURLToImport = "")
					servletSSLCf.setKeystoreURLImported();
					return;
				}

				// files differ or the destination file doesn't exist yet
				FileOutputStream keyStoreStream = new FileOutputStream(jfireServerKeystoreFile);
				try {
					IOUtil.transferStreamData(keystoreToImportStream, keyStoreStream);
				}	finally {
					keyStoreStream.close();
				}

				// Write all ssl socket related infos into a properties file next to jfire-server.keystore,
				// because the org.nightlabs.rmissl.socket.SSLCompressionServerSocketFactory and
				// org.nightlabs.rmissl.socket.SSLCompressionRMIServerSocketFactory need to know these
				// infos and the projects don't know anything from each other (and are not allowed to).
				Properties props = new Properties();
				props.put("org.nightlabs.ssl.keystorePassword", servletSSLCf.getKeystorePassword());
				props.put("org.nightlabs.ssl.serverCertificateAlias", servletSSLCf.getSslServerCertificateAlias());
				props.put("org.nightlabs.ssl.serverCertificatePassword", servletSSLCf.getSslServerCertificatePassword());
				File keystorePropFile = new File(getJBossConfigDir(), "jfire-server.keystore.properties").getAbsoluteFile();
				keystorePropFileStream = new FileOutputStream(keystorePropFile);
				props.store(keystorePropFileStream, "The properties needed to read the correct private " +
						"certificate from the jfire-server.keystore.\n" +
						"These credentials are needed by the SSLCompressionServerSocketFactory.");

				// set the keystore file to the imported state (== set the keystoreURLToImport = "")
				servletSSLCf.setKeystoreURLImported();
				setRebootRequired(true);
			}
			catch (IOException e) {
				throw new ServerConfigurationException(e);
			}
			finally {
				if (keystorePropFileStream != null)
				{
					try
					{
						keystorePropFileStream.close();
					}
					catch (IOException e)
					{
						throw new ServerConfigurationException("Couldn't close the output stream from writing "+
								"the keystore properties!", e);
					}
				}
			}
		}
	}

	@Override
	protected void afterUndoConfigureServer(Throwable x)
			throws ServerConfigurationException
	{
		super.afterUndoConfigureServer(x);

		if (x == null)
		{
			try {
				clearKeystoreFile();
			} catch (IOException e) {
				throw new ServerConfigurationException(e);
			}
		}
	}

	private void clearKeystoreFile() throws IOException
	{
		final File jfireServerKeystoreFile = new File(JFIRE_SERVER_KEYSTORE_FILE);

		if (!jfireServerKeystoreFile.exists())
			return;

		int backupFileIndex = 0;
		File backupFile;
		do {
			backupFile = new File(jfireServerKeystoreFile.getAbsolutePath() + '.' + (backupFileIndex++) + ".bak");

			if (backupFile.exists() && IOUtil.compareFiles(jfireServerKeystoreFile, backupFile)) {
				jfireServerKeystoreFile.delete();
				return;
			}
		} while (backupFile.exists());

		if (!jfireServerKeystoreFile.renameTo(backupFile)) {
			IOUtil.copyFile(jfireServerKeystoreFile, backupFile);
			jfireServerKeystoreFile.delete();
		}
	}

}
