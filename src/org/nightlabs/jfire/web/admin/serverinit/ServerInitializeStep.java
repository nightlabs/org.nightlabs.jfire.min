package org.nightlabs.jfire.web.admin.serverinit;

public class ServerInitializeStep 
{
	private String name;
	private String forward;
	private Object bean;

	public ServerInitializeStep(String name, String forward, Object bean) 
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
}