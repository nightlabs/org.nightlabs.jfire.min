package org.nightlabs.jfire.web.admin.beaninfo;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public class ExtendedPropertyDescriptor extends PropertyDescriptor implements Comparable<ExtendedPropertyDescriptor>
{
	private ExtendedBeanInfo beanInfo;
	private int orderHint;

	public ExtendedPropertyDescriptor(ExtendedBeanInfo beanInfo, PropertyDescriptor pd) throws IntrospectionException
	{
		super(pd.getName(), pd.getReadMethod(), pd.getWriteMethod());
		this.beanInfo = beanInfo;
		this.orderHint = 5000;
		String s = getPropertyValue("orderHint");
		System.out.println("order hint: "+s);
		if(s != null) {
			try {
				this.orderHint = Integer.parseInt(s);
			} catch(NumberFormatException e) {
				// ignore
				e.printStackTrace();
			}
		}
	}
	
	public String getLocalizedPropertyValue(String key)
	{
		return beanInfo.getLocalizedValue("property."+getName()+"."+key);
	}
	
	public String getPropertyValue(String key)
	{
		return beanInfo.getValue("property."+getName()+"."+key);
	}
	
	public Boolean getBooleanPropertyValue(String key)
	{
		String b = getPropertyValue(key);
		if(b != null) {
			if("true".equalsIgnoreCase(b))
				return true;
			if("false".equalsIgnoreCase(b))
				return false;
		}
		return null;
	}
	
	public List<String> getPossibleValues()
	{
		String valuesString = getPropertyValue("values");
		if(valuesString == null)
			return null;
		StringTokenizer st = new StringTokenizer(valuesString, ",");
		List<String> result = new ArrayList<String>(st.countTokens());
		while(st.hasMoreTokens())
			result.add(st.nextToken());
		return result;
	}
	
	public String getPossibleValueDisplayName(String valueName)
	{
		String s = getLocalizedPropertyValue(valueName+".displayName");
		return s == null ? valueName : s;
	}
	
	@Override
	public String getDisplayName() 
	{
		String s = getLocalizedPropertyValue("displayName");
		return s == null ? super.getDisplayName() : s;
	}
	
	@Override
	public String getShortDescription() 
	{
		String s = getLocalizedPropertyValue("shortDescription");
		return s == null ? super.getShortDescription() : s;
	}
	
	@Override
	public boolean isHidden() 
	{
		Boolean b = getBooleanPropertyValue("hidden");
		return b == null ? super.isHidden() : b;
	}
	
	public ExtendedBeanInfo getBeanInfo()
	{
		return beanInfo;
	}
	
	public void setOrderHint(int orderHint)
	{
		this.orderHint = orderHint;
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(ExtendedPropertyDescriptor o)
	{
		if(orderHint == o.orderHint)
			return getName().compareTo(o.getName());
		return orderHint - o.orderHint;
	}
}
