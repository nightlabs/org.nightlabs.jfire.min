/**
 * 
 */
package org.nightlabs.jfire.testsuite.login;

import javax.security.auth.login.LoginException;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.nightlabs.jfire.base.login.JFireLogin;
import org.nightlabs.jfire.organisation.OrganisationManager;
import org.nightlabs.jfire.organisation.OrganisationManagerUtil;
import org.nightlabs.jfire.security.UserManagerUtil;
import org.nightlabs.jfire.test.cascadedauthentication.TestRequestResultTreeNode;
import org.nightlabs.jfire.testsuite.JFireTestSuite;
import org.nightlabs.util.Util;

/**
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 *
 */
@JFireTestSuite(JFireLoginTestSuite.class)
public class LoginTest extends TestCase {
	private static final Logger logger = Logger.getLogger(LoginTest.class);

	public LoginTest() { }

	public void testCorrectLogin() throws Exception {
		JFireLogin login = JFireTestLogin.getUserLogin(JFireTestLogin.USER_QUALIFIER_SERVER_ADMIN);
		login.login();
		UserManagerUtil.getHome(login.getInitialContextProperties()).create();		
	}

	public void testInCorrectLogin() {
		JFireLogin login = new JFireLogin("chezfrancois.jfire.org", "francoiz", "text");
		try {
			login.login();
		} catch (LoginException e) {
			// OK, caught some login exception
			return;
		}
		fail("Could login with wrong credentials");
//		UserManagerUtil.getHome(login.getInitialContextProperties()).create();		
	}

//	public void testCascadedAuthenticationOneOrganisationPerLevel()
//	throws Exception
//	{
//		// first try only one organisation per level
//		JFireLogin login = JFireTestLogin.getUserLogin(JFireTestLogin.USER_QUALIFIER_SALESMAN); // which user does not matter for this test.
//		login.login();
//		TestRequestResultTreeNode tree = new TestRequestResultTreeNode(null, "chezfrancois.jfire.org");
//		tree.createChildTestRequestResultTree("reseller.jfire.org").createChildTestRequestResultTree("jfire.nightlabs.org");
//		OrganisationManager organisationManager = OrganisationManagerUtil.getHome(login.getInitialContextProperties()).create();
//		tree = organisationManager.testCascadedAuthentication(tree);
//		checkResult(tree);
//	}

	@SuppressWarnings("unused")
	public void testCascadedAuthenticationMultipleOrganisationsPerLevelNoLoopbacks()
	throws Exception
	{
		// now try multiple organisations per level
		JFireLogin login = JFireTestLogin.getUserLogin(JFireTestLogin.USER_QUALIFIER_SALESMAN); // which user does not matter for this test.
		login.login();

		TestRequestResultTreeNode tree = new TestRequestResultTreeNode(null, "chezfrancois.jfire.org");
		TestRequestResultTreeNode child_0 = tree.createChildTestRequestResultTree("jfire.nightlabs.org");
		TestRequestResultTreeNode child_1 = tree.createChildTestRequestResultTree("reseller.jfire.org");

		TestRequestResultTreeNode child_0_0 = child_0.createChildTestRequestResultTree("chezfrancois.jfire.org");
		TestRequestResultTreeNode child_0_1 = child_0.createChildTestRequestResultTree("reseller.jfire.org");
		TestRequestResultTreeNode child_0_2 = child_0.createChildTestRequestResultTree("chezfrancois.jfire.org");

		OrganisationManager organisationManager = OrganisationManagerUtil.getHome(login.getInitialContextProperties()).create();
		tree = organisationManager.testCascadedAuthentication(tree);
		checkResult(tree);
	}

//	@SuppressWarnings("unused")
//	public void testCascadedAuthenticationMultipleOrganisationsPerLevelWithLoopbacks()
//	throws Exception
//	{
//		// now try multiple organisations per level
//		JFireLogin login = JFireTestLogin.getUserLogin(JFireTestLogin.USER_QUALIFIER_SALESMAN); // which user does not matter for this test.
//		login.login();
//
//		TestRequestResultTreeNode tree = new TestRequestResultTreeNode(null, "chezfrancois.jfire.org");
//		TestRequestResultTreeNode child_0 = tree.createChildTestRequestResultTree("chezfrancois.jfire.org");
//		TestRequestResultTreeNode child_1 = tree.createChildTestRequestResultTree("reseller.jfire.org");
//
//		TestRequestResultTreeNode child_0_0 = child_0.createChildTestRequestResultTree("reseller.jfire.org");
//		TestRequestResultTreeNode child_0_1 = child_0.createChildTestRequestResultTree("chezfrancois.jfire.org");
//
//		TestRequestResultTreeNode child_1_0 = child_1.createChildTestRequestResultTree("reseller.jfire.org");
//		TestRequestResultTreeNode child_1_1 = child_1.createChildTestRequestResultTree("chezfrancois.jfire.org");
//
//		TestRequestResultTreeNode child_0_0_0 = child_0_0.createChildTestRequestResultTree("jfire.nightlabs.org");
//		TestRequestResultTreeNode child_0_0_1 = child_0_0.createChildTestRequestResultTree("chezfrancois.jfire.org");
//		TestRequestResultTreeNode child_0_0_2 = child_0_0.createChildTestRequestResultTree("reseller.jfire.org"); 
//
//		TestRequestResultTreeNode child_0_1_0 = child_0_1.createChildTestRequestResultTree("chezfrancois.jfire.org");
//		TestRequestResultTreeNode child_0_1_1 = child_0_1.createChildTestRequestResultTree("jfire.nightlabs.org");
//		TestRequestResultTreeNode child_0_1_2 = child_0_1.createChildTestRequestResultTree("reseller.jfire.org");
//
//		TestRequestResultTreeNode child_1_0_0 = child_0_0.createChildTestRequestResultTree("reseller.jfire.org");
//		TestRequestResultTreeNode child_1_0_1 = child_0_0.createChildTestRequestResultTree("jfire.nightlabs.org");
//		TestRequestResultTreeNode child_1_0_2 = child_0_0.createChildTestRequestResultTree("chezfrancois.jfire.org");
//
//		OrganisationManager organisationManager = OrganisationManagerUtil.getHome(login.getInitialContextProperties()).create();
//		tree = organisationManager.testCascadedAuthentication(tree);
//		checkResult(tree);
//	}

