package net.sf.taverna.t2.workbench.run;

import java.awt.BorderLayout;
import java.util.Date;
import java.util.Map;

import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
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

import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.workbench.ui.zaria.UIComponentSPI;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.DataflowInputPort;

public class DataflowRunsComponent extends JSplitPane implements UIComponentSPI {

	private static final long serialVersionUID = 1L;
	
	private static DataflowRunsComponent singletonInstance;

	private DefaultListModel runListModel;
	
	private JList runList;
	
	private DataflowRunsComponent() {
		
		runListModel = new DefaultListModel();
		runList = new JList(runListModel);
		runList.setBorder(new CompoundBorder(LineBorder.createGrayLineBorder() ,new EmptyBorder(5,5,5,5)));
		runList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
//		runList.setSelectedIndex(0);
		runList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				if (!e.getValueIsAdjusting()) {
					Object selection = runList.getSelectedValue();
					if (selection instanceof RunComponent) {
						RunComponent dataflowRun = (RunComponent) selection;
						setBottomComponent(dataflowRun);
						revalidate();
					}
				}
			}
		});
		
		JPanel runListPanel = new JPanel(new BorderLayout());
		runListPanel.add(runList, BorderLayout.CENTER);
		
		JScrollPane scrollPane = new JScrollPane(runListPanel);
		scrollPane.setBorder(null);
		setTopComponent(scrollPane);
		setDividerLocation(300);

	}
	
	public static DataflowRunsComponent getInstance() {
		if (singletonInstance == null) {
			singletonInstance = new DataflowRunsComponent();
		}
		return singletonInstance;
	}
	
	public void runDataflow(final Dataflow dataflow, Map<DataflowInputPort, T2Reference> inputs) {
		RunComponent runComponent = new RunComponent(dataflow, inputs, new Date());
		runListModel.add(0, runComponent);
		runList.setSelectedIndex(0);
		runComponent.run();
	}

	public ImageIcon getIcon() {
		// TODO Auto-generated method stub
		return null;
	}

	public void onDisplay() {
		// TODO Auto-generated method stub
		
	}

	public void onDispose() {
		// TODO Auto-generated method stub
		
	}

}
