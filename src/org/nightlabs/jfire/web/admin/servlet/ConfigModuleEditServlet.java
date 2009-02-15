package org.nightlabs.jfire.web.admin.servlet;

import java.beans.IntrospectionException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.nightlabs.jfire.web.admin.beaninfo.BeanInfoUtil;
import org.nightlabs.jfire.web.admin.beaninfo.ExtendedPropertyDescriptor;

/**
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public abstract class ConfigModuleEditServlet extends HttpServlet
{
	/**
	 * The serial version of this class.
	 */
	private static final long serialVersionUID = 1L;

	protected abstract Object getConfigModule();
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException 
	{
		handleRequest(req, resp);
	}
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException 
	{
		handleRequest(req, resp);
	}
	
	protected void handleRequest(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
	{
		Object configModule = getConfigModule();
		ExtendedPropertyDescriptor[] extendedPropertyDescriptors;
		try {
			extendedPropertyDescriptors = BeanInfoUtil.getExtendedPropertyDescriptors(configModule.getClass(), Locale.getDefault());
		} catch (IntrospectionException e) {
			throw new RuntimeException(e);
		}
		req.setAttribute("beanedit.propertydescriptors", extendedPropertyDescriptors);
		req.getRequestDispatcher("/jsp/configmoduleedit.jsp").include(req, resp);
	}
	
	private static Random random = new Random();
	
	public static void startEdit(HttpServletRequest req, Object bean)
	{
		System.out.println("start edit: "+bean);
		Map<Integer, Object> beans = getBeans(req);
		if(beans == null) {
			beans = new HashMap<Integer, Object>();
			req.getSession().setAttribute("beanedit.beans", beans);
		}
		while(true) {
			int n = random.nextInt();
			if(!beans.containsKey(n)) {
				beans.put(n, bean);
				req.setAttribute("beanedit.beankey", bean);
				break;
			}
		}
	}

	@SuppressWarnings("unchecked")
	private static Map<Integer, Object> getBeans(HttpServletRequest req) 
	{
		return (Map<Integer, Object>) req.getSession().getAttribute("beanedit.beans");
	}
	
	public static void finishEdit(HttpServletRequest req)
	{
		System.out.println("finish edit");
		Map<Integer, Object> beans = getBeans(req);
		if(beans == null)
			throw new IllegalStateException("No beans known");
		String beanKey = req.getParameter("beanedit.beankey");
		if(beanKey == null)
			throw new IllegalStateException("Invalid finish edit request: Parameter beanedit.beankey not found");
		int n;
		try {
			n = Integer.parseInt(beanKey);
		} catch(Throwable e) {
			throw new IllegalStateException("Invalid finish edit request: Parameter beanedit.beankey invalid", e);
		}
		try {
			Object bean = beans.get(n);
			// TODO save bean
			System.out.println("SAVE BEAN: "+bean.getClass().getName());
		} catch(Throwable e) {
			throw new RuntimeException("Saving bean failed", e);
		} finally {
			beans.remove(n);
		}
	}
}
