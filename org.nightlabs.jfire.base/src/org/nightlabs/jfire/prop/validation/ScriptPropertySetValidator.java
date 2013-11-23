/**
 *
 */
package org.nightlabs.jfire.prop.validation;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.FetchGroups;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Join;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.PersistenceModifier;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;
import javax.jdo.annotations.Value;
import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.apache.log4j.Logger;
import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.prop.IStruct;
import org.nightlabs.jfire.prop.PropertySet;
import org.nightlabs.jfire.prop.validation.id.ScriptPropertySetValidatorID;

/**
 * Implementation of {@link IPropertySetValidator} that is able to validate a {@link PropertySet}
 * based on an script of a certain script-language (e.g. JavaScript).
 *
 * @jdo.persistence-capable
 * 		identity-type="application"
 * 		objectid-class="org.nightlabs.jfire.prop.validation.id.ScriptPropertySetValidatorID"
 *    	detachable="true"
 *    	table="JFireBase_Prop_ScriptPropertySetValidator"
 *
 * @jdo.create-objectid-class
 *
 * @jdo.implements name="org.nightlabs.jfire.prop.validation.IPropertySetValidator"
 *
 * @jdo.fetch-group name "IStruct.fullData" fetch-groups="default" fields="script, scriptLanguage, key2ValidationResults"
 *
 * @author Daniel Mazurek - Daniel.Mazurek [dot] nightlabs [dot] de
 */@PersistenceCapable(
	objectIdClass=ScriptPropertySetValidatorID.class,
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireBase_Prop_ScriptPropertySetValidator")
@FetchGroups(
	@FetchGroup(
		fetchGroups={"default"},
		name="IStruct.fullData",
		members={@Persistent(name="script"), @Persistent(name="scriptLanguage"), @Persistent(name="key2ValidationResults")})
)
public class ScriptPropertySetValidator implements IPropertySetValidator, IScriptValidator<PropertySet, IStruct> {

	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(ScriptPropertySetValidator.class);

	public static final String SCRIPT_ENGINE_NAME = "JavaScript";
	public static final String VAR_PROPETRY_SET = "propertySet";
	public static final String VAR_STRUCT = "struct";

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */	@PrimaryKey
	@Column(length=100)
	private String organisationID;

	/**
	 * @jdo.field primary-key="true"
	 */	@PrimaryKey
	private long validatorID;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)

	private String scriptLanguage;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 * @jdo.column length="10000"
	 */		@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	@Column(length=10000)

    private String script;

    /**
     * key:	unique identifier
	 * value: I18nValidationResult which contains ValidationResultType and localized messages
	 *
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="map"
	 *		key-type="java.lang.String"
	 *		value-type="org.nightlabs.jfire.prop.validation.I18nValidationResult"
	 *		table="JFireBase_Prop_ScriptPropertySetValidator_ValidationResults"
	 *		dependent-value="true"
	 *
	 * @jdo.join
	 */    @Join
    @Persistent(
    	table="JFireBase_Prop_ScriptPropertySetValidator_ValidationResults",
    	persistenceModifier=PersistenceModifier.PERSISTENT)
    @Value(dependent="true")

    private Map<String, I18nValidationResult> key2ValidationResults;

    /**
     * @jdo.field persistence-modifier="none"
     */    @Persistent(persistenceModifier=PersistenceModifier.NONE)

	private transient ScriptEngine scriptEngine;

	/**
	 * @deprecated only for JDO
	 */
	@Deprecated
	protected ScriptPropertySetValidator() {
	}

	/**
	 * Creates a ScriptPropertySetValidator.
	 *
	 * @param scriptLanguage
	 * @param script
	 */
	public ScriptPropertySetValidator(String scriptLanguage, String script) {
		this(IDGenerator.getOrganisationID(), IDGenerator.nextID(ScriptPropertySetValidator.class), scriptLanguage, script);
	}

	/**
	 * Creates a ScriptPropertySetValidator.
	 *
	 * @param organisationID
	 * @param validatorID
	 * @param scriptLanguage
	 * @param script
	 */
	public ScriptPropertySetValidator(String organisationID, long validatorID, String scriptLanguage, String script) {
		super();
		this.organisationID = organisationID;
		this.validatorID = validatorID;
		this.scriptLanguage = scriptLanguage;
		this.script = script;
		this.key2ValidationResults = new HashMap<String, I18nValidationResult>();
	}

	private ScriptEngine getJavaScriptEngine() {
		if (scriptEngine == null) {
	    	long start = System.currentTimeMillis();
		    ScriptEngineManager mgr = new ScriptEngineManager();
		    if (logger.isDebugEnabled()) {
			    long durationInitSEM = System.currentTimeMillis() - start;
		    	logger.debug("Instantiate ScriptEngineManager took "+durationInitSEM+" ms!");
		    }
		    // jsEngine is a newly create engine, the bindings
		    // made in this scope will only be valid for this engine.
		    long start2 = System.currentTimeMillis();
		    ScriptEngine jsEngine = mgr.getEngineByName(SCRIPT_ENGINE_NAME);
		    if (logger.isDebugEnabled()) {
			    long durationInitJSE = System.currentTimeMillis() - start2;
		    	logger.debug("Instantiate JavaScriptEngine took "+durationInitJSE+" ms!");
		    }
		    scriptEngine = jsEngine;
		}
		return scriptEngine;
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.prop.validation.IPropertyElementValidator#validate(java.lang.Object, java.lang.Object)
	 */
	@Override
	public ValidationResult validate(PropertySet dataElement, IStruct structElement)
	{
	    // jsEngine is a newly create engine, the bindings
	    // made in this scope will only be valid for this engine.
	    ScriptEngine jsEngine = getJavaScriptEngine();
		Bindings bindings = jsEngine.getBindings(ScriptContext.ENGINE_SCOPE);
		bindings.put(VAR_PROPETRY_SET, dataElement);
		bindings.put(VAR_STRUCT, structElement);
	    try {
	        // run script
			Object scriptResult = jsEngine.eval(script);
	        // check result type
	        //   -> String: lookup in key2ValidationResults (its the key)
	        //   -> everything else: return null
			if (scriptResult instanceof String) {
				I18nValidationResult validationResult = key2ValidationResults.get(scriptResult);
				if (validationResult != null) {
					String message = validationResult.getI18nValidationResultMessage().getText();
					return new ValidationResult(validationResult.getResultType(), message);
				}
			}
			return null;
		} catch (ScriptException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Returns the scriptLanguage.
	 * @return the scriptLanguage
	 */
	public String getScriptLanguage() {
		return scriptLanguage;
	}

	/**
	 * Sets the scriptLanguage.
	 * @param scriptLanguage the scriptLanguage to set
	 */
	public void setScriptLanguage(String scriptLanguage) {
		this.scriptLanguage = scriptLanguage;
	}

	/**
	 * Returns the script.
	 * @return the script
	 */
	public String getScript() {
		return script;
	}

	/**
	 * Sets the script.
	 * @param script the script to set
	 */
	public void setScript(String script) {
		this.script = script;
	}

	/**
	 * Returns the organisationID.
	 * @return the organisationID
	 */
	public String getOrganisationID() {
		return organisationID;
	}

	/**
	 * Returns the validatorID.
	 * @return the validatorID
	 */
	public long getValidatorID() {
		return validatorID;
	}

	/**
	 * Returns the localized message for the given key.
	 *
	 * @param key the key for the message
	 * @param languageID the languageID of the localized message
	 * @return the localized message for the given key.
	 */
	protected String getMessage(String key, String languageID) {
		I18nValidationResult validationResult = key2ValidationResults.get(key);
		if (validationResult != null) {
			return validationResult.getI18nValidationResultMessage().getText(languageID);
		}
		return null;
	}

	/**
	 * Adds an {@link I18nValidationResult} for the given key.
	 * @param key the key for the I18nValidationResult.
	 * @param validationResult the I18nValidationResult which contains the localized message and type for the given key.
	 */
	public void addValidationResult(String key, I18nValidationResult validationResult) {
		this.key2ValidationResults.put(key, validationResult);
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.prop.validation.IScriptValidator#removeValidationResult(java.lang.String)
	 */
	@Override
	public void removeValidationResult(String key) {
		key2ValidationResults.remove(key);
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.prop.validation.IScriptValidator#getValidationResultKey(java.lang.String)
	 */
	@Override
	public I18nValidationResult getValidationResult(String key) {
		return key2ValidationResults.get(key);
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.prop.validation.IScriptValidator#getValidationResultKeys()
	 */
	@Override
	public Set<String> getValidationResultKeys() {
		return Collections.unmodifiableSet(key2ValidationResults.keySet());
	}
}
