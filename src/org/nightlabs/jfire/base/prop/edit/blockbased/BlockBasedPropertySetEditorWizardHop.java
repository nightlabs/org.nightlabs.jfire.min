/**
 * 
 */
package org.nightlabs.jfire.base.prop.edit.blockbased;

import org.eclipse.jface.resource.ImageDescriptor;
import org.nightlabs.base.wizard.IWizardHopPage;
import org.nightlabs.base.wizard.WizardHop;
import org.nightlabs.jfire.prop.PropertySet;
import org.nightlabs.jfire.prop.id.StructBlockID;

/**
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 *
 */
public class BlockBasedPropertySetEditorWizardHop extends WizardHop {

//	private List<CompoundDataBlockWizardPage> wizardPages = new ArrayList<CompoundDataBlockWizardPage>();
	
	private PropertySet propertySet;
	
	/**
	 * 
	 */
	public BlockBasedPropertySetEditorWizardHop(PropertySet propertySet) {
		this.propertySet = propertySet;
	}
	
	public CompoundDataBlockWizardPage addWizardPage(StructBlockID[] structBlockIDs, String name, String title, String message, ImageDescriptor image) {
		CompoundDataBlockWizardPage page = new CompoundDataBlockWizardPage(
				name, title, propertySet.getStructure(), propertySet, structBlockIDs
		);
		if (image != null)
			page.setImageDescriptor(image);
		if (message != null)
			page.setMessage(message);
		if (getEntryPage() == null)
			setEntryPage(page);
		else
			addHopPage(page);
		return page;
	}
	
	public CompoundDataBlockWizardPage addWizardPage(StructBlockID[] structBlockIDs, String name, String title, String message) {
		return addWizardPage(structBlockIDs, name, title, message, null);
	}
	
	public CompoundDataBlockWizardPage addWizardPage(StructBlockID[] structBlockIDs, String name, String title) {
		return addWizardPage(structBlockIDs, name, title, null, null);
	}

	public CompoundDataBlockWizardPage addWizardPage(StructBlockID[] structBlockIDs, String name) {
		return addWizardPage(structBlockIDs, name, null, null, null);
	}
	
	public CompoundDataBlockWizardPage addWizardPage(StructBlockID[] structBlockIDs) {
		return addWizardPage(structBlockIDs, getGenericPageName(), null, null, null);
	}
	
	public void updatePropertySet() {
		if (getEntryPage() != null) {
			((CompoundDataBlockWizardPage) getEntryPage()).updatePropertySet();
		}
		for (IWizardHopPage page : getHopPages()) {
			((CompoundDataBlockWizardPage) page).updatePropertySet();
		}
	}
	
	private String getGenericPageName() {
		if (getEntryPage() == null)
			return BlockBasedPropertySetEditorWizardHop.class.getName() + "#0";
		return BlockBasedPropertySetEditorWizardHop.class.getName() + "#" + String.valueOf(getHopPages().size());
	}
}
