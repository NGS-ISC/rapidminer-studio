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
package com.rapidminer.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;

import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkEvent.EventType;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.rapidminer.Process;
import com.rapidminer.gui.actions.SaveAction;
import com.rapidminer.gui.look.Colors;
import com.rapidminer.gui.processeditor.ProcessEditor;
import com.rapidminer.gui.tools.ExtendedJScrollPane;
import com.rapidminer.gui.tools.FilterTextField;
import com.rapidminer.gui.tools.ResourceDockKey;
import com.rapidminer.gui.tools.SwingTools;
import com.rapidminer.gui.tools.UpdateQueue;
import com.rapidminer.gui.tools.dialogs.ConfirmDialog;
import com.rapidminer.io.process.XMLTools;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.tools.LogService;
import com.rapidminer.tools.WebServiceTools;
import com.rapidminer.tools.XMLException;
import com.rapidminer.tools.plugin.Plugin;
import com.vlsolutions.swing.docking.DockKey;
import com.vlsolutions.swing.docking.Dockable;


/**
 *
 * This class contains methods that generate an item that shows a help text either from an XML file
 * if provided or from the description contained by the operator itself. The actual document is
 * generated by the {@link OperatorDocToHtmlConverter}.
 *
 * @author Philipp Kersting, Marco Boeck
 *
 */
public class OperatorDocumentationBrowser extends JPanel implements Dockable, ProcessEditor {

	final JEditorPane editor = new JEditorPane("text/html", "<html>-</html>");

	private ExtendedJScrollPane scrollPane = new ExtendedJScrollPane();

	public static final String DOCUMENTATION_ROOT = "core/";

	public Operator displayedOperator = null;

	public URL currentResourceURL = null;

	private boolean ignoreSelections = false;

	public static final String OPERATOR_HELP_DOCK_KEY = "operator_help";

	private final DockKey DOCK_KEY = new ResourceDockKey(OPERATOR_HELP_DOCK_KEY);

	private static final long serialVersionUID = 1L;

	private UpdateQueue documentationUpdateQueue = new UpdateQueue("documentation_update_queue");

	/**
	 * Prepares the dockable and its elements.
	 */
	public OperatorDocumentationBrowser() {
		setLayout(new BorderLayout());

		// Instantiate Editor and set Settings
		editor.addHyperlinkListener(new OperatorHelpLinkListener());
		editor.setEditable(false);
		HTMLEditorKit hed = new HTMLEditorKit();
		hed.setStyleSheet(createStyleSheet(hed.getStyleSheet()));
		editor.setEditorKit(hed);
		editor.setBackground(Colors.PANEL_BACKGROUND);
		editor.setContentType("text/html");

		// add editor to scrollPane
		scrollPane = new ExtendedJScrollPane(editor);

		scrollPane.setMinimumSize(new Dimension(100, 100));
		scrollPane.setPreferredSize(new Dimension(100, 100));

		// add scrollPane to Dockable
		scrollPane.setBorder(null);
		this.add(scrollPane, BorderLayout.CENTER);
		this.setVisible(true);
		this.validate();

		documentationUpdateQueue.start();
	}

	@Override
	public void processChanged(Process process) {
		// not needed
	}

	/**
	 * This method gets called if the user clicks on an operator that has been placed in the
	 * process.
	 */
	@Override
	public void setSelection(List<Operator> selection) {
		if (selection != null && !selection.isEmpty()) {
			Operator operator = selection.get(0);
			if (!operator.equals(displayedOperator) && !ignoreSelections) {
				assignDocumentation(operator);
			}
		}
	}

	/**
	 * This is called by the {@link #setSelection(List)} method. It creates an absolute path that
	 * indicates the corresponding documentation XML file.
	 */
	private void assignDocumentation(Operator operator) {
		URL resourceURL = getDocResourcePath(operator);
		changeDocumentation(operator);
		displayedOperator = operator;
		currentResourceURL = resourceURL;
	}

	@Override
	public void processUpdated(Process process) {
		// not needed
	}

	@Override
	public Component getComponent() {
		return this;
	}

	@Override
	public DockKey getDockKey() {
		return DOCK_KEY;
	}

	/**
	 * Event handler that handles clicking on a link to a tutorial process or internal anchor
	 */
	private class OperatorHelpLinkListener implements HyperlinkListener {

