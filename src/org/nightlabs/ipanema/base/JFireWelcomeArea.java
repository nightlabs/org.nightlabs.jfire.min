/*
 * Created 	on Nov 4, 2004
 * 					by Alexander Bieber
 *
 */

package org.nightlabs.ipanema.base;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
/**
 * @author Alexander Bieber
 */
public class JFireWelcomeArea extends Composite{
	
	private Label label;

	public JFireWelcomeArea(
			Composite parent, 
			int style 
	) {
		super(parent, style);
		initialize();
	}
	
	private void initialize() {
		GridData gridData4 = new GridData();
		GridLayout gridLayoutStatic = new GridLayout();
		label = new Label(this, SWT.NONE);
		label.setText("Hallo JFire");
		this.setLayout(gridLayoutStatic);
		gridLayoutStatic.numColumns = 1;
		gridData4.horizontalAlignment = org.eclipse.swt.layout.GridData.FILL;
		gridData4.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData4);
		setSize(new org.eclipse.swt.graphics.Point(214,62));
	}
	

}
