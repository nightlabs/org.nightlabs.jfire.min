package org.nightlabs.jfire.dashboard;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class DashboardGadgetClientScriptsConfig implements Serializable {

	private static final long serialVersionUID = 20111227L;

	private List<ClientScript> clientScripts;
	
	private boolean confirmProcessing;
	
	public DashboardGadgetClientScriptsConfig() { 
		clientScripts = new ArrayList<DashboardGadgetClientScriptsConfig.ClientScript>();
	}

	public static class ClientScript {
		
		private String name;
		private String script;
		
		public ClientScript() {
			this.name = "";
			this.script = "";
		}
		
		public ClientScript(final String name, final String script) {
			this.name = name;
			this.script = script;
		}

		public String getName() {
			return name;
		}

		public String getScript() {
			return script;
		}
	}
	
	public ClientScript createNewClientScript(final String name, final String script) {
		return new ClientScript(name, script);
	}

	public List<DashboardGadgetClientScriptsConfig.ClientScript> getClientScripts() {
		return clientScripts;
	}

	public void setClientScripts(final List<DashboardGadgetClientScriptsConfig.ClientScript> clientScripts) {
		this.clientScripts = clientScripts;
	}

	public boolean isConfirmProcessing() {
		return confirmProcessing;
	}

	public void setConfirmProcessing(final boolean confirmProcessing) {
		this.confirmProcessing = confirmProcessing;
	}
}
