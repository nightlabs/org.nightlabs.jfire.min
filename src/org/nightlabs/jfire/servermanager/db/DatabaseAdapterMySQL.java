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

package org.nightlabs.jfire.servermanager.db;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.log4j.Logger;
import org.nightlabs.jfire.servermanager.config.JFireServerConfigModule;


/**
 * @author Marco Schulze - marco at nightlabs dot de
 */
public class DatabaseAdapterMySQL
implements DatabaseAdapter
{
	private java.sql.Connection connCreateDB = null;
	private String databaseName = null;

	public void createDatabase(
			JFireServerConfigModule jfireServerConfigModule,
			String databaseURL)
	throws DatabaseException
	{
		Logger LOGGER = Logger.getLogger(DatabaseAdapterMySQL.class);

		JFireServerConfigModule.Database dbCf = jfireServerConfigModule.getDatabase();

		if (!databaseURL.startsWith("jdbc:mysql:"))
			throw new IllegalArgumentException("databaseURL must start with 'jdbc:mysql:'!");

		int lastSlashPos = databaseURL.lastIndexOf('/');
		if (lastSlashPos < 0)
			throw new IllegalArgumentException("databaseURL is malformed: Misses '/' before database name!");

		String dbServerURL = databaseURL.substring(0, lastSlashPos + 1);
		String databaseName = databaseURL.substring(lastSlashPos + 1);

		LOGGER.info("Creating database \""+databaseName+"\" on server \""+dbServerURL+"\"");

		try {
			connCreateDB = DriverManager.getConnection(
					dbServerURL, dbCf.getDatabaseUserName(), dbCf.getDatabasePassword());
			Statement stmt = connCreateDB.createStatement();
			stmt.execute("create database " + databaseName);
			this.databaseName = databaseName;
		} catch (SQLException e) {
			throw new DatabaseException(e);
		}
	}

	public void dropDatabase()
			throws DatabaseException
	{
		try {
			if (connCreateDB != null) {
				if (databaseName != null) {
					Statement stmt = connCreateDB.createStatement();
					stmt.execute("drop database " + databaseName);
				}
				connCreateDB.close();
				connCreateDB = null;
			}
		} catch (SQLException e) {
			throw new DatabaseException(e);
		}
	}
}
