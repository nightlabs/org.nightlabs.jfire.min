package org.nightlabs.jfire.base;

import javax.ejb.Remote;

@Remote
public interface InitRemote {

	/**
	 * Empty initialisation that only serves for grouping all initialisations in this module for dependency declaration.
	 * This init is dependent on all other inits of JFireBaseEAR.
	 */
	void initialise();

}
