package org.nightlabs.jfire.prop.datafield;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterOutputStream;

import org.apache.log4j.Logger;
import org.nightlabs.io.DataBuffer;
import org.nightlabs.jdo.ObjectIDUtil;
import org.nightlabs.jfire.prop.DataBlock;
import org.nightlabs.jfire.prop.DataField;
import org.nightlabs.jfire.prop.PropertySet;
import org.nightlabs.jfire.prop.StructField;
import org.nightlabs.util.IOUtil;

import javax.jdo.annotations.FetchGroups;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.PersistenceModifier;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Column;
import javax.jdo.annotations.IdentityType;

/**
 * {@link DataField} that stores an image in binary form.
 *
 * @jdo.persistence-capable
 * 		identity-type="application"
 *    persistence-capable-superclass="org.nightlabs.jfire.prop.DataField"
 *    detachable="true"
 *    table="JFireBase_Prop_ImageDataField"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.fetch-group name="FetchGroupsProp.fullData" fetch-groups="default" fields="content,fileTimestamp,fileName"
 *
 * @author Tobias Langner <!-- tobias[dot]langner[at]nightlabs[dot]de -->
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 */
@PersistenceCapable(
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireBase_Prop_ImageDataField")
@FetchGroups(
	@FetchGroup(
		fetchGroups={"default"},
		name="FetchGroupsProp.fullData",
		members={@Persistent(name="content"), @Persistent(name="fileTimestamp"), @Persistent(name="fileName")})
)
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class ImageDataField
extends DataField
implements IContentDataField
{
	private static final long serialVersionUID = 20090116L;

	private static Logger logger = Logger.getLogger(ImageDataField.class);

	/**
	 * TODO: This info/functionality should come from somewhere else.
	 */
	public static final class ContentTypeUtil {

		public static final String IMAGE_PNG = "image/png";
		public static final String IMAGE_GIF = "image/gif";
		public static final String IMAGE_JPEG = "image/jpeg";
		public static final String IMAGE_PCX = "image/pcx";

		// first entry in a list of equal content types is default extension
		private static String[][] contentTypes = new String[][] {
			{ IMAGE_JPEG,                  ".jpg" },
			{ IMAGE_JPEG,                  ".jpeg" },
			{ IMAGE_GIF,                   ".gif" },
			{ IMAGE_PNG,                   ".png" },
			{ IMAGE_PCX,                   ".pcx" }
		};

		public static String getFileExtension(String contentType)
		{
			if(contentType != null) {
				String _contentType = contentType.toLowerCase();
				for (String[] pair : contentTypes) {
					if(pair[0].equals(_contentType))
							return pair[1];
				}
			}
			return ".bin";
		}

		public static String getContentType(String fileName)
		{
			if(fileName != null) {
				String _fileName = fileName.toLowerCase();
				for (String[] pair : contentTypes) {
					if(_fileName.endsWith(pair[1]))
							return pair[0];
				}
			}
			return "application/unknown";
		}
	}

	/**
	 * @jdo.field persistence-modifier="persistent"
	 * @jdo.column sql-type="BLOB"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	@Column(sqlType="BLOB")
	private byte[] content;

	/** @jdo.field persistence-modifier="persistent" */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private Date fileTimestamp;

	/** @jdo.field persistence-modifier="persistent" */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private String fileName;

	/** @jdo.field persistence-modifier="persistent" */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private String contentType;

	/** @jdo.field persistence-modifier="persistent" */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private String contentEncoding;

	/** @jdo.field persistence-modifier="persistent" */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private String description;

	/**
	 * Create a new {@link ImageDataField} for the given {@link DataBlock}
	 * that represents the given {@link StructField}.
	 *
	 * @param dataBlock The {@link DataBlock} the new {@link ImageDataField} will be part of.
	 * @param structField The {@link StructField} the new {@link ImageDataField} represents in the data structure.
	 */
	public ImageDataField(DataBlock dataBlock, StructField<ImageDataField> structField) {
		super(dataBlock, structField);
	}

	/**
	 * Used for cloning.
	 */
	protected ImageDataField(String organisationID, long propertySetID, ImageDataField imageDataField) {
		super(organisationID, propertySetID, imageDataField);
	}

	/*
	 * (non-Javadoc)
	 * @see org.nightlabs.jfire.prop.DataField#cloneDataField(org.nightlabs.jfire.prop.PropertySet)
	 */
	@Override
	public DataField cloneDataField(PropertySet propertySet) {
		ImageDataField newField = new ImageDataField(propertySet.getOrganisationID(), propertySet.getPropertySetID(), this);
		newField.fileName = this.fileName;
		newField.fileTimestamp = this.fileTimestamp;

		if (this.content != null) {
			newField.content = new byte[this.content.length];
			for (int i = 0; i < this.content.length; i++) {
				newField.content[i] = this.content[i];
			}
//			newField.content = this.content.clone();
		}
		return newField;
	}

	/*
	 * (non-Javadoc)
	 * @see org.nightlabs.jfire.prop.DataField#isEmpty()
	 */
	@Override
	public boolean isEmpty() {
		return content == null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.nightlabs.jfire.prop.datafield.IContentDataField#getContentType()
	 */
	@Override
	public String getContentType() {
		return contentType;
	}

	/**
	 * Set the content-type of this {@link ImageDataField}.
	 * @param contentType The content-type to set.
	 */
	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	/**
	 * Set the content encoding of this {@link ImageDataField}
	 * @param contentEncoding The encoding to set.
	 */
	public void setContentEncoding(String contentEncoding) {
		this.contentEncoding = contentEncoding;
	}

	/*
	 * (non-Javadoc)
	 * @see org.nightlabs.jfire.prop.datafield.IContentDataField#getContentEncoding()
	 */
	@Override
	public String getContentEncoding() {
		return contentEncoding;
	}

	/**
	 * Get the binary content of this {@link ImageDataField}.
	 * Note, that the content might be encoded, see {@link #getContentEncoding()}.
	 */
	public byte[] getContent() {
		return content;
	}
	
	@Override
	public byte[] getPlainContent() {
		if (isEmpty())
			return null;
		if (IContentDataField.CONTENT_ENCODING_PLAIN.equals(getContentEncoding())) {
			return getContent();
		} else if (IContentDataField.CONTENT_ENCODING_DEFLATE.equals(getContentEncoding())) {
			ByteArrayOutputStream out = new ByteArrayOutputStream(getContent().length);
			InflaterOutputStream inflater = new InflaterOutputStream(out);
			try {
				inflater.write(getContent());
			} catch (IOException e) {
				throw new RuntimeException("Could not decode image data", e);
			}
			return out.toByteArray();
		}
		throw new IllegalStateException("This ImageDataField was encoded with an unknown encoding type " + getContentEncoding() + ". Can't decode the content");
	}

	/**
	 * Set the content. Note, that the content might be encoded, see {@link #setContentEncoding()}.
	 * @param content The content to set
	 */
	public void setContent(byte[] content)
	{
		this.content = content;
	}

	/**
	 * Clear this image data field.
	 */
	public void clear()
	{
		this.content = null;
		this.fileTimestamp = null;
		this.fileName = null;
		this.contentEncoding = null;
		this.contentType = null;
		this.description = null;
	}

	public void saveToStream(OutputStream out) throws IOException {
		OutputStream stream = out;
		if (IContentDataField.CONTENT_ENCODING_DEFLATE.equals(getContentEncoding())) {
			stream = new InflaterOutputStream(out);
		}
		BufferedOutputStream buf = new BufferedOutputStream(stream);
		buf.write(getContent());
		buf.flush();
		// I don't close any streams here as they are all delegating
		// to the parameter stream and won't allocate resources themselves, I hope ;-)
	}

	public void saveToFile(File file) throws IOException {
		FileOutputStream out = new FileOutputStream(file);
		try {
			saveToStream(out);
		} finally {
			out.close();
		}
	}

	public File saveToDir(File dir) throws IOException {
		String fName = getFileName();
		if (fName == null) {
			fName =
				"no_name_" +
				getOrganisationID() + "_" + ObjectIDUtil.longObjectIDFieldToString(getPropertySetID()) + "_" +
				getStructBlockOrganisationID() + "_" + getStructBlockID() + "_" +
				getStructFieldOrganisationID() + "_" + getStructFieldID() + "." + ContentTypeUtil.getFileExtension(getContentType());
		}
		File saveFile = new File(dir, fName);
		saveToFile(saveFile);
		return saveFile;
	}

	/**
	 * Load the binary data from the given stream and encodes the data using the deflate algorithm.
	 *
	 * @param in The {@link InputStream} to read from.
	 * @param length The length of the data to read. This is just a hint, it is not treated as the exact length of the data to read.
	 * @param fileTimestamp The time-stamp to set for the data read.
	 * @param fileName The (file)name to set for the data read.
	 * @param contentType The content-type to set for the data read.
	 * @throws IOException If reading the stream fails.
	 */
	public void loadStream(InputStream in, long length, Date fileTimestamp, String fileName, String contentType)
	throws IOException
	{
		logger.debug("Loading stream for ImageDataField");
		boolean error = true;
		try {
			DataBuffer db = new DataBuffer((long) (length * 0.6));
			OutputStream out = new DeflaterOutputStream(db.createOutputStream());
			try {
				IOUtil.transferStreamData(in, out);
			} finally {
				out.close();
			}
			content = db.createByteArray();

			this.fileTimestamp = fileTimestamp;
			this.fileName = fileName;
			this.contentEncoding = IContentDataField.CONTENT_ENCODING_DEFLATE;
			this.contentType = contentType;
			this.description = fileName;

			error = false;
		} finally {
			if (error) { // make sure that in case of an error all the file members are null.
				clear();
			}
		}
	}

	/**
	 * Load the binary data of the given stream and store it encoded using the deflate algorithm.
	 *
	 * @param in The {@link InputStream} to read from.
	 * @param name The (file)name to set for the data read.
	 * @param contentType The content-type to set for the data read.
	 * @throws IOException If reading the stream fails.
	 */
	public void loadStream(InputStream in, String name, String contentType)
	throws IOException
	{
		loadStream(in, 10 * 1024, new Date(), name, contentType);
	}

	/**
	 * Load the contents of the given file and store them encoded using the deflate algorithm.
	 *
	 * @param f The file to load the data from.
	 * @param contentType The content-type to set for the data read.
	 * @throws IOException If reading the file fails.
	 */
	public void loadFile(File f, String contentType)
	throws IOException
	{
		logger.debug("Loading file "+f+" as ImageDataField");
		FileInputStream in = new FileInputStream(f);
		try {
			loadStream(in, f.length(), new Date(f.lastModified()), f.getName(), contentType);
		} finally {
			in.close();
		}
	}

	/**
	 * Get the (file)name of the data.
	 * Note, that this might not be set to a filename with the correct
	 * extension according to the content-type.
	 *
	 * @return The (file)name of the data.
	 */
	public String getFileName() {
		return fileName;
	}

	/**
     * Set the fileName.
     * @param fileName the fileName to set
     */
    public void setFileName(String fileName)
    {
	    this.fileName = fileName;
    }

	/**
	 * Get the time-stamp of the data.
	 * This might represent the time the data was read or
	 * the time-stamp of the file the data was read from.
	 * @return The time-stamp of the data.
	 */
	public Date getFileTimestamp() {
		return fileTimestamp;
	}

	/**
     * Set the fileTimestamp.
     * @param fileTimestamp the fileTimestamp to set
     */
    public void setFileTimestamp(Date fileTimestamp)
    {
	    this.fileTimestamp = fileTimestamp;
    }

	/**
	 * @return The description of the image stored by this {@link ImageDataField}.
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Set the description of the image stored by this {@link ImageDataField}.
	 * @param imageDescription The description to set.
	 */
	public void setDescription(String imageDescription) {
		this.description = imageDescription;
	}

	@Override
	public Object getData() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setData(Object data) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean supportsInputType(Class<?> inputType) {
		return false;
	}
}
