package org.nightlabs.jfire.base.overview;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.nightlabs.base.composite.XComposite;

/**
 * @author Daniel.Mazurek [at] NightLabs [dot] de
 *
 */
public abstract class SearchEntryViewController
implements EntryViewController 
{	
	public SearchEntryViewController() {
		super();
	}
	
	private Composite searchComposite;
	public Composite getSearchComposite() {
		return searchComposite;
	}
	
	private Composite resultComposite;
	public Composite getResultComposite() {
		return resultComposite;
	}
	
	public Composite createComposite(Composite parent) 
	{
//		Button applyButton = new Button(parent, SWT.NONE);
//		applyButton.setText("Apply");
//		applyButton.addSelectionListener(applyListener);
		
		SashForm form = new SashForm(parent, SWT.VERTICAL);
		form.setLayout(new FillLayout());
		searchComposite = createSearchComposite(form);
		resultComposite = createResultComposite(form);
		form.setWeights(new int[] {1,3});
		return form;
	}
	
	public abstract Composite createSearchComposite(Composite parent);	
	public abstract Composite createResultComposite(Composite parent);
	public abstract void applySearch();
	
	private SelectionListener applyListener = new SelectionListener(){	
		public void widgetSelected(SelectionEvent e) {
			applySearch();
		}	
		public void widgetDefaultSelected(SelectionEvent e) {
			widgetSelected(e);
		}	
	};
}
