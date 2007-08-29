package org.nightlabs.jfire.testsuite.trade;

import javax.jdo.PersistenceManager;

import org.nightlabs.jfire.accounting.pay.Payment;
import org.nightlabs.jfire.accounting.pay.PaymentResult;
import org.nightlabs.jfire.accounting.pay.id.PaymentDataID;
import org.nightlabs.jfire.base.BaseSessionBeanImpl;
import org.nightlabs.jfire.store.deliver.Delivery;
import org.nightlabs.jfire.store.deliver.DeliveryResult;
import org.nightlabs.jfire.store.deliver.id.DeliveryDataID;
import org.nightlabs.jfire.transfer.Stage;

/**
 * @ejb.bean name="jfire/ejb/JFireTestSuite/JFireTradeTestSuiteManager"	
 *					 jndi-name="jfire/ejb/JFireTestSuite/JFireTradeTestSuiteManager"
 *					 type="Stateless" 
 *					 transaction-type="Container"
 *
 * @ejb.util generate = "physical"
 */
public class JFireTradeTestSuiteManager extends BaseSessionBeanImpl {
	public void checkDeliveryAndPayment(DeliveryDataID deliveryDataID, PaymentDataID paymentDataID, Stage deliveryFailureStage, Stage paymentFailureStage) {
		PersistenceManager pm = getPersistenceManager();
		Delivery delivery = (Delivery) pm.getObjectById(deliveryDataID);
		Payment payment = (Payment) pm.getObjectById(paymentDataID);
		
		if (deliveryFailureStage == null && paymentFailureStage == null) {
			// If there was no sabotage, everything should be ok
			if (! (delivery.isSuccessfulAndComplete() && payment.isSuccessfulAndComplete()))
				throw new IllegalStateException("No failure stages given, but either delivery or payment stage failed.");				
		} else {
			// There was sabotage, so the overall states of delivery and payment should be failed/rolledBack.			
			if (deliveryFailureStage != null) {
				if (! (delivery.isFailed() && delivery.isRolledBack() && payment.isFailed() && payment.isRolledBack()))
					throw new IllegalStateException("Delivery failed in stage " + deliveryFailureStage + " but: delivery.failed="+delivery.isFailed()+", delivery.rolledBack="+delivery.isRolledBack()+
					", payment.failed="+payment.isFailed()+", payment.rolledBack="+payment.isRolledBack());
			}
			if (paymentFailureStage != null) {
				if (! (delivery.isFailed() && delivery.isRolledBack() && payment.isFailed() && payment.isRolledBack()))
					throw new IllegalStateException("Payment failed in stage " + paymentFailureStage + " but: delivery.failed="+delivery.isFailed()+", delivery.rolledBack="+delivery.isRolledBack()+
					", payment.failed="+payment.isFailed()+", payment.rolledBack="+payment.isRolledBack());
			}
			
			// All delivery/payment results before the failure should be ok			
			// All delivery/payment results after the failure should be failed too
			checkDeliveryResults(delivery, deliveryFailureStage);
			checkPaymentResults(payment, paymentFailureStage);
		}
	}
	
	private void checkDeliveryResults(Delivery delivery, Stage deliveryFailureStage) {
		for (Stage stage : deliveryFailureStage.before()) {
			DeliveryResult result = getDeliveryResult(delivery, stage);
			if (result.isFailed())
				throw new IllegalStateException("DeliveryResult of stage '" + stage + "' of delivery " + delivery.getPrimaryKey() + " is failed.");
		}
		
		for (Stage stage : deliveryFailureStage.after()) {
			DeliveryResult result = getDeliveryResult(delivery, stage);
			if (!result.isFailed())
				throw new IllegalStateException("DeliveryResult of stage '" + stage + "' of delivery " + delivery.getPrimaryKey() + " is not failed.");
		}		
	}
	
	private void checkPaymentResults(Payment payment, Stage paymentFailureStage) {
		for (Stage stage : paymentFailureStage.before()) {
			PaymentResult result = getPaymentResult(payment, stage);
			if (result.isFailed())
				throw new IllegalStateException("PaymentResult of stage '" + stage + "' of payment " + payment.getPrimaryKey() + " is failed.");
		}
		
		for (Stage stage : paymentFailureStage.after()) {
			PaymentResult result = getPaymentResult(payment, stage);
			if (!result.isFailed())
				throw new IllegalStateException("PaymentResult of stage '" + stage + "' of payment " + payment.getPrimaryKey() + " is not failed.");
		}
	}
	
	private PaymentResult getPaymentResult(Payment payment, Stage stage) {
		switch (stage) {
		case ClientBegin: return payment.getPayBeginClientResult();			
		case ServerBegin: return payment.getPayBeginServerResult();
		case ClientDoWork: return payment.getPayDoWorkClientResult();
		case ServerDoWork: return payment.getPayDoWorkServerResult();
		case ClientEnd: return payment.getPayEndClientResult();
		case ServerEnd: return payment.getPayEndServerResult();
		}
		return null;
	}
	
	private DeliveryResult getDeliveryResult(Delivery delivery, Stage stage) {
		switch (stage) {
		case ClientBegin: return delivery.getDeliverBeginClientResult();			
		case ServerBegin: return delivery.getDeliverBeginServerResult();
		case ClientDoWork: return delivery.getDeliverDoWorkClientResult();
		case ServerDoWork: return delivery.getDeliverDoWorkServerResult();
		case ClientEnd: return delivery.getDeliverEndClientResult();
		case ServerEnd: return delivery.getDeliverEndServerResult();
		}
		return null;
	}
}
