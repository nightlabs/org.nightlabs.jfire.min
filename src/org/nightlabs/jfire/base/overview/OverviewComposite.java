package org.nightlabs.jfire.base.overview;

import org.eclipse.nebula.widgets.pshelf.PShelf;
import org.eclipse.nebula.widgets.pshelf.PShelfItem;
import org.eclipse.nebula.widgets.pshelf.RedmondShelfRenderer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.nightlabs.base.composite.XComposite;

/**
 * @author Daniel.Mazurek [at] NightLabs [dot] de
 *
 */
public class OverviewComposite 
extends XComposite 
{
	public OverviewComposite(Composite parent, int style, LayoutMode layoutMode,
			LayoutDataMode layoutDataMode) 
	{
		super(parent, style, layoutMode, layoutDataMode);
		createComposite(this);
	}

	public OverviewComposite(Composite parent, int style) {
		super(parent, style);
		createComposite(this);
	}

	private PShelf shelf;
	protected void createComposite(Composite parent) 
	{
		parent.setLayout(new FillLayout());

		shelf = new PShelf(parent, SWT.NONE);
		shelf.setRenderer(new RedmondShelfRenderer());
//		RedmondShelfRenderer renderer = new RedmondShelfRenderer();
//		renderer.setGradient1(new Color(null, 198, 223, 225));
//		renderer.setGradient1(new Color(null, 255, 255, 255));
//		renderer.setHoverGradient1(new Color(null, 198, 223, 225));
//		renderer.setHoverGradient2(new Color(null, 255, 255, 255));
//		renderer.setHover(true);
//		shelf.setRenderer(renderer);

		shelf.setLayoutData(new GridData(GridData.FILL_BOTH));

		for (Category category: OverviewRegistry.sharedInstance().getCategoriesWithEntries()) {
			PShelfItem categoryItem = new PShelfItem(shelf,SWT.NONE);
			categoryItem.setData(category);
			categoryItem.setText(category.getCategoryFactory().getName());
			categoryItem.setImage(category.getCategoryFactory().getImage());
			categoryItem.getBody().setLayout(new FillLayout());
			category.createCategoryComposite(categoryItem.getBody());
		}
	}
}
