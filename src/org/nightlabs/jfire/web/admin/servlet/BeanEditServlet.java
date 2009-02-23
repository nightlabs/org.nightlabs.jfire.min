package org.nightlabs.jfire.web.admin.servlet;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.beanutils.BeanUtils;
import org.nightlabs.jfire.web.admin.beaninfo.ExtendedBeanInfo;
import org.nightlabs.jfire.web.admin.beaninfo.ExtendedPropertyDescriptor;

/**
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public class BeanEditServlet extends HttpServlet
{
	private static final String BEANEDIT_VALUE_PARAMETER_PREFIX = "beanedit.value.";
	/**
	 * The serial version of this class.
	 */
	private static final long serialVersionUID = 1L;

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
		Integer beanKey = (Integer) req.getAttribute("beanedit.beankey");
		if(beanKey == null)
			throw new IllegalStateException();
		// TODO: implement saving here for parameter beankey
		Map<Integer, Object> beans = getBeans(req);
		if(beans == null)
			throw new IllegalStateException("No beans known");
		Object bean = beans.get(beanKey);
		if(bean == null)
			throw new IllegalStateException("Bean not found for key "+beanKey);
		ExtendedBeanInfo beanInfo = getExtendedBeanInfo(bean);
		req.setAttribute("beanedit.beaninfo", beanInfo);
		req.getRequestDispatcher("/jsp/configmoduleedit.jsp").include(req, resp);
	}

	private static ExtendedBeanInfo getExtendedBeanInfo(Object bean) {
		ExtendedBeanInfo beanInfo;
		try {
			BeanInfo baseBeanInfo = Introspector.getBeanInfo(bean.getClass());
			// TODO use correct locale
			beanInfo = new ExtendedBeanInfo(baseBeanInfo, Locale.getDefault());
		} catch (IntrospectionException e) {
			throw new RuntimeException(e);
		}
		return beanInfo;
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
				req.setAttribute("beanedit.beankey", n);
				break;
			}
		}
	}

	@SuppressWarnings("unchecked")
	private static Map<Integer, Object> getBeans(HttpServletRequest req) 
	{
		return (Map<Integer, Object>) req.getSession().getAttribute("beanedit.beans");
	}
	
	/**
	 * Save data to the bean. This is done by using the request parameters.
	 * The bean key must also be available in the request parameters.
	 * @param req The request
	 */
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
			if(bean == null)
				throw new IllegalStateException("Invalid finish edit request: Bean unknown for key "+beanKey);
			saveBean(bean, req);
		} catch(Throwable e) {
			throw new RuntimeException("Saving bean failed", e);
		} finally {
			beans.remove(n);
		}
	}
	
	private static void saveBean(Object bean, HttpServletRequest req) throws IllegalAccessException, InvocationTargetException, IntrospectionException
	{
		System.out.println("SAVE BEAN: "+bean.getClass().getName());
		ExtendedBeanInfo beanInfo = getExtendedBeanInfo(bean);
		Map<String, ExtendedPropertyDescriptor> epds = beanInfo.getExtendedPropertyDescriptorsByName();
		Map<String, Object> properties = new HashMap<String, Object>();
		Enumeration<?> parameterNames = req.getParameterNames();
		while(parameterNames.hasMoreElements()) {
			String name = (String)parameterNames.nextElement();
			if(name.startsWith(BEANEDIT_VALUE_PARAMETER_PREFIX)) {
				String propertyName = name.substring(BEANEDIT_VALUE_PARAMETER_PREFIX.length());
				Object realValue = getRealValue(bean, epds.get(propertyName), req.getParameter(name));
				properties.put(propertyName, realValue);
			}
		}
		if(!properties.isEmpty()) {
			System.out.println("VALUES: "+properties);
			BeanUtils.populate(bean, properties);
		}
	}

	private static Object getRealValue(Object bean, ExtendedPropertyDescriptor epd, String parameter) 
	{
		if(epd.getPropertyType() == String.class)
			return parameter;
		else if(epd.getPropertyType() == Boolean.class || epd.getPropertyType() == Boolean.TYPE)
			return Boolean.valueOf(parameter);
		else if(epd.getPropertyType() == Integer.class || epd.getPropertyType() == Integer.TYPE)
			return Integer.valueOf(parameter);
		else
			throw new IllegalArgumentException("NYI: type: "+epd.getPropertyType());
	}
}
