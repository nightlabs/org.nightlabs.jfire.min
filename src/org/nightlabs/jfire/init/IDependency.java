package org.nightlabs.jfire.init;

import org.nightlabs.jfire.datastoreinit.Resolution;

public interface IDependency<I> {
	public Resolution getResolution();
}
