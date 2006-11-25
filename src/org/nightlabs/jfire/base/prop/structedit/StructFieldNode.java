package org.nightlabs.jfire.base.prop.structedit;

import java.util.Locale;

import org.nightlabs.i18n.I18nText;
import org.nightlabs.jfire.prop.AbstractStructField;

public class StructFieldNode extends TreeNode implements Comparable<StructFieldNode>
{
	private AbstractStructField field;
	private StructBlockNode parentBlock;
	
	/**
	 * Creates a new StructFieldNode.
	 * @param field The {@link AbstractStructField} to be represented. Can be null to indicate an
	 * 				{@link AbstractStructField} whose type has not been specified yet.
	 * @param parentBlock The parentBlock node.
	 * @param deletable Wether the node can be deleted or not.
	 */
	public StructFieldNode(AbstractStructField field, StructBlockNode parent)
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
	
	public AbstractStructField getField()
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
