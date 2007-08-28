package org.nightlabs.jfire.testsuite.trade;

import java.util.Collections;

import org.nightlabs.jfire.accounting.pay.AbstractPaymentController;
import org.nightlabs.jfire.accounting.pay.PaymentData;
import org.nightlabs.jfire.accounting.pay.PaymentResult;

public class ServerOnlyPaymentController extends AbstractPaymentController {
	
	public ServerOnlyPaymentController(PaymentData paymentData) {
		setTransferDatas(Collections.singletonList(paymentData));
	}

	@Override
	protected boolean _clientBegin() {
		for (PaymentData data : getTransferDatas()) {
			// TODO set pay begin results
		}
		return true;
	}

	@Override
	protected void _clientDoWork() {
	}

	@Override
	protected void _clientEnd() {
	}

	@Override
	public void verifyData() {
	}
}
