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
package org.nightlabs.jfire.idgenerator;

import javax.jdo.spi.PersistenceCapable;

import org.apache.log4j.Logger;
import org.nightlabs.jdo.ObjectIDUtil;


/**
 * This class should be used to create unique IDs within the scope of an organisation
 * and a namespace. You can use the same API on the client as on the server side:
 * Call {@link #nextID(String)} in order to obtain a single unique identifier or
 * {@link #nextIDs(String, int)} if you need multiple IDs.
 * <p>
 * If you obtain identifiers for instances of a JDO class, it is highly
 * recommended to use one of the overloaded <code>nextID(...)</code> methods that
 * take a class as argument:
 * <ul>
 *	<li>{@link #nextID(Class)}</li>
 *	<li>{@link #nextID(Class, String)}</li>
 *	<li>{@link #nextIDs(Class, int)}</li>
 *	<li>{@link #nextIDs(Class, String, int)}</li>
 * </ul>
 * These methods check if the specified class is the root of a persistent inheritance hierarchy
 * and thus prevent duplicate key exceptions.
 * </p>
 * <p>
 * As a convenience, there are additionally methods that convert the resulting <code>long</code> numbers
 * into <code>String</code> instances:
 * <ul>
 *	<li>{@link #nextIDString(Class)}</li>
 *	<li>{@link #nextIDString(Class, String)}</li>
 *	<li>{@link #nextIDStrings(Class, int)}</li>
 *	<li>{@link #nextIDStrings(Class, String, int)}</li>
 *	<li>{@link #nextIDString(String)}</li>
 *	<li>{@link #nextIDStrings(String, int)}</li>
 * </ul>
 * </p>
 *
 * @author Marco Schulze - marco at nightlabs dot de
 */
public abstract class IDGenerator
{
	public static final String PROPERTY_KEY_ID_GENERATOR_CLASS = "org.nightlabs.jfire.idgenerator.idGeneratorClass";

	private static IDGenerator sharedInstance = null;

	/**
	 * @return the shared instance of the ID generator. Note, that a clustered server might have
	 *		multiple shared instances and thus it must be ensured that not two different instances return
	 *		colliding IDs (e.g. each one must have a separate range of cached IDs, if they cache IDs).
	 */
	protected synchronized static IDGenerator sharedInstance()
	{
		if (sharedInstance == null) {
			String className = System.getProperty(PROPERTY_KEY_ID_GENERATOR_CLASS);
			if (className == null)
				throw new IllegalStateException("System property " + PROPERTY_KEY_ID_GENERATOR_CLASS + " is not set!");

			Class<?> clazz;
			try {
				clazz = Class.forName(className);
			} catch (ClassNotFoundException e) {
				throw new RuntimeException(e);
			}

			try {
				sharedInstance = (IDGenerator) clazz.newInstance();
			} catch (InstantiationException e) {
				throw new RuntimeException(e);
			} catch (IllegalAccessException e) {
				throw new RuntimeException(e);
			}
		}

		return sharedInstance;
	}

	/**
	 * Set the shared instance. This allows injection of an IDGenerator whithout using a system property.
	 * @param sharedInstance The IDGenerator instance
	 */
	public synchronized static void setSharedInstance(IDGenerator sharedInstance) {
		IDGenerator.sharedInstance = sharedInstance;
	}
	
	/**
	 * @return You must return that organisationID in which the created IDs are unique - this means the organisationID
	 *	of the current user.
	 */
	protected abstract String _getOrganisationID();

	/**
	 * @param namespace At most 255 characters. Specifies a namespace within which the generated IDs must be unique.
	 * @param quantity How many IDs shall be generated? Once generated, they'll never occur again, thus are lost if
	 *		they will not be used. This must be >= 1.
	 * @return as many IDs (unique within the scope of <code>namespace</code>) as defined by <code>quanitity</code>.
	 */
	protected abstract long[] _nextIDs(String namespace, int quantity);

