package org.nightlabs.jfire.serverconfigurator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Date;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.apache.log4j.Logger;
import org.nightlabs.jfire.serverconfigurator.ServerConfiguratorHistory.ServerConfiguratorAction;
import org.nightlabs.jfire.servermanager.config.JFireServerConfigModule;
import org.nightlabs.jfire.servermanager.config.ServletSSLCf;
import org.nightlabs.util.IOUtil;

/**
 * You can configure one implementation of <code>ServerConfigurator</code> for your JFire server
 * (see {@link JFireServerConfigModule#getJ2ee()}),
 * which is triggered on every server start. The ServerConfigurator should ensure that your
 * server is configured in the appropriate way.
 *
 * @author Marco Schulze - marco at nightlabs dot de
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public abstract class ServerConfigurator
{
	/**
	 * LOG4J logger used by this class
	 */
	private static final Logger logger = Logger.getLogger(ServerConfigurator.class);

	/**
	 * The config to use for configuration
	 */
	private JFireServerConfigModule jfireServerConfigModule;

	/**
	 * Wether a reboot is required after doing the changes.
	 */
	private boolean rebootRequired = false;

	/**
	 * Get the jfireServerConfigModule.
	 * @return the jfireServerConfigModule
	 */
	public JFireServerConfigModule getJFireServerConfigModule()
	{
		return jfireServerConfigModule;
	}

	/**
	 * Set the jfireServerConfigModule.
	 * @param jfireServerConfigModule the jfireServerConfigModule to set
	 */
	public void setJFireServerConfigModule(JFireServerConfigModule jfireServerConfigModule)
	{
		this.jfireServerConfigModule = jfireServerConfigModule;
	}

	/**
	 * This method indicates whether the j2ee server needs to be rebooted.
	 * @return Returns true after {@link #doConfigureServer()} has been called,
	 * 		if it modified the server configuration in a way that requires it to
	 * 		be rebooted.
	 */
	public boolean isRebootRequired()
	{
		return rebootRequired;
	}

	/**
	 * Set the reboot required value. If set to <code>true</code>, the
	 * server will be stopped after all server configurations have happened.
	 * @param rebootRequired <code>true</code> if a reboot is required -
	 * 		<code>false</code> otherwise
	 */
	protected void setRebootRequired(boolean rebootRequired)
	{
		// NEVER REMOVE THIS LINE:
		logger.info("setRebootRequired: rebootRequired=" + rebootRequired);
		this.rebootRequired = rebootRequired;
	}

	/**
	 * Configure the server using the {@link ServerConfigurator} defined in the
	 * given server config module.
	 * <p>
	 * This method will return <code>true</code> if the server needs to be restarted.
	 *
	 * @param jfireServerConfigModule The server config module
	 * @return <code>true</code> if the server needs to be restarted -
	 * 		<code>false</code> otherwise.
	 * @throws ServerConfigurationException In case of an error during configuration.
	 */
	public static boolean configureServer(JFireServerConfigModule jfireServerConfigModule) throws ServerConfigurationException
	{
		boolean rebootRequired = false;

		// instantiating and calling ServerConfigurator
		String serverConfiguratorClassName = jfireServerConfigModule.getJ2ee().getServerConfigurator();

		if (serverConfiguratorClassName == null || "".equals(serverConfiguratorClassName))
			throw new ServerConfigurationException("jfireServerConfigModule.getJ2ee().getServerConfigurator() returned null or an empty string!");


		logger.info("Loading database driver classes."); // the server configurator might need to communicate with a database server
		try {
			Class.forName(jfireServerConfigModule.getDatabase().getDatabaseDriverName_noTx());
		} catch (Exception x) {
			logger.warn("Could not load database driver configured for noTx!", x);
		}
		try {
			Class.forName(jfireServerConfigModule.getDatabase().getDatabaseDriverName_localTx());
		} catch (Exception x) {
			logger.warn("Could not load database driver configured for localTx!", x);
		}
		try {
			Class.forName(jfireServerConfigModule.getDatabase().getDatabaseDriverName_xa());
		} catch (Exception x) {
			logger.warn("Could not load database driver configured for xa!", x);
		}
		logger.info("Configuring server with ServerConfigurator " + serverConfiguratorClassName);


		if (logger.isDebugEnabled())
			logger.debug("Instantiating ServerConfigurator: " + serverConfiguratorClassName);

		Class<?> serverConfiguratorClass;
		try {
			serverConfiguratorClass = Class.forName(serverConfiguratorClassName);
		} catch (Throwable x) {
			throw new ServerConfigurationException("Loading ServerConfigurator class " + serverConfiguratorClassName + " (configured in JFireServerConfigModule) failed!", x);
		}

		if (!ServerConfigurator.class.isAssignableFrom(serverConfiguratorClass))
			throw new IllegalStateException("ServerConfigurator " + serverConfiguratorClassName + " (configured in JFireServerConfigModule) does not extend class " + ServerConfigurator.class);

		ServerConfigurator serverConfigurator;
		try {
			serverConfigurator = (ServerConfigurator) serverConfiguratorClass.newInstance();
			serverConfigurator.setJFireServerConfigModule(jfireServerConfigModule);
		} catch (Throwable x) {
			throw new ServerConfigurationException("Instantiating ServerConfigurator from class " + serverConfiguratorClassName + " (configured in JFireServerConfigModule) failed!", x);
		}


		// before we configure the server, we check whether another server-configurator was used before
		// and needs to undo its configuration changes
		File serverConfiguratorHistoryFile = new File(jfireServerConfigModule._getConfig().getConfigDir(), ServerConfigurator.class.getName() + ".history");
		ServerConfiguratorHistory serverConfiguratorHistory;
		try {
			serverConfiguratorHistory = new ServerConfiguratorHistory(serverConfiguratorHistoryFile);
		} catch (IOException e) {
			throw new ServerConfigurationException("Could not instantiate ServerConfiguratorHistory!", e);
		}
		ServerConfiguratorHistory.Item lastHistoryItem = serverConfiguratorHistory.getLastItem();
		if (
				lastHistoryItem != null &&
				!serverConfiguratorClassName.equals(lastHistoryItem.getServerConfiguratorClassName()) &&
				ServerConfiguratorAction.undoConfigureServer != lastHistoryItem.getServerConfiguratorAction()
		)
		{
			// The last server configuration action was done by a different configurator and it was not undo.
			// That means, we must undo now!

			if (logger.isDebugEnabled())
				logger.debug("Instantiating ServerConfigurator for undo: " + lastHistoryItem.getServerConfiguratorClassName());

			Class<?> serverConfiguratorClassForUndo;
			try {
				serverConfiguratorClassForUndo = Class.forName(lastHistoryItem.getServerConfiguratorClassName());
			} catch (Throwable x) {
				throw new ServerConfigurationException("Loading ServerConfigurator class " + lastHistoryItem.getServerConfiguratorClassName() + " (from history) failed!", x);
			}

			if (!ServerConfigurator.class.isAssignableFrom(serverConfiguratorClassForUndo))
				throw new IllegalStateException("ServerConfigurator " + serverConfiguratorClassName + " (from history) does not extend class " + ServerConfigurator.class); // should really never happen!

			ServerConfigurator serverConfiguratorForUndo;
			try {
				serverConfiguratorForUndo = (ServerConfigurator) serverConfiguratorClassForUndo.newInstance();
				serverConfiguratorForUndo.setJFireServerConfigModule(jfireServerConfigModule);
			} catch (Throwable x) {
				throw new ServerConfigurationException("Instantiating ServerConfigurator from class " + serverConfiguratorClassName + " (from history) failed!", x);
			}

			try {
				serverConfiguratorForUndo.undoConfigureServer();
			} catch (Exception x) {
				serverConfiguratorForUndo.afterUndoConfigureServer(x);
				if (x instanceof ServerConfigurationException)
					throw (ServerConfigurationException) x;
				else
					throw new ServerConfigurationException(x);
			}
			serverConfiguratorForUndo.afterUndoConfigureServer(null);

			if (serverConfiguratorForUndo.isRebootRequired())
				rebootRequired = true;

			// we store the history after undoing the configuration, so the undo is only written (thus preventing further undos)
			// when the undo has finished successfully without exception.
			serverConfiguratorHistory.addItem(new ServerConfiguratorHistory.Item(new Date(), serverConfiguratorClassForUndo.getName(), ServerConfiguratorAction.undoConfigureServer));
			try {
				serverConfiguratorHistory.write();
			} catch (IOException e) {
				throw new ServerConfigurationException("Could not write ServerConfiguratorHistory!", e);
			}
		}

		// We first store the history and then actually perform the configuration, because
		// an exception might happen when parts of the configuration were changed - already requiring undo.
		serverConfiguratorHistory.addItem(new ServerConfiguratorHistory.Item(new Date(), serverConfiguratorClass.getName(), ServerConfiguratorAction.doConfigureServer));
		try {
			serverConfiguratorHistory.write();
		} catch (IOException e) {
			throw new ServerConfigurationException("Could not write ServerConfiguratorHistory!", e);
		}

		try {
			serverConfigurator.doConfigureServer();
		} catch (Exception x) {
			serverConfigurator.afterDoConfigureServer(x);
			if (x instanceof ServerConfigurationException)
				throw (ServerConfigurationException) x;
			else
				throw new ServerConfigurationException(x);
		}
		serverConfigurator.afterDoConfigureServer(null);

		if (serverConfigurator.isRebootRequired())
			rebootRequired = true;

		return rebootRequired;
	}

	private static File getNonExistingFile(File directory, String pattern)
	{ // This method was moved from ServerConfiguratorJBoss to here. Marco.
		if(pattern == null)
			throw new NullPointerException("pattern is null");

//		synchronized(pattern) {
			// This block was originally synchronized on the pattern argument. I don't think that this made much sense
			// since even with the same pattern, there might be different String instances. Thus, I think we either don't
			// synchronize at all or we use IOUtil.class for synchronization, because e.g. IOUtil.createUniqueRandomFolder uses
			// the same object for synchronization.
			// I changed my mind and commented the synchronized block. Synchronization is not necessary and doesn't make sense
			// anyway without creating the file *within* the synchronized block. If the synchronized block ends before the
			// file is created, the file might be created by another thread before this method returns but after the synchronized
			// block ended. Hence, it makes no sense at all here.
			int idx = 1;
			File f;
			do {
				if (directory == null)
					f = new File(String.format(pattern, idx));
				else
					f = new File(directory, String.format(pattern, idx));

				idx++;
			} while(f.exists());
			return f;
//		}
	}

	/**
	 * Get the backup directory for the given file or directory. This method will create a <code>File</code> instance
	 * pointing to a sibling of the given <code>fileOrDirectory</code> instance. It will not create the directory in the
	 * file system. If you want this, use {@link #createBackupDirectory(File)} instead.
	 *
	 * @param fileOrDirectory the file or directory.
	 * @return the instance of <code>File</code> denoting the backup directory.
	 */
	private File getBackupDirectory(File fileOrDirectory)
	{
		File backupDir = new File(fileOrDirectory.getParentFile(), ".bak-" + this.getClass().getName() + "-bak");
		return backupDir;
	}

	/**
	 * Get the backup directory for the given file or directory and make sure it exists in the file system.
	 * In contrast to {@link #getBackupDirectory(File)}, this method will actually create the directory if it
	 * does not exist.
	 *
	 * @param fileOrDirectory the file or directory.
	 * @return the instance of <code>File</code> denoting the backup directory.
	 * @throws IOException if the backup directory does not exist and creation failed.
	 */
	private File createBackupDirectory(File fileOrDirectory)
	throws IOException
	{
		File backupDir = getBackupDirectory(fileOrDirectory);
		if (!backupDir.exists()) {
			if (!backupDir.mkdirs())
				throw new IOException("Could not create directory: " + backupDir.getAbsolutePath());
		}
		return backupDir;
	}

	protected File backup(File f) throws IOException
	{
		if(!f.exists() || !f.canRead())
			throw new FileNotFoundException("Invalid file to backup: "+f);

		File backupDir = createBackupDirectory(f);

		File backupFile = new File(backupDir, f.getName() + ".bak");
		if(backupFile.exists())
			backupFile = getNonExistingFile(backupDir, f.getName() + ".%d.bak");

		if(f.isDirectory())
			IOUtil.copyDirectory(f, backupFile);
		else
			IOUtil.copyFile(f, backupFile);

		logger.info("Created backup of file "+f.getAbsolutePath()+": "+backupFile.getName());
		return backupFile;
	}

	protected File moveToBackup(File f) throws IOException
	{
		if(!f.exists())
			throw new FileNotFoundException("Invalid file to backup: "+f);

		File backupDir = createBackupDirectory(f);

		File backupFile = new File(backupDir, f.getName() + ".bak");
		if(backupFile.exists())
			backupFile = getNonExistingFile(backupDir, f.getName() + ".%d.bak");

		if(!f.renameTo(backupFile))
			throw new IOException("Renaming file "+f.getAbsolutePath()+" to "+f.getName()+" failed");
		return backupFile;
	}

	private Set<File> backupDirsUsedForRestore = new HashSet<File>();

	protected void restore(File originalFile) throws IOException
	{
		File backupDir = getBackupDirectory(originalFile);
		if (!backupDir.exists()) {
			Exception x = new IOException("Backup directory \"" + backupDir.getAbsolutePath() + "\" does not exist! Cannot restore file: " + originalFile.getAbsolutePath());
			logger.error(x.getMessage(), x);
			return;
		}

		File backupFile = new File(backupDir, originalFile.getName() + ".bak");
		if(!backupFile.exists()) {
			Exception x = new IOException("Backup file \"" + backupFile.getAbsolutePath() + "\" does not exist! Cannot restore file: " + originalFile.getAbsolutePath());
			logger.error(x.getMessage(), x);
			return;
		}

		if(backupFile.isDirectory())
			IOUtil.copyDirectory(backupFile, originalFile);
		else
			IOUtil.copyFile(backupFile, originalFile);

		backupDirsUsedForRestore.add(backupDir);
	}

	/**
	 * This method is called as well explicitely by a GUI operation (via the web-frontend for
	 * server initialization or the JFire installer), as implicitely on every server start.
	 * <p>
	 * Configuration changes might require the server to be rebooted. If you changed the
	 * configuration in a way that renders the server non-workable or require a reboot for
	 * another reason, you should call {@link #setRebootRequired(boolean)}.
	 *
	 * @throws ServerConfigurationException In case of an error during configuration.
	 */
	protected abstract void doConfigureServer() throws ServerConfigurationException;

	private File jfireServerKeystoreFile = new File("jfire-server.keystore");

	protected void afterDoConfigureServer(Throwable x) throws ServerConfigurationException
	{
		final JFireServerConfigModule jfireServerConfigModule = getJFireServerConfigModule();

		if (!jfireServerConfigModule.getServletSSLCf().isKeystoreURLImported())
		{
			final String keystoreURLToImport = jfireServerConfigModule.getServletSSLCf().getKeystoreURLToImport();
			FileOutputStream keystorePropFileStream = null;

			try
			{
				InputStream keystoreToImportStream;
				// distinguish between default and non-default keystore via this constant
				if ("".equals(keystoreURLToImport))
				{
					throw new IllegalStateException("No keystore can be found in " +
							"%jboss%/bin/jfire-server.keystore and no keystoreToImport is set!");
				}
				else if (ServletSSLCf.DEFAULT_KEYSTORE.equals(keystoreURLToImport))
				{
					keystoreToImportStream = ServerConfigurator.class.getResourceAsStream("/jfire-server.keystore");
				}
				else
					keystoreToImportStream = new URL(keystoreURLToImport).openStream();

				boolean transferData = false;
				if (! jfireServerKeystoreFile.exists() || ! jfireServerKeystoreFile.canRead())
				{
					transferData = true;
				}
				else
				{
					FileInputStream fileInputStream = new FileInputStream(jfireServerKeystoreFile);
					if (fileInputStream.available() != keystoreToImportStream.available() ||
							! IOUtil.compareInputStreams(
									keystoreToImportStream, fileInputStream, fileInputStream.available() ))
					{
						transferData = true;
					}
				}

				final ServletSSLCf servletSSLCf = jfireServerConfigModule.getServletSSLCf();

				// if files are equal -> don't need to copy.
				if (! transferData)
				{
					// set the keystore file to the imported state (== set the keystoreURLToImport = "")
					servletSSLCf.setKeystoreURLImported();
					return;
				}

				// files differ or the destination file doesn't exist yet
				FileOutputStream keyStoreStream = new FileOutputStream(jfireServerKeystoreFile);
				try {
					IOUtil.transferStreamData(keystoreToImportStream, keyStoreStream);
				}	finally {
					keyStoreStream.close();
				}

				// Write all ssl socket related infos into a properties file next to jfire-server.keystore,
				// because the org.nightlabs.rmissl.socket.SSLCompressionServerSocketFactory and
				// org.nightlabs.rmissl.socket.SSLCompressionRMIServerSocketFactory need to know these
				// infos and the projects don't know anything from each other (and are not allowed to).
				Properties props = new Properties();
				props.put("org.nightlabs.ssl.keystorePassword", servletSSLCf.getKeystorePassword());
				props.put("org.nightlabs.ssl.serverCertificateAlias", servletSSLCf.getSslServerCertificateAlias());
				props.put("org.nightlabs.ssl.serverCertificatePassword", servletSSLCf.getSslServerCertificatePassword());
				File keystorePropFile = new File("jfire-server.keystore.properties").getAbsoluteFile();
				keystorePropFileStream = new FileOutputStream(keystorePropFile);
				props.store(keystorePropFileStream, "The properties needed to read the correct private " +
						"certificate from the jfire-server.keystore.\n" +
						"These credentials are needed by the SSLCompressionServerSocketFactory.");

				// set the keystore file to the imported state (== set the keystoreURLToImport = "")
				servletSSLCf.setKeystoreURLImported();
				setRebootRequired(true);
			}
			catch (IOException e) {
				throw new ServerConfigurationException(e);
			}
			finally {
				if (keystorePropFileStream != null)
				{
					try
					{
						keystorePropFileStream.close();
					}
					catch (IOException e)
					{
						throw new ServerConfigurationException("Couldn't close the output stream from writing "+
								"the keystore properties!", e);
					}
				}
			}
		}
	}

	/**
	 * Revert the server configuration to the state it had before {@link #doConfigureServer()}
	 * was called (the first time). The easiest way to achieve this is making backup copies
	 * in {@link #doConfigureServer()} and replacing the modified files by their backup copies
	 * in this method.
	 *
	 * @throws ServerConfigurationException In case of an error during reversion of the configuration.
	 */
	protected abstract void undoConfigureServer() throws ServerConfigurationException;

	protected void afterUndoConfigureServer(Throwable x) throws ServerConfigurationException
	{
		if (x == null) {
			for (File dir : backupDirsUsedForRestore)
				IOUtil.deleteDirectoryRecursively(dir);

			backupDirsUsedForRestore.clear();

			try {
				clearKeystoreFile();
			} catch (IOException e) {
				throw new ServerConfigurationException(e);
			}
		}
	}

	private void clearKeystoreFile() throws IOException
	{
		if (!jfireServerKeystoreFile.exists())
			return;

		int backupFileIndex = 0;
		File backupFile;
		do {
			backupFile = new File(jfireServerKeystoreFile.getAbsolutePath() + '.' + (backupFileIndex++) + ".bak");

			if (backupFile.exists() && IOUtil.compareFiles(jfireServerKeystoreFile, backupFile)) {
				jfireServerKeystoreFile.delete();
				return;
			}
		} while (backupFile.exists());

		if (!jfireServerKeystoreFile.renameTo(backupFile)) {
			IOUtil.copyFile(jfireServerKeystoreFile, backupFile);
			jfireServerKeystoreFile.delete();
		}
	}
}
