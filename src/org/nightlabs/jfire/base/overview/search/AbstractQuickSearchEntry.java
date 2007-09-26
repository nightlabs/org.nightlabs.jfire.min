package org.nightlabs.jfire.base.overview.search;

/**
 * Abstract base class for {@link QuickSearchEntry}
 * 
 * @author Daniel Mazurek - daniel <at> nightlabs <dot> de
 */
public abstract class AbstractQuickSearchEntry 
implements QuickSearchEntry 
{
	private QuickSearchEntryFactory factory = null;
	private String searchText = null;
	private long minInclude = 0;
	private long maxExclude = Long.MAX_VALUE;
	
	public AbstractQuickSearchEntry(QuickSearchEntryFactory factory) {
		super();
		this.factory = factory;
	}
		
	public String getSearchText() {
		return searchText;
	}
	public void setSearchText(String searchText) {
		this.searchText = searchText;
	}
				
	public void setResultRange(long minInclude, long maxExclude) {
		this.minInclude = minInclude;
		this.maxExclude = maxExclude;
	}
	
	public long getMinIncludeRange() {
		return minInclude;
	}
	
	public long getMaxExcludeRange() {
		return maxExclude;
	}
	
	public QuickSearchEntryFactory getFactory() {
		return factory;
	}
	
}
