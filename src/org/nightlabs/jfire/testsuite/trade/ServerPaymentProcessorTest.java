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
			serverPaymentProcessorTest = new ServerPaymentProcessorTest(OBJECT_ID.organisationID, OBJECT_ID.serverPaymentProcessorID);
			serverPaymentProcessorTest = pm.makePersistent(serverPaymentProcessorTest);
		}

		return serverPaymentProcessorTest;
	}

	private static final long serialVersionUID = 2L;
	
	public static ServerPaymentProcessorID OBJECT_ID =
		ServerPaymentProcessorID.create(Organisation.DEVIL_ORGANISATION_ID, ServerPaymentProcessorTest.class.getName());

	@Deprecated
	protected ServerPaymentProcessorTest() {

	}

	protected ServerPaymentProcessorTest(String organisationID, String serverPaymentProcessorID) {
		super(organisationID, serverPaymentProcessorID);
	}

	@Override
	protected PaymentResult externalPayBegin(PayParams payParams) throws PaymentException {
		return getPaymentResult(payParams, Stage.ServerBegin);
	}

	@Override
	protected PaymentResult externalPayDoWork(PayParams payParams) throws PaymentException {
		return getPaymentResult(payParams, Stage.ServerDoWork);
	}
	
	@Override
	protected PaymentResult externalPayCommit(PayParams payParams) throws PaymentException {
		return getPaymentResult(payParams, Stage.ServerEnd);
	}

	@Override
	protected PaymentResult externalPayRollback(PayParams payParams) throws PaymentException {
		return new PaymentResult(PaymentResult.CODE_ROLLED_BACK_WITH_EXTERNAL, null, null);
	}

	@Override
	public Anchor getAnchorOutside(PayParams payParams) {
		return getAccountOutside(payParams, "anchorOutside.test");
	}
	
	private PaymentResult getPaymentResult(PayParams payParams, Stage stage) {
		if (getPaymentData(payParams).getFailureStage() == stage) {
			return new PaymentResult(PaymentResult.CODE_FAILED, "Delivery sabotaged in stage " + stage, null);
		} else {
			switch (stage) {
			case ServerBegin:
				return new PaymentResult(PaymentResult.CODE_APPROVED_WITH_EXTERNAL, null, null);
			case ServerDoWork:
				return new PaymentResult(PaymentResult.CODE_PAID_WITH_EXTERNAL, null, null);
			case ServerEnd:
				return new PaymentResult(PaymentResult.CODE_COMMITTED_WITH_EXTERNAL, null, null);
			}
		}
		return null;
	}
	
	private PaymentDataTestCase getPaymentData(PayParams payParams) {
		if (payParams.paymentData instanceof PaymentDataTestCase)
			return (PaymentDataTestCase) payParams.paymentData;
		else
			throw new IllegalArgumentException("PaymentData;");
	}	
}
