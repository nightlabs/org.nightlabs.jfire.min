/**
 * 
 */
package org.nightlabs.jfire.base.security;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.nightlabs.j2ee.LoginData;
import org.nightlabs.jfire.base.JFireEjb3Factory;
import org.nightlabs.jfire.base.jdo.GlobalJDOManagerProvider;
import org.nightlabs.jfire.base.jdo.notification.JDOLifecycleAdapterWorkerThreadAsync;
import org.nightlabs.jfire.base.jdo.notification.JDOLifecycleEvent;
import org.nightlabs.jfire.base.jdo.notification.JDOLifecycleListener;
import org.nightlabs.jfire.jdo.notification.IJDOLifecycleListenerFilter;
import org.nightlabs.jfire.jdo.notification.JDOLifecycleState;
import org.nightlabs.jfire.security.AbstractSecurityReflector;
import org.nightlabs.jfire.security.AuthorizedObjectRefLifecycleListenerFilter;
import org.nightlabs.jfire.security.ISecurityReflector;
import org.nightlabs.jfire.security.JFireSecurityManagerRemote;
import org.nightlabs.jfire.security.NoUserException;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.jfire.security.UserDescriptor;
import org.nightlabs.jfire.security.id.AuthorityID;
import org.nightlabs.jfire.security.id.RoleID;
import org.nightlabs.jfire.security.id.UserLocalID;

/**
 * This is the implementation of {@link SecurityReflector} that can be used for JFire clients.
 * 
 * @author Marco Schulze - marco at nightlabs dot de 
 * @author Alexander Bieber <!-- alex [AT] nightlabs.d [DOT] de -->
 */
public class SecurityReflectorClient extends AbstractSecurityReflector implements ISecurityReflector {

	private static final long serialVersionUID = 20080906L;
	
	private LoginData loginData;
	
	public SecurityReflectorClient() {
	}
	
	/**
	 * Set the {@link LoginData} this {@link SecurityReflector} should work with.
	 * 
	 * @param loginData The login data to set.
	 */
	public void setLoginData(LoginData loginData) {
		this.loginData = loginData;
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.security.ISecurityReflector#createInitialContext()
	 */
	@Override
	public InitialContext createInitialContext() throws NoUserException {
		if (loginData == null)
			throw new NoUserException("No loginData was yet provided for this " + this.getClass().getSimpleName());
		try {
			return new InitialContext(getInitialContextProperties());
		} catch (NamingException e) {
			throw new RuntimeException(e);
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.security.ISecurityReflector#getInitialContextProperties()
	 */
	@Override
	public Properties getInitialContextProperties() throws NoUserException {
		if (loginData == null)
			throw new NoUserException("No loginData was yet provided for this " + this.getClass().getSimpleName());
		return loginData.getInitialContextProperties();
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.security.ISecurityReflector#getUserDescriptor()
	 */
	@Override
	public UserDescriptor getUserDescriptor() throws NoUserException {
		if (loginData == null)
			throw new NoUserException("No loginData was yet provided for this " + this.getClass().getSimpleName());
		return new UserDescriptor(loginData.getOrganisationID(), loginData.getUserID(), loginData.getWorkstationID(), loginData.getSessionID());
	}

	private Map<AuthorityID, Set<RoleID>> cache_authorityID2roleIDSet = new HashMap<AuthorityID, Set<RoleID>>();

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.security.AbstractSecurityReflector#getRoleIDs(org.nightlabs.jfire.security.id.AuthorityID)
	 */
	@Override
	public synchronized Set<RoleID> getRoleIDs(AuthorityID authorityID) throws NoUserException
	{
		Set<RoleID> result = cache_authorityID2roleIDSet.get(authorityID);
		if (result != null)
			return result;

		try {
			JFireSecurityManagerRemote jfireSecurityManager = JFireEjb3Factory.getRemoteBean(JFireSecurityManagerRemote.class, getInitialContextProperties());
			result = jfireSecurityManager.getRoleIDs(authorityID);
		} catch (NoUserException e) {
			throw e;
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			if (ExceptionUtils.indexOfThrowable(e, NoUserException.class) < 0)
				throw new RuntimeException(e);
			else
				throw new NoUserException(e);
		}

		cache_authorityID2roleIDSet.put(authorityID, result);
		return result;
	}

	private JDOLifecycleListener authorizedObjectRefLifecycleListener = null;

	private class AuthorizedObjectRefLifecycleListener extends JDOLifecycleAdapterWorkerThreadAsync
	{
		private IJDOLifecycleListenerFilter filter;

		public AuthorizedObjectRefLifecycleListener() {
			filter = createAuthorizedObjectRefLifecycleListenerFilter();
		}

		@Override
		public IJDOLifecycleListenerFilter getJDOLifecycleListenerFilter() {
			return filter;
		}

		@Override
		public void notify(JDOLifecycleEvent event) {
			clearAuthorityID2RoleIDCache();
		}
	}

	protected IJDOLifecycleListenerFilter createAuthorizedObjectRefLifecycleListenerFilter() {
		UserDescriptor userDescriptor = getUserDescriptor();
		return new AuthorizedObjectRefLifecycleListenerFilter(
				UserLocalID.create(userDescriptor.getOrganisationID(), userDescriptor.getUserID(), userDescriptor.getOrganisationID()),
				JDOLifecycleState.DIRTY, JDOLifecycleState.DELETED
		);		
	}
	
	protected void clearAuthorityID2RoleIDCache() {
		synchronized (SecurityReflectorClient.this) {
			cache_authorityID2roleIDSet.clear();
		}
	}
	
	public synchronized void unregisterAuthorizedObjectRefLifecycleListener()
	{
		if (authorizedObjectRefLifecycleListener != null) {
			GlobalJDOManagerProvider.sharedInstance().getLifecycleManager().removeLifecycleListener(authorizedObjectRefLifecycleListener);
			authorizedObjectRefLifecycleListener = null;
			cache_authorityID2roleIDSet.clear();
		}
	}

	public synchronized void registerAuthorizedObjectRefLifecycleListener()
	{
		unregisterAuthorizedObjectRefLifecycleListener();

		authorizedObjectRefLifecycleListener = new AuthorizedObjectRefLifecycleListener();
		GlobalJDOManagerProvider.sharedInstance().getLifecycleManager().addLifecycleListener(authorizedObjectRefLifecycleListener);
		cache_authorityID2roleIDSet.clear();
	}
}
