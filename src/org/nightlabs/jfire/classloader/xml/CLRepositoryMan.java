/* ************************************************************************** *
 * Copyright (C) 2004 NightLabs GmbH, Marco Schulze                           *
 * All rights reserved.                                                       *
 * http://www.NightLabs.de                                                    *
 *                                                                            *
 * This program and the accompanying materials are free software; you can re- *
 * distribute it and/or modify it under the terms of the GNU Lesser General   *
 * Public License as published by the Free Software Foundation; either ver 2  *
 * of the License, or any later version.                                      *
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
 * In case, you want to use this module or parts of it in a commercial way    * 
 * that is not allowed by the GPL, pleas contact us and we will provide a     *
 * commercial licence.                                                        *
 * ************************************************************************** */

/*
 * Created on 11.11.2004
 */
package org.nightlabs.jfire.classloader.xml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Pattern;

import javax.xml.transform.TransformerException;

import org.apache.log4j.Logger;
import org.apache.xpath.CachedXPathAPI;
import org.w3c.dom.Node;
import org.w3c.dom.traversal.NodeIterator;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import org.nightlabs.xml.DOMParser;
import org.nightlabs.xml.XMLReadException;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 */
public class CLRepositoryMan
{
	public static final Logger LOGGER = Logger.getLogger(CLRepositoryMan.class);
	
	public static class Publication {
		private boolean inherit = true;
		private List resourcePatterns = new ArrayList();
		
		public Publication()
		{
		}

		public void addResourcePattern(String resourcePattern)
		{
			resourcePatterns.add(resourcePattern);
			compositeResourcePatternStr = null;
		}
		
		/**
		 * @return Returns the resourcePatterns.
		 */
		public List getResourcePatterns()
		{
			return resourcePatterns;
		}
		
		private String compositeResourcePatternStr = null;
		public String getCompositeResourcePatternStr()
		{
			if (compositeResourcePatternStr == null) {
				StringBuffer sb = new StringBuffer('^');
				for (Iterator it = resourcePatterns.iterator(); it.hasNext(); ) {
					sb.append('(');
					sb.append(it.next());
					sb.append(')');
					if (it.hasNext())
						sb.append('|');
				}
				sb.append('$');
				compositeResourcePatternStr = sb.toString();
			}
			return compositeResourcePatternStr;
		}
		
		private Pattern compositeResourcePattern = null;
		public Pattern getCompositeResourcePattern()
		{
			if (compositeResourcePattern == null)
				compositeResourcePattern = Pattern.compile(getCompositeResourcePatternStr());

			return compositeResourcePattern;
		}
		/**
		 * @return Returns the inherit.
		 */
		public boolean isInherit()
		{
			return inherit;
		}
		/**
		 * @param inherit The inherit to set.
		 */
		public void setInherit(boolean inherit)
		{
			this.inherit = inherit;
		}
	}
	
	/**
	 * List of Publication
	 */
	protected List inheritedPublications = null;

	/**
	 * key: String targetName<br/>
	 * value: Publication publication
	 */
	protected Map localPublications = new HashMap();

	private SAXParseException parseException = null;
	
	public CLRepositoryMan()
	{
		this(null);
	}
	
	public CLRepositoryMan(List _inheritedPublications)
	{
		this.inheritedPublications = _inheritedPublications;
//		if (this.inheritedPublications == null)
//			this.inheritedPublications = new ArrayList();
	}
	
	public void readCLRepositoryXML(JarFile jar, JarEntry jarEntry)
		throws XMLReadException, SAXException, IOException, TransformerException
	{
		InputStream in = jar.getInputStream(jarEntry);
		try {
			String dir = jarEntry.getName();
			int slashIdx = dir.lastIndexOf('/');
			dir = dir.substring(0, slashIdx);
			readCLRepositoryXML(jarEntry.getName(), in, true);
		} finally {
			in.close();
		}
	}
	
