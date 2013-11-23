/* *****************************************************************************
 * JFire - it's hot - Free ERP System - http://jfire.org                       *
 * Copyright (C) 2004-2005 NightLabs - http://NightLabs.org                    *
 *                                                                             *
 * This library is free software; you can redistribute it and/or               *
 * modify it under the terms of the GNU Lesser General Public                  *
 * License as published by the Free Software Foundation; either                *
 * version 2.1 of the License, or (at your option) any later version.          *
 *                                                                             *
 * This library is distributed in the hope that it will be useful,             *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of              *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU           *
 * Lesser General Public License for more details.                             *
 *                                                                             *
 * You should have received a copy of the GNU Lesser General Public            *
 * License along with this library; if not, write to the                       *
 *     Free Software Foundation, Inc.,                                         *
 *     51 Franklin St, Fifth Floor,                                            *
 *     Boston, MA  02110-1301  USA                                             *
 *                                                                             *
 * Or get it online :                                                          *
 *     http://opensource.org/licenses/lgpl-license.php                         *
 *                                                                             *
 *                                                                             *
 ******************************************************************************/

package org.nightlabs.jfire.servermanager.xml;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.apache.log4j.Logger;
import org.nightlabs.util.CollectionUtil;
import org.nightlabs.util.IOUtil;
import org.nightlabs.xml.DOMParser;
import org.nightlabs.xml.NLDOMUtil;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;


/**
 * This class manages application.xml resources form EARs. It reads additional information
 * from web.xml or ejb-jar.xml by using WEBMan or EJBJarMan.
 *
 * @author marco
 */
public class EARApplication
{
	/**
	 * LOG4J logger used by this class
	 */
	private static final Logger logger = Logger.getLogger(EARApplication.class);

	private SAXParseException parseException = null;

	private File ear;
	private EARModuleType[] moduleTypes;

	public EARApplication(File ear, EARModuleType ... moduleTypes)
	throws XMLReadException
	{
		logger.debug("Creating instance of EARApplication for ear \""+ear.getAbsolutePath()+"\".");
		this.ear = ear;
		this.moduleTypes = moduleTypes;
		try {

			JarFile earJarFile = null;
			InputStream applicationXmlInputStream = null;
			try {
				if (!ear.isDirectory()) {
					earJarFile = new JarFile(ear);
					JarEntry jarEntry = (JarEntry) earJarFile.getEntry("META-INF/application.xml");
					if (jarEntry == null)
						throw new IllegalStateException("EAR-JAR-file \"" + ear.getAbsolutePath() + "\" does not contain \"META-INF/application.xml\"!");

					applicationXmlInputStream = earJarFile.getInputStream(jarEntry);
				}
				else {
					File file = new File(ear, "META-INF/application.xml");
					if (!file.exists())
						throw new FileNotFoundException("File \"META-INF/application.xml\" cannot be found within EAR \""+ear.getAbsolutePath()+"\"!");

					applicationXmlInputStream = new FileInputStream(file);
				}

				InputSource inputSource = new InputSource(applicationXmlInputStream);
				DOMParser parser = new DOMParser();
//				// list of features: http://xml.apache.org/xerces2-j/features.html
//				// We must deactivate checking, because we might be offline!
//				parser.setFeature("http://xml.org/sax/features/validation", false);
				parser.setErrorHandler(new ErrorHandler(){
					public void error(SAXParseException exception) throws SAXException {
						logger.error("Parse (application.xml): ", exception);
						parseException = exception;
					}

					public void fatalError(SAXParseException exception) throws SAXException {
						logger.fatal("Parse (application.xml): ", exception);
						parseException = exception;
					}

					public void warning(SAXParseException exception) throws SAXException {
						logger.warn("Parse (application.xml): ", exception);
					}
				});
				parser.parse(inputSource);
				if (parseException != null)
					throw parseException;

				XPath xPath = XPathFactory.newInstance().newXPath();
				xPath.setNamespaceContext(new NamespaceContext() {
					@Override
					public String getNamespaceURI(String prefix) {
						if (prefix == null) throw new IllegalArgumentException("prefix must not be null");
		        if ("jee".equals(prefix)) return "http://java.sun.com/xml/ns/javaee";
		        return XMLConstants.NULL_NS_URI;
					}
					@Override
					public String getPrefix(String namespaceURI) {
						throw new UnsupportedOperationException();
					}
					@Override
					public @SuppressWarnings("unchecked") Iterator getPrefixes(String namespaceURI) {
						throw new UnsupportedOperationException();
					}
				});

				for (EARModuleType moduleType : moduleTypes) {
					if (EARModuleType.web.equals(moduleType)) {
						NodeList nodes = (NodeList) xPath.evaluate("//jee:application/jee:module/jee:web", parser.getDocument(), XPathConstants.NODESET);
						for (int i = 0; i < nodes.getLength(); ++i) {
							Node n = nodes.item(i);
							ModuleDef moduleDef = new ModuleDef(moduleType);
							Node nWebUri = NLDOMUtil.findSingleNode(n, "web-uri");
							Node nContextRoot = NLDOMUtil.findSingleNode(n, "context-root");
							moduleDef.setResourceURI(nWebUri.getTextContent().trim());
							moduleDef.setContextPath(nContextRoot.getTextContent().trim());
							readWebXML(ear, earJarFile, moduleDef);
							modules.add(moduleDef);
						}
					}
					else if (EARModuleType.ejb.equals(moduleType)) {
						NodeList nodes = (NodeList) xPath.evaluate("//jee:application/jee:module/jee:ejb", parser.getDocument(), XPathConstants.NODESET);
						for (int i = 0; i < nodes.getLength(); ++i) {
							Node n = nodes.item(i);
							String ejbJar = n.getTextContent().trim();
							ModuleDef moduleDef = new ModuleDef(moduleType);
							moduleDef.setResourceURI(ejbJar);
							modules.add(moduleDef);
						}
					}
					else
						throw new UnsupportedOperationException("ModuleType \""+moduleType+"\" not yet supported!");
				}

			} finally {
				if (applicationXmlInputStream != null)
					applicationXmlInputStream.close();

				if (earJarFile != null)
					earJarFile.close();
			}
		} catch (Exception x) {
			throw new XMLReadException(x);
		}
	}

