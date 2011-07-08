package org.nightlabs.jfire.web.admin;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 * @author Khaled - khaled[at]nightlabs[dot]de
 */
public class Util
{
	public static String getParameter(HttpServletRequest request, String name)
	{
		String x = request.getParameter(name);
		if(x == null || x.isEmpty())
			throw new UserInputException(
					String.format("Invalid parameter: '%s'",name), 
					String.format("Invalid parameter: '%s'",name));
		return x;
	}
	
	public static boolean haveParameter(HttpServletRequest request, String name)
	{
		try {
			getParameter(request, name);
			return true;
		} catch(Throwable e) {
			return false;
		}
	}

	public static int getParameterAsInt(HttpServletRequest request, String name)
	{
		try {
			return Integer.parseInt(getParameter(request, name));
		} catch(Throwable e) {
			throw new UserInputException(
					String.format("Invalid parameter: '%s'",name), 
					String.format("Invalid parameter: '%s'",name), 
					e);
		}
	}

	/**
	 * Return request parameter as boolean. The string values "true" and "false"
	 * are supported as well as numbers where 0 stands for <code>false</code> and 
	 * all other values for <code>true</code>. Additionally, <code>null</code> and
	 * the empty string will produce <code>false</code>.
	 * @param request The request
	 * @param name The parameter name
	 * @return The parameter as boolean.
	 */
	public static boolean getParameterAsBoolean(HttpServletRequest request, String name)
	{
		String value = request.getParameter(name);
		if(value == null || value.isEmpty())
			return false;
		try {
			return Boolean.parseBoolean(value);
		} catch(Throwable e) {
			int n = getParameterAsInt(request, name);
			return n != 0;
		}
	}
	
	public static boolean haveParameterAsInt(HttpServletRequest request, String name)
	{
		try {
			getParameterAsInt(request, name);
			return true;
		} catch(Throwable e) {
			return false;
		}
	}

	public static boolean haveParameterValue(HttpServletRequest request, String name, String value)
	{
		String v = request.getParameter(name);
		return v != null && v.equals(value);
	}
}