/**
 * Copyright (C) 2001-2018 by RapidMiner and the contributors
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
package com.rapidminer.operator.ports.quickfix;

import com.rapidminer.operator.ports.InputPort;
import com.rapidminer.operator.ports.OutputPort;


/**
 * @author Simon Fischer
 */
public class ConnectToQuickFix extends AbstractQuickFix {

	private InputPort inputPort;
	private OutputPort outputPort;

	public ConnectToQuickFix(InputPort inputPort, OutputPort outputPort) {
		super(MAX_RATING, false, inputPort.isConnected() ? "reconnect_to" : "connect_to", outputPort
				.getSpec());
		this.inputPort = inputPort;
		this.outputPort = outputPort;
	}

	@Override
	public void apply() {
		if (inputPort.isConnected()) {
			inputPort.getSource().disconnect();
		}
		outputPort.connectTo(inputPort);
	}
}
