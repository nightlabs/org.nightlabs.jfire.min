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
package org.nightlabs.ipanema.servermanager.dbcreate;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import org.nightlabs.ipanema.servermanager.config.JFireServerConfigModule;


/**
 * @author marco
 */
public class DatabaseCreatorMySQL implements DatabaseCreator {
	/**
	 * @see org.nightlabs.ipanema.servermanager.dbcreate.DatabaseCreator#createDatabase(org.nightlabs.ipanema.servermanager.config.JFireServerConfigModule, String, String, String)
	 */
	public void createDatabase(JFireServerConfigModule ipanemaServerConfigModule,
			String dbServerURL, String databaseName, String databaseURL)
			throws CreateDatabaseException
	{
		JFireServerConfigModule.Database dbCf = ipanemaServerConfigModule.getDatabase();
		
		try {
			java.sql.Connection conn = DriverManager.getConnection(
					dbServerURL, dbCf.getDatabaseUserName(), dbCf.getDatabasePassword());
			try {
				Statement stmt = conn.createStatement();
				StringBuffer sql = new StringBuffer();

				sql.append("create database ");
				sql.append(databaseName);

				stmt.execute(sql.toString());
			} finally {
				conn.close();
			}
		} catch (SQLException e) {
			throw new CreateDatabaseException(e);
		}
	}
}
