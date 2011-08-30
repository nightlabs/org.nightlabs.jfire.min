package org.nightlabs.jfire.installer;

import java.util.Properties;

import org.nightlabs.installer.Constants;
import org.nightlabs.installer.base.InstallationException;
import org.nightlabs.installer.base.defaults.DefaultValueProvider;

public class RandomServerIDValueProvider
extends DefaultValueProvider
{
	@Override
	public Properties getValues() throws InstallationException {
		String serverID = Long.toString(System.currentTimeMillis(), 36) + ".server.jfire.org";
		Properties defaultValues = new Properties();
		defaultValues.setProperty(Constants.RESULT, serverID);
		return defaultValues;
	}
}
