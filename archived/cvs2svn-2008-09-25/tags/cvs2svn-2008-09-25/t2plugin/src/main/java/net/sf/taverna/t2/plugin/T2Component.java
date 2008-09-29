package net.sf.taverna.t2.plugin;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.prefs.Preferences;

import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.ListSelectionModel;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.log4j.Logger;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scuflui.spi.WorkflowModelViewSPI;

public class T2Component extends JSplitPane implements WorkflowModelViewSPI {

	private static final long serialVersionUID = 6964568042620234711L;

	private static final Logger logger = Logger.getLogger(T2Component.class);

	public static final String DATA_STORE_PROPERTY = "dataManagerDir";
	
	static final File defaultDataManagerDir = new File(System
			.getProperty("taverna.home"), "t2-datamanager");

	static final Preferences userPrefs = Preferences
			.userNodeForPackage(T2Component.class);

	private ScuflModel model;
	
	private DefaultListModel runListModel;
	
	private JList runList;
	
	private RunComponent currentRun;
	
	private JPanel runPanel;

	private PreferencesFrame preferencesFrame = new PreferencesFrame();

	public T2Component() {
//		setLayout(new BorderLayout());
		
		runPanel = new JPanel(new BorderLayout());
		setBottomComponent(runPanel);
		
		currentRun = new RunComponent(this);
		runPanel.add(currentRun, BorderLayout.CENTER);
		
		JButton preferencesButton = new JButton("Preferences");
		preferencesButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				preferencesFrame.pack();
				preferencesFrame.setLocationRelativeTo(T2Component.this);
				preferencesFrame.setVisible(true);
			}

		});

		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		buttonPanel.add(preferencesButton);

		runListModel = new DefaultListModel();
		runListModel.add(0, currentRun);
		runList = new JList(runListModel);
		runList.setBorder(new CompoundBorder(LineBorder.createGrayLineBorder() ,new EmptyBorder(5,5,5,5)));
		runList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		runList.setSelectedIndex(0);
		runList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				if (!e.getValueIsAdjusting()) {
					Object selection = runList.getSelectedValue();
					if (selection instanceof RunComponent) {
						if (selection == currentRun) {
							currentRun.setModel(model);
						}
						runPanel.removeAll();
						runPanel.add((RunComponent) selection, BorderLayout.CENTER);
						runPanel.revalidate();
						runPanel.repaint();
					}
				}
			}
		});
		
		JPanel runListPanel = new JPanel(new BorderLayout());
		runListPanel.add(buttonPanel, BorderLayout.NORTH);
		runListPanel.add(runList, BorderLayout.CENTER);
		
		JScrollPane scrollPane = new JScrollPane(runListPanel);
		scrollPane.setBorder(null);
		setTopComponent(scrollPane);
		setDividerLocation(300);
	}
	
	public void addRun(RunComponent runComponent) {
		currentRun = new RunComponent(this);
		runListModel.add(0, currentRun);
		currentRun.setModel(model);
		runList.setSelectedIndex(1);
	}
	
	public void attachToModel(ScuflModel model) {
		this.model = model;
		currentRun.setModel(model);
		currentRun.hideHealthCheck();
		runList.setSelectedIndex(0);
	}

	public void detachFromModel() {

	}

	public ImageIcon getIcon() {
		return null;
	}

	public void onDisplay() {

	}

	public void onDispose() {

	}

}
