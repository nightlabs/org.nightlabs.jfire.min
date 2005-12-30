/* *****************************************************************************
 * JFire - it's hot - Free ERP System - http://jfire.org                       *
 * Copyright (C) 2004-2005 NightLabs - http://NightLabs.org                    *
 *                                                                             *
 * This library is free software; you can redistribute it and/or               *
 * modify it under the terms of the GNU Lesser General Public                  *
 * License as published by the Free Software Foundation; either                *
 * version 2.1 of the License, or (at your option) any later version.          *
 *                                                                             *
 * This library is distributed in the hope that it will be useful,             *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of              *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU           *
 * Lesser General Public License for more details.                             *
 *                                                                             *
 * You should have received a copy of the GNU Lesser General Public            *
 * License along with this library; if not, write to the                       *
 *     Free Software Foundation, Inc.,                                         *
 *     51 Franklin St, Fifth Floor,                                            *
 *     Boston, MA  02110-1301  USA                                             *
 *                                                                             *
 * Or get it online :                                                          *
 *     http://opensource.org/licenses/lgpl-license.php                         *
 *                                                                             *
 *                                                                             *
 ******************************************************************************/

package org.nightlabs.jfire.base.app;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.ICoolBarManager;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.ContributionItemFactory;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;

import org.nightlabs.base.action.NewFileRegistry;
import org.nightlabs.base.action.OpenFileAction;
import org.nightlabs.base.action.ReOpenFileAction;
import org.nightlabs.base.config.RecentFileCfMod;
import org.nightlabs.config.Config;
import org.nightlabs.config.ConfigException;
import org.nightlabs.jfire.base.JFireBasePlugin;

/**
 * Creates the Menu 
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 *
 */
public class JFireActionBuilder extends ActionBarAdvisor
{
	public static final String RECENT_FILES_MENU_ID = "recentFilesMenu";
	
	
	// File-Menu
	private IMenuManager newMenu;
	private IMenuManager recentFilesMenu;
//	private ActionFactory.IWorkbenchAction newAction;
//	private ActionFactory.IWorkbenchAction closeAction;
//	private ActionFactory.IWorkbenchAction closeAllAction;
	private ActionFactory.IWorkbenchAction saveAction;
	private ActionFactory.IWorkbenchAction saveAsAction;
//	private ActionFactory.IWorkbenchAction printAction;
//	private ActionFactory.IWorkbenchAction importAction;
//	private ActionFactory.IWorkbenchAction exportAction;
//	private ActionFactory.IWorkbenchAction propertiesAction;
	private ActionFactory.IWorkbenchAction quitAction;
	private OpenFileAction openAction;
	
	// Help-Menu
//	private ActionFactory.IWorkbenchAction introAction; 
	private ActionFactory.IWorkbenchAction helpAction;
//	private ActionFactory.IWorkbenchAction updateAction;
	private ActionFactory.IWorkbenchAction aboutAction;
	
	// Window-Menu
	private IContributionItem openPerspectiveMenu;
	private IContributionItem showViewMenu;	
	private ActionFactory.IWorkbenchAction preferencesAction;
			
