package org.nightlabs.jfire.init;

import java.util.Comparator;

public class InvocationInitComparator<I extends InvocationInit<?, ?>>
implements Comparator<I> {
	@Override
	public int compare(I o1, I o2) {
		int prioDiff = o1.getPriority() - o2.getPriority();
		if (prioDiff != 0)
			return prioDiff;
		else
			return o1.getName().compareTo(o2.getName());
	}
}