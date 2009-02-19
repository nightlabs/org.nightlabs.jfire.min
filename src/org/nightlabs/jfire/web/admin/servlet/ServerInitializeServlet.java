package org.nightlabs.jfire.web.admin.servlet;

import java.io.IOException;
import java.rmi.RemoteException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.nightlabs.jfire.server.ServerManager;
import org.nightlabs.jfire.servermanager.config.JFireServerConfigModule;
import org.nightlabs.jfire.servermanager.config.ServerCf;
import org.nightlabs.jfire.web.admin.ServerSetupUtil;
import org.nightlabs.jfire.web.admin.UserInputException;
import org.nightlabs.jfire.web.admin.serverinit.FirstOrganisationBean;
import org.nightlabs.jfire.web.admin.serverinit.PresetsBean;
import org.nightlabs.jfire.web.admin.serverinit.RootOrganisationBean;
import org.nightlabs.jfire.web.admin.serverinit.ServerInitializeStep;

/**
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public class ServerInitializeServlet extends BaseServlet
{
	//	private static final String NAVIGATION_VALUE_FINISH = "finish";

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
		ServerManager serverManager = ServerSetupUtil.getBogoServerManager();
		JFireServerConfigModule cfMod;
		try {
			cfMod = serverManager.getJFireServerConfigModule();
			if (cfMod.getLocalServer() == null) { // this shouldn't happen anymore.
				ServerCf server = new ServerCf();
				server.init();
				cfMod.setLocalServer(server);
			}
		} catch (Throwable e) {
			throw new RuntimeException("Error accessing server configuration", e);
		}
		
		ServerInitializeStep[] steps = new ServerInitializeStep[] {
				new ServerInitializeStep("welcome", "/jsp/serverinitialize/welcome.jsp", null),
				new ServerInitializeStep("presets", null, new PresetsBean()),
				new ServerInitializeStep("localserver", null, cfMod.getLocalServer()),
				new ServerInitializeStep("database", null, cfMod.getDatabase()),
				new ServerInitializeStep("jdo", null, cfMod.getJdo()),
				new ServerInitializeStep("smtp", null, cfMod.getSmtp()),
				new ServerInitializeStep("servletssl", null, cfMod.getServletSSLCf()),
				new ServerInitializeStep("rootorganisation", null, new RootOrganisationBean()),
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
		ServerInitializeStep stepToShow = null;

		// coming from a form - we have a navigation parameter
		// save the data and redirect to the next page
		String navigation = req.getParameter(NAVIGATION_PARAMETER_KEY);
		if(navigation != null) {
			System.out.println("have navigation: "+navigation);
			String saveStepName = req.getParameter("step");
			ServerInitializeStep stepToSave = null;
			if(saveStepName != null)
				stepToSave = findStepByName(req, saveStepName);
			if(stepToSave != null) {
				System.out.println("step to save by step parameter: "+stepToSave.getName());
				if(NAVIGATION_VALUE_NEXT.equals(navigation)) {
					System.out.println("Have step to save: "+stepToSave);
					if(stepToSave.getBean() != null) {
						// save data to the bean
						BeanEditServlet.finishEdit(req);
					}
					stepToShow = findNextStep(req, stepToSave);
					if(stepToShow == null) {
						// we are done. do initialization
						boolean needReboot = performInitialization();
						if(needReboot)
							setContent(req, "/jsp/serverinitialize/reboot.jsp");
						else
							setContent(req, "/jsp/serverinitialize/success.jsp");
						resetSteps(req);
						return;
					}
				} else if(NAVIGATION_VALUE_PREVIOUS.equals(navigation)) {
					stepToShow = findPreviousStep(req, stepToSave);
					if(stepToShow == null)
						stepToShow = getSteps(req)[0];
				}
			}
			if(stepToShow == null)
				stepToShow = getSteps(req)[0];
			System.out.println("redirecting to show next step: "+stepToShow.getName());
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
			System.out.println("step to show by path info: "+stepToShow.getName());
		}


		// jump in for fresh initialize or unknown step
		if(stepToShow == null) {
			resetSteps(req);
			System.out.println("resetting steps - starting fresh");
			redirect(req, resp, "/serverinitialize/"+getSteps(req)[0].getName());
			return;
		}

		// show step inside this page response
		if(stepToShow.getForward() != null) {
			System.out.println("FORWARD: "+stepToShow.getForward());
			setContent(req, stepToShow.getForward());
		} else {
			// show bean editor
			System.out.println("BEAN: "+stepToShow.getName());
			BeanEditServlet.startEdit(req, stepToShow.getBean());
//			req.getSession().setAttribute("beanedit.bean", stepToShow.getBean());
			req.setAttribute("stepToShow", stepToShow);
			setContent(req, "/jsp/serverinitialize/beaneditheader.jsp", "/beanedit", "/jsp/serverinitialize/beaneditfooter.jsp");
		}
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
	
	/**
	 * @return <code>true</code> if a reboot is required.
	 */
	private boolean performInitialization()
	{
		// TODO do something!
		return false;
	}
}
