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
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

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
import org.eclipse.jface.action.IAction;
import org.eclipse.swt.widgets.Display;

import org.nightlabs.ModuleException;
import org.nightlabs.base.extensionpoint.AbstractEPProcessor;
import org.nightlabs.base.extensionpoint.EPProcessorException;
import org.nightlabs.config.Config;
import org.nightlabs.jfire.base.JFireBasePlugin;
import org.nightlabs.jfire.base.jdo.cache.Cache;
import org.nightlabs.jfire.classloader.JFireRCLBackend;
import org.nightlabs.jfire.classloader.JFireRCLBackendUtil;
import org.nightlabs.jfire.classloader.JFireRCDLDelegate;
import org.nightlabs.j2ee.InitialContextProvider;
import org.nightlabs.math.Base62Coder;

/**
 * Defines a client login to a j2ee server.<br/>
 * Use the static function getLogin. If logged in an instance of this class is returned. 
 * If not and all attempts to authenticate on the server fail a {@link javax.security.auth.login.LoginException} is thrown. 
 * 
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 */
public class Login
	extends AbstractEPProcessor
	implements InitialContextProvider
{
	public static final Logger LOGGER = Logger.getLogger(Login.class);
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
	 * Loginstate: Working offline
	 * @see LoginStateListener
	 */
	public static final int LOGINSTATE_OFFLINE = 2;


	private static Login sharedInstanceLogin = null;
	
	/**
	 * Class used to pass the result of 
	 * login procedures back to {@link Login}
	 * @author alex
	 */
	public static class AsyncLoginResult {
		private Throwable exception = null;
		private boolean success = false;
		private String message = "";
		private boolean workOffline = false;
		
		private boolean wasAuthenticationErr = false;
		private boolean wasCommunicationErr = false;
		private boolean wasSocketTimeout = false;
		
		public void reset() {
			exception = null;
			success = false;
			message = "";
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

	private int currLoginState = LOGINSTATE_LOGGED_OUT;
	/**
	 * Returns one of {@link Login#LOGINSTATE_LOGGED_IN}, {@link Login#LOGINSTATE_LOGGED_OUT}
	 * or {@link Login#LOGINSTATE_OFFLINE}
	 * @return
	 */
	public int getLoginState() {
		return currLoginState;
	}

	/**
	 * First removes the JFireRCDLDelegate from
	 * the parent classloader and then flushes 
	 * user information (logs out).
	 */
	public void logout() {
		// remove class loader delegate
		JFireRCDLDelegate.sharedInstance().unregister();
		// logout
		loginContext = null;
		nullUserMembers();
		notifyLoginStateListeners(LOGINSTATE_LOGGED_OUT);
		try {
			Cache.sharedInstance().close();
		} catch (ModuleException e) {
			throw new RuntimeException();
		}
	}

	/**
	 * Nulls all members associated to a login.
	 */
	private void nullUserMembers() {
		organisationID = null;
		userID = null;
		loginName = null;
		password = null;
		serverURL = null;
		contextFactory = null;
		securityProtocol = null;
		workstationID = null;
	}
	
	
	private volatile boolean handlingLogin = false;
	private AsyncLoginResult loginResult = new AsyncLoginResult();
	
	private Runnable loginHandlerRunanble = new Runnable() {
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
					loginContext = new JFireLoginContext("jfire", new LoginCallbackHandler());
					
					ILoginHandler lHandler = getLoginHandler();
					if (lHandler == null)
						throw new LoginException("Cannot login, loginHandler is not set!");
					
					LOGGER.debug("Calling login handler");
					lHandler.handleLogin(loginContext,sharedInstanceLogin.runtimeConfigModule, loginResult);
					
					
					if ((!loginResult.isSuccess()) || (loginResult.getException() != null)) {
						loginContext = null;
						return;
					}
					
					// copy properties
					sharedInstanceLogin.copyPropertiesFrom(loginContext);
					// done should be logged in by now
					
					// at the end, we register the JFireRCDLDelegate
					JFireRCDLDelegate.sharedInstance().register(); // this method does nothing, if already registered.
					
					// notify loginstate listeners
					notifyLoginStateListeners(LOGINSTATE_LOGGED_IN);				
				} catch(Throwable t){
					loginContext = null;
					LOGGER.error("Exception thrown while logging in.",t);
					loginResult.setException(t);
				}
			} finally {
				handlingLogin = false;
				synchronized (loginResult) {
					LOGGER.debug("Login handler done notifying loginResult");
					loginResult.notifyAll();
				}
			}
		}
	};
		// not logged in by now
	/**
	 * Actually performs the login procedure.<br/>
	 * This method calls {@link #doLogin(boolean)} with parameter forceLogoutFirst
	 * set to false, so nothing will happen if already logged in.
	 * 
	 * @throws LoginException Exception is thrown whenever some error occurs during login. 
	 * But not that the user might be presented the possibility to work offline. 
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
	 * But not that the user might be presented the possibility to work offline. 
	 * In this case a LoginException is thrown as well with a {@link WorkOfflineException} as cause.
	 * 
	 * @see ILoginHandler
	 * @see Login#setLoginHandler(ILoginHandler)
	 */
	private void doLogin(final boolean forceLogoutFirst) throws LoginException {
		LOGGER.debug("Login requested by thread "+Thread.currentThread());		
		if ((currLoginState == LOGINSTATE_OFFLINE)){
			long elapsedTime = System.currentTimeMillis() - lastWorkOfflineDecision;
			if (elapsedTime > WORK_OFFLINE_TIMEOUT) {
				LoginException lEx = new LoginException();
				lEx.initCause(new WorkOfflineException());
				throw lEx;
			}
		}
		
		if (getLoginState() == LOGINSTATE_LOGGED_IN) {
			LOGGER.debug("Already logged in, returnning. Thread "+Thread.currentThread());		
			return;
		}
		if (!handlingLogin) {
			handlingLogin = true;
			Display.getDefault().asyncExec(loginHandlerRunanble);
		}
		if (!Display.getDefault().getThread().equals(Thread.currentThread())) {
			LOGGER.debug("Login requestor-thread "+Thread.currentThread()+" waiting for login handler");		
			synchronized (loginResult) {
				while (handlingLogin) {
					try {
						loginResult.wait(5000);
					} catch (InterruptedException e) { }
				}
			}
			LOGGER.debug("Login requestor-thread "+Thread.currentThread()+" returned");		
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
				LOGGER.error("Exception thrown while logging in.",loginResult.getException());
			throw new LoginException(loginResult.getException().getMessage());
		}
		if (!loginResult.isSuccess()) {
			if (loginResult.isWorkOffline()) {
				// if user decided to work offline first notify loginstate listeners
				notifyLoginStateListeners(LOGINSTATE_OFFLINE);
				// but then still throw Exception with WorkOffline as cause
				LoginException lEx = new LoginException(loginResult.getMessage());
				lEx.initCause(new WorkOfflineException(loginResult.getMessage()));
				throw lEx;
			}
			else
				throw new LoginException(loginResult.getMessage());
		}
		Cache.sharedInstance().open(sessionID);

		LOGGER.debug("Login OK. Thread "+Thread.currentThread());
	}
	
	/**
	 * If not logged in by now does so and
	 * returns the static instance of Login.
	 * @return
	 * @throws LoginException
	 */
	public static Login getLogin()
	throws LoginException
	{
		return getLogin(true);
	}
	
	public static Login sharedInstance()
	{
		if (sharedInstanceLogin == null)
			throw new NullPointerException("createLogin has not been called! SharedInstance is null!");
		return sharedInstanceLogin;
	}
	
	/**
	 * Returns the static instance of Login.
	 * If doLogin is true the login procedure is started.
	 * @param doLogin specifies weather the login procedure should be started
	 * @throws LoginException
	 * @see Login#doLogin()
	 */
	public static Login getLogin(boolean doLogin)
	throws LoginException
	{
		if (sharedInstanceLogin == null)
			throw new NullPointerException("createLogin not called! sharedInstance is null!");

		if (doLogin) {
			sharedInstanceLogin.doLogin();
		}
		return sharedInstanceLogin;
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
	
	private LoginConfigModule loginConfigModule;
	private LoginConfigModule runtimeConfigModule = new LoginConfigModule();
	
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
		
		try {
			loginConfigModule = ((LoginConfigModule)Config.sharedInstance().createConfigModule(LoginConfigModule.class));
			if (loginConfigModule != null) {
				BeanUtils.copyProperties(runtimeConfigModule,loginConfigModule);
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
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
		this.serverURL = runtimeConfigModule.getServerURL();
		this.contextFactory = runtimeConfigModule.getInitialContextFactory();
		this.securityProtocol = runtimeConfigModule.getSecurityProtocol();
		this.workstationID = runtimeConfigModule.getWorkstationID();
	}



	protected transient Properties initialContextProperties = null;
	protected transient InitialContext initialContext = null;

	public void flushInitialContextProperties() {
		initialContextProperties = null;
	}

	/**
	 * Returns InitialContextProperties
	 */
	public Hashtable getInitialContextProperties() throws LoginException{
//		boolean doReload;
		
//		if (getLoginState() != LOGINSTATE_LOGGED_IN) {
//		  LOGGER.debug("getInitialContextProperties(): begin");
//			doLogin();
//
//			LOGGER.debug("getInitialContextProperties(): logged in");
//			
//			LOGGER.debug("getInitialContextProperties(): generating props");
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

	
	
	public InitialContext getInitialContext() throws NamingException, LoginException
	{
		LOGGER.debug("getInitialContext(): begin");
		doLogin();
		LOGGER.debug("getInitialContext(): logged in");
		if (initialContext != null)
			return initialContext;

		LOGGER.debug("getInitialContext(): creating new initctx.");
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
		return runtimeConfigModule;
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
	
	public synchronized void addLoginStateListener(LoginStateListener loginStateListener) {
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
	
	private long lastWorkOfflineDecision = System.currentTimeMillis();
	
	
	protected void notifyLoginStateListeners(int loginState){
		synchronized (loginStateListenerRegistry) {
			try {
				currLoginState = loginState;
				if (currLoginState == LOGINSTATE_OFFLINE)
					lastWorkOfflineDecision = System.currentTimeMillis();
				
				if (!isProcessed()) {
					try {
						process();
					} catch (EPProcessorException e) {
						LOGGER.error("Processing LoginStateListener extensions failed!", e);
					}
				}
				for (Iterator it = new LinkedList(loginStateListenerRegistry).iterator(); it.hasNext();) {
					try {
						LoginStateListenerRegistryItem item = (LoginStateListenerRegistryItem)it.next();
						item.getLoginStateListener().loginStateChanged(loginState,item.getAction());
					} catch (Throwable t) {
						LOGGER.warn("Caught exception while notifying LoginStateListener. Continue.", t);
					}
				}
			} catch (Throwable t) {
				LOGGER.warn("Cought exception while notifying LoginStateListener. Abort.", t);
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
		return "org.nightlabs.jfire.base.loginstatelistener";
	}

	/**
	 * @see org.nightlabs.base.extensionpoint.AbstractEPProcessor#processElement(IExtension, org.eclipse.core.runtime.IConfigurationElement)
	 */
	public void processElement(IExtension extension, IConfigurationElement element) throws EPProcessorException
	{
		if ("loginStateListener".equals(element.getName())) {
			try {
				LoginStateListener listener = (LoginStateListener) element.createExecutableExtension("class");
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
			LOGGER.error("Obtaining shared instance of Login failed!", e);
		}
		
		if ( login != null)
			login.copyPropertiesFrom(loginContext);
		else
			throw new IllegalStateException("Shared instance of Login must not be null");
		
		loginResult.setSuccess(false);
		loginResult.setMessage(null);
		loginResult.setException(null);
		login.flushInitialContextProperties();
		
		// verify login
		JFireRCLBackend jfireCLBackend = null;
//		LanguageManager languageManager = null;
		if (jfireCLBackend == null) {
			try {
				jfireCLBackend = JFireRCLBackendUtil.getHome(
						login.getInitialContextProperties()).create();
//				languageManager = LanguageManagerUtil.getHome(
//						login.getInitialContextProperties()).create();
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
				else {
					if (ExceptionUtils.indexOfThrowable(cause, SecurityException.class) >= 0) {
						loginResult.setWasAuthenticationErr(true);
            loginResult.setSuccess(false);
          }
				}
			} catch (LoginException x) {
				LOGGER.warn("Login failed with a very weird LoginException!", x);
				// something went very wrong as we are in the login procedure
				IllegalStateException ill = new IllegalStateException("Caught LoginException although getLogin(FALSE) was executed. "+x.getMessage());
				ill.initCause(x);
				throw ill;
			} catch (Exception x) {
				if (x instanceof CommunicationException) {
					loginResult.setWasCommunicationErr(true);
				}
				if (x instanceof SocketTimeoutException) {
					loginResult.setWasSocketTimeout(true);
				}
				// cant create local bean stub
				LOGGER.warn("Login failed!", x);
				LoginException loginE = new LoginException(x.getMessage());
				loginE.initCause(x);
				loginResult.setMessage(JFireBasePlugin.getResourceString("login.error.unhadledExceptionMessage"));
				loginResult.setException(loginE);
			}
     }
    
		return loginResult;
	}
	
}