		@Override
		public void hyperlinkUpdate(HyperlinkEvent e) {
			if (e.getEventType().equals(EventType.ACTIVATED)) {
				if (e.getDescription().startsWith("tutorial:")) {
					// ask for confirmation before stopping the currently running process and
					// opening another one!
					if (RapidMinerGUI.getMainFrame().getProcessState() == Process.PROCESS_STATE_RUNNING
							|| RapidMinerGUI.getMainFrame().getProcessState() == Process.PROCESS_STATE_PAUSED) {
						if (SwingTools.showConfirmDialog("close_running_process",
								ConfirmDialog.YES_NO_OPTION) != ConfirmDialog.YES_OPTION) {
							return;
						}
					}

					// ask user if he wants to save his current process because the example process
					// will replace his current process
					if (RapidMinerGUI.getMainFrame().isChanged()) {
						// current process is flagged as unsaved
						int returnVal = SwingTools.showConfirmDialog("save_before_show_tutorial_process",
								ConfirmDialog.YES_NO_CANCEL_OPTION);
						if (returnVal == ConfirmDialog.CANCEL_OPTION) {
							return;
						} else if (returnVal == ConfirmDialog.YES_OPTION) {
							SaveAction.saveAsync(RapidMinerGUI.getMainFrame().getProcess());
						}
					} else {
						// current process is not flagged as unsaved
						if (SwingTools.showConfirmDialog("show_tutorial_process",
								ConfirmDialog.OK_CANCEL_OPTION) == ConfirmDialog.CANCEL_OPTION) {
							return;
						}
					}

					try {
						if (currentResourceURL == null) {
							// should not happen, because then there would be no link in the first
							// place
							return;
						}
						Document document = XMLTools.parse(WebServiceTools.openStreamFromURL(currentResourceURL));

						int index = Integer.parseInt(e.getDescription().substring("tutorial:".length()));

						NodeList nodeList = document.getElementsByTagName("tutorialProcess");
						Node processNode = nodeList.item(index - 1);
						Node process = null;
						int i = 0;
						while (i < processNode.getChildNodes().getLength()) {
							if (processNode.getChildNodes().item(i).getNodeName().equals("process")) {
								process = processNode.getChildNodes().item(i);
							}
							i++;
						}

						StringWriter buffer = new StringWriter();
						DOMSource processSource = new DOMSource(process);
						Transformer t = TransformerFactory.newInstance().newTransformer();
						t.transform(processSource, new StreamResult(buffer));
						Process exampleProcess = new Process(buffer.toString());
						Operator formerOperator = displayedOperator;
						ignoreSelections = true;
						RapidMinerGUI.getMainFrame().setProcess(exampleProcess, true);
						Collection<Operator> displayedOperators = RapidMinerGUI.getMainFrame().getProcess()
								.getAllOperators();
						for (Operator item : displayedOperators) {
							if (item.getClass().equals(formerOperator.getClass())) {
								RapidMinerGUI.getMainFrame().selectOperator(item);
								ignoreSelections = false;
							}
						}
					} catch (TransformerException e1) {
						LogService.getRoot().log(Level.WARNING,
								"com.rapidminer.tools.documentation.ExampleProcess.creating_example_process_error", e1);
					} catch (SAXException e1) {
						LogService.getRoot().log(Level.WARNING,
								"com.rapidminer.tools.documentation.ExampleProcess.parsing_xml_error", e1);
					} catch (IOException e1) {
						LogService.getRoot().log(Level.WARNING,
								"com.rapidminer.tools.documentation.ExampleProcess.reading_file_error", e1);
					} catch (XMLException e1) {
						LogService.getRoot().log(Level.WARNING,
								"com.rapidminer.tools.documentation.ExampleProcess.parsing_xml_error", e1);
					}

				} else if (e.getDescription().startsWith("#")) {
					// go to internal anchor
					String desc = e.getDescription();
					desc = desc.substring(1);
					editor.scrollToReference(desc);
				} else if (e.getDescription().startsWith("tag:")) {
					// filter tag in operator list
					FilterTextField filterField = RapidMinerGUI.getMainFrame().getNewOperatorEditor().getNewOperatorGroupTree().getFilterField();
					filterField.setFilterText(e.getDescription().substring(4));
				} else {
					// open url in default browser
					Desktop desktop = Desktop.getDesktop();
					if (desktop.isSupported(Desktop.Action.BROWSE)) {
						URI uri;
						try {
							uri = new java.net.URI(e.getDescription());
							desktop.browse(uri);
						} catch (URISyntaxException e1) {
							LogService.getRoot().log(Level.WARNING, "com.rapidminer.tools.desktop.browse.malformed_url", e1);
							return;
						} catch (IOException e1) {
							LogService.getRoot().log(Level.WARNING, "com.rapidminer.tools.desktop.browse.open_browser", e1);
							return;
						}
					} else {
						LogService.getRoot().log(Level.WARNING, "com.rapidminer.tools.desktop.browse.not_supported");
						return;
					}
				}
			}
		}
	}

