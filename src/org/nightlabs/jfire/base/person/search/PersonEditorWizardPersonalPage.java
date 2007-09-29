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

package org.nightlabs.jfire.base.person.search;

import org.nightlabs.base.ui.resource.SharedImages;
import org.nightlabs.jfire.base.JFireBasePlugin;
import org.nightlabs.jfire.base.person.edit.blockbased.special.PersonPersonalDataWizardPage;
import org.nightlabs.jfire.base.resource.Messages;
import org.nightlabs.jfire.person.Person;
import org.nightlabs.jfire.person.PersonStruct;
import org.nightlabs.jfire.prop.id.StructBlockID;

/**
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 *
 */
public class PersonEditorWizardPersonalPage extends PersonPersonalDataWizardPage {

	public static final StructBlockID[] BLOCKIDS = 
		new StructBlockID[] {PersonStruct.PERSONALDATA};
	
	/**
	 * @param title
	 * @param pageName
	 * @param titleImage
	 */
	public PersonEditorWizardPersonalPage(Person person) {
		super(
				PersonEditorWizardPersonalPage.class.getName(),
				Messages.getString("org.nightlabs.jfire.base.person.search.PersonEditorWizardPersonalPage.title"), //$NON-NLS-1$
				person);
		setImageDescriptor(
				SharedImages.getSharedImageDescriptor(
						JFireBasePlugin.getDefault(),
						PersonEditorWizardPersonalPage.class, null, SharedImages.ImageDimension._75x70));

		// TODO: Add editor struct block registry as throw-away object

//		EditorStructBlockRegistry.sharedInstance().addEditorStructBlocks(
//			LegalEntityEditorWizard.WIZARD_EDITOR_DOMAIN,
//			this.getClass().getName(),
//			BLOCKIDS
//		);
	}

}
