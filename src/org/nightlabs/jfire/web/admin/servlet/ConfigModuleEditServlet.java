package org.nightlabs.jfire.web.admin.servlet;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.beans.PropertyEditor;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.beanutils.PropertyUtils;
import org.nightlabs.jfire.config.ConfigModule;

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

	protected List<PropertyDescriptor> getPropertyDescriptors(Object configModule)
	{
		try {
			Introspector.setBeanInfoSearchPath(new String[] {"org.nightlabs.jfire.web.admin.beaninfo"});
			BeanInfo beanInfo = Introspector.getBeanInfo(configModule.getClass());
			PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
			for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
				System.out.println("property: "+propertyDescriptor.getName());
				PropertyEditor propertyEditor = propertyDescriptor.createPropertyEditor(configModule);
				System.out.println("editor: "+propertyEditor);
			}
			return Arrays.asList(propertyDescriptors);
		} catch (IntrospectionException e) {
			throw new RuntimeException(e);
		}
//		PropertyDescriptor[] propertyDescriptors = PropertyUtils.getPropertyDescriptors(configModule);
//		for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
//			System.out.println("property: "+propertyDescriptor.getName());
//			PropertyEditor propertyEditor = propertyDescriptor.createPropertyEditor(configModule);
//			System.out.println("editor: "+propertyEditor);
//		}
//		return Arrays.asList(propertyDescriptors);
//		Class<? extends ConfigModule> clazz = configModule.getClass();
//		Method[] methods = clazz.getMethods();
//		for (Method method : methods) {
//			String methodName = method.getName();
////			if(methodName.startsWith("get") || methodName.startsWith("is"))
//		}
	}
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		handleRequest(req, resp);
	}
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		handleRequest(req, resp);
	}
	
	protected void handleRequest(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
	{
		Object configModule = getConfigModule();
		req.setAttribute("propertydescriptors", getPropertyDescriptors(configModule));
		req.getRequestDispatcher("/jsp/configmoduleedit.jsp").include(req, resp);
	}
}
