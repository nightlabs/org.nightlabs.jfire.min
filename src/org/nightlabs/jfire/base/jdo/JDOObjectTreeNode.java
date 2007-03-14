package org.nightlabs.jfire.base.jdo;

import java.util.List;

public class JDOObjectTreeNode<JDOObjectID, JDOObject, Controller extends ActiveJDOObjectTreeController>
{
	private Controller activeJDOObjectTreeController;
	private JDOObjectTreeNode parent;
	private JDOObject jdoObject;

	public void setActiveJDOObjectTreeController(
			Controller activeJDOObjectTreeController)
	{
		this.activeJDOObjectTreeController = activeJDOObjectTreeController;
	}
	public void setParent(JDOObjectTreeNode parent)
	{
		this.parent = parent;
	}
	public void setJdoObject(JDOObject jdoObject)
	{
		this.jdoObject = jdoObject;
	}
	public Controller getActiveJDOObjectTreeController()
	{
		return activeJDOObjectTreeController;
	}
	public JDOObjectTreeNode getParent()
	{
		return parent;
	}
	public JDOObject getJdoObject()
	{
		return jdoObject;
	}

	public List<JDOObjectTreeNode> childNodes = null;

	public List<JDOObjectTreeNode> getChildNodes()
	{
		return childNodes;
	}

	public void setChildNodes(List<JDOObjectTreeNode> childNodes)
	{
		this.childNodes = childNodes;
	}
}
