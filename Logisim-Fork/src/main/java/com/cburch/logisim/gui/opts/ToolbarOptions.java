/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.gui.opts;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;

import com.cburch.logisim.file.Loader;
import com.cburch.logisim.file.ToolbarData;
import com.cburch.logisim.file.XmlWriter;
import com.cburch.logisim.gui.main.ProjectExplorer;
import com.cburch.logisim.gui.main.ProjectExplorer.Event;
import com.cburch.logisim.tools.AddTool;
import com.cburch.logisim.tools.Tool;
import com.cburch.logisim.util.TableLayout;

class ToolbarOptions extends OptionsPanel {
	private class Listener implements ProjectExplorer.Listener, ActionListener, ListSelectionListener {
		@Override
		public void actionPerformed(ActionEvent event) {
			Object src = event.getSource();
			if (src == addTool) {
				doAddTool(explorer.getSelectedTool().cloneTool());
				saveToolBarData();
			} else if (src == addSeparator) {
				getOptions().getToolbarData().addSeparator();
				saveToolBarData();
			} else if (src == moveUp) {
				doMove(-1);
				saveToolBarData();
			} else if (src == moveDown) {
				doMove(1);
				saveToolBarData();
			} else if (src == remove) {
				int index = list.getSelectedIndex();
				if (index >= 0) {
					getProject().doAction(ToolbarActions.removeTool(getOptions().getToolbarData(), index));
					list.clearSelection();
				}
				saveToolBarData();
			}
		}

		private void computeEnabled() {
			int index = list.getSelectedIndex();
			addTool.setEnabled(explorer.getSelectedTool() != null);
			moveUp.setEnabled(index > 0);
			moveDown.setEnabled(index >= 0 && index < list.getModel().getSize() - 1);
			remove.setEnabled(index >= 0);
		}

		@Override
		public void deleteRequested(Event event) {
		}

		private void doAddTool(Tool tool) {
			if (tool != null) {
				getProject().doAction(ToolbarActions.addTool(getOptions().getToolbarData(), tool));
			}
		}

		private void doMove(int delta) {
			int oldIndex = list.getSelectedIndex();
			int newIndex = oldIndex + delta;
			ToolbarData data = getOptions().getToolbarData();
			if (oldIndex >= 0 && newIndex >= 0 && newIndex < data.size()) {
				getProject().doAction(ToolbarActions.moveTool(data, oldIndex, newIndex));
				list.setSelectedIndex(newIndex);
			}
		}

		@Override
		public void doubleClicked(Event event) {
			Object target = event.getTarget();
			if (target instanceof Tool)
				doAddTool((Tool) target);
		}

		@Override
		public JPopupMenu menuRequested(Event event) {
			return null;
		}

		@Override
		public void moveRequested(Event event, AddTool dragged, AddTool target) {
		}

		@Override
		public void selectionChanged(Event event) {
			computeEnabled();
		}

		@Override
		public void valueChanged(ListSelectionEvent event) {
			computeEnabled();
		}
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -8520398012707734655L;

	private Listener listener = new Listener();

	private ProjectExplorer explorer;
	private JButton addTool;
	private JButton addSeparator;
	private JButton moveUp;
	private JButton moveDown;
	private JButton remove;
	private ToolbarList list;

	public ToolbarOptions(OptionsFrame window) {
		super(window);
		explorer = new ProjectExplorer(getProject());
		addTool = new JButton();
		addSeparator = new JButton();
		moveUp = new JButton();
		moveDown = new JButton();
		remove = new JButton();

		list = new ToolbarList(getOptions().getToolbarData());

		TableLayout middleLayout = new TableLayout(1);
		JPanel middle = new JPanel(middleLayout);
		middle.add(addTool);
		middle.add(addSeparator);
		middle.add(moveUp);
		middle.add(moveDown);
		middle.add(remove);
		middleLayout.setRowWeight(4, 1.0);

		explorer.setListener(listener);
		addTool.addActionListener(listener);
		addSeparator.addActionListener(listener);
		moveUp.addActionListener(listener);
		moveDown.addActionListener(listener);
		remove.addActionListener(listener);
		list.addListSelectionListener(listener);
		listener.computeEnabled();

		GridBagLayout gridbag = new GridBagLayout();
		GridBagConstraints gbc = new GridBagConstraints();
		setLayout(gridbag);
		JScrollPane explorerPane = new JScrollPane(explorer, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		JScrollPane listPane = new JScrollPane(list, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gridbag.setConstraints(explorerPane, gbc);
		add(explorerPane);
		gbc.fill = GridBagConstraints.VERTICAL;
		gbc.anchor = GridBagConstraints.NORTH;
		gbc.weightx = 0.0;
		gridbag.setConstraints(middle, gbc);
		add(middle);
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weightx = 1.0;
		gridbag.setConstraints(listPane, gbc);
		add(listPane);
	}

	@Override
	public String getHelpText() {
		return Strings.get("toolbarHelp");
	}

	@Override
	public String getTitle() {
		return Strings.get("toolbarTitle");
	}

	@Override
	public void localeChanged() {
		addTool.setText(Strings.get("toolbarAddTool"));
		addSeparator.setText(Strings.get("toolbarAddSeparator"));
		moveUp.setText(Strings.get("toolbarMoveUp"));
		moveDown.setText(Strings.get("toolbarMoveDown"));
		remove.setText(Strings.get("toolbarRemove"));
		list.localeChanged();
	}
	private void saveToolBarData() {
		Loader loader = getLogisimFile().getLoader();
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = null;
		try {
			docBuilder = docFactory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
		Document doc = docBuilder.newDocument();
		XmlWriter writer = new XmlWriter(getLogisimFile(), doc, loader);
		writer.saveToolBarData();
	}
}
