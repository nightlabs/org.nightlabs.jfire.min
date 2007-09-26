package org.nightlabs.jfire.base.prop.search;

public interface PropertySetSearchFilterItemEditorHelperFactory<Helper extends PropertySetSearchFilterItemEditorHelper> {
	/**
	 * Should create a new instance of a PropertySetSearchFilterItemEditorHelper.
	 */
	Helper createHelper();
}

