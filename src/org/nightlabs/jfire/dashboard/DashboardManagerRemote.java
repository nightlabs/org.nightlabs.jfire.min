/**
 * 
 */
package org.nightlabs.jfire.dashboard;

import javax.ejb.Remote;

/**
 * @author abieber
 *
 */
@Remote
public interface DashboardManagerRemote {
	void initialise() throws Exception;
}
