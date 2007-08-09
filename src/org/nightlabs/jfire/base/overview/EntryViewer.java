/**
 * 
 */
package org.nightlabs.jfire.base.overview;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.widgets.Composite;
import org.nightlabs.base.selection.SelectionProvider;

/**
 * {@link EntryViewer}s might be used to create a detailed
 * view of a category {@link Entry}. They are created
 * by the Entry.
 * <p>
 * One use case of the {@link EntryViewer} is {@link OverviewEntryEditor}
 * that will display its {@link Composite}.
 * </p>
 * 
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 */
public interface EntryViewer {

	/**
	 * Creates the Composite for this {@link EntryViewer}.
	 * <p>
	 * This method should only be called once per EntryViewer.
	 * Note, that implementations might ensure this and
	 * throw an exception of it is called more than once.
	 * </p>
	 * @return The {@link Composite} of this {@link EntryViewer}.
	 */
	Composite createComposite(Composite parent);
	
	/**
	/**
	 * Returns the {@link Composite} of this {@link EntryViewer} that
	 * was created by {@link #createComposite(Composite)}.
	 * <p>
	 * This method should only be called after {@link #createComposite(Composite)} was called.
	 * Note, that implementations might ensure this and
	 * throw an exception if it is called before {@link #createComposite(Composite)}.
	 * </p>
	 * @return The {@link Composite} created by {@link #createComposite(Composite)}.
	 */
	Composite getComposite();

	/**
	 * Returns the {@link Entry} that created this {@link EntryViewer}. 
	 * @return The {@link Entry} that created this {@link EntryViewer}.
	 */
	Entry getEntry();
	
	/**
	 * Returns the optional {@link MenuManager}, may be null
	 * @return The optional menuManager
	 */
	MenuManager getMenuManager();
	
	/**
	 * Returns the optional {@link SelectionProvider}, may be null
	 * @return The optional SelectionProvider 
	 */
	ISelectionProvider getSelectionProvider();
	
	/**
	 * Returns the optional {@link ToolBarManager}, may be null
	 * @return The optional toolbarManager
	 */
	ToolBarManager getToolBarManager();
	
}
