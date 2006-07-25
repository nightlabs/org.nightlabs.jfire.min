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

import java.io.InputStream;

import org.apache.log4j.Logger;
import org.apache.xpath.CachedXPathAPI;
import org.w3c.dom.Node;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import org.nightlabs.xml.DOMParser;

/**
 * This class manages web.xml resources.
 * 
 * @author marco
 */
public class WEBMan
{
	/**
	 * LOG4J logger used by this class
	 */
	private static final Logger logger = Logger.getLogger(WEBMan.class);
	
	private SAXParseException parseException = null;

	public WEBMan(InputStream webIn)
	throws XMLReadException
	{
		try {
			InputSource inputSource = new InputSource(webIn);
			DOMParser parser = new DOMParser();
//			// list of features: http://xml.apache.org/xerces2-j/features.html
//			parser.setFeature("http://xml.org/sax/features/validation", true);
			parser.setErrorHandler(new ErrorHandler(){
				public void error(SAXParseException exception) throws SAXException {
					logger.error("Parse (ejb-rolegroup.xml): ", exception);
					parseException = exception;
				}
	
				public void fatalError(SAXParseException exception) throws SAXException {
					logger.fatal("Parse (ejb-rolegroup.xml): ", exception);
					parseException = exception;
				}
	
				public void warning(SAXParseException exception) throws SAXException {
					logger.warn("Parse (ejb-rolegroup.xml): ", exception);
				}
			});
			parser.parse(inputSource);
			if (parseException != null)
				throw parseException;

			CachedXPathAPI xpa = new CachedXPathAPI();
			
			Node nDisplayName = xpa.selectSingleNode(parser.getDocument(), "//web-app/display-name");
			setDisplayName(nDisplayName.getChildNodes().item(0).getNodeValue());
		} catch (Exception x) {
			throw new XMLReadException(x);
		}
	}
	
	private String displayName;
	/**
	 * @return Returns the displayName.
	 */
	public String getDisplayName() {
		return displayName;
	}
	/**
	 * @param displayName The displayName to set.
	 */
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}
}
