package net.sf.taverna.t2.plugin.input;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.JDialog;
import javax.swing.JFrame;

import net.sf.taverna.t2.cloudone.datamanager.DataFacade;
import net.sf.taverna.t2.cloudone.datamanager.NotFoundException;
import net.sf.taverna.t2.cloudone.datamanager.RetrievalException;
import net.sf.taverna.t2.cloudone.datamanager.memory.InMemoryDataManager;
import net.sf.taverna.t2.cloudone.identifier.EntityIdentifier;
import net.sf.taverna.t2.plugin.input.InputComponent.InputComponentCallback;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.DataflowInputPort;
import net.sf.taverna.t2.workflowmodel.EditException;
import net.sf.taverna.t2.workflowmodel.Edits;
import net.sf.taverna.t2.workflowmodel.EditsRegistry;
import net.sf.taverna.t2.workflowmodel.impl.ContextManager;
import net.sf.taverna.t2.workflowmodel.impl.DataflowImpl;
import net.sf.taverna.t2.workflowmodel.impl.DataflowInputPortImpl;

public class ViewInputs {

	/**
	 * @param args
	 * @throws EditException
	 */
	public static void main(String[] args) throws EditException {
		ContextManager.baseManager = new InMemoryDataManager();
		new ViewInputs().run();
	}

	@SuppressWarnings("unchecked")
	private void run() throws EditException {
		// JFrame frame = new JFrame();
		JDialog dialog = new JDialog();
		DataStuff dataStuff = new DataStuff();
		Edits edits = EditsRegistry.getEdits();
		Dataflow f = edits.createDataflow();
		edits.getCreateDataflowInputPortEdit(f, "firstStuff", 1, 1).doEdit();
		edits.getCreateDataflowInputPortEdit(f, "secondThing", 1, 1).doEdit();
		List<? extends DataflowInputPort> inputPorts = f.getInputPorts();
		InputComponent inputComp = new InputComponent(inputPorts, dataStuff);
		dialog.add(inputComp);
		dialog.setVisible(true);
	}

	public class DataStuff implements InputComponentCallback<DataflowInputPort> {

		public String getButtonText() {
			return "run this stuff";
		}

		public void invoke(Map<DataflowInputPort, EntityIdentifier> entities) {
			System.out.println("hello");
			DataFacade facade = new DataFacade(ContextManager.baseManager);
			for (EntityIdentifier entity : entities.values()) {
				try {
					System.out.println(facade.resolve(entity));
				} catch (RetrievalException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (NotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

	}

}
