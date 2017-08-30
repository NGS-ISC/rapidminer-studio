/*
 * 
 */
package com.rapidminer.gui;

import javax.swing.JButton;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import com.rapidminer.tutorial.Tutorial;
import com.rapidminer.tutorial.gui.TutorialSelector;

/**
 * The menus related UI interface.
 * 
 * @author Gabor Bakos
 */
public interface MenusUI {

	/**
	 * @return the toolbar button for running processes on the Server
	 */
	public JButton getRunRemoteToolbarButton();

	public void addMenuItem(int menuIndex, int itemIndex, JMenuItem item);

	public void addMenu(int menuIndex, JMenu menu);

	public void addMenuSeparator(int menuIndex);

	/**
	 * This methods provide plugins the possibility to modify the menus
	 * 
	 * @param index
	 *            The index of the menu to remove. ({@code 0}-based.)
	 */
	public void removeMenu(int index);

	public void removeMenuItem(int menuIndex, int itemIndex);

	/**
	 * This returns the file menu to change menu entries
	 * 
	 * @return The file menu.
	 */
	public JMenu getFileMenu();

	/**
	 * This returns the settings menu to change menu entries
	 *
	 * @return the settings menu
	 */
	public JMenu getSettingsMenu();

	/**
	 * This returns the settings menu to change menu entries.
	 *
	 * @deprecated the tools menu was split into multiple menus. Use {@link #getConnectionsMenu()}
	 *             or {@link #getSettingsMenu()} instead
	 */
	@Deprecated
	public JMenu getToolsMenu();

//Removed in 7.0
//	/**
//	 * This returns the complete menu bar to insert additional menus
//	 * 
//	 * @return The main menubar.
//	 */
//	public JMenuBar getMainMenuBar();

	/**
	 * This returns the edit menu to change menu entries
	 * 
	 * @return The edit menu.
	 */
	public JMenu getEditMenu();

	/**
	 * This returns the process menu to change menu entries
	 * 
	 * @return The process menu.
	 */
	public JMenu getProcessMenu();

	/**
	 * This returns the help menu to change menu entries
	 * 
	 * @return The help menu.
	 */
	public JMenu getHelpMenu();

	/**
	 * This returns the extensions menu to change menu entries
	 *
	 * @since 7.0.0
	 */
	public JMenu getExtensionsMenu();

	/**
	 * The {@link TutorialSelector} holds the selected {@link Tutorial}.
	 *
	 * @return the registered tutorial selector
	 * @since 7.0.0
	 */
	public TutorialSelector getTutorialSelector();

	JMenu getConnectionsMenu();
}