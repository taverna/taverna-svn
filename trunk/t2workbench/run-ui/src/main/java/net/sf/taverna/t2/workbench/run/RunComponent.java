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
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.DataflowInputPort;

import org.springframework.context.ApplicationContext;

public class RunComponent extends JSplitPane {

	private Dataflow dataflow;
	
	private Map<DataflowInputPort, T2Reference> inputs;
	
	private Date date;

	private MonitorViewComponent monitorViewComponent;
	
	private ResultsComponent resultsComponent;
	
	private Observer<MonitorMessage> monitorObserver;

	private int results = 0;

	public RunComponent(Dataflow dataflow, Map<DataflowInputPort, T2Reference> inputs, Date date) {
		this.dataflow = dataflow;
		this.inputs = inputs;
		this.date = date;
		
		setOrientation(VERTICAL_SPLIT);
		
		monitorViewComponent = new MonitorViewComponent();
		setTopComponent(monitorViewComponent);
		
		resultsComponent = new ResultsComponent();
		setBottomComponent(resultsComponent);

		setDividerLocation(-1);
		monitorObserver = monitorViewComponent.setDataflow(dataflow);
	}

	public void run() {
		
		InvocationContext context = createContext();

//		resultsComponent.setContext(context);
		MonitorManager.getInstance().addObserver(monitorObserver);
		// Use the empty context by default to root this facade on the monitor
		// tree
		WorkflowInstanceFacade facade = new WorkflowInstanceFacadeImpl(dataflow, context, "");
		facade.addResultListener(new ResultListener() {

			public void resultTokenProduced(WorkflowDataToken token,
					String portName) {
				if (token.getIndex().length == 0) {
					results++;
					if (results == dataflow.getOutputPorts().size()) {
//						resultComponent.deregister(facade);
//						facade.removeResultListener(this);
						MonitorManager.getInstance().removeObserver(monitorObserver);
						results = 0;
					}
				}
			}

		});
		determineOutputMimeTypes();
//		resultsComponent.register(facade);
		facade.fire();
		if (inputs != null) {
			for (Entry<DataflowInputPort, T2Reference> entry : inputs
					.entrySet()) {
				DataflowInputPort inputPort = entry.getKey();
				T2Reference identifier = entry.getValue();
				int[] index = new int[] {};
				try {
					facade.pushData(new WorkflowDataToken("", index,
							identifier, context), inputPort.getName());
				} catch (TokenOrderException e) {
					e.printStackTrace();
				}
			}
		}

	}
	
	private InvocationContext createContext() {

		InvocationContext context = new InvocationContext() {

			private ApplicationContext context = null;

			public ReferenceService getReferenceService() {
				if (context == null) {
					context = new RavenAwareClassPathXmlApplicationContext(
							"inMemoryReferenceServiceContext.xml");
				}
				ReferenceService referenceService = (ReferenceService) context
						.getBean("referenceService");
				return referenceService;
			}

			public <T> List<? extends T> getEntities(Class<T> arg0) {
				return new ArrayList<T>();
			}

		};
		return context;
	}


	private void determineOutputMimeTypes() {
		// FIXME get mime types from annotations on DataflowOutputPorts
		Map<String, List<String>> mimeTypeMap = new HashMap<String, List<String>>();
//		for (Port port : this.model.getWorkflowSinkPorts()) {
//			String name2 = port.getName();
//			String syntacticType = port.getSyntacticType();
//			List<String> typeList = port.getMetadata().getMIMETypeList();
//
//			if (!typeList.contains(syntacticType)) {
//				typeList.add(syntacticType);
//			}
//			mimeTypeMap.put(name2, typeList);
//		}
//		this.resultComponent.setOutputMimeTypes(mimeTypeMap);
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
		final RunComponent other = (RunComponent) obj;
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
	
}
