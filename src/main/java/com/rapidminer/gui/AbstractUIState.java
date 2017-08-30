/*
 *
 */

package com.rapidminer.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.event.EventListenerList;

import com.rapidminer.BreakpointListener;
import com.rapidminer.Process;
import com.rapidminer.ProcessLocation;
import com.rapidminer.ProcessStorageListener;
import com.rapidminer.RapidMiner;
import com.rapidminer.core.io.data.source.DataSourceFactoryRegistry;
import com.rapidminer.gui.actions.Actions;
import com.rapidminer.gui.actions.AutoWireAction;
import com.rapidminer.gui.actions.ExitAction;
import com.rapidminer.gui.actions.ExportProcessAction;
import com.rapidminer.gui.actions.ImportDataAction;
import com.rapidminer.gui.actions.ImportProcessAction;
import com.rapidminer.gui.actions.NewAction;
import com.rapidminer.gui.actions.NewPerspectiveAction;
import com.rapidminer.gui.actions.OpenAction;
import com.rapidminer.gui.actions.PauseAction;
import com.rapidminer.gui.actions.PropagateRealMetaDataAction;
import com.rapidminer.gui.actions.RedoAction;
import com.rapidminer.gui.actions.RunAction;
import com.rapidminer.gui.actions.SaveAction;
import com.rapidminer.gui.actions.SaveAsAction;
import com.rapidminer.gui.actions.SettingsAction;
import com.rapidminer.gui.actions.StopAction;
import com.rapidminer.gui.actions.ToggleAction;
import com.rapidminer.gui.actions.ToggleExpertModeAction;
import com.rapidminer.gui.actions.UndoAction;
import com.rapidminer.gui.actions.ValidateAutomaticallyAction;
import com.rapidminer.gui.actions.ValidateProcessAction;
import com.rapidminer.gui.actions.export.ShowPrintAndExportDialogAction;
import com.rapidminer.gui.dialog.UnknownParametersInfoDialog;
import com.rapidminer.gui.flow.ErrorTable;
import com.rapidminer.gui.flow.ProcessPanel;
import com.rapidminer.gui.flow.ProcessUndoManager;
import com.rapidminer.gui.flow.processrendering.annotations.model.WorkflowAnnotation;
import com.rapidminer.gui.flow.processrendering.event.ProcessRendererAnnotationEvent;
import com.rapidminer.gui.flow.processrendering.event.ProcessRendererEventListener;
import com.rapidminer.gui.flow.processrendering.event.ProcessRendererModelEvent;
import com.rapidminer.gui.flow.processrendering.event.ProcessRendererOperatorEvent;
import com.rapidminer.gui.flow.processrendering.event.ProcessRendererOperatorEvent.OperatorEvent;
import com.rapidminer.gui.flow.processrendering.model.ProcessRendererModel;
import com.rapidminer.gui.look.Colors;
import com.rapidminer.gui.operatortree.OperatorTree;
import com.rapidminer.gui.operatortree.OperatorTreePanel;
import com.rapidminer.gui.operatortree.actions.CutCopyPasteDeleteAction;
import com.rapidminer.gui.operatortree.actions.ToggleBreakpointItem;
import com.rapidminer.gui.osx.OSXAdapter;
import com.rapidminer.gui.plotter.PlotterPanel;
import com.rapidminer.gui.processeditor.ExtendedProcessEditor;
import com.rapidminer.gui.processeditor.MacroViewer;
import com.rapidminer.gui.processeditor.NewOperatorEditor;
import com.rapidminer.gui.processeditor.ProcessContextProcessEditor;
import com.rapidminer.gui.processeditor.ProcessEditor;
import com.rapidminer.gui.processeditor.XMLEditor;
import com.rapidminer.gui.processeditor.results.ResultDisplay;
import com.rapidminer.gui.processeditor.results.ResultDisplayTools;
import com.rapidminer.gui.properties.OperatorPropertyPanel;
import com.rapidminer.gui.security.PasswordManager;
import com.rapidminer.gui.tools.ProcessGUITools;
import com.rapidminer.gui.tools.ProgressThread;
import com.rapidminer.gui.tools.ResourceAction;
import com.rapidminer.gui.tools.ResourceMenu;
import com.rapidminer.gui.tools.ResultWarningPreventionRegistry;
import com.rapidminer.gui.tools.StatusBar;
import com.rapidminer.gui.tools.SwingTools;
import com.rapidminer.gui.tools.SystemMonitor;
import com.rapidminer.gui.tools.bubble.BubbleWindow;
import com.rapidminer.gui.tools.dialogs.ConfirmDialog;
import com.rapidminer.gui.tools.dialogs.DecisionRememberingConfirmDialog;
import com.rapidminer.gui.tools.dialogs.wizards.dataimport.DataImportWizardFactory;
import com.rapidminer.gui.tools.dialogs.wizards.dataimport.DataImportWizardRegistry;
import com.rapidminer.gui.tools.ioobjectcache.IOObjectCacheViewer;
import com.rapidminer.gui.tools.logging.LogViewer;
import com.rapidminer.operator.IOContainer;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorChain;
import com.rapidminer.operator.UnknownParameterInformation;
import com.rapidminer.operator.ports.Port;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeBoolean;
import com.rapidminer.parameter.ParameterTypeCategory;
import com.rapidminer.parameter.ParameterTypeColor;
import com.rapidminer.parameter.ParameterTypeInt;
import com.rapidminer.repository.gui.RepositoryBrowser;
import com.rapidminer.tools.LogService;
import com.rapidminer.tools.Observable;
import com.rapidminer.tools.Observer;
import com.rapidminer.tools.ParameterService;
import com.rapidminer.tools.ProcessTools;
import com.rapidminer.tools.SystemInfoUtilities;
import com.rapidminer.tools.SystemInfoUtilities.OperatingSystem;
import com.rapidminer.tools.config.ConfigurationManager;
import com.rapidminer.tools.config.gui.ConfigurableDialog;
import com.rapidminer.tools.container.Pair;
import com.rapidminer.tutorial.Tutorial;
import com.rapidminer.tutorial.gui.TutorialBrowser;
import com.rapidminer.tutorial.gui.TutorialSelector;
import com.vlsolutions.swing.docking.DockGroup;
import com.vlsolutions.swing.docking.Dockable;
import com.vlsolutions.swing.docking.DockingContext;
import com.vlsolutions.swing.docking.DockingDesktop;
import com.vlsolutions.swing.toolbars.ToolBarContainer;

/**
 * The abstract base class implementation of the {@link MainUIState} interface.
 * 
 * Created with refactor from {@link MainFrame}.
 * 
 * @author Gabor Bakos
 */
public abstract class AbstractUIState implements MainUIState, ProcessEndHandler {
	/**
	 * This listener takes care of changes relevant to the {@link MainFrame} itself, such as the
	 * {@link ProcessThread} and saving, loading or setting a process.
	 *
	 * @since 7.5
	 * @author Jan Czogalla
	 */
	private class MainProcessListener implements ExtendedProcessEditor, ProcessStorageListener {

		private Process oldProcess;

		@Override
		public void processChanged(Process process) {
			Process currentProcess = oldProcess;
			oldProcess = process;
			boolean firstProcess = currentProcess == null;
			if (!firstProcess) {
				currentProcess.removeObserver(processObserver);
				if (currentProcess.getProcessState() != Process.PROCESS_STATE_STOPPED) {
					if (processThread != null) {
						processThread.stopProcess();
					}
				}
			}

			if (process != null) {

				synchronized (process) {
					processThread = new ProcessThread(process);
					process.addObserver(processObserver, true);
					process.addBreakpointListener(breakpointListener);
					if (VALIDATE_AUTOMATICALLY_ACTION.isSelected()) {
						// not running at this point!
						validateProcess(true);
					}
				}
			}
			processPanel.getProcessRenderer().repaint();
			setTitle();
			getStatusBar().clearSpecialText();
			processPanel.getViewPort().firePropertyChange(ProcessPanel.SCROLLER_UPDATE, false, true);
		}

		@Override
		public void setSelection(List<Operator> selection) {

		}

		@Override
		public void processUpdated(Process process) {
			setTitle();
			if (getProcess().getProcessLocation() != null) {
				AbstractUIState.this.SAVE_ACTION.setEnabled(processModel.hasChanged());
			}
			processPanel.getProcessRenderer().repaint();
		}

		@Override
		public void stored(Process process) {
			SAVE_ACTION.setEnabled(false);
			setTitle();
			updateRecentFileList();
		}

		@Override
		public void opened(Process process) {
			if (process.getImportMessage() != null && process.getImportMessage().contains("error")) {
				SwingTools.showLongMessage("import_message", process.getImportMessage());
			}

			SwingUtilities.invokeLater(new Runnable() {

				@Override
				public void run() {
					SAVE_ACTION.setEnabled(false);
					setTitle();
				}
			});

			List<UnknownParameterInformation> unknownParameters = null;
			synchronized (process) {
				RapidMinerGUI.useProcessFile(process);
				unknownParameters = process.getUnknownParameters();
			}

			updateRecentFileList();

			// show unsupported parameters info?
			if (unknownParameters != null && unknownParameters.size() > 0) {
				final UnknownParametersInfoDialog unknownParametersInfoDialog = new UnknownParametersInfoDialog(
						AbstractUIState.this.getWindow(), unknownParameters);
				if (SwingUtilities.isEventDispatchThread()) {
					unknownParametersInfoDialog.setVisible(true);
				} else {
					try {
						SwingUtilities.invokeAndWait(new Runnable() {

							@Override
							public void run() {
								unknownParametersInfoDialog.setVisible(true);
							}
						});
					} catch (Exception e) {
						LogService.getRoot().log(Level.WARNING, "Error opening the unknown parameter dialog: " + e, e);
					}
				}
			}
		}

		@Override
		public void processViewChanged(Process process) {}

	}


	/**
	 * The property name for &quot;Maximum number of states in the undo
	 * list.&quot;
	 */
	public static final String PROPERTY_RAPIDMINER_GUI_UNDOLIST_SIZE = "rapidminer.gui.undolist.size";

	/** Updates the list of recently used files. */
	public abstract void updateRecentFileList();

	/**
	 * Update the elements of the main tool bar.
	 */
	public void updateToolbar() {
		toolBar.update();
	}

	/**
	 * Sets the window title (RapidMiner + filename + an asterisk if process was
	 * modified.
	 */
	public abstract void setTitle();

	// public abstract void processChanged();

	// public abstract void setExperiment(Process process);

	// public abstract Process getExperiment();

	// public abstract void changeMode(int mode);

