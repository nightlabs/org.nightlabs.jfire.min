package org.nightlabs.jfire.base.prop.structedit;

import org.eclipse.swt.widgets.Composite;
import org.nightlabs.base.composite.XComposite;
import org.nightlabs.base.language.LanguageChooser;
import org.nightlabs.jfire.prop.AbstractStructField;

public class TextStructFieldEditor implements IStructFieldEditor
{
	private Composite comp = null;
	
	public static class TextStructFieldEditorFactory extends AbstractStructFieldEditorFactory
	{
		public String getStructFieldEditorClass()
		{
			return TextStructFieldEditor.class.getName();
		}		
	}
	
	public TextStructFieldEditor() { }
	
	public void setData(AbstractStructField field)
	{
		
	}

	public Composite createComposite(Composite parent, int style, LanguageChooser langChooser, StructEditor structEditor)
	{
		comp = new XComposite(parent, style);
		return comp;
	}

	public Composite getComposite()
	{
		return comp;
	}	
}
