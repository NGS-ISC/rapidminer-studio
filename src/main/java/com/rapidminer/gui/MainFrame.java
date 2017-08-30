/**
 * Copyright (C) 2001-2017 by RapidMiner and the contributors
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
package com.rapidminer.gui;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JToolBar;

import com.rapidminer.Process;
import com.rapidminer.ProcessLocation;
import com.rapidminer.ProcessStorageListener;
import com.rapidminer.RapidMiner;
import com.rapidminer.gui.actions.Actions;
import com.rapidminer.gui.actions.OpenAction;
import com.rapidminer.gui.actions.RunAction;
import com.rapidminer.gui.actions.SaveAction;
import com.rapidminer.gui.actions.ToggleAction;
import com.rapidminer.gui.flow.ProcessPanel;
import com.rapidminer.gui.operatortree.OperatorTree;
import com.rapidminer.gui.processeditor.ExtendedProcessEditor;
import com.rapidminer.gui.processeditor.NewOperatorEditor;
import com.rapidminer.gui.processeditor.ProcessContextProcessEditor;
import com.rapidminer.gui.processeditor.ProcessEditor;
import com.rapidminer.gui.processeditor.results.ResultDisplay;
import com.rapidminer.gui.properties.OperatorPropertyPanel;
import com.rapidminer.gui.tools.SwingTools;
import com.rapidminer.gui.tools.dialogs.ConfirmDialog;
import com.rapidminer.gui.tools.dialogs.wizards.dataimport.DataImportWizardFactory;
import com.rapidminer.gui.tools.dialogs.wizards.dataimport.DataImportWizardRegistry;
import com.rapidminer.gui.tools.logging.LogViewer;
import com.rapidminer.operator.IOContainer;
import com.rapidminer.operator.Operator;
import com.rapidminer.repository.RepositoryLocation;
import com.rapidminer.repository.gui.RepositoryBrowser;
import com.rapidminer.tutorial.gui.TutorialSelector;
import com.vlsolutions.swing.docking.DockGroup;
import com.vlsolutions.swing.docking.Dockable;
import com.vlsolutions.swing.docking.DockingDesktop;

/**
 * The main component class of the RapidMiner GUI. The class holds a lot of Actions that can be used
 * for the tool bar and for the menu bar. MainFrame has methods for handling the process (saving,
 * opening, creating new). It keeps track of the state of the process and enables/disables buttons.
 * It must be notified whenever the process changes and propagates this event to its children. Most
 * of the code is enclosed within the Actions.
 *
 * @author Ingo Mierswa, Simon Fischer, Sebastian Land, Marius Helf, Jan Czogalla
 */
@SuppressWarnings("deprecation")
public class MainFrame extends ApplicationFrame implements WindowListener, MainUIState, ProcessEndHandler {

	private static final long serialVersionUID = 1L;

	/** The property name for &quot;The pixel size of each plot in matrix plots.&quot; */
	public static final String PROPERTY_RAPIDMINER_GUI_PLOTTER_MATRIXPLOT_SIZE = "rapidminer.gui.plotter.matrixplot.size";

	/**
	 * The property name for &quot;The maximum number of rows used for a plotter, using only a
	 * sample of this size if more rows are available.&quot;
	 */
	public static final String PROPERTY_RAPIDMINER_GUI_PLOTTER_ROWS_MAXIMUM = "rapidminer.gui.plotter.rows.maximum";

	/**
	 * The property name for the &quot; The maximum number of examples in a data set for which
	 * default plotter settings will be generated.&quot;
	 */
	public static final String PROPERTY_RAPIDMINER_GUI_PLOTTER_DEFAULT_MAXIMUM = "rapidminer.gui.plotter.default.maximum";

	/**
	 * The property name for &quot;Limit number of displayed classes plotter legends. -1 for no
	 * limit.&quot;
	 */
	public static final String PROPERTY_RAPIDMINER_GUI_PLOTTER_LEGEND_CLASSLIMIT = "rapidminer.gui.plotter.legend.classlimit";

