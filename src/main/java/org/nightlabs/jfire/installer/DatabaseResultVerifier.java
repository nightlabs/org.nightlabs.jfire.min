package org.nightlabs.jfire.installer;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import org.nightlabs.installer.base.VerificationException;
import org.nightlabs.installer.base.defaults.DefaultResultVerifier;

public class DatabaseResultVerifier extends DefaultResultVerifier
{
	@Override
	public void verify() throws VerificationException {
		super.verify();

		String dbDriverName_noTx = getInstallationEntity().getResult("10_driverName_noTx.result");
		String dbDriverName_localTx = getInstallationEntity().getResult("11_driverName_localTx.result");
		String dbDriverName_xa = getInstallationEntity().getResult("12_driverName_xa.result");
		String connectionURLTemplate = getInstallationEntity().getResult("20_connectionURL.result");
		if (!connectionURLTemplate.contains("${databaseName}"))
			throw new VerificationException("Connection URL template does not contain the variable \"${databaseName}\"!");

		String user = getInstallationEntity().getResult("50_userName.result");
		String password = getInstallationEntity().getResult("60_password.result");

		if (dbDriverName_noTx.contains("mysql")) {
			try {
				Class.forName(dbDriverName_noTx);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
				throw new VerificationException("Cannot load database driver (no-tx) " + dbDriverName_noTx + ": " + e.getLocalizedMessage());
			}

			try {
				Class.forName(dbDriverName_localTx);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
				throw new VerificationException("Cannot load database driver (local-tx) " + dbDriverName_noTx + ": " + e.getLocalizedMessage());
			}

			try {
				Class.forName(dbDriverName_xa);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
				throw new VerificationException("Cannot load database driver (xa) " + dbDriverName_noTx + ": " + e.getLocalizedMessage());
			}

			String connectionURL_base = connectionURLTemplate.replace("${databaseName}", "");
			Connection connection = null;
			try {
				try {
					connection = DriverManager.getConnection(connectionURL_base, user, password);
				} catch (SQLException e) {
					e.printStackTrace();
					throw new VerificationException("Could not connect to MySQL database server (connection URL \"" + connectionURL_base + "\"): " + e.getLocalizedMessage());
				}

				String testDB = "delete_me_" + Long.toString(System.currentTimeMillis(), 36);
				Statement statement = null;
				try {
					statement = connection.createStatement();
				} catch (SQLException e) {
					e.printStackTrace();
					throw new VerificationException("Creating java.sql.Statement for connection to MySQL database server (connection URL \"" + connectionURL_base + "\") failed: " + e.getLocalizedMessage());
				}

				try {
					statement.execute("CREATE DATABASE " + testDB);
				} catch (SQLException e) {
					e.printStackTrace();
					throw new VerificationException("Creating a test database in MySQL server (connection URL \"" + connectionURL_base + "\") failed! Ensure that the user \"" + user + "\" has the necessary privileges to create databases in your MySQL server! " + e.getLocalizedMessage());
				}
				try {
					String connectionURL_testDB = connectionURLTemplate.replace("${databaseName}", testDB);
					Connection connectionTestDB = null;
					try {
						try {
							connectionTestDB = DriverManager.getConnection(connectionURL_testDB, user, password);
						} catch (SQLException e) {
							e.printStackTrace();
							throw new VerificationException("Even though a test database was created successfully, it is impossible to connect to it using the connection URL \"" + connectionURL_testDB + "\": " + e.getLocalizedMessage());
						}

						Statement statementTestDB;
						try {
							statementTestDB = connectionTestDB.createStatement();
						} catch (SQLException e) {
							e.printStackTrace();
							throw new VerificationException("Even though a test database was created successfully, and the connection to the URL \"" + connectionURL_testDB + "\" established, a java.sql.Statement could not be created: " + e.getLocalizedMessage());
						}

						try {
							statementTestDB.execute("CREATE TABLE delete_me ( field int not null )");
						} catch (SQLException e) {
							e.printStackTrace();
							throw new VerificationException("Could not create a test table in the test database (connection URL \"" + connectionURL_testDB + "\"). Make sure the user \"" + user + "\" has all required privileges in your MySQL server! " + e.getLocalizedMessage());
						}

						try {
							statementTestDB.execute("INSERT INTO delete_me VALUES ( 5 )");
						} catch (SQLException e) {
							e.printStackTrace();
							throw new VerificationException("Could not insert a record into a test table in a test database (connection URL \"" + connectionURL_testDB + "\"). Make sure the user \"" + user + "\" has all required privileges in your MySQL server! " + e.getLocalizedMessage());
						}

						try {
							statementTestDB.execute("UPDATE delete_me SET field = 10 WHERE field = 5");
						} catch (SQLException e) {
							e.printStackTrace();
							throw new VerificationException("Could not update a record in a test table in a test database (connection URL \"" + connectionURL_testDB + "\"). Make sure the user \"" + user + "\" has all required privileges in your MySQL server! " + e.getLocalizedMessage());
						}

						try {
							statementTestDB.execute("DELETE FROM delete_me WHERE field = 10");
						} catch (SQLException e) {
							e.printStackTrace();
							throw new VerificationException("Could not delete a record in a test table in a test database (connection URL \"" + connectionURL_testDB + "\"). Make sure the user \"" + user + "\" has all required privileges in your MySQL server! " + e.getLocalizedMessage());
						}

					} finally {
						if (connectionTestDB != null)
							try { connectionTestDB.close(); } catch (SQLException e) { e.printStackTrace(); }
					}

				} finally {
					String sql = "DROP DATABASE " + testDB;
					try {
						statement.execute(sql);
					} catch (SQLException e) {
						System.err.println("Executing SQL failed: " + sql);
						e.printStackTrace();
						// no error message - it should be a warning though
						// TODO how can I give the user a warning in this installer framework? Marco.
					}
				}

			} finally {
				if (connection != null)
					try { connection.close(); } catch (SQLException e) { e.printStackTrace(); }
			}
		} // if (dbDriverName_noTx.contains("mysql")) {
	}
}
