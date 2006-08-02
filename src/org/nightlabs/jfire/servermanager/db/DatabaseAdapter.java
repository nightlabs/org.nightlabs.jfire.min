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

import org.nightlabs.jfire.servermanager.config.JFireServerConfigModule;

/**
 * A database adapter is used for those things that need to be done outside of JDO.
 * Currently, this is only the creation of a database - once it exists, all
 * interaction is done using JDO.
 *
 * @author Marco Schulze - marco at nightlabs dot de
 */
public interface DatabaseAdapter
{
	/**
	 * This method tests whether the database server configured in the
	 * given config module is accessible. Note, that the jdbc database driver
	 * has already been loaded by Class.forName(...) - i.e. you don't need to
	 * do this anymore.
	 *
	 * @param jfireServerConfigModule The configuration module which holds the database connection properties.
	 * @throws DatabaseException Thrown, if the configuration is incorrect or the database server cannot be reached.
	 */
	void test(JFireServerConfigModule jfireServerConfigModule)
	throws DatabaseException;

	/**
	 * @param jfireServerConfigModule The configuration module which has the database connection properties. You can read
	 *		username and password out of it.
	 * @param databaseURL The complete connection URL for accessing the new database. You must parse it in order to
	 *		check whether 1st you are really able to handle this kind of database (e.g. if it starts with 'jdbc:mysql' and you
	 *		support only PostgreSQL, then you have to throw an exception).
	 * @throws DatabaseAlreadyExistsException Thrown if the database already exists and therefore cannot be created.
	 * @throws DatabaseException Thrown, if accessing the database server fails or creating the database fails.
	 */
	void createDatabase(
			JFireServerConfigModule jfireServerConfigModule,
			String databaseURL)
	throws DatabaseAlreadyExistsException, DatabaseException;

	/**
	 * This method is called after {@link #createDatabase(JFireServerConfigModule, String) } in case an error occured
	 * during organisation setup and thus, the corrupted new database needs to be thrown away.
	 *
	 * @throws DatabaseException
	 */
	void dropDatabase()
	throws DatabaseException;
}
