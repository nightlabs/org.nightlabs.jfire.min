package org.nightlabs.jfire.person;

import java.util.Locale;

import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.NullValue;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.PersistenceModifier;
import javax.jdo.annotations.Persistent;

import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.jfire.prop.PropertySet;
import org.nightlabs.jfire.prop.Struct;
import org.nightlabs.jfire.prop.StructLocal;
import org.nightlabs.util.NLLocale;

/**
 * Instances of {@link Person} represent a person in the JFire datastore.
 * As {@link Person} is a subclass of {@link PropertySet} it has
 * a extensible set of properties. A standard set of properties is defined
 * in {@link PersonStruct}.
 * <p>
 * A {@link Person} might have several roles in a JFire organisation, it
 * might be for example a business partner (LegalEntity) or an employee
 * etc.. Therefore these role objects like LegalEntity should not be
 * instances/subclasses of {@link Person}, but should have a reference to a
 * {@link Person}. This way the same {@link Person} instance can have
 * its different roles like in real life.
 * </p>
 * <p>
 * Note that the {@link Struct} scope for a {@link Person} is pre-defined
 * (as developers must rely on some data each person has), but each
 * person might have an extended set of Properties that will be referenced
 * by there {@link StructLocal} scope. *
 * The default for the {@link StructLocal} scope of a
 * {@link Person} is {@link #STRUCT_LOCAL_SCOPE}).
 * </p>
 *
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 * 		persistence-capable-superclass="org.nightlabs.jfire.prop.PropertySet"
 *		detachable="true"
 *		table="JFireBase_Person"
 *
 * @jdo.inheritance strategy="new-table"
 */
@PersistenceCapable(
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireBase_Person")
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class Person extends PropertySet {

	/**
	 * The scope of the {@link Struct} used for {@link Person}s, this can not be changed for {@link Person}s.
	 */
	public static final String STRUCT_SCOPE = Struct.DEFAULT_SCOPE;
	/**
	 * The scope of the {@link StructLocal} used for Persons by default.
	 */
	public static final String STRUCT_LOCAL_SCOPE = StructLocal.DEFAULT_SCOPE;
	
	/**
	 * Property name constant for the {@link #locale} property.
	 */
	public static final String PROP_LOCALE = "locale";

	/**
	 * The serial version of this class.
	 */
	private static final long serialVersionUID = 20080610L;

	/**
	 * @jdo.field persistence-modifier="persistent" null-value="exception" default-fetch-group="true"
	 */
	@Persistent(
		nullValue=NullValue.EXCEPTION,
		defaultFetchGroup="true",
		persistenceModifier=PersistenceModifier.PERSISTENT)
	private Locale locale;

	/**
	 * @deprecated Only for JDO
	 */
	@Deprecated
	public Person() { }

	/**
	 * Create a new {@link Person} with the given primary key fields
	 * and default values for {@link Struct} scope and {@link StructLocal} scope.
	 *
	 * @param organisationID The organisation id
	 * @param propertySetID The property set id
	 */
	public Person(String organisationID, long propertySetID)
	{
		super(organisationID, propertySetID, Organisation.DEV_ORGANISATION_ID, Person.class.getName(), STRUCT_SCOPE, STRUCT_LOCAL_SCOPE);
		locale = NLLocale.getDefault();
	}

	/**
	 * Create a new {@link Person} with the given primary key fields.
	 *
	 * @param organisationID The organisation id
	 * @param propertySetID The property set id
	 */
	public Person(String organisationID, long propertySetID, String structLocalScope)
	{
		super(organisationID, propertySetID, Organisation.DEV_ORGANISATION_ID, Person.class.getName(), STRUCT_SCOPE, structLocalScope);
		locale = NLLocale.getDefault();
	}

	/**
	 * Overrides and prevents the change of the {@link Struct} scope of a Person.
	 * An {@link UnsupportedOperationException} will be thrown on the attempt
	 * to change the {@link Struct} scope of a Person. It is set only in
	 * the constructor.
	 * <p>
	 * As this is called during inflating/deflating of the {@link Person}
	 * it's allowed to call this method, but not to change the value of the scope.
	 * </p>
	 * @param structScope Has to be the same as already set in the {@link Person}.
	 */
	@Override
	protected void setStructScope(String structScope) {
		if (structScope == null)
			throw new IllegalArgumentException("Parameter structScope might not be null.");
		if (!structScope.equals(this.getStructScope()))
			throw new UnsupportedOperationException("A person might not change its struct-scope!");
	}

	/*
	 * (non-Javadoc)
	 * @see org.nightlabs.jfire.prop.PropertySet#createPropertyClone()
	 */
	@Override
	protected PropertySet createPropertySetClone() {
		return new Person(getOrganisationID(), IDGenerator.nextID(PropertySet.class));
	}

	/**
	 * Get the person's locale. This should be used for mailings or whenever communication with this person happens.
	 *
	 * @return the person's the locale. This is never <code>null</code>.
	 */
	public Locale getLocale() {
		return locale;
	}
	/**
	 * Set the person's locale.
	 *
	 * @param locale the person's locale.
	 */
	public void setLocale(Locale locale) {
		if (locale == null)
			throw new IllegalArgumentException("locale must not be null!");

		this.locale = locale;
	}
}
