package uk.org.mygrid.logbook.ui.util;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import uk.org.mygrid.logbook.util.ProcessRunBean;

public class ProcessRunWithIterationsImpl extends ProcessRunImpl {
	
	private static Logger logger = Logger.getLogger(ProcessRunWithIterationsImpl.class);

    List<ProcessRun> iterations;

    ProcessRun currentIteration;
    
    boolean selected;

	private Set<ProcessRunBean> iterationBeans;

	public ProcessRunWithIterationsImpl() {
		// default
	}
	
    public ProcessRunWithIterationsImpl(ProcessRunBean processRunBean) throws ParseException {
		super(processRunBean);
		iterationBeans = processRunBean.getProcessIterations();
	}

	public List<ProcessRun> getIterations() {
		if (iterations == null && iterationBeans != null) {
			iterations = new ArrayList<ProcessRun>();
			for (ProcessRunBean processRunBean : iterationBeans) {
				try {
					if (processRunBean != null) {
						if (processRunBean.getProcessIterations() == null)
							iterations.add(new ProcessRunImpl(
									processRunBean));
						else
							iterations
									.add(new ProcessRunWithIterationsImpl(
											processRunBean));
					}
				} catch (ParseException e) {
					logger.warn(e);
				}
			}
		}
        return iterations;
    }

    public void setIterations(List<ProcessRun> iterations) {
        this.iterations = iterations;
    }

	public ProcessRun getCurrentIteration(){
		return currentIteration;
	}
    
	public void setCurrentIteration(int i){
		
		this.currentIteration = iterations.get(i);
		
	}

	public boolean isSelected() {
		return selected;
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
	}
    
    
}
