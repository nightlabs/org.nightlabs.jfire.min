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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.log4j.Logger;
import org.apache.xpath.CachedXPathAPI;
import org.nightlabs.jfire.module.ModuleType;
import org.w3c.dom.Node;
import org.w3c.dom.traversal.NodeIterator;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import org.nightlabs.xml.DOMParser;

/**
 * This class manages application.xml resources form EARs. It reads additional information
 * from web.xml or ejb-jar.xml by using WEBMan or EJBJarMan.
 * 
 * @author marco
 */
public class EARApplicationMan
{
	public static final Logger LOGGER = Logger.getLogger(EARApplicationMan.class);

	private SAXParseException parseException = null;
	
	private ModuleType moduleType;
	
	public EARApplicationMan(File ear, ModuleType _moduleType)
	throws XMLReadException
	{
		LOGGER.debug("Creating instance of EARApplicationMan for ear \""+ear.getAbsolutePath()+"\".");
		this.moduleType = _moduleType;
		try {
			if (!ear.isDirectory())
				throw new UnsupportedOperationException("EAR \""+ear.getAbsolutePath()+"\" is not a directory! EAR jars are not yet supported!");

			File file = new File(ear, "META-INF/application.xml");
			if (!file.exists())
				throw new FileNotFoundException("File \"META-INF/application.xml\" cannot be found within EAR \""+ear.getAbsolutePath()+"\"!");

			InputStream in = new FileInputStream(file);
			try {
				InputSource inputSource = new InputSource(in);
				DOMParser parser = new DOMParser();
//				// list of features: http://xml.apache.org/xerces2-j/features.html
//				// We must deactivate checking, because we might be offline!
//				parser.setFeature("http://xml.org/sax/features/validation", false);
				parser.setErrorHandler(new ErrorHandler(){
					public void error(SAXParseException exception) throws SAXException {
						LOGGER.error("Parse (ejb-rolegroup.xml): ", exception);
						parseException = exception;
					}
		
					public void fatalError(SAXParseException exception) throws SAXException {
						LOGGER.fatal("Parse (ejb-rolegroup.xml): ", exception);
						parseException = exception;
					}
		
					public void warning(SAXParseException exception) throws SAXException {
						LOGGER.warn("Parse (ejb-rolegroup.xml): ", exception);
					}
				});
				parser.parse(inputSource);
				if (parseException != null)
					throw parseException;
	
				CachedXPathAPI xpa = new CachedXPathAPI();
				
				if (ModuleType.MODULE_TYPE_WEB.equals(moduleType)) {
					NodeIterator it = xpa.selectNodeIterator(parser.getDocument(), "//module/web");
					Node n = it.nextNode();
					while (n != null) {
						Node nWebUri = xpa.selectSingleNode(n, "web-uri");
						Node nContextRoot = xpa.selectSingleNode(n, "context-root");
						ModuleDef moduleDef = new ModuleDef();
						moduleDef.setResourceURI(nWebUri.getChildNodes().item(0).getNodeValue());
						moduleDef.setContextPath(nContextRoot.getChildNodes().item(0).getNodeValue());
						readWebXML(ear, moduleDef);
						modules.add(moduleDef);
						n = it.nextNode();
					}
				} // if (ModuleType.MODULE_TYPE_WEB.equals(moduleType)) {
				else
					throw new UnsupportedOperationException("ModuleType \""+moduleType.getModuleType()+"\" not yet supported!");

			} finally {
				in.close();
			}
		} catch (Exception x) {
			throw new XMLReadException(x);
		}
	}
	
	protected void readWebXML(File ear, ModuleDef moduleDef)
		throws XMLReadException, IOException
	{
		if (moduleDef.getResourceURI() == null)
			throw new IllegalArgumentException("Property moduleDef.resourceURI of EAR \""+ear.getAbsolutePath()+"\" is null!");
		
		File moduleFile = new File(ear, moduleDef.getResourceURI());
		if (!moduleFile.exists())
			throw new FileNotFoundException("moduleDef.resourceURI of EAR \""+ear.getAbsolutePath()+"\" not correct: File \""+moduleFile.getAbsolutePath()+"\" does not exist!");
		
		if (!moduleFile.isFile())
			throw new UnsupportedOperationException("EAR \""+ear.getAbsolutePath()+"\": moduleDef.resourceURI \""+moduleFile.getAbsolutePath()+"\" does not point to a file! Only jars are allowed here!");
		
		JarFile jf = new JarFile(moduleFile);
		JarEntry jeWebXML = jf.getJarEntry("WEB-INF/web.xml");
		if (jeWebXML == null) {
			LOGGER.warn("Jar \""+moduleFile.getCanonicalPath()+"\" does not contain \"WEB-INF/web.xml\"!");
		}
		WEBMan webMan = new WEBMan(jf.getInputStream(jeWebXML));
		moduleDef.setName(webMan.getDisplayName());
	}

	/**
	 * @return Returns the moduleType.
	 */
	public ModuleType getModuleType() {
		return moduleType;
	}

	private Collection modules = new ArrayList();
	/**
	 * @return Returns the modules.
	 */
	public Collection getModules() {
		return modules;
	}
}
