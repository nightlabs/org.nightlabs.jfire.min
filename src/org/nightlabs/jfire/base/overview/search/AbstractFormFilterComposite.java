package org.nightlabs.jfire.base.overview.search;

import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;
import org.nightlabs.base.composite.XComposite;

/**
 * This abstract implementation can be used to build form based search filter composites 
 * 
 * @author Daniel.Mazurek [at] NightLabs [dot] de
 *
 */
public abstract class AbstractFormFilterComposite 
extends XComposite 
{

	public AbstractFormFilterComposite(Composite parent, int style,
			LayoutMode layoutMode, LayoutDataMode layoutDataMode) 
	{
		super(parent, style, layoutMode, layoutDataMode);
		createComposite(this);
	}

	public AbstractFormFilterComposite(Composite parent, int style) {
		super(parent, style);
		createComposite(this);
	}

	private FormToolkit toolkit;
	protected FormToolkit getToolkit() {
		return toolkit;
	}
	
	private ScrolledForm form;
	protected ScrolledForm getForm() {
		return form;
	}
	
	public static final  int SECTION_STYLE = Section.TITLE_BAR | Section.TWISTIE | Section.EXPANDED;
	
	protected void createComposite(Composite parent) 
	{
		toolkit = new FormToolkit(Display.getDefault());
		form = toolkit.createScrolledForm(parent);
		form.setLayoutData(new GridData(GridData.FILL_BOTH));
		form.getBody().setLayout(new TableWrapLayout());
		
		createSections();
	}
	
	protected Section createSection(String text) 
	{
		Section section = toolkit.createSection(form.getBody(), SECTION_STYLE);
		section.setText(text);
		section.addExpansionListener(new ExpansionAdapter() {
		  public void expansionStateChanged(ExpansionEvent e) {
		   form.layout(true, true);
		  }
		 });
		TableWrapData td = new TableWrapData(TableWrapData.FILL_GRAB);		
		section.setLayoutData(td);
		return section;
	}
	
	protected abstract void createSections();	
}
