package org.nightlabs.jfire.jdo.cache.cluster;

import java.util.Collection;
import java.util.Date;
import java.util.Random;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameAlreadyBoundException;
import javax.naming.NameClassPair;
import javax.naming.NameNotFoundException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;

import org.nightlabs.jfire.servermanager.j2ee.J2EEAdapter;
import org.nightlabs.math.Base62Coder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * <p>
 * Manager for storing and retrieving {@link SessionDescriptor}s to/from a memory shared
 * by all cluster-nodes in order to coordinate a fail-over from one cluster-node to
 * another.
 * </p>
 * <p>
 * This class is thread-safe. All methods of an instance can be called simultaneously by multiple threads.
 * </p>
 * <p>
 * The current implementation uses HA-JNDI as such shared memory.
 * </p>
 *
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public class SessionFailOverManager
{
	private static final Logger logger = LoggerFactory.getLogger(SessionFailOverManager.class);

	private static final String JNDI_SESSION_BASE_DIR = "java:/jfire/system/cache/session/";
	private static final String JNDI_SESSION_TIME_IDX_DIR = JNDI_SESSION_BASE_DIR + "time-idx/";
	private static final String JNDI_SESSION_MAIN_DIR = JNDI_SESSION_BASE_DIR + "main/";
	private static final String JNDI_TIMESTAMP_SUFFIX = ".timestamp";

	private static final long CLEAN_UP_PERIOD_MSEC = 1000L * 60L * 10L; // Clean up not more often than once in 10 minutes. It might be less often, though!
	public static final long EXPIRY_AGE_MSEC = 1000L * 60L * 60L; // expire after 1 hour

	private static final Base62Coder base62Coder = Base62Coder.sharedInstance();

	protected String getSessionIDPrefixedWithTimeIdx(String sessionID, Date timestamp)
	{
		final int fixedTimestampDigits = 11;
		StringBuilder sb = new StringBuilder(fixedTimestampDigits + 1 + sessionID.length());
		String v = base62Coder.encode(timestamp.getTime());
		int zeroDigits = fixedTimestampDigits - v.length();
		if (zeroDigits < 0)
			throw new IllegalStateException("How the hell can the timestamp be longer than " + fixedTimestampDigits + " digits in base-62?!?!??! It is \"" + v + "\"!");

		for (int i = 0; i < zeroDigits; ++i)
			sb.append('0');

		sb.append(v);
		if (sb.length() != fixedTimestampDigits)
			throw new IllegalStateException("Why the hell is this string \"" + sb + "\" not exactly " + fixedTimestampDigits + " chars long???!!!");

		sb.append('.').append(sessionID);

		return sb.toString();
	}

	private J2EEAdapter j2eeAdapter;

	public SessionFailOverManager() { }

	protected J2EEAdapter getJ2EEAdapter() throws NamingException
	{
		if (j2eeAdapter == null)
			j2eeAdapter = (J2EEAdapter) new InitialContext().lookup(J2EEAdapter.JNDI_NAME);

		return j2eeAdapter;
	}

	private Context createContext(InitialContext initCtx, String jndiName) throws NamingException
	{
		Context ctx;
		try {
			ctx = (Context) initCtx.lookup(jndiName);
		} catch (NameNotFoundException x) {
			String[] parts = jndiName.split("/");
			String dir = parts[0];
			for (int i = 1; i < parts.length; ++i) {
				if (!dir.isEmpty())
					dir += "/";

				dir += parts[i];

				try {
					Context subCtx = initCtx.createSubcontext(dir);
					subCtx.close();
				} catch (NameAlreadyBoundException nabe) {
					// ignore, because it's fine if it already exists - this is what we want ;-)
				}
			}

			ctx = (Context) initCtx.lookup(jndiName);
		}
		return ctx;
	}

	public void storeSession(SessionDescriptor sessionDescriptor)
	{
		String sessionID = sessionDescriptor.getSessionID();
		try {
			InitialContext initCtx = getJ2EEAdapter().createClusterInitialContext();

			Date timestamp = new Date();
			String sessionIDPrefixedWithTimeIdx = getSessionIDPrefixedWithTimeIdx(sessionID, timestamp);

			// First bind into the time-ordered directory, to ensure proper clean-up.
			Context ctxTimeIdx = createContext(initCtx, JNDI_SESSION_TIME_IDX_DIR);
			ctxTimeIdx.rebind(sessionIDPrefixedWithTimeIdx, ""); // the contents of the node don't matter => binding an empty string
			ctxTimeIdx.close();

			// Then bind into the main directory.
			Context ctxMain = createContext(initCtx, JNDI_SESSION_MAIN_DIR);
			ctxMain.rebind(sessionID + JNDI_TIMESTAMP_SUFFIX, timestamp);
			ctxMain.rebind(sessionID, sessionDescriptor);
			ctxMain.close();

			initCtx.close();
		} catch (NamingException e) {
			throw new RuntimeException(e);
		}
	}

	public void refreshTimestamps(Collection<String> sessionIDs)
	{
		if (logger.isTraceEnabled())
			logger.trace("refreshTimestamps: entered with {} sessionIDs.", sessionIDs.size());

		Date timestamp = new Date();

		try {
			InitialContext initCtx = getJ2EEAdapter().createClusterInitialContext();
			try {
				Context ctxTimeIdx = createContext(initCtx, JNDI_SESSION_TIME_IDX_DIR);
				Context ctxMain = createContext(initCtx, JNDI_SESSION_MAIN_DIR);

				for (String sessionID : sessionIDs) {
					String sessionIDPrefixedWithTimeIdx = getSessionIDPrefixedWithTimeIdx(sessionID, timestamp);
					ctxTimeIdx.rebind(sessionIDPrefixedWithTimeIdx, ""); // the contents of the node don't matter => binding an empty string
					ctxMain.rebind(sessionID + JNDI_TIMESTAMP_SUFFIX, timestamp);
				}

				ctxMain.close();
				ctxTimeIdx.close();
			} finally {
				initCtx.close();
			}
		} catch (NamingException e) {
			throw new RuntimeException(e);
		}


		logger.trace("refreshTimestamps: leaving.");
	}

	public SessionDescriptor loadSession(String sessionID)
	{
		try {
			InitialContext initCtx = getJ2EEAdapter().createClusterInitialContext();
			String sessionJNDIName = JNDI_SESSION_MAIN_DIR + sessionID;

			SessionDescriptor result;
			try {
				result = (SessionDescriptor) initCtx.lookup(sessionJNDIName);
			} catch (NameNotFoundException x) {
				result = null;
			}

			initCtx.close();
			return result;
		} catch (NamingException e) {
			throw new RuntimeException(e);
		}
	}

	private static final Random random = new Random();

	/**
	 * Periodically delegate to {@link #cleanUpNow()}, most of the time do nothing.
	 * This method should be called pretty often (e.g. once per minute). Since the
	 * cleanup is done in cluster-wide memory, it makes no sense to call {@link #cleanUpNow()}
	 * too often or even simultaneously on multiple threads. This method will try
	 * to optimize the cleanup process. It can be called from many threads in many
	 * different instances of {@link SessionFailOverManager} or the same instance.
	 */
	public void cleanUpPeriodically()
	{
		logger.trace("cleanUpPeriodically: entered");

		// Wait for a random time between 0 and 5 seconds to minimise collisions
		// (i.e. 2 or more cluster nodes cleaning up at the same time).
		try { Thread.sleep(Math.abs(random.nextLong() % 5000)); } catch (InterruptedException e) { /* ignore */ }

		logger.trace("cleanUpPeriodically: after sleep.");

		long origLastCleanupTimestamp;

		// Make sure only one single thread in the current JVM (i.e. cluster-node) does the clean-up.
		synchronized (SessionFailOverManager.class) {
			origLastCleanupTimestamp = lastCleanupTimestamp;
			long ageMSec = System.currentTimeMillis() - origLastCleanupTimestamp;
			if (ageMSec < CLEAN_UP_PERIOD_MSEC) {
				if (logger.isTraceEnabled())
					logger.trace("cleanUpPeriodically: lastCleanupTimestamp={} ageMSec={} => skipping.", origLastCleanupTimestamp, ageMSec);

				return;
			}

			lastCleanupTimestamp = System.currentTimeMillis();
		}

		if (logger.isDebugEnabled())
			logger.debug("cleanUpPeriodically: lastCleanupTimestamp={} => checking timestamp in JNDI.", origLastCleanupTimestamp);

		// Try to prevent multiple cluster-nodes doing the clean-up simultaneously.
		try {
			InitialContext initCtx = getJ2EEAdapter().createClusterInitialContext();
			try {
				createContext(initCtx, JNDI_SESSION_BASE_DIR).close();

				String jndiLastCleanupTimestampJNDIName = JNDI_SESSION_BASE_DIR + "lastCleanup";

				Long jndiLastCleanupTimestamp;
				try {
					jndiLastCleanupTimestamp = (Long) initCtx.lookup(jndiLastCleanupTimestampJNDIName);
				} catch (NameNotFoundException x) {
					jndiLastCleanupTimestamp = Long.valueOf(System.currentTimeMillis() - CLEAN_UP_PERIOD_MSEC);
				}

				long ageMSec = System.currentTimeMillis() - jndiLastCleanupTimestamp;
				if (ageMSec < CLEAN_UP_PERIOD_MSEC) {
					lastCleanupTimestamp = origLastCleanupTimestamp;
					logger.debug("cleanUpPeriodically: jndiLastCleanupTimestamp={} ageMSec={} => skipping.", jndiLastCleanupTimestamp, ageMSec);
					return;
				}

				initCtx.rebind(jndiLastCleanupTimestampJNDIName, Long.valueOf(System.currentTimeMillis()));
			} finally {
				initCtx.close();
			}
		} catch (NamingException e) {
			throw new RuntimeException(e);
		}

		cleanUpNow();
	}

	private static volatile long lastCleanupTimestamp = System.currentTimeMillis() - CLEAN_UP_PERIOD_MSEC;

	/**
	 * Cleans up all entries that are older than {@link #EXPIRY_AGE_MSEC}.
	 */
	public void cleanUpNow()
	{
		logger.debug("cleanUpNow: entered.");
		try {
			InitialContext initCtx = getJ2EEAdapter().createClusterInitialContext();
			try {
				Context timeCtx = null;
				try {
					try {
						timeCtx = (Context) initCtx.lookup(JNDI_SESSION_TIME_IDX_DIR);
					} catch (NameNotFoundException x) {
						// Nothing to clean up, because nothing stored there, yet.
						return;
					}

					// everything alphabetically *before* this timestamp needs to be cleaned up.
					String expiryTimestampS = base62Coder.encode(System.currentTimeMillis() - EXPIRY_AGE_MSEC);

					// We read the session references and make sure they are chronologically ordered (=> SortedSet).
					NamingEnumeration<NameClassPair> ne = timeCtx.list("");
					SortedSet<String> names = new TreeSet<String>();
					while (ne.hasMore()) {
						NameClassPair nameClassPair = ne.next();
						String name = nameClassPair.getName();
						names.add(name);
					}
					ne.close();

					// We iterate the chronologically ordered references starting with the oldest entries.
					for (String name : names) {
						if (name.compareTo(expiryTimestampS) < 0) {
							logger.debug("cleanUp: Maybe expired: {}", name);

							String sessionTimeIdxJNDIName = JNDI_SESSION_TIME_IDX_DIR + name;
							int idx = name.indexOf('.');
							if (idx < 0)
								throw new IllegalStateException("No '.' in JNDI name: " + sessionTimeIdxJNDIName);

							String timestampS = name.substring(0, idx);
							String sessionID = name.substring(idx + 1);
							initCtx.unbind(sessionTimeIdxJNDIName);

							String sessionTimestampJNDIName = JNDI_SESSION_MAIN_DIR + sessionID + JNDI_TIMESTAMP_SUFFIX;
							Date sessionTimestamp = null;
							try {
								sessionTimestamp = (Date) initCtx.lookup(sessionTimestampJNDIName);
							} catch (NameNotFoundException x) {
								logger.warn(
										"cleanUp: Found session reference {}, but {} does not exist.",
										sessionTimeIdxJNDIName, sessionTimestampJNDIName
								);
							}

							String sessionMainJNDIName = JNDI_SESSION_MAIN_DIR + sessionID;
							SessionDescriptor sessionDescriptor = null;
							try {
								sessionDescriptor = (SessionDescriptor) initCtx.lookup(sessionMainJNDIName);
							} catch (NameNotFoundException x) {
								logger.warn(
										"cleanUp: Found session reference {}, but {} does not exist.",
										sessionTimeIdxJNDIName, sessionMainJNDIName
								);
							}

							if (sessionDescriptor != null) {
								long timestamp = base62Coder.decode(timestampS);

								// If there is no (last access) timestamp in the JNDI, we set it to the
								// one of our index, which will cause the session to be removed.
								if (sessionTimestamp == null)
									sessionTimestamp = new Date(timestamp);

								if (sessionTimestamp.getTime() != timestamp) {
									logger.debug(
											"cleanUp: Chronological reference \"{}\" expired, but main session entry has different timestamp. Not removing this session.",
											sessionTimeIdxJNDIName
									);
								}
								else {
									logger.debug("cleanUp: Session {} expired. Removing it from JNDI.", sessionID);
									initCtx.unbind(sessionTimestampJNDIName);
									initCtx.unbind(sessionMainJNDIName);
								}
							}
							else if (sessionTimestamp != null) {
								initCtx.unbind(sessionTimestampJNDIName);
							}
						}
						else {
							logger.debug("cleanUp: Definitely not yet expired: {}", name);
							break;
						}
					}
				} finally {
					if (timeCtx != null)
						timeCtx.close();
				}
			} finally {
				initCtx.close();
			}
		} catch (NamingException e) {
			throw new RuntimeException(e);
		}
	}
}
