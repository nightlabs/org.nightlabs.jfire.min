package org.nightlabs.jfire.base.prop.structedit;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;
import org.nightlabs.base.composite.ListComposite;
import org.nightlabs.base.composite.XComposite;
import org.nightlabs.jfire.prop.structfield.ImageStructField;

public class ImageStructFieldEditor extends AbstractStructFieldEditor<ImageStructField> {
	private ImageStructFieldEditorComposite imageStructFieldEditorComposite;
	
	public static class ImageStructFieldEditorFactory extends AbstractStructFieldEditorFactory {
		public String getStructFieldEditorClass() {
			return ImageStructFieldEditor.class.getName();
		}
	}

	@Override
	protected Composite createSpecialComposite(Composite parent, int style) {
		imageStructFieldEditorComposite = new ImageStructFieldEditorComposite(parent);
		return imageStructFieldEditorComposite;
	}

	@Override
	protected void setSpecialData(ImageStructField field) {
		imageStructFieldEditorComposite.setField(field);
	}
}

class ImageStructFieldEditorComposite extends XComposite {
	private Spinner widthSpinner;
	private Spinner heightSpinner;
	private ListComposite<String> formatList;
	private ImageStructField imageField;
	
	public ImageStructFieldEditorComposite(Composite parent) {
		super(parent, SWT.NONE, LayoutMode.TIGHT_WRAPPER, LayoutDataMode.GRID_DATA_HORIZONTAL, 4);
		new Label(this, SWT.NONE).setText("Maximum width: ");
		widthSpinner = new Spinner(this, SWT.BORDER);
		widthSpinner.setMaximum(10000);
		
		new Label(this, SWT.NONE).setText("Maximum height: ");
		heightSpinner = new Spinner(this, SWT.BORDER);
		heightSpinner.setMaximum(10000);
		
		XComposite comp = new XComposite(this, SWT.NONE, LayoutMode.TIGHT_WRAPPER, LayoutDataMode.GRID_DATA_HORIZONTAL, 3);
		comp.getGridData().heightHint = 150;
		comp.getGridData().horizontalSpan = 4;
		new Label(comp, SWT.NONE).setText("Allowed extensions: ");
		formatList = new ListComposite<String>(comp, SWT.NONE);
		
		XComposite editComp = new XComposite(comp, SWT.NONE, LayoutMode.TIGHT_WRAPPER, LayoutDataMode.GRID_DATA, 2);
		Button addButton = new Button(editComp, SWT.PUSH);
		addButton.setText("+");
		
		final Text newFormat = new Text(editComp, SWT.BORDER);
		GridData gd = new GridData();
		gd.verticalSpan = 2;
		gd.grabExcessHorizontalSpace = true;
		gd.verticalAlignment = SWT.CENTER;
		newFormat.setLayoutData(gd);
		
		Button remButton = new Button(editComp, SWT.PUSH);
		remButton.setText("-");
		
		widthSpinner.addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent e) {}
			public void focusLost(FocusEvent e) {
				imageField.setWidth(widthSpinner.getSelection());
			}
		});
		
		heightSpinner.addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent e) {}
			public void focusLost(FocusEvent e) {
				imageField.setHeight(heightSpinner.getSelection());
			}
		});
		
		addButton.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {}
			public void widgetSelected(SelectionEvent e) {
				String text = newFormat.getText();
				if (text.length() == 3) {
					formatList.addElement(text);
					imageField.addImageFormat(text);
				}
			}
		});
		
		remButton.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {}
			public void widgetSelected(SelectionEvent e) {
				String text = newFormat.getText();
				if (text.length() == 3) {
					formatList.removeElement(text);
					imageField.removeImageFormat(text);
				}
			}
		});
		
		setEnabled(false);
	}
	
	protected void setField(ImageStructField field) {
		if (field == null) {
			setEnabled(false);
			return;
		}
		
		setEnabled(true);
		
		this.imageField = field;
		formatList.setInput(imageField.getImageFormats());
		widthSpinner.setSelection(imageField.getWidth());
		heightSpinner.setSelection(imageField.getHeight());
	}
}
