package org.nightlabs.jfire.testsuite.trade;

import javax.security.auth.login.LoginException;

import org.nightlabs.jfire.accounting.pay.AbstractPaymentController;

public class ServerOnlyPaymentController extends AbstractPaymentController {

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
	public void verifyData() {
	}
}