	/**
	 * Refreshes the documentation text.
	 *
	 * @param resourceURL
	 *            url to the xml resource
	 */
	private void changeDocumentation(final Operator operator) {
		documentationUpdateQueue.execute(new Runnable() {

			@Override
			public void run() {

				final String finalHtml = OperatorDocLoader.getDocumentation(operator);
				SwingUtilities.invokeLater(new Runnable() {

					@Override
					public void run() {
						editor.setText("<html>" + finalHtml + "</html>");
						editor.setCaretPosition(0);
					}
				});

			}

		});
	}

	/**
	 * This method creates and returns a stylesheet that makes the documentation look as it's
	 * supposed to look.
	 *
	 * @return the stylesheet
	 */
	private StyleSheet createStyleSheet(StyleSheet css) {
		css.addRule("body {font-family: Open Sans; font-size: 10px;}");
		css.addRule("p {font-size:10px; font-family: Open Sans; margin-top: 0px; padding-top: 0px;}");
		css.addRule("ul li {padding-bottom:1ex; font-family: Open Sans; font-size:10px; list-style-type: circle;}");
		css.addRule("h2 {font-size:14px; font-family: Open Sans; margin-bottom: 0px; margin-top: 0px;}");
		css.addRule("h4 {color: #000000; font-size:10px; font-family: Open Sans; font-weight: bold; margin-bottom: 5px;}");
		css.addRule("h5 {color: #3399FF; font-size:11px; font-family: Open Sans;}");
		css.addRule("h5 img {margin-right:8px; font-family: Open Sans;}");
		css.addRule(
				".parametersHeading {color: #000000; font-size:10px; font-family: Open Sans; font-weight: bold; margin-bottom: 0px;}");
		css.addRule(".parametersTable {cellspacing: 0px; border: 0;}");
		css.addRule(".typeIcon {height: 10px; width: 10px;}");
		css.addRule("td {vertical-align: top; font-family: Open Sans;}");
		css.addRule(".lilIcon {padding: 2px 4px 2px 0px;}");
		css.addRule("td {font-size: 10px; font-family: Open Sans;}");
		css.addRule(".packageName {color: #777777; font-size:10px; font-family: Open Sans; font-weight: normal;}");
		css.addRule(".parameterDetails {color: #777777; font-size:9px; font-family: Open Sans;}");
		css.addRule(".parameterDetailsCell{margin-bottom: 4px; padding-bottom: 4px;}");
		css.addRule(".tutorialProcessLink {margin-top: 6px; margin-bottom: 5px;}");
		css.addRule("hr {border: 0;height: 1px;}");
		css.addRule("a {color:" + SwingTools.getColorHexValue(Colors.LINKBUTTON_LOCAL) + "}");
		css.addRule("table {align:left;}");
		css.addRule(".tags {font-size: 9px; color: #777777;}");
		return css;
	}

	/**
	 * Sets the operator for which the operator documentation is shown.
	 *
	 * @param operator
	 */
	public void setDisplayedOperator(Operator operator) {
		if (operator != null && !operator.getOperatorDescription().isDeprecated()
				&& (this.displayedOperator == null || this.displayedOperator != null && !operator.getOperatorDescription()
						.getName().equals(this.displayedOperator.getOperatorDescription().getName()))) {
			assignDocumentation(operator);
		}
	}

	public static URL getDocResourcePath(Operator op) {
		Plugin provider = op.getOperatorDescription().getProvider();
		boolean isExtension = provider != null;
		String documentationRoot = isExtension ? provider.getPrefix() + "/" : DOCUMENTATION_ROOT;
		String groupPath = op.getOperatorDescription().getGroup().replace(".", "/");

		// if extension uses the extension folder as tree root...
		if (isExtension && provider.useExtensionTreeRoot()
				&& groupPath.startsWith(OperatorDescription.EXTENSIONS_GROUP_IDENTIFIER)) {

			// remove extension group identifier
			groupPath = groupPath.substring(groupPath.indexOf('/') + 1, groupPath.length());

			// remove extension name
			int firstIndexOfSlash = groupPath.indexOf('/');
			if (firstIndexOfSlash != -1) {
				groupPath = groupPath.substring(firstIndexOfSlash + 1, groupPath.length()) + "/";
			} else {
				groupPath = "";
			}
		} else {
			groupPath += "/";
		}
		String key = op.getOperatorDescription().getKeyWithoutPrefix();

		String opDescXMLResourcePath = documentationRoot + groupPath + key + ".xml";
		URL resourceURL = Plugin.getMajorClassLoader().getResource(opDescXMLResourcePath);
		return resourceURL;
	}

}
