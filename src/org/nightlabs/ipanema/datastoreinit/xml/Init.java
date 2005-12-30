/*
 * Created on Feb 14, 2005
 */
package org.nightlabs.ipanema.datastoreinit.xml;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 */
public class Init
{
	private DatastoreInitMan datastoreInitMan;
	private String bean;
	private String method;
	private int priority;

	private List dependencies = new ArrayList();

	public Init(DatastoreInitMan datastoreInitMan, String bean, String method, int priority)
	{
		this.datastoreInitMan = datastoreInitMan;
		this.bean = bean;
		this.method = method;
		this.priority = priority;
	}
	
	/**
	 * @return Returns the datastoreInitMan.
	 */
	public DatastoreInitMan getDatastoreInitMan()
	{
		return datastoreInitMan;
	}
	/**
	 * @return Returns the bean.
	 */
	public String getBean()
	{
		return bean;
	}
	/**
	 * @param bean The bean to set.
	 */
	public void setBean(String bean)
	{
		this.bean = bean;
	}
	/**
	 * @return Returns the method.
	 */
	public String getMethod()
	{
		return method;
	}
	/**
	 * @param method The method to set.
	 */
	public void setMethod(String method)
	{
		this.method = method;
	}
	/**
	 * @return Returns the priority.
	 */
	public int getPriority()
	{
		return priority;
	}
	/**
	 * @param priority The priority to set.
	 */
	public void setPriority(int priority)
	{
		this.priority = priority;
	}
	
	public void addDependency(Dependency dependency)
	{
		this.dependencies.add(dependency);
	}
	/**
	 * @return Returns the dependencies.
	 */
	public Collection getDependencies()
	{
		return dependencies;
	}
}
