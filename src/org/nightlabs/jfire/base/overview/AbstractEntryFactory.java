package org.nightlabs.jfire.base.overview;

import org.eclipse.swt.graphics.Image;
import org.nightlabs.i18n.I18nText;
import org.nightlabs.i18n.I18nTextBuffer;

/**
 * The abstract base implementation of {@link EntryFactory}
 * @author Daniel.Mazurek [at] NightLabs [dot] de
 *
 */
public abstract class AbstractEntryFactory 
implements EntryFactory 
{

	public AbstractEntryFactory() {
		super();
	}

	private Image image;
	public Image getImage() {
		return image;
	}

	private I18nText name = new I18nTextBuffer();
	public I18nText getName() {
		return name;
	}

	public void setImage(Image image) {
		this.image = image;
	}

	public void setName(I18nText name) {
		this.name = name;
	}

}
