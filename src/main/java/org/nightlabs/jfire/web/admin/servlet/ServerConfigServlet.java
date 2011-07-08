package org.nightlabs.jfire.web.admin.servlet;

import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.nightlabs.jfire.base.JFireEjb3Factory;
import org.nightlabs.jfire.server.ServerManagerRemote;
import org.nightlabs.jfire.servermanager.config.DatabaseCf;
import org.nightlabs.jfire.servermanager.config.J2eeCf;
import org.nightlabs.jfire.servermanager.config.JDOCf;
import org.nightlabs.jfire.servermanager.config.JFireServerConfigModule;
import org.nightlabs.jfire.servermanager.config.RootOrganisationCf;
import org.nightlabs.jfire.servermanager.config.ServerCf;
import org.nightlabs.jfire.servermanager.config.SslCf;
import org.nightlabs.jfire.servermanager.config.SmtpMailServiceCf;
import org.nightlabs.jfire.web.admin.SessionLogin;
import org.nightlabs.jfire.web.admin.Util;
import org.nightlabs.jfire.web.admin.beaninfo.ServerConfigBeanInfo;
import org.nightlabs.jfire.web.admin.serverinit.RootOrganisationBean;

/**
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public class ServerConfigServlet extends BaseServlet
{
	/**
	 * The serial version.
	 */
	private static final long serialVersionUID = 1L;

	private static final Logger log = Logger.getLogger(ServerConfigServlet.class);

	private static class BeanEditRequest extends HttpServletRequestWrapper
	{
		private int beanKey;

		/**
		 * Create a new BeanEditRequest instance.
		 * @param request
		 */
		public BeanEditRequest(HttpServletRequest request, int beanKey)
		{
			super(request);
			this.beanKey = beanKey;
		}

		/* (non-Javadoc)
		 * @see javax.servlet.ServletRequestWrapper#getAttribute(java.lang.String)
		 */
		@Override
		public Object getAttribute(String name)
		{
			if("beanedit.beankey".equals(name))
				return beanKey;
			return super.getAttribute(name);
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.web.admin.servlet.BaseServlet#handleRequest(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected void handleRequest(HttpServletRequest req, HttpServletResponse resp) throws Exception
	{
		SessionLogin login = SessionLogin.getLogin(req.getSession());
		ServerManagerRemote manager = JFireEjb3Factory.getRemoteBean(ServerManagerRemote.class, login.getInitialContextProperties());

		if(Util.haveParameterValue(req, "action", "save")) {
			log.info("Have save request");
			JFireServerConfigModule config = manager.getJFireServerConfigModule();
			Map<Integer, Object> beans = BeanEditServlet.finishEdit(req);
			for(Object bean : beans.values()) {
				if(bean instanceof ServerCf)
					config.setLocalServer((ServerCf) bean);
				else if(bean instanceof DatabaseCf)
					config.setDatabase((DatabaseCf) bean);
				else if(bean instanceof J2eeCf)
					config.setJ2ee((J2eeCf) bean);
				else if(bean instanceof JDOCf)
					config.setJdo((JDOCf) bean);
				else if(bean instanceof SmtpMailServiceCf)
					config.setSmtp((SmtpMailServiceCf) bean);
				else if(bean instanceof SslCf)
					config.setSslCf((SslCf) bean);
				else if(bean instanceof RootOrganisationBean) {
					RootOrganisationBean rob = (RootOrganisationBean)bean;
					RootOrganisationCf rootOrganisationCf = config.getRootOrganisation();
					rob.copyToCf(rootOrganisationCf);
					config.setRootOrganisation(rootOrganisationCf);
				}
			}
			manager.setJFireServerConfigModule(config);
		}

		JFireServerConfigModule config = manager.getJFireServerConfigModule();
		RootOrganisationCf rootOrganisationCf = config.getRootOrganisation();
		addContent(req, "/jsp/configeditheader.jsp");
		// TODO locale
		addContent(req, "/beanedit", new BeanEditRequest(req, BeanEditServlet.startEdit(req, config.getLocalServer(), new ServerConfigBeanInfo(ServerCf.class, Locale.getDefault()))));
		addContent(req, "/beanedit", new BeanEditRequest(req, BeanEditServlet.startEdit(req, config.getDatabase())));
		addContent(req, "/beanedit", new BeanEditRequest(req, BeanEditServlet.startEdit(req, config.getJ2ee())));
		addContent(req, "/beanedit", new BeanEditRequest(req, BeanEditServlet.startEdit(req, config.getJdo())));
		addContent(req, "/beanedit", new BeanEditRequest(req, BeanEditServlet.startEdit(req, config.getSmtp())));
		addContent(req, "/beanedit", new BeanEditRequest(req, BeanEditServlet.startEdit(req, config.getSslCf())));
		RootOrganisationBean rootOrganisationBean = new RootOrganisationBean();
		rootOrganisationBean.copyFromCf(rootOrganisationCf);
		addContent(req, "/beanedit", new BeanEditRequest(req, BeanEditServlet.startEdit(req, rootOrganisationBean)));
		addContent(req, "/jsp/configeditfooter.jsp");
	}
}
