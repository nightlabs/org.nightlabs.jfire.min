package org.nightlabs.jfire.serverupdate.base;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.nightlabs.version.Version;

/**
 * Very unefficient implementation of a backed sub-map for the unlikely usage of
 * {@link UpdateProcedureSet#subSet(UpdateProcedure, UpdateProcedure)}.
 * This method is supported but we (1) probably won't use it, (2) the number of elements in this data structure is quite small
 * and thus this simple implementation should be OK for now.
 *
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
class InternalSubMap2 implements SortedMap<String, SortedMap<Version,UpdateStep>>
{
	private SortedMap<String, SortedMap<Version, UpdateStep>> parent_moduleID2from2updateStep;
	private UpdateProcedure fromIncluding;
	private UpdateProcedure toExcluding;

	public InternalSubMap2(
			SortedMap<String, SortedMap<Version, UpdateStep>> parent_moduleID2from2updateStep,
			UpdateProcedure fromIncluding, UpdateProcedure toExcluding
	)
	{
		if (parent_moduleID2from2updateStep == null)
			throw new IllegalArgumentException("parent_moduleID2from2updateStep == null");

		if (fromIncluding == null)
			throw new IllegalArgumentException("fromIncluding == null");

		if (toExcluding == null)
			throw new IllegalArgumentException("toExcluding == null");

		this.parent_moduleID2from2updateStep = parent_moduleID2from2updateStep;
		this.fromIncluding = fromIncluding;
		this.toExcluding = toExcluding;

		if (fromIncluding.compareTo(toExcluding) <= 0) {
			// They are the same or the 'from' is greater than the 'to' and thus, this map is empty!
			this.fromIncluding = null;
			this.toExcluding = null;
		}
	}

	@Override
	public Comparator<? super String> comparator() {
		return null;
	}

	private Map<String, SortedMap<Version, UpdateStep>> createInternalSubMap() // currently copies :-( inefficient, but OK.
	{
		if (fromIncluding == null)
			return Collections.emptyMap();

		// This is not clean as it is a copy and not backed, but we probably never need this anyway and thus it should be sufficient.
		SortedMap<String, SortedMap<Version, UpdateStep>> result = new TreeMap<String, SortedMap<Version,UpdateStep>>();

		for (Map.Entry<String, SortedMap<Version, UpdateStep>> me1 : parent_moduleID2from2updateStep.entrySet()) {
			SortedMap<Version, UpdateStep> mapCopy = null;
			for (Map.Entry<Version, UpdateStep> me2 : me1.getValue().entrySet()) {
				UpdateStep updateStepCopy = null;
				for (UpdateProcedure up : me2.getValue().getUpdateProcedures()) {
					if (isContained(up)) {
						if (updateStepCopy == null)
							updateStepCopy = new UpdateStep(up);
						else
							updateStepCopy.addUpdateProcedure(up);
					}
				}
				if (updateStepCopy != null) {
					if (mapCopy == null)
						mapCopy = new TreeMap<Version, UpdateStep>();

					mapCopy.put(me2.getKey(), updateStepCopy);
				}
			}

			if (mapCopy != null)
				result.put(me1.getKey(), mapCopy);
		}
		return result;
	}

	@Override
	public Set<Map.Entry<String, SortedMap<Version, UpdateStep>>> entrySet() {
		return createInternalSubMap().entrySet();
	}

	@Override
	public String firstKey() {
		if (isEmpty())
			throw new NoSuchElementException();

		return fromIncluding.getModuleID(); // fromIncluding cannot be null, if isEmpty() returned false!
	}

	@Override
	public SortedMap<String, SortedMap<Version, UpdateStep>> headMap(String toKey) {
		throw new UnsupportedOperationException("NYI"); // this method is not needed by us
	}

	@Override
	public Set<String> keySet() {
		return createInternalSubMap().keySet();
	}

	@Override
	public String lastKey() {
		if (isEmpty())
			throw new NoSuchElementException();

		String lastKey = null;
		for (String key : keySet()) // very very unefficient, but no problem ;-) Marco.
			lastKey = key;

		return lastKey;
	}

	@Override
	public SortedMap<String, SortedMap<Version, UpdateStep>> subMap(String fromKey, String toKey) {
		throw new UnsupportedOperationException("NYI"); // this method is not needed by us
	}

	@Override
	public SortedMap<String, SortedMap<Version, UpdateStep>> tailMap(String fromKey) {
		throw new UnsupportedOperationException("NYI"); // this method is not needed by us
	}

	@Override
	public Collection<SortedMap<Version, UpdateStep>> values() {
		return createInternalSubMap().values();
	}

	@Override
	public void clear() {
		throw new UnsupportedOperationException("read-only!");
	}

	@Override
	public boolean containsKey(Object key) {
		return get(key) != null; // very simple, but unefficient :-( Still sufficient ;-) Marco.
	}

	@Override
	public boolean containsValue(Object value) {
		throw new UnsupportedOperationException("NYI"); // this method is not needed by us
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * <b>Important:</b> This implementation returns a <b>non-backed</b> copy of the internal maps!
	 * </p>
	 */
	@Override
	public SortedMap<Version, UpdateStep> get(Object key) {
		if (fromIncluding == null)
			return null;

		SortedMap<Version, UpdateStep> map = parent_moduleID2from2updateStep.get(key);
		if (map == null)
			return map;

		// This is not clean as it is a copy and not backed, but we probably never need this anyway and thus it should be sufficient.
		SortedMap<Version, UpdateStep> mapCopy = new TreeMap<Version, UpdateStep>();
		for (Map.Entry<Version, UpdateStep> me : map.entrySet()) {
			UpdateStep updateStepCopy = null;
			for (UpdateProcedure up : me.getValue().getUpdateProcedures()) {
				if (isContained(up)) {
					if (updateStepCopy == null)
						updateStepCopy = new UpdateStep(up);
					else
						updateStepCopy.addUpdateProcedure(up);
				}
			}
			if (updateStepCopy != null)
				mapCopy.put(me.getKey(), updateStepCopy);
		}

		if (mapCopy.isEmpty())
			return null;

		return mapCopy;
	}

	private boolean isContained(UpdateProcedure updateProcedure)
	{
		if (fromIncluding == null)
			return false;

		return isContained(updateProcedure, fromIncluding, toExcluding);
	}

	public static boolean isContained(UpdateProcedure updateProcedure, UpdateProcedure fromIncluding, UpdateProcedure toExcluding)
	{
		return updateProcedure.compareTo(fromIncluding) >= 0 && updateProcedure.compareTo(toExcluding) < 0;
	}

	@Override
	public boolean isEmpty() {
		return size() == 0;
	}

	@Override
	public SortedMap<Version, UpdateStep> put(String key, SortedMap<Version, UpdateStep> value) {
		throw new UnsupportedOperationException("read-only!");
	}

	@Override
	public void putAll(Map<? extends String, ? extends SortedMap<Version, UpdateStep>> m) {
		throw new UnsupportedOperationException("read-only!");
	}

	@Override
	public SortedMap<Version, UpdateStep> remove(Object key) {
		throw new UnsupportedOperationException("read-only!");
	}

	@Override
	public int size() {
		if (fromIncluding == null)
			return 0;

		return this.entrySet().size();
	}
}