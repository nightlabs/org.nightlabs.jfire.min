/**
 *
 */
package org.nightlabs.jfire.testsuite.base.security;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.both;
import static org.nightlabs.jfire.testsuite.hamcrest.IsNotEmptyMatcher.isNotEmpty;
import static org.nightlabs.jfire.testsuite.hamcrest.IsNotNullMatcher.isNotNull;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import javax.jdo.FetchPlan;
import javax.jdo.JDOHelper;
import javax.security.auth.login.LoginException;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jdo.query.QueryCollection;
import org.nightlabs.jfire.base.JFireEjb3Factory;
import org.nightlabs.jfire.base.login.JFireLogin;
import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.prop.PropertySet;
import org.nightlabs.jfire.security.Authority;
import org.nightlabs.jfire.security.AuthorityType;
import org.nightlabs.jfire.security.AuthorizedObject;
import org.nightlabs.jfire.security.AuthorizedObjectRef;
import org.nightlabs.jfire.security.JFireSecurityManagerRemote;
import org.nightlabs.jfire.security.RoleGroup;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.security.UserLocal;
import org.nightlabs.jfire.security.UserSecurityGroup;
import org.nightlabs.jfire.security.id.AuthorityID;
import org.nightlabs.jfire.security.id.RoleGroupID;
import org.nightlabs.jfire.security.id.UserID;
import org.nightlabs.jfire.security.id.UserLocalID;
import org.nightlabs.jfire.security.id.UserSecurityGroupID;
import org.nightlabs.jfire.security.search.UserQuery;
import org.nightlabs.jfire.testsuite.TestCase;


/**
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 * @author Fitas Amine - fitas [at] nightlabs [dot] de
 *
 */

public class UserSecurityGroupTestCase extends TestCase {

	Logger logger = Logger.getLogger(UserSecurityGroupTestCase.class);

	private static String[] FETCH_GROUP_SECURITYGROUP =new String[] {FetchPlan.DEFAULT,
		UserSecurityGroup.FETCH_GROUP_MEMBERS,
		User.FETCH_GROUP_USER_LOCAL,
		User.FETCH_GROUP_PERSON,
		PropertySet.FETCH_GROUP_FULL_DATA};


	private static ThreadLocal<UserSecurityGroupID> newUserSecurityGroupID = new ThreadLocal<UserSecurityGroupID>();

	@Test
	public void testCreateUserGroup() throws Exception {
		logger.info("Create UserGroup: begin");		
		UserSecurityGroup userSecurityGroup = new UserSecurityGroup(SecurityReflector.getUserDescriptor().getOrganisationID(), 
				"UserGroup"+String.valueOf(IDGenerator.nextID(UserSecurityGroup.class)));

		userSecurityGroup.setName("Test User Group");
		userSecurityGroup.setDescription("This User Security group is for a testing scenario.");	
		JFireSecurityManagerRemote m = JFireEjb3Factory.getRemoteBean(JFireSecurityManagerRemote.class, SecurityReflector.getInitialContextProperties());

		userSecurityGroup = m.storeUserSecurityGroup(userSecurityGroup, 
				true, 
				(String[]) null,
				1);

		if(userSecurityGroup!=null)
		{
			newUserSecurityGroupID.set((UserSecurityGroupID)JDOHelper.getObjectId(userSecurityGroup));
			logger.info("the following UserSecurityGroup was created"+userSecurityGroup.getName());
		}
		else
			fail("No UserSecurityGroup was Create!!!");

		logger.info("Create UserGroup: end");
	}
	
