package org.nightlabs.jfire.test.cascadedauthentication;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;
import org.nightlabs.util.Util;

public class TestRequestResultTreeNode
implements Serializable
{
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(TestRequestResultTreeNode.class);

	public TestRequestResultTreeNode(TestRequestResultTreeNode parent, String request_organisationID)
	{
		this.parent = parent;
		this.request_organisationID = request_organisationID;
	}

	private TestRequestResultTreeNode parent;
	private String request_organisationID;
	private String result_organisationID_beforeRecursion;
	private String result_organisationID_afterRecursion;
	private List<TestRequestResultTreeNode> children = new ArrayList<TestRequestResultTreeNode>();

	public TestRequestResultTreeNode getParent()
	{
		return parent;
	}
	protected void setParent(TestRequestResultTreeNode parent)
	{
		this.parent = parent;
	}

	public String getRequest_organisationID()
	{
		return request_organisationID;
	}

	public String getResult_organisationID_beforeRecursion()
	{
		return result_organisationID_beforeRecursion;
	}
	public void setResult_organisationID_beforeRecursion(String result_organisationID_beforeRecursion)
	{
		this.result_organisationID_beforeRecursion = result_organisationID_beforeRecursion;

		if (!Util.equals(this.request_organisationID, this.result_organisationID_beforeRecursion))
			logger.warn("request_organisationID != result_organisationID_beforeRecursion : " + this.request_organisationID + " != " + this.result_organisationID_beforeRecursion);
	}

	public String getResult_organisationID_afterRecursion()
	{
		return result_organisationID_afterRecursion;
	}
	public void setResult_organisationID_afterRecursion(String result_organisationID_afterRecursion)
	{
		this.result_organisationID_afterRecursion = result_organisationID_afterRecursion;

		if (!Util.equals(this.request_organisationID, this.result_organisationID_afterRecursion))
			logger.warn("request_organisationID != result_organisationID_afterRecursion : " + this.request_organisationID + " != " + this.result_organisationID_afterRecursion);
	}

	public List<TestRequestResultTreeNode> getChildren()
	{
		return Collections.unmodifiableList(new ArrayList<TestRequestResultTreeNode>(children));
	}

	public void replaceChild(TestRequestResultTreeNode oldChildNode, TestRequestResultTreeNode newChildNode)
	{
		int index = children.indexOf(oldChildNode);
		if (index < 0)
			throw new IllegalArgumentException("oldChildNode is not in children List: " + oldChildNode);

		newChildNode.setParent(this);
		children.remove(index);
		children.add(index, newChildNode);
	}

	public TestRequestResultTreeNode createChildTestRequestResultTree(String request_organisationID)
	{
		TestRequestResultTreeNode node = new TestRequestResultTreeNode(this, request_organisationID);
		children.add(node);
		return node;
	}
}
