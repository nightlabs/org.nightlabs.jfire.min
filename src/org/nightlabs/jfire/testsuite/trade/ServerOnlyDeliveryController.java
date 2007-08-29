package org.nightlabs.jfire.testsuite.trade;

import java.util.Collections;

import org.nightlabs.jfire.store.deliver.AbstractDeliveryController;
import org.nightlabs.jfire.store.deliver.DeliveryData;
import org.nightlabs.jfire.store.deliver.DeliveryResult;

public class ServerOnlyDeliveryController extends AbstractDeliveryController {
	
	public ServerOnlyDeliveryController(DeliveryData deliveryData) {
		setTransferDatas(Collections.singletonList(deliveryData));
	}

	@Override
	protected boolean _clientBegin() {
		for (DeliveryData data : getTransferDatas())
			data.getDelivery().setDeliverBeginClientResult(new DeliveryResult(
					DeliveryResult.CODE_APPROVED_NO_EXTERNAL,
					(String) null,
					(Throwable) null
			));
		return true;
	}

	@Override
	protected void _clientDoWork() {
		for (DeliveryData data : getTransferDatas())
			data.getDelivery().setDeliverBeginClientResult(new DeliveryResult(
					DeliveryResult.CODE_DELIVERED_NO_EXTERNAL,
					(String) null,
					(Throwable) null
			));
	}

	@Override
	protected void _clientEnd() {
		for (DeliveryData data : getTransferDatas())
			data.getDelivery().setDeliverBeginClientResult(new DeliveryResult(
					DeliveryResult.CODE_COMMITTED_NO_EXTERNAL,
					(String) null,
					(Throwable) null
			));
	}

	@Override
	public boolean isRollbackRequired() {
		return false;
	}

	@Override
	public void verifyData() {
	}
}
