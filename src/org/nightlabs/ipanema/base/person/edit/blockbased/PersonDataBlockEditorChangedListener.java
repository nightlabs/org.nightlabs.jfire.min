/*
 * Created 	on Nov 30, 2004
 * 					by alex
 *
 */
package org.nightlabs.ipanema.base.person.edit.blockbased;

import org.nightlabs.ipanema.base.person.edit.PersonDataFieldEditor;

/**
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 */
public interface PersonDataBlockEditorChangedListener {
	public void personDataBlockEditorChanged(PersonDataBlockEditor dataBlockEditor, PersonDataFieldEditor dataFieldEditor);
}
