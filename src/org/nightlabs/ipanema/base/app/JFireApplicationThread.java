/*
 * Created 	on Nov 12, 2004
 * 					by Alexander Bieber
 *
 */
package org.nightlabs.ipanema.base.app;

import javax.security.auth.login.LoginException;

import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.application.WorkbenchAdvisor;

import org.nightlabs.base.app.AbstractApplicationThread;
import org.nightlabs.base.app.AbstractWorkbenchAdvisor;
import org.nightlabs.ipanema.base.login.Login;
import org.nightlabs.ipanema.base.login.LoginConfigModule;
import org.nightlabs.ipanema.base.update.StartupUpdateManager;

/**
 * @author Alexander Bieber
 */
public class JFireApplicationThread 
//extends Thread
extends AbstractApplicationThread
{
	public static final String DEFAULT_NAME = "JFireApplicationThread";

	public JFireApplicationThread()
	{
		this(DEFAULT_NAME);
	}

	/**
	 * @param target
	 */
	public JFireApplicationThread(Runnable target) {
		super(target);
	}

	/**
	 * @param name
	 */
	public JFireApplicationThread(String name) {
		super(name);
	}

	/**
	 * @param group
	 * @param target
	 */
	public JFireApplicationThread(ThreadGroup group, Runnable target) {
		super(group, target);
	}

	/**
	 * @param target
	 * @param name
	 */
	public JFireApplicationThread(Runnable target, String name) {
		super(target, name);
	}
	
	public JFireApplicationThread(ThreadGroup group) {
		super(group, DEFAULT_NAME);
	}

	/**
	 * @param group
	 * @param name
	 */
	public JFireApplicationThread(ThreadGroup group, String name) {
		super(group, name);
	}

	/**
	 * @param group
	 * @param target
	 * @param name
	 */
	public JFireApplicationThread(ThreadGroup group, Runnable target,
			String name) {
		super(group, target, name);
	}

	/**
	 * @param group
	 * @param target
	 * @param name
	 * @param stackSize
	 */
	public JFireApplicationThread(ThreadGroup group, Runnable target,
			String name, long stackSize) {
		super(group, target, name, stackSize);
	}
	
//	private JFireApplication application;
//	void setJFireApplication(JFireApplication app) {
//		this.application = app;
//	}
	
	
	private int platformResultCode = -1;
  protected Display display;
	
	public int getPlatformResultCode() {
		return platformResultCode;
	}
	
	public void run() 
	{
		try {
			// create the display
		  display = PlatformUI.createDisplay();

			WorkbenchAdvisor workbenchAdvisor = new JFireWorkbenchAdvisor(display);
			
      try
      {
        LoginConfigModule lcm = Login.getLogin(false).getLoginConfigModule();
        if(lcm.getAutomaticUpdate() == true)
        {
					Login.getLogin();
          StartupUpdateManager updateManager = new StartupUpdateManager(lcm);
          updateManager.run();
          if(updateManager.doRestart())
          {
            platformResultCode = PlatformUI.RETURN_RESTART;
            return;
          }
        }
      }
      catch(LoginException e)
      {
      }
      
      platformResultCode = PlatformUI.createAndRunWorkbench(display, workbenchAdvisor);
		}
		finally {
			synchronized(JFireApplication.getMutex()) {
				JFireApplication.getMutex().notifyAll();
			}
		}
	}
	
	public AbstractWorkbenchAdvisor initWorkbenchAdvisor() {
		return new JFireWorkbenchAdvisor(display);
	}
	
}
