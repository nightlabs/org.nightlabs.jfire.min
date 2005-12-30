/*
 * Created 	on Jan 10, 2005
 * 					by alex
 *
 */
package org.nightlabs.ipanema.base.person.edit.fieldbased;


/**
 * 
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 *
 */
public class DisguisedPersonEditor extends FieldBasedPersonEditor {

	public static final String EDITORTYPE_FIELD_BASED_DISGUISED = "field-based-disguised";
	
	/**
	 * 
	 */
	public DisguisedPersonEditor() {
		super();
		setShowEmptyFields(false);
		setEditorType(EDITORTYPE_FIELD_BASED_DISGUISED);
	}
	
}
