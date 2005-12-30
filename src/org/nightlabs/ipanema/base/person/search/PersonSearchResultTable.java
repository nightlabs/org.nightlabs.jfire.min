/*
 * Created 	on Dec 19, 2004
 * 					by alex
 *
 */
package org.nightlabs.ipanema.base.person.search;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import org.nightlabs.base.layout.WeightedTableLayout;
import org.nightlabs.base.table.AbstractTableComposite;
import org.nightlabs.ipanema.base.JFireBasePlugin;
import org.nightlabs.ipanema.person.Person;

/**
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 */
public class PersonSearchResultTable extends AbstractTableComposite {
	
	public PersonSearchResultTable(Composite parent, int style) {
		super(parent, style, true);
	}

	public void setInput(Collection persons) {
		tableViewer.setInput(persons);
	}
	
	public void refresh() {
		super.refresh();
	}	
	
	/**
	 * Returns the (first) selected Person or null
	 * @return The (first) selected Person or null
	 */
	public Person getSelectedPerson() {
		if (getTable().getSelectionCount() == 1) {
			return (Person)getTable().getSelection()[0].getData();
		}
		return null;
	}

	/**
	 * Returns all selected Persons in a Set.
	 * @return All selected Persons in a Set.
	 */
	public Set getSelectedPersons() {
		Set result = new HashSet();
		TableItem[] items = getTable().getSelection();
		for (int i = 0; i < items.length; i++) {
			result.add(items[i].getData());
		}
		return result;
	}

	protected void createTableColumns(TableViewer tableViewer, Table table) {
		new TableColumn(table, SWT.LEFT, 0).setText(JFireBasePlugin.getResourceString("person.search.result.table.col0"));
		new TableColumn(table, SWT.LEFT, 0).setText(JFireBasePlugin.getResourceString("person.search.result.table.col0"));
		new TableColumn(table, SWT.LEFT, 0).setText(JFireBasePlugin.getResourceString("person.search.result.table.col0"));
		new TableColumn(table, SWT.LEFT, 0).setText(JFireBasePlugin.getResourceString("person.search.result.table.col0"));
		table.setLayout(new WeightedTableLayout(new int[] {1,1,1,1}));
	}

	protected void setTableProvider(TableViewer tableViewer) {
		tableViewer.setContentProvider(new PersonSearchResultTableContentProvider());
		tableViewer.setLabelProvider(new PersonSearchResultTableLabelProvider());
	}
	
	public PersonSearchResultTableContentProvider getContentProvider() {
		IContentProvider contentProvider = getTableViewer().getContentProvider();
		if (!(contentProvider instanceof PersonSearchResultTableContentProvider))
			throw new IllegalStateException("This PersonSearchTable's contentProvider is an instance of "+contentProvider.getClass().getName()+" instead of PersonSearchResultTableContentProvider.");
		return (PersonSearchResultTableContentProvider)contentProvider;
	}

}
