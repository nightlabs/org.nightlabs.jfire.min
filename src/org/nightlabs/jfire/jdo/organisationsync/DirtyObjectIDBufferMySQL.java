/*
 * Created on Sep 15, 2005
 */
package org.nightlabs.jfire.jdo.organisationsync;

import java.lang.reflect.Constructor;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.jdo.PersistenceManager;

import org.apache.log4j.Logger;

import org.nightlabs.jdo.ObjectIDUtil;

public class DirtyObjectIDBufferMySQL
implements DirtyObjectIDBuffer
{
	public static final Logger LOGGER = Logger.getLogger(DirtyObjectIDBufferMySQL.class);

	private OrganisationSyncManagerFactory organisationSyncManagerFactory;

	protected static class ConnectionHandle
	{
		private PersistenceManager persistenceManager;
		private Connection connection;
		public ConnectionHandle(PersistenceManager persistenceManager)
		{
			this.persistenceManager = persistenceManager;
			this.connection = (Connection) persistenceManager.getDataStoreConnection().getNativeConnection();
			if (this.connection == null)
				throw new NullPointerException("persistenceManager.getDataStoreConnection().getNativeConnection() returned null!");
		}
		public PersistenceManager getPersistenceManager()
		{
			return persistenceManager;
		}
		public Connection getConnection()
		{
			return connection;
		}
		public void close()
		{
			persistenceManager.close();
		}
	}

	private ConnectionHandle connection_add;
	private ConnectionHandle connection_process = null;

	public DirtyObjectIDBufferMySQL() { }

	protected ConnectionHandle createConnection() throws SQLException
	{
		return new ConnectionHandle(
				organisationSyncManagerFactory.getPersistenceManagerFactory().getPersistenceManager());
//		return DriverManager.getConnection(
//				organisationSyncManagerFactory.getPersistenceManagerFactory().getConnectionURL());
	}

	/**
	 * @see org.nightlabs.jfire.jdo.organisationsync.DirtyObjectIDBuffer#init(org.nightlabs.jfire.jdo.organisationsync.OrganisationSyncManagerFactory)
	 */
	public void init(OrganisationSyncManagerFactory organisationSyncManagerFactory) throws DirtyObjectIDBufferException
	{
		this.organisationSyncManagerFactory = organisationSyncManagerFactory;

		try {
			this.connection_add = createConnection();

			Statement statement = connection_add.getConnection().createStatement();
			try {
				statement.executeUpdate(
						"CREATE TABLE IF NOT EXISTS OutgoingChangeListenerDescriptor (" +
						"  objectIDClassName VARCHAR(255) NOT NULL," +
						"  objectIDFieldPart VARCHAR(255) NOT NULL," +
						"  inProcess TINYINT NOT NULL," +
						"  PRIMARY KEY (objectIDClassName, objectIDFieldPart)" +
						") ENGINE = MyISAM");
			} finally {
				statement.close();
			}
		} catch (Exception x) {
			throw new DirtyObjectIDBufferException(x);
		}
	}

	public OrganisationSyncManagerFactory getOrganisationSyncManagerFactory()
	{
		return organisationSyncManagerFactory;
	}


	/**
	 * @see org.nightlabs.jfire.jdo.organisationsync.DirtyObjectIDBuffer#addDirtyObjectIDs(java.util.Collection)
	 */
	public void addDirtyObjectIDs(Collection objectIDs) throws DirtyObjectIDBufferException
	{
		boolean successful = false;
		int tryNumber = 0;
		while (!successful) {
			try {
				if (connection_add == null)
					connection_add = createConnection();

				Statement statement = connection_add.getConnection().createStatement();
				try {
					for (Iterator it = objectIDs.iterator(); it.hasNext(); ) {
						String[] parts = ObjectIDUtil.splitObjectIDString(it.next().toString());
						String className = parts[0];
						String fieldPart = parts[1];
						statement.executeUpdate("REPLACE OutgoingChangeListenerDescriptor VALUES (\""+className+"\", \""+fieldPart+"\", 0)");
					}
				} finally {
					statement.close();
				}

				successful = true;
			} catch (Throwable x) {
				safeCloseConnection(connection_add);
				connection_add = null;
				if (tryNumber >= 1)
					throw new RuntimeException(x);
				else
					LOGGER.error("Exception while storing dirty object IDs! Trying again after reconnect.", x);
			}

			++tryNumber;
		} // while (!successful) {
	}

	private boolean fetchDirtyObjectIDsClear = true;

	/**
	 * @see org.nightlabs.jfire.jdo.organisationsync.DirtyObjectIDBuffer#fetchDirtyObjectIDs()
	 */
	public synchronized Set fetchDirtyObjectIDs() throws DirtyObjectIDBufferException
	{
		if (!fetchDirtyObjectIDsClear)
			LOGGER.warn("fetchDirtyObjectIDs() called again before clearFetchedDirtyObjectIDs()! Maybe there was an error during last execution cycle.", new Exception());

		fetchDirtyObjectIDsClear = false;

		try {
			HashSet res = new HashSet();

			if (connection_process == null)
				connection_process = createConnection();

			Statement statement = connection_process.getConnection().createStatement();
			try {
				statement.executeUpdate("UPDATE OutgoingChangeListenerDescriptor SET inProcess = 1");
				ResultSet resultSet = statement.executeQuery("SELECT * FROM OutgoingChangeListenerDescriptor WHERE inProcess = 1");
				while (resultSet.next()) {
					String objectIDClassName = resultSet.getString("objectIDClassName");
					String objectIDFieldPart = resultSet.getString("objectIDFieldPart");

					Class objectIDClass;
					try {
						objectIDClass = Class.forName(objectIDClassName);
					} catch (ClassNotFoundException e) {
						throw new DirtyObjectIDBufferException("Could not load class with objectIDClassName=\""+objectIDClassName+"\"! Check table OutgoingChangeListenerDescriptor!", e);
					}

					Constructor constructor;
					try {
						constructor = objectIDClass.getConstructor(new Class[]{ String.class });
					} catch (SecurityException x) {
						throw new DirtyObjectIDBufferException("Cannot access the one-string-constructor of class with objectIDClassName=\""+objectIDClassName+"\"! This class should have a public one-string-constructor!", x);
					} catch (NoSuchMethodException x) {
						throw new DirtyObjectIDBufferException("The class with objectIDClassName=\""+objectIDClassName+"\" misses the one-string-constructor! This class should have a public constructor with one String-parameter!", x);
					}

					String keyStr = ObjectIDUtil.createObjectIDString(objectIDClassName, objectIDFieldPart);
					Object objectID;
					try {
						objectID = constructor.newInstance(new Object[]{ keyStr });
					} catch (Throwable e) {
						throw new DirtyObjectIDBufferException("Could not instantiate class with objectIDClassName=\""+objectIDClassName+"\" and objectIDString=\"" + keyStr + "\"!", e);
					}

					res.add(objectID);
				}
			} finally {
				statement.close();
			}

			return res;
		} catch (SQLException e) {
			safeCloseConnection(connection_process);
			connection_process = null;
			throw new DirtyObjectIDBufferException(e);
		}
	}

	/**
	 * @see org.nightlabs.jfire.jdo.organisationsync.DirtyObjectIDBuffer#clearFetchedDirtyObjectIDs()
	 */
	public synchronized void clearFetchedDirtyObjectIDs() throws DirtyObjectIDBufferException
	{
		try {
			if (connection_process == null)
				connection_process = createConnection();

			Statement statement = connection_process.getConnection().createStatement();
			try {
				statement.executeUpdate("DELETE FROM OutgoingChangeListenerDescriptor WHERE inProcess = 1");
			} finally {
				statement.close();
			}
		} catch (SQLException e) {
			safeCloseConnection(connection_process);
			connection_process = null;
		}

		fetchDirtyObjectIDsClear = true;
	}

	protected static void safeCloseConnection(ConnectionHandle connectionHandle)
	{
		if (connectionHandle == null)
			return;

		try {
			connectionHandle.close();
		} catch (Throwable t) {
			LOGGER.error("Closing SQL connection failed!", t);
		}
	}
}
