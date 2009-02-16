package org.nightlabs.jfire.web.admin.beaninfo;

import java.beans.BeanDescriptor;
import java.util.Enumeration;

public class ExtendedBeanDescriptor extends BeanDescriptor 
{
	private BeanDescriptor baseBeanDescriptor;
	private ExtendedBeanInfo beanInfo;
	private String name;
	
	public ExtendedBeanDescriptor(ExtendedBeanInfo beanInfo, BeanDescriptor baseBeanDescriptor) 
	{
		super(baseBeanDescriptor.getBeanClass());
		this.baseBeanDescriptor = baseBeanDescriptor;
		this.beanInfo = beanInfo;
		if(this.name != null)
			this.baseBeanDescriptor.setName(name);
	}

	public Enumeration<String> attributeNames() {
		return baseBeanDescriptor.attributeNames();
	}

	public Class<?> getBeanClass() {
		return baseBeanDescriptor.getBeanClass();
	}

	public Class<?> getCustomizerClass() {
		return baseBeanDescriptor.getCustomizerClass();
	}

	public String getDisplayName() 
	{
		String s = beanInfo.getLocalizedValue("displayName"); 
		return s == null ? baseBeanDescriptor.getDisplayName() : s;
	}

	public String getName() {
		return baseBeanDescriptor.getName();
	}

	public String getShortDescription() {
		return baseBeanDescriptor.getShortDescription();
	}

	public Object getValue(String attributeName) {
		return baseBeanDescriptor.getValue(attributeName);
	}

	public boolean isExpert() {
		return baseBeanDescriptor.isExpert();
	}

	public boolean isHidden() {
		return baseBeanDescriptor.isHidden();
	}

	public boolean isPreferred() {
		return baseBeanDescriptor.isPreferred();
	}

	public void setDisplayName(String displayName) {
		baseBeanDescriptor.setDisplayName(displayName);
	}

	public void setExpert(boolean expert) {
		baseBeanDescriptor.setExpert(expert);
	}

	public void setHidden(boolean hidden) {
		baseBeanDescriptor.setHidden(hidden);
	}

	public void setName(String name) 
	{
		this.name = name;
		if(baseBeanDescriptor != null)
			baseBeanDescriptor.setName(name);
	}

	public void setPreferred(boolean preferred) {
		baseBeanDescriptor.setPreferred(preferred);
	}

	public void setShortDescription(String text) {
		baseBeanDescriptor.setShortDescription(text);
	}

	public void setValue(String attributeName, Object value) {
		baseBeanDescriptor.setValue(attributeName, value);
	}
}
