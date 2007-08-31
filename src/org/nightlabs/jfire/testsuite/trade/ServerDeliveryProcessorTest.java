package org.nightlabs.jfire.testsuite.trade;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;

import org.nightlabs.jfire.accounting.pay.PaymentResult;
import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.jfire.store.deliver.DeliveryException;
import org.nightlabs.jfire.store.deliver.DeliveryResult;
import org.nightlabs.jfire.store.deliver.ServerDeliveryProcessor;
import org.nightlabs.jfire.store.deliver.id.ServerDeliveryProcessorID;
import org.nightlabs.jfire.transfer.Anchor;
import org.nightlabs.jfire.transfer.Stage;

/**
 * @author Tobias Langner <!-- tobias[dot]langner[at]nightlabs[dot]de -->
 * 
 * @jdo.persistence-capable
 *		identity-type="application"
 *		persistence-capable-superclass="org.nightlabs.jfire.store.deliver.ServerDeliveryProcessor"
 *		detachable="true"
 *
 * @jdo.inheritance strategy="superclass-table"
 */
class ServerDeliveryProcessorTest extends ServerDeliveryProcessor {

	public static ServerDeliveryProcessorTest getServerDeliveryProcessorTest(PersistenceManager pm) {
		ServerDeliveryProcessorTest serverDeliveryProcessorTest;
		try {
			pm.getExtent(ServerDeliveryProcessorTest.class);
			serverDeliveryProcessorTest = (ServerDeliveryProcessorTest) pm.getObjectById(ServerDeliveryProcessorID.create(
					Organisation.DEVIL_ORGANISATION_ID, ServerDeliveryProcessorTest.class.getName()));
		} catch (JDOObjectNotFoundException e) {
			serverDeliveryProcessorTest = new ServerDeliveryProcessorTest(OBJECT_ID.organisationID, OBJECT_ID.serverDeliveryProcessorID);
			serverDeliveryProcessorTest = (ServerDeliveryProcessorTest) pm.makePersistent(serverDeliveryProcessorTest);
		}

		return serverDeliveryProcessorTest;
	}
	
	private static final long serialVersionUID = 2L;
	
	public static final ServerDeliveryProcessorID OBJECT_ID =
		ServerDeliveryProcessorID.create(Organisation.DEVIL_ORGANISATION_ID, ServerDeliveryProcessorTest.class.getName());

	@Deprecated
	protected ServerDeliveryProcessorTest() {

	}

	protected ServerDeliveryProcessorTest(String organisationID, String serverDeliveryProcessorID) {
		super(organisationID, serverDeliveryProcessorID);
	}

	@Override
	protected DeliveryResult externalDeliverBegin(DeliverParams deliverParams) throws DeliveryException {
		return getDeliveryResult(deliverParams, Stage.ServerBegin);
	}

	@Override
	protected DeliveryResult externalDeliverDoWork(DeliverParams deliverParams) throws DeliveryException {
		return getDeliveryResult(deliverParams, Stage.ServerDoWork);
	}

	@Override
	protected DeliveryResult externalDeliverCommit(DeliverParams deliverParams) throws DeliveryException {
		return getDeliveryResult(deliverParams, Stage.ServerEnd);
	}

	@Override
	protected DeliveryResult externalDeliverRollback(DeliverParams deliverParams) throws DeliveryException {
		return null;
	}

	@Override
	public Anchor getAnchorOutside(DeliverParams deliverParams) {
		return getRepositoryOutside(deliverParams, "anchorOutside.test");
	}

	private DeliveryResult getDeliveryResult(DeliverParams deliverParams, Stage stage) {
		if (getDeliveryData(deliverParams).getFailureStage() == stage) {
			return new DeliveryResult(DeliveryResult.CODE_FAILED, "Delivery sabotaged in stage " + stage, null);
		} else {
			switch (stage) {
			case ServerBegin:
				return new DeliveryResult(DeliveryResult.CODE_APPROVED_WITH_EXTERNAL, null, null);
			case ServerDoWork:
				return new DeliveryResult(DeliveryResult.CODE_DELIVERED_WITH_EXTERNAL, null, null);
			case ServerEnd:
				return new DeliveryResult(DeliveryResult.CODE_COMMITTED_WITH_EXTERNAL, null, null);
			}
		}
		return null;
	}

	private DeliveryDataTestCase getDeliveryData(DeliverParams deliverParams) {
		if (deliverParams.deliveryData instanceof DeliveryDataTestCase)
			return (DeliveryDataTestCase) deliverParams.deliveryData;
		else
			throw new IllegalArgumentException("DeliveryData;");
	}
}
