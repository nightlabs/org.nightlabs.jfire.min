package org.nightlabs.jfire.base.login;

import java.io.Serializable;

import org.nightlabs.config.Initializable;

/**
 * This class holds a single login configuration without the password. It is intended to be used in {@link LoginConfigModule} to
 * be able to store multiple login identities to let the user select the one to be used for the next login.
 * 
 * @author Tobias Langner <!-- tobias[dot]langner[at]nightlabs[dot]de -->
 */
public class LoginConfiguration implements Serializable, Initializable {

	private static final long serialVersionUID = 4L;

	private String userID = null;
	private String workstationID;
	private String organisationID = null;
	private String serverURL = null;
	private String initialContextFactory = null;
	private String securityProtocol = null;
	private boolean automaticUpdate = false;
	
	private String configurationName = null;

	public LoginConfiguration() {
		this(null, null, null, null, null, null, null);
	}

	/**
	 * 
	 * TODO amend for use with automatic update
	 * 
	 * @param userID
	 * @param workstationID
	 * @param organisationID
	 * @param serverURL
	 * @param initialContextFactory
	 * @param securityProtocol
	 * @param securityConfigurationEntries
	 */
	public LoginConfiguration(String userID, String workstationID, String organisationID, String serverURL, String initialContextFactory,
			String securityProtocol, String configurationName)
	{
		this.userID = userID;
		this.workstationID = workstationID;
		this.organisationID = organisationID;
		this.serverURL = serverURL;
		this.initialContextFactory = initialContextFactory;
		this.securityProtocol = securityProtocol;
		this.configurationName = configurationName;
	}

	/**
	 * Using the init method additionally, it will cause the default values to be written to the ConfigModule.
	 */
	public void init()
	{
		if(workstationID == null)
			workstationID = ""; //$NON-NLS-1$

		if (organisationID == null)
			organisationID = ""; //$NON-NLS-1$

		if (userID == null)
			userID = ""; //$NON-NLS-1$

//		default values
		if(initialContextFactory == null)
			initialContextFactory = "org.jboss.security.jndi.LoginInitialContextFactory"; //$NON-NLS-1$
		if (securityProtocol == null || "".equals(securityProtocol)) //$NON-NLS-1$
			securityProtocol = "jfire"; //$NON-NLS-1$
		if(serverURL == null)
			serverURL = "jnp://localhost:1099"; //$NON-NLS-1$
	}

	public boolean isAutomaticUpdate() {
		return automaticUpdate;
	}

	public void setAutomaticUpdate(boolean automaticUpdate) {
		this.automaticUpdate = automaticUpdate;
	}

	public String getInitialContextFactory() {
		return initialContextFactory;
	}

	public void setInitialContextFactory(String initialContextFactory) {
		this.initialContextFactory = initialContextFactory;
	}

	public String getOrganisationID() {
		return organisationID;
	}

	public void setOrganisationID(String organisationID) {
		this.organisationID = organisationID;
	}

	public String getSecurityProtocol() {
		return securityProtocol;
	}

	public void setSecurityProtocol(String securityProtocol) {
		this.securityProtocol = securityProtocol;
	}

	public String getServerURL() {
		return serverURL;
	}

	public void setServerURL(String serverURL) {
		this.serverURL = serverURL;
	}

	public String getUserID() {
		return userID;
	}

	public void setUserID(String userID) {
		this.userID = userID;
	}

	public String getWorkstationID() {
		return workstationID;
	}

	public void setWorkstationID(String workstationID) {
		this.workstationID = workstationID;
	}
	
	public String getConfigurationName() {
		return configurationName;
	}
	
	public void setConfigurationName(String configurationName) {
		this.configurationName = configurationName;
	}

	@Override
	public String toString() {
		if (configurationName == null || "".equals(configurationName))
			return userID + "@" + organisationID + " (" + workstationID + ") (" + serverURL + ")";
		else
			return configurationName;
	}
	
	public String toShortString() {
		if (configurationName == null || "".equals(configurationName))
			return configurationName;
		
		String _serverURL = serverURL.substring(serverURL.indexOf("://")+3);
		return shorten(userID, 8) +	"@" + shorten(organisationID, 8) + " (" + shorten(workstationID, 8) + ") (" + shorten(_serverURL, 10) + ")";
	}
	
	public String shorten(String target, int count) {
		if (target.length() <= count)
			return target;
		
		int tailCount = count/2;
		int headCount = count - tailCount;
		String front = target.substring(0, headCount);
		String tail = target.substring(target.length()-tailCount);
		return front + ".." + tail;
	}

	@Override
	public int hashCode() {
		final int PRIME = 31;
		int result = 1;
		result = PRIME * result + ((organisationID == null) ? 0 : organisationID.hashCode());
		result = PRIME * result + ((serverURL == null) ? 0 : serverURL.hashCode());
		result = PRIME * result + ((userID == null) ? 0 : userID.hashCode());
		result = PRIME * result + ((workstationID == null) ? 0 : workstationID.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final LoginConfiguration other = (LoginConfiguration) obj;
		if (organisationID == null) {
			if (other.organisationID != null)
				return false;
		} else if (!organisationID.equals(other.organisationID))
			return false;
		if (serverURL == null) {
			if (other.serverURL != null)
				return false;
		} else if (!serverURL.equals(other.serverURL))
			return false;
		if (userID == null) {
			if (other.userID != null)
				return false;
		} else if (!userID.equals(other.userID))
			return false;
		if (workstationID == null) {
			if (other.workstationID != null)
				return false;
		} else if (!workstationID.equals(other.workstationID))
			return false;
		return true;
	}
}