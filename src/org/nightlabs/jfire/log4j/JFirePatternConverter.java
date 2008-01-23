/**
 * 
 */
package org.nightlabs.jfire.log4j;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.helpers.FormattingInfo;
import org.apache.log4j.helpers.PatternConverter;
import org.apache.log4j.spi.LoggingEvent;

public class JFirePatternConverter extends PatternConverter
{
	private static Map<String, JFirePatternConverterDelegate> delegateMap = Collections.synchronizedMap(new HashMap<String, JFirePatternConverterDelegate>());

	public static void registerDelegate(JFirePatternConverterDelegate delegate)
	{
		delegateMap.put(delegate.getDelegateID(), delegate);
	}

	public static void unregisterDelegate(JFirePatternConverterDelegate delegate)
	{
		delegateMap.remove(delegate.getDelegateID());
	}

	private String delegateID;

	public JFirePatternConverter(FormattingInfo formattingInfo, String delegateID) {
    super(formattingInfo);
    this.delegateID = delegateID;
  }

	@Override
	protected String convert(LoggingEvent event)
	{
		// check whether we are on the same thread - otherwise we don't show the user-info, since it very likely is wrong or non-existent
		String currentThreadName = Thread.currentThread().getName();
		if (currentThreadName == null)
			return "{current thread name unknown}";

		if (!currentThreadName.equals(event.getThreadName()))
			return "{thread mismatch}";

		JFirePatternConverterDelegate delegate = delegateMap.get(delegateID);
		if (delegate == null)
			return "{no delegate \"" + delegateID + "\"}";

		String res;
		try {
			res = delegate.convert(event);
		} catch (Throwable t) {
			res = '{' + t.getClass().getName() + '}';
		}
		return res;
	}
}