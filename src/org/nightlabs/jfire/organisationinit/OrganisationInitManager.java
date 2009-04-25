package org.nightlabs.jfire.organisationinit;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;

import javax.naming.InitialContext;

import org.apache.log4j.Logger;
import org.apache.xpath.CachedXPathAPI;
import org.nightlabs.datastructure.PrefixTree;
import org.nightlabs.jfire.base.InvokeUtil;
import org.nightlabs.jfire.init.AbstractInitManager;
import org.nightlabs.jfire.init.DependencyCycleException;
import org.nightlabs.jfire.init.InitException;
import org.nightlabs.jfire.init.Resolution;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.servermanager.JFireServerManagerFactory;
import org.nightlabs.jfire.servermanager.config.ServerCf;
import org.nightlabs.jfire.servermanager.createorganisation.CreateOrganisationProgress;
import org.nightlabs.jfire.servermanager.createorganisation.CreateOrganisationStatus;
import org.nightlabs.jfire.servermanager.createorganisation.CreateOrganisationStep;
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

public class OrganisationInitManager
extends AbstractInitManager<OrganisationInit, OrganisationInitDependency>
{
	private static final Logger logger = Logger.getLogger(OrganisationInitManager.class);

	private boolean canPerformInit = false;

	private SAXParseException parseException = null;

	/**
	 * Holds instances of type <tt>Init</tt>.
	 */
	private List<OrganisationInit> inits = new ArrayList<OrganisationInit>();

	public OrganisationInitManager(JFireServerManagerFactoryImpl jfsmf, ManagedConnectionFactoryImpl mcf, J2EEAdapter j2eeAdapter)
	throws OrganisationInitException
	{
		final PrefixTree<OrganisationInit> initTrie = new PrefixTree<OrganisationInit>();

		scan(
				mcf,
				new String[] {
						"META-INF/organisation-init.xml",
//						// BEGIN downward compatibility // The contents need to be modified anyway after our change to EJB3 => require renaming files, too.
//						"META-INF/datastoreinit.xml"
//						// END downward compatibility
				},
				new JarEntryHandler[] {
						new JarEntryHandler() {
							@Override
							public void handleJarEntry(EARApplication ear, String jarName, InputStream in) throws Exception
							{
								List<OrganisationInit> serverInits = parseOrganisationInitXML(ear.getEar().getName(), jarName, in);
								for (OrganisationInit init : serverInits) {
									inits.add(init);
									initTrie.insert(new String[] {init.getModule(), init.getArchive(), init.getBean(), init.getMethod()}, init);
								}
							}
						}
				}
		);
		// Now all meta data files have been read.

		// substitute the temporary dependency definitions by links to the actual inits
		try {
			establishDependencies(inits, initTrie);
		} catch (InitException e1) {
			throw new OrganisationInitException("Datastore initialisation failed: " + e1.getMessage());
		}

		// Now all inits have references of their required and dependent inits.
		Comparator<OrganisationInit> comp = new Comparator<OrganisationInit>() {
			public int compare(OrganisationInit o1, OrganisationInit o2) {
				int prioDiff = o1.getPriority() - o2.getPriority();
				if (prioDiff != 0)
					return prioDiff;
				else
					return o1.getName().compareTo(o2.getName());
			}
		};
		try {
			inits = resolveDependencies(inits, comp);
		} catch (DependencyCycleException e) {
			throw new OrganisationInitException(e + "Information regarding the cycle: "+ e.getCycleInfo());
		}
		canPerformInit = true;

		if (logger.isDebugEnabled()) {
			logger.debug("************************************************");
			logger.debug("Organisation Inits in execution order:");
			printInits(inits);
			logger.debug("************************************************");
		}
	}

	public List<OrganisationInit> getInits()
	{
		return Collections.unmodifiableList(inits);
	}

	protected List<OrganisationInit> parseOrganisationInitXML(String jfireEAR, String jfireJAR, InputStream ejbJarIn)
	throws XMLReadException
	{
		List<OrganisationInit> _inits = new ArrayList<OrganisationInit>();

		try {
			InputSource inputSource = new InputSource(ejbJarIn);
			DOMParser parser = new DOMParser();
			parser.setErrorHandler(new ErrorHandler(){
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
			});
			parser.parse(inputSource);
			if (parseException != null)
				throw parseException;

			CachedXPathAPI xpa = new CachedXPathAPI();

			String rootNodeName = "organisation-initialisation";
			if (xpa.selectSingleNode(parser.getDocument(), "//" + rootNodeName) == null) {
				rootNodeName = "datastore-initialisation";
				if (xpa.selectSingleNode(parser.getDocument(), "//" + rootNodeName) != null)
					logger.warn("https://www.jfire.org/modules/bugs/view.php?id=579 : organisation-init.xml or datastoreinit.xml contains old elements: EAR=" + jfireEAR + " JAR=" + jfireJAR);
			}
			NodeIterator ni = xpa.selectNodeIterator(parser.getDocument(), "//" + rootNodeName + "/init");
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
					throw new XMLReadException("jfireEAR '"+jfireEAR+"' jfireJAR '"+jfireJAR+"': Reading organisation-init.xml failed: Attribute 'bean' of element 'init' must be defined!");

				if (methodStr == null)
					throw new XMLReadException("jfireEAR '"+jfireEAR+"' jfireJAR '"+jfireJAR+"': Reading organisation-init.xml failed: Attribute 'method' of element 'init' must be defined!");

				int priority = 500;
				if (priorityStr != null) {
					try {
						priority = Integer.parseInt(priorityStr);
					} catch (NumberFormatException x) {
						throw new XMLReadException("jfireEAR '"+jfireEAR+"' jfireJAR '"+jfireJAR+"': Reading organisation-init.xml failed: Attribute 'priority' of element 'init' must be a valid integer (or be omitted)!");
					}
				}

				OrganisationInit init = new OrganisationInit(jfireEAR, jfireJAR, beanStr, methodStr, priority);

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

					if (moduleStr == null)
						throw new XMLReadException("jfireEAR '"+jfireEAR+"' jfireJAR '"+jfireJAR+"': Reading organisation-init.xml failed: Attribute 'module' of element 'depends' must be defined!");

					if (archiveStr == null && (beanStr != null || methodStr != null))
						throw new XMLReadException("jfireEAR '" + jfireEAR + "' jfireJAR '" + jfireJAR
										+ "': Reading organisation-init.xml failed: Attribute 'bean/method' of element 'depends' is defined whereas 'archive' is undefined!");

					if (beanStr == null && methodStr != null)
						throw new XMLReadException("jfireEAR '" + jfireEAR + "' jfireJAR '" + jfireJAR
										+ "': Reading organisation-init.xml failed: Attribute 'method' of element 'depends' is defined whereas 'bean' is undefined!");

					OrganisationInitDependency dep = new OrganisationInitDependency(moduleStr, archiveStr, beanStr, methodStr, resolution);
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

	@Override
	protected Logger getLogger() {
		return logger;
	}

	@Override
	protected String[] getTriePath(OrganisationInitDependency dependency) {
		String[] fields = new String[4];
		fields[0] = dependency.getModule();
		fields[1] = dependency.getArchive();
		fields[2] = dependency.getBean();
		fields[3] = dependency.getMethod();

		List<String> toReturn = new ArrayList<String>(fields.length);

		for (int i = 0; i < fields.length; i++) {
			if (fields[i] == null || fields[i].equals(""))
				break;
			toReturn.add(fields[i]);
		}

		return toReturn.toArray(new String[0]);
	}

	public void initialiseOrganisation(
			JFireServerManagerFactory ismf, ServerCf localServer, String organisationID, String systemUserPassword) throws OrganisationInitException
	{
		initialiseOrganisation(ismf, localServer, organisationID, systemUserPassword, null);
	}

	public void initialiseOrganisation(
			JFireServerManagerFactory ismf, ServerCf localServer, String organisationID, String systemUserPassword, CreateOrganisationProgress createOrganisationProgress) throws OrganisationInitException
	{
		if (!canPerformInit) {
			logger.error("Organisation initialisation can not be performed due to errors above.");
			return;
		}

		try {
			Properties props = InvokeUtil.getInitialContextProperties(ismf, localServer, organisationID, User.USER_ID_SYSTEM, systemUserPassword);
			InitialContext initCtx = new InitialContext(props);
			try {
				for (OrganisationInit init : inits) {
					logger.info("Invoking OrganisationInit: " + init);

					if (createOrganisationProgress != null)
						createOrganisationProgress.addCreateOrganisationStatus(
								new CreateOrganisationStatus(CreateOrganisationStep.DatastoreInitManager_initialiseDatastore_begin, new String[] { init.getName() }));

					try {
						// we force a new (nested) transaction by using a delegate-ejb with the appropriate tags
//						Object delegateBean = InvokeUtil.createBean(initCtx, "jfire/ejb/JFireBaseBean/OrganisationInitDelegate");
						Object delegateBean = initCtx.lookup(InvokeUtil.JNDI_PREFIX_EJB_BY_REMOTE_INTERFACE + "org.nightlabs.jfire.organisationinit.OrganisationInitDelegateRemote");
						Method beanMethod = delegateBean.getClass().getMethod("invokeOrganisationInitInNestedTransaction", OrganisationInit.class);
						beanMethod.invoke(delegateBean, init);
//						InvokeUtil.removeBean(delegateBean);

						if (createOrganisationProgress != null)
							createOrganisationProgress.addCreateOrganisationStatus(
									new CreateOrganisationStatus(CreateOrganisationStep.DatastoreInitManager_initialiseDatastore_endWithSuccess, new String[] { init.getName() }));
					} catch (Exception x) {
						logger.error("Init failed! " + init, x);

						if (createOrganisationProgress != null)
							createOrganisationProgress.addCreateOrganisationStatus(
									new CreateOrganisationStatus(CreateOrganisationStep.DatastoreInitManager_initialiseDatastore_endWithError, x));
					}
				}

			} finally {
		   	initCtx.close();
			}
		} catch (Exception x) {
			throw new OrganisationInitException(x);
		}
	}

}
