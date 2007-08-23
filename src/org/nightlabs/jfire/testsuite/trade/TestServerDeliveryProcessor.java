package org.nightlabs.jfire.testsuite.trade;

import org.nightlabs.jfire.store.deliver.DeliveryException;
import org.nightlabs.jfire.store.deliver.DeliveryResult;
import org.nightlabs.jfire.store.deliver.ServerDeliveryProcessor;
import org.nightlabs.jfire.transfer.Anchor;

public class TestServerDeliveryProcessor extends ServerDeliveryProcessor {

	@Override
	protected DeliveryResult externalDeliverBegin(DeliverParams deliverParams) throws DeliveryException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected DeliveryResult externalDeliverCommit(DeliverParams deliverParams) throws DeliveryException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected DeliveryResult externalDeliverDoWork(DeliverParams deliverParams) throws DeliveryException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected DeliveryResult externalDeliverRollback(DeliverParams deliverParams) throws DeliveryException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Anchor getAnchorOutside(DeliverParams deliverParams) {
		// TODO Auto-generated method stub
		return null;
	}

}