	/** The property name for &quot;The color for minimum values of the plotter legend.&quot; */
	public static final String PROPERTY_RAPIDMINER_GUI_PLOTTER_LEGEND_MINCOLOR = "rapidminer.gui.plotter.legend.mincolor";

	/** The property name for &quot;The color for maximum values of the plotter legend.&quot; */
	public static final String PROPERTY_RAPIDMINER_GUI_PLOTTER_LEGEND_MAXCOLOR = "rapidminer.gui.plotter.legend.maxcolor";

	/**
	 * The property name for &quot;Limit number of displayed classes for colorized plots. -1 for no
	 * limit.&quot;
	 */
	public static final String PROPERTY_RAPIDMINER_GUI_PLOTTER_COLORS_CLASSLIMIT = "rapidminer.gui.plotter.colors.classlimit";

	/** The property name for &quot;Maximum number of states in the undo list.&quot; */
	public static final String PROPERTY_RAPIDMINER_GUI_UNDOLIST_SIZE = "rapidminer.gui.undolist.size";

	/**
	 * The property name for &quot;Maximum number of examples to use for the attribute editor. -1
	 * for no limit.&quot;
	 */
	public static final String PROPERTY_RAPIDMINER_GUI_ATTRIBUTEEDITOR_ROWLIMIT = "rapidminer.gui.attributeeditor.rowlimit";

	/** The property name for &quot;Beep on process success?&quot; */
	public static final String PROPERTY_RAPIDMINER_GUI_BEEP_SUCCESS = "rapidminer.gui.beep.success";

	/** The property name for &quot;Beep on error?&quot; */
	public static final String PROPERTY_RAPIDMINER_GUI_BEEP_ERROR = "rapidminer.gui.beep.error";

	/** The property name for &quot;Beep when breakpoint reached?&quot; */
	public static final String PROPERTY_RAPIDMINER_GUI_BEEP_BREAKPOINT = "rapidminer.gui.beep.breakpoint";

	/**
	 * The property name for &quot;Limit number of displayed rows in the message viewer. -1 for no
	 * limit.&quot;
	 */
	public static final String PROPERTY_RAPIDMINER_GUI_MESSAGEVIEWER_ROWLIMIT = "rapidminer.gui.messageviewer.rowlimit";

	/** The property name for &quot;Shows process info screen after loading?&quot; */
	public static final String PROPERTY_RAPIDMINER_GUI_PROCESSINFO_SHOW = "rapidminer.gui.processinfo.show";

	public static final String PROPERTY_RAPIDMINER_GUI_SAVE_BEFORE_RUN = "rapidminer.gui.save_before_run";

	public static final String PROPERTY_RAPIDMINER_GUI_SAVE_ON_PROCESS_CREATION = "rapidminer.gui.save_on_process_creation";

	/**
	 * The property determining whether or not to switch to result view when results are produced.
	 */
	public static final String PROPERTY_RAPIDMINER_GUI_AUTO_SWITCH_TO_RESULTVIEW = "rapidminer.gui.auto_switch_to_resultview";

	/** Log level of the LoggingViewer. */
	public static final String PROPERTY_RAPIDMINER_GUI_LOG_LEVEL = "rapidminer.gui.log_level";

	private static final int MAX_LOCATION_TITLE_LENGTH = 150;

	// --------------------------------------------------------------------------------

	public static final int EDIT_MODE = 0;
	public static final int RESULTS_MODE = 1;
	public static final int WELCOME_MODE = 2;

	// --------------------------------------------------------------------------------

	private final AbstractUIState state;

	// DOCKING

	// These are probably not required.
	public static final DockGroup DOCK_GROUP_ROOT = AbstractUIState.DOCK_GROUP_ROOT;
	public static final DockGroup DOCK_GROUP_RESULTS = AbstractUIState.DOCK_GROUP_RESULTS;

	// --------------------------------------------------------------------------------
	// LISTENERS And OBSERVERS

