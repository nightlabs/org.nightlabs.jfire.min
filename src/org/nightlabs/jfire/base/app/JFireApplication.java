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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.security.auth.login.LoginException;

import org.eclipse.equinox.app.IApplication;
import org.eclipse.swt.widgets.Display;
import org.nightlabs.base.app.AbstractApplication;
import org.nightlabs.base.app.AbstractWorkbenchAdvisor;
import org.nightlabs.jfire.base.login.Login;
import org.nightlabs.jfire.base.login.LoginConfigModule;
import org.nightlabs.jfire.base.update.StartupUpdateManager;

/**
 * JFireApplication is the main executed class {@see JFireApplication#run(Object)}. 
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 */
public class JFireApplication 
extends AbstractApplication 
{
	public static final String PLUGIN_ID = "org.nightlabs.jfire.base"; //$NON-NLS-1$
	
	private static List<JFireApplicationListener> applicationListener = new LinkedList<JFireApplicationListener>();
	
	public static void addApplicationListener(JFireApplicationListener listener) {
		applicationListener.add(listener);
	}
	
	public static void removeApplicationListener(JFireApplicationListener listener) {
		applicationListener.remove(listener);
	}
	
	public static final int APPLICATION_EVENTTYPE_STARTED = 1;

	void notifyApplicationListeners(int applicationEventType) {
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
		return "jfire"; //$NON-NLS-1$
	}

	
	@Override
	protected void preCreateWorkbench() 
	{
//		try {
//			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());	
//		} catch (Exception e) {
//			// Do nothing
//		}  
		
		try
		{
//			Login.getLogin(); // we always login in order to prevent our class-loading problems.

			LoginConfigModule lcm = Login.sharedInstance().getLoginConfigModule();
			if(lcm.getLastSavedLoginConfiguration().isAutomaticUpdate() == true)
			{
				Login.getLogin();
				StartupUpdateManager updateManager = new StartupUpdateManager(lcm);
				updateManager.run();
				if(updateManager.doRestart())
				{
					setPlatformReturnCode(IApplication.EXIT_RESTART);
					return;
				}
			}
		}
		catch(LoginException e)
		{
		}
	}
	
	public AbstractWorkbenchAdvisor initWorkbenchAdvisor(Display display) {
		return new JFireWorkbenchAdvisor(display);
	}
	
}
