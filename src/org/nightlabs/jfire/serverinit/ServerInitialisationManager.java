package org.nightlabs.jfire.serverinit;

import java.io.File;
import java.io.FileFilter;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.naming.InitialContext;

import org.apache.log4j.Logger;
import org.apache.xpath.CachedXPathAPI;
import org.nightlabs.jfire.init.AbstractInitManager;
import org.nightlabs.jfire.init.DependencyCycleException;
import org.nightlabs.jfire.init.InitException;
import org.nightlabs.jfire.servermanager.JFireServerManager;
import org.nightlabs.jfire.servermanager.JFireServerManagerFactory;
import org.nightlabs.jfire.servermanager.j2ee.J2EEAdapter;
import org.nightlabs.jfire.servermanager.ra.JFireServerManagerFactoryImpl;
import org.nightlabs.jfire.servermanager.ra.ManagedConnectionFactoryImpl;
import org.nightlabs.util.ds.CycleException;
import org.nightlabs.util.ds.DirectedGraph;
import org.nightlabs.util.ds.PrefixTree;
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
 * @author Tobias Langner <!-- tobias[DOT]langner[AT]nightlabs[DOT]de -->
 */
public class ServerInitialisationManager extends AbstractInitManager<ServerInit, ServerInitDependency> {
	/**
	 * LOG4J logger used by this class
	 */
	private static final Logger logger = Logger.getLogger(ServerInitialisationManager.class);
	private boolean canPerformInit = false;

	private SAXParseException parseException = null;

	private FileFilter earFileFilter = new FileFilter() {
		public boolean accept(File pathname) {
			return pathname.getName().endsWith(".ear");
		}
	};

	private FileFilter jarFileFilter = new FileFilter() {
		public boolean accept(File pathname) {
			return pathname.getName().endsWith(".jar");
		}
	};

	private List<ServerInit> earlyInits = new ArrayList<ServerInit>();
	private List<ServerInit> lateInits = new ArrayList<ServerInit>();
	
	private JFireServerManagerFactory jfireServerManagerFactory;

	public ServerInitialisationManager(JFireServerManagerFactoryImpl jfsmf, ManagedConnectionFactoryImpl mcf,
			J2EEAdapter j2eeAdapter)
	throws ServerInitException {
		jfireServerManagerFactory = jfsmf;
		
		String deployBaseDir = mcf.getConfigModule().getJ2ee().getJ2eeDeployBaseDirectory();
		File jfireModuleBaseDir = new File(deployBaseDir);
		PrefixTree<ServerInit> earlyInitTrie = new PrefixTree<ServerInit>();
		PrefixTree<ServerInit> lateInitTrie = new PrefixTree<ServerInit>();

		// Scan all JARs within all EARs for serverinit.xml files.
		File[] ears = jfireModuleBaseDir.listFiles(earFileFilter);
		for (int i = 0; i < ears.length; ++i) {
			File ear = ears[i];

			File[] jars = ear.listFiles(jarFileFilter);
			for (int m = 0; m < jars.length; ++m) {
				File jar = jars[m];
				try {
					JarFile jf = new JarFile(jar);
					try {
						JarEntry je = jf.getJarEntry("META-INF/serverinit.xml");
						if (je != null) {
							InputStream in = jf.getInputStream(je);
							try {
								logger.debug("Parsing: serverinit.xml of " + ear.getName() + "#" + jar.getName());
								List<ServerInit> serverInits = parseServerInitXML(ear.getName(), jar.getName(), in, "early-init");
								for (ServerInit init : serverInits) {
									earlyInits.add(init);
									earlyInitTrie.insert(new String[] { init.getModule(), init.getArchive(), init.getInitialiserClass() }, init);
								}
								
								in = jf.getInputStream(je);
								serverInits = parseServerInitXML(ear.getName(), jar.getName(), in, "late-init");
								for (ServerInit init : serverInits) {
									lateInits.add(init);
									lateInitTrie.insert(new String[] { init.getModule(), init.getArchive(), init.getInitialiserClass() }, init);
								}
							} finally {
								in.close();
							}
						} // if (je != null) {
					} finally {
						jf.close();
					}
				} catch (Exception e) {
					logger.error("Reading from JAR '" + jar.getAbsolutePath() + "' failed!", e);
				}
			}
		}
		// Now all meta data files have been read.
		
		// substitute the temporary dependency definitions by links to the actual inits
		establishDependencies(earlyInits, earlyInitTrie);
		establishDependencies(lateInits, lateInitTrie);
		
		// Now all inits have references of their required and dependent inits.
		Comparator<ServerInit> comp = new Comparator<ServerInit>() {
			public int compare(ServerInit o1, ServerInit o2) {
				return o1.getPriority() - o2.getPriority();
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
					logger.error("Parse (serverinit.xml): ", exception);
					parseException = exception;
				}

				public void fatalError(SAXParseException exception) throws SAXException {
					logger.fatal("Parse (serverinit.xml): ", exception);
					parseException = exception;
				}

				public void warning(SAXParseException exception) throws SAXException {
					logger.warn("Parse (serverinit.xml): ", exception);
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

					if (moduleStr == null)
						throw new XMLReadException("jfireEAR '" + jfireEAR + "' jfireJAR '" + jfireJAR
								+ "': Reading serverinit.xml failed: Attribute 'module' of element 'depends' must be defined!");

					if (archiveStr == null && classStr != null)
						throw new XMLReadException("jfireEAR '" + jfireEAR + "' jfireJAR '" + jfireJAR
										+ "': Reading serverinit.xml failed: Attribute 'class' of element 'depends' is defined whereas 'archive' is undefined!");

					ServerInitDependency dep = new ServerInitDependency(moduleStr, archiveStr, classStr);
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
