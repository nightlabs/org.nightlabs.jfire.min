package org.nightlabs.jfire.base.login;

import java.io.Serializable;

import org.nightlabs.util.Util;

public class LoginProfile implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private String username;
	private String organisationID;
	private String serverURL;
	private String initialContextFactory;
	private String workstationID;
	
	public LoginProfile(String username, String organisationID, String serverURL, String initialContextFactory, String workstationID) {
		super();
		this.username = username;
		this.organisationID = organisationID;
		this.serverURL = serverURL;
		this.initialContextFactory = initialContextFactory;
		this.workstationID = workstationID;
	}

	/**
	 * @return the username
	 */
	public String getUsername() {
		return username;
	}
	/**
	 * @return the organisationID
	 */
	public String getOrganisationID() {
		return organisationID;
	}
	/**
	 * @return the serverURL
	 */
	public String getServerURL() {
		return serverURL;
	}
	/**
	 * @return the initialContextFactory
	 */
	public String getInitialContextFactory() {
		return initialContextFactory;
	}
	/**
	 * @return the workstationID
	 */
	public String getWorkstationID() {
		return workstationID;
	}
	
	public String getLabel() {
		return username + "@" + organisationID + " (" + workstationID + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((initialContextFactory == null) ? 0 : initialContextFactory.hashCode());
		result = prime * result + ((organisationID == null) ? 0 : organisationID.hashCode());
		result = prime * result + ((serverURL == null) ? 0 : serverURL.hashCode());
		result = prime * result + ((username == null) ? 0 : username.hashCode());
		result = prime * result + ((workstationID == null) ? 0 : workstationID.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final LoginProfile o = (LoginProfile) obj;
		return
				Util.equals(o.initialContextFactory, this.initialContextFactory) &&
				Util.equals(o.organisationID, this.organisationID) &&
				Util.equals(o.serverURL, this.serverURL) &&
				Util.equals(o.username, this.username) &&
				Util.equals(o.workstationID, this.workstationID);
	}
}
