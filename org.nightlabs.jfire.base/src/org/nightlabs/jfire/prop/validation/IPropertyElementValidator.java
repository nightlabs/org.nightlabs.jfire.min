package org.nightlabs.jfire.prop.validation;

import java.io.Serializable;

/**
 * Interface for all validators for {@link org.nightlabs.jfire.prop.PropertySet} elements.
 * @author Tobias Langner <!-- tobias[dot]langner[at]nightlabs[dot]de -->
 * @param <DataElement>
 * @param <StructElement>
 */
public interface IPropertyElementValidator<DataElement, StructElement> extends Serializable
{
	/**
	 * Validates the given dataElement and returns a {@link ValidationResult} indicating
	 * the result of the validation or <code>null</code> if everything is okay, i.e.
	 * the content of the data element is valid.
	 *
	 * @param dataElement The data element to be validated.
	 * @param structElement The structure element that corresponds to the data element.
	 * @return the result of the validation or <code>null</code> if the validations was successful.
	 */
	public ValidationResult validate(DataElement dataElement, StructElement structElement);
}
