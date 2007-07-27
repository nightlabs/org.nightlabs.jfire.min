package org.nightlabs.jfire.jboss.serverconfigurator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.nightlabs.annotation.Implement;
import org.nightlabs.jfire.jboss.authentication.JFireServerLocalLoginModule;
import org.nightlabs.jfire.jboss.authentication.JFireServerLoginModule;
import org.nightlabs.jfire.jboss.cascadedauthentication.CascadedAuthenticationClientInterceptor;
import org.nightlabs.jfire.serverconfigurator.ServerConfigurationException;
import org.nightlabs.jfire.serverconfigurator.ServerConfigurator;
import org.nightlabs.jfire.servermanager.config.SMTPMailServiceCf;
import org.nightlabs.util.IOUtil;
import org.nightlabs.xml.DOMParser;
import org.nightlabs.xml.NLDOMUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
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

	private static File getNonExistingFile(String pattern)
	{
		if(pattern == null)
			throw new NullPointerException("pattern is null");
		synchronized(pattern) {
			int idx = 1;
			File f;
			do {
				f = new File(String.format(pattern, idx));
				idx++;
			} while(f.exists());
			return f;
		}
	}
	
	protected static File backup(File f) throws IOException
	{
		if(!f.exists() || !f.canRead())
			throw new FileNotFoundException("Invalid file to backup: "+f);
		File backupFile = new File(f.getAbsolutePath()+".bak");
		if(backupFile.exists())
			backupFile = getNonExistingFile(f.getAbsolutePath()+".%d.bak");
		IOUtil.copyFile(f, backupFile);
		logger.info("Created backup of file "+f.getAbsolutePath()+": "+backupFile.getName());
		return backupFile;
	}

	protected static File moveToBackup(File f) throws IOException
	{
		if(!f.exists())
			throw new FileNotFoundException("Invalid file to backup: "+f);
		File backupFile = new File(f.getAbsolutePath()+".bak");
		if(backupFile.exists())
			backupFile = getNonExistingFile(f.getAbsolutePath()+".%d.bak");
		if(!f.renameTo(backupFile))
			throw new IOException("Renaming file "+f.getAbsolutePath()+" to "+f.getName()+" failed");
		return backupFile;
	}

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
	@Implement
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
			configureJavaOpts(jbossBinDir);
			removeUnneededFiles(jbossDeployDir);

		} catch(Exception e) {
			throw new ServerConfigurationException("Server configuration failed in server configurator "+getClass().getName(), e);
		}
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
		File destFile = new File(jbossBinDir, "CascadedAuthenticationClientInterceptor.properties");
		if (!destFile.exists()) {
			logger.info("File " + destFile.getAbsolutePath() + " does not exist. Will create it with enable=yes.");
			Properties props = new Properties();
			props.put("enable", "yes");
			FileOutputStream out = new FileOutputStream(destFile);
			try {
				props.store(out, "Automatically created by " + this.getClass().getName());
			} finally {		
				out.close();
			}
			CascadedAuthenticationClientInterceptor.reloadProperties(); // reboot should not be necessary anymore after this extension
		}
	}
	
	private static void setMBeanAttribute(Document document, String mbeanCode, String attributeName, String comment, String content)
	{
		Node attributeNode = getMBeanAttributeNode(document, mbeanCode, attributeName);
		if(attributeNode != null)
			NLDOMUtil.setTextContentWithComment(attributeNode, comment, content);
	}

	private static Node getMBeanAttributeNode(Document document, String mbeanCode, String attributeName)
	{
		Node mbeanNode = NLDOMUtil.findNodeByAttribute(document, "server/mbean", "code", mbeanCode);
		if(mbeanNode == null) {
			logger.error("mbean node not found for code=\""+mbeanCode+"\"");
			return null;
		}
		Node attributeNode = NLDOMUtil.findNodeByAttribute(mbeanNode, "attribute", "name", attributeName);
		if(attributeNode == null) {
			logger.error("attribute node not found for name=\""+attributeName+"\"");
			return null;
		}
		return attributeNode;
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
		String text = IOUtil.readTextFile(destFile);
		String modificationMarker = "!!!ModifiedByJFire!!!";
		if (text.indexOf(modificationMarker) >= 0)
			return;
			
		backup(destFile);

		logger.info("File " + destFile.getAbsolutePath() + " was not yet updated. Will increase transaction timeout and reduce JAAS cache timeout to 5 min - we cannot deactivate the JAAS cache completely or reduce the timeout further, because that causes JPOX problems (though I don't understand why).");
		setRebootRequired(true);
		
		DOMParser parser = new DOMParser();
		parser.parse(new InputSource(new FileInputStream(destFile)));
		Document document = parser.getDocument();
		
		// JAAS TIMEOUT
		setMBeanAttribute(
				document, 
				"org.jboss.security.plugins.JaasSecurityManagerService", 
				"DefaultCacheTimeout", 
				" " + 
						modificationMarker + "\n " +
						ServerConfiguratorJBoss.class.getName() + " has reduced the JAAS cache timeout to 5 min.\n" +
						" JFire has its own cache, which is updated immediately. We cannot completely deactivate the JAAS cache, however,\n" +
						" because that causes JPOX bugs (why?!).\n Marco :-) ", 
				"300");
		
		// TRANSACTION TIMEOUT
		setMBeanAttribute(
				document, 
				"org.jboss.tm.TransactionManagerService", 
				"TransactionTimeout", 
				" " + 
						modificationMarker + "\n " +
						ServerConfiguratorJBoss.class.getName() + " has increased the transaction timeout to 15 min. ", 
				"900");
		
		// IGNORE SUFFIX
		Node n = getMBeanAttributeNode(document, "org.jboss.deployment.scanner.URLDeploymentScanner", "FilterInstance");
		Node p = NLDOMUtil.findNodeByAttribute(n, "property", "name", "suffixes");
		if(p == null) {
			Element newPropertyElement = document.createElement("property");
			newPropertyElement.setAttribute("name", "suffixes");
			newPropertyElement.setTextContent("#,$,%,~,\\,v,.BAK,.bak,.old,.orig,.tmp,.rej,.sh");
			n.appendChild(newPropertyElement);
			p = newPropertyElement;
		}
		String oldText = p.getTextContent();
		String newText = oldText+",-clrepository.xml";
		NLDOMUtil.setTextContentWithComment(
				p, 
				" " + 
						modificationMarker + "\n         " +
						ServerConfiguratorJBoss.class.getName() + " has added -clrepository.xml ",
				newText);
			
		
		
		String xmlEncoding = document.getXmlEncoding();
		if(xmlEncoding == null)
			xmlEncoding = "UTF-8";
		NLDOMUtil.writeDocument(document, new FileOutputStream(destFile), xmlEncoding);
			
			/*

			Pattern pattern = Pattern.compile(
					"(<mbean[^>]*?org\\.jboss.security\\.plugins\\.JaasSecurityManagerService(?:\\n|.)*?<attribute +?name *?= *?\"DefaultCacheTimeout\")>[0-9]*<((?:\\n|.)*?</mbean>)"
					);
			text = pattern.matcher(text).replaceAll(
					"<!-- " + modificationMarker + "\n " +
					ServerConfiguratorJBoss.class.getName() + " has reduced the JAAS cache timeout to 5 min.\n" +
					" JFire has its own cache, which is updated immediately. We cannot completely deactivate the JAAS cache, however,\n" +
					" because that causes JPOX bugs (why?!).\n Marco :-)\n-->\n" +
					"   $1>300<$2"
					);

// IMHO 60 is the default - Marco.
//			pattern = Pattern.compile(
//					"(<mbean[^>]*?org\\.jboss\\.security\\.plugins\\.JaasSecurityManagerService(?:\\n|.)*?<attribute +?name *?= *?\"DefaultCacheResolution\")>[0-9]*<((?:\\n|.)*?</mbean>)"
//					);
//			text = pattern.matcher(text).replaceAll("$1>60<$2");

			pattern = Pattern.compile(
					"(<mbean[^>]*?org\\.jboss\\.tm\\.TransactionManagerService(?:\\n|.)*?<attribute +?name *?= *?\"TransactionTimeout\")>[0-9]*<((?:\\n|.)*?</mbean>)"
					);
			text = pattern.matcher(text).replaceAll(
					"<!-- " + modificationMarker + "\n " +
					ServerConfiguratorJBoss.class.getName() + " has increased the transaction timeout to 15 min.\n-->\n" +
					"   $1>900<$2"
					);

			
			pattern = Pattern.compile("(<property\\s+name\\s*=\\s*\"suffixes\">)([^<]*)(</property>)");
			text = pattern.matcher(text).replaceAll(
					"<!-- " + modificationMarker + "\n         " +
					ServerConfiguratorJBoss.class.getName() + " has added -clrepository.xml\n         -->\n" +
					"         $1$2,-clrepository.xml$3");
			
			
			Utils.writeTextFile(destFile, text);
			*/
	}

	/**
	 * *** work necessary for NightLabsCascadedAuthenticationJBoss ***
	 * check/modify ${jboss.conf}/standardjboss.xml and REBOOT if changes occured
	 * 
	 * @param jbossConfDir The JBoss config dir
	 * @throws FileNotFoundException If the file eas not found
	 * @throws IOException In case of an io error
	 */
	private void configureStandardJBossXml(File jbossConfDir) throws FileNotFoundException, IOException
	{
		File destFile = new File(jbossConfDir, "standardjboss.xml");
		String text = IOUtil.readTextFile(destFile);
		if (text.indexOf(CascadedAuthenticationClientInterceptor.class.getName()) < 0) {
			
			backup(destFile);
			
			logger.info("File " + destFile.getAbsolutePath() + " does not contain an interceptor registration for "+CascadedAuthenticationClientInterceptor.class.getName()+". Will add it.");
			
			setRebootRequired(true); // this is a must, because the conf directory doesn't support redeployment
			String replacementText = "$1\n            <interceptor>"+CascadedAuthenticationClientInterceptor.class.getName()+"</interceptor>";

			Pattern pattern = Pattern.compile("(<client-interceptors>[^<]*?<home>)");
			text = pattern.matcher(text).replaceAll(replacementText);			

			pattern = Pattern.compile("(<client-interceptors>[^<]*?<home>(.|\\n)*?</home>[^<]*?<bean>)");
			text = pattern.matcher(text).replaceAll(replacementText);

			IOUtil.writeTextFile(destFile, text);
		}
	}

	private void configureMailServiceXml(File jbossDeployDir) throws FileNotFoundException, IOException, SAXException
	{
		File destFile = new File(jbossDeployDir, "mail-service.xml");
		String text = IOUtil.readTextFile(destFile);
	//	String modificationMarker = "!!!ModifiedByJFire!!!";
	//	if (text.indexOf(modificationMarker) >= 0)
	//		return;
			
		backup(destFile);
		logger.info("File " + destFile.getAbsolutePath() + " was not yet updated. Will change SMTP settings.");
		//  No reboot necessary as JBoss will automaticly notice any changes on the mail-services.xml
		setRebootRequired(false);
		
		DOMParser parser = new DOMParser();
		parser.parse(new InputSource(new FileInputStream(destFile)));
		Document document = parser.getDocument();
		
		//document.getDocumentElement().appendChild(document.createComment(modificationMarker));
		
		SMTPMailServiceCf smtp = getJFireServerConfigModule().getSmtp();
		Node propertyElement;
	
		System.out.println("Password: "+ smtp.getPassword());
		System.out.println("Username: "+ smtp.getUsername());
		System.out.println("UsAuthentication: "+ smtp.getUseAuthentication());
		System.out.println("Host: "+smtp.getHost());
		System.out.println("Port: "+String.valueOf(smtp.getPort()));
		System.out.println("From: "+smtp.getMailFrom());
		System.out.println("Debug: "+String.valueOf(smtp.getDebug()));
		
		propertyElement = NLDOMUtil.findNodeByAttribute(document, "server/mbean/attribute", "name", "User");
		
		/*
		}
		if(propertyElement.getFirstChild() == null) {
			Node textNode = document.createTextNode(smtp.getUsername());
			propertyElement.appendChild(textNode);
		} else propertyElement.getFirstChild().setNodeValue(smtp.getUsername());
		
		propertyElement = NLDOMUtil.findNodeByAttribute(document, "server/mbean/attribute", "name", "Password");
		if(propertyElement.getFirstChild() == null) {
			Node textNode = document.createTextNode(smtp.getPassword());
			propertyElement.appendChild(textNode);
		} else propertyElement.getFirstChild().setNodeValue(smtp.getPassword());
		*/
		propertyElement = NLDOMUtil.findNodeByAttribute(document, "server/mbean/attribute/configuration/property", "name", "mail.smtp.host");
		propertyElement.getAttributes().getNamedItem("value").setNodeValue(smtp.getHost());
		
		propertyElement = NLDOMUtil.findNodeByAttribute(document, "server/mbean/attribute/configuration/property", "name", "mail.smtp.port");
		propertyElement.getAttributes().getNamedItem("value").setNodeValue(String.valueOf(smtp.getPort()));
		
		propertyElement = NLDOMUtil.findNodeByAttribute(document, "server/mbean/attribute/configuration/property", "name", "mail.from");
		propertyElement.getAttributes().getNamedItem("value").setNodeValue(smtp.getMailFrom());
		
		propertyElement = NLDOMUtil.findNodeByAttribute(document, "server/mbean/attribute/configuration/property", "name", "mail.debug");
		propertyElement.getAttributes().getNamedItem("value").setNodeValue(String.valueOf(smtp.getDebug()));
		
		FileOutputStream out = new FileOutputStream(destFile);
		try {
			NLDOMUtil.writeDocument(document, out, "UTF-8");
		} finally {
			out.close();
		}
	}
	
	private void configureJBossjtaPropertiesXml(File jbossConfDir)
	throws FileNotFoundException, IOException
	{
		File destFile = new File(jbossConfDir, "jbossjta-properties.xml");
		if (!destFile.exists()) {
			logger.info("The JTA configuration file \""+ destFile.getAbsolutePath() +"\" does not exist. Assuming that this is JBoss is older than 4.2.0.GA and not updating the file!");
			return;
		}

		String text = IOUtil.readTextFile(destFile);
		if (text.indexOf("com.arjuna.ats.jta.allowMultipleLastResources") < 0) {
			backup(destFile);
			logger.info("File " + destFile.getAbsolutePath() + " does not contain property \"com.arjuna.ats.jta.allowMultipleLastResources\". Will add it.");
			setRebootRequired(true); // I'm not sure whether the arjuna JTA controller would be reinitialised... this is at least safe.

			Pattern pattern = Pattern.compile("(<properties depends=\"arjuna\" name=\"jta\">)");
			String replacementText = "$1\n        <property name=\"com.arjuna.ats.jta.allowMultipleLastResources\" value=\"true\"/>";
			text = pattern.matcher(text).replaceAll(replacementText);

			IOUtil.writeTextFile(destFile, text);
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
		
		// --- TODO: for backwards compatability only - remove in later versions:
		String rmiHost = serverConfiguratorSettings == null ? null : serverConfiguratorSettings.getProperty("java.rmi.server.hostname");
		if (rmiHost != null) {
			javaOpts = "-Djava.rmi.server.hostname="+rmiHost+" "+javaOpts;
			// issue #87:
			javaOpts += " -Djava.rmi.server.useLocalHostname=true";
		}
		// ---

		// issue #58:
		javaOpts += " -XX:PermSize=64m -XX:MaxPermSize=128m";
		
		configureRunConf(jbossBinDir, javaOpts);
		configureRunBat(jbossBinDir, javaOpts);
	}
	
	private void configureRunConf(File jbossBinDir, String javaOpts) throws FileNotFoundException, IOException
	{
		String optsBegin = "# JAVA_OPTS by JFire server configurator\nJAVA_OPTS=\"$JAVA_OPTS ";
		String optsEnd = "\"";
		Pattern oldOpts = Pattern.compile(
				"^"+Pattern.quote(optsBegin)+"([^\"]*)"+Pattern.compile(optsEnd)+"$", 
				Pattern.MULTILINE);
		
		File destFile = new File(jbossBinDir, "run.conf");
		String text = IOUtil.readTextFile(destFile);

		String newSetting = optsBegin+javaOpts+optsEnd+"\n";
		Matcher m = oldOpts.matcher(text);
		boolean changed = false;
		boolean found = m.find();
		if(found && !m.group(1).equals(javaOpts)) {
			text = m.replaceFirst(Matcher.quoteReplacement(newSetting));
			changed = true;
		} else if(!found) {
			text += "\n"+newSetting;
			changed = true;
		}
		if(changed) {
			setRebootRequired(true);
			backup(destFile);
			IOUtil.writeTextFile(destFile, text);
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
}
