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

package org.nightlabs.jfire.base;

import org.nightlabs.jfire.editlock.id.EditLockTypeID;
import org.nightlabs.jfire.organisation.Organisation;

/**
 * Class defining global values for the JFireBase EAR.
 *
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 *
 */
public final class JFireBaseEAR {

	public static final String MODULE_NAME = JFireBaseEAR.class.getSimpleName();

	/**
	 * {@link EditLockTypeID} of {@link org.nightlabs.jfire.editlock.EditLock}s for {@link org.nightlabs.jfire.config.Config}s.
	 */
	public static final EditLockTypeID EDIT_LOCK_TYPE_ID_CONFIG = EditLockTypeID.create(
			Organisation.DEV_ORGANISATION_ID, "EditLockTypeConfig");
	/**
	 * {@link EditLockTypeID} of {@link org.nightlabs.jfire.editlock.EditLock}s for {@link org.nightlabs.jfire.config.ConfigModule}s.
	 */
	public static final EditLockTypeID EDIT_LOCK_TYPE_ID_CONFIG_MODULE = EditLockTypeID.create(
			Organisation.DEV_ORGANISATION_ID, "EditLockTypeConfigModule");

	public static boolean JPOX_WORKAROUND_FLUSH_ENABLED = true;
}
