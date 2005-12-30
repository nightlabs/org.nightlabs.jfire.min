/* ************************************************************************** *
 * Copyright (C) 2004 NightLabs GmbH, Marco Schulze                           *
 * All rights reserved.                                                       *
 * http://www.NightLabs.de                                                    *
 *                                                                            *
 * This program and the accompanying materials are free software; you can re- *
 * distribute it and/or modify it under the terms of the GNU General Public   *
 * License as published by the Free Software Foundation; either ver 2 of the  *
 * License, or any later version.                                             *
 *                                                                            *
 * This module is distributed in the hope that it will be useful, but WITHOUT *
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FIT- *
 * NESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more *
 * details.                                                                   *
 *                                                                            *
 * You should have received a copy of the GNU General Public License along    *
 * with this module; if not, write to the Free Software Foundation, Inc.:     *
 *    59 Temple Place, Suite 330                                              *
 *    Boston MA 02111-1307                                                    *
 *    USA                                                                     *
 *                                                                            *
 * Or get it online:                                                          *
 *    http://www.opensource.org/licenses/gpl-license.php                      *
 *                                                                            *
 * In case, you want to use this module or parts of it in a proprietary pro-  *
 * ject, you can purchase it under the NightLabs Commercial License. Please   *
 * contact NightLabs GmbH under info AT nightlabs DOT com for more infos or   *
 * visit http://www.NightLabs.com                                             *
 * ************************************************************************** */

/*
 * Created on 13.08.2004
 */
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
	public static Logger LOGGER = Logger.getLogger(WEBMan.class);
	
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
