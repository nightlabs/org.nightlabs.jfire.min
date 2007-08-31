package org.nightlabs.jfire.testsuite.trade;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.nightlabs.jfire.accounting.pay.AbstractPaymentController;
import org.nightlabs.jfire.accounting.pay.PaymentData;
import org.nightlabs.jfire.accounting.pay.PaymentResult;

public class ServerOnlyPaymentController extends AbstractPaymentController {
	
	public ServerOnlyPaymentController(PaymentData paymentData) {
		setTransferDatas(Collections.singletonList(paymentData));
	}
	
	@Override
	protected boolean _clientBegin() {
		List<PaymentResult> paymentResults = new LinkedList<PaymentResult>();
		for (PaymentData data : getTransferDatas()) {
			PaymentResult result = new PaymentResult(
					PaymentResult.CODE_APPROVED_NO_EXTERNAL,
					(String)null,
					(Throwable)null);
			data.getPayment().setPayBeginClientResult(result);
			paymentResults.add(result);
		}
		setLastStageResults(paymentResults);
		return true;
	}

	@Override
	protected void _clientDoWork() {
		List<PaymentResult> paymentResults = new LinkedList<PaymentResult>();
		for (PaymentData data : getTransferDatas()) {
			PaymentResult result = new PaymentResult(
					PaymentResult.CODE_PAID_NO_EXTERNAL,
					(String)null,
					(Throwable)null);
			data.getPayment().setPayBeginClientResult(result);
			paymentResults.add(result);
		}
		setLastStageResults(paymentResults);
	}

	@Override
	protected void _clientEnd() {
		List<PaymentResult> paymentResults = new LinkedList<PaymentResult>();
		for (PaymentData data : getTransferDatas()) {
			PaymentResult result = new PaymentResult(
					PaymentResult.CODE_COMMITTED_NO_EXTERNAL,
					(String)null,
					(Throwable)null);
			data.getPayment().setPayBeginClientResult(result);
			paymentResults.add(result);
		}
		setLastStageResults(paymentResults);
	}

	@Override
	public void verifyData() {
	}
}