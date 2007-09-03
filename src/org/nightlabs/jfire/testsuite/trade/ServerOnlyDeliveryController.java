package org.nightlabs.jfire.testsuite.trade;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.nightlabs.jfire.store.deliver.AbstractDeliveryController;
import org.nightlabs.jfire.store.deliver.DeliveryData;
import org.nightlabs.jfire.store.deliver.DeliveryResult;

public class ServerOnlyDeliveryController extends AbstractDeliveryController {

	public ServerOnlyDeliveryController(DeliveryData deliveryData) {
		super(Collections.singletonList(deliveryData));
	}

	@Override
	protected boolean _clientBegin() {
		List<DeliveryResult> deliveryResults = new LinkedList<DeliveryResult>();
		for (DeliveryData data : getTransferDatas()) {
			DeliveryResult result = new DeliveryResult(
					DeliveryResult.CODE_APPROVED_NO_EXTERNAL,
					(String) null,
					(Throwable) null
			);
			data.getDelivery().setDeliverBeginClientResult(result);
			deliveryResults.add(result);
		}
		setLastStageResults(deliveryResults);
		return true;
	}

	@Override
	protected void _clientDoWork() {
		List<DeliveryResult> deliveryResults = new LinkedList<DeliveryResult>();
		for (DeliveryData data : getTransferDatas()) {
			DeliveryResult result = new DeliveryResult(
					DeliveryResult.CODE_DELIVERED_NO_EXTERNAL,
					(String) null,
					(Throwable) null
			);
			data.getDelivery().setDeliverBeginClientResult(result);
			deliveryResults.add(result);
		}
		setLastStageResults(deliveryResults);
	}

	@Override
	protected void _clientEnd() {
		List<DeliveryResult> deliveryResults = new LinkedList<DeliveryResult>();
		for (DeliveryData data : getTransferDatas()) {
			DeliveryResult result = new DeliveryResult(
					DeliveryResult.CODE_COMMITTED_NO_EXTERNAL,
					(String) null,
					(Throwable) null
			);
			data.getDelivery().setDeliverBeginClientResult(result);
			deliveryResults.add(result);
		}
		setLastStageResults(deliveryResults);
	}

	@Override
	public boolean isRollbackRequired() {
		return false;
	}

	@Override
	public void verifyData() {
	}
}
