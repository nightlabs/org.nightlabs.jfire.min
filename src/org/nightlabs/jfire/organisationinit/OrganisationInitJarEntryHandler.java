package org.nightlabs.jfire.organisationinit;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.xpath.CachedXPathAPI;
import org.nightlabs.datastructure.PrefixTree;
import org.nightlabs.jfire.init.Resolution;
import org.nightlabs.jfire.servermanager.xml.EARApplication;
import org.nightlabs.jfire.servermanager.xml.JarEntryHandler;
import org.nightlabs.xml.DOMParser;
import org.nightlabs.xml.XMLReadException;
import org.w3c.dom.Node;
import org.w3c.dom.traversal.NodeIterator;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class OrganisationInitJarEntryHandler implements JarEntryHandler {
	private static final Logger logger = Logger.getLogger(OrganisationInitJarEntryHandler.class);		
	
	private final PrefixTree<OrganisationInit> initTrie = new PrefixTree<OrganisationInit>();
	private final List<OrganisationInit> inits = new ArrayList<OrganisationInit>();

	@Override
	public void handleJarEntry(EARApplication ear, String jarName, InputStream in) throws Exception
	{
		List<OrganisationInit> serverInits = parseOrganisationInitXML(ear.getEar().getName(), jarName, in);
		for (OrganisationInit init : serverInits) {
			inits.add(init);
//			initTrie.insert(new String[] {init.getModule(), init.getArchive(), init.getBean(), init.getMethod()}, init);
			initTrie.insert(init.getInvocationPath(), init);
		}
	}

	private static class OrganisationInitXMLParseErrorHandler implements ErrorHandler {
		private SAXParseException parseException = null;

		public void error(SAXParseException exception) throws SAXException {
			logger.error("Parse (organisation-init.xml): ", exception);
			parseException = exception;
		}

		public void fatalError(SAXParseException exception) throws SAXException {
			logger.fatal("Parse (organisation-init.xml): ", exception);
			parseException = exception;
		}

		public void warning(SAXParseException exception) throws SAXException {
			logger.warn("Parse (organisation-init.xml): ", exception);
		}

		public SAXParseException getParseException() {
			return parseException;
		}
	}
	
	protected List<OrganisationInit> parseOrganisationInitXML(String jfireEAR, String jfireJAR, InputStream ejbJarIn)
	throws XMLReadException
	{
		List<OrganisationInit> _inits = new ArrayList<OrganisationInit>();

		try {
			InputSource inputSource = new InputSource(ejbJarIn);
			DOMParser parser = new DOMParser();
			OrganisationInitJarEntryHandler.OrganisationInitXMLParseErrorHandler errorHandler = new OrganisationInitXMLParseErrorHandler();
			parser.setErrorHandler(errorHandler);
			parser.parse(inputSource);
			if (errorHandler.getParseException() != null) {
				throw errorHandler.getParseException();
			}

			CachedXPathAPI xpa = new CachedXPathAPI();

			String rootNodeName = "organisation-initialisation";
			NodeIterator ni = xpa.selectNodeIterator(parser.getDocument(), "//" + rootNodeName + "/init");
			Node nInit = ni.nextNode();
			while (nInit != null) {
				Node nInvocation = nInit.getAttributes().getNamedItem("invocation");
				String invocationStr = null;
				if (nInvocation != null) {
					Node txt = nInvocation.getFirstChild();
					if (txt != null)
						invocationStr = txt.getNodeValue();
				}
				
//				Node nBean = nInit.getAttributes().getNamedItem("bean");
//				String beanStr = null;
//				if (nBean != null) {
//					Node txt = nBean.getFirstChild();
//					if (txt != null)
//						beanStr = txt.getNodeValue();
//				}
//
//				Node nMethod = nInit.getAttributes().getNamedItem("method");
//				String methodStr = null;
//				if (nMethod != null) {
//					Node txt = nMethod.getFirstChild();
//					if (txt != null)
//						methodStr = txt.getNodeValue();
//				}
//
				Node nPriority = nInit.getAttributes().getNamedItem("priority");
				String priorityStr = null;
				if (nPriority != null) {
					Node txt = nPriority.getFirstChild();
					if (txt != null)
						priorityStr = txt.getNodeValue();
				}

//				if (beanStr == null)
//					throw new XMLReadException("jfireEAR '"+jfireEAR+"' jfireJAR '"+jfireJAR+"': Reading organisation-init.xml failed: Attribute 'bean' of element 'init' must be defined!");
//
//				if (methodStr == null)
//					throw new XMLReadException("jfireEAR '"+jfireEAR+"' jfireJAR '"+jfireJAR+"': Reading organisation-init.xml failed: Attribute 'method' of element 'init' must be defined!");

				if (invocationStr == null)
					throw new XMLReadException("jfireEAR '"+jfireEAR+"' jfireJAR '"+jfireJAR+"': Reading organisation-init.xml failed: Attribute 'invocation' of element 'init' must be defined!");

				int priority = 500;
				if (priorityStr != null) {
					try {
						priority = Integer.parseInt(priorityStr);
					} catch (NumberFormatException x) {
						throw new XMLReadException("jfireEAR '"+jfireEAR+"' jfireJAR '"+jfireJAR+"': Reading organisation-init.xml failed: Attribute 'priority' of element 'init' must be a valid integer (or be omitted)!");
					}
				}

				OrganisationInit init = new OrganisationInit(invocationStr, priority);

				NodeIterator niDepends = xpa.selectNodeIterator(nInit, "depends");
				Node nDepends = niDepends.nextNode();
				while (nDepends != null) {
					nInvocation = nInit.getAttributes().getNamedItem("invocation");
					invocationStr = null;
					if (nInvocation != null) {
						Node txt = nInvocation.getFirstChild();
						if (txt != null)
							invocationStr = txt.getNodeValue();
					}
					
//					Node nModule = nDepends.getAttributes().getNamedItem("module");
//					String moduleStr = null;
//					if (nModule != null) {
//						Node txt = nModule.getFirstChild();
//						if (txt != null)
//							moduleStr = txt.getNodeValue();
//					}
//
//					Node nArchive = nDepends.getAttributes().getNamedItem("archive");
//					String archiveStr = null;
//					if (nArchive != null) {
//						Node txt = nArchive.getFirstChild();
//						if (txt != null)
//							archiveStr = txt.getNodeValue();
//					}
//
//					nBean = nDepends.getAttributes().getNamedItem("bean");
//					beanStr = null;
//					if (nBean != null) {
//						Node txt = nBean.getFirstChild();
//						if (txt != null)
//							beanStr = txt.getNodeValue();
//					}
//
//					nMethod = nDepends.getAttributes().getNamedItem("method");
//					methodStr = null;
//					if (nMethod != null) {
//						Node txt = nMethod.getFirstChild();
//						if (txt != null)
//							methodStr = txt.getNodeValue();
//					}

					Node nResolution = nDepends.getAttributes().getNamedItem("resolution");
					String resolutionStr = null;
					if (nResolution != null) {
						Node txt = nResolution.getFirstChild();
						if (txt != null) {
							resolutionStr = txt.getNodeValue();
						}
					}

					Resolution resolution = null;
					try {
						resolution = Resolution.getEnumConstant(resolutionStr);
					} catch (NullPointerException npe) {
						resolution = Resolution.Required;
					} catch (IllegalArgumentException e) {
						logger.warn("jfireEAR '" + jfireEAR + "' jfireJAR '" + jfireJAR
								+ "': Reading organisation-init.xml failed: Value '"+resolutionStr+"' of attribute resolution is not valid. Using default 'required'.");
						resolution = Resolution.Required;
					}

					if (invocationStr == null)
						throw new XMLReadException("jfireEAR '"+jfireEAR+"' jfireJAR '"+jfireJAR+"': Reading organisation-init.xml failed: Attribute 'invocation' of element 'depends' must be defined!");
					
//					if (moduleStr == null)
//						throw new XMLReadException("jfireEAR '"+jfireEAR+"' jfireJAR '"+jfireJAR+"': Reading organisation-init.xml failed: Attribute 'module' of element 'depends' must be defined!");
//
//					if (archiveStr == null && (beanStr != null || methodStr != null))
//						throw new XMLReadException("jfireEAR '" + jfireEAR + "' jfireJAR '" + jfireJAR
//										+ "': Reading organisation-init.xml failed: Attribute 'bean/method' of element 'depends' is defined whereas 'archive' is undefined!");
//
//					if (beanStr == null && methodStr != null)
//						throw new XMLReadException("jfireEAR '" + jfireEAR + "' jfireJAR '" + jfireJAR
//										+ "': Reading organisation-init.xml failed: Attribute 'method' of element 'depends' is defined whereas 'bean' is undefined!");

					OrganisationInitDependency dep = new OrganisationInitDependency(invocationStr, resolution);
					init.addDependency(dep);

					nDepends = niDepends.nextNode();
				}

				_inits.add(init);

				nInit = ni.nextNode();
			}
		} catch(XMLReadException x) {
			throw x;
		} catch(Exception x) {
			throw new XMLReadException("jfireEAR '"+jfireEAR+"' jfireJAR '"+jfireJAR+"': Reading organisation-init.xml failed!", x);
		}

		return _inits;
	}
	
	public PrefixTree<OrganisationInit> getInitTrie() {
		return initTrie;
	}
	
	public List<OrganisationInit> getInits() {
		return inits;
	}
}