package org.nightlabs.jfire.update.base;

import java.util.Map;
import java.util.SortedMap;

import org.nightlabs.version.Version;

class InternalMapEntry implements Map.Entry<String, SortedMap<Version, UpdateStep>>
{
	private String key;
	private SortedMap<Version, UpdateStep> value;

	public InternalMapEntry(String key, SortedMap<Version, UpdateStep> value) {
		this.key = key;
		this.value = value;
	}

	@Override
	public String getKey() {
		return key;
	}

	@Override
	public SortedMap<Version, UpdateStep> getValue() {
		return value;
	}

	@Override
	public SortedMap<Version, UpdateStep> setValue(SortedMap<Version, UpdateStep> value) {
		throw new UnsupportedOperationException();
	}
}