package org.nightlabs.jfire.crossorganisationregistrationinit;

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
import org.nightlabs.jfire.organisationinit.OrganisationInitException;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.security.id.UserID;
import org.nightlabs.jfire.servermanager.JFireServerManager;
import org.nightlabs.jfire.servermanager.JFireServerManagerFactory;
import org.nightlabs.jfire.servermanager.config.ServerCf;
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

public class CrossOrganisationRegistrationInitManager
extends AbstractInitManager<CrossOrganisationRegistrationInit, OrganisationInitDependency>
{
	private static final Logger logger = Logger.getLogger(CrossOrganisationRegistrationInitManager.class);

	private boolean canPerformInit = false;

	private SAXParseException parseException = null;

	/**
	 * Holds instances of type <tt>Init</tt>.
	 */
	private List<CrossOrganisationRegistrationInit> inits = new ArrayList<CrossOrganisationRegistrationInit>();

	public CrossOrganisationRegistrationInitManager(JFireServerManager jfsm)
	throws CrossOrganisationRegistrationInitException
	{
		final PrefixTree<CrossOrganisationRegistrationInit> initTrie = new PrefixTree<CrossOrganisationRegistrationInit>();

		// Scan all JARs within all EARs for organisation-init.xml files.
		scan(
				jfsm, new String[] { "META-INF/cross-organisation-registration-init.xml" },
				new JarEntryHandler[] {
						new JarEntryHandler() {
							@Override
							public void handleJarEntry(EARApplication ear, String jarName, InputStream in) throws Exception
							{
								List<CrossOrganisationRegistrationInit> serverInits = parseInitXML(ear.getEar().getName(), jarName, in);
								for (CrossOrganisationRegistrationInit init : serverInits) {
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
			throw new CrossOrganisationRegistrationInitException("Datastore initialisation failed: " + e1.getMessage());
		}

		// Now all inits have references of their required and dependent inits.
		Comparator<CrossOrganisationRegistrationInit> comp = new Comparator<CrossOrganisationRegistrationInit>() {
			public int compare(CrossOrganisationRegistrationInit o1, CrossOrganisationRegistrationInit o2) {
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
			throw new CrossOrganisationRegistrationInitException(e + "Information regarding the cycle: "+ e.getCycleInfo());
		}
		canPerformInit = true;

		if (logger.isDebugEnabled()) {
			logger.debug("************************************************");
			logger.debug("Organisation Inits in execution order:");
			printInits(inits);
			logger.debug("************************************************");
		}
	}

	public List<CrossOrganisationRegistrationInit> getInits()
	{
		return Collections.unmodifiableList(inits);
	}

	protected List<CrossOrganisationRegistrationInit> parseInitXML(String jfireEAR, String jfireJAR, InputStream ejbJarIn)
	throws XMLReadException
	{
		List<CrossOrganisationRegistrationInit> _inits = new ArrayList<CrossOrganisationRegistrationInit>();

		try {
			InputSource inputSource = new InputSource(ejbJarIn);
			DOMParser parser = new DOMParser();
			parser.setErrorHandler(new ErrorHandler(){
				public void error(SAXParseException exception) throws SAXException {
					logger.error("Parse (cross-organisation-registration-init.xml): ", exception);
					parseException = exception;
				}

				public void fatalError(SAXParseException exception) throws SAXException {
					logger.fatal("Parse (cross-organisation-registration-init.xml): ", exception);
					parseException = exception;
				}

				public void warning(SAXParseException exception) throws SAXException {
					logger.warn("Parse (cross-organisation-registration-init.xml): ", exception);
				}
			});
			parser.parse(inputSource);
			if (parseException != null)
				throw parseException;

			CachedXPathAPI xpa = new CachedXPathAPI();

			NodeIterator ni = xpa.selectNodeIterator(parser.getDocument(), "//cross-organisation-registration-initialisation/init");
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
					throw new XMLReadException("jfireEAR '"+jfireEAR+"' jfireJAR '"+jfireJAR+"': Reading cross-organisation-registration-init.xml failed: Attribute 'bean' of element 'init' must be defined!");

				if (methodStr == null)
					throw new XMLReadException("jfireEAR '"+jfireEAR+"' jfireJAR '"+jfireJAR+"': Reading cross-organisation-registration-init.xml failed: Attribute 'method' of element 'init' must be defined!");

				int priority = 500;
				if (priorityStr != null) {
					try {
						priority = Integer.parseInt(priorityStr);
					} catch (NumberFormatException x) {
						throw new XMLReadException("jfireEAR '"+jfireEAR+"' jfireJAR '"+jfireJAR+"': Reading cross-organisation-registration-init.xml failed: Attribute 'priority' of element 'init' must be a valid integer (or be omitted)!");
					}
				}

				CrossOrganisationRegistrationInit init = new CrossOrganisationRegistrationInit(jfireEAR, jfireJAR, beanStr, methodStr, priority);

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
								+ "': Reading cross-organisation-registration-init.xml failed: Value '"+resolutionStr+"' of attribute resolution is not valid. Using default 'required'.");
						resolution = Resolution.Required;
					}

					if (moduleStr == null)
						throw new XMLReadException("jfireEAR '"+jfireEAR+"' jfireJAR '"+jfireJAR+"': Reading cross-organisation-registration-init.xml failed: Attribute 'module' of element 'depends' must be defined!");

					if (archiveStr == null && (beanStr != null || methodStr != null))
						throw new XMLReadException("jfireEAR '" + jfireEAR + "' jfireJAR '" + jfireJAR
										+ "': Reading cross-organisation-registration-init.xml failed: Attribute 'bean/method' of element 'depends' is defined whereas 'archive' is undefined!");

					if (beanStr == null && methodStr != null)
						throw new XMLReadException("jfireEAR '" + jfireEAR + "' jfireJAR '" + jfireJAR
										+ "': Reading cross-organisation-registration-init.xml failed: Attribute 'method' of element 'depends' is defined whereas 'bean' is undefined!");

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
			throw new XMLReadException("jfireEAR '"+jfireEAR+"' jfireJAR '"+jfireJAR+"': Reading cross-organisation-registration-init.xml failed!", x);
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
			JFireServerManagerFactory ismf, ServerCf localServer, String organisationID, String systemUserPassword, Context context) throws OrganisationInitException
	{
		if (!canPerformInit) {
			logger.error("Cross organisation registration initialisation can not be performed due to errors above.");
			return;
		}

		try {
			Properties props = InvokeUtil.getInitialContextProperties(ismf, UserID.create(organisationID, User.USER_ID_SYSTEM), systemUserPassword);
			InitialContext initCtx = new InitialContext(props);
			try {
				Throwable firstInitException = null;

				for (CrossOrganisationRegistrationInit init : inits) {
					logger.info("Invoking CrossOrganisationRegistrationInit: " + init);

					try {
						// we force a new (nested) transaction by using a delegate-ejb with the appropriate tags
//						Object delegateBean = InvokeUtil.createBean(initCtx, "jfire/ejb/JFireBaseBean/OrganisationInitDelegate");
						Object delegateBean = initCtx.lookup(InvokeUtil.JNDI_PREFIX_EJB_BY_REMOTE_INTERFACE + "org.nightlabs.jfire.organisationinit.OrganisationInitDelegateRemote");
						Method beanMethod = delegateBean.getClass().getMethod("invokeCrossOrganisationRegistrationInitInNestedTransaction", CrossOrganisationRegistrationInit.class, Context.class);
						beanMethod.invoke(delegateBean, init, context);
//						InvokeUtil.removeBean(delegateBean);
					} catch (Throwable x) { // we catch this in order to execute all inits before escalating
						if (firstInitException == null)
							firstInitException = x;

						logger.error("CrossOrganisationRegistrationInit failed! " + init, x);
					}
				}

				// We escalate the first exception that occured in order to ensure the inits to be retried.
				// Since this method is called by an Async-Invocation, it's retried a few times and afterwards, an administrator
				// has the possibility to initiate further retries.
				if (firstInitException != null)
					throw firstInitException;

			} finally {
		   	initCtx.close();
			}
		} catch (Throwable x) {
			throw new OrganisationInitException(x);
		}
	}

}