	/**
	 * The property name for &quot;Shows process info screen after
	 * loading?&quot;
	 */
	public static final String PROPERTY_RAPIDMINER_GUI_PROCESSINFO_SHOW = "rapidminer.gui.processinfo.show";
	public static final String PROPERTY_RAPIDMINER_GUI_SAVE_BEFORE_RUN = "rapidminer.gui.save_before_run";
	public static final String PROPERTY_RAPIDMINER_GUI_SAVE_ON_PROCESS_CREATION = "rapidminer.gui.save_on_process_creation";
	/** The title of the frame. */
	public static final String TITLE = "RapidMiner";
	public static final int EDIT_MODE = 0;
	public static final int RESULTS_MODE = 1;
	public static final int WELCOME_MODE = 2;
	public final transient Action AUTO_WIRE = new AutoWireAction(this);
//Removed in 7.x?
//	public final transient Action AUTO_WIRE_RECURSIVELY = new AutoWireAction(
//			this, "wire_recursive", CompatibilityLevel.PRE_VERSION_5, true,
//			true);
//	public final transient Action REWIRE = new AutoWireAction(this, "rewire",
//			CompatibilityLevel.PRE_VERSION_5, false, false);
//	public final transient Action REWIRE_RECURSIVELY = new AutoWireAction(this,
//			"rewire_recursive", CompatibilityLevel.PRE_VERSION_5, true, false);
	public final transient Action NEW_ACTION = new NewAction(this);
	public final transient Action OPEN_ACTION = new OpenAction();
	public final transient SaveAction SAVE_ACTION = new SaveAction();
	public final transient Action SAVE_AS_ACTION = new SaveAsAction();
	//Removed in 6.x
//	public final transient Action SAVE_AS_TEMPLATE_ACTION = new SaveAsTemplateAction(
//			this);
//	public final transient Action MANAGE_TEMPLATES_ACTION = new ManageTemplatesAction();
//	public final transient Action MANAGE_BUILDING_BLOCKS_ACTION = new ManageBuildingBlocksAction(
//			this);
//	public final transient Action PRINT_ACTION = new PrintAction(
//			this.getWindow(), "all");
//	public final transient Action PRINT_PREVIEW_ACTION = new PrintPreviewAction(
//			this.getWindow(), "all");
//	public final transient Action PAGE_SETUP_ACTION = new PageSetupAction();
	public final transient ToggleAction PROPAGATE_REAL_METADATA_ACTION = new PropagateRealMetaDataAction(this);
//Removed in 7.0
//	public final transient Action IMPORT_CSV_FILE_ACTION = new ResourceAction(
//			"import_csv_file") {
//
//		private static final long serialVersionUID = 4632580631996166900L;
//
//		@Override
//		public void actionPerformed(final ActionEvent e) {
//			// CSVImportWizard wizard = new CSVImportWizard("import_csv_file");
//			CSVImportWizard wizard;
//			try {
//				wizard = new CSVImportWizard();
//				wizard.setVisible(true);
//			} catch (final OperatorException e1) {
//				// should not happen if operator == null
//				throw new RuntimeException("Failed to create wizard.", e1);
//			}
//		}
//	};
//	public final transient Action IMPORT_EXCEL_FILE_ACTION = new ResourceAction(
//			"import_excel_sheet") {
//
//		private static final long serialVersionUID = 975782163819088729L;
//
//		@Override
//		public void actionPerformed(final ActionEvent e) {
//			try {
//				final ExcelImportWizard wizard = new ExcelImportWizard();
//				wizard.setVisible(true);
//			} catch (final OperatorException e1) {
//				// should not happen if operator == null
//				throw new RuntimeException("Failed to create wizard.", e1);
//			}
//		}
//	};
	//Removed in 6.x
//	public final transient Action IMPORT_XML_FILE_ACTION = new ResourceAction(
//			"import_xml_file") {
//
//		private static final long serialVersionUID = 1L;
//
//		@Override
//		public void actionPerformed(final ActionEvent e) {
//			try {
//				final XMLImportWizard wizard = new XMLImportWizard();
//				wizard.setVisible(true);
//			} catch (final OperatorException e1) {
//				// should not happen if operator == null
//				throw new RuntimeException("Failed to create wizard.", e1);
//			}
//		}
//	};
//	public final transient Action IMPORT_ACCESS_FILE_ACTION = new ResourceAction(
//			"import_access_table") {
//
//		private static final long serialVersionUID = 3725652002686421768L;
//
//		@Override
//		public void actionPerformed(final ActionEvent e) {
//			AccessImportWizard wizard;
//			try {
//				wizard = new AccessImportWizard("import_access_table");
//				wizard.setVisible(true);
//			} catch (final SQLException e1) {
//				SwingTools.showSimpleErrorMessage(
//						"db_connection_failed_simple", e1, e1.getMessage());
//			}
//		}
//	};
//	public final transient Action IMPORT_DATABASE_TABLE_ACTION = new ResourceAction(
//			"import_database_table") {
//
//		private static final long serialVersionUID = 3725652002686421768L;
//
//		@Override
//		public void actionPerformed(final ActionEvent e) {
//			DatabaseImportWizard wizard;
//			try {
//				wizard = new DatabaseImportWizard("import_database_table");
//				wizard.setVisible(true);
//			} catch (final SQLException e1) {
//				SwingTools.showSimpleErrorMessage(
//						"db_connection_failed_simple", e1, e1.getMessage());
//			}
//		}
//	};
	private final transient Action importDataAction = new ImportDataAction();
	public final transient Action IMPORT_PROCESS_ACTION = new ImportProcessAction();
	public final transient Action EXPORT_PROCESS_ACTION = new ExportProcessAction();
	public final transient Action EXPORT_ACTION = new ShowPrintAndExportDialogAction(false);
	public final transient Action EXIT_ACTION = new ExitAction(this);
	//Removed in 6.x
//	public final transient RunRemoteNowAction RUN_REMOTE_NOW_ACTION = new RunRemoteNowAction(this);
	public final transient RunAction RUN_ACTION = new RunAction(this);
	public final transient Action PAUSE_ACTION = new PauseAction(this);
	public final transient Action STOP_ACTION = new StopAction(this);
	//Removed in 6.x
//	public final transient Action RUN_REMOTE_ACTION = new RunRemoteAction();
	public final transient Action VALIDATE_ACTION = new ValidateProcessAction(
			this);
	public final transient ToggleAction VALIDATE_AUTOMATICALLY_ACTION = new ValidateAutomaticallyAction();
	//Removed in 6.x
	//public final transient Action OPEN_TEMPLATE_ACTION = new WizardAction(this);
	
	private transient JButton runRemoteToolbarButton;

	public final transient Action NEW_PERSPECTIVE_ACTION = new NewPerspectiveAction(
			this);
	public final transient Action SETTINGS_ACTION = new SettingsAction();
	public final transient ToggleAction TOGGLE_EXPERT_MODE_ACTION = new ToggleExpertModeAction();
//Removed in 7.0
//	public final transient Action COMIC_ACTION = new ComicAction();
//	public final transient Action TOUR_ACTION = new TourAction();
	public final transient Action UNDO_ACTION = new UndoAction(this);
	public final transient Action REDO_ACTION = new RedoAction(this);
//Removed in 7.0
//	public final transient Action ANOVA_CALCULATOR_ACTION = new AnovaCalculatorAction();
	public final transient Action MANAGE_CONFIGURABLES_ACTION = new ResourceAction(true, "manage_configurables") {

		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(final ActionEvent e) {
			ConfigurableDialog dialog = new ConfigurableDialog(getProcess());
			dialog.setVisible(true);
		}
	};
	
	//Removed in 6.x
//	public final transient Action CHECK_FOR_JDBC_DRIVERS_ACTION = new CheckForJDBCDriversAction();
//	public final transient Action MANAGE_DB_CONNECTIONS_ACTION = new ResourceAction(
//			true, "manage_db_connections") {
//
//		private static final long serialVersionUID = 2457587046500212869L;
//
//		@Override
//		public void actionPerformed(final ActionEvent e) {
//			final ManageDatabaseConnectionsDialog dialog = new ManageDatabaseConnectionsDialog();
//			dialog.setVisible(true);
//		}
//	};
	
	
	// --------------------------------------------------------------------------------
	
	// DOCKING
	public static final DockGroup DOCK_GROUP_ROOT = new DockGroup("root");
	public static final DockGroup DOCK_GROUP_RESULTS = new DockGroup("results");
	protected final DockingContext dockingContext = new DockingContext();
	protected final DockingDesktop dockingDesktop = new DockingDesktop(
			"mainDesktop", dockingContext);
	private final MainProcessListener mainProcessListener = new MainProcessListener();
	protected final Actions actions = new Actions(this);
//Removed in 7.0
//	protected final WelcomeScreen welcomeScreen = new WelcomeScreen(this);
	protected final ResultDisplay resultDisplay = ResultDisplayTools
			.makeResultDisplay();
	protected final EventListenerList processEditors = new EventListenerList();

	protected final ProcessPanel processPanel = new ProcessPanel(this);
	protected final LogViewer logViewer = new LogViewer(this);
	protected final SystemMonitor systemMonitor = new SystemMonitor();
	
//	private List<Operator> selectedOperators = Collections.emptyList();

	protected final OperatorDocumentationBrowser operatorDocumentationBrowser = new OperatorDocumentationBrowser();
	protected final OperatorTreePanel operatorTree = new OperatorTreePanel(this);
	protected final ErrorTable errorTable = new ErrorTable(this);
	protected final OperatorPropertyPanel propertyPanel = new OperatorPropertyPanel(
			this);
	protected final XMLEditor xmlEditor = new XMLEditor(this);
	//Removed in 6.x
//	protected final CommentEditor commentEditor = new CommentEditor();
	protected final ProcessContextProcessEditor processContextEditor = new ProcessContextProcessEditor();
	private final ProcessRendererModel processModel = processPanel.getProcessRenderer().getModel();
//Removed in 7.0
//	protected final ComicRenderer comicRenderer = new ComicRenderer(processPanel.getProcessRenderer(), this);
	protected final NewOperatorEditor newOperatorEditor = new NewOperatorEditor(processPanel.getProcessRenderer().getDragListener());
	protected final RepositoryBrowser repositoryBrowser = new RepositoryBrowser(processPanel.getProcessRenderer().getDragListener());
	
	protected final MacroViewer macroViewer = new MacroViewer();

	private final IOObjectCacheViewer ioobjectCacheViewer = new IOObjectCacheViewer(RapidMiner.getGlobalIOObjectCache());
	//Removed in 6.x
//	protected final RemoteProcessViewer remoteProcessViewer = new RemoteProcessViewer();

	private final PerspectiveController perspectiveController = new PerspectiveController(dockingContext);
	private final TutorialSelector tutorialSelector = new TutorialSelector(this, perspectiveController.getModel());
	private final TutorialBrowser tutorialBrowser = new TutorialBrowser(tutorialSelector);

	/**
	 * @deprecated use {@link #perspectiveController} instead
	 */
	@Deprecated
	private final Perspectives perspectives = new Perspectives(perspectiveController);

	protected boolean changed = false;
	protected boolean tutorialMode = false;
	private int undoIndex;

	/** the bubble which displays a warning that no result ports are connected */
	private BubbleWindow noResultConnectionBubble;
	/** the bubble which displays a warning that a port must receive input but is not connected */
	private BubbleWindow missingInputBubble;
	/**
	 * the bubble which displays a warning that a parameter must be set as he has no default value
	 */
	private BubbleWindow missingParameterBubble;
	
	protected final JMenuBar menuBar;
	protected final MainToolBar toolBar;
	protected final JMenu fileMenu;
//Removed in 7.0
//	protected final JMenu importMenu;
	protected final JMenu editMenu;
	protected final JMenu processMenu;
//Removed in 7.0
//	protected final JMenu toolsMenu;
	protected final JMenu settingsMenu;
	protected final JMenu connectionsMenu;
	protected final JMenu viewMenu;
	protected final JMenu helpMenu;
	protected final JMenu extensionsMenu;
	
	private DockableMenu dockableMenu;
	
	protected final JMenu recentFilesMenu = new ResourceMenu("recent_files");

	private final ProcessUndoManager undoManager = new ProcessUndoManager();

	private final LinkedList<ProcessStorageListener> storageListeners = new LinkedList<ProcessStorageListener>();

	/** XML representation of the process at last validation. */
	private String lastProcessXML;

	/** the OperatorChain which was last viewed */
	private OperatorChain lastProcessDisplayedOperatorChain;

	private Insets menuBarInsets = new Insets(0, 0, 0, 5);
	
	/**
	 * The host name of the system. Might be empty (no host name will be shown)
	 * and will be initialized in the first call of {@link #setTitle()}.
	 */
	protected String hostname = null;
	protected transient Process process = null;
	protected transient ProcessThread processThread;
	protected final MetaDataUpdateQueue metaDataUpdateQueue = new MetaDataUpdateQueue(
			this);
	
	@Deprecated
	private transient final DataImportWizardRegistry importWizardRegistry = new DataImportWizardRegistry() {

		private final List<DataImportWizardFactory> factories = new ArrayList<>();

		@Override
		public void register(DataImportWizardFactory factory) {
			if (factory == null) {
				throw new IllegalArgumentException("factory must not be null");
			}
			synchronized (factories) {
				factories.add(factory);
//Removed in 7.0
//				importMenu.add(factory.createAction());
			}
		}

		@Override
		public List<DataImportWizardFactory> getFactories() {
			synchronized (factories) {
				return new ArrayList<>(factories);
			}
		}
	};


	// --------------------------------------------------------------------------------
	// LISTENERS And OBSERVERS

	private final PerspectiveChangeListener perspectiveChangeListener = new PerspectiveChangeListener() {
		@Override
		public void perspectiveChangedTo(Perspective perspective) {
			// check all ConditionalActions on perspective switch
			getActions().enableActions();

			// try to request focus for the process renderer so actions are enabled after
			// perspective switch and
			// ProcessRenderer is visible
			if (getProcessPanel().getProcessRenderer().isShowing()) {
				getProcessPanel().getProcessRenderer().requestFocusInWindow();
			}
		}
	};
	
	private long lastUpdate = 0;
	private final Timer updateTimer = new Timer(500, new ActionListener() {

		@Override
		public void actionPerformed(final ActionEvent e) {
			updateProcessNow();
		}
	}) {

		private static final long serialVersionUID = 1L;
		{
			setRepeats(false);
		}
	};

