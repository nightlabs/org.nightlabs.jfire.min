package org.nightlabs.jfire.testsuite.trade;

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Set;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import junit.framework.TestCase;

import org.junit.Test;
import org.nightlabs.ModuleException;
import org.nightlabs.jfire.accounting.id.CurrencyID;
import org.nightlabs.jfire.accounting.id.TariffID;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.jfire.simpletrade.SimpleTradeManager;
import org.nightlabs.jfire.simpletrade.SimpleTradeManagerUtil;
import org.nightlabs.jfire.simpletrade.store.SimpleProductType;
import org.nightlabs.jfire.store.StoreManager;
import org.nightlabs.jfire.store.StoreManagerUtil;
import org.nightlabs.jfire.store.id.ProductTypeID;
import org.nightlabs.jfire.store.search.ProductTypeQuery;
import org.nightlabs.jfire.trade.Article;
import org.nightlabs.jfire.trade.LegalEntity;
import org.nightlabs.jfire.trade.Offer;
import org.nightlabs.jfire.trade.Order;
import org.nightlabs.jfire.trade.TradeManager;
import org.nightlabs.jfire.trade.TradeManagerUtil;
import org.nightlabs.jfire.trade.id.OfferID;
import org.nightlabs.jfire.trade.id.OrderID;
import org.nightlabs.jfire.transfer.id.AnchorID;

public class PaymentDeliveryTestCase extends TestCase {
	@Test
	public void testPaymentDelivery() throws RemoteException, ModuleException {
		ServerOnlyDeliveryController deliveryController = new ServerOnlyDeliveryController();
		ServerOnlyPaymentController paymentController = new ServerOnlyPaymentController();
		
		TradeManager tm = getTradeManager();
		SimpleTradeManager stm = getSimpleTradeManager();
		StoreManager sm = getStoreManager();
		
		AnchorID anonymousCustomerID = AnchorID.create(SecurityReflector.getUserDescriptor().getOrganisationID(), LegalEntity.ANCHOR_TYPE_ID_PARTNER, LegalEntity.ANCHOR_ID_ANONYMOUS);
		Order order = tm.createOrder(anonymousCustomerID, null, CurrencyID.create("EUR"), null, new String[] { Order.FETCH_GROUP_THIS_ORDER }, -1);
		OrderID orderID = (OrderID) JDOHelper.getObjectId(order);
		
		Offer offer = tm.createOffer(orderID, null, new String[] { Offer.FETCH_GROUP_THIS_OFFER }, -1);
		OfferID offerID = (OfferID) JDOHelper.getObjectId(offer);
		
		ProductTypeQuery<SimpleProductType> query = new ProductTypeQuery<SimpleProductType>();
		query.setAvailable(true);
		query.setSaleable(true);
		Set<ProductTypeID> productTypeIDs = sm.getProductTypeIDs(Collections.singletonList(query));
		ProductTypeID productTypeID = productTypeIDs.iterator().next();
		TariffID tariffID = TariffID.create(SecurityReflector.getUserDescriptor().getOrganisationID(), "_normal_price_");
		
		
		Collection<Article> articles = stm.createArticles(null, offerID, productTypeID, (int)(1+Math.random()*9), tariffID, true, true, new String[] { Article.FETCH_GROUP_ARTICLE_LOCAL }, -1);
		
		
	}
	
	private <T> Collection<T> getJDOObjects(PersistenceManager pm, Class<T> clazz, int number) {
		Query query = pm.newQuery(clazz);
		query.setRange(0, number);
		return (Collection<T>) query.execute();
	}
	
	private <T> T getJDOObject(PersistenceManager pm, Class<T> clazz) {
		return getJDOObjects(pm, clazz, 1).iterator().next();
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
}
