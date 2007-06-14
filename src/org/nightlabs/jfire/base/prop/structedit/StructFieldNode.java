package org.nightlabs.jfire.base.prop.structedit;

import java.util.Locale;

import org.nightlabs.i18n.I18nText;
import org.nightlabs.jfire.prop.StructField;
import org.nightlabs.util.Utils;

public class StructFieldNode extends TreeNode //implements Comparable<StructFieldNode>
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
		if (field == null)
			throw new IllegalArgumentException("field must not be null!");
		
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

	@Override
	public boolean isEditable()
	{
		return false;
	}
	
	@Override
	public String toString() {
		return field.toString();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == this) return true;
		if (!(obj instanceof StructFieldNode)) return false;
		
		return field.equals(((StructFieldNode)obj).field);
	}
	
	@Override
	public int hashCode() {
		return Utils.hashCode(field);
	}
}
