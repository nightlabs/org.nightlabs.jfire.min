/* ************************************************************************** *
 * Copyright (C) 2004 NightLabs GmbH, Marco Schulze                           *
 * All rights reserved.                                                       *
 * http://www.NightLabs.de                                                    *
 *                                                                            *
 * This program and the accompanying materials are free software; you can re- *
 * distribute it and/or modify it under the terms of the GNU General Public   *
 * License as published by the Free Software Foundation; either ver 2 of the  *
 * License, or any later version.                                             *
 *                                                                            *
 * This module is distributed in the hope that it will be useful, but WITHOUT *
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FIT- *
 * NESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more *
 * details.                                                                   *
 *                                                                            *
 * You should have received a copy of the GNU General Public License along    *
 * with this module; if not, write to the Free Software Foundation, Inc.:     *
 *    59 Temple Place, Suite 330                                              *
 *    Boston MA 02111-1307                                                    *
 *    USA                                                                     *
 *                                                                            *
 * Or get it online:                                                          *
 *    http://www.opensource.org/licenses/gpl-license.php                      *
 *                                                                            *
 * In case, you want to use this module or parts of it in a proprietary pro-  *
 * ject, you can purchase it under the NightLabs Commercial License. Please   *
 * contact NightLabs GmbH under info AT nightlabs DOT com for more infos or   *
 * visit http://www.NightLabs.com                                             *
 * ************************************************************************** */

/*
 * Created on 17.06.2004
 */
package org.nightlabs.jfire.servermanager.config;

import org.nightlabs.config.ConfigModule;
import org.nightlabs.config.InitException;

/**
 * @author marco
 */
public class CreateOrganisationConfigModule extends ConfigModule
{
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
	 * The default is 3000 msec. Minimum is 1000 msec. Maximum is 60000 msec.
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
	public void init() throws InitException
	{
		if (waitForPersistenceManager_tryCount < 1)
			setWaitForPersistenceManager_tryCount(3);

		if (waitForPersistenceManager_timeout < 10000 ||
				waitForPersistenceManager_timeout > 300000)
			setWaitForPersistenceManager_timeout(20 * 1000); // msec

		if (waitForPersistenceManager_checkPeriod < 1000 ||
				waitForPersistenceManager_checkPeriod > 60000)
			setWaitForPersistenceManager_checkPeriod(3000); // msec
	}

}