	/**
	 * @deprecated since 7.5; a view is automatically pushed on the undo stack when using
	 *             {@link ProcessRendererModel#setDisplayedChain(OperatorChain)}
	 */
	@Deprecated
	public void addViewSwitchToUndo() {}

	/** Lets the process model check for changes in the process */
	private void updateProcessNow() {
		lastUpdate = System.currentTimeMillis();
		if (processModel.checkForNewUndoStep()) {
			validateProcess(false);
		}
		processPanel.getProcessRenderer().repaint();
	}

	@Override
	public void validateProcess(final boolean force) {
		if (force || getProcessState() != Process.PROCESS_STATE_RUNNING) {
			metaDataUpdateQueue.validate(getProcess(), force || VALIDATE_AUTOMATICALLY_ACTION.isSelected());
		} else {
			processModel.fireProcessUpdated();
		}
	}

	public boolean isProcessRendererFocused() {
		return processPanel.getProcessRenderer().hasFocus();
	}

	private transient final Observer<Process> processObserver = new Observer<Process>() {

		@Override
		public void update(Observable<Process> observable, Process arg) {
			// if (process.getProcessState() == Process.PROCESS_STATE_RUNNING) {
			// return;
			// }
			if (System.currentTimeMillis() - lastUpdate > 500) {
				updateProcessNow();
			} else {
				if (getProcessState() == Process.PROCESS_STATE_RUNNING) {
					if (!updateTimer.isRunning()) {
						updateTimer.start();
					}
				} else {
					updateProcessNow();
				}
			}
		}
	};

	private transient final BreakpointListener breakpointListener = new BreakpointListener() {

		@Override
		public void breakpointReached(Process process, final Operator operator, final IOContainer ioContainer, int location) {
			if (process.equals(getProcess())) {
				RUN_ACTION.setState(process.getProcessState());
				ProcessThread.beep("breakpoint");
				Window window = getWindow();
				if (window != null) {
					window.toFront();
				}
				selectAndShowOperator(operator, true);
				resultDisplay.showData(ioContainer, "Breakpoint in " + operator.getName() + ", application " + operator.getApplyCount());
			}
		}

		/** Since the mainframe triggers the resume itself this method does nothing. */
		@Override
		public void resume() {
			RUN_ACTION.setState(getProcessState());
		}
	};

	private JToolBar buttonToolbar;
	private final JFrame window;

	/**
	 * Registers all RapidMiner GUI properties. This must often be done centrally in
	 * mainframe to ensure that the properties are set when the GUI is started.
	 */
	static {
		ParameterService.registerParameter(new ParameterTypeInt(MainFrame.PROPERTY_RAPIDMINER_GUI_PLOTTER_MATRIXPLOT_SIZE, "The pixel size of each plot in matrix plots.", 1, Integer.MAX_VALUE, 200));
		ParameterService.registerParameter(new ParameterTypeInt(MainFrame.PROPERTY_RAPIDMINER_GUI_PLOTTER_ROWS_MAXIMUM, "The maximum number of rows used for a plotter, using only a sample of this size if more rows are available.", 1, Integer.MAX_VALUE, PlotterPanel.DEFAULT_MAX_NUMBER_OF_DATA_POINTS));
		ParameterService.registerParameter(new ParameterTypeInt(MainFrame.PROPERTY_RAPIDMINER_GUI_PLOTTER_LEGEND_CLASSLIMIT, "Limit number of displayed classes plotter legends. -1 for no limit.", -1, Integer.MAX_VALUE, 10));
		ParameterService.registerParameter(new ParameterTypeColor(MainFrame.PROPERTY_RAPIDMINER_GUI_PLOTTER_LEGEND_MINCOLOR, "The color for minimum values of the plotter legend.", java.awt.Color.blue));
		ParameterService.registerParameter(new ParameterTypeColor(MainFrame.PROPERTY_RAPIDMINER_GUI_PLOTTER_LEGEND_MAXCOLOR, "The color for maximum values of the plotter legend.", java.awt.Color.red));
		ParameterService.registerParameter(new ParameterTypeInt(MainFrame.PROPERTY_RAPIDMINER_GUI_PLOTTER_COLORS_CLASSLIMIT, "Limit number of displayed classes for colorized plots. -1 for no limit.", -1, Integer.MAX_VALUE, 10));
		ParameterService.registerParameter(new ParameterTypeInt(MainFrame.PROPERTY_RAPIDMINER_GUI_UNDOLIST_SIZE, "Maximum number of states in the undo list.", 1, Integer.MAX_VALUE, 100));
		ParameterService.registerParameter(new ParameterTypeInt(MainFrame.PROPERTY_RAPIDMINER_GUI_ATTRIBUTEEDITOR_ROWLIMIT, "Maximum number of examples to use for the attribute editor. -1 for no limit.", -1, Integer.MAX_VALUE, 50));
		ParameterService.registerParameter(new ParameterTypeBoolean(MainFrame.PROPERTY_RAPIDMINER_GUI_BEEP_SUCCESS, "Beep on process success?", false));
		ParameterService.registerParameter(new ParameterTypeBoolean(MainFrame.PROPERTY_RAPIDMINER_GUI_BEEP_ERROR, "Beep on error?", false));
		ParameterService.registerParameter(new ParameterTypeBoolean(MainFrame.PROPERTY_RAPIDMINER_GUI_BEEP_BREAKPOINT, "Beep when breakpoint reached?", false));
		ParameterService.registerParameter(new ParameterTypeInt(MainFrame.PROPERTY_RAPIDMINER_GUI_MESSAGEVIEWER_ROWLIMIT, "Limit number of displayed rows in the message viewer. -1 for no limit.", -1, Integer.MAX_VALUE, 1000));
		//Removed in 6.x
//		ParameterService.registerParameter(new ParameterTypeColor(MainFrame.PROPERTY_RAPIDMINER_GUI_MESSAGEVIEWER_HIGHLIGHT_NOTES, "The color for notes in the message viewer.", new java.awt.Color(51, 151, 51)));
//		ParameterService.registerParameter(new ParameterTypeColor(MainFrame.PROPERTY_RAPIDMINER_GUI_MESSAGEVIEWER_HIGHLIGHT_WARNINGS, "The color for warnings in the message viewer.", new java.awt.Color(51, 51, 255)));
//		ParameterService.registerParameter(new ParameterTypeColor(MainFrame.PROPERTY_RAPIDMINER_GUI_MESSAGEVIEWER_HIGHLIGHT_ERRORS, "The color for errors in the message viewer.", new java.awt.Color(255, 51, 204)));
//		ParameterService.registerParameter(new ParameterTypeColor(MainFrame.PROPERTY_RAPIDMINER_GUI_MESSAGEVIEWER_HIGHLIGHT_LOGSERVICE, "The color for the logging service indicator in the message viewer.", new java.awt.Color(184, 184, 184)));
		ParameterService.registerParameter(new ParameterTypeBoolean(MainFrame.PROPERTY_RAPIDMINER_GUI_PROCESSINFO_SHOW, "Shows process info screen after loading?", true));
		ParameterService.registerParameter(new ParameterTypeCategory(MainFrame.PROPERTY_RAPIDMINER_GUI_SAVE_BEFORE_RUN, "Save process before running process?", DecisionRememberingConfirmDialog.PROPERTY_VALUES, DecisionRememberingConfirmDialog.FALSE));
		ParameterService.registerParameter(new ParameterTypeBoolean(MainFrame.PROPERTY_RAPIDMINER_GUI_SAVE_ON_PROCESS_CREATION, "Save process when creating them?", false));
		ParameterService.registerParameter(new ParameterTypeCategory(MainFrame.PROPERTY_RAPIDMINER_GUI_AUTO_SWITCH_TO_RESULTVIEW, "Automatically switch to results perspective when results are created?", DecisionRememberingConfirmDialog.PROPERTY_VALUES, DecisionRememberingConfirmDialog.TRUE));
		//Removed in 6.x
//		ParameterService.registerParameter(new ParameterTypeCategory(MainFrame.PROPERTY_RAPIDMINER_GUI_RESULT_DISPLAY_TYPE, "Determines the result display style.", ResultDisplayTools.TYPE_NAMES, 0));
		ParameterService.registerParameter(new ParameterTypeCategory(MainFrame.PROPERTY_RAPIDMINER_GUI_LOG_LEVEL, "Minimum level of messages that are logged in the GUIs log view.", LogViewer.SELECTABLE_LEVEL_NAMES, LogViewer.DEFAULT_LEVEL_INDEX));
	}

