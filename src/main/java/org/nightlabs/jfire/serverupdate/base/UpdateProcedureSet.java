package org.nightlabs.jfire.serverupdate.base;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;

import org.nightlabs.version.Version;

/**
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public class UpdateProcedureSet
implements SortedSet<UpdateProcedure>
{
	private UpdateProcedureSet parent = null;
	private String subSet_moduleID = null;
	private UpdateProcedure subSet_fromIncluding = null;
	private UpdateProcedure subSet_toExcluding = null;

	private Map<String, Integer> moduleID2size = null;
	private SortedMap<String, SortedMap<Version, UpdateStep>> moduleID2from2updateStep = null;

	public UpdateProcedureSet() {
		moduleID2from2updateStep = new TreeMap<String, SortedMap<Version,UpdateStep>>();
		moduleID2size = new HashMap<String, Integer>();
	}

	public UpdateProcedureSet(Collection<? extends UpdateProcedure> updateProcedures) {
		this();
		addAll(updateProcedures);
	}

	/**
	 * <p>
	 * Constructor used to create a sub-Set which is backed by the <code>parent</code>. That
	 * means, changes that are performed in the <code>parent</code> will be visible in the sub-set, too.
	 * </p>
	 * <p>
	 * At the moment, the sub-set is read-only, because we don't need write operations on it, but this might
	 * change in the future.
	 * </p>
	 *
	 * @param parent the main <code>UpdateProcedureSet</code> which is backing the new sub-set.
	 * @param moduleID <code>null</code> or the moduleID for which to provide a sub-set.
	 */
	protected UpdateProcedureSet(UpdateProcedureSet parent, String moduleID) {
		this.parent = parent;
		this.subSet_moduleID = moduleID;

		// Unfortunately, we cannot use SortedMap.subMap(fromKey, toKey), because the toKey is EXclusive and we don't know anything
		// but the current moduleID (because the parent might be modified and thus statically looking for the next moduleID NOW might
		// not be correct when new items are added in the parent).
		moduleID2from2updateStep = new InternalSubMap1(parent.moduleID2from2updateStep, moduleID);
	}

	/**
	 * <p>
	 * Constructor used to create a sub-Set which is backed by the <code>parent</code>. That
	 * means, changes that are performed in the <code>parent</code> will be visible in the sub-set, too.
	 * </p>
	 * <p>
	 * At the moment, the sub-set is read-only, because we don't need write operations on it, but this might
	 * change in the future.
	 * </p>
	 *
	 * @param parent the main <code>UpdateProcedureSet</code> which is backing the new sub-set.
	 */
	protected UpdateProcedureSet(UpdateProcedureSet parent, UpdateProcedure fromIncluding, UpdateProcedure toExcluding) {
		this.parent = parent;
		this.subSet_fromIncluding = fromIncluding;
		this.subSet_toExcluding = toExcluding;

		// Unfortunately, we cannot use SortedMap.subMap(fromKey, toKey), because the toKey is EXclusive and we don't know anything
		// but the current moduleID (because the parent might be modified and thus statically looking for the next moduleID NOW might
		// not be correct when new items are added in the parent).
		moduleID2from2updateStep = new InternalSubMap2(parent.moduleID2from2updateStep, fromIncluding, toExcluding);
	}

	private void assertWritable()
	{
		if (parent != null)
			throw new UnsupportedOperationException("This instance of UpdateProcedureSet is read-only!");
	}

	@Override
	public Comparator<? super UpdateProcedure> comparator() {
		return null; // We use the native order.
	}

	@Override
	public UpdateProcedure first() {
		String key1 = moduleID2from2updateStep.firstKey();
		SortedMap<Version, UpdateStep> from2updateStep = moduleID2from2updateStep.get(key1);

		Version key2 = from2updateStep.firstKey();
		UpdateStep updateStep = from2updateStep.get(key2);

		return updateStep.getUpdateProcedures().first();
	}


	@Override
	public UpdateProcedure last() {
		String key1 = moduleID2from2updateStep.lastKey();
		SortedMap<Version, UpdateStep> from2updateStep = moduleID2from2updateStep.get(key1);

		Version key2 = from2updateStep.lastKey();
		UpdateStep updateStep = from2updateStep.get(key2);

		return updateStep.getUpdateProcedures().last();
	}

	//Includes the fromElement but not the toElement
	@Override
	public UpdateProcedureSet subSet(UpdateProcedure fromElement, UpdateProcedure toElement) {
		if (parent != null)
			throw new UnsupportedOperationException("It is currently not possible to obtain a sub-set of a sub-set!");

		return new UpdateProcedureSet(this, fromElement, toElement);
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * <b>Important:</b> The implementation of <code>headSet(...)</code> in {@link UpdateProcedureSet} currently
	 * does <b>not</b> return a backed {@link SortedSet}, but an independent copy! This might change any time in
	 * the future without prior notice!
	 * </p>
	 */
	//Returns a view of the portion of this sorted set whose elements are strictly less than toElement. (not included the toElement)
	@Override
	public UpdateProcedureSet headSet(UpdateProcedure toElement) {
		UpdateProcedureSet result = new UpdateProcedureSet();
		for (UpdateProcedure updateProcedure : this) {
			if (updateProcedure.compareTo(toElement) < 0)
				result.add(updateProcedure);
			else
				break; // No need to continue iteration as they are sorted and thus all following will not match the comparison, too.
		}
		return result;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * <b>Important:</b> The implementation of <code>tailSet(...)</code> in {@link UpdateProcedureSet} currently
	 * does <b>not</b> return a backed {@link SortedSet}, but an independent copy! This might change any time in
	 * the future without prior notice!
	 * </p>
	 */
	//Returns a view of the portion of this sorted set whose elements are greater than or *equal* to fromElement. (included the fromElement)
	@Override
	public UpdateProcedureSet tailSet(UpdateProcedure fromElement) {
		UpdateProcedureSet result = new UpdateProcedureSet();
		for (UpdateProcedure updateProcedure : this) {
			if (updateProcedure.compareTo(fromElement) >= 0)
				result.add(updateProcedure);
		}
		return result;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Before adding an element, this method checks whether the operation is legal. If it would lead to an inconsistent
	 * state, an {@link InconsistentUpdateProceduresException} is thrown. Note, though, that no final decision can be
	 * made whether it is really consistent or not, because certain inconsistencies are only detectable after all
	 * elements have been added (e.g. the existence of holes is legal as long as not all elements are populated).
	 * You <b>must</b> therefore call {@link #assertConsistency()} after you finished adding!
	 * </p>
	 * @throws InconsistentUpdateProceduresException if an inconsistent situation would be caused by adding the given element.
	 */
	@Override
	public boolean add(UpdateProcedure updateProcedure)
	throws InconsistentUpdateProceduresException, UnsupportedOperationException, ClassCastException, NullPointerException, IllegalArgumentException
	{
		assertWritable();

		if (updateProcedure == null)
			throw new NullPointerException("updateProcedure must not be null!");

		String moduleID = updateProcedure.getModuleID();
		//Preparing a from-map
		SortedMap<Version, UpdateStep> from2updateStep = moduleID2from2updateStep.get(moduleID);
		if (from2updateStep == null) {
			from2updateStep = new TreeMap<Version, UpdateStep>();
			moduleID2from2updateStep.put(moduleID, from2updateStep);
		}

		try {
			Version fromVersion = updateProcedure.getFromVersion();
			UpdateStep updateStep = from2updateStep.get(fromVersion);
			if (updateStep == null) {
				updateStep = new UpdateStep(updateProcedure); //Adding new UpdateStep
				from2updateStep.put(fromVersion, updateStep);
				incrementSize(moduleID, 1);
				return true;
			}

			try {
				if (updateStep.addUpdateProcedure(updateProcedure))
					incrementSize(moduleID, 1);
			} catch (InconsistentUpdateProceduresException x) {
				if (InconsistentUpdateProceduresException.Reason.toVersionMismatch == x.getReason()) {
					StringBuilder updateProcedureClassesOfUpdateStep = new StringBuilder();
					for (UpdateProcedure up : updateStep.getUpdateProcedures()) {
						if (updateProcedureClassesOfUpdateStep.length() > 0)
							updateProcedureClassesOfUpdateStep.append(", ");

						updateProcedureClassesOfUpdateStep.append(up.getClass().getName());
					}

					throw new InconsistentUpdateProceduresException(
							InconsistentUpdateProceduresException.Reason.overlap,
							"At least the update procedures " + updateProcedureClassesOfUpdateStep + " have moduleID='" + updateStep.getModuleID() + "', fromVersion='" + updateStep.getFromVersion() + "' and toVersion='" + updateStep.getToVersion() + "', but at least one update procedure overlaps with toVersion='" + updateProcedure.getToVersion() + "'!"
					);
				}
				throw x;
			}

			return true;
		} finally {
			if (from2updateStep.isEmpty())
				moduleID2from2updateStep.remove(moduleID);
		}
	}

	/**
	 * Increment or decrement the size. For decrementing, use a negative <code>increment</code> value.
	 *
	 * @param moduleID the moduleID.
	 * @param increment the increment (when &gt; 0) or decrement (when &lt; 0).
	 */
	private void incrementSize(String moduleID, int increment)
	{
		if (moduleID == null)
			throw new IllegalArgumentException("moduleID == null");

		__incrementSize(moduleID, increment);
		__incrementSize(null, increment); // null is an alias for the sum of all modules
	}

	private void __incrementSize(String moduleID, int increment)
	{
		Integer size = moduleID2size.get(moduleID);
		size = (size == null ? 0 : size) + increment;
		moduleID2size.put(moduleID, size);
	}

	/**
	 * Check whether the {@link UpdateProcedure}s collected in this instance of {@link UpdateProcedureSet} form
	 * a consistent state that can be used for a clean update. Since only some illegal states can be
	 * detected {@link #add(UpdateProcedure) already when adding an update procedure}, you have to call this
	 * method after you finished populating this <code>UpdateProcedureSet</code>.
	 *
	 * @throws InconsistentUpdateProceduresException if an inconsistent state has been detected.
	 */
	public void assertConsistency()
	throws InconsistentUpdateProceduresException
	{
		UpdateProcedure lastUpdateProcedure = null;
		for (UpdateProcedure updateProcedure : this) {
			// At the first iteration, lastUpdateProcedure is null, of course.
			// Version numbers are scoped by module, thus we don't verify anything if moduleIDs are different.
			if (lastUpdateProcedure != null && lastUpdateProcedure.getModuleID().equals(updateProcedure.getModuleID()))
			{
				// If the module of the last and the current UpdateProcedure is the same,
				// the version numbers must either be the same or they must be consecutive (without holes).
				if (lastUpdateProcedure.getFromVersion().equals(updateProcedure.getFromVersion()))
				{
					// If the fromVersion is still the same, the toVersion must be the same, too!
					// This should never happen, btw., because the add method should already prevent it!
					if (!lastUpdateProcedure.getToVersion().equals(updateProcedure.getToVersion()))
						throw new InconsistentUpdateProceduresException(
								InconsistentUpdateProceduresException.Reason.toVersionMismatch,
								"FUCKING SHIT! THIS SHOULD NEVER HAPPEN! THIS SITUATION SHOULD HAVE BEEN PREVENTED BY THE 'add' METHOD!!! The 'toVersion' of the UpdateProcedure " + lastUpdateProcedure + " does not match the 'toVersion' of this UpdateProcedure: " + updateProcedure
						);
				}
				else {
					if (!lastUpdateProcedure.getToVersion().equals(updateProcedure.getFromVersion()))
						throw new InconsistentUpdateProceduresException(
								InconsistentUpdateProceduresException.Reason.hole,
								"There is a hole (non-consecutive updates): The 'toVersion' of the UpdateProcedure " + lastUpdateProcedure + " does not match the 'fromVersion' of this UpdateProcedure: " + updateProcedure
						);
				}
			}

			lastUpdateProcedure = updateProcedure;
		}
	}

	@Override
	public boolean addAll(Collection<? extends UpdateProcedure> c) {
		boolean result = false;
		for (UpdateProcedure updateProcedure : c) {
			if (add(updateProcedure))
				result = true;
		}
		return result;
	}

	@Override
	public void clear() {
		moduleID2from2updateStep.clear();
		moduleID2size.clear();
	}

	@Override
	public boolean contains(Object o) {
		// If the passed object is not an instance of UpdateProcedure (this includes null!), it definitely
		// cannot be an element in this set!
		if (!(o instanceof UpdateProcedure))
			return false;

		// Now that we know that it is an UpdateProcedure, we can use the information in it to do highly efficient lookups.
		UpdateProcedure updateProcedure = (UpdateProcedure) o;
		SortedMap<Version, UpdateStep> from2updateStep = moduleID2from2updateStep.get(updateProcedure.getModuleID());
		if (from2updateStep == null)
			return false;

		UpdateStep updateStep = from2updateStep.get(updateProcedure.getFromVersion());
		if (updateStep == null)
			return false;

		return updateStep.getUpdateProcedures().contains(updateProcedure);
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		if (c == null)
			throw new NullPointerException("collection must not be null!");

		for (Object object : c) {
			if (!contains(object))
				return false;
		}
		return true;
	}

	@Override
	public boolean isEmpty() {
		return size() == 0;
	}

	private final class UpdateProcedureIterator implements Iterator<UpdateProcedure>
	{
		private Iterator<SortedMap<Version, UpdateStep>> from2updateStepIterator = moduleID2from2updateStep.values().iterator();
		private Iterator<UpdateStep> updateStepIterator = null;
		private Iterator<UpdateProcedure> updateProcedureIterator = null;
		private UpdateProcedure updateProcedure = null;

		private UpdateProcedure getNext()
		{
			if (updateProcedure != null)
				return updateProcedure;

			if (updateProcedureIterator != null && !updateProcedureIterator.hasNext())
				updateProcedureIterator = null;

			if (updateStepIterator != null && !updateStepIterator.hasNext())
				updateStepIterator = null;

			if (updateProcedureIterator == null && updateStepIterator == null) {
				if (!from2updateStepIterator.hasNext())
					return null;

				SortedMap<Version, UpdateStep> map = from2updateStepIterator.next();
				updateStepIterator = map.values().iterator();
			}

			if (updateProcedureIterator == null) {
				if (!updateStepIterator.hasNext())
					return getNext();

				UpdateStep updateStep = updateStepIterator.next();
				updateProcedureIterator = updateStep.getUpdateProcedures().iterator();
			}

			if (!updateProcedureIterator.hasNext())
				return getNext(); // in case, the new iterator is empty from the beginning (should never happen!), we restart as the iterator is checked at the beginning of this method.

			updateProcedure = updateProcedureIterator.next();
			return updateProcedure;
		}

		@Override
		public boolean hasNext() {
			return getNext() != null;
		}

		@Override
		public UpdateProcedure next() {
			UpdateProcedure next = getNext();
			if (next == null)
				throw new NoSuchElementException();

			updateProcedure = null; // Clear the prepared updateProcedure to make getNext() fetch a new one.
			return next;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException("The remove operation is currently not yet implemented.");
		}
	}

	@Override
	public Iterator<UpdateProcedure> iterator() {
		return new UpdateProcedureIterator();
	}

	@Override
	public boolean remove(Object o) {
		assertWritable();

		// Instead of an exception, simply return false.
		if (!(o instanceof UpdateProcedure))
			return false;

		UpdateProcedure updateProcedure = (UpdateProcedure)o;
		SortedMap<Version, UpdateStep> from2updateStep = moduleID2from2updateStep.get(updateProcedure.getModuleID());
		if (from2updateStep == null)
			return false;

		UpdateStep updateStep = from2updateStep.get(updateProcedure.getFromVersion());
		if (updateStep == null)
			return false;

		boolean result = updateStep.removeUpdateProcedure(updateProcedure);

		// If the element was removed, we *MUST* decrement our size!
		if (result)
			incrementSize(updateProcedure.getModuleID(), -1);

		// Clean up empty Maps + Collections.
		if (updateStep.getUpdateProcedures().isEmpty())
			from2updateStep.remove(updateProcedure.getFromVersion());

		if (from2updateStep.isEmpty())
			moduleID2from2updateStep.remove(updateProcedure.getModuleID());

		return result;
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		assertWritable();

		if (c == null)
			throw new NullPointerException("removed object must not be null!");

		boolean result = false;
		for (Object o : c) {
			if (remove(o))
				result = true;
		}

		return result;
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		assertWritable();

		if (c == null)
			throw new NullPointerException("removed object must not be null!");

		Map<String, Set<UpdateProcedure>> moduleID2updateProcedureSet = new HashMap<String, Set<UpdateProcedure>>();
		for (Object o : c) {
			if (!(o instanceof UpdateProcedure)) {
				throw new IllegalArgumentException("removed object must be UpdateProcedure!");
			}

			UpdateProcedure updateProcedure = (UpdateProcedure)o;
			String moduleID = updateProcedure.getModuleID();
			Set<UpdateProcedure> updateProcedureSet = moduleID2updateProcedureSet.get(moduleID);
			if (updateProcedureSet == null) {
				updateProcedureSet = new HashSet<UpdateProcedure>();
				moduleID2updateProcedureSet.put(moduleID, updateProcedureSet);
			}

			updateProcedureSet.add(updateProcedure);
		}

		for (String moduleID : moduleID2updateProcedureSet.keySet()) {
			Set<UpdateProcedure> updateProcedureSet = moduleID2updateProcedureSet.get(moduleID);
			SortedMap<Version, UpdateStep> from2updateStep = moduleID2from2updateStep.get(moduleID);

			for (UpdateProcedure up : updateProcedureSet) {
				UpdateStep updateStep = from2updateStep.get(up.getFromVersion());
				updateStep.getUpdateProcedures().retainAll(updateProcedureSet);
			}
		}

		return true;
	}

	@Override
	public int size() {
		Integer size;
		if (subSet_moduleID != null)
			size = parent.moduleID2size.get(subSet_moduleID);
		else if (subSet_fromIncluding != null) {
			// Very unefficient, but this mode of operation is very unlikely, anyway, thus we only support it in a rudimentary implementation.
			int c = 0;
			for (UpdateProcedure up : this) {
				if (InternalSubMap2.isContained(up, subSet_fromIncluding, subSet_toExcluding))
					++c;
			}
			size = c;
		}
		else
			size = moduleID2size.get(null);

		if (size != null)
			return size;
		else
			return 0;
	}

	@Override
	public Object[] toArray() {
		int size = size();
		Object[] r = new Object[size];
		int idx = -1;
		for (Iterator<UpdateProcedure> it = this.iterator(); it.hasNext(); )
			r[++idx] = it.next();

		return r;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T[] toArray(T[] a) {
		int size = size();
		T[] r = a.length >= size ? a : (T[])java.lang.reflect.Array.newInstance(a.getClass().getComponentType(), size);
		int idx = -1;
		for (Iterator<UpdateProcedure> it = this.iterator(); it.hasNext(); )
			r[++idx] = (T) it.next();

		if (++idx < r.length)
			r[idx] = null; // null-terminate

		return r;
	}

	/**
	 * Get a sub-set of this {@link UpdateProcedureSet} which contains only the {@link UpdateProcedure}s
	 * for the module specified by the given <code>moduleID</code>. The sub-set is backed by this
	 * <code>UpdateProcedureSet</code>, but currently read-only.
	 *
	 * @param moduleID the identifier of the module for which to provide a sub-set.
	 * @return the sub-set.
	 */
	public UpdateProcedureSet subSet(String moduleID)
	{
		if (parent != null)
			throw new UnsupportedOperationException("It is currently not possible to obtain a sub-set of a sub-set!");

		if (moduleID == null)
			throw new IllegalArgumentException("moduleID == null");

		return new UpdateProcedureSet(this, moduleID);
	}

	/**
	 * @return All ModuleIDs known to this 
	 */
	public Set<String> getModuleIDs() {
		Set<String> keySet = new HashSet<String>(moduleID2size.keySet());
		keySet.remove(null);
		return Collections.unmodifiableSet(keySet);
	}
}
