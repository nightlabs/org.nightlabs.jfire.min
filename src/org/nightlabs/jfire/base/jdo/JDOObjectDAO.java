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

package org.nightlabs.jfire.base.jdo;

import org.eclipse.core.runtime.IProgressMonitor;

/**
 * JDO object retrieval through the JFire client cache.
 * <p>
 * Inherit this class with a JDO object id class and
 * JDO object class as generic parameters to provide
 * an accessor object for this kind of JDO object.
 * 
 * @author Marco Schulze - marco at nightlabs dot de
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public abstract class JDOObjectDAO<JDOObjectID, JDOObject> extends BaseJDOObjectDAO<JDOObjectID, JDOObject, IProgressMonitor>
{

	@Override
	protected void monitorBeginTask(IProgressMonitor monitor, String taskName, int totalWork) {
		monitor.beginTask(taskName, totalWork);
	}

	@Override
	protected void monitorDone(IProgressMonitor monitor) {
		monitor.done();
	}

	@Override
	protected boolean monitorIsCanceled(IProgressMonitor monitor) {
		return monitor.isCanceled();
	}

	@Override
	protected void monitorSetCanceled(IProgressMonitor monitor, boolean canceled) {
		monitor.setCanceled(canceled);
	}

	@Override
	protected void monitorSetTaskName(IProgressMonitor monitor, String name) {
		monitor.setTaskName(name);
	}

	@Override
	protected void monitorSubTask(IProgressMonitor monitor, String name) {
		monitor.subTask(name);
	}

	@Override
	protected void monitorWorked(IProgressMonitor monitor, int work) {
		monitor.worked(work);
	}
}
