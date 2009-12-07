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
import org.nightlabs.jfire.servermanager.config.DatabaseCf;
import org.nightlabs.jfire.servermanager.config.JFireServerConfigModule;
import org.nightlabs.jfire.servermanager.db.AbstractDatabaseAdapter;
import org.nightlabs.jfire.servermanager.db.DatabaseAlreadyExistsException;
import org.nightlabs.jfire.servermanager.db.DatabaseException;


/**
 * @author Marco Schulze - marco at nightlabs dot de
 */
public class DatabaseAdapterPostgreSQL
extends AbstractDatabaseAdapter
{
	@Override
	public void test(JFireServerConfigModule jfireServerConfigModule)
	throws DatabaseException
	{
		try {
			DatabaseCf dbCf = jfireServerConfigModule.getDatabase();
			Connection sqlConn = DriverManager.getConnection(
					dbCf.getDatabaseURL(null),
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

	@Override
	public void createDatabase(
			JFireServerConfigModule jfireServerConfigModule,
			String databaseURL)
	throws DatabaseAlreadyExistsException, DatabaseException
	{
		assertOpen();

		this.databaseName = null;
		Logger logger = Logger.getLogger(DatabaseAdapterPostgreSQL.class);

		DatabaseCf dbCf = jfireServerConfigModule.getDatabase();

		if (!databaseURL.startsWith("jdbc:postgresql:"))
			throw new IllegalArgumentException("databaseURL must start with 'jdbc:postgresql:'!");

		int lastSlashPos = databaseURL.lastIndexOf('/');
		if (lastSlashPos < 0)
			throw new IllegalArgumentException("databaseURL is malformed: Misses '/' before database name!");

		String dbServerURL = databaseURL.substring(0, lastSlashPos + 1);
		String databaseName = databaseURL.substring(lastSlashPos + 1);

		int firstQuestionMarkPos = databaseName.indexOf('?');
		if (firstQuestionMarkPos >= 0) {
			String optionSuffix = databaseName.substring(firstQuestionMarkPos);
			databaseName = databaseName.substring(0, firstQuestionMarkPos);
			dbServerURL += optionSuffix;
		}

		logger.info("Creating database \""+databaseName+"\" on server \""+dbServerURL+"\"");

		try {
			connCreateDB = DriverManager.getConnection(
					dbServerURL, dbCf.getDatabaseUserName(), dbCf.getDatabasePassword());
			Statement stmt = connCreateDB.createStatement();

			// check whether database exists
			ResultSet resultSet = stmt.executeQuery("SELECT datname FROM pg_database WHERE datname = '" + databaseName + "'");
			if (resultSet.next()) // the database exists
				throw new DatabaseAlreadyExistsException(dbCf.getDatabaseURL(databaseName));

			stmt.execute(
					"CREATE DATABASE " + databaseName + "\n" +
					"WITH\n"
//					+ "	ENCODING = 'UTF8'\n"
//					+ "	LC_COLLATE = 'de_DE.UTF-8'\n"
//					+ "	LC_CTYPE = 'de_DE.UTF-8'\n"
		      + "	CONNECTION LIMIT = 200"
			);

			this.databaseName = databaseName;
		} catch (SQLException e) {
			logger.info("Database \""+databaseName+"\" on server \""+dbServerURL+"\" could not be created, because of an unexpected failure!", e);
			throw new DatabaseException(e);
		}
		// It's no problem that we don't close the connCreateDB, if dropDatabase() is not called.
		// According to the javadoc, the resources will be closed, when the object is garbage-collected.
		// new: we close it now in the close() method. it's cleaner. Marco.
	}

	@Override
	public void dropDatabase()
	throws DatabaseException
	{
		assertOpen();

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

	@Override
	public void close() throws DatabaseException
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
