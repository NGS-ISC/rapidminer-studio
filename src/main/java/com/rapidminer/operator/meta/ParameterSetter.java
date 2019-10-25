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
package com.rapidminer.operator.meta;

import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.ProcessSetupError.Severity;
import com.rapidminer.operator.SimpleProcessSetupError;
import com.rapidminer.operator.ports.DummyPortPairExtender;
import com.rapidminer.operator.ports.InputPort;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.operator.ports.PortPairExtender;
import com.rapidminer.operator.ports.quickfix.DictionaryQuickFix;
import com.rapidminer.operator.ports.quickfix.ParameterSettingQuickFix;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeList;
import com.rapidminer.parameter.ParameterTypeString;
import com.rapidminer.parameter.UndefinedParameterError;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


/**
 * Sets a set of parameters. These parameters can either be generated by a
 * {@link ParameterOptimizationOperator} or read by a
 * {@link com.rapidminer.extension.legacy.operator.io.ParameterSetLoader}. This operator is useful, e.g. in the
 * following scenario. If one wants to find the best parameters for a certain learning scheme, one
 * usually is also interested in the model generated with this parameters. While the first is easily
 * possible using a {@link ParameterOptimizationOperator}, the latter is not possible because the
 * {@link ParameterOptimizationOperator} does not return the IOObjects produced within, but only a
 * parameter set. This is, because the parameter optimization operator knows nothing about models,
 * but only about the performance vectors produced within. Producing performance vectors does not
 * necessarily require a model. <br/>
 * To solve this problem, one can use a <code>ParameterSetter</code>. Usually, a process with a
 * <code>ParameterSetter</code> contains at least two operators of the same type, typically a
 * learner. One learner may be an inner operator of the {@link ParameterOptimizationOperator} and
 * may be named &quot;Learner&quot;, whereas a second learner of the same type named
 * &quot;OptimalLearner&quot; follows the parameter optimization and should use the optimal
 * parameter set found by the optimization. In order to make the <code>ParameterSetter</code> set
 * the optimal parameters of the right operator, one must specify its name. Therefore, the parameter
 * list <var>name_map</var> was introduced. Each parameter in this list maps the name of an operator
 * that was used during optimization (in our case this is &quot;Learner&quot;) to an operator that
 * should now use these parameters (in our case this is &quot;OptimalLearner&quot;).
 * 
 * @author Simon Fischer, Ingo Mierswa
 */
public class ParameterSetter extends Operator {

	/**
	 * The parameter name for &quot;A list mapping operator names from the set to operator names in
	 * the process setup.&quot;
	 */
	public static final String PARAMETER_NAME_MAP = "name_map";

	private PortPairExtender dummyPorts = new DummyPortPairExtender("through", getInputPorts(), getOutputPorts());
	private InputPort parameterInput = getInputPorts().createPort("parameter set", ParameterSet.class);
	private OutputPort parameterPassThrough = getOutputPorts().createPassThroughPort("parameter set");

	public ParameterSetter(OperatorDescription description) {
		super(description);
		dummyPorts.start();
		getTransformer().addRule(dummyPorts.makePassThroughRule());
		getTransformer().addPassThroughRule(parameterInput, parameterPassThrough);
	}

	@Override
	public void doWork() throws OperatorException {
		ParameterSet parameterSet = parameterInput.getData(ParameterSet.class);

		Map<String, String> nameMap = new HashMap<String, String>();
		List<String[]> nameList = getParameterList(PARAMETER_NAME_MAP);
		Iterator<String[]> i = nameList.iterator();
		while (i.hasNext()) {
			String[] keyValue = i.next();
			nameMap.put(keyValue[0], keyValue[1]);
		}
		parameterSet.applyAll(getProcess(), nameMap);
		parameterPassThrough.deliver(parameterSet);
		dummyPorts.passDataThrough();
	}

	@Override
	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		ParameterType type = new ParameterTypeList(
				PARAMETER_NAME_MAP,
				"A list mapping operator names from the parameter set to operator names in the process setup.",
				new ParameterTypeString("set_operator_name", "The name of the operator in the parameter set."),
				new ParameterTypeString("operator_name",
						"The name of an operator in the process setup, which parameters should be set in according to the parameter set."));
		type.setExpert(false);
		type.setPrimary(true);
		types.add(type);
		return types;
	}

	@Override
	public int checkProperties() {
		int errorNumber = 0;
		try {
			final List<String[]> nameList = getParameterList(PARAMETER_NAME_MAP);
			;
			if (nameList.size() > 0) {
				// check if valid operator names are present
				HashSet<String> operatorNames = new HashSet<String>();
				operatorNames.addAll(getProcess().getAllOperatorNames());
				for (final String[] pair : nameList) {
					if (!operatorNames.contains(pair[1])) {
						errorNumber++;
						addError(new SimpleProcessSetupError(Severity.ERROR, this.getPortOwner(),
								Collections.singletonList(new DictionaryQuickFix("replacement map", operatorNames, pair[1]) {

									@Override
									public void insertChosenOption(String chosenOption) {
										pair[1] = chosenOption;
										setListParameter(PARAMETER_NAME_MAP, nameList);
									}
								}), "parameter_unknown_operator", PARAMETER_NAME_MAP, pair[1]));
					}
				}
			} else {
				// list empty: add quickfix for opening definition window
				errorNumber++;
				addError(new SimpleProcessSetupError(Severity.ERROR, this.getPortOwner(),
						Collections.singletonList(new ParameterSettingQuickFix(this, PARAMETER_NAME_MAP, "")),
						"parameter_list_undefined", "the mapping of operator names"));
			}
		} catch (UndefinedParameterError e) {
			// list is undefined: Add quickfix for opening definition window
			errorNumber++;
			addError(new SimpleProcessSetupError(Severity.ERROR, this.getPortOwner(),
					Collections.singletonList(new ParameterSettingQuickFix(this, PARAMETER_NAME_MAP, "")),
					"parameter_list_undefined", "the mapping of operator names"));
		}
		return errorNumber + super.checkProperties();
	}
}
