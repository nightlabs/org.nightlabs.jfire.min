package org.nightlabs.jfire.base.security;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.nightlabs.base.dialog.CenteredDialog;
import org.nightlabs.jfire.base.resource.Messages;
import org.nightlabs.jfire.security.User;

/**
 * @author Daniel.Mazurek [at] NightLabs [dot] de
 *
 */
public class UserSearchDialog 
extends CenteredDialog 
{
	/**
	 * @param parentShell
	 * @param searchText
	 */
	public UserSearchDialog(Shell parentShell, String searchText) {
		super(parentShell);
		this.searchText = searchText;
	}

	/**
	 * @param parentShell
	 * @param searchText
	 */
	public UserSearchDialog(IShellProvider parentShell, String searchText) {
		super(parentShell);
		this.searchText = searchText;
	}
	
	@Override
	public void create() {
		setShellStyle(getShellStyle() | SWT.RESIZE);
		super.create();
		getShell().setText(Messages.getString("org.nightlabs.jfire.base.security.UserSearchDialog.SearchUser")); //$NON-NLS-1$
		getShell().setSize(500, 350);
	}

	@Override
	protected Control createDialogArea(Composite parent) 
	{
		userSearchComposite = new UserSearchComposite(parent, SWT.NONE);
		if (searchText != null && !searchText.trim().equals("")) { //$NON-NLS-1$
			userSearchComposite.getUserIDText().setText(searchText);
		}
		userSearchComposite.getUserTable().getTableViewer().addDoubleClickListener(userDoubleClickListener);		
		return userSearchComposite;
	}
	
	private UserSearchComposite userSearchComposite = null;
	private String searchText = ""; //$NON-NLS-1$
	private User selectedUser = null;
	public User getSelectedUser() {
		return selectedUser;
	}

	public static final int SEARCH_ID = IDialogConstants.CLIENT_ID + 1;
	
	@Override
	protected void createButtonsForButtonBar(Composite parent) 
	{
		super.createButtonsForButtonBar(parent);
		Button searchButton = createButton(parent, SEARCH_ID, Messages.getString("org.nightlabs.jfire.base.security.UserSearchDialog.Search"), true); //$NON-NLS-1$
		searchButton.addSelectionListener(searchButtonListener);		
	}
	
	private SelectionListener searchButtonListener = new SelectionListener(){	
		public void widgetSelected(SelectionEvent e) {
			userSearchComposite.searchPressed();
		}	
		public void widgetDefaultSelected(SelectionEvent e) {
			widgetSelected(e);
		}	
	};
	
	@Override
	public boolean close() {
		selectedUser = userSearchComposite.getSelectedUser();
		return super.close();
	}
	
	private IDoubleClickListener userDoubleClickListener = new IDoubleClickListener(){	
		public void doubleClick(DoubleClickEvent event) {
			if (!event.getSelection().isEmpty() && event.getSelection() instanceof StructuredSelection) {
				StructuredSelection sel = (StructuredSelection) event.getSelection();
				if (sel.getFirstElement() instanceof User) {
					selectedUser = (User) sel.getFirstElement();
					close();
				}
			}			
		}	
	};
	
}
