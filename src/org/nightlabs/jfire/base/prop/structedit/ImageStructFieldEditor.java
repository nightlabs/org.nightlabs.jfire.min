package org.nightlabs.jfire.base.prop.structedit;

import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jface.viewers.LabelProvider;
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
import org.nightlabs.jfire.prop.ModifyListener;
import org.nightlabs.jfire.prop.structfield.ImageStructField;

public class ImageStructFieldEditor extends AbstractStructFieldEditor<ImageStructField> {
	private ImageStructFieldEditorComposite imageStructFieldEditorComposite;
	private ImageStructField imageField;
	
	public static class ImageStructFieldEditorFactory extends AbstractStructFieldEditorFactory {
		public String getStructFieldEditorClass() {
			return ImageStructFieldEditor.class.getName();
		}
	}
	
	public ImageStructFieldEditor() {
		
	}

	@Override
	protected Composite createSpecialComposite(Composite parent, int style) {
		imageStructFieldEditorComposite = new ImageStructFieldEditorComposite(parent, this);
		return imageStructFieldEditorComposite;
	}

	@Override
	protected void setSpecialData(ImageStructField field) {
		imageField = field;
		imageStructFieldEditorComposite.setField(field);
		
		imageField.addModifyListener(new ModifyListener() {
			public void modifyData() {
				updateErrorMessage();
			}
		});
	}
	
	protected void updateErrorMessage() {
		if (!imageField.validateData()) {
			setErrorMessage(imageField.getValidationError());
		}	else {
			setErrorMessage("");
		}
	}
	
	@Override
	public boolean validateInput() {
		return imageField.validateData();
	}
	
	@Override
	public String getErrorMessage() {
		return imageField.getValidationError();
	}
	
	@Override
	public void restoreData() {
		imageField.clearImageFormats();
		imageField.addImageFormat("*");
	}
}

class ImageStructFieldEditorComposite extends XComposite implements Serializable {
	private Spinner sizeSpinner;
	private ListComposite<String> formatList;
	private ImageStructField imageField;
	private ImageStructFieldEditor editor;
	
	public ImageStructFieldEditorComposite(Composite parent, ImageStructFieldEditor imageStructFieldEditor) {
		super(parent, SWT.NONE, LayoutMode.TIGHT_WRAPPER, LayoutDataMode.GRID_DATA, 2);
		
		this.editor = imageStructFieldEditor;
		new Label(this, SWT.NONE).setText("Maximum size (KB): ");
		sizeSpinner = new Spinner(this, SWT.BORDER);
		sizeSpinner.setMaximum(Integer.MAX_VALUE);		
		
//		XComposite comp = new XComposite(this, SWT.NONE, LayoutMode.TIGHT_WRAPPER, LayoutDataMode.GRID_DATA);
//		comp.getGridData().heightHint = 250;
//		comp.getGridData().horizontalSpan = 2;
		
		new Label(this, SWT.NONE); new Label(this, SWT.NONE); // Spacers
		
		new Label(this, SWT.NONE).setText("Allowed extensions: ");
		XComposite extComp = new XComposite(this, SWT.NONE, LayoutMode.TIGHT_WRAPPER, LayoutDataMode.GRID_DATA, 2);
		extComp.getGridData().horizontalSpan = 2;
		
		GridData gd = new GridData();
		
		XComposite editComp = new XComposite(extComp, SWT.NONE, LayoutMode.TIGHT_WRAPPER, LayoutDataMode.GRID_DATA, 3);
		formatList = new ListComposite<String>(editComp, SWT.V_SCROLL);
		formatList.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				return "*." + element;
			}
		});
		
		XComposite buttonComp = new XComposite(editComp, SWT.NONE, LayoutMode.TIGHT_WRAPPER, LayoutDataMode.NONE);
		gd.widthHint = 25;
		final Button addButton = new Button(buttonComp, SWT.PUSH);
		addButton.setText("+");
		addButton.setLayoutData(gd);		
		final Button remButton = new Button(buttonComp, SWT.PUSH);
		remButton.setText("-");
		remButton.setLayoutData(gd);
		
		final Text newFormat = new Text(editComp, SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.verticalSpan = 2;
		gd.verticalAlignment = SWT.CENTER;
		newFormat.setLayoutData(gd);
		
		sizeSpinner.addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent e) {}
			public void focusLost(FocusEvent e) {
				imageField.setMaxSizeKB(sizeSpinner.getSelection());
			}
		});
		
		addButton.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {}
			public void widgetSelected(SelectionEvent e) {
				if (addExtension(newFormat.getText())) {
					newFormat.setText("");
					newFormat.setFocus();
				}
				editor.setChanged();
			}
		});
		
		remButton.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {}
			public void widgetSelected(SelectionEvent e) {
				imageField.removeImageFormat(formatList.getSelectedElement());
				formatList.setInput(imageField.getImageFormats());
				formatList.setSelection(0);
				editor.setChanged();
			}
		});
		
		newFormat.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				if (addExtension(newFormat.getText())) {
					newFormat.setText("");
					newFormat.setFocus();
				}
			}
			public void widgetSelected(SelectionEvent e) {}
		});
	}		
	
	protected void setField(ImageStructField field) {
		if (field == null) {
			setEnabled(false);
			return;
		}
		
		setEnabled(true);
		
		this.imageField = field;
		formatList.setInput(imageField.getImageFormats());
		sizeSpinner.setSelection((int) imageField.getMaxSizeKB());		
	}
	
	protected boolean addExtension(String ext) {
		String text = ext;
		Matcher extMatcher = Pattern.compile("(?:\\*\\.|\\.)?([\\w\\*]+)").matcher(text);
		if (extMatcher.matches()) {
			text = extMatcher.group(1);
			imageField.addImageFormat(text);
			formatList.setInput(imageField.getImageFormats());
			return true;
		}
		return false;
	}
}
