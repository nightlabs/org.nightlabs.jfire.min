package org.nightlabs.jfire.security;

import org.nightlabs.jdo.ObjectID;
import org.nightlabs.jdo.ObjectIDUtil;
import org.nightlabs.jfire.security.id.AuthorityID;
import org.nightlabs.jfire.security.id.AuthorityTypeID;

/**
 * Objects implementing this interface can have an individual {@link Authority} (a kind of security configuration)
 * assigned.
 * <p>
 * A class implementing this interface must declare the fields defined in the subclass {@link FieldName}. In case,
 * the interface {@link org.nightlabs.inheritance.Inheritable} is implemented, too, the inheritance meta-data is managed
 * via the field names declared here.
 * </p>
 * <p>
 * The fields <code>securingAuthorityTypeID</code> and <code>securingAuthorityID</code> should be of type {@link String}.
 * The methods {@link ObjectIDUtil#createObjectID(String)} and {@link ObjectID#toString()} should be used to convert back and forth
 * between {@link String} and {@link AuthorityTypeID} / {@link AuthorityID}.
 * </p>
 *
 * @author marco schulze - marco at nightlabs dot de
 */
public interface SecuredObject
{
	/**
	 * Class holding field name constants according to the convention defined by {@link org.nightlabs.inheritance.Inheritable}.
	 */
	public static final class FieldName {
		/**
		 * Name of the field which is used for the method {@link SecuredObject#getSecuringAuthorityTypeID()}.
		 */
		public static final String securingAuthorityTypeID = "securingAuthorityTypeID";
		/**
		 * Name of the field which is used for the method {@link SecuredObject#getSecuringAuthorityID()}.
		 */
		public static final String securingAuthorityID = "securingAuthorityID";
	}

	/**
	 * Return that <code>AuthorityType</code> that controls the <code>Authority</code> in the field 'securingAuthority'
	 * (getter {@link #getSecuringAuthorityID()}).
	 * There is the rule that <code>this.securingAuthority.authorityType == this.securingAuthorityType</code>.
	 *
	 * @return the <code>AuthorityType</code> of the <code>Authority</code> returned by {@link #getSecuringAuthorityID()}. Must not return <code>null</code>.
	 */
	AuthorityTypeID getSecuringAuthorityTypeID();

	/**
	 * Get the object-id of the {@link Authority} which is responsible for this object or <code>null</code>, if none is assigned.
	 * If there is no <code>Authority</code> assigned, the default access rights configuration, configured for the
	 * whole organisation is applied alone.
	 *
	 * @return the <code>Authority</code> (i.e. access right configuration) which is assigned to this object or <code>null</code>.
	 */
	AuthorityID getSecuringAuthorityID();

	/**
	 * Set the object-id of the {@link Authority} to be responsible for the access rights of this object or <code>null</code> to
	 * clear this property. If none is assgned, the organisation's default access rights configuration is taken
	 * into account alone.
	 * <p>
	 * There is the rule that <code>this.securingAuthority.authorityType == this.securingAuthorityType</code>.
	 * </p>
	 *
	 * @param authorityID the new <code>Authority</code> or <code>null</code>.
	 */
	void setSecuringAuthorityID(AuthorityID authorityID);
}
