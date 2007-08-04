package org.nightlabs.jfire.servermanager.createorganisation;

import java.io.Serializable;

import org.nightlabs.util.Util;

public class CreateOrganisationProgressID
implements Serializable
{
	private static final long serialVersionUID = 1L;

	private String id;

	public CreateOrganisationProgressID(String id)
	{
		if (id == null)
			throw new IllegalArgumentException("id must not be null!");

		this.id = id;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (!(obj instanceof CreateOrganisationProgressID)) return false;
		CreateOrganisationProgressID o = (CreateOrganisationProgressID) obj;
		return Util.equals(this.id, o.id);
	}
	@Override
	public int hashCode()
	{
		return Util.hashCode(id);
	}
	@Override
	public String toString()
	{
		return CreateOrganisationProgressID.class.getName() + '[' + id + ']';
	}
}
