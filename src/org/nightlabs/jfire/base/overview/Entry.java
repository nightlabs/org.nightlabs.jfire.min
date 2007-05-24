package org.nightlabs.jfire.base.overview;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.widgets.Composite;

/**
 * Object created by {@link EntryFactory}. Entrys are held b
 *  
 * @author Daniel.Mazurek [at] NightLabs [dot] de
 *
 */
public interface Entry 
{
	/**
	 * Performs what should be done when this {@link Entry} is activated. (e.g. by double-click etc.)
	 */
	void handleActivation();
	
	/**
	 * returns the Composite created by the controller
	 * @return the Composite created by the controller
	 */
	Composite createEntryComposite(Composite parent);
	
	/**
	 * document
	 * @param parent
	 * @return
	 */
	Composite createCategoryEntryComposite(Composite parent);
	
	/**
	 * returns the id of the {@link Entry}
	 * @return the id of the {@link Entry}
	 */
	String getID();
	
	/**
	 * returns the optional menuManager, may be null
	 * @return the optional menuManager
	 */
	MenuManager getMenuManager();
	
	/**
	 * returns the optional SelectionProvider, may be null
	 * @return the optional SelectionProvider 
	 */
	ISelectionProvider getSelectionProvider();
	
	
	EntryFactory getEntryFactory();
	
}
