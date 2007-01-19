package org.nightlabs.jfire.base.prop.structedit;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.nightlabs.base.language.LanguageChooser;
import org.nightlabs.jfire.prop.StructBlock;
import org.nightlabs.jfire.prop.StructField;

public class StructTree {
	private StructTreeComposite structTreeComposite;
	private StructEditor structEditor;
	
	private Map<StructBlock, StructBlockNode> blockNodeMap;
	private Map<StructField, StructFieldNode> fieldNodeMap;
	
	public StructTree(StructEditor structEditor) {
		this.structEditor = structEditor;
		blockNodeMap = new HashMap<StructBlock, StructBlockNode>();
		fieldNodeMap = new HashMap<StructField, StructFieldNode>();
	}
	
	public Composite createComposite(Composite parent, int style, LanguageChooser languageChooser) {
		structTreeComposite = new StructTreeComposite(parent, true, languageChooser);
		return structTreeComposite;
	}
	
	public StructBlockNode getCurrentBlockNode() {
		return structTreeComposite.getCurrentBlockNode();
	}
	
	public TreeNode getSelectedNode() {
		if (!structTreeComposite.getSelection().isEmpty())
			return (TreeNode) ((IStructuredSelection) structTreeComposite.getSelection()).getFirstElement();
		else
			return null;
	}
	
	public ISelection getSelection() {
		return structTreeComposite.getSelection();
	}
	
	public void setInput(Object input) {
		structTreeComposite.getTreeViewer().setInput(input);
	}
	
	public void addSelectionChangedListener(ISelectionChangedListener listener) {
		structTreeComposite.getTreeViewer().addSelectionChangedListener(listener);
	}
	
	public void removeSelectionChangedListener(ISelectionChangedListener listener) {
		structTreeComposite.getTreeViewer().removeSelectionChangedListener(listener);
	}

	public Control getComposite() {
		return structTreeComposite;
	}

	public void addStructBlock(StructBlock newBlock) {
		StructBlockNode node = structTreeComposite.addStructBlock(newBlock);
		blockNodeMap.put(newBlock, node);
	}

	public void addStructField(StructBlockNode blockNode, StructField newField) {
		StructFieldNode node = structTreeComposite.addStructField(blockNode, newField);
		fieldNodeMap.put(newField, node);
	}
	
	public void select(StructBlock block) {
		structTreeComposite.setSelection(blockNodeMap.get(block));
	}
	
	public void select(StructField field) {
		structTreeComposite.setSelection(fieldNodeMap.get(field));
	}
	
	public void select(TreeNode node) {
		structTreeComposite.setSelection(node);
	}
	
	public void refresh() {
		structTreeComposite.refresh();
	}

	public void removeStructBlock(StructBlockNode blockNode) {
		structTreeComposite.removeStructBlock(blockNode);
	}

	public void removeStructField(StructBlockNode parentBlock, StructFieldNode fieldNode) {
		structTreeComposite.removeStructField(parentBlock, fieldNode);
	}
	
	public TreeViewer getTreeViewer() {
		return structTreeComposite.getTreeViewer();
	}
	
	public StructTreeComposite getStructTreeComposite() {
		return structTreeComposite;
	}
	
	public void refreshElement(StructBlock block) {
		structTreeComposite.getTreeViewer().refresh(blockNodeMap.get(block));
	}
	
	public void refreshElement(StructField field) {
		structTreeComposite.getTreeViewer().refresh(fieldNodeMap.get(field));
	}

	public void refreshSelected() {
		structTreeComposite.getTreeViewer().refresh(getSelectedNode());
	}
}