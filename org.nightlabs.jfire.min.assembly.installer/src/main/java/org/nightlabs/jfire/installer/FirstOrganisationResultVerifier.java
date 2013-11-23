package org.nightlabs.jfire.installer;

import org.nightlabs.installer.base.VerificationException;
import org.nightlabs.installer.base.defaults.DefaultResultVerifier;
import org.nightlabs.jfire.security.UserLocal;

/**
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public class FirstOrganisationResultVerifier extends DefaultResultVerifier
{
	/* (non-Javadoc)
	 * @see org.nightlabs.installer.base.defaults.DefaultResultVerifier#verify()
	 */
	@Override
	public void verify() throws VerificationException
	{
		super.verify();
		String pwd1 = getInstallationEntity().getResult("40_userPassword.result"); //$NON-NLS-1$
		String pwd2 = getInstallationEntity().getResult("50_userPasswordAgain.result"); //$NON-NLS-1$

		if(pwd1 == null || pwd2 == null)
			throw new IllegalStateException("Illegal password results (at least one is null)"); //$NON-NLS-1$

		if(!pwd1.equals(pwd2))
			throw new VerificationException(Messages.getString("FirstOrganisationResultVerifier.passwordMatchError")); //$NON-NLS-1$

		if (pwd1.length() < UserLocal.MIN_PASSWORD_LENGTH)
			throw new VerificationException(String.format(Messages.getString("FirstOrganisationResultVerifier.passwordTooShort"), UserLocal.MIN_PASSWORD_LENGTH)); //$NON-NLS-1$

		if (!UserLocal.isValidPassword(pwd1))
			throw new VerificationException(Messages.getString("FirstOrganisationResultVerifier.passwordNotValid")); //$NON-NLS-1$
	}
}
