package org.nightlabs.jfire.installer;

import org.nightlabs.installer.Constants;
import org.nightlabs.installer.base.VerificationException;
import org.nightlabs.installer.base.defaults.DefaultResultVerifier;

/**
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public class TestSuiteResultVerifier extends DefaultResultVerifier
{
	/* (non-Javadoc)
	 * @see org.nightlabs.installer.base.defaults.DefaultResultVerifier#verify()
	 */
	@Override
	public void verify() throws VerificationException
	{
		super.verify();
		boolean sendMail =
			Constants.CHECK_TRUE.equals(getInstallationEntity().getResult("10_installTestSuite")) && //$NON-NLS-1$
			(Constants.CHECK_TRUE.equals(getInstallationEntity().getResult("20_sendMailAll.result")) || //$NON-NLS-1$
			 Constants.CHECK_TRUE.equals(getInstallationEntity().getResult("30_sendMailFailure.result")) || //$NON-NLS-1$
			 Constants.CHECK_TRUE.equals(getInstallationEntity().getResult("40_sendMailSkip.result"))); //$NON-NLS-1$
		if(sendMail) {
			String toAddress = getInstallationEntity().getResult("50_mailTo.result"); //$NON-NLS-1$
			if(toAddress == null || "".equals(toAddress)) //$NON-NLS-1$
				throw new VerificationException(Messages.getString("TestSuiteResultVerifier.mailToError")); //$NON-NLS-1$
			String fromAddress = getInstallationEntity().getResult("60_mailFrom.result"); //$NON-NLS-1$
			if(fromAddress == null || "".equals(fromAddress)) //$NON-NLS-1$
				throw new VerificationException(Messages.getString("TestSuiteResultVerifier.mailFromError")); //$NON-NLS-1$
			String smtpHost = getInstallationEntity().getResult("80_mailHost.result"); //$NON-NLS-1$
			if(smtpHost == null || "".equals(smtpHost)) //$NON-NLS-1$
				throw new VerificationException(Messages.getString("TestSuiteResultVerifier.smtpError")); //$NON-NLS-1$
		}
	}
}
