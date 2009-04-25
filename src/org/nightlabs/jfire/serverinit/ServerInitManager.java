package org.nightlabs.jfire.serverinit;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javax.naming.InitialContext;

import org.apache.log4j.Logger;
import org.apache.xpath.CachedXPathAPI;
import org.nightlabs.datastructure.PrefixTree;
import org.nightlabs.jfire.init.AbstractInitManager;
import org.nightlabs.jfire.init.DependencyCycleException;
import org.nightlabs.jfire.init.InitException;
import org.nightlabs.jfire.init.Resolution;
import org.nightlabs.jfire.servermanager.JFireServerManagerFactory;
import org.nightlabs.jfire.servermanager.j2ee.J2EEAdapter;
import org.nightlabs.jfire.servermanager.ra.JFireServerManagerFactoryImpl;
import org.nightlabs.jfire.servermanager.ra.ManagedConnectionFactoryImpl;
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

/**
 *
 * @author Tobias Langner <!-- tobias[dot]langner[at]nightlabs[dot]de -->
 */
public class ServerInitManager extends AbstractInitManager<ServerInit, ServerInitDependency> {
	/**
	 * LOG4J logger used by this class
	 */
	private static final Logger logger = Logger.getLogger(ServerInitManager.class);
	private boolean canPerformInit = false;

	private SAXParseException parseException = null;

	private List<ServerInit> earlyInits = new ArrayList<ServerInit>();
	private List<ServerInit> lateInits = new ArrayList<ServerInit>();

	private JFireServerManagerFactory jfireServerManagerFactory;

	public ServerInitManager(JFireServerManagerFactoryImpl jfsmf, ManagedConnectionFactoryImpl mcf,
			J2EEAdapter j2eeAdapter)
	throws ServerInitException {
		jfireServerManagerFactory = jfsmf;

		final PrefixTree<ServerInit> earlyInitTrie = new PrefixTree<ServerInit>();
		final PrefixTree<ServerInit> lateInitTrie = new PrefixTree<ServerInit>();

		// Scan all JARs within all EARs for serverinit.xml files.
		scan(
				mcf, new String[] {
						"META-INF/server-init.xml",
						"META-INF/serverinit.xml" // downward compatibility
				},
				new JarEntryHandler[] {
						new JarEntryHandler() {
							@Override
							public void handleJarEntry(EARApplication ear, String jarName, InputStream in) throws Exception
							{
								logger.debug("Parsing: serverinit.xml (early-init)  of " + ear.getEar().getName() + "#" + jarName);
								List<ServerInit> serverInits = parseServerInitXML(ear.getEar().getName(), jarName, in, "early-init");
								for (ServerInit init : serverInits) {
									earlyInits.add(init);
									earlyInitTrie.insert(new String[] { init.getModule(), init.getArchive(), init.getInitialiserClass() }, init);
								}
							}
						},
						new JarEntryHandler() {
							@Override
							public void handleJarEntry(EARApplication ear, String jarName, InputStream in) throws Exception
							{
								logger.debug("Parsing: serverinit.xml (late-init) of " + ear.getEar().getName() + "#" + jarName);
								List<ServerInit> serverInits = parseServerInitXML(ear.getEar().getName(), jarName, in, "late-init");
								for (ServerInit init : serverInits) {
									lateInits.add(init);
									lateInitTrie.insert(new String[] { init.getModule(), init.getArchive(), init.getInitialiserClass() }, init);
								}
							}
						}

				}
		);
		// Now all meta data files have been read.

		// substitute the temporary dependency definitions by links to the actual inits
		try {
			establishDependencies(earlyInits, earlyInitTrie);
			establishDependencies(lateInits, lateInitTrie);
		} catch (InitException e1) {
			throw new ServerInitException("ServerInit failed: " + e1.getMessage());
		}

		// Now all inits have references of their required and dependent inits.
		Comparator<ServerInit> comp = new Comparator<ServerInit>() {
			public int compare(ServerInit o1, ServerInit o2) {
				int prioDiff = o1.getPriority() - o2.getPriority();
				if (prioDiff != 0)
					return prioDiff;
				else
					return o1.getName().compareTo(o2.getName());
			}
		};
		try {
			earlyInits = resolveDependencies(earlyInits, comp);
			lateInits = resolveDependencies(lateInits, comp);
		} catch (DependencyCycleException e) {
			throw new ServerInitException(e +"Information regarding the cycle: "+ e.getCycleInfo());
		}


		canPerformInit = true;

		if (logger.isDebugEnabled()) {
			logger.debug("************************************************");
			logger.debug("Server Inits in execution order:");
			logger.debug("Early server inits:");
			printInits(earlyInits);
			logger.debug("Late server inits:");
			printInits(lateInits);
			logger.debug("************************************************");
		}
	}



