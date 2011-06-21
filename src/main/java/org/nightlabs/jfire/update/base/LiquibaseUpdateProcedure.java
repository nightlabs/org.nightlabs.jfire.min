/**
 * 
 */
package org.nightlabs.jfire.update.base;

import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URL;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;

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
								throw new IllegalArgumentException("The udpate-changeLog-changeSet-id attribute was malformed '" + jfireAttributesString + "'. Format should be moduleID|fromVersion|toVersion");
							}
							changeLogModuleID = jfireAttrs[0].trim();
							try {
								changeLogFromVersion = new Version(jfireAttrs[1].trim());
								changeLogToVersion = new Version(jfireAttrs[2].trim());
							} catch (MalformedVersionException e) {
								throw new SAXException(e);
							}
						}
						
						if ("property".equals(qName)) {
							String nameValue = attributes.getValue("name");
							String value = attributes.getValue("value");
							if ("jfire.update.moduleID".equals(nameValue)) {
								changeLogModuleID = value;
							} else if ("jfire.update.fromVersion".equals(nameValue)) {
								try {
									changeLogFromVersion = new Version(value);
								} catch (MalformedVersionException e) {
									throw new SAXException(e);
								}
							} else if ("jfire.update.toVersion".equals(nameValue)) {
								try {
									changeLogToVersion = new Version(value);
								} catch (MalformedVersionException e) {
									throw new SAXException(e);
								}
							}
						}
					}
				});
				
				if (changeLogModuleID == null || changeLogFromVersion == null || changeLogToVersion == null) {
					throw new IllegalArgumentException("The update-changelog '" + url.toString() + "' does not have the necessary jfire-properties set (moduleID, fromVersion, toVersion).");
				}
					
			} finally {
				in.close();
			}
			
		} catch (Exception e) {
			throw new RuntimeException("Error while initially parsing update-changelog '" + url.toString() + "'", e);
		}
		
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.update.UpdateProcedure#_getModuleID()
	 */
	@Override
	protected String _getModuleID() {
		return changeLogModuleID;
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.update.UpdateProcedure#_getFromVersion()
	 */
	@Override
	protected Version _getFromVersion() {
		return changeLogFromVersion;
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.update.UpdateProcedure#_getToVersion()
	 */
	@Override
	protected Version _getToVersion() {
		return changeLogToVersion;
	}
	
	public URL getChangeLogURL() {
		return changeLogURL;
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.update.UpdateProcedure#run()
	 */
	@Override
	public void run() throws Exception {
//		getUpdateContext().createConnection();
		DatabaseFactory databaseFactory = DatabaseFactory.getInstance();
		Database database = databaseFactory.findCorrectDatabaseImplementation(new JdbcConnection(getUpdateContext().createConnection()));
		String resourcePath = changeLogURL.toString();
		if (changeLogURL.getProtocol().equalsIgnoreCase("jar")) {
			resourcePath = changeLogURL.getPath();
			resourcePath = resourcePath.substring(resourcePath.indexOf("!") + 2);
		}
		Liquibase liquibase = new Liquibase(resourcePath, new ClassLoaderResourceAccessor(getClass().getClassLoader()), database);
		liquibase.update(null, new PrintWriter(System.err));
		System.err.flush();
	}

}
