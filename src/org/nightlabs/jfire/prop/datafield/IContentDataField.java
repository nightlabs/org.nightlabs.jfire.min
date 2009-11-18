/**
 *
 */
package org.nightlabs.jfire.prop.datafield;

import org.nightlabs.jfire.prop.DataField;

/**
 * Common interface for all subclasses of {@link DataField} that
 * in some way store binary content (This could be for example images or other documents).
 *
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 *
 */
public interface IContentDataField {

	/**
	 * Constant for content encoding "deflate".
	 */
	public static final String CONTENT_ENCODING_DEFLATE = "deflate";
	/**
	 * Constant for content encoding "plain" meaning no encoding, but raw data.
	 */
	public static final String CONTENT_ENCODING_PLAIN = "plain";

	/**
	 * Returns the content type of the data stored by this field
	 * according to the MIME pattern of content type naming.
	 *
	 * @return Content type in MIME naming.
	 */
	String getContentType();

	/**
	 * Return the content encoding (i.e. in which way the content is stored) of the data stored by this field
	 * according to the MIME pattern of content encoding naming.
	 *
	 * @return Content encoding in MIME naming.
	 */
	String getContentEncoding();

	/**
	 * Returns a description of the content of this field.
	 *
	 * @return A description of the content of this field.
	 */
	// TODO Why is this not multilingual? Is it by purpose or accidentally? Marco.
	String getDescription();

	/**
	 * Returns the actual content still encoded in {@link #getContentEncoding()}.
	 * @return The actual content still encoded in {@link #getContentEncoding()}.
	 */
	byte[] getContent();

	/**
	 * Returns the content of this data field but already decoded.
	 * @return The content of this data field but already decoded.
	 */
	byte[] getPlainContent();

}
