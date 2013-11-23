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

package org.nightlabs.jfire.servermanager.config;

import org.nightlabs.config.ConfigModule;
import org.nightlabs.config.InitException;

/**
 * @author marco
 */
public class CreateOrganisationConfigModule extends ConfigModule
{
	/**
	 * The serial version of this class.
	 */
	private static final long serialVersionUID = 1L;

	private int waitForPersistenceManager_tryCount = 0;
	private int waitForPersistenceManager_timeout = 0; // default see init()
	private int waitForPersistenceManager_checkPeriod = 0; // default see init()

	/**
	 * @return Returns the waitForPersistenceManager_timeout.
	 */
	public int getWaitForPersistenceManager_timeout() {
		return waitForPersistenceManager_timeout;
	}
	/**
	 * This timeout defines how long to wait for the existence of a newly
	 * deployed PersistenceManagerFactory after a new organisation has been
	 * created.
	 * <br/><br/>
	 * The default is 60000 msec. Minimum is 10000 msec. Maximum is 300000 msec.
	 * If an illegal value is found in the configuration, it is set to the default.
	 *
	 * @param waitForPersistenceManager_timeout The waitForPersistenceManager_timeout to set.
	 */
	public void setWaitForPersistenceManager_timeout(
			int waitForPersistenceManager_timeout) {
		this.waitForPersistenceManager_timeout = waitForPersistenceManager_timeout;
		setChanged();
	}
	/**
	 * This setting controls, in which period to retry whether a newly deployed
	 * PersistenceManagerFactory can be fetched from JNDI.
	 * <br/><br/>
	 * The default is 1000 msec. Minimum is 300 msec. Maximum is 60000 msec.
	 * If an illegal value is found in the configuration, it is set to the default.
	 *
	 * @return Returns the waitForPersistenceManager_checkPeriod.
	 */
	public int getWaitForPersistenceManager_checkPeriod() {
		return waitForPersistenceManager_checkPeriod;
	}
	/**
	 * @param waitForPersistenceManager_checkPeriod The waitForPersistenceManager_checkPeriod to set.
	 */
	public void setWaitForPersistenceManager_checkPeriod(
			int waitForPersistenceManager_checkPeriod) {
		this.waitForPersistenceManager_checkPeriod = waitForPersistenceManager_checkPeriod;
		setChanged();
	}

	public int getWaitForPersistenceManager_tryCount()
	{
		return waitForPersistenceManager_tryCount;
	}
	public void setWaitForPersistenceManager_tryCount(
			int waitForPersistenceManager_tryCount)
	{
		this.waitForPersistenceManager_tryCount = waitForPersistenceManager_tryCount;
		setChanged();
	}

	/**
	 * @see org.nightlabs.config.Initializable#init()
	 */
	@Override
	public void init() throws InitException
	{
		if (waitForPersistenceManager_tryCount < 1)
			setWaitForPersistenceManager_tryCount(3);

		if (waitForPersistenceManager_timeout < 10000 ||
				waitForPersistenceManager_timeout > 300000)
			setWaitForPersistenceManager_timeout(20 * 1000); // msec

		if (waitForPersistenceManager_checkPeriod < 300 ||
				waitForPersistenceManager_checkPeriod > 60000)
			setWaitForPersistenceManager_checkPeriod(1000); // msec
	}

}
