package org.nightlabs.ipanema.base.app;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

import org.nightlabs.base.app.AbstractApplication;
import org.nightlabs.base.app.AbstractApplicationThread;

/**
 * JFireApplication is the main executed class {@see JFireApplication#run(Object)}. 
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 */
public class JFireApplication 
extends AbstractApplication 
{
	public static final String PLUGIN_ID = "org.nightlabs.ipanema.base"; //$NON-NLS-1$
	public static final Logger LOGGER = Logger.getLogger(JFireApplication.class);
	
	private static List applicationListener = new LinkedList();
	
	public static void addApplicationListener(JFireApplicationListener listener) {
		applicationListener.add(listener);
	}
	
	public static void removeApplicationListener(JFireApplicationListener listener) {
		applicationListener.remove(listener);
	}
	
	public static final int APPLICATION_EVENTTYPE_STARTED = 1;
	
	void noitfyApplicationListeners(int applicationEventType) {
		for (Iterator iter = applicationListener.iterator(); iter.hasNext();) {
			JFireApplicationListener listener = (JFireApplicationListener) iter.next();
			switch (applicationEventType) {
				case APPLICATION_EVENTTYPE_STARTED: 
					listener.applicationStarted();
					break;					
			}			
		}
	}

	public String initApplicationName() {
		return "ipanema";
	}

	public AbstractApplicationThread initApplicationThread(ThreadGroup group) {
		return new JFireApplicationThread(group);
	}
	
}