	public JFireActionBuilder(IActionBarConfigurer configurer) {
		super(configurer);
		try {
			fileHistory = (RecentFileCfMod) Config.sharedInstance().createConfigModule(RecentFileCfMod.class);
		} catch (ConfigException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}	
	
	/**
	 * @see org.eclipse.ui.application.ActionBarAdvisor#makeActions(org.eclipse.ui.IWorkbenchWindow)
	 */
	protected void makeActions(IWorkbenchWindow window) {
//	ISharedImages images = window.getWorkbench().getSharedImages();

		// File-Menu		
		newMenu = new MenuManager("New", ActionFactory.NEW.getId());
//		newMenu.add((ActionFactory.NEW.create(window)));
		newMenu.add(new GroupMarker(ActionFactory.NEW.getId()));
		
//		newAction = ActionFactory.NEW.create(window);
		openAction = new OpenFileAction();
		openAction.addPropertyChangeListener(historyFileListener);
		recentFilesMenu = new MenuManager(JFireBasePlugin.getResourceString("menu.openrecentfiles.text"), RECENT_FILES_MENU_ID);
		recentFilesMenu.add(new GroupMarker(IWorkbenchActionConstants.HISTORY_GROUP));
//		closeAction = ActionFactory.CLOSE.create(window);
//		closeAllAction = ActionFactory.CLOSE_ALL.create(window);
		saveAction = ActionFactory.SAVE.create(window);
		saveAsAction = ActionFactory.SAVE_AS.create(window);	
//		printAction = ActionFactory.PRINT.create(window);
//		importAction = ActionFactory.IMPORT.create(window);
//		exportAction = ActionFactory.EXPORT.create(window);
//		propertiesAction = ActionFactory.PROPERTIES.create(window);
		quitAction = ActionFactory.QUIT.create(window);
		
		// Window-Menu
		openPerspectiveMenu = ContributionItemFactory.PERSPECTIVES_SHORTLIST.create(window);
		showViewMenu = ContributionItemFactory.VIEWS_SHORTLIST.create(window);		
		preferencesAction = ActionFactory.PREFERENCES.create(window);
				
		// Help-Menu
//		introAction = ActionFactory.INTRO.create(window);
		helpAction = ActionFactory.HELP_CONTENTS.create(window);	
		aboutAction = ActionFactory.ABOUT.create(window); 
	}

	/**
	 * @see org.eclipse.ui.application.ActionBarAdvisor#fillMenuBar(org.eclipse.jface.action.IMenuManager)
	 */
	public void fillMenuBar(IMenuManager menuBar) 
	{
	  // File-Menu
		IMenuManager fileMenu = new MenuManager("&File", IWorkbenchActionConstants.M_FILE);  //$NON-NLS-2$
		menuBar.add(fileMenu);

		fileMenu.add(new GroupMarker(IWorkbenchActionConstants.FILE_START));
		fileMenu.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
		fileMenu.add(newMenu);
		createNewEntries(newMenu);
    fileMenu.add(openAction); 

    fileMenu.add(recentFilesMenu);
		historyFileMenuManager = recentFilesMenu;
		createHistoryEntries(historyFileMenuManager);
		
		fileMenu.add(new Separator());
//		fileMenu.add(closeAction);
//		fileMenu.add(closeAllAction);
    
    fileMenu.add(new GroupMarker(IWorkbenchActionConstants.SAVE_GROUP));
		fileMenu.add(saveAction);
		fileMenu.add(saveAsAction);
		fileMenu.add(new Separator());
		
//		fileMenu.add(printAction);
		fileMenu.add(new GroupMarker(IWorkbenchActionConstants.IMPORT_EXT));
//		fileMenu.add(importAction);
//		fileMenu.add(exportAction);
		fileMenu.add(new Separator());
				
		fileMenu.add(quitAction);
    fileMenu.add(new GroupMarker(IWorkbenchActionConstants.FILE_END));		
		
    menuBar.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
    
    // Window-Menu
		IMenuManager windowMenu = new MenuManager("&Window", IWorkbenchActionConstants.M_WINDOW);
		menuBar.add(windowMenu);		
		
		// Perspective-SubMenu
		MenuManager openPerspectiveMenuMgr = new MenuManager("Open Perspective", "openPerspective");
		openPerspectiveMenuMgr.add(openPerspectiveMenu);
		windowMenu.add(openPerspectiveMenuMgr);		
		
		// View-SubMenu
		MenuManager showViewMenuMgr = new MenuManager("Show View", "showView");
		showViewMenuMgr.add(showViewMenu);
		windowMenu.add(showViewMenuMgr);		
		windowMenu.add(new Separator());
		
		windowMenu.add(preferencesAction);
		
		// Help-Menu
		IMenuManager helpMenu = new MenuManager("&Help", IWorkbenchActionConstants.M_HELP);  //$NON-NLS-2$
		menuBar.add(helpMenu);
//		helpMenu.add(introAction);
		helpMenu.add(helpAction);
		helpMenu.add(new Separator());		 
		helpMenu.add(aboutAction);
	}

	public void fillCoolBar(ICoolBarManager coolBar) 
	{
	}
	
	public void dispose() 
	{
	  	aboutAction.dispose();
//	  	closeAction.dispose();
//	  	closeAllAction.dispose();
//	  	exportAction.dispose();
	  	helpAction.dispose();
//	  	importAction.dispose();
//	  	introAction.dispose();
//	  	newAction.dispose();

	  	preferencesAction.dispose();
//	  	printAction.dispose();
//	  	propertiesAction.dispose();
	    quitAction.dispose();
	    saveAction.dispose();
	    saveAsAction.dispose();	    
	}
	
	protected RecentFileCfMod fileHistory;
	protected IMenuManager historyFileMenuManager;	
	protected int historyEntries = 0;
	protected int maxHistoryLength = 0;
	protected String firstHistoryID = null; 
	protected String lastHistoryID = null;
		
	protected PropertyChangeListener historyFileListener = new PropertyChangeListener() {	
		public void propertyChange(PropertyChangeEvent arg0) {
			if (arg0.getPropertyName().equals(OpenFileAction.HISTORY_FILE_ADDED)) {
				String fileName = (String) arg0.getNewValue();
				addHistoryFile(historyFileMenuManager, fileName, false);
			}
		}	
	}; 
		
	protected void addHistoryFile(IMenuManager menuMan, String fileName, boolean append) 
	{
		ReOpenFileAction action = new ReOpenFileAction(fileName);
		if (firstHistoryID == null) {
			firstHistoryID = action.getId();
			menuMan.add(action);				
		}
		else 
		{
			if (!append) {
				menuMan.insertBefore(firstHistoryID, action);
				firstHistoryID = action.getId();				
			}
			else
				menuMan.add(action);
		}
		
		historyEntries++;
		
		if (maxHistoryLength == historyEntries)
			lastHistoryID = action.getId();
		
		if (maxHistoryLength < historyEntries) 
		{
			menuMan.remove(lastHistoryID);
			if (!fileHistory.getRecentFileNames().contains(fileName))
				fileHistory.getRecentFileNames().add(fileName);
			
			for (int i=0; i<fileHistory.getRecentFileNames().size()-maxHistoryLength; i++) {
				fileHistory.getRecentFileNames().remove(i);				
			}				
		}
	}
	
	/**
	 * creates the MenuEntries of all previous opened files
	 * @param menuMan The IMenuManager to which the entries should be added
	 */
	protected void createHistoryEntries(IMenuManager menuMan) 
	{
		if (fileHistory != null) {
			List fileNames = fileHistory.getRecentFileNames();
			maxHistoryLength = fileHistory.getMaxHistoryLength();
			if (fileNames.size() != 0) {
				for (int i=fileNames.size()-1; i!=0; i--) {
					String fileName = (String) fileNames.get(i);
					addHistoryFile(menuMan, fileName, true);
				}											
			}
		}			
	}	
		
	protected void createNewEntries(IMenuManager menuMan)
	{
		NewFileRegistry newFileRegistry = NewFileRegistry.sharedInstance(); 
		Map categoryID2Action = newFileRegistry.getCategory2Actions();
		List defaultActions = new ArrayList();
		for (Iterator it = categoryID2Action.keySet().iterator(); it.hasNext(); ) 
		{
			String categoryID = (String) it.next();
			IAction action = (IAction) categoryID2Action.get(categoryID);			
			if (categoryID.equals(NewFileRegistry.DEFAULT_CATEGORY)) {
				defaultActions.add(action);
			}
			else {
				String categoryName = newFileRegistry.getCategoryName(categoryID);
				if (categoryName != null && !categoryName.equals("")) {					
					IMenuManager categoryMenu = new MenuManager(categoryName);
					categoryMenu.add(action);
					menuMan.add(categoryMenu);
				}
			}			
		}
		for (Iterator itDefault = defaultActions.iterator(); itDefault.hasNext(); ) {
			menuMan.add((IAction)itDefault.next());
		}
	}
	
}
