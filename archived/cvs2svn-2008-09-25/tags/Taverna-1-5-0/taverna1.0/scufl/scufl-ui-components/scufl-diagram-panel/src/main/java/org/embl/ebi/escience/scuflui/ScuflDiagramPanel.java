/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.prefs.Preferences;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;

import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scuflui.shared.ExtensionFileFilter;
import org.embl.ebi.escience.scuflui.shared.ShadedLabel;
import org.embl.ebi.escience.scuflui.shared.StreamCopier;
import org.embl.ebi.escience.scuflui.shared.StreamDevourer;
import org.embl.ebi.escience.scuflui.spi.WorkflowModelViewSPI;

/**
 * Wraps a ScuflDiagram up in a JScrollPane and provides a toolbar to alter port
 * display, type display and allow save to disk of the current diagram view in
 * dot, svg or png formats.
 * 
 * @author Tom Oinn
 */
public class ScuflDiagramPanel extends JPanel implements WorkflowModelViewSPI {

	String[] displayPolicyStrings = { "All ports", "Bound ports", "No ports",
			"Blobs", "Blobs + Names" };

	String[] saveTypes = { "dot", "png", "svg", "ps", "ps2" };

	String[] saveExtensions = { "dot", "png", "svg", "ps", "ps" };

	String[] saveTypeNames = { "dot text", "PNG bitmap",
			"scalable vector graphics", "postscript", "postscript for PDF" };

	String[] alignment = { "Top to bottom", "Left to right" };

	JComboBox displayPolicyChooser = new JComboBox(displayPolicyStrings);

	JComboBox alignmentChooser = new JComboBox(alignment);

	ScuflSVGDiagram diagram = new ScuflSVGDiagram();

	JCheckBox typeDisplay = new JCheckBox("Show types", false);

	JCheckBox showBoring = new JCheckBox("Boring?", true);

	JCheckBox fitToWindow = new JCheckBox("Fit to window", true);

	final JFileChooser fc;

	
	
	public javax.swing.ImageIcon getIcon() {
		return TavernaIcons.windowDiagram;
	}

	JPopupMenu createSaveDiagramMenu() {
		JPopupMenu menu = new JPopupMenu();
		for (int i = 0; i < saveTypes.length; i++) {
			String type = saveTypes[i];
			String extension = saveExtensions[i];
			ImageIcon icon = new ImageIcon(ScuflDiagramPanel.class
					.getResource("icons/graphicalview/saveAs"
							+ type.toUpperCase() + ".png"));
			JMenuItem item = new JMenuItem("Save as " + saveTypeNames[i],
					icon);
			item.addActionListener(new DotInvoker(type, extension));
			menu.add(item);
		}
		return menu;
	}
	