	/**
	 * @param title
	 */
	public AbstractUIState(/*final String initialPerspective,*/ final JFrame frame,
			final Container contentPane) {
		// super(MainFrame.TITLE);
		window = frame;

//		addProcessEditor(actions);
//		addProcessEditor(xmlEditor);
//		//Removed in 6.x
////		addProcessEditor(commentEditor);
//		addProcessEditor(propertyPanel);
//		addProcessEditor(operatorTree);
//		addProcessEditor(operatorDocumentationBrowser);
//		addProcessEditor(processPanel);
//		addProcessEditor(errorTable);
//		addProcessEditor(processContextEditor);
//		addProcessEditor(getStatusBar());
//		addProcessEditor(resultDisplay);
//		addProcessEditor(macroViewer);

		addProcessListeners();
		if (frame != null) {
			frame.setTitle(AbstractUIState.TITLE);
			SwingTools.setFrameIcon(frame);
		}

		// load perspectives now because otherwise the WSDesktop class does not know the nodes and
		// won't restore the user customized perspective
		perspectiveController.loadAll();

		dockingContext.addDesktop(dockingDesktop);
//Removed in 7.0
//		dockingDesktop.registerDockable(welcomeScreen);
		dockingDesktop.registerDockable(repositoryBrowser);
		dockingDesktop.registerDockable(operatorTree);
		dockingDesktop.registerDockable(propertyPanel);
		dockingDesktop.registerDockable(processPanel);
		//Removed in 6.x
//		dockingDesktop.registerDockable(commentEditor);
		dockingDesktop.registerDockable(xmlEditor);
		dockingDesktop.registerDockable(newOperatorEditor);
		dockingDesktop.registerDockable(errorTable);
		dockingDesktop.registerDockable(resultDisplay);
		dockingDesktop.registerDockable(logViewer);
		dockingDesktop.registerDockable(systemMonitor);
		dockingDesktop.registerDockable(operatorDocumentationBrowser);
		dockingDesktop.registerDockable(processContextEditor);
		//Removed in 6.x
//		dockingDesktop.registerDockable(remoteProcessViewer);
		dockingDesktop.registerDockable(processPanel.getProcessRenderer()
				.getOverviewPanel());
		dockingDesktop.registerDockable(macroViewer);
		dockingDesktop.registerDockable(tutorialBrowser);

//Removed in 7.0
//		TemplateView templateView = new TemplateView(this);
//		templateView.getDockKey().setCloseEnabled(false);
//		templateView.getDockKey().setAutoHideEnabled(false);
//		dockingDesktop.registerDockable(templateView);
		// Test
		
		final ToolBarContainer toolBarContainer = ToolBarContainer
				.createDefaultContainer(true, true, true, true);
		toolBarContainer.setBorder(BorderFactory.createEmptyBorder(6, 3, 0, 3));
		toolBarContainer.setOpaque(true);
		toolBarContainer.setBackground(Colors.WINDOW_BACKGROUND);
		//??? getContentPane)()
		contentPane.add(toolBarContainer, BorderLayout.CENTER);
		toolBarContainer.add(dockingDesktop, BorderLayout.CENTER);

		systemMonitor.startMonitorThread();
		resultDisplay.getDockKey().setCloseEnabled(false);
		resultDisplay.getDockKey().setAutoHideEnabled(false);
		resultDisplay.init(this);

		// menu bar
		menuBar = new JMenuBar();
		if (frame != null) {
			frame.setJMenuBar(menuBar);
		}

		fileMenu = new ResourceMenu("file");
		fileMenu.setMargin(menuBarInsets);
		fileMenu.add(NEW_ACTION);
		fileMenu.add(OPEN_ACTION);
		//Removed in 6.x
//		fileMenu.add(OPEN_TEMPLATE_ACTION);
		updateRecentFileList();
		fileMenu.add(recentFilesMenu);
		fileMenu.addSeparator();
		fileMenu.add(SAVE_ACTION);
		fileMenu.add(SAVE_AS_ACTION);
		//Removed in 6.x
//		fileMenu.add(SAVE_AS_TEMPLATE_ACTION);
//		fileMenu.addSeparator();
//		importMenu = new ResourceMenu("file.import");
//		// add export and exit as last file menu actions
//		fileMenu.addSeparator();
//		fileMenu.add(EXPORT_ACTION);
//		fileMenu.addSeparator();
//		fileMenu.add(EXIT_ACTION);

		//Removed in 6.x
//		importMenu.add(IMPORT_CSV_FILE_ACTION);
//		importMenu.add(IMPORT_EXCEL_FILE_ACTION);
//		importMenu.add(IMPORT_XML_FILE_ACTION);
//		importMenu.add(IMPORT_ACCESS_FILE_ACTION);
//		importMenu.add(IMPORT_DATABASE_TABLE_ACTION);
//		importMenu.add(BlobImportWizard.IMPORT_BLOB_ACTION);
//Removed in 7.0
//		fileMenu.add(importMenu);
		fileMenu.add(importDataAction);
		fileMenu.add(IMPORT_PROCESS_ACTION);
		fileMenu.add(EXPORT_PROCESS_ACTION);
		//Removed in 6.x
//		fileMenu.addSeparator();
//		fileMenu.add(PRINT_ACTION);
//		fileMenu.add(PRINT_PREVIEW_ACTION);
//		fileMenu.add(PAGE_SETUP_ACTION);
//		fileMenu.add(EXPORT_ACTION);
//		fileMenu.addSeparator();
//		fileMenu.add(EXIT_ACTION);
		menuBar.add(fileMenu);

//Removed in 7.0
//		importWizardRegistry.register(new DataImportWizardFactory() {
//
//			@Override
//			public DataImportWizard createWizard() throws WizardCreationException {
//				try {
//					return new CSVImportWizard();
//				} catch (OperatorException e) {
//					// should not happen if operator == null
//					throw new WizardCreationException(e);
//				}
//			}
//
//			@Override
//			public Action createAction() {
//				return IMPORT_CSV_FILE_ACTION;
//			}
//		});
//
//		importWizardRegistry.register(new DataImportWizardFactory() {
//
//			@Override
//			public DataImportWizard createWizard() throws WizardCreationException {
//				try {
//					return new ExcelImportWizard();
//				} catch (OperatorException e) {
//					// should not happen if operator == null
//					throw new WizardCreationException(e);
//				}
//			}
//
//			@Override
//			public Action createAction() {
//				return IMPORT_EXCEL_FILE_ACTION;
//			}
//		});

		// edit menu
		((ResourceAction) actions.INFO_OPERATOR_ACTION).addToActionMap(JComponent.WHEN_FOCUSED, true, true, null, getProcessPanel().getProcessRenderer(), getOperatorTree());
		((ResourceAction) actions.TOGGLE_ACTIVATION_ITEM).addToActionMap(JComponent.WHEN_FOCUSED, true, true, null, getProcessPanel().getProcessRenderer(), getOperatorTree());
		((ResourceAction) actions.RENAME_OPERATOR_ACTION).addToActionMap(JComponent.WHEN_FOCUSED, true, true, null, getProcessPanel().getProcessRenderer(), getOperatorTree());
		//Removed in 6.x
//		((ResourceAction) actions.NEW_OPERATOR_ACTION).addToActionMap(JComponent.WHEN_FOCUSED, true, true, null, getProcessPanel().getProcessRenderer(), getOperatorTree());
//		((ResourceAction) actions.NEW_BUILDING_BLOCK_ACTION).addToActionMap(JComponent.WHEN_FOCUSED, true, true, null, getProcessPanel().getProcessRenderer(), getOperatorTree());
//		((ResourceAction) actions.SAVE_BUILDING_BLOCK_ACTION).addToActionMap(JComponent.WHEN_FOCUSED, true, true, null, getProcessPanel().getProcessRenderer(), getOperatorTree());
		// not added for ProcessRenderer because there the DELETE_SELECTED_CONNECTION action is active
		((ResourceAction) actions.DELETE_OPERATOR_ACTION).addToActionMap(JComponent.WHEN_FOCUSED, true, true, null, getOperatorTree());
		// commented out because toggleBreakpoint action is used at various places, especially at operator paramter frame which breaks if action is disabled
//        for (ToggleBreakpointItem item : actions.TOGGLE_BREAKPOINT) {
//        	 ((ResourceAction)item).addToActionMap(JComponent.WHEN_FOCUSED, true, true, null, getProcessPanel().getProcessRenderer(), getOperatorTree());
//        }
		((ResourceAction) actions.TOGGLE_ALL_BREAKPOINTS).addToActionMap(JComponent.WHEN_FOCUSED, true, true, null, getProcessPanel().getProcessRenderer(), getOperatorTree());
		editMenu = new ResourceMenu("edit");
		editMenu.setMargin(menuBarInsets);
		editMenu.add(UNDO_ACTION);
		editMenu.add(REDO_ACTION);
		editMenu.addSeparator();
		editMenu.add(actions.INFO_OPERATOR_ACTION);
		editMenu.add(actions.TOGGLE_ACTIVATION_ITEM.createMenuItem());
		editMenu.add(actions.RENAME_OPERATOR_ACTION);
		//Removed in 6.x
//		editMenu.addSeparator();
//		editMenu.add(actions.NEW_OPERATOR_ACTION);
//		editMenu.add(actions.NEW_BUILDING_BLOCK_ACTION);
//		editMenu.add(actions.SAVE_BUILDING_BLOCK_ACTION);
		editMenu.addSeparator();
		editMenu.add(CutCopyPasteDeleteAction.CUT_ACTION);
		editMenu.add(CutCopyPasteDeleteAction.COPY_ACTION);
		editMenu.add(CutCopyPasteDeleteAction.PASTE_ACTION);
		editMenu.add(CutCopyPasteDeleteAction.DELETE_ACTION);
//        editMenu.add(actions.DELETE_OPERATOR_ACTION);
		editMenu.addSeparator();
		for (final ToggleBreakpointItem item : actions.TOGGLE_BREAKPOINT) {
			editMenu.add(item.createMenuItem());
		}
		editMenu.add(actions.TOGGLE_ALL_BREAKPOINTS.createMenuItem());
		// editMenu.add(actions.MAKE_DIRTY_ACTION);
		menuBar.add(editMenu);

		// process menu
		processMenu = new ResourceMenu("process");
		processMenu.setMargin(menuBarInsets);
		processMenu.add(RUN_ACTION);
		processMenu.add(PAUSE_ACTION);
		processMenu.add(STOP_ACTION);
		processMenu.addSeparator();
		processMenu.add(PROPAGATE_REAL_METADATA_ACTION.createMenuItem());
		processMenu.add(VALIDATE_ACTION);
		processMenu.add(VALIDATE_AUTOMATICALLY_ACTION.createMenuItem());
		// JCheckBoxMenuItem onlyDirtyMenu = new JCheckBoxMenuItem(new
		// ResourceAction(true, "execute_only_dirty") {
		// private static final long serialVersionUID = 2158722678316407076L;
		// @Override
		// public void actionPerformed(ActionEvent e) {
		// if (((JCheckBoxMenuItem)e.getSource()).isSelected()) {
		// getProcess().setExecutionMode(ExecutionMode.ONLY_DIRTY);
		// } else {
		// getProcess().setExecutionMode(ExecutionMode.ALWAYS);
		// }
		// }
		// });
		// expMenu.add(onlyDirtyMenu);

		//Removed in 6.x
//		final JCheckBoxMenuItem debugmodeMenu = new JCheckBoxMenuItem(
//				new ResourceAction(true, "process_debug_mode") {
//
//					private static final long serialVersionUID = 2158722678316407076L;
//
//					@Override
//					public void actionPerformed(final ActionEvent e) {
//						if (((JCheckBoxMenuItem) e.getSource()).isSelected()) {
//							getProcess().setDebugMode(
//									DebugMode.COLLECT_METADATA_AFTER_EXECUTION);
//						} else {
//							getProcess().setDebugMode(DebugMode.DEBUG_OFF);
//						}
//					}
//				});
//		processMenu.add(debugmodeMenu);
		processMenu.addSeparator();

		processMenu.add(AUTO_WIRE);
		processMenu.add(processPanel.getFlowVisualizer().ALTER_EXECUTION_ORDER.createMenuItem());
//Removed in 7.x?
//		final JMenu wiringMenu = new ResourceMenu("wiring");
//Moved to processMenu in 7.0
//Removed in 7.x?
//		wiringMenu.add(AUTO_WIRE);
//		wiringMenu.add(AUTO_WIRE_RECURSIVELY);
//		wiringMenu.add(REWIRE);
//		wiringMenu.add(REWIRE_RECURSIVELY);
//		processMenu.add(wiringMenu);
//Moved to processMenu in 7.0
//		final JMenu orderMenu = new ResourceMenu("execution_order");
//		orderMenu
//				.add(processPanel.getFlowVisualizer().ALTER_EXECUTION_ORDER
//						.createMenuItem());
//Removed in 7.0
//		orderMenu
//				.add(processPanel.getFlowVisualizer().SHOW_EXECUTION_ORDER);
//		processMenu.add(orderMenu);
		final JMenu layoutMenu = new ResourceMenu("process_layout");
		layoutMenu
				.add(processPanel.getProcessRenderer().getArrangeOperatorsAction());
		layoutMenu.add(processPanel.getProcessRenderer().getAutoFitAction());
		//Removed in 6.x
//		layoutMenu
//				.add(processPanel.getProcessRenderer().INCREASE_PROCESS_LAYOUT_WIDTH_ACTION);
//		layoutMenu
//				.add(processPanel.getProcessRenderer().DECREASE_PROCESS_LAYOUT_WIDTH_ACTION);
//		layoutMenu
//				.add(processPanel.getProcessRenderer().INCREASE_PROCESS_LAYOUT_HEIGHT_ACTION);
//		layoutMenu
//				.add(processPanel.getProcessRenderer().DECREASE_PROCESS_LAYOUT_HEIGHT_ACTION);
		processMenu.add(layoutMenu);
		//Removed in 6.x
//		processMenu.addSeparator();
//		processMenu.add(RUN_REMOTE_ACTION);
		menuBar.add(processMenu);

//Removed in 7.0
//		// tools menu
//		toolsMenu = new ResourceMenu("tools");
		//Removed in 6.x
//		toolsMenu.add(MANAGE_BUILDING_BLOCKS_ACTION);
//		toolsMenu.add(MANAGE_TEMPLATES_ACTION);
//		toolsMenu.addSeparator();
//Removed in 7.0
//		toolsMenu.add(ANOVA_CALCULATOR_ACTION);
		//Removed in 6.x
//		toolsMenu.addSeparator();
//		toolsMenu.add(CHECK_FOR_JDBC_DRIVERS_ACTION);
//		toolsMenu.add(MANAGE_DB_CONNECTIONS_ACTION);
//		toolsMenu.add(ManageDatabaseDriversDialog.SHOW_DIALOG_ACTION);
//		toolsMenu.addSeparator();
//		toolsMenu.add(UsageStatsTransmissionDialog.SHOW_STATISTICS_ACTION);
//		toolsMenu.add(SETTINGS_ACTION);
//Removed in 7.0
//		menuBar.add(toolsMenu);

		// view menu
		viewMenu = new ResourceMenu("view");
		viewMenu.setMargin(menuBarInsets);
		viewMenu.add(new PerspectiveMenu(perspectiveController));
		viewMenu.add(NEW_PERSPECTIVE_ACTION);
		viewMenu.add(dockableMenu = new DockableMenu(dockingContext));
		viewMenu.add(perspectiveController.getRestoreDefaultAction());
//Removed in 7.0
//		viewMenu.addSeparator();
//		viewMenu.add(TOGGLE_EXPERT_MODE_ACTION.createMenuItem());
		viewMenu.add(perspectiveController.getRestoreDefaultAction());
		menuBar.add(viewMenu);

		// create settings menu (will be added in finishInitialization())
		settingsMenu = new ResourceMenu("settings");
		settingsMenu.setMargin(menuBarInsets);

		// connections menu
		connectionsMenu = new ResourceMenu("connections");
		connectionsMenu.setMargin(menuBarInsets);
		menuBar.add(connectionsMenu);

		// help menu
		helpMenu = new ResourceMenu("help");
		// extensions menu
		extensionsMenu = new ResourceMenu("extensions");
		extensionsMenu.setMargin(menuBarInsets);
//Removed in 7.0
//		helpMenu.add(COMIC_ACTION);
		//Removed in 6.x
//		helpMenu.add(new BrowseAction("help_support", URI
//				.create("http://rapid-i.com/content/view/60/89/lang,en/")));
//		helpMenu.add(new BrowseAction("help_videotutorials", URI
//				.create("http://rapid-i.com/content/view/189/198/")));
//		helpMenu.add(new BrowseAction("help_forum", URI
//				.create("http://forum.rapid-i.com")));
//		helpMenu.add(new BrowseAction("help_wiki", URI
//				.create("http://wiki.rapid-i.com")));
		//TODO update to Mind Era sites?
//Removed in 7.0
//		helpMenu.add(new BrowseAction("help_documentation", URI
//				.create("http://redirects.rapidminer.com/app/studio/6/documentation")));
//		helpMenu.add(new BrowseAction("help_forum", URI.create("http://forum.rapidminer.com")));

		//Removed in 6.x
//		helpMenu.addSeparator();
//		// helpMenu.add(CHECK_FOR_UPDATES_ACTION);
//		helpMenu.add(UpdateDialog.UPDATE_ACTION);
//		helpMenu.add(ExtensionDialog.MANAGE_EXTENSIONS);
//
//		final List allPlugins = Plugin.getAllPlugins();
//		if (allPlugins.size() > 0) {
//			final JMenu extensionsMenu = new ResourceMenu("about_extensions");
//			final Iterator i = allPlugins.iterator();
//			while (i.hasNext()) {
//				final Plugin plugin = (Plugin) i.next();
//				if (plugin.showAboutBox()) {
//					extensionsMenu.add(new ResourceAction("about_extension",
//							plugin.getName()) {
//
//						private static final long serialVersionUID = 1L;
//
//						@Override
//						public void actionPerformed(final ActionEvent e) {
//							plugin.createAboutBox(
//									AbstractUIState.this.getWindow())
//									.setVisible(true);
//						}
//					});
//				}
//			}
//			helpMenu.add(extensionsMenu);
//		}

		// Tool Bar
		//Changed in 6.x
//		final RapidDockingToolbar fileToolBar = new RapidDockingToolbar("file");
//		fileToolBar.add(makeToolbarButton(NEW_ACTION));
//		fileToolBar.add(makeToolbarButton(OPEN_ACTION));
//		fileToolBar.add(makeToolbarButton(SAVE_ACTION));
//		fileToolBar.add(makeToolbarButton(SAVE_AS_ACTION));
//		fileToolBar.add(makeToolbarButton(PRINT_ACTION));
//
//		final RapidDockingToolbar editToolBar = new RapidDockingToolbar("edit");
//		editToolBar.add(makeToolbarButton(UNDO_ACTION));
//		editToolBar.add(makeToolbarButton(REDO_ACTION));
//
//		final RapidDockingToolbar runToolBar = new RapidDockingToolbar("run");
//		runToolBar.add(makeToolbarButton(RUN_ACTION));
//		runToolBar.add(makeToolbarButton(PAUSE_ACTION));
//		runToolBar.add(makeToolbarButton(STOP_ACTION));
//
//		if ("true".equals(System
//				.getProperty(RapidMiner.PROPERTY_DEVELOPER_MODE))) {
//			runToolBar.addSeparator();
//			runToolBar.add(makeToolbarButton(VALIDATE_ACTION));
//		}
//
//		final RapidDockingToolbar viewToolBar = perspectives
//				.getWorkspaceToolBar();
//		//TODO: enable when tour is useable
////		RapidDockingToolbar achievementToolBar = new RapidDockingToolbar("achievement");
////		DropDownButton achievement = TourChooser.makeAchievmentDropDown();
////		achievement.addToToolBar(achievementToolBar);
//		
//		
//		final ToolBarPanel toolBarPanel = toolBarContainer
//				.getToolBarPanelAt(BorderLayout.NORTH);
//		toolBarPanel.add(fileToolBar, new ToolBarConstraints(0, 0));
//		toolBarPanel.add(editToolBar, new ToolBarConstraints(0, 1));
//		toolBarPanel.add(runToolBar, new ToolBarConstraints(0, 2));
//		toolBarPanel.add(viewToolBar, new ToolBarConstraints(0, 3));

//Removed in 7.0
//		buttonToolbar = new ExtendedJToolBar(true);
//
//		buttonToolbar.add(makeToolbarButton(NEW_ACTION));
//		buttonToolbar.add(makeToolbarButton(OPEN_ACTION));
//		buttonToolbar.add(makeToolbarButton(SAVE_ACTION));
//		buttonToolbar.add(makeToolbarButton(SAVE_AS_ACTION));
//		buttonToolbar.add(makeToolbarButton(EXPORT_ACTION));
//		buttonToolbar.addSeparator();
//
//		buttonToolbar.add(makeToolbarButton(UNDO_ACTION));
//		buttonToolbar.add(makeToolbarButton(REDO_ACTION));
//		buttonToolbar.addSeparator();
//
//		// create run remote button and disable it by default
//		runRemoteToolbarButton = makeToolbarButton(new ResourceActionAdapter("run_remote_now"));
//		runRemoteToolbarButton.setEnabled(false);
//
//		buttonToolbar.add(runRemoteToolbarButton);
//		buttonToolbar.add(makeToolbarButton(RUN_ACTION));
//		buttonToolbar.add(makeToolbarButton(PAUSE_ACTION));
//		buttonToolbar.add(makeToolbarButton(STOP_ACTION));
//		buttonToolbar.addSeparator();

//Removed in 7.0
//		DropDownButton comicDropdown = ComicDialog.makeDropDownButton();
//		comicDropdown.setUsePopupActionOnMainButton();
//		buttonToolbar.add(Box.createHorizontalGlue());
//		comicDropdown.addToToolBar(buttonToolbar);
//
//		JPanel toolbarPanel = new JPanel(new BorderLayout());
//		toolbarPanel.add(buttonToolbar, BorderLayout.WEST);
//		toolbarPanel.add(Box.createHorizontalGlue(), BorderLayout.CENTER);
//		toolbarPanel.add(PerspectivesPanelBar.getPerspecitvesPanelBar(perspectives), BorderLayout.EAST);
//		toolbarPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));
//
//		contentPane.add(toolbarPanel, BorderLayout.NORTH);
		// main tool bar
		toolBar = new MainToolBar(this);
		getStatusBar().setBackground(Colors.WINDOW_BACKGROUND);
		contentPane.add(toolBar, BorderLayout.NORTH);
		contentPane.add(getStatusBar(), BorderLayout.SOUTH);
		getStatusBar().startClockThread();

		// listen for selection changes in the ProcessRendererView and notify all registered process
		// editors
		processPanel.getProcessRenderer().getModel().registerEventListener(new ProcessRendererEventListener() {

			@Override
			public void modelChanged(ProcessRendererModelEvent e) {
				// ignore
			}

			@Override
			public void operatorsChanged(ProcessRendererOperatorEvent e, Collection<Operator> operators) {
				if (e.getEventType() == OperatorEvent.SELECTED_OPERATORS_CHANGED) {
					for (ProcessEditor editor : processEditors.getListeners(ProcessEditor.class)) {
						editor.setSelection(new LinkedList<Operator>(operators));
					}
					for (ExtendedProcessEditor editor : processEditors.getListeners(ExtendedProcessEditor.class)) {
						editor.setSelection(new LinkedList<Operator>(operators));
					}
				}
			}

			@Override
			public void annotationsChanged(ProcessRendererAnnotationEvent e, Collection<WorkflowAnnotation> annotations) {
				// ignore
			}
		});

		setProcess(new Process(), true);
//		selectOperator(process.getRootOperator());
//		addToUndoList();

//		perspectives.showPerspective(initialPerspective);
		perspectiveController.getModel().addPerspectiveChangeListener(perspectiveChangeListener);
		if (getWindow() != null) {
			getWindow().pack();
		}
		metaDataUpdateQueue.start();
	}

