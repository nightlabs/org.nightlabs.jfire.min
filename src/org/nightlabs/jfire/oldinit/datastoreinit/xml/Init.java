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

package org.nightlabs.jfire.oldinit.datastoreinit.xml;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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

	private List<Dependency> dependencies = new ArrayList<Dependency>();

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
		if (priority < 0 || priority > 999)
			throw new IllegalArgumentException("priority out of range! must be 0..999");

		this.priority = priority;
	}
	
	public void addDependency(Dependency dependency)
	{
		this.dependencies.add(dependency);
	}
	/**
	 * @return Returns the dependencies.
	 */
	public Collection<Dependency> getDependencies()
	{
		return Collections.unmodifiableList(dependencies);
	}
	
	public void replaceDependency(Dependency oldDependency, Collection<Dependency> newDependencies)
	{
		int idx = dependencies.indexOf(oldDependency);
		if (idx >= 0)
			dependencies.remove(idx);

		dependencies.addAll(idx, newDependencies);
	}

	@Override
	public String toString()
	{
		StringBuffer dependenciesSB = new StringBuffer();
		dependenciesSB.append('[');

		for (Dependency dependency : dependencies)
			dependenciesSB.append(dependency.toStringWithoutInit());

		dependenciesSB.append(']');

		return this.getClass().getName() + '['
			+ "module=" + datastoreInitMan.getJFireEAR() + ','
			+ "archive=" + datastoreInitMan.getJFireJAR() + ','
			+ "bean=" + bean + ','
			+ "method=" + method + ','
			+ "dependencies=" + dependenciesSB.toString()
			+ ']';
	}

	public String toStringWithoutDependencies()
	{
		return this.getClass().getName() + '['
		+ "module=" + datastoreInitMan.getJFireEAR() + ','
		+ "archive=" + datastoreInitMan.getJFireJAR() + ','
		+ "bean=" + bean + ','
		+ "method=" + method + ']';
	}

}
