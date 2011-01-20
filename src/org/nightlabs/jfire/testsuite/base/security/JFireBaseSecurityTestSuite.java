/**
 *
 */
package org.nightlabs.jfire.testsuite.base.security;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 *
 */
@RunWith(Suite.class)
@SuiteClasses({NewUserTestCase.class, UserSecurityGroupTestCase.class}) 
public class JFireBaseSecurityTestSuite
{}
