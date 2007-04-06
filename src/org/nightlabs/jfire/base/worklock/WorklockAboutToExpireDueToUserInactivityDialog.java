package org.nightlabs.jfire.base.worklock;

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

public class WorklockAboutToExpireDueToUserInactivityDialog
extends CenteredDialog
{
	private WorklockMan worklockMan;

	private CountdownButton okButton;

	private Set<WorklockCarrier> worklockCarriers = new HashSet<WorklockCarrier>();
	private WorklockCarrierTable worklockCarrierTable;

	public WorklockAboutToExpireDueToUserInactivityDialog(WorklockMan worklockMan, Shell parentShell)
	{
		super(parentShell);
		this.worklockMan = worklockMan;
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
				worklockMan.softReleaseWorklockOnUserInactivity(worklockCarriers);

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
			worklockMan.onCloseWorklockAboutToExpireDueToUserInactivityDialog();
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
		l.setText("The following worklocks will be released because of user inactivity:");

		worklockCarrierTable = new WorklockCarrierTable(page, SWT.NONE);

		return page;
	}

	private int countdownPostponeCounter = 0;

	/**
	 * This method must be called on the UI thread!
	 * @param worklockCarrier the {@link WorklockCarrier} to be added. If it was already added before, this method silently returns without any action.
	 */
	public void addWorklockCarrier(WorklockCarrier worklockCarrier)
	{
		if (worklockCarriers.add(worklockCarrier)) {
			worklockCarrierTable.setInput(worklockCarriers);
			long rest = okButton.getCountdownRest();
			if (rest < 10 && countdownPostponeCounter < 3) {
				okButton.setCountdownRest(10);
				++countdownPostponeCounter;
			}
		}
	}
}
