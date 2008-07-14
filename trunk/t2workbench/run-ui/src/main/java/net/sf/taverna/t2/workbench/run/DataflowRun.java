package net.sf.taverna.t2.workbench.run;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JSplitPane;

import net.sf.taverna.platform.spring.RavenAwareClassPathXmlApplicationContext;
import net.sf.taverna.t2.facade.ResultListener;
import net.sf.taverna.t2.facade.WorkflowInstanceFacade;
import net.sf.taverna.t2.facade.impl.WorkflowInstanceFacadeImpl;
import net.sf.taverna.t2.invocation.InvocationContext;
import net.sf.taverna.t2.invocation.TokenOrderException;
import net.sf.taverna.t2.invocation.WorkflowDataToken;
import net.sf.taverna.t2.lang.observer.Observer;
import net.sf.taverna.t2.monitor.MonitorManager;
import net.sf.taverna.t2.monitor.MonitorManager.MonitorMessage;
import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.workbench.views.monitor.MonitorViewComponent;
import net.sf.taverna.t2.workbench.views.results.ResultViewComponent;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.DataflowInputPort;
import net.sf.taverna.t2.workflowmodel.EditException;

import org.springframework.context.ApplicationContext;

public class DataflowRun {

	private Dataflow dataflow;
	
	private ReferenceService referenceService;
	
	private Map<String, T2Reference> inputs;
	
	private Date date;

	private MonitorViewComponent monitorViewComponent;
	
	private ResultViewComponent resultsComponent;
	
	private Observer<MonitorMessage> monitorObserver;

	private int results = 0;

	public DataflowRun(Dataflow dataflow, ReferenceService referenceService, Map<String, T2Reference> inputs, Date date) {
		this.dataflow = dataflow;
		this.referenceService = referenceService;
		this.inputs = inputs;
		this.date = date;		
		monitorViewComponent = new MonitorViewComponent();
		resultsComponent = new ResultViewComponent();
	}

	public void run() {
		
		monitorObserver = monitorViewComponent.setDataflow(dataflow);
		InvocationContext context = createContext();

//		resultsComponent.setContext(context);
		MonitorManager.getInstance().addObserver(monitorObserver);
		// Use the empty context by default to root this facade on the monitor
		// tree
		final WorkflowInstanceFacadeImpl facade = new WorkflowInstanceFacadeImpl(dataflow, context, "");
		facade.addResultListener(new ResultListener() {

			public void resultTokenProduced(WorkflowDataToken token,
					String portName) {
				if (token.getIndex().length == 0) {
					System.out.println("Result for : " + portName);
					results++;
					if (results == dataflow.getOutputPorts().size()) {
//						resultsComponent.deregister(facade);
						facade.removeResultListener(this);
						MonitorManager.getInstance().removeObserver(monitorObserver);
						monitorObserver = null;
						facade.removeResultListener(this);
						results = 0;
					}
				}
			}

		});
		try {
			resultsComponent.register(facade);
		} catch (EditException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		facade.fire();
		if (inputs != null) {
			for (Entry<String, T2Reference> entry : inputs.entrySet()) {
				String portName = entry.getKey();
				T2Reference identifier = entry.getValue();
				int[] index = new int[] {};
				try {
					facade.pushData(new WorkflowDataToken("", index,
							identifier, context), portName);
				} catch (TokenOrderException e) {
					e.printStackTrace();
				}
			}
		}

	}
	
	private InvocationContext createContext() {

		InvocationContext context = new InvocationContext() {

			public ReferenceService getReferenceService() {
				return referenceService;
			}

			public <T> List<? extends T> getEntities(Class<T> arg0) {
				return new ArrayList<T>();
			}

		};
		return context;
	}


	@Override
	public String toString() {
		return dataflow.getLocalName() +  " " + DateFormat.getTimeInstance().format(date);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((dataflow == null) ? 0 : dataflow.getInternalIdentier().hashCode());
		result = prime * result + ((date == null) ? 0 : date.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final DataflowRun other = (DataflowRun) obj;
		if (dataflow == null) {
			if (other.dataflow != null)
				return false;
		} else if (!dataflow.getInternalIdentier().equals(other.dataflow.getInternalIdentier()))
			return false;
		if (date == null) {
			if (other.date != null)
				return false;
		} else if (!date.equals(other.date))
			return false;
		return true;
	}

	public Dataflow getDataflow() {
		return dataflow;
	}

	public void setDataflow(Dataflow dataflow) {
		this.dataflow = dataflow;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	/**
	 * Returns the monitorViewComponent.
	 *
	 * @return the monitorViewComponent
	 */
	public MonitorViewComponent getMonitorViewComponent() {
		return monitorViewComponent;
	}

	/**
	 * Returns the resultsComponent.
	 *
	 * @return the resultsComponent
	 */
	public ResultViewComponent getResultsComponent() {
		return resultsComponent;
	}
	
}
