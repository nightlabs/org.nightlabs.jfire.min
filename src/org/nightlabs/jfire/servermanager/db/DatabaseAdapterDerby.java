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

import org.apache.log4j.Logger;
import org.nightlabs.jfire.servermanager.config.DatabaseCf;
import org.nightlabs.jfire.servermanager.config.JFireServerConfigModule;
import org.nightlabs.util.IOUtil;


/**
 * @author Marco Schulze - marco at nightlabs dot de
 */
public class DatabaseAdapterDerby
extends AbstractDatabaseAdapter
{
	private static final Logger logger = Logger.getLogger(DatabaseAdapterDerby.class);

	public void test(JFireServerConfigModule jfireServerConfigModule)
	throws DatabaseException
	{
		String url = null;

		try {
			DatabaseCf dbCf = jfireServerConfigModule.getDatabase();

			url = dbCf.getDatabaseURL(dbCf.getDatabasePrefix() + "derbytest" + dbCf.getDatabaseSuffix());
			String user = dbCf.getDatabaseUserName();
			String pw = dbCf.getDatabasePassword();

			// in case the db-test-directory already exists, we delete it
			// otherwise the create might throw an exception (just had it). marco.
			deleteDatabaseDirectory(url);

			// and open the connection.
			Connection sqlConn = DriverManager.getConnection(url + ";create=true", user, pw);

			sqlConn.close();
		} catch (SQLException e) {
			throw new DatabaseException(e);
		} finally {
			if (url != null) {
				String databaseURL_shutdown = url + ";shutdown=true";
				try {
					DriverManager.getConnection(databaseURL_shutdown);
				} catch (SQLException x) {
					// according to http://db.apache.org/derby/docs/dev/devguide/tdevdvlp40464.html this always causes an SQL exception if it was successful - strange ;-)
					logger.debug("Shutting down the database was successful. Strange, but true, this causes an SQLException.", x);
				}
				
				deleteDatabaseDirectory(url);
			}
		}
	}

	private String databaseURL;

	public void createDatabase(
			JFireServerConfigModule jfireServerConfigModule,
			String databaseURL)
	throws DatabaseAlreadyExistsException, DatabaseException
	{
		assertOpen();
		this.databaseURL = databaseURL;
		DatabaseCf dbCf = jfireServerConfigModule.getDatabase();

		if (!databaseURL.startsWith("jdbc:derby:"))
			throw new IllegalArgumentException("databaseURL must start with 'jdbc:derby:'!");

		// Find out whether the db already exists
		try {
			java.sql.Connection testDBConn = DriverManager.getConnection(
					databaseURL, dbCf.getDatabaseUserName(), dbCf.getDatabasePassword());
			testDBConn.close();
			throw new DatabaseAlreadyExistsException(databaseURL);
		} catch (SQLException e) {
			// fine, it does not yet exist => ignore
		}

		try {
//			DriverPropertyInfo[] propertyInfos = java.sql.DriverManager.getDriver("jdbc:derby:").getPropertyInfo(databaseURL, new Properties());
			java.sql.Connection connCreateDB = DriverManager.getConnection(
					databaseURL + ";create=true", dbCf.getDatabaseUserName(), dbCf.getDatabasePassword());

			Statement stmt = connCreateDB.createStatement();
			stmt.execute("create table MY_FIRST_TABLE (a int not null)");
			stmt.execute("drop table MY_FIRST_TABLE");
			stmt.close();
			connCreateDB.close();
		} catch (SQLException e) {
			throw new DatabaseException(e);
		}
	}

	private void deleteDatabaseDirectory(String databaseURL)
	{
		long maxRetryTimeMSec = 30000;
		long waitBetweenRetryMSec = 1000;

		if (databaseURL.startsWith("jdbc:derby:")) {
			String dir = databaseURL.substring("jdbc:derby:".length());
			long start = System.currentTimeMillis();
			while (true) {
				boolean deleteSuccessful = IOUtil.deleteDirectoryRecursively(dir);
				if (deleteSuccessful)
					break;

				if (System.currentTimeMillis() - start > maxRetryTimeMSec) {
					logger.error("deleteDatabaseDirectory: Could not delete db dir \"" + dir + "\" within timeout=" + maxRetryTimeMSec + " msec.");
					break;
				}

				logger.warn("deleteDatabaseDirectory: Could not delete db dir \"" + dir + "\" - will wait and try again.");
				try {
					Thread.sleep(waitBetweenRetryMSec);
				} catch (InterruptedException e) {
					logger.error("deleteDatabaseDirectory: Could not delete db dir \"" + dir + "\", because waiting was interrupted.");
					break;
				}
			}
		}
	}

	@Override
	public void dropDatabase()
			throws DatabaseException
	{
		assertOpen();
		if (databaseURL != null) {
			String databaseURL_shutdown = databaseURL + ";shutdown=true";
			try {
				DriverManager.getConnection(databaseURL_shutdown);
			} catch (SQLException x) {
				// according to http://db.apache.org/derby/docs/dev/devguide/tdevdvlp40464.html this always causes an SQL exception if it was successful - strange ;-)
				logger.debug("Shutting down the database was successful. Strange, but true, this causes an SQLException.", x);
			}

			deleteDatabaseDirectory(databaseURL);
		}
	}

	@Override
	public void close()
			throws DatabaseException
	{
		super.close();
		databaseURL = null;
	}

}
