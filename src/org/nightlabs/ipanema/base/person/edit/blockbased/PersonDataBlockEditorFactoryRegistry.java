/*
 * Created 	on Jan 20, 2005
 * 					by alex
 *
 */
package org.nightlabs.ipanema.base.person.edit.blockbased;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.swt.widgets.Composite;

import org.nightlabs.base.extensionpoint.AbstractEPProcessor;
import org.nightlabs.base.extensionpoint.EPProcessorException;
import org.nightlabs.ipanema.person.PersonDataBlock;
import org.nightlabs.ipanema.person.id.PersonStructBlockID;

/**
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 *
 */
public class PersonDataBlockEditorFactoryRegistry extends AbstractEPProcessor {
	
	public static final String EXTENSION_POINT_ID = "org.nightlabs.ipanema.base.person_edit_specializedDataBlockEditor";
	public static final String EXTENSION_POINT_ELEMENT_NAME = "specializeddatablockeditor"; // lower case for error tolerance
	public static final String EXTENSION_POINT_CLASS_ATTRIBUTE_NAME = "class"; // lower case for error tolerance
	
	/**
	 * key: PersonStructBlockID: providerID<br/>
	 * value: PersonDataBlockEditorFactory provider
	 */
	private Map providerRegistry = new HashMap();

	public void addPersonDataBlockEditorProvider(PersonDataBlockEditorFactory provider) {
		PersonStructBlockID blockID = provider.getProviderStructBlockID();
		providerRegistry.put(blockID,provider);
	}
	
	public PersonDataBlockEditor getPersonDataBlockEditor(
		PersonDataBlock dataBlock,
		Composite parent, 
		int style,
		int columnHint
	) 
	throws EPProcessorException 
	{
		if (!isProcessed())
			process();
		PersonStructBlockID blockID = PersonStructBlockID.create(dataBlock.getPersonStructBlockOrganisationID(),dataBlock.getPersonStructBlockID());
		PersonDataBlockEditorFactory provider = (PersonDataBlockEditorFactory)providerRegistry.get(blockID);
		if (provider != null)
			return provider.createPersonDataBlockEditor(dataBlock,parent,style);
		else
			return new GenericPersonDataBlockEditor(dataBlock,parent,style,columnHint);
	}
	
	private static PersonDataBlockEditorFactoryRegistry sharedInstance;
	
	public static PersonDataBlockEditorFactoryRegistry getSharedInstace() {
		if (sharedInstance == null)
			sharedInstance = new PersonDataBlockEditorFactoryRegistry();
		return sharedInstance;
	}

	/**
	 * @see org.nightlabs.base.extensionpoint.AbstractEPProcessor#getExtensionPointID()
	 */
	public String getExtensionPointID() {
		return EXTENSION_POINT_ID;
	}

	/**
	 * @see org.nightlabs.base.extensionpoint.AbstractEPProcessor#processElement(IExtension, org.eclipse.core.runtime.IConfigurationElement)
	 */
	public void processElement(IExtension extension, IConfigurationElement element) throws EPProcessorException {
		try{
			if (element.getName().toLowerCase().equals(EXTENSION_POINT_ELEMENT_NAME)){
				PersonDataBlockEditorFactory provider = (PersonDataBlockEditorFactory) element.createExecutableExtension(EXTENSION_POINT_CLASS_ATTRIBUTE_NAME);
				addPersonDataBlockEditorProvider(provider);
			}
			else {
				throw new IllegalArgumentException("Element "+element.getName()+" is not supported by extension-point "+EXTENSION_POINT_ID);
			}
		}catch(Throwable e){
			throw new EPProcessorException(e);
		}
	}

}
