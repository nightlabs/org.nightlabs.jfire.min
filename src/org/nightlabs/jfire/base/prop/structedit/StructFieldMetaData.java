package org.nightlabs.jfire.base.prop.structedit;

public class StructFieldMetaData
{
	private StructFieldEditorFactory editorFactory;
	private StructFieldFactory fieldFactory;
	private String fieldName;
	private String description;
	
	public StructFieldMetaData(StructFieldFactory _fieldFactory, StructFieldEditorFactory _editorFactory, String _fieldName, String _description)
	{
		editorFactory = _editorFactory;
		fieldFactory = _fieldFactory;
		fieldName = _fieldName;
		description = _description;		
	}

	public StructFieldEditorFactory getEditorFactory()
	{
		return editorFactory;
	}
	
	public StructFieldFactory getFieldFactory()
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
