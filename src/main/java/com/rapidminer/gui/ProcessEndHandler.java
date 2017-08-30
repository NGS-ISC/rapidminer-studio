/*
 * 
 */
package com.rapidminer.gui;

import com.rapidminer.Process;
import com.rapidminer.operator.IOContainer;

/**
 * A simple interface to define the {@link #processEnded(Process, IOContainer)}
 * method.
 * 
 * @author Gabor Bakos
 */
public interface ProcessEndHandler {
	/**
	 * Will be invoked from the process thread after the process was
	 * successfully ended.
	 * 
	 * @param process
	 *            The {@link Process} which ended.
	 * @param results
	 *            The results of the execution.
	 */
	void processEnded(final Process process, final IOContainer results);
}