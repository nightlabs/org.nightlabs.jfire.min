package org.nightlabs.jfire.installer;

import java.util.Properties;

import org.nightlabs.installer.Constants;
import org.nightlabs.installer.base.defaults.DefaultValueProvider;

/**
 * @version $Revision$ - $Date$
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public class RandomIDValueProvider extends DefaultValueProvider
{
	/* (non-Javadoc)
	 * @see org.nightlabs.installer.base.DefaultValueProvider#getValues()
	 */
	@Override
	public Properties getValues()
	{
		String template = getConfig().getProperty("template"); //$NON-NLS-1$
		if(template == null)
			template = Messages.getString("RandomIDValueProvider.defaultID"); //$NON-NLS-1$
		String lengthStr = getConfig().getProperty("randomLength"); //$NON-NLS-1$
		int length = 10;
		if(lengthStr != null) {
			try {
				length = Integer.parseInt(lengthStr);
			} catch(NumberFormatException e) {}
		}

		String randomID = String.format(template, getRandomString(length));

		Properties defaultValues = new Properties();
		defaultValues.setProperty(Constants.RESULT, randomID);
		return defaultValues;
	}
	
	private static String getRandomString(int length)
	{
		StringBuffer random = new StringBuffer(length);
		char[] randomChars = {
				'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
				'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
				'0', '1', '2', '3', '4', '5', '6', '7', '8', '9' };
		for(int i=0; i<length; i++)
			random.append(randomChars[(int)Math.round(Math.random()*(randomChars.length-1))]);
		return random.toString();
	}
}
