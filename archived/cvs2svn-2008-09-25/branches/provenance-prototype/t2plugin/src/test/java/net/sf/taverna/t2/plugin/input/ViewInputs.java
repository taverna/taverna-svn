package net.sf.taverna.t2.plugin.input;

import java.util.List;
import java.util.Map;

import javax.swing.JDialog;

import net.sf.taverna.t2.cloudone.datamanager.DataFacade;
import net.sf.taverna.t2.cloudone.datamanager.DataManager;
import net.sf.taverna.t2.cloudone.datamanager.NotFoundException;
import net.sf.taverna.t2.cloudone.datamanager.RetrievalException;
import net.sf.taverna.t2.cloudone.datamanager.memory.InMemoryDataManager;
import net.sf.taverna.t2.cloudone.identifier.EntityIdentifier;
import net.sf.taverna.t2.invocation.InvocationContext;
import net.sf.taverna.t2.plugin.input.InputComponent.InputComponentCallback;
import net.sf.taverna.t2.provenance.ProvenanceConnector;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.DataflowInputPort;
import net.sf.taverna.t2.workflowmodel.EditException;
import net.sf.taverna.t2.workflowmodel.Edits;
import net.sf.taverna.t2.workflowmodel.EditsRegistry;

public class ViewInputs {

	private static InvocationContext context;
	/**
	 * @param args
	 * @throws EditException
	 */
	public static void main(String[] args) throws EditException {
		final DataManager dataManager = new InMemoryDataManager();
		context = new InvocationContext() {

			public DataManager getDataManager() {
				return dataManager;
			}

			public ProvenanceConnector getProvenanceManager() {
				// TODO Auto-generated method stub
				return null;
			}
			
		};
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
		InputComponent inputComp = new InputComponent(inputPorts, dataStuff,context);
		dialog.add(inputComp);
		dialog.setVisible(true);
	}

	public class DataStuff implements InputComponentCallback<DataflowInputPort> {

		public String getButtonText() {
			return "run this stuff";
		}

		public void invoke(Map<DataflowInputPort, EntityIdentifier> entities) {
			System.out.println("hello");
			DataFacade facade = new DataFacade(context.getDataManager());
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
