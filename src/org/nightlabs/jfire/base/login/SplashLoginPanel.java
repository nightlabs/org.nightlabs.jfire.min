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

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.SpringLayout;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.log4j.Logger;

import org.nightlabs.config.Config;
import org.nightlabs.config.ConfigException;
import org.nightlabs.jfire.base.resource.Messages;
import org.nightlabs.rcp.splash.SplashScreen;
import org.nightlabs.util.SpringUtilities;

/**
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public class SplashLoginPanel extends JPanel 
{
	/**
	 * LOG4J logger used by this class
	 */
	private static final Logger logger = Logger.getLogger(SplashLoginPanel.class);

	private JFireLoginContext loginContext;
	private LoginConfigModule loginConfigModule;
	private boolean workOffline = false;

	private JPanel fieldWrapperPanel;

	private JPanel messagePanel;
	private JLabel labelMessage;

	private JPanel editPanel; 
	private JLabel labelUsername;
	private JTextField textUsername;
	private JLabel labelPassword;
	private JPasswordField textPassword;
	private JLabel labelWorkstation;
	private JTextField textWorkstation;

	private JPanel detailWrapper;
	private JPanel detailPanel;
	private JLabel labelOrganisationID;
	private JTextField textOrganisationID;
	private JLabel labelSecurityProtocol;
	private JTextField textSecurityProtocol;
	private JLabel labelServerURL;
	private JTextField textServerURL;
	private JLabel labelInitialContextFactory;
	private JTextField textInitialContextFactory;

	private JPanel checkBoxPanel;
	private JCheckBox checkBoxSaveSettings;

	private JPanel buttonPanel;
	private JButton buttonLogin;
	private JButton buttonQuit;
	private JButton buttonWorkOffline;
	private JToggleButton buttonShowDetails;

	private JPanel southPanel;

	private LoginConfigModule persistentLoginModule;

	public SplashLoginPanel(JFireLoginContext loginContext, LoginConfigModule loginConfigModule) {
		super();
		this.loginContext = loginContext;
		this.loginConfigModule = loginConfigModule;

		this.setFont(this.getFont().deriveFont(Font.ROMAN_BASELINE));

		try {
			persistentLoginModule = ((LoginConfigModule)Config.sharedInstance().createConfigModule(LoginConfigModule.class));
		} catch (ConfigException e) {
			throw new RuntimeException(e);
		}
		setOpaque(false);
		this.setLayout(new BorderLayout());
		
		LoginConfiguration lastLoginConfiguration = persistentLoginModule.getLastLoginConfiguration();
		if (lastLoginConfiguration == null)
			lastLoginConfiguration = new LoginConfiguration();

		fieldWrapperPanel = new JPanel();
		fieldWrapperPanel.setOpaque(false);
		fieldWrapperPanel.setLayout(new BorderLayout());
		add(fieldWrapperPanel, BorderLayout.NORTH);

		editPanel = new JPanel();
		editPanel.setOpaque(false);
		editPanel.setLayout(new SpringLayout());

		messagePanel = new JPanel();
		messagePanel.setOpaque(false);
		messagePanel.setLayout(new FlowLayout(FlowLayout.LEADING, 10, 10));
		labelMessage = new JLabel(Messages.getString("login.SplashLoginPanel.pleaseLogin")); //$NON-NLS-1$
		messagePanel.add(labelMessage);
		fieldWrapperPanel.add(messagePanel, BorderLayout.NORTH);

		fieldWrapperPanel.add(editPanel, BorderLayout.SOUTH);

		labelUsername = new JLabel(Messages.getString("login.SplashLoginPanel.username"), JLabel.LEADING); //$NON-NLS-1$
		editPanel.add(labelUsername);
		textUsername = new JTextField(15);
		labelUsername.setLabelFor(textUsername);
		textUsername.setText(lastLoginConfiguration.getUserID());
		editPanel.add(textUsername);

		labelPassword = new JLabel(Messages.getString("login.SplashLoginPanel.password"), JLabel.LEADING); //$NON-NLS-1$
		editPanel.add(labelPassword);
		textPassword = new JPasswordField(15);
		labelPassword.setLabelFor(textPassword);
		textPassword.setText(""); //$NON-NLS-1$
		textPassword.setEchoChar('*');
		editPanel.add(textPassword);

		labelWorkstation = new JLabel(Messages.getString("login.SplashLoginPanel.workstation"), JLabel.LEADING); //$NON-NLS-1$
		editPanel.add(labelWorkstation);
		textWorkstation = new JTextField(15);
		labelWorkstation.setLabelFor(textWorkstation);
		textWorkstation.setText(lastLoginConfiguration.getWorkstationID());
		editPanel.add(textWorkstation);

		SpringUtilities.makeCompactGrid(
				editPanel,
				3, 2,
				10, 20,
				10, 10
		);				
		detailWrapper = new JPanel();
		detailWrapper.setOpaque(false);
		detailWrapper.setLayout(new BorderLayout());

		detailPanel = new JPanel();
		detailPanel.setOpaque(false);
		detailPanel.setLayout(new SpringLayout());
		detailWrapper.add(detailPanel, BorderLayout.NORTH);

		labelOrganisationID = new JLabel(Messages.getString("login.SplashLoginPanel.organisation"), JLabel.LEADING); //$NON-NLS-1$
		detailPanel.add(labelOrganisationID);
		textOrganisationID = new JTextField(15);
		labelOrganisationID.setLabelFor(textOrganisationID);
		textOrganisationID.setText(lastLoginConfiguration.getOrganisationID());
		detailPanel.add(textOrganisationID);

		labelSecurityProtocol = new JLabel(Messages.getString("login.SplashLoginPanel.securityProtocol"), JLabel.LEADING); //$NON-NLS-1$
//		detailPanel.add(labelSecurityProtocol);
		textSecurityProtocol = new JTextField(15);
		labelSecurityProtocol.setLabelFor(textSecurityProtocol);
		textSecurityProtocol.setText(lastLoginConfiguration.getSecurityProtocol());
//		detailPanel.add(textSecurityProtocol);

		labelServerURL = new JLabel(Messages.getString("login.SplashLoginPanel.serverURL"), JLabel.LEADING); //$NON-NLS-1$
		detailPanel.add(labelServerURL);
		textServerURL = new JTextField(15);
		labelServerURL.setLabelFor(textServerURL);
		textServerURL.setText(lastLoginConfiguration.getServerURL());
		detailPanel.add(textServerURL);

		labelInitialContextFactory = new JLabel(Messages.getString("login.SplashLoginPanel.initialContextFactory"), JLabel.LEADING); //$NON-NLS-1$
		detailPanel.add(labelInitialContextFactory);		
		textInitialContextFactory = new JTextField(15);
		labelInitialContextFactory.setLabelFor(textInitialContextFactory);
		textInitialContextFactory.setText(lastLoginConfiguration.getInitialContextFactory());
		detailPanel.add(textInitialContextFactory);
		SpringUtilities.makeCompactGrid(
				detailPanel,
				3, 2,
				10, 20,
				10, 10
		);				
		checkBoxPanel = new JPanel();
		checkBoxPanel.setOpaque(false);
		checkBoxPanel.setLayout(new FlowLayout(FlowLayout.LEADING));
		checkBoxSaveSettings = new JCheckBox(Messages.getString("login.SplashLoginPanel.saveSettings"), false); //$NON-NLS-1$
		checkBoxSaveSettings.setOpaque(false);
		checkBoxPanel.add(checkBoxSaveSettings);
		detailWrapper.add(checkBoxPanel, BorderLayout.SOUTH);


		setPreferredSize(SplashScreen.getSplashImageDimension());
//		setMinimumSize(new Dimension(400,400));
//		setSize(new Dimension(400,400));
//		setMaximumSize(new Dimension(400, 400));

		southPanel = new JPanel();
		southPanel.setOpaque(false);
		add(southPanel, BorderLayout.SOUTH);
		southPanel.setLayout(new BorderLayout());

		buttonPanel = new JPanel();
		buttonPanel.setOpaque(false);
		buttonPanel.setAlignmentX(JPanel.RIGHT_ALIGNMENT);
		buttonPanel.setLayout(new FlowLayout(FlowLayout.TRAILING));
		southPanel.add(buttonPanel, BorderLayout.SOUTH);
		buttonLogin = new JButton(Messages.getString("login.SplashLoginPanel.login")); //$NON-NLS-1$
		buttonPanel.add(buttonLogin);
		buttonLogin.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				synchronized (SplashScreen.getMutex()) {
					SplashScreen.getMutex().notifyAll();
				}
			}
		});

		buttonWorkOffline = new JButton(Messages.getString("login.SplashLoginPanel.workOffline")); //$NON-NLS-1$
		buttonPanel.add(buttonWorkOffline);
		buttonWorkOffline.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				workOffline = true;
				synchronized (SplashScreen.getMutex()) {
					SplashScreen.getMutex().notifyAll();
				}
			}
		});

		buttonQuit = new JButton(Messages.getString("login.SplashLoginPanel.quit")); //$NON-NLS-1$
		buttonPanel.add(buttonQuit);
		buttonQuit.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});
		buttonShowDetails = new JToggleButton(Messages.getString("login.SplashLoginPanel.details")); //$NON-NLS-1$
		buttonPanel.add(buttonShowDetails);
		buttonShowDetails.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (buttonShowDetails.isSelected()) {
					fieldWrapperPanel.remove(editPanel);
					fieldWrapperPanel.add(detailWrapper, BorderLayout.SOUTH);
				}
				else {
					fieldWrapperPanel.remove(detailWrapper);
					fieldWrapperPanel.add(editPanel, BorderLayout.SOUTH);
				}
				setComponentFont(SplashLoginPanel.this, SplashLoginPanel.this.getFont());
				SplashScreen.setSplashPanel(SplashLoginPanel.this);
			}
		});

		setComponentFont(this, this.getFont());
	}

	/**
	 * Assigns the values in the GUI to the LoginConfigModule
	 * and JFireLoginContext passed in the constructor.
	 */
	public void assignLoginValues() {
		
		String userID = textUsername.getText();
		String organisationID = textOrganisationID.getText();
		String securityProtocol = textSecurityProtocol.getText();
		String serverURL = textServerURL.getText();
		String initialContextFactory = textInitialContextFactory.getText();
		String workstationID = textWorkstation.getText();
		
//		loginConfigModule.setUserID(textUsername.getText());
//		loginConfigModule.setOrganisationID(textOrganisationID.getText());
//		loginConfigModule.setSecurityProtocol(textSecurityProtocol.getText());
//		loginConfigModule.setServerURL(textServerURL.getText());
//		loginConfigModule.setInitialContextFactory(textInitialContextFactory.getText());
//		loginConfigModule.setWorkstationID(textWorkstation.getText());
		
		loginConfigModule.setCurrentLoginConfiguration(userID, workstationID, organisationID, serverURL, initialContextFactory, securityProtocol);

		if (checkBoxSaveSettings.isSelected()) {
			try {
				loginConfigModule.persistCurrentConfiguration();
				
				BeanUtils.copyProperties(persistentLoginModule, loginConfigModule);
				persistentLoginModule.setChanged();
			} catch (Exception e) {
				logger.error("Saving config failed!", e); //$NON-NLS-1$
			}
		}
		
		LoginConfiguration loginConfig = loginConfigModule.getCurrentLoginConfiguration();

		loginContext.setCredentials(loginConfig.getUserID(), loginConfig.getOrganisationID(), new String(textPassword.getPassword()));
	}

	public void setErrMessage(String message) {
		labelMessage.setText(message);
		SplashScreen.setSplashPanel(SplashLoginPanel.this);
	}

	private void setComponentFont(JComponent component, Font font) {
		component.setFont(font);
		Component[] components = component.getComponents();
		for (int i = 0; i < components.length; i++) {
			if (components[i] instanceof JComponent)
				setComponentFont((JComponent)components[i], font);
			else {
				components[i].setFont(font);
			}
		}
	}

	public boolean isWorkOffline()
	{
		return workOffline;
	}

	public void paintComponent(Graphics g)
	{
		g.setColor(new Color(1.0f, 1.0f, 1.0f, 0.5f));
		g.fillRect(0, 0, getWidth(), getHeight());
	}
}
