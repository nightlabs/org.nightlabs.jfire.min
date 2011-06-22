package org.nightlabs.jfire.serverupdate.base;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.nightlabs.jfire.serverupdate.base.db.JDBCConfiguration;

/**
 * @author Chairat Kongarayawetchakun - chairat [AT] nightlabs [DOT] de
 */
public class UpdateContext 
{
	private Connection connection;
	private JDBCConfiguration configuration;
	
	public UpdateContext(JDBCConfiguration configuration) {
		this.configuration = configuration;
	}
	
	public Connection getConnection() {
		try {
			if (connection == null)
				System.setProperty("jdbc.drivers", configuration.getDriverClass());
				connection = DriverManager.getConnection(
						configuration.getDatabaseURL(), configuration.getUserName(), configuration.getPassword());
		} catch (SQLException e) {
			throw new RuntimeException(e);		
		}
		return connection;
	}
}