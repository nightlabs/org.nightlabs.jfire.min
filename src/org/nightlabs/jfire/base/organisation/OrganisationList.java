package org.nightlabs.jfire.base.organisation;

import java.util.Iterator;
import java.util.List;

import javax.jdo.FetchPlan;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.nightlabs.annotation.Implement;
import org.nightlabs.base.composite.ListComposite;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.jfire.organisation.id.OrganisationID;

public class OrganisationList
		extends ListComposite<Organisation>
{
	public static final String[] FETCH_GROUPS_ORGANISATION = {
		FetchPlan.DEFAULT
	};

	private OrganisationFilter organisationFilter;
	private OrganisationIDDataSource organisationIDDataSource;

	public OrganisationList(Composite parent)
	{
		this(parent, null, null);
	}
	public OrganisationList(Composite parent, OrganisationFilter _organisationFilter)
	{
		this(parent, null, _organisationFilter);
	}
	public OrganisationList(Composite parent, OrganisationIDDataSource _organisationIDDataSource)
	{
		this(parent, _organisationIDDataSource, null);
	}
	/**
	 * @param parent The parent composite.
	 * @param _organisationIDDataSource The datasource which provides {@link OrganisationID}s. If this is <code>null</code>,
	 *		all {@link Organisation}s known to this organisation are loaded from the server.
	 * @param _organisationFilter A filter which filters the loaded {@link Organisation}s on the client-side. When using
	 *		an {@link OrganisationIDDataSource}, you should better pass <code>null</code> here and filter already on
	 *		the server-side.
	 */
	public OrganisationList(Composite parent, OrganisationIDDataSource _organisationIDDataSource, OrganisationFilter _organisationFilter)
	{
		super(parent, SWT.NONE, new LabelProvider() {
			@Override
			public String getText(Object element)
			{
				if (!(element instanceof Organisation))
					return String.valueOf(element);

				Organisation o = (Organisation) element;
				return o.getOrganisationID();
			}
		});

		this.organisationIDDataSource = _organisationIDDataSource;
		this.organisationFilter = _organisationFilter;

		new Job("Load Organisations") {
			@Implement
			protected IStatus run(IProgressMonitor monitor)
			{
				final List<Organisation> organisations = organisationIDDataSource == null ?
					OrganisationDAO.sharedInstance().getOrganisations(
						FETCH_GROUPS_ORGANISATION, NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT, monitor)
						:
					OrganisationDAO.sharedInstance().getOrganisations(
							organisationIDDataSource.getOrganisationIDs(),
							FETCH_GROUPS_ORGANISATION, NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT, monitor);

				if (organisationFilter != null) {
					for(Iterator<Organisation> it = organisations.iterator(); it.hasNext(); ) {
						if (!organisationFilter.includeOrganisation(it.next()))
							it.remove();
					}
				}

				Display.getDefault().asyncExec(new Runnable()
				{
					public void run()
					{
						removeAll();
						addElements(organisations);
					}
				});

				return Status.OK_STATUS;
			}
		}.schedule();
	}

}
