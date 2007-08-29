package org.nightlabs.jfire.testsuite.trade;

import org.nightlabs.jfire.accounting.pay.Payment;
import org.nightlabs.jfire.accounting.pay.PaymentData;
import org.nightlabs.jfire.transfer.Stage;

/**
 * @author Tobias Langner <!-- tobias[dot]langner[at]nightlabs[dot]de -->
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		persistence-capable-superclass="org.nightlabs.jfire.accounting.pay.PaymentData"
 *		detachable="true"
 *		table="JFireTestSuite_PaymentDataTestCase"
 *
 * @jdo.inheritance strategy="new-table"
 */
public class PaymentDataTestCase extends PaymentData {
	private static final long serialVersionUID = 1L;
	
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
