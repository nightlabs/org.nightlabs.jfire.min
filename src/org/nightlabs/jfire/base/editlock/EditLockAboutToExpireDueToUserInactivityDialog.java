package org.nightlabs.jfire.base.editlock;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.nightlabs.base.composite.CountdownButton;
import org.nightlabs.base.dialog.CenteredDialog;

public class EditLockAboutToExpireDueToUserInactivityDialog
extends CenteredDialog
{
	private EditLockMan editLockMan;

	private CountdownButton okButton;

	private Set<EditLockCarrier> editLockCarriers = new HashSet<EditLockCarrier>();
	private EditLockCarrierTable editLockCarrierTable;

	public EditLockAboutToExpireDueToUserInactivityDialog(EditLockMan editLockMan, Shell parentShell)
	{
		super(parentShell);
		this.editLockMan = editLockMan;
	}

	@Override
	protected Button createButton(Composite parent, int id, String label, boolean defaultButton)
	{
		if (id != OK)
			return super.createButton(parent, id, label, defaultButton);

		((GridLayout) parent.getLayout()).numColumns++;
		okButton = new CountdownButton(parent, SWT.PUSH, 60);
		okButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				editLockMan.softReleaseEditLockOnUserInactivity(editLockCarriers);

				setReturnCode(OK);
				close();
			}
		});
		return null;
	}

	@Override
	protected void configureShell(Shell newShell)
	{
		super.configureShell(newShell);
		setToCenteredLocationPreferredSize(newShell, 500, 300);
	}

	@Override
	public boolean close()
	{
		if (super.close()) {
			editLockMan.onCloseEditLockAboutToExpireDueToUserInactivityDialog();
			return true;
		}

		return false;
	}

	@Override
	protected Control createDialogArea(Composite parent)
	{
		Composite page = (Composite) super.createDialogArea(parent);
		Label l = new Label(page, SWT.WRAP);
		l.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		l.setText("The following editLocks will be released because of user inactivity:");

		editLockCarrierTable = new EditLockCarrierTable(page, SWT.NONE);

		return page;
	}

	private int countdownPostponeCounter = 0;

	/**
	 * This method must be called on the UI thread!
	 * @param editLockCarrier the {@link EditLockCarrier} to be added. If it was already added before, this method silently returns without any action.
	 */
	public void addEditLockCarrier(EditLockCarrier editLockCarrier)
	{
		if (editLockCarriers.add(editLockCarrier)) {
			editLockCarrierTable.setInput(editLockCarriers);
			long rest = okButton.getCountdownRest();
			if (rest < 10 && countdownPostponeCounter < 3) {
				okButton.setCountdownRest(10);
				++countdownPostponeCounter;
			}
		}
	}
}
