package net.sf.taverna.t2.workbench.run;

import java.awt.BorderLayout;
import java.awt.Color;
import java.util.Date;
import java.util.Map;

import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import net.sf.taverna.platform.spring.RavenAwareClassPathXmlApplicationContext;
import net.sf.taverna.t2.reference.ReferenceContext;
import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.workbench.reference.config.ReferenceConfiguration;
import net.sf.taverna.t2.workbench.ui.zaria.UIComponentSPI;
import net.sf.taverna.t2.workflowmodel.Dataflow;

import org.springframework.context.ApplicationContext;

public class DataflowRunsComponent extends JSplitPane implements UIComponentSPI {

	private static final long serialVersionUID = 1L;
	
	private static DataflowRunsComponent singletonInstance;
	
	private DefaultListModel runListModel;
	
	private JList runList;
	
	private JSplitPane topPanel;
	
	private DataflowRunsComponent() {
		super(JSplitPane.VERTICAL_SPLIT);
		setDividerLocation(400);
		
		topPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		topPanel.setDividerLocation(200);
		topPanel.setBorder(null);
		setTopComponent(topPanel);
		
		runListModel = new DefaultListModel();
		runList = new JList(runListModel);
		runList.setBorder(new EmptyBorder(5,5,5,5));
		runList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
//		runList.setSelectedIndex(0);
		runList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				if (!e.getValueIsAdjusting()) {
					Object selection = runList.getSelectedValue();
					if (selection instanceof DataflowRun) {
						int location = getDividerLocation();
						DataflowRun dataflowRun = (DataflowRun) selection;
						topPanel.setBottomComponent(dataflowRun.getMonitorViewComponent());
						setBottomComponent(dataflowRun.getResultsComponent());
						setDividerLocation(location);
						revalidate();
					}
				}
			}
		});
		
		JPanel runListPanel = new JPanel(new BorderLayout());
		runListPanel.setBorder(LineBorder.createGrayLineBorder());
		
		JLabel worklflowRunsLabel = new JLabel("Workflow Runs");
		worklflowRunsLabel.setBorder(new EmptyBorder(5,5,5,5));
		runListPanel.add(worklflowRunsLabel, BorderLayout.NORTH);
		
		JScrollPane scrollPane = new JScrollPane(runList);
		scrollPane.setBorder(null);
		runListPanel.add(scrollPane, BorderLayout.CENTER);		

		topPanel.setTopComponent(runListPanel);
		
		JPanel tempMonitorPanel = new JPanel(new BorderLayout());
		tempMonitorPanel.setBorder(LineBorder.createGrayLineBorder());
		tempMonitorPanel.setBackground(Color.WHITE);
		tempMonitorPanel.add(new JLabel("No workflows have been run yet", JLabel.CENTER), BorderLayout.CENTER);
		topPanel.setBottomComponent(tempMonitorPanel);
				
		JPanel tempResultsPanel = new JPanel(new BorderLayout());
		tempResultsPanel.setBackground(Color.WHITE);
		
		worklflowRunsLabel.setBorder(new EmptyBorder(5,5,5,5));

		tempResultsPanel.add(new JLabel("Results"), BorderLayout.NORTH);
		tempResultsPanel.add(new JLabel("No results yet", JLabel.CENTER), BorderLayout.CENTER);
		setBottomComponent(tempResultsPanel);
		
//		revalidate();
//		setDividerLocation(.3);

	}
	
	public static DataflowRunsComponent getInstance() {
		if (singletonInstance == null) {
			singletonInstance = new DataflowRunsComponent();
		}
		return singletonInstance;
	}
	
	public void runDataflow(final Dataflow dataflow, ReferenceService referenceService, Map<String, T2Reference> inputs) {
		DataflowRun runComponent = new DataflowRun(dataflow, referenceService, inputs, new Date());
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
