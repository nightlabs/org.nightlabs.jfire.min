package org.nightlabs.jfire.web.admin.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.KeyStore;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.nightlabs.jfire.organisation.OrganisationManagerRemote;
import org.nightlabs.jfire.server.ServerManagerRemote;
import org.nightlabs.jfire.serverconfigurator.ServerConfigurator;
import org.nightlabs.jfire.servermanager.config.DatabaseCf;
import org.nightlabs.jfire.servermanager.config.J2eeCf;
import org.nightlabs.jfire.servermanager.config.JDOCf;
import org.nightlabs.jfire.servermanager.config.JFireServerConfigModule;
import org.nightlabs.jfire.servermanager.config.RootOrganisationCf;
import org.nightlabs.jfire.servermanager.config.ServerCf;
import org.nightlabs.jfire.servermanager.config.ServletSSLCf;
import org.nightlabs.jfire.servermanager.config.SmtpMailServiceCf;
import org.nightlabs.jfire.web.admin.ServerSetupUtil;
import org.nightlabs.jfire.web.admin.serverinit.FirstOrganisationBean;
import org.nightlabs.jfire.web.admin.serverinit.PresetsBean;
import org.nightlabs.jfire.web.admin.serverinit.RootOrganisationBean;
import org.nightlabs.jfire.web.admin.serverinit.ServerInitializeStep;

/**
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public class ServerInitializeServlet extends BaseServlet
{
	private static final Logger log = Logger.getLogger(ServerInitializeServlet.class);

	private static final String NAVIGATION_VALUE_FINISH = "finish";

	private static final String NAVIGATION_VALUE_PREVIOUS = "previous";

	private static final String NAVIGATION_PARAMETER_KEY = "navigation";

	private static final String NAVIGATION_VALUE_NEXT = "next";

	/**
	 * The serial version of this class.
	 */
	private static final long serialVersionUID = 1L;

