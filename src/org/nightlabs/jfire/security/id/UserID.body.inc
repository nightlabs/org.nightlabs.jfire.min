
	public static UserID create(JFireBasePrincipal principal)
	{
		return UserID.create(principal.getOrganisationID(), principal.getUserID());
	}

	public static UserID create(UserLocalID userLocalID)
	{
		return UserID.create(userLocalID.organisationID, userLocalID.userID);
	}