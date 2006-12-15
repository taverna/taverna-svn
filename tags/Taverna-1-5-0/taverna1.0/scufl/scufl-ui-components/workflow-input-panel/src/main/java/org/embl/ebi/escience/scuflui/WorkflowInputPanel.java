package org.embl.ebi.escience.scuflui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JToolBar;

import org.apache.log4j.Logger;
import org.embl.ebi.escience.baclava.DataThing;
import org.embl.ebi.escience.scufl.Port;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scuflui.shared.ShadedLabel;

/**
 * Provide input for running a workflow. Supports loading/saving input
 * documents, loading individual inputs from files, and manually editing
 * list of input values.
 * 
 * @author Stian Soiland
 *
 */
public class WorkflowInputPanel extends JPanel {
	private static Logger logger = Logger.getLogger(WorkflowInputPanel.class);
	
	private ScuflModel model = null;
	DiagramAndInputs diagramAndInputs;
	
	
	public WorkflowInputPanel(ScuflModel model) {
		super(new BorderLayout());
		init(model);
	}

	private void init(ScuflModel model) {
		if (this.model != null) {
			logger.error("init(model) called twice");
			// TOOD: would be OK if we had a method for detaching
			// everything from the old model first..
		}
		this.model = model;
		removeAll();
		add(new Header(), BorderLayout.NORTH);
		diagramAndInputs = new DiagramAndInputs();
		add(diagramAndInputs, BorderLayout.CENTER);
		add(new RunButtons(), BorderLayout.SOUTH);
	}

	
	public class Header extends JPanel {
		ShadedLabel header = new ShadedLabel("Run workflow: " + model.getDescription().getTitle(),
				ShadedLabel.TAVERNA_BLUE);
		public Header() {
			super(new BorderLayout());
			add(header, BorderLayout.NORTH);
			//add(globalToolbar(), BorderLayout.SOUTH);
		}
		
		public JToolBar globalToolbar() {
            JToolBar toolbar = new JToolBar();
            JButton loadInputDocButton = new JButton("Load Input Doc",
                    TavernaIcons.openIcon);
            JButton saveInputDocButton = new JButton("Save Input Doc",
                    TavernaIcons.saveIcon);
            loadInputDocButton.setToolTipText("Load Input Document");
            saveInputDocButton.setToolTipText("Save Input Document");
            toolbar.setFloatable(false);
            toolbar.setRollover(true);
            toolbar.add(loadInputDocButton);
            toolbar.add(saveInputDocButton);
            return toolbar;
		}
	}
	
	public class DiagramAndInputs extends JSplitPane {
		DescriptionAndDiagram descAndDiag = new DescriptionAndDiagram();
		Inputs inputs = new Inputs();
		boolean initialDividerSet = false;
		public DiagramAndInputs() {
			super(JSplitPane.VERTICAL_SPLIT, false);
			setTopComponent(descAndDiag);
			setBottomComponent(inputs);
		}
		/**
		 * Set divider location to 30% at first call 
		 */
		public int getDividerLocation() {
			if (! initialDividerSet) {
				setDividerLocation(0.3);
				initialDividerSet = true;
			}
			return super.getDividerLocation();
		}
	}
	
	public class DescriptionAndDiagram extends JPanel {
		//JLabel description = new JLabel(model.getDescription().getText());
		ScuflSVGDiagram diagram = new ScuflSVGDiagram(false, false);
		public DescriptionAndDiagram() {
			super(new BorderLayout());
			// FIXME: make description wrap lines
			JTextArea description = new JTextArea(model.getDescription().getText());
			description.setEditable(false);
			description.setLineWrap(true);
			description.setOpaque(false);
			description.setWrapStyleWord(true);
			// Avoid stealing all width
        	description.setMinimumSize(new Dimension(25, 10));
			// FIXME: detach from model when window is closed
			diagram.attachToModel(model);
			add(description, BorderLayout.NORTH);
			add(diagram, BorderLayout.CENTER);
		}
	}

	
	public class Input extends JPanel {
		Port port;
		
		public Input(Port port) {
			this.port = port;
			setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
			
			JLabel title = new JLabel("<html><b>" +port.getName() + "</b> " +
					port.getMetadata().getFirstMIMEType() + "</html>");
			title.setAlignmentX(Component.LEFT_ALIGNMENT);
			add(title);
			
			String description = port.getMetadata().getDescription();
			if (! description.equals("")) {
				JTextArea descriptionArea = new JTextArea(description);
				descriptionArea.setEditable(false);
				descriptionArea.setLineWrap(true);
				descriptionArea.setOpaque(false);
				descriptionArea.setWrapStyleWord(true);
				descriptionArea.setAlignmentX(Component.LEFT_ALIGNMENT);
				add(descriptionArea);
			}
		}
	}
	
	public class Inputs extends JPanel {
		private WorkflowInputMapBuilder builder;
		public Inputs() {
			super();
			setLayout(new GridBagLayout());
			GridBagConstraints c = new GridBagConstraints();
			c.gridx = 0;
			c.gridy = GridBagConstraints.RELATIVE;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.weightx = 0.9;
			c.anchor = GridBagConstraints.FIRST_LINE_START;
			add(new ShadedLabel("Inputs", ShadedLabel.TAVERNA_GREEN), c);
			builder = new WorkflowInputMapBuilder();
			// FIXME: also call detachFromModel
			c.fill = GridBagConstraints.BOTH;
			c.weighty = 0.1;
			add(builder, c);
			builder.attachToModel(model);	
		}
	}

	public class RunButtons extends JPanel {
		Action runAction = new AbstractAction("Run workflow", TavernaIcons.runIcon){
			public void actionPerformed(ActionEvent e) {
				Map<String, DataThing> inputs = diagramAndInputs.inputs.builder.bakeInputMap();
				WorkflowInputPanelFactory.executeWorkflow(model, inputs);
			}
		};
		public RunButtons() {
			add(new JButton(runAction), BorderLayout.EAST);
		}
	}

	public void setInputs(Map<String, DataThing> inputs) throws InputsNotMatchingException {
		diagramAndInputs.inputs.builder.setWorkflowInputs(inputs);
	}
}
