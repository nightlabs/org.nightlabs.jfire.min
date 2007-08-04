package org.nightlabs.jfire.servermanager.createorganisation;

import java.io.NotSerializableException;
import java.io.Serializable;
import java.util.Date;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.nightlabs.util.Util;

public class CreateOrganisationStatus
implements Serializable
{
	private static final long serialVersionUID = 1L;

	private CreateOrganisationStep createOrganisationStep;
	private Date createDT = new Date();
	private String[] messageArgs;
	private Throwable throwable;
	private transient Throwable throwableTransient;
	private String throwableStackTrace;

	public CreateOrganisationStatus(CreateOrganisationStep createOrganisationStep, Throwable throwable)
	{
		this(
				createOrganisationStep, throwable, (String[])null);
	}

	public CreateOrganisationStatus(CreateOrganisationStep createOrganisationStep,
			String... messageArgs)
	{
		this(createOrganisationStep, null, messageArgs);
	}

	public CreateOrganisationStatus(
			CreateOrganisationStep createOrganisationStep, Throwable throwable, String... messageArgs)
	{
		if (createOrganisationStep == null)
			throw new IllegalArgumentException("createOrganisationStep must not be null!");

		this.createOrganisationStep = createOrganisationStep;
		this.messageArgs = messageArgs;

		if (throwable != null) {
			if (this.messageArgs == null) {
				this.messageArgs = new String[] { throwable.getClass().getName(), throwable.getMessage() };
			}

			this.throwableTransient = throwable;
			this.throwableStackTrace = ExceptionUtils.getStackTrace(throwable);

			try {
				this.throwable = Util.cloneSerializable(throwable);
			} catch (Throwable t) {
				if (ExceptionUtils.indexOfThrowable(t, NotSerializableException.class) < 0)
					Logger.getLogger(CreateOrganisationStatus.class).error("Util.cloneSerializable(throwable) failed with unexpected throwable!", t);

				// else: ignore
			}
		}
	}

	public CreateOrganisationStep getCreateOrganisationStep()
	{
		return createOrganisationStep;
	}

	public Date getCreateDT()
	{
		return createDT;
	}

//	/** // The createOrganisationStep should be used for this purpose!
//	 * Get <code>null</code>, if there is no message, or the message key,
//	 * which identifies a message to be shown in the UI.
//	 * <p>
//	 * Usually, the UI
//	 * would read a message from a properties file using this key and then
//	 * pass it together with the result of {@link #getMessageArgs()} to
//	 * {@link String#format(java.util.Locale, String, Object[])}.
//	 * </p> 
//	 *
//	 * @return a key identifying the message.
//	 */
//	public String getMessageKey()
//	{
//		return messageKey;
//	}
	/**
	 * @return the arguments to the message - can be <code>null</code>.
	 */
	public String[] getMessageArgs()
	{
		return messageArgs;
	}

	/**
	 * Get the StackTrace as string if there was an exception thrown. If there was no Throwable
	 * catched, this is <code>null</code>.
	 *
	 * @return a {@link String} holding the stack-trace of the {@link Throwable} that occured.
	 */
	public String getThrowableStackTrace()
	{
		return throwableStackTrace;
	}

	/**
	 * Get the {@link Throwable}. Unlike {@link #getThrowableStackTrace()}, this method might return
	 * <code>null</code>, even if there was an error. This happens, if the <code>CreateOrganisationStatus</code>
	 * was serialized inbetween and the <code>Throwable</code> was not serializable.
	 *
	 * @return the <code>Throwable</code> that occured.
	 */
	public Throwable getThrowable()
	{
		if (throwableTransient != null)
			return throwableTransient;

		return throwable;
	}
}
