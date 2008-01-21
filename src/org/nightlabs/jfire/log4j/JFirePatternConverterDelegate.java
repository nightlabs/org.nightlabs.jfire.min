package org.nightlabs.jfire.log4j;

import org.apache.log4j.spi.LoggingEvent;

public interface JFirePatternConverterDelegate
{
	 String getDelegateID();

	 String convert(LoggingEvent event);
}
