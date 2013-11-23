/**
 * 
 */
package org.nightlabs.jfire.prop.validation;

import java.util.Set;

/**
 * Interface for implementations of {@link IPropertyElementValidator} which are script based.
 * 
 * @author Daniel Mazurek - Daniel.Mazurek [dot] nightlabs [dot] de
 */
public interface IScriptValidator<DataElement, StructElement>
extends IPropertyElementValidator<DataElement, StructElement>
{
	/**
	 * Returns the scriptLanguage.
	 * @return the scriptLanguage
	 */
	public String getScriptLanguage();
	
	/**
	 * Sets the scriptLanguage.
	 * @param scriptLanguage the scriptLanguage to set
	 */
	public void setScriptLanguage(String scriptLanguage);

	/**
	 * Returns the script.
	 * @return the script
	 */
	public String getScript();
	
	/**
	 * Sets the script.
	 * @param script the script to set
	 */
	public void setScript(String script);
	
	/**
	 * 
	 * @param key the key for the validationResult
	 * @param validationResult the validationResult for the given key
	 */
	public void addValidationResult(String key, I18nValidationResult validationResult);
	
	/**
	 * Removes the I18nValidationResult which is associated with the given key.
	 * @param key the key to remove.
	 */
	public void removeValidationResult(String key);
	
	/**
	 * Returns all result keys.
	 * @return all result keys.
	 */
	public Set<String> getValidationResultKeys();
	
	/**
	 * 
	 * @param key the key for the I18nValidationResult.
	 * @return the I18nValidationResult for the given key.
	 */
	public I18nValidationResult getValidationResult(String key);
}
