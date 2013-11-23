
	public static StructID create(
			java.lang.String organisationID,
			Class<?> linkClass,
			java.lang.String structScope
	)
	{
		return create(
				organisationID,
				linkClass.getName(),
				structScope
		);
	}