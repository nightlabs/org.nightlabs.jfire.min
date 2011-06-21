/**
 *
 */
package org.nightlabs.jfire.serverupdate.launcher;

/**
 *  This is a copy of the class <code>org.nightlabs.util.ParameterCoderMinusHexExt</code> because
 * it is not available in this project (<code>JFireServerUpdateLauncher</code> must not have
 * any dependencies!).
 * <p>
 * This {@link ParameterCoder} allows the following characters additional to
 * {@link ParameterCoderMinusHex}:
 * <code>'_' '.' '+'</code>
 * </p>
 *
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 */
class ParameterCoderMinusHexExt extends ParameterCoderMinusHex {

	private static char[] extraAllowedChars = new char[] {'_', '.', '+'};

	public ParameterCoderMinusHexExt() {}

	@Override
	protected boolean literalAllowed(int c) {
		boolean superResult = super.literalAllowed(c);
		if (superResult)
			return true;
		for (char check : extraAllowedChars) {
			if (check == c)
				return true;
		}
		return false;
	}

}
