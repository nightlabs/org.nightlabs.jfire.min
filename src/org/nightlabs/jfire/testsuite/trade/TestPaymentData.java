package org.nightlabs.jfire.testsuite.trade;

import org.nightlabs.jfire.accounting.pay.Payment;
import org.nightlabs.jfire.accounting.pay.PaymentData;
import org.nightlabs.jfire.transfer.Stage;

public class TestPaymentData extends PaymentData {
	private Stage failureStage;

	public TestPaymentData(Payment payment, Stage failureStage) {
		super(payment);
		this.failureStage = failureStage;
	}
	
	public Stage getFailureStage() {
		return failureStage;
	}
}
