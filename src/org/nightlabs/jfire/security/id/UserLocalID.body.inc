
	public static UserLocalID create(UserID userID, String localOrganisationID)
	{
		return UserLocalID.create(userID.organisationID, userID.userID, localOrganisationID);
	}