//	private static final String STEP_SESSION_KEY = "serverinitialize.step";

	private static final String STEPS_SESSION_KEY = "serverinitialize.steps";

	private ServerInitializeStep[] setupSteps()
	{
		ServerManagerRemote serverManager = ServerSetupUtil.getBogoServerManager();
		final JFireServerConfigModule cfMod;
		try {
			cfMod = serverManager.getJFireServerConfigModule();
			if (cfMod.getLocalServer() == null) { // this indicates it's a new server needing setup - see implementation of JFireServerManagerFactoryImpl.isNewServerNeedingSetup()
				ServerCf server = new ServerCf();
				server.init();
				cfMod.setLocalServer(server);
			}
		} catch (Throwable e) {
			throw new RuntimeException("Error accessing server configuration", e);
		}

		RootOrganisationBean rootOrganisationBean = new RootOrganisationBean();
		rootOrganisationBean.copyFromCf(cfMod.getRootOrganisation());

		ServerInitializeStep[] steps = new ServerInitializeStep[] {
				new ServerInitializeStep("welcome", "/jsp/serverinitialize/welcome.jsp", null),
				new ServerInitializeStep("presets", null, new PresetsBean(), new ServerInitializeStep.PopulateListener() {
					@Override
					public void afterPopulate(Object bean)
					{
						PresetsBean b = (PresetsBean)bean;
						String presets = b.getPresets();
						if(presets.equals("jboss_mysql")) {
							cfMod.getDatabase().loadDefaults("MySQL");
							cfMod.getJ2ee().setServerConfigurator("org.nightlabs.jfire.jboss.serverconfigurator.ServerConfiguratorJBossMySQL");
						} else if(presets.equals("jboss_derby")) {
							cfMod.getDatabase().loadDefaults("Derby");
							cfMod.getJ2ee().setServerConfigurator("org.nightlabs.jfire.jboss.serverconfigurator.ServerConfiguratorJBossDerby");
						} else {
							throw new IllegalStateException("Unknown preset type: "+presets);
						}
					}
				}),
				new ServerInitializeStep("localserver", null, cfMod.getLocalServer()),
				new ServerInitializeStep("jee", null, cfMod.getJ2ee()),
				new ServerInitializeStep("database", null, cfMod.getDatabase()),
				new ServerInitializeStep("jdo", null, cfMod.getJdo()),
				new ServerInitializeStep("smtp", null, cfMod.getSmtp()),
				new ServerInitializeStep("servletssl", null, cfMod.getServletSSLCf()),
				new ServerInitializeStep("rootorganisation", null, rootOrganisationBean),
				new ServerInitializeStep("firstorganisation", null, new FirstOrganisationBean()),
				new ServerInitializeStep("overview", "/jsp/serverinitialize/overview.jsp", null)
		};

		return steps;
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.web.admin.servlet.BaseServlet#handleRequest(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected void handleRequest(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
	{
		resp.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
		resp.setHeader("Pragma", "no-cache");

		ServerInitializeStep stepToShow = null;

		// coming from a form - we have a navigation parameter
		// save the data and redirect to the next page
		String navigation = req.getParameter(NAVIGATION_PARAMETER_KEY);
		if(navigation != null) {
			if(log.isDebugEnabled())
				log.debug("have navigation: "+navigation);
			String saveStepName = req.getParameter("step");
			ServerInitializeStep stepToSave = null;
			if(saveStepName != null)
				stepToSave = findStepByName(req, saveStepName);
			if(stepToSave != null) {
				if(log.isDebugEnabled())
					log.debug("step to save by step parameter: "+stepToSave.getName());
				if(NAVIGATION_VALUE_NEXT.equals(navigation) || NAVIGATION_VALUE_FINISH.equals(navigation)) {
					if(log.isDebugEnabled())
						log.debug("Have step to save: "+stepToSave);
					if(stepToSave.getBean() != null) {
						// save data to the bean
						BeanEditServlet.finishEdit(req);
						if(stepToSave.getPopulateListener() != null)
							stepToSave.getPopulateListener().afterPopulate(stepToSave.getBean());
					}
					stepToShow = findNextStep(req, stepToSave);
					if(stepToShow == null || NAVIGATION_VALUE_FINISH.equals(navigation)) {
						// we are done. do initialization
						finish(req, resp);
						return;
					}
				} else if(NAVIGATION_VALUE_PREVIOUS.equals(navigation)) {
					stepToShow = findPreviousStep(req, stepToSave);
					if(stepToShow == null)
						stepToShow = getSteps(req)[0];
				}
			}
			if(NAVIGATION_VALUE_FINISH.equals(navigation)) {
				// we are done. do initialization
				finish(req, resp);
				return;
			}
			if(stepToShow == null)
				stepToShow = getSteps(req)[0];
			if(log.isDebugEnabled())
				log.debug("redirecting to show next step: "+stepToShow.getName());
			redirect(req, resp, "/serverinitialize/"+stepToShow.getName());
			return;
		}


		// about to show a form - get target from path
		String pathInfo = req.getPathInfo();
		if(pathInfo != null && !"/".equals(pathInfo) && pathInfo.length() > 1) {
			// remove leading '/'
			String stepName = pathInfo.substring(1);
			// find step
			stepToShow = findStepByName(req, stepName);
			if(log.isDebugEnabled())
				log.debug("step to show by path info: "+stepToShow.getName());
		}


		// jump in for fresh initialize or unknown step
		if(stepToShow == null) {
			resetSteps(req);
			if(log.isDebugEnabled())
				log.debug("resetting steps - starting fresh");
			redirect(req, resp, "/serverinitialize/"+getSteps(req)[0].getName());
			return;
		}

		// show step inside this page response
		if(stepToShow.getForward() != null) {
			//System.out.println("FORWARD: "+stepToShow.getForward());
			setContent(req, stepToShow.getForward());
		} else {
			// show bean editor
			//System.out.println("BEAN: "+stepToShow.getName());
			BeanEditServlet.startEdit(req, stepToShow.getBean());
//			req.getSession().setAttribute("beanedit.bean", stepToShow.getBean());
			req.setAttribute("stepToShow", stepToShow);
			setContent(req, "/jsp/serverinitialize/beaneditheader.jsp", "/beanedit", "/jsp/serverinitialize/beaneditfooter.jsp");
		}
	}

	private void finish(HttpServletRequest req, HttpServletResponse resp) throws IOException
	{
		boolean needReboot = performInitialization(getStepsByName(req));
		if(needReboot)
			setContent(req, "/jsp/serverinitialize/reboot.jsp");
		else
			// redirect to index which will show the login form
			// or let the user create an organisation
			redirect(req, resp, "/");
			//setContent(req, "/jsp/serverinitialize/success.jsp");
		resetSteps(req);
	}

	private ServerInitializeStep findPreviousStep(HttpServletRequest req, ServerInitializeStep saveStep)
	{
		int previousIdx = findStepIdxByName(req, saveStep.getName()) - 1;
		if(previousIdx < 0)
			return null;
		return getSteps(req)[previousIdx];
	}

	private ServerInitializeStep findNextStep(HttpServletRequest req, ServerInitializeStep saveStep)
	{
		int previousIdx = findStepIdxByName(req, saveStep.getName()) + 1;
		ServerInitializeStep[] steps = getSteps(req);
		if(previousIdx == 0 || previousIdx >= steps.length)
			return null;
		return steps[previousIdx];
	}

	private void resetSteps(HttpServletRequest req)
	{
		req.getSession().setAttribute(STEPS_SESSION_KEY, null);
	}

	private ServerInitializeStep findStepByName(HttpServletRequest req, String stepName)
	{
		int idx = findStepIdxByName(req, stepName);
		return idx == -1 ? null : getSteps(req)[idx];
	}

	private int findStepIdxByName(HttpServletRequest req, String stepName)
	{
		ServerInitializeStep[] steps = getSteps(req);
		for (int idx=0; idx < steps.length; idx++) {
			if(steps[idx].getName().equals(stepName)) {
				return idx;
			}
		}
		return -1;
	}

	private ServerInitializeStep[] getSteps(HttpServletRequest req)
	{
		ServerInitializeStep[] steps = (ServerInitializeStep[])req.getSession().getAttribute(STEPS_SESSION_KEY);
		if(steps == null) {
			steps = setupSteps();
			req.getSession().setAttribute(STEPS_SESSION_KEY, steps);
		}
		return steps;
	}

	private Map<String, ServerInitializeStep> getStepsByName(HttpServletRequest req)
	{
		ServerInitializeStep[] steps = getSteps(req);
		Map<String, ServerInitializeStep> result = new HashMap<String, ServerInitializeStep>(steps.length);
		for(ServerInitializeStep step : steps)
			result.put(step.getName(), step);
		return result;
	}

	/**
	 * @return <code>true</code> if a reboot is required.
	 */
	private boolean performInitialization(Map<String, ServerInitializeStep> stepsByName)
	{
		log.info("Server initialisation starting...");

		try {
			JFireServerConfigModule cfMod = createServerConfigModule(stepsByName);
			ServerManagerRemote serverManager = ServerSetupUtil.getBogoServerManager();
			serverManager.setJFireServerConfigModule(cfMod);

			FirstOrganisationBean firstOrganisationBean = (FirstOrganisationBean) stepsByName.get("firstorganisation").getBean();

			// save the data and create the organisation at next boot, because doing it after changing the server configuration is likely to fail due to hot undeploy/redeploy.
			boolean createOrganisation = firstOrganisationBean.getOrganisationID() != null && !firstOrganisationBean.getOrganisationID().isEmpty();
			if(createOrganisation) {
				OrganisationManagerRemote organisationManager = ServerSetupUtil.getBogoOrganisationManager();
				organisationManager.createOrganisationAfterReboot(
						firstOrganisationBean.getOrganisationID(),
						firstOrganisationBean.getOrganisationName(),
						firstOrganisationBean.getAdminUserName(),
						firstOrganisationBean.getAdminPassword(),
						true);
			}

			// we give the server 10 sec (before shutting down) to have enough time for the creation of the info page and for storing the current state
			boolean serverIsShuttingDown = serverManager.configureServerAndShutdownIfNecessary(10000);
			if (serverIsShuttingDown) {
				log.info("Server is shutting down - will create organisation after reboot.");
				return true;
			}

			if(createOrganisation) {
				OrganisationManagerRemote organisationManager = ServerSetupUtil.getBogoOrganisationManager();
				organisationManager.createOrganisation(
						firstOrganisationBean.getOrganisationID(),
						firstOrganisationBean.getOrganisationName(),
						firstOrganisationBean.getAdminUserName(),
						firstOrganisationBean.getAdminPassword(),
						true);
			}

		} catch(Exception e) {
			log.error("Server initialisation failed", e);
			// TODO: struts error handling
			throw new RuntimeException("Server initialisation failed", e);
		}

		log.info("Server initialisation done.");

		return false;
	}

	private JFireServerConfigModule createServerConfigModule(Map<String, ServerInitializeStep> stepsByName)
	{
		JFireServerConfigModule cfMod = new JFireServerConfigModule();

		// root organisation:
		RootOrganisationBean rootOrganisationBean = (RootOrganisationBean) stepsByName.get("rootorganisation").getBean();
		boolean standalone = rootOrganisationBean.getServerID() == null || rootOrganisationBean.getServerID().isEmpty();
		if(!standalone) {
			ServerCf rootOrgServer = new ServerCf(rootOrganisationBean.getServerID());
			RootOrganisationCf rootOrg = new RootOrganisationCf(
					rootOrganisationBean.getOrganisationID(),
					rootOrganisationBean.getOrganisationName(),
					rootOrgServer
			);
//			rootOrgServer.setServerName(rootOrganisationBean.getServerName());
//			rootOrgServer.setJ2eeServerType(rootOrganisationBean.getJ2eeServerType());
//			rootOrgServer.setInitialContextURL(Server.PROTOCOL_JNP, rootOrganisationBean.getInitialContextURL_jnp());
//			rootOrgServer.setInitialContextURL(Server.PROTOCOL_HTTPS, rootOrganisationBean.getInitialContextURL_https());
			cfMod.setRootOrganisation(rootOrg);
			rootOrg.init();
			rootOrganisationBean.copyToCf(rootOrg);
		}

		// local server:
		ServerCf localServer = (ServerCf) stepsByName.get("localserver").getBean();
		cfMod.setLocalServer(localServer);

		// servlet / ssl
		ServletSSLCf servletSSLCf = (ServletSSLCf) stepsByName.get("servletssl").getBean();
		String keystoreURLToImport = servletSSLCf.getKeystoreURLToImport();
		if(keystoreURLToImport != null && !keystoreURLToImport.isEmpty()) {
			try
			{
				KeyStore ks = KeyStore.getInstance( "JKS" );
				final String keystorePassword = servletSSLCf.getKeystorePassword();
				InputStream keystoreToImportStream;
				// distinguish between default and non-default keystore via this constant
				if (ServletSSLCf.DEFAULT_KEYSTORE.equals(keystoreURLToImport))
				{
					keystoreToImportStream = ServerConfigurator.class.getResourceAsStream("/jfire-server.keystore");
				}
				else
					keystoreToImportStream = new URL(keystoreURLToImport).openStream();

				ks.load(keystoreToImportStream, keystorePassword.toCharArray());

				final String wantedAlias = servletSSLCf.getSslServerCertificateAlias();
				if (! ks.containsAlias(wantedAlias))
					throw new IllegalStateException("No certificate with alias '"+ wantedAlias+"' found in "+ keystoreURLToImport);

				// TODO: test the certificate password somehow!
			}
			catch (Exception e)
			{
				throw new IllegalStateException("Keystore initialisation error:", e);
			}
		}
		cfMod.setServletSSLCf(servletSSLCf);

		// j2ee
		J2eeCf j2ee = (J2eeCf) stepsByName.get("jee").getBean();
		cfMod.setJ2ee(j2ee);

		// database
		DatabaseCf database = (DatabaseCf) stepsByName.get("database").getBean();
		cfMod.setDatabase(database);

		// jdo
		JDOCf jdo = (JDOCf) stepsByName.get("jdo").getBean();
		cfMod.setJdo(jdo);

		// smtp
		SmtpMailServiceCf smtp = (SmtpMailServiceCf) stepsByName.get("smtp").getBean();
		if (smtp.getPort() == null) {
			if (SmtpMailServiceCf.ENCRYPTION_METHOD_NONE.equals(smtp.getEncryptionMethod()))
				smtp.setPort(SmtpMailServiceCf.DEFAULT_PORT_PLAIN);
			else if (SmtpMailServiceCf.ENCRYPTION_METHOD_SSL.equals(smtp.getEncryptionMethod()))
				smtp.setPort(SmtpMailServiceCf.DEFAULT_PORT_SSL);
		}
		cfMod.setSmtp(smtp);

		// call init in order to ensure that the cfMod is in a consistent state
		cfMod.init();

		return cfMod;
	}
}