	/**
	 * This method generates an ID that is unique within the scope of a namespace
	 * inside of the current user's organisation. If you need more than one ID, you should
	 * better call {@link #nextIDs(String, int) } as this will result in higher performance.
	 *
	 * @param namespace A String identifier with not more than 255 characters. It should not contain
	 *		special characters (stay within the ASCII charset). You can use "-", "/", "." etc. though.
	 *		If it is the first time, this namespace is used, the first generated ID will be 1.
	 * @return a unique number within current organisation and given <code>namespace</code>.
	 */
	public static long nextID(String namespace)
	{
		return nextIDs(namespace, 1)[0];
	}

	/**
	 * This method generates IDs which are unique within the scope of a namespace
	 * inside of the current user's organisation.
	 *
	 * @param namespace A String identifier with not more than 255 characters. It should not contain
	 *		special characters (stay within the ASCII charset). You can use "-", "/", "." etc. though.
	 *		If it is the first time, this namespace is used, the first generated ID will be 1.
	 * @param quantity How many IDs shall be generated? Must be at least 1.
	 * @return an array of unique numbers with the size as specified by <code>quantity</code>.
	 *		They are unique within the current organisation and the given <code>namespace</code>.
	 */
	public static long[] nextIDs(String namespace, int quantity)
	{
		if (namespace == null)
			throw new IllegalArgumentException("namespace must not be null!");

		if (quantity < 1)
			throw new IllegalArgumentException("quantity < 1");

		IDGenerator sharedInstance = sharedInstance();
		long[] res = sharedInstance._nextIDs(namespace, quantity);
		if (res == null)
			throw new IllegalStateException("Implementation of _nextIDs(...) returned null! Class: " + sharedInstance.getClass().getName());

		if (res.length != quantity)
			throw new IllegalStateException("Implementation of _nextIDs(...) returned " + res.length + " IDs, but " + quantity + " were requested! Class: " + sharedInstance.getClass().getName());

		return res;
	}

	/**
	 * The generated IDs are only valid within the scope of the namespace and within one organisation. Thus,
	 * it's interesting to know, in which organisation. This will return the organisationID of the current
	 * user.
	 *
	 * @return Returns the organisationID of the current user.
	 */
	public static String getOrganisationID()
	{
		return sharedInstance()._getOrganisationID();
	}

	/**
	 * This method calls {@link #nextID(String)} with the
	 * fully qualified name of the given class. It is not simply a convenience
	 * method, but checks via {@link #checkClass(Class)} whether the argument
	 * is the root class of a <code>PersistenceCapable</code> type hierarchy!
	 *
	 * @param clazz The class whose name should be used as namespace.
	 * @return the next unique ID in the namespace of the class name.
	 */
	public static long nextID(Class<?> clazz)
	{
		checkClass(clazz);
		return nextID(clazz.getName());
	}

	/**
	 * This method calls {@link #nextIDs(String, int)} with
	 * the fully qualified name of the given class. It is not simply a convenience
	 * method, but checks via {@link #checkClass(Class)} whether the argument
	 * is the root class of a <code>PersistenceCapable</code> type hierarchy!
	 *
	 * @param clazz The class whose name should be used as namespace.
	 * @param quantity The number of desired IDs. Must be at least 1.
	 * @return the next unique IDs in the namespace of the class name.
	 */
	public static long[] nextIDs(Class<?> clazz, int quantity)
	{
		checkClass(clazz);
		return nextIDs(clazz.getName(), quantity);
	}

