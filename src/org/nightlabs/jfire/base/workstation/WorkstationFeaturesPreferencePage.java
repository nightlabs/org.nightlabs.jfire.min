package org.nightlabs.jfire.base.workstation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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

public class WorkstationFeaturesPreferencePage extends
		AbstractWorkstationConfigModulePreferencePage {
	
	protected static class FeatureTable extends AbstractTableComposite<WorkstationFeature> {
		
		class LabelProvider extends TableLabelProvider {
			public String getColumnText(Object element, int columnIdx) {
				return ((WorkstationFeature)element).getFeatureID();
			}
		}
		
		public FeatureTable(Composite parent) {
			super(parent, SWT.NONE, true, AbstractTableComposite.DEFAULT_STYLE_SINGLE | SWT.CHECK);
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

	@Override
	protected void discardPreferencePageWidgets() {
		// do nothing now
	}

	@Override
	public Class getConfigModuleClass() {
		return WorkstationFeaturesCfMod.class;
	}

	@Override
	protected void updateConfigModule(ConfigModule configModule) {
		if (! (configModule instanceof WorkstationFeaturesCfMod))
			throw new IllegalStateException("The WorkstationFeaturesPreference may only be used with WorkstationFeaturesCfMods!");
		
		WorkstationFeaturesCfMod featuresCfMod = (WorkstationFeaturesCfMod) configModule;
		Map<String, WorkstationFeature> oldFeatures = featuresCfMod.getFeatures();
		
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
	protected void updatePreferencePage(ConfigModule configModule) {
		featureTable.setInput(getAvailableFeatures());
		featureTable.setCheckedElements(new ArrayList<WorkstationFeature>(
				((WorkstationFeaturesCfMod)configModule).getFeatures().values())
				);
		featureTable.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent arg0) {
				setConfigChanged(true);
			}
		});
	}

	protected List<WorkstationFeature> getAvailableFeatures() {
		List<WorkstationFeature> result = new ArrayList<WorkstationFeature>();
		if (!(getCurrentConfigModule() instanceof WorkstationFeaturesCfMod))
			return result;
		WorkstationFeaturesCfMod cfMod = (WorkstationFeaturesCfMod) getCurrentConfigModule();
		result.add(new WorkstationFeature(cfMod, "org.nightlabs.base", "1.0.0"));
		result.add(new WorkstationFeature(cfMod, "org.nightlabs.jfire.base", "1.0.0"));
		result.add(new WorkstationFeature(cfMod, "org.nightlabs.jfire.base.admin", "1.0.0"));
		return result;
	}

	@Override
	protected void setEditable(boolean editable) {
		// TODO Auto-generated method stub
		super.setEditable(editable);
	}

}
