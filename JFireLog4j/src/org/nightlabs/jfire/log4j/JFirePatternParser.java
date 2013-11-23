package org.nightlabs.jfire.log4j;

public class JFirePatternParser
		extends org.apache.log4j.helpers.PatternParser
{
	public JFirePatternParser(String pattern)
	{
		super(pattern);
	}

	@Override
	protected void finalizeConverter(char c)
	{
		switch (c) {
			case 'j':
				String delegateID = extractOption();
				addConverter(
						new JFirePatternConverter(formattingInfo, delegateID)
				);
			break;

			default:
				super.finalizeConverter(c);
			break;
		}
	}
}
