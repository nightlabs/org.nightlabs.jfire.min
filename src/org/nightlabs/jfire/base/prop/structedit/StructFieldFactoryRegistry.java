package org.nightlabs.jfire.base.prop.structedit;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.nightlabs.base.ui.extensionpoint.AbstractEPProcessor;
import org.nightlabs.base.ui.extensionpoint.EPProcessorException;
import org.nightlabs.jfire.prop.StructField;
import org.nightlabs.jfire.prop.exception.PropertyException;

public class StructFieldFactoryRegistry extends AbstractEPProcessor {
	/**
	 * Key: Field class name
	 * Value: Field meta data
	 */
	private static Map<String, StructFieldMetaData> fieldMetaDataMap;

	/**
	 * Key: Field type
	 * Value: Field class name
	 */
	private static Map<String, String> fieldClassMap;
	private static final String EXTENSION_POINT_ID = "org.nightlabs.jfire.base.propStructField"; //$NON-NLS-1$
	private static final String EXTENSION_POINT_ELEMENT_NAME = "propstructfield"; // lower case for error tolerance //$NON-NLS-1$

	private static final Logger logger = Logger.getLogger(StructFieldFactoryRegistry.class);

	public StructFieldFactoryRegistry() {
		fieldMetaDataMap = new HashMap<String, StructFieldMetaData>();
		fieldClassMap = new HashMap<String, String>();
		;
	}

	public synchronized void addFieldMetadata(String fieldClass, StructFieldFactory fieldFactory,
			StructFieldEditorFactory editorFactory, String fieldName, String description) {
		fieldMetaDataMap.put(fieldClass, new StructFieldMetaData(fieldFactory, editorFactory, fieldName, description));
		fieldClassMap.put(fieldName, fieldClass);
	}

	public synchronized void removeEditorFactory(String fieldClass) {
		fieldClassMap.remove(fieldMetaDataMap.get(fieldClass).getFieldName());
		fieldMetaDataMap.remove(fieldClass);
	}

	private StructFieldEditorFactory getEditorFactory(Class fieldClass) throws PropertyException {
		// make sure the EP was already processed
		checkProcessing();

		StructFieldEditorFactory editorFactory = null;
		Class current = fieldClass;
		String currentName;
		StructFieldMetaData sfmd;

		// also check parents of the class
		do {
			currentName = current.getName();
			sfmd = fieldMetaDataMap.get(currentName);
			if (sfmd == null)
				break;
			editorFactory = sfmd.getEditorFactory();
			current = current.getSuperclass();
		} while (editorFactory == null && current != null);

		if (editorFactory != null)
			return editorFactory;
		else {
			logger.warn("No editor found for class " + fieldClass.getName() + ". Using DefaultStructFieldEditor instead."); //$NON-NLS-1$ //$NON-NLS-2$
			return new DefaultStructFieldEditor.DefaultStructFieldEditorFactory();
			//throw new StructFieldEditorFactoryNotFoundException("No editor found for class "+fieldClass.getName());
		}
	}

	public StructFieldEditor getEditorSingleton(StructField field) throws PropertyException {
		StructFieldEditorFactory editorFactory = getEditorFactory(field.getClass());
		return editorFactory.getStructFieldEditorSingleton(field.getClass().getName());
	}

	@Override
	public void processElement(IExtension extension, IConfigurationElement element) throws Exception {
		try {
			if (element.getName().toLowerCase().equals(EXTENSION_POINT_ELEMENT_NAME)) {
				StructFieldEditorFactory editorFactory = (StructFieldEditorFactory) element
						.createExecutableExtension("editorFactoryClass"); //$NON-NLS-1$
				StructFieldFactory fieldFactory = (StructFieldFactory) element.createExecutableExtension("factoryClass"); //$NON-NLS-1$
				String structFieldClass = element.getAttribute("class"); //$NON-NLS-1$
				String fieldName = element.getAttribute("name"); //$NON-NLS-1$
				String description = element.getAttribute("description"); //$NON-NLS-1$
				description = description == null ? "" : description; //$NON-NLS-1$
				editorFactory.setStructFieldClass(structFieldClass);

				sharedInstance().addFieldMetadata(structFieldClass, fieldFactory, editorFactory, fieldName, description);
			} else {
				throw new IllegalArgumentException("Element " + element.getName() + " is not supported by extension-point " //$NON-NLS-1$ //$NON-NLS-2$
						+ EXTENSION_POINT_ID);
			}
		} catch (Throwable e) {
			throw new EPProcessorException(e);
		}
	}

	private static StructFieldFactoryRegistry sharedInstance;

	/**
	 * Returns the static shared instance of a DataFieldEditorFactoryRegistry. 
	 * @return The static shared instance of a DataFieldEditorFactoryRegistry.
	 */
	public static StructFieldFactoryRegistry sharedInstance() {
		if (sharedInstance == null)
			sharedInstance = new StructFieldFactoryRegistry();
		return sharedInstance;
	}

	/**
	 * @see org.nightlabs.base.ui.extensionpoint.AbstractEPProcessor#getExtensionPointID()
	 */
	@Override
	public String getExtensionPointID() {
		return EXTENSION_POINT_ID;
	}

	public Map<String, StructFieldMetaData> getFieldMetaDataMap() {
		checkProcessing();
		return fieldMetaDataMap;
	}
}