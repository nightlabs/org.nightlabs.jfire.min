/* *****************************************************************************
 * JFire - it's hot - Free ERP System - http://jfire.org                       *
 * Copyright (C) 2004-2005 NightLabs - http://NightLabs.org                    *
 *                                                                             *
 * This library is free software; you can redistribute it and/or               *
 * modify it under the terms of the GNU Lesser General Public                  *
 * License as published by the Free Software Foundation; either                *
 * version 2.1 of the License, or (at your option) any later version.          *
 *                                                                             *
 * This library is distributed in the hope that it will be useful,             *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of              *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU           *
 * Lesser General Public License for more details.                             *
 *                                                                             *
 * You should have received a copy of the GNU Lesser General Public            *
 * License along with this library; if not, write to the                       *
 *     Free Software Foundation, Inc.,                                         *
 *     51 Franklin St, Fifth Floor,                                            *
 *     Boston, MA  02110-1301  USA                                             *
 *                                                                             *
 * Or get it online :                                                          *
 *     http://opensource.org/licenses/lgpl-license.php                         *
 *                                                                             *
 *                                                                             *
 ******************************************************************************/

package org.nightlabs.jfire.base.login;

import java.net.SocketTimeoutException;
import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.ejb.EJBException;
import javax.naming.CommunicationException;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.security.auth.login.LoginException;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.nightlabs.base.exceptionhandler.ExceptionHandlerRegistry;
import org.nightlabs.base.exceptionhandler.ExceptionHandlerRegistry.Mode;
import org.nightlabs.base.extensionpoint.AbstractEPProcessor;
import org.nightlabs.base.extensionpoint.EPProcessorException;
import org.nightlabs.base.job.Job;
import org.nightlabs.base.notification.SelectionManager;
import org.nightlabs.base.progress.ProgressMonitorWrapper;
import org.nightlabs.base.util.RCPUtil;
import org.nightlabs.classloader.osgi.DelegatingClassLoaderOSGI;
import org.nightlabs.config.Config;
import org.nightlabs.j2ee.InitialContextProvider;
import org.nightlabs.jfire.base.JFireBasePlugin;
import org.nightlabs.jfire.base.j2ee.JFireJ2EEPlugin;
import org.nightlabs.jfire.base.jdo.cache.Cache;
import org.nightlabs.jfire.base.jdo.notification.JDOLifecycleManager;
import org.nightlabs.jfire.base.resource.Messages;
import org.nightlabs.jfire.classloader.JFireRCDLDelegate;
import org.nightlabs.jfire.classloader.JFireRCLBackend;
import org.nightlabs.jfire.classloader.JFireRCLBackendUtil;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.security.dao.UserDAO;
import org.nightlabs.jfire.security.id.UserID;
import org.nightlabs.math.Base62Coder;
import org.nightlabs.progress.ProgressMonitor;