	@Test
	public void testListUserSecurityGroups() throws Exception{
		logger.info("List UserSecurityGroups: begin");		
		
		if (newUserSecurityGroupID.get() == null) 
			fail("Seems that creating the User Security Group has failed, no UserSecurityGroupID was registered in the ThreadLocal");
		
		JFireSecurityManagerRemote sm = JFireEjb3Factory.getRemoteBean(JFireSecurityManagerRemote.class,SecurityReflector.getInitialContextProperties());

		Set<UserSecurityGroupID> userSecurityGroupIDs = sm.getUserSecurityGroupIDs();	
		if (userSecurityGroupIDs == null || userSecurityGroupIDs.isEmpty())
			fail("No UserSecurityGroups was found!!!");
		
		List<UserSecurityGroup> userSecurityGroups = sm.getUserSecurityGroups(userSecurityGroupIDs, FETCH_GROUP_SECURITYGROUP, NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT);			
		logger.info("the following UserSecurityGroups was found!!!");
		for (UserSecurityGroup group : userSecurityGroups) {
			logger.info("group name = "+ group.getName());
		}
		logger.info("List UserSecurityGroups: end");
	}	
	
	@Test
	public void testAssignUsersToSecurityGroup() throws Exception {	

		logger.info("Assign UsersToSecurityGroup: begin");		

		if (newUserSecurityGroupID.get() == null) {
			fail("Seems that creating the User Security Group has failed, no UserSecurityGroupID was registered in the ThreadLocal");
		}

		JFireSecurityManagerRemote sm = JFireEjb3Factory.getRemoteBean(JFireSecurityManagerRemote.class, SecurityReflector.getInitialContextProperties());

		UserSecurityGroup group = sm.getUserSecurityGroups(
				Collections.singleton(newUserSecurityGroupID.get()), 
				FETCH_GROUP_SECURITYGROUP
				, NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT).iterator().next();
		
		Random rndGen = new Random(System.currentTimeMillis());	
		// add a random test case user to the group	
		List<User> users = QueryNewUsers(NewUserTestCase.NEW_USER_PREFEXID);		
		sm.setMembersOfUserSecurityGroup(newUserSecurityGroupID.get(), 
				Collections.singleton(
						(UserLocalID) JDOHelper.getObjectId(
								users.get(rndGen.nextInt(users.size())).getUserLocal()) ));

		logger.info("Assign UsersToSecurityGroup: end");	
	}



	private final String QUERY_USER_ROLEGROUP_ID =  "org.nightlabs.jfire.security.queryUsers";

