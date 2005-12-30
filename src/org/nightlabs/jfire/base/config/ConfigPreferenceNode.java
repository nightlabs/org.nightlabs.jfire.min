/*
 * Created 	on May 31, 2005
 * 					by alex
 *
 */
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
