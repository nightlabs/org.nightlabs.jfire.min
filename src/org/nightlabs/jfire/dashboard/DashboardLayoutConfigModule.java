/**
 * 
 */
package org.nightlabs.jfire.dashboard;

import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.PersistenceCapable;

import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.layout.AbstractEditLayoutConfigModule;
import org.nightlabs.jfire.layout.AbstractEditLayoutEntry;

/**
 * @author abieber
 *
 */
@PersistenceCapable(
		identityType=IdentityType.APPLICATION,
		detachable="true")
@Inheritance(strategy=InheritanceStrategy.SUPERCLASS_TABLE)
public class DashboardLayoutConfigModule<T> extends AbstractEditLayoutConfigModule<T, DashboardGadgetLayoutEntry<T>> {

	private static final long serialVersionUID = 20111212L;

	/**
	 * 
	 */
	public DashboardLayoutConfigModule() {
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.layout.AbstractEditLayoutConfigModule#createEditLayoutEntry(java.lang.String)
	 */
	@Override
	public DashboardGadgetLayoutEntry<T> createEditLayoutEntry(String entryType) {
		DashboardGadgetLayoutEntry<T> entry = new DashboardGadgetLayoutEntry<T>(this, IDGenerator.nextID(AbstractEditLayoutEntry.class), entryType);
		return entry;
	}

	
	
}
