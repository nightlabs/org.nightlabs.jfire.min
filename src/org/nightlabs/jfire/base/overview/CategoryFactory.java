package org.nightlabs.jfire.base.overview;

import java.util.List;

import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.swt.graphics.Image;

/**
 * A {@link CategoryFactory} can be used to create {@link Category}s. It is used for example in
 * the Overview perspective. CategoryFactories can be registered via the extension-points
 * that use the schema like (schema/overview.exsd) and a extension point processor like
 * {@link OverviewRegistry}.
 * <p>
 * One known extension-point ist org.nightlabs.jfire.base.overview.
 *  
 * @author Daniel.Mazurek [at] NightLabs [dot] de
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 */
public interface CategoryFactory
extends IExecutableExtension
{
	/**
	 * returns the name of the category
	 * @return the name of the category
	 */
	String getName();
	
	/**
	 * returns the image of the category 
	 * @return the image of the category
	 */
	Image getImage();
	
	/**
	 * returns the id of the category 
	 * @return the id of the category
	 */
	public String getCategoryID();

	/**
	 * returns the index of the category
	 * @return the index of the category
	 */
	public int getIndex();
	
	/**
	 * Returns the list of {@link EntryFactory}s in this {@link CategoryFactory}. 
	 * This list is modifyable. Changes will reflect on the next {@link Category}
	 * created by this {@link CategoryFactory}.
	 * 
	 * @return The list of {@link EntryFactory}s in this {@link CategoryFactory}.
	 */
	List<EntryFactory> getEntryFactories();
	
	/**
	 * Creates a new {@link Category}. The new Category will not have any {@link Entry}s, though.
	 * @return A new {@link Category}.
	 */
	public Category createCategory();
	
	/**
	 * Creates a new {@link Category} along with all its entries defined by {@link #getEntryFactories()}.
	 * @return A new {@link Category} with all its entries.
	 */
	public Category createCategoryWithEntries();
}
