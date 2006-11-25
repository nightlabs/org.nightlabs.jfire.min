package org.nightlabs.jfire.base.prop.structedit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.nightlabs.base.language.LanguageChangeEvent;
import org.nightlabs.base.language.LanguageChangeListener;
import org.nightlabs.base.language.LanguageChooser;
import org.nightlabs.base.table.TableLabelProvider;
import org.nightlabs.base.tree.AbstractTreeComposite;
import org.nightlabs.base.tree.TreeContentProvider;
import org.nightlabs.jfire.prop.AbstractStructField;
import org.nightlabs.jfire.prop.IStruct;
import org.nightlabs.jfire.prop.StructBlock;
import org.nightlabs.jfire.prop.id.StructID;

public class StructTree extends AbstractTreeComposite implements LanguageChangeListener {
	private StructID id;
	private List<StructBlockNode> blockNodes;
	private String currLanguageId;

	public StructTree(Composite parent, boolean init, LanguageChooser langChooser) {
		super(parent, SWT.BORDER | SWT.H_SCROLL, true, init, false);
		langChooser.addLanguageChangeListener(this);
		this.currLanguageId = langChooser.getLanguage().getLanguageID();
	}

	@Override
	public void createTreeColumns(Tree tree) {
	}

	@Override
	public void setTreeProvider(TreeViewer treeViewer) {
		treeViewer.setLabelProvider(new LabelProvider());
		treeViewer.setContentProvider(new ContentProvider());
	}

	// public void setInput(String organisationID, Class linkClass)
	// {
	// id = StructID.create(organisationID, linkClass.getName());
	// getTreeViewer().setInput(id);
	// }

	public StructBlockNode getCurrentBlockNode() {
		TreeNode current = (TreeNode) ((IStructuredSelection) getSelection()).getFirstElement();
		if (current instanceof StructBlockNode)
			return (StructBlockNode) current;
		else if (current instanceof StructFieldNode)
			return ((StructFieldNode) current).getParentBlock();
		return null;
	}

	public StructBlockNode addStructBlock(StructBlock psb) {
		StructBlockNode blockNode = new StructBlockNode(psb);
		blockNodes.add(blockNode);
		refresh();
		setSelection(blockNode);
		return blockNode;
	}

	public StructFieldNode addStructField(StructBlockNode parentNode, AbstractStructField field) {
		StructFieldNode fieldNode = new StructFieldNode(field, parentNode);
		parentNode.addField(fieldNode);
		refresh();
		setSelection(fieldNode);
		return fieldNode;
	}

	public void removeStructBlock(StructBlockNode blockNode) {
		blockNodes.remove(blockNode);
		refresh();
	}

	public void removeStructField(StructBlockNode parentBlock, StructFieldNode fieldNode) {
		parentBlock.removeField(fieldNode);
		refresh();
	}

	private class LabelProvider extends TableLabelProvider {
		@Override
		public String getText(Object element) {
			return getColumnText(element, 0);
		}

		public String getColumnText(Object element, int columnIndex) {
			String label = "";
			if (element instanceof TreeNode)
				label = ((TreeNode) element).getI18nText().getText(currLanguageId);
			if (element instanceof String)
				label = (String) element;

			return label;
		}
	}

	private class ContentProvider extends TreeContentProvider {
		private String text;
		
		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			super.inputChanged(viewer, oldInput, newInput);

			if (newInput instanceof IStruct) {
				text = null;
				IStruct struct = (IStruct) newInput;
				blockNodes = new ArrayList<StructBlockNode>(struct.getStructBlocks().size());

				for (StructBlock psb : struct.getStructBlocks()) {
					StructBlockNode newBlockNode = new StructBlockNode(psb);
					blockNodes.add(newBlockNode);
					for (AbstractStructField field : psb.getStructFields())
						newBlockNode.addField(new StructFieldNode(field, newBlockNode));
				}
			} else if (newInput instanceof String) {
				blockNodes = null;
				text = (String) newInput;
			}
		}

		public Object[] getElements(Object inputElement) {
			if (blockNodes != null) {
				Collections.sort(blockNodes);
				return blockNodes.toArray(new Object[0]);
			}
			else if (text != null) {
				return new Object[] { text };
			}
			
			return new Object[0];
		}

		@Override
		public boolean hasChildren(Object element) {
			if (element instanceof TreeNode)
				return ((TreeNode) element).hasChildren();
			else
				return false;
		}

		@Override
		public Object[] getChildren(Object parentElement) {
			if (parentElement instanceof TreeNode)
				return ((TreeNode) parentElement).getChildren();
			else
				return null;
		}
	}

	public void languageChanged(LanguageChangeEvent event) {
		currLanguageId = event.getNewLanguage().getLanguageID();
		refresh(true);
	}
}