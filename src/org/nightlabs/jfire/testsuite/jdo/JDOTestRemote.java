package org.nightlabs.jfire.testsuite.jdo;

import javax.ejb.Remote;

@Remote
public interface JDOTestRemote {

	void createArrayListFromQueryResult();

	void createHashSetFromQueryResult();

}