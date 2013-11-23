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
package org.nightlabs.jfire.prop.datafield;

import java.util.Date;

import org.nightlabs.jfire.prop.PropertySet;

/**
 * Helper class wrapping properties of a certain {@link ImageDataField} instance. It is used for inheritance purposes in the case the contents
 * of an {@link ImageDataField} are inherited between {@link PropertySet}s.<p>
 * Every time the contents of an {@link ImageDataField} are inherited a new instance of this class is created wrapping the data of the image that is associated
 * with the {@link ImageDataField} to be inherited (see {@link ImageDataField#getData()}. This instance is then utilised to set the data (see {@link ImageDataField#setData(Object)}
 * of the corresponding {@link ImageDataField} that is part of the target (child) {@link PropertySet}, i.e. the {@link PropertySet} that will inherit all (or perhaps only a certain
 * part of the) data from the source (mother) {@link PropertySet}.
 * @author Frederik Loeser <!-- frederik [AT] nightlabs [DOT] de -->
 */
public class ImageDataFieldContent {

	/** The content of the image whose data are wrapped by this instance. */
	private byte[] content;
	/** The content encoding of the image whose data are wrapped by this instance. */
	private String contentEncoding;
	/** The content type of the image whose data are wrapped by this instance. */
	private String contentType;
	/** The description for the image whose data are wrapped by this instance. */
	private String description;
	/** The name of the file under which the image whose data are wrapped by this instance is stored. */
	private String fileName;
	/** The timestamp of the file under which the image whose data are wrapped by this instance is stored. */
	private Date fileTimestamp;

	/**
	 * Initialises a new {@link ImageDataFieldContent} instance.
	 * @param content The content of the image whose data are wrapped by this instance.
	 * @param contentEncoding The content encoding of the image whose data are wrapped by this instance.
	 * @param contentType The content type of the image whose data are wrapped by this instance.
	 * @param description The description for the image whose data are wrapped by this instance.
	 * @param filename The name of the file under which the image whose data are wrapped by this instance is stored.
	 * @param fileTimestamp The timestamp of the file under which the image whose data are wrapped by this instance is stored.
	 */
	public ImageDataFieldContent(final byte[] content, final String contentEncoding, final String contentType, final String description,
		final String filename, final Date fileTimestamp) {
		this.content = content;
		this.contentEncoding = contentEncoding;
		this.contentType = contentType;
		this.description = description;
		this.fileName = filename;
		this.fileTimestamp = fileTimestamp;
	}

	/**
	 * @return the content of the image whose data are wrapped by this instance.
	 */
	public byte[] getContent() {
		return content;
	}
	/**
	 * @return the content encoding of the image whose data are wrapped by this instance..
	 */
	public String getContentEncoding() {
		return contentEncoding;
	}
	/**
	 * @return the content type of the image whose data are wrapped by this instance.
	 */
	public String getContentType() {
		return contentType;
	}
	/**
	 * @return the description for the image whose data are wrapped by this instance.
	 */
	public String getDescription() {
		return description;
	}
	/**
	 * @return the name of the file under which the image whose data are wrapped by this instance is stored.
	 */
	public String getFileName() {
		return fileName;
	}
	/**
	 * @return the timestamp of the file under which the image whose data are wrapped by this instance is stored.
	 */
	public Date getFileTimestamp() {
		return fileTimestamp;
	}
}
