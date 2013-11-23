/* This class has been auto-generated Please DO NOT edit this file! */
package org.nightlabs.jfire.config.id;

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
 * The JDO object id class for {@link org.nightlabs.jfire.config.ConfigModuleInitialiser}.
 * <p>This class was auto-generated.</p>
 */
@Generated("org.nightlabs.eclipse.sdk.jdo.ObjectIdGenerator")
public class ConfigModuleInitialiserID
implements ObjectID
{
	/**
	 * The serial version uid of this class.
	 */
	@Generated("org.nightlabs.eclipse.sdk.jdo.ObjectIdGenerator")
	private static final long serialVersionUID = -9062695677416098225L;

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
	 * Primary key field organisationID.
	 * Declared as primary key field in {@link org.nightlabs.jfire.config.ConfigModuleInitialiser}.
	 */
	@Generated("org.nightlabs.eclipse.sdk.jdo.ObjectIdGenerator")
	public java.lang.String organisationID;

	/**
	 * Primary key field configModuleClassName.
	 * Declared as primary key field in {@link org.nightlabs.jfire.config.ConfigModuleInitialiser}.
	 */
	@Generated("org.nightlabs.eclipse.sdk.jdo.ObjectIdGenerator")
	public java.lang.String configModuleClassName;

	/**
	 * Primary key field configModuleInitialiserID.
	 * Declared as primary key field in {@link org.nightlabs.jfire.config.ConfigModuleInitialiser}.
	 */
	@Generated("org.nightlabs.eclipse.sdk.jdo.ObjectIdGenerator")
	public java.lang.String configModuleInitialiserID;

	/**
	 * Create a new empty instance of ConfigModuleInitialiserID.
	 */
	@Generated("org.nightlabs.eclipse.sdk.jdo.ObjectIdGenerator")
	public ConfigModuleInitialiserID()
	{
	}

	/**
	 * Create a new instance of ConfigModuleInitialiserID.
	 * This is done by parsing the <code>keyStr</code> that has been created
	 * by {@link #toString()} and setting all fields to the values from the string.
	 * <p>
	 * This means, the following code will create a copy of this class:<br/><br/>
	 * <code>ConfigModuleInitialiserID newConfigModuleInitialiserID = new ConfigModuleInitialiserID(oldConfigModuleInitialiserID.toString());</code>
	 * </p>
	 * @param keyStr A String formatted as "jdo/{className}?{field0}={value0}&amp;{field1}={value1}...&amp;{fieldN}={valueN}"
	 *     where all values are url encoded using {@link #ENCODING}.
	 */
	@Generated("org.nightlabs.eclipse.sdk.jdo.ObjectIdGenerator")
	public ConfigModuleInitialiserID(final String keyStr)
	throws ParseException, SecurityException,
			NoSuchFieldException, IllegalArgumentException,
			IllegalAccessException, NoSuchMethodException,
			InstantiationException, InvocationTargetException
	{
		Class<? extends ConfigModuleInitialiserID> clazz = this.getClass();

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
						field.set(this, Character.valueOf((char)0));
					else // for all other primitives - i.e. byte, short, int, long, float, double
						field.set(this, Byte.valueOf((byte)0));
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
	 * {@link #ConfigModuleInitialiserID(String)}.
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
			sb.append("organisationID="); //$NON-NLS-1$
			sb.append(URLEncoder.encode(String.valueOf(organisationID), ENCODING));
			sb.append('&');
			sb.append("configModuleClassName="); //$NON-NLS-1$
			sb.append(URLEncoder.encode(String.valueOf(configModuleClassName), ENCODING));
			sb.append('&');
			sb.append("configModuleInitialiserID="); //$NON-NLS-1$
			sb.append(URLEncoder.encode(String.valueOf(configModuleInitialiserID), ENCODING));
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
		ConfigModuleInitialiserID other = (ConfigModuleInitialiserID) obj;
		if(organisationID == null) {
			if(other.organisationID != null)
				return false;
		} else if(!organisationID.equals(other.organisationID))
			return false;
		if(configModuleClassName == null) {
			if(other.configModuleClassName != null)
				return false;
		} else if(!configModuleClassName.equals(other.configModuleClassName))
			return false;
		if(configModuleInitialiserID == null) {
			if(other.configModuleInitialiserID != null)
				return false;
		} else if(!configModuleInitialiserID.equals(other.configModuleInitialiserID))
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
		result = prime * result + ((organisationID == null) ? 0 : organisationID.hashCode());
		result = prime * result + ((configModuleClassName == null) ? 0 : configModuleClassName.hashCode());
		result = prime * result + ((configModuleInitialiserID == null) ? 0 : configModuleInitialiserID.hashCode());
		return result;
	}

	/**
	 * Create a new object id instance.
	 * @param organisationID The primary key field organisationID.
	 * @param configModuleClassName The primary key field configModuleClassName.
	 * @param configModuleInitialiserID The primary key field configModuleInitialiserID.
	 * @return a newly created instance of <code>ConfigModuleInitialiserID</code>
	 *     with the primary-key fields set to the given parameters.
	 */
	@Generated("org.nightlabs.eclipse.sdk.jdo.ObjectIdGenerator")
	public static ConfigModuleInitialiserID create(String organisationID, String configModuleClassName, String configModuleInitialiserID)
	{
		ConfigModuleInitialiserID n = new ConfigModuleInitialiserID();
		n.organisationID = organisationID;
		n.configModuleClassName = configModuleClassName;
		n.configModuleInitialiserID = configModuleInitialiserID;
		return n;
	}
}
