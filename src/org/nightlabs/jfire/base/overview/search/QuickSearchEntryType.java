/**
 * 
 */
package org.nightlabs.jfire.base.overview.search;

import org.eclipse.swt.graphics.Image;
import org.nightlabs.base.search.SearchCompositeImage;
import org.nightlabs.i18n.I18nText;
import org.nightlabs.progress.ProgressMonitor;

/**
 * Interface which defines an QuickSearchEntryType which can be used 
 * e.g. in the Overview to perform a quick search with only a search text 
 * in a special context defined by this QuickSearchEntryType
 *  
 * @author Daniel Mazurek - daniel <at> nightlabs <dot> de
 *	
 */
public interface QuickSearchEntryType 
{
	/**
	 * returns the text to search for
	 * @return the text to search for
	 */
	String getSearchText();
	
	/**
	 * sets the search text
	 * @param searchText the search text to set
	 */
	void setSearchText(String searchText);
	
	/**
	 * returns the optional image, may be null
	 * should be 16x16 pixels
	 * 
	 * @return the optional image
	 */
	Image getImage();
	
	/**
	 * returns the optional decorator image, may be null
	 * should be 8x8 pixels
	 * 
	 * @return the optional decorator image
	 */
	Image getDecoratorImage();
	
	/**
	 * returns the optional composed decorator image {@link SearchCompositeImage}
	 * @return the composed Image out of {@link #getDecoratorImage()} if it is not null
	 */
	Image getComposedDecoratorImage();
	
	/**
	 * returns the multilanguage capable name of the QuickSearchEntryType 
	 * @return the name of the QuickSearchEntryType as {@link I18nText}
	 */
	String getName();
//	I18nText getName();
	
	/**
	 * performs the search with the given search text returned
	 * by {@link #getSearchText()}
	 * 
	 * @return the result of the search
	 */
	Object search(ProgressMonitor monitor);
	
	/**
	 * sets the range for the result
	 *  
	 * @param minInclude the minimum range which is inlcuded
	 * @param maxExclude the maximum range which is excluded
	 */
	void setResultRange(long minInclude, long maxExclude);
}
