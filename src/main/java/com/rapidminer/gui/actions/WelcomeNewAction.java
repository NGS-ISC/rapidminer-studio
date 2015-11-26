/**
 * Copyright (C) 2001-2015 by RapidMiner and the contributors
 *
 * Complete list of developers available at our web site:
 *
 *      http://rapidminer.com
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 */
package com.rapidminer.gui.actions;

import java.awt.Color;
import java.awt.event.ActionEvent;

import com.rapidminer.gui.MainUIState;
import com.rapidminer.gui.Perspectives;


/**
 * Start the corresponding action.
 * 
 * @author Ingo Mierswa
 */
public class WelcomeNewAction extends WelcomeAction {

	private static final long serialVersionUID = 1358354112149248404L;
	private MainUIState mainFrame;

	public WelcomeNewAction(MainUIState mainFrame) {

		super("new", new Color(42, 127, 197));
		this.mainFrame = mainFrame;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		this.mainFrame.getPerspectives().showPerspective(Perspectives.DESIGN);
		this.mainFrame.newProcess();
	}
}
