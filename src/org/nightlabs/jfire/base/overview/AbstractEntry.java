package org.nightlabs.jfire.base.overview;

import org.eclipse.swt.graphics.Image;
import org.nightlabs.i18n.I18nText;
import org.nightlabs.i18n.I18nTextBuffer;

/**
 * @author Daniel.Mazurek [at] NightLabs [dot] de
 *
 */
public abstract class AbstractEntry 
implements Entry 
{

	public AbstractEntry() {		
	}

	private Image image;
	public Image getImage() {
		return image;
	}
	public void setImage(Image image) {
		this.image = image;
	}
	
	private I18nText name = new I18nTextBuffer();
	public I18nText getName() {
		return name;
	}
	public void setName(I18nText name) {
		this.name = name;
	}

}
