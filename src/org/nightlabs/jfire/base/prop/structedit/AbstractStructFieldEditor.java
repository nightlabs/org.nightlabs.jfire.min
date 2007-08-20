package org.nightlabs.jfire.base.prop.structedit;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.nightlabs.base.composite.XComposite;
import org.nightlabs.base.composite.XComposite.LayoutMode;
import org.nightlabs.base.language.I18nTextEditor;
import org.nightlabs.base.language.LanguageChooser;
import org.nightlabs.base.language.I18nTextEditor.EditMode;
import org.nightlabs.jfire.base.JFireBasePlugin;
import org.nightlabs.jfire.base.resource.Messages;
import org.nightlabs.jfire.prop.StructField;

public abstract class AbstractStructFieldEditor<F extends StructField> implements StructFieldEditor<F> {
	
	private Composite specialComposite;
	private I18nTextEditor fieldNameEditor;
	private StructEditor structEditor;
	private F structField;
	private LanguageChooser languageChooser;
	private ErrorComposite errorComp;
	private String errorMessage;
	private Group editorGroup;
	
	public void setChanged() {
		getStructEditor().setChanged(true);
	}
	
	public Composite createComposite(Composite parent, int style, StructEditor structEditor, LanguageChooser languageChooser) {
		this.languageChooser = languageChooser;
		this.structEditor = structEditor;
		editorGroup = new Group(parent, SWT.NONE);		
		editorGroup.setLayoutData(new GridData(GridData.FILL_BOTH));
		GridLayout gl = new GridLayout();
		XComposite.configureLayout(LayoutMode.ORDINARY_WRAPPER, gl);
		editorGroup.setLayout(gl);
		((GridLayout)editorGroup.getLayout()).marginTop = 15;
				
		fieldNameEditor = new I18nTextEditor(editorGroup, this.languageChooser, Messages.getString("org.nightlabs.jfire.base.prop.structedit.AbstractStructFieldEditor.fieldNameEditor.caption")); //$NON-NLS-1$
		fieldNameEditor.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		new Label(editorGroup, SWT.SEPARATOR | SWT.HORIZONTAL).setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		specialComposite = createSpecialComposite(editorGroup, style);

		errorComp = new ErrorComposite(editorGroup);
		return editorGroup;
	}
	
	public void setErrorMessage(String error) {
		this.errorMessage = error;
		errorComp.setErrorMessage(error);
	}
	
	protected StructEditor getStructEditor() {
		return structEditor;
	}
	
	protected F getStructField() {
		return structField;
	}
	
	public I18nTextEditor getFieldNameEditor() {
		return fieldNameEditor;
	}
	
	public LanguageChooser getLanguageChooser() {
		return languageChooser;
	}
	
	public Composite getComposite() {
		return editorGroup;
	}

	public void setData(F field) {
		if (editorGroup == null)
			throw new IllegalStateException("You have to call createComposite(...) prior to calling setData(...)"); //$NON-NLS-1$
		
		if (field == null)
		{
			fieldNameEditor.reset();
			fieldNameEditor.setEnabled(false);
			if (specialComposite != null)
				specialComposite.dispose();
			return;
		}
		
		fieldNameEditor.setEnabled(true);		
		fieldNameEditor.setI18nText(field.getName(), EditMode.DIRECT);
		StructFieldMetaData sfmd = StructFieldFactoryRegistry.sharedInstance().getFieldMetaDataMap().get(field.getClass().getName());
		if (sfmd != null) {
			String fieldName = sfmd.getFieldName();
			editorGroup.setText(fieldName);
		} else
			editorGroup.setText("Uknown field type"); //$NON-NLS-1$
		
		setSpecialData(field);
	}
	
	protected abstract void setSpecialData(F field);
	
	/*
	 * (non-Javadoc)
	 * @see org.nightlabs.jfire.base.prop.structedit.StructPartEditor#getPartNameEditor()
	 */
	public I18nTextEditor getPartNameEditor() {
		return fieldNameEditor;
	}
	
	/**
	 * Extendors should create struct field specific gui in this method and render the
	 * data of the struct field since this method is called every time a new struct
	 * field is selected.
	 * 
	 * @param parent
	 * @param style
	 * @return
	 */
	protected abstract Composite createSpecialComposite(Composite parent, int style);
	
	/**
	 * This method is intended to be overridden if the managed struct field supports validation.
	 * Extendors should save their data in order to restore it upon a call to {@link #restoreData()}:
	 * This happens if validation fails and the user wants to discard the changes.
	 * 
	 * @see org.nightlabs.jfire.base.prop.structedit.StructFieldEditor#saveData()
	 */
	public void saveData() {
		// do nothing by default
	}
	
	/**
	 * This method is intended to be overriden if the managed struct field supports validation.
	 * Extendors should restore the data previously saved by {@link #saveData()}.
	 * This method is called if validation fails and the user wants to discard the changes.
	 * 
	 * @see org.nightlabs.jfire.base.prop.structedit.StructFieldEditor#restoreData()
	 */
	public void restoreData() {
		// do nothing by default
	}
	
	public String getErrorMessage() {
		return errorMessage;
	}
	
	/**
	 * This method is intended to be overriden by struct editors that require validation
	 * and should return a boolean indicating whether the user input is valid for the
	 * type of struct field. In addition, extendors can call {@link #setErrorMessage(String)}
	 * to display a message explaining the validation error if there is one.
	 */
	public boolean validateInput() {
		return true; // no validation done by default
	}
		
	public void setEnabled(boolean enabled) {
		if (editorGroup != null)
			editorGroup.setEnabled(enabled);
	}
}

class ErrorComposite extends XComposite {
	private Image errorImage;
	private Label errorLabel;
	private Label errorImageLabel;
	
	public ErrorComposite(Composite parent) {
		super(parent, SWT.NONE, LayoutMode.ORDINARY_WRAPPER, LayoutDataMode.GRID_DATA_HORIZONTAL, 2);
		
		errorImage = JFireBasePlugin.getImageDescriptor("icons/Validation_error.gif").createImage(); //$NON-NLS-1$
		
		errorImageLabel = new Label(this, SWT.NONE);
		errorLabel = new Label(this, SWT.NONE);		
		errorImageLabel.setImage(errorImage);
		errorLabel.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_END));
		setVisible(false);
		
		addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				if (errorImage != null)
					errorImage.dispose();
			}
		});
	}
	
	protected void setErrorMessage(String error) {
		if (error == null || error.equals("")) { //$NON-NLS-1$
			setVisible(false);
		} else {
			errorLabel.setText(error);
			setVisible(true);
		}
		errorLabel.pack();
		pack();
		layout();
	}
}