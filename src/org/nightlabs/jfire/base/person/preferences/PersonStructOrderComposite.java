package org.nightlabs.jfire.base.person.preferences;

import java.util.Map;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import org.nightlabs.jfire.base.JFireBasePlugin;



/**
 * 
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 */
public class PersonStructOrderComposite extends Composite {
	private Composite titleComposite;
	private Label labelTitle;
	private Group groupStructBlocks;
	private Button buttonFieldDown;
	private Button buttonFieldUp;
	private Composite fieldsTableComposite;
	private Composite fieldsButtonsComposite;
	private Button buttonBlockDown;
	private Button buttonBlockUp;
	private Composite blocksButtonsComposite;
	private Composite blocksTableComposite;
	private Group groupStructFields;
	
	
	private TableViewer tableViewerBlocks;
	private TableViewer tableViewerFields;
	
	
	public static void showGUI() {
		Display display = Display.getDefault();
		Shell shell = new Shell(display);
		PersonStructOrderComposite inst = new PersonStructOrderComposite(shell, SWT.NULL);
		Point size = inst.getSize();
		shell.setLayout(new FillLayout());
		shell.layout();
		if(size.x == 0 && size.y == 0) {
			inst.pack();
			shell.pack();
		} else {
			Rectangle shellBounds = shell.computeTrim(0, 0, size.x, size.y);
			int MENU_HEIGHT = 22;
			if (shell.getMenuBar() != null)
				shellBounds.height -= MENU_HEIGHT;
			shell.setSize(shellBounds.width, shellBounds.height);
		}
		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
	}
	
	
	/**
	 * 
	 * @param parent
	 * @param style
	 */
	public PersonStructOrderComposite(Composite parent, int style) {
		super(parent, style);
		GridLayout thisLayout = new GridLayout();
		thisLayout.numColumns = 2;
		thisLayout.makeColumnsEqualWidth = true;
		this.setLayout(thisLayout);
		
		GridData thisData = new GridData(GridData.FILL_BOTH);
		this.setLayoutData(thisData);

		titleComposite = new Composite(this, SWT.NONE);
		RowLayout titleCompositeLayout = new RowLayout(org.eclipse.swt.SWT.HORIZONTAL);
		GridData titleCompositeLData = new GridData();
		titleCompositeLData.grabExcessHorizontalSpace = true;
		titleCompositeLData.horizontalSpan = 2;
		titleCompositeLData.horizontalAlignment = GridData.FILL;
		titleComposite.setLayoutData(titleCompositeLData);
		titleComposite.setLayout(titleCompositeLayout);
		
		labelTitle = new Label(titleComposite, SWT.NONE | SWT.WRAP);
		labelTitle.setText("Define the display order of the person structure");
		
		groupStructBlocks = new Group(this, SWT.NONE);
		GridLayout groupStructBlocksLayout = new GridLayout();
		groupStructBlocksLayout.numColumns = 2;
		groupStructBlocksLayout.horizontalSpacing = 0;
		groupStructBlocksLayout.marginWidth = 0;
		groupStructBlocksLayout.marginHeight = 0;
		GridData groupStructBlocksLData = new GridData();
		groupStructBlocksLData.grabExcessHorizontalSpace = true;
		groupStructBlocksLData.grabExcessVerticalSpace = true;
		groupStructBlocksLData.verticalAlignment = GridData.FILL;
		groupStructBlocksLData.horizontalAlignment = GridData.FILL;
		groupStructBlocks.setLayoutData(groupStructBlocksLData);
		groupStructBlocks.setLayout(groupStructBlocksLayout);
		groupStructBlocks.setText("struct blocks");
		
		blocksTableComposite = new Composite(groupStructBlocks, SWT.NONE);
		GridLayout blocksTableCompositeLayout = new GridLayout();
		GridData blocksTableCompositeLData = new GridData();
		blocksTableCompositeLData.grabExcessVerticalSpace = true;
		blocksTableCompositeLData.verticalAlignment = GridData.FILL;
		blocksTableCompositeLData.grabExcessHorizontalSpace = true;
		blocksTableCompositeLData.horizontalAlignment = GridData.FILL;
		blocksTableComposite.setLayoutData(blocksTableCompositeLData);
		blocksTableCompositeLayout.makeColumnsEqualWidth = true;
		blocksTableComposite.setLayout(blocksTableCompositeLayout);
		
		blocksButtonsComposite = new Composite(groupStructBlocks, SWT.NONE);
		GridLayout blocksButtonsCompositeLayout = new GridLayout();
		GridData blocksButtonsCompositeLData = new GridData();
		blocksButtonsCompositeLData.grabExcessVerticalSpace = true;
		blocksButtonsCompositeLData.verticalAlignment = GridData.FILL;
		blocksButtonsCompositeLData.horizontalAlignment = GridData.END;
		blocksButtonsCompositeLData.horizontalIndent = 0;
//		blocksButtonsCompositeLData.widthHint = 40;
		blocksButtonsComposite.setLayoutData(blocksButtonsCompositeLData);
		blocksButtonsCompositeLayout.makeColumnsEqualWidth = true;
		blocksButtonsComposite.setLayout(blocksButtonsCompositeLayout);
		blocksButtonsCompositeLayout.horizontalSpacing = 0;
//		blocksButtonsCompositeLayout.verticalSpacing = 0;
		blocksButtonsCompositeLayout.marginWidth = 0;
		blocksButtonsCompositeLayout.marginHeight = 0;
		
		buttonBlockUp = new Button(blocksButtonsComposite, SWT.PUSH);
		buttonBlockUp.setText("Up");
		GridData buttonLData = new GridData(GridData.FILL_HORIZONTAL);
		buttonLData.horizontalIndent = 0;
		buttonLData.horizontalIndent = 0;
		buttonBlockUp.setLayoutData(buttonLData);
		buttonBlockUp.addSelectionListener(new SelectionListener() {

			public void widgetSelected(SelectionEvent evt) {
				buttonUpPressed(evt);
			}

			public void widgetDefaultSelected(SelectionEvent arg0) {
			}
		});
		
		buttonBlockDown = new Button(blocksButtonsComposite, SWT.PUSH);
		buttonBlockDown.setText("Down");
		buttonLData = new GridData(GridData.FILL_HORIZONTAL);
		buttonLData.horizontalIndent = 0;
		buttonLData.horizontalIndent = 0;
		buttonBlockDown.setLayoutData(buttonLData);
		buttonBlockDown.addSelectionListener(new SelectionListener() {

			public void widgetSelected(SelectionEvent evt) {
				buttonDownPressed(evt);
			}

			public void widgetDefaultSelected(SelectionEvent arg0) {
			}
		});

		
		groupStructFields = new Group(this, SWT.NONE);
		GridLayout groupStructFieldsLayout = new GridLayout();
		groupStructFieldsLayout.numColumns = 2;
		groupStructFieldsLayout.horizontalSpacing = 0;
		groupStructFieldsLayout.verticalSpacing = 0;
		groupStructFieldsLayout.marginHeight = 0;
		groupStructFieldsLayout.marginWidth = 0;
		GridData groupStructFieldsLData = new GridData();
		groupStructFieldsLData.grabExcessHorizontalSpace = true;
		groupStructFieldsLData.horizontalAlignment = GridData.FILL;
		groupStructFieldsLData.grabExcessVerticalSpace = true;
		groupStructFieldsLData.verticalAlignment = GridData.FILL;
		groupStructFields.setLayoutData(groupStructFieldsLData);
		groupStructFields.setLayout(groupStructFieldsLayout);
		groupStructFields.setText("struct fields");
		
		fieldsTableComposite = new Composite(groupStructFields, SWT.NONE);
		GridLayout fieldsTableCompositeLayout = new GridLayout();
		GridData fieldsTableCompositeLData = new GridData();
		fieldsTableCompositeLData.grabExcessVerticalSpace = true;
		fieldsTableCompositeLData.verticalAlignment = GridData.FILL;
		fieldsTableCompositeLData.horizontalAlignment = GridData.FILL;
		fieldsTableCompositeLData.grabExcessHorizontalSpace = true;
		fieldsTableComposite.setLayoutData(fieldsTableCompositeLData);
		fieldsTableCompositeLayout.makeColumnsEqualWidth = true;
		fieldsTableComposite.setLayout(fieldsTableCompositeLayout);
		
		fieldsButtonsComposite = new Composite(groupStructFields, SWT.NONE);
		GridLayout fieldsButtonsCompositeLayout = new GridLayout();
		GridData fieldsButtonsCompositeLData = new GridData();
		fieldsButtonsCompositeLData.verticalAlignment = GridData.FILL;
		fieldsButtonsCompositeLData.grabExcessVerticalSpace = true;
		fieldsButtonsCompositeLData.horizontalAlignment = GridData.END;
		fieldsButtonsComposite.setLayoutData(fieldsButtonsCompositeLData);
		fieldsButtonsCompositeLayout.makeColumnsEqualWidth = true;
		fieldsButtonsComposite.setLayout(fieldsButtonsCompositeLayout);
		
		buttonFieldUp = new Button(fieldsButtonsComposite, SWT.PUSH);
		buttonFieldUp.setText("Up");
		buttonLData = new GridData(GridData.FILL_HORIZONTAL);
		buttonLData.horizontalIndent = 0;
		buttonLData.horizontalIndent = 0;
		buttonFieldUp.setLayoutData(buttonLData);
		
		buttonFieldDown = new Button(fieldsButtonsComposite, SWT.PUSH);
		buttonFieldDown.setText("Down");
		buttonLData = new GridData(GridData.FILL_HORIZONTAL);
		buttonLData.horizontalIndent = 0;
		buttonLData.horizontalIndent = 0;
		buttonFieldDown.setLayoutData(buttonLData);
		
		// Blocks table
		tableViewerBlocks = new TableViewer(blocksTableComposite, SWT.BORDER | SWT.H_SCROLL | SWT.FULL_SELECTION | SWT.MULTI);    
		GridData tgd = new GridData(GridData.FILL_BOTH);    
		Table t = tableViewerBlocks.getTable(); 
		t.setHeaderVisible(true);
		t.setLinesVisible(true);
		t.setLayoutData(tgd);
		t.setLayout(new TableLayout());    
		tableViewerBlocks.setContentProvider(new BlockOrderListContentProvider());
		tableViewerBlocks.setLabelProvider(new BlockOrderListLabelProvider());
		new TableColumn(t, SWT.LEFT, 0).setText(JFireBasePlugin.getResourceString("person.preferences.pages.PersonStructOrder.blockstable.col0"));
		tableViewerBlocks.setInput(
				PersonStructOrderConfigModule.sharedInstance()
				.structBlockDisplayOrder()
		);
		
		
		// Fields table
		tableViewerFields = new TableViewer(fieldsTableComposite, SWT.BORDER | SWT.H_SCROLL | SWT.FULL_SELECTION | SWT.MULTI);
		tgd = new GridData(GridData.FILL_BOTH);    
		t = tableViewerFields.getTable(); 
		t.setHeaderVisible(true);
		t.setLinesVisible(true);
		t.setLayoutData(tgd);
		t.setLayout(new TableLayout());    
		new TableColumn(t, SWT.LEFT, 0).setText(JFireBasePlugin.getResourceString("person.preferences.pages.PersonStructOrder.fieldstable.col0"));
		
		this.layout();
	}
	
	
	protected void buttonUpPressed(SelectionEvent evt) {
		if (evt.getSource().equals(buttonBlockUp)) {
			tableViewerBlocks.getElementAt(0);
			IStructuredSelection selection = (IStructuredSelection)tableViewerBlocks.getSelection();
			if (!selection.isEmpty()) {
				Map.Entry entry = (Map.Entry)selection.getFirstElement();
				int priority = ((Integer)entry.getValue()).intValue();
	//			tableViewerBlocks.getSelection();
				((BlockOrderListContentProvider)tableViewerBlocks.getContentProvider()).moveUp(priority);
				tableViewerBlocks.refresh();
	//			tableViewerBlocks.getContentProvider().
	//			tableViewerBlocks.
			}
		}
	}
	
	protected void buttonDownPressed(SelectionEvent evt) {
		if (evt.getSource().equals(buttonBlockDown)) {
			tableViewerBlocks.getElementAt(0);
			IStructuredSelection selection = (IStructuredSelection)tableViewerBlocks.getSelection();
			if (!selection.isEmpty()) {
				Map.Entry entry = (Map.Entry)selection.getFirstElement();
				int priority = ((Integer)entry.getValue()).intValue();
	//			tableViewerBlocks.getSelection();
				((BlockOrderListContentProvider)tableViewerBlocks.getContentProvider()).moveDown(priority);
				tableViewerBlocks.refresh();
	//			tableViewerBlocks.getContentProvider().
	//			tableViewerBlocks.
			}
		}
	}
	
	/**
	 * 
	 */
	public Map getStructBlockOrder() {
		return ((BlockOrderListContentProvider)tableViewerBlocks.getContentProvider()).getStructBlockOrder();
	}
}
