package org.nightlabs.jfire.base.prop.structedit;

import org.nightlabs.i18n.I18nText;

public abstract class TreeNode
{
	public abstract String getLabel();
	public abstract I18nText getI18nText();
	public abstract boolean hasChildren();
	public abstract TreeNode[] getChildren();
	public abstract boolean isEditable();
}