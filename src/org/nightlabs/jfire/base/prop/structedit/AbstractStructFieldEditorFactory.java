package org.nightlabs.jfire.base.prop.structedit;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;

/**
 * 
 * @author Tobias Langner <tobias[DOT]langner[AT]nightlabs[DOT]de>
 */
public abstract class AbstractStructFieldEditorFactory implements IStructFieldEditorFactory
{
	protected String editorClass = null;
	protected String structFieldClass = null;
	
	/**
	 * Key: String structFieldClass
	 * Value: Instance of {@link IStructFieldEditor}
	 */
	protected static Map<String, IStructFieldEditor> editors;
	
	public AbstractStructFieldEditorFactory()
	{
		editors = new HashMap<String, IStructFieldEditor>();
	}
	
	
	public IStructFieldEditor getStructFieldEditorSingleton(String structFieldClass)
	{
		IStructFieldEditor editor;		
		editor = editors.get(structFieldClass);
		
		if (editor == null)
		{
			try
			{
				editor = (IStructFieldEditor) Class.forName(getStructFieldEditorClass()).newInstance();
			}
			catch (Throwable t)
			{
				IllegalStateException ill = new IllegalStateException("Error instantiating "+getStructFieldEditorClass());
				ill.initCause(t);
				throw ill;
			}
			
			editors.put(structFieldClass, editor);
		}
		
		return editor;
	}
	
	public String getStructFieldClass()
	{
		return structFieldClass;
	}
	
	public void setStructFieldClass(String theClass)
	{
		structFieldClass = theClass;
	}		

	public void setStructFieldEditorClass(String theClass)
	{
		editorClass = theClass;
	}
	
	public void setInitializationData(IConfigurationElement config, String propertyName, Object data) throws CoreException
	{
	}
}