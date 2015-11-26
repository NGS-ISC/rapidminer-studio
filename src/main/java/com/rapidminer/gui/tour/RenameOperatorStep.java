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
package com.rapidminer.gui.tour;

import java.awt.Window;

import com.rapidminer.ProcessSetupListener;
import com.rapidminer.gui.RapidMinerGUI;
import com.rapidminer.gui.flow.ProcessPanel;
import com.rapidminer.gui.properties.OperatorPropertyPanel;
import com.rapidminer.gui.tour.BubbleWindow.AlignedSide;
import com.rapidminer.operator.ExecutionUnit;
import com.rapidminer.operator.Operator;


/**
 * This Subclass of {@link Step} will open a {@link BubbleWindow} which closes if a {@link Operator}
 * of the given Class was renamed to the given String.
 * 
 * @author Philipp Kersting and Thilo Kamradt
 * 
 */

public class RenameOperatorStep extends Step {

	private AlignedSide alignment;
	private Window owner = RapidMinerGUI.getMainFrame().getWindow();
	private BubbleType element;
	private String i18nKey;
	private String targetName;
	private Class<? extends Operator> operatorClass;
	private String buttonKey = "rename_in_processrenderer";
	private ProcessSetupListener listener = null;
	private String dockableKeyBUTTON = OperatorPropertyPanel.PROPERTY_EDITOR_DOCK_KEY;
	private String dockableKeyDOCKABLE = ProcessPanel.PROCESS_PANEL_DOCK_KEY;

	/**
	 * creates a Step which points to the given Operator, the Process-tab or the
	 * renameOperator-Button in the Parameters-tab
	 * 
	 * @param element
	 *            decides to which Component this Step will point
	 * @param preferredAlignment
	 *            offer for alignment but the Class will calculate by itself whether the position is
	 *            usable.
	 * @param i18nKey
	 *            of the message which will be shown in the {@link BubbleWindow}.
	 * @param operatorClass
	 *            Class or Superclass of the {@link Operator} which should be renamed.
	 * @param targetName
	 *            the Name the {@link Operator} should have after this Step.
	 * @param arguments
	 *            arguments to pass thought to the I18N Object
	 */
	public RenameOperatorStep(BubbleType element, AlignedSide preferredAlignment, String i18nKey,
			Class<? extends Operator> operatorClass, String targetName, Object... arguments) {
		super();
		this.alignment = preferredAlignment;
		this.i18nKey = i18nKey;
		this.targetName = targetName;
		this.operatorClass = operatorClass;
		this.element = element;
		this.arguments = arguments;
	}

	@Override
	boolean createBubble() {
		switch (element) {
			case BUTTON:
				bubble = new ButtonBubble(owner, dockableKeyBUTTON, alignment, i18nKey, buttonKey, false, arguments);
				break;
			case DOCKABLE:
				bubble = new DockableBubble(owner, alignment, i18nKey, dockableKeyDOCKABLE, arguments);
				break;
			case OPERATOR:
				bubble = new OperatorBubble(owner, alignment, i18nKey, operatorClass, arguments);
		}
		listener = new ProcessSetupListener() {

			@Override
			public void operatorRemoved(Operator operator, int oldIndex, int oldIndexAmongEnabled) {
				// don't care

			}

			@Override
			public void operatorChanged(Operator operator) {
				if (RenameOperatorStep.this.operatorClass.isInstance(operator) && operator.getName().equals(targetName)) {
					bubble.triggerFire();
					RapidMinerGUI.getMainFrame().getProcess().removeProcessSetupListener(this);
				}
			}

			@Override
			public void operatorAdded(Operator operator) {
				// don't care

			}

			@Override
			public void executionOrderChanged(ExecutionUnit unit) {
				// don't care

			}
		};
		RapidMinerGUI.getMainFrame().getProcess().addProcessSetupListener(listener);
		return true;
	}

	@Override
	protected void stepCanceled() {
		if (listener != null) {
			RapidMinerGUI.getMainFrame().getProcess().removeProcessSetupListener(listener);
		}
	}

	@Override
	public Step[] getPreconditions() {
		if (element == BubbleType.BUTTON) {
			return new Step[] { new PerspectivesStep(1), new NotShowingStep(dockableKeyBUTTON),
					new NotViewableStep(alignment, owner, buttonKey, dockableKeyBUTTON) };
		} else {
			return new Step[] { new PerspectivesStep(1), new NotShowingStep(dockableKeyDOCKABLE) };
		}
	}
}
