package org.nightlabs.jfire.classloader.remote;

import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.nightlabs.jfire.classloader.remote.backend.JFireRCLBackendRemote;

class JFireRCLBackendPool
{
	private static final Logger logger = Logger.getLogger(JFireRCLBackendPool.class);
//	private static final long beanStubLifetimeMSec = 30000;
	private static final long beanStubLifetimeMSec = 10L * 60L * 1000L; // It seems such a stub is usable even after the server rebooted (only invalid if modules are [re]deployed). Keeping it longer (10 minutes), now. Marco.

	private static class Carrier {
		public Carrier(JFireRCLBackendRemote jFireRCLBackend)
		{
			assert jFireRCLBackend != null;
			this.jFireRCLBackend = jFireRCLBackend;
		}

		private long createTimestamp = System.currentTimeMillis();
		private JFireRCLBackendRemote jFireRCLBackend;

		public long getCreateTimestamp()
		{
			return createTimestamp;
		}
		public JFireRCLBackendRemote getJFireRCLBackend()
		{
			return jFireRCLBackend;
		}
	}

	private List<Carrier> instancesAvailable = new LinkedList<Carrier>();

	/**
	 * @return null, if there is no (young enough) instance left in the pool or an instance of <code>JFireRCLBackend</code>.
	 */
	public synchronized JFireRCLBackendRemote getJFireRCLBackend()
	{
		Carrier carrier = null;
		boolean debug = logger.isDebugEnabled();

		if (debug)
			logger.debug("getJFireRCLBackend: instancesAvailable.size()=" + instancesAvailable.size());

		while (true) {
			if (instancesAvailable.isEmpty()) {
				if (debug)
					logger.debug("getJFireRCLBackend: No cached JFireRCLBackend found which is young enough! Returning null.");

				return null;
			}

			carrier = instancesAvailable.remove(0);
			if (System.currentTimeMillis() - carrier.getCreateTimestamp() > beanStubLifetimeMSec) {
				if (debug)
					logger.debug("getJFireRCLBackend: dropped cached JFireRCLBackend with age " + (System.currentTimeMillis() - carrier.getCreateTimestamp()) + " msec.");

				carrier = null;
			}

			if (carrier != null) {
				if (debug)
					logger.debug("getJFireRCLBackend: found cached JFireRCLBackend with age " + (System.currentTimeMillis() - carrier.getCreateTimestamp()) + " msec.");

				return carrier.getJFireRCLBackend();
			}
		}
	}

	public synchronized void putJFireRCLBackend(JFireRCLBackendRemote jFireRCLBackend)
	{
		Carrier carrier = new Carrier(jFireRCLBackend);
		instancesAvailable.add(carrier);

		if (logger.isDebugEnabled())
			logger.debug("putJFireRCLBackend: added a JFireRCLBackend to pool. instancesAvailable.size()=" + instancesAvailable.size());
	}

	/**
	 * Clears the list of all cached bean stubs.
	 */
	public void clear()
	{
		instancesAvailable.clear();
	}
}