	/**
	 * Adds all relevant {@link ProcessEditor ProcessEditors}. Adds the {@link MainProcessListener}
	 * as first.
	 */
	private void addProcessListeners() {
		processModel.addProcessEditor(mainProcessListener);
		processModel.addProcessStorageListener(mainProcessListener);
		processModel.addProcessEditor(actions);
		processModel.addProcessEditor(xmlEditor);
		processModel.addProcessEditor(propertyPanel);
		processModel.addProcessEditor(operatorTree);
		processModel.addProcessEditor(operatorDocumentationBrowser);
		processModel.addProcessEditor(processPanel);
		processModel.addProcessEditor(errorTable);
		processModel.addProcessEditor(processContextEditor);
		processModel.addProcessEditor(getStatusBar());
		processModel.addProcessEditor(resultDisplay);
		processModel.addProcessEditor(macroViewer);
	}

	/**
	 * Finishes the MainFrame initialization. Should be called after all extension have been
	 * initialized.
	 */
	public void finishInitialization() {

		// Configurators (if they exist)
		if (!ConfigurationManager.getInstance().isEmpty()) {
//Removed in 7.0
//			toolsMenu.addSeparator();
//			toolsMenu.add(MANAGE_CONFIGURABLES_ACTION);
			connectionsMenu.addSeparator();
			connectionsMenu.add(MANAGE_CONFIGURABLES_ACTION);
		}

//Removed in 7.0
//		toolsMenu.addSeparator();
//		
		// Password Manager
		settingsMenu.add(PasswordManager.OPEN_WINDOW);
		if (SystemInfoUtilities.getOperatingSystem() != OperatingSystem.OSX || !OSXAdapter.isAdapted()) {
			settingsMenu.add(SETTINGS_ACTION);
		}


		// add settings menu as second last menu or third last if there are entries in the help menu
		menuBar.add(settingsMenu);

		// add extensions menu as last menu or second last if there are entries in the help
		// menu
		menuBar.add(extensionsMenu);

		// Add Help menu as last entry if it is not empty
		if (helpMenu.getItemCount() > 0) {
			helpMenu.setMargin(menuBarInsets);
			menuBar.add(helpMenu);
		}
	}

	protected JButton makeToolbarButton(final Action action) {
		final JButton button = new JButton(action);
		if (button.getIcon() != null) {
			button.setText(null);
		}
		return button;
	}

	//Removed in 6.x
//	@Override
//	public boolean isTutorialMode() {
//		return this.tutorialMode;
//	}
//
//	/*
//	 * (non-Javadoc)
//	 * 
//	 * @see com.rapidminer.gui.MainUIState#startTutorial()
//	 */
//	@Override
//	public void startTutorial() {
//		if (close()) {
//			new Tutorial(this).setVisible(true);
//		}
//	}
//
//	/*
//	 * (non-Javadoc)
//	 * 
//	 * @see com.rapidminer.gui.MainUIState#setTutorialMode(boolean)
//	 */
//	@Override
//	public void setTutorialMode(final boolean mode) {
//		this.tutorialMode = mode;
//		if (tutorialMode) {
//			SAVE_ACTION.setEnabled(false);
//			SAVE_AS_ACTION.setEnabled(false);
//		} else {
//			SAVE_ACTION.setEnabled(false);
//			SAVE_AS_ACTION.setEnabled(true);
//		}
//	}

//Removed in 7.0
//	@Override
//	public void setExpertMode(final boolean expert) {
//		TOGGLE_EXPERT_MODE_ACTION.setSelected(expert);
//		TOGGLE_EXPERT_MODE_ACTION.actionToggled(null);
//	}

	@Override
	public OperatorPropertyPanel getPropertyPanel() {
		return propertyPanel;
	}

	/**
	 * Returns a registry for {@link DataImportWizardFactory} instances. The factories are used to
	 * populate menus such as the main import menu.
	 *
	 * @return the registry
	 * @deprecated Use {@link DataSourceFactoryRegistry} instead. Registering a
	 *             {@link DataImportWizardRegistry} will not have an effect anymore.
 	 */
	@Deprecated
	public DataImportWizardRegistry getDataImportWizardRegistry() {
		return importWizardRegistry;
	}

	@Override
	public LogViewer getLogViewer() {
		return logViewer;
	}

	@Override
	public NewOperatorEditor getNewOperatorEditor() {
		return newOperatorEditor;
	}

	@Override
	public OperatorTree getOperatorTree() {
		return operatorTree.getOperatorTree();
	}

	@Override
	public Actions getActions() {
		return actions;
	}

	@Override
	public ResultDisplay getResultDisplay() {
		return resultDisplay;
	}
	
//Removed in 7.0
//	@Override
//	public WelcomeScreen getWelcomeScreen() {
//		return welcomeScreen;
//	}

