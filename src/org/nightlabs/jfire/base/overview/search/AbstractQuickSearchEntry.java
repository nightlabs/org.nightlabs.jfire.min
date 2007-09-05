/**
 * 
 */
package org.nightlabs.jfire.base.overview.search;

import org.eclipse.swt.graphics.Image;
import org.nightlabs.base.search.SearchCompositeImage;
import org.nightlabs.i18n.I18nText;
import org.nightlabs.i18n.I18nTextBuffer;


/**
 * @author Daniel Mazurek - daniel <at> nightlabs <dot> de
 *
 */
public abstract class AbstractQuickSearchEntry 
implements QuickSearchEntryType 
{
	private String searchText = null;
	public String getSearchText() {
		return searchText;
	}
	public void setSearchText(String searchText) {
		this.searchText = searchText;
	}

//	private I18nText name = new I18nTextBuffer();
//	public I18nText getName() {
//		return name;
//	}
	
	public Image getDecoratorImage() {
		return null;
	}
	
	public Image getImage() {
		return null;
	}
	
	private Image composedDecoratorImage = null;
	public Image getComposedDecoratorImage() 
	{
		if (composedDecoratorImage == null && getDecoratorImage() != null) {
			composedDecoratorImage = new SearchCompositeImage(getDecoratorImage()).createImage();
		}
		return composedDecoratorImage;
	}
	
	protected long minInclude = 0;
	protected long maxExclude = Long.MAX_VALUE;
	public void setResultRange(long minInclude, long maxExclude) {
		this.minInclude = minInclude;
		this.maxExclude = maxExclude;
	}
			
}
