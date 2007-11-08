package net.sf.taverna.t2.plugin;

import javax.swing.JComponent;

import net.sf.taverna.t2.annotation.AbstractAnnotatedThing;
import net.sf.taverna.t2.cloudone.datamanager.NotFoundException;
import net.sf.taverna.t2.cloudone.datamanager.RetrievalException;
import net.sf.taverna.t2.cloudone.entity.gui.EntityViewer;
import net.sf.taverna.t2.facade.ResultListener;
import net.sf.taverna.t2.invocation.WorkflowDataToken;
import net.sf.taverna.t2.workflowmodel.DataflowOutputPort;
import net.sf.taverna.t2.workflowmodel.Datalink;
import net.sf.taverna.t2.workflowmodel.EditException;
import net.sf.taverna.t2.workflowmodel.Edits;
import net.sf.taverna.t2.workflowmodel.EventHandlingInputPort;
import net.sf.taverna.t2.workflowmodel.Port;
import net.sf.taverna.t2.workflowmodel.impl.ContextManager;
import net.sf.taverna.t2.workflowmodel.impl.EditsImpl;

import org.apache.log4j.Logger;

public class OutputHandler extends AbstractAnnotatedThing<Port> implements
		EventHandlingInputPort {

	private static final Logger logger = Logger.getLogger(OutputHandler.class);

	private final Edits edits = new EditsImpl();

	private DataflowOutputPort dataflowOutputPort;

	private JComponent resultsComponent;

	private ResultListener resultListener;

	private Datalink link;

	private OutputHandler(DataflowOutputPort dataflowOutputPort,
			JComponent resultsComponent, ResultListener resultListener) {
		this.dataflowOutputPort = dataflowOutputPort;
		this.resultsComponent = resultsComponent;
		this.resultListener = resultListener;
	}

	public static void register(DataflowOutputPort dataflowOutputPort,
			JComponent resultsComponent, ResultListener resultListener)
			throws EditException {
		OutputHandler outputHandler = new OutputHandler(dataflowOutputPort,
				resultsComponent, resultListener);
		resultsComponent.setName(dataflowOutputPort.getName());
		outputHandler.link = outputHandler.edits.createDatalink(
				dataflowOutputPort, outputHandler);
		outputHandler.edits.getConnectDatalinkEdit(outputHandler.link).doEdit();
	}

	public Datalink getIncomingLink() {
		return link;
	}

	public void receiveEvent(WorkflowDataToken token) {
		logger.info("Received Event : " + token);
		if (token.getIndex().length == 0) {
			try {
				resultsComponent.add(EntityViewer.getPanelForEntity(
						ContextManager.baseManager, token.getData()));
				resultsComponent.revalidate();
			} catch (RetrievalException e) {
				e.printStackTrace();
			} catch (NotFoundException e) {
				e.printStackTrace();
			}
		}
		resultListener.resultTokenProduced(token.getData(), token
				.getIndex(), getName());
	}

	public int getDepth() {
		return 0;
	}

	public String getName() {
		return dataflowOutputPort.getName();
	}

}
