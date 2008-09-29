package net.sf.taverna.service.executeremotely.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
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

import org.embl.ebi.escience.baclava.DataThing;
import org.embl.ebi.escience.scufl.Port;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scuflui.InputsNotMatchingException;
import org.embl.ebi.escience.scuflui.ScuflSVGDiagram;
import org.embl.ebi.escience.scuflui.TavernaIcons;
import org.embl.ebi.escience.scuflui.WorkflowInputMapBuilder;
import org.embl.ebi.escience.scuflui.WorkflowInputPanelFactory;
import org.embl.ebi.escience.scuflui.shared.ShadedLabel;

/**
 * Provide input for running a workflow. Supports loading/saving input
 * documents, loading individual inputs from files, and manually editing list of
 * input values.
 * <p>
 * This is designed as an improvement of
 * {@link org.embl.ebi.escience.scuflui.WorkflowInputPanel} for Taverna 1.6, but
 * backwards compatible for 1.5.2.x inclusion in the executeremotely plugin
 * 
 * @author Stian Soiland
 */
public class WorkflowInputPanel extends JPanel {

	public ScuflModel model = null;

	public DiagramAndInputs diagramAndInputs;

	public Header header;

	public RunButtons runButtons;

	public Action runAction = new RunAction();

	public WorkflowInputPanel(ScuflModel model) {
		super(new BorderLayout());
		this.model = model;
		init();
	}

	public void init() {
		makeGuiElements();
		addGuiElements();
	}

	public void makeGuiElements() {
		header = new Header();
		diagramAndInputs = new DiagramAndInputs();
		runButtons = new RunButtons();
	}

	public void addGuiElements() {
		removeAll();
		add(header, BorderLayout.NORTH);
		add(diagramAndInputs, BorderLayout.CENTER);
		add(runButtons, BorderLayout.SOUTH);
	}

	public Map<String, DataThing> getInputs() {
		return diagramAndInputs.getInputs();
	}

	public void runWorkflow(ScuflModel model, Map<String, DataThing> inputs) {
		WorkflowInputPanelFactory.executeWorkflow(model, inputs);
	}

	public final class RunAction extends AbstractAction {
		public RunAction() {
			super("Run workflow", TavernaIcons.runIcon);
		}

		public void actionPerformed(ActionEvent e) {
			Map<String, DataThing> inputs = diagramAndInputs.inputs.getInputs();
			runWorkflow(model, inputs);
		}
	}

	public class Header extends JPanel {
		public JPanel header = makeHeader();

		public Header() {
			super(new BorderLayout());
			add(header, BorderLayout.NORTH);
		}

		public JPanel makeHeader() {
			return new ShadedLabel("Run workflow: "
				+ model.getDescription().getTitle(), ShadedLabel.TAVERNA_GREEN);
		}

		public JToolBar globalToolbar() {
			JToolBar toolbar = new JToolBar();
			JButton loadInputDocButton =
				new JButton("Load Input Doc", TavernaIcons.openIcon);
			JButton saveInputDocButton =
				new JButton("Save Input Doc", TavernaIcons.saveIcon);
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

		public Inputs inputs = new Inputs();

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
			if (!initialDividerSet) {
				setDividerLocation(0.3);
				initialDividerSet = true;
			}
			return super.getDividerLocation();
		}

		public Map<String, DataThing> getInputs() {
			return inputs.getInputs();
		}
	}

	public class DescriptionAndDiagram extends JPanel {
		// JLabel description = new JLabel(model.getDescription().getText());
		ScuflSVGDiagram diagram = new ScuflSVGDiagram(false, false);

		public DescriptionAndDiagram() {
			super(new BorderLayout());
			// FIXME: make description wrap lines
			JTextArea description =
				new JTextArea(model.getDescription().getText());
			description.setEditable(false);
			description.setLineWrap(true);
			description.setOpaque(false);
			description.setWrapStyleWord(true);
			description.setFont(Font.getFont("Dialog"));
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

			JLabel title =
				new JLabel("<html><b>" + port.getName() + "</b> "
					+ port.getMetadata().getFirstMIMEType() + "</html>");
			title.setAlignmentX(Component.LEFT_ALIGNMENT);
			add(title);

			String description = port.getMetadata().getDescription();
			if (!description.equals("")) {
				JTextArea descriptionArea = new JTextArea(description);
				descriptionArea.setEditable(false);
				descriptionArea.setLineWrap(true);
				descriptionArea.setOpaque(false);
				descriptionArea.setWrapStyleWord(true);
				descriptionArea.setAlignmentX(Component.LEFT_ALIGNMENT);
				descriptionArea.setFont(Font.getFont("Dialog"));
				add(descriptionArea);
			}
		}
	}

	public class Inputs extends JPanel {
		public WorkflowInputMapBuilder builder;

		public Inputs() {
			super();
			setLayout(new GridBagLayout());
			GridBagConstraints c = new GridBagConstraints();
			c.gridx = 0;
			c.gridy = GridBagConstraints.RELATIVE;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.weightx = 0.9;
			c.anchor = GridBagConstraints.FIRST_LINE_START;
			add(new ShadedLabel("Inputs", ShadedLabel.TAVERNA_BLUE), c);
			builder = new WorkflowInputMapBuilder();
			// FIXME: also call detachFromModel
			c.fill = GridBagConstraints.BOTH;
			c.weighty = 0.1;
			add(builder, c);
			builder.attachToModel(model);
		}

		public Map<String, DataThing> getInputs() {
			return builder.bakeInputMap();
		}

	}

	public class RunButtons extends JPanel {

		public RunButtons() {
			addButtons();
		}

		public void addButtons() {
			removeAll();
			add(new JButton(runAction), BorderLayout.EAST);
		}

	}

	public void setInputs(Map<String, DataThing> inputs)
		throws InputsNotMatchingException {
		diagramAndInputs.inputs.builder.setWorkflowInputs(inputs);
	}
}
