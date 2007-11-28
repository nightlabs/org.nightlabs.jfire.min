package org.nightlabs.jfire.testsuite.trade;

import javax.jdo.PersistenceManager;

import junit.framework.TestCase;

import org.nightlabs.jfire.testsuite.TestSuite;

public class JFireTradeTestSuite extends TestSuite {
	public JFireTradeTestSuite(Class<? extends TestCase>... classes) {
		super(classes);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.nightlabs.jfire.testsuite.TestSuite#canRunTests(javax.jdo.PersistenceManager)
	 */
	@Override
	public String canRunTests(PersistenceManager pm) throws Exception {
		return "Temporarily deactivated!";
//		boolean init = false;
//		ServerDeliveryProcessorTest deliveryProcessorTest;
//		try {
//			deliveryProcessorTest = (ServerDeliveryProcessorTest) pm.getObjectById(ServerDeliveryProcessorTest.OBJECT_ID);
//		} catch (JDOObjectNotFoundException e) {
//			init = true;		
//		}
//		
//		if (!init)
//			return null;
//		
//		deliveryProcessorTest = ServerDeliveryProcessorTest.getServerDeliveryProcessorTest(pm);		
//		ModeOfDelivery modManual = (ModeOfDelivery) pm.getObjectById(ModeOfDeliveryConst.MODE_OF_DELIVERY_ID_MANUAL);
//		deliveryProcessorTest.addModeOfDelivery(modManual);
//		deliveryProcessorTest.getName().setText(Locale.ENGLISH.getLanguage(), "TestCase DeliveryProcessor");
//		
//		ServerPaymentProcessorTest paymentProcessorTest = ServerPaymentProcessorTest.getServerPaymentProcessorTest(pm);
//		ModeOfPayment mopCash = (ModeOfPayment) pm.getObjectById(ModeOfPaymentConst.MODE_OF_PAYMENT_ID_CASH);
//		paymentProcessorTest.addModeOfPayment(mopCash);
//		paymentProcessorTest.getName().setText(Locale.ENGLISH.getLanguage(), "TestCase PaymentProcessor");
//		
//		return null;
	}

}
