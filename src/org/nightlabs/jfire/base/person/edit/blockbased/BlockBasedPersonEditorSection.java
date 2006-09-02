/* *****************************************************************************
 * JFire - it's hot - Free ERP System - http://jfire.org                       *
 * Copyright (C) 2004-2006 NightLabs - http://NightLabs.org                    *
 *                                                                             *
 * This library is free software; you can redistribute it and/or               *
 * modify it under the terms of the GNU Lesser General Public                  *
 * License as published by the Free Software Foundation; either                *
 * version 2.1 of the License, or (at your option) any later version.          *
 *                                                                             *
 * This library is distributed in the hope that it will be useful,             *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of              *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU           *
 * Lesser General Public License for more details.                             *
 *                                                                             *
 * You should have received a copy of the GNU Lesser General Public            *
 * License along with this library; if not, write to the                       *
 *     Free Software Foundation, Inc.,                                         *
 *     51 Franklin St, Fifth Floor,                                            *
 *     Boston, MA  02110-1301  USA                                             *
 *                                                                             *
 * Or get it online :                                                          *
 *     http://www.gnu.org/copyleft/lesser.html                                 *
 ******************************************************************************/
package org.nightlabs.jfire.base.person.edit.blockbased;

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
import org.nightlabs.jfire.base.person.PersonStructProvider;
import org.nightlabs.jfire.base.person.edit.PersonDataFieldEditor;
import org.nightlabs.jfire.person.Person;

/**
 * A section containing a block based Person editor.
 * 
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 */
public class BlockBasedPersonEditorSection extends RestorableSectionPart
{
	/**
	 * Logger used by this class.
	 */
	private static final Logger logger = Logger.getLogger(BlockBasedPersonEditorSection.class);
	
	/**
	 * The person editor used in this section.
	 */
	private BlockBasedPersonEditor blockBasedPersonEditor;

	/**
	 * The person editor control showed in this section.
	 */
	private Control blockBasedPersonEditorControl;	

	/**
	 * Create an instance of UserPropertiesSection.
	 * @param parent The parent for this section
	 * @param toolkit The toolkit to use
	 */
	public BlockBasedPersonEditorSection(FormPage page, Composite parent, String sectionDescriptionText)
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
		blockBasedPersonEditor.updatePerson();
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

		blockBasedPersonEditor = new BlockBasedPersonEditor();
		blockBasedPersonEditorControl = blockBasedPersonEditor.createControl(container, false);
		blockBasedPersonEditorControl.setLayoutData(new GridData(GridData.FILL_BOTH));

		blockBasedPersonEditor.setChangeListener(new PersonDataBlockEditorChangedListener() {
			public void personDataBlockEditorChanged(PersonDataBlockEditor dataBlockEditor, PersonDataFieldEditor dataFieldEditor)
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
				PersonStructProvider.getPersonStructure().explodePerson(person);
				blockBasedPersonEditor.setPerson(person, true);
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
