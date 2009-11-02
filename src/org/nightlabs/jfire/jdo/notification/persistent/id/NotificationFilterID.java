/* This class has been auto-generated Please DO NOT edit this file! */
package org.nightlabs.jfire.jdo.notification.persistent.id;

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
 * The JDO object id class for {@link org.nightlabs.jfire.jdo.notification.persistent.NotificationFilter}.
 * <p>This class was auto-generated.</p>
 */
@Generated("org.nightlabs.eclipse.sdk.jdo.ObjectIdGenerator")
public class NotificationFilterID
implements ObjectID
{
	/**
	 * The serial version uid of this class.
	 */
	@Generated("org.nightlabs.eclipse.sdk.jdo.ObjectIdGenerator")
	private static final long serialVersionUID = -1860630195L;

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
	 * Declared as primary key field in {@link org.nightlabs.jfire.jdo.notification.persistent.NotificationFilter}.
	 */
	@Generated("org.nightlabs.eclipse.sdk.jdo.ObjectIdGenerator")
	public java.lang.String organisationID;

	/**
	 * Primary key field subscriberType.
	 * Declared as primary key field in {@link org.nightlabs.jfire.jdo.notification.persistent.NotificationFilter}.
	 */
	@Generated("org.nightlabs.eclipse.sdk.jdo.ObjectIdGenerator")
	public java.lang.String subscriberType;

	/**
	 * Primary key field subscriberID.
	 * Declared as primary key field in {@link org.nightlabs.jfire.jdo.notification.persistent.NotificationFilter}.
	 */
	@Generated("org.nightlabs.eclipse.sdk.jdo.ObjectIdGenerator")
	public java.lang.String subscriberID;

	/**
	 * Primary key field subscriptionID.
	 * Declared as primary key field in {@link org.nightlabs.jfire.jdo.notification.persistent.NotificationFilter}.
	 */
	@Generated("org.nightlabs.eclipse.sdk.jdo.ObjectIdGenerator")
	public java.lang.String subscriptionID;

	/**
	 * Create a new empty instance of NotificationFilterID.
	 */
	@Generated("org.nightlabs.eclipse.sdk.jdo.ObjectIdGenerator")
	public NotificationFilterID()
	{
	}

	/**
	 * Create a new instance of NotificationFilterID.
	 * This is done by parsing the <code>keyStr</code> that has been created
	 * by {@link #toString()} and setting all fields to the values from the string.
	 * <p>
	 * This means, the following code will create a copy of this class:<br/><br/>
	 * <code>NotificationFilterID newNotificationFilterID = new NotificationFilterID(oldNotificationFilterID.toString());</code>
	 * </p>
	 * @param keyStr A String formatted as "jdo/{className}?{field0}={value0}&amp;{field1}={value1}...&amp;{fieldN}={valueN}"
	 *     where all values are url encoded using {@link #ENCODING}.
	 */
	@Generated("org.nightlabs.eclipse.sdk.jdo.ObjectIdGenerator")
	public NotificationFilterID(final String keyStr)
	throws ParseException, SecurityException,
			NoSuchFieldException, IllegalArgumentException,
			IllegalAccessException, NoSuchMethodException,
			InstantiationException, InvocationTargetException
	{
		Class<? extends NotificationFilterID> clazz = this.getClass();

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
	 * {@link #NotificationFilterID(String)}.
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
			sb.append("subscriberType="); //$NON-NLS-1$
			sb.append(URLEncoder.encode(String.valueOf(subscriberType), ENCODING));
			sb.append('&');
			sb.append("subscriberID="); //$NON-NLS-1$
			sb.append(URLEncoder.encode(String.valueOf(subscriberID), ENCODING));
			sb.append('&');
			sb.append("subscriptionID="); //$NON-NLS-1$
			sb.append(URLEncoder.encode(String.valueOf(subscriptionID), ENCODING));
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
		NotificationFilterID other = (NotificationFilterID) obj;
		if(organisationID == null) {
			if(other.organisationID != null)
				return false;
		} else if(!organisationID.equals(other.organisationID))
			return false;
		if(subscriberType == null) {
			if(other.subscriberType != null)
				return false;
		} else if(!subscriberType.equals(other.subscriberType))
			return false;
		if(subscriberID == null) {
			if(other.subscriberID != null)
				return false;
		} else if(!subscriberID.equals(other.subscriberID))
			return false;
		if(subscriptionID == null) {
			if(other.subscriptionID != null)
				return false;
		} else if(!subscriptionID.equals(other.subscriptionID))
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
		result = prime * result + ((subscriberType == null) ? 0 : subscriberType.hashCode());
		result = prime * result + ((subscriberID == null) ? 0 : subscriberID.hashCode());
		result = prime * result + ((subscriptionID == null) ? 0 : subscriptionID.hashCode());
		return result;
	}

	/**
	 * Create a new object id instance.
	 * @param organisationID The primary key field organisationID.
	 * @param subscriberType The primary key field subscriberType.
	 * @param subscriberID The primary key field subscriberID.
	 * @param subscriptionID The primary key field subscriptionID.
	 * @return a newly created instance of <code>NotificationFilterID</code>
	 *     with the primary-key fields set to the given parameters.
	 */
	@Generated("org.nightlabs.eclipse.sdk.jdo.ObjectIdGenerator")
	public static NotificationFilterID create(String organisationID, String subscriberType, String subscriberID, String subscriptionID)
	{
		NotificationFilterID n = new NotificationFilterID();
		n.organisationID = organisationID;
		n.subscriberType = subscriberType;
		n.subscriberID = subscriberID;
		n.subscriptionID = subscriptionID;
		return n;
	}
}
