package org.nightlabs.jfire.base.overview.search;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Spinner;
import org.nightlabs.base.composite.XComposite;

/**
 * @author Daniel.Mazurek [at] NightLabs [dot] de
 *
 */
public class SpinnerSearchEntry 
extends XComposite 
{

	public SpinnerSearchEntry(Composite parent, int style, LayoutMode layoutMode,
			LayoutDataMode layoutDataMode, String text) 
	{
		super(parent, style, layoutMode, layoutDataMode);
		this.text = text;
		createComposite(this);
	}

	public SpinnerSearchEntry(Composite parent, int style, String text) {
		super(parent, style);
		this.text = text;
		createComposite(this);
	}

	private String text = null;
	private Button activeButton = null;
	private Spinner spinner = null;
	public Spinner getSpinner() {
		return spinner;
	}
	
	protected void createComposite(Composite parent) 
	{
		Group group = new Group(parent, SWT.NONE);
		group.setText(text);
		group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		group.setLayout(new GridLayout());
		
		activeButton = new Button(group, SWT.CHECK);
		activeButton.setText("Active");
		activeButton.addSelectionListener(activeButtonListener);
		
		spinner = new Spinner(group, SWT.BORDER);
		spinner.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		spinner.setDigits(2);		
	}
	
	private SelectionListener activeButtonListener = new SelectionListener(){	
		public void widgetSelected(SelectionEvent e) {
			spinner.setEnabled(activeButton.getSelection());
		}	
		public void widgetDefaultSelected(SelectionEvent e) {
			widgetSelected(e);
		}	
	};
		
	public boolean isActive() {
		return activeButton.getSelection();
	}
	
	public void setActive(boolean active) {
		activeButton.setSelection(active);
		spinner.setEnabled(active);
	}
}
