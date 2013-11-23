package org.nightlabs.jfire.init;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.nightlabs.datastructure.PrefixTree;
import org.nightlabs.jfire.servermanager.xml.EARApplication;
import org.nightlabs.jfire.servermanager.xml.JarEntryHandler;
import org.nightlabs.xml.DOMParser;
import org.nightlabs.xml.NLDOMUtil;
import org.nightlabs.xml.XMLReadException;
import org.w3c.dom.Node;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public abstract class InvocationInitJarEntryHandler<I extends InvocationInit<I, D>, D extends InvocationInitDependency<I>>
implements JarEntryHandler {
	private static final Logger logger = Logger.getLogger(InvocationInitJarEntryHandler.class);

	private final PrefixTree<I> initTrie = new PrefixTree<I>();
	private final List<I> inits = new ArrayList<I>();

	@Override
	public void handleJarEntry(EARApplication ear, String jarName, InputStream in) throws Exception
	{
		List<I> _inits = parseOrganisationInitXML(ear.getEar().getName(), jarName, in);
		for (I init : _inits) {
			this.inits.add(init);
//			this.initTrie.insert(new String[] {init.getModule(), init.getArchive(), init.getBean(), init.getMethod()}, init);
			this.initTrie.insert(init.getInvocationPath(), init);
		}
	}

	private class OrganisationInitXMLParseErrorHandler implements ErrorHandler {
		private SAXParseException parseException = null;

		public void error(SAXParseException exception) throws SAXException {
			logger.error("Parse ("+ getInitXmlFileName() +"): " + exception, exception);
			parseException = exception;
		}

		public void fatalError(SAXParseException exception) throws SAXException {
			logger.fatal("Parse ("+ getInitXmlFileName() +"): " + exception, exception);
			parseException = exception;
		}

		public void warning(SAXParseException exception) throws SAXException {
			logger.warn("Parse ("+ getInitXmlFileName() +"): " + exception, exception);
		}

		public SAXParseException getParseException() {
			return parseException;
		}
	}

	protected abstract String getInitXmlFileName();

	protected abstract String getRootNodeName();

	protected abstract I createInvocationInit(String invocation, int priority);

	protected abstract D createInvocationInitDependency(String invocation, Resolution resolution);

	protected abstract String getInitXmlNamespaceURI();

	protected List<I> parseOrganisationInitXML(String jfireEAR, String jfireJAR, InputStream ejbJarIn)
	throws XMLReadException
	{
		List<I> _inits = new ArrayList<I>();

		try {
			InputSource inputSource = new InputSource(ejbJarIn);
			DOMParser parser = new DOMParser();
//			parser.setFeature("http://xml.org/sax/features/namespace-prefixes", true); // Gibt's net :-(
			parser.setFeature("http://xml.org/sax/features/namespaces", true);
			OrganisationInitXMLParseErrorHandler errorHandler = new OrganisationInitXMLParseErrorHandler();
			parser.setErrorHandler(errorHandler);
			parser.parse(inputSource);
			if (errorHandler.getParseException() != null) {
				throw errorHandler.getParseException();
			}

//			CachedXPathAPI xpa = new CachedXPathAPI();
//			String nsPrefix = getInitXmlNamespaceURI() + ':';
			String nsPrefix = parser.getDocument().lookupPrefix(getInitXmlNamespaceURI());
			if (nsPrefix == null)
				nsPrefix = "";

			if (!nsPrefix.isEmpty())
				nsPrefix += ":";

			String rootNodeName = getRootNodeName();
			Node rootNode = parser.getDocument().getChildNodes().item(0);
			if ((rootNode.getNamespaceURI() != null && !getInitXmlNamespaceURI().equals(rootNode.getNamespaceURI())) || !rootNodeName.equals(rootNode.getLocalName()))
				throw new XMLReadException("Root element '" + rootNodeName + "' in namespace '" + getInitXmlNamespaceURI() + "' expected, but '" + rootNode.getLocalName() + "' in namespace '" + rootNode.getNamespaceURI() + "' found!");

//			NodeIterator ni = xpa.selectNodeIterator(parser.getDocument(), "//" + nsPrefix + rootNodeName + "/" + nsPrefix + "init", rootNode);
//			Node nInit = ni.nextNode(); // finds nothing - nInit is null since we switched to XSD (with namespaces) :-(

			Node nInit = rootNode.getFirstChild();
			while (nInit != null) {
				if ((nInit.getNamespaceURI() == null || getInitXmlNamespaceURI().equals(nInit.getNamespaceURI())) && "init".equals(nInit.getLocalName())) {
					String invocationStr = NLDOMUtil.getAttributeValue(nInit, "invocation");
//					Node nInvocation = nInit.getAttributes().getNamedItem("invocation");
//					String invocationStr = null;
//					if (nInvocation != null) {
//						Node txt = nInvocation.getFirstChild();
//						if (txt != null)
//							invocationStr = txt.getNodeValue();
//					}

					String priorityStr = NLDOMUtil.getAttributeValue(nInit, "priority");
//					Node nPriority = nInit.getAttributes().getNamedItem("priority");
//					String priorityStr = null;
//					if (nPriority != null) {
//						Node txt = nPriority.getFirstChild();
//						if (txt != null)
//							priorityStr = txt.getNodeValue();
//					}

					if (invocationStr == null)
						throw new XMLReadException("jfireEAR '"+jfireEAR+"' jfireJAR '"+jfireJAR+"': Reading "+ getInitXmlFileName() +" failed: Attribute 'invocation' of element 'init' must be defined!");

					int priority = 500;
					if (priorityStr != null) {
						try {
							priority = Integer.parseInt(priorityStr);
						} catch (NumberFormatException x) {
							throw new XMLReadException("jfireEAR '"+jfireEAR+"' jfireJAR '"+jfireJAR+"': Reading "+ getInitXmlFileName() +" failed: Attribute 'priority' of element 'init' must be a valid integer (or be omitted)!");
						}
					}

					I init = createInvocationInit(invocationStr, priority);

//					NodeIterator niDepends = xpa.selectNodeIterator(nInit, "depends");
//					Node nDepends = niDepends.nextNode(); // again selectNodeIterator(...) doesn't work with namespaces (at least not with the current config) :-(
					Node nDepends = nInit.getFirstChild();
					while (nDepends != null) {
						if ((nDepends.getNamespaceURI() == null || getInitXmlNamespaceURI().equals(nDepends.getNamespaceURI())) && "depends".equals(nDepends.getLocalName())) {
							invocationStr = NLDOMUtil.getAttributeValue(nDepends, "invocation");
//							nInvocation = nInit.getAttributes().getNamedItem("invocation");
//							invocationStr = null;
//							if (nInvocation != null) {
//								Node txt = nInvocation.getFirstChild();
//								if (txt != null)
//									invocationStr = txt.getNodeValue();
//							}

							String resolutionStr = NLDOMUtil.getAttributeValue(nDepends, "resolution");
//							Node nResolution = nDepends.getAttributes().getNamedItem("resolution");
//							String resolutionStr = null;
//							if (nResolution != null) {
//								Node txt = nResolution.getFirstChild();
//								if (txt != null) {
//									resolutionStr = txt.getNodeValue();
//								}
//							}

							Resolution resolution = null;
							try {
								resolution = Resolution.getEnumConstant(resolutionStr);
							} catch (NullPointerException npe) {
								resolution = Resolution.Required;
							} catch (IllegalArgumentException e) {
								logger.warn("jfireEAR '" + jfireEAR + "' jfireJAR '" + jfireJAR
										+ "': Reading "+ getInitXmlFileName() +" failed: Value '"+resolutionStr+"' of attribute resolution is not valid. Using default 'required'.");
								resolution = Resolution.Required;
							}

							if (invocationStr == null)
								throw new XMLReadException("jfireEAR '"+jfireEAR+"' jfireJAR '"+jfireJAR+"': Reading "+ getInitXmlFileName() +" failed: Attribute 'invocation' of element 'depends' must be defined!");

							D dep = createInvocationInitDependency(invocationStr, resolution);
							init.addDependency(dep);
						}

						nDepends = nDepends.getNextSibling();
					}

					_inits.add(init);

				}
				nInit = nInit.getNextSibling();
			}
		} catch(XMLReadException x) {
			throw x;
		} catch(Exception x) {
			throw new XMLReadException("jfireEAR '"+jfireEAR+"' jfireJAR '"+jfireJAR+"': Reading "+ getInitXmlFileName() +" failed!", x);
		}

		return _inits;
	}

	public PrefixTree<I> getInitTrie() {
		return initTrie;
	}

	public List<I> getInits() {
		return inits;
	}
}