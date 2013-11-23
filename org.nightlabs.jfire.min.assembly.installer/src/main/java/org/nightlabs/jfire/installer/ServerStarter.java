package org.nightlabs.jfire.installer;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.nightlabs.installer.Logger;
import org.nightlabs.installer.base.InstallationException;
import org.nightlabs.installer.util.Programs;
import org.nightlabs.jfire.jboss.serverconfigurator.config.ServicePortsConfigModule;

/**
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public class ServerStarter
{
	Executer executer;
	private volatile Process serverProcess = null;

	private static Pattern upAndRunning = Pattern.compile(".*JFireServer is up and running.*"); //$NON-NLS-1$

	private static Pattern[] infoIncludes = {
		Pattern.compile("Core system initialized"), //$NON-NLS-1$
		Pattern.compile("Creating database.*"), //$NON-NLS-1$
		Pattern.compile("Creating deploymentDescriptor.*"), //$NON-NLS-1$
		Pattern.compile("Empty organisation.*has been created.*"), //$NON-NLS-1$
		Pattern.compile("Bound ConnectionManager.*"), //$NON-NLS-1$
		Pattern.compile("Creating table .*"), //$NON-NLS-1$
		Pattern.compile("Invoking DatastoreInit.*"), //$NON-NLS-1$
		Pattern.compile("Init J2EE application.*"), //$NON-NLS-1$
		Pattern.compile("Deploying .*"), //$NON-NLS-1$
		Pattern.compile("JBoss .* Started in .*"), //$NON-NLS-1$
		upAndRunning,
	};
	private static Pattern[] errorExcludes = {
		Pattern.compile("Could not find class .* that .* references\\. It may not be in your classpath and you may not be getting field and constructor weaving for this class\\.")
	};
	private static Pattern[] warningExcludes = {
	};

	private static class LogLine
	{
		Date time;
		String level;
		String id;
		String message;

		private static boolean timeOnly = true;
		private static String timePattern = "((\\d{2}):(\\d{2}):(\\d{2}),(\\d+))"; //$NON-NLS-1$
		private static String dateTimePattern = "((\\d{4})-(\\d{2})-(\\d{2}))\\s"+timePattern; //$NON-NLS-1$
		private static String levelPattern = "([A-Z]+)"; //$NON-NLS-1$
		private static String idPattern = "\\[([^\\]]+)\\]"; //$NON-NLS-1$
		private static String messagePattern = "(.*)"; //$NON-NLS-1$
		private static Pattern p = Pattern.compile("^"+(timeOnly ? timePattern : dateTimePattern)+"\\s+"+levelPattern+"\\s+"+idPattern+"\\s+"+messagePattern+"$", Pattern.DOTALL); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$

		public static LogLine create(String line)
		{
			Matcher m = p.matcher(line);
			if(m.matches()) {
				LogLine ml = new LogLine();
//				Calendar calendar = Calendar.getInstance();
//				System.out.println(calendar.getTime());
				if(timeOnly) {
//					calendar.set(Calendar.HOUR, -calendar.get(Calendar.HOUR));
//					calendar.set(Calendar.MINUTE, -calendar.get(Calendar.MINUTE));
//					calendar.set(Calendar.SECOND,	-calendar.get(Calendar.SECOND));
//					calendar.set(Calendar.MILLISECOND, -calendar.get(Calendar.MILLISECOND));
//					calendar.set(Calendar.HOUR, Integer.parseInt(m.group(2)));
//					calendar.set(Calendar.MINUTE, Integer.parseInt(m.group(3)));
//					calendar.set(Calendar.SECOND,	Integer.parseInt(m.group(4)));
//					calendar.set(Calendar.MILLISECOND, Integer.parseInt(m.group(5)));
					ml.level = m.group(6);
					ml.id = m.group(7);
					ml.message = m.group(8);
				} else {
//					calendar.set(Calendar.YEAR, Integer.parseInt(m.group(2)));
//					calendar.set(Calendar.MONTH, Integer.parseInt(m.group(3))-1);
//					calendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(m.group(4)));
//					calendar.set(Calendar.HOUR, Integer.parseInt(m.group(6)));
//					calendar.set(Calendar.MINUTE, Integer.parseInt(m.group(7)));
//					calendar.set(Calendar.SECOND,	Integer.parseInt(m.group(8)));
//					calendar.set(Calendar.MILLISECOND, Integer.parseInt(m.group(9)));
					ml.level = m.group(10);
					ml.id = m.group(11);
					ml.message = m.group(12);
				}
//				ml.time = calendar.getTime();
				ml.time = new Date();
				return ml;
			}
			return null;
		}

		public void appendLine(String line)
		{
			//System.out.println("APPENDING: "+line);
			message += "\n"+line; //$NON-NLS-1$
		}
	}

	public static void main(String[] args)
	{
		LogLine l = LogLine.create("15:17:55,000 DEBUG [JPOX.Transaction] Connection com.mysql.jdbc.Connection@1a28023 closed for pessimistic transaction\n"); //$NON-NLS-1$
		if(l != null) {
			System.out.println("time: "+l.time); //$NON-NLS-1$
			System.out.println("level: "+l.level); //$NON-NLS-1$
			System.out.println("id: "+l.id); //$NON-NLS-1$
			System.out.println("message: "+l.message); //$NON-NLS-1$
		}
	}

	public ServerStarter(Executer executer)
	{
		this.executer = executer;
	}

	/**
	 * The timeout of the server to run the test suite (if enabled by user or defaults-file)
	 * after the server has written the {@link #upAndRunning} to the output.
	 * <p>
	 * See https://www.jfire.org/modules/bugs/view.php?id=1076
	 * </p>
	 */
	private static long TIMEOUT_RUN_TESTSUITE = 1000L * 3600L; // give it 1 hour to run the tests
	/**
	 * The maximum time the server is allowed to run in total - including the creation of the
	 * organisation and all initialisation work PLUS the {@link #TIMEOUT_RUN_TESTSUITE}. The
	 * server is shut down after this time in all cases (no matter what has or has not been
	 * written to the output).
	 * <p>
	 * See https://www.jfire.org/modules/bugs/view.php?id=1076
	 * </p>
	 */
	private static long TIMEOUT_RUN_SERVER_HARD_LIMIT = (1000L * 60L * 30L) + TIMEOUT_RUN_TESTSUITE; // give the server a total time of 30 minutes for the initial setup and the time for the test suite.

	public void startServer() throws InstallationException
	{
		// add shutdown hook to kill server if still running when installer is terminated
		Runtime.getRuntime().addShutdownHook(new Thread(new KillServerAfterTimeout(0)));

		// No matter what happens, we shutdown the server after 1.5 hours. See https://www.jfire.org/modules/bugs/view.php?id=1076
		Executors.newSingleThreadExecutor().execute(new KillServerAfterTimeout(TIMEOUT_RUN_SERVER_HARD_LIMIT));

		executer.fireProgress(Messages.getString("ServerStarter.progressStartingServer")); //$NON-NLS-1$
		ProcessBuilder pb = getJBossProcessBuilder();
		try {
			pb.redirectErrorStream(true);
			executer.fireProgress("Running "+pb.command()); //$NON-NLS-1$
			serverProcess = pb.start();
			BufferedReader reader = new BufferedReader(new InputStreamReader(serverProcess.getInputStream()));
			LogLine lastLogLine = null;
			boolean stopping = false;
			while(true) {
				String line;
				try {
					line = reader.readLine();
				} catch(IOException e) {
					if(stopping)
						break;
					else
						throw e;
				}
				if(line == null) {
					if(!stopping)
						throw new InstallationException(Messages.getString("ServerStarter.serverDiedError")); //$NON-NLS-1$
					else
						break;
				}
				Logger.out.println(line);
				LogLine logLine = LogLine.create(line);
				if(logLine == null) {
					if(lastLogLine != null)
						lastLogLine.appendLine(line);
				} else {
					if(!stopping) {
						if(lastLogLine != null)
							fireProgress(lastLogLine);
						lastLogLine = logLine;
					}
					//System.out.println("log: \""+lastLogLine.message+"\"");
					if(!stopping && upAndRunning.matcher(lastLogLine.message).find()) {
						stopping = true;
						if (isTestSuiteRunEnabled())
							Executors.newSingleThreadExecutor().execute(new KillServerAfterTimeout(TIMEOUT_RUN_TESTSUITE));
						else
							Executors.newSingleThreadExecutor().execute(new KillServerAfterTimeout(60000)); // shutdown after 1 minute
//							shutdownServer(); // shutdown after a short delay.
					}
				}
			}
//			if(lastLogLine != null)
//			fireProgress(lastLogLine);
		} catch (IOException e) {
			throw new InstallationException(Messages.getString("ServerStarter.serverStartError"), e); //$NON-NLS-1$
		} finally {
			serverProcess = null;
		}
	}

	private void fireProgress(LogLine logLine)
	{
		if(logLine.level.equals("INFO")) { //$NON-NLS-1$
			for (Pattern p : infoIncludes) {
				Matcher m = p.matcher(logLine.message);
				if(m.matches()) {
					executer.fireProgress(logLine.message);
					return;
				}
			}
		} else if(logLine.level.equals("ERROR")) { //$NON-NLS-1$
			for (Pattern p : errorExcludes) {
				if(p.matcher(logLine.message).matches())
					return;
			}
			executer.fireProgress(Messages.getString("ServerStarter.error")+logLine.message); //$NON-NLS-1$
		} else if(logLine.level.equals("WARNING")) { //$NON-NLS-1$
			for (Pattern p : warningExcludes) {
				if(p.matcher(logLine.message).matches())
					return;
			}
			executer.fireProgress(Messages.getString("ServerStarter.warning")+logLine.message); //$NON-NLS-1$
		}
	}

	private boolean isTestSuiteRunEnabled()
	{
		return (
				Boolean.parseBoolean(executer.getInstallationEntity().getResult("80_testSuite.10_installTestSuite.result")) &&
				Boolean.parseBoolean(executer.getInstallationEntity().getResult("80_testSuite.15_runTestSuite.result"))
		);
	}

	private class KillServerAfterTimeout implements Runnable
	{
		private long timeoutMillis;

		public KillServerAfterTimeout(long timeoutMillis)
		{
			this.timeoutMillis = timeoutMillis;
		}

		/* (non-Javadoc)
		 * @see java.lang.Runnable#run()
		 */
		public void run()
		{
			long endTime = System.currentTimeMillis() + timeoutMillis;
			int lastUntilKill = -1;
			while(serverProcess != null && endTime > System.currentTimeMillis()) {
				int untilKill = Math.round(((endTime - System.currentTimeMillis()) / 1000));
				if(untilKill % 5 == 0 && untilKill != lastUntilKill) {
					executer.fireProgress(String.format(Messages.getString("ServerStarter.serverAutoKill"), untilKill)); //$NON-NLS-1$
					lastUntilKill = untilKill;
				}
				try {
					Thread.sleep(100);
				} catch (InterruptedException ignore) {}
			}
			killServer();
		}
	}

	public void killServer()
	{
		Process serverProcess = this.serverProcess;
		if(serverProcess != null) {
			executer.fireProgress(Messages.getString("ServerStarter.killingServer")); //$NON-NLS-1$
			serverProcess.destroy();
			serverProcess = null;
		}
	}

	private ProcessBuilder getJBossProcessBuilder() throws InstallationException
	{
		/*
=========================================================================

  JBoss Bootstrap Environment

  JBOSS_HOME: /home/marc/bin/jfire-server

  JAVA: /home/marc/bin/jdk1.5.0_11/bin/java

  JAVA_OPTS: -Dprogram.name=run.sh -server -Xms128m -Xmx512m -Dsun.rmi.dgc.client.gcInterval=3600000 -Dsun.rmi.dgc.server.gcInterval=3600000  -XX:PermSize=64m -XX:MaxPermSize=128m -Djava.net.preferIPv4Stack=true

  CLASSPATH: /home/marc/bin/jfire-server/bin/run.jar:/home/marc/bin/jdk1.5.0_11/lib/tools.jar

JBOSS_ENDORSED_DIRS="$JBOSS_HOME/lib/endorsed"
-Djava.endorsed.dirs="$JBOSS_ENDORSED_DIRS"

org.jboss.Main

=========================================================================
		 */

		// TODO: handle JBOSS_NATIVE_DIR

		String bindAddress = null;
		String rmiHost = null;
		ServicePortsConfigModule serviceConfig = executer.getJFireConfig().getConfigModule(ServicePortsConfigModule.class);
		bindAddress  = serviceConfig.getDefaultServiceHost();
		rmiHost = serviceConfig.getServiceNamingRMIHost();
		if (rmiHost == null) {
			rmiHost = bindAddress;
		}
		
		String java = Programs.findJava();
		File endorsed = new File(executer.getLibDir(), "endorsed"); //$NON-NLS-1$
		String clientOrServerMode = "-client"; //$NON-NLS-1$
		File runJar = new File(executer.getBinDir(), "run.jar"); //$NON-NLS-1$
		String classpath = runJar.getAbsolutePath();
		String[] command = {
				java,
//					"-Xrunjdwp:server=y,transport=dt_socket,address=8000,suspend=y",
				"-Dprogram.name=JFireInstaller",  //$NON-NLS-1$
				// Shutdown server by itself:
				"-Dorg.nightlabs.jfire.servermanager.JFireServerManagerFactory.shutdownAfterStartup=true",  //$NON-NLS-1$
				"-Dorg.nightlabs.jfire.testsuite.JFireTestManager.runOnStartup=" + isTestSuiteRunEnabled(),  //$NON-NLS-1$
				// AOP FIXME
				"-javaagent:../server/default/deploy/jboss-aop-jdk50.deployer/pluggable-instrumentor.jar",
				// bind address
				"-Djboss.bind.address="+bindAddress,
				// rmiHost
				"-Djava.rmi.server.hostname="+rmiHost,
				clientOrServerMode,
				"-Xms128m",  //$NON-NLS-1$
				"-Xmx1000m",  //$NON-NLS-1$
//					"-XX:PermSize=64m",  //$NON-NLS-1$
				"-XX:MaxPermSize=128m",  //$NON-NLS-1$
				"-Djava.net.preferIPv4Stack=true", //$NON-NLS-1$
				"-Djava.endorsed.dirs="+endorsed.getAbsolutePath(), //$NON-NLS-1$
				"-classpath", //$NON-NLS-1$
				classpath,
				"org.jboss.Main" //$NON-NLS-1$
		};
		ProcessBuilder pb = new ProcessBuilder(command);
		pb.directory(executer.getBinDir());
		pb.environment().put("JBOSS_HOME", executer.getInstallationDir().getAbsolutePath()); //$NON-NLS-1$

		return pb;
	}
}
