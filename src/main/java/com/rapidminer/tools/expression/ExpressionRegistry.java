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
package com.rapidminer.tools.expression;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.rapidminer.tools.expression.internal.BasicConstants;
import com.rapidminer.tools.expression.internal.StandardFunctionsWithConstants;
import com.rapidminer.tools.expression.internal.StandardOperations;


/**
 * Registry for registering {@link ExpressionParserModule}s. Can be asked for all known
 * {@link ExpressionParserModule}s or all with a given name.
 *
 * @author Gisa Schaefer
 * @since 6.5.0
 */
public enum ExpressionRegistry {

	INSTANCE;

	private Map<String, List<ExpressionParserModule>> modules = new LinkedHashMap<>();

	private ExpressionRegistry() {
		register(BasicConstants.INSTANCE);
		register(StandardFunctionsWithConstants.INSTANCE);
		register(StandardOperations.INSTANCE);
	}

	/**
	 * Registers a {@link ExpressionParserModule} under its name.
	 *
	 * @param module
	 *            the module to register
	 */
	public void register(ExpressionParserModule module) {
		if (module == null) {
			throw new IllegalArgumentException("module must not be null");
		}
		String key = module.getKey();

		if (modules.containsKey(key)) {
			modules.get(key).add(module);
		} else {
			List<ExpressionParserModule> modulesList = new LinkedList<>();
			modulesList.add(module);
			modules.put(key, modulesList);
		}
	}

	/**
	 * Returns all {@link ExpressionParserModule}s registered under the given key.
	 *
	 * @param key
	 *            the key to look for
	 * @return the {@link ExpressionParserModule}s registered under the key or an empty list
	 */
	public List<ExpressionParserModule> get(String key) {
		List<ExpressionParserModule> resultList = modules.get(key);
		if (resultList == null) {
			return Collections.emptyList();
		} else {
			return resultList;
		}
	}

	/**
	 * Returns all registered {@link ExpressionParserModule}s.
	 *
	 * @return a list containing all known {@link ExpressionParserModule}s
	 */
	public List<ExpressionParserModule> getAll() {
		List<ExpressionParserModule> allList = new LinkedList<>();
		for (List<ExpressionParserModule> list : modules.values()) {
			allList.addAll(list);
		}
		return allList;
	}

	/**
	 * Returns the standard {@link ExpressionParserModule}s generated by the core. Use this if you
	 * want to control which custom modules are used.
	 *
	 * @return the modules for standard functions, operations and constants generated by the core
	 */
	public List<ExpressionParserModule> getStandardModules() {
		List<ExpressionParserModule> standardModules = new ArrayList<>(3);
		standardModules.add(BasicConstants.INSTANCE);
		standardModules.add(StandardFunctionsWithConstants.INSTANCE);
		standardModules.add(StandardOperations.INSTANCE);
		return standardModules;
	}

	/**
	 * Returns the module containing the functions for the standard operations (+, -, *, /, %, ^, <,
	 * >, <=, >=, ==, !=, &&, ||, !) used by the {@link ExpressionParser}. Use this if you want to
	 * generate an {@link ExpressionParser} via the {@link ExpressionParserBuilder} with your own
	 * functions and the standard operations.
	 *
	 * @return the module with the functions for the standard operations
	 */
	public ExpressionParserModule getOperations() {
		return StandardOperations.INSTANCE;
	}

}
