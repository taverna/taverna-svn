package net.sf.taverna.t2.plugin;

import java.awt.BorderLayout;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import net.sf.taverna.t2.facade.ResultListener;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.DataflowOutputPort;
import net.sf.taverna.t2.workflowmodel.EditException;

import org.embl.ebi.escience.scuflui.spi.UIComponentSPI;

public class ResultComponent extends JPanel implements UIComponentSPI {

	private JTabbedPane tabbedPane;
	
	public ResultComponent() {
		setLayout(new BorderLayout());
		add(new JLabel("Results"), BorderLayout.NORTH);

		tabbedPane = new JTabbedPane();
		add(tabbedPane, BorderLayout.CENTER);
	}
	
	public void register(Dataflow dataflow, ResultListener resultListener) throws EditException {
		clear();
		List<? extends DataflowOutputPort> dataflowOutputPorts = dataflow.getOutputPorts();
		for (DataflowOutputPort dataflowOutputPort : dataflowOutputPorts) {
			JComponent outputPanel = new JPanel();
			OutputHandler.register(dataflowOutputPort, outputPanel, resultListener);
			tabbedPane.add(outputPanel);
		}
		revalidate();
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

	public void clear() {
		tabbedPane.removeAll();
	}

}
