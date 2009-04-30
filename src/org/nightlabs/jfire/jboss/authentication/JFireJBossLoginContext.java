package org.nightlabs.jfire.jboss.authentication;

import java.security.Principal;
import java.security.acl.Group;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.Configuration;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import org.apache.log4j.Logger;
import org.jboss.security.RunAsIdentity;
import org.jboss.security.SecurityAssociation;
import org.jboss.security.SecurityAssociation.SubjectContext;
import org.nightlabs.jfire.base.JFireBasePrincipal;

/**
 * A special <code>LoginContext</code> that came into existence because of this JBoss bug: https://jira.jboss.org/jira/browse/JBAS-6289
 *
 * @author marco
 */
public class JFireJBossLoginContext extends LoginContext
{
	private static final Logger logger = Logger.getLogger(JFireJBossLoginContext.class);

	private static final boolean RUN_AS_IDENTITY_ENABLED = false;

	public JFireJBossLoginContext(String name)
	throws LoginException
	{
		super(name);
	}

	public JFireJBossLoginContext(String name, Subject subject)
	throws LoginException
	{
		super(name, subject);
	}

	public JFireJBossLoginContext(String name, CallbackHandler callbackHandler)
	throws LoginException
	{
		super(name, callbackHandler);
	}

	public JFireJBossLoginContext(String name, Subject subject, CallbackHandler callbackHandler)
	throws LoginException
	{
		super(name, subject, callbackHandler);
	}

	public JFireJBossLoginContext(String name, Subject subject, CallbackHandler callbackHandler, Configuration config)
	throws LoginException
	{
		super(name, subject, callbackHandler, config);
	}

	private boolean authenticated = false;
	private Principal oldPrincipal = null;
	private JFireJBossRunAsIdentity jfireJBossRunAsIdentity;

	@Override
	public void login() throws LoginException
	{
		if (authenticated)
			throw new IllegalStateException("Already authenticated! Cannot login again using the same LoginContext instance without logging out first!");

		if (RUN_AS_IDENTITY_ENABLED) {
			oldPrincipal = SecurityAssociation.getPrincipal();
		}

		super.login();

		authenticated = true;

		if (RUN_AS_IDENTITY_ENABLED) {

			SubjectContext subjectContext = SecurityAssociation.peekSubjectContext();
			Subject subject = subjectContext.getSubject();
			Group roleSet = null;
			for (Principal principal : subject.getPrincipals()) {
				if ("Roles".equals(principal.getName())) {
					roleSet = (Group) principal;
					break;
				}
			}

			String firstRoleName = null;
			Set<String> extraRoleNames = null;
			if (roleSet == null)
				throw new IllegalStateException("Subject does not contain 'Roles'!");

			for (Enumeration<? extends Principal> ePrincipal = roleSet.members(); ePrincipal.hasMoreElements(); ) {
				Principal role = ePrincipal.nextElement();
				if (firstRoleName == null)
					firstRoleName = role.getName();
				else {
					if (extraRoleNames == null)
						extraRoleNames = new HashSet<String>();

					extraRoleNames.add(role.getName());
				}
			}

			RunAsIdentity jbossRunAsIdentity = new RunAsIdentity(
					firstRoleName == null ? "_unknown_" : firstRoleName,
							subjectContext.getPrincipal().getName(),
							extraRoleNames
			);
			jfireJBossRunAsIdentity = new JFireJBossRunAsIdentity(
					jbossRunAsIdentity,
					(JFireBasePrincipal) subjectContext.getPrincipal()
			);
			SecurityAssociation.pushRunAsIdentity(jfireJBossRunAsIdentity);
		}
	}


	@Override
	public void logout() throws LoginException
	{
		if (!authenticated)
			throw new IllegalStateException("Not authenticated! Cannot logout! You must not call logout, if login was not called before!");

		if (RUN_AS_IDENTITY_ENABLED) {
			RunAsIdentity poppedRunAs = SecurityAssociation.popRunAsIdentity();
			if (poppedRunAs != jfireJBossRunAsIdentity) {
				logger.warn("Popped run-as-identity is not the same as previously pushed one! expected=" + jfireJBossRunAsIdentity + " found=" + poppedRunAs, new Exception("StackTrace"));
			}
		}

		super.logout();

		authenticated = false;

		if (RUN_AS_IDENTITY_ENABLED) {
			jfireJBossRunAsIdentity = null;

			Principal currentPrincipal = SecurityAssociation.getPrincipal();
			Principal principalAfterRestore = currentPrincipal;
			Principal callerPrincipalAfterRestore = SecurityAssociation.getCallerPrincipal();

			if (currentPrincipal != oldPrincipal) { // must really be the same instance - not only equal
				int logoutCounter = 0;
				do {
					++logoutCounter;
					//				SecurityAssociation.popSubjectContext();
					Principal principalBeforeLogout = SecurityAssociation.getPrincipal();
					super.logout();
					currentPrincipal = SecurityAssociation.getPrincipal();
					if (principalBeforeLogout == currentPrincipal)
						throw new IllegalStateException("loginContext.logout() had no effect! The current principal didn't change!");
				} while (currentPrincipal != null && currentPrincipal != oldPrincipal);

				if (currentPrincipal == oldPrincipal) {
					if (logger.isDebugEnabled()) {
						if (logger.isTraceEnabled()) {
							logger.trace(
									"logout: Restoring identity was successful but detected that between login and logout, there were " + logoutCounter + " login(s) without a corresponding logout!" +
									" SecurityAssociation.principal=" + principalAfterRestore +
									" SecurityAssociation.callerPrincipal=" + callerPrincipalAfterRestore +
									" oldPrincipal="+oldPrincipal,
									new Exception("StackTrace")
							);
						}
						else {
							logger.debug(
									"logout: Restoring identity was successful but detected that between login and logout, there were " + logoutCounter + " login(s) without a corresponding logout!" +
									" SecurityAssociation.principal=" + principalAfterRestore +
									" SecurityAssociation.callerPrincipal=" + callerPrincipalAfterRestore +
									" oldPrincipal="+oldPrincipal
							);
						}
					}
				}
				else
					throw new IllegalStateException(
							"Restoring identity failed! Maybe there was a manual logout done (i.e. SecurityAssociation.pop...) without a corresponding login?!" +
							" SecurityAssociation.principal=" + principalAfterRestore +
							" SecurityAssociation.callerPrincipal=" + callerPrincipalAfterRestore +
							" oldPrincipal="+oldPrincipal
					);
			}

			oldPrincipal = null;
		}
	}
}
