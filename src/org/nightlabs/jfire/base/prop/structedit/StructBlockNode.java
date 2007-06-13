package org.nightlabs.jfire.base.prop.structedit;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import org.nightlabs.i18n.I18nText;
import org.nightlabs.jfire.prop.StructBlock;

/**
 * @author Tobias Langner <tobias[DOT]langner[AT]nightlabs[DOT]de>
 */
public class StructBlockNode extends TreeNode implements Comparable<StructBlockNode>
{
	private StructBlock block;
	private List<StructFieldNode> fields;
	
	public StructBlockNode(StructBlock block)
	{
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
		Collections.sort(fields);
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

	/**
	 * Compares to StructBlockNodes with respect to their label
	 */
	public int compareTo(StructBlockNode o)
	{
		return getLabel().compareTo(o.getLabel());
	}

	@Override
	public boolean isEditable()
	{
//		Login.getLogin().
		return false;
	}
	
//	@Override
//	public String toString() {
//		return block.toString();
//	}
}
