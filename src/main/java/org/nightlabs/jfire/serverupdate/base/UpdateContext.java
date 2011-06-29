package org.nightlabs.jfire.serverupdate.base;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.nightlabs.jfire.serverupdate.base.db.JDBCConfiguration;
import org.nightlabs.util.IOUtil;

/**
 * @author Chairat Kongarayawetchakun - chairat [AT] nightlabs [DOT] de
 */
public class UpdateContext 
{
	private Connection connection;
	private JDBCConfiguration configuration;
	private ServerUpdateParameters parameters;
	private PrintWriter dryRunPrintWriter;
	
	public UpdateContext(JDBCConfiguration configuration) {
		this.configuration = configuration;
	}
	
	public Connection getConnection() {
		try {
			if (connection == null) {
				System.setProperty("jdbc.drivers", configuration.getDriverClass());
				connection = DriverManager.getConnection(
						configuration.getDatabaseURL(), configuration.getUserName(), configuration.getPassword());
			}
		} catch (SQLException e) {
			throw new RuntimeException(e);		
		}
		return connection;
	}

	public ServerUpdateParameters getParameters() {
		return parameters;
	}

	public void setParameters(ServerUpdateParameters parameters) {
		this.parameters = parameters;
	}
	
	/**
	 * Returns the {@link PrintWriter} that can be used to print the update-output to a file or System.err.
	 * It is in the update-context, because the individual {@link LiquibaseUpdateProcedure}s should write to 
	 * the same file.
	 * 
	 * @return A {@link PrintWriter} to write to.
	 * @throws FileNotFoundException ...
	 * @throws UnsupportedEncodingException ...
	 */
	public PrintWriter getDryRunPrintWriter() throws FileNotFoundException, UnsupportedEncodingException {
		if (dryRunPrintWriter == null) {
			dryRunPrintWriter = new PrintWriter(System.out);
			File dryRunFile = parameters.getDryRunFile();
			if (parameters != null && dryRunFile != null) {
				String fileNamePrefix = IOUtil.getFileNameWithoutExtension(dryRunFile.getName());
				String fileExt = IOUtil.getFileExtension(dryRunFile.getName());
				if (fileExt == null) {
					fileExt = "";
				}
				if (!fileExt.isEmpty()) {
					fileExt = "." + fileExt;
				}
				dryRunFile = new File(dryRunFile.getParentFile(), fileNamePrefix + "_" + configuration.getStrippedJndiName() + fileExt);
				dryRunPrintWriter = new PrintWriter(dryRunFile, IOUtil.CHARSET_NAME_UTF_8);
			}
		}
		return dryRunPrintWriter;
	}
	
}