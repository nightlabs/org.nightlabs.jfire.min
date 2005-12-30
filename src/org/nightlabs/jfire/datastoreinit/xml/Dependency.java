/* *****************************************************************************
 * JFire - it's hot - Free ERP System - http://jfire.org                       *
 * Copyright (C) 2004-2005 NightLabs - http://NightLabs.org                    *
 *                                                                             *
 * This library is free software; you can redistribute it and/or               *
 * modify it under the terms of the GNU Lesser General Public                  *
 * License as published by the Free Software Foundation; either                *
 * version 2.1 of the License, or (at your option) any later version.          *
 *                                                                             *
 * This library is distributed in the hope that it will be useful,             *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of              *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU           *
 * Lesser General Public License for more details.                             *
 *                                                                             *
 * You should have received a copy of the GNU Lesser General Public            *
 * License along with this library; if not, write to the                       *
 *     Free Software Foundation, Inc.,                                         *
 *     51 Franklin St, Fifth Floor,                                            *
 *     Boston, MA  02110-1301  USA                                             *
 *                                                                             *
 * Or get it online :                                                          *
 *     http://opensource.org/licenses/lgpl-license.php                         *
 *                                                                             *
 *                                                                             *
 ******************************************************************************/

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
