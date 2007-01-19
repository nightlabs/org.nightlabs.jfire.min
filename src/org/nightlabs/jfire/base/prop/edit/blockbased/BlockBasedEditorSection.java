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
import org.nightlabs.jfire.base.prop.StructLocalDAO;
import org.nightlabs.jfire.base.prop.edit.DataFieldEditor;
import org.nightlabs.jfire.person.Person;
import org.nightlabs.jfire.prop.IStruct;

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

	/* (non-Javadoc)
	 * @see org.eclipse.ui.forms.AbstractFormPart#commit(boolean)
	 */
	@Override
	public void commit(boolean onSave)
	{
		System.err.println("***********************************");
		System.err.println("Person properties: commit("+onSave+")");
		System.err.println("***********************************");
		super.commit(onSave);
		blockBasedPersonEditor.updateProperty();
	}

	/**
	 * Create the content for this section.
	 * @param section The section to fill
	 * @param toolkit The toolkit to use
	 */
	protected void createClient(Section section, FormToolkit toolkit, String sectionDescriptionText) 
	{
//		section.setText("User Properties");
//		section.setExpanded(true);
		section.setLayout(new GridLayout());
		section.setLayoutData(new GridData(GridData.FILL_BOTH));

		createDescriptionControl(section, toolkit, sectionDescriptionText);

		Composite container = EntityEditorUtil.createCompositeClient(toolkit, section, 1);

		blockBasedPersonEditor = new BlockBasedEditor();
		blockBasedPersonEditorControl = blockBasedPersonEditor.createControl(container, false);
		blockBasedPersonEditorControl.setLayoutData(new GridData(GridData.FILL_BOTH));

		blockBasedPersonEditor.setChangeListener(new DataBlockEditorChangedListener() {

			public void propDataBlockEditorChanged(DataBlockEditor dataBlockEditor, DataFieldEditor dataFieldEditor) 
			{
				markDirty();
			}
		});
	}
	
	public void setPerson(final Person person) {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				if(person == null)
					return;
				IStruct struct = StructLocalDAO.sharedInstance().getStructLocal(Person.class);
				struct.explodeProperty(person);
				blockBasedPersonEditor.setProperty(person, struct, true);
			}
		});
	}

	private void createDescriptionControl(Section section, FormToolkit toolkit, String sectionDescriptionText)
	{
		if (sectionDescriptionText == null || "".equals(sectionDescriptionText))
			return;

		section.setText(sectionDescriptionText);
	}
}
