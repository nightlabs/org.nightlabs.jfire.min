package org.nightlabs.jfire.log4j;

import org.apache.log4j.helpers.PatternParser;

public class JFirePatternLayout
		extends org.apache.log4j.PatternLayout
{
	public JFirePatternLayout()
	{
	}

	public JFirePatternLayout(String pattern)
	{
		super(pattern);
	}

	@Override
	protected PatternParser createPatternParser(String pattern)
	{
		return new JFirePatternParser(pattern);
	}
}