	/**
	 * This method calls {@link #nextID(String)} with the
	 * fully qualified name of the given class (and a suffix, if given).
	 * It is not simply a convenience
	 * method, but checks via {@link #checkClass(Class)} whether the argument
	 * is the root class of a <code>PersistenceCapable</code> type hierarchy!
	 *
	 * @param clazz The class whose name should be used as namespace.
	 * @param suffix The <code>suffix</code> which will be appended to the class name, if it's neither <code>null</code>,
	 *		nor an empty space. If the suffix is appended, the separator {@link #classSuffixSeparator}
	 *		({@value #classSuffixSeparator}) will be used inbetween.
	 * @return the next unique ID in the namespace of the class name.
	 */
	public static long nextID(Class<?> clazz, String suffix)
	{
		checkClass(clazz);
		if (suffix == null || emptyString.equals(suffix))
			return nextID(clazz.getName());

		return nextID(clazz.getName() + classSuffixSeparator + suffix);
	}

	/**
	 * This method calls {@link #nextIDs(String, int)} with
	 * the fully qualified name of the given class (and a suffix, if given).
	 * It is not simply a convenience
	 * method, but checks via {@link #checkClass(Class)} whether the argument
	 * is the root class of a <code>PersistenceCapable</code> type hierarchy!
	 *
	 * @param clazz The class whose name should be (together with the <code>suffix</code>) used as namespace.
	 * @param suffix The <code>suffix</code> which will be appended to the class name, if it's neither <code>null</code>,
	 *		nor an empty space. If the suffix is appended, the separator {@link #classSuffixSeparator}
	 *		({@value #classSuffixSeparator}) will be used inbetween.
	 * @param quantity The number of desired IDs. Must be at least 1.
	 * @return the next unique IDs in the namespace of the class name.
	 */
	public static long[] nextIDs(Class<?> clazz, String suffix, int quantity)
	{
		checkClass(clazz);
		if (suffix == null || emptyString.equals(suffix))
			return nextIDs(clazz.getName(), quantity);

		return nextIDs(clazz.getName() + classSuffixSeparator + suffix, quantity);
	}

	private static final String emptyString = "";
	private static final char classSuffixSeparator = '#';

	/**
	 * If the given <code>clazz</code> implements {@link PersistenceCapable}, this method
	 * checks, whether it is the topmost class in an inheritence structure (i.e. the first one
	 * which implements this interface). If it is not the root class in an inheritance structure,
	 * an {@link IllegalArgumentException} will be thrown.
	 *
	 * @param clazz The class to be checked.
	 *
	 * @throws IllegalArgumentException If the given <code>clazz</code> implements {@link PersistenceCapable} (and therefore
	 *		is a JDO class), but not the root class (i.e. the one that declares the primary key for the whole inheritance tree).
	 */
	private static void checkClass(Class<?> clazz)
	throws IllegalArgumentException
	{
		if (!PersistenceCapable.class.isAssignableFrom(clazz)) {
			Logger.getLogger(IDGenerator.class).warn("Class '" + clazz + "' does not implement "+PersistenceCapable.class+"!", new Exception());
			return;
		}

		if (PersistenceCapable.class.isAssignableFrom(clazz.getSuperclass())) {
			Class<?> topPC = clazz;
			while (PersistenceCapable.class.isAssignableFrom(topPC.getSuperclass()))
				topPC = topPC.getSuperclass();

			throw new IllegalArgumentException("Class " + clazz.getName() + " is not the top-most PersistenceCapable in the type hierarchy! You must pass the class " + topPC.getName() + " instead!");
		}
	}

	/**
	 * This method calls {@link #nextID(String)} and converts the resulting identifier
	 * into a <code>String</code> using {@link ObjectIDUtil#longObjectIDFieldToString(long)}.
	 *
	 * @param namespace A String identifier with not more than 255 characters. It should not contain
	 *		special characters (stay within the ASCII charset). You can use "-", "/", "." etc. though.
	 *		If it is the first time, this namespace is used, the first generated ID will be 1.
	 * @return a unique number within current organisation and given <code>namespace</code> (converted into a <code>String</code>).
	 */
	public static String nextIDString(String namespace)
	{
		return ObjectIDUtil.longObjectIDFieldToString(nextID(namespace));
	}

