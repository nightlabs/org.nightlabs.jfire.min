/**
 * 
 */
package org.nightlabs.jfire.prop.datafield;

import java.util.Locale;

import org.nightlabs.i18n.I18nText;
import org.nightlabs.jfire.prop.DataField;
import org.nightlabs.jfire.prop.IDataField;

/**
 * Common interface for all subclasses of {@link DataField} that
 * can return a text representation of themselves depending on a given {@link Locale}.
 * 
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 *
 */
public interface II18nTextDataField extends IDataField {

//	/**
//	 * Returns the text representation of this field for the given {@link Locale}.
//	 * 
//	 * @param locale The locale the text representation should be based on.
//	 * @return The text representation of this field for the given {@link Locale}.
//	 */
//	String getText(Locale locale);
	
	/**
	 * Returns the internationalized text representation of this field.
	 * 
	 * @return The text internationalized representation of this field.
	 */
	I18nText getI18nText();
}
