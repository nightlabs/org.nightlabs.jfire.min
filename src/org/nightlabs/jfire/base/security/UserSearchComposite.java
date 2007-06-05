package org.nightlabs.jfire.base.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.jdo.FetchPlan;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.nightlabs.base.composite.XComposite;
import org.nightlabs.base.job.Job;
import org.nightlabs.base.table.AbstractTableComposite;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.base.login.Login;
import org.nightlabs.jfire.base.resource.Messages;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.security.UserManager;
import org.nightlabs.jfire.security.UserManagerUtil;
import org.nightlabs.jfire.security.dao.UserDAO;
import org.nightlabs.jfire.security.id.UserID;
import org.nightlabs.jfire.security.search.UserQuery;
import org.nightlabs.progress.ProgressMonitor;

/**
 * @author Daniel.Mazurek [at] NightLabs [dot] de
 *
 */
public class UserSearchComposite 
extends XComposite 
{
	public UserSearchComposite(Composite parent, int style) {
		super(parent, style);
		createComposite(this);
	}

	public UserSearchComposite(Composite parent, int style,
			LayoutMode layoutMode, LayoutDataMode layoutDataMode) {
		super(parent, style, layoutMode, layoutDataMode);
		createComposite(this);
	}
 
	private Text userIDText = null;
	public Text getUserIDText() {
		return userIDText;
	}
	
	private Text nameText = null;
	public Text getNameText() {
		return nameText;
	}
	
//	private Text userTypeText = null;
//	public Text getUserTypeText() {
//		return userTypeText;
//	}

	private Combo userTypeCombo = null;
	public Combo getUserTypeCombo() {
		return userTypeCombo;
	}
	
	private UserTable userTable = null;
	public UserTable getUserTable() {
		return userTable;
	}
	
	private User selectedUser = null;
	public User getSelectedUser() {
		return selectedUser;
	}
	
	protected void createComposite(Composite parent) 
	{
		Composite searchComp = new XComposite(parent, SWT.NONE, LayoutMode.TOTAL_WRAPPER);
		searchComp.setLayout(new GridLayout(3, true));
		searchComp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		userIDText = createTextSearchEntry(searchComp, Messages.getString("security.UserSearchComposite.userID")); //$NON-NLS-1$
		nameText = createTextSearchEntry(searchComp, Messages.getString("security.UserSearchComposite.name")); //$NON-NLS-1$
//		userTypeText = createTextSearchEntry(searchComp, Messages.getString("security.UserSearchComposite.userType")); //$NON-NLS-1$
		Composite wrapper = new XComposite(searchComp, SWT.NONE, LayoutMode.TOTAL_WRAPPER);
		Label label = new Label(wrapper, SWT.NONE);
		label.setText(Messages.getString("security.UserSearchComposite.userType")); //$NON-NLS-1$
		userTypeCombo = new Combo(wrapper, SWT.BORDER);
		userTypeCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		userTypeCombo.setItems(new String[] {"User", "UserGroup", "Organisation"}); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				
		userTable = new UserTable(parent, SWT.NONE, true, AbstractTableComposite.DEFAULT_STYLE_SINGLE);
		userTable.getTableViewer().getTable().setLinesVisible(true);
		userTable.getTableViewer().getTable().setHeaderVisible(true);
		userTable.addSelectionChangedListener(userTableSelectionListener);
	}
	
	protected Text createTextSearchEntry(Composite parent, String labelString) 
	{
		Composite wrapper = new XComposite(parent, SWT.NONE, LayoutMode.TOTAL_WRAPPER);
		Label label = new Label(wrapper, SWT.NONE);
		label.setText(labelString);
		Text text = new Text(wrapper, SWT.BORDER);
		text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		return text;
	}
	
	protected UserQuery getUserQuery() 
	{
		UserQuery userQuery = new UserQuery();
		
		if (!nameText.getText().trim().equals("")) //$NON-NLS-1$
			userQuery.setName(nameText.getText());

		if (!userIDText.getText().trim().equals("")) //$NON-NLS-1$
			userQuery.setUserID(userIDText.getText());

//		if (!userTypeText.getText().trim().equals("")) //$NON-NLS-1$
//			userQuery.setUserType(userTypeText.getText());
		
		if (userTypeCombo.getSelectionIndex() != -1 && !userTypeCombo.getText().trim().equals("")) //$NON-NLS-1$
			userQuery.setUserType(userTypeCombo.getText());
		
		return userQuery;
	}
	
	public void searchPressed() 
	{		
		userTable.setInput(Messages.getString("security.UserSearchComposite.LoadingEvent")); //$NON-NLS-1$
		Job job = new Job(Messages.getString("security.UserSearchComposite.LoadingEvent")) { //$NON-NLS-1$
			protected IStatus run(ProgressMonitor monitor){
				try {
					UserManager um = UserManagerUtil.getHome(Login.getLogin().getInitialContextProperties()).create();
					final Collection<UserQuery> queries = new ArrayList<UserQuery>(1);
					Display.getDefault().syncExec(new Runnable(){
						public void run() {
							queries.add(getUserQuery());
						}					
					});
					Set<UserID> userIDs = um.getUserIDs(queries);					
					if (userIDs != null && !userIDs.isEmpty()) {
						String[] USER_FETCH_GROUPS = new String[] {FetchPlan.DEFAULT};
						final List<User> users = UserDAO.sharedInstance().getUsers(userIDs, USER_FETCH_GROUPS, 
								NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT, monitor);
						Display.getDefault().asyncExec(new Runnable() {
							public void run() {					
								userTable.setInput(users);
							}
						});						
					}
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
				return Status.OK_STATUS;
			}
		};
//		job.setPriority(Job.SHORT);
		job.schedule();		
	}
	
	private ISelectionChangedListener userTableSelectionListener = new ISelectionChangedListener(){	
		public void selectionChanged(SelectionChangedEvent event) {
			if (!event.getSelection().isEmpty() && event.getSelection() instanceof StructuredSelection) {
				StructuredSelection sel = (StructuredSelection) event.getSelection();
				if (sel.getFirstElement() instanceof User) {
					selectedUser = (User) sel.getFirstElement();
				}
			}
		}
	};
	
}
