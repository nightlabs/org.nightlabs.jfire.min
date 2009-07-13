/**
 *
 */
package org.nightlabs.jfire.security;

import org.nightlabs.jfire.security.id.AuthorityID;

/**
 * This exception is thrown when an attempt to modify an externally/remotely managed {@link Authority} was detected.
 * Such an {@link Authority} is tagged with a non-<code>null</code> managed-by property.
 *
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 */
public class ManagedAuthorityModficationException extends RuntimeException {

	private static final long serialVersionUID = 20081222L;

	private AuthorityID authorityID;
	private String managedBy;

	/**
	 * Constructs a {@link ManagedAuthorityModficationException} for the given
	 * {@link ProductTypeID} and managed-by tag.
	 * @param productTypeID The {@link ProductTypeID} of the {@link ProductType} for which a modification attempt was detected.
	 * @param managedBy The managed-by tag of the {@link ProductType} for which a modification attempt was detected.
	 */
	public ManagedAuthorityModficationException(AuthorityID authorityID, String managedBy) {
		super("Attempt to store the externally/remotely managed Authority " + authorityID + " which is managed by " + managedBy);
		this.authorityID = authorityID;
		this.managedBy = managedBy;
	}

	/**
	 * @return The {@link AuthorityID} of the {@link Authority} for which a modification attempt was detected.
	 */
	public AuthorityID getAuthorityID() {
		return authorityID;
	}

	/**
	 * @return The managed-by tag of the {@link Authority} for which a modification attempt was detected.
	 */
	public String getManagedBy() {
		return managedBy;
	}
}