	@Override
	public JButton getRunRemoteToolbarButton() {
		return runRemoteToolbarButton;
	}
	
	@Override
	public int getProcessState() {
		Process process = getProcess();
		if (process == null) {
			return Process.PROCESS_STATE_UNKNOWN;
		} else {
			return process.getProcessState();
		}
	}

	@Override
	public final Process getProcess() {
		return processModel.getProcess();
	}

	@Override
	public void newProcess() {
		newProcess(true);
	}
	public void newProcess(final boolean checkforUnsavedWork) {
		// ask for confirmation before stopping the currently running process and opening a new one!
		if (getProcessState() == Process.PROCESS_STATE_RUNNING || getProcessState() == Process.PROCESS_STATE_PAUSED) {
			if (SwingTools.showConfirmDialog("close_running_process",
					ConfirmDialog.YES_NO_OPTION) != ConfirmDialog.YES_OPTION) {
				return;
			}
		}

		ProgressThread newProgressThread = new ProgressThread("new_process") {

			@Override
			public void run() {
				// Invoking close() will ask the user to save their work if there are unsaved
				// changes. This method can be skipped if it is already clear that changes should be
				// discarded.
				boolean resetProcess = checkforUnsavedWork ? close(false) : true;
				if (resetProcess) {
					// process changed -> clear undo history
					resetUndo();

					stopProcess();
					changed = false;
					setProcess(new Process(), true);
					addToUndoList();
					if (!"false"
							.equals(ParameterService.getParameterValue(PROPERTY_RAPIDMINER_GUI_SAVE_ON_PROCESS_CREATION))) {
						SaveAction.saveAsync(getProcess());
					}
					// always have save action enabled. If process is not yet associated with
					// location SaveAs will be used
					SAVE_ACTION.setEnabled(true);
				}
			}
		};
		newProgressThread.setIndeterminate(true);
		newProgressThread.setCancelable(false);
		newProgressThread.start();
	}


	/**
	 * Runs or resumes the current process. If the process is started, checks for potential errors
	 * first and prevents execution unless the user has disabled the pre-run check.
	 */
	public void runProcess() {
		runProcess(true);
	}

	/**
	 * Runs or resumes the current process.
	 *
	 * @param precheckBeforeExecution
	 *            if {@code true} and the process is started, checks for potential errors first and
	 *            prevents execution unless the user has disabled the pre-run check
	 */
	public void runProcess(boolean precheckBeforeExecution) {
		if (getProcessState() == Process.PROCESS_STATE_STOPPED) {
			// Run
			if (isChanged() || getProcess().getProcessLocation() == null) {
				if (DecisionRememberingConfirmDialog.confirmAction("save_before_run",
						PROPERTY_RAPIDMINER_GUI_SAVE_BEFORE_RUN)) {
					SaveAction.saveAsync(getProcess());
				}
			}

			// don't run process if showstoppers are present
			// this only returns true if the user did not disable the strict process check in the
			// preferences
			if (precheckBeforeExecution && doesProcessContainShowstoppers()) {
				return;
			}

			processThread = new ProcessThread(getProcess());
			try {
				processThread.start();
			} catch (Exception t) {
				SwingTools.showSimpleErrorMessage("cannot_start_process", t);
			}
		} else {
			getProcess().resume();
		}
	}

	/**
	 * Can be used to stop the currently running process. Please note that
	 * the ProcessThread will still be running in the background until the current
	 * operator is finished.
	 */
	@Override
	public void stopProcess() {
		if (getProcessState() != Process.PROCESS_STATE_STOPPED) {
			getProcess().getLogger().info(
					"Process stopped. Completing current operator.");
			getStatusBar().setSpecialText(
					"Process stopped. Completing current operator.");
			if (processThread != null) {
				if (processThread.isAlive()) {
					processThread.setPriority(Thread.MIN_PRIORITY);
					processThread.stopProcess();
				}
			}
		}
	}

	@Override
	public void pauseProcess() {
		if (getProcessState() == Process.PROCESS_STATE_RUNNING) {
			getProcess().getLogger().info(
					"Process paused. Completing current operator.");
			getStatusBar().setSpecialText(
					"Process paused. Completing current operator.");
			if (processThread != null) {
				processThread.pauseProcess();
			}
		}
	}

	/**
	 * Will be invoked from the process thread after the process was
	 * successfully ended.
	 */
	@Override
	public void processEnded(final Process process, final IOContainer results) {
		if (process.equals(getProcess())) {
			if (results != null) {
				SwingUtilities.invokeLater(new Runnable() {

					@Override
					public void run() {
						final JFrame window = AbstractUIState.this.getWindow();
						if (window != null) {
							window.toFront();
						}
					}
				});
			}
		}
		if (process.equals(getProcess())) {
			if (results != null) {
				resultDisplay.showData(results, "Process results");
			}
		}
	}

	/**
	 * Sets a (new) process.
	 *
	 * @param process
	 *            the process to be set
	 * @param newProcess
	 *            whether the process should be treated as new
	 */
	public void setProcess(Process process, boolean newProcess) {
		setOrOpenProcess(process, newProcess, false);
	}

	/**
	 * Sets or loads a (new) process. Resets the undo stack for the model if necessary. <br/>
	 * Note: It should hold: open => newProcess
	 */
	private void setOrOpenProcess(final Process process, final boolean newProcess, final boolean open) {
		boolean firstProcess = getProcess() == null;
		processModel.setProcess(process, newProcess, open);
		if (newProcess) {
			enableUndoAction();
			if (!firstProcess) {
				// VLDocking appears to get nervous when applying two perspectives while the
				// window is not yet visible. So to avoid that we set design and then welcome
				// during startup, avoid applying design if this is the first process we create.
				perspectiveController.showPerspective(PerspectiveModel.DESIGN);
			}
		}
		updateProcessNow();
	}

//	/**
//	 * Sets a new process and registers the MainFrame's listeners.
//	 */
//	public void setProcess(Process process, boolean newProcess, boolean addToUndoList) {
//		final boolean firstProcess = this.process == null;
//		if (this.process != null) {
//			// this.process.getRootOperator().removeObserver(processObserver);
//			this.process.removeObserver(processObserver);
//		}
//
//		if (getProcessState() != Process.PROCESS_STATE_STOPPED) {
//			if (processThread != null) {
//				processThread.stopProcess();
//			}
//		}
//
//		if (process != null) {
//			// process.getRootOperator().addObserver(processObserver, true);
//			process.addObserver(processObserver, true);
//
//			synchronized (process) {
//				this.process = process;
//				this.processThread = new ProcessThread(this.process);
//				this.process.addBreakpointListener(breakpointListener);
//				if (addToUndoList) {
//					addToUndoList(process.getRootOperator().getXML(true, false), false);
//				}
//				fireProcessChanged();
//				processPanel.getProcessRenderer().getModel().setDisplayedChain(this.getProcess().getRootOperator());
//				processPanel.getProcessRenderer().getModel().fireDisplayedChainChanged();
//				selectOperator(this.process.getRootOperator());
//				if (VALIDATE_AUTOMATICALLY_ACTION.isSelected()) {
//					validateProcess(false);
//				}
//			}
//		}
//		if (newProcess && !firstProcess) {
//			// VLDocking appears to get nervous when applying two perspectives
//			// while the
//			// window is not yet visible. So to avoid that we set design and
//			// then welcome
//			// during startup, avoid applying design if this is the first
//			// process we create.
//			perspectiveController.showPerspective(PerspectiveModel.DESIGN);
//		}
//		setTitle();
////Removed in 7.0
////		getStatusBar().setTrafficLight(StatusBar.TRAFFIC_LIGHT_INACTIVE);
//		getStatusBar().clearSpecialText();
//	}

	/**
	 * Sets a new process and registers the MainFrame's listeners.
	 *
	 * @deprecated Since 7.5. Use {@link #setProcess(Process, boolean)} instead, since undo steps
	 *             are automatically added when necessary.
	 */
	@Deprecated
	public void setProcess(final Process process, final boolean newProcess, final boolean addToUndoList) {
		setProcess(process, newProcess);
	}
	
	/** Returns true if the process has changed since the last save. */
	@Override
	public boolean isChanged() {
		return processModel.hasChanged();
	}

	private boolean addToUndoList() {
		return addToUndoList(null, false);
	}

