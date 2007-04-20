package org.nightlabs.jfire.base.overview;

import java.util.List;

import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.nebula.widgets.pshelf.PShelf;
import org.eclipse.nebula.widgets.pshelf.PShelfItem;
import org.eclipse.nebula.widgets.pshelf.RedmondShelfRenderer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Table;
import org.nightlabs.base.composite.ListComposite;
import org.nightlabs.base.composite.XComposite;
import org.nightlabs.base.layout.WeightedTableLayout;
import org.nightlabs.base.table.AbstractTableComposite;
import org.nightlabs.base.table.TableContentProvider;
import org.nightlabs.base.table.TableLabelProvider;

/**
 * @author Daniel.Mazurek [at] NightLabs [dot] de
 *
 */
public class OverviewComposite 
extends XComposite 
{
	public OverviewComposite(Composite parent, int style, LayoutMode layoutMode,
			LayoutDataMode layoutDataMode) 
	{
		super(parent, style, layoutMode, layoutDataMode);
		createComposite(this);
	}

	public OverviewComposite(Composite parent, int style) {
		super(parent, style);
		createComposite(this);
	}

	private PShelf shelf;
	protected void createComposite(Composite parent) 
	{
		parent.setLayout(new FillLayout());
		
		shelf = new PShelf(parent, SWT.NONE);
	  shelf.setRenderer(new RedmondShelfRenderer());
//		RedmondShelfRenderer renderer = new RedmondShelfRenderer();
//		renderer.setGradient1(new Color(null, 198, 223, 225));
//		renderer.setGradient1(new Color(null, 255, 255, 255));
//		renderer.setHoverGradient1(new Color(null, 198, 223, 225));
//		renderer.setHoverGradient2(new Color(null, 255, 255, 255));
//		renderer.setHover(true);
//		shelf.setRenderer(renderer);
		
		shelf.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		for (Category category : OverviewRegistry.sharedInstance().getCategories()) 
		{
	    PShelfItem categoryItem = new PShelfItem(shelf,SWT.NONE);
	    categoryItem.setText(category.getName().getText());
	    categoryItem.setImage(category.getImage());
	    categoryItem.getBody().setLayout(new FillLayout());
	    
//	    ListComposite<Entry> listComp = new ListComposite<Entry>(
//	    		categoryItem.getBody(), SWT.NONE, listLabelProvider);
//	    listComp.setInput(OverviewRegistry.sharedInstance().getEntries(category));
//	    listComp.addSelectionChangedListener(selectionListener);
//	    ListViewer viewer = new ListViewer(listComp.getList());
//	    viewer.addDoubleClickListener(doubleClickListener);
	    
	    AbstractTableComposite<Entry> tableComp = new AbstractTableComposite<Entry>(
	    		categoryItem.getBody(), SWT.NONE)
	    {
				@Override
				protected void createTableColumns(TableViewer tableViewer, Table table) {					
			    TableColumn iconColumn = new TableColumn(table, SWT.NONE);
			    iconColumn.setText("Icon");
			    TableColumn nameItem = new TableColumn(table, SWT.NONE);
			    nameItem.setText("Text");    				
				}
				@Override
				protected void setTableProvider(TableViewer tableViewer) {
			    tableViewer.setContentProvider(tableContentProvider);
			    tableViewer.setLabelProvider(tableLabelProvider);					
				}	    	
	    };
	    TableViewer tableViewer = tableComp.getTableViewer();
	    tableViewer.addSelectionChangedListener(selectionListener);
	    tableViewer.addDoubleClickListener(doubleClickListener);	    	    
	    tableViewer.getTable().setHeaderVisible(false);
	    tableViewer.getTable().setLinesVisible(false);
	    tableViewer.getTable().setLayout(new WeightedTableLayout(new int[] {1, 1}, new int[] {20, -1}));
	    tableViewer.setInput(OverviewRegistry.sharedInstance().getEntries(category));	    
		};
	}
	
	private ISelectionChangedListener selectionListener = new ISelectionChangedListener(){	
		public void selectionChanged(SelectionChangedEvent event) {
			if (!event.getSelection().isEmpty() && event.getSelection() instanceof StructuredSelection) {
				StructuredSelection sel = (StructuredSelection) event.getSelection();
				Entry entry = (Entry) sel.getFirstElement();
				entry.openEntry();
			}
		}	
	};
	
	private IDoubleClickListener doubleClickListener = new IDoubleClickListener(){	
		public void doubleClick(DoubleClickEvent event) {
			if (!event.getSelection().isEmpty() && event.getSelection() instanceof StructuredSelection) {
				StructuredSelection sel = (StructuredSelection) event.getSelection();
				Entry entry = (Entry) sel.getFirstElement();
				entry.openEntry();
			}
		}	
	};
	
	private IStructuredContentProvider tableContentProvider = new TableContentProvider()
	{		
		public Object[] getElements(Object inputElement) 
		{
			if (inputElement instanceof List) {
				return ((List<Entry>) inputElement).toArray();
			}
			return null;
		}	
	};
	
	private ILabelProvider tableLabelProvider = new TableLabelProvider()
	{	
		public String getColumnText(Object element, int columnIndex) 
		{
			if (columnIndex == 1) {
				Entry entry = (Entry) element;
				return entry.getName().getText();				
			}
			return null;
		}
	
		public Image getColumnImage(Object element, int columnIndex) 
		{
			if (columnIndex == 0) {
				Entry entry = (Entry) element;
				return entry.getImage();				
			}			
			return null;
		}	
	};
	
//	private ILabelProvider listLabelProvider = new LabelProvider()
//	{	
//		public String getText(Object element) 
//		{
//			Entry entry = (Entry) element;
//			return entry.getName().getText();				
//		}
//	
//		public Image getImage(Object element) 
//		{
//			Entry entry = (Entry) element;
//			return entry.getImage();				
//		}	
//	};	
}
