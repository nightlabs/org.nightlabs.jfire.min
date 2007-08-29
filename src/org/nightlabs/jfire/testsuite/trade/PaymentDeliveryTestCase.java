package org.nightlabs.jfire.testsuite.trade;

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import javax.jdo.FetchPlan;
import javax.jdo.JDOHelper;
import javax.security.auth.login.LoginException;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.nightlabs.ModuleException;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.accounting.AccountingManager;
import org.nightlabs.jfire.accounting.AccountingManagerUtil;
import org.nightlabs.jfire.accounting.Tariff;
import org.nightlabs.jfire.accounting.gridpriceconfig.TariffPricePair;
import org.nightlabs.jfire.accounting.id.CurrencyID;
import org.nightlabs.jfire.accounting.id.TariffID;
import org.nightlabs.jfire.accounting.pay.ModeOfPaymentConst;
import org.nightlabs.jfire.accounting.pay.ModeOfPaymentFlavour;
import org.nightlabs.jfire.accounting.pay.Payment;
import org.nightlabs.jfire.accounting.pay.PaymentController;
import org.nightlabs.jfire.accounting.pay.PaymentData;
import org.nightlabs.jfire.accounting.pay.id.ModeOfPaymentFlavourID;
import org.nightlabs.jfire.accounting.pay.id.PaymentDataID;
import org.nightlabs.jfire.accounting.pay.id.PaymentID;
import org.nightlabs.jfire.accounting.pay.id.ServerPaymentProcessorID;
import org.nightlabs.jfire.accounting.priceconfig.id.PriceConfigID;
import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.jfire.simpletrade.SimpleTradeManager;
import org.nightlabs.jfire.simpletrade.SimpleTradeManagerUtil;
import org.nightlabs.jfire.simpletrade.store.SimpleProductType;
import org.nightlabs.jfire.simpletrade.store.search.SimpleProductTypeQuery;
import org.nightlabs.jfire.store.ProductType;
import org.nightlabs.jfire.store.StoreManager;
import org.nightlabs.jfire.store.StoreManagerUtil;
import org.nightlabs.jfire.store.deliver.Delivery;
import org.nightlabs.jfire.store.deliver.DeliveryController;
import org.nightlabs.jfire.store.deliver.DeliveryData;
import org.nightlabs.jfire.store.deliver.ModeOfDeliveryFlavour;
import org.nightlabs.jfire.store.deliver.ModeOfDeliveryFlavour.ModeOfDeliveryFlavourProductTypeGroupCarrier;
import org.nightlabs.jfire.store.deliver.id.DeliveryDataID;
import org.nightlabs.jfire.store.deliver.id.ModeOfDeliveryFlavourID;
import org.nightlabs.jfire.store.deliver.id.ServerDeliveryProcessorID;
import org.nightlabs.jfire.store.id.ProductTypeID;
import org.nightlabs.jfire.testsuite.JFireTestSuite;
import org.nightlabs.jfire.trade.Article;
import org.nightlabs.jfire.trade.LegalEntity;
import org.nightlabs.jfire.trade.Offer;
import org.nightlabs.jfire.trade.Order;
import org.nightlabs.jfire.trade.TradeManager;
import org.nightlabs.jfire.trade.TradeManagerUtil;
import org.nightlabs.jfire.trade.id.ArticleContainerID;
import org.nightlabs.jfire.trade.id.ArticleID;
import org.nightlabs.jfire.trade.id.CustomerGroupID;
import org.nightlabs.jfire.trade.id.OfferID;
import org.nightlabs.jfire.trade.id.OrderID;
import org.nightlabs.jfire.trade.id.SegmentID;
import org.nightlabs.jfire.trade.id.SegmentTypeID;
import org.nightlabs.jfire.transfer.Stage;
import org.nightlabs.jfire.transfer.id.AnchorID;

@JFireTestSuite(JFireTradeTestSuite.class)
public class PaymentDeliveryTestCase extends TestCase {

