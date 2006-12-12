package org.nightlabs.jfire.serverinit;

import org.nightlabs.jfire.init.InitException;


/**
 * Interface for server initialiser classes.
 * 
 * @author Tobias Langner <!-- tobias[DOT]langner[AT]nightlabs[DOT]de -->
 */
public interface IServerInitialiser {
	/**
	 * This method is called upon execution of the respective server init.
	 */
	public void initialise() throws InitException;
}
