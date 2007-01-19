package org.nightlabs.jfire.base.prop.structedit;

import java.util.Locale;

import org.nightlabs.i18n.I18nText;
import org.nightlabs.jfire.prop.AbstractStructField;
import org.nightlabs.jfire.prop.StructField;

public class StructFieldNode extends TreeNode implements Comparable<StructFieldNode>
{
	private StructField field;
	private StructBlockNode parentBlock;
	
	/**
	 * Creates a new StructFieldNode.
	 * @param field The {@link StructField} to be represented. Can be null to indicate an
	 * 				{@link StructField} whose type has not been specified yet.
	 * @param parentBlock The parentBlock node.
	 * @param deletable Wether the node can be deleted or not.
	 */
	public StructFieldNode(StructField field, StructBlockNode parent)
	{
		this.field = field;
		this.parentBlock = parent;		
	}
	
	@Override
	public I18nText getI18nText()
	{
		return field.getName();
	}
	
	@Override
	public String getLabel()
	{
		//return field.getStructFieldKey();
		return field.getName().getText(Locale.getDefault().getLanguage());
	}

	@Override
	public TreeNode[] getChildren()
	{
		return null;
	}

	@Override
	public boolean hasChildren()
	{
		return false;
	}
	
	public StructField getField()
	{
		return field;
	}
	
	public StructBlockNode getParentBlock()
	{
		return parentBlock;
	}

	/**
	 * Compares to StructBlockNodes with respect to their label
	 */
	public int compareTo(StructFieldNode o)
	{
		return getLabel().compareTo(o.getLabel());
	}

	@Override
	public boolean isEditable()
	{
		// TODO Auto-generated method stub
		return false;
	}
}