	private static final Logger logger = Logger.getLogger(PaymentDeliveryTestCase.class);

	@Test
	public synchronized void testPaymentAndDeliveryResults() throws RemoteException, ModuleException, LoginException {
		logger.debug("testPaymentAndDeliveryResults");
		
		runPaymentAndDelivery(null, null);
		runPaymentAndDelivery(null, Stage.ServerBegin);
		runPaymentAndDelivery(Stage.ServerBegin, Stage.ServerBegin);
		runPaymentAndDelivery(Stage.ServerDoWork, Stage.ServerBegin);
		runPaymentAndDelivery(Stage.ServerEnd, Stage.ServerBegin);
		
		runPaymentAndDelivery(null, Stage.ServerDoWork);
		runPaymentAndDelivery(Stage.ServerBegin, Stage.ServerDoWork);
		runPaymentAndDelivery(Stage.ServerDoWork, Stage.ServerDoWork);
		runPaymentAndDelivery(Stage.ServerEnd, Stage.ServerDoWork);
		
		runPaymentAndDelivery(null, Stage.ServerEnd);
		runPaymentAndDelivery(Stage.ServerBegin, Stage.ServerEnd);
		runPaymentAndDelivery(Stage.ServerDoWork, Stage.ServerEnd);
		runPaymentAndDelivery(Stage.ServerEnd, Stage.ServerEnd);

		runPaymentAndDelivery(Stage.ServerBegin, null);
		runPaymentAndDelivery(Stage.ServerDoWork, null);
		runPaymentAndDelivery(Stage.ServerEnd, null);
		
	}
	
	private void runPaymentAndDelivery(Stage deliveryFailureStage, Stage paymentFailureStage) throws RemoteException, ModuleException, LoginException {
		TestEnvironment te = prepareTestEnvironment();
		PaymentData paymentData = new PaymentDataTestCase(te.payment, paymentFailureStage);
		DeliveryData deliveryData = new DeliveryDataTestCase(te.delivery, deliveryFailureStage);

		ServerOnlyDeliveryController deliveryController = new ServerOnlyDeliveryController(deliveryData);
		ServerOnlyPaymentController paymentController = new ServerOnlyPaymentController(paymentData);

		performStages(deliveryController, paymentController);

		PaymentDataID paymentDataID = PaymentDataID.create(paymentData.getOrganisationID(), paymentData.getPaymentID());
		DeliveryDataID deliveryDataID = DeliveryDataID.create(deliveryData.getOrganisationID(), deliveryData.getDeliveryID());

		// ask the bean to check the payment + delivery
		
//		// get the current server version of paymentData + deliveryData
//		paymentData = getAccountingManager().getPaymentData(paymentDataID, new String[] { FetchPlan.DEFAULT, PaymentData.FETCH_GROUP_PAYMENT, Payment.FET });
//		deliveryData =  getStoreManager().getDeliveryData(deliveryDataID);
		
	}

