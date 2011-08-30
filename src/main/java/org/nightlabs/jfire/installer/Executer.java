package org.nightlabs.jfire.installer;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.spi.LoggingEvent;
import org.nightlabs.config.Config;
import org.nightlabs.installer.Constants;
import org.nightlabs.installer.ExecutionProgressEvent;
import org.nightlabs.installer.Logger;
import org.nightlabs.installer.base.InstallationException;
import org.nightlabs.installer.base.defaults.DefaultExecuter;
import org.nightlabs.installer.util.DaemonThreadFactory;
import org.nightlabs.installer.util.LineConsumer;
import org.nightlabs.installer.util.ObservedProcess;
import org.nightlabs.installer.util.ProgramException;
import org.nightlabs.installer.util.Programs;
import org.nightlabs.installer.util.Util;
import org.nightlabs.jfire.jboss.serverconfigurator.config.ServicePortsConfigModule;
import org.nightlabs.jfire.organisation.CreateOrganisationAfterRebootData;
import org.nightlabs.jfire.server.data.dir.JFireServerDataDirectory;
import org.nightlabs.jfire.servermanager.config.DatabaseCf;
import org.nightlabs.jfire.servermanager.config.J2eeCf;
import org.nightlabs.jfire.servermanager.config.JDOCf;
import org.nightlabs.jfire.servermanager.config.JFireServerConfigModule;
import org.nightlabs.jfire.servermanager.config.RootOrganisationCf;
import org.nightlabs.jfire.servermanager.config.ServerCf;
import org.nightlabs.util.IOUtil;

import com.izforge.izpack.util.OsVersion;
import com.izforge.izpack.util.TargetFactory;
import com.izforge.izpack.util.os.Shortcut;

/**
 * @version $Revision$ - $Date$
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 * @author Daniel Mazurek - Daniel.Mazurek [dot] nightlabs [dot] de
 */
public class Executer extends DefaultExecuter
{
//	private static final String JAR_CONTENT_PATH = "jboss"; //$NON-NLS-1$
	private static final String JAR_CONTENT_PATH = "jfire-server"; //$NON-NLS-1$ // renamed 2011-08-30

	public static final String ZIP_ENTRY_SEPARATOR = "/"; //$NON-NLS-1$

	private Set<File> filesToCleanup = new HashSet<File>();
	private int workDone = 0;
	private Integer totalWork = null;
	private Executor threadPool = Executors.newCachedThreadPool(new DaemonThreadFactory());

	private static Executer runningInstance;

	public Executer()
	{
		runningInstance = this;
	}

	public static class LogAppender extends AppenderSkeleton
	{
		@Override
		protected void append(LoggingEvent event)
		{
			runningInstance.fireProgress(event.getRenderedMessage());
		}

		// @Override // since we compile the installer for Java 5, we cannot tag the overriding of an interface method
		public void close()
		{
		}

		// @Override // since we compile the installer for Java 5, we cannot tag the overriding of an interface method
		public boolean requiresLayout()
		{
			return false;
		}
	}

	void fireProgress(String description)
	{
		ExecutionProgressEvent executionProgressEvent = new ExecutionProgressEvent(
				runningInstance,
				description,
				ExecutionProgressEvent.Type.progress,
				(double)++workDone,
				getTotalWork()
		);
		//System.out.println("workDone: "+workDone);
		fireProgress(executionProgressEvent);
	}