	// --------------------------------------------------------------------------------
	/** Creates a new main frame containing the RapidMiner GUI. */
	public MainFrame() {
		this("welcome");
	}

	public MainFrame(final String initialPerspective) {
		super(AbstractUIState.TITLE);
		state = new AbstractUIState(/* initialPerspective, */ this, getContentPane()) {

			@Override
			public boolean close() {
				if (!isChanged()) {
					return true;
				}
				ProcessLocation loc = getProcess().getProcessLocation();
				String locName;
				if (loc != null) {
					locName = loc.getShortName();
				} else {
					locName = "unnamed";
				}
				switch (SwingTools.showConfirmDialog("save", ConfirmDialog.YES_NO_CANCEL_OPTION, locName)) {
					case ConfirmDialog.YES_OPTION:
						SaveAction.save(getProcess());
						// it may happen that save() does not actually save the process, because the
						// user hits cancel in the
						// saveAs dialog or an error occurs. In this case the process won't be marked as
						// unchanged. Thus,
						// we return the process changed status.
						return !isChanged();
					case ConfirmDialog.NO_OPTION:
						// ask for confirmation before stopping the currently running process (if
						// askForConfirmation=true)
						if (/*askForConfirmation*/true) {
							if (RapidMinerGUI.getMainFrame().getProcessState() == Process.PROCESS_STATE_RUNNING
								|| RapidMinerGUI.getMainFrame().getProcessState() == Process.PROCESS_STATE_PAUSED) {
								if (SwingTools.showConfirmDialog("close_running_process",
										ConfirmDialog.YES_NO_OPTION) != ConfirmDialog.YES_OPTION) {
									return false;
								}
							}
						}
						if (getProcessState() != Process.PROCESS_STATE_STOPPED) {
							synchronized (processThread) {
								processThread.stopProcess();
							}
						}
						return true;
					default: // cancel
						return false;
				}
//				if (changed) {
//					final ProcessLocation loc = process.getProcessLocation();
//					String locName;
//					if (loc != null) {
//						locName = loc.getShortName();
//					} else {
//						locName = "unnamed";
//					}
//					switch (SwingTools.showConfirmDialog("save", ConfirmDialog.YES_NO_CANCEL_OPTION, locName)) {
//					case ConfirmDialog.YES_OPTION:
//						SaveAction.save(getProcess());
//
//						// it may happen that save() does not actually save the
//						// process,
//						// because the user hits cancel in the
//						// saveAs dialog or an error occurs. In this case the
//						// process
//						// won't be marked as unchanged. Thus,
//						// we return the process changed status.
//						return !isChanged();
//					case ConfirmDialog.NO_OPTION:
//						if (getProcessState() != Process.PROCESS_STATE_STOPPED) {
//							synchronized (processThread) {
//								processThread.stopProcess();
//							}
//						}
//						return true;
//					default: // cancel
//						return false;
//					}
//				} else {
//					return true;
//				}
			}

			@Override
			public void exit(final boolean relaunch) {
				if (isChanged()) {
					final ProcessLocation loc = getProcess().getProcessLocation();
					String locName;
					if (loc != null) {
						locName = loc.getShortName();
					} else {
						locName = "unnamed";
					}
					switch (SwingTools.showConfirmDialog("save", ConfirmDialog.YES_NO_CANCEL_OPTION, locName)) {
					case ConfirmDialog.YES_OPTION:
						SaveAction.save(getProcess());
						if (isChanged()) {
							return;
						}
						break;
					case ConfirmDialog.NO_OPTION:
						break;
					case ConfirmDialog.CANCEL_OPTION:
					default:
						return;
					}
				} else {
					if (!relaunch) { // in this case we have already confirmed
						final int answer = ConfirmDialog.showConfirmDialog(ApplicationFrame.getApplicationFrame(),
								"exit", ConfirmDialog.YES_NO_OPTION, RapidMinerGUI.PROPERTY_CONFIRM_EXIT,
								ConfirmDialog.YES_OPTION);
						if (answer != ConfirmDialog.YES_OPTION) {
							return;
						}
					}
				}
				stopProcess();
				dispose();
				RapidMiner.quit(relaunch ? RapidMiner.ExitMode.RELAUNCH : RapidMiner.ExitMode.NORMAL);
			}

			@Override
			public void updateRecentFileList() {
				recentFilesMenu.removeAll();
				final List<ProcessLocation> recentFiles = RapidMinerGUI.getRecentFiles();
				int j = 1;
				for (final ProcessLocation recentLocation : recentFiles) {
					final JMenuItem menuItem = new JMenuItem(j + " " + recentLocation.toMenuString());
					menuItem.setMnemonic('0' + j);
					menuItem.addActionListener(new ActionListener() {

						@Override
						public void actionPerformed(final ActionEvent e) {
							if (RapidMinerGUI.getMainFrame().close()) {
								OpenAction.open(recentLocation, true);
							}
						}
					});
					recentFilesMenu.add(menuItem);
					j++;
				}
			}

			/**
			 * Sets the window title (RapidMiner + filename + an asterisk if
			 * process was modified.
			 */
			@Override
			public void setTitle() {
				if (hostname == null) {
					try {
						hostname = " @ " + InetAddress.getLocalHost().getHostName();
					} catch (final UnknownHostException e) {
						hostname = "";
					}
				}

				if (this.process != null) {
					final ProcessLocation loc = process.getProcessLocation();
					if (loc != null) {
						String locString = loc.toString();
						// location string exceeding arbitrary number will be
						// cut into repository name + /.../ + process name
						if (locString.length() > MAX_LOCATION_TITLE_LENGTH) {
							locString = RepositoryLocation.REPOSITORY_PREFIX
									+ process.getRepositoryLocation().getRepositoryName() + RepositoryLocation.SEPARATOR
									+ "..." + RepositoryLocation.SEPARATOR + loc.getShortName();
						}
						MainFrame.this.setTitle(locString + (changed ? "*" : "") + " \u2013 " + TITLE + hostname);
					} else {
						MainFrame.this.setTitle("<new process" + (changed ? "*" : "") + "> \u2013 " + TITLE + hostname);
					}
				} else {
					MainFrame.this.setTitle(TITLE + hostname);
				}
			}
		};
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		addWindowListener(this);
		pack();
		// state.metaDataUpdateQueue.start();
	}

