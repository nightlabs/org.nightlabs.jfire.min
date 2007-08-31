package org.nightlabs.jfire.testsuite.trade;

import java.rmi.RemoteException;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.jdo.PersistenceManager;

import org.apache.log4j.Logger;
import org.nightlabs.jfire.accounting.pay.Payment;
import org.nightlabs.jfire.accounting.pay.PaymentData;
import org.nightlabs.jfire.accounting.pay.PaymentResult;
import org.nightlabs.jfire.accounting.pay.id.PaymentDataID;
import org.nightlabs.jfire.base.BaseSessionBeanImpl;
import org.nightlabs.jfire.store.deliver.Delivery;
import org.nightlabs.jfire.store.deliver.DeliveryData;
import org.nightlabs.jfire.store.deliver.DeliveryResult;
import org.nightlabs.jfire.store.deliver.id.DeliveryDataID;
import org.nightlabs.jfire.transfer.Stage;

/**
 * @ejb.bean name="jfire/ejb/JFireTestSuite/JFireTradeTestSuiteManager"	
 *					 jndi-name="jfire/ejb/JFireTestSuite/JFireTradeTestSuiteManager"
 *					 type="Stateless" 
 *					 transaction-type="Container"
 *
 * @ejb.util generate="physical"
 */
public class JFireTradeTestSuiteManagerBean extends BaseSessionBeanImpl implements SessionBean {
	private static final Logger logger = Logger.getLogger(JFireTradeTestSuiteManagerBean.class);

	private static final long serialVersionUID = 1L;

	/**
	 * @throws DeliveryAndPaymentVerificationException 
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 */
	public void checkDeliveryAndPayment(DeliveryDataID deliveryDataID, PaymentDataID paymentDataID, Stage deliveryFailureStage,
			Stage paymentFailureStage) throws DeliveryAndPaymentVerificationException {
		PersistenceManager pm = getPersistenceManager();
		DeliveryData deliveryData = (DeliveryData) pm.getObjectById(deliveryDataID);
		PaymentData paymentData = (PaymentData) pm.getObjectById(paymentDataID);

		Delivery delivery = deliveryData.getDelivery();
		Payment payment = paymentData.getPayment();
		
		logger.debug("DeliveryFailureStage="+deliveryFailureStage+", PaymentFailureStage="+paymentFailureStage);
		logger.debug("Delivery: failed=" + delivery.isFailed() + ",rolledBack=" + delivery.isRolledBack() + ",pending=" + delivery.isPending()
				+ ",extPayDone=" + delivery.isExternalDeliveryDone() + ",succAndComp=" + delivery.isSuccessfulAndComplete());
		logger.debug("Payment: failed=" + payment.isFailed() + ",rolledBack=" + payment.isRolledBack() + ",pending=" + payment.isPending()
				+ ",extPayDone=" + payment.isExternalPaymentDone() + ",succAndComp=" + payment.isSuccessfulAndComplete());
		logger.debug("");

		if (deliveryFailureStage == null && paymentFailureStage == null) {
			// If there was no sabotage, everything should be ok
			if (delivery.isFailed() || payment.isFailed())
				throw new DeliveryAndPaymentVerificationException("No failure stages given, but either delivery or payment stage failed.");
		} else {

			// There was sabotage, so the overall states of delivery and payment should be failed/rolledBack.			
			if (deliveryFailureStage != null) {
				//				if (! (delivery.isFailed() && delivery.isRolledBack() && payment.isFailed() && payment.isRolledBack()))
				if (delivery.isSuccessfulAndComplete() || payment.isSuccessfulAndComplete())
					throw new DeliveryAndPaymentVerificationException("Delivery failed in stage " + deliveryFailureStage + " but: delivery.failed=" + delivery.isFailed()
							+ ", delivery.rolledBack=" + delivery.isRolledBack() + ", payment.failed=" + payment.isFailed() + ", payment.rolledBack="
							+ payment.isRolledBack());

				checkDeliveryResults(delivery, deliveryFailureStage);
			}
			if (paymentFailureStage != null) {
				//				if (! (delivery.isFailed() && delivery.isRolledBack() && payment.isFailed() && payment.isRolledBack()))
				if (delivery.isSuccessfulAndComplete() || payment.isSuccessfulAndComplete())
					throw new DeliveryAndPaymentVerificationException("Payment failed in stage " + paymentFailureStage + " but: delivery.failed=" + delivery.isFailed()
							+ ", delivery.rolledBack=" + delivery.isRolledBack() + ", payment.failed=" + payment.isFailed() + ", payment.rolledBack="
							+ payment.isRolledBack());

				checkPaymentResults(payment, paymentFailureStage);
			}

			//			if (paymentFailureStage.isAfter(Stage.ServerDoWork) && !payment.isRolledBack())
			//				throw new P
		}
	}