	protected void readWebXML(File ear, JarFile earJarFile, ModuleDef moduleDef)
		throws XMLReadException, IOException
	{
		if (moduleDef.getResourceURI() == null)
			throw new IllegalArgumentException("Property moduleDef.resourceURI of EAR \""+ear.getAbsolutePath()+"\" is null!");

		if (earJarFile != null) {
			JarEntry moduleJarEntry = (JarEntry) earJarFile.getEntry(moduleDef.getResourceURI());
			if (moduleJarEntry == null)
				throw new IllegalStateException("EAR-JAR-file \"" + ear.getAbsolutePath() + "\" does not contain \"" + moduleDef.getResourceURI() + "\" referenced in \"META-INF/application.xml\"!");

			JarInputStream moduleJarInputStream = new JarInputStream(earJarFile.getInputStream(moduleJarEntry));
			try {
				JarEntry je;
				while (null != (je = moduleJarInputStream.getNextJarEntry())) {
					if ("WEB-INF/web.xml".equals(je.getName())) {
						WEBMan webMan = new WEBMan(moduleJarInputStream);
						moduleDef.setName(webMan.getDisplayName());
					}
					moduleJarInputStream.closeEntry();
				}
			} finally {
				moduleJarInputStream.close();
			}
		}
		else {
			File moduleFile = new File(ear, moduleDef.getResourceURI());
			if (!moduleFile.exists())
				throw new FileNotFoundException("moduleDef.resourceURI of EAR \""+ear.getAbsolutePath()+"\" not correct: File \""+moduleFile.getAbsolutePath()+"\" does not exist!");

			if (!moduleFile.isFile())
				throw new UnsupportedOperationException("EAR \""+ear.getAbsolutePath()+"\": moduleDef.resourceURI \""+moduleFile.getAbsolutePath()+"\" does not point to a file! Only jars are allowed here!");

			JarFile jf = new JarFile(moduleFile);
			JarEntry jeWebXML = jf.getJarEntry("WEB-INF/web.xml");
			if (jeWebXML == null) {
				logger.warn("Jar \""+moduleFile.getCanonicalPath()+"\" does not contain \"WEB-INF/web.xml\"!");
			}
			InputStream webXmlInputStream = jf.getInputStream(jeWebXML);
			try {
				WEBMan webMan = new WEBMan(webXmlInputStream);
				moduleDef.setName(webMan.getDisplayName());
			} finally {
				webXmlInputStream.close();
			}
		}
	}

