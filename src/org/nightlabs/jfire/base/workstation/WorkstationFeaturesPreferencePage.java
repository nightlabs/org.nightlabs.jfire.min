package org.nightlabs.jfire.base.workstation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.nightlabs.base.composite.XComposite;
import org.nightlabs.base.composite.XComposite.LayoutMode;
import org.nightlabs.base.table.AbstractTableComposite;
import org.nightlabs.base.table.TableContentProvider;
import org.nightlabs.base.table.TableLabelProvider;
import org.nightlabs.jfire.base.config.AbstractWorkstationConfigModulePreferencePage;
import org.nightlabs.jfire.config.ConfigModule;
import org.nightlabs.jfire.workstation.WorkstationFeature;
import org.nightlabs.jfire.workstation.WorkstationFeaturesCfMod;

public class WorkstationFeaturesPreferencePage 
//extends AbstractWorkstationConfigModulePreferencePage<WorkstationFeaturesCfMod> {
extends AbstractWorkstationConfigModulePreferencePage 
{	
	protected static class FeatureTable extends AbstractTableComposite<WorkstationFeature> {
		
		class LabelProvider extends TableLabelProvider {
			public String getColumnText(Object element, int columnIdx) {
				return ((WorkstationFeature)element).getFeatureID();
			}
		}
		
		public FeatureTable(Composite parent) {
			super(parent, SWT.NONE, true, AbstractTableComposite.DEFAULT_STYLE_SINGLE_BORDER | SWT.CHECK);
		}

		@Override
		protected void createTableColumns(TableViewer tableViewer, Table table) {
		}

		@Override
		protected void setTableProvider(TableViewer tableViewer) {
			tableViewer.setContentProvider(new TableContentProvider());
			tableViewer.setLabelProvider(new LabelProvider());
		}
	}

	private FeatureTable featureTable;
	
	@Override
	protected void createPreferencePage(Composite parent) {
		XComposite container = new XComposite(parent, SWT.NONE, LayoutMode.TIGHT_WRAPPER);
		featureTable = new FeatureTable(container);
	}

//	@Override
//	public Class<WorkstationFeaturesCfMod> getConfigModuleClass() {
//		return WorkstationFeaturesCfMod.class;
//	}
	@Override
	public String getConfigModuleClassName() {
		return "org.nightlabs.jfire.workstation.WorkstationFeaturesCfMod";
	}
	
	private static final Set<String> fetchGroups = new HashSet<String>();
	
	@Override
	public Set<String> getConfigModuleFetchGroups() {
		if (fetchGroups.isEmpty()) {
			fetchGroups.addAll(getCommonConfigModuleFetchGroups());
			fetchGroups.add(WorkstationFeaturesCfMod.FETCH_GROUP_THIS_FEATURES);			
		}
		
		return fetchGroups;
	}

	@Override
	public void updateConfigModule() 
	{
//		WorkstationFeaturesCfMod wscfmod = (WorkstationFeaturesCfMod) currentConfigModule;
		WorkstationFeaturesCfMod wscfmod = (WorkstationFeaturesCfMod) getConfigModuleController().getConfigModule();
		Map<String, WorkstationFeature> oldFeatures = wscfmod.getFeatures();
//		Map<String, WorkstationFeature> oldFeatures = currentConfigModule.getFeatures();
		
		// FIXME: JPOX is having Problems clearing the list before adding new entries => Duplicate Key Exeption!
		// 				Workaround in WorkstationFeatureCfMod.JDOpreattach();
		List<WorkstationFeature> newFeaturesList = featureTable.getCheckedElements();
		if (newFeaturesList == null || newFeaturesList.isEmpty()) {
			oldFeatures.clear();
			return;
		}
		
		// TODO: How to handle version numbers?? Currently they are ignored.
		// Remove features that are not in new feature list and remove features from new feature list 
		// which are already active
		Map<String, WorkstationFeature> newFeatures = new HashMap<String, WorkstationFeature>();
		if (oldFeatures != null) {
			List<String> oldFeaturesToRemove = new LinkedList<String>();
			for (WorkstationFeature feature : newFeaturesList)
				newFeatures.put(feature.getFeatureID(), feature);

			// sort out Features already marked and get all features to remove
			for (String oldFeature : oldFeatures.keySet()) {
				if (newFeatures.containsKey(oldFeature))
					newFeatures.remove(oldFeature);
				else
					oldFeaturesToRemove.add(oldFeature);
			}
			
			// remove features not marked anymore
			for (String featureToRemove : oldFeaturesToRemove)
				oldFeatures.remove(featureToRemove);
		} // if (features != null)
		
		// add new features
		for (WorkstationFeature feature : newFeatures.values()) {
			oldFeatures.put(feature.getFeatureID(), feature);
		}
	}

	@Override
//	protected void updatePreferencePage(WorkstationFeaturesCfMod configModule) {
//	protected void updatePreferencePage(ConfigModule configModule) 
	protected void updatePreferencePage()
	{
		WorkstationFeaturesCfMod wscfmod = (WorkstationFeaturesCfMod) getConfigModuleController().getConfigModule();
		featureTable.setInput(getAvailableFeatures());
		featureTable.setCheckedElements(new ArrayList<WorkstationFeature>(
//				configModule.getFeatures().values())
				wscfmod.getFeatures().values())
		);
		featureTable.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent arg0) {
				setConfigChanged(true);
			}
		});
	}

	protected List<WorkstationFeature> getAvailableFeatures() {
		List<WorkstationFeature> result = new ArrayList<WorkstationFeature>();
		if (!(getConfigModuleController().getConfigModule() instanceof WorkstationFeaturesCfMod))
			return result;
		WorkstationFeaturesCfMod cfMod = (WorkstationFeaturesCfMod) getConfigModuleController().getConfigModule();
		result.add(new WorkstationFeature(cfMod, "org.nightlabs.base", "1.0.0"));
		result.add(new WorkstationFeature(cfMod, "org.nightlabs.jfire.base", "1.0.0"));
		result.add(new WorkstationFeature(cfMod, "org.nightlabs.jfire.base.admin", "1.0.0"));
		return result;
	}

}