	public TestEnvironment prepareTestEnvironment() throws RemoteException, ModuleException {
		TradeManager tm = getTradeManager();
		SimpleTradeManager stm = getSimpleTradeManager();
		StoreManager sm = getStoreManager();
		AccountingManager am = getAccountingManager();
		
		AnchorID anonymousCustomerID = AnchorID.create(IDGenerator.getOrganisationID(), LegalEntity.ANCHOR_TYPE_ID_PARTNER,
				LegalEntity.ANCHOR_ID_ANONYMOUS);

		CurrencyID euroID = CurrencyID.create("EUR");
		Order order = tm.createOrder(
				anonymousCustomerID, (String) null, euroID,
				new SegmentTypeID[] { null }, // this will create one default segment
				new String[] { Order.FETCH_GROUP_THIS_ORDER },
				NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT);
		OrderID orderID = (OrderID) JDOHelper.getObjectId(order);

		Offer offer = tm.createOffer(orderID, null, new String[] { Offer.FETCH_GROUP_THIS_OFFER , FetchPlan.DEFAULT}, NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT);
		OfferID offerID = (OfferID) JDOHelper.getObjectId(offer);

		SimpleProductTypeQuery query = new SimpleProductTypeQuery();
		query.setSaleable(true);
		Set<ProductTypeID> productTypeIDs = sm.getProductTypeIDs(Collections.singletonList(query));
		ProductTypeID productTypeID = productTypeIDs.iterator().next();

		SimpleProductType productType = stm.getSimpleProductType(productTypeID, new String[] { FetchPlan.DEFAULT,
				ProductType.FETCH_GROUP_PACKAGE_PRICE_CONFIG }, NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT);

		Collection<TariffPricePair> tariffPricePairs = stm.getTariffPricePairs(
				(PriceConfigID) JDOHelper.getObjectId(productType.getPackagePriceConfig()),
				(CustomerGroupID) JDOHelper.getObjectId(order.getCustomerGroup()), euroID, new String[] { FetchPlan.DEFAULT },
				new String[] { FetchPlan.DEFAULT });
		Tariff tariff = tariffPricePairs.iterator().next().getTariff();
		Collection<Article> articles = stm.createArticles((SegmentID) JDOHelper.getObjectId(order.getSegments().iterator().next()), offerID,
				productTypeID, (int) (1 + Math.random() * 9), (TariffID) JDOHelper.getObjectId(tariff), true, true,
				new String[] { Article.FETCH_GROUP_ARTICLE_LOCAL, FetchPlan.DEFAULT }, NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT);

		Collection<ArticleID> articleIDs = new LinkedList<ArticleID>();
		for (Article art : articles)
			articleIDs.add((ArticleID) JDOHelper.getObjectId(art));

		sm.createDeliveryNote((ArticleContainerID) JDOHelper.getObjectId(offer), (String)null, false, new String[0], NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT);
		Delivery delivery = new Delivery(IDGenerator.getOrganisationID(), IDGenerator.nextID(Delivery.class));
		ModeOfDeliveryFlavourProductTypeGroupCarrier modfProductTypeGroupCarrier = sm.getModeOfDeliveryFlavourProductTypeGroupCarrier(productTypeIDs,
				Collections.singleton(JDOHelper.getObjectId(order.getCustomerGroup())), ModeOfDeliveryFlavour.MERGE_MODE_SUBTRACTIVE,
				new String[] { FetchPlan.DEFAULT }, NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT);
		ModeOfDeliveryFlavourID manualFlavourID = ModeOfDeliveryFlavourID.create(Organisation.DEVIL_ORGANISATION_ID, "manual");
		ModeOfDeliveryFlavourID flavourIDToBeUsed = null;
		for (ModeOfDeliveryFlavour flavour : (Collection<ModeOfDeliveryFlavour>) modfProductTypeGroupCarrier.getModeOfDeliveryFlavours()) {
			if (JDOHelper.getObjectId(flavour).equals(manualFlavourID))
				flavourIDToBeUsed = manualFlavourID;
		}

		if (flavourIDToBeUsed == null)
			throw new IllegalStateException("Delivery works only with manual delivery available.");

		delivery.setModeOfDeliveryFlavourID(flavourIDToBeUsed);
		delivery.setPartner(order.getCustomer());

		delivery.setClientDeliveryProcessorFactoryID("dummy"); // should not matter
		delivery.setServerDeliveryProcessorID(ServerDeliveryProcessorID.create(Organisation.DEVIL_ORGANISATION_ID, ServerDeliveryProcessorTest.class.getName()));

		//		productType.get
		//		DeliveryNote deliveryNote = sm.createDeliveryNote(articleIDs, null, true, new String[] { FetchPlan.DEFAULT },
		//				NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT);

		delivery.setArticles(new HashSet<Article>(articles));
		delivery.setDeliveryDirection(Delivery.DELIVERY_DIRECTION_OUTGOING);

		Payment payment = new Payment(IDGenerator.getOrganisationID(), IDGenerator.nextID(Payment.class));
		Collection<ModeOfPaymentFlavour> mopFlavours = am.getAvailableModeOfPaymentFlavoursForOneCustomerGroup((CustomerGroupID) JDOHelper
				.getObjectId(order.getCustomerGroup()), new String[] { FetchPlan.DEFAULT }, NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT);

		ModeOfPaymentFlavourID mopIDToBeUsed = null;
		for (ModeOfPaymentFlavour flavour : mopFlavours) {
			if (JDOHelper.getObjectId(flavour).equals(ModeOfPaymentConst.MODE_OF_PAYMENT_FLAVOUR_ID_CASH))
				mopIDToBeUsed = ModeOfPaymentConst.MODE_OF_PAYMENT_FLAVOUR_ID_CASH;
		}

		if (mopIDToBeUsed == null)
			throw new IllegalStateException("Payment works only with cash payment available.");

		payment.setAmount(offer.getPrice().getAmount());
		payment.setPartner(order.getCustomer());
		payment.setClientPaymentProcessorFactoryID("dummy");
		payment.setServerPaymentProcessorID(ServerPaymentProcessorID.create(Organisation.DEVIL_ORGANISATION_ID, ServerPaymentProcessorTest.class.getName()));
		payment.setCurrencyID(euroID);
		payment.setModeOfPaymentFlavourID(mopIDToBeUsed);
		payment.setPaymentDirection(Payment.PAYMENT_DIRECTION_INCOMING);

		return new TestEnvironment(delivery, payment);
	}

