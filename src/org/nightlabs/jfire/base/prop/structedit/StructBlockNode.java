package org.nightlabs.jfire.base.prop.structedit;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import org.nightlabs.i18n.I18nText;
import org.nightlabs.jfire.prop.StructBlock;
import org.nightlabs.util.Utils;

/**
 * @author Tobias Langner <!-- tobias[dot]langner[at]nightlabs[dot]de -->
 */
public class StructBlockNode extends TreeNode //implements Comparable<StructBlockNode>
{
	private StructBlock block;
	private List<StructFieldNode> fields;
	
	public StructBlockNode(StructBlock block)
	{
		if (block == null)
			throw new IllegalArgumentException("block must not be null!");

		this.block = block;
		fields = new LinkedList<StructFieldNode>();		
	}	
	
	public void addField(StructFieldNode field)
	{
		fields.add(field);
	}
	
	public void removeField(StructFieldNode field)
	{
		fields.remove(field);
	}
	
	@Override
	public I18nText getI18nText()
	{
		return block.getName();
	}
	
	@Override
	public String getLabel()
	{
		return block.getName().getText(Locale.getDefault().getLanguage());
	}

	@Override
	public TreeNode[] getChildren()
	{
		return fields.toArray(new TreeNode[0]);
	}

	@Override
	public boolean hasChildren()
	{
		return !block.getStructFields().isEmpty();
	}
	
	public StructBlock getBlock()
	{
		return block;
	}

	@Override
	public boolean isEditable()
	{
//		Login.getLogin().
		return false;
	}
	
	@Override
	public String toString() {
		return block.toString();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) return true;
		if (!(obj instanceof StructBlockNode)) return false;
		StructBlockNode o = (StructBlockNode) obj;
		return Utils.equals(o.block, this.block);
	}
	@Override
	public int hashCode() {
		return Utils.hashCode(this.block);
	}
}
