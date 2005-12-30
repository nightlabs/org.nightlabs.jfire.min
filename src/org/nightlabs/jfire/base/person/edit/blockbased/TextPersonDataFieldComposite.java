/*
 * Created 	on Nov 26, 2004
 * 					by alex
 *
 */
package org.nightlabs.jfire.base.person.edit.blockbased;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import org.nightlabs.jfire.base.person.edit.AbstractPersonDataFieldComposite;

/**
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 */
public class TextPersonDataFieldComposite extends AbstractPersonDataFieldComposite {

	private Label fieldName;
	private Text fieldText;
	private TextPersonDataFieldEditor editor;
	private ModifyListener modifyListener;
	
	/**
	 * Assumes to have a parent composite with GridLaout and
	 * adds it own GridData.
	 * @param editor
	 * @param parent
	 * @param style
	 */
	public TextPersonDataFieldComposite(TextPersonDataFieldEditor editor, Composite parent, int style, ModifyListener modListener) {
		super(parent, style);
		if (!(parent.getLayout() instanceof GridLayout))
			throw new IllegalArgumentException("Parent should have a GridLayout!");
		
		this.editor = editor;
		
		GridLayout layout = new GridLayout();
		setLayout(layout);
		layout.horizontalSpacing = 0;
		layout.verticalSpacing = 0;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		GridData gridData = new GridData(GridData.FILL_BOTH);
		setLayoutData(gridData);
		
		fieldName = new Label(this,SWT.PUSH);
		GridData nameData = new GridData(GridData.FILL_HORIZONTAL);
		nameData.grabExcessHorizontalSpace = true;
		fieldName.setLayoutData(nameData);
		
		fieldText = new Text(this, SWT.PUSH | SWT.BORDER | SWT.SINGLE);
		GridData textData = new GridData(GridData.FILL_HORIZONTAL);
		textData.grabExcessHorizontalSpace = true;
		fieldText.setEditable(true);
		fieldText.setEnabled(true);
		fieldText.setLayoutData(textData);
		this.modifyListener = modListener;
		fieldText.addModifyListener(modifyListener);
	}

	/**
	 * @see org.nightlabs.jfire.base.person.edit.AbstractPersonDataFieldComposite#refresh()
	 */
	public void refresh() {
//		fieldName.setText(editor.getPersonStructField().getFieldName().getText());
		// TODO: reactivate above line
		fieldName.setText(editor.getPersonStructField().getPersonStructFieldID());
		if (editor.getData().getText() == null)
			fieldText.setText("");
		else
			fieldText.setText(editor.getData().getText());
	}
	
	public String getText() {
		return fieldText.getText();
	}
	
	
	
	public void dispose() {
		fieldText.removeModifyListener(modifyListener);
		super.dispose();
	}
}
