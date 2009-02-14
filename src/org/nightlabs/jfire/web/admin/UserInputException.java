package org.nightlabs.jfire.web.admin;

/**
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public class UserInputException extends RuntimeException
{
	/**
	 * The serial version of this class.
	 */
	private static final long serialVersionUID = 1L;
	
	private String localizedMessage;
	
	public UserInputException()
	{
		super();
	}

	public UserInputException(String message, Throwable cause)
	{
		super(message, cause);
	}

	public UserInputException(String message)
	{
		super(message);
	}

	public UserInputException(Throwable cause)
	{
		super(cause);
	}

	public UserInputException(String message, String localizedMessage, Throwable cause)
	{
		super(message, cause);
		setLocalizedMessage(localizedMessage);
	}

	public UserInputException(String message, String localizedMessage)
	{
		super(message);
		setLocalizedMessage(localizedMessage);
	}

	/**
	 * Set the localizedMessage.
	 * @param localizedMessage the localizedMessage to set
	 */
	public void setLocalizedMessage(String localizedMessage)
	{
		this.localizedMessage = localizedMessage;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Throwable#getLocalizedMessage()
	 */
	@Override
	public String getLocalizedMessage()
	{
		if(localizedMessage == null)
			return super.getLocalizedMessage();
		else
			return localizedMessage;
	}
}
