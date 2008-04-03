package uk.org.mygrid.dataplaygroundui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;

import org.embl.ebi.escience.baclava.DataThing;
import org.embl.ebi.escience.scufl.DuplicatePortNameException;
import org.embl.ebi.escience.scufl.OutputPort;
import org.embl.ebi.escience.scufl.PortCreationException;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scuflui.InputsNotMatchingException;
import org.embl.ebi.escience.scuflui.WorkflowInputMapBuilder;
import org.embl.ebi.escience.scuflui.shared.ShadedLabel;
import org.embl.ebi.escience.scuflui.spi.UIComponentSPI;

import uk.org.mygrid.dataplayground.PlaygroundDataThing;

public class PlaygroundInputPanel extends JPanel implements UIComponentSPI {

	private static UIComponentSPI instance;

	public static UIComponentSPI getInstance() {
		if (instance == null) {
			instance = new PlaygroundInputPanel();
		}
		return instance;
	}

	private WorkflowInputMapBuilder builder;
	private ScuflModel model;
	private PlaygroundDataThing datathing;
	private ShadedLabel header;

	private PlaygroundRendererPanel playgroundRendererPanel;

	public PlaygroundInputPanel() {
		super();
		model = new ScuflModel();
		/**
		 * try { model.getWorkflowSourceProcessor() .addPort( new
		 * OutputPort(model .getWorkflowSourceProcessor(), "Data"));
		 * model.forceUpdate(); } catch (PortCreationException pce) {
		 * System.out.println("port Creation problem"); } catch
		 * (DuplicatePortNameException dpne) { System.out.println("port name
		 * exception"); }
		 */
		setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = GridBagConstraints.RELATIVE;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 0.9;
		c.anchor = GridBagConstraints.FIRST_LINE_START;
		header = new ShadedLabel("Data Editor", ShadedLabel.TAVERNA_BLUE);
		add(header, c);
		builder = new WorkflowInputMapBuilder();
		// FIXME: also call detachFromModel
		c.fill = GridBagConstraints.BOTH;
		c.weighty = 0.9;
		add(builder, c);
		builder.attachToModel(model);

		JButton setButton = new JButton(new AbstractAction("Set") {

			public void actionPerformed(ActionEvent arg0) {

				Map m = builder.bakeInputMap();
				Iterator i = m.values().iterator();
				if (i.hasNext()) {
					DataThing d = (DataThing) i.next();
					System.out.println(d.getDataObject() + " "
							+ d.getSyntacticType() + " "
							+ d.getMetadata().getFirstMIMEType());
					// playgroundRendererPanel.remove(datathing);
					datathing.setDataThing(d);
					playgroundRendererPanel.replace(datathing);
					playgroundRendererPanel.select(datathing);
				}

			}

		});
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.LAST_LINE_START;
		c.weighty = 0.1;
		add(setButton, c);

		playgroundRendererPanel = (PlaygroundRendererPanel) PlaygroundRendererPanel
				.getInstance();

	}

	public ImageIcon getIcon() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getName() {

		return "Data";
	}

	public void onDisplay() {
		// TODO Auto-generated method stub

	}

	public void onDispose() {
		// TODO Auto-generated method stub

	}

	public void reset() {

		model.clear();
		model.forceUpdate();
	}

	public void setData(PlaygroundDataThing d) {

		this.datathing = d;
		model = new ScuflModel();

		// System.out.println("DataObject = " +
		// d.getDataThing().getDataObject());
		// System.out.println("DataThing MIME type = " +
		// d.getDataThing().getMetadata().getFirstMIMEType());

		try {

			// create the input port for in the model
			model.getWorkflowSourceProcessor().addPort(
					new OutputPort(model.getWorkflowSourceProcessor(),
							datathing.getName()));

			// create a matching data map from the port to the data thing
			HashMap<String, DataThing> dataMap = new HashMap<String, DataThing>();
			dataMap.put(datathing.getName(), datathing.getDataThing());

			builder.attachToModel(model);
			builder.setWorkflowInputs(dataMap);
			builder.attachToModel(model);
			header = new ShadedLabel(datathing.getName(),
					ShadedLabel.TAVERNA_BLUE);

		} catch (PortCreationException pce) {
			System.out.println("port Creation problem");
		} catch (DuplicatePortNameException dpne) {
			System.out.println("port name exception");
		} catch (InputsNotMatchingException e) {
			System.out.println("Inputs not matching exception");
			e.printStackTrace();
		}

		this.repaint();
	}
}
