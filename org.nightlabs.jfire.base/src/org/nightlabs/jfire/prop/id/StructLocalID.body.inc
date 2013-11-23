
	public static StructLocalID create(
			java.lang.String organisationID,
			Class<?> linkClass,
			java.lang.String structScope,
			java.lang.String structLocalScope
	)
	{
		return create(
				organisationID,
				linkClass.getName(),
				structScope,
				structLocalScope
		);
	}