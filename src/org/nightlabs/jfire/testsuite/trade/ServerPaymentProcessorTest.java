package org.nightlabs.jfire.testsuite.trade;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;

import org.nightlabs.jfire.accounting.pay.PaymentException;
import org.nightlabs.jfire.accounting.pay.PaymentResult;
import org.nightlabs.jfire.accounting.pay.ServerPaymentProcessor;
import org.nightlabs.jfire.accounting.pay.id.ServerPaymentProcessorID;
import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.jfire.transfer.Anchor;
import org.nightlabs.jfire.transfer.Stage;

/**
 * @author Tobias Langner <!-- tobias[dot]langner[at]nightlabs[dot]de -->
 * 
 * @jdo.persistence-capable
 *		identity-type="application"
 *		persistence-capable-superclass="org.nightlabs.jfire.accounting.pay.ServerPaymentProcessor"
 *		detachable="true"
 *
 * @jdo.inheritance strategy="superclass-table"
 */
public class ServerPaymentProcessorTest extends ServerPaymentProcessor {

	public static ServerPaymentProcessorTest getServerPaymentProcessorTest(PersistenceManager pm) {
		ServerPaymentProcessorTest serverPaymentProcessorTest;
		try {
			pm.getExtent(ServerPaymentProcessorTest.class);
			serverPaymentProcessorTest = (ServerPaymentProcessorTest) pm.getObjectById(ServerPaymentProcessorID.create(
					Organisation.DEVIL_ORGANISATION_ID, ServerPaymentProcessorTest.class.getName()));
		} catch (JDOObjectNotFoundException e) {
			serverPaymentProcessorTest = new ServerPaymentProcessorTest(Organisation.DEVIL_ORGANISATION_ID, ServerPaymentProcessorTest.class.getName());
			serverPaymentProcessorTest = (ServerPaymentProcessorTest) pm.makePersistent(serverPaymentProcessorTest);
		}

		return serverPaymentProcessorTest;
	}

	private static final long serialVersionUID = 1L;

	@Deprecated
	protected ServerPaymentProcessorTest() {

	}

	public ServerPaymentProcessorTest(String organisationID, String serverPaymentProcessorID) {
		super(organisationID, serverPaymentProcessorID);
	}

	@Override
	protected PaymentResult externalPayBegin(PayParams payParams) throws PaymentException {
		checkStatus(payParams, Stage.ServerBegin);
		return null;
	}

	@Override
	protected PaymentResult externalPayDoWork(PayParams payParams) throws PaymentException {
		checkStatus(payParams, Stage.ServerDoWork);
		return null;
	}
	
	@Override
	protected PaymentResult externalPayCommit(PayParams payParams) throws PaymentException {
		checkStatus(payParams, Stage.ServerEnd);
		return null;
	}

	@Override
	protected PaymentResult externalPayRollback(PayParams payParams) throws PaymentException {
		return null;
	}

	@Override
	public Anchor getAnchorOutside(PayParams deliverParams) {
		// TODO Auto-generated method stub
		return null;
	}
	
	private void checkStatus(PayParams payParams, Stage stage) {
		if (getPaymentData(payParams).getFailureStage() == stage)
			throw new RuntimeException("Sabotaged payment in stage " + stage.toString());
	}

	private PaymentDataTestCase getPaymentData(PayParams payParams) {
		if (payParams.paymentData instanceof PaymentDataTestCase)
			return (PaymentDataTestCase) payParams.paymentData;
		else
			throw new IllegalArgumentException("PaymentData;");
	}	
}
