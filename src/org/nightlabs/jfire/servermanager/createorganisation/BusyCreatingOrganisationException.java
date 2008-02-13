package org.nightlabs.jfire.servermanager.createorganisation;

import java.util.Set;

import org.nightlabs.ModuleException;

public class BusyCreatingOrganisationException
extends ModuleException
{
	private static final long serialVersionUID = 1L;

	private Set<String> organisationIDsBusy;
	private String organisationIDNew;

	private static Object[] createStringFormatArgs(String organisationIDNew, Set<String> organisationIDsBusy)
	{
		if (organisationIDNew == null)
			throw new IllegalArgumentException("organisationIDNew must not be null!");

		if (organisationIDsBusy == null)
			throw new IllegalArgumentException("organisationIDsBusy must not be null!");

		String[] res = new String[organisationIDsBusy.size() + 1];
		res[0] = organisationIDNew;
		int idx = 0;
		for (String organisationIDBusy : organisationIDsBusy)
			res[++idx] = organisationIDBusy;

		return res;
	}

	/**
	 * This exception is thrown if someone calls {@link JFireServerManager#createOrganisationAsync(String, String, String, String, boolean)}
	 * while there is already another organisation in the progress of being created. Even though, the system currently only allows one
	 * organisation at a time to be created, we already manage multiple - hence it might later be changed.
	 *
	 * @param organisationIDNew the id of the organisation that cannot be created now, because it
	 */
	public BusyCreatingOrganisationException(String organisationIDNew, Set<String> organisationIDsBusy)
	{
		super(String.format("The organisation %s cannot be created, because the system is currently busy creating another organisation: %s", createStringFormatArgs(organisationIDNew, organisationIDsBusy)));
	}

	public String getOrganisationIDNew()
	{
		return organisationIDNew;
	}

	public Set<String> getOrganisationIDsBusy()
	{
		return organisationIDsBusy;
	}
}
