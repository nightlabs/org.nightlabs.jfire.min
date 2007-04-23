package org.nightlabs.jfire.base.overview;

import org.eclipse.jface.action.CoolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.widgets.Composite;

/**
 * The Controller Object which is responsible for the handling
 * of an {@link Entry}
 *  
 * @author Daniel.Mazurek [at] NightLabs [dot] de
 *
 */
public interface EntryViewController 
{
	/**
	 * returns the Composite created by the controller
	 * @return the Composite created by the controller
	 */
	Composite createComposite(Composite parent);
	
	/**
	 * returns the id of the {@link EntryViewController}
	 * @return the id of the {@link EntryViewController}
	 */
	String getID();
	
	/**
	 * returns the menuManager, may be null
	 * @return the menuManager
	 */
	MenuManager getMenuManager();
	
	/**
	 * returns the optional SelectionProvider, may be null
	 * @return the optional SelectionProvider 
	 */
	ISelectionProvider getSelectionProvider();
	
//	/**
//	 * returns the toolBarManager, may be null
//	 * @return the toolBarManager
//	 */
//	ToolBarManager getToolBarManager();
//
//	/**
//	 * returns the coolBarManager, may be null
//	 * @return the coolBarManager
//	 */
//	CoolBarManager getCoolBarManager();
	
}
