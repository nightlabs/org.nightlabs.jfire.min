package org.nightlabs.jfire.serverupdate.base.db;

import java.util.regex.Pattern;

public class JDBCConfiguration 
{
	private String jndiName;
	private String databaseURL;
	private String driverClass;
	private String userName;
	private String password;
	
	public JDBCConfiguration(String jndiName, String databaseURL, String driverClass, String userName, String password) {
		this.jndiName = jndiName;
		this.databaseURL = databaseURL;
		this.driverClass = driverClass;
		this.userName = userName;
		this.password = password;
	}

	public String getDatabaseURL() {
		return databaseURL;
	}

	public String getDriverClass() {
		return driverClass;
	}

	public String getUserName() {
		return userName;
	}

	public String getPassword() {
		return password;
	}
	
	public String getJndiName() {
		return jndiName;
	}
	
	public String getStrippedJndiName() {
		String jndiStripped = jndiName.replaceAll("jfire\\/datasource\\/", "");
		jndiStripped = jndiStripped.replaceAll("\\/local-tx", "");
		return Pattern.compile("[^a-zA-Z0-9#\\.\\$!_\\-/:+]").matcher(jndiStripped).replaceAll("_").toLowerCase();
	}
}