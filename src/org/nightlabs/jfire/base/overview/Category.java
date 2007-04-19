package org.nightlabs.jfire.base.overview;

import org.eclipse.swt.graphics.Image;
import org.nightlabs.i18n.I18nText;

/**
 * @author Daniel.Mazurek [at] NightLabs [dot] de
 *
 */
public interface Category 
{
	I18nText getName();
	void setName(I18nText name);
	
	Image getImage();
	void setImage(Image image);
	
	public String getCategoryID();
	public void setCategoryID(String categoryID);
}
