package org.nightlabs.jfire.multitxjob;

import java.util.Collection;
import java.util.Map;

import javax.jdo.PersistenceManager;

import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.timer.Task;

/**
 * <p>
 * Utility class for handling long-running tasks that must be spread over multiple transactions.
 * The usual use case for this utility is a job that consists of many steps - e.g. a list of
 * entities to be processed.
 * </p>
 * <p>
 * In this scenario, you would have a {@link Task} that periodically processes the entities and
 * maybe additionally some other events that add up entities to be processed.
 * </p>
 * <p>
 * In your timer <code>Task</code> or async <code>Invocation</code>, whenever you see that the
 * maximum transaction duration time (recommended 1 or 2 minutes) elapsed, you put the remaining
 * (not yet processed) entities into a new {@link MultiTxJobPart} (you can do the same in an event
 * to schedule the entities to be processed by the <code>Task</code> later):
 * </p>
 * <p>
 * <pre>
 * {@code
 * Collection<MyEntity> remainingEntities = processEntities(entitiesToBeProcessed);
 * MultiTxJob.createMultiTxJobPart(pm, MY_MULTI_TX_JOB_ID, remainingEntities);
 * }
 * </pre>
 * </p>
 * <p>
 * At the beginning of your <code>Task</code>, you check whether there are still remaining entities from
 * a previous run or from some event. If there are none, you either return (since there's nothing to do)
 * or you query entities by some criteria:
 * <pre>
 * {@code
 * Collection<MyEntity> oldEntities = CollectionUtil.castCollection(
 * 	(Collection<?>)MultiTxJob.popMultiTxJobPartData(pm, MY_MULTI_TX_JOB_ID)
 * );
 * }
 *
 * Collection<MyEntity> entitiesToBeProcessed;
 * if (oldEntities != null)
 * 	entitiesToBeProcessed = oldEntities;
 * else
 * 	entitiesToBeProcessed = queryEntititiesToBeProcessed(pm);
 * </pre>
 * </p>
 *
 * @author marco schulze - marco at nightlabs dot de
 */
public class MultiTxJob
{
	private MultiTxJob() { }

	/**
	 * Get all {@link MultiTxJobPart}s for a certain multi-tx-job (specified by its unique <code>multiTxJobID</code>).
	 *
	 * @param pm the door to the datastore.
	 * @param multiTxJobID the unique identifier of your multi-tx-job.
	 * @return a {@link Collection} of {@link MultiTxJobPart}s - never <code>null</code>.
	 */
	public static Collection<? extends MultiTxJobPart> getMultiTxJobParts(PersistenceManager pm, String multiTxJobID)
	{
		return MultiTxJobPart.getMultiTxJobParts(pm, multiTxJobID);
	}

	/**
	 * Store data for a multi-tx-job that can later be {@link #popMultiTxJobPartData(PersistenceManager, String) popped} and processed.
	 *
	 * @param pm the door to the datastore.
	 * @param multiTxJobID the unique identifier of your multi-tx-job.
	 * @param data a single entity (quite unlikely) or a {@link Collection} /  {@link Map} of entities (more common)
	 * 		depending on your <code>multiTxJobID</code>. For each multi-tx-job, you can decide yourself
	 * 		what data you manage - the only requirement is that your multi-tx-job understands the result of the method
	 * 		{@link #popMultiTxJobPartData(PersistenceManager, String)} when it calls it later.
	 * @return the job-part that is used to store the <code>data</code> in persistent storage (the JDO datastore).
	 */
	public static MultiTxJobPart createMultiTxJobPart(PersistenceManager pm, String multiTxJobID, Object data)
	{
		MultiTxJobPart multiTxJobPart = new MultiTxJobPart(multiTxJobID, IDGenerator.nextID(MultiTxJobPart.class, multiTxJobID));
		multiTxJobPart.setData(data);
		return pm.makePersistent(multiTxJobPart);
	}

	/**
	 * Get the first {@link MultiTxJobPart}'s data and remove the <code>MultiTxJobPart</code> from the
	 * datastore. If there is no data pending for the current multi-tx-job, this method returns <code>null</code>.
	 *
	 * @param pm the door to the datastore.
	 * @param multiTxJobID the unique identifier of your multi-tx-job.
	 * @return <code>null</code> or a single entity (quite unlikely) or a {@link Collection} /  {@link Map} of entities (more common)
	 * 		depending on your <code>multiTxJobID</code>. For each multi-tx-job, you can decide yourself
	 * 		what data you manage - the only requirement is that your multi-tx-job understands the result of this method (your code
	 * 		stores it in {@link #createMultiTxJobPart(PersistenceManager, String, Object)}).
	 */
	public static Object popMultiTxJobPartData(PersistenceManager pm, String multiTxJobID)
	{
		NLJDOHelper.enableTransactionSerializeReadObjects(pm);
		try {
			MultiTxJobPart multiTxJobPart = MultiTxJobPart.getFirstMultiTxJobPart(pm, multiTxJobID);
			if (multiTxJobPart == null)
				return null;

			Object data = multiTxJobPart.getData();
			pm.deletePersistent(multiTxJobPart);
			return data;
		} finally {
			NLJDOHelper.disableTransactionSerializeReadObjects(pm);
		}
	}
}
