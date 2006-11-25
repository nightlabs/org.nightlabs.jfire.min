package org.nightlabs.jfire.base.prop.structedit;

public class StructFieldMetaData
{
	private IStructFieldEditorFactory editorFactory;
	private IStructFieldFactory fieldFactory;
	private String fieldName;
	private String description;
	
	public StructFieldMetaData(IStructFieldFactory _fieldFactory, IStructFieldEditorFactory _editorFactory, String _fieldName, String _description)
	{
		editorFactory = _editorFactory;
		fieldFactory = _fieldFactory;
		fieldName = _fieldName;
		description = _description;		
	}

	public IStructFieldEditorFactory getEditorFactory()
	{
		return editorFactory;
	}
	
	public IStructFieldFactory getFieldFactory()
	{
		return fieldFactory;
	}
	
	public String getFieldDescription()
	{
		return description;
	}

	public String getFieldName()
	{
		if (fieldName == null || fieldName == "")
			return editorFactory.getStructFieldClass();
		else
			return fieldName;
	}	
	
	public String toString()
	{
		return getFieldName();
	}
}
