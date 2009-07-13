package org.nightlabs.jfire.workstation.search;

import javax.jdo.Query;

import org.nightlabs.jdo.query.AbstractJDOQuery;
import org.nightlabs.jfire.workstation.Workstation;

/**
 * A Query to find {@link Workstation}s.
 *
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 * @author Marius Heinzmann - marius[at]nightlabs[dot]com
 */
public class WorkstationQuery
	extends AbstractJDOQuery
{
	private static final long serialVersionUID = 20080811L;

	private String organisationID;
	private boolean workstationIDRegex = false;
	/**
	 * This member is used to create the jdoql query
	 */
	@SuppressWarnings("unused")
	private transient String workstationIDExpr;
	private String workstationID;
	private boolean descriptionRegex = false;
	/**
	 * This member is used to create the jdoql query
	 */
	@SuppressWarnings("unused")
	private transient String descriptionExpr;
	private String description;

	public static final class FieldName
	{
		public static final String organisationID = "organisationID";
		public static final String workstationIDRegex = "workstationIDRegex";
		public static final String workstationID = "workstationID";
		public static final String descriptionRegex = "descriptionRegex";
		public static final String description = "description";
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jdo.query.JDOQuery#prepareQuery()
	 */
	@Override
	protected void prepareQuery(Query q) {
		StringBuffer filter = new StringBuffer();

		filter.append(" true");

		if (isFieldEnabled(FieldName.organisationID) && organisationID != null) {
			String containsString = "(this.organisationID == :organisationID)";
			filter.append("\n && "+containsString);
		}

		if (isFieldEnabled(FieldName.workstationID) && workstationID != null) {
			workstationIDExpr = isWorkstationIDRegex() ? workstationID : ".*" + workstationID + ".*";
			filter.append("\n && (this.workstation.toLowerCase().matches(:workstationIDExpr.toLowerCase()))");
		}

		if (isFieldEnabled(FieldName.description) && description != null) {
			descriptionExpr = isDescriptionRegex() ? description : ".*" + description + ".*";
			filter.append("\n && (this.description.toLowerCase().matches(:descriptionExpr.toLowerCase()))");
		}
	}

	public String getOrganisationID() {
		return organisationID;
	}

	public void setOrganisationID(String organisationID)
	{
		final String oldOrganisationID = this.organisationID;
		this.organisationID = organisationID;
		notifyListeners(FieldName.organisationID, oldOrganisationID, organisationID);
	}

	public String getWorkstationID() {
		return workstationID;
	}

	/**
	 * Sets the workstationID to search for.
	 * This can either be a regular expression (use {@link #setWorkstationIDRegex(boolean)} then)
	 * or a string that should be contained in the worktationID of the workstations to find.
	 * 
	 * @param workstationID The workstationID to set.
	 */
	public void setWorkstationID(String workstationID)
	{
		final String oldWorkstationID = this.workstationID;
		this.workstationID = workstationID;
		notifyListeners(FieldName.workstationID, oldWorkstationID, workstationID);
	}
	
	/**
	 * @return Whether the value set with {@link #setWorkstationID(String)} represents a regular
	 *         expression. If this is <code>true</code>, the value set with {@link #setWorkstationID(String)}
	 *         will be passed directly as matching string, if it is <code>false</code> a regular expression
	 *         will be made out of it by prefixing and suffixing the value with ".*"
	 */
	public boolean isWorkstationIDRegex() {
		return workstationIDRegex;
	}

	/**
	 * Sets whether the value set with {@link #setWorkstationID(String)} represents a 
	 * regular expression.
	 * 
	 * @param workstationIDRegex The workstationIDRegex to search. 
	 */
	public void setWorkstationIDRegex(boolean workstationIDRegex) {
		final boolean oldWorkstationIDRegex = this.workstationIDRegex;
		this.workstationIDRegex = workstationIDRegex;
		notifyListeners(FieldName.workstationIDRegex, oldWorkstationIDRegex, workstationIDRegex);
	}
	

	public String getDescription() {
		return description;
	}

	/**
	 * Sets the description to search for.
	 * This can either be a regular expression (use {@link #setDescriptionRegex(boolean)} then)
	 * or a string that should be contained in the description of the workstations to find.
	 * 
	 * @param workstationID The workstationID to set.
	 */
	public void setDescription(String description)
	{
		final String oldDescription = description;
		this.description = description;
		notifyListeners(FieldName.description, oldDescription, description);
	}

	/**
	 * @return Whether the value set with {@link #setDescription(String)} represents a regular
	 *         expression. If this is <code>true</code>, the value set with {@link #setDescription(String)}
	 *         will be passed directly as matching string, if it is <code>false</code> a regular expression
	 *         will be made out of it by prefixing and suffixing the value with ".*"
	 */
	public boolean isDescriptionRegex() {
		return descriptionRegex;
	}

	/**
	 * Sets whether the value set with {@link #setDescription(String)} represents a 
	 * regular expression.
	 * 
	 * @param descriptionRegexRegex The descriptionRegexRegex to search. 
	 */
	public void setDescriptionRegex(boolean descriptionRegex) {
		final boolean oldDescriptionRegex = this.workstationIDRegex;
		this.descriptionRegex = descriptionRegex;
		notifyListeners(FieldName.descriptionRegex, oldDescriptionRegex, descriptionRegex);
	}
	
	@Override
	protected Class<Workstation> initCandidateClass()
	{
		return Workstation.class;
	}
}