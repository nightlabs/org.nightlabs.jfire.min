/*
 * Created 	on Nov 30, 2004
 * 					by alex
 *
 */
package org.nightlabs.jfire.base.person.edit.blockbased;

import org.nightlabs.jfire.base.person.edit.PersonDataFieldEditor;

/**
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 */
public interface PersonDataBlockEditorChangedListener {
	public void personDataBlockEditorChanged(PersonDataBlockEditor dataBlockEditor, PersonDataFieldEditor dataFieldEditor);
}
