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

package org.nightlabs.jfire.base.config;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.graphics.Image;

/**
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 *
 */
public class ConfigPreferenceNode {

	private String configPreferenceID;
	private String configPreferenceName;
	private AbstractConfigModulePreferencePage preferencePage;
	private String categoryID;
	private ConfigPreferenceNode parent;
	private List children = new ArrayList();
	
	private Image icon;
	
	/**
	 * 
	 */
	public ConfigPreferenceNode(
		String configPreferenceID,
		String configPreferenceName,
		String categoryID,
		ConfigPreferenceNode parent,
		AbstractConfigModulePreferencePage preferencePage
	) {
		super();
		this.configPreferenceID = configPreferenceID;
		this.configPreferenceName = configPreferenceName;
		this.categoryID = categoryID;
		this.parent = parent;
		this.preferencePage = preferencePage;
	}
	
	public ConfigPreferenceNode getParent() {
		return parent;
	}
	
	public void setParent(ConfigPreferenceNode parent) {
		this.parent = parent;
	}
	
	public List getChildren() {
		return children;
	}
	
	public void addChild(ConfigPreferenceNode child) {
		children.add(child);
		child.setParent(this);
	}
	
	public String getConfigPreferenceID() {
		return configPreferenceID;
	}
	
	public void setConfigPreferenceID(String configPreferenceID) {
		this.configPreferenceID = configPreferenceID;
	}
	
	public String getConfigPreferenceName() {
		return configPreferenceName;
	}
	
	public void setConfigPreferenceName(String configPreferenceName) {
		this.configPreferenceName = configPreferenceName;
	}
	
	public String getCategoryID() {
		return categoryID;
	}
	
	public void setCategoryID(String categoryID) {
		this.categoryID = categoryID;
	}
	
	public Image getIcon() {
		return icon;
	}
	
	public void setIcon(Image icon) {
		this.icon = icon;
	}
	
	public AbstractConfigModulePreferencePage getPreferencePage() {
		return preferencePage;
	}

}
