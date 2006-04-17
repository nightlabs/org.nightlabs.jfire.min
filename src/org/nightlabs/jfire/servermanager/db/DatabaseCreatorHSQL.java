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
 * @author marco
 */
public class DatabaseCreatorHSQL implements DatabaseCreator {

	public void createDatabase(JFireServerConfigModule jfireServerConfigModule,
			String databaseURL)
			throws CreateDatabaseException
	{
		Logger LOGGER = Logger.getLogger(DatabaseCreatorHSQL.class);

		JFireServerConfigModule.Database dbCf = jfireServerConfigModule.getDatabase();

		if (!databaseURL.startsWith("jdbc:hsqldb:"))
			throw new IllegalArgumentException("databaseURL must start with 'jdbc:hsqldb:'!");

//		int lastSlashPos = databaseURL.lastIndexOf('/');
//		if (lastSlashPos < 0)
//			throw new IllegalArgumentException("databaseURL is malformed: Misses '/' before database name!");
//
//		String dbServerURL = databaseURL.substring(0, lastSlashPos + 1);
//		String databaseName = databaseURL.substring(lastSlashPos + 1);

//		LOGGER.info("Creating database \""+databaseName+"\" on server \""+dbServerURL+"\"");

		try {
//			java.sql.Connection conn = DriverManager.getConnection(
//					dbServerURL, dbCf.getDatabaseUserName(), dbCf.getDatabasePassword());
			java.sql.Connection conn = DriverManager.getConnection(
					databaseURL, dbCf.getDatabaseUserName(), dbCf.getDatabasePassword());
			try {
				Statement stmt = conn.createStatement();
				StringBuffer sql = new StringBuffer();

				sql.append("create table MY_FIRST_TABLE (a int not null)");

				stmt.execute(sql.toString());
			} finally {
				conn.close();
			}
		} catch (SQLException e) {
			throw new CreateDatabaseException(e);
		}
	}
}
