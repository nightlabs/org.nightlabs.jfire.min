package org.nightlabs.jfire.testsuite.xdoclet;

import org.nightlabs.jfire.testsuite.xdoclet.id.TestJDOID;


/**
 * @author Marco Schulze - Marco at NightLabs dot de
 *
 * @jdo.persistence-capable 
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.testsuite.xdoclet.id.TestJDOID"
 *		detachable="true"
 *		table="JFireBase_TestJDO"
 *
 * @jdo.inheritance strategy = "new-table"
 *
 * @jdo.create-objectid-class field-order="f_boolean, f_char, f_byte, f_short, f_int, f_long, f_String"
 */
public class TestJDO
{
	/**
	 * @jdo.field primary-key="true"
	 */
	private boolean f_boolean;
	/**
	 * @jdo.field primary-key="true"
	 */
	private char f_char;
	/**
	 * @jdo.field primary-key="true"
	 */
	private byte f_byte;
	/**
	 * @jdo.field primary-key="true"
	 */
	private short f_short;
	/**
	 * @jdo.field primary-key="true"
	 */
	private int f_int;
	/**
	 * @jdo.field primary-key="true"
	 */
	private long f_long;

	/**
	 * @jdo.field primary-key="true"
	 */
	private String f_String;

	public TestJDO()
	{
	}

	public static void main(String[] args)
	{
		try {
			TestJDOID testJDOID = TestJDOID.create(true, '§', (byte)20, (short)9347, 229, 2094385324L, "Tra/ll_-ala!");
			System.out.println(testJDOID.toString());
			TestJDOID testJDOID2 = new TestJDOID(testJDOID.toString());
			System.out.println(testJDOID2.toString());
			System.out.println("equals: " + testJDOID.equals(testJDOID2));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
