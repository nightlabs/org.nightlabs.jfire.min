/*
 * Created on Jul 28, 2005
 */
package org.nightlabs.ipanema.base.jdo.cache;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;

/**
 * Whenever a new <tt>Carrier</tt> is created, it will be put into a <tt>CarrierContainer</tt>.
 * The <tt>Cache</tt> manages a <tt>LinkedList</tt> of such containers with one being
 * the active container. All newly created <tt>Carrier</tt>s are put into this active one.
 * After a certain time, the <tt>Cache</tt> rolls its <tt>CarrierContainer</tt>s. This
 * means, a new container is created, added as first container and the oldest containers
 * are dropped (so only a certain number of containers is permanently existing).
 * <p>
 * This mechanism prevents iterating all <tt>Carrier</tt>s and checking the creation time of them.
 * This way, simply all <tt>Carrier</tt>s of an old <tt>CarrierContainer</tt> are dropped.
 * <p>
 * This works similar to the server side management of <tt>CacheSession</tt>s in
 * <tt>CacheSessionContainer</tt>s, but with the following difference: A <tt>CacheSession</tt>
 * changes its container when it is refreshed by simply using it while a <tt>Carrier</tt>
 * in the local cache stays in its container and therefore "dies" always after a limited time.
 *
 * @author Marco Schulze - marco at nightlabs dot de
 */
public class CarrierContainer
{
	public static final Logger LOGGER = Logger.getLogger(CarrierContainer.class);

	private Cache cache;

	protected CarrierContainer(Cache cache)
	{
		this.cache = cache;
	}

	private long createDT = System.currentTimeMillis();
	/**
	 * @return Returns the createDT.
	 */
	public long getCreateDT()
	{
		return createDT;
	}

	/**
	 * key: {@link Key} key<br/>
	 * value: {@link Carrier} carrier
	 */
	private Map carriersByKey = new HashMap();

	/**
	 * @param carrier The <tt>Carrier</tt> to add - never <tt>null</tt>.
	 */
	protected void addCarrier(Carrier carrier)
	{
		if (closed)
			throw new IllegalStateException("This CarrierContainer is closed! Why the hell do you try to add a Carrier?");

		if (carrier == null)
			throw new NullPointerException("carrier");

		if (LOGGER.isDebugEnabled())
			LOGGER.debug("Adding Carrier with key " + carrier.getKey());

		synchronized (carriersByKey) {
			carriersByKey.put(carrier.getKey(), carrier);
		}
	}

	protected void removeCarrier(Key key)
	{
		if (closed)
			return;

		synchronized (carriersByKey) {
			if (carriersByKey.remove(key) != null) {
				if (LOGGER.isDebugEnabled())
					LOGGER.debug("Removed Carrier for key " + key);
			}
			else {
				if (LOGGER.isDebugEnabled())
					LOGGER.debug("Could not remove (because did not find) Carrier for key " + key);
			}

		}
	}

	private boolean closed = false;

	protected void close()
	{
		closed = true;

		if (LOGGER.isDebugEnabled())
			LOGGER.debug("Closing CarrierContainer (created " + createDT + ")");

		synchronized (carriersByKey) {
			for (Iterator it = carriersByKey.keySet().iterator(); it.hasNext(); ) {
				Key key = (Key) it.next();

				cache.remove(key);
			}
		}
	}

	protected Cache getCache()
	{
		return cache;
	}
}