	@Override
	public void finishInitialization() {
		state.finishInitialization();
	}

	/**
	 * Returns a registry for {@link DataImportWizardFactory} instances. The
	 * factories are used to populate menus such as the main import menu.
	 *
	 * @return the registry
	 */
	@Override
	@Deprecated
	public DataImportWizardRegistry getDataImportWizardRegistry() {
		return state.getDataImportWizardRegistry();
	}

	@Override
	public LogViewer getLogViewer() {
		return state.getLogViewer();
	}

	/**
	 * @return the toolbar button for running processes on the Server
	 */
	@Override
	public JButton getRunRemoteToolbarButton() {
		return state.getRunRemoteToolbarButton();
	}

	/**
	 * @deprecated Use {@link #getProcess()} instead
	 */
	@Deprecated
	public final Process getExperiment() {
		return getProcess();
	}

	// ====================================================
	// M A I N A C T I O N S
	// ===================================================

	/**
	 * Sets a new process and registers the MainFrame listener. Please note that
	 * this method does not invoke {@link #processChanged()}. Do so if
	 * necessary.
	 *
	 * @deprecated Use {@link #setProcess(Process, boolean)} instead
	 */
	@Deprecated
	public void setExperiment(final Process process) {
		setProcess(process, true);
	}

	/**
	 * Must be called when the process changed (such that is different from the
	 * process before). Enables the correct actions if the process can be saved
	 * to disk.
	 *
	 * @deprecated this method is no longer necessary (and does nothing) since
	 *             the MainFrame observes the process using an Observer pattern.
	 *             See {@link #processObserver}.
	 */
	@Deprecated
	public void processChanged() {
	}

