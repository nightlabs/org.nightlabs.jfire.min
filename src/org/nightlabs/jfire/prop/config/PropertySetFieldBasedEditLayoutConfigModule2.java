package org.nightlabs.jfire.prop.config;

import java.util.Set;

import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.PersistenceCapable;

import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.layout.AbstractEditLayoutConfigModule;
import org.nightlabs.jfire.layout.AbstractEditLayoutEntry;
import org.nightlabs.jfire.prop.StructField;

/**
 * Base implementation for FieldBased UI of PropertySets.
 *
 * @see AbstractEditLayoutConfigModule
 *
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] -->
 * @author Marius Heinzmann <!-- marius [AT] nightlabs [DOT] de -->
 */
@PersistenceCapable(
	identityType=IdentityType.APPLICATION,
	detachable="true")
@Inheritance(strategy=InheritanceStrategy.SUPERCLASS_TABLE)
public class PropertySetFieldBasedEditLayoutConfigModule2
	extends AbstractEditLayoutConfigModule<Set<StructField>, PropertySetFieldBasedEditLayoutEntry2>
{
	private static final long serialVersionUID = 20100108L;

	/**
	 * Constructs a new {@link PropertySetFieldBasedEditLayoutConfigModule}
	 */
	public PropertySetFieldBasedEditLayoutConfigModule2() {
	}

	@Override
	public PropertySetFieldBasedEditLayoutEntry2 createEditLayoutEntry(String entryType) {
		PropertySetFieldBasedEditLayoutEntry2 entry = new PropertySetFieldBasedEditLayoutEntry2(
				this, IDGenerator.nextID(AbstractEditLayoutEntry.class),
				entryType);
		return entry;
	}

}
