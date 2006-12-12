package org.nightlabs.jfire.init;

public class DependencyCycleException extends InitException {
	private String cycleInfo;
	
	public DependencyCycleException(String message, String cycleInfo) {
		super(message);
		this.cycleInfo = cycleInfo;
	}
	
	public String getCycleInfo() {
		return cycleInfo;
	}
}
