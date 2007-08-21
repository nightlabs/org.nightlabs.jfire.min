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

import org.eclipse.swt.widgets.Display;
import org.nightlabs.base.app.AbstractApplication;
import org.nightlabs.base.app.AbstractWorkbenchAdvisor;

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
//		try
//		{
			// TODO put the Update stuff into a LoginStateListener!

//			Login.getLogin(); // we always login in order to prevent our class-loading problems.
//			LoginConfigModule lcm = Login.sharedInstance().getLoginConfigModule();
//			// TODO @Carnage lcm.getLastSavedLoginConfiguration() can return null, but this was not handled in the
//			// following if clause => NPE. I added the check for null, but I don't know whether it's correct with == + || or whether
//			// it should be != + && 
//			if (lcm.getLastSavedLoginConfiguration() == null || lcm.getLastSavedLoginConfiguration().isAutomaticUpdate())
//			{
//				Login.getLogin();
//				StartupUpdateManager updateManager = new StartupUpdateManager(lcm);
//				updateManager.run();
//				if(updateManager.doRestart())
//				{
//					setPlatformReturnCode(IApplication.EXIT_RESTART);
//					return;
//				}
//			}
//		} catch(LoginException e) {
//			e.printStackTrace(); // TODO what should be here? There was nothing in this catch block! because there is no logger, I dump at least to std-out. Marco. ;-)
//		}
	}
	
	public AbstractWorkbenchAdvisor initWorkbenchAdvisor(Display display) {
		return new JFireWorkbenchAdvisor(display);
	}
	
}
