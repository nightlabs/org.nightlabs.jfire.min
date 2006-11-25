package org.nightlabs.jfire.base.prop.structedit;

import org.eclipse.swt.widgets.Composite;
import org.nightlabs.base.language.LanguageChooser;
import org.nightlabs.jfire.prop.AbstractStructField;

public interface IStructFieldEditor
{
	/**
	 * Editors should refresh their data during this method. The composite should be refreshed as well.
	 * @param field
	 */
	public void setData(AbstractStructField field);
	
		
	/**
	 * Here a data field editor should add its control to a parent composite.<br/>
	 * The Composite returned should be a singleton and be updated with data changes.
	 * No data-display will be made here. See {@link #setData(AbstractStructField)}.
	 *
	 * @param parent the parent of the composite
	 * @param style SWT style flag
	 * @param langChooser The {@link LanguageChooser} that controls the language currently edited.
	 * @param structEditor TODO
	 * @return a composite containing the struct field specific editor
	 */
	public Composite createComposite(Composite parent, int style, LanguageChooser langChooser, StructEditor structEditor);
	
	/**
	 * 
	 * @return
	 */
	public Composite getComposite();
}
