package org.nightlabs.jfire.prop.validation;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.apache.log4j.Logger;
import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.prop.DataField;
import org.nightlabs.jfire.prop.StructField;

import javax.jdo.annotations.Join;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.Value;
import javax.jdo.annotations.FetchGroups;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Column;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceModifier;

/**
 * Implementation of {@link IDataFieldValidator} that is able to validate a {@link DataField}
 * based on an script of a certain script-language (e.g. JavaScript).
 *  
 * @jdo.persistence-capable
 * 		identity-type="application"
 *    	persistence-capable-superclass="org.nightlabs.jfire.prop.validation.DataFieldValidator"
 * 		table="JFireBase_Prop_ScriptDataFieldValidator"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.fetch-group name "IStruct.fullData" fetch-groups="default" fields="script, scriptLanguage, key2ValidationResults"
 * 
 * @author Daniel Mazurek - Daniel.Mazurek [dot] nightlabs [dot] de
 */@PersistenceCapable(
	identityType=IdentityType.APPLICATION,
	table="JFireBase_Prop_ScriptDataFieldValidator")
@FetchGroups(
	@FetchGroup(
		fetchGroups={"default"},
		name="IStruct.fullData",
		members={@Persistent(name="script"), @Persistent(name="scriptLanguage"), @Persistent(name="key2ValidationResults")})
)
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)

public class ScriptDataFieldValidator<DataFieldType extends DataField, StructFieldType extends StructField<DataFieldType>>
extends DataFieldValidator<DataFieldType, StructFieldType>
implements IScriptValidator<DataFieldType, StructFieldType>
{
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(ScriptDataFieldValidator.class);
	
	public static final String SCRIPT_ENGINE_NAME = "JavaScript";
	public static final String VAR_DATA_FIELD = "dataField"; 
	public static final String VAR_STRUCT_FIELD = "structField";

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
	 *		table="JFireBase_Prop_ScriptDataFieldValidator_ValidationResults"
	 *		dependent-value="true"
	 *
	 * @jdo.join
	 */    @Join
    @Persistent(
    	table="JFireBase_Prop_ScriptDataFieldValidator_ValidationResults",
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
	protected ScriptDataFieldValidator() {
	}

//	/**
//	 * 
//	 * @param scriptLanguage
//	 * @param script
//	 */
//	public ScriptDataFieldValidator(String scriptLanguage, String script) {
//		this(IDGenerator.getOrganisationID(), IDGenerator.nextID(ScriptDataFieldValidator.class), scriptLanguage, script);
//	}

	/**
	 * 
	 * @param scriptLanguage
	 * @param script
	 * @param structField
	 */
	public ScriptDataFieldValidator(String scriptLanguage, String script, StructField<?> structField) {
		this(IDGenerator.getOrganisationID(), IDGenerator.nextID(DataFieldValidator.class), scriptLanguage, script, structField);
	}
	
//	/**
//	 * 
//	 * @param organisationID
//	 * @param validatorID
//	 * @param scriptLanguage
//	 * @param script
//	 */
//	public ScriptDataFieldValidator(String organisationID, long validatorID, String scriptLanguage, String script) {
//		super(organisationID, validatorID);
//		this.scriptLanguage = scriptLanguage;
//		this.script = script;
//		this.key2ValidationResults = new HashMap<String, I18nValidationResult>();
//	}
	
	/**
	 * 
	 * @param organisationID
	 * @param validatorID
	 * @param scriptLanguage
	 * @param script
	 * @param structField
	 */
	public ScriptDataFieldValidator(String organisationID, long validatorID, String scriptLanguage, String script, StructField<?> structField) {
		super(organisationID, validatorID, structField);
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
	
	@Override
	public ValidationResult validate(DataFieldType dataElement, StructFieldType structElement) {
	    ScriptEngine jsEngine = getJavaScriptEngine();
		Bindings bindings = jsEngine.getBindings(ScriptContext.ENGINE_SCOPE);
		bindings.put(VAR_DATA_FIELD, dataElement);
		bindings.put(VAR_STRUCT_FIELD, structElement);	    
	    try {
	        // run script
			Object scriptResult = jsEngine.eval(script);
	        // check result type
	        //   -> String: lookup in key2ValidationResults (its the key)
	        //   -> everything else: return null
			if (scriptResult instanceof String) {
				I18nValidationResult validationResult = key2ValidationResults.get((String)scriptResult);
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
		return key2ValidationResults.keySet();
	}
	
}
