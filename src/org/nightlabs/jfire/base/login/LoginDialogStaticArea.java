/* *****************************************************************************
 * JFire - it's hot - Free ERP System - http://jfire.org                       *
 * Copyright (C) 2004-2005 NightLabs - http://NightLabs.org                    *
 *                                                                             *
 * This library is free software; you can redistribute it and/or               *
 * modify it under the terms of the GNU Lesser General Public                  *
 * License as published by the Free Software Foundation; either                *
 * version 2.1 of the License, or (at your option) any later version.          *
 *                                                                             *
 * This library is distributed in the hope that it will be useful,             *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of              *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU           *
 * Lesser General Public License for more details.                             *
 *                                                                             *
 * You should have received a copy of the GNU Lesser General Public            *
 * License along with this library; if not, write to the                       *
 *     Free Software Foundation, Inc.,                                         *
 *     51 Franklin St, Fifth Floor,                                            *
 *     Boston, MA  02110-1301  USA                                             *
 *                                                                             *
 * Or get it online :                                                          *
 *     http://opensource.org/licenses/lgpl-license.php                         *
 *                                                                             *
 *                                                                             *
 ******************************************************************************/

package org.nightlabs.jfire.base.login;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import org.nightlabs.jfire.base.JFireBasePlugin;
/**
 * @author Alexander Bieber
 */
public class LoginDialogStaticArea extends Composite {

	/**
	 * Inner class for the Composite that displays
	 * possible error messages to the user.
	 * @author Alexander Bieber
	 */
	private class ErrorMessageComposite extends Composite {
		private Label labelIcon;
		private Label labelErrMessage;
		/**
		 * @param parent
		 * @param style
		 */
		public ErrorMessageComposite(Composite parent, int style) {
			super(parent, style);
			this.setLayout(new RowLayout());
			labelIcon = new Label(this,SWT.PUSH);
			labelIcon.setImage(Dialog.getImage(Dialog.DLG_IMG_MESSAGE_ERROR));
			labelErrMessage = new Label(this,SWT.PUSH);
			labelErrMessage.setText("");
		}
		
		public void setErrMsg(String message) {
			labelErrMessage.setText(message);
		}		
	}
	
	/*
	 * ******** END inner class ErrorMessageComposite *********  
	 */
	
	private Label labelUserID = null;
	private Text textUserID = null;
	private Label labelPassword = null;
	private Text textPassword = null;
	private LoginConfigModule runtimeLoginConfigModule = null;
	private ErrorMessageComposite errMessage = null;
	public LoginDialogStaticArea(
			Composite parent, 
			int style, 
			LoginConfigModule loginConfigModule
	) {
		super(parent, style);
		this.runtimeLoginConfigModule = loginConfigModule;
		initialize();
		initValues();
	}
	
	private void initValues() {
		textUserID.setText(runtimeLoginConfigModule.getUserID());
		textUserID.setFocus();
	}
	
	GridData gridDataErr = new GridData(GridData.FILL_HORIZONTAL);
	
	private void initialize() {
		GridData gridData5 = new GridData();
		GridData gridData4 = new GridData();
		GridLayout gridLayoutStatic = new GridLayout();
		errMessage = new ErrorMessageComposite(this, SWT.NONE);
		labelUserID = new Label(this, SWT.NONE);
		textUserID = new Text(this, SWT.BORDER);
		labelPassword = new Label(this, SWT.NONE);
		textPassword = new Text(this, SWT.BORDER);
		this.setLayout(gridLayoutStatic);
		gridLayoutStatic.numColumns = 2;
		labelUserID.setText(JFireBasePlugin.getResourceString("login.label.user"));
		gridData4.horizontalAlignment = org.eclipse.swt.layout.GridData.FILL;
		gridData4.grabExcessHorizontalSpace = true;
		textUserID.setLayoutData(gridData4);
		labelPassword.setText(JFireBasePlugin.getResourceString("login.label.pass"));
		gridData5.grabExcessHorizontalSpace = true;
		gridData5.horizontalAlignment = org.eclipse.swt.layout.GridData.FILL;
		textPassword.setLayoutData(gridData5);
		textPassword.setEchoChar('*');
//		setSize(new org.eclipse.swt.graphics.Point(150,62));
		gridDataErr.horizontalSpan = 2;
		gridDataErr.verticalAlignment = GridData.BEGINNING;
		gridDataErr.grabExcessHorizontalSpace = true;
		gridDataErr.heightHint = 0;
		errMessage.setLayoutData(gridDataErr);
		errMessage.setErrMsg("");
	}
	
	public void setErrMessage(String errorMsg) {
		setErrMessage(errorMsg,2);
	}
	public void setErrMessage(String errorMsg, int lineNo) {
		if (errorMsg.equals("")) {
			errMessage.setErrMsg("");
			gridDataErr.heightHint = 0;
		}
		else {
			errMessage.setErrMsg(errorMsg);
			if (lineNo < 2)
				lineNo = 2;
			gridDataErr.heightHint = 35 + 15*(lineNo-2);
		}
		if (LoginDialog.getSharedInstace() != null) {
			LoginDialog.getSharedInstace().doRelayout();
		}
		
	}
	
	public Label getLabelPassword() {
		return labelPassword;
	}
	public Label getLabelUserID() {
		return labelUserID;
	}
	public Text getTextPassword() {
		return textPassword;
	}
	public Text getTextUserID() {
		return textUserID;
	}
}  //  @jve:decl-index=0:visual-constraint="10,10"
