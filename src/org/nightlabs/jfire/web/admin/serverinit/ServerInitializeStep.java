package org.nightlabs.jfire.web.admin.serverinit;

import java.util.Map;

public class ServerInitializeStep 
{
	private String name;
	private String forward;
	private Object bean;
	private PopulateListener populateListener;

	public ServerInitializeStep(String name, String forward, Object bean) 
	{
		this(name, forward, bean, null);
	}
	
	public ServerInitializeStep(String name, String forward, Object bean, PopulateListener populateListener) 
	{
		if(name == null)
			throw new NullPointerException("name");
		if(forward == null && bean == null)
			throw new IllegalArgumentException("Either forward or bean must be non-null: "+name);
		if(forward != null && bean != null)
			throw new IllegalArgumentException("Either forward or bean must be null: "+name);
		this.name = name;
		this.forward = forward;
		this.bean = bean;
		this.populateListener = populateListener;
	}

	public String getName() {
		return name;
	}

	public String getForward() {
		return forward;
	}

	public Object getBean() {
		return bean;
	}
	
	public PopulateListener getPopulateListener()
	{
		return populateListener;
	}
	
	public static interface PopulateListener
	{
		void afterPopulate(Object bean);
	}
}