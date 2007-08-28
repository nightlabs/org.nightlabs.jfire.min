package org.nightlabs.jfire.testsuite.trade;

import java.util.Locale;

import javax.jdo.PersistenceManager;

import org.nightlabs.jfire.accounting.pay.ModeOfPayment;
import org.nightlabs.jfire.accounting.pay.ModeOfPaymentConst;
import org.nightlabs.jfire.store.deliver.ModeOfDelivery;
import org.nightlabs.jfire.store.deliver.ModeOfDeliveryConst;
import org.nightlabs.jfire.testsuite.TestSuite;

public class JFireTradeTestSuite extends TestSuite {

	/*
	 * (non-Javadoc)
	 * @see org.nightlabs.jfire.testsuite.TestSuite#canRunTests(javax.jdo.PersistenceManager)
	 */
	@Override
	public String canRunTests(PersistenceManager pm) throws Exception {
		ServerDeliveryProcessorTest deliveryProcessorTest = ServerDeliveryProcessorTest.getServerDeliveryProcessorTest(pm);
		ModeOfDelivery modManual = (ModeOfDelivery) pm.getObjectById(ModeOfDeliveryConst.MODE_OF_DELIVERY_ID_MANUAL);
		deliveryProcessorTest.addModeOfDelivery(modManual);
		deliveryProcessorTest.getName().setText(Locale.ENGLISH.getLanguage(), "TestCase DeliveryProcessor");
		
		ServerPaymentProcessorTest paymentProcessorTest = ServerPaymentProcessorTest.getServerPaymentProcessorTest(pm);
		ModeOfPayment mopCash = (ModeOfPayment) pm.getObjectById(ModeOfPaymentConst.MODE_OF_PAYMENT_FLAVOUR_ID_CASH);
		paymentProcessorTest.addModeOfPayment(mopCash);
		paymentProcessorTest.getName().setText(Locale.ENGLISH.getLanguage(), "TestCase PaymentProcessor");
		
		return null;
	}

}