	void fireProgress(ExecutionProgressEvent executionProgressEvent)
	{
		InstallationManager.getInstallationManager().fireExecutionProgress(
				executionProgressEvent);
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.installer.base.defaults.DefaultExecuter#getTotalWork()
	 */
	@Override
	public double getTotalWork()
	{
		if(totalWork == null) {
			String tw = getConfig().getProperty("totalWork.noOrganisations"); //$NON-NLS-1$
			totalWork = -1;
			if(tw != null) {
				try {
					totalWork = Integer.parseInt(tw);
					if(createOrganisation())
						totalWork += Integer.parseInt(getConfig().getProperty("totalWork.perOrganisation")); //$NON-NLS-1$
					if(createDemoOrganisation())
						totalWork += Integer.parseInt(getConfig().getProperty("totalWork.perOrganisation")); //$NON-NLS-1$
				} catch(Throwable e) {
					totalWork = -1;
				}
			}
		}
		return totalWork;
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.installer.base.DefaultExecuter#execute()
	 */
	@Override
	public void execute() throws InstallationException
	{
		fireProgress(new ExecutionProgressEvent(this, Messages.getString("Executer.progressStarting"), ExecutionProgressEvent.Type.starting)); //$NON-NLS-1$
		try {
//			dumpResults();
			unpackFiles();
			setupRuntimeLog4j();
			configureService();
			configureServer();
			setupOrganisations();
			setupTestSuite();
			chmodScripts();
			setupServerLog4jFirstStart();
			startServer();
			setupServerLog4jProductive();
			try {
				createShortcuts();
			} catch(InstallationException e) {
				InstallationException ee = new InstallationException("An error occured creating the program shortcuts. However, the installation itsself was successful and JFire can be used.", e);
				InstallationManager.getInstallationManager().getErrorHandler().handle(ee, false);
			}
			try {
				installService();
			} catch(InstallationException e) {
				InstallationException ee = new InstallationException("An error occured registering JFire as a service. However, the installation itsself was successful and JFire can be used.", e);
				InstallationManager.getInstallationManager().getErrorHandler().handle(ee, false);
			}
		} finally {
			cleanup();
		}
		dumpUserInformation();
		fireProgress(new ExecutionProgressEvent(this, Messages.getString("Executer.progressDone"), ExecutionProgressEvent.Type.done)); //$NON-NLS-1$
	}

	private void configureService() throws InstallationException
	{
		// change windows service stuff:
		if(OsVersion.IS_WINDOWS) {
			try {
				File serviceBat = new File(getBinDir(), "service.bat"); //$NON-NLS-1$
				String serviceBatContents = Util.readTextFile(serviceBat, Charset.defaultCharset().name());
				String serviceName = getInstallationEntity().getResult("18_installOptions.60_serviceName.result"); //$NON-NLS-1$
				if(serviceName == null || serviceName.length() == 0)
					serviceName = Messages.getString("Executer.defaultServiceName"); //$NON-NLS-1$
				String serviceDescription = getInstallationEntity().getResult("18_installOptions.70_serviceDescription.result"); //$NON-NLS-1$
				if(serviceDescription == null || serviceDescription.length() == 0)
					serviceDescription = serviceName;
				serviceBatContents = serviceBatContents
					.replace("SVCNAME=JBAS50SVC", "SVCNAME="+serviceName) //$NON-NLS-1$ //$NON-NLS-2$
					.replace("SVCDISP=JBoss Application Server 5.0", "SVCDISP="+serviceDescription) //$NON-NLS-1$ //$NON-NLS-2$
					.replace("SVCDESC=JBoss Application Server 5.0.0 GA/Platform: Windows x86", "SVCDESC="+serviceDescription); //$NON-NLS-1$ //$NON-NLS-2$
				Util.writeTextFile(serviceBat, serviceBatContents, Charset.defaultCharset().name());
			} catch(Exception e) {
				throw new InstallationException(Messages.getString("Executer.serviceBatConfigError"), e); //$NON-NLS-1$
			}
		}
	}

	private void dumpUserInformation() throws InstallationException
	{
		String serverBinDir = new File (getInstallationDir(), "bin").getAbsolutePath(); //$NON-NLS-1$
		String runScript = OsVersion.IS_WINDOWS ? "run.bat" : "./run.sh"; //$NON-NLS-1$ //$NON-NLS-2$
		fireProgress(
				new ExecutionProgressEvent(
						this,
						String.format(Messages.getString("Executer.howtoStartServer"), serverBinDir, runScript), //$NON-NLS-1$
						ExecutionProgressEvent.Type.progress
				)
		);

		if (createDemoOrganisation()) {
			fireProgress(
					new ExecutionProgressEvent(
							this,
							String.format(Messages.getString("Executer.howtoLoginDemoOrganisation")), //$NON-NLS-1$
							ExecutionProgressEvent.Type.progress
					)
			);
			String organisationID = "chezfrancois.jfire.org"; //$NON-NLS-1$
			String userID = "francois"; //$NON-NLS-1$
			String password = "test"; //$NON-NLS-1$
			fireProgress(
					new ExecutionProgressEvent(
							this,
							String.format(Messages.getString("Executer.howtoLoginDemoOrganisation_organisationID"), organisationID), //$NON-NLS-1$
							ExecutionProgressEvent.Type.progress
					)
			);
			fireProgress(
					new ExecutionProgressEvent(
							this,
							String.format(Messages.getString("Executer.howtoLoginDemoOrganisation_userID"), userID), //$NON-NLS-1$
							ExecutionProgressEvent.Type.progress
					)
			);
			fireProgress(
					new ExecutionProgressEvent(
							this,
							String.format(Messages.getString("Executer.howtoLoginDemoOrganisation_password"), password), //$NON-NLS-1$
							ExecutionProgressEvent.Type.progress
					)
			);
		} // if (createDemoOrganisation()) {

		if (createOrganisation()) {
			fireProgress(
					new ExecutionProgressEvent(
							this,
							String.format(Messages.getString("Executer.howtoLoginRealOrganisation")), //$NON-NLS-1$
							ExecutionProgressEvent.Type.progress
					)
			);
			String organisationID = getInstallationEntity().getResult("70_firstOrganisation.10_organisationID.result"); //$NON-NLS-1$
			String userID = getInstallationEntity().getResult("70_firstOrganisation.30_userName.result"); //$NON-NLS-1$
			String password = getInstallationEntity().getResult("70_firstOrganisation.40_userPassword.result"); //$NON-NLS-1$
			fireProgress(
					new ExecutionProgressEvent(
							this,
							String.format(Messages.getString("Executer.howtoLoginRealOrganisation_organisationID"), organisationID), //$NON-NLS-1$
							ExecutionProgressEvent.Type.progress
					)
			);
			fireProgress(
					new ExecutionProgressEvent(
							this,
							String.format(Messages.getString("Executer.howtoLoginRealOrganisation_userID"), userID), //$NON-NLS-1$
							ExecutionProgressEvent.Type.progress
					)
			);
			fireProgress(
					new ExecutionProgressEvent(
							this,
							String.format(Messages.getString("Executer.howtoLoginRealOrganisation_password"), password), //$NON-NLS-1$
							ExecutionProgressEvent.Type.progress
					)
			);
		} // if (createOrganisation()) {
	}

	private void setupTestSuite() throws InstallationException
	{
		File testSuiteDir = new File(getDeployBaseDir(), "JFireTestSuite.ear"); //$NON-NLS-1$
		if(!Constants.CHECK_TRUE.equals(getInstallationEntity().getResult("80_testSuite.10_installTestSuite.result"))) { //$NON-NLS-1$)
			// delete the ear
			fireProgress(Messages.getString("Executer.progressRemovingTestSuite")); //$NON-NLS-1$
//			Util.deleteDirectoryRecursively(testSuiteDir); // commented it out since it is included in the iteration below

			// Delete all JFireTestSuite*.ear directories (there might be multiple test-modules for the several parts of JFire).
			for (File ear : getDeployBaseDir().listFiles()) {
				String name = ear.getName();
				if (name.startsWith("JFireTestSuite") && name.endsWith(".ear")) //$NON-NLS-1$ //$NON-NLS-2$
					Util.deleteDirectoryRecursively(ear);
			}
		} else {
			try {
				// configure it
				File propertiesFile = new File(testSuiteDir, "jfireTestSuite.properties"); //$NON-NLS-1$
				FileInputStream in = new FileInputStream(propertiesFile);
				Properties props = new Properties();
				try {
					props.load(in);
				} finally {
					in.close();
				}

				props.setProperty("listener.Default.mail.alwaysSend.enabled",  //$NON-NLS-1$
						getInstallationEntity().getResult("80_testSuite.20_sendMailAll.result")); //$NON-NLS-1$
				props.setProperty("listener.Default.mail.onFailure.enabled",  //$NON-NLS-1$
						getInstallationEntity().getResult("80_testSuite.30_sendMailFailure.result")); //$NON-NLS-1$
				props.setProperty("listener.Default.mail.onSkip.enabled",  //$NON-NLS-1$
						getInstallationEntity().getResult("80_testSuite.40_sendMailSkip.result")); //$NON-NLS-1$
				props.setProperty("listener.Default.mail.smtp.host",  //$NON-NLS-1$
						getInstallationEntity().getResult("80_testSuite.80_mailHost.result")); //$NON-NLS-1$
				props.setProperty("listener.Default.mail.to",  //$NON-NLS-1$
						getInstallationEntity().getResult("80_testSuite.50_mailTo.result")); //$NON-NLS-1$
				props.setProperty("listener.Default.mail.from",  //$NON-NLS-1$
						getInstallationEntity().getResult("80_testSuite.60_mailFrom.result")); //$NON-NLS-1$
				props.setProperty("listener.Default.mail.subject",  //$NON-NLS-1$
						getInstallationEntity().getResult("80_testSuite.70_mailSubject.result")); //$NON-NLS-1$

				FileOutputStream out = new FileOutputStream(propertiesFile);
				try {
					props.store(out, "Automatically configured by JFire Installer"); //$NON-NLS-1$
				} finally {
					in.close();
				}

			} catch(IOException e) {
				throw new InstallationException(Messages.getString("Executer.testSuiteConfigError"), e); //$NON-NLS-1$
			}
		}
	}

	private void setupOrganisations() throws InstallationException
	{
		if(createOrganisation()) {
			try {
				// the data dir is needed to trigger creation of a custom organisation when the server is not yet running
				File jfireDataDir = new File(getDataDir(), "jfire"); //$NON-NLS-1$
				System.setProperty(JFireServerDataDirectory.PROPERTY_KEY_JFIRE_DATA_DIRECTORY, jfireDataDir.getAbsolutePath());

				// this is done in a ResultVerifier - just check again:
				if(!getInstallationEntity().getResult("70_firstOrganisation.40_userPassword.result").equals(getInstallationEntity().getResult("70_firstOrganisation.50_userPasswordAgain.result")))  //$NON-NLS-1$  //$NON-NLS-2$
					throw new InstallationException(Messages.getString("Executer.passwordsError")); //$NON-NLS-1$

				fireProgress(Messages.getString("Executer.progressOrganisationCreation")); //$NON-NLS-1$
				CreateOrganisationAfterRebootData coar = new CreateOrganisationAfterRebootData();
				coar.addOrganisation(
						getInstallationEntity().getResult("70_firstOrganisation.10_organisationID.result"), //$NON-NLS-1$
						getInstallationEntity().getResult("70_firstOrganisation.20_organisationName.result"), //$NON-NLS-1$
						getInstallationEntity().getResult("70_firstOrganisation.30_userName.result"), //$NON-NLS-1$
						getInstallationEntity().getResult("70_firstOrganisation.40_userPassword.result"), //$NON-NLS-1$
						true
				);
			} catch(IOException e) {
				throw new InstallationException(Messages.getString("Executer.organisationCreationError"), e); //$NON-NLS-1$
			}
		}
		if(!createDemoOrganisation()) {
			// remove deployed JFireChezFrancois project
			fireProgress(Messages.getString("Executer.progressRemovingChezFrancois")); //$NON-NLS-1$
			File chezFrancoisEARFile = new File(getDeployBaseDir(), "JFireChezFrancoisEAR.ear"); //$NON-NLS-1$
			if (!chezFrancoisEARFile.exists())
				Logger.out.println("The JFireChezFrancois module file does not exist: " + chezFrancoisEARFile.getAbsolutePath());
			else if (!chezFrancoisEARFile.delete())
				Logger.out.println("WARNING: Could not delete JFireChezFrancois module file: " + chezFrancoisEARFile.getAbsolutePath());
		}
	}

	private boolean createDemoOrganisation()
	{
		Logger.out.println("Create demo organisation: "+getInstallationEntity().getResult("65_organisations.10_createDemoOrganisation.result")); //$NON-NLS-1$ //$NON-NLS-2$
		return Constants.CHECK_TRUE.equals(getInstallationEntity().getResult("65_organisations.10_createDemoOrganisation.result")); //$NON-NLS-1$
	}

	private boolean createOrganisation()
	{
		Logger.out.println("Create custom organisation: "+getInstallationEntity().getResult("65_organisations.20_createOrganisation.result")); //$NON-NLS-1$ //$NON-NLS-2$
		return Constants.CHECK_TRUE.equals(getInstallationEntity().getResult("65_organisations.20_createOrganisation.result")); //$NON-NLS-1$
	}

	File getBinDir() throws InstallationException
	{
		File installDir = getInstallationDir();
		File binDir = new File(installDir, "bin"); //$NON-NLS-1$
		return binDir;
	}

	File getLibDir() throws InstallationException
	{
		File installDir = getInstallationDir();
		File binDir = new File(installDir, "lib"); //$NON-NLS-1$
		return binDir;
	}

	private void startServer() throws InstallationException
	{
		new ServerStarter(this).startServer();
	}

	private void cleanup()
	{
		for (File f : filesToCleanup)
			delete(f);
	}

	private void delete(File file)
	{
		if(file.isDirectory())
			for (File f : file.listFiles())
				delete(f);
		file.delete();
		//fireProgress(String.format(Messages.getString("Executer.removedFile"), file.getAbsolutePath())); //$NON-NLS-1$
	}

	public static void main(String[] args)
	{
		try {
			Executer e = new Executer();
			e.setupRuntimeLog4j();
		} catch(Throwable e) {
			e.printStackTrace();
		}
	}

	private void unpackFiles() throws InstallationException
	{
		File enclosingJar = EnvironmentHelper.getEnclosingJar();
		if (enclosingJar == null)
			throw new InstallationException(Messages.getString("Executer.errInstallEnv")); //$NON-NLS-1$
		File installDir = getInstallationDir();

		try {
			// unpack jboss:
//			unzip(enclosingJar, installDir, JAR_CONTENT_PATH + ZIP_ENTRY_SEPARATOR, this);

			File jarFile = enclosingJar;
			File targetDir = installDir;
			String entryPrefix = JAR_CONTENT_PATH + ZIP_ENTRY_SEPARATOR;

			fireProgress(String.format(Messages.getString("Executer.extractingFiles"), jarFile, targetDir)); //$NON-NLS-1$

			ZipFile zipFile = new ZipFile(jarFile);

			try {
				unzip(zipFile, targetDir, entryPrefix, this);
				if(OsVersion.IS_WINDOWS) {
					String nativeZipEntryPath = "jboss-native-win"+ZIP_ENTRY_SEPARATOR+"bin"+ZIP_ENTRY_SEPARATOR; //$NON-NLS-1$ //$NON-NLS-2$
					unzipFile(zipFile, nativeZipEntryPath+"service.bat", new File(getBinDir(), "service.bat"), this); //$NON-NLS-1$ //$NON-NLS-2$
					unzipFile(zipFile, nativeZipEntryPath+"jbosssvc.exe", new File(getBinDir(), "jbosssvc.exe"), this); //$NON-NLS-1$ //$NON-NLS-2$
					unzipIcon();
				}
			} finally {
				zipFile.close();
			}

			fireProgress(Messages.getString("Executer.extractingDone")); //$NON-NLS-1$

		} catch (IOException e) {
			throw new InstallationException(Messages.getString("Executer.errUnpack"), e); //$NON-NLS-1$
		}
	}

	private void unzipIcon() throws FileNotFoundException, InstallationException, IOException {
		InputStream in = getClass().getResourceAsStream(Messages.getString("Executer.icoFileSource")); //$NON-NLS-1$
		FileOutputStream out = new FileOutputStream(new File(getBinDir(), Messages.getString("Executer.icoFileTargetName"))); //$NON-NLS-1$
		try {
			Util.transferStreamData(in, out);
		} finally {
			in.close();
			out.close();
		}
	}

	private static void unzipFile(ZipFile zipFile, String entryName, File targetFile, Executer executer) throws InstallationException, IOException
	{
		ZipEntry entry = zipFile.getEntry(entryName);
		if(entry == null)
			throw new InstallationException("Zip entry not found: "+entryName);
		unzip(zipFile, entry, targetFile, executer);
	}

//	public static void unzip(File jarFile, File targetDir, String entryPrefix, Executer executer) throws ZipException, IOException, FileNotFoundException
//	{
//		if(executer != null)
//			executer.fireProgress(String.format(Messages.getString("Executer.extractingFiles"), jarFile, targetDir)); //$NON-NLS-1$
//
//		ZipFile zipFile = new ZipFile(jarFile);
//
//		try {
//			unzip(zipFile, targetDir, entryPrefix, executer);
//		} finally {
//			zipFile.close();
//		}
//
//		if(executer != null)
//			executer.fireProgress(Messages.getString("Executer.extractingDone")); //$NON-NLS-1$
//	}

	public static void unzip(ZipFile zipFile, File targetDir, String entryPrefix, Executer executer) throws IOException, FileNotFoundException
	{
		Enumeration<? extends ZipEntry> entries = zipFile.entries();
		while (entries.hasMoreElements()) {
			ZipEntry entry = entries.nextElement();
			if (entryPrefix == null || (entry.getName().startsWith(entryPrefix) && entry.getName().length() > entryPrefix.length())) {
				if (entry.isDirectory()) {
					// create the directory
					File dir;
					if(entryPrefix != null)
						dir = new File(targetDir, entry.getName().substring(entryPrefix.length()));
					else
						dir = new File(targetDir, entry.getName());
					dir.mkdirs();
					if (!dir.isDirectory())
						throw new IOException(Messages.getString("Executer.errMkDir") + dir.getAbsoluteFile()); //$NON-NLS-1$
				} else {
					File file;
					if(entryPrefix != null)
						file = new File(targetDir, entry.getName().substring(entryPrefix.length()));
					else
						file = new File(targetDir, entry.getName());

					unzip(zipFile, entry, file, executer);
				}
			}
		}
	}

	private static void unzip(ZipFile zipFile, ZipEntry entry, File file, Executer executer) throws IOException
	{
		if(executer != null)
			executer.fireProgress(Messages.getString("Executer.extractingFile") + entry.getName()); //$NON-NLS-1$

		InputStream in = zipFile.getInputStream(entry);

		File dir = new File(file.getParent());
		if (dir.exists())
			assert (dir.isDirectory());
		else
			dir.mkdirs();

		// copy data
		BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(file));
		try {
			Util.transferStreamData(in, out);
		} finally {
			in.close();
			out.close();
		}
	}

	File getInstallationDir() throws InstallationException
	{
		File installDir = new File(getInstallationEntity().getResult("17_installDir.10_installDir.result")); //$NON-NLS-1$
		installDir.mkdirs();
		if (!installDir.isDirectory())
			throw new InstallationException(String.format(
					Messages.getString("Executer.errInstallDir"), //$NON-NLS-1$
					installDir.getAbsolutePath()));
		return installDir;
	}

	private File getDeployBaseDir() throws InstallationException
	{
		File deployBaseDir = new File(getInstallationEntity().getResult("20_localServer.50_deployBaseDir.result")); //$NON-NLS-1$
		deployBaseDir.mkdirs();
		if (!deployBaseDir.isDirectory())
			throw new InstallationException(String.format(
					Messages.getString("Executer.deployBaseDirError"), //$NON-NLS-1$
					deployBaseDir.getAbsolutePath()));
		return deployBaseDir;
	}

	private File getConfDir() throws InstallationException
	{
		File conf = new File(getDeployBaseDir().getParentFile().getParentFile(), "conf"); //$NON-NLS-1$
		conf.mkdirs();
		if (!conf.isDirectory())
			throw new InstallationException(String.format(Messages.getString("Executer.configDirError"), //$NON-NLS-1$
					conf.getAbsolutePath()));
		return conf;
	}

	private File getDataDir() throws InstallationException {
		File conf = new File(getDeployBaseDir().getParentFile().getParentFile(), "data"); //$NON-NLS-1$
		conf.mkdirs();
		if (!conf.isDirectory())
			throw new InstallationException(String.format(Messages.getString("Executer.configDirError"), //$NON-NLS-1$
					conf.getAbsolutePath()));
		return conf;
	}


	private File getJFireConfigDir() throws InstallationException
	{
		File conf = new File(getDataDir(), "jfire/config"); //$NON-NLS-1$
		conf.mkdirs();
		if (!conf.isDirectory())
			throw new InstallationException(String.format(Messages.getString("Executer.configDirError"), //$NON-NLS-1$
					conf.getAbsolutePath()));
		return conf;
	}

	private void setupRuntimeLog4j()
	{
		Properties log4Props = new Properties();
		log4Props.setProperty("log4j.rootLogger", "DEBUG, MYAPPENDER"); //$NON-NLS-1$ //$NON-NLS-2$
		log4Props.setProperty("log4j.appender.MYAPPENDER", LogAppender.class //$NON-NLS-1$
				.getName());
		PropertyConfigurator.configure(log4Props);
	}

	private void setupServerLog4jFirstStart() throws InstallationException
	{
		setupServerLog4j(getClass().getResourceAsStream("log4j-firststart.xml")); //$NON-NLS-1$
	}

	private void setupServerLog4jProductive() throws InstallationException
	{
		setupServerLog4j(getClass().getResourceAsStream("log4j-productive.xml")); //$NON-NLS-1$
	}

	private void setupServerLog4j(InputStream source) throws InstallationException
	{
		File conf = getConfDir();
		File targetFile = new File(conf, "jboss-log4j.xml"); //$NON-NLS-1$
		if(!targetFile.exists())
			targetFile = new File(conf, "log4j.xml"); //$NON-NLS-1$
		if(!targetFile.exists())
			throw new InstallationException(Messages.getString("Executer.log4jError")); //$NON-NLS-1$
		try {
			backup(targetFile);
			FileOutputStream target = new FileOutputStream(targetFile);
			Util.transferStreamData(source, target);
			target.close();
		} catch(IOException e) {
			throw new InstallationException(Messages.getString("Executer.log4jUpdateError")); //$NON-NLS-1$
		}
	}

	private Config config;
	
	public Config getJFireConfig() {
		return config;
	}
	
	private void configureServer() throws InstallationException
	{
		try {
			// save config
			File configDir = getJFireConfigDir();
			if (!configDir.exists()) {
				configDir.mkdir();
			}
			config = new Config(new File(configDir, "Config.xml")); //$NON-NLS-1$
			JFireServerConfigModule jfireServerConfigModule = config.createConfigModule(JFireServerConfigModule.class);
			ServicePortsConfigModule servicePortsConfigModule = config.createConfigModule(ServicePortsConfigModule.class);

			fillServerConfigModule(jfireServerConfigModule, servicePortsConfigModule);
			fillServicePortsConfigModule(servicePortsConfigModule);

			config.save();

			// launch server configurator
			String java = Programs.findJava();
			File tmpDir = IOUtil.getTempDir();
			File unpackDir = new File(tmpDir, "JFireIntegrationJBossEAR");
			unpackDir.mkdir();
			unpackDir.deleteOnExit();
			filesToCleanup.add(unpackDir);
			IOUtil.unzipArchive(new File(this.getDeployBaseDir(), "org.nightlabs.jfire.jboss.ear.ear"), unpackDir);
			// File jfireIntegrationJBossJar = new File(unpackDir, "org.nightlabs.jfire.jboss-1.2.0-SNAPSHOT.jar"); //$NON-NLS-1$
			File jfireIntegrationJBossJar = null;
			File[] unpacked = unpackDir.listFiles();
			if (unpacked == null)
				throw new IllegalStateException("unpackDir.listFiles() returned null! unpackDir=" + unpackDir.getAbsolutePath());

			final String jfireIntegrationJBossJarPrefix = "org.nightlabs.jfire.jboss-";
			final String jfireIntegrationJBossJarSuffix = ".jar";
			for (File file : unpacked) {
				if (file.getName().startsWith(jfireIntegrationJBossJarPrefix) && file.getName().endsWith(jfireIntegrationJBossJarSuffix)) {
					jfireIntegrationJBossJar = file;
					break;
				}
			}
			if (jfireIntegrationJBossJar == null)
				throw new IllegalStateException("unpackDir does not contain a file whose name starts with \"" + jfireIntegrationJBossJarPrefix + "\" and ends with with \"" + jfireIntegrationJBossJarSuffix + "\"! unpackDir=" + unpackDir.getAbsolutePath());

			String classpath = jfireIntegrationJBossJar.getAbsolutePath();
			String[] command = {
					java,
					"-classpath", //$NON-NLS-1$
					classpath,
					"org.nightlabs.jfire.jboss.serverconfigurator.Launcher" //$NON-NLS-1$
			};
			ProcessBuilder pb = new ProcessBuilder(command);
			pb.directory(this.getBinDir());
			Process process = pb.start();
			ObservedProcess op = new ObservedProcess(process, threadPool);
			int result = op.waitForProcess(new LineConsumer(false) {
				@Override
				protected void consumeLine(String line) {
					Logger.out.println(line);
					fireProgress(line);
				}
			}, new LineConsumer(false) {
				@Override
				protected void consumeLine(String line) {
					Logger.err.println(line);
					fireProgress(line);
				}
			});
			if (result != 0)
				throw new InstallationException("The ServerConfigurator ended with error code " + result + "!"); //$NON-NLS-1$ //$NON-NLS-2$
		} catch (InstallationException e) {
			throw e;
		} catch (Exception e) {
			throw new InstallationException(Messages.getString("Executer.errServerConfig"), e); //$NON-NLS-1$
		}
	}

	private void chmodScripts() throws InstallationException
	{
		if(OsVersion.IS_UNIX) {
			File binDir = getBinDir();
			// chmod +x *.sh
			String[] chmodFiles = binDir.list(new FilenameFilter() {
				public boolean accept(File dir, String name) {
					return name.endsWith(".sh"); //$NON-NLS-1$
				}
			});
			for (String file : chmodFiles) {
				try {
					Programs.chmod(new File(binDir, file), "+x", false); //$NON-NLS-1$
				} catch (ProgramException e) {
					// should never happen
					e.printStackTrace();
				}
			}
		}
	}

	protected void fillServicePortsConfigModule(ServicePortsConfigModule servicePortsConfigModule)
	throws InstallationException
	{
		String defaultServiceHost = getInstallationEntity().getResult("20_localServer.55_bindAdress.result");
		if(defaultServiceHost != null)
			servicePortsConfigModule.setDefaultServiceHost(defaultServiceHost);

		servicePortsConfigModule.setServiceNamingBindingPort(getServicePort("25_serverServices.15_naming-port.result"));
		servicePortsConfigModule.setServiceNamingBindingHost(getServiceHost("25_serverServices.17_naming-host.result"));

		servicePortsConfigModule.setServiceNamingRMIPort(getServicePort("25_serverServices.20_rmi-port.result"));
		servicePortsConfigModule.setServiceNamingRMIHost(getServiceHost("25_serverServices.21_rmi-host.result"));

		servicePortsConfigModule.setServiceWebServicePort(getServicePort("25_serverServices.30_webservice-port.result"));
		servicePortsConfigModule.setServiceWebServiceHost(getServiceHost("25_serverServices.35_webservice-host.result"));

		servicePortsConfigModule.setServiceTomcatPort(getServicePort("25_serverServices.40_tomcat-port.result"));
		servicePortsConfigModule.setServiceTomcatHost(getServiceHost("25_serverServices.45_tomcat-host.result"));

		servicePortsConfigModule.setServiceRemotingConnectorPort(getServicePort("25_serverServices.50_jbossmessaging-port.result"));
		servicePortsConfigModule.setServiceRemotingConnectorHost(getServiceHost("25_serverServices.55_jbossmessaging-host.result"));

		servicePortsConfigModule.setServiceJrmpPort(getServicePort("25_serverServices.60_jrmp-port.result"));
		servicePortsConfigModule.setServiceJrmpHost(getServiceHost("25_serverServices.65_jrmp-host.result"));

		servicePortsConfigModule.setServicePooledPort(getServicePort("25_serverServices.70_pooled-port.result"));
		servicePortsConfigModule.setServicePooledHost(getServiceHost("25_serverServices.75_pooled-host.result"));
	}

	private String getServiceHost(String resultKey)
	{
		String host = getInstallationEntity().getResult(resultKey);
		if(host != null && host.trim().isEmpty())
			return null;
		return host;
	}

	private int getServicePort(String resultKey)
	{
		String portString = getInstallationEntity().getResult(resultKey);
		if(portString == null || portString.trim().isEmpty())
			// 0 is the default value in the config module and will be replaced when init() is called
			return 0;
		int port = Integer.valueOf(portString);
		if(port < 1)
			// 0 is the default value in the config module and will be replaced when init() is called
			return 0;
		return port;
	}

	protected void fillServerConfigModule(
			JFireServerConfigModule jfireServerConfigModule, ServicePortsConfigModule servicePortsConfigModule)
			throws InstallationException
	{
		// local server
		ServerCf localServer = new ServerCf();
		localServer.setServerID(getInstallationEntity().getResult("20_localServer.10_serverID.result")); //$NON-NLS-1$
		localServer.setServerName(getInstallationEntity().getResult("20_localServer.20_serverName.result")); //$NON-NLS-1$
		localServer.setJ2eeServerType(getInstallationEntity().getResult("20_localServer.30_serverType.result")); //$NON-NLS-1$
		localServer.setInitialContextURL(getInstallationEntity().getResult("20_localServer.40_initialContextURL.result")); //$NON-NLS-1$
		jfireServerConfigModule.setLocalServer(localServer);

		// j2ee
		J2eeCf j2ee = new J2eeCf();
		j2ee.setJ2eeDeployBaseDirectory(getInstallationEntity().getResult("20_localServer.50_deployBaseDir.result")); //$NON-NLS-1$
		j2ee.setServerConfigurator(getInstallationEntity().getResult("22_serverConfigurator.60_serverConfigurator.result")); //$NON-NLS-1$
		// --- optional settings:
		// removed rmi host because now all jboss services can be configured
//		Properties serverConfiguratorSettings = new Properties();
//		String rmiHost = getInstallationEntity().getResult("20_localServer.70_rmiHost.result"); //$NON-NLS-1$
//		if(rmiHost != null && !"".equals(rmiHost)) //$NON-NLS-1$
//			serverConfiguratorSettings.setProperty("java.rmi.server.hostname", rmiHost); //$NON-NLS-1$
		// not working :-( - see #374
//		String bindAddress = getInstallationEntity().getResult("20_localServer.80_bindAddress.result"); //$NON-NLS-1$
//		if(bindAddress != null && !"".equals(bindAddress)) //$NON-NLS-1$
//			serverConfiguratorSettings.setProperty("j2ee.bind.address", bindAddress); //$NON-NLS-1$
//		if(!serverConfiguratorSettings.isEmpty())
//			j2ee.setServerConfiguratorSettings(serverConfiguratorSettings);
		// ---
		jfireServerConfigModule.setJ2ee(j2ee);

		// database
		DatabaseCf database = new DatabaseCf();
		database.setDatabaseDriverName_noTx(getInstallationEntity().getResult("40_database.10_driverName_noTx.result")); //$NON-NLS-1$
		database.setDatabaseDriverName_localTx(getInstallationEntity().getResult("40_database.11_driverName_localTx.result")); //$NON-NLS-1$
		database.setDatabaseDriverName_xa(getInstallationEntity().getResult("40_database.12_driverName_xa.result")); //$NON-NLS-1$
		database.setDatabaseURL(getInstallationEntity().getResult("40_database.20_connectionURL.result")); //$NON-NLS-1$
		database.setDatabasePrefix(getInstallationEntity().getResult("40_database.30_namePrefix.result")); //$NON-NLS-1$
		database.setDatabaseSuffix(getInstallationEntity().getResult("40_database.40_nameSuffix.result")); //$NON-NLS-1$
		database.setDatabaseUserName(getInstallationEntity().getResult("40_database.50_userName.result")); //$NON-NLS-1$
		database.setDatabasePassword(getInstallationEntity().getResult("40_database.60_password.result")); //$NON-NLS-1$
		database.setDatabaseAdapter(getInstallationEntity().getResult("40_database.70_adapter.result")); //$NON-NLS-1$
		database.setDatasourceMetadataTypeMapping(getInstallationEntity().getResult("40_database.80_typeMapping.result")); //$NON-NLS-1$
		database.setDatasourceConfigFile(getInstallationEntity().getResult("40_database.85_datasourceConfigurationFile.result")); //$NON-NLS-1$
		database.setDatasourceTemplateDSXMLFile(getInstallationEntity().getResult("40_database.90_datasourceConfigurationTemplateFile.result")); //$NON-NLS-1$

		jfireServerConfigModule.setDatabase(database);

		// jdo
		JDOCf jdo = new JDOCf();
		jdo.setJdoDeploymentDirectory(getInstallationEntity().getResult("50_jdo.10_deploymentDirectory.result")); //$NON-NLS-1$
		jdo.setJdoDeploymentDescriptorFile(getInstallationEntity().getResult("50_jdo.20_deploymentDescriptorFile.result")); //$NON-NLS-1$
		jdo.setJdoDeploymentDescriptorTemplateFile(getInstallationEntity().getResult("50_jdo.40_deploymentDescriptorTemplateFile.result")); //$NON-NLS-1$
		jdo.setJdoPersistenceConfigurationFile(getInstallationEntity().getResult("50_jdo.25_persistenceConfigurationFile.result")); //$NON-NLS-1$
		jdo.setJdoPersistenceConfigurationTemplateFile(getInstallationEntity().getResult("50_jdo.45_persistenceConfigurationTemplateFile.result")); //$NON-NLS-1$
		jfireServerConfigModule.setJdo(jdo);

		// root organisation
		ServerCf rootOrgServer = new ServerCf(getInstallationEntity().getResult("60_rootOrganisation.30_serverID.result")); //$NON-NLS-1$
		RootOrganisationCf rootOrg = new RootOrganisationCf(
				getInstallationEntity().getResult("60_rootOrganisation.10_organisationID.result"), //$NON-NLS-1$
				getInstallationEntity().getResult("60_rootOrganisation.20_organisationName.result"), //$NON-NLS-1$
				rootOrgServer);
		rootOrgServer.setServerName(getInstallationEntity().getResult("60_rootOrganisation.40_serverName.result")); //$NON-NLS-1$
		rootOrgServer.setJ2eeServerType(getInstallationEntity().getResult("60_rootOrganisation.50_serverType.result")); //$NON-NLS-1$
		rootOrgServer.setInitialContextURL(getInstallationEntity().getResult("60_rootOrganisation.60_initialContextURL.result")); //$NON-NLS-1$
		jfireServerConfigModule.setRootOrganisation(rootOrg);
	}

	private static File getNonExistingFile(String pattern)
	{
		if(pattern == null)
			throw new NullPointerException("pattern is null"); //$NON-NLS-1$
		synchronized(pattern) {
			int idx = 1;
			File f;
			do {
				f = new File(String.format(pattern, idx));
				idx++;
			} while(f.exists());
			return f;
		}
	}

	protected File backup(File f) throws IOException
	{
		if(!f.exists() || !f.canRead())
			throw new FileNotFoundException("Invalid file to backup: "+f); //$NON-NLS-1$
		File backupFile = new File(f.getAbsolutePath()+".bak"); //$NON-NLS-1$
		if(backupFile.exists())
			backupFile = getNonExistingFile(f.getAbsolutePath()+".%d.bak"); //$NON-NLS-1$
		Util.copyFile(f, backupFile);
		fireProgress(String.format(Messages.getString("Executer.progressBackup"), f.getAbsolutePath(), backupFile.getName())); //$NON-NLS-1$
		return backupFile;
	}

	private Shortcut createShortcut(int type) throws Exception
	{
		String shortcutName = Messages.getString("Executer.shortcutName"); //$NON-NLS-1$
		String description = Messages.getString("Executer.shortcutDescription"); //$NON-NLS-1$
		String defaultProgramGroupName = Messages.getString("Executer.defaultProgramGroupName"); //$NON-NLS-1$
		String workingDir = getBinDir().getAbsolutePath();
		String executable = OsVersion.IS_WINDOWS ? "run.bat" : "run.sh"; //$NON-NLS-1$ //$NON-NLS-2$

		Shortcut shortcut = (Shortcut)TargetFactory.getInstance().makeObject(Shortcut.class.getName());
		shortcut.initialize(type, shortcutName);
		shortcut.setLinkType(type);
		shortcut.setLinkName(shortcutName);
		shortcut.setType(Messages.getString("Executer.shortcutType")); //$NON-NLS-1$
		shortcut.setCategories(Messages.getString("Executer.shortcutCategories")); //$NON-NLS-1$
		if(OsVersion.IS_WINDOWS)
			shortcut.setIconLocation(new File(getBinDir(), Messages.getString("Executer.icoFileTargetName")).getAbsolutePath(), 0); //$NON-NLS-1$
		else {
			// TODO: get linux icon!
			;
		}
		if("all".equals(getInstallationEntity().getResult("18_installOptions.60_installForUsers.result"))) { //$NON-NLS-1$ //$NON-NLS-2$
			shortcut.setUserType(Shortcut.ALL_USERS);
			shortcut.setCreateForAll(true);
		} else {
			shortcut.setUserType(Shortcut.CURRENT_USER);
			shortcut.setCreateForAll(false);
		}
		if(type == Shortcut.APPLICATIONS) {
			String programGroup = getInstallationEntity().getResult("18_installOptions.30_programGroup.result"); //$NON-NLS-1$
			if(programGroup == null || programGroup.trim().length() == 0)
				programGroup = defaultProgramGroupName;
			shortcut.setProgramGroup(programGroup);
		} else {
			shortcut.setProgramGroup(""); //$NON-NLS-1$
		}
		shortcut.setTerminal(Messages.getString("Executer.shortcutTerminal")); //$NON-NLS-1$
		shortcut.setDescription(description);
		shortcut.setWorkingDirectory(workingDir);
		shortcut.setTargetPath(new File(workingDir, executable).getAbsolutePath());

		return shortcut;
	}

	private void createShortcuts() throws InstallationException
	{
		if(Constants.CHECK_TRUE.equals(getInstallationEntity().getResult("18_installOptions.10_createDesktopEntry.result"))) { //$NON-NLS-1$
			try {
				Shortcut shortcut = createShortcut(Shortcut.DESKTOP);
				shortcut.save();
			} catch(Exception e) {
				throw new InstallationException(Messages.getString("Executer.desktopShortcutError"), e); //$NON-NLS-1$
			}
		}

		if(Constants.CHECK_TRUE.equals(getInstallationEntity().getResult("18_installOptions.20_createStartMenuEntry.result"))) { //$NON-NLS-1$
			try {
				Shortcut shortcut = createShortcut(Shortcut.APPLICATIONS);
				shortcut.save();
			} catch(Exception e) {
				throw new InstallationException(Messages.getString("Executer.startMenuShortcutError"), e); //$NON-NLS-1$
			}
		}
	}

	private void installService() throws InstallationException
	{
		if(!OsVersion.IS_WINDOWS)
			return;

		if(Constants.CHECK_TRUE.equals(getInstallationEntity().getResult("18_installOptions.50_installAsService.result"))) { //$NON-NLS-1$
			try {
				File serviceBat = new File(getBinDir(), "service.bat"); //$NON-NLS-1$

				ProcessBuilder pb = new ProcessBuilder(serviceBat.getAbsolutePath(), "install"); //$NON-NLS-1$
				pb.directory(getBinDir());
				Process process = pb.start();
				ObservedProcess op = new ObservedProcess(process, threadPool);
				int result = op.waitForProcess(new LineConsumer(false) {
					@Override
					protected void consumeLine(String line) {
						Logger.out.println(line);
						fireProgress(line);
					}
				}, new LineConsumer(false) {
					@Override
					protected void consumeLine(String line) {
						Logger.err.println(line);
						fireProgress(line);
					}
				});
				if (result != 0)
					throw new InstallationException(String.format(Messages.getString("Executer.serviceInstallerError"), result)); //$NON-NLS-1$
			} catch(InstallationException e) {
				throw e;
			} catch(Exception e) {
				throw new InstallationException(Messages.getString("Executer.serviceInstallationError"), e); //$NON-NLS-1$
			}
		}
	}
}
