package org.nightlabs.jfire.security.search;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;

import javax.jdo.Query;

import org.apache.log4j.Logger;
import org.nightlabs.jdo.query.AbstractJDOQuery;
import org.nightlabs.jfire.security.User;

/**
 * {@link AbstractJDOQuery} for searching {@link User}s based on the given criteria
 *
 * @author Daniel.Mazurek [at] NightLabs [dot] de
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 * @author Marius Heinzmann - marius[at]nightlabs[dot]com
 */
public class UserQuery
	extends AbstractJDOQuery
{
	/**
	 * The serial version of this class.
	 */
	private static final long serialVersionUID = 20080811L;

	private static final Logger logger = Logger.getLogger(UserQuery.class);

	public static final class FieldName
	{
		public static final String maxChangeDT = "maxChangeDT";
		public static final String minChangeDT = "minChangeDT";
		public static final String nameRegex = "nameRegex";
		public static final String name = "name";
		public static final String organisationID = "organisationID";
		public static final String userID = "userID";
		public static final String userTypes = "userTypes";
	}

	@Override
	protected void prepareQuery(Query q)
	{
		StringBuffer filter = new StringBuffer();

		filter.append(" true");

		if (isFieldEnabled(FieldName.name) && name != null) {
			nameExpr = isNameRegex() ? name : ".*" + name + ".*";
			filter.append("\n && (this.name.toLowerCase().matches(:nameExpr))");
		}

		if (isFieldEnabled(FieldName.organisationID) && organisationID != null) {
			String containsString = "(this.organisationID.indexOf(\""+organisationID+"\") >= 0)";
			filter.append("\n && "+containsString);
		}

		if (isFieldEnabled(FieldName.userID) && userID != null) {
			String containsString = "(this.userID.indexOf(\""+userID+"\") >= 0)";
			filter.append("\n && "+containsString);
		}

		if (isFieldEnabled(FieldName.userTypes) && userTypes != null) {
			StringBuffer containsString = new StringBuffer();
			containsString.append("(");
			for (Iterator<String> it = userTypes.iterator(); it.hasNext();) {
				String userType = it.next();
				// This applies to UserGroup as well when only User is set, was this intended ?
//				containsString.append("(this.userType.indexOf(\""+userType+"\") >= 0)");
				containsString.append("(this.userType == \"" + userType + "\")");
				if (it.hasNext())
					containsString.append(" || ");
			}
			containsString.append(")");
			filter.append("\n && "+containsString);
		}

		if (isFieldEnabled(FieldName.minChangeDT) && minChangeDT != null) {
			filter.append("\n && this.changeDT >= :minChangeDT");
		}

		if (isFieldEnabled(FieldName.maxChangeDT) && maxChangeDT != null) {
			filter.append("\n && this.changeDT <= :maxChangeDT");
		}

		if (logger.isDebugEnabled()) {
			logger.debug("Filter:");
			logger.debug(filter.toString());
		}

		q.setFilter(filter.toString());
	}

	private boolean nameRegex = false;
	/**
	 * This member is used to create the jdoql query
	 */
	@SuppressWarnings("unused")
	private transient String nameExpr;
	private String name = null;
	/**
	 * returns the name.
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * sets the name
	 * @param name the name to set
	 */
	public void setName(String name)
	{
		final String oldName = this.name;
		this.name = name;
		notifyListeners(FieldName.name, oldName, name);
	}

	/**
	 * @return Whether the value set with {@link #setName(String)} represents a regular
	 *         expression. If this is <code>true</code>, the value set with {@link #setName(String)}
	 *         will be passed directly as matching string, if it is <code>false</code> a regular expression
	 *         will be made out of it by prefixing and suffixing the value with ".*"
	 */
	public boolean isNameRegex() {
		return nameRegex;
	}

	/**
	 * Sets whether the value set with {@link #setName(String)} represents a 
	 * regular expression.
	 * 
	 * @param nameRegex The nameRegex to search. 
	 */
	public void setNameRegex(boolean nameRegex) {
		final boolean oldnameRegex = this.nameRegex;
		this.nameRegex = nameRegex;
		notifyListeners(FieldName.nameRegex, oldnameRegex, nameRegex);
	}
	
	
	private String userID = null;
	/**
	 * returns the userID.
	 * @return the userID
	 */
	public String getUserID() {
		return userID;
	}

	/**
	 * sets the userID
	 * @param userID the userID to set
	 */
	public void setUserID(String userID)
	{
		final String oldUserID = this.userID;
		this.userID = userID;
		notifyListeners(FieldName.userID, oldUserID, userID);
	}

	private Collection<String> userTypes = null;
	/**
	 * returns the userTypes.
	 * @return the userTypes
	 */
	public Collection<String> getUserTypes() {
		return userTypes;
	}

	/**
	 * sets the userTypes
	 * @param userTypes the userTypes to set
	 */
	public void setUserTypes(Collection<String> userTypes)
	{
		final Collection<String> oldUserTypes = this.userTypes;
		this.userTypes = userTypes;
		notifyListeners(FieldName.userTypes, oldUserTypes, Collections.unmodifiableCollection(userTypes));
	}

	private String organisationID = null;
	/**
	 * returns the organisationID.
	 * @return the organisationID
	 */
	public String getOrganisationID() {
		return organisationID;
	}

	/**
	 * sets the organisationID
	 * @param organisationID the organisationID to set
	 */
	public void setOrganisationID(String organisationID)
	{
		final String oldOrganisationID = this.organisationID;
		this.organisationID = organisationID;
		notifyListeners(FieldName.organisationID, oldOrganisationID, organisationID);
	}

	private Date minChangeDT = null;
	/**
	 * returns the minChangeDT.
	 * @return the minChangeDT
	 */
	public Date getMinChangeDT() {
		return minChangeDT;
	}

	/**
	 * sets the minChangeDT
	 * @param minChangeDT the minChangeDT to set
	 */
	public void setMinChangeDT(Date minChangeDT)
	{
		final Date oldMinChangeDate = this.minChangeDT;
		this.minChangeDT = minChangeDT;
		notifyListeners(FieldName.minChangeDT, oldMinChangeDate, minChangeDT);
	}

	private Date maxChangeDT = null;
	/**
	 * returns the maxChangeDT.
	 * @return the maxChangeDT
	 */
	public Date getMaxChangeDT() {
		return maxChangeDT;
	}

	/**
	 * sets the maxChangeDT
	 * @param maxChangeDT the maxChangeDT to set
	 */
	public void setMaxChangeDT(Date maxChangeDT)
	{
		final Date oldMaxChangeDate = this.maxChangeDT;
		this.maxChangeDT = maxChangeDT;
		notifyListeners(FieldName.maxChangeDT, oldMaxChangeDate, maxChangeDT);
	}

	@Override
	protected Class<User> initCandidateClass()
	{
		return User.class;
	}
}