package org.nightlabs.jfire.testsuite.trade;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;

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
			serverDeliveryProcessorTest = new ServerDeliveryProcessorTest(Organisation.DEVIL_ORGANISATION_ID, ServerDeliveryProcessorTest.class.getName());
			serverDeliveryProcessorTest = (ServerDeliveryProcessorTest) pm.makePersistent(serverDeliveryProcessorTest);
		}

		return serverDeliveryProcessorTest;
	}

	private static final long serialVersionUID = 1L;

	@Deprecated
	protected ServerDeliveryProcessorTest() {

	}

	public ServerDeliveryProcessorTest(String organisationID, String serverDeliveryProcessorID) {
		super(organisationID, serverDeliveryProcessorID);
	}

	@Override
	protected DeliveryResult externalDeliverBegin(DeliverParams deliverParams) throws DeliveryException {
		checkStatus(deliverParams, Stage.ServerBegin);
		return null;
	}

	@Override
	protected DeliveryResult externalDeliverDoWork(DeliverParams deliverParams) throws DeliveryException {
		checkStatus(deliverParams, Stage.ServerDoWork);
		return null;
	}

	@Override
	protected DeliveryResult externalDeliverCommit(DeliverParams deliverParams) throws DeliveryException {
		checkStatus(deliverParams, Stage.ServerEnd);
		return null;
	}

	@Override
	protected DeliveryResult externalDeliverRollback(DeliverParams deliverParams) throws DeliveryException {
		return null;
	}

	@Override
	public Anchor getAnchorOutside(DeliverParams deliverParams) {
		// TODO Auto-generated method stub
		return null;
	}

	private void checkStatus(DeliverParams deliverParams, Stage stage) {
		if (getDeliveryData(deliverParams).getFailureStage() == stage)
			throw new RuntimeException("Sabotaged delivery in stage " + stage.toString());
	}

	private DeliveryDataTestCase getDeliveryData(DeliverParams deliverParams) {
		if (deliverParams.deliveryData instanceof DeliveryDataTestCase)
			return (DeliveryDataTestCase) deliverParams.deliveryData;
		else
			throw new IllegalArgumentException("DeliveryData;");
	}
}
