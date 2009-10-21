/**
 *
 */
package org.nightlabs.jfire.testsuite.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

import javax.jdo.FetchPlan;
import javax.jdo.JDOHelper;
import javax.naming.InitialContext;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.base.JFireEjb3Factory;
import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.prop.PropertySet;
import org.nightlabs.jfire.security.Authority;
import org.nightlabs.jfire.security.AuthorityType;
import org.nightlabs.jfire.security.AuthorizedObjectRef;
import org.nightlabs.jfire.security.JFireSecurityManagerRemote;
import org.nightlabs.jfire.security.RoleGroup;
import org.nightlabs.jfire.security.RoleGroupRef;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.security.UserSecurityGroup;
import org.nightlabs.jfire.security.UserSecurityGroupRef;
import org.nightlabs.jfire.security.id.AuthorityID;
import org.nightlabs.jfire.security.id.RoleGroupID;
import org.nightlabs.jfire.security.id.UserID;
import org.nightlabs.jfire.security.id.UserLocalID;
import org.nightlabs.jfire.security.id.UserSecurityGroupID;
import org.nightlabs.jfire.security.listener.SecurityChangeController;
import org.nightlabs.jfire.servermanager.j2ee.J2EEAdapter;
import org.nightlabs.jfire.testsuite.JFireTestSuite;


/**
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 * @author Fitas Amine - fitas [at] nightlabs [dot] de
 *
 */
@JFireTestSuite(JFireBaseSecurityTestSuite.class)
public class UserSecurityGroupTestCase extends TestCase {

	Logger logger = Logger.getLogger(UserSecurityGroupTestCase.class);


	private static String[] FETCH_GROUP_SECURITYGROUP =new String[] {FetchPlan.DEFAULT,
		UserSecurityGroup.FETCH_GROUP_MEMBERS,
		User.FETCH_GROUP_USER_LOCAL,
		User.FETCH_GROUP_PERSON,
		PropertySet.FETCH_GROUP_FULL_DATA};


	private static ThreadLocal<UserSecurityGroupID> newUserSecurityGroupID = new ThreadLocal<UserSecurityGroupID>();


	public UserSecurityGroupTestCase() {
		this("Testing the functionality around usergroups.");
	}

	/**
	 * @param name
	 */
	public UserSecurityGroupTestCase(String name) {
		super(name);
	}

	@Test
	public void testCreateUserGroup() throws Exception {
		logger.info("Create UserGroup: begin");		
		UserSecurityGroup userSecurityGroup = new UserSecurityGroup(SecurityReflector.getUserDescriptor().getOrganisationID(), 
				"UserGroup"+String.valueOf(IDGenerator.nextID(UserSecurityGroup.class)));

		userSecurityGroup.setName("Test User Group");
		userSecurityGroup.setDescription("This group consists out of testing scenario.");	
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
	public void testAssignUsersToSecurityGroup() throws Exception {	

		JFireSecurityManagerRemote sm = JFireEjb3Factory.getRemoteBean(JFireSecurityManagerRemote.class, SecurityReflector.getInitialContextProperties());

		UserSecurityGroup group = sm.getUserSecurityGroups(
				Collections.singleton(newUserSecurityGroupID.get()), 
				FETCH_GROUP_SECURITYGROUP
				, NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT).iterator().next();


		Collection<UserID> ids = sm.getUserIDs(SecurityReflector.getUserDescriptor().getOrganisationID(), null);

		Collection<User> users = sm.getUsers(ids, new String[] {
				User.FETCH_GROUP_NAME,
				User.FETCH_GROUP_USER_LOCAL
		}, NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT);


		// filter out all internal users (concrete _System_ and _Other_), since they should not end up in a user-group
		List<User> members = new ArrayList<User>();
		for (Iterator<User> it = users.iterator(); it.hasNext(); ) {
			User user = it.next();
			if (user.getName().startsWith("_") && user.getName().endsWith("_")) //$NON-NLS-1$ //$NON-NLS-2$
				it.remove();
			else
				members.add(user);
		}

		Set<UserLocalID> includedUserLocalIDs = new HashSet<UserLocalID>(1);

		Random rndGen = new Random(System.currentTimeMillis());	

		// add a random user to the group
		includedUserLocalIDs.add((UserLocalID) JDOHelper.getObjectId( 
					(members.get(rndGen.nextInt(members.size())).getUserLocal())));
		

		sm.setMembersOfUserSecurityGroup(newUserSecurityGroupID.get(), includedUserLocalIDs);
		sm.storeUserSecurityGroup(group, 
				false, 
				FETCH_GROUP_SECURITYGROUP,
				NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT);
	}


	
	
	
	@Test
	public void testAssignRoleGroup() throws Exception {	

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
			Set<RoleGroupID> roleGroupIDs = new HashSet<RoleGroupID>(); 
			Set<RoleGroup> roleGroups = authority.getAuthorityType().getRoleGroups();
			for (RoleGroup roleGroup : roleGroups) {
				if(roleGroup.getRoleGroupID().equals("org.nightlabs.jfire.security.queryUsers"))
				{
					logger.info("I found the role group");
					logger.info(roleGroup.getRoleGroupID());
					//RoleGroupRef roleGroupRef = authority.createRoleGroupRef(roleGroup);
					//userGroupRef.addRoleGroupRef(roleGroupRef);
					roleGroupIDs.add((RoleGroupID) JDOHelper.getObjectId(roleGroup));
				}	

				logger.info(roleGroup.getRoleGroupID());
			}
			
			sm.setGrantedRoleGroups(newUserSecurityGroupID.get(), authorityID, roleGroupIDs);
			
	}
	
	
}
