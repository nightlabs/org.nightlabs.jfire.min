/*
 * Created 	on Jun 17, 2004
 * 					by alex
 *
 */
package org.nightlabs.ipanema.base.person.edit.blockbased;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;

import org.nightlabs.base.composite.groupedcontent.GroupedContentComposite;
import org.nightlabs.base.composite.groupedcontent.GroupedContentProvider;
import org.nightlabs.ipanema.base.person.PersonStructProvider;
import org.nightlabs.ipanema.person.Person;
import org.nightlabs.ipanema.person.PersonDataBlockGroup;

/**
 * @see org.nightlabs.ipanema.base.person.edit.blockbased.PersonDataBlockEditor
 * @see org.nightlabs.ipanema.base.person.edit.blockbased.PersonEditorStructBlockRegistry
 * @see org.nightlabs.ipanema.base.person.edit.PersonEditor
 *  
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 */
public class BlockBasedPersonEditor extends AbstractBlockBasedPersonEditor {
	
	private static Logger LOGGER = Logger.getLogger(BlockBasedPersonEditor.class);
	public static final String EDITORTYPE_BLOCK_BASED = "block-based";
	
	private GroupedContentComposite groupedContentComposite;
	
	private class ContentProvider implements GroupedContentProvider {
		private PersonDataBlockGroupEditor groupEditor;
		private PersonDataBlockGroup blockGroup;
		
		public ContentProvider(PersonDataBlockGroup blockGroup) {
			this.blockGroup = blockGroup;			
		}
		
		public Image getGroupIcon() {
			return null;
		}
		public String getGroupTitle() {
			return blockGroup.getPersonStructBlock(PersonStructProvider.getPersonStructure()).getPersonStructBlockID();
		}
		public Composite createGroupContent(Composite parent) {
			groupEditor = new PersonDataBlockGroupEditor(blockGroup, parent);
			if (changeListener != null)
				groupEditor.addPersonDataBlockEditorChangedListener(changeListener);
			return groupEditor;
		}
		public void refresh(PersonDataBlockGroup blockGroup) {
			if (groupEditor != null) {
				groupEditor.refresh(blockGroup);
			}
			this.blockGroup = blockGroup;
		}
		public void updatePerson() {
			if (groupEditor != null) {
				groupEditor.updatePerson();
			}
		}
	}
	
	public BlockBasedPersonEditor() {
		super (null);
	}
	
	public BlockBasedPersonEditor(Person person) {
		super(person);
	}
	
	private Map groupContentProvider = new HashMap();
	/**
	 * Refreshes the UI-Representation of the given Person.
	 * 
	 * @param changeListener
	 */
	public void refreshControl() {
		Display.getDefault().asyncExec( 
			new Runnable() {
				public void run() {
					getPersonStructure().explodePerson(person);
					
					// get the ordered dataBlocks
					for (Iterator it = BlockBasedPersonEditor.this.getOrderedDataBlockGroupsIterator(); it.hasNext(); ) {
						PersonDataBlockGroup blockGroup = (PersonDataBlockGroup)it.next();
						if (shouldDisplayStructBlock(blockGroup)) {
							if (!groupContentProvider.containsKey(blockGroup.getStructBlockKey())) {
								ContentProvider contentProvider = new ContentProvider(blockGroup);
								groupContentProvider.put(blockGroup.getStructBlockKey(),contentProvider);
								groupedContentComposite.addGroupedContentProvider(contentProvider);
							}
							else {			
								ContentProvider contentProvider = (ContentProvider)groupContentProvider.get(blockGroup.getStructBlockKey());								
								contentProvider.refresh(blockGroup);
							}
						} // if (shouldDisplayStructBlock(blockGroup)) {
					}		
					groupedContentComposite.layout();
				}
			}
		);
	}

	private PersonDataBlockEditorChangedListener changeListener;
	
	public Control createControl(Composite parent, PersonDataBlockEditorChangedListener changeListener, boolean refresh) {
		this.changeListener = changeListener;
		return createControl(parent, refresh);
	}
	
	/**
	 * @param changeListener The changeListener to set.
	 */
	public void setChangeListener(
			PersonDataBlockEditorChangedListener changeListener) {
		this.changeListener = changeListener;
	}

	public Control createControl(Composite parent, boolean refresh) {
		if (groupedContentComposite == null) {
			groupedContentComposite = new GroupedContentComposite(parent, SWT.NONE, true);
			groupedContentComposite.setGroupTitle("personTail");
		}
		if (refresh)
			refreshControl();
		return groupedContentComposite;
	}

	public void disposeControl() {
		if (groupedContentComposite != null)
			if (!groupedContentComposite.isDisposed())
				groupedContentComposite.dispose();
		groupedContentComposite = null;
	}

	public void updatePerson() {
		for (Iterator it = groupContentProvider.values().iterator(); it.hasNext(); ) {
			ContentProvider contentProvider = (ContentProvider)it.next();
			contentProvider.updatePerson();
		}
	}
	
}
