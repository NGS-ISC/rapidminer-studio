/**
 * Copyright (C) 2001-2019 by RapidMiner and the contributors
 * 
 * Complete list of developers available at our web site:
 * 
 * http://rapidminer.com
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see http://www.gnu.org/licenses/.
*/
package com.rapidminer.tools.expression.internal.function.rounding;

import com.rapidminer.tools.Ontology;
import com.rapidminer.tools.expression.DoubleCallable;
import com.rapidminer.tools.expression.ExpressionEvaluator;
import com.rapidminer.tools.expression.ExpressionParsingException;
import com.rapidminer.tools.expression.ExpressionType;
import com.rapidminer.tools.expression.FunctionDescription;
import com.rapidminer.tools.expression.FunctionInputException;
import com.rapidminer.tools.expression.internal.SimpleExpressionEvaluator;
import com.rapidminer.tools.expression.internal.function.Abstract2DoubleInputFunction;


/**
 *
 * Abstract class for a {@link Function} that has either one or two double arguments.
 *
 * @author David Arnu
 *
 */
public abstract class Abstract1or2DoubleInputFunction extends Abstract2DoubleInputFunction {

	/**
	 * Constructs an AbstractFunction with {@link FunctionDescription} generated from the arguments
	 * and the function name generated from the description.
	 *
	 * @param i18nKey
	 *            the key for the {@link FunctionDescription}. The functionName is read from
	 *            "gui.dialog.function.i18nKey.name", the helpTextName from ".help", the groupName
	 *            from ".group", the description from ".description" and the function with
	 *            parameters from ".parameters". If ".parameters" is not present, the ".name" is
	 *            taken for the function with parameters.
	 * @param returnType
	 *            the {@link Ontology#ATTRIBUTE_VALUE_TYPE}
	 */
	public Abstract1or2DoubleInputFunction(String i18n, int returnType) {
		super(i18n, FunctionDescription.UNFIXED_NUMBER_OF_ARGUMENTS, returnType);
	}

	@Override
	public ExpressionEvaluator compute(ExpressionEvaluator... inputEvaluators) {
		if (inputEvaluators.length == 2) {
			ExpressionType type = getResultType(inputEvaluators);

			ExpressionEvaluator left = inputEvaluators[0];
			ExpressionEvaluator right = inputEvaluators[1];

			return new SimpleExpressionEvaluator(makeDoubleCallable(left, right), type, isResultConstant(inputEvaluators));
		} else if (inputEvaluators.length == 1) {
			ExpressionType type = getResultType(inputEvaluators);
			ExpressionEvaluator input = inputEvaluators[0];
			return new SimpleExpressionEvaluator(makeDoubleCallable(input), type, isResultConstant(inputEvaluators));

		}
		throw new FunctionInputException("expression_parser.function_wrong_input_two", getFunctionName(), 1, 2,
				inputEvaluators.length);
	}

	/**
	 * Builds a double callable from a single input {@link #compute(double)}, where constant child
	 * results are evaluated.
	 *
	 * @param input
	 *            the input
	 * @return the resulting double callable
	 */
	private DoubleCallable makeDoubleCallable(ExpressionEvaluator input) {
		final DoubleCallable func = input.getDoubleFunction();

		try {
			final Double value = input.isConstant() ? func.call() : Double.NaN;
			if (input.isConstant()) {
				final double result = compute(value);
				return new DoubleCallable() {

					@Override
					public double call() throws Exception {
						return result;
					}
				};
			} else {
				return new DoubleCallable() {

					@Override
					public double call() throws Exception {
						return compute(func.call());
					}
				};
			}
		} catch (ExpressionParsingException e) {
			throw e;
		} catch (Exception e) {
			throw new ExpressionParsingException(e);
		}
	}

	/**
	 * Computes the value a single input argument
	 *
	 * @param value
	 * @return the result of the computation
	 */
	protected abstract double compute(double value);

}