	@Test
	public void testAssignRoleGroup() throws Exception {	

		logger.info("Assign AssignRoleGroup: begin");	

		if (newUserSecurityGroupID.get() == null) {
			fail("Seems that creating the User Security Group has failed, no UserSecurityGroupID was registered in the ThreadLocal");
		}

		JFireSecurityManagerRemote sm = JFireEjb3Factory.getRemoteBean(JFireSecurityManagerRemote.class, SecurityReflector.getInitialContextProperties());

		AuthorityID authorityID = AuthorityID.create(SecurityReflector.getUserDescriptor().getOrganisationID(), 
				Authority.AUTHORITY_ID_ORGANISATION);

		UserSecurityGroup group = sm.getUserSecurityGroups(
				Collections.singleton(newUserSecurityGroupID.get()), 
				FETCH_GROUP_SECURITYGROUP
				, NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT).iterator().next();

		Authority authority = sm.getAuthorities(Collections.singleton(authorityID), 
				new String[] {
			Authority.FETCH_GROUP_NAME,
			Authority.FETCH_GROUP_AUTHORITY_TYPE,
			Authority.FETCH_GROUP_AUTHORIZED_OBJECT_REFS,
			Authority.FETCH_GROUP_ROLE_GROUP_REFS,
			AuthorizedObjectRef.FETCH_GROUP_ROLE_GROUP_REFS,
			AuthorizedObjectRef.FETCH_GROUP_AUTHORITY,
			AuthorityType.FETCH_GROUP_ROLE_GROUPS,
			RoleGroup.FETCH_GROUP_NAME}, 
			NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT).iterator().next();

		//UserSecurityGroupRef userGroupRef = (UserSecurityGroupRef) authority.createAuthorizedObjectRef(group);
		//sm.setGrantedRoleGroups(authorizedObjectID, authorityID, roleGroupIDs);


		//List<RoleGroupIDSetCarrier>  roleGroupIDSetCarrier = sm.getRoleGroupIDSetCarriers(authorityID);
		Set<RoleGroupID> assignedRoleGroupIDs = new HashSet<RoleGroupID>(); 
		Set<RoleGroupID> roleGroupIDs = sm.getRoleGroupIDs();	
		RoleGroupID queryUserRoleGroupID  = RoleGroupID.create(QUERY_USER_ROLEGROUP_ID);

		// check if the Query User ID exists among the role groups
		for (RoleGroupID roleGroupID :  roleGroupIDs) 
			if(roleGroupID.equals(queryUserRoleGroupID))
				assignedRoleGroupIDs.add(roleGroupID);


		//		Set<RoleGroupID> roleGroupIDs = new HashSet<RoleGroupID>(); 
		//		Set<RoleGroup> roleGroups = authority.getAuthorityType().getRoleGroups();
		//		for (RoleGroup roleGroup : roleGroups) {
		//			if(roleGroup.getRoleGroupID().equals(QUERY_USER_ROLEGROUP_ID))
		//			{
		//				logger.info("I found the role group");
		//				logger.info(roleGroup.getRoleGroupID());
		//				roleGroupIDs.add((RoleGroupID) JDOHelper.getObjectId(roleGroup));
		//			}	
		//
		//			logger.info(roleGroup.getRoleGroupID());
		//		}

		sm.setGrantedRoleGroups(newUserSecurityGroupID.get(), authorityID, assignedRoleGroupIDs);		

		sm.storeUserSecurityGroup(group, 
				false, 
				FETCH_GROUP_SECURITYGROUP,
				NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT);


		logger.info("Assign AssignRoleGroup: end");	

	}


	
	@Test
	public void testLoginUserofSecurityGroup() throws Exception {	
		logger.info("testLoginUserofSecurityGroup: begin");	
		
		if (newUserSecurityGroupID.get() == null) {
			fail("Seems that creating the User Security Group has failed, no UserSecurityGroupID was registered in the ThreadLocal");
		}

		JFireSecurityManagerRemote sm = JFireEjb3Factory.getRemoteBean(JFireSecurityManagerRemote.class, SecurityReflector.getInitialContextProperties());

		UserSecurityGroup group = sm.getUserSecurityGroups(
				Collections.singleton(newUserSecurityGroupID.get()), 
				FETCH_GROUP_SECURITYGROUP
				, NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT).iterator().next();
		for (AuthorizedObject member : group.getMembers()) {
			if (member instanceof UserLocal) {
				UserLocal userLocal = (UserLocal) member;
				JFireLogin login = new JFireLogin(userLocal.getOrganisationID(), 
						userLocal.getUserID(), NewUserTestCase.NEW_USER_PASSWORD);
				try {
					login.login();
				} catch (LoginException e) {
					fail("Could not login with the new user");
					return;
				}
				finally
				{
					login.logout();		
				}
				
				try {
					QueryNewUsers(NewUserTestCase.NEW_USER_PREFEXID);
				} catch (Exception e) {
					logger.info("ended with failure.");
					fail("Could not Access the authirized EJB method");	
				}
				finally
				{
					login.logout();		
				}
			}
		}
		logger.info("testLoginUserofSecurityGroup: end");	
	}
	
	private List<User> QueryNewUsers(String query) throws Exception{
		JFireSecurityManagerRemote sm = JFireEjb3Factory.getRemoteBean(JFireSecurityManagerRemote.class,SecurityReflector.getInitialContextProperties());
		final QueryCollection<UserQuery> queries =new QueryCollection<UserQuery>(User.class);
		UserQuery userQuery = new UserQuery();
		userQuery.setUserID(query);
		queries.add(userQuery);
		Set<UserID> userIDs = sm.getUserIDs(queries);
		assertThat("No UserIDs was found!!!",
				userIDs,both(isNotNull()).and(isNotEmpty())); 
		List<User> users = sm.getUsers(userIDs, FETCH_GROUP_SECURITYGROUP, NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT);
		assertThat("No Users was found!!!",
				users,both(isNotNull()).and(isNotEmpty())); 
		return users;		
	}
	
	

}
