	public static StructFieldID create (
		StructBlockID structBlockID,
		String _structFieldOrganisationID, 
		String _structFieldID 
	) {
		return create(
			structBlockID.structBlockOrganisationID,
			structBlockID.structBlockID,
			_structFieldOrganisationID,
			_structFieldID
		);
	}
	
	public static StructFieldID create (
			StructBlockID structBlockID,
			String _structFieldID 
		) 
	{
		return create(
			structBlockID.structBlockOrganisationID,
			structBlockID.structBlockID,
			structBlockID.structBlockOrganisationID,
			_structFieldID
		);
	}

	public String getPrimaryKey() {
		return 
			structBlockOrganisationID+"/"+
			structBlockID+"/"+
			structFieldOrganisationID+"/"+
			structFieldID;
	}
		