package org.nightlabs.jfire.log4j;

import org.nightlabs.jfire.init.InitException;
import org.nightlabs.jfire.serverinit.ServerInitialiserDelegate;

public class RegisterJFirePatternConverterDelegatesServerInitialiser
		extends ServerInitialiserDelegate
{

	@Override
	public void initialise()
			throws InitException
	{
		for(JFirePatternConverterDelegateImpl.Type type : JFirePatternConverterDelegateImpl.Type.values())
			JFirePatternConverter.registerDelegate(new JFirePatternConverterDelegateImpl(type));
	}

}
