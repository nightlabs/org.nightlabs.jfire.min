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
		for (PaymentData data : getTransferDatas())
			data.getPayment().setPayBeginClientResult(new PaymentResult(
				PaymentResult.CODE_APPROVED_NO_EXTERNAL,
				(String)null,
				(Throwable)null));
		
		return true;
	}

	@Override
	protected void _clientDoWork() {
		for (PaymentData data : getTransferDatas())
			data.getPayment().setPayBeginClientResult(new PaymentResult(
				PaymentResult.CODE_PAID_NO_EXTERNAL,
				(String)null,
				(Throwable)null));
	}

	@Override
	protected void _clientEnd() {
		for (PaymentData data : getTransferDatas())
			data.getPayment().setPayBeginClientResult(new PaymentResult(
				PaymentResult.CODE_COMMITTED_NO_EXTERNAL,
				(String)null,
				(Throwable)null));
	}

	@Override
	public void verifyData() {
	}
}
