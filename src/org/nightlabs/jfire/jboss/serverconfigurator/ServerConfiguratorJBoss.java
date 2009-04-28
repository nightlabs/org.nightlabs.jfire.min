package org.nightlabs.jfire.jboss.serverconfigurator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.util.Collection;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.transform.TransformerException;

import org.apache.log4j.Logger;
import org.apache.xpath.CachedXPathAPI;
import org.nightlabs.jfire.jboss.authentication.JFireServerLocalLoginModule;
import org.nightlabs.jfire.jboss.authentication.JFireServerLoginModule;
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
 * Note, that it has been tested only with the JBoss version 4.0.4 and 4.2.0.
 *
 * @author Marco Schulze - marco at nightlabs dot de
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public class ServerConfiguratorJBoss
		extends ServerConfigurator
{
	private static final String HTTPS_CONNECTOR_MODIFIED_COMMENT = "Connector was enabled via ServerConfigurator!";
	private static final String HTTPS_CONNECTOR_KEY_ALIAS = "keyAlias";
	private static final String HTTPS_CONNECTOR_KEYSTORE_PASS = "keystorePass";
	private static final String HTTPS_CONNECTOR_KEYSTORE_FILE = "keystoreFile";
	private static final String HTTPS_CONNECTOR_KEYSTORE_FILE_LOCATION = "../../bin/jfire-server.keystore";

	private static final Logger logger = Logger.getLogger(ServerConfiguratorJBoss.class);
	protected static final boolean rebootOnDeployDirChanges = false;

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
			File jbossDeployDir = new File(getJFireServerConfigModule().getJ2ee().getJ2eeDeployBaseDirectory()).getParentFile().getAbsoluteFile();
			File jbossConfDir = new File(jbossDeployDir.getParentFile(), "conf");
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
			configureStandardJBossXml(jbossConfDir);
			configureMailServiceXml(jbossDeployDir);
			configureJBossjtaPropertiesXml(jbossConfDir);
			configureJBossServiceXml(jbossConfDir);
			configureCascadedAuthenticationClientInterceptorProperties(jbossBinDir);
			configureTomcatServerXml(jbossDeployDir);
			configureInvokerJBossServiceXml(jbossDeployDir);
			configureInvokerWebXml(jbossDeployDir);
			patchRunScripts(jbossBinDir);
			configureJavaOpts(jbossBinDir);
			removeUnneededFiles(jbossDeployDir);

		} catch(Exception e) {
			throw new ServerConfigurationException("Server configuration failed in server configurator "+getClass().getName(), e);
		}
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
	private void configureInvokerJBossServiceXml(File jbossDeployDir) throws SAXException, IOException
	{
		final File invokerDeployerDir = new File(jbossDeployDir, "http-invoker.sar").getAbsoluteFile();
		if (! invokerDeployerDir.exists())
		{
			logger.error("Couldn't find the jboss http invoker deployer folder! Assumed to be in" +
					invokerDeployerDir.toString());
			return;
		}

		final File jbossHttpInvokerServerXml = new File(new File(invokerDeployerDir, "META-INF"),
				"jboss-service.xml").getAbsoluteFile();
		if (! jbossHttpInvokerServerXml.exists())
		{
			logger.error("Couldn't find the http invoker jboss-server.xml! Assumed to be " +
					jbossHttpInvokerServerXml.toString());
			return;
		}

		final DOMParser parser = new DOMParser();
		FileInputStream serverXmlStream = new FileInputStream(jbossHttpInvokerServerXml);;
		try
		{
			parser.parse(new InputSource(serverXmlStream));
		}
		finally
		{
			serverXmlStream.close();
		}
		final Document document = parser.getDocument();

		Collection<Node> invokerMBeans = NLDOMUtil.findNodeList(document, "server/mbean");
		final Node invokerMBeansParent = invokerMBeans.iterator().next().getParentNode();
		boolean configChanged = false;

		// ensure all invoker point to read-only-filtered urls (URLs that are in the domain specified by
		// the read-only filter in the web.xml of the invoker web deployment.
		// See #configureInvokerWebXml()
		Node httpsInvokerNode = null;
		InvokerBeans: for (Node invokerMBean : invokerMBeans)
		{
			final Attr invokerClass = (Attr) invokerMBean.getAttributes().getNamedItem("code");
			// skip the default HttpInvoker
			if (! invokerClass.getValue().endsWith("HttpProxyFactory"))
				continue;

			// check if generated https invoker was found
			final String invokerName = ((Attr) invokerMBean.getAttributes().getNamedItem("name")).getValue();
			if ("jboss:service=invoker,type=https,target=Naming,readonly=true".equalsIgnoreCase(invokerName))
				httpsInvokerNode = invokerMBean;

			NodeList attributeChildNodes = invokerMBean.getChildNodes();
			for (int i=0; i < attributeChildNodes.getLength(); i++)
			{
				final Node attrNode = attributeChildNodes.item(i);
				if (attrNode.getNodeType() == Node.TEXT_NODE || attrNode.getNodeType() == Node.COMMENT_NODE)
					continue;

				NamedNodeMap attrNodeAttributes = attrNode.getAttributes();
				final Attr nameAttribute = (Attr) attrNodeAttributes.getNamedItem("code");
				if (nameAttribute == null || ! "InvokerURLSuffix".equals(nameAttribute.getValue()))
					continue;

				final String invokerURLSuffix = attrNode.getTextContent();
				// check if invoker URL is in the read-only domain (the default == 'readonly')
				if (invokerURLSuffix.contains("readonly"))
					continue InvokerBeans;

				int invokerIndex = invokerURLSuffix.lastIndexOf("invoker/");
				if (invokerIndex == -1)
				{
					logger.warn("Cannot modify the URL suffix of the http invokers since the URL pattern " +
					"is unkown! \n It is assumed to be ':%port/%path/invoker/%servletname");
					return;
				}

				int endOfInvoker = invokerIndex + "invoker/".length();
				String newInvokerURLSuffix = invokerURLSuffix.substring(0, endOfInvoker);
				newInvokerURLSuffix += "readonly/" + invokerURLSuffix.substring(endOfInvoker);
				attrNode.setTextContent(newInvokerURLSuffix);
				configChanged = true;
			}
		}

		final ServletSSLCf servletSSLCf = getJFireServerConfigModule().getServletSSLCf();

		if (httpsInvokerNode == null)
		{ // create default https invoker that has the following structure
			httpsInvokerNode = document.createElement("mbean");

			// add code & name attributes
			NamedNodeMap attributes = httpsInvokerNode.getAttributes();
			Attr codeAttr = document.createAttribute("code");
			codeAttr.setValue("org.jboss.invocation.http.server.HttpProxyFactory");
			attributes.setNamedItem(codeAttr);

			Attr nameAttr = document.createAttribute("name");
			nameAttr.setValue("jboss:service=invoker,type=https,target=Naming,readonly=true");
			attributes.setNamedItem(nameAttr);

			// add child nodes (all the attribute-nodes like URLPrefix, URLSuffix, ExportedInterface, etc.)
			Node childNode = document.createElement("attribute");

			childNode.setTextContent("jboss:service=Naming");
			nameAttr = document.createAttribute("name");
			nameAttr.setValue("InvokerName");
			childNode.getAttributes().setNamedItem(nameAttr);
			httpsInvokerNode.appendChild(childNode);

			childNode = document.createElement("attribute");
			childNode.setTextContent("https://");
			nameAttr = document.createAttribute("name");
			nameAttr.setValue("InvokerURLPrefix");
			childNode.getAttributes().setNamedItem(nameAttr);
			httpsInvokerNode.appendChild(childNode);

			childNode = document.createElement("attribute");
			childNode.setTextContent(":"+ servletSSLCf.getSSLPort()+"/invoker/readonly/JMXInvokerServlet");
			nameAttr = document.createAttribute("name");
			nameAttr.setValue("InvokerURLSuffix");
			childNode.getAttributes().setNamedItem(nameAttr);
			httpsInvokerNode.appendChild(childNode);

			childNode = document.createElement("attribute");
			childNode.setTextContent("true");
			nameAttr = document.createAttribute("name");
			nameAttr.setValue("UseHostName");
			childNode.getAttributes().setNamedItem(nameAttr);
			httpsInvokerNode.appendChild(childNode);

			childNode = document.createElement("attribute");
			childNode.setTextContent("org.jnp.interfaces.Naming");
			nameAttr = document.createAttribute("name");
			nameAttr.setValue("ExportedInterface");
			childNode.getAttributes().setNamedItem(nameAttr);
			httpsInvokerNode.appendChild(childNode);

			childNode = document.createElement("attribute");
			nameAttr = document.createAttribute("name");
			nameAttr.setValue("JndiName");
			childNode.getAttributes().setNamedItem(nameAttr);
			httpsInvokerNode.appendChild(childNode);

			childNode = document.createElement("attribute");
			nameAttr = document.createAttribute("name");
			nameAttr.setValue("ClientInterceptors");
			childNode.getAttributes().setNamedItem(nameAttr);
			httpsInvokerNode.appendChild(childNode);

			Node clientInterceptors = document.createElement("interceptors");
			childNode.appendChild(clientInterceptors);

			Node interceptor = document.createElement("interceptor");
			interceptor.setTextContent("org.jboss.proxy.ClientMethodInterceptor");
			clientInterceptors.appendChild(interceptor);

			interceptor = document.createElement("interceptor");
			interceptor.setTextContent("org.jboss.proxy.SecurityInterceptor");
			clientInterceptors.appendChild(interceptor);

			interceptor = document.createElement("interceptor");
			interceptor.setTextContent("org.jboss.naming.interceptors.ExceptionInterceptor");
			clientInterceptors.appendChild(interceptor);

			interceptor = document.createElement("interceptor");
			interceptor.setTextContent("org.jboss.invocation.InvokerInterceptor");
			clientInterceptors.appendChild(interceptor);

			invokerMBeansParent.appendChild(httpsInvokerNode);
			configChanged = true;
		}
		else
		{ // update the invoker declaration to meet the https port specs from the server config.
			NodeList childNodes = httpsInvokerNode.getChildNodes();
			for (int i = 0; i < childNodes.getLength(); i++) {
				final Node childNode = childNodes.item(i);

				if (childNode.getNodeType() == Node.TEXT_NODE || childNode.getNodeType() == Node.COMMENT_NODE)
					continue;

				if (! "InvokerURLSuffix".equals(
						((Attr)childNode.getAttributes().getNamedItem("name")).getValue()))
					continue;

				final String oldURLSuffix = childNode.getTextContent();
				String newUrlSuffix = oldURLSuffix;
				Pattern pattern = Pattern.compile(":(\\d*).*");
				Matcher matcher = pattern.matcher(oldURLSuffix);

				if (! matcher.matches())
				{
					logger.warn("In the URL suffix of the https Invoker no port definition was found! \n"+
							"suffix: "+ oldURLSuffix);
					break;
				}

				int oldPort = Integer.parseInt(matcher.group(1));
				if (oldPort != servletSSLCf.getSSLPort())
				{
					newUrlSuffix = oldURLSuffix.replaceFirst(":(\\d*)", ":"+Integer.toString(servletSSLCf.getSSLPort()));
					configChanged = true;
					childNode.setTextContent(newUrlSuffix);
				}
				break;
			}
		}

		if (configChanged)
		{
			setRebootRequired(true);

			// write modified file
			backup(jbossHttpInvokerServerXml);
			String xmlEncoding = document.getXmlEncoding();
			if(xmlEncoding == null)
				xmlEncoding = "UTF-8";

			FileOutputStream out = new FileOutputStream(jbossHttpInvokerServerXml);
			try {
				NLDOMUtil.writeDocument(document, out, xmlEncoding);
			} finally {
				out.close();
			}
		}
	}

	/**
	 * This method edits the ReadOnlyAccessFilter for servlets to allow read-only access to all
	 * domains and modifies the JNDIFactory servlet to use the read-only invoker.
	 *
	 * @param jbossDeployDir the deploy directory of the JBoss J2EE server.
	 * @throws SAXException
	 * @throws IOException
	 */
	private void configureInvokerWebXml(File jbossDeployDir) throws SAXException, IOException
	{
		final File invokerDeployerDir = new File(jbossDeployDir, "http-invoker.sar").getAbsoluteFile();
		if (! invokerDeployerDir.exists())
		{
			logger.error("Couldn't find the jboss http invoker deployer folder! Assumed to be in" +
					invokerDeployerDir.toString());
			return;
		}

		final File httpInvokerWebXml = new File(new File(new File(invokerDeployerDir, "invoker.war"), "WEB-INF"), "web.xml").getAbsoluteFile();
		if (! httpInvokerWebXml.exists())
		{
			logger.error("Couldn't find the http invoker web.xml! Assumed to be " +
					httpInvokerWebXml.toString());
			return;
		}

		final DOMParser parser = new DOMParser();
		FileInputStream serverXmlStream = new FileInputStream(httpInvokerWebXml);
		try
		{
			parser.parse(new InputSource(serverXmlStream));
		}
		finally
		{
			serverXmlStream.close();
		}
		final Document document = parser.getDocument();

		boolean configChanged = false;
		Collection<Node> filterList = NLDOMUtil.findNodeList(document, "web-app/filter");
		for (Node filter : filterList)
		{
			Node filterNameNode = NLDOMUtil.findElementNode("filter-name", filter);
			if (! "ReadOnlyAccessFilter".equals(filterNameNode.getTextContent()))
				continue;

			Collection<Node> paramValues = NLDOMUtil.findNodeList(filter, "init-param/param-value");
			for (Node paramValue : paramValues) {
				if (! "readonly".equals(paramValue.getTextContent()))
					continue;

				// allow all domains to be read
				paramValue.setTextContent("");
				configChanged = true;
				break;
			}
		}

		Collection<Node> servletList = NLDOMUtil.findNodeList(document, "web-app/servlet");
		Servlets: for (Node servlet : servletList)
		{
			Node servletNameNode = NLDOMUtil.findElementNode("servlet-name", servlet);
			if (! "JNDIFactory".equals(servletNameNode.getTextContent()))
				continue;

			Collection<Node> paramValues = NLDOMUtil.findNodeList(servlet, "init-param/param-value",
					false, false);
			for (Node paramValue : paramValues)
			{
				if (! "jboss:service=invoker,type=http,target=Naming".equals(paramValue.getTextContent()))
					continue;

				paramValue.setTextContent("jboss:service=invoker,type=http,target=Naming,readonly=true");
				configChanged = true;
				break Servlets;
			}
		}

		if (configChanged)
		{
			setRebootRequired(true);

			// write modified file
			backup(httpInvokerWebXml);
			String xmlEncoding = document.getXmlEncoding();
			if(xmlEncoding == null)
				xmlEncoding = "UTF-8";

			FileOutputStream out = new FileOutputStream(httpInvokerWebXml);
			try {
				NLDOMUtil.writeDocument(document, out, xmlEncoding);
			} finally {
				out.close();
			}
		}
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
			modified = adaptHttpsConnector(document, httpsNode, getJFireServerConfigModule());
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

			modified = adaptHttpsConnector(conDoc, httpsConnectorNode, getJFireServerConfigModule());

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
	 *
	 * @return whether the attributes of the given httpsConnector node have been modified.
	 */
	private boolean adaptHttpsConnector(Document document, Node httpsConnector,
			JFireServerConfigModule serverConfigModule)
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

		if (attributes.getNamedItem(HTTPS_CONNECTOR_KEYSTORE_FILE) == null ||
				!((Attr) attributes.getNamedItem(HTTPS_CONNECTOR_KEYSTORE_FILE)).getValue().equals(HTTPS_CONNECTOR_KEYSTORE_FILE_LOCATION))
		{
			Attr keystoreFileAttr = (Attr) attributes.getNamedItem(HTTPS_CONNECTOR_KEYSTORE_FILE);
			if (keystoreFileAttr == null) {
				keystoreFileAttr = document.createAttribute(HTTPS_CONNECTOR_KEYSTORE_FILE);
				attributes.setNamedItem(keystoreFileAttr);
			}

			keystoreFileAttr.setValue(HTTPS_CONNECTOR_KEYSTORE_FILE_LOCATION);
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
	 * We deactivate the JAAS cache, because we have our own cache that is
	 * proactively managed and reflects changes immediately.
	 * Additionally, we extend the transaction timeout to 15 min (default is 5 min).
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


//		// add the custom compression socket mbean service
//		// find if we already wrote the node
//
//		Node jrmp_node = NLDOMUtil.findNodeByAttribute(document, "server/mbean", "name",
//		"jboss:service=invoker,type=jrmp,socketType=CompressionSocketFactory");
//
//		if(jrmp_node == null) {
//
//			changed = true;
//
//			Element root = document.getDocumentElement();
//
//
//			// find the Node JRMP invoker to add our custom invoker before that node
//			jrmp_node = NLDOMUtil.findNodeByAttribute(document, "server/mbean", "code",
//			"org.jboss.invocation.jrmp.server.JRMPInvoker");
//
//			if(jrmp_node == null) {
//				logger.error("MBean attribute node org.jboss.invocation.jrmp.server.JRMPInvoker->code not found");
//			}
//			else
//			{
//				Element newnode = document.createElement("mbean");// Create Root Element
//				newnode.setAttribute("code", "org.jboss.invocation.jrmp.server.JRMPInvoker");
//				newnode.setAttribute("name", "jboss:service=invoker,type=jrmp,socketType=CompressionSocketFactory");
//
//
//				Comment comment = document.createComment("Add the Custom Compression SSL Socket mbean invoker");
//				newnode.appendChild(comment);
//
//				Element item = document.createElement("attribute");       // Create element
//				item.setAttribute("name", "RMIObjectPort");
//				item.appendChild( document.createTextNode("24445") );
//				newnode.appendChild( item );
//
//				item = document.createElement("attribute");       // Create element
//				item.setAttribute("name", "RMIClientSocketFactory");
//				item.appendChild( document.createTextNode("org.nightlabs.rmissl.socket.SSLCompressionRMIClientSocketFactory") );
//				newnode.appendChild( item );
//
//				item = document.createElement("attribute");       // Create element
//				item.setAttribute("name", "RMIServerSocketFactory");
//				item.appendChild( document.createTextNode("org.nightlabs.rmissl.socket.SSLCompressionRMIServerSocketFactory") );
//				newnode.appendChild( item );
//
//
//				root.insertBefore(newnode,jrmp_node);
//
//				logger.info("Added Custom MBean Invoker for the JRMP Compression invoker");
//			}
//
//		}

		// JAAS TIMEOUT
		changed = replaceMBeanAttribute(
				document,
				"org.jboss.security.plugins.JaasSecurityManagerService",
				"DefaultCacheTimeout",
				" " +
						modificationMarker + "\n " +
						ServerConfiguratorJBoss.class.getName() + " has reduced the JAAS cache timeout to 5 min.\n" +
						" JFire has its own cache, which is updated immediately. We cannot completely deactivate the JAAS cache, however,\n" +
						" because that causes JPOX bugs (why?!).\n Marco :-) ",
				"300") || needRestart;
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
						ServerConfiguratorJBoss.class.getName() + " has increased the transaction timeout to 15 min. ",
				"900") || needRestart;
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

	/**
	 * *** work necessary for NightLabsCascadedAuthenticationJBoss ***
	 * check/modify ${jboss.conf}/standardjboss.xml and REBOOT if changes occured
	 *
	 * @param jbossConfDir The JBoss config dir
	 * @throws FileNotFoundException If the file eas not found
	 * @throws IOException In case of an io error
	 * @throws TransformerException
	 */
	private void configureStandardJBossXml(File jbossConfDir) throws FileNotFoundException, IOException, SAXException, TransformerException
	{
		boolean backupDone = false; // track whether the backup is already done - we don't need 2 backups for this one method.
		File destFile = new File(jbossConfDir, "standardjboss.xml");
		String text = IOUtil.readTextFile(destFile);

		//check if we already had the stateless container for the SSL Compression Invoker
		if (text.indexOf("<name>stateless-compression-invoker</name>") < 0)
		{
			logger.info("configureStandardJBossXml: Will add Compression SSL invoker to file: " + destFile.getAbsolutePath());

			if (!backupDone) {
				backup(destFile);
				backupDone = true;
			}

			setRebootRequired(true); // this is a must, because the conf directory doesn't support redeployment

			DOMParser parser = new DOMParser();
			InputStream in = new FileInputStream(destFile);
			try {
				parser.parse(new InputSource(in));
			} finally {
				in.close();
			}
			Document document = parser.getDocument();

			//configure the ssl compression invoker

			Node root = document.getDocumentElement();

			Node proxynode = NLDOMUtil.findSingleNode(root, "invoker-proxy-bindings");;

			Element newnode = document.createElement("invoker-proxy-binding");// Create Root Element

			Comment comment = document.createComment("Add the Custom Compression SSL Socket proxy Bindings");
			newnode.appendChild(comment);

			Element item = document.createElement("name");       // Create element
			item.appendChild( document.createTextNode("stateless-compression-invoker") );
			newnode.appendChild( item );

			item = document.createElement("invoker-mbean");       // Create element
			item.appendChild( document.createTextNode("jboss:service=invoker,type=jrmp,socketType=CompressionSocketFactory") );
			newnode.appendChild( item );

			item = document.createElement("proxy-factory");       // Create element
			item.appendChild( document.createTextNode("org.jboss.proxy.ejb.ProxyFactory") );
			newnode.appendChild( item );


			item = document.createElement("proxy-factory-config");

			Element item_sub = document.createElement("client-interceptors");

			// add the home tag

			Element item_sub1 = document.createElement("home");

			Element item_value = document.createElement("interceptor");
			item_value.appendChild( document.createTextNode("org.jboss.proxy.ejb.HomeInterceptor") );
			item_sub1.appendChild(item_value);

			item_value = document.createElement("interceptor");
			item_value.appendChild( document.createTextNode("org.jboss.proxy.SecurityInterceptor") );
			item_sub1.appendChild(item_value);

			item_value = document.createElement("interceptor");
			item_value.appendChild( document.createTextNode("org.jboss.proxy.TransactionInterceptor") );
			item_sub1.appendChild(item_value);

			item_value = document.createElement("interceptor");
			item_value.appendChild( document.createTextNode("org.jboss.invocation.InvokerInterceptor") );
			item_sub1.appendChild(item_value);


			item_sub.appendChild(item_sub1);


			// add the bean tag

			item_sub1 = document.createElement("bean");

			item_value = document.createElement("interceptor");
			item_value.appendChild( document.createTextNode("org.jboss.proxy.ejb.StatelessSessionInterceptor") );
			item_sub1.appendChild(item_value);

			item_value = document.createElement("interceptor");
			item_value.appendChild( document.createTextNode("org.jboss.proxy.SecurityInterceptor") );
			item_sub1.appendChild(item_value);

			item_value = document.createElement("interceptor");
			item_value.appendChild( document.createTextNode("org.jboss.proxy.TransactionInterceptor") );
			item_sub1.appendChild(item_value);

			item_value = document.createElement("interceptor");
			item_value.appendChild( document.createTextNode("org.jboss.invocation.InvokerInterceptor") );
			item_sub1.appendChild(item_value);

			item_sub.appendChild(item_sub1);


			item.appendChild( item_sub );

			newnode.appendChild( item );

			proxynode.appendChild(newnode);


			FileOutputStream out = new FileOutputStream(destFile);
			try {
				NLDOMUtil.writeDocument(
						document,
						out,
						"UTF-8","-//JBoss//DTD JBOSS 4.0//EN",
						"http://www.jboss.org/j2ee/dtd/jboss_4_0.dtd"
				);
			} finally {
				out.close();
			}
		}


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
	}

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
		javaOpts += " -XX:PermSize=64m -XX:MaxPermSize=128m";

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
				new File(new File(jbossDeployDir, "jboss-web.deployer"), "server.xml"),
				new File(new File(new File(jbossDeployDir, "http-invoker.sar"), "META-INF"), "jboss-service.xml"),
				new File(new File(new File(new File(jbossDeployDir, "http-invoker.sar"), "invoker.war"), "WEB-INF"), "web.xml")
		};

		try {
			for (File f : filesToRestore)
				restore(f);

			new File(jbossBinDir, "CascadedAuthenticationClientInterceptor.properties").delete();
		} catch (IOException e) {
			throw new ServerConfigurationException(e);
		}
	}
}
