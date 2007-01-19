package org.nightlabs.jfire.base.prop.structedit;

import org.eclipse.swt.widgets.Composite;
import org.nightlabs.base.language.I18nTextEditor;
import org.nightlabs.base.language.LanguageChooser;
import org.nightlabs.jfire.prop.StructBlock;

public class StructBlockEditor implements StructPartEditor<StructBlock> {

	private StructBlockEditorComposite structBlockEditorComposite;
	
	public Composite createComposite(Composite parent, int style, StructEditor structEditor, LanguageChooser languageChooser) {
		structBlockEditorComposite = new StructBlockEditorComposite(parent, style, languageChooser);
		return structBlockEditorComposite;
	}

	public Composite getComposite() {
		return structBlockEditorComposite;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.nightlabs.jfire.base.prop.structedit.StructPartEditor#setData(java.lang.Object)
	 */
	public void setData(StructBlock data) {
		if (structBlockEditorComposite == null)
			throw new IllegalStateException("You have to call createComposite(...) prior to calling setData(...)");
		
		structBlockEditorComposite.setStructBlock(data);
	}

	public void setEnabled(boolean enabled) {
		if (structBlockEditorComposite != null)
			structBlockEditorComposite.setEnabled(enabled);
	}

	public I18nTextEditor getPartNameEditor() {
		return structBlockEditorComposite.getBlockNameEditor();
	}
}
