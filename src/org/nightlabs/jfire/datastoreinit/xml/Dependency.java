/*
 * Created on Feb 14, 2005
 */
package org.nightlabs.jfire.datastoreinit.xml;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 */
public class Dependency
{
	private Init init;
	private String module;
	private String archive;
	private String bean;
	private String method;

	public Dependency(Init init, String module, String archive, String bean, String method)
	{
		if (init == null)
			throw new NullPointerException("init == null!");

		if (module == null)
			throw new NullPointerException("module must be defined!");

		if (archive == null)
			throw new NullPointerException("archive must be defined!");

		if (bean == null)
			throw new NullPointerException("bean must be defined!");

		if (method == null)
			throw new NullPointerException("method must be defined!");

		this.init = init;
		this.module = module;
		this.archive = archive;
		this.bean = bean;
		this.method = method;
	}

	/**
	 * @return Returns the init.
	 */
	public Init getInit()
	{
		return init;
	}
	/**
	 * @return Returns the module.
	 */
	public String getModule()
	{
		return module;
	}
	/**
	 * @param module The module to set.
	 */
	public void setModule(String module)
	{
		this.module = module;
	}
	/**
	 * @return Returns the archive.
	 */
	public String getArchive()
	{
		return archive;
	}
	/**
	 * @param archive The archive to set.
	 */
	public void setArchive(String archive)
	{
		this.archive = archive;
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
}
