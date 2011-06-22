/**
 * 
 */
package org.nightlabs.jfire.serverupdate.base;

import java.io.InputStream;
import java.io.PrintWriter;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URL;
import java.sql.Connection;
import java.util.HashSet;
import java.util.Set;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;

import org.nightlabs.jfire.serverupdate.launcher.ServerUpdateParameters;
import org.nightlabs.liquibase.datanucleus.LiquibaseDNConstants;
import org.nightlabs.util.IOUtil;
import org.nightlabs.version.MalformedVersionException;
import org.nightlabs.version.Version;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * @author abieber
 *
 */
public class LiquibaseUpdateProcedure extends UpdateProcedure {

	private final URL changeLogURL;
	private String changeLogModuleID;
	private Version changeLogFromVersion;
	private Version changeLogToVersion;
	
	/**
	 * 
	 */
	public LiquibaseUpdateProcedure(URL changeLogURL) {
		readChangeLogProperties(changeLogURL);
		this.changeLogURL = changeLogURL;
	}
	
	private void readChangeLogProperties(URL url) {
		
		try {
			
			InputStream in = url.openStream();
			try {
				
				SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
				
				parser.parse(in, new DefaultHandler() {

					@Override
					public void startElement(String uri, String localName,
							String qName, Attributes attributes)
							throws SAXException {
						
						if ("changeSet".equals(qName)) {
							String jfireAttributesString = attributes.getValue("id");
							String[] jfireAttrs = jfireAttributesString.split("\\|");
							if (jfireAttrs == null || jfireAttrs.length != 3) {
								throw new IllegalArgumentException("The udpate-changeLog-changeSet-id attribute was malformed '" + jfireAttributesString + "'. Format should be \"moduleID|fromVersion|toVersion\"");
							}
							changeLogModuleID = jfireAttrs[0].trim();
							try {
								changeLogFromVersion = new Version(jfireAttrs[1].trim());
								changeLogToVersion = new Version(jfireAttrs[2].trim());
							} catch (MalformedVersionException e) {
								throw new SAXException(e);
							}
						}
					}
				});
				
				if (changeLogModuleID == null || changeLogFromVersion == null || changeLogToVersion == null) {
					throw new IllegalArgumentException("The update-changelog '" + url.toString() + "' does not have the necessary jfire-properties set in its id-attribute (moduleID, fromVersion, toVersion).");
				}
					
			} finally {
				in.close();
			}
			
		} catch (Exception e) {
			throw new RuntimeException("Error while initially parsing update-changelog '" + url.toString() + "'", e);
		}
		
	}

	@Override
	protected String _getModuleID() {
		return changeLogModuleID;
	}

	@Override
	protected Version _getFromVersion() {
		return changeLogFromVersion;
	}

	@Override
	protected Version _getToVersion() {
		return changeLogToVersion;
	}
	
	public URL getChangeLogURL() {
		return changeLogURL;
	}

	
	/**
	 * InvocationHandler that implements a Proxy to {@link Connection} that will ignore all
	 * transaction-relevant calls. Transactions are managed by the {@link ServerUpdaterDelegate}
	 * so the Liquibase-transaction-management is cut off.
	 */
	static class ConnectionProxyHandler implements InvocationHandler {
		
		/** ignored methods */
		private static Set<String> ignoredMethods; 
		static {
			ignoredMethods = new HashSet<String>();
			ignoredMethods.add("setAutoCommit");
			ignoredMethods.add("commit");
			ignoredMethods.add("rollback");
		}
		/** The connection of this Handler */
		private Connection connection;
		
		public ConnectionProxyHandler(Connection connection) {
			super();
			this.connection = connection;
		}
		
		@Override
		public Object invoke(Object proxy, Method method, Object[] args)
				throws Throwable {
			if (ignoredMethods.contains(method.getName())) {
				// we ignore this method
				return null;
			}
			return method.invoke(connection, args);
		}
	};
	
	/**
	 * Creates a proxy-{@link Connection} that will be passed to the Liquibase-run. 
	 * 
	 * @param connection The connection to create a proxy for.
	 * @return A new proxy implementing {@link Connection} and delegating to the given connection.
	 */
	protected Connection createLiquibaseConnectionProxy(Connection connection) {
		return (Connection) Proxy.newProxyInstance(getClass().getClassLoader(),
				new Class<?>[] { Connection.class },
				new ConnectionProxyHandler(connection));
	}
	
	@Override
	public void run(ServerUpdateParameters parameters) throws Exception {
		
		// TODO: We know that for JFire we configure this value, however later we should read that from the persistence.xml
		System.setProperty(LiquibaseDNConstants.IDENTIFIER_CASE, "lowercase");
		
		// Create the Liquibase database using a connection-proxy from out UpdateContext
		DatabaseFactory databaseFactory = DatabaseFactory.getInstance();
		Connection connectionProxy = createLiquibaseConnectionProxy(getUpdateContext().getConnection());
		Database database = databaseFactory.findCorrectDatabaseImplementation(new JdbcConnection(connectionProxy));
		
		// Prepare the Resource-Path for the ClassLoaderResourceAccessor
		String resourcePath = changeLogURL.toString();
		if (changeLogURL.getProtocol().equalsIgnoreCase("jar")) {
			resourcePath = changeLogURL.getPath();
			resourcePath = resourcePath.substring(resourcePath.indexOf("!") + 2);
		}
		
		// Create the Liquibase instance
		Liquibase liquibase = new Liquibase(
				resourcePath,
				new ClassLoaderResourceAccessor(getClass().getClassLoader()),
				database);
		
		if (!parameters.isDryRun()) {
			// We are not running dry, so we actually perform an update using liquibase
			liquibase.update(null);
		} else {
			// We are running dry, so we configure liquibase to print a PrintWriter
			PrintWriter lqWriter = new PrintWriter(System.out);
			if (parameters.getDryRunFile() != null) {
				lqWriter = new PrintWriter(parameters.getDryRunFile(), IOUtil.CHARSET_NAME_UTF_8);
			}
			liquibase.update(null, lqWriter);
			lqWriter.flush();
		}
		
		liquibase.update(null, new PrintWriter(System.err));
		System.err.flush();
	}

}
