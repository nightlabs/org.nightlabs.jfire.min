package org.nightlabs.jfire.prop.validation;


/**
 * Instances of this class contain the result of the validation of a property set element,
 * i.e. a message and the type of the result.
 *
 * @author Tobias Langner <!-- tobias[dot]langner[at]nightlabs[dot]de -->
 */
public class ValidationResult
{
	private String message;
	private ValidationResultType type;

	/**
	 * Creates a new validation result with the given type and message.
	 * @param type The type of the result.
	 * @param message The message of the result.
	 */
	public ValidationResult(ValidationResultType type, String message) {
		super();
		assert type != null;
		assert message != null;

		this.type = type;
		this.message = message;
	}

	/**
	 * Returns the type of the validation result.
	 * @return the type of the validation result.
	 */
	public ValidationResultType getType() {
		return type;
	}

	/**
	 * Returns the message of the validation result.
	 * @return the message of the validation result.
	 */
	public String getMessage() {
		return message;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() 
	{
		// type and message are never null
		return type.hashCode() ^ message.hashCode();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) 
	{
		if(obj == null || !(obj instanceof ValidationResult))
			return false;
		ValidationResult other = (ValidationResult)obj;
		// type and message are never null
		return
			type.equals(other.type) &&
			message == other.message;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() 
	{
		return super.toString()+"[type="+type+",message="+message+"]";
	}
}
