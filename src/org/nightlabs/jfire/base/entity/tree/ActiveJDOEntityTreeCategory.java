package org.nightlabs.jfire.base.entity.tree;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.nightlabs.base.ui.entity.tree.EntityTreeCategory;
import org.nightlabs.base.ui.entity.tree.IEntityTreeCategoryContentConsumer;
import org.nightlabs.base.ui.tree.TreeContentProvider;
import org.nightlabs.jfire.base.jdo.ActiveJDOObjectController;
import org.nightlabs.jfire.base.jdo.JDOObjectsChangedEvent;
import org.nightlabs.jfire.base.resource.Messages;
import org.nightlabs.jfire.jdo.notification.IJDOLifecycleListenerFilter;
import org.nightlabs.progress.ProgressMonitor;

public abstract class ActiveJDOEntityTreeCategory<JDOObjectID, JDOObject>
extends EntityTreeCategory
{
	protected class ContentProvider
	extends TreeContentProvider
	{
		public Object[] getElements(Object inputElement)
		{
			List<JDOObject> jdoObjects = getActiveJDOObjectController().getJDOObjects();
			if (jdoObjects == null)
				return new String[] { Messages.getString("org.nightlabs.jfire.base.entity.tree.ActiveJDOEntityTreeCategory.loadingData") }; //$NON-NLS-1$

			return jdoObjects.toArray();
		}
	}

	protected class ActiveEntityTreeCategoryJDOObjectController
	extends ActiveJDOObjectController<JDOObjectID, JDOObject>
	{
		protected Class getJDOObjectClass()
		{
			return ActiveJDOEntityTreeCategory.this.getJDOObjectClass();
		}

		@Override
		protected IJDOLifecycleListenerFilter createJDOLifecycleListenerFilter()
		{
			IJDOLifecycleListenerFilter filter = ActiveJDOEntityTreeCategory.this.createJDOLifecycleListenerFilter();
			if (filter != null)
				return filter;

			return super.createJDOLifecycleListenerFilter();
		}

		@Override
		protected void onJDOObjectsChanged(JDOObjectsChangedEvent<JDOObjectID, JDOObject> event)
		{
			fireEntityTreeCategoryChange();
		}

		protected Collection<JDOObject> retrieveJDOObjects(Set<JDOObjectID> objectIDs, ProgressMonitor monitor)
		{
			return ActiveJDOEntityTreeCategory.this.retrieveJDOObjects(objectIDs, monitor);
		}

		protected Collection<JDOObject> retrieveJDOObjects(ProgressMonitor monitor)
		{
			return ActiveJDOEntityTreeCategory.this.retrieveJDOObjects(monitor);
		}

		protected void sortJDOObjects(List<JDOObject> objects)
		{
			ActiveJDOEntityTreeCategory.this.sortJDOObjects(objects);
		}
	}

	/**
	 * The implementation of {@link ActiveJDOObjectController} within this Category
	 * delegates to this method. If this method returns <code>null</code>, the default (super)
	 * implementation of the controller's <code>createJDOLifecycleListenerFilter()</code>
	 * method will be used to create the filter.
	 * <p>
	 * If this method returns an instance (i.e. not <code>null</code>), the controller won't
	 * call <code>super.createJDOLifecycleListenerFilter()</code>, but instead use the
	 * instance returned by this method.
	 * </p>
	 *
	 * @see ActiveJDOObjectController#createJDOLifecycleListenerFilter()
	 */
	protected IJDOLifecycleListenerFilter createJDOLifecycleListenerFilter()
	{
		return null;
	}

	/**
	 * @see ActiveJDOObjectController#getJDOObjectClass()
	 */
	protected abstract Class getJDOObjectClass();
	/**
	 * @see ActiveJDOObjectController#retrieveJDOObjects(Set<JDOObjectID> objectIDs, IProgressMonitor monitor)
	 */
	protected abstract Collection<JDOObject> retrieveJDOObjects(Set<JDOObjectID> objectIDs, ProgressMonitor monitor);
	/**
	 * @see ActiveJDOObjectController#retrieveJDOObjects(ProgressMonitor monitor)
	 */
	protected abstract Collection<JDOObject> retrieveJDOObjects(ProgressMonitor monitor);
	/**
	 * @see ActiveJDOObjectController#sortJDOObjects(List<JDOObject> objects)
	 */
	protected abstract void sortJDOObjects(List<JDOObject> objects);

	private ActiveJDOObjectController<JDOObjectID, JDOObject> activeJDOObjectController;
	protected ActiveJDOObjectController<JDOObjectID, JDOObject> getActiveJDOObjectController()
	{
		if (activeJDOObjectController == null)
			activeJDOObjectController = new ActiveEntityTreeCategoryJDOObjectController();

		return activeJDOObjectController;
	}

	private Set<IEntityTreeCategoryContentConsumer> contentConsumers = new HashSet<IEntityTreeCategoryContentConsumer>();

	protected ITreeContentProvider _createContentProvider(final IEntityTreeCategoryContentConsumer contentConsumer)
	{
		contentConsumers.add(contentConsumer);
		contentConsumer.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e)
			{
				contentConsumers.remove(contentConsumer);
				if (contentConsumers.isEmpty() && activeJDOObjectController != null) {
					activeJDOObjectController.close();
					activeJDOObjectController = null;
				}
			}
		});
		return new ContentProvider();
	}
}
