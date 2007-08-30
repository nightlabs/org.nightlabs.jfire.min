package org.nightlabs.jfire.testsuite.trade;

import java.rmi.RemoteException;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.jdo.PersistenceManager;

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
public class JFireTradeTestSuiteManagerBean
extends BaseSessionBeanImpl
implements SessionBean
{
	
	private static final long serialVersionUID = 1L;

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 */
	public void checkDeliveryAndPayment(DeliveryDataID deliveryDataID, PaymentDataID paymentDataID, Stage deliveryFailureStage, Stage paymentFailureStage) {
		PersistenceManager pm = getPersistenceManager();
		DeliveryData deliveryData = (DeliveryData) pm.getObjectById(deliveryDataID);
		PaymentData paymentData = (PaymentData) pm.getObjectById(paymentDataID);
		
		Delivery delivery = deliveryData.getDelivery();
		Payment payment = paymentData.getPayment();
		
		if (deliveryFailureStage == null && paymentFailureStage == null) {
			// If there was no sabotage, everything should be ok
			if (delivery.isFailed() || payment.isFailed())
				throw new IllegalStateException("No failure stages given, but either delivery or payment stage failed.");				
		} else {
			// There was sabotage, so the overall states of delivery and payment should be failed/rolledBack.			
			if (deliveryFailureStage != null) {
//				if (! (delivery.isFailed() && delivery.isRolledBack() && payment.isFailed() && payment.isRolledBack()))
				if (delivery.isSuccessfulAndComplete() || payment.isSuccessfulAndComplete())
					throw new IllegalStateException("Delivery failed in stage " + deliveryFailureStage + " but: delivery.failed="+delivery.isFailed()+", delivery.rolledBack="+delivery.isRolledBack()+
					", payment.failed="+payment.isFailed()+", payment.rolledBack="+payment.isRolledBack());
				
				checkDeliveryResults(delivery, deliveryFailureStage);
			}
			if (paymentFailureStage != null) {
//				if (! (delivery.isFailed() && delivery.isRolledBack() && payment.isFailed() && payment.isRolledBack()))
				if (delivery.isSuccessfulAndComplete() || payment.isSuccessfulAndComplete())
					throw new IllegalStateException("Payment failed in stage " + paymentFailureStage + " but: delivery.failed="+delivery.isFailed()+", delivery.rolledBack="+delivery.isRolledBack()+
					", payment.failed="+payment.isFailed()+", payment.rolledBack="+payment.isRolledBack());
				
				checkPaymentResults(payment, paymentFailureStage);
			}
		}
	}
	
	private void checkDeliveryResults(Delivery delivery, Stage deliveryFailureStage) {
		for (Stage stage : deliveryFailureStage.before(false)) {
			DeliveryResult result = getDeliveryResult(delivery, stage);
			if (result != null && result.isFailed())
				throw new IllegalStateException("DeliveryResult of stage '" + stage + "' of delivery " + delivery.getPrimaryKey() + " is failed.");
		}
		
		for (Stage stage : deliveryFailureStage.after(true)) {
			DeliveryResult result = getDeliveryResult(delivery, stage);
			if (result != null && !result.isFailed())
				throw new IllegalStateException("DeliveryResult of stage '" + stage + "' of delivery " + delivery.getPrimaryKey() + " is not failed.");
		}		
	}
	
	private void checkPaymentResults(Payment payment, Stage paymentFailureStage) {
		for (Stage stage : paymentFailureStage.before(false)) {
			PaymentResult result = getPaymentResult(payment, stage);
			if (result != null && result.isFailed())
				throw new IllegalStateException("PaymentResult of stage '" + stage + "' of payment " + payment.getPrimaryKey() + " is failed.");
		}
		
		for (Stage stage : paymentFailureStage.after(true)) {
			PaymentResult result = getPaymentResult(payment, stage);
			if (result != null && !result.isFailed())
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
	
	/**
   * @ejb.create-method
   * @ejb.permission role-name="_Guest_"
   */
  public void ejbCreate() throws CreateException
  {
  }

  /**
   * @ejb.permission unchecked="true"
   */
  public void ejbRemove() throws EJBException, RemoteException
  {
  }

  public void setSessionContext(SessionContext sessionContext)
  throws EJBException, RemoteException
  {
          super.setSessionContext(sessionContext);
  }
  public void unsetSessionContext()
  {
          super.unsetSessionContext();
  }
	
	public void ejbActivate() throws EJBException, RemoteException {
	}

	public void ejbPassivate() throws EJBException, RemoteException {
	}
}
