/*
 * Created 	on Oct 5, 2004
 * 					by Alexander Bieber
 *
 */
package org.nightlabs.jfire.test.util;

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
