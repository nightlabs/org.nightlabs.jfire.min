package org.nightlabs.jfire.testsuite.trade;

import org.nightlabs.jfire.store.deliver.Delivery;
import org.nightlabs.jfire.store.deliver.DeliveryData;
import org.nightlabs.jfire.transfer.Stage;

public class TestDeliveryData extends DeliveryData {
	private Stage failureStage;

	public TestDeliveryData(Delivery delivery, Stage failureStage) {
		super(delivery);
		this.failureStage = failureStage;
	}
	
	public Stage getFailureStage() {
		return failureStage;
	}
}
