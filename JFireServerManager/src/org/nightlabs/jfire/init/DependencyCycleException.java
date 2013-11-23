package org.nightlabs.jfire.init;

public class DependencyCycleException extends InitException {
	/**
	 * The serial version of this class.
	 */
	private static final long serialVersionUID = 1L;
	
	private String cycleInfo;
	
	public DependencyCycleException(String message, String cycleInfo) {
		super(message);
		this.cycleInfo = cycleInfo;
	}
	
	public String getCycleInfo() {
		return cycleInfo;
	}
}
