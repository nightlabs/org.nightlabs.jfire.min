/**
 * 
 */
package org.nightlabs.jfire.base.prop.edit.blockbased;

import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.nightlabs.base.editor.RestorableSectionPart;
import org.nightlabs.base.entity.editor.EntityEditorUtil;
import org.nightlabs.jfire.base.prop.edit.DataFieldEditor;
import org.nightlabs.jfire.prop.AbstractDataField;
import org.nightlabs.jfire.prop.IStruct;
import org.nightlabs.jfire.prop.PropertySet;

/**
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 *
 */
public class BlockBasedEditorSection extends RestorableSectionPart
{
	/**
	 * The person editor used in this section.
	 */
	private BlockBasedEditor blockBasedPersonEditor;

	/**
	 * The person editor control showed in this section.
	 */
	private Control blockBasedPersonEditorControl;	

	/**
	 * Create an instance of UserPropertiesSection.
	 * @param parent The parent for this section
	 * @param toolkit The toolkit to use
	 */
	public BlockBasedEditorSection(FormPage page, Composite parent, String sectionDescriptionText)
	{
		super(parent, page.getEditor().getToolkit(), ExpandableComposite.EXPANDED | ExpandableComposite.TITLE_BAR | ExpandableComposite.TWISTIE | ExpandableComposite.TITLE_BAR);
		createClient(getSection(), page.getEditor().getToolkit(), sectionDescriptionText);
	}

	/**
	 * Create an instance of UserPropertiesSection.
	 * @param parent The parent for this section
	 * @param toolkit The toolkit to use
	 */
	public BlockBasedEditorSection(FormPage page, Composite parent, int sectionType, String sectionDescriptionText)
	{
		super(parent, page.getEditor().getToolkit(), sectionType);
		createClient(getSection(), page.getEditor().getToolkit(), sectionDescriptionText);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.forms.AbstractFormPart#commit(boolean)
	 */
	@Override
	public void commit(boolean onSave)
	{
		super.commit(onSave);
		blockBasedPersonEditor.updatePropertySet();
	}

	/**
	 * Create the content for this section.
	 * @param section The section to fill
	 * @param toolkit The toolkit to use
	 */
	protected void createClient(Section section, FormToolkit toolkit, String sectionDescriptionText) 
	{
		section.setLayout(new GridLayout());
		section.setLayoutData(new GridData(GridData.FILL_BOTH));
		createDescriptionControl(section, toolkit, sectionDescriptionText);
		Composite container = EntityEditorUtil.createCompositeClient(toolkit, section, 1);

		blockBasedPersonEditor = new BlockBasedEditor();
		blockBasedPersonEditorControl = blockBasedPersonEditor.createControl(container, false);
		blockBasedPersonEditorControl.setLayoutData(new GridData(GridData.FILL_BOTH));
		blockBasedPersonEditor.setChangeListener(new DataBlockEditorChangedListener() {
			public void propDataBlockEditorChanged(AbstractDataBlockEditor dataBlockEditor, DataFieldEditor<? extends AbstractDataField> dataFieldEditor) {
				markDirty();
			}
		});
	}
	
	public void setProperty(final PropertySet property, final IStruct struct) {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				if(property == null)
					return;
				blockBasedPersonEditor.setPropertySet(property, true);
			}
		});
	}

	private void createDescriptionControl(Section section, FormToolkit toolkit, String sectionDescriptionText)
	{
		if (sectionDescriptionText == null || "".equals(sectionDescriptionText)) //$NON-NLS-1$
			return;

		section.setText(sectionDescriptionText);
	}
}
