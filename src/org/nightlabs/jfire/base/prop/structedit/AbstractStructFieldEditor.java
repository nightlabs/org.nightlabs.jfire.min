package org.nightlabs.jfire.base.prop.structedit;

public abstract class AbstractStructFieldEditor implements IStructFieldEditor {
	public void setChanged() {
		getStructEditor().setChanged(true);
	}
	
	protected abstract StructEditor getStructEditor();
}
