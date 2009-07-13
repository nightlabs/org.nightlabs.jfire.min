package org.nightlabs.jfire.base.expression;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.Discriminator;
import javax.jdo.annotations.DiscriminatorStrategy;
import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.FetchGroups;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.Join;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.PersistenceModifier;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import org.nightlabs.jfire.base.expression.id.CompositionID;
import org.nightlabs.jfire.idgenerator.IDGenerator;

/**
 * Abstract base class for {@link IExpression}s that are composed of multiple {@link IExpression}s.
 * Currently there are only the implementations {@link AndCondition} and {@link OrCondition} and
 * I can't think of any other meaningful ones right now.
 *
 * @author Tobias Langner <!-- tobias[dot]langner[at]nightlabs[dot]de -->
 *
 * @see AndCondition
 * @see OrCondition
 *
 *
 *
 * @jdo.persistence-capable
 * 		identity-type="application"
 * 		objectid-class="org.nightlabs.jfire.base.expression.id.CompositionID"
 *    detachable="true"
 *    table="JFireBase_Composition"
 *
 * @jdo.create-objectid-class
 *
 * @jdo.inheritance
 * 		strategy="new-table"
 *
 * @jdo.inheritance-discriminator
 * 		strategy="class-name"
 * 		column="className"
 *
 * @jdo.implements name="org.nightlabs.jfire.base.expression.IExpression"
 *
 * @jdo.fetch-group
 * 		name="IExpression.fullData"
 * 		fetch-groups="default"
 * 		fields="expressions[-1]"
 *
 * @jdo.fetch-group
 * 		name="IStruct.fullData"
 * 		fetch-groups="default"
 * 		fields="expressions[-1]"
 *
 * TODO check fetch-groups (what are necessary, what are not)! I (Marco) just had this exception without the above fetch-group "IStruct.fullData":
 * 23:38:33,687 ERROR [ExceptionHandlerRegistry] javax.jdo.JDODetachedFieldAccessException: You have just attempted to access field "expressions" yet this field was not detached when you detached the object. Either dont access this field, or detach it when detaching the object.
javax.jdo.JDODetachedFieldAccessException: You have just attempted to access field "expressions" yet this field was not detached when you detached the object. Either dont access this field, or detach it when detaching the object.
	at org.nightlabs.jfire.base.expression.Composition.jdoGetexpressions(Composition.java)
	at org.nightlabs.jfire.base.expression.Composition.getExpressions(Composition.java:105)
	at org.nightlabs.jfire.base.expression.OrCondition.evaluate(OrCondition.java:38)
	at org.nightlabs.jfire.prop.validation.ExpressionDataBlockValidator.validate(ExpressionDataBlockValidator.java:98)
	at org.nightlabs.jfire.prop.validation.ExpressionDataBlockValidator.validate(ExpressionDataBlockValidator.java:29)
	at org.nightlabs.jfire.prop.DataBlock.validate(DataBlock.java:355)
	at org.nightlabs.jfire.base.ui.prop.edit.blockbased.AbstractDataBlockEditor.validateDataBlock(AbstractDataBlockEditor.java:156)
	at org.nightlabs.jfire.base.ui.prop.edit.blockbased.AbstractDataBlockEditor.setValidationResultManager(AbstractDataBlockEditor.java:141)
	at org.nightlabs.jfire.base.ui.prop.edit.blockbased.DataBlockGroupEditor.reCreateDataBlockEditors(DataBlockGroupEditor.java:148)
	at org.nightlabs.jfire.base.ui.prop.edit.blockbased.DataBlockGroupEditor.createDataBlockEditors(DataBlockGroupEditor.java:110)
	at org.nightlabs.jfire.base.ui.prop.edit.blockbased.DataBlockGroupEditor.refresh(DataBlockGroupEditor.java:97)
	at org.nightlabs.jfire.base.ui.prop.edit.blockbased.DataBlockGroupEditor.<init>(DataBlockGroupEditor.java:86)
	at org.nightlabs.jfire.base.ui.prop.edit.blockbased.CompoundDataBlockWizardPage.createDataBlockEditors(CompoundDataBlockWizardPage.java:154)
	at org.nightlabs.jfire.base.ui.person.edit.blockbased.special.PersonPersonalDataWizardPage.createPageContents(PersonPersonalDataWizardPage.java:87)
 *
 * @author Tobias Langner <!-- tobias[dot]langner[at]nightlabs[dot]de -->
 */
