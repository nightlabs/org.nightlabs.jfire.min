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
import org.nightlabs.base.composite.XComposite;
import org.nightlabs.jdo.query.JDOQuery;
import org.nightlabs.jdo.ui.JDOQueryComposite;
import org.nightlabs.jfire.base.resource.Messages;

/**
 * Abstract Base Class for a Composite which return {@link JDOQuery}s for searching 
 * It uses {@link JDOQueryComposite}s which are displayed in a {@link Section}
 * 
 * @author Daniel.Mazurek [at] NightLabs [dot] de
 */
public abstract class AbstractQueryFilterComposite 
extends XComposite 
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
		createComposite(this);
	}

	/**
	 * @param parent
	 * @param style
	 */
	public AbstractQueryFilterComposite(Composite parent, int style) {
		super(parent, style);
		createComposite(this);
	}

	private List<JDOQueryComposite> queryComposites;
	private Map<Button, Section> button2Section;
	private Map<Button, JDOQueryComposite> button2Composite = null;
	private List<QuickSearchEntry> quickSearchEntries = null;
	
	public List<QuickSearchEntry> getQuickSearchEntryTypes() {
		return quickSearchEntries;
	}
	
	protected Map<Button, Section> getButton2Section() {
		if (button2Section == null)
			button2Section = new HashMap<Button, Section>();
		return button2Section;
	}
	
	protected Map<Button, JDOQueryComposite> getButton2QueryComposite() {
		if (button2Composite == null)
			button2Composite = new HashMap<Button, JDOQueryComposite>();
		return button2Composite;
	}

	protected void createComposite(Composite parent) {
		createContents(parent);
		queryComposites = registerJDOQueryComposites();
	}
	
	protected void configureSection(Section section, JDOQueryComposite comp) 
	{
		Button activeButton = new Button(section, SWT.CHECK);
		activeButton.setText(Messages.getString("org.nightlabs.jfire.base.overview.search.AbstractQueryFilterComposite.activeButton.text")); //$NON-NLS-1$
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
	
	/**
	 * returns a List of {@link JDOQueryComposite}s which should be displayed
	 * @return a List of {@link JDOQueryComposite}s which should be displayed
	 */
	protected abstract List<JDOQueryComposite> registerJDOQueryComposites();
	
	/**
	 * returns the {@link Class} of the type of object which should queried
	 * @return the {@link Class} of the type of object which should queried
	 */
	protected abstract Class getQueryClass();
	
	/**
	 * creates the contents, usually the same composites like 
	 * returned in {@link #registerJDOQueryComposites()} 
	 * but here here can the layout be defined
	 * 
	 * @param parent the parent Composite
	 */
	protected abstract void createContents(Composite parent);	
}
