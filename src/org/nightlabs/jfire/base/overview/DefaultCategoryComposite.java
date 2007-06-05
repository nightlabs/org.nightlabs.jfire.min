/**
 * 
 */
package org.nightlabs.jfire.base.overview;

import java.util.List;

import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.nightlabs.base.layout.WeightedTableLayout;
import org.nightlabs.base.table.AbstractTableComposite;
import org.nightlabs.base.table.TableContentProvider;
import org.nightlabs.base.table.TableLabelProvider;

/**
 * The default category composite displays a {@link Category}s {@link Entry}s
 * in a Table with icon and name.
 * 
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 */
public class DefaultCategoryComposite extends AbstractTableComposite<Entry> {

	
	private class ContentProvider extends TableContentProvider {
		
		public Object[] getElements(Object inputElement) 
		{
			if (inputElement instanceof List) {
				return ((List<Entry>) inputElement).toArray();
			}
			return null;
		}	
	};

	private class LabelProvider extends TableLabelProvider {	
		public String getColumnText(Object element, int columnIndex) 
		{
			if (columnIndex == 1) {
				Entry entry = (Entry) element;
				return entry.getEntryFactory().getName();				
			}
			return null;
		}

		public Image getColumnImage(Object element, int columnIndex) 
		{
			if (columnIndex == 0) {
				Entry entry = (Entry) element;
				return entry.getEntryFactory().getImage();				
			}			
			return null;
		}	
	};
	
	
	/**
	 * @param parent
	 * @param style
	 */
	public DefaultCategoryComposite(Composite parent, int style, Category category) {
		super(parent, style);
		setInput(category.getEntries());
	}

	@Override
	protected void createTableColumns(TableViewer tableViewer, Table table) {					
		TableColumn iconColumn = new TableColumn(table, SWT.NONE);
		iconColumn.setText("Icon");
		TableColumn nameItem = new TableColumn(table, SWT.NONE);
		nameItem.setText("Text");    				
		table.setHeaderVisible(false);
		table.setLinesVisible(false);
		table.setLayout(new WeightedTableLayout(new int[] {1, 1}, new int[] {20, -1}));
		
	}
	
	@Override
	protected void setTableProvider(TableViewer tableViewer) {
		tableViewer.setContentProvider(new ContentProvider());
		tableViewer.setLabelProvider(new LabelProvider());		
//		tableViewer.addSelectionChangedListener(new SelectionListener());
		tableViewer.addDoubleClickListener(new DoubleClickListener());	    	    
	}	    	

	private class SelectionListener implements ISelectionChangedListener {	
		public void selectionChanged(SelectionChangedEvent event) {
			Entry entry = getFirstSelectedElement();
			if (entry != null)
				entry.handleActivation();
		}	
	};

	private class DoubleClickListener implements IDoubleClickListener {	
		public void doubleClick(DoubleClickEvent event) {
			Entry entry = getFirstSelectedElement();
			if (entry != null)
				entry.handleActivation();
		}	
	};
	
}
