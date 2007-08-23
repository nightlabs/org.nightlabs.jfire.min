package org.nightlabs.jfire.testsuite.trade;

import javax.security.auth.login.LoginException;

import org.nightlabs.jfire.store.deliver.AbstractDeliveryController;

public class ServerOnlyDeliveryController extends AbstractDeliveryController {

	@Override
	public boolean clientBegin() throws LoginException {
		return true;
	}

	@Override
	public void clientDoWork() throws LoginException {
	}

	@Override
	public void clientEnd() throws LoginException {
	}

	@Override
	public boolean isRollbackRequired() {
		return false;
	}

	@Override
	public void verifyData() {
	}
}
