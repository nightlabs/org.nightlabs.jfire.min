package org.nightlabs.jfire.prop.config;

import java.util.Set;

import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.PersistenceCapable;

import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.layout.AbstractEditLayoutConfigModule;
import org.nightlabs.jfire.layout.AbstractEditLayoutEntry;
import org.nightlabs.jfire.prop.PropertySet;
import org.nightlabs.jfire.prop.StructField;

/**
 * Base-class for config-modules that store a StructField-based ui-layout for PropertySets. The
 * applications for subclasses of this class are the search for {@link PropertySet}s or their
 * editing.
 * <p>
 * The entries stored by this config-module ({@link PropertySetEditLayoutEntry}) are capable of
 * storing a reference to one or more StructFields.
 * </p>
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
public class PropertySetEditLayoutConfigModule
	extends AbstractEditLayoutConfigModule<Set<StructField>, PropertySetEditLayoutEntry>
{
	private static final long serialVersionUID = 20100108L;

	/**
	 * Constructs a new {@link PropertySetFieldBasedEditLayoutConfigModule}
	 */
	public PropertySetEditLayoutConfigModule() {
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Creates an {@link PropertySetEditLayoutEntry} that is capable of referencing one or more
	 * StructFields.
	 * </p>
	 */
	@Override
	public PropertySetEditLayoutEntry createEditLayoutEntry(String entryType) {
		PropertySetEditLayoutEntry entry = new PropertySetEditLayoutEntry(
				this, IDGenerator.nextID(AbstractEditLayoutEntry.class),
				entryType);
		return entry;
	}

}
