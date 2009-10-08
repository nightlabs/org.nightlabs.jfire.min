/**
 *
 */
package org.nightlabs.jfire.testsuite.base;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.nightlabs.jfire.testsuite.JFireTestSuite;

/**
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 * @author Fitas Amine - fitas [at] nightlabs [dot] de
 *
 */
@JFireTestSuite(JFireBaseTestSuite.class)
public class UserGroupTest extends TestCase {

	Logger logger = Logger.getLogger(UserGroupTest.class);

	public UserGroupTest() {
		this("Testing the functionality around usergroups.");
	}

	/**
	 * @param name
	 */
	public UserGroupTest(String name) {
		super(name);
	}

	public void testCreateUserGroup() throws Exception {
		logger.info("Create UserGroup: begin");
		// This is nonsense. It should use API methods to create a User*Security*Group. This class
		// should be renamed, too.
//		JFireTestSuiteBaseManagerRemote um = JFireEjb3Factory.getRemoteBean(JFireTestSuiteBaseManagerRemote.class, SecurityReflector.getInitialContextProperties());
//		UserSecurityGroup UserGroup = um.createUserGroup("UserGroup"+String.valueOf(IDGenerator.nextID(UserSecurityGroup.class)));
//		UserGroup.setName("Test User Group");
//		UserGroup.setDescription("This group consists out of testing scenario.");
//
//		if(UserGroup!=null)
//		{
//			logger.info("the following UserSecurityGroup was created"+UserGroup.getName());
//		}
//		else
//			fail("No Users was found!!!");
//
//		logger.info("Create UserGroup: end");
	}

}
