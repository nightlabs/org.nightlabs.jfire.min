/**
 * 
 */
package org.nightlabs.jfire.base.prop.edit.blockbased;

import org.eclipse.jface.resource.ImageDescriptor;
import org.nightlabs.base.wizard.IWizardHopPage;
import org.nightlabs.base.wizard.WizardHop;
import org.nightlabs.base.wizard.WizardHopPage;
import org.nightlabs.jfire.prop.PropertySet;
import org.nightlabs.jfire.prop.StructBlock;
import org.nightlabs.jfire.prop.id.StructBlockID;

/**
 * A wizard hop that can create {@link CompoundDataBlockWizardPage}s as its
 * sub-pages. It is indendet to be placed in a Wizard taht should edit a 
 * {@link PropertySet}. 
 * 
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 */
public class BlockBasedPropertySetEditorWizardHop extends WizardHop {

//	private List<CompoundDataBlockWizardPage> wizardPages = new ArrayList<CompoundDataBlockWizardPage>();
		
	private String editorScope;
	
	private PropertySet propertySet;
	
	private EditorStructBlockRegistry editorStructBlockRegistry;

	/**
	 * Create a new {@link BlockBasedPropertySetEditorWizardHop} for the given {@link PropertySet}.
	 * 
	 * @param propertySet The property set that should be edited by this WizardHop.
	 */
	public BlockBasedPropertySetEditorWizardHop(PropertySet propertySet) {
		this.propertySet = propertySet;
		editorStructBlockRegistry = new EditorStructBlockRegistry(propertySet.getStructLocalLinkClass(), propertySet.getStructLocalScope());
	}
	
	/**
	 * Add a new {@link CompoundDataBlockWizardPage} to this hop. The new page will display
	 * DataBlockEditors for the given structBlockIDs. 
	 * <p>
	 * If the given structBlockIDs are either <code>null</code> or empty a {@link FullDataBlockCoverageWizardPage}
	 * will be added that displays all remaining {@link StructBlock}s not added previously.
	 * </p> 
	 * 
	 * @param structBlockIDs The id of the {@link StructBlock}s that should be edited by the new page.
	 * @param name The name of the new page.
	 * @param title The title of the new page.
	 * @param message The message of the new page.
	 * @param image The image descriptor of the new page.
	 * @return The newly created page.
	 */
	public IWizardHopPage addWizardPage(StructBlockID[] structBlockIDs, String name, String title, String message, ImageDescriptor image) {
		WizardHopPage page = null;
		if (structBlockIDs == null || structBlockIDs.length == 0) {
			page = new FullDataBlockCoverageWizardPage(name, title, propertySet, editorStructBlockRegistry);
		} else {
			page = new CompoundDataBlockWizardPage(
				name, title, propertySet, structBlockIDs
			);
			editorStructBlockRegistry.addEditorStructBlocks(getEditorScope(), structBlockIDs);
		}
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
	
	public IWizardHopPage addWizardPage(StructBlockID[] structBlockIDs, String name, String title, String message) {
		return addWizardPage(structBlockIDs, name, title, message, null);
	}
	
	public IWizardHopPage addWizardPage(StructBlockID[] structBlockIDs, String name, String title) {
		return addWizardPage(structBlockIDs, name, title, null, null);
	}

	public IWizardHopPage addWizardPage(StructBlockID[] structBlockIDs, String name) {
		return addWizardPage(structBlockIDs, name, null, null, null);
	}
	
	public IWizardHopPage addWizardPage(StructBlockID[] structBlockIDs) {
		return addWizardPage(structBlockIDs, getGenericPageName(), null, null, null);
	}
	
	public void updatePropertySet() {
		if (getEntryPage() != null) {
			callUpdatePropertySet(getEntryPage());
		}
		for (IWizardHopPage page : getHopPages()) {
			callUpdatePropertySet(page);
		}
	}
	
	private void callUpdatePropertySet(IWizardHopPage page) {
		if (page instanceof CompoundDataBlockWizardPage)
			((CompoundDataBlockWizardPage) page).updatePropertySet();
		else if (page instanceof FullDataBlockCoverageWizardPage)
			((FullDataBlockCoverageWizardPage) page).updatePropertySet();
	}
	
	private String getGenericPageName() {
		if (getEntryPage() == null)
			return BlockBasedPropertySetEditorWizardHop.class.getName() + "#0"; //$NON-NLS-1$
		return BlockBasedPropertySetEditorWizardHop.class.getName() + "#" + String.valueOf(getHopPages().size()); //$NON-NLS-1$
	}
	
	private String getEditorScope() {
		if (editorScope == null) {
			editorScope = this.getClass().getName() + "#" + System.identityHashCode(this); //$NON-NLS-1$
		}
		return editorScope;
	}
}
