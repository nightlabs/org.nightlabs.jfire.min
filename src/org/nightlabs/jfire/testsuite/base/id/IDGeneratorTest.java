/**
 *
 */
package org.nightlabs.jfire.testsuite.base.id;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import javax.naming.InitialContext;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.nightlabs.j2ee.LoginData;
import org.nightlabs.jdo.ObjectIDUtil;
import org.nightlabs.jfire.base.InvokeUtil;
import org.nightlabs.jfire.base.login.JFireLogin;
import org.nightlabs.jfire.base.login.JFireSecurityConfiguration;
import org.nightlabs.jfire.idgenerator.IDGeneratorHelperRemote;
import org.nightlabs.jfire.testsuite.JFireTestSuite;
import org.nightlabs.util.CacheDirTag;
import org.nightlabs.util.IOUtil;
import org.nightlabs.util.ObservedProcess;

/**
 * A TestCase that checks ID generation
 */
@JFireTestSuite(JFireIDGeneratorTestSuite.class)
public class IDGeneratorTest extends TestCase
{
	private static Logger logger = Logger.getLogger(IDGeneratorTest.class);

	private static boolean SKIPPED = false;

	@Override
	protected void setUp() throws Exception
	{
		logger.info("setUp: invoked");
	}

	@Override
	protected void tearDown()
			throws Exception
	{
		logger.info("tearDown: invoked");
	}

	private static void populateClasspath(File dir, StringBuilder classpath)
	{
		for (File child : dir.listFiles()) {
			if (child.isDirectory())
				populateClasspath(child, classpath);
			else if (child.isFile() && child.getName().endsWith(".ear")) {
				File tmpBaseDir;
				try {
					tmpBaseDir = new File(IOUtil.createUserTempDir("jfire_server.", null), "ear");
					if (!tmpBaseDir.isDirectory())
						tmpBaseDir.mkdirs();

					if (!tmpBaseDir.isDirectory())
						throw new IOException("Could not create directory: " + tmpBaseDir.getAbsolutePath());
				} catch (IOException e) {
					throw new RuntimeException(e);
				}

				CacheDirTag cacheDirTag = new CacheDirTag(tmpBaseDir);
				try {
					cacheDirTag.tag("JFire - http://www.jfire.org", true, false);
				} catch (IOException e) {
					logger.warn("initialise: " + e, e);
				}

				File tmpEarDir = new File(tmpBaseDir, child.getName());
				try {
					IOUtil.unzipArchiveIfModified(child, tmpEarDir);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
				populateClasspath(tmpEarDir, classpath);
			}
			else if (child.isFile() && child.getName().endsWith(".jar")) {
				if (classpath.length() > 0)
					classpath.append(File.pathSeparatorChar);

				classpath.append(child.getAbsolutePath());
			}
		}
	}

	public void testGetIDsExternally()
	throws Exception
	{
		if (SKIPPED) {
			logger.debug("testGetIDsExternally: skipped!");
			return;
		}

		String javaHome = System.getProperty("java.home");
		String java = IOUtil.addFinalSlash(IOUtil.addFinalSlash(javaHome) + "bin") + "java";
		StringBuilder cp = new StringBuilder();

		populateClasspath(new File("../server/default/deploy"), cp);
		populateClasspath(new File("../server/default/lib"), cp);
		populateClasspath(new File("../lib"), cp);

		String[] cmd = {
				java,
				"-cp",
				cp.toString(),
				IDGeneratorTest.class.getName()
		};
		Process p = Runtime.getRuntime().exec(cmd);
		ObservedProcess observedProcess = new ObservedProcess(p);

		ByteArrayOutputStream processOutput = new ByteArrayOutputStream();
		int exitCode = observedProcess.waitForProcess(processOutput, processOutput);
		if (exitCode != 0)
			fail("Process finished with exitCode=" + exitCode + " and the following output: " + processOutput.toString());
	}

	public void testGetIDsInternally()
	throws Exception
	{
		if (SKIPPED) {
			logger.debug("testGetIDsInternally: skipped!");
			return;
		}

		_testGetIDs();
	}

	private static final int ITERATION_QUANTITY = 1;

	private static boolean manualCascadedAuthentication = false;

	public void _testGetIDs()
	throws Exception
	{
		LoginData ld = new LoginData("chezfrancois.jfire.org", "francois", "test");
		ld.setDefaultValues();

		if (manualCascadedAuthentication)
			ld.setInitialContextFactory("org.jnp.interfaces.NamingContextFactory");

		long maxID = -1;
		String namespace = ObjectIDUtil.makeValidIDString("namespace", true);
		for (int i = 0; i < ITERATION_QUANTITY; i++) {
			InitialContext initialContext = null;
			JFireLogin jfireLogin = null;
			if (manualCascadedAuthentication) {
				jfireLogin = new JFireLogin(ld);
				jfireLogin.login();
			}
			try {
				initialContext = new InitialContext(ld.getInitialContextProperties());
				IDGeneratorHelperRemote idGeneratorHelper = (IDGeneratorHelperRemote) initialContext.lookup(InvokeUtil.JNDI_PREFIX_EJB_BY_REMOTE_INTERFACE + IDGeneratorHelperRemote.class.getName());
				long[] ids = idGeneratorHelper.clientNextIDs(namespace, 0, 5);
				for (long id : ids) {
					if (id <= maxID)
						fail("IDGenerator returned duplicate id for namespace \"" + namespace + "\": " + ObjectIDUtil.longObjectIDFieldToString(id));
					maxID = id;
				}
			} finally {
				if (jfireLogin != null)
					jfireLogin.logout();

				if (initialContext != null)
					initialContext.close();
			}
		}
	}

	public static void main(String[] args) throws Exception {
		JFireSecurityConfiguration.declareConfiguration();
		new IDGeneratorTest()._testGetIDs();
	}

}
