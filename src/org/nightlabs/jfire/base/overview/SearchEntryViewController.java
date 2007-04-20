package org.nightlabs.jfire.base.overview;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;
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
		 		
		SashForm sashform = new SashForm(parent, SWT.VERTICAL);
		sashform.setLayout(new FillLayout());
		searchComposite = createSearchComposite(sashform);
		resultComposite = createResultComposite(sashform);
		sashform.setWeights(new int[] {1,3});
		
		if (searchComposite instanceof XComposite) {
			((XComposite)searchComposite).setToolkit(new FormToolkit(Display.getDefault()));
			((XComposite)searchComposite).adaptToToolkit();
		}
		return sashform;
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
	
	@Override
	public boolean equals(Object obj) 
	{
		if (obj == null)
			return false;
		
		if (!(obj instanceof EntryViewController))
				return false;
		
		EntryViewController controller = (EntryViewController) obj;
		if (controller.getID().equals(getID()))
				return true;
		
		return false;
	}
	
}
