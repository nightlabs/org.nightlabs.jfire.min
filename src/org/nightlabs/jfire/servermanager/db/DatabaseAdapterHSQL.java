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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import org.nightlabs.jfire.servermanager.config.JFireServerConfigModule;


/**
 * @author Marco Schulze - marco at nightlabs dot de
 */
public class DatabaseAdapterHSQL
implements DatabaseAdapter
{
	public void test(JFireServerConfigModule jfireServerConfigModule)
	throws DatabaseException
	{
		try {
			JFireServerConfigModule.Database dbCf = jfireServerConfigModule.getDatabase();
			Connection sqlConn = DriverManager.getConnection(
					dbCf.getDatabaseURL("test"),
					dbCf.getDatabaseUserName(),
					dbCf.getDatabasePassword()
			);			
			
			sqlConn.close();
		} catch (SQLException e) {
			throw new DatabaseException(e);
		}
	}

	private java.sql.Connection connCreateDB = null; 

	public void createDatabase(
			JFireServerConfigModule jfireServerConfigModule,
			String databaseURL)
	throws DatabaseException
	{
//		Logger LOGGER = Logger.getLogger(DatabaseAdapterHSQL.class);

		JFireServerConfigModule.Database dbCf = jfireServerConfigModule.getDatabase();

		if (!databaseURL.startsWith("jdbc:hsqldb:"))
			throw new IllegalArgumentException("databaseURL must start with 'jdbc:hsqldb:'!");

		try {
			connCreateDB = DriverManager.getConnection(
					databaseURL, dbCf.getDatabaseUserName(), dbCf.getDatabasePassword());

			Statement stmt = connCreateDB.createStatement();
			stmt.execute("create table MY_FIRST_TABLE (a int not null)");
			stmt.execute("drop table MY_FIRST_TABLE");
		} catch (SQLException e) {
			throw new DatabaseException(e);
		}
	}

	public void dropDatabase()
			throws DatabaseException
	{
		// TODO how can I drop a HSQL database???
		try {
			if (connCreateDB != null) {
//				Statement stmt = connCreateDB.createStatement();
//				stmt.execute("dropall");
				connCreateDB.close();
				connCreateDB = null;
			}
		} catch (SQLException e) {
			throw new DatabaseException(e);
		}
	}

}