	private void checkDeliveryResults(Delivery delivery, Stage deliveryFailureStage) throws DeliveryAndPaymentVerificationException {
		for (Stage stage : deliveryFailureStage.before(false)) {
			DeliveryResult result = getDeliveryResult(delivery, stage);
			if (result != null && result.isFailed())
				throw new DeliveryAndPaymentVerificationException("DeliveryResult of stage '" + stage + "' of delivery " + delivery.getPrimaryKey() + " is failed.");
		}

		for (Stage stage : deliveryFailureStage.after(true)) {
			DeliveryResult result = getDeliveryResult(delivery, stage);
			if (result != null && !result.isFailed())
				throw new DeliveryAndPaymentVerificationException("DeliveryResult of stage '" + stage + "' of delivery " + delivery.getPrimaryKey() + " is not failed.");
		}
	}

	private void checkPaymentResults(Payment payment, Stage paymentFailureStage) throws DeliveryAndPaymentVerificationException {
		for (Stage stage : paymentFailureStage.before(false)) {
			PaymentResult result = getPaymentResult(payment, stage);
			if (result != null && result.isFailed())
				throw new DeliveryAndPaymentVerificationException("PaymentResult of stage '" + stage + "' of payment " + payment.getPrimaryKey() + " is failed.");
		}

		for (Stage stage : paymentFailureStage.after(true)) {
			PaymentResult result = getPaymentResult(payment, stage);
			if (result != null && !result.isFailed())
				throw new DeliveryAndPaymentVerificationException("PaymentResult of stage '" + stage + "' of payment " + payment.getPrimaryKey() + " is not failed.");
		}
	}

	private PaymentResult getPaymentResult(Payment payment, Stage stage) {
		switch (stage) {
		case ClientBegin:
			return payment.getPayBeginClientResult();
		case ServerBegin:
			return payment.getPayBeginServerResult();
		case ClientDoWork:
			return payment.getPayDoWorkClientResult();
		case ServerDoWork:
			return payment.getPayDoWorkServerResult();
		case ClientEnd:
			return payment.getPayEndClientResult();
		case ServerEnd:
			return payment.getPayEndServerResult();
		}
		return null;
	}

	private DeliveryResult getDeliveryResult(Delivery delivery, Stage stage) {
		switch (stage) {
		case ClientBegin:
			return delivery.getDeliverBeginClientResult();
		case ServerBegin:
			return delivery.getDeliverBeginServerResult();
		case ClientDoWork:
			return delivery.getDeliverDoWorkClientResult();
		case ServerDoWork:
			return delivery.getDeliverDoWorkServerResult();
		case ClientEnd:
			return delivery.getDeliverEndClientResult();
		case ServerEnd:
			return delivery.getDeliverEndServerResult();
		}
		return null;
	}

	/**
	 * @ejb.create-method
	 * @ejb.permission role-name="_Guest_"
	 */
	public void ejbCreate() throws CreateException {
	}

	/**
	 * @ejb.permission unchecked="true"
	 */
	public void ejbRemove() throws EJBException, RemoteException {
	}

	public void setSessionContext(SessionContext sessionContext) throws EJBException, RemoteException {
		super.setSessionContext(sessionContext);
	}

	public void unsetSessionContext() {
		super.unsetSessionContext();
	}

	public void ejbActivate() throws EJBException, RemoteException {
	}

	public void ejbPassivate() throws EJBException, RemoteException {
	}
}
