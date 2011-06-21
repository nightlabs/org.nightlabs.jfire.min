package org.nightlabs.jfire.update.base;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeSet;

import org.nightlabs.version.Version;

/**
 * Highly efficient (currently read-only) <b>backed</b> sub-map-implementation for
 * {@link UpdateProcedureSet#subSet(String)}.
 *
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
class InternalSubMap1 implements SortedMap<String, SortedMap<Version,UpdateStep>>
{
	private SortedMap<String, SortedMap<Version, UpdateStep>> parent_moduleID2from2updateStep;
	private String moduleID;

	public InternalSubMap1(
			SortedMap<String, SortedMap<Version, UpdateStep>> parent_moduleID2from2updateStep,
			String moduleID
	)
	{
		if (parent_moduleID2from2updateStep == null)
			throw new IllegalArgumentException("parent_moduleID2from2updateStep == null");

		if (moduleID == null)
			throw new IllegalArgumentException("moduleID == null");

		this.parent_moduleID2from2updateStep = parent_moduleID2from2updateStep;
		this.moduleID = moduleID;
	}

	@Override
	public Comparator<? super String> comparator() {
		return null;
	}

	@Override
	public Set<Map.Entry<String, SortedMap<Version, UpdateStep>>> entrySet() {
		SortedMap<Version,UpdateStep> map = parent_moduleID2from2updateStep.get(moduleID);
		if (map == null || map.isEmpty())
			return Collections.emptySet();
		else {
			SortedSet<Map.Entry<String, SortedMap<Version, UpdateStep>>> result = new TreeSet<Map.Entry<String,SortedMap<Version,UpdateStep>>>();
			result.add(new InternalMapEntry(moduleID, map));
			return Collections.unmodifiableSortedSet(result);
		}
	}

	@Override
	public String firstKey() {
		if (isEmpty())
			throw new NoSuchElementException();
		else
			return moduleID;
	}

	@Override
	public SortedMap<String, SortedMap<Version, UpdateStep>> headMap(String toKey) {
		throw new UnsupportedOperationException("NYI"); // this method is not needed by us
	}

	@Override
	public Set<String> keySet() {
		if (isEmpty())
			return Collections.emptySet();
		else
			return Collections.singleton(moduleID);
	}

	@Override
	public String lastKey() {
		if (isEmpty())
			throw new NoSuchElementException();
		else
			return moduleID;
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
		SortedMap<Version,UpdateStep> map = parent_moduleID2from2updateStep.get(moduleID);
		if (map == null || map.isEmpty())
			return Collections.emptySet();
		else
			return Collections.singleton(map);
	}

	@Override
	public void clear() {
		throw new UnsupportedOperationException("read-only!");
	}

	@Override
	public boolean containsKey(Object key) {
		return moduleID.equals(key);
	}

	@Override
	public boolean containsValue(Object value) {
		throw new UnsupportedOperationException("NYI"); // this method is not needed by us
	}

	@Override
	public SortedMap<Version, UpdateStep> get(Object key) {
		if (!moduleID.equals(key))
			return null;

		SortedMap<Version,UpdateStep> map = parent_moduleID2from2updateStep.get(moduleID);
		return map;
	}

	@Override
	public boolean isEmpty() {
		SortedMap<Version,UpdateStep> map = parent_moduleID2from2updateStep.get(moduleID);
		return map == null || map.isEmpty();
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
		SortedMap<Version,UpdateStep> map = parent_moduleID2from2updateStep.get(moduleID);
		return map == null ? 0 : map.size();
	}
}