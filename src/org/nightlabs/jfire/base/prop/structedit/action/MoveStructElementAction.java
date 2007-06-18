/**
 * 
 */
package org.nightlabs.jfire.base.prop.structedit.action;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.nightlabs.base.action.SelectionListenerAction;
import org.nightlabs.base.resource.SharedImages;
import org.nightlabs.jfire.base.prop.structedit.StructBlockNode;
import org.nightlabs.jfire.base.prop.structedit.StructEditor;
import org.nightlabs.jfire.base.prop.structedit.StructFieldNode;
import org.nightlabs.jfire.base.prop.structedit.StructureChangedListener;
import org.nightlabs.jfire.base.prop.structedit.TreeNode;
import org.nightlabs.jfire.prop.StructBlock;
import org.nightlabs.jfire.prop.StructField;

/**
 * 
 * @author Marius Heinzmann [marius<at>NightLabs<dot>de]
 */
public class MoveStructElementAction 
	extends SelectionListenerAction 
{
	private StructEditor editor;
	private boolean up;

	public MoveStructElementAction(StructEditor editor, boolean up) {
		this(editor, up, null); 
	}
	
	public MoveStructElementAction(StructEditor editor, boolean up, StructureChangedListener listener) {
		super(editor.getStructTree().getStructTreeComposite());
		addStructureChangedListener(listener);
		this.editor = editor;
		this.up = up;
		
		setText(up ? "Move element up" : "Move element down"); 
		setImageDescriptor( up ? SharedImages.UP_16x16 : SharedImages.DOWN_16x16 );
		setToolTipText( up ? "Moves the selected Struct element one position up." : 
												 "Moves the selected Struct element one position down.");
	}

	@Override
	public void run() {
		TreeNode selectedNode = editor.getStructTree().getSelectedNode();
		if (selectedNode == null)
			return;
		
		if (selectedNode instanceof StructBlockNode) {
			final StructBlock movingBlock = ((StructBlockNode) selectedNode).getBlock();
			final List<StructBlock> blockList = editor.getStruct().getStructBlocks();
			final int movingBlockIndex = blockList.indexOf(movingBlock);
			if (up ? movingBlockIndex == 0 : movingBlockIndex == blockList.size()-1)
				return;
			
			final int exchangeBlockIndex= up ? movingBlockIndex-1 : movingBlockIndex+1; 
			final StructBlock exchangedBlock = blockList.get( exchangeBlockIndex );
			blockList.set( exchangeBlockIndex, movingBlock );
			blockList.set( movingBlockIndex, exchangedBlock );
			
		} else if (selectedNode instanceof StructFieldNode) {
			final StructField movingField = ((StructFieldNode) selectedNode).getField();
			final StructBlock outerBlock = ((StructFieldNode) selectedNode).getParentBlock().getBlock();
			final List<StructField> fieldList = outerBlock.getStructFields();
			final int movingFieldIndex = fieldList.indexOf(movingField);
			if ( up ? movingFieldIndex == 0 : movingFieldIndex == fieldList.size()-1)
				return;
			final int exchangeFieldIndex = up ? movingFieldIndex-1 : movingFieldIndex+1; 

			final StructField exchangedField = fieldList.get( exchangeFieldIndex );
			fieldList.set( exchangeFieldIndex, movingField );
			fieldList.set( movingFieldIndex, exchangedField );
			
		} else {
			throw new IllegalArgumentException("The returned selected TreeNode is neither a StructBlockNode " +
					"nor a StructFieldNode! Seems like the NodeTypes have changed!");
		}

		editor.getStructTree().refresh();
		notifyListeners();
	}
	
	/* (non-Javadoc)
	 * @see org.nightlabs.base.action.IUpdateActionOrContributionItem#calculateEnabled()
	 */
	public boolean calculateEnabled() {
		return getSelection() != null ? true : false;
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.base.action.IUpdateActionOrContributionItem#calculateVisible()
	 */
	public boolean calculateVisible() {
		return true;
	}

	private Set<StructureChangedListener> listeners = new HashSet<StructureChangedListener>();
	
	public void addStructureChangedListener(StructureChangedListener listener) {
		if (! listeners.contains(listener))
			listeners.add(listener);
	}
	
	private void notifyListeners() {
		for (StructureChangedListener listener : listeners) {
			listener.structureChanged();
		}
	}

}