	/**
	 * The directory or file of the EAR.
	 *
	 * @return the directory or file of the EAR.
	 */
	public File getEar() {
		return ear;
	}

	/**
	 * @return Returns the moduleType.
	 */
	public EARModuleType[] getModuleTypes() {
		return moduleTypes;
	}

	private Collection<ModuleDef> modules = new ArrayList<ModuleDef>();

	/**
	 * @return Returns the modules.
	 */
	public Collection<ModuleDef> getModules() {
		return Collections.unmodifiableCollection(modules);
	}

	public void handleJarEntries(String[] jarEntryNames, JarEntryHandler[] jarEntryHandlers)
	{
		if (jarEntryNames == null)
			throw new IllegalArgumentException("jarEntryNames must not be null!");

		if (jarEntryHandlers == null)
			throw new IllegalArgumentException("jarEntryHandlers must not be null!");

		Set<String> jarEntryNameSet = null;

		if (!ear.isDirectory()) {
			try {
				JarFile earJarFile = new JarFile(ear);
				try {
					for (ModuleDef moduleDef : modules) {
						JarEntry moduleJarEntry = (JarEntry) earJarFile.getEntry(moduleDef.getResourceURI());
						if (moduleJarEntry == null)
							throw new IllegalStateException("EAR-JAR-file \"" + ear.getAbsolutePath() + "\" does not contain \"" + moduleDef.getResourceURI() + "\" referenced in \"META-INF/application.xml\"!");

						JarInputStream moduleJarInputStream = new JarInputStream(earJarFile.getInputStream(moduleJarEntry));
						try {
							JarEntry je;
							while (null != (je = moduleJarInputStream.getNextJarEntry())) {
								if (jarEntryNameSet == null)
									jarEntryNameSet = CollectionUtil.array2HashSet(jarEntryNames);

								if (jarEntryNameSet.contains(je.getName())) {
									ByteArrayOutputStream bout = new ByteArrayOutputStream();
									IOUtil.transferStreamData(moduleJarInputStream, bout);
									bout.close();
									byte[] buffer = bout.toByteArray();

									for (JarEntryHandler jarEntryHandler : jarEntryHandlers) {
										InputStream in = new ByteArrayInputStream(buffer);
										try {
											jarEntryHandler.handleJarEntry(this, moduleDef.getResourceURI(), in);
										} finally {
											in.close();
										}
									}
								}
								moduleJarInputStream.closeEntry();
							}
						} finally {
							moduleJarInputStream.close();
						}
					}
				} finally {
					earJarFile.close();
				}
			} catch (Exception x) {
				logger.error("Reading from EAR-JAR '"+ear.getAbsolutePath()+"' failed!", x);
			}
		}
		else {
//			File[] jars = ear.listFiles(jarFileFilter);
//			for (int m = 0; m < jars.length; ++m) {
//				File jar = jars[m];
			for (ModuleDef moduleDef : modules) {
				File jar = new File(ear, moduleDef.getResourceURI());
				try {
					if (!jar.exists())
						throw new IllegalStateException("EAR-directory \"" + ear.getAbsolutePath() + "\" does not contain \"" + moduleDef.getResourceURI() + "\" referenced in \"META-INF/application.xml\"!");

					JarFile jf = new JarFile(jar);
					try {
						JarEntry je = null;
						for (String jarEntryName : jarEntryNames) {
							je = jf.getJarEntry(jarEntryName);
							if (je != null)
								break;
						}

						if (je != null) {
							for (JarEntryHandler jarEntryHandler : jarEntryHandlers) {
								InputStream in = jf.getInputStream(je);
								try {
									jarEntryHandler.handleJarEntry(this, jar.getName(), in);
								} finally {
									in.close();
								}
							}
						} // if (je != null) {
					} finally {
						jf.close();
					}
				} catch (Exception e) {
					logger.error("Reading from JAR '"+jar.getAbsolutePath()+"' failed!", e);
				}
			}
		}
	}
}
