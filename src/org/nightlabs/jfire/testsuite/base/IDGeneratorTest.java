/**
 *
 */
package org.nightlabs.jfire.testsuite.base;

import java.io.ByteArrayOutputStream;
import java.io.File;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.nightlabs.j2ee.LoginData;
import org.nightlabs.jdo.ObjectIDUtil;
import org.nightlabs.jfire.base.login.JFireSecurityConfiguration;
import org.nightlabs.jfire.idgenerator.IDGeneratorHelper;
import org.nightlabs.jfire.idgenerator.IDGeneratorHelperUtil;
import org.nightlabs.jfire.testsuite.JFireTestSuite;
import org.nightlabs.util.IOUtil;
import org.nightlabs.util.ObservedProcess;

/**
 * A TestCase that checks ID generation
 */
@JFireTestSuite(JFireBaseTestSuite.class)
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

	private static final int ITERATION_QUANTITY = 20000;

	public void _testGetIDs()
	throws Exception
	{
		LoginData ld = new LoginData("chezfrancois.jfire.org", "francois", "test");
		ld.setDefaultValues();

		long maxID = -1;
		String namespace = ObjectIDUtil.makeValidIDString("namespace", true);
		for (int i = 0; i < ITERATION_QUANTITY; i++) {
			IDGeneratorHelper idGeneratorHelper = IDGeneratorHelperUtil.getHome(ld.getInitialContextProperties()).create();
			long[] ids = idGeneratorHelper.clientNextIDs(namespace, 0, 5);
			for (long id : ids) {
				if (id <= maxID)
					fail("IDGenerator returned duplicate id for namespace \"" + namespace + "\": " + ObjectIDUtil.longObjectIDFieldToString(id));
				maxID = id;
			}
		}
	}

	public static void main(String[] args) throws Exception {
		JFireSecurityConfiguration.declareConfiguration();
		new IDGeneratorTest()._testGetIDs();
	}

}
