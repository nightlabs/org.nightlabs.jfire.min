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
import java.sql.ResultSet;
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
	public void test(JFireServerConfigModule jfireServerConfigModule)
	throws DatabaseException
	{
		try {
			JFireServerConfigModule.Database dbCf = jfireServerConfigModule.getDatabase();
			Connection sqlConn = DriverManager.getConnection(
					dbCf.getDatabaseURL(null),
//					"jdbc:"+dbCf.getDatabaseProtocol()+"://"+dbCf.getDatabaseHost()+"/",
					dbCf.getDatabaseUserName(),
					dbCf.getDatabasePassword()
			);			
			
			sqlConn.close();
		} catch (SQLException e) {
			throw new DatabaseException(e);
		}
	}


	private Connection connCreateDB = null;
	private String databaseName = null;

	public void createDatabase(
			JFireServerConfigModule jfireServerConfigModule,
			String databaseURL)
	throws DatabaseAlreadyExistsException, DatabaseException
	{
		this.databaseName = null;
		Logger logger = Logger.getLogger(DatabaseAdapterMySQL.class);

		JFireServerConfigModule.Database dbCf = jfireServerConfigModule.getDatabase();

		if (!databaseURL.startsWith("jdbc:mysql:"))
			throw new IllegalArgumentException("databaseURL must start with 'jdbc:mysql:'!");

		int lastSlashPos = databaseURL.lastIndexOf('/');
		if (lastSlashPos < 0)
			throw new IllegalArgumentException("databaseURL is malformed: Misses '/' before database name!");

		String dbServerURL = databaseURL.substring(0, lastSlashPos + 1);
		String databaseName = databaseURL.substring(lastSlashPos + 1);

		logger.info("Creating database \""+databaseName+"\" on server \""+dbServerURL+"\"");

		try {
			connCreateDB = DriverManager.getConnection(
					dbServerURL, dbCf.getDatabaseUserName(), dbCf.getDatabasePassword());
			Statement stmt = connCreateDB.createStatement();

// We check the error code below instead! That reduces two calls to one.
//			// check whether database exists
//			ResultSet resultSet = stmt.executeQuery("show databases like '" + databaseName + "'");
//			if (resultSet.next()) // the database exists
//				throw new DatabaseAlreadyExistsException(dbServerURL, databaseName);

			stmt.execute("create database " + databaseName);
			this.databaseName = databaseName;
		} catch (SQLException e) {
// We check the error code here instead of querying before: http://dev.mysql.com/doc/refman/5.1/en/error-messages-server.html
			if (1007 == e.getErrorCode())
				throw new DatabaseAlreadyExistsException(dbServerURL, databaseName);

			throw new DatabaseException(e);
		}
		// It's no problem that we don't close the connCreateDB, if dropDatabase() is not called.
		// According to the javadoc, the resources will be closed, when the object is garbage-collected.
	}

	public void dropDatabase()
			throws DatabaseException
	{
		try {
			if (connCreateDB != null) {
				try {
					if (databaseName != null) {
						Statement stmt = connCreateDB.createStatement();
						stmt.execute("drop database " + databaseName);
						databaseName = null;
					}
				} finally {
					connCreateDB.close();
					connCreateDB = null;
				}
			}
		} catch (SQLException e) {
			throw new DatabaseException(e);
		}
	}

}
