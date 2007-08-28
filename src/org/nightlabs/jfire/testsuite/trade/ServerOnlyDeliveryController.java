package org.nightlabs.jfire.testsuite.trade;

import java.util.Collections;

import org.nightlabs.jfire.store.deliver.AbstractDeliveryController;
import org.nightlabs.jfire.store.deliver.DeliveryData;

public class ServerOnlyDeliveryController extends AbstractDeliveryController {
	
	public ServerOnlyDeliveryController(DeliveryData deliveryData) {
		setTransferDatas(Collections.singletonList(deliveryData));
	}

	@Override
	protected boolean _clientBegin() {
		return true;
	}

	@Override
	protected void _clientDoWork() {
	}

	@Override
	protected void _clientEnd() {
	}

	@Override
	public boolean isRollbackRequired() {
		return false;
	}

	@Override
	public void verifyData() {
	}
}
