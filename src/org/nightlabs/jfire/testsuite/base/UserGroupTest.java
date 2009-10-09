/**
 *
 */
package org.nightlabs.jfire.testsuite.base;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.nightlabs.jfire.base.JFireEjb3Factory;
import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.security.JFireSecurityManagerRemote;
import org.nightlabs.jfire.security.UserSecurityGroup;
import org.nightlabs.jfire.security.SecurityReflector;
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
		UserSecurityGroup userSecurityGroup = new UserSecurityGroup(SecurityReflector.getUserDescriptor().getOrganisationID(), 
				"UserGroup"+String.valueOf(IDGenerator.nextID(UserSecurityGroup.class)));
		
		userSecurityGroup.setName("Test User Group");
		userSecurityGroup.setDescription("This group consists out of testing scenario.");	
		JFireSecurityManagerRemote m = JFireEjb3Factory.getRemoteBean(JFireSecurityManagerRemote.class, SecurityReflector.getInitialContextProperties());
	
		m.storeUserSecurityGroup(userSecurityGroup, 
				false, 
				(String[]) null,
				1);

		if(userSecurityGroup!=null)
		{
			logger.info("the following UserSecurityGroup was created"+userSecurityGroup.getName());
		}
		else
			fail("No UserSecurityGroup was Create!!!");

		logger.info("Create UserGroup: end");
	}

}