	protected void readCLRepositoryXML(
//			String clRepositoryDirectory, 
			final String clRepositoryAbsFileName,
			InputStream in, boolean forbidMultipleTargets)
		throws SAXException, IOException, TransformerException, XMLReadException
	{
		InputSource inputSource = new InputSource(in);
		DOMParser parser = new DOMParser();
//		parser.setOnlineMode(true);
//		// list of features: http://xml.apache.org/xerces2-j/features.html
//		// We must deactivate checking, because we might be offline!
//		parser.setFeature("http://xml.org/sax/features/validation", false);
		parser.setErrorHandler(new ErrorHandler(){
			public void error(SAXParseException exception) throws SAXException {
				LOGGER.error("Parse ("+clRepositoryAbsFileName+"): ", exception);
				parseException = exception;
			}

			public void fatalError(SAXParseException exception) throws SAXException {
				LOGGER.fatal("Parse ("+clRepositoryAbsFileName+"): ", exception);
				parseException = exception;
			}

			public void warning(SAXParseException exception) throws SAXException {
				LOGGER.warn("Parse ("+clRepositoryAbsFileName+"): ", exception);
			}
		});
		parseException = null;
		parser.parse(inputSource);
		if (parseException != null)
			throw parseException;

		CachedXPathAPI xpa = new CachedXPathAPI();

		NodeIterator it = xpa.selectNodeIterator(parser.getDocument(), "//classloader-repository/publish");
		Node n = it.nextNode();
		while (n != null) {
			Node nTarget = n.getAttributes().getNamedItem("target");
//			Node nTarget = xpa.selectSingleNode(n, "target");
			String targetStr = null;
			if (nTarget != null) {
				Node txt = nTarget.getFirstChild(); // getChildNodes().item(0);
				if (txt != null)
					targetStr = txt.getNodeValue();
			}

			if (targetStr == null)
				targetStr = ".";
			
			boolean inherit = true;
			Node nInherit = n.getAttributes().getNamedItem("inherit");
			if (nInherit != null) {
				Node txt = nInherit.getFirstChild();
				if (txt != null) {
					inherit = Boolean.valueOf(txt.getNodeValue()).booleanValue();
				}
			}

			if (forbidMultipleTargets && !".".equals(targetStr))
				throw new XMLReadException("Within a JAR file, multiple targets are not allowed! Publication's target must be \".\" or omitted!");

			if (targetStr.indexOf('/') > 0 || targetStr.indexOf(File.separatorChar) > 0)
				throw new XMLReadException("target contains separator! You must exclusively use local directories/jars - no \"/\"!");
			Publication publication = new Publication(); //new File(clrepository.getParent(), targetStr));
			publication.setInherit(inherit);
			NodeIterator itClassPatterns = xpa.selectNodeIterator(n, "resources");
			Node nClassPattern = itClassPatterns.nextNode();
			while (nClassPattern != null) {
				Node txt = nClassPattern.getChildNodes().item(0);
				if (txt == null)
					throw new NullPointerException("tag resources must not be empty!"); // TODO Is it really null, if the tag resources is empty? 
				String classPattern = txt.getNodeValue();
				publication.addResourcePattern(classPattern);
				nClassPattern = itClassPatterns.nextNode();
			}
			n = it.nextNode();

			localPublications.put(targetStr, publication);
		}
	}
	
	public void readCLRepositoryXML(File clrepository)
	throws XMLReadException
	{
		LOGGER.debug("reading cl repository file \""+clrepository.getAbsolutePath()+"\".");
		try {
			if (!clrepository.exists())
				throw new FileNotFoundException("CLRepository file  \""+clrepository.getAbsolutePath()+"\" does not exist.");
			
			if (!clrepository.isFile())
				throw new UnsupportedOperationException("CLRepositoryMan \""+clrepository.getAbsolutePath()+"\" is not a file!");

			InputStream in = new FileInputStream(clrepository);
			try {
				readCLRepositoryXML(// clrepository.getParent(), 
						clrepository.getAbsolutePath(), in, false);

			} finally {
				in.close();
			}
		} catch (Exception x) {
			throw new XMLReadException(x);
		}
	}

	/**
	 * This method returns all instances of Publication that apply to the given target file
	 * which is either a directory or a JAR.
	 *
	 * @param targetFileName
	 * @return
	 */
	public List getApplicablePublications(String targetFileName)
	{
//		File parentFile = targetFile.getParentFile();
//		if (parentFile == null)
//			throw new NullPointerException("targetFile doesn't have a parent!");
//
//		if (!parentFile.equals(baseDir))
//			throw new IllegalArgumentException("targetFile is not a child of the base dir \""+baseDir.getAbsolutePath()+"\"!");

		List res = new ArrayList();
		if (inheritedPublications != null)
			res.addAll(inheritedPublications);

		Publication publication = (Publication)localPublications.get(".");
		if (publication != null) {
			if (!publication.isInherit())
				res.clear();
			res.add(publication);
		}

		if (targetFileName == null) {
			for (Iterator it = localPublications.entrySet().iterator(); it.hasNext(); ) {
				Map.Entry me = (Map.Entry)it.next();
				if (!".".equals(me.getKey()))
					res.add(me.getValue());
			}
		}
		else {
			publication = (Publication)localPublications.get(targetFileName);
			if (publication != null) {
				if (!publication.isInherit())
					res.clear();
				res.add(publication);
			}
		}

		return res;
	}

	public void writeCLRepositoryXML(File clRepositoryFile, String target, boolean inherit)
		throws IOException
	{
		OutputStream out = new FileOutputStream(clRepositoryFile);
		try {
			Writer w = new OutputStreamWriter(out, "UTF-8");
			try {
				w.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<!DOCTYPE classloader-repository\nPUBLIC \"-//NightLabs GmbH//DTD ClassLoader Repository 1.0//EN\"\n\"http://www.nightlabs.de/dtd/clrepository_1_0.dtd\">\n\n");
				w.write("<classloader-repository>\n");
				w.write("	<publish target=\""+target+"\" inherit=\""+Boolean.toString(inherit)+"\">\n");
				if(inheritedPublications != null)
				{
				  for (Iterator it = inheritedPublications.iterator(); it.hasNext(); ) {
				    Publication p = (Publication) it.next();
				    for (Iterator itRes = p.getResourcePatterns().iterator(); itRes.hasNext(); ) {
				      String pat = (String)itRes.next();
				      w.write("		<resources>"+pat+"</resources>\n");
				    }
				  }
				}
				w.write("	</publish>\n");
				w.write("</classloader-repository>\n");
			} finally {
				w.close();
			}
		} finally {
			out.close();
		}
	}
}