/**
 * Defines a client login to the JFire server
 * <p>
 * Use the static function getLogin. If logged in an instance of this class is returned. 
 * If not and all attempts to authenticate on the server fail a {@link javax.security.auth.login.LoginException} is thrown.
 * <p>
 * In general all code that requires the user to log in should place a line like
 * <code>Login.getLogin();</code>
 * somewhere before. If the user is already logged in this method immediately exits and returns
 * the static Login member. If the user some time before decided to work OFFLINE this method
 * will throw an {@link WorkOfflineException} to indicate this and not make any attempts to 
 * login unless {@link #setForceLogin(boolean)} was not set to true. This means that user interface
 * actions have to do something like the following to login:
 * <pre>
 *   Login.getLogin(false).setForceLogin(true);
 *   Login.getLogin();
 * </pre>
 * 
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 */
public class Login
extends AbstractEPProcessor
implements InitialContextProvider
{
	private static final String CLASS_ELEMENT = "class"; //$NON-NLS-1$
	private static final String LOGIN_STATE_LISTENER_ELEMENT = "loginStateListener"; //$NON-NLS-1$

	/**
	 * LOG4J logger used by this class
	 */
	private static final Logger logger = Logger.getLogger(Login.class);
	
	public static final long WORK_OFFLINE_TIMEOUT = 60000; // One minute

	/**
	 * Loginstate: Logged in
	 * @see LoginStateListener
	 */
	public static final int LOGINSTATE_LOGGED_IN = 0;
	/**
	 * Loginstate: Logged out
	 * @see LoginStateListener
	 */
	public static final int LOGINSTATE_LOGGED_OUT = 1;
	/**
	 * Loginstate: Working OFFLINE indicating also that the user wants 
	 * to stay OFFLINE and causing the Login to fail for a certain time
	 * when not forced.
	 * @see LoginStateListener
	 */
	public static final int LOGINSTATE_OFFLINE = 2;


	private static Login sharedInstanceLogin = null;

	private boolean forceLogin = false;

	private long lastWorkOfflineDecisionTime = System.currentTimeMillis();

	private int currLoginState = LOGINSTATE_LOGGED_OUT;


	/**
	 * Class used to pass the result of 
	 * login procedures back to {@link Login}
	 * @author alex
	 */
	public static class AsyncLoginResult {
		private Throwable exception = null;
		private boolean success = false;
		private String message = ""; //$NON-NLS-1$
		private boolean workOffline = false;

		private boolean wasAuthenticationErr = false;
		private boolean wasCommunicationErr = false;
		private boolean wasSocketTimeout = false;

		public void reset() {
			exception = null;
			success = false;
			message = ""; //$NON-NLS-1$
			workOffline = false;

			wasAuthenticationErr = false;
			wasCommunicationErr = false;
			wasSocketTimeout = false;
		}

		public Throwable getException() {
			return exception;
		}
		public void setException(Throwable exception) {
			this.exception = exception;
		}
		public boolean isSuccess() {
			return success;
		}
		public void setSuccess(boolean success) {
			this.success = success;
		}
		public String getMessage() {
			return message;
		}
		public void setMessage(String message) {
			this.message = message;
		}		
		public boolean isWorkOffline() {
			return workOffline;
		}
		public void setWorkOffline(boolean workOffline) {
			this.workOffline = workOffline;
		}
		/**
		 * @return Returns the wasAuthenticationErr.
		 */
		public boolean isWasAuthenticationErr() {
			return wasAuthenticationErr;
		}
		/**
		 * @param wasAuthenticationErr The wasAuthenticationErr to set.
		 */
		public void setWasAuthenticationErr(boolean wasAuthenticationErr) {
			this.wasAuthenticationErr = wasAuthenticationErr;
		}
		/**
		 * @return Returns the wasCommunicationErr.
		 */
		public boolean isWasCommunicationErr() {
			return wasCommunicationErr;
		}
		/**
		 * @param wasCommunicationErr The wasCommunicationErr to set.
		 */
		public void setWasCommunicationErr(boolean wasCommunicationErr) {
			this.wasCommunicationErr = wasCommunicationErr;
		}
		/**
		 * @return Returns the wasSocketTimeout.
		 */
		public boolean isWasSocketTimeout() {
			return wasSocketTimeout;
		}
		/**
		 * @param wasSocketTimeout The wasSocketTimeout to set.
		 */
		public void setWasSocketTimeout(boolean wasSocketTimeout) {
			this.wasSocketTimeout = wasSocketTimeout;
		}		

		public void copyValuesTo(AsyncLoginResult loginResult) {
			loginResult.exception = this.exception;
			loginResult.success = this.success;
			loginResult.message = this.message;
			loginResult.workOffline = this.workOffline;

			loginResult.wasAuthenticationErr = this.wasAuthenticationErr;
			loginResult.wasCommunicationErr = this.wasCommunicationErr;
			loginResult.wasSocketTimeout = this.wasSocketTimeout;
		}
	}

	/**
	 * Used internally within static block, to be able
	 * to provide a {@link InitialContextProvider}
	 * for {@link JFireCLDelegate} without logging in. 
	 */
	protected static void createLogin(){
		sharedInstanceLogin = new Login();
		org.nightlabs.base.login.Login.sharedInstance(); // force processing of extension point
	}

	static {
		createLogin();
	}

	private String sessionID;

	public String getSessionID()
	{
		if (sessionID == null) {
			Base62Coder coder = Base62Coder.sharedInstance();
			sessionID = coder.encode(System.currentTimeMillis(), 1) + '-' + coder.encode((long)(Math.random() * 10000), 1);
		}

		return sessionID;
	}

	/**
	 * Checks if currently logged in.
	 * @return
	 */
	public static boolean isLoggedIn() {
		if (sharedInstanceLogin == null)
			return false;
		return sharedInstanceLogin.currLoginState == LOGINSTATE_LOGGED_IN;
	}

	/**
	 * Returns one of {@link Login#LOGINSTATE_LOGGED_IN}, {@link Login#LOGINSTATE_LOGGED_OUT}
	 * or {@link Login#LOGINSTATE_OFFLINE}
	 * @return
	 */
	public int getLoginState() {
		return currLoginState;
	}

	public void logout() {
		logout(true);
	}
	
	/**
	 * First removes the JFireRCDLDelegate from
	 * the parent classloader and then flushes 
	 * user information (logs out).
	 */
	private void logout(boolean doNotify) {
		// remove class loader delegate
		JFireRCDLDelegate.sharedInstance().unregister(DelegatingClassLoaderOSGI.getSharedInstance());
		// logout
		loginContext = null;
		nullUserMembers();
//		sessionID = null;
		Exception ex = null;
		try {
			Cache.sharedInstance().close();
		} catch (Exception e) {
			ex = e;
		}
		if (doNotify) {
			try {
				notifyLoginStateListeners(LOGINSTATE_LOGGED_OUT);
			} catch (Exception e) {
				ex = e;
			}
		}
		if (ex != null)
			throw new RuntimeException(ex);
	}
	
	public void workOffline() {
		if (currLoginState != LOGINSTATE_OFFLINE) {
			logout(false);
			currLoginState = LOGINSTATE_OFFLINE;
			notifyLoginStateListeners(LOGINSTATE_OFFLINE);
		}
		
	}

	/**
	 * Nulls all members associated to a login.
	 */
	private void nullUserMembers() {
		organisationID = null;
		userID = null;
		sessionID = null;
		loginName = null;
		password = null;
		serverURL = null;
		contextFactory = null;
		securityProtocol = null;
		workstationID = null;
	}


	private volatile boolean handlingLogin = false;
	private AsyncLoginResult loginResult = new AsyncLoginResult();

	protected LoginConfigModule getRuntimeConfigModule()
	{
		if (_runtimeConfigModule == null) {
			try {
				LoginConfigModule _loginConfigModule = ((LoginConfigModule)Config.sharedInstance().createConfigModule(LoginConfigModule.class));
				if (_loginConfigModule != null) {
					_runtimeConfigModule = new LoginConfigModule();
					BeanUtils.copyProperties(_runtimeConfigModule, _loginConfigModule);
//					BeanUtils.copyProperties(_runtimeConfigModule, _loginConfigModule);
				}
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		return _runtimeConfigModule;
	}

	private Runnable loginHandlerRunnable = new Runnable() {
		private boolean logoutFirst = false;

		public void run() {
			handlingLogin = true;
			if (logoutFirst)
				logout();
			try {
				try{
					if (sharedInstanceLogin == null)
						createLogin();

					loginResult.reset();
					// create a loginContext
					loginContext = new JFireLoginContext("jfire", new LoginCallbackHandler()); //$NON-NLS-1$

					ILoginHandler lHandler = getLoginHandler();
					if (lHandler == null)
						throw new LoginException("Cannot login, loginHandler is not set!"); //$NON-NLS-1$

					logger.debug("Calling login handler"); //$NON-NLS-1$
					lHandler.handleLogin(loginContext,sharedInstanceLogin.getRuntimeConfigModule(), loginResult);


					if ((!loginResult.isSuccess()) || (loginResult.getException() != null)) {
						loginContext = null;
						return;
					}

					// copy properties
					sharedInstanceLogin.copyPropertiesFrom(loginContext);
					// done should be logged in by now

					// at the end, we register the JFireRCDLDelegate
					JFireRCDLDelegate.sharedInstance().register(DelegatingClassLoaderOSGI.getSharedInstance()); // this method does nothing, if already registered.
					Set<String> test = JFireRCDLDelegate.sharedInstance().getPublishedRemotePackages();
					for (String pkg : test) {
						System.out.println(" "+pkg+","); //$NON-NLS-1$ //$NON-NLS-2$
					}
					boolean needRestart = JFireJ2EEPlugin.getDefault().updateManifest();
					if (needRestart) {
						// Set the exception-handler mode to bypass
						ExceptionHandlerRegistry.sharedInstance().setMode(Mode.bypass);
						Display.getDefault().asyncExec(new Runnable()
						{
							public void run()
							{
								Shell shell = RCPUtil.getActiveWorkbenchShell();
								MessageDialog.openInformation(
										shell,
										Messages.getString("org.nightlabs.jfire.base.login.Login.rebootDialogTitle"), //$NON-NLS-1$
										Messages.getString("org.nightlabs.jfire.base.login.Login.rebootDialogMessage")); //$NON-NLS-1$

								safeRestart();
							}
						});
					}
					forceLogin = false;
					currLoginState = LOGINSTATE_LOGGED_IN;
				} catch(Throwable t){
					loginContext = null;
					logger.error("Exception thrown while logging in.",t); //$NON-NLS-1$
					loginResult.setException(t);
				}
			} finally {
				handlingLogin = false;
				synchronized (loginResult) {
					logger.debug("Login handler done notifying loginResult"); //$NON-NLS-1$
					loginResult.notifyAll();
				}
			}
		}
	};
	// not logged in by now

	/**
	 * This method is necessary, because the restart may be required at a very early stage. Thus,
	 * this method will recursively delay the restart until it can be performed without an exception.
	 */
	private void safeRestart()
	{
		if (RCPUtil.getActiveWorkbenchShell() != null)
			PlatformUI.getWorkbench().restart();
		else { // too early!
			new org.eclipse.core.runtime.jobs.Job("Restart") { //$NON-NLS-1$
				protected IStatus run(IProgressMonitor monitor)
				{
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// ignore
					}
					Display.getDefault().asyncExec(new Runnable()
					{
						public void run()
						{
							safeRestart();
						}
					});
					return Status.OK_STATUS;
				}
			}.schedule();
		}
	}

	/**
	 * Actually performs the login procedure.<br/>
	 * This method calls {@link #doLogin(boolean)} with parameter forceLogoutFirst
	 * set to false, so nothing will happen if already logged in.
	 * 
	 * @throws LoginException Exception is thrown whenever some error occurs during login. 
	 * But not that the user might be presented the possibility to work OFFLINE. 
	 * In this case a LoginException is thrown as well with a {@link WorkOfflineException} as cause.
	 * 
	 * @see ILoginHandler
	 * @see Login#setLoginHandler(ILoginHandler)
	 * @see #doLogin(boolean)
	 */
	public void doLogin() throws LoginException {
		doLogin(false);
	}

	/**
	 * Actually performs the login procedure.
	 * To do so it calls {@link ILoginHandler#handleLogin(JFireLoginContext, LoginConfigModule, Login.AsyncLoginResult)}
	 * of the LoginHandler defined with {@link #setLoginHandler(ILoginHandler)}.
	 * 
	 * @param forceLogoutFirst Defines weather to logout first
	 * 
	 * @throws LoginException Exception is thrown whenever some error occurs during login. 
	 * But not that the user might be presented the possibility to work OFFLINE. 
	 * In this case a LoginException is thrown as well with a {@link WorkOfflineException} as cause.
	 * 
	 * @see ILoginHandler
	 * @see Login#setLoginHandler(ILoginHandler)
	 */
	private void doLogin(final boolean forceLogoutFirst) throws LoginException {
		int oldLoginstate = currLoginState;
		logger.debug("Login requested by thread "+Thread.currentThread());		 //$NON-NLS-1$
		if ((currLoginState == LOGINSTATE_OFFLINE)){
			long elapsedTime = System.currentTimeMillis() - lastWorkOfflineDecisionTime;			
			if (!forceLogin && elapsedTime < WORK_OFFLINE_TIMEOUT) {
				LoginException lEx = new LoginException();
				lEx.initCause(new WorkOfflineException());
				throw lEx;
			}
		}

		if (getLoginState() == LOGINSTATE_LOGGED_IN) {
			logger.debug("Already logged in, returnning. Thread "+Thread.currentThread()); //$NON-NLS-1$
			if (forceLogin) forceLogin = false;
			return;
		}
		if (!handlingLogin) {
			handlingLogin = true;
//			Display.getDefault().asyncExec(loginHandlerRunnable);
			Display.getDefault().syncExec(loginHandlerRunnable);
		}
		if (!Display.getDefault().getThread().equals(Thread.currentThread())) {
			logger.debug("Login requestor-thread "+Thread.currentThread()+" waiting for login handler");		 //$NON-NLS-1$ //$NON-NLS-2$
			synchronized (loginResult) {
				while (handlingLogin) {
					try {
						loginResult.wait(5000);
					} catch (InterruptedException e) { }
				}
			}
			logger.debug("Login requestor-thread "+Thread.currentThread()+" returned");		 //$NON-NLS-1$ //$NON-NLS-2$
		}
		else {
			while (handlingLogin) {
				Display.getCurrent().readAndDispatch();
			}
		}
		// exception throwing
		if (loginResult.getException() != null){
			if (loginResult.getException() instanceof LoginException)
				throw (LoginException)loginResult.getException();
			else
				logger.error("Exception thrown while logging in.",loginResult.getException()); //$NON-NLS-1$
			throw new LoginException(loginResult.getException().getMessage());
		}
		if (!loginResult.isSuccess()) {
			if (loginResult.isWorkOffline()) {
				// if user decided to work OFFLINE first notify loginstate listeners
				currLoginState = LOGINSTATE_OFFLINE;
				notifyLoginStateListeners(currLoginState);
				// but then still throw Exception with WorkOffline as cause
				LoginException lEx = new LoginException(loginResult.getMessage());
				lEx.initCause(new WorkOfflineException(loginResult.getMessage()));
				throw lEx;
			}
			else
				throw new LoginException(loginResult.getMessage());
		}

		// We should be logged in now, open the cache if not already open
		if (currLoginState == LOGINSTATE_LOGGED_IN) {
			try {
				Cache.sharedInstance().open(getSessionID()); // the cache is opened implicitely now by default, but it is closed *after* a logout.
			} catch (Throwable t) {
				logger.debug("Cache could not be opened!", t);
			}
		}

		if (currLoginState != oldLoginstate) {
			try {
				// notify loginstate listeners
				notifyLoginStateListeners(currLoginState);
			} catch (Throwable t) {
				// TODO: ignore ??
				logger.error(t);
			}
		}

		logger.debug("Login OK. Thread "+Thread.currentThread()); //$NON-NLS-1$
	}

	private org.nightlabs.jfire.base.jdo.JDOObjectID2PCClassNotificationInterceptor objectID2PCClassNotificationInterceptor = null;

	/**
	 * Sets whether to force login on next attempt even if login state is 
	 * {@link #LOGINSTATE_OFFLINE}.
	 * 
	 * @param forceLogin Whether to force Login on the next attempt.
	 */
	public void setForceLogin(boolean forceLogin) {
		this.forceLogin = forceLogin;
	}

	/**
	 * If not logged in by now does so and
	 * returns the static instance of Login.
	 * <p>
	 * This method can be called on every {@link Thread} (no matter, whether UI or not).
	 * </p>
	 *
	 * @return
	 * @throws LoginException
	 */
	public static Login getLogin()
	throws LoginException
	{
		return getLogin(true);
	}

	/**
	 * This method can be called on every {@link Thread} (no matter, whether UI or not). It returns the shared instance of Login
	 * without causing a popup dialog to appear.
	 */
	public static Login sharedInstance()
	{
		if (sharedInstanceLogin == null)
			throw new NullPointerException("createLogin has not been called! SharedInstance is null!"); //$NON-NLS-1$
		return sharedInstanceLogin;
	}

	/**
	 * Returns the static instance of Login.
	 * If doLogin is true the login procedure is started.
	 * <p>
	 * This method can be called on every {@link Thread} (no matter, whether UI or not).
	 * </p>
	 *
	 * @param doLogin specifies weather the login procedure should be started
	 * @throws LoginException
	 * @see Login#doLogin()
	 */
	public static Login getLogin(boolean doLogin)
	throws LoginException
	{
		if (sharedInstanceLogin == null)
			throw new NullPointerException("createLogin not called! sharedInstance is null!"); //$NON-NLS-1$

		if (doLogin) {
			sharedInstanceLogin.doLogin();
		}
		return sharedInstanceLogin;
	}

	/**
	 * This method spawns a {@link Job} and calls {@link #getLogin()} on it. This method
	 * should be used in login-aware UI to cause a login-dialog to popup without blocking
	 * the UI.
	 * <p>
	 * This method can be called on every {@link Thread} (no matter, whether UI or not).
	 * </p>
	 */
	public static void loginAsynchronously()
	{
		Job job = new Job(Messages.getString("org.nightlabs.jfire.base.login.Login.authenticationJob")) { //$NON-NLS-1$
			@Override
			protected IStatus run(ProgressMonitor arg0) throws Exception
			{
				try {
					Login.getLogin();
				} catch (LoginException e) {
					// we ignore it as the UI should not required to be logged in (if it's implemented correctly)
				}

				return Status.OK_STATUS;
			}
		};
		job.setPriority(Job.SHORT);
		job.schedule();
	}

	private String organisationID;
	private String userID;
	// loginName is the concated product of userID, organisationID and sessionID (userID@organisationID:sessionID)
	private String loginName;
	private String password;
	private String serverURL;
	private String contextFactory;
	private String securityProtocol;
	private String workstationID;

//	private LoginConfigModule _loginConfigModule;
	private LoginConfigModule _runtimeConfigModule = null; // new LoginConfigModule();

	// LoginContext instantiated to perform the login
	private JFireLoginContext loginContext = null;

	// ILoginHandler to handle the user interaction
	private ILoginHandler loginHandler = null;
	public ILoginHandler getLoginHandler() {
		return loginHandler;
	}
	/**
	 * Here one can hook a {@link ILoginHandler} to handle user interaction for login.
	 * @param loginHandler
	 */
	public void setLoginHandler(ILoginHandler loginHandler) {
		this.loginHandler = loginHandler;
	}

	/**
	 * Creates a new Login. It takes properties from the static {@link JFireLoginContext} 
	 * field of this class (if ). Thats why it does not take organisationID, loginName and password parameters.  
	 * 
	 * @throws NamingException
	 */
	protected Login() 
	{
		if (loginContext != null){
			copyPropertiesFrom(loginContext);
		}

//		try {
//			loginConfigModule = ((LoginConfigModule)Config.sharedInstance().createConfigModule(LoginConfigModule.class));
//			if (loginConfigModule != null) {
//				BeanUtils.copyProperties(runtimeConfigModule,loginConfigModule);
//			}
//		} catch (Exception e) {
//			throw new RuntimeException(e);
//		}
	}


	/**
	 * @return Returns the organisationID.
	 */
	public String getOrganisationID() {		
		return organisationID;
	}
	/**
	 * @return Returns the userID.
	 */
	public String getUserID() {
		return userID;
	}
	/**
	 * @return Returns the password.
	 */
	public String getPassword() {
		return password;
	}
	/**
	 * @return Returns the workstationID.
	 */
	public String getWorkstationID() {
		return workstationID;
	}

	public User getUser(String fetchGroups[], int maxFetchDepth, IProgressMonitor monitor) {
		return UserDAO.sharedInstance().getUser(
				UserID.create(organisationID, userID), 
				fetchGroups, maxFetchDepth, new ProgressMonitorWrapper(monitor)
			);
	}
	

	public JFireLoginContext getLoginContext() {
		return loginContext;
	}

	protected static final char SESSION_SEPARATOR = '/';

	public void copyPropertiesFrom(JFireLoginContext _loginContext){
		this.organisationID = _loginContext.getOrganisationID();
		this.userID = _loginContext.getUserID();
		this.password = _loginContext.getPassword();
		this.loginName = _loginContext.getUsername() + SESSION_SEPARATOR + getSessionID();
	}

	private void copyPropertiesFromConfig(){
		LoginConfiguration currentConfig = getRuntimeConfigModule().getLatestLoginConfiguration();
		this.serverURL = currentConfig.getServerURL();
		this.contextFactory = currentConfig.getInitialContextFactory();
		this.securityProtocol = currentConfig.getSecurityProtocol();
		this.workstationID = currentConfig.getWorkstationID();
	}

	protected transient Properties initialContextProperties = null;
	protected transient InitialContext initialContext = null;

	public void flushInitialContextProperties() {
		initialContextProperties = null;
	}

	/**
	 * Returns InitialContextProperties
	 */
	public Properties getInitialContextProperties() // throws LoginException
	{
//		boolean doReload;

//		if (getLoginState() != LOGINSTATE_LOGGED_IN) {
//		LOGGER.debug("getInitialContextProperties(): begin");
//		doLogin();

//		LOGGER.debug("getInitialContextProperties(): logged in");

//		LOGGER.debug("getInitialContextProperties(): generating props");
//		}
		if (initialContextProperties == null){
			copyPropertiesFrom(loginContext);
			copyPropertiesFromConfig();
			Properties props = new Properties();
			props.put(InitialContext.INITIAL_CONTEXT_FACTORY,contextFactory);
			props.put(InitialContext.PROVIDER_URL, serverURL);
			props.put(InitialContext.SECURITY_PRINCIPAL, loginName);
			props.put(InitialContext.SECURITY_CREDENTIALS, password);
			props.put(InitialContext.SECURITY_PROTOCOL, securityProtocol);
			initialContextProperties = props;
		}
		return initialContextProperties;
	}

	public InitialContext createInitialContext()
	{
		try {
			return new InitialContext(getInitialContextProperties());
		} catch (NamingException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * @deprecated Do not use anymore! Use 
	 */
	public InitialContext getInitialContext() throws NamingException, LoginException
	{
		logger.debug("getInitialContext(): begin"); //$NON-NLS-1$
		doLogin();
		logger.debug("getInitialContext(): logged in"); //$NON-NLS-1$
		if (initialContext != null)
			return initialContext;

		logger.debug("getInitialContext(): creating new initctx."); //$NON-NLS-1$
		initialContext = new InitialContext(getInitialContextProperties());
		return initialContext;
	}

	/**
	 * Returns the runtime (not the persitent) LoginConfigModule. The persistent
	 * one can be obtained via {@link Config}.
	 * 
	 * @return The runtime (not the persitent) LoginConfigModule.
	 */
	public LoginConfigModule getLoginConfigModule() {
		return getRuntimeConfigModule();
	}


	/**
	 * Simple class to hold {@link LoginStateListener} 
	 * and their associated {@link IAction}.   
	 */
	protected static class LoginStateListenerRegistryItem {
		private LoginStateListener loginStateListener;
		private IAction action;
		public LoginStateListenerRegistryItem(LoginStateListener loginStateListener, IAction action) {
			super();
			this.loginStateListener = loginStateListener;
			this.action = action;
		}		
		public IAction getAction() {
			return action;
		}
		public LoginStateListener getLoginStateListener() {
			return loginStateListener;
		}
		private boolean checkActionOnEquals = true;

		public boolean isCheckActionOnEquals() {
			return checkActionOnEquals;
		}
		public void setCheckActionOnEquals(boolean checkActionOnEquals) {
			this.checkActionOnEquals = checkActionOnEquals;
		}

		public boolean equals(Object o) {
			if (o instanceof LoginStateListenerRegistryItem) {
				if ( ((LoginStateListenerRegistryItem)o).getLoginStateListener().equals(this.loginStateListener)) {
					if (isCheckActionOnEquals()) {
						return ((LoginStateListenerRegistryItem)o).getAction().equals(this.action);
					}
					else 
						return true;
				} 
				else
					return false;
			} 
			else
				return false;
		}
	}

	/**
	 * Holds instances of {@link Login.LoginStateListenerRegistryItem}.
	 */
	private List loginStateListenerRegistry = new LinkedList();

	public void addLoginStateListener(LoginStateListener loginStateListener) {
		addLoginStateListener(loginStateListener,null);
	}

	public void addLoginStateListener(LoginStateListener loginStateListener, IAction action) {
		synchronized (loginStateListenerRegistry) {
			LoginStateListenerRegistryItem regItem = new LoginStateListenerRegistryItem(loginStateListener,action);
			loginStateListenerRegistry.add(regItem);
			loginStateListener.loginStateChanged(getLoginState(), action);
		}
	}

	/**
	 * Removes all occurences of the given {@link LoginStateListener}
	 * @param loginStateListener
	 */
	public synchronized void removeLoginStateListener(LoginStateListener loginStateListener) {
		removeLoginStateListener(loginStateListener,null,true);
	}

	/**
	 * Removes either the first or all occurences of the given {@link LoginStateListener}
	 * @param loginStateListener
	 * @param allOccurences
	 */
	public synchronized void removeLoginStateListener(LoginStateListener loginStateListener, boolean allOccurences) {
		removeLoginStateListener(loginStateListener,null,allOccurences);
	}

	/**
	 * Removes only the {@link LoginStateListener} associated to the given {@link IAction}.  
	 * @param loginStateListener
	 * @param action
	 */
	public synchronized void removeLoginStateListener(LoginStateListener loginStateListener, IAction action) {
		removeLoginStateListener(loginStateListener,action,false);
	}

	/**
	 * Removes either all occurences of the given {@link LoginStateListener} or only 
	 * the one associated to the given {@link IAction}.  
	 * @param loginStateListener
	 * @param action
	 * @param allOccurencesOfListener
	 */
	public void removeLoginStateListener(LoginStateListener loginStateListener, IAction action, boolean allOccurencesOfListener) {
		synchronized (loginStateListenerRegistry) {
			LoginStateListenerRegistryItem searchItem = new LoginStateListenerRegistryItem(loginStateListener,action);
			if (allOccurencesOfListener) {
				searchItem.setCheckActionOnEquals(false);
			}
			if (!allOccurencesOfListener) {
				loginStateListenerRegistry.remove(searchItem);
			}
			else {
				while (loginStateListenerRegistry.contains(searchItem)) {
					loginStateListenerRegistry.remove(searchItem);
				}
			}
		}
	}


	protected void notifyLoginStateListeners(int loginState){

		synchronized (loginStateListenerRegistry) {
			try {
				currLoginState = loginState;
				if (currLoginState == LOGINSTATE_OFFLINE)
					lastWorkOfflineDecisionTime = System.currentTimeMillis();

				checkProcessing();

				if (LOGINSTATE_LOGGED_IN == loginState && objectID2PCClassNotificationInterceptor == null) {
						objectID2PCClassNotificationInterceptor = new org.nightlabs.jfire.base.jdo.JDOObjectID2PCClassNotificationInterceptor();
						SelectionManager.sharedInstance().addInterceptor(objectID2PCClassNotificationInterceptor);
						JDOLifecycleManager.sharedInstance().addInterceptor(objectID2PCClassNotificationInterceptor);
				}

				if (LOGINSTATE_LOGGED_IN != loginState && objectID2PCClassNotificationInterceptor != null) {
					SelectionManager.sharedInstance().removeInterceptor(objectID2PCClassNotificationInterceptor);
					JDOLifecycleManager.sharedInstance().removeInterceptor(objectID2PCClassNotificationInterceptor);
					objectID2PCClassNotificationInterceptor = null;
				}

//				// WORKAROUND: For classloading deadlock (give the Cache some time to do its own classloading first)
//				Thread.sleep(3000); This is now done in Cache.NotificationThread#run where it is much more effective (and more user-friendly). Marco.

				for (Iterator it = new LinkedList(loginStateListenerRegistry).iterator(); it.hasNext();) {
					try {
						LoginStateListenerRegistryItem item = (LoginStateListenerRegistryItem)it.next();
						item.getLoginStateListener().loginStateChanged(loginState,item.getAction());
					} catch (Throwable t) {
						logger.warn("Caught exception while notifying LoginStateListener. Continue.", t); //$NON-NLS-1$
					}
//					// WORKAROUND: For classloading deadlock
//					Thread.sleep(200);
				}
			} catch (Throwable t) {
				logger.warn("Cought exception while notifying LoginStateListener. Abort.", t); //$NON-NLS-1$
			}
		}
	}

	/**
	 * Do not call this method yourself.<br/>
	 * It is used to trigger the notification right after the 
	 * WorkbenchWindow is shown, as Login can be requested
	 * at a point in startup when actions and other
	 * LoginStateListeners are not build yet.<br/>
	 */
	protected void triggerLoginStateNotification() {
		notifyLoginStateListeners(getLoginState());
	}

	/**
	 * @see org.nightlabs.base.extensionpoint.AbstractEPProcessor#getExtensionPointID()
	 */
	public String getExtensionPointID()
	{
		return "org.nightlabs.jfire.base.loginstatelistener"; //$NON-NLS-1$
	}

	/**
	 * @see org.nightlabs.base.extensionpoint.AbstractEPProcessor#processElement(IExtension, org.eclipse.core.runtime.IConfigurationElement)
	 */
	public void processElement(IExtension extension, IConfigurationElement element) throws Exception
	{
		if (LOGIN_STATE_LISTENER_ELEMENT.equals(element.getName())) {
			try {
				LoginStateListener listener = (LoginStateListener) element.createExecutableExtension(CLASS_ELEMENT);
				addLoginStateListener(listener);
			} catch (CoreException e) {
				throw new EPProcessorException(e);
			}
		}
	}

	public static Login.AsyncLoginResult testLogin(JFireLoginContext loginContext) {
		Login.AsyncLoginResult loginResult = new Login.AsyncLoginResult();

		Login login = null;
		try {
			login = Login.getLogin(false);
		} catch (LoginException e) {
			logger.error("Obtaining shared instance of Login failed!", e); //$NON-NLS-1$
		}

		if ( login != null)
			login.copyPropertiesFrom(loginContext);
		else
			throw new IllegalStateException("Shared instance of Login must not be null"); //$NON-NLS-1$

		loginResult.setSuccess(false);
		loginResult.setMessage(null);
		loginResult.setException(null);
		login.flushInitialContextProperties();

		// verify login
		JFireRCLBackend jfireCLBackend = null;
//		LanguageManager languageManager = null;
		if (jfireCLBackend == null) {
			try {
				System.out.println(Thread.currentThread().getContextClassLoader());
				System.out.println(JFireBasePlugin.class.getClassLoader());
//				Class.forName("org.jboss.security.jndi.LoginInitialContextFactory");
//				RMIClassLoader.loadClass("org.jboss.security.jndi.LoginInitialContextFactory");
//				RMIClassLoader.getDefaultProviderInstance().loadClass(codebase, name, defaultLoader));
//				Thread.currentThread().setContextClassLoader(Login.class.getClass().getClassLoader());
				System.out.println("**********************************************************"); //$NON-NLS-1$
				System.out.println("Create testing login"); //$NON-NLS-1$
				jfireCLBackend = JFireRCLBackendUtil.getHome(
						login.getInitialContextProperties()).create();
				System.out.println("**********************************************************"); //$NON-NLS-1$
//				languageManager = LanguageManagerUtil.getHome(
//				login.getInitialContextProperties()).create();
				loginResult.setSuccess(true);
			} catch (RemoteException remoteException) {
				Throwable cause = remoteException.getCause();
				if (cause != null && cause.getCause() instanceof EJBException) {
					EJBException ejbE = (EJBException)cause.getCause();
					if (ejbE != null) {
						if (ejbE.getCausedByException() instanceof SecurityException)
							// SecurityException authentication failure
							loginResult.setWasAuthenticationErr(true);
					}
				}
				else if (cause != null && ExceptionUtils.indexOfThrowable(cause, LoginException.class) >= 0) {
					loginResult.setWasAuthenticationErr(true);
				}
				else {
					if (ExceptionUtils.indexOfThrowable(cause, SecurityException.class) >= 0) {
						loginResult.setWasAuthenticationErr(true);				
						loginResult.setSuccess(false);
					}
					else {
						loginResult.setException(remoteException);
						loginResult.setSuccess(false);
					}
				}
//			} catch (LoginException x) {
//				logger.warn("Login failed with a very weird LoginException!", x);
//				// something went very wrong as we are in the login procedure
//				IllegalStateException ill = new IllegalStateException("Caught LoginException although getLogin(FALSE) was executed. "+x.getMessage());
//				ill.initCause(x);
//				throw ill;
			} catch (Exception x) {
				if (x instanceof CommunicationException) {
					loginResult.setWasCommunicationErr(true);
				}
				if (x instanceof SocketTimeoutException) {
					loginResult.setWasSocketTimeout(true);
				}
				// cant create local bean stub
				logger.warn("Login failed!", x); //$NON-NLS-1$
				LoginException loginE = new LoginException(x.getMessage());
				loginE.initCause(x);
				loginResult.setMessage(Messages.getString("org.nightlabs.jfire.base.login.Login.errorUnhandledExceptionMessage")); //$NON-NLS-1$
				loginResult.setException(loginE);
			}
		}

		return loginResult;
	}
	
}
