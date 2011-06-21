package org.nightlabs.jfire.serverupdate.launcher.config;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.nightlabs.jfire.serverupdate.launcher.Log;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class ServerUpdateConfig
{
	public ServerUpdateConfig()
	throws SAXException, IOException, ParserConfigurationException
	{
		File configFile = new File("update_server.xml");
		Log.info("Loading configuration file: %s", configFile.getAbsolutePath());
		if (!configFile.exists())
			throw new IllegalStateException("Config file does not exist: " + configFile.getAbsolutePath());

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document doc = builder.parse(configFile);
		Element configurations = doc.getDocumentElement();
		NodeList nodeList = configurations.getChildNodes();

		int numClasspath = 0;

		List<Directory> classpath = new ArrayList<Directory>();
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node node = nodeList.item(i);
			if (node.getNodeName().equals("classpath")) {
				numClasspath++;
				Directory directory = new Directory(node);

				if (Log.isDebugEnabled())
					Log.debug("Classpath " + numClasspath + ": " + directory.getFile().getAbsolutePath() + ", exist: " + directory.getFile().exists());

				classpath.add(directory);
			}
		}
		this.classpath = Collections.unmodifiableList(classpath);

		int numdeploymentDirectory = 0;
		List<Directory> deploymentDirectories = new ArrayList<Directory>();
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node node = nodeList.item(i);
			if (node.getNodeName().equals("deployment")) {
				numdeploymentDirectory++;

				Directory directory = new Directory(node);

				if (Log.isDebugEnabled())
					Log.debug("Deployment " + numdeploymentDirectory + ": " + directory.getFile().getAbsolutePath() + ", exist: " + directory.getFile().exists());

				deploymentDirectories.add(directory);
			}
		}
		this.deploymentDirectories = Collections.unmodifiableList(deploymentDirectories);
	}

	private List<Directory> deploymentDirectories;

	public List<Directory> getDeploymentDirectories() {
		return deploymentDirectories;
	}

	private List<Directory> classpath;

	public List<Directory> getClasspath() {
		return classpath;
	}
}