	private static void checkResult(TestRequestResultTreeNode tree)
	{
		if (!checkResult(tree, true)) // first ignore failures so that we get a complete debug output
			checkResult(tree, false); // and in case there was an error, run it again with failures active.
	}

	private static boolean checkResult(TestRequestResultTreeNode tree, boolean ignoreFailures)
	{
		if (tree == null)
			throw new IllegalArgumentException("tree must not be null!");
		
		if (tree.getParent() != null)
			throw new IllegalArgumentException("tree.getParent() != null");

		return checkResultNode(tree, 0, 0, ignoreFailures);
	}

	private static boolean checkResultNode(TestRequestResultTreeNode node, int level, int childIdx, boolean ignoreFailures)
	{
		boolean result = true;
		logger.info("*** level = " + level + " child = " + childIdx + " ***");
		logger.info("  request: " + node.getRequest_organisationID() + " (" + getAuthenticationStack_request(node) + ")");
		logger.info("  resultBeforeRecursion: " + node.getResult_organisationID_beforeRecursion() + " (" + getAuthenticationStack_resultBeforeRecursion(node) + ")");
		logger.info("  resultAfterRecursion: " + node.getResult_organisationID_afterRecursion()  + " (" + getAuthenticationStack_resultAfterRecursion(node) + ")");
		logger.info("***");

		if (!Util.equals(node.getRequest_organisationID(), node.getResult_organisationID_beforeRecursion())) {
			result = false;
			if (!ignoreFailures)
				failNotEquals("requested organisationID is not equal to result organisationID before recursion!", node.getRequest_organisationID(), node.getResult_organisationID_beforeRecursion());
		}

		if (!Util.equals(node.getRequest_organisationID(), node.getResult_organisationID_afterRecursion())) {
			result = false;
			if (!ignoreFailures)
				failNotEquals("requested organisationID is not equal to result organisationID after recursion!", node.getRequest_organisationID(), node.getResult_organisationID_afterRecursion());
		}

		int subChildIdx = 0;
		for (TestRequestResultTreeNode child : node.getChildren())
			result &= checkResultNode(child, level + 1, subChildIdx++, ignoreFailures);

		return result;
	}

	private static String getAuthenticationStack_request(TestRequestResultTreeNode node)
	{
		StringBuffer sb = new StringBuffer();

		while (node != null) {
			if (sb.length() > 0)
				sb.insert(0, " => ");

			sb.insert(0, node.getRequest_organisationID());
			node = node.getParent();
		}

		return sb.toString();
	}

	private static String getAuthenticationStack_resultBeforeRecursion(TestRequestResultTreeNode node)
	{
		StringBuffer sb = new StringBuffer();

		while (node != null) {
			if (sb.length() > 0)
				sb.insert(0, " => ");

			sb.insert(0, node.getResult_organisationID_beforeRecursion());
			node = node.getParent();
		}

		return sb.toString();
	}

	private static String getAuthenticationStack_resultAfterRecursion(TestRequestResultTreeNode node)
	{
		StringBuffer sb = new StringBuffer();

		while (node != null) {
			if (sb.length() > 0)
				sb.insert(0, " => ");

			sb.insert(0, node.getResult_organisationID_afterRecursion());
			node = node.getParent();
		}

		return sb.toString();
	}

}
