/*
 * 
 */
package com.rapidminer.gui;

import com.rapidminer.Process;
import com.rapidminer.gui.flow.ProcessPanel;

/**
 * This interface gives access to the process related methods.
 * 
 * @author Gabor Bakos
 */
public interface ProcessState {

	public void validateProcess(boolean force);

	public int getProcessState();

	public Process getProcess();

	/**
	 * Creates a new process. If there are unsaved changes, the user will be asked to save their
	 * work.
	 */
	public void newProcess();

	/**
	 * Creates a new process. Depending on the given parameter, the user will or will not be asked
	 * to save unsaved changes.
	 *
	 * @param checkforUnsavedWork
	 *            Iff {@code true} the user is asked to save their unsaved work (if any), otherwise
	 *            unsaved work is discarded without warning.
	 */
	public void newProcess(final boolean checkforUnsavedWork);


	/**
	 * Runs or resumes the current process. If the process is started, checks for potential errors
	 * first and prevents execution unless the user has disabled the pre-run check.
	 */
	public void runProcess();

	/**
	 * Runs or resumes the current process.
	 *
	 * @param precheckBeforeExecution
	 *            if {@code true} and the process is started, checks for potential errors first and
	 *            prevents execution unless the user has disabled the pre-run check
	 */
	public void runProcess(boolean precheckBeforeExecution);

	/**
	 * Can be used to stop the currently running process. Please note that the
	 * ProcessThread will still be running in the background until the current
	 * operator is finished.
	 */
	public void stopProcess();

	public void pauseProcess();

	/**
	 * Sets a new process and registers the MainFrame's listeners.
	 */
	public void setProcess(Process process, boolean newProcess);

	/**
	 * Sets a new process and registers the MainFrame's listeners.
	 */
	public void setProcess(Process process, boolean newProcess, boolean addToUndoList);

	/** Returns true if the process has changed since the last save. */
	public boolean isChanged();

	public void undo();

	public void redo();

	/**
	 * Returns <code>true</code> if the current process has undo steps available.
	 * @return
	 */
	public boolean hasUndoSteps();

	/**
	 * Returns <code>true</code> if the current process has redo steps available.
	 * @return
	 */
	public boolean hasRedoSteps();

	/**
	 * @deprecated Since 7.5. Use {@link #setOpenedProcess(Process)}, the other parameters are
	 *             irrelevant.
	 */
	@Deprecated
	public void setOpenedProcess(Process process, boolean showInfo,
			final String sourceName);

	//Removed in 6.x
//	public void saveAsTemplate();

	public void fireProcessUpdated();

	public void processHasBeenSaved();

	public ProcessPanel getProcessPanel();
}