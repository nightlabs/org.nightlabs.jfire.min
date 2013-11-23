package org.nightlabs.jfire.security;

import java.io.Serializable;
import java.util.Collection;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.jdo.annotations.Column;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.PersistenceModifier;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;
import javax.jdo.annotations.Queries;

import org.apache.log4j.Logger;
import org.nightlabs.jdo.ObjectIDUtil;
import org.nightlabs.jfire.security.id.AuthorityID;
import org.nightlabs.jfire.security.id.AuthorityMetaID;
import org.nightlabs.jfire.security.id.AuthorityTypeID;

@PersistenceCapable(
		objectIdClass=AuthorityMetaID.class,
		identityType=IdentityType.APPLICATION,
		detachable="true",
		table="JFireBase_AuthorityMeta")
@Queries({
	@javax.jdo.annotations.Query(
			name="getAuthorityMetaByAuthorityID",
			value="SELECT WHERE this.securingAuthorityID == :authorityID"
	)
})
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class AuthorityMeta implements SecuredObject, Serializable
{
	private static final Logger logger = Logger.getLogger(AuthorityMeta.class);

	public static final AuthorityMeta getAuthorityMetaByAuthorityID(PersistenceManager pm, AuthorityID authorityID)
	{
		if (authorityID != null) {
			String authorityIDString = authorityID.toString();
			Query q = pm.newNamedQuery(AuthorityMeta.class, "getAuthorityMetaByAuthorityID");
			try {
				Collection<AuthorityMeta> authorityMetas = (Collection<AuthorityMeta>) q.execute(authorityIDString);
				if (authorityMetas.size() > 1) {
					logger.error("More then one AuthorityMeta exists for authority "+authorityID);
				}
				if (authorityMetas.size() == 1)
					return authorityMetas.iterator().next();

				return null;
			} finally {
				q.closeAll();
			}
		}
		return null;
	}

	@PrimaryKey
	@Column(length=100)
	private String organisationID;

	@PrimaryKey
	@Column(length=50)
	private String authorityMetaID;

	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private String securingAuthorityTypeID;

	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private String securingAuthorityID;

	/**
	 * @deprecated only for JDO
	 */
	@Deprecated
	protected AuthorityMeta() {}

	public AuthorityMeta(String organisationID, String authorityMetaID) {
		this.organisationID = organisationID;
		this.authorityMetaID = authorityMetaID;
	}

	@Override
	public AuthorityID getSecuringAuthorityID() {
		if (securingAuthorityID == null)
			return null;

		try {
			return new AuthorityID(securingAuthorityID);
		} catch (Exception e) {
			throw new RuntimeException(e); // should never happen.
		}
	}

	@Override
	public AuthorityTypeID getSecuringAuthorityTypeID()
	{
		if (securingAuthorityTypeID == null)
			return null;

		return (AuthorityTypeID) ObjectIDUtil.createObjectID(securingAuthorityTypeID);
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.security.SecuredObject#setSecuringAuthorityID(org.nightlabs.jfire.security.id.AuthorityID)
	 */
	@Override
	public void setSecuringAuthorityID(AuthorityID authorityID)
	{
		if (authorityID == null) {
			securingAuthorityID = null;
		} else {
			securingAuthorityID = authorityID.toString();
		}
	}

	public void setSecuringAuthorityTypeID(AuthorityTypeID authorityTypeID) {
		if (this.securingAuthorityTypeID != null && !this.getSecuringAuthorityTypeID().equals(authorityTypeID))
			throw new IllegalStateException("A different AuthorityType has already been assigned! Cannot change this value afterwards! Currently assigned: " + this.securingAuthorityTypeID + " New value: " + authorityTypeID);

		this.securingAuthorityTypeID = authorityTypeID == null ? null : authorityTypeID.toString();
	}

	/**
	 * @return the organisationID
	 */
	public String getOrganisationID() {
		return organisationID;
	}

	/**
	 * @return the authorityMetaID
	 */
	public String getAuthorityMetaID() {
		return authorityMetaID;
	}

}