@PersistenceCapable(
	objectIdClass=CompositionID.class,
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireBase_Composition")
@FetchGroups({
	@FetchGroup(
		fetchGroups={"default"},
		name="IExpression.fullData",
		members=@Persistent(
			name="expressions",
			recursionDepth=-1)),
	@FetchGroup(
		fetchGroups={"default"},
		name="IStruct.fullData",
		members=@Persistent(
			name="expressions",
			recursionDepth=-1))
})
@Discriminator(
	column="className",
	strategy=DiscriminatorStrategy.CLASS_NAME)
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public abstract class Composition implements IExpression
{
	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	@PrimaryKey
	@Column(length=100)
	private String organisationID;

	/**
	 * @jdo.field primary-key="true"
	 */
	@PrimaryKey
	private long compositionID;

	/**
	 * @jdo.field
	 * 		persistence-modifier="persistent"
	 * 		collection-type="collection"
	 * 		dependent-element="true"
	 * 		table="JFireBase_Composition_expressions"
	 *
	 * @jdo.join
	 */
	@Join
	@Persistent(
		dependentElement="true",
		table="JFireBase_Composition_expressions",
		persistenceModifier=PersistenceModifier.PERSISTENT)
	private List<IExpression> expressions;

	/**
	 * @deprecated Only for JDO.
	 */
	@Deprecated
	protected Composition() {
	}

	/**
	 * Creates a new composition of the given expressions with an autogenerated primary key.
	 *
	 * @param expressions The expressions to be composed.
	 */
	public Composition(IExpression... expressions) {
		this(IDGenerator.getOrganisationID(), IDGenerator.nextID(Composition.class), expressions);
	}

	/**
	 * Creates a new composition of the given expressions with the given primary key.
	 *
	 * @param organisationID The organisationID of the new expression.
	 * @param compositionID The ID of the composition.
	 * @param expressions The expressions to be composed.
	 */
	public Composition(String organisationID, long compositionID, IExpression... expressions) {
		assert expressions != null : "expressions != null";

		this.organisationID = organisationID;
		this.compositionID = compositionID;
		this.expressions = new ArrayList<IExpression>(Arrays.asList(expressions));
	}

	/**
	 * Returns the expressions of this composition.
	 * @return the expressions of this composition.
	 */
	public List<IExpression> getExpressions() {
		return Collections.unmodifiableList(expressions);
	}

	public String getOrganisationID() {
		return organisationID;
	}

	public long getCompositionID() {
		return compositionID;
	}

	@Override
	public String toString() {
		return toString("", "  ");
	}

	@Override
	public String toString(String indent, String indentChar) {
		String newIndent = indent + indentChar;
		StringBuilder sb = new StringBuilder();
		sb.append(indent).append("(\n");
		IExpression expr;
		for (Iterator<IExpression> expressionIt = getExpressions().iterator(); expressionIt.hasNext();) {
			expr = expressionIt.next();
			sb.append(expr.toString(newIndent, indentChar));

			if (expressionIt.hasNext())
				sb.append("\n").append(newIndent).append(getOperatorText()).append("\n");
		}

		return sb.append("\n").append(indent).append(")").toString();
	}

	public abstract String getOperatorText();

	public void addExpression(IExpression expression) {
		expressions.add(expression);
	}

	public void removeExpression(IExpression expression) {
		if (expressions.size() > 2) {
			expressions.remove(expression);
		}
		else {
			throw new IllegalStateException("A condition must at least have 2 expressions");
		}
	}

	public void replaceExpression(IExpression originalExpression, IExpression replacementExpression) {
		int index = expressions.indexOf(originalExpression);
		if (index != -1) {
			expressions.set(index, replacementExpression);
		}
		else {
			throw new IllegalArgumentException("Param originalExpression is not contained in this composition");
		}
	}
}