	JPopupMenu createMenu() {
		JPopupMenu menu = new JPopupMenu();
		menu.add(new ShadedLabel("Port detail", ShadedLabel.TAVERNA_BLUE));
		menu.addSeparator();
		ButtonGroup portButtonGroup = new ButtonGroup();
		for (int i = 0; i < displayPolicyStrings.length; i++) {
			JRadioButtonMenuItem item = new JRadioButtonMenuItem(
					displayPolicyStrings[i]);
			item.setSelected(diagram.getDotView().getPortDisplay() == i);
			menu.add(item);
			portButtonGroup.add(item);
			final int j = i;
			item.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ae) {
					diagram.getDotView().setPortDisplay(j);
					updateDiagram();
				}
			});
		}
		menu.addSeparator();
		menu.add(new ShadedLabel("Alignment", ShadedLabel.TAVERNA_GREEN));
		menu.addSeparator();
		ButtonGroup alignButtonGroup = new ButtonGroup();
		for (int i = 0; i < alignment.length; i++) {
			JRadioButtonMenuItem item = new JRadioButtonMenuItem(alignment[i]);
			item
					.setSelected((diagram.getDotView().getAlignment() ? 1 : 0) == i);
			menu.add(item);
			alignButtonGroup.add(item);
			final boolean b = (i == 1);
			item.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ae) {
					diagram.getDotView().setAlignment(b);
					updateDiagram();
				}
			});
		}
		menu.addSeparator();
		menu.add(new ShadedLabel("Features", ShadedLabel.TAVERNA_ORANGE));
		menu.addSeparator();
		JCheckBoxMenuItem types = new JCheckBoxMenuItem("Show types");
		types.setSelected(diagram.getDotView().getTypeLabelDisplay());
		types.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				diagram.getDotView().setTypeLabelDisplay(
						e.getStateChange() == ItemEvent.SELECTED);
				updateDiagram();
			}
		});
		menu.add(types);
		JCheckBoxMenuItem boring = new JCheckBoxMenuItem("Show boring entities");
		boring.setSelected(diagram.getDotView().getShowBoring());
		boring.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				diagram.getDotView().setBoring(
						e.getStateChange() == ItemEvent.SELECTED);
				updateDiagram();
			}
		});
		menu.add(boring);
		JCheckBoxMenuItem inline = new JCheckBoxMenuItem(
				"Expand nested workflows");
		inline.setSelected(diagram.getDotView().getExpandWorkflow());
		inline.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				diagram.getDotView().setExpandWorkflow(
						e.getStateChange() == ItemEvent.SELECTED);
				updateDiagram();
			}
		});
		menu.add(inline);
		/**
		 * JCheckBoxMenuItem scale = new JCheckBoxMenuItem("Fit to window");
		 * scale.setSelected(diagram.getFitToWindow());
		 * scale.addItemListener(new ItemListener() { public void
		 * itemStateChanged(ItemEvent e) {
		 * diagram.setFitToWindow(e.getStateChange() == ItemEvent.SELECTED); }
		 * }); menu.add(scale);
		 */
		return menu;
	}

	public void updateDiagram() {
		diagram.updateGraphic();
		doLayout();
		repaint();
	}

	public ScuflDiagramPanel() {
		super();
		setLayout(new BorderLayout());

		// Create the diagram
		JScrollPane diagramPane = new JScrollPane(diagram);
		diagramPane.setPreferredSize(new Dimension(0, 0));
		diagramPane.getViewport().setBackground(java.awt.Color.WHITE);
		add(diagramPane, BorderLayout.CENTER);
		// diagram.setFitToWindow(true);

		JToolBar toolbar = new JToolBar();

		
		final JButton saveAs = new JButton("Save diagram", 
				TavernaIcons.savePNGIcon);
		saveAs.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				JPopupMenu menu = createSaveDiagramMenu();
				menu.show(saveAs, 0, saveAs.getHeight());
			}
		});
		toolbar.add(saveAs);
		toolbar.addSeparator();

		final JButton configure = new JButton("Configure diagram",
				TavernaIcons.editIcon);
		configure.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				JPopupMenu menu = createMenu();
				menu.show(configure, 0, configure.getHeight());
			}
		});
		toolbar.add(new JButton(new RefreshAction()));
		toolbar.addSeparator();
		toolbar.add(configure);
		toolbar.add(Box.createHorizontalGlue());

		toolbar.setFloatable(false);
		toolbar.setRollover(true);
		toolbar.setMaximumSize(new Dimension(2000, 30));
		fc = new JFileChooser();
		add(toolbar, BorderLayout.PAGE_START);
	}

	class DotInvoker implements ActionListener {
		String type = "dot";

		String extension = "dot";

		public DotInvoker() {
			//
		}

		public DotInvoker(String type, String extension) {
			this.type = type;
			this.extension = extension;
		}

		public void actionPerformed(ActionEvent e) {
			try {
				Preferences prefs = Preferences
						.userNodeForPackage(ScuflDiagramPanel.class);
				String curDir = prefs.get("currentDir", System
						.getProperty("user.home"));
				fc.setCurrentDirectory(new File(curDir));
				fc.resetChoosableFileFilters();
				fc.setFileFilter(new ExtensionFileFilter(
						new String[] { extension }));
				int returnVal = fc.showSaveDialog(ScuflDiagramPanel.this);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					prefs
							.put("currentDir", fc.getCurrentDirectory()
									.toString());
					File file = fc.getSelectedFile();
					// Rewrite the file name if it doesn't end with the
					// specified extension
					if (file.getName().endsWith("." + extension) == false) {
						file = new File(file.toURI().resolve(
								file.getName() + "." + extension));
					}
					if (type.equals("dot")) {
						// Just write out the dot text, no processing required
						PrintWriter out = new PrintWriter(new FileWriter(file));
						out.println(diagram.getDot());
						out.flush();
						out.close();
					} else {
						FileOutputStream fos = new FileOutputStream(file);
						// Invoke DOT to get the SVG document as a byte stream
						// FIXME: Should use MyGridConfiguration.getProperty(), 
						// but that would not include the system property
						// specified at command line on Windows (runme.bat) 
						// and OS X (Taverna.app)
						String dotLocation = System
								.getProperty("taverna.dotlocation");
						if (dotLocation == null) {
							dotLocation = "dot";
						}
						Process dotProcess = Runtime.getRuntime().exec(
								new String[] { dotLocation, "-T" + type });
						OutputStream dotOut = dotProcess.getOutputStream();
						dotOut.write(diagram.getDot().getBytes());
						dotOut.flush();
						dotOut.close();
						new StreamDevourer(dotProcess.getErrorStream()).start();
						new StreamCopier(dotProcess.getInputStream(), fos)
								.start();
					}
				}
			} catch (Exception ex) {
				ex.printStackTrace();
				JOptionPane.showMessageDialog(null,
						"Problem saving diagram : \n" + ex.getMessage(),
						"Error!", JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	public void attachToModel(ScuflModel model) {
		diagram.attachToModel(model);
	}

	public void detachFromModel() {
		diagram.detachFromModel();
	}

	public String getName() {
		return "Workflow diagram";
	}

	public void onDisplay() {
	}

	public void onDispose() {
		diagram.detachFromModel();		
	}

	class RefreshAction extends AbstractAction {
		public RefreshAction() {
			putValue(SMALL_ICON, TavernaIcons.refreshIcon);
			putValue(NAME, "Refresh");
			putValue(SHORT_DESCRIPTION, "Redraw workflow diagram");
		}
		public void actionPerformed(ActionEvent e) {
			updateDiagram();
		}
	}


}
