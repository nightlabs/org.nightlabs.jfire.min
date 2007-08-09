package org.nightlabs.jfire.base.overview;

import java.util.List;

import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.swt.graphics.Image;

/**
 * A {@link CategoryFactory} can be used to create {@link Category}s. It is used for example in
 * the Overview perspective. CategoryFactories can be registered via the extension-points
 * that use the schema like (schema/overview.exsd) and an extension point processor like
 * {@link OverviewRegistry}.
 * <p>
 * One known extension-point is org.nightlabs.jfire.base.overview.
 * </p>
 * <p>
 * An extension-point applying the schema mentioned should have the possibility to 
 * register {@link EntryFactory}s for a CategoryFactory. {@link Entry}s are sub-elements
 * of {@link Category}s and are processed by the category.
 * </p>
 * <p>
 * Usually the {@link DefaultCategoryFactory} is used for extensions 
 * (&lt;categoryFactory&gt; with no class-attribute specified). This will create a 
 * {@link DefaultCategory} that itself will create a Composite displaying all entries
 * in a Table viewer. However another class implementing this interface could be
 * registered that creates a different category implementation which handles
 * the entries in a completly different way.
 * </p> 
 * @author Daniel.Mazurek [at] NightLabs [dot] de
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 */
public interface CategoryFactory
extends IExecutableExtension
{
	/**
	 * Returns the name of the category
	 * @return the name of the category
	 */
	String getName();
	
	/**
	 * Returns the image of the category 
	 * @return the image of the category
	 */
	Image getImage();
	
	/**
	 * Returns the id of the category 
	 * @return the id of the category
	 */
	public String getCategoryID();

	/**
	 * Returns the index of the category
	 * @return the index of the category
	 */
	public int getIndex();
	
	/**
	 * Returns the list of {@link EntryFactory}s in this {@link CategoryFactory}. 
	 * This list is modifyable. Changes will reflect on the next {@link Category}
	 * created by this {@link CategoryFactory}.
	 * <p>
	 * From the start this will have been filled with the {@link EntryFactory}s
	 * registered via the extension point.
	 * </p>
	 * 
	 * @return The list of {@link EntryFactory}s in this {@link CategoryFactory}.
	 */
	List<EntryFactory> getEntryFactories();
	
	/**
	 * Creates a new {@link Category}. 
	 * The new Category will not have any {@link Entry}s, though.
	 * This means you will have to call {@link Category#createEntries()}
	 * in order to create the registered entries for the new 
	 * Category.
	 * 
	 * @return A new {@link Category}.
	 */
	public Category createCategory();
}
