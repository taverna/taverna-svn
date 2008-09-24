package net.sf.taverna.t2.performance;

import java.io.File;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;

import net.sf.taverna.raven.repository.Repository;
import net.sf.taverna.raven.repository.impl.LocalRepository;
import net.sf.taverna.t2.cloudone.datamanager.AbstractDataManager;
import net.sf.taverna.t2.cloudone.datamanager.DataFacade;
import net.sf.taverna.t2.cloudone.datamanager.DataManager;
import net.sf.taverna.t2.cloudone.datamanager.memory.InMemoryDataManager;
import net.sf.taverna.t2.cloudone.identifier.EntityIdentifier;
import net.sf.taverna.t2.compatibility.WorkflowModelTranslator;
import net.sf.taverna.t2.compatibility.WorkflowTranslationException;
import net.sf.taverna.t2.facade.WorkflowInstanceFacade;
import net.sf.taverna.t2.invocation.InvocationContext;
import net.sf.taverna.t2.invocation.TokenOrderException;
import net.sf.taverna.t2.invocation.WorkflowDataToken;
import net.sf.taverna.t2.testing.CaptureResultsListener;
import net.sf.taverna.t2.testing.DataflowTimeoutException;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.DataflowInputPort;
import net.sf.taverna.t2.workflowmodel.impl.EditsImpl;

import org.embl.ebi.escience.baclava.DataThing;
import org.embl.ebi.escience.scufl.ConcurrencyConstraintCreationException;
import org.embl.ebi.escience.scufl.DataConstraintCreationException;
import org.embl.ebi.escience.scufl.DuplicateConcurrencyConstraintNameException;
import org.embl.ebi.escience.scufl.DuplicateProcessorNameException;
import org.embl.ebi.escience.scufl.MalformedNameException;
import org.embl.ebi.escience.scufl.ProcessorCreationException;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scufl.UnknownPortException;
import org.embl.ebi.escience.scufl.UnknownProcessorException;
import org.embl.ebi.escience.scufl.enactor.EnactorProxy;
import org.embl.ebi.escience.scufl.enactor.WorkflowEventAdapter;
import org.embl.ebi.escience.scufl.enactor.WorkflowEventListener;
import org.embl.ebi.escience.scufl.enactor.WorkflowInstance;
import org.embl.ebi.escience.scufl.enactor.WorkflowSubmissionException;
import org.embl.ebi.escience.scufl.enactor.event.WorkflowCompletionEvent;
import org.embl.ebi.escience.scufl.enactor.event.WorkflowFailureEvent;
import org.embl.ebi.escience.scufl.enactor.implementation.FreefluoEnactorProxy;
import org.embl.ebi.escience.scufl.enactor.implementation.WorkflowEventDispatcher;
import org.embl.ebi.escience.scufl.parser.XScuflFormatException;
import org.embl.ebi.escience.scufl.parser.XScuflParser;
import org.embl.ebi.escience.utils.TavernaSPIRegistry;


/**
 * Compares workflow execution times in T1 & T2
 * 
 * @author Ian Dunlop 
 * (with code borrowed from WorkflowLauncher & T2 plugin)
 *
 */
public class PerformanceTester {

	private AbstractDataManager dataManager;
	private DataFacade dataFacade;
	private InvocationContext context;
	private Dataflow dataflow;
	private String repository;
	private ScuflModel model;

	public PerformanceTester() {
	}