	public List<ServerInit> parseServerInitXML(String jfireEAR, String jfireJAR, InputStream ejbJarIn, String initName)
			throws XMLReadException {
		List<ServerInit> serverInits = new ArrayList<ServerInit>();
		try {
			InputSource inputSource = new InputSource(ejbJarIn);
			DOMParser parser = new DOMParser();
			parser.setErrorHandler(new ErrorHandler() {
				public void error(SAXParseException exception) throws SAXException {
					logger.error("Parse (server-init.xml): ", exception);
					parseException = exception;
				}

				public void fatalError(SAXParseException exception) throws SAXException {
					logger.fatal("Parse (server-init.xml): ", exception);
					parseException = exception;
				}

				public void warning(SAXParseException exception) throws SAXException {
					logger.warn("Parse (server-init.xml): ", exception);
				}
			});
			parser.parse(inputSource);
			if (parseException != null)
				throw parseException;

			CachedXPathAPI xpa = new CachedXPathAPI();

			NodeIterator ni = xpa.selectNodeIterator(parser.getDocument(), "//server-initialisation/" + initName);
			Node nInit = ni.nextNode();
			while (nInit != null) {
				Node nClass = nInit.getAttributes().getNamedItem("class");
				String classStr = null;
				if (nClass != null) {
					Node txt = nClass.getFirstChild();
					if (txt != null)
						classStr = txt.getNodeValue();
				}

				Node nPriority = nInit.getAttributes().getNamedItem("priority");
				String priorityStr = null;
				if (nPriority != null) {
					Node txt = nPriority.getFirstChild();
					if (txt != null)
						priorityStr = txt.getNodeValue();
				}

				if (classStr == null)
					throw new XMLReadException("jfireEAR '" + jfireEAR + "' jfireJAR '" + jfireJAR
							+ "': Reading serverinit.xml failed: Attribute 'class' of element '" + initName + "' must be defined!");

				int priority = 500;
				if (priorityStr != null) {
					try {
						priority = Integer.parseInt(priorityStr);
					} catch (NumberFormatException x) {
						throw new XMLReadException("jfireEAR '" + jfireEAR + "' jfireJAR '" + jfireJAR
								+ "': Reading serverinit.xml failed: Attribute 'priority' of element '" + initName
								+ "' must be a valid integer (or be omitted)!");
					}
				}

				ServerInit init = new ServerInit(jfireEAR, jfireJAR, classStr, priority);

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

					nClass = nDepends.getAttributes().getNamedItem("class");
					classStr = null;
					if (nClass != null) {
						Node txt = nClass.getFirstChild();
						if (txt != null)
							classStr = txt.getNodeValue();
					}

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
						logger.warn("Value '"+resolutionStr+"' of attribute resolution is not valid. Using default 'required'.");
						resolution = Resolution.Required;
					}

					if (moduleStr == null)
						throw new XMLReadException("jfireEAR '" + jfireEAR + "' jfireJAR '" + jfireJAR
								+ "': Reading serverinit.xml failed: Attribute 'module' of element 'depends' must be defined!");

					if (archiveStr == null && classStr != null)
						throw new XMLReadException("jfireEAR '" + jfireEAR + "' jfireJAR '" + jfireJAR
										+ "': Reading serverinit.xml failed: Attribute 'class' of element 'depends' is defined whereas 'archive' is undefined!");

					ServerInitDependency dep = new ServerInitDependency(moduleStr, archiveStr, classStr, resolution);
					init.addDependency(dep);
					nDepends = niDepends.nextNode();
				}

				serverInits.add(init);
				nInit = ni.nextNode();
			}
		} catch (XMLReadException x) {
			throw x;
		} catch (Exception x) {
			throw new XMLReadException("jfireEAR '" + jfireEAR + "' jfireJAR '" + jfireJAR
					+ "': Reading serverinit.xml failed!", x);
		}
		return serverInits;
	}

	@Override
	protected String[] getTriePath(ServerInitDependency dependency) {
		String archive = dependency.getArchive();
		String module = dependency.getModule();
		String theClass = dependency.getIntialiserClass();

		if (archive == null || archive.equals(""))
			return new String[] { module };
		else if (theClass == null || theClass.equals(""))
			return new String[] { module, archive };
		else
			return new String[] { module, archive, theClass };
	}

	public void performEarlyInits(InitialContext ctx) {
		initialiseServer(earlyInits, ctx);
	}

	public void performLateInits(InitialContext ctx) {
		initialiseServer(lateInits, ctx);
	}

	private void initialiseServer(List<ServerInit> inits, InitialContext ctx) {
		if (!canPerformInit) {
			logger.error("Server initialisation can not be performed due to errors above.");
			return;
		}

		for (ServerInit init : inits) {
			logger.info("Invoking ServerInit: " + init);
			try {
				Object initialiserObj = Class.forName(init.getInitialiserClass()).newInstance();
				if (initialiserObj instanceof ServerInitialiserDelegate) {
					ServerInitialiserDelegate serverInitialiser = (ServerInitialiserDelegate) initialiserObj;
					serverInitialiser.setInitialContext(ctx);
					serverInitialiser.setJFireServerManagerFactory(jfireServerManagerFactory);
					serverInitialiser.initialise();
				} else {
					throw new Exception("Class \'" + init.getInitialiserClass() + "\' does not implement IServerInitialiser");
				}

			} catch (Exception x) {
				logger.error("Init failed! " + init, x);
			}
		}
	}

	@Override
	protected Logger getLogger() {
		return logger;
	}
}
