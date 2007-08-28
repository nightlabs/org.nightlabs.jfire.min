package org.nightlabs.jfire.testsuite.trade;

import org.nightlabs.jfire.accounting.pay.Payment;
import org.nightlabs.jfire.accounting.pay.PaymentData;
import org.nightlabs.jfire.transfer.Stage;

public class PaymentDataTestCase extends PaymentData {
	private Stage failureStage;

	public PaymentDataTestCase(Payment payment, Stage failureStage) {
		super(payment);
		this.failureStage = failureStage;
	}
	
	public Stage getFailureStage() {
		return failureStage;
	}
	
	public void setFailureStage(Stage failureStage) {
		this.failureStage = failureStage;
	}
}
