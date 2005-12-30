/*
 * Created on Sep 28, 2004
 */
package org.nightlabs.jfire.base.login;

import java.util.ArrayList;

import org.nightlabs.config.ConfigModule;
import org.nightlabs.config.InitException;

/**
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 */
public class LoginConfigModule extends ConfigModule 
{
    
    private String organisationID = null;
		private String serverURL = null;
    private String initialContextFactory = null;
    private String securityProtocol = null;
//    private String loginModuleName = null;
    private String userID = null;
    
    private String workstationID;
    private boolean automaticUpdate;
    
    private ArrayList securityConfigurations = null;
    
    
    
    public void init() throws InitException {
        super.init();
        
        if(workstationID == null)
          workstationID = "";
        
        if (organisationID == null)
        	organisationID = "";
        
        if (userID == null)
        	userID = "";
        
        // default values
    		if(initialContextFactory == null)
    			initialContextFactory = "org.jboss.security.jndi.LoginInitialContextFactory";
        if (securityProtocol == null || "".equals(securityProtocol))
          securityProtocol = "jfire";
    		if(serverURL == null)
    			serverURL = "jnp://localhost:1099";
        
        if (securityConfigurations == null) {
        	securityConfigurations = new ArrayList();
        	securityConfigurations.add(
        		new JFireSecurityConfigurationEntry(
        			"jfire",
							"org.jboss.security.ClientLoginModule"							
        		)
        	);
        }
    }   
    
		
		public String getOrganisationID() {
			return organisationID;
		}
		public void setOrganisationID(String organisationID) {
			this.organisationID = organisationID;
			setChanged();
		}
    public String getInitialContextFactory() {
			return initialContextFactory;
		}
		public void setInitialContextFactory(String initialContextFactory) {
			this.initialContextFactory = initialContextFactory;
			setChanged();
		}
		public String getServerURL() {
			return serverURL;
		}
		public void setServerURL(String serverURL) {
			this.serverURL = serverURL;
			setChanged();
		}		
		public String getSecurityProtocol() {
			return securityProtocol;
		}
		public void setSecurityProtocol(String securityProtocol) {
			this.securityProtocol = securityProtocol;
			setChanged();
		}		
		public ArrayList getSecurityConfigurations() {
			return securityConfigurations;
		}
		public void setSecurityConfigurations(ArrayList securityConfigurations) {
			this.securityConfigurations = securityConfigurations;
			setChanged();
		}
		
		public String getUserID() {
			return userID;
		}
		public void setUserID(String userID) {
			this.userID = userID;
			setChanged();
		}


    public String getWorkstationID()
    {
      return workstationID;
    }


    public void setWorkstationID(String workstationID)
    {
      this.workstationID = workstationID;
      setChanged();
    }


    public boolean getAutomaticUpdate()
    {
      return automaticUpdate;
    }


    public void setAutomaticUpdate(boolean automaticUpdate)
    {
      this.automaticUpdate = automaticUpdate;
      setChanged();
    }
}