	/**
	 * Adds the current state of the process to the undo list.
	 * 
	 * Note: This method must not be exposed by making it public. It may confuse
	 * the MainFrame such that it can no longer determine correctly whether
	 * validation is possible.
	 * 
	 * @return true if process really differs.
	 */
	private boolean addToUndoList(String currentStateXML, boolean viewSwitch) {
		String lastStateXML = null;
		if (undoManager.getNumberOfUndos() != 0) {
			lastStateXML = undoManager.getXml(undoIndex);
		}

		if (currentStateXML == null) {
			currentStateXML = this.process.getRootOperator().getXML(true);
		}
		if (currentStateXML != null) {
			// mark as changed only if the XML has changed
			if (lastStateXML == null || !lastStateXML.equals(currentStateXML) || viewSwitch) {
				if (undoIndex < undoManager.getNumberOfUndos() - 1) {
					while (undoManager.getNumberOfUndos() > undoIndex + 1) {
						undoManager.removeLast();
					}
				}
				undoManager.add(currentStateXML, getProcessPanel().getProcessRenderer().getModel().getDisplayedChain(),
						getFirstSelectedOperator());
				String maxSizeProperty = ParameterService.getParameterValue(PROPERTY_RAPIDMINER_GUI_UNDOLIST_SIZE);
				int maxSize = 20;
				try {
					if (maxSizeProperty != null) {
						maxSize = Integer.parseInt(maxSizeProperty);
					}
				} catch (NumberFormatException e) {
					LogService.getRoot().warning("com.rapidminer.gui.main_frame_warning");
				}
				while (undoManager.getNumberOfUndos() > maxSize) {
					undoManager.removeFirst();
				}
				undoIndex = undoManager.getNumberOfUndos() - 1;
				enableUndoAction();

				boolean oldChangedValue = AbstractUIState.this.changed;
				// mark as changed only if the XML has changed
				if (currentStateXML.equals(lastStateXML)) {
					return false;
				}

				AbstractUIState.this.changed = lastStateXML != null;

				if (!oldChangedValue) {
					setTitle();
				}
				if (AbstractUIState.this.process.getProcessLocation() != null) {
					AbstractUIState.this.getSaveAction().setEnabled(true);
				}
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}

	@Override
	public void undo() {
//		if (undoIndex > 0) {
//			undoIndex--;
//			setProcessIntoStateAt(undoIndex, true);
//		}
//		enableUndoAction();
		Exception e = processModel.undo();
		if (e != null) {
			SwingTools.showSimpleErrorMessage("while_changing_process", e);
		}
	}

	@Override
	public void redo() {
//		if (undoIndex < undoManager.getNumberOfUndos()) {
//			undoIndex++;
//			setProcessIntoStateAt(undoIndex, false);
//		}
//		enableUndoAction();
		Exception e = processModel.redo();
		if (e != null) {
			SwingTools.showSimpleErrorMessage("while_changing_process", e);
		}
	}

//	private void enableUndoAction() {
//		if (undoIndex > 0) {
//			UNDO_ACTION.setEnabled(true);
//		} else {
//			UNDO_ACTION.setEnabled(false);
//		}
//		if (undoIndex < undoManager.getNumberOfUndos() - 1) {
//			REDO_ACTION.setEnabled(true);
//		} else {
//			REDO_ACTION.setEnabled(false);
//		}
//	}

	public void enableUndoAction() {
		UNDO_ACTION.setEnabled(processModel.hasUndoSteps());
		REDO_ACTION.setEnabled(processModel.hasRedoSteps());
	}
	/**
	 * {@inheritDoc}
	 */
	public boolean hasUndoSteps() {
		//return undoIndex > 0;
		return processModel.hasUndoSteps();
	}
	
	/**
	 * {@inheritDoc}
	 */
	public boolean hasRedoSteps() {
		//return undoIndex < undoManager.getNumberOfUndos() - 1;
		return processModel.hasRedoSteps();
	}

	private void setProcessIntoStateAt(int undoIndex, boolean undo) {
		String stateXML = undoManager.getXml(undoIndex);
		OperatorChain shownOperatorChain = null;
		if (undo) {
			shownOperatorChain = undoManager.getOperatorChain(undoIndex);
		} else {
			shownOperatorChain = undoManager.getOperatorChain(undoIndex);
		}
		Operator selectedOperator = undoManager.getSelectedOperator(undoIndex);
		try {
			synchronized (process) {
				String oldXml = process.getRootOperator().getXML(true);
				Process process = new Process(stateXML, this.process);
				// this.process.setupFromXML(stateXML);
				setProcess(process, false);
				// cannot use method processChanged() because this would add the
				// old state to the undo stack!
				if (!stateXML.equals(oldXml)) {
					this.changed = true;
					setTitle();
					if (this.process.getProcessLocation() != null) {
						this.SAVE_ACTION.setEnabled(true);
					}
				}

				// restore selected operator
				if (selectedOperator != null) {
					Operator restoredOperator = getProcess().getOperator(selectedOperator.getName());
					if (restoredOperator != null) {
						selectOperator(restoredOperator);
					}
				}

				// restore process panel view on correct subprocess on undo
				if (shownOperatorChain != null) {
					OperatorChain restoredOperatorChain = (OperatorChain) getProcess()
							.getOperator(shownOperatorChain.getName());
					processPanel.getProcessRenderer().getModel().setDisplayedChain(restoredOperatorChain);
					processPanel.getProcessRenderer().getModel().fireDisplayedChainChanged();
				}
			}
		} catch (Exception e) {
			SwingTools.showSimpleErrorMessage("while_changing_process", e);
		}

		lastProcessDisplayedOperatorChain = getProcessPanel().getProcessRenderer().getModel().getDisplayedChain();
		lastProcessXML = process.getRootOperator().getXML(true, false);	}

	/**
	 * Closes the current process
	 * @param askForConfirmation if <code>true</code>, will prompt the user if he really wants to close the current process
	 * @return Saved?
	 */
	public boolean close(boolean askForConfirmation) {
//		if (changed) {
//			final ProcessLocation loc = process.getProcessLocation();
//			String locName;
//			if (loc != null) {
//				locName = loc.getShortName();
//			} else {
//				locName = "unnamed";
//			}
//			switch (SwingTools.showConfirmDialog("save", ConfirmDialog.YES_NO_CANCEL_OPTION, locName)) {
//				case ConfirmDialog.YES_OPTION:
//					SaveAction.save(getProcess());
//
//					// it may happen that save() does not actually save the process, because the user hits cancel in the
//					// saveAs dialog or an error occurs. In this case the process won't be marked as unchanged. Thus,
//					// we return the process changed status.
//					return !isChanged();
//				case ConfirmDialog.NO_OPTION:
//					// ask for confirmation before stopping the currently running process (if askForConfirmation=true)
//					if (askForConfirmation) {
//						if (RapidMinerGUI.getMainFrame().getProcessState() == Process.PROCESS_STATE_RUNNING ||
//								RapidMinerGUI.getMainFrame().getProcessState() == Process.PROCESS_STATE_PAUSED) {
//							if (SwingTools.showConfirmDialog("close_running_process",
//									ConfirmDialog.YES_NO_OPTION) != ConfirmDialog.YES_OPTION) {
//								return false;
//							}
//						}
//					}
//					if (getProcessState() != Process.PROCESS_STATE_STOPPED) {
//						synchronized (processThread) {
//							processThread.stopProcess();
//						}
//					}
//					return true;
//				default: // cancel
//					return false;
//			}
//		} else {
//			return true;
//		}
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
				if (askForConfirmation) {
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
	}

	public boolean close() {
		return close(true);
	}

	/**
	 * @deprecated Since 7.5. Use {@link #setOpenedProcess(Process)}, the other parameters are
	 *             irrelevant.
	 */
	@Deprecated
	@Override
	public void setOpenedProcess(final Process process, final boolean showInfo,
									final String sourceName) {
//		// process changed -> clear undo history
//		resetUndo();
//		setProcess(process, true);
//
//		if (process.getImportMessage() != null && process.getImportMessage().contains("error")) {
//			SwingTools.showLongMessage("import_message", process.getImportMessage());
//		}
//
//		SwingUtilities.invokeLater(new Runnable() {
//
//			@Override
//			public void run() {
//				SAVE_ACTION.setEnabled(false);
//			}
//		});
//
//		List<UnknownParameterInformation> unknownParameters = null;
//		synchronized (process) {
//			RapidMinerGUI.useProcessFile(AbstractUIState.this.process);
//			unknownParameters = process.getUnknownParameters();
//		}
//
//		addToUndoList();
//		updateRecentFileList();
//		changed = false;
//
//		SwingUtilities.invokeLater(new Runnable() {
//
//			@Override
//			public void run() {
//				SAVE_ACTION.setEnabled(false);
//				setTitle();
//			}
//		});
//
//		// show unsupported parameters info?
//		if (unknownParameters != null && unknownParameters.size() > 0) {
//			final UnknownParametersInfoDialog unknownParametersInfoDialog = new UnknownParametersInfoDialog(AbstractUIState.this.getWindow(),
//					unknownParameters);
//			if (SwingUtilities.isEventDispatchThread()) {
//				unknownParametersInfoDialog.setVisible(true);
//			} else {
//				try {
//					SwingUtilities.invokeAndWait(new Runnable() {
//
//						@Override
//						public void run() {
//							unknownParametersInfoDialog.setVisible(true);
//						}
//					});
//				} catch (Exception e) {
//					LogService.getRoot().log(Level.WARNING, "Error opening the unknown parameter dialog: " + e, e);
//				}
//			}
//		}
//		fireProcessLoaded();
		setOpenedProcess(process);
	}

	/** Opens the specified process. */
	public void setOpenedProcess(final Process process) {
		setOrOpenProcess(process, true, true);
	}

	private void resetUndo() {
		undoIndex = 0;
		undoManager.reset();
		enableUndoAction();
	}

	//Removed in 6.x
//	@Override
//	public void saveAsTemplate() {
//		synchronized (process) {
//			final SaveAsTemplateDialog dialog = new SaveAsTemplateDialog(
//					AbstractUIState.this.process);
//			dialog.setVisible(true);
//			if (dialog.isOk()) {
//				try {
//					dialog.getTemplate().saveAsUserTemplate(
//							AbstractUIState.this.process);
//				} catch (final Exception ioe) {
//					SwingTools.showSimpleErrorMessage(
//							"cannot_write_template_file", ioe);
//				}
//			}
//		}
//	}

//NEW
	public void exit(final boolean relaunch) {
		if (isChanged()) {
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
				// ask for special confirmation before exiting RapidMiner while a process is
				// running!
				if (getProcessState() == Process.PROCESS_STATE_RUNNING
						|| getProcessState() == Process.PROCESS_STATE_PAUSED) {
					if (SwingTools.showConfirmDialog("exit_despite_running_process",
							ConfirmDialog.YES_NO_OPTION) == ConfirmDialog.NO_OPTION) {
						return;
					}
				} else {
					int answer = ConfirmDialog.showConfirmDialog(ApplicationFrame.getApplicationFrame(), "exit",
							ConfirmDialog.YES_NO_OPTION, RapidMinerGUI.PROPERTY_CONFIRM_EXIT, ConfirmDialog.YES_OPTION);
					if (answer != ConfirmDialog.YES_OPTION) {
						return;
					}
				}
			}
		}
		stopProcess();
		final JFrame mainWindow = getWindow();
		if (mainWindow != null) {
			mainWindow.dispose();
		}
		RapidMiner.quit(relaunch ? RapidMiner.ExitMode.RELAUNCH : RapidMiner.ExitMode.NORMAL);
	}

	/**
	 * This methods provide plugins the possibility to modify the menus
	 */
	@Override
	public void removeMenu(final int index) {
		menuBar.remove(menuBar.getMenu(index));
	}

	@Override
	public void removeMenuItem(final int menuIndex, final int itemIndex) {
		menuBar.getMenu(menuIndex).remove(itemIndex);
	}

	@Override
	public void addMenuItem(final int menuIndex, final int itemIndex,
							final JMenuItem item) {
		menuBar.getMenu(menuIndex).add(item, itemIndex);
	}

	@Override
	public void addMenu(int menuIndex, final JMenu menu) {
		menu.setMargin(menuBarInsets);
		if (menuIndex < -1 || menuIndex >= menuBar.getComponentCount()) {
			menuIndex = -1;
		}
		menuBar.add(menu, menuIndex);
	}

	@Override
	public void addMenuSeparator(final int menuIndex) {
		menuBar.getMenu(menuIndex).addSeparator();
	}

	// / LISTENERS

	public List<Operator> getSelectedOperators() {
		//return processPanel.getProcessRenderer().getModel().getSelectedOperators();
		return processModel.getSelectedOperators();
	}

	public Operator getFirstSelectedOperator() {
//		return processPanel.getProcessRenderer().getModel().getSelectedOperators().isEmpty() ? null
//				: processPanel.getProcessRenderer().getModel().getSelectedOperators().get(0);
		List<Operator> selectedOperators = processModel.getSelectedOperators();
		return selectedOperators.isEmpty() ? null : selectedOperators.get(0);
	}

	public void addProcessEditor(final ProcessEditor p) {
		//processEditors.add(ProcessEditor.class, p);
		processModel.addProcessEditor(p);
	}

	@Override
	public void addExtendedProcessEditor(final ExtendedProcessEditor p) {
		//processEditors.add(ExtendedProcessEditor.class, p);
		processModel.addProcessEditor(p);
	}

	/**
	 * @deprecated use {@link #removeExtendedProcessEditor(ExtendedProcessEditor)} instead.
	 */
	@Deprecated
	public void removeProcessEditor(final ProcessEditor p) {
//		processEditors.remove(ProcessEditor.class, p);
		processModel.removeProcessEditor(p);
	}

	/**
	 * Removes the given {@link ExtendedProcessEditor} listener.
	 *
	 * @param p
	 */
	@Override
	public void removeExtendedProcessEditor(final ExtendedProcessEditor p) {
//		processEditors.remove(ExtendedProcessEditor.class, p);
		processModel.removeProcessEditor(p);
	}

	@Override
	public void addProcessStorageListener(ProcessStorageListener listener) {
//		storageListeners.add(listener);
		processModel.addProcessStorageListener(listener);
	}

	@Override
	public void removeProcessStorageListener(ProcessStorageListener listener) {
//		storageListeners.remove(listener);
		processModel.removeProcessStorageListener(listener);
	}

	@Override
	public void selectAndShowOperator(Operator currentlySelected, boolean showParent) {
		if (currentlySelected == null) {
			currentlySelected = getProcess().getRootOperator();
		}
		OperatorChain parent = currentlySelected.getParent();
		// if this is not a chain, it can not be displayed as such!
		showParent |= !(currentlySelected instanceof OperatorChain);
		// root chain has no parent
		showParent &= parent != null;
		OperatorChain dispChain = showParent ? parent : (OperatorChain) currentlySelected;
		processModel.setDisplayedChainAndFire(dispChain);
		selectOperators(Collections.singletonList(currentlySelected));
	}

	@Override
	public void selectOperator(Operator currentlySelected) {
		if (currentlySelected == null) {
			currentlySelected = getProcess().getRootOperator();
		}
		selectOperators(Collections.singletonList(currentlySelected));
	}

	@Override
	public void selectOperators(List<Operator> currentlySelected) {
		if (currentlySelected == null) {
			currentlySelected = Collections.<Operator> singletonList(getProcess().getRootOperator());
		}
		for (Operator op : currentlySelected) {
			Process selectedProcess = op.getProcess();
			if (selectedProcess == null || selectedProcess != getProcess()) {
				SwingTools.showVerySimpleErrorMessage("op_deleted", op.getName());
				return;
			}
		}

//		ProcessRendererModel model = processPanel.getProcessRenderer().getModel();
//		model.clearOperatorSelection();
//		model.addOperatorsToSelection(currentlySelected);
//		model.fireOperatorSelectionChanged(currentlySelected);
		processModel.clearOperatorSelection();
		processModel.addOperatorsToSelection(currentlySelected);
		processModel.fireOperatorSelectionChanged(currentlySelected);
	}

	//Removed in 6.x
//	/**
//	 * Notifies the main editor of the change of the currently selected
//	 * operator.
//	 */
//	private void fireSelectedOperatorChanged(
//												final List<Operator> currentlySelected) {
//		for (final ProcessEditor editor : processEditors
//				.getListeners(ProcessEditor.class)) {
//			editor.setSelection(currentlySelected);
//		}
//	}

	@Override
	public void fireProcessUpdated() {
//		for (final ProcessEditor editor : processEditors
//				.getListeners(ProcessEditor.class)) {
//			editor.processUpdated(process);
//		}
//		for (ExtendedProcessEditor editor : processEditors.getListeners(ExtendedProcessEditor.class)) {
//			editor.processUpdated(process);
//		}
		processModel.fireProcessUpdated();
	}


	/**
	 * Fire this when the process view has changed, e.g. when the user enters/leaves a subprocess in
	 * the process design panel.
	 */
	protected void fireProcessViewChanged() {
		for (ExtendedProcessEditor editor : processEditors.getListeners(ExtendedProcessEditor.class)) {
			editor.processViewChanged(process);
		}
	}

	private void fireProcessChanged() {
		for (final ProcessEditor editor : processEditors
				.getListeners(ProcessEditor.class)) {
			editor.processChanged(process);
		}
		for (ExtendedProcessEditor editor : processEditors.getListeners(ExtendedProcessEditor.class)) {
			editor.processChanged(process);
		}
	}

	private void fireProcessLoaded() {
		LinkedList<ProcessStorageListener> list = new LinkedList<ProcessStorageListener>(storageListeners);
		for (ProcessStorageListener l : list) {
			l.opened(process);
		}
	}

	private void fireProcessStored() {
		LinkedList<ProcessStorageListener> list = new LinkedList<>(storageListeners);
		for (ProcessStorageListener l : list) {
			l.stored(process);
		}
	}
	
	@Override
	public DockingDesktop getDockingDesktop() {
		return dockingDesktop;
	}

	/**
	 * @deprecated use {@link #getPerspectiveController()} instead
	 */
	@Override
	public Perspectives getPerspectives() {
		return perspectives;
	}

	public PerspectiveController getPerspectiveController() {
		return perspectiveController;
	}

	@Override
	public void handleBrokenProxessXML(final ProcessLocation location,
										final String xml, final Exception e) {
		SwingTools.showSimpleErrorMessage("while_loading", e, location.toString(), e.getMessage());
		Process process = new Process();
		process.setProcessLocation(location);
		setProcess(process, true);
		perspectiveController.showPerspective(PerspectiveModel.DESIGN);
		xmlEditor.setText(xml);
	}

	@Override
	public OperatorDocumentationBrowser getOperatorDocViewer() {
		return operatorDocumentationBrowser;
	}

	@Override
	public ProcessPanel getProcessPanel() {
		return processPanel;
	}

//Removed in 7.0
//	public ComicRenderer getComicRenderer() {
//		return comicRenderer;
//	}

	@Override
	public void registerDockable(final Dockable dockable) {
		dockingDesktop.registerDockable(dockable);
	}

	@Override
	public void processHasBeenSaved() {
//		SAVE_ACTION.setEnabled(false);
//		changed = false;
//		setTitle();
//		updateRecentFileList();
//		fireProcessStored();
//
//		//Removed in 6.x
////		// update RUN_REMOTE_NOW action enabled state
////		try {
////			if (process.getRepositoryLocation() != null && process.getRepositoryLocation().getRepository() instanceof RemoteRepository) {
////				RUN_REMOTE_NOW_ACTION.setEnabled(true);
////			} else {
////				RUN_REMOTE_NOW_ACTION.setEnabled(false);
////			}
////		} catch (RepositoryException e) {
////			RUN_REMOTE_NOW_ACTION.setEnabled(false);
////		}
		processModel.processHasBeenSaved();
	}

	@Override
	public ProcessContextProcessEditor getProcessContextEditor() {
		return processContextEditor;
	}

	@Override
	public RepositoryBrowser getRepositoryBrowser() {
		return repositoryBrowser;
	}

	@Override
	public Component getXMLEditor() {
		return xmlEditor;
	}

	/**
	 * This returns the file menu to change menu entries
	 */
	@Override
	public JMenu getFileMenu() {
		return fileMenu;
	}

	public JMenu getConnectionsMenu() {
		return connectionsMenu;
	}
	
	/**
	 * This returns the settings menu to change menu entries.
	 *
	 * @deprecated the tools menu was split into multiple menus. Use {@link #getConnectionsMenu()}
	 *             or {@link #getSettingsMenu()} instead
	 */
	@Override
	@Deprecated
	public JMenu getToolsMenu() {
		return settingsMenu;
	}

	/**
	 * This returns the settings menu to change menu entries
	 *
	 * @return the settings menu
	 */
	@Override
	public JMenu getSettingsMenu() {
		return settingsMenu;
 	}

//Removed in 7.0
//	/**
//	 * This returns the complete menu bar to insert additional menus
//	 */
//	@Override
//	public JMenuBar getMainMenuBar() {
//		return menuBar;
//	}

	/**
	 * This returns the edit menu to change menu entries
	 */
	@Override
	public JMenu getEditMenu() {
		return editMenu;
	}

	/**
	 * This returns the process menu to change menu entries
	 */
	@Override
	public JMenu getProcessMenu() {
		return processMenu;
	}

	/**
	 * This returns the help menu to change menu entries
	 */
	@Override
	public JMenu getHelpMenu() {
		return helpMenu;
	}

	/**
	 * This returns the extensions menu to change menu entries
	 *
	 * @since 7.0.0
	 */
	public JMenu getExtensionsMenu() {
		return extensionsMenu;
	}

	/*
	 * (non-Javadoc)
	 * @see com.rapidminer.gui.MainUIState#getDockableMenu()
	 */
	@Override
	public DockableMenu getDockableMenu() {
		return dockableMenu;
		
	}

	/**
	 *
	 * @return the toolbar containing e.g. process run buttons
	 */
	public JToolBar getButtonToolbar() {
		return buttonToolbar;
	}
	

	/**
	 * The {@link TutorialSelector} holds the selected {@link Tutorial}.
	 *
	 * @return the registered tutorial selector
	 * @since 7.0.0
	 */
	public TutorialSelector getTutorialSelector() {
		return tutorialSelector;
	}

	/**
	 * Checks the current process for potential problems. If a problem is deemed big enough (e.g. an
	 * operator that requires input but is not connected), returns {@code true}. If no showstoppers
	 * are found, returns {@code false}. This method also alerts the user about the problems so
	 * after it returns, nothing else needs to be done.
	 *
	 * @return {@code true} if the process contains a problem which should prevent process
	 *         execution; {@code false} otherwise
	 */
	private boolean doesProcessContainShowstoppers() {
		// prevent two bubbles on top of each other
		getProcessPanel().getOperatorWarningHandler().killWarningBubble();

        // if any operator has a mandatory parameter with no value and no default value. As it
		// cannot predict execution behavior (e.g. Branch operators), this may turn up problems
		// which would not occur during process execution
		Process process = getProcess();
		Pair<Operator, ParameterType> missingParamPair = ProcessTools.getOperatorWithoutMandatoryParameter(process);
		if (missingParamPair != null) {
			// if there is already one of these, kill
			if (missingParameterBubble != null) {
				missingParameterBubble.killBubble(true);
			}

			missingParameterBubble = ProcessGUITools.displayPrecheckMissingMandatoryParameterWarning(
					missingParamPair.getFirst(), missingParamPair.getSecond());
			return true;
		}

		// if any port needs data but is not connected. As it cannot predict execution behavior
		// (e.g. Branch operators), this may turn up problems which would not occur during
		// process execution
		Port missingInputPort = ProcessTools.getPortWithoutMandatoryConnection(process);
		if (missingInputPort != null) {
			// if there is already one of these, kill
			if (missingInputBubble != null) {
				missingInputBubble.killBubble(true);
			}

			missingInputBubble = ProcessGUITools.displayPrecheckInputPortDisconnectedWarning(missingInputPort);
			return true;
		}

		// if there is already one of these, kill
		if (noResultConnectionBubble != null) {
			noResultConnectionBubble.killBubble(true);
		}

		// if the process has no connected result ports and the last executed
		// process root child operator does not prevent a warning bubble we need
		// to notify the user that no output port is connected
		boolean isWarnOnNoResultProcess = Boolean
				.parseBoolean(ParameterService.getParameterValue(RapidMinerGUI.PROPERTY_SHOW_NO_RESULT_WARNING));
		if (isWarnOnNoResultProcess) {
			boolean connectedResultPort = ProcessTools.isProcessConnectedToResultPort(process);
			Operator lastExecutedProcessRootChild = ProcessTools.getLastExecutedRootChild(process);
			if (!connectedResultPort && lastExecutedProcessRootChild != null
					&& !ResultWarningPreventionRegistry.isResultWarningSuppressed(lastExecutedProcessRootChild)) {
				noResultConnectionBubble = ProcessGUITools.displayPrecheckNoResultPortInformation(process);
				return true;
			}
		}

		// no showstopper
		return false;
	}


	// The status bar of the application, usually displayed at the bottom
	// of the frame.
	private final StatusBar statusBar = new StatusBar(/*false, true, true*/);

	@Override
	public StatusBar getStatusBar() {
		return statusBar;
	}

	//Removed in 6.x
//	/*
//	 * (non-Javadoc)
//	 * 
//	 * @see com.rapidminer.gui.MainUIState#getImportAccessFileAction()
//	 */
//	@Override
//	public Action getImportAccessFileAction() {
//		return IMPORT_ACCESS_FILE_ACTION;
//	}

//	/*
//	 * (non-Javadoc)
//	 * 
//	 * @see com.rapidminer.gui.MainUIState#getImportCsvFileAction()
//	 */
//	@Override
//	public Action getImportCsvFileAction() {
//		return IMPORT_CSV_FILE_ACTION;
//	}

	//Removed in 6.x
//	/*
//	 * (non-Javadoc)
//	 * 
//	 * @see com.rapidminer.gui.MainUIState#getImportDatabaseTableAction()
//	 */
//	@Override
//	public Action getImportDatabaseTableAction() {
//		return IMPORT_DATABASE_TABLE_ACTION;
//	}

//	/*
//	 * (non-Javadoc)
//	 * 
//	 * @see com.rapidminer.gui.MainUIState#getImportExcelFileAction()
//	 */
//	@Override
//	public Action getImportExcelFileAction() {
//		return IMPORT_EXCEL_FILE_ACTION;
//	}

	//Removed in 6.x
//	/*
//	 * (non-Javadoc)
//	 * @see com.rapidminer.gui.MainUIState#getImportXmlFileAction()
//	 */
//	@Override
//	public Action getImportXmlFileAction() {
//		return IMPORT_XML_FILE_ACTION;
//	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.rapidminer.gui.MainUIState#getValidateAutomaticallyAction()
	 */
	@Override
	public ToggleAction getValidateAutomaticallyAction() {
		return VALIDATE_AUTOMATICALLY_ACTION;
	}

	/*
	 * (non-Javadoc)
	 * @see com.rapidminer.gui.MainUIState#getPropagateRealMetadataAction()
	 */
	@Override
	public Action getPropagateRealMetadataAction() {
		return PROPAGATE_REAL_METADATA_ACTION;
	}

//Removed in 7.0
//	/*
//	 * (non-Javadoc)
//	 * 
//	 * @see com.rapidminer.gui.MainUIState#getRewireRecursively()
//	 */
//	@Override
//	public Action getRewireRecursively() {
//		return REWIRE_RECURSIVELY;
//	}

	//Removed in 6.x
//	/*
//	 * (non-Javadoc)
//	 * @see com.rapidminer.gui.MainUIState#getRunRemoteNowAction()
//	 */
//	@Override
//	public RunRemoteNowAction getRunRemoteNowAction() {
//		return RUN_REMOTE_NOW_ACTION;
//	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.rapidminer.gui.MainUIState#getRunAction()
	 */
	@Override
	public RunAction getRunAction() {
		return RUN_ACTION;
	}

	/*
	 * (non-Javadoc)
	 * @see com.rapidminer.gui.MainUIState#getSaveAction()
	 */
	@Override
	public SaveAction getSaveAction() {
		return SAVE_ACTION;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.rapidminer.gui.MainUIState#getToggleExpertModeAction()
	 */
	@Override
	public ToggleAction getToggleExpertModeAction() {
		return TOGGLE_EXPERT_MODE_ACTION;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.rapidminer.gui.MainUIState#getWindow()
	 */
	@Override
	public JFrame getWindow() {
		return window;
	}

	/**
	 * @return the processThread
	 */
	public final ProcessThread getProcessThread() {
		return processThread;
	}

	/**
	 * @return the metaDataUpdateQueue
	 */
	public final MetaDataUpdateQueue getMetaDataUpdateQueue() {
		return metaDataUpdateQueue;
	}
}