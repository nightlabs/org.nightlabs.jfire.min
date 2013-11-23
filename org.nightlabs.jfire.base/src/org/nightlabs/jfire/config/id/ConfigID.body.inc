
	/**
	 * Creates an id-object that points to the given linkObject. You should always
	 * pass an instance of the keyObject itself (not it's id-object)
	 */
	public static ConfigID create(String organisationID, Object linkObject) 
	{
		return create(organisationID, getConfigKeyForLinkObject(linkObject), linkObject.getClass().getName());
	}
	
//	/**
//	 * Creates an id-object that points to the given Organisable linkObject. 
//	 * You should always pass an instance of the keyObject itself (not it's id-object)
//	 */
//	public static ConfigID create(Organisable linkObject) 
//	{
//		return create(linkObject.getOrganisationID(), getConfigKeyForLinkObject(linkObject), linkObject.getClass().getName());
//	}
	
	/**
	 * Creates an id-object that points to the given linkObject. Pass here the
	 * object-id of the linked object you whant to create a ConfigID for.
	 */
	public static ConfigID create(String organistaionID, ObjectID linkObjectID, Class linkObjectClass) {
		return create(organistaionID, getConfigKeyForLinkObject(linkObjectID), linkObjectClass.getName());
	}
	
	public static String getConfigKeyForLinkObject(Object linkObject) {
		if (linkObject instanceof org.nightlabs.jdo.ObjectID)
			return linkObject.toString();		
		else if (linkObject instanceof PersistenceCapable) 
		{
			Object idObject = JDOHelper.getObjectId(linkObject);
			if (idObject == null)
				throw new IllegalArgumentException("JDOHelper could not find an id-object for the given keyObject "+linkObject);
			return idObject.toString();
		}
		else
			throw new IllegalArgumentException("The given linkObject is neither PersistenceCapable nor an ObjectID.");
	}