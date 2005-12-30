/*
 * Created 	on Oct 4, 2004
 * 					by Alexander Bieber
 *
 */
package org.nightlabs.ipanema.base.login;

import java.awt.EventQueue;
import java.lang.reflect.InvocationTargetException;

import javax.security.auth.login.LoginException;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;
import org.eclipse.ui.PlatformUI;

import org.nightlabs.ipanema.base.JFireBasePlugin;
import org.nightlabs.rcp.splash.SplashScreen;

/**
 * @see org.nightlabs.ipanema.base.login.ILoginHandler
 * 
 * @author Alexander Bieber
 */
public class JFireLoginHandler implements ILoginHandler {
	public static final Logger LOGGER = Logger.getLogger(JFireLoginHandler.class);

	/**
	 * Opens an instance of {@link LoginDialog}. 
	 * The dialog sets the loginResult and loginContext values.
	 * A login verification is performed to be sure the user can
	 * be identified by the credentials he specified.
	 * @see org.nightlabs.ipanema.base.login.ILoginHandler#handleLogin(org.nightlabs.ipanema.base.login.JFireLoginContext)
	 * @see LoginDialog
	 */
	public void handleLogin(JFireLoginContext loginContext, LoginConfigModule loginConfigModule, Login.AsyncLoginResult loginResult) throws LoginException {
		if (SplashScreen.waitForVisibleSplash()) // isSplashVisible())
			handleSplashLogin(loginContext, loginConfigModule, loginResult);
		else
			handleSWTLogin(loginContext, loginConfigModule, loginResult);
	}
	
	protected void handleSWTLogin(JFireLoginContext loginContext, LoginConfigModule loginConfigModule, Login.AsyncLoginResult loginResult) throws LoginException {
		LoginDialog loginDialog = new LoginDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell());
		LoginDialog.registerSharedInstance(loginDialog);
		try {
			loginDialog.setLoginResult(loginResult);
			loginDialog.setLoginModule(loginConfigModule);
			loginDialog.setLoginContext(loginContext);
			
			// LoginDialog does all the work
			loginDialog.open();
		}
		finally {
			LoginDialog.deregisterSharedInstance();
		}
	}
	
	protected void handleSplashLogin(JFireLoginContext loginContext, LoginConfigModule loginConfigModule, final Login.AsyncLoginResult loginResult) throws LoginException 
	{
		final SplashLoginPanel loginPanel = new SplashLoginPanel(loginContext, loginConfigModule);
		loginPanel.doLayout();
//		Object mutex = new Object();
		try {
      Runnable runLogin =  new Runnable() {
				public void run() {
					SplashScreen.setSplashPanel(loginPanel);
					SplashScreen.setProgressIndeterminite(false);
					SplashScreen.setProgressMinMax(0,1);
					SplashScreen.setProgressValue(0);
					SplashScreen.setSplashMessage("");
				}
      };
      if (EventQueue.isDispatchThread())
        runLogin.run();
      else
        SwingUtilities.invokeAndWait(runLogin);
      
		} catch (InterruptedException e) {
			throw new LoginException("Error in SplashLogin: "+e.getMessage());
		} catch (InvocationTargetException e) {
			throw new LoginException("Error in SplashLogin: "+e.getMessage());
		}
		boolean loggedIn = false;
		int loginTries = 0;
		// Wait for the login
		while ((!loggedIn) && (loginTries < 3)) {
			try {
				synchronized (SplashScreen.getMutex()) {
					SplashScreen.getMutex().wait();
				}
			} catch (InterruptedException e) {
				throw new LoginException("Caught InterruptedException while waiting for login: "+e.getMessage());
			}
      if(loginPanel.isWorkOffline()) {
        SplashScreen.setSplashMessage("Work offline");
        break;
      }
			loginPanel.assignLoginValues();
			SplashScreen.setSplashMessage("Try to log in ...");
			Login.AsyncLoginResult testResult = Login.testLogin(loginContext);
			testResult.copyValuesTo(loginResult);
			loggedIn = testResult.isSuccess();
			if (loggedIn) {
				SplashScreen.setSplashMessage("Login successful");
				break;
			}
			loginTries++;
			
			SwingUtilities.invokeLater(new Runnable(){
				public void run() {
					if ((!loginResult.isWasAuthenticationErr()) && (loginResult.isSuccess()))
						return;
					else {
						// login failed
						if (loginResult.isWasAuthenticationErr()) {
							loginPanel.setErrMessage(JFireBasePlugin.getResourceString("login.error.authenticationFailed"));
						}
						else if (loginResult.isWasCommunicationErr()) {
							loginPanel.setErrMessage(JFireBasePlugin.getResourceString("login.error.communicatinError")+" "+loginResult.getException().getMessage());
						}
						else {
							String message = loginResult.getMessage();
							if (loginResult.getException() != null) {
								message += "\n"+loginResult.getException().getClass().getName()+": "+loginResult.getException().getLocalizedMessage();
								Throwable cause = loginResult.getException().getCause();
								while ( cause != null ) {
									message += "\n"+cause.getClass().getName()+": "+cause.getLocalizedMessage();
									cause = cause.getCause();
								}
								loginResult.getException().printStackTrace();
							}
							loginPanel.setErrMessage(message);
							
						}
					}
				}
			});
		}
		SplashScreen.resetSplashPanel();
	}

}
