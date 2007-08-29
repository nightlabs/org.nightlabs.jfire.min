package org.nightlabs.jfire.testsuite.trade;

import org.nightlabs.jfire.store.deliver.Delivery;
import org.nightlabs.jfire.store.deliver.DeliveryData;
import org.nightlabs.jfire.transfer.Stage;

/**
 * @author Tobias Langner <!-- tobias[dot]langner[at]nightlabs[dot]de -->
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		persistence-capable-superclass="org.nightlabs.jfire.store.deliver.DeliveryData"
 *		detachable="true"
 *		table="JFireTestSuite_DeliveryDataDataTestCase"
 *
 * @jdo.inheritance strategy="new-table"
 */
public class DeliveryDataTestCase extends DeliveryData {
	private static final long serialVersionUID = 1L;
	
	private Stage failureStage;

	public DeliveryDataTestCase(Delivery delivery, Stage failureStage) {
		super(delivery);
		this.failureStage = failureStage;
	}
	
	public Stage getFailureStage() {
		return failureStage;
	}
}