	/**
	 * This method calls {@link #nextID(String)} and converts the resulting identifier
	 * into a <code>String</code> using {@link ObjectIDUtil#longObjectIDFieldToString(long)}.
	 *
	 * @param namespace A String identifier with not more than 255 characters. It should not contain
	 *		special characters (stay within the ASCII charset). You can use "-", "/", "." etc. though.
	 *		If it is the first time, this namespace is used, the first generated ID will be 1.
	 * @return a unique number within current organisation and given <code>namespace</code> (converted into a <code>String</code>).
	 */
	public static String nextIDString(Class<?> clazz)
	{
		return ObjectIDUtil.longObjectIDFieldToString(nextID(clazz));
	}

	/**
	 * This method calls {@link #nextID(Class, String)} and converts the resulting identifier
	 * into a <code>String</code> using {@link ObjectIDUtil#longObjectIDFieldToString(long)}.
	 *
	 * @param clazz The class whose name should be used as namespace.
	 * @param suffix The <code>suffix</code> which will be appended to the class name, if it's neither <code>null</code>,
	 *		nor an empty space. If the suffix is appended, the separator {@link #classSuffixSeparator}
	 *		({@value #classSuffixSeparator}) will be used inbetween.
	 * @return the next unique ID in the namespace of the class name.
	 */
	public static String nextIDString(Class<?> clazz, String suffix)
	{
		return ObjectIDUtil.longObjectIDFieldToString(nextID(clazz, suffix));
	}

	/**
	 * This method calls {@link #nextIDs(String, int)} and converts the resulting identifiers
	 * into a <code>String</code> array using {@link ObjectIDUtil#longObjectIDFieldToString(long)}.
	 *
	 * @param namespace A String identifier with not more than 255 characters. It should not contain
	 *		special characters (stay within the ASCII charset). You can use "-", "/", "." etc. though.
	 *		If it is the first time, this namespace is used, the first generated ID will be 1.
	 * @param quantity How many IDs shall be generated? Must be at least 1.
	 * @return an array of unique numbers with the size as specified by <code>quantity</code>.
	 *		They are unique within the current organisation and the given <code>namespace</code>.
	 */
	public static String[] nextIDStrings(String namespace, int quantity)
	{
		long[] ids = nextIDs(namespace, quantity);
		return convertIDs(ids);
	}

	/**
	 * This method calls {@link #nextIDs(Class, int)} and converts the resulting identifiers
	 * into a <code>String</code> array using {@link ObjectIDUtil#longObjectIDFieldToString(long)}.
	 *
	 * @param clazz The class whose name should be used as namespace.
	 * @param quantity The number of desired IDs. Must be at least 1.
	 * @return the next unique IDs in the namespace of the class name.
	 */
	public static String[] nextIDStrings(Class<?> clazz, int quantity)
	{
		long[] ids = nextIDs(clazz, quantity);
		return convertIDs(ids);
	}

	/**
	 * This method calls {@link #nextIDs(Class, String, int)} and converts the resulting identifiers
	 * into a <code>String</code> array using {@link ObjectIDUtil#longObjectIDFieldToString(long)}.
	 *
	 * @param clazz The class whose name should be (together with the <code>suffix</code>) used as namespace.
	 * @param suffix The <code>suffix</code> which will be appended to the class name, if it's neither <code>null</code>,
	 *		nor an empty space. If the suffix is appended, the separator {@link #classSuffixSeparator}
	 *		({@value #classSuffixSeparator}) will be used inbetween.
	 * @param quantity The number of desired IDs. Must be at least 1.
	 * @return the next unique IDs in the namespace of the class name.
	 */
	public static String[] nextIDStrings(Class<?> clazz, String suffix, int quantity)
	{
		long[] ids = nextIDs(clazz, suffix, quantity);
		return convertIDs(ids);
	}

	private static String[] convertIDs(long[] ids)
	{
		String[] result = new String[ids.length];
		for (int idx = 0; idx < ids.length; ++idx)
			result[idx] = ObjectIDUtil.longObjectIDFieldToString(ids[idx]);

		return result;
	}
}
