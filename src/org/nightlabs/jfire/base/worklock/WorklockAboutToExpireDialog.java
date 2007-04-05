package org.nightlabs.jfire.base.worklock;

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

public class WorklockAboutToExpireDialog
extends CenteredDialog
{
	private CountdownButton okButton;

	public WorklockAboutToExpireDialog(Shell parentShell)
	{
		super(parentShell);
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
				setReturnCode(OK);
				close();
			}
		});
		return null;
	}

	@Override
	protected Control createDialogArea(Composite parent)
	{
		Composite page = (Composite) super.createDialogArea(parent);
		Label l = new Label(page, SWT.WRAP);
		l.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		l.setText("");
		return page;
	}
}
