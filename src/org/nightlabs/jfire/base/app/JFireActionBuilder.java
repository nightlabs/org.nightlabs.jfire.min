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

import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.nightlabs.base.ui.app.DefaultActionBuilder;

/**
 * Creates the Menu 
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 *
 */
public class JFireActionBuilder 
extends DefaultActionBuilder
{
	public JFireActionBuilder(IActionBarConfigurer configurer) {
		super(configurer);
	}
	
	@Override
	protected void fillStatusLine(IStatusLineManager statusLine) {
		super.fillStatusLine(statusLine);
//		IContributionItem beginGroup = statusLine.find(StatusLineManager.BEGIN_GROUP);
//		statusLine.add(new LocaleStatusLineContribution("Locale"));
//		statusLine.prependToGroup(StatusLineManager.BEGIN_GROUP, new LoginStateStatusLineContribution(Messages.getString("app.JFireActionBuilder.loginStatus"))); //$NON-NLS-1$
////		statusLine.appendToGroup(StatusLineManager.BEGIN_GROUP, new LocaleStatusLineContribution("Locale")); //$NON-NLS-1$
//		statusLine.remove(beginGroup);
//		statusLine.prependToGroup(StatusLineManager.MIDDLE_GROUP, beginGroup);
	}
	
}
