package org.nightlabs.jfire.testsuite.trade;

import org.nightlabs.jfire.accounting.pay.PaymentException;
import org.nightlabs.jfire.accounting.pay.PaymentResult;
import org.nightlabs.jfire.accounting.pay.ServerPaymentProcessor;
import org.nightlabs.jfire.transfer.Anchor;

public class TestServerPaymentProcessor extends ServerPaymentProcessor {

	@Override
	protected PaymentResult externalPayBegin(PayParams payParams) throws PaymentException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected PaymentResult externalPayCommit(PayParams payParams) throws PaymentException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected PaymentResult externalPayDoWork(PayParams payParams) throws PaymentException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected PaymentResult externalPayRollback(PayParams payParams) throws PaymentException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Anchor getAnchorOutside(PayParams payParams) {
		// TODO Auto-generated method stub
		return null;
	}

}
