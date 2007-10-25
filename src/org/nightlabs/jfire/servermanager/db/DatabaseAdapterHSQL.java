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

import org.nightlabs.annotation.Implement;
import org.nightlabs.jfire.servermanager.config.DatabaseCf;
import org.nightlabs.jfire.servermanager.config.JFireServerConfigModule;


/**
 * @author Marco Schulze - marco at nightlabs dot de
 * @deprecated We don't use HQL anymore, since it doesn't support transaction isolation level read-committed.
 */
@Deprecated
public class DatabaseAdapterHSQL
extends AbstractDatabaseAdapter
{
	public void test(JFireServerConfigModule jfireServerConfigModule)
	throws DatabaseException
	{
		try {
			DatabaseCf dbCf = jfireServerConfigModule.getDatabase();
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
	throws DatabaseAlreadyExistsException, DatabaseException
	{
//		Logger logger = Logger.getLogger(DatabaseAdapterHSQL.class);

		DatabaseCf dbCf = jfireServerConfigModule.getDatabase();

		if (!databaseURL.startsWith("jdbc:hsqldb:"))
			throw new IllegalArgumentException("databaseURL must start with 'jdbc:hsqldb:'!");

		try {
			connCreateDB = DriverManager.getConnection(
					databaseURL, dbCf.getDatabaseUserName(), dbCf.getDatabasePassword());

			Statement stmt = connCreateDB.createStatement();
			stmt.execute("create table MY_FIRST_TABLE (a int not null)");
			stmt.execute("drop table MY_FIRST_TABLE");
		} catch (SQLException e) {
			// TODO throw a DatabaseAlreadyExistsException, if this is the cause of the failure. Is there a way to know which databases exist?!
			throw new DatabaseException(e);
		}
	}

	@Implement
	public void dropDatabase()
			throws DatabaseException
	{
		assertOpen();
		// TODO how can I drop a HSQL database - or delete all its tables???
		try {
			if (connCreateDB != null) {
				Statement stmt = connCreateDB.createStatement();
//				stmt.execute("dropall");
				stmt.close();
			}
		} catch (SQLException e) {
			throw new DatabaseException(e);
		}
	}

	@Override
	public void close()
			throws DatabaseException
	{
		super.close();
		try {
			if (connCreateDB != null) {
				connCreateDB.close();
				connCreateDB = null;
			}
		} catch (SQLException e) {
			throw new DatabaseException(e);
		}
	}
}
