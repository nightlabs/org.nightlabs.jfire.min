package org.nightlabs.initialisejdo;

import javax.ejb.Remote;

@Remote
public interface InitialiseJDORemote {

	void initialise() throws Exception;

}
