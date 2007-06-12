package org.nightlabs.jfire.base.overview.search;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.Section;
import org.nightlabs.jdo.JDOQueryComposite;
import org.nightlabs.jdo.query.JDOQuery;

/**
 * @author Daniel.Mazurek [at] NightLabs [dot] de
 *
 */
public abstract class AbstractQueryFilterComposite 
extends AbstractFormFilterComposite 
{
	/**
	 * @param parent
	 * @param style
	 * @param layoutMode
	 * @param layoutDataMode
	 */
	public AbstractQueryFilterComposite(Composite parent, int style,
			LayoutMode layoutMode, LayoutDataMode layoutDataMode) {
		super(parent, style, layoutMode, layoutDataMode);
	}

	/**
	 * @param parent
	 * @param style
	 */
	public AbstractQueryFilterComposite(Composite parent, int style) {
		super(parent, style);
	}

	private List<JDOQueryComposite> queryComposites;
	private Map<Button, Section> button2Section;
	protected Map<Button, Section> getButton2Section() {
		if (button2Section == null)
			button2Section = new HashMap<Button, Section>();
		return button2Section;
	}
	
	private Map<Button, JDOQueryComposite> button2Composite = null;
	protected Map<Button, JDOQueryComposite> getButton2QueryComposite() {
		if (button2Composite == null)
			button2Composite = new HashMap<Button, JDOQueryComposite>();
		return button2Composite;
	}

	
	@Override
	protected void createComposite(Composite parent) {
		super.createComposite(parent);
		this.queryComposites = registerJDOQueryComposites();
	}
	
	protected void configureSection(Section section, JDOQueryComposite comp) 
	{
		Button activeButton = new Button(section, SWT.CHECK);
		activeButton.setText("Active");
		activeButton.setSelection(comp.isActive());
		activeButton.addSelectionListener(new SelectionListener(){	
			public void widgetSelected(SelectionEvent e) {
				Button b = (Button) e.getSource();
				JDOQueryComposite comp = getButton2QueryComposite().get(b);
				if (comp != null)
					comp.setActive(b.getSelection());
				Section section = getButton2Section().get(b);
				if (section != null)
					section.setExpanded(b.getSelection());
			}	
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}	
		});				
		section.setTextClient(activeButton);
		getButton2QueryComposite().put(activeButton, comp);		
		getButton2Section().put(activeButton, section);		
	}
	
//	private SelectionListener selectionListener = new SelectionListener(){	
//		public void widgetSelected(SelectionEvent e) {
//			Button b = (Button) e.getSource();
//			JDOQueryComposite comp = button2Composite.get(b);
//			if (comp != null)
//				comp.setActive(b.getSelection());
//			Section section = button2Section.get(b);
//			if (section != null)
//				section.setExpanded(b.getSelection());
//		}	
//		public void widgetDefaultSelected(SelectionEvent e) {
//			widgetSelected(e);
//		}	
//	};
	
	public List<JDOQuery> getJDOQueries() 
	{
		if (queryComposites != null) 
		{
			List<JDOQuery> queries = new ArrayList<JDOQuery>(queryComposites.size());
			for (JDOQueryComposite comp : queryComposites) {
				if (comp.isActive())
					queries.add(comp.getJDOQuery());
			}
			return queries;
		}
		return null;
	}
	
	protected abstract List<JDOQueryComposite> registerJDOQueryComposites();
	protected abstract Class getQueryClass();
}