	/**
	 * Sets the window title (RapidMiner + filename + an asterisk if process was
	 * modified.
	 */
	public void setTitle() {
		state.setTitle();
	}

	// //////////////////// File menu actions ////////////////////

	/**
	 * Closes the current process
	 *
	 * @param askForConfirmation
	 *            if <code>true</code>, will prompt the user if he really wants
	 *            to close the current process
	 * @return
	 */
	@Override
	public boolean close(final boolean askForConfirmation) {
		return state.close(askForConfirmation);
	}

	@Override
	public boolean close() {
		return state.close();
	}

	@Override
	public void exit(final boolean relaunch) {
		state.exit(relaunch);
	}

	/** Updates the list of recently used files. */
	public void updateRecentFileList() {
		state.updateRecentFileList();
	}

	@Override
	public void windowOpened(final WindowEvent e) {
	}

	@Override
	public void windowClosing(final WindowEvent e) {
		exit(false);
	}

	@Override
	public void windowClosed(final WindowEvent e) {
	}

	@Override
	public void windowIconified(final WindowEvent e) {
	}

	@Override
	public void windowDeiconified(final WindowEvent e) {
	}

	@Override
	public void windowActivated(final WindowEvent e) {
	}

	@Override
	public void windowDeactivated(final WindowEvent e) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.rapidminer.gui.MainUIState#getWindow()
	 */
	@Override
	public JFrame getWindow() {
		return this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.rapidminer.gui.MenusUI#addMenuItem(int, int,
	 * javax.swing.JMenuItem)
	 */
	@Override
	public void addMenuItem(final int menuIndex, final int itemIndex, final JMenuItem item) {
		state.addMenuItem(menuIndex, itemIndex, item);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.rapidminer.gui.MenusUI#addMenu(int, javax.swing.JMenu)
	 */
	@Override
	public void addMenu(final int menuIndex, final JMenu menu) {
		state.addMenu(menuIndex, menu);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.rapidminer.gui.MenusUI#addMenuSeparator(int)
	 */
	@Override
	public void addMenuSeparator(final int menuIndex) {
		state.addMenuSeparator(menuIndex);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.rapidminer.gui.MenusUI#removeMenu(int)
	 */
	@Override
	public void removeMenu(final int index) {
		state.removeMenu(index);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.rapidminer.gui.MenusUI#removeMenuItem(int, int)
	 */
	@Override
	public void removeMenuItem(final int menuIndex, final int itemIndex) {
		state.removeMenuItem(menuIndex, itemIndex);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.rapidminer.gui.MenusUI#getFileMenu()
	 */
	@Override
	public JMenu getFileMenu() {
		return state.getFileMenu();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.rapidminer.gui.MenusUI#getToolsMenu()
	 */
	@Override
	@Deprecated
	public JMenu getToolsMenu() {
		return state.getToolsMenu();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.rapidminer.gui.MenusUI#getEditMenu()
	 */
	@Override
	public JMenu getEditMenu() {
		return state.getEditMenu();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.rapidminer.gui.MenusUI#getProcessMenu()
	 */
	@Override
	public JMenu getProcessMenu() {
		return state.getProcessMenu();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.rapidminer.gui.MenusUI#getHelpMenu()
	 */
	@Override
	public JMenu getHelpMenu() {
		return state.getHelpMenu();
	}

	// / LISTENERS

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.rapidminer.gui.MainUIState#addViewSwitchToUndo()
	 */
	@Override
	public void addViewSwitchToUndo() {
		state.addViewSwitchToUndo();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.rapidminer.gui.ProcessState#validateProcess(boolean)
	 */
	@Override
	public void validateProcess(final boolean force) {
		state.validateProcess(force);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.rapidminer.gui.ProcessState#getProcessState()
	 */
	@Override
	public int getProcessState() {
		return state.getProcessState();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.rapidminer.gui.ProcessState#getProcess()
	 */
	@Override
	public Process getProcess() {
		return state.getProcess();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.rapidminer.gui.ProcessState#newProcess()
	 */
	@Override
	public void newProcess() {
		state.newProcess();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.rapidminer.gui.ProcessState#runProcess()
	 */
	@Override
	public void runProcess() {
		state.runProcess();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.rapidminer.gui.ProcessState#runProcess(boolean)
	 */
	@Override
	public void runProcess(final boolean precheckBeforeExecution) {
		state.runProcess(precheckBeforeExecution);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.rapidminer.gui.ProcessState#stopProcess()
	 */
	@Override
	public void stopProcess() {
		state.stopProcess();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.rapidminer.gui.ProcessState#pauseProcess()
	 */
	@Override
	public void pauseProcess() {
		state.pauseProcess();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.rapidminer.gui.ProcessState#setProcess(com.rapidminer.Process,
	 * boolean)
	 */
	@Override
	public void setProcess(final Process process, final boolean newProcess) {
		state.setProcess(process, newProcess);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.rapidminer.gui.ProcessState#setProcess(com.rapidminer.Process,
	 * boolean, boolean)
	 */
	@Override
	public void setProcess(final Process process, final boolean newProcess, final boolean addToUndoList) {
		state.setProcess(process, newProcess, addToUndoList);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.rapidminer.gui.ProcessState#isChanged()
	 */
	@Override
	public boolean isChanged() {
		return state.isChanged();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.rapidminer.gui.ProcessState#undo()
	 */
	@Override
	public void undo() {
		state.undo();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.rapidminer.gui.ProcessState#redo()
	 */
	@Override
	public void redo() {
		state.redo();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.rapidminer.gui.ProcessState#hasUndoSteps()
	 */
	@Override
	public boolean hasUndoSteps() {
		return state.hasUndoSteps();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.rapidminer.gui.ProcessState#hasRedoSteps()
	 */
	@Override
	public boolean hasRedoSteps() {
		return state.hasRedoSteps();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.rapidminer.gui.ProcessState#setOpenedProcess(com.rapidminer.Process,
	 * boolean, java.lang.String)
	 */
	@Override
	public void setOpenedProcess(final Process process, final boolean showInfo, final String sourceName) {
		state.setOpenedProcess(process, showInfo, sourceName);
	}

	// Removed in 6.x
	// /* (non-Javadoc)
	// * @see com.rapidminer.gui.ProcessState#saveAsTemplate()
	// */
	// @Override
	// public void saveAsTemplate() {
	// state.saveAsTemplate();
	// }
	//
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.rapidminer.gui.ProcessState#fireProcessUpdated()
	 */
	@Override
	public void fireProcessUpdated() {
		state.fireProcessUpdated();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.rapidminer.gui.ProcessState#processHasBeenSaved()
	 */
	@Override
	public void processHasBeenSaved() {
		state.processHasBeenSaved();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.rapidminer.gui.MainUIState#getPropertyPanel()
	 */
	@Override
	public OperatorPropertyPanel getPropertyPanel() {
		return state.getPropertyPanel();
	}

	// Changed in 6.x
	// /* (non-Javadoc)
	// * @see com.rapidminer.gui.MainUIState#getMessageViewer()
	// */
	// @Override
	// public LoggingViewer getMessageViewer() {
	// return state.getMessageViewer();
	// }

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.rapidminer.gui.MainUIState#getNewOperatorEditor()
	 */
	@Override
	public NewOperatorEditor getNewOperatorEditor() {
		return state.getNewOperatorEditor();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.rapidminer.gui.MainUIState#getOperatorTree()
	 */
	@Override
	public OperatorTree getOperatorTree() {
		return state.getOperatorTree();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.rapidminer.gui.MainUIState#getActions()
	 */
	@Override
	public Actions getActions() {
		return state.getActions();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.rapidminer.gui.MainUIState#getResultDisplay()
	 */
	@Override
	public ResultDisplay getResultDisplay() {
		return state.getResultDisplay();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.rapidminer.gui.MainUIState#getSelectedOperators()
	 */
	@Override
	public List<Operator> getSelectedOperators() {
		return state.getSelectedOperators();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.rapidminer.gui.MainUIState#getFirstSelectedOperator()
	 */
	@Override
	public Operator getFirstSelectedOperator() {
		return state.getFirstSelectedOperator();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.rapidminer.gui.MainUIState#addProcessEditor(com.rapidminer.gui.
	 * processeditor.ProcessEditor)
	 */
	@Override
	public void addProcessEditor(final ProcessEditor p) {
		state.addProcessEditor(p);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.rapidminer.gui.MainUIState#addExtendedProcessEditor(com.rapidminer.
	 * gui.processeditor.ExtendedProcessEditor)
	 */
	@Override
	public void addExtendedProcessEditor(final ExtendedProcessEditor p) {
		state.addExtendedProcessEditor(p);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.rapidminer.gui.ProcessState#removeProcessEditor(com.rapidminer.gui.
	 * processeditor.ProcessEditor)
	 */
	@Override
	@Deprecated
	public void removeProcessEditor(final ProcessEditor p) {
		state.removeProcessEditor(p);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.rapidminer.gui.MainUIState#removeExtendedProcessEditor(com.rapidminer
	 * .gui.processeditor.ExtendedProcessEditor)
	 */
	@Override
	public void removeExtendedProcessEditor(final ExtendedProcessEditor p) {
		state.removeExtendedProcessEditor(p);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.rapidminer.gui.MainUIState#addProcessStorageListener(com.rapidminer.
	 * ProcessStorageListener)
	 */
	@Override
	public void addProcessStorageListener(final ProcessStorageListener listener) {
		state.addProcessStorageListener(listener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.rapidminer.gui.MainUIState#removeProcessStorageListener(com.
	 * rapidminer.ProcessStorageListener)
	 */
	@Override
	public void removeProcessStorageListener(final ProcessStorageListener listener) {
		state.removeProcessStorageListener(listener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.rapidminer.gui.MainUIState#selectOperator(com.rapidminer.operator.
	 * Operator)
	 */
	@Override
	public void selectOperator(final Operator currentlySelected) {
		state.selectOperator(currentlySelected);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.rapidminer.gui.MainUIState#selectOperators(java.util.List)
	 */
	@Override
	public void selectOperators(final List<Operator> currentlySelected) {
		state.selectOperators(currentlySelected);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.rapidminer.gui.MainUIState#getDockingDesktop()
	 */
	@Override
	public DockingDesktop getDockingDesktop() {
		return state.getDockingDesktop();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.rapidminer.gui.MainUIState#getPerspectives()
	 */
	@Override
	@Deprecated
	public Perspectives getPerspectives() {
		return state.getPerspectives();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.rapidminer.gui.MainUIState#handleBrokenProxessXML(com.rapidminer.
	 * ProcessLocation, java.lang.String, java.lang.Exception)
	 */
	@Override
	public void handleBrokenProxessXML(final ProcessLocation location, final String xml, final Exception e) {
		state.handleBrokenProxessXML(location, xml, e);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.rapidminer.gui.MainUIState#getOperatorDocViewer()
	 */
	@Override
	public OperatorDocumentationBrowser getOperatorDocViewer() {
		return state.getOperatorDocViewer();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.rapidminer.gui.MainUIState#getProcessPanel()
	 */
	@Override
	public ProcessPanel getProcessPanel() {
		return state.getProcessPanel();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.rapidminer.gui.MainUIState#registerDockable(com.vlsolutions.swing.
	 * docking.Dockable)
	 */
	@Override
	public void registerDockable(final Dockable dockable) {
		state.registerDockable(dockable);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.rapidminer.gui.MainUIState#getProcessContextEditor()
	 */
	@Override
	public ProcessContextProcessEditor getProcessContextEditor() {
		return state.getProcessContextEditor();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.rapidminer.gui.MainUIState#getRepositoryBrowser()
	 */
	@Override
	public RepositoryBrowser getRepositoryBrowser() {
		return state.getRepositoryBrowser();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.rapidminer.gui.MainUIState#getXMLEditor()
	 */
	@Override
	public Component getXMLEditor() {
		return state.getXMLEditor();
	}

	// Removed in 6.x
	// /*
	// * (non-Javadoc)
	// * @see com.rapidminer.gui.MainUIState#getImportXmlFileAction()
	// */
	// @Override
	// public Action getImportXmlFileAction() {
	// return state.getImportXmlFileAction();
	// }
	//
	// /* (non-Javadoc)
	// * @see com.rapidminer.gui.MainUIState#getImportAccessFileAction()
	// */
	// @Override
	// public Action getImportAccessFileAction() {
	// return state.getImportAccessFileAction();
	// }
	//
	// /* (non-Javadoc)
	// * @see com.rapidminer.gui.MainUIState#getImportDatabaseTableAction()
	// */
	// @Override
	// public Action getImportDatabaseTableAction() {
	// return state.getImportDatabaseTableAction();
	// }

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.rapidminer.gui.MainUIState#getValidateAutomaticallyAction()
	 */
	@Override
	public ToggleAction getValidateAutomaticallyAction() {
		return state.getValidateAutomaticallyAction();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.rapidminer.gui.MainUIState#getPropagateRealMetadataAction()
	 */
	@Override
	public Action getPropagateRealMetadataAction() {
		return state.getPropagateRealMetadataAction();
	}

	// Removed in 6.x
	// /*
	// * (non-Javadoc)
	// * @see com.rapidminer.gui.MainUIState#getRunRemoteNowAction()
	// */
	// @Override
	// public RunRemoteNowAction getRunRemoteNowAction() {
	// return state.getRunRemoteNowAction();
	// }

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.rapidminer.gui.MainUIState#getRunAction()
	 */
	@Override
	public RunAction getRunAction() {
		return state.getRunAction();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.rapidminer.gui.MainUIState#getSaveAction()
	 */
	@Override
	public SaveAction getSaveAction() {
		return state.getSaveAction();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.rapidminer.gui.MainUIState#getToggleExpertModeAction()
	 */
	@Override
	public ToggleAction getToggleExpertModeAction() {
		return state.getToggleExpertModeAction();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.rapidminer.gui.ProcessEndHandler#processEnded(com.rapidminer.Process,
	 * com.rapidminer.operator.IOContainer)
	 */
	@Override
	public void processEnded(final Process process, final IOContainer results) {
		state.processEnded(process, results);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.rapidminer.gui.MainUIState#getDockableMenu()
	 */
	@Override
	public DockableMenu getDockableMenu() {
		return state.getDockableMenu();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.rapidminer.gui.MainUIState#getButtonToolbar()
	 */
	@Override
	public JToolBar getButtonToolbar() {
		return state.getButtonToolbar();
	}

	@Override
	public JMenu getSettingsMenu() {
		return state.getSettingsMenu();
	}

	@Override
	public JMenu getExtensionsMenu() {
		return state.getExtensionsMenu();
	}

	@Override
	public TutorialSelector getTutorialSelector() {
		return state.getTutorialSelector();
	}

	@Override
	public JMenu getConnectionsMenu() {
		return state.getConnectionsMenu();
	}

	@Override
	public void newProcess(final boolean checkforUnsavedWork) {
		state.newProcess(checkforUnsavedWork);
	}

	@Override
	public PerspectiveController getPerspectiveController() {
		return state.getPerspectiveController();
	}

	@Override
	public void updateToolbar() {
		state.updateToolbar();
	}

	/* (non-Javadoc)
	 * @see com.rapidminer.gui.MainUIState#selectAndShowOperator(com.rapidminer.operator.Operator, boolean)
	 */
	@Override
	public void selectAndShowOperator(Operator currentlySelected, boolean showParent) {
		state.selectAndShowOperator(currentlySelected, showParent);
	}

	/* (non-Javadoc)
	 * @see com.rapidminer.gui.MainUIState#setOpenedProcess(com.rapidminer.Process)
	 */
	@Override
	public void setOpenedProcess(Process process) {
		state.setOpenedProcess(process);
	}
}
