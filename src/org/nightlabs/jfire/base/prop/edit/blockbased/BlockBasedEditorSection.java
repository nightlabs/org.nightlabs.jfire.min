/**
 * 
 */
package org.nightlabs.jfire.base.prop.edit.blockbased;

import org.apache.log4j.Logger;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormText;
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
	 * Logger used by this class.
	 */
	private static final Logger logger = Logger.getLogger(BlockBasedEditorSection.class);
	
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
		super(parent, page.getEditor().getToolkit(), ExpandableComposite.TITLE_BAR | ExpandableComposite.TWISTIE | ExpandableComposite.TITLE_BAR);
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
		if (sectionDescriptionText == null || "".endsWith(sectionDescriptionText))
			return;
		FormText text = toolkit.createFormText(section, true);
		try {
			text.setText(sectionDescriptionText, true, false);
		} catch (Exception e) {
			logger.warn("Could not set Text for person editor section (description): "+sectionDescriptionText, e);
		}
		section.setDescriptionControl(text);
	}
}
