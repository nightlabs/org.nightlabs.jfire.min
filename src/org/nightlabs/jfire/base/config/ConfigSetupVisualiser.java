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

package org.nightlabs.jfire.base.config;

import org.nightlabs.jfire.config.id.ConfigID;

/**
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 *
 */
public interface ConfigSetupVisualiser {
	
	/**
	 * Returns a displayable name for the keyObject
	 * linked the given Config.
	 * If the name is multilingual a localized
	 * name should be returned.
	 * This method should never return null or an empty string
	 * but the configKey when an error occurs in name resolving.
	 * 
	 * @param configID The ConfigID to which's linked object the name is searched.
	 * @return A displayable name for the object linked to the given Config.
	 */
	String getKeyObjectName(ConfigID configID);
	
	/**
	 * Returns a localized description of the given Config which might
	 * be a ConfigGroup as well.
	 * Here as well the configKey should be returned on an error, not null or
	 * an empty String.
	 */
	String getConfigDescription(ConfigID configID);
	
	
}
