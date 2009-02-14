package org.nightlabs.jfire.web.admin.servlet;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.nightlabs.jfire.web.admin.UserInputException;

/**
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public class ServerInitializeServlet extends HttpServlet
{
	/**
	 * The serial version of this class. 
	 */
	private static final long serialVersionUID = 1L;
	
	private static final String STEP_SESSION_KEY = "serverinitialize.step";

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
	{
		handleRequest(req, resp);
	}
	
	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
	{
		handleRequest(req, resp);
	}
	
	private static final Map<Integer, String> steps = new HashMap<Integer, String>();
	static {
		steps.put(0, "/jsp/serverinitialize/000_welcome.jsp");
		steps.put(1, "/jsp/serverinitialize/010_localServerEdit.jsp");
		steps.put(2, "/jsp/serverinitialize/020_servletSSLEdit.jsp");
		steps.put(3, "/jsp/serverinitialize/030_databaseEdit.jsp");
		steps.put(4, "/jsp/serverinitialize/040_jdoEdit.jsp");
		steps.put(5, "/jsp/serverinitialize/050_smtpEdit.jsp");
		steps.put(6, "/jsp/serverinitialize/060_rootOrganisationEdit.jsp");
		steps.put(7, "/jsp/serverinitialize/070_organisationEdit.jsp");
		steps.put(8, "/jsp/serverinitialize/080_userEdit.jsp");
		steps.put(9, "/jsp/serverinitialize/500_dataOverview.jsp");
	}
	
	private void handleRequest(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
	{
		Integer step = (Integer)req.getSession().getAttribute(STEP_SESSION_KEY);
		if(step == null) {
			// check if init is ok now (or server is already set up)
			
			step = 0;
		}
			
		String navigation = req.getParameter("navigation");
		if(navigation != null) {
			if("next".equals(navigation)) {
				try {
					storeData(req);
					step++;
				} catch (UserInputException e) {
					req.setAttribute("error", e);
				}
			} else if("previous".equals(navigation)) {
				step--;
			} else if("finish".equals(navigation)) {
				boolean needReboot = performInitialization();
				if(needReboot)
					req.getRequestDispatcher("/jsp/serverinitialize/600_reboot.jsp").forward(req, resp);
				else
					req.getRequestDispatcher("/jsp/serverinitialize/600_success.jsp").forward(req, resp);
			}
		}
		if(step < 0)
			step = 0;
		else if(step >= steps.size())
			step = steps.size() - 1;
		
		req.getRequestDispatcher(steps.get(step)).forward(req, resp);
	}
	
	private void storeData(HttpServletRequest req) throws UserInputException
	{
	}

	/**
	 * @return <code>true</code> if a reboot is required.
	 */
	private boolean performInitialization()
	{
		return false;
	}
}
