/* *****************************************************************************
 * JFire - it's hot - Free ERP System - http://jfire.org                       *
 * Copyright (C) 2004-2005 NightLabs - http://NightLabs.org                    *
 *                                                                             *
 * This library is free software; you can redistribute it and/or               *
 * modify it under the terms of the GNU Lesser General Public                  *
 * License as published by the Free Software Foundation; either                *
 * version 2.1 of the License, or (at your option) any later version.          *
 *                                                                             *
 * This library is distributed in the hope that it will be useful,             *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of              *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU           *
 * Lesser General Public License for more details.                             *
 *                                                                             *
 * You should have received a copy of the GNU Lesser General Public            *
 * License along with this library; if not, write to the                       *
 *     Free Software Foundation, Inc.,                                         *
 *     51 Franklin St, Fifth Floor,                                            *
 *     Boston, MA  02110-1301  USA                                             *
 *                                                                             *
 * Or get it online :                                                          *
 *     http://opensource.org/licenses/lgpl-license.php                         *
 *                                                                             *
 *                                                                             *
 ******************************************************************************/

package org.nightlabs.jfire.base.app;

import javax.security.auth.login.LoginException;

import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.application.WorkbenchAdvisor;

import org.nightlabs.base.app.AbstractApplicationThread;
import org.nightlabs.base.app.AbstractWorkbenchAdvisor;
import org.nightlabs.jfire.base.login.Login;
import org.nightlabs.jfire.base.login.LoginConfigModule;
import org.nightlabs.jfire.base.update.StartupUpdateManager;

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
        LoginConfigModule lcm = Login.sharedInstance().getLoginConfigModule();
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
