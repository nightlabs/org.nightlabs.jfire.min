package org.nightlabs.jfire.dashboard;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class DashboardGadgetClientScriptsConfig implements Serializable {

	private static final long serialVersionUID = 20111227L;

	private List<ClientScript> clientScripts;
	
	private boolean confirmProcessing;
	
	public DashboardGadgetClientScriptsConfig() { 
		this.clientScripts = new ArrayList<DashboardGadgetClientScriptsConfig.ClientScript>();
	}
	
	public DashboardGadgetClientScriptsConfig(final List<ClientScript> clientScripts) {
		this.clientScripts = clientScripts;
	}

	public static class ClientScript {
		
		private String name;
		private String content;
		
		public ClientScript() {
			this.name = "";
			this.content = "";
		}
		
		public ClientScript(final String name, final String content) {
			this.name = name;
			this.content = content;
		}

		public String getName() {
			return name;
		}

		public String getContent() {
			return content;
		}

		public void setName(final String name) {
			this.name = name;
		}

		public void setContent(final String content) {
			this.content = content;
		}
		
	}
	
//	public ClientScript createNewClientScript(final String name, final String script) {
//		return new ClientScript(name, script);
//	}

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
