package org.nightlabs.jfire.base.idgenerator;

import org.nightlabs.jfire.idgenerator.IDGenerator;

public class IDGeneratorClient
		extends IDGenerator
{

	@Override
	protected long[] _nextIDs(String namespace, int quantity)
	{
		// obtain EJB if necessary and manage local cache
		throw new UnsupportedOperationException("NYI");
	}

}
