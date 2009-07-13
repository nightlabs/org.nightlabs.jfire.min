	public static DataFieldID create(
			DataBlock dataBlock, String _structFieldOrganisationID, String _structFieldID)
	{ 
		DataFieldID n = new DataFieldID();
		n.organisationID = dataBlock.getOrganisationID();
		n.propertySetID = dataBlock.getPropertySetID();
		n.structBlockOrganisationID = dataBlock.getStructBlockOrganisationID();
		n.structBlockID = dataBlock.getStructBlockID();
		n.dataBlockID = dataBlock.getDataBlockID();
		n.structFieldOrganisationID = _structFieldOrganisationID;
		n.structFieldID = _structFieldID;
		return n;
	}
	
	
	public static DataFieldID create(StructFieldID structFieldID, int dataBlockID) {
		DataFieldID n = new DataFieldID();
		n.structBlockOrganisationID = structFieldID.structBlockOrganisationID;
		n.structBlockID = structFieldID.structBlockID;
		n.structFieldOrganisationID = structFieldID.structFieldOrganisationID;
		n.structFieldID = structFieldID.structFieldID;
		n.dataBlockID = dataBlockID;
		return n;
	}

	public static DataFieldID create(PropertySet propertySet, StructFieldID structFieldID, int dataBlockID) {
		DataFieldID n = new DataFieldID();
		n.organisationID = propertySet.getOrganisationID();
		n.propertySetID = propertySet.getPropertySetID();
		n.structBlockOrganisationID = structFieldID.structBlockOrganisationID;
		n.structBlockID = structFieldID.structBlockID;
		n.structFieldOrganisationID = structFieldID.structFieldOrganisationID;
		n.structFieldID = structFieldID.structFieldID;
		n.dataBlockID = dataBlockID;
		return n;
	}
	