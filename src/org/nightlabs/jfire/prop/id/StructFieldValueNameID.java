/* This class has been auto-generated Please DO NOT edit this file! */
package org.nightlabs.jfire.prop.id;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.ParseException;
import java.util.StringTokenizer;

import javax.annotation.Generated;

import org.nightlabs.jdo.ObjectID;


/**
 * The JDO object id class for {@link org.nightlabs.jfire.prop.i18n.StructFieldValueName}.
 * <p>This class was auto-generated.</p>
 */
@Generated("org.nightlabs.eclipse.sdk.jdo.ObjectIdGenerator")
public class StructFieldValueNameID
implements ObjectID
{
	/**
	 * The serial version uid of this class.
	 */
	@Generated("org.nightlabs.eclipse.sdk.jdo.ObjectIdGenerator")
	private static final long serialVersionUID = 4613149246578521330L;

	/**
	 * The values of all fields are URL encoded in UTF-8.
	 */
	@Generated("org.nightlabs.eclipse.sdk.jdo.ObjectIdGenerator")
	public static final String ENCODING = "UTF-8"; //$NON-NLS-1$

	/**
	 * The object id URL prefix.
	 */
	@Generated("org.nightlabs.eclipse.sdk.jdo.ObjectIdGenerator")
	private static final String JDO_PREFIX = "jdo"; //$NON-NLS-1$

	/**
	 * The object id URL prefix separator.
	 */
	@Generated("org.nightlabs.eclipse.sdk.jdo.ObjectIdGenerator")
	private static final char JDO_PREFIX_SEPARATOR = '/';

	/**
	 * The object id URL class separator.
	 */
	@Generated("org.nightlabs.eclipse.sdk.jdo.ObjectIdGenerator")
	private static final char CLASS_SEPARATOR = '?';

	/**
	 * The object id URL tokenizer separators.
	 */
	@Generated("org.nightlabs.eclipse.sdk.jdo.ObjectIdGenerator")
	private static final String SEPARATORS_FOR_TOKENIZER = "/?=&"; //$NON-NLS-1$

	/**
	 * The object id URL key/value separator.
	 */
	@Generated("org.nightlabs.eclipse.sdk.jdo.ObjectIdGenerator")
	private static final String SEPARATOR_KEY_VALUE = "="; //$NON-NLS-1$

	/**
	 * The object id URL entry separator.
	 */
	@Generated("org.nightlabs.eclipse.sdk.jdo.ObjectIdGenerator")
	private static final String SEPARATOR_ENTRY = "&"; //$NON-NLS-1$

	/**
	 * The radix that is used for encoding/decoding field values of numeric IDs (byte, short, int, long).
	 */
	@Generated("org.nightlabs.eclipse.sdk.jdo.ObjectIdGenerator")
	private static final int RADIX = 36;

	/**
	 * Primary key field structBlockOrganisationID.
	 * Declared as primary key field in {@link org.nightlabs.jfire.prop.i18n.StructFieldValueName}.
	 */
	@Generated("org.nightlabs.eclipse.sdk.jdo.ObjectIdGenerator")
	public java.lang.String structBlockOrganisationID;

	/**
	 * Primary key field structBlockID.
	 * Declared as primary key field in {@link org.nightlabs.jfire.prop.i18n.StructFieldValueName}.
	 */
	@Generated("org.nightlabs.eclipse.sdk.jdo.ObjectIdGenerator")
	public java.lang.String structBlockID;

	/**
	 * Primary key field structFieldID.
	 * Declared as primary key field in {@link org.nightlabs.jfire.prop.i18n.StructFieldValueName}.
	 */
	@Generated("org.nightlabs.eclipse.sdk.jdo.ObjectIdGenerator")
	public java.lang.String structFieldID;

	/**
	 * Primary key field structFieldOrganisationID.
	 * Declared as primary key field in {@link org.nightlabs.jfire.prop.i18n.StructFieldValueName}.
	 */
	@Generated("org.nightlabs.eclipse.sdk.jdo.ObjectIdGenerator")
	public java.lang.String structFieldOrganisationID;

	/**
	 * Primary key field structFieldValueID.
	 * Declared as primary key field in {@link org.nightlabs.jfire.prop.i18n.StructFieldValueName}.
	 */
	@Generated("org.nightlabs.eclipse.sdk.jdo.ObjectIdGenerator")
	public java.lang.String structFieldValueID;

	/**
	 * Create a new empty instance of StructFieldValueNameID.
	 */
	@Generated("org.nightlabs.eclipse.sdk.jdo.ObjectIdGenerator")
	public StructFieldValueNameID()
	{
	}

	/**
	 * Create a new instance of StructFieldValueNameID.
	 * This is done by parsing the <code>keyStr</code> that has been created
	 * by {@link #toString()} and setting all fields to the values from the string.
	 * <p>
	 * This means, the following code will create a copy of this class:<br/><br/>
	 * <code>StructFieldValueNameID newStructFieldValueNameID = new StructFieldValueNameID(oldStructFieldValueNameID.toString());</code>
	 * </p>
	 * @param keyStr A String formatted as "jdo/{className}?{field0}={value0}&amp;{field1}={value1}...&amp;{fieldN}={valueN}"
	 *     where all values are url encoded using {@link #ENCODING}.
	 */
	@Generated("org.nightlabs.eclipse.sdk.jdo.ObjectIdGenerator")
	public StructFieldValueNameID(final String keyStr)
	throws ParseException, SecurityException,
			NoSuchFieldException, IllegalArgumentException,
			IllegalAccessException, NoSuchMethodException,
			InstantiationException, InvocationTargetException
	{
		Class<? extends StructFieldValueNameID> clazz = this.getClass();

		StringTokenizer st = new StringTokenizer(keyStr, SEPARATORS_FOR_TOKENIZER, true);
		String jdoPrefix = st.nextToken();
		if (!JDO_PREFIX.equals(jdoPrefix))
			throw new ParseException(
					"keyStr \""+ //$NON-NLS-1$
					keyStr+
					"\" does not start with jdo prefix \""+ //$NON-NLS-1$
					JDO_PREFIX+
					"\"!", 0); //$NON-NLS-1$
		if (!st.hasMoreTokens() || st.nextToken().charAt(0) != JDO_PREFIX_SEPARATOR)
			throw new ParseException(
					"keyStr \""+ //$NON-NLS-1$
					keyStr+
					"\" is missing separator \""+ //$NON-NLS-1$
					JDO_PREFIX_SEPARATOR+
					"\" after jdo prefix!", 0); //$NON-NLS-1$

		String className = st.nextToken();
		if (!className.equals(clazz.getName()))
			throw new ParseException(
					"keyStr defines class \""+ //$NON-NLS-1$
					className+
					"\", but this is an instance of \""+ //$NON-NLS-1$
					clazz.getName()+
					"\"!", 0); //$NON-NLS-1$

		if (!st.hasMoreTokens() || st.nextToken().charAt(0) != CLASS_SEPARATOR)
			throw new ParseException(
					"keyStr \""+ //$NON-NLS-1$
					keyStr+
					"\" is missing separator \""+ //$NON-NLS-1$
					CLASS_SEPARATOR+
					"\" after class!", 0); //$NON-NLS-1$

		while (st.hasMoreTokens()) {
			String key = st.nextToken();
			String valStr = ""; //$NON-NLS-1$
			if (st.hasMoreTokens()) {
				String sep = st.nextToken();
				if (!SEPARATOR_KEY_VALUE.equals(sep))
					throw new ParseException(
							"Expected \""+ //$NON-NLS-1$
							SEPARATOR_KEY_VALUE+
							"\", but found \""+ //$NON-NLS-1$
							sep+
							"\"!", 0); //$NON-NLS-1$

				if (st.hasMoreTokens()) {
					valStr = st.nextToken();
					if (SEPARATOR_ENTRY.equals(valStr)) {
						sep = valStr;
						valStr = ""; //$NON-NLS-1$
					}
					else
						try {
							valStr = URLDecoder.decode(valStr, ENCODING);
						} catch (UnsupportedEncodingException e) {
							e.printStackTrace();
							throw new RuntimeException(e);
						}
				}
				if (!SEPARATOR_ENTRY.equals(sep)) {
					if (st.hasMoreTokens()) {
						sep = st.nextToken();
						if (!SEPARATOR_ENTRY.equals(sep))
							throw new ParseException(
									"Expected \""+ //$NON-NLS-1$
									SEPARATOR_ENTRY+
									"\", but found \""+ //$NON-NLS-1$
									sep+"\"!", 0); //$NON-NLS-1$
					}
				} // if (!SEPARATOR_ENTRY.equals(val)) {
			} // if (st.hasMoreTokens()) {
			Field field = clazz.getField(key);
			Class<?> fieldType = field.getType();
			if (valStr == null) {
				if (!fieldType.isPrimitive())
					field.set(this, null);
				else {
					if (boolean.class.isAssignableFrom(fieldType))
						field.set(this, Boolean.FALSE);
					else if (char.class.isAssignableFrom(fieldType))
						field.set(this, new Character((char)0));
					else // for all other primitives - i.e. byte, short, int, long, float, double
						field.set(this, new Byte((byte)0));
				}
			}
			else {
				Object val = null;
				if (String.class.isAssignableFrom(fieldType))
					val = valStr;
				else if (boolean.class.isAssignableFrom(fieldType))
					val = Boolean.valueOf(valStr);
				else if (char.class.isAssignableFrom(fieldType))
					val = Character.valueOf(valStr.charAt(0));
				else if (byte.class.isAssignableFrom(fieldType))
					val = Byte.valueOf(valStr, RADIX);
				else if (short.class.isAssignableFrom(fieldType))
					val = Short.valueOf(valStr, RADIX);
				else if (int.class.isAssignableFrom(fieldType))
					val = Integer.valueOf(valStr, RADIX);
				else if (long.class.isAssignableFrom(fieldType))
					val = Long.valueOf(valStr, RADIX);
				else
					throw new IllegalArgumentException(
							"Type "+ //$NON-NLS-1$
							fieldType.getName()+
							" of member "+ //$NON-NLS-1$
							key+
							" is not unsupported!"); //$NON-NLS-1$
				field.set(this, val);
			}
		}
	}

	/**
	 * Create a string representation of this object id.
	 * <p>
	 * JDO expects the result of this method to be compatible with the constructor
	 * {@link #StructFieldValueNameID(String)}.
	 * This method takes all the primary-key-fields and encodes them with their name
	 * and their value.
	 * </p>
	 * @return a string representation of this object id.
	 * @see java.lang.Object#toString()
	 */
	@Override
	@Generated("org.nightlabs.eclipse.sdk.jdo.ObjectIdGenerator")
	public String toString()
	{
		StringBuffer sb = new StringBuffer(JDO_PREFIX);
		sb.append(JDO_PREFIX_SEPARATOR);
		sb.append(this.getClass().getName());
		sb.append(CLASS_SEPARATOR);

		try {
			sb.append("structBlockOrganisationID="); //$NON-NLS-1$
			sb.append(URLEncoder.encode(String.valueOf(structBlockOrganisationID), ENCODING));
			sb.append('&');
			sb.append("structBlockID="); //$NON-NLS-1$
			sb.append(URLEncoder.encode(String.valueOf(structBlockID), ENCODING));
			sb.append('&');
			sb.append("structFieldID="); //$NON-NLS-1$
			sb.append(URLEncoder.encode(String.valueOf(structFieldID), ENCODING));
			sb.append('&');
			sb.append("structFieldOrganisationID="); //$NON-NLS-1$
			sb.append(URLEncoder.encode(String.valueOf(structFieldOrganisationID), ENCODING));
			sb.append('&');
			sb.append("structFieldValueID="); //$NON-NLS-1$
			sb.append(URLEncoder.encode(String.valueOf(structFieldValueID), ENCODING));
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(
					"Encoding failed with encoding " + //$NON-NLS-1$
					ENCODING, e);
		}
		return sb.toString();
	}

	/**
	 * Compare all primary key fields (according to the JDO spec).
	 * @param obj the reference object with which to compare.
	 * @return <code>true</code> if all primary key fields are equal - <code>false</code> otherwise.
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	@Generated("org.nightlabs.eclipse.sdk.jdo.ObjectIdGenerator")
	public boolean equals(Object obj)
	{
		if(this == obj)
			return true;
		if(obj == null)
			return false;
		if(getClass() != obj.getClass())
			return false;
		StructFieldValueNameID other = (StructFieldValueNameID) obj;
		if(structBlockOrganisationID == null) {
			if(other.structBlockOrganisationID != null)
				return false;
		} else if(!structBlockOrganisationID.equals(other.structBlockOrganisationID))
			return false;
		if(structBlockID == null) {
			if(other.structBlockID != null)
				return false;
		} else if(!structBlockID.equals(other.structBlockID))
			return false;
		if(structFieldID == null) {
			if(other.structFieldID != null)
				return false;
		} else if(!structFieldID.equals(other.structFieldID))
			return false;
		if(structFieldOrganisationID == null) {
			if(other.structFieldOrganisationID != null)
				return false;
		} else if(!structFieldOrganisationID.equals(other.structFieldOrganisationID))
			return false;
		if(structFieldValueID == null) {
			if(other.structFieldValueID != null)
				return false;
		} else if(!structFieldValueID.equals(other.structFieldValueID))
			return false;
		return true;
	}

	/**
	 * Returns a hash code for this object id. The hash code for a
	 * object id object is computed by combining the hash codes of
	 * all primary key fields.
	 * @return a hash code for this object.
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	@Generated("org.nightlabs.eclipse.sdk.jdo.ObjectIdGenerator")
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((structBlockOrganisationID == null) ? 0 : structBlockOrganisationID.hashCode());
		result = prime * result + ((structBlockID == null) ? 0 : structBlockID.hashCode());
		result = prime * result + ((structFieldID == null) ? 0 : structFieldID.hashCode());
		result = prime * result + ((structFieldOrganisationID == null) ? 0 : structFieldOrganisationID.hashCode());
		result = prime * result + ((structFieldValueID == null) ? 0 : structFieldValueID.hashCode());
		return result;
	}

	/**
	 * Create a new object id instance.
	 * @param structBlockOrganisationID The primary key field structBlockOrganisationID.
	 * @param structBlockID The primary key field structBlockID.
	 * @param structFieldID The primary key field structFieldID.
	 * @param structFieldOrganisationID The primary key field structFieldOrganisationID.
	 * @param structFieldValueID The primary key field structFieldValueID.
	 * @return a newly created instance of <code>StructFieldValueNameID</code>
	 *     with the primary-key fields set to the given parameters.
	 */
	@Generated("org.nightlabs.eclipse.sdk.jdo.ObjectIdGenerator")
	public static StructFieldValueNameID create(String structBlockOrganisationID, String structBlockID, String structFieldID, String structFieldOrganisationID, String structFieldValueID)
	{
		StructFieldValueNameID n = new StructFieldValueNameID();
		n.structBlockOrganisationID = structBlockOrganisationID;
		n.structBlockID = structBlockID;
		n.structFieldID = structFieldID;
		n.structFieldOrganisationID = structFieldOrganisationID;
		n.structFieldValueID = structFieldValueID;
		return n;
	}
}
