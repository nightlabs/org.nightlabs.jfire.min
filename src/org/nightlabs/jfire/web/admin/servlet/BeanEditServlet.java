package org.nightlabs.jfire.web.admin.servlet;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.nightlabs.jfire.web.admin.beaninfo.ExtendedBeanInfo;
import org.nightlabs.jfire.web.admin.beaninfo.ExtendedPropertyDescriptor;

/**
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public class BeanEditServlet extends HttpServlet
{
	/**
	 * The serial version of this class.
	 */
	private static final long serialVersionUID = 1L;

	private static final Logger log = Logger.getLogger(BeanEditServlet.class);

	public static final String BEANKEY_ATTRIBUTE_KEY = "beanedit.beankey";
	public static final String BEANINFO_ATTRIBUTE_KEY = "beanedit.beaninfo";
	public static final String BEAN_ATTRIBUTE_KEY = "beanedit.bean";
	
	public static final String BEANS_ATTRIBUTE_KEY = "beanedit.beans";
	public static final String BEANINFOS_ATTRIBUTE_KEY = "beanedit.beaninfos";
	
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
		Integer beanKey = (Integer) req.getAttribute(BEANKEY_ATTRIBUTE_KEY);
		log.info("BeanKey to show: "+beanKey);
		
		// no bean key found
		if(beanKey == null)
			throw new IllegalStateException("No bean key");
		
		// TODO: implement saving here for parameter beankey
		
		// prepare bean and show jsp
		Map<Integer, Object> beans = getBeans(req);
		if(beans == null)
			throw new IllegalStateException("No beans known");
		Object bean = beans.get(beanKey);
		if(bean == null)
			throw new IllegalStateException("Bean not found for key "+beanKey);
		
		Map<Integer, ExtendedBeanInfo> beanInfos = getBeanInfos(req);
		if(beanInfos == null)
			throw new IllegalStateException("No bean infos known");
		ExtendedBeanInfo beanInfo = beanInfos.get(beanKey);
		if(beanInfo == null)
			throw new IllegalStateException("Bean info not found for key "+beanKey);
		
		req.setAttribute(BEAN_ATTRIBUTE_KEY, bean);
		req.setAttribute(BEANINFO_ATTRIBUTE_KEY, beanInfo);
		
		req.getRequestDispatcher("/jsp/beanedit.jsp").include(req, resp);
	}

	private static ExtendedBeanInfo getExtendedBeanInfo(Object bean) 
	{
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

	/**
	 * @return The bean key to be included as request attribute
	 */
	public static int startEdit(HttpServletRequest req, Object bean)
	{
		return startEdit(req, bean, null);
	}
	
	/**
	 * @return The bean key to be included as request attribute
	 */
	public static int startEdit(HttpServletRequest req, Object bean, ExtendedBeanInfo beanInfo)
	{
		log.info("Start edit bean: "+bean);
		Map<Integer, Object> beans = getBeans(req);
		if(beans == null) {
			beans = new HashMap<Integer, Object>();
			req.getSession().setAttribute(BEANS_ATTRIBUTE_KEY, beans);
		}
		
		Map<Integer, ExtendedBeanInfo> beanInfos = getBeanInfos(req);
		if(beanInfos == null) {
			beanInfos = new HashMap<Integer, ExtendedBeanInfo>();
			req.getSession().setAttribute(BEANINFOS_ATTRIBUTE_KEY, beanInfos);
		}
		
		int beanKey;
		do {
			beanKey = Math.abs(random.nextInt());
		} while(beans.containsKey(beanKey));
		
		beans.put(beanKey, bean);
		req.setAttribute(BEANKEY_ATTRIBUTE_KEY, beanKey);
		
		if(beanInfo == null)
			beanInfo = getExtendedBeanInfo(bean);
		beanInfos.put(beanKey, beanInfo);
		req.setAttribute(BEANINFOS_ATTRIBUTE_KEY, beanInfo);
		
		return beanKey;
	}

	@SuppressWarnings("unchecked")
	private static Map<Integer, Object> getBeans(HttpServletRequest req) 
	{
		return (Map<Integer, Object>) req.getSession().getAttribute(BEANS_ATTRIBUTE_KEY);
	}
	
	@SuppressWarnings("unchecked")
	private static Map<Integer, ExtendedBeanInfo> getBeanInfos(HttpServletRequest req) 
	{
		return (Map<Integer, ExtendedBeanInfo>) req.getSession().getAttribute(BEANINFOS_ATTRIBUTE_KEY);
	}
	
	
	private static Set<Integer> findBeanKeys(HttpServletRequest req)
	{
		HashSet<Integer> beanKeys = new HashSet<Integer>();
		Enumeration<?> parameterNames = req.getParameterNames();
		Pattern p = Pattern.compile("beanedit\\.(\\d+)\\.value\\.(.*)");
		while(parameterNames.hasMoreElements()) {
			String s = (String)parameterNames.nextElement();
			Matcher m = p.matcher(s);
			if(m.matches())
				beanKeys.add(Integer.valueOf(m.group(1)));
		}
		return beanKeys;
	}
	
	/**
	 * Save data to the bean. This is done by using the request parameters.
	 * The bean key must also be available in the request parameters.
	 * @param req The request
	 * @return the saved beans by bean key
	 */
	public static Map<Integer, Object> finishEdit(HttpServletRequest req)
	{
		log.info("Finish edit beans");
		
		Set<Integer> beanKeys = findBeanKeys(req);
		if(beanKeys.isEmpty()) {
			log.error("No bean keys found in request. Nothing to do.");
			return Collections.emptyMap();
		}
			
		Map<Integer, Object> beans = getBeans(req);
		if(beans == null)
			throw new IllegalStateException("No beans known");
		
//		String beanKey = req.getParameter("beanedit.beankey");
//		if(beanKey == null)
//			throw new IllegalStateException("Invalid finish edit request: Parameter beanedit.beankey not found");
//		int n;
//		try {
//			n = Integer.parseInt(beanKey);
//		} catch(Throwable e) {
//			throw new IllegalStateException("Invalid finish edit request: Parameter beanedit.beankey invalid", e);
//		}

		Map<Integer, Object> result = new HashMap<Integer, Object>(beanKeys.size());
		
		for(Integer beanKey : beanKeys) {
			log.info("Saving bean for key: "+beanKey);
			try {
				Object bean = beans.get(beanKey);
				if(bean == null)
					throw new IllegalStateException("Invalid finish edit request: Bean unknown for key "+beanKey);
				saveBean(bean, beanKey, req);
				result.put(beanKey, bean);
			} catch(Throwable e) {
				throw new RuntimeException("Saving bean failed", e);
			} finally {
				beans.remove(beanKey);
			}
		}
		
		return result;
	}
	
	private static void saveBean(Object bean, int beanKey, HttpServletRequest req) throws IllegalAccessException, InvocationTargetException, IntrospectionException
	{
		log.info("Saving bean: "+bean.getClass().getName());
		ExtendedBeanInfo beanInfo = getExtendedBeanInfo(bean);
		Map<String, ExtendedPropertyDescriptor> epds = beanInfo.getExtendedPropertyDescriptorsByName();
		Enumeration<?> parameterNames = req.getParameterNames();
		String valueParameterPrefix = "beanedit."+beanKey+".value.";
		while(parameterNames.hasMoreElements()) {
			String name = (String)parameterNames.nextElement();
			if(name.startsWith(valueParameterPrefix)) {
				String propertyName = name.substring(valueParameterPrefix.length());
				ExtendedPropertyDescriptor epd = epds.get(propertyName);
				Object realValue = getRealValue(bean, epd, req.getParameter(name));
				epd.setValue(bean, realValue);
			}
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
