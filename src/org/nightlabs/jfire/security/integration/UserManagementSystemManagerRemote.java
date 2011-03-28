package org.nightlabs.jfire.security.integration;
import javax.ejb.Remote;

/**
 * Remote interface for UserManagementSystemManagerBean
 * 
 * @author Denis Dudnik <deniska.dudnik[at]gmail{dot}com>
 *
 */
@Remote
public interface UserManagementSystemManagerRemote {

	void initialise();

}
