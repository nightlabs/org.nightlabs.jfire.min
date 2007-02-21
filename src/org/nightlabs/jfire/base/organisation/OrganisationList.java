package org.nightlabs.jfire.base.organisation;

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

public class OrganisationList
		extends ListComposite<Organisation>
{
	public static final String[] FETCH_GROUPS_ORGANISATION = {
		FetchPlan.DEFAULT
	};

	public OrganisationList(Composite parent)
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

		new Job("Load Organisations") {
			@Implement
			protected IStatus run(IProgressMonitor monitor)
			{
				final List<Organisation> organisations = OrganisationDAO.sharedInstance().getOrganisations(
						FETCH_GROUPS_ORGANISATION, NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT, monitor);

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
