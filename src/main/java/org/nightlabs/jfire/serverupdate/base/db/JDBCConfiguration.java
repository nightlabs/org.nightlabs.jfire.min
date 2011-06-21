package org.nightlabs.jfire.serverupdate.base.db;

public class JDBCConfiguration 
{
	private String databaseURL;
	private String driverClass;
	private String userName;
	private String password;
	
	public JDBCConfiguration(String databaseURL, String driverClass, String userName, String password) {
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
}