	public void init(InputStream inputStream, String repository) {
		System.setProperty("raven.eclipse", "true");
		this.repository = repository;
		dataManager = new InMemoryDataManager("namespace",
				Collections.EMPTY_SET);
		dataFacade = new DataFacade(dataManager);
		context = new InvocationContext() {
			public DataManager getDataManager() {
				return dataManager;
			}
		};
		Repository myRepository = LocalRepository.getRepository(new File(
				this.repository));
		TavernaSPIRegistry.setRepository(myRepository);
		model = new ScuflModel();
		try {
			XScuflParser.populate(inputStream,
					model, null);
		} catch (UnknownProcessorException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnknownPortException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ProcessorCreationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DataConstraintCreationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DuplicateProcessorNameException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MalformedNameException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ConcurrencyConstraintCreationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DuplicateConcurrencyConstraintNameException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (XScuflFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//		workflowLauncher = new WorkflowLauncher(model);
		try {
			dataflow = WorkflowModelTranslator.doTranslation(model);
			dataflow.checkValidity();
		} catch (WorkflowTranslationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public long runT1(int num, Map<String, DataThing> inputs) throws WorkflowSubmissionException {
		
		final Object lock = new Object();
		long totalTime = 0;
		long averageTime = 0;

		for (int i = 0; i < num; i++) {
			EnactorProxy enactor = FreefluoEnactorProxy.getInstance();
			
			final WorkflowInstance workflowInstance = enactor.compileWorkflow(
					model, inputs, null);
			long startTimeInMillis = Calendar.getInstance()
					.getTimeInMillis();
			WorkflowEventListener completionListener = new WorkflowEventAdapter() {
				public void workflowCompleted(WorkflowCompletionEvent e) {
					if (e.getWorkflowInstance() == workflowInstance) {
						synchronized (lock) {
							lock.notifyAll();
						}
					}
				}
				public void workflowFailed(WorkflowFailureEvent e) {
					if (e.getWorkflowInstance() == workflowInstance) {
						synchronized (lock) {
							lock.notifyAll();					
						}
					}
				}
			};
			
			try {
				WorkflowEventDispatcher.DISPATCHER.addListener(completionListener);

				try {
					workflowInstance.run();
					synchronized (lock) {
						try {						
							lock.wait();
						} catch (InterruptedException e) {
						}
					}
				} catch (Exception e) {
					
				}
			} finally {			
				WorkflowEventDispatcher.DISPATCHER
						.removeListener(completionListener);
				workflowInstance.destroy();
				long endTimeInMillis = Calendar.getInstance().getTimeInMillis();
				long execTime = endTimeInMillis - startTimeInMillis;
				totalTime = totalTime + execTime;
			}
		}
		averageTime = totalTime / num;
		return averageTime;
	}

	public long runT2(int num, Map<DataflowInputPort, EntityIdentifier> entities) {
		long totalTime = 0;
		long averageTime = 0;

		for (int i = 0; i < num; i++) {
			WorkflowInstanceFacade facade;
			facade = new EditsImpl().createWorkflowInstanceFacade(dataflow,
					context, "");
			CaptureResultsListener listener = new CaptureResultsListener(
					dataflow, dataFacade);
			facade.addResultListener(listener);
			long startTimeInMillis = Calendar.getInstance().getTimeInMillis();
			facade.fire();
			if (entities != null) {
				for (Entry<DataflowInputPort, EntityIdentifier> entry : entities
						.entrySet()) {
					DataflowInputPort inputPort = entry.getKey();
					EntityIdentifier identifier = entry.getValue();
					try {
						facade.pushData(new WorkflowDataToken("", new int[] {},
								identifier, context), inputPort.getName());
					} catch (TokenOrderException e) {
						e.printStackTrace();
					}
				}
			}
			try {
				waitForCompletion(listener);
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (DataflowTimeoutException e) {
				e.printStackTrace();
			}
			long endTimeInMillis = Calendar.getInstance().getTimeInMillis();
			long execTime = endTimeInMillis - startTimeInMillis;
			totalTime = totalTime + execTime;
		}
		averageTime = totalTime / num;
		return averageTime;
	}

	protected void waitForCompletion(CaptureResultsListener listener)
			throws InterruptedException, DataflowTimeoutException {
		waitForCompletion(listener, 30);
	}

	protected void waitForCompletion(CaptureResultsListener listener,
			int maxtimeSeconds) throws InterruptedException,
			DataflowTimeoutException {
		float time = 0;
		int maxTime = maxtimeSeconds * 1000;
		int interval = 100;
		while (!listener.isFinished()) {
			Thread.sleep(interval);
			time += interval;
			if (time > maxTime) {
				throw new DataflowTimeoutException("The max time of "
						+ maxtimeSeconds
						+ "s was exceed waiting for the results");
			}
		}
	}

}
