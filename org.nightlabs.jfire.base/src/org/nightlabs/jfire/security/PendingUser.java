package org.nightlabs.jfire.security;

import java.io.Serializable;
import java.util.Date;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.Discriminator;
import javax.jdo.annotations.DiscriminatorStrategy;
import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.FetchGroups;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import org.nightlabs.jdo.ObjectIDUtil;
import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.jfire.person.Person;
import org.nightlabs.jfire.security.id.PendingUserID;
import org.nightlabs.util.IOUtil;

/**
 * A user placeholder. Objects of this type can be used to register a user id
 * at some point in time and later create a real User object for it.
 * <p>
 * The service layer methods are responsible of checking for the existence of
 * PendingUsers when creating a new User and vice versa.
 * </p>
 *
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
@PersistenceCapable(
		detachable="true",
		identityType=IdentityType.APPLICATION,
		objectIdClass=PendingUserID.class,
		table="JFireBase_PendingUser")
@FetchGroups({
	@FetchGroup(
		name=PendingUser.FETCH_GROUP_PERSON,
		members=@Persistent(name="person")),
	@FetchGroup(
		name=PendingUser.FETCH_GROUP_NAME,
		members=@Persistent(name="name")),
	@FetchGroup(
		name=PendingUser.FETCH_GROUP_PENDING_USER_DATA,
		members=@Persistent(name="pendingUserData"))
})
@Discriminator(strategy=DiscriminatorStrategy.CLASS_NAME)
public class PendingUser implements Serializable
{
	/**
	 * The serial version.
	 */
	private static final long serialVersionUID = 1L;

	public static final String FETCH_GROUP_PERSON = "PendingUser.person";
	public static final String FETCH_GROUP_NAME = "PendingUser.name";
	public static final String FETCH_GROUP_PENDING_USER_DATA = "PendingUser.pendingUserData";

	/**
	 * The user id to register. Analogical to {@link User}.
	 */
	@Persistent
	@PrimaryKey
	@Column(length=100)
	private String userID;

	/**
	 * The organisationID to which the pending user belongs. Analogical to {@link User}.
	 */
	@Persistent
	@PrimaryKey
	@Column(length=100)
	private String organisationID;

	/**
	 * User name. Analogical to {@link User}.
	 */
	@Persistent
	@Column(length=255)
	private String name;

	/**
	 * User description. Analogical to {@link User}.
	 */
	@Persistent
	@Column(sqlType="clob")
	private String description;

	/**
	 * The user's person. Analogical to {@link User}.
	 */
	@Persistent(loadFetchGroup="all")
	private Person person;

	/**
	 * The password to use when creating the real user object. As this value
	 * needs to be readable when creating the user, it is only Base64 encoded but
	 * not hashed.
	 */
	@Persistent
	@Column(length=255)
	private String password;

	/**
	 * The date when this pending user was created.
	 */
	@Persistent
	private Date createDT;

	/**
	 * The date until this pending user is valid. <code>null</code> if this
	 * pending user is valid forever.
	 */
	@Persistent
	private Date validUntilDT;

	/**
	 * Flag indicating that timer tasks are allowed to delete this pending user
	 * after the {@link #validUntil} date was reached.
	 */
	@Persistent
	private boolean deleteAutomatically;

	/**
	 * Arbitrary data to be used by the application creating such a pending user e.g
	 * a random confirmation string.
	 */
	@Persistent
	@Column(sqlType="blob")
	private byte[] pendingUserData;


	@Deprecated
	protected PendingUser() {}


	/**
	 *
	 * @param _organisationID
	 * @param userID Can be left 'null' to create an adequate next _userID-String.
	 * @deprecated Use {@link #PendingUser(PendingUserID)} instead!
	 */
	@Deprecated
	public PendingUser(String organisationID, String userID)
	{
		this(organisationID == null || userID == null ? null : PendingUserID.create(organisationID, userID));
	}
	public PendingUser(PendingUserID pendingUserID)
	{
		if (pendingUserID == null) {
			this.organisationID = IDGenerator.getOrganisationID();
			this.userID = IDGenerator.nextIDString(PendingUser.class);
		}
		else {
			Organisation.assertValidOrganisationID(pendingUserID.organisationID);
			ObjectIDUtil.assertValidIDString(pendingUserID.userID, "userID");
			this.organisationID = pendingUserID.organisationID;
			this.userID = pendingUserID.userID;
		}
	}

	/**
	 * Get the name.
	 * @return the name
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * Set the name.
	 * @param name the name to set
	 */
	public void setName(String name)
	{
		this.name = name;
	}

	/**
	 * Get the description.
	 * @return the description
	 */
	public String getDescription()
	{
		return description;
	}

	/**
	 * Set the description.
	 * @param description the description to set
	 */
	public void setDescription(String description)
	{
		this.description = description;
	}

	/**
	 * Get the person.
	 * @return the person
	 */
	public Person getPerson()
	{
		return person;
	}


	/**
	 * Set the person.
	 * @param person the person to set
	 */
	public void setPerson(Person person)
	{
		this.person = person;
	}

	/**
	 * Get the createDT.
	 * @return the createDT
	 */
	public Date getCreateDT()
	{
		return createDT;
	}

	/**
	 * Set the createDT.
	 * @param createDT the createDT to set
	 */
	public void setCreateDT(Date createDT)
	{
		this.createDT = createDT;
	}

	/**
	 * Get the validUntilDT.
	 * @return the validUntilDT
	 */
	public Date getValidUntilDT()
	{
		return validUntilDT;
	}

	/**
	 * Set the validUntilDT.
	 * @param validUntilDT the validUntilDT to set
	 */
	public void setValidUntilDT(Date validUntilDT)
	{
		this.validUntilDT = validUntilDT;
	}

	/**
	 * Get the deleteAutomatically.
	 * @return the deleteAutomatically
	 */
	public boolean isDeleteAutomatically()
	{
		return deleteAutomatically;
	}

	/**
	 * Set the deleteAutomatically.
	 * @param deleteAutomatically the deleteAutomatically to set
	 */
	public void setDeleteAutomatically(boolean deleteAutomatically)
	{
		this.deleteAutomatically = deleteAutomatically;
	}

	/**
	 * Get the pendingUserData.
	 * @return the pendingUserData
	 */
	public byte[] getPendingUserData()
	{
		return pendingUserData;
	}

	/**
	 * Set the pendingUserData.
	 * @param pendingUserData the pendingUserData to set
	 */
	public void setPendingUserData(byte[] pendingUserData)
	{
		this.pendingUserData = pendingUserData;
	}

	/**
	 * Get the userID.
	 * @return the userID
	 */
	public String getUserID()
	{
		return userID;
	}

	/**
	 * Get the organisationID.
	 * @return the organisationID
	 */
	public String getOrganisationID()
	{
		return organisationID;
	}

	/**
	 * Get the pendoings user password as plain text.
	 * @return The unencoded password
	 */
	public String getPasswordPlain()
	{
		if(this.password == null)
			return null;
		byte[] encoded = this.password.getBytes(IOUtil.CHARSET_UTF_8);
		byte[] decoded = org.apache.commons.codec.binary.Base64.decodeBase64(encoded);
		return new String(decoded, IOUtil.CHARSET_UTF_8);
	}

	/**
	 * Set the pending users password
	 * @param plainPassword The unencoded password to set
	 */
	public void setPasswordPlain(String plainPassword)
	{
		if (plainPassword != null && !UserLocal.isValidPassword(plainPassword))
			throw new IllegalArgumentException("The given password is not a valid password!");
		if(plainPassword == null)
			this.password = null;
		else {
			byte[] raw = plainPassword.getBytes(IOUtil.CHARSET_UTF_8);
			byte[] encoded = org.apache.commons.codec.binary.Base64.encodeBase64(raw);
			this.password = new String(encoded, IOUtil.CHARSET_UTF_8);
		}
	}
}
