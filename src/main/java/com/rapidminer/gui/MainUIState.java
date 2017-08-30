/*
 *
 */

package com.rapidminer.gui;

import java.awt.Component;
import java.util.List;

import javax.swing.Action;
import javax.swing.JFrame;
import javax.swing.JToolBar;

import com.rapidminer.Process;
import com.rapidminer.ProcessLocation;
import com.rapidminer.ProcessStorageListener;
import com.rapidminer.core.io.data.source.DataSourceFactoryRegistry;
import com.rapidminer.gui.actions.Actions;
import com.rapidminer.gui.actions.RunAction;
import com.rapidminer.gui.actions.SaveAction;
import com.rapidminer.gui.actions.ToggleAction;
import com.rapidminer.gui.operatortree.OperatorTree;
import com.rapidminer.gui.processeditor.ExtendedProcessEditor;
import com.rapidminer.gui.processeditor.NewOperatorEditor;
import com.rapidminer.gui.processeditor.ProcessContextProcessEditor;
import com.rapidminer.gui.processeditor.ProcessEditor;
import com.rapidminer.gui.processeditor.results.ResultDisplay;
import com.rapidminer.gui.properties.OperatorPropertyPanel;
import com.rapidminer.gui.tools.StatusBar;
import com.rapidminer.gui.tools.dialogs.wizards.dataimport.DataImportWizardFactory;
import com.rapidminer.gui.tools.dialogs.wizards.dataimport.DataImportWizardRegistry;
import com.rapidminer.gui.tools.logging.LogViewer;
import com.rapidminer.operator.Operator;
import com.rapidminer.repository.gui.RepositoryBrowser;
import com.vlsolutions.swing.docking.Dockable;
import com.vlsolutions.swing.docking.DockingDesktop;

/**
 * The interface providing the functionality of the UI for RapidMiner.
 *
 * @author Gabor Bakos
 */
public interface MainUIState extends MenusUI, ProcessState {
	public void finishInitialization();

	// Removed in 7.0
	// public void setExpertMode(boolean expert);

	public OperatorPropertyPanel getPropertyPanel();

	/**
	 * Returns a registry for {@link DataImportWizardFactory} instances. The
	 * factories are used to populate menus such as the main import menu.
	 *
	 * @return the registry
	 * @deprecated Use {@link DataSourceFactoryRegistry} instead. Registering a
	 *             {@link DataImportWizardRegistry} will not have an effect
	 *             anymore.
	 */
	@Deprecated
	public DataImportWizardRegistry getDataImportWizardRegistry();

	public LogViewer getLogViewer();

	public NewOperatorEditor getNewOperatorEditor();

	public OperatorTree getOperatorTree();

	public Actions getActions();

	public ResultDisplay getResultDisplay();

	// Removed in 7.0
	// public WelcomeScreen getWelcomeScreen();

	// /** Updates the list of recently used files. */
	// public void updateRecentFileList();

	/*
	 * public void windowOpened(WindowEvent e);
	 *
	 * public void windowClosing(WindowEvent e);
	 *
	 * public void windowClosed(WindowEvent e);
	 *
	 * public void windowIconified(WindowEvent e);
	 *
	 * public void windowDeiconified(WindowEvent e);
	 *
	 * public void windowActivated(WindowEvent e);
	 *
	 * public void windowDeactivated(WindowEvent e);
	 */

	public List<Operator> getSelectedOperators();

	public Operator getFirstSelectedOperator();

	public void selectOperator(Operator currentlySelected);

	public void selectOperators(List<Operator> currentlySelected);

	public DockingDesktop getDockingDesktop();

	/**
	 * @deprecated use {@link #perspectiveController} instead
	 */
	@Deprecated
	public Perspectives getPerspectives();

	public PerspectiveController getPerspectiveController();

	public void handleBrokenProxessXML(ProcessLocation location, String xml, Exception e);

	public OperatorDocumentationBrowser getOperatorDocViewer();

	// Removed in 7.0
	// public ComicRenderer getComicRenderer();

	public void registerDockable(Dockable dockable);

	public ProcessContextProcessEditor getProcessContextEditor();

	RepositoryBrowser getRepositoryBrowser();

	public Component getXMLEditor();

	/**
	 * Returns the status bar of the application.
	 *
	 * @return status bar
	 */
	public StatusBar getStatusBar();

	/**
	 * Sets the window title (RapidMiner + filename + an asterisk if process was
	 * modified.
	 */
	public void setTitle();

	/**
	 * Closes the current process
	 *
	 * @param askForConfirmation
	 *            if <code>true</code>, will prompt the user if he really wants
	 *            to close the current process
	 * @return Saved?
	 */
	public boolean close(final boolean askForConfirmation);

	/**
	 * @return Saved?
	 */
	boolean close();

	/**
	 * @param relaunch
	 */
	void exit(boolean relaunch);

	/** Updates the list of recently used files. */
	public void updateRecentFileList();

	/**
	 * Update the elements of the main tool bar.
	 */
	public void updateToolbar();

	JFrame getWindow();

	// Removed in 7.0 or 6.x
	// Action getImportCsvFileAction();
	//
	// Action getImportExcelFileAction();

	// Removed in 6.x
	// Action getImportXmlFileAction();
	//
	// Action getImportAccessFileAction();
	//
	// Action getImportDatabaseTableAction();

	ToggleAction getValidateAutomaticallyAction();

	Action getPropagateRealMetadataAction();

	// Removed in 7.0
	// Action getRewireRecursively();

	// Removed in 6.x
	// RunRemoteNowAction getRunRemoteNowAction();

	RunAction getRunAction();

	SaveAction getSaveAction();

	ToggleAction getToggleExpertModeAction();

	/**
	 * @deprecated use {@link #addExtendedProcessEditor(ExtendedProcessEditor)}
	 *             instead.
	 */
	@Deprecated
	public void addProcessEditor(final ProcessEditor p);

	/**
	 * Adds the given {@link ExtendedProcessEditor} listener.
	 *
	 * @param p
	 */
	public void addExtendedProcessEditor(final ExtendedProcessEditor p);

	/**
	 * @deprecated use
	 *             {@link #removeExtendedProcessEditor(ExtendedProcessEditor)}
	 *             instead.
	 */
	@Deprecated
	public void removeProcessEditor(final ProcessEditor p);

	/**
	 * Removes the given {@link ExtendedProcessEditor} listener.
	 *
	 * @param p
	 */
	public void removeExtendedProcessEditor(final ExtendedProcessEditor p);

	void removeProcessStorageListener(ProcessStorageListener listener);

	void addProcessStorageListener(ProcessStorageListener listener);

	void addViewSwitchToUndo();

	DockableMenu getDockableMenu();

	/**
	 *
	 * @return the toolbar containg e.g. process run buttons
	 */
	public JToolBar getButtonToolbar();

	/**
	 * Selects and shows a single given operator. Will switch the displayed chain either to the
	 * parent or the selected chain, depending on the provided flag. This can be used to easily
	 * update the view. Convenience method.
	 *
	 * @param currentlySelected
	 * @param showParent
	 * @since 7.5
	 * @see #selectOperators(List)
	 * @see #selectOperator(Operator)
	 */
	public void selectAndShowOperator(Operator currentlySelected, boolean showParent);

	/**
	 * @param process
	 */
	public void setOpenedProcess(Process process);
}