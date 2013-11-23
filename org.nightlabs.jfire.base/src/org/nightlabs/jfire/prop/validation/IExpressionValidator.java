/**
 * 
 */
package org.nightlabs.jfire.prop.validation;

import org.nightlabs.jfire.base.expression.IExpression;

/**
 * @author Daniel Mazurek - Daniel.Mazurek [dot] nightlabs [dot] de
 *
 */
public interface IExpressionValidator 
{
	public IExpression getExpression();
	
	public void setExpression(IExpression expression);
	
	public I18nValidationResult getValidationResult();
}
