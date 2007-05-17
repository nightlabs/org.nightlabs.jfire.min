package org.nightlabs.jfire.base.login;

import java.util.LinkedList;
import java.util.List;

public class LoginProfileList {
	private LinkedList<LoginProfile> loginProfileQueue;
	
	private static LoginProfileList sharedInstance;
	
	private static final int QUEUE_SIZE = 10;
	
	private LoginProfileList() {
		loginProfileQueue = new LinkedList<LoginProfile>();
	}
	
	public static LoginProfileList sharedInstance() {
		if (sharedInstance == null)
			sharedInstance = new LoginProfileList();
		
		return sharedInstance;
	}
	
	public void addLoginProfile(LoginProfile loginProfile) {
		if (loginProfileQueue.size() == QUEUE_SIZE)
			loginProfileQueue.poll();
		
		loginProfileQueue.add(loginProfile);
	}
	
	public List<LoginProfile> getLoginProfileList() {
		return loginProfileQueue;
	}
}
