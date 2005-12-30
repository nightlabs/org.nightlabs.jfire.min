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

package org.nightlabs.jfire.base.login;

import java.io.Serializable;
import java.util.HashMap;

/**
 * @author Alexander Bieber
 */
public class JFireSecurityConfigurationEntry implements Serializable {
	public static final String MODULE_CONTROL_FLAG_REQUIRED = "required";
	public static final String MODULE_CONTROL_FLAG_REQUISITE  = "requisite";
	public static final String MODULE_CONTROL_FLAG_SUFFICIENT = "sufficient";
	public static final String MODULE_CONTROL_FLAG_OPTIONAL = "optional";
	public static final String MODULE_CONTROL_FLAG_NONE = "";
	
	
	private String applicationName = "";
	private String loginModuleName = "";
	private String controlFlag = null;
	private HashMap options = null;
	
	public JFireSecurityConfigurationEntry() { }
	
	public JFireSecurityConfigurationEntry(String applicationName, String loginModule){
		this(applicationName,loginModule,MODULE_CONTROL_FLAG_NONE,null);
	}
	
	public JFireSecurityConfigurationEntry(String applicationName, String loginModule, String controlFlag){
		this(applicationName,loginModule,controlFlag,null);
	}
	
	public JFireSecurityConfigurationEntry(String applicationName, String loginModule, String controlFlag, HashMap options){
		this.applicationName = applicationName;
		this.loginModuleName = loginModule;
		this.controlFlag = controlFlag;
		if (options == null)
			this.options = new HashMap();
		else
			this.options = options;
	}
	
	
	
	public String getApplicationName() {
		return applicationName;
	}
	public void setApplicationName(String applicationName) {
		this.applicationName = applicationName;
	}
	public String getControlFlag() {
		return controlFlag;
	}
	public void setControlFlag(String controlFlag) {
		this.controlFlag = controlFlag;
	}
	public String getLoginModuleName() {
		return loginModuleName;
	}
	public void setLoginModuleName(String loginModuleName) {
		this.loginModuleName = loginModuleName;
	}
	public HashMap getOptions() {
		return options;
	}
	public void setOptions(HashMap options) {
		this.options = options;
	}
}
