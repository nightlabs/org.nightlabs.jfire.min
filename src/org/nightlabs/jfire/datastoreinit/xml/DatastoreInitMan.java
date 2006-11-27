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

package org.nightlabs.jfire.datastoreinit.xml;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.xpath.CachedXPathAPI;
import org.nightlabs.xml.DOMParser;
import org.nightlabs.xml.XMLReadException;
import org.w3c.dom.Node;
import org.w3c.dom.traversal.NodeIterator;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 */
public class DatastoreInitMan
{
	/**
	 * LOG4J logger used by this class
	 */
	private static final Logger logger = Logger.getLogger(DatastoreInitMan.class);

	/**
	 * The EAR module directory in which the JAR is deployed. E.g. "JFireBase.ear"
	 */
	private String jfireEAR;
	
	/**
	 * The JAR within the <tt>jfireEAR</tt>. E.g. "JFireBaseBean.jar"
	 */
	private String jfireJAR;

	private SAXParseException parseException = null;
	
	/**
	 * Holds instances of type <tt>Init</tt>.
	 */
	private List<Init> inits = new ArrayList<Init>();

	public DatastoreInitMan(String _jfireEAR, String _jfireJAR, InputStream ejbJarIn)
	throws XMLReadException
	{
		try {
			this.jfireEAR = _jfireEAR;
			this.jfireJAR = _jfireJAR;
			InputSource inputSource = new InputSource(ejbJarIn);
			DOMParser parser = new DOMParser();
		//	parser.setValidate(false);
		//	// list of features: http://xml.apache.org/xerces2-j/features.html
		//	// We deactivate validation because lomboz generates non-valid xml resources, if
		//	// there are no beans in a jar.
		//	// And because we might be offline.
		//	parser.setFeature("http://xml.org/sax/features/validation", false);
			parser.setErrorHandler(new ErrorHandler(){
				public void error(SAXParseException exception) throws SAXException {
					logger.error("Parse (datastoreinit.xml): ", exception);
					parseException = exception;
				}
		
				public void fatalError(SAXParseException exception) throws SAXException {
					logger.fatal("Parse (datastoreinit.xml): ", exception);
					parseException = exception;
				}
		
				public void warning(SAXParseException exception) throws SAXException {
					logger.warn("Parse (datastoreinit.xml): ", exception);
				}
			});
			parser.parse(inputSource);
			if (parseException != null)
				throw parseException;
	
			CachedXPathAPI xpa = new CachedXPathAPI();
			
			NodeIterator ni = xpa.selectNodeIterator(parser.getDocument(), "//datastore-initialization/init");
			Node nInit = ni.nextNode();
			while (nInit != null) {
				Node nBean = nInit.getAttributes().getNamedItem("bean");
				String beanStr = null;
				if (nBean != null) {
					Node txt = nBean.getFirstChild();
					if (txt != null)
						beanStr = txt.getNodeValue();
				}

				Node nMethod = nInit.getAttributes().getNamedItem("method");
				String methodStr = null;
				if (nMethod != null) {
					Node txt = nMethod.getFirstChild();
					if (txt != null)
						methodStr = txt.getNodeValue();
				}

				Node nPriority = nInit.getAttributes().getNamedItem("priority");
				String priorityStr = null;
				if (nPriority != null) {
					Node txt = nPriority.getFirstChild();
					if (txt != null)
						priorityStr = txt.getNodeValue();
				}
				
				if (beanStr == null)
					throw new XMLReadException("jfireEAR '"+_jfireEAR+"' jfireJAR '"+_jfireJAR+"': Reading datastoreinit.xml failed: Attribute 'bean' of element 'init' must be defined!");

				if (methodStr == null)
					throw new XMLReadException("jfireEAR '"+_jfireEAR+"' jfireJAR '"+_jfireJAR+"': Reading datastoreinit.xml failed: Attribute 'method' of element 'init' must be defined!");

				int priority = 500;
				if (priorityStr != null) {
					try {
						priority = Integer.parseInt(priorityStr);
					} catch (NumberFormatException x) {
						throw new XMLReadException("jfireEAR '"+_jfireEAR+"' jfireJAR '"+_jfireJAR+"': Reading datastoreinit.xml failed: Attribute 'priority' of element 'init' must be a valid integer (or be omitted)!");
					}
				}

				Init init = new Init(this, beanStr, methodStr, priority);

				NodeIterator niDepends = xpa.selectNodeIterator(nInit, "depends");
				Node nDepends = niDepends.nextNode();
				while (nDepends != null) {
					Node nModule = nDepends.getAttributes().getNamedItem("module");
					String moduleStr = null;
					if (nModule != null) {
						Node txt = nModule.getFirstChild();
						if (txt != null)
							moduleStr = txt.getNodeValue();
					}

					Node nArchive = nDepends.getAttributes().getNamedItem("archive");
					String archiveStr = null;
					if (nArchive != null) {
						Node txt = nArchive.getFirstChild();
						if (txt != null)
							archiveStr = txt.getNodeValue();
					}
					
					nBean = nDepends.getAttributes().getNamedItem("bean");
					beanStr = null;
					if (nBean != null) {
						Node txt = nBean.getFirstChild();
						if (txt != null)
							beanStr = txt.getNodeValue();
					}

					nMethod = nDepends.getAttributes().getNamedItem("method");
					methodStr = null;
					if (nMethod != null) {
						Node txt = nMethod.getFirstChild();
						if (txt != null)
							methodStr = txt.getNodeValue();
					}

					if (moduleStr == null)
						throw new XMLReadException("jfireEAR '"+_jfireEAR+"' jfireJAR '"+_jfireJAR+"': Reading datastoreinit.xml failed: Attribute 'module' of element 'depends' must be defined!");

//					if (archiveStr == null)
//						throw new XMLReadException("jfireEAR '"+_jfireEAR+"' jfireJAR '"+_jfireJAR+"': Reading datastoreinit.xml failed: Attribute 'archive' of element 'depends' must be defined!");
//
//					if (beanStr == null)
//						throw new XMLReadException("jfireEAR '"+_jfireEAR+"' jfireJAR '"+_jfireJAR+"': Reading datastoreinit.xml failed: Attribute 'bean' of element 'depends' must be defined!");
//
//					if (methodStr == null)
//						throw new XMLReadException("jfireEAR '"+_jfireEAR+"' jfireJAR '"+_jfireJAR+"': Reading datastoreinit.xml failed: Attribute 'method' of element 'depends' must be defined!");

					Dependency dep = new Dependency(init, moduleStr, archiveStr, beanStr, methodStr);
					init.addDependency(dep);

					nDepends = niDepends.nextNode();
				}

				inits.add(init);

				nInit = ni.nextNode();
			}
		} catch(XMLReadException x) {
			throw x;
		} catch(Exception x) {
			throw new XMLReadException("jfireEAR '"+_jfireEAR+"' jfireJAR '"+_jfireJAR+"': Reading datastoreinit.xml failed!", x);
		}
	}

	/**
	 * @return Returns the inits.
	 */
	public Collection getInits()
	{
		return inits;
	}
	/**
	 * @return Returns the jfireEAR.
	 */
	public String getJFireEAR()
	{
		return jfireEAR;
	}
	/**
	 * @return Returns the jfireJAR.
	 */
	public String getJFireJAR()
	{
		return jfireJAR;
	}
}
