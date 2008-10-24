package org.nightlabs.jfire.testsuite.cascadedauthentication;

import javax.naming.InitialContext;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.nightlabs.jfire.base.login.JFireLogin;
import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.jfire.organisation.OrganisationManager;
import org.nightlabs.jfire.organisation.OrganisationManagerUtil;
import org.nightlabs.jfire.test.cascadedauthentication.TestRequestResultTreeNode;
import org.nightlabs.jfire.testsuite.JFireTestSuite;
import org.nightlabs.jfire.testsuite.login.JFireTestLogin;
import org.nightlabs.util.Util;

@JFireTestSuite(CascadedAuthenticationTestSuite.class)
public class CascadedAuthenticationTest
extends TestCase
{
	private static final Logger logger = Logger.getLogger(CascadedAuthenticationTest.class);

	private String organisationID_root = null;
	private String organisationID_chezfrancois = "chezfrancois.jfire.org";
	private String organisationID_reseller = "reseller.jfire.org";
	private String organisationID_a = "a.jfire.org";
	private String organisationID_b = "b.jfire.org";

	@Override
	protected void setUp()
			throws Exception
	{
		InitialContext initialContext = new InitialContext();
		organisationID_root = Organisation.getRootOrganisationID(initialContext);
		initialContext.close();
	}

	public void testCascadedAuthenticationOneOrganisationPerLevel()
	throws Exception
	{
		JFireLogin login = JFireTestLogin.getUserLogin(JFireTestLogin.USER_QUALIFIER_SALESMAN); // which user does not matter for this test.
		login.login();
		try {
			TestRequestResultTreeNode tree = new TestRequestResultTreeNode(null, organisationID_chezfrancois);
			tree.createChildTestRequestResultTree(organisationID_reseller).createChildTestRequestResultTree(organisationID_root);
			OrganisationManager organisationManager = OrganisationManagerUtil.getHome(login.getInitialContextProperties()).create();
			tree = organisationManager.testCascadedAuthentication(tree);
			checkResult(tree);
		} finally {
			login.logout();
		}
	}

	@SuppressWarnings("unused")
	public void testCascadedAuthenticationMultipleOrganisationsPerLevelNoLoopbacks()
	throws Exception
	{
		JFireLogin login = JFireTestLogin.getUserLogin(JFireTestLogin.USER_QUALIFIER_SALESMAN); // which user does not matter for this test.
		login.login();
		try {
			TestRequestResultTreeNode tree = new TestRequestResultTreeNode(null, organisationID_chezfrancois);
			TestRequestResultTreeNode child_0 = tree.createChildTestRequestResultTree(organisationID_root);
			TestRequestResultTreeNode child_1 = tree.createChildTestRequestResultTree(organisationID_reseller);

			TestRequestResultTreeNode child_0_0 = child_0.createChildTestRequestResultTree(organisationID_chezfrancois);
			TestRequestResultTreeNode child_0_1 = child_0.createChildTestRequestResultTree(organisationID_reseller);
			TestRequestResultTreeNode child_0_2 = child_0.createChildTestRequestResultTree(organisationID_chezfrancois);

			OrganisationManager organisationManager = OrganisationManagerUtil.getHome(login.getInitialContextProperties()).create();
			tree = organisationManager.testCascadedAuthentication(tree);
			checkResult(tree);
		} finally {
			login.logout();
		}
	}

	@SuppressWarnings("unused")
	public void testCascadedAuthenticationMultipleOrganisationsPerLevelWithLoopbacks()
	throws Exception
	{
		JFireLogin login = JFireTestLogin.getUserLogin(JFireTestLogin.USER_QUALIFIER_SALESMAN); // which user does not matter for this test.
		login.login();
		try {
			TestRequestResultTreeNode tree = new TestRequestResultTreeNode(null, organisationID_chezfrancois);
			TestRequestResultTreeNode child_0 = tree.createChildTestRequestResultTree(organisationID_chezfrancois);
			TestRequestResultTreeNode child_1 = tree.createChildTestRequestResultTree(organisationID_reseller);

			TestRequestResultTreeNode child_0_0 = child_0.createChildTestRequestResultTree(organisationID_reseller);
			TestRequestResultTreeNode child_0_1 = child_0.createChildTestRequestResultTree(organisationID_chezfrancois);

			TestRequestResultTreeNode child_1_0 = child_1.createChildTestRequestResultTree(organisationID_reseller);
			TestRequestResultTreeNode child_1_1 = child_1.createChildTestRequestResultTree(organisationID_chezfrancois);

			TestRequestResultTreeNode child_0_0_0 = child_0_0.createChildTestRequestResultTree(organisationID_root);
			TestRequestResultTreeNode child_0_0_1 = child_0_0.createChildTestRequestResultTree(organisationID_chezfrancois);
			TestRequestResultTreeNode child_0_0_2 = child_0_0.createChildTestRequestResultTree(organisationID_reseller);

			TestRequestResultTreeNode child_0_1_0 = child_0_1.createChildTestRequestResultTree(organisationID_chezfrancois);
			TestRequestResultTreeNode child_0_1_1 = child_0_1.createChildTestRequestResultTree(organisationID_root);
			TestRequestResultTreeNode child_0_1_2 = child_0_1.createChildTestRequestResultTree(organisationID_reseller);

			TestRequestResultTreeNode child_1_0_0 = child_1_0.createChildTestRequestResultTree(organisationID_reseller);
			TestRequestResultTreeNode child_1_0_1 = child_1_0.createChildTestRequestResultTree(organisationID_root);
			TestRequestResultTreeNode child_1_0_2 = child_1_0.createChildTestRequestResultTree(organisationID_chezfrancois);

			OrganisationManager organisationManager = OrganisationManagerUtil.getHome(login.getInitialContextProperties()).create();
			tree = organisationManager.testCascadedAuthentication(tree);
			checkResult(tree);
		} finally {
			login.logout();
		}
	}

//	@SuppressWarnings("unused")
//	public void testCascadedAuthenticationMultipleOrganisationsPerLevelWithLoopbacksMultiServer()
//	throws Exception
//	{
//		JFireLogin login = JFireTestLogin.getUserLogin(JFireTestLogin.USER_QUALIFIER_SALESMAN); // which user does not matter for this test.
//		login.login();
//
//		TestRequestResultTreeNode tree = new TestRequestResultTreeNode(null, organisationID_chezfrancois);
//		TestRequestResultTreeNode child_0 = tree.createChildTestRequestResultTree(organisationID_chezfrancois);
//		TestRequestResultTreeNode child_1 = tree.createChildTestRequestResultTree(organisationID_reseller);
//
//		TestRequestResultTreeNode child_0_0 = child_0.createChildTestRequestResultTree(organisationID_reseller);
//		TestRequestResultTreeNode child_0_1 = child_0.createChildTestRequestResultTree(organisationID_chezfrancois);
//		TestRequestResultTreeNode child_0_2 = child_0.createChildTestRequestResultTree(organisationID_a);
//		TestRequestResultTreeNode child_0_3 = child_0.createChildTestRequestResultTree(organisationID_b);
//
//		TestRequestResultTreeNode child_1_0 = child_1.createChildTestRequestResultTree(organisationID_reseller);
//		TestRequestResultTreeNode child_1_1 = child_1.createChildTestRequestResultTree(organisationID_chezfrancois);
//		TestRequestResultTreeNode child_1_2 = child_1.createChildTestRequestResultTree(organisationID_b);
//		TestRequestResultTreeNode child_1_3 = child_1.createChildTestRequestResultTree(organisationID_a);
//
//		TestRequestResultTreeNode child_0_0_0 = child_0_0.createChildTestRequestResultTree(organisationID_root);
//		TestRequestResultTreeNode child_0_0_1 = child_0_0.createChildTestRequestResultTree(organisationID_chezfrancois);
//		TestRequestResultTreeNode child_0_0_2 = child_0_0.createChildTestRequestResultTree(organisationID_reseller);
//		TestRequestResultTreeNode child_0_0_3 = child_0_0.createChildTestRequestResultTree(organisationID_a);
//
//		TestRequestResultTreeNode child_0_1_0 = child_0_1.createChildTestRequestResultTree(organisationID_chezfrancois);
//		TestRequestResultTreeNode child_0_1_1 = child_0_1.createChildTestRequestResultTree(organisationID_root);
//		TestRequestResultTreeNode child_0_1_2 = child_0_1.createChildTestRequestResultTree(organisationID_reseller);
//		TestRequestResultTreeNode child_0_1_3 = child_0_1.createChildTestRequestResultTree(organisationID_b);
//
//		TestRequestResultTreeNode child_0_2_0 = child_0_2.createChildTestRequestResultTree(organisationID_chezfrancois);
//		TestRequestResultTreeNode child_0_2_1 = child_0_2.createChildTestRequestResultTree(organisationID_root);
//		TestRequestResultTreeNode child_0_2_2 = child_0_2.createChildTestRequestResultTree(organisationID_reseller);
//		TestRequestResultTreeNode child_0_2_3 = child_0_2.createChildTestRequestResultTree(organisationID_a);
//		TestRequestResultTreeNode child_0_2_4 = child_0_2.createChildTestRequestResultTree(organisationID_b);
//
//		TestRequestResultTreeNode child_1_0_0 = child_1_0.createChildTestRequestResultTree(organisationID_reseller);
//		TestRequestResultTreeNode child_1_0_1 = child_1_0.createChildTestRequestResultTree(organisationID_root);
//		TestRequestResultTreeNode child_1_0_2 = child_1_0.createChildTestRequestResultTree(organisationID_chezfrancois);
//		TestRequestResultTreeNode child_1_0_3 = child_1_0.createChildTestRequestResultTree(organisationID_a);
//		TestRequestResultTreeNode child_1_0_4 = child_1_0.createChildTestRequestResultTree(organisationID_b);
//
//		TestRequestResultTreeNode child_1_2_0 = child_1_2.createChildTestRequestResultTree(organisationID_b);
//		TestRequestResultTreeNode child_1_2_1 = child_1_2.createChildTestRequestResultTree(organisationID_reseller);
//		TestRequestResultTreeNode child_1_2_2 = child_1_2.createChildTestRequestResultTree(organisationID_chezfrancois);
//		TestRequestResultTreeNode child_1_2_3 = child_1_2.createChildTestRequestResultTree(organisationID_a);
//
//		TestRequestResultTreeNode child_1_3_0 = child_1_3.createChildTestRequestResultTree(organisationID_reseller);
//		TestRequestResultTreeNode child_1_3_1 = child_1_3.createChildTestRequestResultTree(organisationID_root);
//		TestRequestResultTreeNode child_1_3_2 = child_1_3.createChildTestRequestResultTree(organisationID_a);
//		TestRequestResultTreeNode child_1_3_3 = child_1_3.createChildTestRequestResultTree(organisationID_b);
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