	public void performStages(DeliveryController deliveryController, PaymentController paymentController) throws LoginException {
		boolean skipServerPayment = false;
		boolean skipServerDelivery = false;

		///////////
		// BEGIN //
		///////////

		// if the client approve failed on ALL client payments, we don't do anything
		// in the server, but call the client's payEnd to allow clean-up.
		skipServerPayment = !paymentController.clientBegin();

		// if the client approve failed for ALL deliveries, we don't do anything
		// in the server, but call the client's deliverEnd to allow clean-up.
		skipServerDelivery = !deliveryController.clientBegin();

		if (skipServerDelivery)
			deliveryController.skipServerStages();
		if (skipServerPayment)
			paymentController.skipServerStages();

		//	TODO perform Server-Payment and -Delivery in one step if both must be done

		deliveryController.serverBegin();
		paymentController.serverBegin();

		if (paymentController.isRollbackRequired() || deliveryController.isRollbackRequired()) {
			paymentController.forceRollback();
			deliveryController.forceRollback();
		}

		////////////
		// DOWORK //
		////////////

		deliveryController.clientDoWork();
		paymentController.clientDoWork();

		deliveryController.serverDoWork();
		paymentController.serverDoWork();

		if (paymentController.isRollbackRequired() || deliveryController.isRollbackRequired()) {
			paymentController.forceRollback();
			deliveryController.forceRollback();
		}

		/////////
		// END //
		/////////

		deliveryController.clientEnd();
		paymentController.clientEnd();

		deliveryController.serverEnd();
		paymentController.serverEnd();

		////////////
		// VERIFY //
		////////////

		paymentController.verifyData();
		deliveryController.verifyData();
	}

	private TradeManager getTradeManager() {
		try {
			return TradeManagerUtil.getHome().create();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private SimpleTradeManager getSimpleTradeManager() {
		try {
			return SimpleTradeManagerUtil.getHome().create();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private StoreManager getStoreManager() {
		try {
			return StoreManagerUtil.getHome().create();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private AccountingManager getAccountingManager() {
		try {
			return AccountingManagerUtil.getHome().create();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}

class TestEnvironment {
	public Delivery delivery;
	public Payment payment;

	public TestEnvironment(Delivery delivery, Payment payment) {
		super();
		this.delivery = delivery;
		this.payment = payment;
	}
}
