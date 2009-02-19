package org.nightlabs.jfire.web.admin.serverinit;

public class FirstOrganisationBean
{
	private String organisationID;
	private String organisationName;
	private String adminUserName;
	private String adminPassword;
	private boolean serverAdmin;
	
	/**
	 * Get the organisationID.
	 * @return the organisationID
	 */
	public String getOrganisationID()
	{
		return organisationID;
	}
	/**
	 * Set the organisationID.
	 * @param organisationID the organisationID to set
	 */
	public void setOrganisationID(String organisationID)
	{
		this.organisationID = organisationID;
	}
	/**
	 * Get the organisationName.
	 * @return the organisationName
	 */
	public String getOrganisationName()
	{
		return organisationName;
	}
	/**
	 * Set the organisationName.
	 * @param organisationName the organisationName to set
	 */
	public void setOrganisationName(String organisationName)
	{
		this.organisationName = organisationName;
	}
	/**
	 * Get the adminUserName.
	 * @return the adminUserName
	 */
	public String getAdminUserName()
	{
		return adminUserName;
	}
	/**
	 * Set the adminUserName.
	 * @param adminUserName the adminUserName to set
	 */
	public void setAdminUserName(String adminUserName)
	{
		this.adminUserName = adminUserName;
	}
	/**
	 * Get the adminPassword.
	 * @return the adminPassword
	 */
	public String getAdminPassword()
	{
		return adminPassword;
	}
	/**
	 * Set the adminPassword.
	 * @param adminPassword the adminPassword to set
	 */
	public void setAdminPassword(String adminPassword)
	{
		this.adminPassword = adminPassword;
	}
	/**
	 * Get the serverAdmin.
	 * @return the serverAdmin
	 */
	public boolean isServerAdmin()
	{
		return serverAdmin;
	}
	/**
	 * Set the serverAdmin.
	 * @param serverAdmin the serverAdmin to set
	 */
	public void setServerAdmin(boolean serverAdmin)
	{
		this.serverAdmin = serverAdmin;
	}
	
	
}
